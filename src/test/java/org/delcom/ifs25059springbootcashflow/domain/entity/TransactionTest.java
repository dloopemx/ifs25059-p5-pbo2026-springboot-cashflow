package org.delcom.ifs25059springbootcashflow.domain.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TransactionTest {

    @Test
    void shortConstructorCreatesTransactionWithoutProof() {
        Transaction tx = new Transaction(1, 10, "Gaji", 5_000_000, TransactionType.INCOME);

        assertEquals(1, tx.getId());
        assertEquals(10, tx.getUserId());
        assertEquals("Gaji", tx.getDescription());
        assertEquals(5_000_000, tx.getAmount());
        assertEquals(TransactionType.INCOME, tx.getType());
        assertNull(tx.getProofPath());
        assertNotNull(tx.getCreatedAt());
        assertTrue(tx.isIncome());
        assertFalse(tx.isExpense());
        assertEquals(5_000_000, tx.signedAmount());
    }

    @Test
    void fullConstructorAndMutators() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 1, 2, 3, 4);
        Transaction tx = new Transaction(2, 10, "Makan", 25_000, TransactionType.EXPENSE, "proofs/a.png", createdAt);

        assertEquals("proofs/a.png", tx.getProofPath());
        assertEquals(createdAt, tx.getCreatedAt());
        assertTrue(tx.isExpense());
        assertFalse(tx.isIncome());
        assertEquals(-25_000, tx.signedAmount());

        tx.changeDescription("Makan siang");
        tx.changeAmount(30_000);
        tx.changeType(TransactionType.INCOME);
        tx.changeProofPath(null);

        assertEquals("Makan siang", tx.getDescription());
        assertEquals(30_000, tx.getAmount());
        assertEquals(TransactionType.INCOME, tx.getType());
        assertNull(tx.getProofPath());
        assertEquals(30_000, tx.signedAmount());
    }

    @Test
    void transactionTypeAppliesSign() {
        assertEquals(100, TransactionType.INCOME.applySign(100));
        assertEquals(-100, TransactionType.EXPENSE.applySign(100));
    }
}
