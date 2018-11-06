package org.tron.studio;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleSetProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.scene.control.Tab;
import lombok.Getter;
import lombok.Setter;
import org.spongycastle.util.encoders.Hex;
import org.tron.studio.solc.SolidityCompiler;

import org.tron.studio.utils.FormatCode;
import org.tron.studio.walletserver.WalletClient;
import javafx.scene.Scene;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.TreeSet;


public class ShareData {

    private static final String[] testAccountAddress = {
            "TAbzgkG8p3yF5aywKVgq9AaAu6hvF2JrVC",
            "TVdyt1s88BdiCjKt6K2YuoSmpWScZYK1QF",
            "TCNVmGtkfknHpKSZXepZDXRowHF7kosxcv",
            "TMmmvwvkBPBv3Gkw9cGKbZ8PLznYkTu3ep",
            "TBJHZu4Sm86aWHtt6VF6KQSzot8vKTuTKx"
    };
    private static final String[] testAccountPrivateKey = {
            "af7c83e40cc67a355852b44051fc9e34452375ae569d5c18dd62e3859b9be229",
            "e901ef62b241b6f1577fd6ea34ef8b1c4b3ddee1e3c051b9e63f5ff729ad47a1",
            "3a54ba30e3ee41b602eca8fb3a3ca1f99f49a3d3ab5d8d646a2ccdd3ffd9c21d",
            "8e3edc3c34c6355cd1b2f0f11a672cddca4468da933813e052e38c93a971798a",
            "7b0b316f60cf3954f0c54c292001f6b59f4a80328a04feafef539f0824ba5078"
    };
    public static class NetWorkEnvironment {
        public String url;
        public int port;

        public NetWorkEnvironment(String url, int port){
            this.url = url;
            this.port = port;
        }
    }

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


    @Getter
    @Setter
    public static String currentEnvironment = "Local TVM";

    public static Boolean enableOptimize = true;
    public static long TRX_SUN_UNIT = 1_000_000;

    public static SimpleStringProperty newAccount = new SimpleStringProperty();
    public static SimpleStringProperty newNetwork = new SimpleStringProperty();
    public static final LinkedHashMap<String, String> testAccount = new LinkedHashMap<>();
    public static final LinkedHashMap<String, NetWorkEnvironment> saved_network = new LinkedHashMap<>();//network name: ip:port
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


    // 文本错误信息
    public static List<FormatCode.MissInfo> missInfoList = new ArrayList<>();

    public static SimpleObjectProperty<Scene> sceneObjectProperty = new SimpleObjectProperty();

    public static boolean isScrolling = false;
    //public static int currentPara = 0;
    private ShareData() {

    }

    private static void setSavedNetWork(){

        saved_network.put("Local TVM", new NetWorkEnvironment("127.0.0.1", 16669));
        saved_network.put("Test Net", new NetWorkEnvironment(testNetRpcIp,testNetRpcPort));
        saved_network.put("Main Net", new NetWorkEnvironment(mainNetRpcIp, mainNetRpcPort));
        try {
            File fXmlFile = new File(Paths.get(System.getProperty("user.home"), "TronStudio", "record", "network.xml").toString());
            if( !fXmlFile.exists() ) return;
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(fXmlFile);
            doc.getDocumentElement().normalize();
            NodeList nList = doc.getElementsByTagName("network");

            for (int temp = 0; temp < nList.getLength(); temp++) {

                Node nNode = nList.item(temp);


                if (nNode.getNodeType() == Node.ELEMENT_NODE) {

                    Element eElement = (Element) nNode;
                    saved_network.put(eElement.getAttribute("id").trim(), new NetWorkEnvironment(eElement.getElementsByTagName("ip").item(0).getTextContent().trim(),
                            Integer.parseInt(eElement.getElementsByTagName("port").item(0).getTextContent().trim())));

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static {
        if (testAccountAddress.length == testAccountPrivateKey.length) {
            for (int i = 0; i < testAccountAddress.length; ++i) {
                testAccount.put(testAccountAddress[i], testAccountPrivateKey[i]);
            }
        }

        setSavedNetWork();
        currentRpcIp = saved_network.get(currentEnvironment).url;
        currentRpcPort = saved_network.get(currentEnvironment).port;
    }

    public static SolidityCompiler.Result getSolidityCompilerResult(String contractName) {
        return solidityCompilerResultMap.get(contractName);
    }

    public static void setSolidityCompilerResult(String contractName, SolidityCompiler.Result solidityCompilerResult) {
        solidityCompilerResultMap.put(contractName, solidityCompilerResult);
        currentSolidityCompilerResult.set(solidityCompilerResult);
    }



}