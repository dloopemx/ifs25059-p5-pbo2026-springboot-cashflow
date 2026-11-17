package org.delcom.ifs25059springbootcashflow.usecase;

import org.delcom.ifs25059springbootcashflow.adapter.repository.TransactionRepository;
import org.delcom.ifs25059springbootcashflow.domain.entity.Balance;
import org.delcom.ifs25059springbootcashflow.domain.entity.SortOption;
import org.delcom.ifs25059springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25059springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25059springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.ifs25059springbootcashflow.domain.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FinanceUseCaseTest {

    private static final int USER = 1;
    private static final int OTHER = 2;

    private FinanceUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new FinanceUseCase(new TransactionRepository());
    }

    @Test
    void addIncomeAndExpenseUpdatesBalance() throws Exception {
        Transaction income = useCase.addIncome(USER, "  Gaji  ", 5_000_000);
        Transaction expense = useCase.addExpense(USER, "Makan", 25_000);
        useCase.addIncome(OTHER, "Milik lain", 99);

        assertEquals("Gaji", income.getDescription());
        assertEquals(TransactionType.INCOME, income.getType());
        assertEquals(TransactionType.EXPENSE, expense.getType());
        assertEquals(2, useCase.getTransactionsByUser(USER).size());
        assertEquals(3, useCase.getAllTransactions().size());

        Balance balance = useCase.getBalance(USER);
        assertEquals(2, balance.transactionCount());
        assertEquals(5_000_000, balance.totalIncome());
        assertEquals(25_000, balance.totalExpense());
        assertEquals(4_975_000, balance.balance());

        useCase.removeTransactionForUser(USER, expense.getId());
        assertEquals(5_000_000, useCase.getBalance(USER).balance());
    }

    @Test
    void addValidatesDescriptionAmountAndType() {
        assertThrows(ValidationException.class, () -> useCase.addIncome(USER, null, 10));
        assertThrows(ValidationException.class, () -> useCase.addIncome(USER, "   ", 10));
        assertThrows(ValidationException.class, () -> useCase.addExpense(USER, "Makan", 0));
        assertThrows(ValidationException.class, () -> useCase.addExpense(USER, "Makan", -5));
        ValidationException ex = assertThrows(ValidationException.class,
                () -> useCase.addTransaction(USER, "Makan", 10, null));
        assertEquals("Tipe transaksi wajib dipilih", ex.getMessage());
        assertTrue(useCase.getTransactionsByUser(USER).isEmpty());
    }

    @Test
    void updateChangesOnlyProvidedFields() throws Exception {
        Transaction tx = useCase.addIncome(USER, "Gaji", 1_000);

        Transaction unchanged = useCase.updateTransactionForUser(USER, tx.getId(), null, null, null, null, false);
        assertEquals("Gaji", unchanged.getDescription());
        assertEquals(1_000, unchanged.getAmount());
        assertNull(unchanged.getProofPath());

        Transaction updated = useCase.updateTransactionForUser(
                USER, tx.getId(), " Bonus ", 2_000L, TransactionType.EXPENSE, "proofs/a.png", false);
        assertEquals("Bonus", updated.getDescription());
        assertEquals(2_000, updated.getAmount());
        assertEquals(TransactionType.EXPENSE, updated.getType());
        assertEquals("proofs/a.png", updated.getProofPath());

        Transaction cleared = useCase.updateTransactionForUser(USER, tx.getId(), null, null, null, "ignored", true);
        assertNull(cleared.getProofPath());
    }

    @Test
    void updateRejectsInvalidInputNotFoundAndForeignOwner() throws Exception {
        Transaction tx = useCase.addIncome(USER, "Gaji", 1_000);

        assertThrows(ValidationException.class,
                () -> useCase.updateTransactionForUser(USER, tx.getId(), " ", null, null, null, false));
        assertThrows(ValidationException.class,
                () -> useCase.updateTransactionForUser(USER, tx.getId(), null, 0L, null, null, false));
        assertThrows(ValidationException.class,
                () -> useCase.updateTransactionForUser(OTHER, tx.getId(), "X", null, null, null, false));
        EntityNotFoundException notFound = assertThrows(EntityNotFoundException.class,
                () -> useCase.updateTransactionForUser(USER, 999, "X", null, null, null, false));
        assertEquals(999, notFound.getId());
        assertEquals("Transaksi dengan ID 999 tidak ditemukan.", notFound.getMessage());
    }

    @Test
    void removeChecksExistenceAndOwnership() throws Exception {
        Transaction tx = useCase.addExpense(USER, "Makan", 10);

        assertThrows(ValidationException.class, () -> useCase.removeTransactionForUser(OTHER, tx.getId()));
        assertThrows(EntityNotFoundException.class, () -> useCase.removeTransactionForUser(USER, 999));
        assertThrows(EntityNotFoundException.class, () -> useCase.removeTransaction(999));

        useCase.removeTransaction(tx.getId());
        assertTrue(useCase.getTransactionsByUser(USER).isEmpty());
    }

    @Test
    void searchIsCaseInsensitiveAndScopedToUser() throws Exception {
        useCase.addIncome(USER, "Gaji Bulanan", 10);
        useCase.addExpense(USER, "Makan siang", 5);
        useCase.addExpense(OTHER, "Gaji orang lain", 5);

        assertEquals(1, useCase.searchTransactions(USER, "  gAJi ").size());
        assertEquals(0, useCase.searchTransactions(USER, "tidakada").size());
        assertEquals(2, useCase.searchTransactions(USER, null).size());
        assertEquals(2, useCase.searchTransactions(USER, "  ").size());
    }

    @Test
    void sortUsesSortOptionComparator() throws Exception {
        useCase.addIncome(USER, "A", 300);
        useCase.addExpense(USER, "B", 100);
        useCase.addIncome(USER, "C", 200);

        List<Long> ascending = useCase.sortTransactions(USER, SortOption.AMOUNT_ASC).stream()
                .map(Transaction::getAmount).toList();
        assertEquals(List.of(100L, 200L, 300L), ascending);

        List<String> expenseFirst = useCase.sort(useCase.getTransactionsByUser(USER), SortOption.EXPENSE_FIRST)
                .stream().map(Transaction::getDescription).toList();
        assertEquals(List.of("B", "A", "C"), expenseFirst);
    }

    @Test
    void parseAmountAcceptsSeparatorsAndRejectsInvalidInput() throws Exception {
        assertEquals(1_000_000, useCase.parseAmount("1000000"));
        assertEquals(1_000_000, useCase.parseAmount(" 1.000.000 "));
        assertEquals(1_000_000, useCase.parseAmount("1,000,000"));
        assertEquals(1_000_000, useCase.parseAmount("1 000 000"));

        assertEquals("Jumlah tidak boleh kosong",
                assertThrows(ValidationException.class, () -> useCase.parseAmount(null)).getMessage());
        assertEquals("Jumlah tidak boleh kosong",
                assertThrows(ValidationException.class, () -> useCase.parseAmount("  ")).getMessage());
        assertEquals("Jumlah harus berupa angka",
                assertThrows(ValidationException.class, () -> useCase.parseAmount("seratus")).getMessage());
        assertEquals("Jumlah terlalu besar",
                assertThrows(ValidationException.class,
                        () -> useCase.parseAmount("99999999999999999999")).getMessage());
        assertEquals("Jumlah harus lebih dari 0",
                assertThrows(ValidationException.class, () -> useCase.parseAmount("0")).getMessage());
        assertEquals("Jumlah harus lebih dari 0",
                assertThrows(ValidationException.class, () -> useCase.parseAmount("-500")).getMessage());
    }

    @Test
    void parseIdRejectsInvalidIds() throws Exception {
        assertEquals(12, useCase.parseId(" 12 "));
        assertThrows(ValidationException.class, () -> useCase.parseId(null));
        assertThrows(ValidationException.class, () -> useCase.parseId("abc"));
        assertThrows(ValidationException.class, () -> useCase.parseId("-1"));
        assertThrows(ValidationException.class, () -> useCase.parseId("0"));
        assertThrows(ValidationException.class, () -> useCase.parseId("12345678901"));
    }
}
