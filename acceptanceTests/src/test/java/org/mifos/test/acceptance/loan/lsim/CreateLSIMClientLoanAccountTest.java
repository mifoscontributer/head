/*
 * Copyright (c) 2005-2011 Grameen Foundation USA
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

package org.mifos.test.acceptance.loan.lsim;

import org.joda.time.DateTime;
import org.mifos.test.acceptance.framework.MifosPage;
import org.mifos.test.acceptance.framework.UiTestCaseBase;
import org.mifos.test.acceptance.framework.account.AccountStatus;
import org.mifos.test.acceptance.framework.loan.CreateLoanAccountSearchParameters;
import org.mifos.test.acceptance.framework.loan.CreateLoanAccountSubmitParameters;
import org.mifos.test.acceptance.framework.loan.DisburseLoanParameters;
import org.mifos.test.acceptance.framework.loan.EditLoanAccountStatusParameters;
import org.mifos.test.acceptance.framework.loan.LoanAccountPage;
import org.mifos.test.acceptance.framework.loan.PaymentParameters;
import org.mifos.test.acceptance.framework.loan.ViewRepaymentSchedulePage;
import org.mifos.test.acceptance.framework.loanproduct.DefineNewLoanProductPage;
import org.mifos.test.acceptance.framework.testhelpers.FormParametersHelper;
import org.mifos.test.acceptance.framework.testhelpers.LoanTestHelper;
import org.mifos.test.acceptance.loanproduct.LoanProductTestHelper;
import org.mifos.test.acceptance.remote.DateTimeUpdaterRemoteTestingService;
import org.mifos.test.acceptance.util.ApplicationDatabaseOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.sql.SQLException;

@SuppressWarnings("PMD")
@ContextConfiguration(locations = {"classpath:ui-test-context.xml"})
@Test(sequential = true, groups = {"loan", "acceptance", "ui", "no_db_unit"})
public class CreateLSIMClientLoanAccountTest extends UiTestCaseBase {

    private LoanTestHelper loanTestHelper;
    private LoanProductTestHelper loanProductTestHelper;
    private String expectedDate;
    @Autowired
    private ApplicationDatabaseOperation applicationDatabaseOperation;

    @Override
    @SuppressWarnings("PMD.SignatureDeclareThrowsException")
    // one of the dependent methods throws Exception
    @BeforeMethod(alwaysRun = true)
    public void setUp() throws Exception {
        super.setUp();
        applicationDatabaseOperation.updateLSIM(1);
        loanTestHelper = new LoanTestHelper(selenium);
        loanProductTestHelper = new LoanProductTestHelper(selenium);
        DateTimeUpdaterRemoteTestingService dateTimeUpdaterRemoteTestingService = new DateTimeUpdaterRemoteTestingService(selenium);
        DateTime targetTime = new DateTime(2010, 1, 22, 10, 55, 0, 0);
        dateTimeUpdaterRemoteTestingService.setDateTime(targetTime);
    }

    @AfterMethod(alwaysRun = true)
    public void logOut() throws SQLException {
        applicationDatabaseOperation.updateLSIM(0);
        (new MifosPage(selenium)).logout();
    }

    @Test(groups = {"loan", "acceptance", "ui"})
    @SuppressWarnings("PMD.SignatureDeclareThrowsException")
    // one of the dependent methods throws Exception
    public void newWeeklyLSIMClientLoanAccount() throws Exception {

        CreateLoanAccountSearchParameters searchParameters = new CreateLoanAccountSearchParameters();
        searchParameters.setSearchString("Stu1233171716380 Client1233171716380");
        searchParameters.setLoanProduct("WeeklyFlatLoanWithOneTimeFees");
        expectedDate = "29-Jan-2010";
        CreateLoanAccountSubmitParameters submitAccountParameters = new CreateLoanAccountSubmitParameters();
        submitAccountParameters.setAmount("9012.0");
        submitAccountParameters.setLsimFrequencyWeeks("on");
        submitAccountParameters.setLsimWeekFrequency("1");
        submitAccountParameters.setLsimWeekDay("Friday");

        createLSIMLoanAndCheckAmountAndInstallmentDate(searchParameters, submitAccountParameters, expectedDate);
    }

    @SuppressWarnings("PMD.SignatureDeclareThrowsException")
    // one of the dependent methods throws Exception
    public void newMonthlyClientLoanAccountWithMeetingOnSpecificDayOfMonth() throws Exception {
        CreateLoanAccountSearchParameters searchParameters = new CreateLoanAccountSearchParameters();
        searchParameters.setSearchString("Client - Mary Monthly1");
        searchParameters.setLoanProduct("MonthlyClientFlatLoan1stOfMonth");
        expectedDate = "05-Feb-2010";
        CreateLoanAccountSubmitParameters submitAccountParameters = new CreateLoanAccountSubmitParameters();
        submitAccountParameters.setAmount("1234.0");
        // create LSIM loan that has repayments on 5th of every month
        submitAccountParameters.setLsimFrequencyMonths("on");
        submitAccountParameters.setLsimMonthTypeDayOfMonth("on");
        submitAccountParameters.setLsimDayOfMonth("5");

        createLSIMLoanAndCheckAmountAndInstallmentDate(searchParameters, submitAccountParameters, expectedDate);
    }

    @SuppressWarnings("PMD.SignatureDeclareThrowsException")
    // one of the dependent methods throws Exception
    public void newMonthlyClientLoanAccountWithMeetingOnSameWeekAndWeekday() throws Exception {
        CreateLoanAccountSearchParameters searchParameters = new CreateLoanAccountSearchParameters();
        searchParameters.setSearchString("Client - Mia Monthly3rdFriday");
        searchParameters.setLoanProduct("MonthlyClientFlatLoanThirdFridayOfMonth");
        expectedDate = "11-Mar-2010";

        CreateLoanAccountSubmitParameters submitAccountParameters = new CreateLoanAccountSubmitParameters();
        submitAccountParameters.setAmount("2765.0");
        // create LSIM loan that has repayments on 2nd Thursday of each month
        submitAccountParameters.setLsimFrequencyMonths("on");
        submitAccountParameters.setLsimMonthTypeNthWeekdayOfMonth("on");
        submitAccountParameters.setLsimMonthRank("Second");
        submitAccountParameters.setLsimWeekDay("Thursday");

        createLSIMLoanAndCheckAmountAndInstallmentDate(searchParameters, submitAccountParameters, expectedDate);
    }

    // http://mifosforge.jira.com/browse/MIFOSTEST-123
    public void createLoanAccountWithNonMeetingDatesForDisburseAndRepay() throws Exception {
        //Given
        DateTimeUpdaterRemoteTestingService dateTimeUpdaterRemoteTestingService = new DateTimeUpdaterRemoteTestingService(selenium);
        DateTime systemTime = new DateTime(2011, 02, 24, 12, 0, 0, 0);
        dateTimeUpdaterRemoteTestingService.setDateTime(systemTime);
        DefineNewLoanProductPage.SubmitFormParameters defineNewLoanProductformParameters = FormParametersHelper.getMonthlyLoanProductParameters();
        CreateLoanAccountSearchParameters searchParameters = new CreateLoanAccountSearchParameters();
        searchParameters.setSearchString("Client - Mary Monthly");
        searchParameters.setLoanProduct(defineNewLoanProductformParameters.getOfferingName());
        CreateLoanAccountSubmitParameters submitAccountParameters = new CreateLoanAccountSubmitParameters();
        submitAccountParameters.setDd("24");
        submitAccountParameters.setMm("02");
        submitAccountParameters.setYy("2011");
        EditLoanAccountStatusParameters editLoanAccountStatusParameters = new EditLoanAccountStatusParameters();
        editLoanAccountStatusParameters.setStatus(AccountStatus.LOAN_APPROVED.getStatusText());
        editLoanAccountStatusParameters.setNote("activate account");
        DisburseLoanParameters disburseLoanParameters = new DisburseLoanParameters();
        disburseLoanParameters.setDisbursalDateDD("24");
        disburseLoanParameters.setDisbursalDateMM("02");
        disburseLoanParameters.setDisbursalDateYYYY("2011");
        disburseLoanParameters.setPaymentType(PaymentParameters.CASH);
        //When
        loanProductTestHelper.defineNewLoanProduct(defineNewLoanProductformParameters);
        //Then
        String loanId = loanTestHelper.createLoanAccount(searchParameters, submitAccountParameters).getAccountId();
        loanTestHelper.changeLoanAccountStatus(loanId, editLoanAccountStatusParameters);
        loanTestHelper.disburseLoan(loanId, disburseLoanParameters);
        loanTestHelper.repayLoan(loanId);
    }

    private void createLSIMLoanAndCheckAmountAndInstallmentDate(CreateLoanAccountSearchParameters searchParameters,
                                                                CreateLoanAccountSubmitParameters submitAccountParameters, String expectedDate) {

        LoanAccountPage loanAccountPage = loanTestHelper.createLoanAccount(searchParameters, submitAccountParameters);
        loanAccountPage.verifyLoanAmount(submitAccountParameters.getAmount());
        ViewRepaymentSchedulePage viewRepaymentSchedulePage = loanAccountPage.navigateToViewRepaymentSchedule();
        viewRepaymentSchedulePage.verifyFirstInstallmentDate(4, 2, expectedDate);

    }
}
