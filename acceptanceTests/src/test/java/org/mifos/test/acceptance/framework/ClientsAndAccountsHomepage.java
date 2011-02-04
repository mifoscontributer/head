/*
 * Copyright (c) 2005-2010 Grameen Foundation USA
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * See also http://www.apache.org/licenses/LICENSE-2.0.html for an
 * explanation of the license and how it is applied.
 */

package org.mifos.test.acceptance.framework;

import com.thoughtworks.selenium.Selenium;

import org.mifos.test.acceptance.framework.center.CreateCenterChooseOfficePage;
import org.mifos.test.acceptance.framework.center.MeetingParameters;
import org.mifos.test.acceptance.framework.client.ChooseOfficePage;
import org.mifos.test.acceptance.framework.client.ClientSearchResultsPage;
import org.mifos.test.acceptance.framework.client.ClientViewDetailsPage;
import org.mifos.test.acceptance.framework.client.CreateClientConfirmationPage;
import org.mifos.test.acceptance.framework.client.CreateClientEnterFamilyDetailsPage;
import org.mifos.test.acceptance.framework.client.CreateClientEnterMfiDataPage;
import org.mifos.test.acceptance.framework.client.CreateClientEnterPersonalDataPage;
import org.mifos.test.acceptance.framework.client.CreateClientPreviewDataPage;
import org.mifos.test.acceptance.framework.collectionsheet.CollectionSheetEntrySelectPage;
import org.mifos.test.acceptance.framework.group.CreateGroupSearchPage;
import org.mifos.test.acceptance.framework.group.GroupSearchPage;
import org.mifos.test.acceptance.framework.loan.CreateLoanAccountSearchPage;
import org.mifos.test.acceptance.framework.loan.CreateLoanAccountsSearchPage;
import org.mifos.test.acceptance.framework.questionnaire.QuestionResponsePage;
import org.mifos.test.acceptance.framework.savings.CreateSavingsAccountSearchPage;
import org.mifos.test.acceptance.framework.testhelpers.FormParametersHelper;
import org.mifos.test.acceptance.util.StringUtil;

import java.util.Map;

public class ClientsAndAccountsHomepage extends AbstractPage {

    public ClientsAndAccountsHomepage() {
        super();
    }

    public ClientsAndAccountsHomepage(Selenium selenium) {
        super(selenium);
        verifyPage("ClientsAccounts");
    }

    // TODO fix these 5 following methods. They all belong in a navigation helper.
    public CollectionSheetEntrySelectPage navigateToEnterCollectionSheetDataUsingLeftMenu() {
        selenium.click("id=menu.link.label.enter.label.collectionsheet.label.data");
        waitForPageToLoad();
        return new CollectionSheetEntrySelectPage(selenium);
    }

    public CreateLoanAccountsSearchPage navigateToCreateMultipleLoanAccountsUsingLeftMenu() {
        selenium.click("menu.link.label.createmultipleloanaccountsprefix.loan.label.createmultipleloanaccountssuffix");
        waitForPageToLoad();
        return new CreateLoanAccountsSearchPage(selenium);
    }

    public CreateLoanAccountSearchPage navigateToCreateLoanAccountUsingLeftMenu() {
        selenium.click("menu.link.label.createloanaccountprefix.loan.label.createloanaccountsuffix");
        waitForPageToLoad();
        return new CreateLoanAccountSearchPage(selenium);
    }

    public CreateSavingsAccountSearchPage navigateToCreateSavingsAccountUsingLeftMenu() {
        selenium.click("menu.link.label.createsavingsaccountprefix.savings.label.createsavingsaccountsuffix");
        waitForPageToLoad();
        return new CreateSavingsAccountSearchPage(selenium);
    }

    public CreateCenterChooseOfficePage navigateToCreateNewCenterPage() {
        selenium.click("menu.link.label.createnew.center");
        waitForPageToLoad();
        return new CreateCenterChooseOfficePage(selenium);
    }

    public GroupSearchPage navigateToCreateNewClientPage() {
        selenium.click("menu.link.label.createnew.client");
        waitForPageToLoad();
        return new GroupSearchPage(selenium);
    }

    public CreateGroupSearchPage navigateToCreateNewGroupPage() {
        selenium.click("menu.link.label.createnew.group");
        waitForPageToLoad();
        return new CreateGroupSearchPage(selenium);
    }

    // TODO belongs in a helper
    public ClientViewDetailsPage createClientAndVerify(String loanOfficer, String officeName) {
        CreateClientEnterPersonalDataPage.SubmitFormParameters formParameters = createClient(loanOfficer, officeName);
        return navigateToClientViewDetails(formParameters);
    }

    public CreateClientEnterPersonalDataPage.SubmitFormParameters createClient(String loanOfficer, String officeName) {
        CreateClientEnterPersonalDataPage clientPersonalDataPage = navigateToPersonalDataPage(officeName);
        CreateClientEnterPersonalDataPage.SubmitFormParameters formParameters = FormParametersHelper.getClientEnterPersonalDataPageFormParameters();
        clientPersonalDataPage=clientPersonalDataPage.create(formParameters);
        clientPersonalDataPage.submitAndGotoCreateClientEnterMfiDataPage();
        navigateToConfirmationPage(loanOfficer);
        return formParameters;
    }

    public ClientViewDetailsPage createClientWithQuestionGroups(String loanOfficer, String officeName, Map<String, String> choiceTags, String answer) {
        CreateClientEnterPersonalDataPage clientPersonalDataPage = navigateToPersonalDataPage(officeName);
        CreateClientEnterPersonalDataPage.SubmitFormParameters formParameters = FormParametersHelper.getClientEnterPersonalDataPageFormParameters();
        clientPersonalDataPage = clientPersonalDataPage.create(formParameters);
        QuestionResponsePage questionResponsePage = clientPersonalDataPage.submitAndGotoCaptureQuestionResponsePage();
        questionResponsePage.populateTextAnswer("name=questionGroups[0].sectionDetails[0].questions[0].value", answer);
        questionResponsePage.populateSmartSelect("txtListSearch", choiceTags);
        questionResponsePage.navigateToNextPage();
        navigateToConfirmationPage(loanOfficer);
        return navigateToClientViewDetails(formParameters);
    }

    private ClientViewDetailsPage navigateToClientViewDetails(CreateClientEnterPersonalDataPage.SubmitFormParameters formParameters) {
        ClientViewDetailsPage clientViewDetailsPage = new CreateClientConfirmationPage(selenium).navigateToClientViewDetailsPage();
        clientViewDetailsPage.verifyName(formParameters.getFirstName() + " " + formParameters.getLastName());
        clientViewDetailsPage.verifyDateOfBirth(formParameters.getDateOfBirthDD(), formParameters.getDateOfBirthMM(), formParameters.getDateOfBirthYYYY());
        clientViewDetailsPage.verifySpouseFather(formParameters.getSpouseFirstName() + " " + formParameters.getSpouseLastName());
        clientViewDetailsPage.verifyHandicapped(formParameters.getHandicapped());
        return clientViewDetailsPage;
    }

    private CreateClientConfirmationPage navigateToConfirmationPage(String loanOfficer) {
        CreateClientEnterMfiDataPage.SubmitFormParameters mfiFormParameters = new CreateClientEnterMfiDataPage.SubmitFormParameters();
        mfiFormParameters.setLoanOfficerId(loanOfficer);

        MeetingParameters meetingFormParameters = new MeetingParameters();
        meetingFormParameters.setWeekFrequency("1");
        meetingFormParameters.setWeekDay(MeetingParameters.WEDNESDAY);
        meetingFormParameters.setMeetingPlace("Bangalore");

        mfiFormParameters.setMeeting(meetingFormParameters);

        CreateClientPreviewDataPage clientPreviewDataPage = new CreateClientEnterMfiDataPage(selenium).submitAndGotoCreateClientPreviewDataPage(mfiFormParameters);
        CreateClientConfirmationPage clientConfirmationPage = clientPreviewDataPage.submit();
        clientConfirmationPage.verifyPage();
        return clientConfirmationPage;
    }

    private CreateClientEnterPersonalDataPage navigateToPersonalDataPage(String officeName) {
        GroupSearchPage groupSearchPage = navigateToCreateNewClientPage();
        ChooseOfficePage chooseOfficePage = groupSearchPage.navigateToCreateClientWithoutGroupPage();
        return chooseOfficePage.chooseOffice(officeName);
    }

    public CreateClientEnterPersonalDataPage createClient(String officeName, String dd, String mm, String yy){
        CreateClientEnterPersonalDataPage clientPersonalDataPage = navigateToPersonalDataPage(officeName);
        CreateClientEnterPersonalDataPage.SubmitFormParameters formParameters = new CreateClientEnterPersonalDataPage.SubmitFormParameters();
        formParameters.setSalutation(CreateClientEnterPersonalDataPage.SubmitFormParameters.MRS);
        formParameters.setFirstName("test");
        formParameters.setLastName("Customer" + StringUtil.getRandomString(8));
        formParameters.setDateOfBirthDD(dd);
        formParameters.setDateOfBirthMM(mm);
        formParameters.setDateOfBirthYYYY(yy);
        formParameters.setGender(CreateClientEnterPersonalDataPage.SubmitFormParameters.FEMALE);
        formParameters.setPovertyStatus(CreateClientEnterPersonalDataPage.SubmitFormParameters.POOR);
        formParameters.setHandicapped("Yes");
        formParameters.setSpouseNameType(CreateClientEnterPersonalDataPage.SubmitFormParameters.FATHER);
        formParameters.setSpouseFirstName("father");
        formParameters.setSpouseLastName("lastname" + StringUtil.getRandomString(8));
        return clientPersonalDataPage.create(formParameters);
    }


    public CreateClientEnterPersonalDataPage createClientForFamilyInfo(String officeName, String dd, String mm, String yy) {
        CreateClientEnterPersonalDataPage clientPersonalDataPage = navigateToPersonalDataPage(officeName);
         CreateClientEnterPersonalDataPage.SubmitFormParameters formParameters = new CreateClientEnterPersonalDataPage.SubmitFormParameters();
         formParameters.setLastName("Customer" + StringUtil.getRandomString(8));
         formParameters.setSalutation(CreateClientEnterPersonalDataPage.SubmitFormParameters.MRS);
         formParameters.setFirstName("test");
         formParameters.setDateOfBirthYYYY(yy);
         formParameters.setLastName("Customer" + StringUtil.getRandomString(8));
         formParameters.setDateOfBirthDD(dd);
         formParameters.setDateOfBirthMM(mm);
         formParameters.setGender(CreateClientEnterPersonalDataPage.SubmitFormParameters.FEMALE);
         formParameters.setPovertyStatus(CreateClientEnterPersonalDataPage.SubmitFormParameters.POOR);
         formParameters.setHandicapped("Yes");
         return clientPersonalDataPage.createWithoutSpouse(formParameters);
     }

    public CreateClientEnterFamilyDetailsPage createFamily(String fname, String lname, String dd, String mm, String yy, CreateClientEnterFamilyDetailsPage page) {
         CreateClientEnterFamilyDetailsPage.SubmitFormParameters formParameters = new CreateClientEnterFamilyDetailsPage.SubmitFormParameters();
         formParameters.setRelationship(CreateClientEnterFamilyDetailsPage.SubmitFormParameters.FATHER);
         formParameters.setFirstName(fname);
         formParameters.setLastName(lname);
         formParameters.setDateOfBirthDD(dd);
         formParameters.setDateOfBirthMM(mm);
         formParameters.setDateOfBirthYY(yy);
         formParameters.setGender(CreateClientEnterFamilyDetailsPage.SubmitFormParameters.MALE);
         formParameters.setLivingStatus(CreateClientEnterFamilyDetailsPage.SubmitFormParameters.TOGETHER);
         return page.createMember(formParameters);
    }

    public CreateClientEnterFamilyDetailsPage createFamilyWithoutLookups(Integer relation,Integer gender, Integer livingStatus,CreateClientEnterFamilyDetailsPage page) {
        CreateClientEnterFamilyDetailsPage.SubmitFormParameters formParameters = new CreateClientEnterFamilyDetailsPage.SubmitFormParameters();
        formParameters.setRelationship(relation);
        formParameters.setFirstName("fname");
        formParameters.setLastName("lname");
        formParameters.setDateOfBirthDD("11");
        formParameters.setDateOfBirthMM("1");
        formParameters.setDateOfBirthYY("2009");
        formParameters.setGender(gender);
        formParameters.setLivingStatus(livingStatus);
        return page.createMember(formParameters);
   }

   public CreateClientPreviewDataPage createClientMFIInformationAndGoToPreviewPage(String loanOfficer,CreateClientEnterMfiDataPage clientMfiDataPage) {
       CreateClientEnterMfiDataPage.SubmitFormParameters mfiFormParameters = new CreateClientEnterMfiDataPage.SubmitFormParameters();
       mfiFormParameters.setLoanOfficerId(loanOfficer);

       MeetingParameters meetingFormParameters = new MeetingParameters();
       meetingFormParameters.setWeekFrequency("1");
       meetingFormParameters.setWeekDay(MeetingParameters.WEDNESDAY);
       meetingFormParameters.setMeetingPlace("Mangalore");

       mfiFormParameters.setMeeting(meetingFormParameters);
       return clientMfiDataPage.submitAndGotoCreateClientPreviewDataPage(mfiFormParameters);
   }
    // TODO is this not in SearchHelper?
    public ClientSearchResultsPage searchForClient(String searchString)
    {
        selenium.type("clients_accounts.input.search", searchString);
        selenium.click("clients_accounts.button.search");
        waitForPageToLoad();
        return new ClientSearchResultsPage(selenium);
    }
}
