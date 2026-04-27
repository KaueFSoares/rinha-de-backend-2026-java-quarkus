package br.kauesoares.resource;

import br.kauesoares.dto.Input;
import br.kauesoares.InputMapper;
import br.kauesoares.Normalizer;
import br.kauesoares.dto.RequestDTO;
import br.kauesoares.dto.ScoreResponse;
import br.kauesoares.VectorSearch;
import br.kauesoares.data.VectorStore;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/fraud-score")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class FraudScoreResource {

    private static final float THRESHOLD = 0.6f;

    @Inject
    InputMapper mapper;
    @Inject
    Normalizer normalizer;
    @Inject
    VectorSearch search;

    private final ThreadLocal<Input> TL_INPUT =
            ThreadLocal.withInitial(Input::new);

    private final ThreadLocal<float[]> TL_VEC =
            ThreadLocal.withInitial(() -> new float[14]);

    private final ThreadLocal<int[]> TL_IDX =
            ThreadLocal.withInitial(() -> new int[5]);

    @POST
    public ScoreResponse score(RequestDTO request) {

        Input input = TL_INPUT.get();
        float[] vec = TL_VEC.get();
        int[] idx = TL_IDX.get();

        mapper.map(request, input);
        normalizer.normalize(input, vec);
        search.top5(vec, idx);

        int fraudCount =
                VectorStore.flags[idx[0]] +
                        VectorStore.flags[idx[1]] +
                        VectorStore.flags[idx[2]] +
                        VectorStore.flags[idx[3]] +
                        VectorStore.flags[idx[4]];

        float fraudScore = fraudCount * 0.2f;

        boolean approved = fraudScore < THRESHOLD;

        return new ScoreResponse(approved, fraudScore);
    }
}