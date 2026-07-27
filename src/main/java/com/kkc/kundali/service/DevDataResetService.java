package com.kkc.kundali.service;

import com.kkc.kundali.dto.DevDataResetResponse;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.Connection;

@Service
public class DevDataResetService {

    @PersistenceContext
    private EntityManager entityManager;

    private final DataSource dataSource;

    public DevDataResetService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Transactional
    public DevDataResetResponse deleteKundaliReportRows(boolean resetIds) {
        /*
         * Delete child rows first.
         * kundali_report_sections depends on kundali_reports.
         */
        int deletedSections = entityManager
                .createQuery("delete from KundaliReportSection")
                .executeUpdate();

        int deletedReports = entityManager
                .createQuery("delete from KundaliReport")
                .executeUpdate();

        boolean idsReset = false;

        if (resetIds) {
            idsReset = resetIdSequencesSafely();
        }

        return new DevDataResetResponse(
                true,
                "Kundali report rows deleted successfully.",
                deletedSections,
                deletedReports,
                idsReset
        );
    }

    private boolean resetIdSequencesSafely() {
        try (Connection connection = dataSource.getConnection()) {
            String databaseName = connection
                    .getMetaData()
                    .getDatabaseProductName()
                    .toLowerCase();

            if (databaseName.contains("mysql")) {
                resetMySqlAutoIncrement();
                return true;
            }

            if (databaseName.contains("postgresql")) {
                resetPostgresSequences();
                return true;
            }

            return false;
        } catch (Exception ex) {
            /*
             * Do not fail row deletion if sequence reset fails.
             * Some DBs or Hibernate identity strategies may use different sequence names.
             */
            return false;
        }
    }

    private void resetMySqlAutoIncrement() {
        entityManager
                .createNativeQuery("ALTER TABLE kundali_report_sections AUTO_INCREMENT = 1")
                .executeUpdate();

        entityManager
                .createNativeQuery("ALTER TABLE kundali_reports AUTO_INCREMENT = 1")
                .executeUpdate();
    }

    private void resetPostgresSequences() {
        entityManager
                .createNativeQuery("ALTER SEQUENCE IF EXISTS kundali_report_sections_id_seq RESTART WITH 1")
                .executeUpdate();

        entityManager
                .createNativeQuery("ALTER SEQUENCE IF EXISTS kundali_reports_id_seq RESTART WITH 1")
                .executeUpdate();
    }
}