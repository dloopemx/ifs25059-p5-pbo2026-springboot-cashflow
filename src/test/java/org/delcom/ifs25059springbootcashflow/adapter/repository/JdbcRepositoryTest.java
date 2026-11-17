package org.delcom.ifs25059springbootcashflow.adapter.repository;

import org.delcom.ifs25059springbootcashflow.domain.entity.Role;
import org.delcom.ifs25059springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25059springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25059springbootcashflow.domain.entity.User;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JdbcRepositoryTest {

    private JdbcUserRepository userRepository;
    private JdbcTransactionRepository transactionRepository;

    @BeforeEach
    void setUp() {
        DataSource dataSource = createDataSource();
        SchemaInitializer.initialize(dataSource);
        userRepository = new JdbcUserRepository(dataSource);
        transactionRepository = new JdbcTransactionRepository(dataSource);
    }

    private DataSource createDataSource() {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:jdbc_repo_" + System.nanoTime() + ";MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE");
        dataSource.setUser("sa");
        dataSource.setPassword("");
        return dataSource;
    }

    @Test
    void userCrudAndFinders() {
        User saved = userRepository.saveNew("andi", "hash", "Andi", Role.USER);
        assertTrue(saved.getId() > 0);

        assertTrue(userRepository.findByUsername("ANDI").isPresent());
        assertEquals(1, userRepository.findAll().size());
        assertTrue(userRepository.findById(saved.getId()).isPresent());
        assertTrue(userRepository.findBy(user -> user.getUsername().equals("andi")).size() == 1);

        saved.changeFullName("Andi Baru");
        saved.changePhotoPath("profiles/a.png");
        userRepository.update(saved);
        assertEquals("Andi Baru", userRepository.findById(saved.getId()).orElseThrow().getFullName());

        User viaSave = userRepository.save(new User(0, "budi", "h", "Budi", Role.USER, null));
        assertTrue(viaSave.getId() > 0);

        assertTrue(userRepository.deleteById(viaSave.getId()));
        assertFalse(userRepository.deleteById(viaSave.getId()));
        assertTrue(userRepository.findByUsername("tidakada").isEmpty());
        assertTrue(userRepository.findById(999).isEmpty());
    }

    @Test
    void transactionCrudAndFinders() {
        User owner = userRepository.saveNew("owner", "hash", "Owner", Role.USER);
        User other = userRepository.saveNew("other", "hash", "Other", Role.USER);
        Transaction salary = transactionRepository.save(owner.getId(), "Gaji", 5_000_000, TransactionType.INCOME);
        assertTrue(salary.getId() > 0);
        assertEquals(TransactionType.INCOME, salary.getType());

        Transaction viaEntity = transactionRepository.save(
                new Transaction(0, owner.getId(), "Makan", 25_000, TransactionType.EXPENSE));
        assertTrue(viaEntity.getId() > salary.getId());
        transactionRepository.save(other.getId(), "Milik lain", 1_000, TransactionType.INCOME);

        assertEquals(3, transactionRepository.findAll().size());
        assertEquals(2, transactionRepository.findByUserId(owner.getId()).size());
        assertEquals(1, transactionRepository.findByUserId(other.getId()).size());
        assertEquals(1, transactionRepository.findBy(tx -> tx.getDescription().equals("Gaji")).size());
        assertTrue(transactionRepository.findById(999).isEmpty());

        Transaction loaded = transactionRepository.findById(salary.getId()).orElseThrow();
        assertEquals(salary.getCreatedAt(), loaded.getCreatedAt());
        assertEquals(5_000_000, loaded.getAmount());

        loaded.changeDescription("Gaji Bulanan");
        loaded.changeAmount(6_000_000);
        loaded.changeType(TransactionType.EXPENSE);
        loaded.changeProofPath("proofs/x.png");
        transactionRepository.update(loaded);
        Transaction updated = transactionRepository.findById(salary.getId()).orElseThrow();
        assertEquals("Gaji Bulanan", updated.getDescription());
        assertEquals(6_000_000, updated.getAmount());
        assertEquals(TransactionType.EXPENSE, updated.getType());
        assertEquals("proofs/x.png", updated.getProofPath());

        assertTrue(transactionRepository.deleteById(salary.getId()));
        assertFalse(transactionRepository.deleteById(salary.getId()));
        List<Transaction> remaining = transactionRepository.findByUserId(owner.getId());
        assertEquals(1, remaining.size());
    }

    @Test
    void schemaInitializerIsIdempotent() {
        DataSource dataSource = createDataSource();
        SchemaInitializer.initialize(dataSource);
        SchemaInitializer.initialize(dataSource);
        assertTrue(new JdbcTransactionRepository(dataSource).findAll().isEmpty());
    }
}
