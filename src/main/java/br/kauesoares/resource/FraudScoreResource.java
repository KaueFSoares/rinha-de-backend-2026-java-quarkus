package br.kauesoares.resource;

import br.kauesoares.ResultEvaluator;
import br.kauesoares.data.MccRiskDataset;
import br.kauesoares.data.VectorDatasetLoader;
import br.kauesoares.dto.Input;
import br.kauesoares.InputMapper;
import br.kauesoares.InputNormalizer;
import br.kauesoares.dto.RequestDTO;
import br.kauesoares.dto.ScoreResponse;
import br.kauesoares.VectorSearch;
import br.kauesoares.data.VectorDataset;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/fraud-score")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class FraudScoreResource {

    private final InputMapper inputMapper = new InputMapper(new MccRiskDataset());
    private final InputNormalizer inputNormalizer = new InputNormalizer();
    private final VectorDataset vectorDataset = new VectorDatasetLoader().load();
    private final VectorSearch vectorSearch = new VectorSearch(vectorDataset);
    private final ResultEvaluator resultEvaluator = new ResultEvaluator(vectorDataset);

    private final ThreadLocal<Input> TL_INPUT = ThreadLocal.withInitial(Input::new);
    private final ThreadLocal<float[]> TL_VEC = ThreadLocal.withInitial(() -> new float[14]);
    private final ThreadLocal<int[]> TL_IDX = ThreadLocal.withInitial(() -> new int[5]);

    @POST
    public ScoreResponse score(RequestDTO request) {

        Input input = TL_INPUT.get();
        float[] vec = TL_VEC.get();
        int[] idx = TL_IDX.get();

        inputMapper.map(request, input);
        inputNormalizer.normalize(input, vec);
        vectorSearch.top5(vec, idx);

        return resultEvaluator.evaluate(idx);
    }
}