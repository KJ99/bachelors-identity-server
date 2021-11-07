package pl.kj.bachelors.identity.application.model;

import java.util.ArrayList;
import java.util.List;

public class HealthCheckResult {
    private List<SingleCheckResult> results;

    public HealthCheckResult() {
        this.results = new ArrayList<>();
    }

    public List<SingleCheckResult> getResults() {
        return results;
    }

    public void setResults(List<SingleCheckResult> results) {
        this.results = results;
    }

    public void addResult(SingleCheckResult result) {
        this.results.add(result);
    }

}
