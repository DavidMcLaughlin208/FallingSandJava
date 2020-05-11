package com.gdx.cellular.spouts;

import com.gdx.cellular.CellularMatrix;
import com.gdx.cellular.CellularMatrix.FunctionInput;

import java.util.function.Consumer;

public interface Spout {

    FunctionInput setFunctionInputs(FunctionInput functionInput);

    Consumer<FunctionInput> getFunction();
}
