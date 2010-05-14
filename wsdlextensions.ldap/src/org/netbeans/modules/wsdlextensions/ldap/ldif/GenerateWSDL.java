package org.netbeans.modules.wsdlextensions.ldap.ldif;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Iterator;
import java.util.Map;
import org.netbeans.modules.wsdlextensions.ldap.utils.LdapConnection;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;

/**
 *
 * @author tianlize
 */
public class GenerateWSDL {

    private File mDir;
    private Map mSelectedObjectMaps;
    private String[] mFunctions;
    private String mFileName;
    private LdapConnection conn;
    private boolean mWsdlWizardEntry = false;

    public GenerateWSDL(File dir, Map objectClasses, String fileName, LdapConnection conn) {
        mDir = dir;
        mSelectedObjectMaps = objectClasses;
        mFileName = fileName;
        this.conn = conn;
        initiateFunctions();
    }
    
    private void initiateFunctions(){
        int size=mSelectedObjectMaps.size();
        mFunctions=new String[size];
        int i=0;
        Iterator it=mSelectedObjectMaps.keySet().iterator();
        while(it.hasNext()){
            mFunctions[i++]=it.next().toString();
        }
    }

    private String generateXMLHead() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
    }

    private String getTab(int level) {
        String ret = "";
        for (int i = 0; i < level; i++) {
            ret += "    ";
        }
        return ret;
    }

    private String upInitial(String str) {
        // do not change case if invoked from wsdl wizard
        if (mWsdlWizardEntry) {
            return str;
        } else {
            // original code
            String ret = str.substring(0, 1).toUpperCase();
            ret += str.substring(1);
            return ret;
        }
    }

    private String lowInitial(String str) {
        String ret = str.substring(0, 1).toLowerCase();
        ret += str.substring(1);
        return ret;
    }

    private String generateTypes(String tag, int level) {
        String ret = "";
//        String tag = upInitial(ldif.getName()) + mFunction;
        ret += getTab(level) + "<types>" + "\n";
        ret += getTab(level + 1) + "<xsd:schema targetNamespace=\"http://j2ee.netbeans.org/wsdl/" + tag + "\">" + "\n";
        ret += generateImportInTypes(tag, level + 2);
        ret += getTab(level + 1) + "</xsd:schema>" + "\n";
        ret += getTab(level) + "</types>" + "\n";

        return ret;
    }

    private String generateImportInTypes(String tag, int level) {
        String ret = "";
        for (int i = 0; i < mFunctions.length; i++) {
            ret += getTab(level) + "<xsd:import namespace=\"http://xml.netbeans.org/schema/" + tag + mFunctions[i] +
                    "\" schemaLocation=\"" + tag + mFunctions[i] + ".xsd\"/>" + "\n";
        }
        return ret;
    }

    private String generateMessages(String tag, int level) {
        String ret = "";
//        String tag = upInitial(ldif.getName()) + mFunction;
        for (int i = 0; i < mFunctions.length; i++) {
            ret += getTab(level) + "<message name=\"" + tag + mFunctions[i] + "OperationRequest\">" + "\n";
            ret += getTab(level + 1) + "<part name=\"request\" element=\"ns" + i + ":Request\"/>" + "\n";
            ret += getTab(level) + "</message>" + "\n";
            ret += getTab(level) + "<message name=\"" + tag + mFunctions[i] + "OperationResponse\">" + "\n";
            ret += getTab(level + 1) + "<part name=\"response\" element=\"ns" + i + ":Response\"/>" + "\n";
            ret += getTab(level) + "</message>" + "\n";
            ret += getTab(level) + "<message name=\"" + tag + mFunctions[i] + "OperationFault\">" + "\n";
            ret += getTab(level + 1) + "<part name=\"fault\" element=\"ns" + i + ":Fault\"/>" + "\n";
            ret += getTab(level) + "</message>" + "\n";
        }
        return ret;
    }

    private String generatePortType(String tag, int level) {
        String ret = "";
        ret += getTab(level) + "<portType name=\"" + tag + "PortType\">" + "\n";
        ret += generatePortTypeOperation(tag, level + 1);
        ret += getTab(level) + "</portType>" + "\n";
        return ret;
    }

    private String generatePortTypeOperation(String tag, int level) {
        String ret = "";
        for (int i = 0; i < mFunctions.length; i++) {
            ret += getTab(level) + "<wsdl:operation name=\"" + tag + mFunctions[i] + "Operation\">" + "\n";
            ret += getTab(level + 1) + "<wsdl:input name=\"request" + tag + mFunctions[i] + "\" message=\"tns:" + tag + mFunctions[i] + "OperationRequest\"/>" + "\n";
            ret += getTab(level + 1) + "<wsdl:output name=\"response" + tag + mFunctions[i] + "\" message=\"tns:" + tag + mFunctions[i] + "OperationResponse\"/>" + "\n";
            ret += getTab(level + 1) + "<wsdl:fault name=\"fault" + tag + mFunctions[i] + "\" message=\"tns:" + tag + mFunctions[i] + "OperationFault\"/>" + "\n";
            ret += getTab(level) + "</wsdl:operation>" + "\n";
        }
        return ret;
    }

    private String generateBindings(String tag, int level) {
        String ret = "";
//        String tag = upInitial(ldif.getName()) + mFunction;

        ret += getTab(level) + "<binding name=\"" + tag + "Binding\" type=\"tns:" + tag + "PortType\">" + "\n";
        ret += getTab(level + 1) + "<ldap:binding/>" + "\n";
        ret += generateBindingsOperation(tag, level + 1);
        ret += getTab(level) + "</binding>" + "\n";
        return ret;
    }

    private String generateBindingsOperation(String tag, int level) {
        String ret = "";
        for (int i = 0; i < mFunctions.length; i++) {
            ret += getTab(level) + "<wsdl:operation name=\"" + tag + mFunctions[i] + "Operation\">" + "\n";
            if ("Add".equals(mFunctions[i])) {
                ret += getTab(level + 1) + "<ldap:operation type=\"insertRequest\"/>" + "\n";
            } else {
                ret += getTab(level + 1) + "<ldap:operation type=\"" + lowInitial(mFunctions[i]) + "Request\"/>" + "\n";
            }
            ret += getTab(level + 1) + "<wsdl:input name=\"request" + tag + mFunctions[i] + "\"/>" + "\n";
            ret += getTab(level + 1) + "<wsdl:output name=\"response" + tag + mFunctions[i] + "\">" + "\n";
            ret += getTab(level + 2) + "<ldap:output returnPartName=\"response\" attributes=\"\"/>" + "\n";
            ret += getTab(level + 1) + "</wsdl:output>" + "\n";
            ret += getTab(level + 1) + "<wsdl:fault name=\"fault" + tag + mFunctions[i] + "\"/>" + "\n";
            ret += getTab(level) + "</wsdl:operation>" + "\n";
        }
        return ret;
    }

    private String generatePLink(String tag, int level) {
        String ret = "";
//        String tag = upInitial(ldif.getName()) + mFunction;
        ret += getTab(level) + "<plnk:partnerLinkType name=\"" + tag + "PartnerLink\">" + "\n";
        ret += getTab(level + 1) + "<plnk:role name=\"" + tag + "PortTypeRole\" portType=\"tns:" + tag + "PortType\"/>" + "\n";
        ret += getTab(level) + "</plnk:partnerLinkType>" + "\n";
        return ret;
    }

    private String generateService(String tag, int level) {
        String ret = "";
//        String tag = upInitial(ldif.getName()) + mFunction;

        ret += getTab(level) + "<service name=\"" + tag + "Service\">" + "\n";
        ret += getTab(level + 1) + "<wsdl:port name=\"" + tag + "Port\" binding=\"tns:" + tag + "Binding\">" + "\n";
        ret += getTab(level + 2) + "<ldap:address" + "\n";

        String[] names = null;
        names = conn.getPropertyNames();
        for (int i = 0; i < names.length; i++) {
            String value = null;
            value = (String) conn.getProperty(names[i]);
            ret += getTab(level + 4) + names[i] + " = \"" + value + "\"\n";
        }
        ret += getTab(level + 2) + "/>" + "\n";
        ret += getTab(level + 1) + "</wsdl:port>\n";
        ret += getTab(level) + "</service>" + "\n";

        return ret;
    }

    private String generateDefinition(String tag) {
        String ret = "";
//        String tag = upInitial(ldif.getName()) + mFunction;

        ret += "<definitions name=\"" + tag + "\" targetNamespace=\"http://j2ee.netbeans.org/wsdl/" + tag + "\"" + "\n";
        ret += getTab(1) + "xmlns=\"http://schemas.xmlsoap.org/wsdl/\"" + "\n";
        ret += getTab(1) + "xmlns:wsdl=\"http://schemas.xmlsoap.org/wsdl/\"" + "\n";
        ret += getTab(1) + "xmlns:ldap=\"http://schemas.sun.com/jbi/wsdl-extensions/ldap/\"" + "\n";
        ret += getTab(1) + "xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\"" + "\n";
        ret += getTab(1) + "xmlns:tns=\"http://j2ee.netbeans.org/wsdl/" + tag + "\"" + "\n";
        for (int i = 0; i < mFunctions.length; i++) {
            ret += getTab(1) + "xmlns:ns" + i + "=\"http://xml.netbeans.org/schema/" + tag + mFunctions[i] + "\"" + "\n";
        }
        ret += getTab(1) + "xmlns:plnk=\"http://docs.oasis-open.org/wsbpel/2.0/plnktype\">" + "\n";

        ret += generateTypes(tag, 1);
        ret += generateMessages(tag, 1);
        ret += generatePortType(tag, 1);
        ret += generateBindings(tag, 1);
        ret += generateService(tag, 1);
        ret += generatePLink(tag, 1);

        ret += "</definitions>" + "\n";
        return ret;
    }

    private String generateWSDL(String tag) {
        String ret = "";
        ret += generateXMLHead();
        
        ret += generateDefinition(upInitial(tag) + mFunctions);
        return ret;
    }

    private String generateMainWSDL() {
        String ret = "";
        ret += generateXMLHead();
        ret += generateDefinition(upInitial(mFileName));
        return ret;
    }

    public void generate() throws IOException {
        generate(null);
    }
    
    public File generate(WSDLComponent wsdlComponent) throws IOException {
        File outputFile = null;
        if (wsdlComponent != null) {
            mWsdlWizardEntry = true;
            outputFile = File.createTempFile(mFileName + "LDAP", ".wsdl");
            outputFile.deleteOnExit();            
        } else {
            outputFile = new File(mDir.getAbsolutePath() + File.separator + upInitial(mFileName) + ".wsdl");
        }
        FileOutputStream mainFos = new FileOutputStream(outputFile);
        String mainDef = generateMainWSDL();
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter
                                 (mainFos,"UTF8"));
        out.write(mainDef);
        out.close();  
        mainFos.close();
        return outputFile;
    }    
}
