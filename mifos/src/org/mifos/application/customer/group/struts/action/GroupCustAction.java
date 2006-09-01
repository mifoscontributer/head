/**

 * GroupCustAction.java version: 1.0



 * Copyright (c) 2005-2006 Grameen Foundation USA

 * 1029 Vermont Avenue, NW, Suite 400, Washington DC 20005

 * All rights reserved.



 * Apache License
 * Copyright (c) 2005-2006 Grameen Foundation USA
 *

 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain
 * a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the

 * License.
 *
 * See also http://www.apache.org/licenses/LICENSE-2.0.html for an explanation of the license

 * and how it is applied.

 *

 */

package org.mifos.application.customer.group.struts.action;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.mifos.application.accounts.business.AccountBO;
import org.mifos.application.accounts.loan.business.LoanBO;
import org.mifos.application.accounts.savings.business.SavingsBO;
import org.mifos.application.customer.business.CustomFieldView;
import org.mifos.application.customer.business.CustomerPositionEntity;
import org.mifos.application.customer.business.CustomerPositionView;
import org.mifos.application.customer.client.util.helpers.ClientConstants;
import org.mifos.application.customer.exceptions.CustomerException;
import org.mifos.application.customer.group.business.GroupBO;
import org.mifos.application.customer.group.business.service.GroupBusinessService;
import org.mifos.application.customer.group.struts.actionforms.GroupCustActionForm;
import org.mifos.application.customer.group.util.helpers.CenterSearchInput;
import org.mifos.application.customer.group.util.helpers.GroupConstants;
import org.mifos.application.customer.struts.action.CustAction;
import org.mifos.application.customer.util.helpers.CustomerLevel;
import org.mifos.application.fees.business.FeeView;
import org.mifos.application.meeting.business.MeetingBO;
import org.mifos.application.util.helpers.ActionForwards;
import org.mifos.application.util.helpers.EntityType;
import org.mifos.framework.business.service.BusinessService;
import org.mifos.framework.business.service.ServiceFactory;
import org.mifos.framework.business.util.Address;
import org.mifos.framework.components.configuration.business.Configuration;
import org.mifos.framework.components.logger.LoggerConstants;
import org.mifos.framework.components.logger.MifosLogManager;
import org.mifos.framework.components.logger.MifosLogger;
import org.mifos.framework.exceptions.ApplicationException;
import org.mifos.framework.exceptions.PageExpiredException;
import org.mifos.framework.exceptions.PersistenceException;
import org.mifos.framework.exceptions.ServiceException;
import org.mifos.framework.exceptions.SystemException;
import org.mifos.framework.security.util.UserContext;
import org.mifos.framework.struts.tags.DateHelper;
import org.mifos.framework.util.helpers.BusinessServiceName;
import org.mifos.framework.util.helpers.Constants;
import org.mifos.framework.util.helpers.SessionUtils;
import org.mifos.framework.util.helpers.TransactionDemarcate;

public class GroupCustAction extends CustAction {

	private MifosLogger logger = MifosLogManager.getLogger(LoggerConstants.GROUP_LOGGER);
	
	@Override
	protected boolean skipActionFormToBusinessObjectConversion(String method) {
		return true;
	}

	@Override
	protected BusinessService getService() throws ServiceException {
		return getGroupBusinessService();
	}
	
	public ActionForward hierarchyCheck(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		ActionForwards actionForward = null;
		boolean isCenterHierarchyExists = Configuration.getInstance().getCustomerConfig(
				getUserContext(request).getBranchId())
				.isCenterHierarchyExists(); 
		if(isCenterHierarchyExists){
			CenterSearchInput searchInputs = new CenterSearchInput(getUserContext(request).getBranchId(), GroupConstants.CREATE_NEW_GROUP);
			SessionUtils.setAttribute(GroupConstants.CENTER_SEARCH_INPUT, searchInputs, request.getSession());
			actionForward = ActionForwards.loadCenterSearch;
		}else
			actionForward = ActionForwards.loadCreateGroup;
		
		return mapping.findForward(actionForward.toString());
	}
	
	public ActionForward chooseOffice(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		return mapping.findForward(ActionForwards.chooseOffice_success
				.toString());
	}
	
	@TransactionDemarcate (saveToken = true)
	public ActionForward load(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		GroupCustActionForm actionForm = (GroupCustActionForm) form;
		doCleanUp(actionForm, request);
		boolean isCenterHierarchyExists = Configuration.getInstance().getCustomerConfig(
				getUserContext(request).getBranchId())
				.isCenterHierarchyExists();
		if(isCenterHierarchyExists){
			actionForm.setParentCustomer(getCustomerBusinessService().getCustomer(Integer
					.valueOf(actionForm.getCenterId())));
			actionForm.setOfficeId(actionForm.getParentCustomer().getOffice().getOfficeId().toString());
		}
		loadCreateMasterData(actionForm, request, isCenterHierarchyExists);

		SessionUtils.setAttribute(GroupConstants.CENTER_HIERARCHY_EXIST,
				isCenterHierarchyExists, request);		
		return mapping.findForward(ActionForwards.load_success.toString());
	}
	
	@TransactionDemarcate(joinToken = true)
	public ActionForward loadMeeting(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		return mapping.findForward(ActionForwards.loadMeeting_success
				.toString());
	}
	
	@TransactionDemarcate(joinToken = true)
	public ActionForward preview(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		return mapping.findForward(ActionForwards.preview_success
				.toString());
	}
	
	@TransactionDemarcate(joinToken = true)
	public ActionForward previous(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		return mapping.findForward(ActionForwards.previous_success
				.toString());
	}
	
	@TransactionDemarcate(validateAndResetToken = true)
	public ActionForward create(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		GroupCustActionForm actionForm = (GroupCustActionForm) form;
		boolean isCenterHierarchyExists = (Boolean)SessionUtils.getAttribute(GroupConstants.CENTER_HIERARCHY_EXIST, request);
		
		GroupBO group ;
		if(isCenterHierarchyExists)
			group = createGroupWithCenter(actionForm, request);
		else
			group = createGroupWithoutCenter(actionForm, request);
		
		group.save();
		actionForm.setCustomerId(group.getCustomerId().toString());
		actionForm.setGlobalCustNum(group.getGlobalCustNum());
		return mapping.findForward(ActionForwards.create_success.toString());
	}	

	@TransactionDemarcate(saveToken = true)
	public ActionForward get(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws PageExpiredException, CustomerException {
		logger.debug("In GroupCustAction get method " );
		GroupCustActionForm actionForm = (GroupCustActionForm) form;
		GroupBO groupBO;
		try {
			groupBO = (GroupBO) getGroupBusinessService().getGroupBySystemId(
					actionForm.getGlobalCustNum());
		} catch (ServiceException se) {
			throw new CustomerException(se);
		}
		groupBO.setUserContext(getUserContext(request));
		groupBO.getCustomerStatus().setLocaleId(
				getUserContext(request).getLocaleId());
		loadMasterDataForDetailsPage(request, groupBO, getUserContext(request)
				.getLocaleId());
		setLocaleForMasterEntities(groupBO, getUserContext(request)
				.getLocaleId());
		SessionUtils.removeAttribute(Constants.BUSINESS_KEY, request);
		SessionUtils.setAttribute(Constants.BUSINESS_KEY, groupBO, request);
		logger.debug("Exiting GroupCustAction get method " );
		return mapping.findForward(ActionForwards.get_success.toString());
	}

	@TransactionDemarcate(joinToken = true)
	public ActionForward manage(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		clearActionForm((GroupCustActionForm) form);
		GroupBO group = (GroupBO) SessionUtils.getAttribute(
				Constants.BUSINESS_KEY, request);
		logger.debug("Entering GroupCustAction manage method and customer id: "+ group.getGlobalCustNum());
		GroupBO groupBO = (GroupBO) getCustomerBusinessService().getBySystemId(
				group.getGlobalCustNum(), CustomerLevel.GROUP.getValue());
		group = null;
		SessionUtils.setAttribute(Constants.BUSINESS_KEY, groupBO, request);

		loadUpdateMasterData(request,groupBO);
		setValuesInActionForm((GroupCustActionForm) form, request);
		logger.debug("Exiting GroupCustAction manage method ");
		return mapping.findForward(ActionForwards.manage_success.toString());
	}

	@TransactionDemarcate(joinToken = true)
	public ActionForward previewManage(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		return mapping.findForward(ActionForwards.previewManage_success
				.toString());
	}

	@TransactionDemarcate(joinToken = true)
	public ActionForward previousManage(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		return mapping.findForward(ActionForwards.previousManage_success
				.toString());
	}

	@TransactionDemarcate(validateAndResetToken = true)
	public ActionForward update(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		GroupBO group = (GroupBO) SessionUtils.getAttribute(
				Constants.BUSINESS_KEY, request);
		GroupCustActionForm actionForm = (GroupCustActionForm) form;

		Date trainedDate = null; 
		if(actionForm.getTrainedDate()!=null)
			trainedDate = getDateFromString(actionForm.getTrainedDate(), getUserContext(request)
				.getPereferedLocale());
		
		group.update(getUserContext(request),actionForm.getDisplayName(), actionForm.getLoanOfficerIdValue(), actionForm.getExternalId(),actionForm.getTrainedValue(),trainedDate, actionForm.getAddress(), actionForm.getCustomFields(), actionForm.getCustomerPositions());
		return mapping.findForward(ActionForwards.update_success.toString());
	}
	
	@TransactionDemarcate(validateAndResetToken = true)
	public ActionForward cancel(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String forward = null;
		GroupCustActionForm actionForm = (GroupCustActionForm) form;
		String fromPage = actionForm.getInput();
		if (fromPage.equals(GroupConstants.MANAGE_GROUP) || fromPage.equals(GroupConstants.PREVIEW_MANAGE_GROUP)){
			forward=ActionForwards.cancelEdit_success.toString();
		}
		return mapping.findForward(forward);
	}

	private void loadMasterDataForDetailsPage(HttpServletRequest request,
			GroupBO groupBO, Short localeId) throws PageExpiredException,
			CustomerException {
		SessionUtils.setAttribute(GroupConstants.IS_GROUP_LOAN_ALLOWED,
				Configuration.getInstance().getCustomerConfig(
						groupBO.getOffice().getOfficeId())
						.canGroupApplyForLoan(), request);
		SessionUtils.setAttribute(GroupConstants.CENTER_HIERARCHY_EXIST,
				Configuration.getInstance().getCustomerConfig(
						groupBO.getOffice().getOfficeId())
						.isCenterHierarchyExists(), request);
		SessionUtils.setAttribute(ClientConstants.LOANCYCLECOUNTER,
				getCustomerBusinessService().fetchLoanCycleCounter(
						groupBO.getCustomerId()), request);
		List<LoanBO> loanAccounts = groupBO.getOpenLoanAccounts();
		List<SavingsBO> savingsAccounts = groupBO.getOpenSavingAccounts();
		setLocaleIdToLoanStatus(loanAccounts, localeId);
		setLocaleIdToSavingsStatus(savingsAccounts, localeId);
		SessionUtils.setAttribute(GroupConstants.GROUPLOANACCOUNTSINUSE,
				loanAccounts, request);
		SessionUtils.setAttribute(GroupConstants.GROUPSAVINGSACCOUNTSINUSE,
				savingsAccounts, request);
		try {
			SessionUtils
					.setAttribute(
							GroupConstants.CLIENT_LIST,
							groupBO
									.getAllCustomerOtherThanCancelledAndClosed(CustomerLevel.CLIENT),
							request);
			loadCustomFieldDefinitions(EntityType.GROUP, request);
		} catch (PersistenceException pe) {
			throw new CustomerException(pe);
		} catch (SystemException se) {
			throw new CustomerException(se);
		} catch (ApplicationException ae) {
			throw new CustomerException(ae);
		}
	}

	private void loadCreateMasterData(GroupCustActionForm actionForm,
			HttpServletRequest request, boolean isCenterHierarchyExists) throws Exception {
		loadCreateCustomFields(actionForm, EntityType.GROUP, request);
		loadFees(actionForm, request);
		if(!isCenterHierarchyExists)
			loadLoanOfficers(actionForm.getOfficeIdValue(), request);		
		loadFormedByPersonnel(actionForm.getOfficeIdValue(), request);		
	}
	
	private void setLocaleIdToLoanStatus(List<LoanBO> accountList,
			Short localeId) {
		for (LoanBO accountBO : accountList)
			setLocaleForAccount((AccountBO) accountBO, localeId);
	}

	private void setLocaleIdToSavingsStatus(List<SavingsBO> accountList,
			Short localeId) {
		for (SavingsBO accountBO : accountList)
			setLocaleForAccount((AccountBO) accountBO, localeId);
	}

	private void setLocaleForAccount(AccountBO account, Short localeId) {
		account.getAccountState().setLocaleId(localeId);
	}

	private void setLocaleForMasterEntities(GroupBO groupBO, Short localeId) {
		for (CustomerPositionEntity customerPositionEntity : groupBO
				.getCustomerPositions())
			customerPositionEntity.getPosition().setLocaleId(localeId);
	}

	private GroupBusinessService getGroupBusinessService()
			throws ServiceException {
		return (GroupBusinessService) ServiceFactory.getInstance()
				.getBusinessService(BusinessServiceName.Group);
	}

	private void loadUpdateMasterData(HttpServletRequest request, GroupBO group)
			throws ApplicationException, SystemException {
		if (!Configuration.getInstance().getCustomerConfig(
				getUserContext(request).getBranchId())
				.isCenterHierarchyExists()) {
			loadLoanOfficers(group.getOffice().getOfficeId(), request);
		}
		loadCustomFieldDefinitions(EntityType.GROUP, request);
		loadPositions(request);
		loadClients(request,group);
	}

	private void setValuesInActionForm(GroupCustActionForm actionForm,
			HttpServletRequest request) throws Exception {
		GroupBO group = (GroupBO) SessionUtils.getAttribute(Constants.BUSINESS_KEY, request);
		if (group.getPersonnel() != null) {
			actionForm.setLoanOfficerId(group.getPersonnel().getPersonnelId()
					.toString());
		}
		actionForm.setDisplayName(group.getDisplayName());
		actionForm.setCustomerId(group.getCustomerId().toString());
		actionForm.setGlobalCustNum(group.getGlobalCustNum());
		actionForm.setExternalId(group.getExternalId());
		actionForm.setAddress(group.getAddress());
		actionForm.setCustomerPositions(createCustomerPositionViews(group
				.getCustomerPositions(), request));
		actionForm.setCustomFields(createCustomFieldViews(group
				.getCustomFields(), request));
		if (group.isTrained()) 
			  actionForm.setTrained(GroupConstants.TRAINED);
		else 
			 actionForm.setTrained(GroupConstants.NOT_TRAINED); 
		if(group.getTrainedDate() != null){
			  actionForm.setTrainedDate(DateHelper.getUserLocaleDate(getUserContext(request).getPereferedLocale(),
		      group.getTrainedDate().toString()));
		}
		 
	}

	private void doCleanUp(GroupCustActionForm actionForm,
			HttpServletRequest request) {
		clearActionForm(actionForm);
		SessionUtils.removeAttribute(GroupConstants.GROUP_MEETING, request.getSession());
	}
	
	private void clearActionForm(GroupCustActionForm actionForm) {
		actionForm.setDefaultFees(new ArrayList<FeeView>());
		actionForm.setAdditionalFees(new ArrayList<FeeView>());
		actionForm.setCustomerPositions(new ArrayList<CustomerPositionView>());
		actionForm.setCustomFields(new ArrayList<CustomFieldView>());
		actionForm.setAddress(new Address());
		actionForm.setDisplayName(null);
		actionForm.setMfiJoiningDate(null);
		actionForm.setGlobalCustNum(null);
		actionForm.setCustomerId(null);
		actionForm.setExternalId(null);
		actionForm.setLoanOfficerId(null);
		actionForm.setTrained(null);
		actionForm.setTrainedDate(null);
		actionForm.setFormedByPersonnel(null);
	}

	public ActionForward validate(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String method = (String) request.getAttribute("methodCalled");
		return mapping.findForward(method + "_failure");
	}
	
	private GroupBO createGroupWithoutCenter(GroupCustActionForm actionForm, HttpServletRequest request) throws Exception{
		UserContext userContext = getUserContext(request);
		Short personnelId = actionForm.getLoanOfficerIdValue()!=null ?actionForm.getLoanOfficerIdValue():userContext.getId();
		checkPermissionForCreate(actionForm.getStatusValue().getValue(),
				userContext, null, actionForm.getOfficeIdValue(), personnelId);
		List<CustomFieldView> customFields = actionForm.getCustomFields();
		convertCustomFieldDateToUniformPattern(customFields, userContext.getPereferedLocale());
		MeetingBO meeting = (MeetingBO) SessionUtils.getAttribute(
				GroupConstants.GROUP_MEETING, request.getSession());
		GroupBO group = new GroupBO(userContext, actionForm.getDisplayName(), actionForm.getStatusValue(),
				actionForm.getExternalId(), actionForm.isCustomerTrained(), actionForm.getTrainedDateValue(userContext.getPereferedLocale()),
				actionForm.getAddress(), customFields, actionForm.getFeesToApply(), actionForm.getFormedByPersonnelValue(), 
				actionForm.getOfficeIdValue(), meeting, actionForm.getLoanOfficerIdValue());
		return group;
	}

	private GroupBO createGroupWithCenter(GroupCustActionForm actionForm, HttpServletRequest request) throws Exception{
		UserContext userContext = getUserContext(request);
		checkPermissionForCreate(actionForm.getStatusValue().getValue(),
				userContext, null, actionForm.getParentCustomer().getOffice().getOfficeId(), 
				actionForm.getParentCustomer().getPersonnel().getPersonnelId());
		
		List<CustomFieldView> customFields = actionForm.getCustomFields();
		convertCustomFieldDateToUniformPattern(customFields, userContext.getPereferedLocale());
		
		GroupBO group = new GroupBO(userContext, actionForm.getDisplayName(), actionForm.getStatusValue(),
				actionForm.getExternalId(), actionForm.isCustomerTrained(), actionForm.getTrainedDateValue(userContext.getPereferedLocale()),
				actionForm.getAddress(), customFields, actionForm.getFeesToApply(), actionForm.getFormedByPersonnelValue(), actionForm.getParentCustomer());
		return group;
	}
}
