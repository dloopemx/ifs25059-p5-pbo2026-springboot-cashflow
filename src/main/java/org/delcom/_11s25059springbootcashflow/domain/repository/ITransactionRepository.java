package org.delcom._11s25059springbootcashflow.domain.repository;

import org.delcom._11s25059springbootcashflow.domain.entity.Transaction;
import org.delcom._11s25059springbootcashflow.domain.entity.TransactionType;

import java.util.List;

/**
 * Port spesifik transaksi yang memperluas generic {@link IRepository}.
 */
public interface ITransactionRepository extends IRepository<Transaction, Integer> {
    /** Menyimpan transaksi baru untuk pengguna tertentu (ID dibuat otomatis). */
    Transaction save(int userId, String description, long amount, TransactionType type);

    /** Mengambil semua transaksi milik satu pengguna. */
    List<Transaction> findByUserId(int userId);
}
