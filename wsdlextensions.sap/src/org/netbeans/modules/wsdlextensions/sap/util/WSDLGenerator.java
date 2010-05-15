/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.wsdlextensions.sap.util;

import org.netbeans.modules.wsdlextensions.sap.model.IDocSegmentDef;
import com.sap.conn.jco.AbapException;
import com.sap.conn.jco.JCoDestination;
import com.sap.conn.jco.JCoException;
import com.sap.conn.jco.JCoFunction;
import com.sap.conn.jco.JCoFunctionTemplate;
import com.sap.conn.jco.JCoListMetaData;
import com.sap.conn.jco.JCoRecordMetaData;
import com.sap.conn.jco.JCoRepository;
import com.sap.conn.jco.JCoTable;
import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.netbeans.modules.wsdlextensions.sap.model.BAPIMethod;
import org.netbeans.modules.wsdlextensions.sap.model.BAPIObject;
import org.netbeans.modules.wsdlextensions.sap.model.SapConnection;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author tli
 */
public class WSDLGenerator {

    public static final String XSD_NS_URI = "http://www.w3.org/2001/XMLSchema";
    public static final String XSD_NS_PREFIX = "xsd";
    public static final String XSD_SCHEMA = "schema";
    public static final String XSD_ELEMENT = "element";
    public static final String XSD_ATTRIBUTE = "attribute";
    public static final String XSD_SIMPLETYPE = "simpleType";
    public static final String XSD_COMPLEXTYPE = "complexType";
    public static final String XSD_SEQUENCE = "sequence";
    public static final String XSD_RESTRICTION = "restriction";
    public static final String XSD_MAXLENGTH = "maxLength";
    public static final String XSD_PATTERN = "pattern";
    public static final String XSD_TOTALDIGITS = "totalDigits";
    public static final String XSD_FRACTIONDIGITS = "fractionDigits";
    public static final String WSDL_NS_URI = "http://schemas.xmlsoap.org/wsdl/";
    public static final String WSDL_NS_PREFIX = "wsdl";
    public static final String WSDL_DEFINITIONS = "definitions";
    public static final String WSDL_MESSAGE = "message";
    public static final String WSDL_PART = "part";
    public static final String WSDL_PORTTYPE = "portType";
    public static final String WSDL_OPERATION = "operation";
    public static final String WSDL_INPUT = "input";
    public static final String WSDL_OUTPUT = "output";
    public static final String WSDL_TYPES = "types";
    public static final String WSDL_BINDING = "binding";
    public static final String WSDL_PORT = "port";
    public static final String WSDL_SERVICE = "service";
    public static final String WSDL_NAME = "name";
    public static final String PLNK_NS_URI = "http://schemas.xmlsoap.org/ws/2003/05/partner-link/";
    public static final String PLNK_PLT = "partnerLinkType";
    public static final String PLNK_ROLE = "role";
    public static final String PLNK_PT = "portType";
    public static final String SAP_NS_URI = "http://schemas.sun.com/jbi/wsdl-extensions/sap/";
    public static final String SAP_BINDING = "binding";
    public static final String SAP_ADDRESS = "address";
    public static final String SAP_FMOPERATION = "fmoperation";
    public static final String SAP_IDOCOPERATION = "idocoperation";
    public static final String SAP_CLIENTPARAMS = "clientparams";
    public static final String SAP_SERVERPARAMS = "serverparams";
    static String SAPXSD_NS_URI_PREFIX = "http://ns.sap.connector/xsd/";
    static String SAPWSDL_NS_URI_PREFIX = "http://ns.sap.connector/wsdl/";
    static String BO_NAME = "bo";
    static String BAPI_NAME = "bapi";
    static String SAPXSD_NS_URI = SAPXSD_NS_URI_PREFIX + BAPI_NAME;
    static String SAPWSDL_NS_URI = SAPWSDL_NS_URI_PREFIX + BAPI_NAME;
    static String GENXSD_NS = ""; // "ns0:"; // null the XSD namespace
    static String GENTNS_NS = "tns:";
    static Map<String, List<Element>> complexType = new HashMap<String, List<Element>>();
    static Map<String, Set<String>> typeMap = new HashMap<String, Set<String>>();

    static {
        typeMap.put("INVALID", new HashSet<String>());
        typeMap.put("CHAR", new HashSet<String>());     // String
        typeMap.put("NUM", new HashSet<String>());       // String
        typeMap.put("BYTE", new HashSet<String>());      // byte[]
        typeMap.put("XSTRING", new HashSet<String>());   // byte[]
        typeMap.put("BCD", new HashSet<String>());       // java.math.BigDecimal
        typeMap.put("INT", new HashSet<String>());       // int
        typeMap.put("FLOAT", new HashSet<String>());     // double
        typeMap.put("DATE", new HashSet<String>());      // java.sql.Date, Date field (YYYYMMDD)
        typeMap.put("TIME", new HashSet<String>());      // java.sql.Time, Time field (hhmmss)
        typeMap.put("STRING", new HashSet<String>());    // String
        typeMap.put("INT2", new HashSet<String>());      // short
        typeMap.put("INT1", new HashSet<String>());      // short
        // We will never use reset values for structs and tables
        typeMap.put("STRUCTURE", new HashSet<String>()); // struct
        typeMap.put("TABLE", new HashSet<String>());     // list of struct
    }
    // IDOC wsdl static variables
    static String IDOC_NAME = "idoc";
    static final String IDOC_DC40 = "EDI_DC40";
    static final String IDOC_DC40_TYPE = "EDI_DC40_TYPE";
    static final String IDOC_ELEMENT = "IDOC";
    static final String IDOC_ELEMENT_TYPE = "IDOC_TYPE";
    static Map<String, List<Element>> complexIDocType = new HashMap<String, List<Element>>();
    static Map<String, Set<String>> typeIDocMap = new HashMap<String, Set<String>>();

    static {
        // types used in idoc
        typeIDocMap.put("ACCP", new HashSet<String>());       // String
        typeIDocMap.put("CHAR", new HashSet<String>());       // String
        typeIDocMap.put("CLNT", new HashSet<String>());       // String
        typeIDocMap.put("CUKY", new HashSet<String>());       // String
        typeIDocMap.put("CURR", new HashSet<String>());       // String
        typeIDocMap.put("DATS", new HashSet<String>());       // String
        typeIDocMap.put("DEC", new HashSet<String>());       // String
        typeIDocMap.put("FLTP", new HashSet<String>());       // String
        typeIDocMap.put("INT1", new HashSet<String>());       // String
        typeIDocMap.put("INT2", new HashSet<String>());       // String
        typeIDocMap.put("INT4", new HashSet<String>());       // String
        typeIDocMap.put("LANG", new HashSet<String>());       // String
        typeIDocMap.put("LCHR", new HashSet<String>());       // String
        typeIDocMap.put("LRAW", new HashSet<String>());       // String
        typeIDocMap.put("NUMC", new HashSet<String>());       // String
        typeIDocMap.put("PREC", new HashSet<String>());       // String
        typeIDocMap.put("QUAN", new HashSet<String>());       // String
        typeIDocMap.put("RAW", new HashSet<String>());       // String
        typeIDocMap.put("RAWSTRING", new HashSet<String>());       // String
        typeIDocMap.put("SSTRING", new HashSet<String>());       // String
        typeIDocMap.put("STRING", new HashSet<String>());       // String
        typeIDocMap.put("TIMS", new HashSet<String>());       // String
        typeIDocMap.put("UNIT", new HashSet<String>());       // String
        typeIDocMap.put("VARC", new HashSet<String>());       // String
    }
    private JCoDestination rfcClient;
    private JCoRepository rfcRepository;
    private SapConnection connection;

    /* Bytes per character used for the partner system */
    private int pBPC;
    private Document doc;

    public WSDLGenerator(JCoDestination client, JCoRepository repository,
            SapConnection connection) {
        try {
            // rfcClient = JCoDestinationManager.getDestination(dest);
            // rfcRepository = rfcClient.getRepository();
            rfcClient = client;
            rfcRepository = repository;
            this.connection = connection;

            //String rel = rfcClient.getAttributes().getPartnerRelease();
            //String lang = rfcClient.getLanguage().substring(0, 1);

            pBPC = 1;
            if (rfcRepository.isUnicode()) {
                pBPC = 2;
            }

            // System.out.println("SAP rel " + rel + ", " + lang + ", BpC " + pBPC);
        } catch (Exception ex) {
            ex.printStackTrace();
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

    protected List<Element> genComplexParam(String paramName, int order,
            boolean paramOptional, String tableName,
            int paramLength, int charLength)
            throws Exception {

        List<Element> eList = new ArrayList<Element>();

        try {
            //directly read the metadata of the table/structure from the SAP Repository
            JCoRecordMetaData tabMetadata = rfcRepository.getRecordMetaData(tableName);

            if (tabMetadata == null) {
                throw new Exception("genComplexParam(): No field table/structure info found. Could not get MetaData for Table/Structure");
            }

            // Iterate through rows
            for (int i = 0, nrows = tabMetadata.getFieldCount(); i < nrows; i++) {
                String pName = tabMetadata.getName(i).toUpperCase();
                String pElmType = tabMetadata.getTypeAsString(i);
                //String pAbap4Type = (String) rfcTypeMap.get(pElmType);
                /*
                System.out.println(pName +", " + pElmType
                + ", (Bcd/Float) " + JCoMetaData.TYPE_BCD + "/" + JCoMetaData.TYPE_FLOAT
                + ", " + tabMetadata.getType(i)
                + ", " + tabMetadata.getLength(i)
                + ", " + tabMetadata.getByteLength(i)
                + ", " + tabMetadata.getUnicodeByteLength(i)
                + ", " + tabMetadata.getDecimals(i));
                 */
                int pLength = 0;

                if (this.pBPC == 2) {
                    pLength = tabMetadata.getUnicodeByteLength(i);
                } else {
                    pLength = tabMetadata.getByteLength(i);
                }
                int pcharLen = tabMetadata.getLength(i);
                String pText = tabMetadata.getDescription(i);
                String tabName = tabMetadata.getRecordTypeName(i);
                //In case of ABAP STRING/SSTRING/XSTRING data type, set charLength=-1 to
                //display charLength as variable in OTD editor.
//                if(tabMetadata.TYPE_STRING==tabMetadata.getType(i) ||
//                        tabMetadata.TYPE_XSTRING==tabMetadata.getType(i)){
//                    pcharLen=-1;
//                }
                //The  getOffset() method is deprecated. Couldn't find any other to get the offset
                //Method was available till JCO version 2.1.5.(last checked)
                //Method could be dropped in future versions of JCO
                //int pOffset = tabMetadata.ggetOffset(i);
                //JCO3 This variable is not used anywhere. Initailizing it to 0 to avoid signature changes in other methods.
                int pOffset = 0;
                int pDecimals = tabMetadata.getDecimals(i);

                //Check if the structure/table (complex type) has a nesting
                if (pElmType == null || pElmType.equals("TABLE") || pElmType.equals("STRUCTURE")) {
//                if (pAbap4Type.trim().equalsIgnoreCase("u")
//                || pAbap4Type.trim().equalsIgnoreCase("h")
//                || pAbap4Type.trim().equalsIgnoreCase("v")    ) {
                    String pTypeName = (String) tabMetadata.getRecordTypeName(i);
                    // eList.add(getElement(pName, pElmType, "" + pName));

                    List<Element> param = genComplexParam(pName, order++, paramOptional, tabName, pLength, pcharLen);
                    complexType.put(pTypeName, param);
                    if (pElmType.equals("TABLE")) {
                        List<Element> tParam = new ArrayList<Element>();
                        tParam.add(getElement("item", pTypeName, "0", "unbounded"));
                        complexType.put("TABLE_" + pTypeName, tParam);
                        eList.add(getElement(paramName, "TABLE_" + pTypeName, null, null));
                    } else { // a structure
                        eList.add(getElement(paramName, pTypeName));
                    }
                } else {
                    // printElement(pName, pElmType, "" + pLength);
                    if (pDecimals > 0) {
                        eList.add(getElement(pName, pElmType, "" + pLength + "." + pDecimals));
                    } else {
                        eList.add(getElement(pName, pElmType, "" + pLength));
                    }
                }
            }
        } catch (AbapException e1) {
            throw new Exception(MessageFormat.format("ERROR_DISCOVER_COMPLEXABAP",
                    new Object[]{e1.getLocalizedMessage()}), e1);
        } catch (JCoException e2) {
            throw new Exception(MessageFormat.format("ERROR_DISCOVER_COMPLEXABAP",
                    new Object[]{e2.getLocalizedMessage()}), e2);
        } catch (Exception e3) {
            throw new Exception(MessageFormat.format("ERROR_DISCOVER_COMPLEXABAP",
                    new Object[]{e3.getLocalizedMessage()}), e3);
        }
        return eList;
    }

    private List<Element> genItemDetail(JCoListMetaData list, String group) {
        List<Element> eList = new ArrayList<Element>();
        // System.out.println("<" + group + ">");
        try {
            int size = (list != null) ? list.getFieldCount() : 0;
            for (int i = 0; i < size; i++) {
                // String name = list.getName(i);
                // System.out.println(group + "-" + i + ": " + list.getRecordMetaData(i));

                String pType = (String) list.getTypeAsString(i);
                String paramName = (String) list.getName(i);
                int order = 0;
                boolean paramOptional = list.isOptional(i);
                String sOptional = (paramOptional) ? "0" : null;
                int paramLength;
                if (pBPC == 2) {
                    paramLength = list.getUnicodeByteLength(i);
                } else {
                    paramLength = list.getByteLength(i);
                }

                int charLength = pBPC == 2 && (list.getType(i) == list.TYPE_STRUCTURE || list.getType(i) == list.TYPE_TABLE)
                        ? paramLength / pBPC : list.getLength(i);
                if (list.TYPE_STRING == list.getType(i) || list.TYPE_XSTRING == list.getType(i)) {
                    charLength = -1;
                }

                if (null == pType || pType.equals("TABLE") || pType.equals("STRUCTURE")) {
                    //<xsd:element name="Availibility" type="tns:Structure_Bapisflava"/>
                    //<xsd:element name="Return" type="tns:Table_bapiret2"/>
                    String pTypeName = (String) list.getRecordTypeName(i);
                    // eList.add(getElement(paramName, pType, "" + pTypeName));

                    String tableName = list.getRecordTypeName(i).toUpperCase();
                    List<Element> param = genComplexParam(paramName, order++, paramOptional, tableName, paramLength, charLength);
                    complexType.put(pTypeName, param);
                    if (pType.equals("TABLE")) {
                        List<Element> tParam = new ArrayList<Element>();
                        tParam.add(getElement("item", pTypeName, "0", "unbounded"));
                        complexType.put("TABLE_" + pTypeName, tParam);
                        eList.add(getElement(paramName, "TABLE_" + pTypeName, sOptional, null));
                    } else { // a structure
                        eList.add(getElement(paramName, pTypeName, sOptional, null));
                    }
                } else {
                    eList.add(getElement(list.getName(i), pType, "" + list.getLength(i), sOptional, null));
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return eList;
    }

    /**
     * Create an element for the given name and type parameters
     *
     * @param name element name
     * @param type element type
     * @param len element length
     * @return generated XSD element
     */
    Element getElement(String name, String type, String len) {
        // <xsd:element name="Type" type="n0:char1"/>
        Element elm = doc.createElement("xsd:" + XSD_ELEMENT);
        elm.setAttribute("name", name);

        if (type.equals("INT")) {
            elm.setAttribute("type", GENXSD_NS + type);
            typeMap.get(type).add("4");
        } else if (type.equals("TIME")) {
            elm.setAttribute("type", GENXSD_NS + type);
            typeMap.get(type).add("8");
        } else if (type.equals("DATE")) {
            elm.setAttribute("type", GENXSD_NS + type);
            typeMap.get(type).add("10");
        } else {
            elm.setAttribute("type", GENXSD_NS + type + "_" + len);
            typeMap.get(type).add(len);
        }
        return elm;
    }

    Element getElement(String name, String type, String len, String min, String max) {
        Element elm = getElement(name, type, len);
        if (min != null) {
            elm.setAttribute("minOccurs", min);
        }
        if (max != null) {
            elm.setAttribute("maxOccurs", max);
        }
        return elm;
    }

    Element getElement(String name, String type) {
        // <xsd:element name="Type" type="n0:char1"/>
        Element elm = doc.createElement("xsd:" + XSD_ELEMENT);
        elm.setAttribute("name", name);
        elm.setAttribute("type", GENXSD_NS + type);
        return elm;
    }

    Element getElement(String name, String type, String min, String max) {
        // <xsd:element name="Type" type="n0:char1"/>
        Element elm = doc.createElement("xsd:" + XSD_ELEMENT);
        elm.setAttribute("name", name);
        elm.setAttribute("type", GENXSD_NS + type);
        if (min != null) {
            elm.setAttribute("minOccurs", min);
        }
        if (max != null) {
            elm.setAttribute("maxOccurs", max);
        }
        return elm;
    }

    /**
     * Generate a Sub XSD type based on length and pattern restritions
     *
     * @param elm parent element
     * @param base base XSD type
     * @param value max length
     * @param mask pattern
     */
    void genSubType(Element elm, String base, String value, String mask) {
        Element rest = doc.createElement("xsd:" + XSD_RESTRICTION);
        rest.setAttribute("base", "xsd:" + base);
        elm.appendChild(rest);
        if (value != null) {
            Element len = doc.createElement("xsd:" + XSD_MAXLENGTH);
            len.setAttribute("value", value);
            rest.appendChild(len);
        }
        if (mask != null) {
            Element pattern = doc.createElement("xsd:" + XSD_PATTERN);
            pattern.setAttribute("value", mask);
            rest.appendChild(pattern);
        }
    }

    void genBCDType(Element elm, String value) {
        /*
        <xsd:simpleType name="BCD_5.4">
        <xsd:restriction base="xsd:decimal">
        <xsd:totalDigits value="9"/>
        <xsd:fractionDigits value="4"/>
        </xsd:restriction>
        </xsd:simpleType>
         */
        Element rest = doc.createElement("xsd:" + XSD_RESTRICTION);
        rest.setAttribute("base", "xsd:decimal");
        elm.appendChild(rest);
        float f = Float.parseFloat(value);
        int dec = (int) f;
        int fract = (int) (f * 10 - dec * 10);
        dec += fract;
        if (dec > 0) {
            Element tDigit = doc.createElement("xsd:" + XSD_TOTALDIGITS);
            tDigit.setAttribute("value", "" + dec);
            rest.appendChild(tDigit);
        }
        if (fract > 0) {
            Element fDigit = doc.createElement("xsd:" + XSD_FRACTIONDIGITS);
            fDigit.setAttribute("value", "" + fract);
            rest.appendChild(fDigit);
        }
    }

    /**
     * Generate XSD simple types for the WSDL
     *
     * @param schema XSD schema element
     */
    void genSimpleType(Element schema) {
        Iterator<String> it = typeMap.keySet().iterator();
        while (it.hasNext()) {
            String pType = it.next();
            Set items = (Set) typeMap.get(pType);
            if (items.size() > 0) {
                Iterator it2 = items.iterator();
                while (it2.hasNext()) {
                    String pName = (String) it2.next();

                    Element sType = doc.createElement("xsd:" + XSD_SIMPLETYPE);
                    // sType.setAttribute("name", pType + "_" + pName);
                    schema.appendChild(sType);

                    if (pType.equalsIgnoreCase("CHAR")) {
                        sType.setAttribute("name", pType + "_" + pName);
                        genSubType(sType, "string", pName, null);
                    } else if (pType.equalsIgnoreCase("INT")) {
                        sType.setAttribute("name", pType);
                        Element rest = doc.createElement("xsd:" + XSD_RESTRICTION);
                        rest.setAttribute("base", "xsd:integer");
                        sType.appendChild(rest);
                    } else if (pType.equalsIgnoreCase("NUM")) {
                        sType.setAttribute("name", pType + "_" + pName);
                        genSubType(sType, "string", pName, "\\d*");
                    } else if (pType.equalsIgnoreCase("DATE")) {
                        sType.setAttribute("name", pType);
                        genSubType(sType, "string", "10", "\\d\\d\\d\\d-\\d\\d-\\d\\d");
                    } else if (pType.equalsIgnoreCase("TIME")) {
                        sType.setAttribute("name", pType);
                        genSubType(sType, "string", "8", "\\d\\d:\\d\\d:\\d\\d");
                    } else if (pType.equalsIgnoreCase("BCD")) {
                        sType.setAttribute("name", pType + "_" + pName);
                        genBCDType(sType, pName);
                    }
                }
            }
        }
    }

    void genComplexType(Element parent, List<Element> elements, String name) {
        try {
            Element cType = doc.createElement("xsd:" + XSD_COMPLEXTYPE);
            cType.setAttribute("name", name);
            parent.appendChild(cType);
            Element sequence = doc.createElement("xsd:" + XSD_SEQUENCE);
            cType.appendChild(sequence);

            for (int i = 0, n = elements.size(); i < n; i++) {
                Element elm = elements.get(i);
                sequence.appendChild(elm);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    Element genMessageItems(Element parent, List<Element> elements, String name, String part) {
        Element item = null;
        try {
            int n = elements.size();
            if (n < 1) {
                return item;
            }
            String pType = name + "_" + part;
            Element cType = doc.createElement("xsd:" + XSD_COMPLEXTYPE);
            cType.setAttribute("name", pType);
            parent.appendChild(cType);
            Element sequence = doc.createElement("xsd:" + XSD_SEQUENCE);
            cType.appendChild(sequence);

            for (int i = 0; i < n; i++) {
                Element elm = elements.get(i);
                sequence.appendChild(elm);
            }

            item = getElement(part, pType, null, null);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return item;
    }

    void genMessages(Element def, Element schema, List<Element> iElements, List<Element> eElements,
            List<Element> cElements, List<Element> tElements, String name) {

        try {
            Element iPart = genMessageItems(schema, iElements, name, "INPUT");
            Element ePart = genMessageItems(schema, eElements, name, "OUTPUT");
            Element cPart = genMessageItems(schema, cElements, name, "CHANGE");
            Element tPart = genMessageItems(schema, tElements, name, "TABLES");

            Element inMessageType = doc.createElement("xsd:" + XSD_ELEMENT);
            schema.appendChild(inMessageType);
            inMessageType.setAttribute("name", name);
            Element cType = doc.createElement("xsd:" + XSD_COMPLEXTYPE);
            inMessageType.appendChild(cType);
            Element sequence = doc.createElement("xsd:" + XSD_SEQUENCE);
            cType.appendChild(sequence);

            if (iPart != null) {
                sequence.appendChild(iPart);
            }
            if (cPart != null) {
                sequence.appendChild(cPart);
            }
            if (tPart != null) {
                sequence.appendChild(tPart);
            }

            Element outMessageType = doc.createElement("xsd:" + XSD_ELEMENT);
            schema.appendChild(outMessageType);
            outMessageType.setAttribute("name", name + "_RESPONSE");
            Element cType2 = doc.createElement("xsd:" + XSD_COMPLEXTYPE);
            outMessageType.appendChild(cType2);
            Element sequence2 = doc.createElement("xsd:" + XSD_SEQUENCE);
            cType2.appendChild(sequence2);

            if (ePart != null) {
                sequence2.appendChild(ePart);
            }
            if (cPart != null) {
                Element cPart2 = (Element) cPart.cloneNode(true);
                sequence2.appendChild(cPart2);
            }
            if (tPart != null) {
                Element tPart2 = (Element) tPart.cloneNode(true);
                sequence2.appendChild(tPart2);
            }

            Element inMessage = doc.createElement("wsdl:" + WSDL_MESSAGE);
            def.appendChild(inMessage);
            inMessage.setAttribute("name", name + "_MSG");
            Element inMessagePart = doc.createElement("wsdl:" + WSDL_PART);
            inMessage.appendChild(inMessagePart);
            inMessagePart.setAttribute("name", "parameters");
            inMessagePart.setAttribute("element", GENXSD_NS + name);

            Element outMessage = doc.createElement("wsdl:" + WSDL_MESSAGE);
            def.appendChild(outMessage);
            outMessage.setAttribute("name", name + "_RESPONSE" + "_MSG");
            Element outMessagePart = doc.createElement("wsdl:" + WSDL_PART);
            outMessage.appendChild(outMessagePart);
            outMessagePart.setAttribute("name", "parameters");
            outMessagePart.setAttribute("element", GENXSD_NS + name + "_RESPONSE");

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void genBinding(Element def, String portTypeName,
            String[] operationNames, boolean hasOutput, boolean isIDoc) {

        Element binding = doc.createElement("wsdl:" + WSDL_BINDING);
        def.appendChild(binding);
        String bindingName = portTypeName + "_Binding";
        binding.setAttribute("name", bindingName);
        binding.setAttribute("type", GENTNS_NS + portTypeName);

        Element sapBinding = doc.createElement("sap:" + SAP_BINDING);
        binding.appendChild(sapBinding);
        sapBinding.setAttribute("transactionalMode", "Transactional"); // configurable?

        for (String operationName : operationNames) {
            Element bindingOperation = doc.createElement("wsdl:" + WSDL_OPERATION);
            binding.appendChild(bindingOperation);
            bindingOperation.setAttribute("name", operationName);

            if (isIDoc) {
                Element sapBindingOperation = doc.createElement("sap:" + SAP_IDOCOPERATION);
                bindingOperation.appendChild(sapBindingOperation);
            } else {
                Element sapBindingOperation = doc.createElement("sap:" + SAP_FMOPERATION);
                bindingOperation.appendChild(sapBindingOperation);
                sapBindingOperation.setAttribute("functionName", operationName); // really needed?
            }

            Element bindingInput = doc.createElement("wsdl:" + WSDL_INPUT);
            bindingOperation.appendChild(bindingInput);

            if (hasOutput) {
                Element bindingOutput = doc.createElement("wsdl:" + WSDL_OUTPUT);
                bindingOperation.appendChild(bindingOutput);
            }
        }
    }

    private void genServiceAndPort(Element def, String portTypeName/*, boolean isClient*/) {
        String bindingName = portTypeName + "_Binding";
        
        Element service = doc.createElement("wsdl:" + WSDL_SERVICE);
        def.appendChild(service);
        service.setAttribute("name", bindingName + "_Service");

        Element port = doc.createElement("wsdl:" + WSDL_PORT);
        service.appendChild(port);
        String portName = bindingName + "_Port";
        port.setAttribute("name", portName);
        port.setAttribute("binding", GENTNS_NS + bindingName);

        Element sapAddress = doc.createElement("sap:" + SAP_ADDRESS);
        port.appendChild(sapAddress);
        sapAddress.setAttribute("systemID", connection.getSystemID());
        sapAddress.setAttribute("user", connection.getUserName());
        sapAddress.setAttribute("clientNumber", connection.getClientNumber());
        sapAddress.setAttribute("systemNumber", connection.getSystemNumber());
        sapAddress.setAttribute("password", connection.getPassword());
        sapAddress.setAttribute("applicationServerHostname", connection.getApplicationServer());

        boolean isClient = true; // TMP
        if (isClient) {
            Element sapClientParams = doc.createElement("sap:" + SAP_CLIENTPARAMS);
            sapAddress.appendChild(sapClientParams);
        } else {
            Element sapServerParams = doc.createElement("sap:" + SAP_SERVERPARAMS);
            sapAddress.appendChild(sapServerParams);
        }
    }

    public void genRfcWsdl(String name, File outputFile) {
        try {
            BAPI_NAME = name;
            SAPXSD_NS_URI = SAPXSD_NS_URI_PREFIX + BAPI_NAME;
            SAPWSDL_NS_URI = SAPWSDL_NS_URI_PREFIX + BAPI_NAME;
            JCoFunctionTemplate ftemplate = getRfcRepository().getFunctionTemplate(name);
            if (ftemplate == null) {
                throw new RuntimeException(name + " not found in SAP.");
            }

            JCoListMetaData ilist = ftemplate.getImportParameterList();
            JCoListMetaData clist = ftemplate.getChangingParameterList();
            JCoListMetaData elist = ftemplate.getExportParameterList();
            JCoListMetaData tlist = ftemplate.getTableParameterList();

            // create WSDL document
            DocumentBuilderFactory fact = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = fact.newDocumentBuilder();
            doc = builder.newDocument();
            Element def = doc.createElement("wsdl:" + WSDL_DEFINITIONS);
            doc.appendChild(def);
            // def.setAttribute("xmlns", WSDL_NS_URI);
            def.setAttribute("xmlns:xsd", XSD_NS_URI);
            def.setAttribute("xmlns:wsdl", WSDL_NS_URI);
            def.setAttribute("xmlns:sap", SAP_NS_URI);
            // def.setAttribute("xmlns:ns0", SAPXSD_NS_URI);
            def.setAttribute("xmlns:tns", SAPWSDL_NS_URI);
            def.setAttribute("targetNamespace", SAPWSDL_NS_URI);

            // parse paramters to collect type definitions
            // System.out.println(name + ": input " + isize + ", change " + csize + ", export " + esize + ", table " + tsize);
            List<Element> iElements = genItemDetail(ilist, "IMPORT");
            List<Element> eElements = genItemDetail(elist, "EXPORT");
            List<Element> cElements = genItemDetail(clist, "CHANGE");
            List<Element> tElements = genItemDetail(tlist, "TABLE");
            System.out.println();

            // generate XSD types
            Element types = doc.createElement("wsdl:" + WSDL_TYPES);
            def.appendChild(types);
            Element schema = doc.createElement("xsd:" + XSD_SCHEMA);
            // schema.setAttribute("targetNamespace", SAPXSD_NS_URI);
            schema.setAttribute("elementFormDefault", "unqualified");
            schema.setAttribute("attributeFormDefault", "qualified");
            types.appendChild(schema);

            //listTypes();
            genSimpleType(schema);
            Iterator<String> it = complexType.keySet().iterator();
            while (it.hasNext()) {
                String pType = it.next();
                List<Element> items = complexType.get(pType);
                genComplexType(schema, items, pType);
            }

            // create messge and parts definitions..
            genMessages(def, schema, iElements, eElements, cElements, tElements, name);

            // create port type, operation, and input/output
            Element portType = doc.createElement("wsdl:" + WSDL_PORTTYPE);
            def.appendChild(portType);
            String portTypeName = name + "_PT";
            portType.setAttribute("name", portTypeName);
            Element operation = doc.createElement("wsdl:" + WSDL_OPERATION);
            portType.appendChild(operation);
            operation.setAttribute("name", name);
            Element input = doc.createElement("wsdl:" + WSDL_INPUT);
            operation.appendChild(input);
            input.setAttribute("message", GENTNS_NS + name + "_MSG");
            Element output = doc.createElement("wsdl:" + WSDL_OUTPUT);
            operation.appendChild(output);
            output.setAttribute("message", GENTNS_NS + name + "_RESPONSE" + "_MSG");

            // create binding, operation, and input/output
            genBinding(def, portTypeName, new String[] {name}, true, false);

            // create service and port
            genServiceAndPort(def, portTypeName);

            // generate Partner Link Types..
            /*def.setAttribute("xmlns:plnk", PLNK_NS_URI);
            Element plt = doc.createElement("plnk:"+PLNK_PLT);
            plt.setAttribute("name", name + "_PLT");
            def.appendChild(plt);
            Element role = doc.createElement("plnk:"+PLNK_ROLE);
            role.setAttribute("name", name + "_Provider");
            plt.appendChild(role);
            Element pt = doc.createElement("plnk:"+PLNK_PT);
            pt.setAttribute("name", "tns:" + name + "_PT");
            role.appendChild(pt);*/

            //XmlUtil.writeToFileObject(outputFile, doc);
            saveDocument(doc, outputFile);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void genBoWsdl(BAPIObject bo, File outputFile) {
        try {
            BO_NAME = bo.getName();
            SAPXSD_NS_URI = SAPXSD_NS_URI_PREFIX + BO_NAME;
            SAPWSDL_NS_URI = SAPWSDL_NS_URI_PREFIX + BO_NAME;

            // create WSDL document
            DocumentBuilderFactory fact = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = fact.newDocumentBuilder();
            doc = builder.newDocument();
            Element def = doc.createElement("wsdl:" + WSDL_DEFINITIONS);
            doc.appendChild(def);
            // def.setAttribute("xmlns", WSDL_NS_URI);
            def.setAttribute("xmlns:xsd", XSD_NS_URI);
            def.setAttribute("xmlns:wsdl", WSDL_NS_URI);
            def.setAttribute("xmlns:sap", SAP_NS_URI);
            // def.setAttribute("xmlns:ns0", SAPXSD_NS_URI);
            def.setAttribute("xmlns:tns", SAPWSDL_NS_URI);
            def.setAttribute("targetNamespace", SAPWSDL_NS_URI);

            // generate schema elements from BAPI methods
            List<BAPIMethod> methods = bo.getMethods();
            int nom = methods.size();
            List<Element>[][] parms = new ArrayList[nom][4];
            for (int i = 0; i < nom; i++) {
                String fname = methods.get(i).getFunction();
                JCoFunctionTemplate ftemplate = getRfcRepository().getFunctionTemplate(fname);
                if (ftemplate == null) {
                    throw new RuntimeException(fname + " not found in SAP.");
                }

                JCoListMetaData ilist = ftemplate.getImportParameterList();
                JCoListMetaData clist = ftemplate.getChangingParameterList();
                JCoListMetaData elist = ftemplate.getExportParameterList();
                JCoListMetaData tlist = ftemplate.getTableParameterList();

                parms[i][0] = genItemDetail(ilist, "IMPORT");
                parms[i][1] = genItemDetail(elist, "EXPORT");
                parms[i][2] = genItemDetail(clist, "CHANGE");
                parms[i][3] = genItemDetail(tlist, "TABLE");
            }

            // generate XSD types
            Element types = doc.createElement("wsdl:" + WSDL_TYPES);
            def.appendChild(types);
            Element schema = doc.createElement("xsd:" + XSD_SCHEMA);
            // schema.setAttribute("targetNamespace", SAPXSD_NS_URI);
            schema.setAttribute("elementFormDefault", "unqualified");
            schema.setAttribute("attributeFormDefault", "qualified");
            types.appendChild(schema);

            //listTypes();
            genSimpleType(schema);
            Iterator<String> it = complexType.keySet().iterator();
            while (it.hasNext()) {
                String pType = it.next();
                List<Element> items = complexType.get(pType);
                genComplexType(schema, items, pType);
            }

            // create messge and parts definitions..
            for (int i = 0; i < nom; i++) {
                String fname = methods.get(i).getFunction();
                genMessages(def, schema, parms[i][0], parms[i][1], parms[i][2], parms[i][3], fname);
            }

            // create port type, operation, and input/output
            String name = BO_NAME;
            Element portType = doc.createElement("wsdl:" + WSDL_PORTTYPE);
            def.appendChild(portType);
            String portTypeName = name + "_PT";
            portType.setAttribute("name", portTypeName);

            for (int i = 0; i < nom; i++) {
                String fname = methods.get(i).getFunction();

                // generate operations
                Element operation = doc.createElement("wsdl:" + WSDL_OPERATION);
                portType.appendChild(operation);
                operation.setAttribute("name", fname);
                Element input = doc.createElement("wsdl:" + WSDL_INPUT);
                operation.appendChild(input);
                input.setAttribute("message", GENTNS_NS + fname + "_MSG");
                Element output = doc.createElement("wsdl:" + WSDL_OUTPUT);
                operation.appendChild(output);
                output.setAttribute("message", GENTNS_NS + fname + "_RESPONSE" + "_MSG");
            }

            // create binding, operation, and input/output
            String[] operationNames = new String[nom];
            for (int i = 0; i < nom; i++) {
                operationNames[i] = methods.get(i).getFunction();
            }
            genBinding(def, portTypeName, operationNames, true, false);

            // create service and port
            genServiceAndPort(def, portTypeName);

            // generate Partner Link Types..
            /*def.setAttribute("xmlns:plnk", PLNK_NS_URI);
            Element plt = doc.createElement("plnk:"+PLNK_PLT);
            plt.setAttribute("name", name + "_PLT");
            def.appendChild(plt);
            Element role = doc.createElement("plnk:"+PLNK_ROLE);
            role.setAttribute("name", name + "_Provider");
            plt.appendChild(role);
            Element pt = doc.createElement("plnk:"+PLNK_PT);
            pt.setAttribute("name", "tns:" + name + "_PT");
            role.appendChild(pt);*/

            //XmlUtil.writeToFileObject(outputFile, doc);
            saveDocument(doc, outputFile);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void saveDocument(Document document, File file) {
        TransformerFactory tFactory = TransformerFactory.newInstance();
        try {
            tFactory.setAttribute("indent-number", "4");
        } catch (IllegalArgumentException ex) {
            // ignore
        }

        try {
            Transformer transformer = tFactory.newTransformer();

            try {
                transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                transformer.setOutputProperty(OutputKeys.METHOD, "xml");
                transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
                transformer.setOutputProperty(OutputKeys.STANDALONE, "yes");
                transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
                transformer.setOutputProperty(
                        "{http://xml.apache.org/xslt}indent-amount", "2");
            } catch (IllegalArgumentException ex) {
                // ignore
            }

            if (file == null) {
                transformer.transform(new DOMSource(document), new StreamResult(System.out));
            } else {
                transformer.transform(new DOMSource(document), new StreamResult(file));
            }

        } catch (TransformerConfigurationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (TransformerFactoryConfigurationError e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (TransformerException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    //-------------------------------------------------------------------------
    // IDOC WSDL Generation Code
    //-------------------------------------------------------------------------
    /**
     * Generate IDOC wsdl
     *
     * @param name IDoc type name
     * @throws com.sap.conn.jco.JCoException
     */
    public void genIDocWsdl(String name, File outputFile) throws JCoException {
        IDOC_NAME = name;
        SAPXSD_NS_URI = SAPXSD_NS_URI_PREFIX + IDOC_NAME;
        SAPWSDL_NS_URI = SAPWSDL_NS_URI_PREFIX + IDOC_NAME;

        JCoFunction function = getRfcRepository().getFunction("IDOCTYPE_READ_COMPLETE");
        if (function == null) {
            throw new RuntimeException("IDOCTYPE_READ_COMPLETE not found in SAP.");
        }

        function.getImportParameterList().setValue("PI_IDOCTYP", name);

        try {
            function.execute(getRfcClient());
        } catch (AbapException e) {
            System.out.println(e.toString());
            return;
        }

        try {
            System.out.println("finished: \n");
            // System.out.println(function.toXML());

            // create WSDL document
            DocumentBuilderFactory fact = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = fact.newDocumentBuilder();
            doc = builder.newDocument();
            Element def = doc.createElement("wsdl:" + WSDL_DEFINITIONS);
            doc.appendChild(def);
            // def.setAttribute("xmlns", WSDL_NS_URI);
            def.setAttribute("xmlns:xsd", XSD_NS_URI);
            def.setAttribute("xmlns:wsdl", WSDL_NS_URI);
            def.setAttribute("xmlns:sap", SAP_NS_URI);
            // def.setAttribute("xmlns:ns0", SAPXSD_NS_URI);
            def.setAttribute("xmlns:tns", SAPWSDL_NS_URI);
            def.setAttribute("targetNamespace", SAPWSDL_NS_URI);

            // generate XSD types
            Element types = doc.createElement("wsdl:" + WSDL_TYPES);
            def.appendChild(types);
            Element schema = doc.createElement("xsd:" + XSD_SCHEMA);
            // schema.setAttribute("targetNamespace", SAPXSD_NS_URI);
            schema.setAttribute("elementFormDefault", "unqualified");
            schema.setAttribute("attributeFormDefault", "qualified");
            types.appendChild(schema);

            // map out the sgement tree
            JCoTable segTable = function.getTableParameterList().getTable("PT_SEGMENTS");
            IDocSegmentDef root = null;
            Map<String, IDocSegmentDef> segMap = new HashMap<String, IDocSegmentDef>();
            for (int i = 0; i < segTable.getNumRows(); i++) {
                IDocSegmentDef seg = new IDocSegmentDef(segTable, i);
                segMap.put(seg.SEGMENTTYP, seg);
                if (seg.PARSEG.length() > 0) {
                    ((IDocSegmentDef) segMap.get(seg.PARSEG)).addChild(seg);
                }

                if (seg.HLEVEL == 1) {
                    root = seg;
                }

//                System.out.println(i + ": " + seg.SEGMENTTYP
//                        + ", " + seg.SEGMENTDEF
//                        + ", " + seg.HLEVEL
//                        + ", " + seg.PARFLG
//                        + ", " + seg.PARPNO
//                        + ", [" + seg.MUSTFL
//                        + ", " + seg.GRP_MUSTFL
//                        + ", " + seg.GRP_OCCMIN
//                        + ", " + seg.GRP_OCCMAX
//                        + "], " + seg.DESCRP
//                        );
            }

            // collect segment field lists, and simple types
            JCoTable fldTable = function.getTableParameterList().getTable("PT_FIELDS");
            genComplexIDocParam(fldTable);
            getRecordDetail(IDOC_DC40);

            // generate simple and complex typs
            genSimpleIDocType(schema);

            genIDocDC40Type(schema);

            Iterator it = complexIDocType.keySet().iterator();
            while (it.hasNext()) {
                String pType = (String) it.next();
                List<Element> items = complexIDocType.get(pType);
                IDocSegmentDef seg = (IDocSegmentDef) segMap.get(pType);
                // System.out.println("GenType: "+pType + ", "+seg);
                genComplexIDocType(schema, items, seg);
            }

            // generate IDOC type
            genIDocType(schema, root);

            // generate messages
            genIDocMessages(def, schema, name);

            // create port type, operation, and input/output
            Element portType = doc.createElement("wsdl:" + WSDL_PORTTYPE);
            def.appendChild(portType);
            String portTypeName = name + "_PT";
            portType.setAttribute("name", portTypeName);
            Element operation = doc.createElement("wsdl:" + WSDL_OPERATION);
            portType.appendChild(operation);
            operation.setAttribute("name", name);
            Element input = doc.createElement("wsdl:" + WSDL_INPUT);
            operation.appendChild(input);
            input.setAttribute("message", GENTNS_NS + name + "_MSG");
            //Element output = doc.createElement("wsdl:" + WSDL_OUTPUT);

            // create binding, operation, and input/output
            genBinding(def, portTypeName, new String[] {name}, false, true);

            // create service and port
            genServiceAndPort(def, portTypeName);

            // generate Partner Link Types..
            /*def.setAttribute("xmlns:plnk", PLNK_NS_URI);
            Element plt = doc.createElement("plnk:"+PLNK_PLT);
            plt.setAttribute("name", name + "_PLT");
            def.appendChild(plt);
            Element role = doc.createElement("plnk:"+PLNK_ROLE);
            role.setAttribute("name", name + "_Provider");
            plt.appendChild(role);
            Element pt = doc.createElement("plnk:"+PLNK_PT);
            pt.setAttribute("name", "tns:" + name + "_PT");
            role.appendChild(pt);*/

            //XmlUtil.writeToFileObject(outputFile, doc);
            saveDocument(doc, outputFile);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    void getRecordDetail(String name) throws JCoException {
        try {
            JCoRecordMetaData recMetadata = rfcRepository.getRecordMetaData(name);

            if (recMetadata == null) {
                System.out.println(" No field table/structure info found.. " + name);
            }

            List<Element> eList = new ArrayList<Element>();
            // Iterate through rows
            for (int i = 0, nrows = recMetadata.getFieldCount(); i < nrows; i++) {
                String pName = recMetadata.getName(i).toUpperCase();
                String pElmType = xlateRfcType(recMetadata.getTypeAsString(i));
                int pcharLen = recMetadata.getLength(i);
                String pText = recMetadata.getDescription(i);
                String recName = recMetadata.getRecordTypeName(i);
// System.out.println(i+": "+pName+", "+pElmType+", "+recName+", "+pText);
                eList.add(getIDocElement(pName, pElmType, "" + pcharLen));
            }

            complexType.put(name, eList);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    String xlateRfcType(String type) {
        if (type.equalsIgnoreCase("DATE")) {
            return "DATS";
        } else if (type.equalsIgnoreCase("TIME")) {
            return "TIMS";
        }
        return type;
    }

    void genIDocMessages(Element def, Element schema, String name) {
        try {
            Element messageType = doc.createElement("xsd:" + XSD_ELEMENT);
            schema.appendChild(messageType);
            messageType.setAttribute("name", name);
            Element cType = doc.createElement("xsd:" + XSD_COMPLEXTYPE);
            messageType.appendChild(cType);
            Element sequence = doc.createElement("xsd:" + XSD_SEQUENCE);
            cType.appendChild(sequence);

            Element elm = getElement(IDOC_ELEMENT, IDOC_ELEMENT_TYPE, "1", "1");
            sequence.appendChild(elm);

            Element inMessage = doc.createElement("wsdl:" + WSDL_MESSAGE);
            def.appendChild(inMessage);
            inMessage.setAttribute("name", name + "_MSG");
            Element inMessagePart = doc.createElement("wsdl:" + WSDL_PART);
            inMessage.appendChild(inMessagePart);
            inMessagePart.setAttribute("name", "parameters");
            inMessagePart.setAttribute("element", GENXSD_NS + name);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    void genComplexIDocParam(JCoTable fldTable) {

        List<Element> eList = null;
        String lastName = "_not_a_Name_";
        // Iterate through rows
        for (int i = 0; i < fldTable.getNumRows(); i++) {
            fldTable.setRow(i);
//            System.out.println(i + ": " + fldTable.getString("SEGMENTTYP")
//                    + ", " + fldTable.getString("FIELDNAME")
//                    + ", " + fldTable.getString("FIELD_POS")
//                    + ", " + fldTable.getString("DATATYPE")
//                    + ", " + fldTable.getString("DESCRP")
//                    );

            String pName = fldTable.getString("SEGMENTTYP").toUpperCase();
            String pField = fldTable.getString("FIELDNAME").toUpperCase();
            String pElmType = fldTable.getString("DATATYPE").toUpperCase();
            int pLength = Integer.parseInt(fldTable.getString("INTLEN"));

            if (!pName.equals(lastName)) { // name changes...
                if (i > 0) { // not the first time..
                    // String defName = (String) ((List) segmentMap.get(lastName)).get(0);
                    complexIDocType.put(lastName, eList);
                }
                eList = new ArrayList<Element>();
                lastName = pName;
            }

            eList.add(getIDocElement(pField, pElmType, "" + pLength));
        }

        //String defName = (String) ((List) segmentMap.get(lastName)).get(0);
        complexIDocType.put(lastName, eList);
    }

    Element getIDocElement(String name, String type, String len) {
        // <xsd:element name="Type" type="n0:char1"/>
        Element elm = doc.createElement("xsd:" + XSD_ELEMENT);
        elm.setAttribute("name", name);
        elm.setAttribute("type", GENXSD_NS + type + "_" + len);
        typeIDocMap.get(type).add(len);

        return elm;
    }

    void genSimpleIDocType(Element schema) {
        Iterator<String> it = typeIDocMap.keySet().iterator();
        while (it.hasNext()) {
            String pType = it.next();
            Set<String> items = typeIDocMap.get(pType);
            if (items.size() > 0) {
                Iterator<String> it2 = items.iterator();
                while (it2.hasNext()) {
                    String pName = it2.next();

                    Element sType = doc.createElement("xsd:" + XSD_SIMPLETYPE);
                    schema.appendChild(sType);

                    sType.setAttribute("name", pType + "_" + pName);
                    genSubType(sType, "string", pName, null);
                }
            }
        }
    }

    void genIDocDC40Type(Element schema) {
        Element cType = doc.createElement("xsd:" + XSD_COMPLEXTYPE);
        cType.setAttribute("name", IDOC_DC40_TYPE);
        schema.appendChild(cType);
        Element sequence = doc.createElement("xsd:" + XSD_SEQUENCE);
        cType.appendChild(sequence);

        List<Element> elements = complexType.get(IDOC_DC40);
        for (int i = 0, n = elements.size(); i < n; i++) {
            Element elm = (Element) elements.get(i);
            sequence.appendChild(elm);
        }

        // add SEGMENT attributes
        Element attr = doc.createElement("xsd:" + XSD_ATTRIBUTE);
        attr.setAttribute("name", "SEGMENT");
        attr.setAttribute("type", "xsd:string");
        // attr.setAttribute("use", "required");
        attr.setAttribute("default", "1");
        cType.appendChild(attr);

    }

    Element genComplexIDocType(Element parent, List<Element> elements, IDocSegmentDef seg) {
        Element cType = null;
        try {
            cType = doc.createElement("xsd:" + XSD_COMPLEXTYPE);
            cType.setAttribute("name", seg.SEGMENTTYP + "_TYPE");
            parent.appendChild(cType);
            Element sequence = doc.createElement("xsd:" + XSD_SEQUENCE);
            cType.appendChild(sequence);

            for (int i = 0, n = elements.size(); i < n; i++) {
                Element elm = elements.get(i);
                sequence.appendChild(elm);
            }

            // add sgement list to non-leaf node
            List<IDocSegmentDef> cList = seg.getChildren();
            if (cList.size() > 0) {
                for (int i = 0, n = cList.size(); i < n; i++) {
                    IDocSegmentDef child = cList.get(i);
                    Element elm = getElement(child.SEGMENTTYP,
                            child.SEGMENTTYP + "_TYPE",
                            (child.MUSTFL.length() > 0) ? "" + child.OCCMIN : "0",
                            child.getMaxValue(child.OCCMAX));
                    sequence.appendChild(elm);
                }
            }

            // add SEGMENT attributes
            Element attr = doc.createElement("xsd:" + XSD_ATTRIBUTE);
            attr.setAttribute("name", "SEGMENT");
            attr.setAttribute("type", "xsd:string");
            // attr.setAttribute("use", "required");
            attr.setAttribute("default", "1");
            cType.appendChild(attr);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return cType;
    }

    void genIDocType(Element parent, IDocSegmentDef seg) {
        try {
            Element cType = doc.createElement("xsd:" + XSD_COMPLEXTYPE);
            cType.setAttribute("name", IDOC_ELEMENT_TYPE);
            parent.appendChild(cType);
            Element sequence = doc.createElement("xsd:" + XSD_SEQUENCE);
            cType.appendChild(sequence);

            // add EDI_DC40
            Element elm_DC40 = getElement(IDOC_DC40, IDOC_DC40_TYPE, "1", "1");
            sequence.appendChild(elm_DC40);

            // add root segment
            Element elm = getElement(seg.SEGMENTTYP, seg.SEGMENTTYP + "_TYPE",
                    (seg.MUSTFL.length() > 0) ? "" + seg.OCCMIN : "0",
                    seg.getMaxValue(seg.OCCMAX));
            sequence.appendChild(elm);

            // add BEGIN attributes
            Element attr = doc.createElement("xsd:" + XSD_ATTRIBUTE);
            attr.setAttribute("name", "BEGIN");
            attr.setAttribute("type", "xsd:string");
            // attr.setAttribute("use", "required");
            attr.setAttribute("default", "1");
            cType.appendChild(attr);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    // PT_SEGMENTS -- Segments
 /*
    0: NR,         NUM,  POSNO,      Sequential Number of Segment in IDoc Type
    1: SEGMENTTYP, CHAR, EDIHSEGTYP, Segment type in 30-character format
    2: SEGMENTDEF, CHAR, EDISEGDEF,  IDoc Development: Segment definition
    3: QUALIFIER,  CHAR, SEGQUAL,    Flag: Qualified segment in IDoc
    4: SEGLEN,     NUM,  DDLEN,      Length of Field (Number of Characters)
    5: PARSEG,     CHAR, EDIHSEGTYP, Segment type in 30-character format
    6: PARPNO,     NUM,  PARPNO,     Sequential number of parent segment
    7: PARFLG,     CHAR, PARFLG,     Flag for parent segment: Segment is start of segment group
    8: MUSTFL,     CHAR, MUSTFL,     Flag: Mandatory entry
    9: OCCMIN,     NUM,  OCCMIN,     Minimum number of segments in sequence
    10: OCCMAX,     NUM,  OCCMAX,     Maximum number of segments in sequence
    11: HLEVEL,     NUM,  HLEVEL,     Hierarchy level of IDoc type segment
    12: DESCRP,     CHAR, EDI_TEXT60, Short description of object
    13: GRP_MUSTFL, CHAR, EDI_GMUSTF, Flag for groups: Mandatory
    14: GRP_OCCMIN, NUM,  EDI_GOCCMI, Minimum number of groups in sequence
    15: GRP_OCCMAX, NUM,  EDI_GOCCMA, Maximum number of groups in sequence
    16: REFSEGTYP,  CHAR, EDIHSEGTYP, Segment type in 30-character format
     */
    // PT_FIELDS -- fileds
/*
    0: SEGMENTTYP, CHAR, EDIHSEGTYP, Segment type in 30-character format
    1: FIELDNAME,  CHAR, FIELDNAME,  Field Name
    2: INTLEN,     NUM,  INTLEN,     Internal Length in Bytes
    3: EXTLEN,     NUM,  OUTPUTLEN,  Output Length
    4: FIELD_POS,  NUM,  EDI_FLDPOS, Position number of field
    5: BYTE_FIRST, NUM,  EDI_BYFRST, Position of first byte
    6: BYTE_LAST,  NUM,  EDI_BYLAST, Position of last byte
    7: ROLLNAME,   CHAR, ROLLNAME,   Data element (semantic domain)
    8: DOMNAME,    CHAR, DOMNAME,    Domain name
    9: DATATYPE,   CHAR, DYNPTYPE,   ABAP/4 Dictionary: Screen data type for Screen Painter
    10: DESCRP,     CHAR, EDI_TEXT60, Short description of object
    11: ISOCODE,    CHAR, SEGISOCODE, IDoc development: ISO code ID in field
    12: VALUETAB,   CHAR, EDI_VALTAB, Value table for IDoc segm. field
     */
}