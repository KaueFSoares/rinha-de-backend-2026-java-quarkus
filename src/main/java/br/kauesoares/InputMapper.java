package br.kauesoares;

import br.kauesoares.data.MccRiskDataset;
import br.kauesoares.dto.Input;
import br.kauesoares.dto.RequestDTO;

public class InputMapper {

    private final MccRiskDataset mccRisk;

    public InputMapper(MccRiskDataset mccRisk) {
        this.mccRisk = mccRisk;
    }

    public void map(RequestDTO r, Input out) {

    }
}