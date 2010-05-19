/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.wsdlextensions.ldap.ldif;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import org.netbeans.modules.wsdlextensions.ldap.impl.ResultSetAttribute;
import org.netbeans.modules.wsdlextensions.ldap.impl.SearchFilterAttribute;
import org.openide.util.Exceptions;

/**
 *
 * @author tianlize
 */
public class GenerateXSDSearch {

    private File mDir;
    private Map mSelectedObjectMap;
    private String mFunction;
    private String mFileName;
    private String mBaseDN;
    private String mSearchFilterTypeElements = "";
    private String mResponseTypeElements = "";
//    private GenerateSearchFilter mLDAPSearchFilter;
    public GenerateXSDSearch() {

    }

    public GenerateXSDSearch(File dir, Map selectedObject, String function, String fileName, String baseDN) {
        mDir = dir;
        mSelectedObjectMap = selectedObject;
        mFunction = function;
        mFileName = fileName;
        mBaseDN = baseDN;
//        mLDAPSearchFilter=new GenerateSearchFilter(selectedObject);
    }

    private String generateXMLHead() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
    }

    private String upInitial(String str) {
        String ret = str.substring(0, 1).toUpperCase();
        ret += str.substring(1);
        return ret;
    }

    private String generateSchemaHead(String objectClassName) {
        String type = upInitial(objectClassName) + mFunction;
        String ret = "<xsd:schema xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\"" + "\n";
        ret += "\t\ttargetNamespace=\"http://xml.netbeans.org/schema/" + type + "\"" + "\n";
        ret += "\t\txmlns:tns=\"http://xml.netbeans.org/schema/" + type + "\"" + "\n";
        ret += "\t\telementFormDefault=\"qualified\"" + "\n";
        ret += "\t\txmlns:ldap=\"http://schemas.sun.com/jbi/wsdl-extensions/ldap/\">" + "\n";
        ret += "\n";
        ret += getTab(1) + "<xsd:import schemaLocation=\"LdapBase.xsd\" namespace=\"http://schemas.sun.com/jbi/wsdl-extensions/ldap/\"/>" + "\n";
        ret += "\n";
        
        return ret;
    }

    private String generateSchemaTail() {
        return "</xsd:schema>";
    }

    private String getTab(int level) {
        String ret = "";
        for (int i = 0; i < level; i++) {
            ret += "    ";
        }
        return ret;
    }

    private String generateElement(String tag, String type, String defaultvalue, int level) {
        String ret = "";
        ret += getTab(level);
        if (defaultvalue != null) {
            ret += "<xsd:element name=\"" + tag + "\" type=\"" + type + "\" default=\"" + defaultvalue + "\"></xsd:element>" + "\n";
        } else {
            ret += "<xsd:element name=\"" + tag + "\" type=\"" + type + "\"></xsd:element>" + "\n";
        }
        return ret;
    }

    private String generateElement(String tag, String type, String defaultvalue, boolean repeat, int level) {
        String ret = "";
        ret += getTab(level);
        if (!repeat) {
            return generateElement(tag, type, defaultvalue, level);
        } else {
            if (defaultvalue != null) {
                ret += "<xsd:element name=\"" + tag + "\" type=\"" + type + "\" default=\"" + defaultvalue + "\" maxOccurs=\"unbounded\"></xsd:element>" + "\n";
            } else {
                ret += "<xsd:element name=\"" + tag + "\" type=\"" + type + "\" maxOccurs=\"unbounded\"></xsd:element>" + "\n";
            }
        }

        return ret;
    }

    private String generateSearchFilterElements(LdifObjectClass mLdif, int level) {
        String ret = "";
        List selected = mLdif.getSelected();
        Iterator it = selected.iterator();
        String objName = mLdif.getName();
        while (it.hasNext()) {
            SearchFilterAttribute sfa = (SearchFilterAttribute) it.next();
            ret += getTab(level) + "<xsd:element name=\"" + objName + "." + sfa.getAttributeName() + "\" >\n";

            ret += getTab(level + 1) + "<xsd:complexType>" + "\n";
            ret += getTab(level + 2) + "<xsd:sequence>" + "\n";
            ret += getTab(level + 3) + "<xsd:element name=\"value\" type=\"xsd:string\"/>" + "\n";
            ret += getTab(level + 2) + "</xsd:sequence>" + "\n";
            ret += generateAttribute("positionIndex", "optional", "xsd:int", String.valueOf(sfa.getPositionIndex()), level + 2);
            ret += generateAttribute("bracketDepth", "optional", "xsd:int", String.valueOf(sfa.getBracketDepth()), level + 2);
            ret += generateAttribute("bracketBeginDepth", "optional", "xsd:int", String.valueOf(sfa.getBracketBeginDepth()), level + 2);
            ret += generateAttribute("bracketEndDepth", "optional", "xsd:int", String.valueOf(sfa.getBracketEndDepth()), level + 2);
            ret += generateAttribute("logicOp", "optional", "xsd:string", sfa.getLogicOp(), level + 2);
            ret += generateAttribute("compareOp", "optional", "xsd:string", sfa.getCompareOp(), level + 2);
            ret += getTab(level + 1) + "</xsd:complexType>" + "\n";
            ret += getTab(level) + "</xsd:element>" + "\n";
            
            ret += "\n";
        }
        mSearchFilterTypeElements += ret;
        DocumentBuilder build;

        return ret;
    }

    private String generateRequestType(int level) {
        String ret = "";
        ret += getTab(level) + "<xsd:complexType name=\"RequestType\">" + "\n";
        ret += getTab(level + 1) + "<xsd:sequence>" + "\n";
        ret += getTab(level + 2) + "<xsd:element name=\"property\" type=\"tns:RequestPropertyType\"></xsd:element>" + "\n";
        ret += getTab(level + 2) + "<xsd:element name=\"attributes\" type=\"tns:SearchFilterType\"></xsd:element>" + "\n";
        ret += getTab(level + 2) + "<xsd:element name=\"connection\" type=\"ldap:ConnectionType\"></xsd:element>" + "\n";
        ret += getTab(level + 1) + "</xsd:sequence>" + "\n";
        ret += getTab(level) + "</xsd:complexType>" + "\n";
        ret += "\n";

        return ret;
    }

    private String generateRequestPropertyType(int level) {
        String ret = "";
        ret += getTab(level) + "<xsd:complexType name=\"RequestPropertyType\">" + "\n";
        ret += getTab(level + 1) + "<xsd:sequence>" + "\n";
        ret += generateElement("requestId", "xsd:string", null, level + 2);
        ret += generateElement("dn", "xsd:string", mBaseDN, level + 2);
        ret += generateElement("scope", "xsd:int", "2", level + 2);
        ret += generateElement("size", "xsd:int", "0", level + 2);
        ret += generateElement("timeout", "xsd:int", "0", level + 2);
        ret += generateElement("deref", "xsd:boolean", "true", level + 2);
        ret += generateElement("referral", "xsd:string", "follow", level + 2);
        ret += getTab(level + 1) + "</xsd:sequence>" + "\n";
        ret += getTab(level) + "</xsd:complexType>" + "\n";
        ret += "\n";
        return ret;
    }

    private String generateSearchFilterType(LdifObjectClass mLdif, int level) {
        String ret = "";
        ret += getTab(level) + "<xsd:complexType name=\"SearchFilterType\" >" + "\n";
        ret += getTab(level + 1) + "<xsd:sequence>" + "\n";
        ret += generateSearchFilterElements(mLdif, level + 2);
        ret += getTab(level + 1) + "</xsd:sequence>" + "\n";
        ret += getTab(level) + "</xsd:complexType>" + "\n";
        ret += "\n";
        
        return ret;
    }

    private String generateMainSearchFilterType(int level) {
        String ret = "";
        ret += getTab(level) + "<xsd:complexType name=\"SearchFilterType\" >" + "\n";
        ret += getTab(level + 1) + "<xsd:sequence>" + "\n";
        ret += mSearchFilterTypeElements;
        ret += getTab(level + 1) + "</xsd:sequence>" + "\n";
        ret += getTab(level) + "</xsd:complexType>" + "\n";
        
        ret += "\n";
        
        return ret;
    }

    private String generateResponseType(int level) {
        String ret = "";

        ret += getTab(level) + "<xsd:complexType name=\"ResponseType\" >" + "\n";
        ret += getTab(level + 1) + "<xsd:sequence>" + "\n";
        ret += getTab(level + 2) + "<xsd:element name=\"property\" type=\"ldap:ResponsePropertyType\"/>" + "\n";
        ret += getTab(level + 2) + "<xsd:element name=\"ResponseElements\" maxOccurs=\"unbounded\" type=\"tns:ResponseAttributeType\"/>" + "\n";
        ret += getTab(level + 1) + "</xsd:sequence>" + "\n";
        ret += getTab(level) + "</xsd:complexType>" + "\n";
        ret += "\n";
        return ret;
    }

    private String generateResponseAttributeType(LdifObjectClass mLdif, int level) {
        String ret = "";

        ret += getTab(level) + "<xsd:complexType name=\"ResponseAttributeType\" >" + "\n";
        ret += getTab(level + 1) + "<xsd:sequence>" + "\n";
        ret += generateResponseElement(mLdif, level + 1);
        ret += getTab(level + 1) + "</xsd:sequence>" + "\n";
        ret += getTab(level) + "</xsd:complexType>" + "\n";
        ret += "\n";

        return ret;
    }

    private String generateMainResponseAttributeType(int level) {
        String ret = "";

        ret += getTab(level) + "<xsd:complexType name=\"ResponseAttributeType\" >" + "\n";
        ret += getTab(level + 1) + "<xsd:sequence>" + "\n";
        ret += mResponseTypeElements;
        ret += getTab(level + 1) + "</xsd:sequence>" + "\n";
        ret += getTab(level) + "</xsd:complexType>" + "\n";
        
        ret += "\n";
        return ret;
    }

    private String generateResponseElement(LdifObjectClass mLdif, int level) {
        String ret = "";

        List selected = mLdif.getResultSet();
        Iterator it = selected.iterator();
        while (it.hasNext()) {
            ResultSetAttribute sra = (ResultSetAttribute) it.next();
            ret += generateElement(sra.getObjName() + "." + sra.getAttributeName(), "xsd:string", null, true, level + 1);
            sra = null;
        }
        selected = null;
        it = null;
        mResponseTypeElements += ret;
        return ret;
    }

    private String generateGlobalElements(int level) {
        String ret = "";
        ret += generateElement("Request", "tns:RequestType", null, level);
        ret += generateElement("Response", "tns:ResponseType", null, level);
        ret += generateElement("Fault", "ldap:FaultType", null, level);
        return ret;
    }

    private String generateAttribute(String tag, String use, String type, String fixed, int level) {
        String ret = "";
        ret += getTab(level);
        if (fixed != null) {
            ret += "<xsd:attribute name=\"" + tag + "\" use=\"" + use + "\" type=\"" + type + "\" fixed=\"" + fixed + "\" ></xsd:attribute>" + "\n";
        } else {
            ret += "<xsd:attribute name=\"" + tag + "\" use=\"" + use + "\" type=\"" + type + "\" ></xsd:attribute>" + "\n";
        }
        return ret;
    }

    private String generateSchema(LdifObjectClass mLdif) {
        String ret = "";
        ret += this.generateXMLHead();
        ret += this.generateSchemaHead(mLdif.getName());
        ret += this.generateRequestType(1);
        ret += this.generateRequestPropertyType(1);
        ret += this.generateSearchFilterType(mLdif, 1);
        ret += this.generateResponseType(1);
        ret += this.generateResponseAttributeType(mLdif, 1);
        ret += this.generateGlobalElements(1);
        ret += this.generateSchemaTail();
        
        return ret;
    }

    private String generateMainSchema() {
        String ret = "";
        ret += this.generateXMLHead();
        ret += this.generateSchemaHead(mFileName);
        ret += this.generateRequestType(1);
        ret += this.generateRequestPropertyType(1);
        ret += this.generateMainSearchFilterType(1);
        ret += this.generateResponseType(1);
        ret += this.generateMainResponseAttributeType(1);
        ret += this.generateGlobalElements(1);
        ret += this.generateSchemaTail();

        return ret;
    }

    public void generate() throws IOException {
        if (mSelectedObjectMap != null & mSelectedObjectMap.size() > 0) {
            Iterator it = mSelectedObjectMap.values().iterator();
            while (it.hasNext()) {
                LdifObjectClass loc = (LdifObjectClass) it.next();
                File outputFile = new File(mDir.getAbsolutePath() + File.separator + upInitial(loc.getName()) + mFunction + ".xsd");
                FileOutputStream fos = new FileOutputStream(outputFile);
                String schema = generateSchema(loc);
                fos.write(schema.getBytes());
                fos.close();
                loc = null;
                schema = null;
                fos = null;
            }
            it = null;
            File outputFile = new File(mDir.getAbsolutePath() + File.separator + upInitial(mFileName) + mFunction + ".xsd");
            FileOutputStream mainFos = new FileOutputStream(outputFile);
            String mainSchema = generateMainSchema();
            mainFos.write(mainSchema.getBytes());
            mainFos.close();
            mainSchema = null;
            mainFos = null;
            try {

                copyBaseSchema();
            } catch (ClassNotFoundException ex) {
                Exceptions.printStackTrace(ex);
            } catch (FileNotFoundException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }
    
    private void copyBaseSchema() throws ClassNotFoundException, FileNotFoundException, IOException {
        Class cls = this.getClass();
        InputStream is = cls.getResourceAsStream("/org/netbeans/modules/wsdlextensions/ldap/resources/LdapBase.xsd");
        File output = new File(mDir, "LdapBase.xsd");
        FileOutputStream fos = new FileOutputStream(output);
        byte[] buf = new byte[is.available()];
        is.read(buf);
        fos.write(buf);
        fos.close();
    }
}
