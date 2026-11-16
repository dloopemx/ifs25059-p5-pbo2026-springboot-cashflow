package org.delcom._11s25059springbootcashflow.adapter.repository;

import org.delcom._11s25059springbootcashflow.domain.entity.Transaction;
import org.delcom._11s25059springbootcashflow.domain.entity.TransactionType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TransactionRepositoryTest {

    @Test
    void savesWithIncrementingIdsAndFindsByUser() {
        TransactionRepository repository = new TransactionRepository();
        Transaction first = repository.save(1, "Gaji", 5_000_000, TransactionType.INCOME);
        Transaction second = repository.save(2, "Makan", 20_000, TransactionType.EXPENSE);
        Transaction third = repository.save(1, "Bensin", 50_000, TransactionType.EXPENSE);

        assertEquals(1, first.getId());
        assertEquals(2, second.getId());
        assertEquals(3, third.getId());
        assertEquals(3, repository.findAll().size());
        assertEquals(List.of(first, third), repository.findByUserId(1));
        assertEquals(List.of(second), repository.findByUserId(2));
        assertTrue(repository.findByUserId(99).isEmpty());
    }

    @Test
    void genericCrudOperations() {
        TransactionRepository repository = new TransactionRepository();
        Transaction saved = repository.save(new Transaction(10, 1, "Manual", 1_000, TransactionType.INCOME));

        assertTrue(repository.findById(10).isPresent());
        assertTrue(repository.findById(11).isEmpty());
        assertEquals(1, repository.findBy(tx -> tx.getAmount() == 1_000).size());

        saved.changeDescription("Diubah");
        repository.update(saved);
        assertEquals("Diubah", repository.findById(10).orElseThrow().getDescription());

        List<Transaction> snapshot = repository.findAll();
        assertNotSame(snapshot, repository.findAll());

        assertTrue(repository.deleteById(10));
        assertFalse(repository.deleteById(10));
        assertTrue(repository.findAll().isEmpty());
    }
}
