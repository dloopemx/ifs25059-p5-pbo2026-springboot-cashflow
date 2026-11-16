package org.delcom._11s25059springbootcashflow.domain.entity;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BalanceTest {

    @Test
    void calculatesIncomeExpenseAndBalance() {
        Balance balance = Balance.of(List.of(
                new Transaction(1, 1, "Gaji", 5_000_000, TransactionType.INCOME),
                new Transaction(2, 1, "Makan", 25_000, TransactionType.EXPENSE),
                new Transaction(3, 1, "Bonus", 1_000_000, TransactionType.INCOME),
                new Transaction(4, 1, "Sewa", 1_000_000, TransactionType.EXPENSE)
        ));

        assertEquals(4, balance.transactionCount());
        assertEquals(6_000_000, balance.totalIncome());
        assertEquals(1_025_000, balance.totalExpense());
        assertEquals(4_975_000, balance.balance());
    }

    @Test
    void emptyBalanceIsZero() {
        assertEquals(Balance.EMPTY, Balance.of(List.of()));
        assertEquals(0, Balance.EMPTY.balance());
    }

    @Test
    void balanceCanBeNegative() {
        Balance balance = Balance.of(List.of(new Transaction(1, 1, "Utang", 500, TransactionType.EXPENSE)));
        assertEquals(-500, balance.balance());
    }
}
