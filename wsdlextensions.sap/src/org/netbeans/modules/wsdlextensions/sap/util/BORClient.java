package org.netbeans.modules.wsdlextensions.sap.util;

import com.sap.conn.jco.AbapException;
import com.sap.conn.jco.JCoDestination;
import com.sap.conn.jco.JCoDestinationManager;
import com.sap.conn.jco.JCoException;
import com.sap.conn.jco.JCoFunction;
import com.sap.conn.jco.JCoRecordMetaData;
import com.sap.conn.jco.JCoRepository;
import com.sap.conn.jco.JCoTable;
import com.sap.conn.jco.ext.DestinationDataProvider;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.wsdlextensions.sap.model.BAPIMethod;
import org.netbeans.modules.wsdlextensions.sap.model.BAPIObject;
import org.netbeans.modules.wsdlextensions.sap.model.IDocType;
import org.netbeans.modules.wsdlextensions.sap.model.RFC;
import org.netbeans.modules.wsdlextensions.sap.model.SapConnection;

/**
 *
 * @author tli
 * @author jqian
 */
public class BORClient {

    private SapConnection connection;
    private JCoDestination rfcClient;
    private JCoRepository rfcRepository;
    static String DESTINATION_NAME1 = "ABAP_AS_WITHOUT_POOL";
    //static String DESTINATION_NAME2 = "ABAP_AS_WITH_POOL";

    public BORClient(SapConnection connection) throws JCoException {
        this.connection = connection;

        init(connection);

        rfcClient = JCoDestinationManager.getDestination(DESTINATION_NAME1);
        rfcRepository = rfcClient.getRepository();

//        String rel = rfcClient.getAttributes().getPartnerRelease();
//        String lang = rfcClient.getLanguage();
//        System.out.println("SAP rel " + rel + ", " + lang);
    }

    private static void init(SapConnection connection) {

        Properties connectProperties = new Properties();

        connectProperties.setProperty(DestinationDataProvider.JCO_ASHOST, connection.getApplicationServer());
        connectProperties.setProperty(DestinationDataProvider.JCO_SYSNR, connection.getSystemNumber());
        connectProperties.setProperty(DestinationDataProvider.JCO_CLIENT, connection.getClientNumber());
        connectProperties.setProperty(DestinationDataProvider.JCO_USER, connection.getUserName());
        connectProperties.setProperty(DestinationDataProvider.JCO_PASSWD, connection.getPassword());
        connectProperties.setProperty(DestinationDataProvider.JCO_LANG, connection.getLanguage());

        createDestinationDataFile(DESTINATION_NAME1, connectProperties);

        //connectProperties.setProperty(DestinationDataProvider.JCO_POOL_CAPACITY, "3");
        //connectProperties.setProperty(DestinationDataProvider.JCO_PEAK_LIMIT, "10");
        //createDestinationDataFile(DESTINATION_NAME2, connectProperties);
    }

    private static void createDestinationDataFile(String destinationName,
            Properties connectProperties) {

        File destCfg = new File(destinationName + ".jcoDestination");
        try {
            FileOutputStream fos = new FileOutputStream(destCfg, false);
            connectProperties.store(fos, "for tests only !");
            fos.close();
        } catch (Exception e) {
            throw new RuntimeException("Unable to create the destination files ", e);
        }
    }

    public BAPIObject buildBOModel() {
        BAPIObject rootObject = null;

        // Mapping BAPIObject ID to BAPIObject.
        Map<String, BAPIObject> id2ObjectMap = new HashMap<String, BAPIObject>();

        // Mapping BAPIObject type (its name) to a list of BAPIObjects.
        // Typically, there is only one BAPIObject per type.
        // In some cases, there could be more than one BAPIObject per type.
        // For example,
        //
        // BAPIObject [id=001396, name=CA-GTF-XF, extName=shortText=SAP Expert Finder] and
        // BAPIObject [id=001389, name=CA-GTF-XF, extName=shortText=SAP Expert Finder]
        //
        // BAPIObject [id=001397, name=EXPERTPROF, extName=ExpertProfileshortText=Expert Profile] and
        // BAPIObject [id=001390, name=EXPERTPROF, extName=ExpertProfileshortText=Expert Profile]
        //
        // BAPIObject [id=001418, name=CA-GTF-BRF, extName=shortText=Business Rule Framework] and
        // BAPIObject [id=001002, name=CA-GTF-BRF, extName=shortText=Business Rule Framework]
        //
        // BAPIObject [id=001419, name=BRF1000, extName=BRFObjectshortText=General BRF Object] and
        // BAPIObject [id=001003, name=BRF1000, extName=BRFObjectshortText=General BRF Object]
        //
        Map<String, List<BAPIObject>> objType2ObjectMap =
                new HashMap<String, List<BAPIObject>>();

        // 1. Get the BOR tree node list and build the BAPIObject "tree".
        JCoTable borTree = invokeFunction("RPY_BOR_TREE_INIT", "BOR_TREE");
        for (int i = 0; i < borTree.getNumRows(); i++) {
            borTree.setRow(i);

            String id = borTree.getString("ID");
            //String type = borTree.getString("TYPE");
            //String level = borTree.getString("LEVEL");
            String parentID = borTree.getString("PARENT");
            //String child = borTree.getString("CHILD");
            String name = borTree.getString("NAME");    // use name as object type
            String extName = borTree.getString("EXT_NAME");
            String description = borTree.getString("SHORT_TEXT");

            BAPIObject parentObject = id2ObjectMap.get(parentID);

            BAPIObject object = new BAPIObject(id, name, extName, description);
            id2ObjectMap.put(id, object);

            List<BAPIObject> objects = objType2ObjectMap.get(name);
            if (objects == null) {
                objects = new ArrayList<BAPIObject>(1);
                objType2ObjectMap.put(name, objects);
//            } else {
//                System.out.println("INFO: Same object type detected: " + object + " and " + objects.get(0));
            }
            objects.add(object);

            if (parentObject != null) {
                parentObject.addChild(object);
            } else {
                rootObject = object;
            }
        }

        // 2. Get the BAPI method list and attach all BAPI methods associated  
        //    with a BO node's object type to the BO node.
        JCoTable apiMethods = invokeFunction("SWO_QUERY_API_METHODS", "API_METHODS");
        for (int i = 0; i < apiMethods.getNumRows(); i++) {
            apiMethods.setRow(i);

            String objType = apiMethods.getString("OBJTYPE");
            String method = apiMethods.getString("METHOD");
            String methodName = apiMethods.getString("METHODNAME");
            String function = apiMethods.getString("FUNCTION");
            String shortText = apiMethods.getString("SHORTTEXT");

            BAPIMethod bapiMethod =
                    new BAPIMethod(objType, method, methodName, function, shortText);
            List<BAPIObject> objects = objType2ObjectMap.get(objType);
            if (objects == null) {
//                System.out.println("WARNING: No object found for method " +
//                        bapiMethod + " (objtype:" + objType + ")");
            } else {
                for (BAPIObject object : objects) {
                    object.addMethod(bapiMethod);
                }
            }
        }

        // 3. Filter BOR tree leaf nodes according to its list of BAPI methods.
        //    All leaf nodes with empty BAPI method list and intermediate nodes
        //    that have no valid leaf BO node are removed.
        for (List<BAPIObject> objects : objType2ObjectMap.values()) {
            for (BAPIObject object : objects) {
                if (!object.childOrSelfHasMethods()) {
                    object.getParent().removeChild(object);
//                System.out.println("INFO: Removing object " + object.getName() +
//                        " because none of its children or itself has methods defined.");
                }
            }
        }

        return rootObject;
    }

    public List<RFC> getRFCList() {
        List<RFC> ret = new ArrayList<RFC>();

        JCoTable table = invokeFunction("RFC_FUNCTION_SEARCH_WITHGROUP", 0); //"RFC_GROUP");TMP
        for (int i = 0; i < table.getNumRows(); i++) {
            table.setRow(i);

            String funcName = table.getString("FUNCNAME");
            String groupName = table.getString("GROUPNAME");
            String sText = table.getString("STEXT");
            String devClass = table.getString("DEVCLASS");

            RFC rfc = new RFC(funcName, groupName, sText, devClass);
            ret.add(rfc);
        }

        return ret;
    }

    private JCoTable invokeFunction(String functionName, /*String TMP*/Object resultTableName) {
        try {
            JCoFunction function = getRfcRepository().getFunction(functionName);
            if (function == null) {
                throw new RuntimeException(functionName + " not found in SAP.");
            }

            try {
                function.execute(getRfcClient());
            } catch (AbapException e) {
                System.out.println(e.toString());
                return null;
            }

            //System.out.println("finished: \n");
            //System.out.println(function.toXML());

            if (resultTableName instanceof String) {
                return function.getTableParameterList().getTable((String) resultTableName);
            } else {
                return function.getTableParameterList().getTable((Integer) resultTableName);
            }
        } catch (JCoException ex) {
            ex.printStackTrace();
        }

        return null;
    }


    /*
    Input-0: I...X STRUCTURE/RPYLBBM 0, FILTER_MISCELLANEOUS, null
    Input-1: I...X STRUCTURE/RPYLBBO 0, FILTER_OBJECT_TYPES, Filter for object types
    Input-2: I...X STRUCTURE/RPYLBBR 0, FILTER_RELATIONSHIPS, Filter for relationships
    Table-0: ..... TABLE/RPYLBBT 0, BOR_TREE, null
    Table-1: ..... TABLE/RPYGSER 0, ERROR_SET, Output: Error messages
    Table-2: ..... TABLE/RPYLBBI 0, OBJECT_TYPE_ID_SET, null

    <RPY_BOR_TREE_INIT>
    <INPUT>
    <FILTER_MISCELLANEOUS>
    <COMPHIER></COMPHIER>
    </FILTER_MISCELLANEOUS>
    <FILTER_OBJECT_TYPES>
    <ALLOBJTYPS></ALLOBJTYPS>
    <MODELLED></MODELLED>
    <IMPLEMNTED></IMPLEMNTED>
    <RELEASED></RELEASED>
    <OBSOLETE></OBSOLETE>
    <BUSOBJECT></BUSOBJECT>
    <ORGTYPES></ORGTYPES>
    <OTHERS></OTHERS>
    <LOCAL></LOCAL>
    <GLOBAL></GLOBAL>
    <WITH_INTF></WITH_INTF>
    </FILTER_OBJECT_TYPES>
    <FILTER_RELATIONSHIPS>
    <ALLRELSHIP></ALLRELSHIP>
    <INHSUB></INHSUB>
    <COMPPART></COMPPART>
    </FILTER_RELATIONSHIPS>
    </INPUT>
    <TABLES>
    <BOR_TREE></BOR_TREE>
    <ERROR_SET></ERROR_SET>
    <OBJECT_TYPE_ID_SET></OBJECT_TYPE_ID_SET>
    </TABLES>
    </RPY_BOR_TREE_INIT>
     */
    public void getBorTree() {
        try {
            JCoFunction function = getRfcRepository().getFunction("RPY_BOR_TREE_INIT");
            if (function == null) {
                throw new RuntimeException("RPY_BOR_TREE_INIT not found in SAP.");
            }

            // function.getImportParameterList().getStructure("FILTER_OBJECT_TYPES").setValue("ALLOBJTYPS", "X");

            try {
                function.execute(getRfcClient());
            } catch (AbapException e) {
                System.out.println(e.toString());
                return;
            }

            //System.out.println("finished: \n");
            //System.out.println(function.toXML());

            JCoTable borTree = function.getTableParameterList().getTable("BOR_TREE");
            printRecordMetaData(borTree);

            for (int i = 0; i < borTree.getNumRows(); i++) {
                borTree.setRow(i);
                System.out.println(i + ": id=" + borTree.getString("ID") + ", type=" + borTree.getString("TYPE") + ", level=" + borTree.getString("LEVEL") + ", parent=" + borTree.getString("PARENT") + ", child=" + borTree.getString("CHILD") + ", name=" + borTree.getString("NAME") + ", short_text=" + borTree.getString("SHORT_TEXT") + ", extname=" + borTree.getString("EXT_NAME") + ", intid=" + borTree.getString("INT_ID") + ", is_busobj=" + borTree.getString("IS_BUSOBJ"));
            }
        } catch (JCoException ex) {
            ex.printStackTrace();
        }
    }

    /*
    Input-0: I...X CHAR/SYST 1, LANGUAGE, Language in which Texts are Read
    Input-1: I...X CHAR/SWOTBASDAT 32, OBJECT_NAME, null
    Input-2: I...X CHAR/RPYBOGF 1, WITH_IMPLEMENTED, null
    Input-3: I...X CHAR/RPYBOGF 1, WITH_INTERNAL_API_METHODS, Language in which Texts are Read
    Input-4: I...X CHAR/RPYBOGF 1, WITH_OBJECT_NAMES, Read object names
    Input-5: I...X CHAR/RPYBOGF 1, WITH_TEXTS, Read texts as well
    Table-0: ..... TABLE/SWOTFIND 0, OBJTYPES, null
    <OBJTYPES>
    <OBJTYPE, CHAR, 20/>
    <OBJNAME, CHAR, 64/>
    <SHORTTEXT, CHAR, 160/>
    </OBJTYPES>
     */
    public void getBoTypes() {
        try {
            JCoFunction function = getRfcRepository().getFunction("SWO_QUERY_API_OBJTYPES");
            if (function == null) {
                throw new RuntimeException("SWO_QUERY_API_OBJTYPES not found in SAP.");
            }

            try {
                function.execute(getRfcClient());
            } catch (AbapException e) {
                System.out.println(e.toString());
                return;
            }

            //System.out.println("finished: \n");
            //System.out.println(function.toXML());

            JCoTable table = function.getTableParameterList().getTable("OBJTYPES");
            printRecordMetaData(table);

            for (int i = 0; i < table.getNumRows(); i++) {
                table.setRow(i);
                System.out.println(i + ": objtype=" + table.getString("OBJTYPE") +
                        ", objname=" + table.getString("OBJNAME") +
                        ", shorttext=" + table.getString("SHORTTEXT"));
            }
        } catch (JCoException ex) {
            ex.printStackTrace();
        }
    }

    /*
    Input-0: I...X CHAR/SYST 1, LANGUAGE, Language in which texts are read
    Input-1: I...X CHAR/SWOTLV 32, METHOD, Method   (if blank: all)
    Input-2: I...X CHAR/SWOTBASDAT 32, OBJECT_NAME, Object name
    Input-3: I...X CHAR/SWOTBASDAT 10, OBJTYPE, Object type (if blank: all)
    Input-4: I...X CHAR/RPYBOGF 1, WITH_IMPL_METHODS, Read methods in 'implemented' status as well?
    Input-5: I...X CHAR/RPYBOGF 1, WITH_INTERNAL_API_METHODS, Method   (if blank: all)
    Input-6: I...X CHAR/RPYBOGF 1, WITH_OBJECT_NAMES, Read texts as well
    Input-7: I...X CHAR/RPYBOGF 1, WITH_TEXTS, Read texts as well
    Table-0: ..... TABLE/SWOTMETHOD 0, API_METHODS, List of API methods
    <API_METHODS>
    <OBJTYPE, CHAR, 20/>
    <METHOD, CHAR, 64/>
    <METHODNAME, CHAR, 64/>
    <DESCRIPT, CHAR, 40/>
    <SHORTTEXT, CHAR, 160/>
    <FUNCTION, CHAR, 60/>
    <CLASSVERB, CHAR, 2/>
    <APITYPE, CHAR, 2/>
    <OBJECTNAME, CHAR, 64/>
    </API_METHODS>
     */
    public void getBoMethods() {
        try {
            JCoFunction function = getRfcRepository().getFunction("SWO_QUERY_API_METHODS");
            if (function == null) {
                throw new RuntimeException("SWO_QUERY_API_METHODS not found in SAP.");
            }

            try {
                function.execute(getRfcClient());
            } catch (AbapException e) {
                System.out.println(e.toString());
                return;
            }

            //System.out.println("finished: \n");
            //System.out.println(function.toXML());

            JCoTable apiMethods = function.getTableParameterList().getTable("API_METHODS");
            printRecordMetaData(apiMethods);

            for (int i = 0; i < apiMethods.getNumRows(); i++) {
                apiMethods.setRow(i);
                System.out.println(i + ": objtype=" + apiMethods.getString("OBJTYPE") +
                        ", method=" + apiMethods.getString("METHOD") +
                        ", methodname=" + apiMethods.getString("METHODNAME") +
                        ", function=" + apiMethods.getString("FUNCTION") +
                        ", shorttext=" + apiMethods.getString("SHORTTEXT") +
                        ", descript=" + apiMethods.getString("DESCRIPT") +
                        ", classverb=" + apiMethods.getString("CLASSVERB") +
                        ", apitype=" + apiMethods.getString("APITYPE") +
                        ", objectname=" + apiMethods.getString("OBJECTNAME"));
            }
        } catch (JCoException ex) {
            ex.printStackTrace();
        }
    }

    public void getRFCs() {
        try {
            JCoFunction function = getRfcRepository().getFunction("RFC_FUNCTION_SEARCH_WITHGROUP");
            if (function == null) {
                throw new RuntimeException("RFC_FUNCTION_SEARCH_WITHGROUP not found in SAP.");
            }

            try {
                function.execute(getRfcClient());
            } catch (AbapException e) {
                System.out.println(e.toString());
                return;
            }

            //System.out.println("finished: \n");
            //System.out.println(function.toXML());

            JCoTable apiMethods = function.getTableParameterList().getTable(0);
            //printRecordMetaData(apiMethods);

            for (int i = 0; i < apiMethods.getNumRows(); i++) {
                apiMethods.setRow(i);
                System.out.println(i + ": funcname=" + apiMethods.getString("FUNCNAME") +
                        ", groupname=" + apiMethods.getString("GROUPNAME") +
                        ", appl=" + apiMethods.getString("APPL") +
                        ", host=" + apiMethods.getString("HOST") +
                        ", stext=" + apiMethods.getString("STEXT") +
                        ", devclass=" + apiMethods.getString("DEVCLASS") +
                        ", compname=" + apiMethods.getString("COMPNAME") +
                        ", compid=" + apiMethods.getString("COMPID") +
                        ", freedate=" + apiMethods.getString("FREEDATE") +
                        ", intrel=" + apiMethods.getString("INTREL") +
                        ", extrel=" + apiMethods.getString("EXTREL") +
                        ", obsolete=" + apiMethods.getString("OBSOLETE"));
            }
        } catch (JCoException ex) {
            ex.printStackTrace();
        }
    }

    private void printRecordMetaData(JCoTable table) {
        System.out.println("Record MetaData: ");
        JCoRecordMetaData metaData = table.getRecordMetaData();
        for (int j = 0; j < table.getNumColumns(); j++) {
            System.out.println("    column" + j + ":" + metaData.getName(j));
        }
    }

    public void getIDocTypes() {
        try {
            JCoDestination destination = getRfcClient();

            List<IDocType> iDocTypes =
                    new IDocUtil().getIDocTypes(destination, "620");

            for (IDocType iDocType : iDocTypes) {
                System.out.println(iDocType);
            }
        } catch (Exception ex) {
            Logger.getLogger(BORClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * @return the rfcClient
     */
    public JCoDestination getRfcClient() {
        return rfcClient;
    }

    /**
     * @return the rfcRepository
     */
    public JCoRepository getRfcRepository() {
        return rfcRepository;
    }

    public SapConnection getConnection() {
        return connection;
    }

    public static void main(String[] args) throws JCoException, Exception {

        String appServer = "ecc6-zone2";
        String systemID = "007";
        String systemNumber = "00";
        String language = "EN";
        String clientNumber = "001"; //800";
        String userName = "PS1";
        String password = "ONLY4RD";

        SapConnection connection =
                new SapConnection("FOO", systemID, appServer, systemNumber,
                null, language, clientNumber, userName, password);

        BORClient client = new BORClient(connection);

//        System.out.println("**********************************");
//        System.out.println("getBorTree()");
//        client.getBorTree();
//
//        System.out.println("**********************************");
//        System.out.println("getBoMethods()");
//        client.getBoMethods();
//
//        System.out.println("**********************************");
//        System.out.println("getBoTypes()");
//        client.getBoTypes();
//
//        System.out.println("**********************************");
//        System.out.println("genWSDL()");
//        WSDLGenerator wg = new WSDLGenerator(client.getRfcClient(),
//                                             client.getRfcRepository());
//        //wg.genRfcWsdl("BAPI_FLIGHT_GETDETAIL", null);
//        wg.genRfcWsdl("BAPI_ACC_BILLING_CHECK", null);

        System.out.println("************************************");
        System.out.println("getRFCs()");
        client.getRFCs();

//        System.out.println("************************************");
//        System.out.println("getIDocTypes()");
//        client.getIDocTypes();
    }
}
