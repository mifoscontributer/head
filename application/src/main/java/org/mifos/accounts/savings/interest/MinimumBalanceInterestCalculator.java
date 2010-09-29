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

package org.mifos.accounts.savings.interest;

import java.math.BigDecimal;
import java.util.List;

import org.joda.time.LocalDate;
import org.mifos.framework.util.helpers.Money;

public class MinimumBalanceInterestCalculator extends AbstractInterestCalculator {

    @Override
    public Money getPrincipal(List<EndOfDayBalance> balanceRecords, final LocalDate calculationStartDate, final LocalDate calculationEndDate) {
        Money minimumBalance = null;

        validateData(balanceRecords,"EndOfDayBalance list");
        validateData(calculationStartDate,"EndOfDayBalance list");
        validateData(calculationEndDate,"EndOfDayBalance list");

        for (EndOfDayBalance balance : balanceRecords) {
            if (minimumBalance == null) {
                minimumBalance = balance.getBalance();
            } else {
                if (minimumBalance.isGreaterThan(balance.getBalance())) {
                    minimumBalance = balance.getBalance();
                }
            }
        }

        return minimumBalance;
    }

    @Override
    public BigDecimal calcInterest(InterestCalculationRange interestCalculationRange, EndOfDayDetail... deposit) {
        // TODO Auto-generated method stub
        return null;
    }

}
