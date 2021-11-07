package pl.kj.bachelors.identity.application.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.kj.bachelors.identity.application.model.HealthCheckResult;
import pl.kj.bachelors.identity.application.model.SingleCheckResult;

import javax.sql.DataSource;
import java.sql.SQLException;

@Service
public class HealthCheckService {
    private final DataSource dataSource;

    public HealthCheckService(@Autowired DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public HealthCheckResult check() {
        HealthCheckResult report = new HealthCheckResult();

        report.addResult(new SingleCheckResult("Main Database", this.checkDatabase()));

        return report;
    }

    private boolean checkDatabase() {
        boolean up;
        try {
            var connection = dataSource.getConnection();
            var stmt = connection.prepareCall("select 1");
            up = stmt.execute();
        } catch (SQLException e) {
            e.printStackTrace();
            up = false;
        }

        return up;
    }
}
