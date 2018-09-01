package org.tron.studio;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.scene.control.Tab;
import lombok.Getter;
import lombok.Setter;
import org.spongycastle.util.encoders.Hex;
import org.tron.studio.solc.SolidityCompiler;
import org.tron.studio.walletserver.WalletClient;

import java.util.HashMap;
import java.util.TreeSet;

public class ShareData {

    private static final String[] testAccountAddress = {
        "TVdyt1s88BdiCjKt6K2YuoSmpWScZYK1QF",
        "TCNVmGtkfknHpKSZXepZDXRowHF7kosxcv",
        "TAbzgkG8p3yF5aywKVgq9AaAu6hvF2JrVC",
        "TMmmvwvkBPBv3Gkw9cGKbZ8PLznYkTu3ep",
        "TBJHZu4Sm86aWHtt6VF6KQSzot8vKTuTKx"
    };
    private static final String[] testAccountPrivateKey = {
        "e901ef62b241b6f1577fd6ea34ef8b1c4b3ddee1e3c051b9e63f5ff729ad47a1",
        "3a54ba30e3ee41b602eca8fb3a3ca1f99f49a3d3ab5d8d646a2ccdd3ffd9c21d",
        "af7c83e40cc67a355852b44051fc9e34452375ae569d5c18dd62e3859b9be229",
        "8e3edc3c34c6355cd1b2f0f11a672cddca4468da933813e052e38c93a971798a",
        "7b0b316f60cf3954f0c54c292001f6b59f4a80328a04feafef539f0824ba5078"
    };

    @Getter
    @Setter
    public static String localRpcIp = "127.0.0.1";

    @Getter
    @Setter
    public static int localRpcPort = 16669;

    @Getter
    @Setter
    public static String testNetRpcIp = "47.254.144.25";

    @Getter
    @Setter
    public static int testNetRpcPort = 50051;

    @Getter
    @Setter
    public static String mainNetRpcIp = "54.236.37.243";

    @Getter
    @Setter
    public static int mainNetRpcPort = 50051;


    @Getter
    @Setter
    public static String currentRpcIp = localRpcIp;

    @Getter
    @Setter
    public static int currentRpcPort = localRpcPort;


    public static Boolean enableOptimize = false;
    public static long TRX_SUN_UNIT = 1_000_000;

    public static SimpleStringProperty newAccount = new SimpleStringProperty();
    public static final HashMap<String, String> testAccount = new HashMap<>();
    public static WalletClient wallet = new WalletClient(Hex.decode(testAccountPrivateKey[0]));

    //合约的编译结果
    public static SimpleObjectProperty<SolidityCompiler.Result> currentSolidityCompilerResult = new SimpleObjectProperty<>();
    private static HashMap<String, SolidityCompiler.Result> solidityCompilerResultMap = new HashMap<>();

    //当前合约文件中，被选中的合约（合约文件中可能包含多份合约）
    public static SimpleStringProperty currentContractName = new SimpleStringProperty();

    //当前正在编辑的合约文件
    public static SimpleStringProperty currentContractFileName = new SimpleStringProperty();
    //新建的合约文件
    public static SimpleStringProperty newContractFileName = new SimpleStringProperty();
    //所有的合约文件列表
    public static SimpleSetProperty<String> allContractFileName = new SimpleSetProperty<>(FXCollections.observableSet(new TreeSet<>()));

    //所有的交易历史记录
    //包括不上链的交易：TransactionExtension
    //包括上链的交易：Transaction
    //包括错误交易信息：ErrorInfo
    public static HashMap<String, TransactionHistoryItem> transactionHistory = new HashMap<>();

    // Add transaction
    public static SimpleStringProperty addTransactionAction = new SimpleStringProperty();
    public static SimpleStringProperty debugTransactionAction = new SimpleStringProperty();
    public static SimpleStringProperty openContract = new SimpleStringProperty();
    public static SimpleStringProperty deleteContract = new SimpleStringProperty();
    public static SimpleStringProperty openContractFileName = new SimpleStringProperty();
    public static Tab currentContractTab;

    public static String currentAccount;
    public static String currentValue;

    //自动编译
    public static SimpleBooleanProperty isAutoCompile = new SimpleBooleanProperty();
    //当前合约文件源代码
    public static SimpleStringProperty currentContractSourceCode = new SimpleStringProperty();

    private ShareData() {

    }
    static {
        testAccount.put(testAccountAddress[0], testAccountPrivateKey[0]);
        testAccount.put(testAccountAddress[1], testAccountPrivateKey[1]);
        testAccount.put(testAccountAddress[2], testAccountPrivateKey[2]);
        testAccount.put(testAccountAddress[3], testAccountPrivateKey[3]);
        testAccount.put(testAccountAddress[4], testAccountPrivateKey[4]);
    }

    public static SolidityCompiler.Result getSolidityCompilerResult(String contractName) {
        return solidityCompilerResultMap.get(contractName);
    }

    public static void setSolidityCompilerResult(String contractName, SolidityCompiler.Result solidityCompilerResult) {
        solidityCompilerResultMap.put(contractName, solidityCompilerResult);
        currentSolidityCompilerResult.set(solidityCompilerResult);
    }

}