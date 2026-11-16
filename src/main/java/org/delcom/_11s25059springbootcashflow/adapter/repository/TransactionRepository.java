package org.delcom._11s25059springbootcashflow.adapter.repository;

import org.delcom._11s25059springbootcashflow.domain.entity.Transaction;
import org.delcom._11s25059springbootcashflow.domain.entity.TransactionType;
import org.delcom._11s25059springbootcashflow.domain.repository.ITransactionRepository;

import java.util.List;

/**
 * Implementasi repository transaksi menggunakan basis generic {@link InMemoryRepository}.
 * Dipakai pada unit test; runtime memakai {@link JdbcTransactionRepository}.
 */
public class TransactionRepository extends InMemoryRepository<Transaction, Integer>
        implements ITransactionRepository {
    private int idCounter = 0;

    public TransactionRepository() {
        super(Transaction::getId);
    }

    @Override
    public Transaction save(int userId, String description, long amount, TransactionType type) {
        return save(new Transaction(nextId(), userId, description, amount, type));
    }

    @Override
    public List<Transaction> findByUserId(int userId) {
        return findBy(transaction -> transaction.getUserId() == userId);
    }

    private int nextId() {
        return ++idCounter;
    }
}
