package br.kauesoares.resource;

import br.kauesoares.dto.*;
import br.kauesoares.dto.Input;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

@Path("/fraud-score")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class FraudScoreResource {

    @Inject
    FraudEngine engine;

    private final ThreadLocal<Input> TL_INPUT =
            ThreadLocal.withInitial(Input::new);

    private final ThreadLocal<float[]> TL_VEC =
            ThreadLocal.withInitial(() -> new float[14]);

    private final ThreadLocal<byte[]> TL_FLAGS =
            ThreadLocal.withInitial(() -> new byte[5]);

    private final ThreadLocal<ScoreResponse> TL_RESP =
            ThreadLocal.withInitial(ScoreResponse::new);

    @POST
    public ScoreResponse score(RequestDTO request) {

        Input input = TL_INPUT.get();
        float[] vec = TL_VEC.get();
        byte[] flags = TL_FLAGS.get();
        ScoreResponse resp = TL_RESP.get();

        engine.inputMapper.map(request, input);
        engine.inputNormalizer.normalize(input, vec);
        engine.vectorSearch.top5(vec, flags);
        return engine.resultEvaluator.evaluate(flags, resp);
    }
}