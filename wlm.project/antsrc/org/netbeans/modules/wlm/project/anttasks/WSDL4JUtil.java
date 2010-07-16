/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 1997-2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.wlm.project.anttasks;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.swing.text.Document;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.netbeans.modules.xml.schema.model.Import;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.w3c.dom.Element;

/**
 * Utility class for WSDL4J used in generation of client WSDL
 *
 * @author mbhasin
 *
 */
public class WSDL4JUtil {
    private static WSDL4JUtil mInstance = null;
    private static final String DIR_SEPERATOR = "/";
    private static final String XSD_EXT = ".xsd";
    private static final String XSD_GEN_DELIM = "_";


    private WSDL4JUtil() {
    }

    public static WSDL4JUtil getInstance() {
        if (mInstance == null) {
            mInstance = new WSDL4JUtil();
        }
        return mInstance;
    }

    /**
     * Reads the wsdl file, searches for matching  schema using the input schemaNamespace and returns the list 
     * of physical locations of the schema file relative to either project source directory or build directory
     * depending on the schema is imported or inline.
     *
     * If the schema is imported the location will be relative to project source directory
     * else relative to build directory as schema will be serialized to build directory if the
     * schema is inline in WSDL.
     *
     * @param wsdlDef WSDL Definition
     * @param schemaNameSpace  Namespace of the schema
     * @param wsdlFileLocation  File location of the WSDL. This must be relative to project source directory
     * @param buildDir build directory
     * @param projectSourceDirectory project source directory.
     * @return List of  WSDL4JUtil.SchemaLocation
     */
    public Set<WSDL4JUtil.SchemaLocation> getSchemaLocations(WSDLModel wsdlModel, String schemaNameSpace, File wsdlFileLocation,
            File buildDir, File projectSourceDirectory, String type) throws Exception {

        if (schemaNameSpace == null) {
            return null;
        }

        Set<WSDL4JUtil.SchemaLocation> listOfSchLoc = new HashSet<WSDL4JUtil.SchemaLocation>();

        if (schemaNameSpace.equals("http://www.w3.org/2001/XMLSchema")) {
            listOfSchLoc.add(getSchemaLocationForXSDSimpleType(schemaNameSpace));
            return listOfSchLoc;
        }

        doValidations(wsdlFileLocation, buildDir);

        int inlineSchInd = 1;

        boolean bMatchingNSFound = false;
        Import iimport = null;
        String schemaNamepsaceURI = null;
        String schemaLocationURIString = null;
        Schema schema = null;
        String schemaTNS = null;

        String prjRelativeWSDLFileDirPath = getWSDLRelativePath(wsdlFileLocation, projectSourceDirectory);
        Iterator<Schema> iter = wsdlModel.getDefinitions().getTypes().getSchemas().iterator();
        
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();

        while (iter.hasNext()) {
            SchemaLocation schLoc = null;
            Element existingEl = null;
           
            schema = iter.next();
            Iterator<Import> importsIter = schema.getImports().iterator();

            while (importsIter.hasNext()) {

                iimport = importsIter.next();
                schemaNamepsaceURI = iimport.getNamespace();
                schemaLocationURIString = iimport.getSchemaLocation();

                File schemaFile = null;
                String relativeLocation = null;

                if (prjRelativeWSDLFileDirPath.equals("")) {
                    relativeLocation = schemaLocationURIString;
                } else {
                    relativeLocation = prjRelativeWSDLFileDirPath + DIR_SEPERATOR + schemaLocationURIString.replace('\\', '/');
                }

                if (schemaNamepsaceURI.equals(schemaNameSpace)) {
                    bMatchingNSFound = true;
                    try {
                        schemaFile = new File(wsdlFileLocation.getParent(), schemaLocationURIString);

                        if (schemaFile.exists()) {
                            schLoc = new SchemaLocation(schemaLocationURIString, schemaFile, relativeLocation, true, schemaNamepsaceURI);
                        } else {
                            schemaFile = new File(schemaLocationURIString);
                            if (schemaFile.exists()) {
                                schLoc = new SchemaLocation(schemaLocationURIString, schemaFile, null, false, schemaNamepsaceURI);
                            }
                        }
                        listOfSchLoc.add(schLoc);
                    } catch (Exception ex) {
                        schLoc = new SchemaLocation(schemaLocationURIString, null, null, false, schemaNamepsaceURI);
                        listOfSchLoc.add(schLoc);
                    }
                }
            }
            existingEl = schema.getPeer();
            
            
            // Inline schema - Generate Schema.
            if (!bMatchingNSFound) {
                String genSchemaFilePath = buildDir.getAbsolutePath();
                String xsdFileName = wsdlFileLocation.getName().replace('.', '_') + XSD_GEN_DELIM + type + XSD_GEN_DELIM + inlineSchInd + XSD_EXT;
                genSchemaFilePath = genSchemaFilePath.replace('\\', '/') + DIR_SEPERATOR + xsdFileName;
                // Add missing namespace definition to schema as inline schema is saved as separate file.
                Map<QName, String> wsdlNamespaceMap = getNameSpacesMap(wsdlModel);
                writeXML(genSchemaFilePath, existingEl);
                try {
                    existingEl = builder.parse(new File (genSchemaFilePath)).getDocumentElement();
                }catch (Exception e) {
                    e.printStackTrace();
                    throw e;
                }
                addWSDLNamespacesToXMLElement(existingEl, wsdlNamespaceMap);                
                writeXML(genSchemaFilePath, existingEl);
                File genSchemaFile = new File(genSchemaFilePath);

                if (genSchemaFile.exists()) {
                    schLoc = new SchemaLocation(null, genSchemaFile, prjRelativeWSDLFileDirPath + DIR_SEPERATOR + xsdFileName, false,
                            schemaTNS);
                    listOfSchLoc.add(schLoc);
                } else {
                    throw new RuntimeException("Could not create " + genSchemaFilePath);
                }
                inlineSchInd++;
            } else {
                if (schemaNameSpace.equals("http://www.w3.org/2001/XMLSchema")) {
                    listOfSchLoc.add(getSchemaLocationForXSDSimpleType(schemaNameSpace));
                    inlineSchInd++;
                }
            }
        }
        return listOfSchLoc;
    }

    private Map<QName, String> getNameSpacesMap(WSDLModel wsdlModel) {
        Map<QName, String> map = wsdlModel.getDefinitions().getAttributeMap();
        return map;
    }

    private SchemaLocation getSchemaLocationForXSDSimpleType(String schemaNameSpace) {
        SchemaLocation schLocation = new SchemaLocation(null, null, null, false, schemaNameSpace);
        schLocation.setSimpleType(true);
        return schLocation;
    }

    private String getWSDLRelativePath(File wsdlFileLocation, File projectSourceDirectory) {
        return getRelativePath(wsdlFileLocation, projectSourceDirectory);
/* vlv # 157613
        String wsdlFileParentDirPath = wsdlFileLocation.getParent();
        wsdlFileParentDirPath = wsdlFileParentDirPath.replace('\\', '/');

        String projectSourceDirPath = projectSourceDirectory.getAbsolutePath().replace('\\', '/');

        int ind1 = wsdlFileParentDirPath.indexOf(projectSourceDirPath);
        if (ind1 == -1) {
            throw new RuntimeException("WSDL Location is not relative to Project source directory");
        }
        return wsdlFileParentDirPath.substring(ind1 + projectSourceDirPath.length());
*/
    }

    private String getRelativePath(File home, File f){
        return matchPathLists(getPathList(home), getPathList(f));
    }

    private List getPathList(File f) {
        List l = new ArrayList();
        File r;
        try {
            r = f.getCanonicalFile();
            while(r != null) {
                l.add(r.getName());
                r = r.getParentFile();
            }
        }
        catch (IOException e) {
            e.printStackTrace();
            l = null;
        }
        return l;
    }
    
    private String matchPathLists(List r, List f) {
        int i;
        int j;
        String s;
        // start at the beginning of the lists
        // iterate while both lists are equal
        s = "";
        i = r.size()-1;
        j = f.size()-1;

        // first eliminate common root
        while((i >= 0)&&(j >= 0)&&(r.get(i).equals(f.get(j)))) {
            i--;
            j--;
        }

        // for each remaining level in the home path, add a ..
        for(;i>=0;i--) {
            s += ".." + File.separator;
        }

        // for each level in the file path, add the path
        for(;j>=1;j--) {
            s += f.get(j) + File.separator;
        }

        // file name
        s += f.get(0);
        return s;
    }

    private void doValidations(File wsdlFileLocation, File buildDir) {
        if (wsdlFileLocation == null || !wsdlFileLocation.exists()) {
            throw new RuntimeException(" WSDLFileLocation is invalid");
        }
        if (buildDir == null || !buildDir.exists()) {
            throw new RuntimeException(" Build directory is invalid");
        }

        String wsdlFileParentDirPath = wsdlFileLocation.getParent();
        if (wsdlFileParentDirPath == null) {
            throw new RuntimeException(" WSDLFileLocation is invalid");
        }
    }

    private void addWSDLNamespacesToXMLElement(Element elem, Map mapOfNamespaces) {
        //  NamedNodeMap nmMap = elem.getAttributes();
        Iterator<Map.Entry> nsMapEntryItr = mapOfNamespaces.entrySet().iterator();
        elem.setAttribute("xmlns", "http://schemas.xmlsoap.org/wsdl/");

        while (nsMapEntryItr.hasNext()) {
            Map.Entry nsMapEntry = nsMapEntryItr.next();
            String mapPrefix =( (QName) nsMapEntry.getKey()).getLocalPart();
            if (mapPrefix.equals("xmlns:") || !mapPrefix.startsWith("xmlns:")) {
                continue;
            }
            String mapNS = (String) nsMapEntry.getValue();
            if (!elem.hasAttribute(mapPrefix)) {
                elem.setAttribute(mapPrefix, mapNS);
            }
        }
    }
    

    public static void writeXML(String genFile, Element elem) {
        PrintWriter pw = null;
        try {
            // Use a Transformer for output
            TransformerFactory tFactory = TransformerFactory.newInstance();
            Transformer transformer = tFactory.newTransformer();
            DOMSource source = new DOMSource(elem);
            pw = new PrintWriter(genFile, "UTF-8");
            StreamResult result = new StreamResult(pw);

            transformer.setOutputProperty(OutputKeys.METHOD, "xml"); // NOI18N
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8"); // NOI18N
            transformer.setOutputProperty(OutputKeys.MEDIA_TYPE, "text/xml"); // NOI18N
            transformer.setOutputProperty(OutputKeys.STANDALONE, "yes"); // NOI18N

            // indent the output to make it more legible...
            //  transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");  // NOI18N
            //  transformer.setOutputProperty(OutputKeys.INDENT, "yes");  // NOI18N
            transformer.transform(source, result);
        } catch (Exception ex) {

        } finally {
            if (pw != null) {
                try {
                    pw.flush();
                } catch (Exception ex) {

                }

                try {
                    pw.close();
                } catch (Exception ex) {

                }
            }
        }
    }

    public class SchemaLocation {
        private File mPhysicalFileLocation = null;
        //Relative location of schema ( either to project or build dir) 
        private String mRelativeLocation = null;
        //If mRelativeLocation is relative to Project source directory
        private boolean mbRelativeToPrjSrcDir = true;

        private String mLocationURI = null;
        private String mNamespaceURI = null;

        private boolean mbSimpleType = false;

        public SchemaLocation(String locationURI, File physicalFileLocation, String relativeLocation, boolean bRelativeToProject,
                String namespaceURI) {
            mLocationURI = locationURI;
            mPhysicalFileLocation = physicalFileLocation;
            mRelativeLocation = relativeLocation;
            mbRelativeToPrjSrcDir = bRelativeToProject;
            mNamespaceURI = namespaceURI;

        }

        public File getFileLocation() {
            return mPhysicalFileLocation;
        }

        public String getRelativeLocation() {
            return mRelativeLocation;
        }

        public String getLocationURI() {
            return mLocationURI;
        }

        public boolean isRelativeToProject() {
            return mbRelativeToPrjSrcDir;
        }

        public String getNamespaceURI() {
            return mNamespaceURI;
        }

        public boolean isSimpleType() {
            return mbSimpleType;
        }

        public void setSimpleType(boolean simpleType) {
            mbSimpleType = simpleType;
        }

        public int hashCode() {
            if (mNamespaceURI != null) {
                return mNamespaceURI.hashCode();
            }
            return 1;

        }

        public boolean equals(Object obj) {
            if (!(obj instanceof SchemaLocation)) {
                return false;
            }
            SchemaLocation compSchLoc = (SchemaLocation) obj;
            if (((mPhysicalFileLocation != null && compSchLoc.getFileLocation() != null && compSchLoc.getFileLocation().equals(
                    mPhysicalFileLocation)) || (mPhysicalFileLocation == null && compSchLoc.getFileLocation() == null))
                    && ((mRelativeLocation != null && compSchLoc.getRelativeLocation() != null && compSchLoc.getRelativeLocation().equals(
                            mRelativeLocation)) || (mRelativeLocation == null && compSchLoc.getRelativeLocation() == null))
                    && ((mNamespaceURI != null && compSchLoc.getNamespaceURI() != null && compSchLoc.getNamespaceURI()
                            .equals(mNamespaceURI)) || (mNamespaceURI == null && compSchLoc.getNamespaceURI() == null))
                    && ((mLocationURI != null && compSchLoc.getLocationURI() != null && compSchLoc.getLocationURI().equals(mLocationURI)) || (mLocationURI == null && compSchLoc
                            .getLocationURI() == null))) {
                return true;
            }
            return false;

        }
    }
    public static void dumpToFile(Document doc, String fName) throws Exception {
        PrintWriter w = new PrintWriter(fName, "UTF-8");
        w.print(doc.getText(0, doc.getLength()));
        w.close();
    }
    
}
