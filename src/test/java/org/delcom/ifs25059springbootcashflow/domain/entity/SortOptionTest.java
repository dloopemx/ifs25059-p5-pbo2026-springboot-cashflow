package org.delcom.ifs25059springbootcashflow.domain.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SortOptionTest {

    private final LocalDateTime base = LocalDateTime.of(2026, 1, 1, 8, 0);
    private final Transaction salary = tx(1, "Gaji", 5_000_000, TransactionType.INCOME, 0);
    private final Transaction food = tx(2, "Makan", 25_000, TransactionType.EXPENSE, 1);
    private final Transaction bonus = tx(3, "Bonus", 1_000_000, TransactionType.INCOME, 2);
    private final Transaction rent = tx(4, "Sewa", 1_000_000, TransactionType.EXPENSE, 3);
    private final List<Transaction> all = List.of(bonus, rent, salary, food);

    private Transaction tx(int id, String description, long amount, TransactionType type, int minutes) {
        return new Transaction(id, 1, description, amount, type, null, base.plusMinutes(minutes));
    }

    private List<Integer> sortedIds(SortOption option) {
        return all.stream().sorted(option.comparator()).map(Transaction::getId).toList();
    }

    @Test
    void amountAscendingThenId() {
        assertEquals(List.of(2, 3, 4, 1), sortedIds(SortOption.AMOUNT_ASC));
    }

    @Test
    void amountDescendingThenId() {
        assertEquals(List.of(1, 3, 4, 2), sortedIds(SortOption.AMOUNT_DESC));
    }

    @Test
    void incomeFirst() {
        assertEquals(List.of(1, 3, 2, 4), sortedIds(SortOption.INCOME_FIRST));
    }

    @Test
    void expenseFirst() {
        assertEquals(List.of(2, 4, 1, 3), sortedIds(SortOption.EXPENSE_FIRST));
    }

    @Test
    void newestAndOldest() {
        assertEquals(List.of(4, 3, 2, 1), sortedIds(SortOption.NEWEST));
        assertEquals(List.of(1, 2, 3, 4), sortedIds(SortOption.OLDEST));
    }

    @Test
    void hasAtLeastFourOptions() {
        assertEquals(6, SortOption.values().length);
        assertEquals(SortOption.NEWEST, SortOption.valueOf("NEWEST"));
    }
}
