package pl.kj.bachelors.identity.application.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.kj.bachelors.identity.application.model.HealthCheckResult;
import pl.kj.bachelors.identity.application.model.SingleCheckResult;

import javax.sql.DataSource;
import java.sql.SQLException;

@Service
public class HealthCheckService {

    public HealthCheckResult check() {
        HealthCheckResult report = new HealthCheckResult();

        report.addResult(new SingleCheckResult("Main Database", this.checkDatabase()));

        return report;
    }

    private boolean checkDatabase() {
        return true;
    }
}
