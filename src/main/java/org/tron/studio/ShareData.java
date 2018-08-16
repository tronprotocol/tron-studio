package org.tron.studio;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;
import org.tron.common.overlay.discover.node.NodeManager;
import org.tron.core.Wallet;
import org.tron.core.WalletSolidity;
import org.tron.core.db.Manager;
import org.tron.studio.solc.CompilationResult;

public class ShareData {

    public static final String[] testAccountAddress = {
        "TVdyt1s88BdiCjKt6K2YuoSmpWScZYK1QF",
        "TCNVmGtkfknHpKSZXepZDXRowHF7kosxcv",
        "TAbzgkG8p3yF5aywKVgq9AaAu6hvF2JrVC",
        "TMmmvwvkBPBv3Gkw9cGKbZ8PLznYkTu3ep",
        "TBJHZu4Sm86aWHtt6VF6KQSzot8vKTuTKx"
    };
    public static final String[] testAccountPrivateKey = {
        "e901ef62b241b6f1577fd6ea34ef8b1c4b3ddee1e3c051b9e63f5ff729ad47a1",
        "3a54ba30e3ee41b602eca8fb3a3ca1f99f49a3d3ab5d8d646a2ccdd3ffd9c21d",
        "af7c83e40cc67a355852b44051fc9e34452375ae569d5c18dd62e3859b9be229",
        "8e3edc3c34c6355cd1b2f0f11a672cddca4468da933813e052e38c93a971798a",
        "7b0b316f60cf3954f0c54c292001f6b59f4a80328a04feafef539f0824ba5078"
    };

    public static Wallet wallet;

    private static Map<String, CompilationResult> compilationResultHashMap = new HashMap<>();

    //当前正在编辑的合约
    public static SimpleObjectProperty<String> currentContractName = new SimpleObjectProperty<>();
    //新建的合约
    public static SimpleObjectProperty<String> newContractName = new SimpleObjectProperty<>();
    //所有被打开的合约列表
    public static SimpleListProperty<String> activeContractName = new SimpleListProperty<>(
        FXCollections.observableArrayList());
    //所有的合约列表
    public static SimpleListProperty<String> allContractName = new SimpleListProperty<>(
        FXCollections.observableArrayList());

    private ShareData() {
    }

    public static CompilationResult getCompilationResult(String contractName) {
        return compilationResultHashMap.get(contractName);
    }

    public static void setCompilationResult(String contractName,
        CompilationResult compilationResult) {
        compilationResultHashMap.put(contractName, compilationResult);
    }

}