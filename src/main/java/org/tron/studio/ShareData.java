package org.tron.studio;

import java.util.HashMap;
import java.util.Map;
import javafx.beans.property.SimpleObjectProperty;
import org.tron.studio.solc.CompilationResult;

public class ShareData {

    private static Map<String, CompilationResult> compilationResultHashMap = new HashMap<>();

    public static SimpleObjectProperty<String> currentContractName = new SimpleObjectProperty<>();

    private ShareData() {
    }

    public static CompilationResult getCompilationResult(String contractName) {
        return compilationResultHashMap.get(contractName);
    }

    public static void setCompilationResult(String contractName, CompilationResult compilationResult) {
        compilationResultHashMap.put(contractName, compilationResult);
    }
}
