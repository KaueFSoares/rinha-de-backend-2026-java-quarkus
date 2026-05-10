package br.kauesoares.resource;

import br.kauesoares.*;
import br.kauesoares.data.*;
import io.quarkus.runtime.Startup;
import jakarta.enterprise.context.ApplicationScoped;

@Startup
@ApplicationScoped
public class FraudEngine {

    public final InputMapper inputMapper;
    public final InputNormalizer inputNormalizer;
    public final VectorSearch vectorSearch;
    public final ResultEvaluator resultEvaluator;

    public FraudEngine() {
        this.inputMapper = new InputMapper(new MccRiskDataset());
        this.inputNormalizer = new InputNormalizer();
        this.vectorSearch = new VectorSearch(new VectorDatasetLoader().load());
        this.resultEvaluator = new ResultEvaluator();
    }
}