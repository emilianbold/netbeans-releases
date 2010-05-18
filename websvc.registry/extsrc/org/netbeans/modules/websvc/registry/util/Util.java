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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.websvc.registry.util;

import java.util.*;
import java.io.*;
import java.net.URL;
import java.net.MalformedURLException;
import org.w3c.dom.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;
import org.openide.*;
import org.openide.util.NbBundle;
import org.openide.awt.StatusDisplayer;
import com.sun.xml.rpc.util.JavaCompilerHelper;
import com.sun.xml.rpc.processor.util.ClientProcessorEnvironment;
import com.sun.xml.rpc.processor.model.Port;
import com.sun.xml.rpc.processor.model.java.JavaParameter;
import org.netbeans.modules.websvc.registry.jaxrpc.Wsdl2Java;
import org.netbeans.modules.websvc.registry.WebServiceException;
import org.netbeans.modules.websvc.registry.model.WebServiceData;
import org.netbeans.modules.websvc.registry.jaxrpc.WrapperClientBeanInfoWriter;

import org.openide.modules.InstalledFileLocator;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;

// BEGIN_NOI18N
/**
 * General utility methods for WSDL documents.
 */
public class Util {
//    public static final int BUFFER_SIZE = 4096;
//    public static final String xsdNamespace = "xsd";
//    final static public String WSDL_FILE_EXTENSION = "wsdl";
//    /*
//     * Removes any namespace prefix.
//     *    eg: 'xsd:element' -> 'element'
//     */
//    public static String removeNamespace(String tagName) {
//        int pos = tagName.indexOf(':');
//        if (pos < 0)
//            return tagName;
//        return tagName.substring(pos+1);
//    }
//    
//    /**
//     * Take an XML Schema data type and make a Java type out of it.
//     *    eg: 'xsd:string' -> 'java.lang.String'
//     */
//    /*
//    public static String xmlSchemaType2JavaType(String typeName) {
//        if ((xsdNamespace+":string").equals(typeName))
//            return "java.lang.String";
//        if ("char_lb_rb".equals(typeName))
//            return "char[]";
//        if (typeName.startsWith(xsdNamespace+":"))
//            return typeName.substring(xsdNamespace.length()+1);
//        return typeName;
//    }
//     */
//    public static void insertSimpleTypes(Map typeMapping) {
//        typeMapping.put("java.lang.Integer", "java.lang.Integer");
//        typeMapping.put("java.lang.Short", "java.lang.Short");
//        typeMapping.put("java.lang.Long", "java.lang.Long");
//        typeMapping.put("java.lang.Double", "java.lang.Double");
//        typeMapping.put("java.lang.Boolean", "java.lang.Boolean");
//        typeMapping.put("java.lang.Character", "java.lang.Character");
//        typeMapping.put("java.lang.Float", "java.lang.Float");
//        typeMapping.put("java.lang.StringBuffer", "java.lang.StringBuffer");
//        typeMapping.put("java.lang.Byte", "java.lang.Byte");
//        typeMapping.put("java.lang.String", "java.lang.String");
//        typeMapping.put("java.math.BigInteger", "java.math.BigInteger");
//        typeMapping.put("char_lb_rb", "char[]");
//        typeMapping.put("string", "java.lang.String");
//        typeMapping.put("int", "int");
//        typeMapping.put("short", "short");
//        typeMapping.put("long", "long");
//        typeMapping.put("double", "double");
//        typeMapping.put("boolean", "boolean");
//        typeMapping.put("char", "char");
//        typeMapping.put("float", "float");
//        typeMapping.put("byte", "byte");
//        typeMapping.put("decimal", "java.math.BigDecimal");
//        typeMapping.put("dateTime", "java.util.Date");
//        typeMapping.put((xsdNamespace+":string").intern(), "java.lang.String");
//        typeMapping.put((xsdNamespace+":int").intern(), "int");
//        typeMapping.put((xsdNamespace+":short").intern(), "short");
//        typeMapping.put((xsdNamespace+":long").intern(), "long");
//        typeMapping.put((xsdNamespace+":double").intern(), "double");
//        typeMapping.put((xsdNamespace+":boolean").intern(), "boolean");
//        typeMapping.put((xsdNamespace+":char").intern(), "char");
//        typeMapping.put((xsdNamespace+":float").intern(), "float");
//        typeMapping.put((xsdNamespace+":byte").intern(), "byte");
//        typeMapping.put((xsdNamespace+":decimal").intern(), "java.math.BigDecimal");
//        typeMapping.put((xsdNamespace+":dateTime").intern(), "java.util.Date");
//    }
//    
//    private static Map baseTypes = null;
//    public static boolean isSerializerNeeded(String type) {
//        if (baseTypes == null) {
//            baseTypes = new HashMap();
//            insertSimpleTypes(baseTypes);
//        }
//        return !baseTypes.containsKey(type);
//    }
//    
//    /**
//     * Given an XML Schema Element, figure out the java type to use.
//     */
//    public static String getType(Element el, Map typeMapping,
//    Map elementTypeMapping) {
//        if (el == null)
//            return null;
//        String elementName = el.getAttribute("name");
//        String type = null;
//        if ((xsdNamespace+":element").equals(el.getNodeName())) {
//            //System.out.println("element name="+elementName);
//            String typeAttribute = el.getAttribute("type");
//            if (typeAttribute != null && !typeAttribute.equals("") &&
//            !typeAttribute.equals("SOAP-ENC:Array")) {
//                type = (String) typeMapping.get(typeAttribute);
//            } else {
//                // Recurse in and see what kind of type this thing is.
//                type = getType(findFirstNode(el), typeMapping, elementTypeMapping);
//            }
//        } else if ((xsdNamespace+":simpleType").equals(el.getNodeName())) {
//            type = (String) typeMapping.get(elementName);
//            if (type == null) {
//                try {
//                    Element restrictionNode = findFirstNodeByName(el, xsdNamespace+":restriction");
//                    type = (String) typeMapping.get(restrictionNode.getAttribute("base"));
//                } catch (NotFoundException e) {
//                    // It's okay (for now), just go with our default.
//                }
//            }
//        } else if ((xsdNamespace+":complexType").equals(el.getNodeName())) {
//            Element annotationNode = null;
//            try {
//                annotationNode = findFirstNodeByName(el, xsdNamespace+":annotation");
//                Element appInfoNode = findFirstNodeByName(annotationNode,
//                xsdNamespace+":appinfo");
//                try {
//                    Node bindingNode = findFirstNodeByName(appInfoNode, "BINDING_TYPE");
//                    type = bindingNode.getFirstChild().getNodeValue();
//                } catch (NotFoundException e1) {
//                    try {
//                        Node oldbindingNode = findFirstNodeByName(appInfoNode, "OLDBINDING_TYPE");
//                        type = oldbindingNode.getFirstChild().getNodeValue();
//                        /*if (elementName != null) {
//                            String baseName = GenPresentation.baseName(type).intern();
//                            if (typeMapping.get(baseName) == null) {
//                                // Put our baseName in there too, since
//                                // we might look for it later by that name.
//                                //System.out.println("Putting in "+baseName+" as "+type);
//                                typeMapping.put(baseName, type);
//                            }
//                        }*/
//                    } catch (NotFoundException e2) {
//                        Node beanNode = findFirstNodeByName(appInfoNode, "BEAN_TYPE");
//                        type = beanNode.getFirstChild().getNodeValue();
//                    }
//                }
//            } catch (NotFoundException e) {
//                //e.printStackTrace();
//                type = "void";   // NOI18N
//            }
//            NodeList nodes = el.getChildNodes();
//            for (int i = 0; i < nodes.getLength(); ++i) {
//                Node node = nodes.item(i);
//                if (!(node instanceof Element))
//                    continue;
//                Element childEl = (Element) node;
//                if (childEl == annotationNode)
//                    continue;
//                // Insert our children's types into our mapping.
//                getType(childEl, typeMapping, elementTypeMapping);
//            }
//        } else if ((xsdNamespace+":sequence").equals(el.getNodeName())) {
//            NodeList nodes = el.getChildNodes();
//            for (int i = 0; i < nodes.getLength(); ++i) {
//                Node node = nodes.item(i);
//                if (!(node instanceof Element))
//                    continue;
//                Element childEl = (Element) node;
//                // Insert our children's types into our mapping.
//                getType(childEl, typeMapping, elementTypeMapping);
//            }
//        } else if ((xsdNamespace+":import").equals(el.getNodeName())) {
//            // Nothing for us to do here....
//        } else {
//            //System.out.println("Unfamiliar XML Schema element: "+el.getNodeName());	// NOI18N
//            return null;
//        }
//        if (type == null)
//            type = "void";
//        //System.out.println("Found type: elementName="+elementName+" type="+type);
//        type = type.intern();
//        if (elementName != null) {
//            typeMapping.put(elementName.intern(), type);
//        }
//        elementTypeMapping.put(el, type);
//        return type;
//    }
//    
//    /**
//     * Return a Collection of all child Elements
//     */
//    public static Collection getChildElements(Element parent) {
//        Collection result = new LinkedList();
//        NodeList children = parent.getChildNodes();
//        for (int i = 0; i < children.getLength(); ++i) {
//            Node child = children.item(i);
//            if (!(child instanceof Element))
//                continue;
//            result.add(child);
//        }
//        return result;
//    }
//    
//    public static Element findFirstNode(Element node) {
//        NodeList nodes = node.getChildNodes();
//        for (int i = 0; i < nodes.getLength(); ++i) {
//            Node n = nodes.item(i);
//            if (n instanceof Element)
//                return (Element) n;
//        }
//        return null;
//    }
//    
//    public static Element findFirstNodeByName(Element node, String name) throws NotFoundException {
//        return findFirstNodeByName(node.getChildNodes(), name);
//    }
//    
//    /**
//     * Search for a node named @param name.  Namespace is ignored.
//     */
//    public static Element findFirstNodeByName(NodeList nodes, String name) throws NotFoundException {
//        name = removeNamespace(name);
//        for (int i = 0; i < nodes.getLength(); ++i) {
//            Node n = nodes.item(i);
//            String localNodeName = n.getLocalName();
//            if (localNodeName == null) {
//                localNodeName = removeNamespace(n.getNodeName());
//            }
//            if (name.equals(localNodeName))
//                return (Element) n;
//        }
//        
//        
//        throw new NotFoundException(MessageFormat.format(NbBundle.getMessage(Util.class,
//        "MSG_UnableToFindNode"),
//        new Object[] {name}), name);
//    }
//    
//    public static Element findFirstNodeByNames(Element node, String[] names) throws NotFoundException {
//        for (int i = 0; i < names.length; ++i)
//            node = findFirstNodeByName(node, names[i]);
//        return node;
//    }
//    
//    public static Element findFirstNodeByName(Document doc, String name) throws NotFoundException {
//        return findFirstNodeByName(doc.getChildNodes(), name);
//    }
//    
//    //Following methods were copied from com.sun.forte4j.webdesigner.xmlcomponent.Util
//    //because the wsdl module cannot be dependent on jwd.
//    
//    public static Class getClass(String className) {
//        Class cls;
//        if (className == null)
//            return null;
//        if (isClassArray(className)) {
//            // Recursively figure out what our type is.
//            // Is this the best way for getting an Array type?
//            cls = getClass(className.substring(0, className.length()-2));
//            Object arrayObject = java.lang.reflect.Array.newInstance(cls, 0);
//            return arrayObject.getClass();
//        }
//        //ClassLoader cl = Top Manager.getDefault().currentClassLoader();
//        //
//        // Removing Top Manager calls (see Issuezilla 31753). Replace above with:
//        ClassLoader cl = ClassPath.getClassPath(null, ClassPath.EXECUTE).getClassLoader(false);
//        
//        // BEGIN_NOI18N
//        if ("int".equals(className))
//            cls = Integer.TYPE;
//        else if ("long".equals(className))
//            cls = Long.TYPE;
//        else if ("char".equals(className))
//            cls = Character.TYPE;
//        else if ("short".equals(className))
//            cls = Short.TYPE;
//        else if ("double".equals(className))
//            cls = Double.TYPE;
//        else if ("float".equals(className))
//            cls = Float.TYPE;
//        else if ("byte".equals(className))
//            cls = Byte.TYPE;
//        else if ("boolean".equals(className))
//            cls = Boolean.TYPE;
//        else if ("void".equals(className))
//            cls= Void.TYPE;
//        else {
//            try {
//                cls = cl.loadClass(className);
//            } catch (java.lang.ClassNotFoundException e) {
//                e.printStackTrace();
//                cls = null;
//            }
//        }
//        // END_NOI18N
//        return cls;
//    }
//    
//    public static boolean compile(DataObject[] dataObjects) {
//        CompilerJob job = new CompilerJob(Compiler.DEPTH_ONE);
//        for(int i = 0; i < dataObjects.length; i++) {
//            CompilerCookie comp = (CompilerCookie) dataObjects[i].
//            getCookie(CompilerCookie.Compile.class);
//            if (comp != null) {
//                comp.addToJob(job, Compiler.DEPTH_ONE);
//            }
//        }
//        CompilerTask task = job.start();
//        return task.isSuccessful();
//    }
//    
//    static public boolean isClassArray(String returnClassName) {
//        return returnClassName.endsWith("[]");                                  // NOI18N
//    }
//    
//    static public boolean isCollectionType(String bindingType) {
//        boolean isCollection = false;
//        Class c = getClass(bindingType);
//        if(c != null){
//            if (java.util.Collection.class.isAssignableFrom(c))
//                isCollection = true;
//        }
//        
//        return isCollection;
//    }
    
    // change all occurrences of oldSubString to newSubString
    public static String changeString(String inString, String oldSubString, String newSubString) {
        if (oldSubString.trim().equals(""))
            return inString;
        
        int start = 0;
        int end = 0;
        StringBuffer changedString = new StringBuffer("");
        
        end = inString.indexOf(oldSubString, start);
        while(end != -1) {
            //add substring before oldSubString and append newSubString
            changedString.append(inString.substring(start, end) + newSubString);
            
            //recompute starting index to end of changed substring
            start = end + oldSubString.length();
            //recompute end index
            end = inString.indexOf(oldSubString, start);
        }
        
        //append remainder of the String, if any
        changedString.append(inString.substring(start));
        
        return changedString.toString();
    }
	
    /**
     * This method will create a jar file for a given WebServiceData object.
     *
     * @param inWSData - the WebServiceData to create the jar file for.
     * @returns jarfileName - the file name (without path) of the jar file created.  null if not created.
     *
     *
     */
    public static boolean createWSJar(WebServiceData inWSData, OutputStream inOutputStream, String inJarFileName) {
        
        String jarFileName = null;
        
        OutputStream outputStream = null;
        
        FileOutputStream fileOutputStream = null;
        Date date = new Date();
        File tmpOutputDir = null;
        File errorFile = null;
        try{
            File tempFile = File.createTempFile("wstemp","ws");
            tmpOutputDir = new File(tempFile.getParentFile(), "wstemp" + date.getTime());
            if (!tmpOutputDir.exists()) tmpOutputDir.mkdirs();
            errorFile = File.createTempFile("wscompile","error",tempFile.getParentFile());
            
            /**
             * if the outputstream is null, create a temporary directory for the wscompile.
             */
            if(null == inOutputStream) {
                fileOutputStream = new FileOutputStream(errorFile);
                outputStream = fileOutputStream;
            } else {
                outputStream = inOutputStream;
            }
        }catch (IOException ioe){
            ErrorManager.getDefault().notify(ioe);
//            StatusDisplayer.getDefault().displayError(NbBundle.getMessage(Util.class, "PROXY_GEN_ERROR"),2);
            StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(Util.class, "PROXY_GEN_ERROR"));
            return false;
        }
        
        /**
         * Now create the class to do the WSDL to Java conversion
         */
        Wsdl2Java wsdl2Java = new Wsdl2Java();
        /**
         * Set the output directory to the temp one we just created.
         */
        wsdl2Java.setOutputDirectory(tmpOutputDir.getAbsolutePath());
        
        /**
         * Set the package.
         */
        wsdl2Java.setPackageName(inWSData.getPackageName());
        
        /**
         * Set the WSDL to use.
         */
        try {
            wsdl2Java.setWsdlUrl(new URL(inWSData.getURL()));
        } catch(MalformedURLException mfue) {
            ErrorManager.getDefault().notify(mfue);
//            StatusDisplayer.getDefault().displayError(NbBundle.getMessage(Util.class, "WS_NOJAR_ERROR"),2);
            StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(Util.class, "WS_NOJAR_ERROR"));
            return false;
        }
        
        /**
         * Set the proxy information.
         */
        System.setProperty("http.proxyHost", WebProxySetter.getInstance().getProxyHost());
        System.setProperty("http.proxyPort", WebProxySetter.getInstance().getProxyPort());
        
        
        /**
         * Do it!
         */
        
        /**
         * Let the user know we'return doing some time consuming process.
         */
        StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(Util.class, "WS_CREATING_JARFILE"));
        
        if(!wsdl2Java.execute(inWSData,outputStream)) {
            ErrorManager.getDefault().log("Util.createWSJar:" + NbBundle.getMessage(Util.class, "WS_WSDL2JAVA_ERROR"));
//            StatusDisplayer.getDefault().displayError(NbBundle.getMessage(Util.class, "WS_WSDL2JAVA_ERROR"),2);
            StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(Util.class, "WS_WSDL2JAVA_ERROR"));
            return false;
        }
        StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(Util.class, "WS_CREATING_JARFILE_FINISHED"));
        
        /**
         * Now we need to compile the wrapper client java files.
         *
         */
        
        ArrayList argList = new ArrayList();
        
        argList.add("-d");
        argList.add(tmpOutputDir.getAbsolutePath());
        argList.add("-classpath");
        String classPath = tmpOutputDir.getAbsolutePath() + File.pathSeparator + Util.getRuntimeClassPath();
        argList.add(classPath);
        argList.add("-g");
        
        /**
         * Now add the files to be compiled
         */
        File wrapperFile = wsdl2Java.getWebserviceClient();
        argList.add(wrapperFile.getAbsolutePath());
        File wrapperBeanInfoFile = wsdl2Java.getWebserviceClientBeanInfo();
        argList.add(wrapperBeanInfoFile.getAbsolutePath());
        
        String [] args = (String [])argList.toArray(new String[0]);
        
        // ByteArrayOutputStream javacOutput = new ByteArrayOutputStream();
        
        /**
         * Define a temp file for the compile results.
         */
//        String outputDir = System.getProperty("user.home");
//        File outputDirFile = new File(outputDir);
        File tempFile = null;
        try{
//            tempFile = File.createTempFile("wstemp","compile_errors",outputDirFile);
            tempFile = File.createTempFile("wstemp","compile_errors");
        }catch (IOException ioe){
            ErrorManager.getDefault().notify(ioe);
//            StatusDisplayer.getDefault().displayError(NbBundle.getMessage(Util.class, "PROXY_GEN_ERROR"),2);
            StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(Util.class, "PROXY_GEN_ERROR"));
            return false;
        }
        
        FileOutputStream out = null;
        
        try {
            out = new FileOutputStream(tempFile);
        } catch(FileNotFoundException fnfe) {
            
            ErrorManager.getDefault().notify(fnfe);
//            StatusDisplayer.getDefault().displayError(NbBundle.getMessage(Util.class, "PROXY_GEN_ERROR"),2);
            StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(Util.class, "PROXY_GEN_ERROR"));
            return false;
        }
        
        JavaCompilerHelper compilerHelper = new JavaCompilerHelper(out);
        
        StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(Util.class, "WS_CLIENTWRAPPER_COMPILING"));
        
        boolean result = compilerHelper.compile(args);
        if (!result) {
            ErrorManager.getDefault().log("Util.createWSJar: " + NbBundle.getMessage(Util.class, "WS_CLIENTWRAPPER_COMPILE_ERROR") + tempFile == null ? "" : tempFile.getAbsolutePath());
//            StatusDisplayer.getDefault().displayError(NbBundle.getMessage(Util.class, "PROXY_GEN_ERROR"),2);
            StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(Util.class, "PROXY_GEN_ERROR"));
            return false;
        } else {
            StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(Util.class, "WS_CLIENTWRAPPER_COMPILE_OK"));
            /**
             * clean up the output file since the compile was successful
             */
            tempFile.delete();
        }
        
        
        /**
         * Now copy the web service icon image to the folder that will be jarred up.
         */
        try {
            // Copy the Image contents from the URL  into the new file craeted in the backing folder.
            URL imageUrl = Util.class.getResource("/org/netbeans/modules/websvc/registry/resources/webservice.png");
            DataInputStream in = new DataInputStream(imageUrl.openStream());
            String iconImagePath = tmpOutputDir.getAbsolutePath() + File.separator + inWSData.getPackageName().replace('.', File.separatorChar);
            File outputFile = new File(iconImagePath,WrapperClientBeanInfoWriter.WEBSERVICE_ICON_FILENAME);
            DataOutputStream outImage = new DataOutputStream(new FileOutputStream(outputFile));
            
            byte[] bytes = new byte[1024];
            int byteCount = in.read(bytes);
            
            while ( byteCount > -1 ) {
                outImage.write( bytes );
                byteCount = in.read(bytes);
            }
            outImage.flush();
            outImage.close();
            in.close();
        } catch (IOException ioe) {
            ErrorManager.getDefault().notify(ioe);
//            StatusDisplayer.getDefault().displayError(NbBundle.getMessage(Util.class, "IMAGE_COPY_ERROR"),2);
            StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(Util.class, "IMAGE_COPY_ERROR"));
            return false;
        }
        
        
        /**
         * Now create the jar file from the output.
         */
        File wsJarFile = new File(inJarFileName);
        JarUtil jarUtil = new JarUtil(wsJarFile);
        jarUtil.addDirectory(new File(wsdl2Java.getOutputDirectory()));
        
        
        
        return true;
    }
    
    public static String upperCaseFirstChar(String inString) {
        if(null == inString) {
            throw new IllegalArgumentException("Null string passed!");
        }
        String returnString =  new String(inString);
        String firstCharacter = returnString.substring(0,1);
        returnString = firstCharacter.toUpperCase() + returnString.substring(1);
        
        return returnString;
    }
    /**
     * This method will take a WSDL port name like "threat.cfc" and change it to
     * "ThreatCfc"
     */
    public static String getProperPortName(String inPortName) {
        String returnString = "";
        /**
         * parse the string by ".", uppercase each piece, and put them back together
         */
        
        /**
         * If we don't have a "." in the string, simply uppercase the first char and
         * return it.
         */
        if(inPortName.indexOf(".") == -1) {
            returnString = Util.upperCaseFirstChar(inPortName);
            return returnString;
        }
        
        StringTokenizer tokenizer = new StringTokenizer(inPortName,".");
        while(tokenizer.hasMoreTokens()) {
            String currentToken = tokenizer.nextToken();
            returnString += Util.upperCaseFirstChar(currentToken);
            
        }
        
        return returnString;
        
    }
    
    public static String [] getRunTimeJarFiles() throws WebServiceException {
        /**
         * Read in the Runtime Jar file Names
         */
        
        ArrayList returnJarFileNames = new ArrayList();
        
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = null;
        
        try {
            builder = factory.newDocumentBuilder();
            
        } catch(ParserConfigurationException pe) {
            ErrorManager.getDefault().notify(pe);
            ErrorManager.getDefault().log("Util.getRunTimeJarFiles: ParserConfigurationException=" + pe);
            throw new WebServiceException(NbBundle.getMessage(Util.class, "ERROR_READING_RUNTIMEJARS"),pe);
        }
        
        Document document = null;
        try {
            File runtimeJarsFile = InstalledFileLocator.getDefault().locate(
                    "config" + File.separator + "WebServices" + File.separator +
                    "websvc_runtimejars.xml", null, false);
            document = builder.parse(runtimeJarsFile);
        } catch(SAXException se) {
            ErrorManager.getDefault().notify(se);
            ErrorManager.getDefault().log("Util.getRunTimeJarFiles: SAXException=" + se);
            throw new WebServiceException(NbBundle.getMessage(Util.class, "ERROR_READING_RUNTIMEJARS"),se);
        } catch(IOException ioe) {
            ErrorManager.getDefault().notify(ioe);
            ErrorManager.getDefault().log("Util.getRunTimeJarFiles: IOException=" + ioe);
            throw new WebServiceException(NbBundle.getMessage(Util.class, "ERROR_READING_RUNTIMEJARS"),ioe);
        }
        
        NodeList list = document.getElementsByTagName("Jar");
        
        String serverInstanceIDs[] = Deployment.getDefault().getServerInstanceIDs ();
        J2eePlatform platform = null;
        for (int i = 0; i < serverInstanceIDs.length; i++) {
            J2eePlatform p = Deployment.getDefault().getJ2eePlatform (serverInstanceIDs [i]);
            if (p != null && p.isToolSupported ("wscompile")) {
                platform = p;
                break;
            }
        }
        File appserverRoot = platform == null ? null : platform.getPlatformRoots () [0];
        String asRootPath = (appserverRoot != null) ? appserverRoot.getAbsolutePath() : "";
        asRootPath = asRootPath.replace('\\', '/');
            
        Node currentNode = null;
        for (int ii=0; ii < list.getLength(); ii++) {
            currentNode = list.item(ii);
            String name = currentNode.getNodeName();
            String localName = currentNode.getLocalName();
            String value = currentNode.getNodeValue();
            NamedNodeMap nodeMap = currentNode.getAttributes();
            Node fileNode = nodeMap.getNamedItem("file");
            String jarString = "";
            try {
                jarString = fileNode.getNodeValue();
            } catch (DOMException de) {
                ErrorManager.getDefault().notify(de);
                ErrorManager.getDefault().log("Util.getRunTimeJarFiles: DOMException=" + de);
                throw new WebServiceException(NbBundle.getMessage(Util.class, "ERROR_READING_RUNTIMEJARS"),de);
            }
            if (jarString.indexOf("\\{appserv\\.home\\}") > -1) {
                jarString = jarString.replaceAll("\\{appserv\\.home\\}", asRootPath);
            } else {
                File f = InstalledFileLocator.getDefault().locate(jarString, null, false);
                if (f != null) {
                    jarString = f.getPath();
                } 
            }
            returnJarFileNames.add(jarString);
        }
        
        return (String []) returnJarFileNames.toArray(new String[0]);
        
    }
    
    /**
     * This method will construct the default classpath to be used for running the "wscompile" tool.
     */
    public static String getRuntimeClassPath() {
        String [] jarFileNames = null;
        String returnClassPath = "";
        try {
            jarFileNames = Util.getRunTimeJarFiles();
        } catch(WebServiceException wse) {
//            StatusDisplayer.getDefault().displayError(wse.getMessage(),2);
            StatusDisplayer.getDefault().setStatusText(wse.getMessage());
            return returnClassPath;
        }
        
        for(int ii=0; null != jarFileNames && ii < jarFileNames.length; ii++) {
            returnClassPath += jarFileNames[ii];
            /**
             * If there's another path, add the path separator
             */
            if(ii+1 < jarFileNames.length) {
                returnClassPath += File.pathSeparator;
            }
        }
        
        return returnClassPath;
        
    }
    /**
     * This method will determine if a package name is valid.  For each qualification of the package name (part between
     * the ".", the first character will be checked against Character.isJavaIdentifierStart() then each character will
     * be checked against Character.isJavaIdentifierPart().
     */
    public static boolean isValidPackageName(String inPackageName) {
        if(null == inPackageName || inPackageName.length() == 0) return false;
        
        /**
         * A "$" seems to be a valid part but messes with the package name so we need to check for it
         * explicitly.
         */
        if(inPackageName.indexOf("$") != -1) {
            return false;
        }
        
        
        /**
         * If we have more than one qualification ("."), we need to check the start of each
         * one for validity then each character of each one.
         */
        
        StringTokenizer tokenizer = new StringTokenizer(inPackageName,".");
        
        if(tokenizer.hasMoreTokens()) {
            String currentLevel = null;
            while(tokenizer.hasMoreTokens()) {
                currentLevel = (String)tokenizer.nextToken();
                if(!Character.isJavaIdentifierStart(currentLevel.charAt(0))) {
                    return false;
                }
                for(int ii=0; ii < currentLevel.length(); ii++) {
                    if(!Character.isJavaIdentifierPart(currentLevel.charAt(ii))) {
                        return false;
                    }
                }
            }
        } else {
            /**
             * we only have a single qualification for the package name.
             */
            if(!Character.isJavaIdentifierStart(inPackageName.charAt(0))) {
                return false;
            }
            for(int ii=0; ii < inPackageName.length(); ii++) {
                if(!Character.isJavaIdentifierPart(inPackageName.charAt(ii))) {
                    return false;
                }
            }
        }
        return true;
    }
    /**
     * This method will determine if an identifier is valid.  The first character will be checked against Character.isJavaIdentifierStart() then each character will
     * be checked against Character.isJavaIdentifierPart().
     */
    public static boolean isValidIdentifier(String inIdentifier) {
        if(null == inIdentifier || inIdentifier.length() == 0) return false;
        
        
        if(!Character.isJavaIdentifierStart(inIdentifier.charAt(0))) {
            return false;
        }
        for(int ii=0; ii < inIdentifier.length(); ii++) {
            if(!Character.isJavaIdentifierPart(inIdentifier.charAt(ii))) {
                return false;
            }
        }
        
        return true;
    }
   
    /**
     * Bug fix: 5059732
     * This method will return the correct type for the parameter.  If the JavaParameter is considered a "Holder"
     * the holder class name will be used.
     *
     *TODO: include in JAX-RPC API
     *
     * If the parameter is a "Holder" we need the holder type and not the JavaType.  This is
     * typically the case when there is no return type and the parameter's meant to be mutable, pass-by-reference
     * type parameters.  I took the code below directly from the JAX-RPC class:
     * "com.sun.xml.rpc.processor.generator.StubGenerator" except that I'm not checking the Operation for an Array type.
     * - David Botterill 6/8/2004
     *
     */
    public static String getParameterType(Port inPort, JavaParameter inParameter) {
        
        String parameterType = "";
        ClientProcessorEnvironment env = new ClientProcessorEnvironment(new ByteArrayOutputStream(), null, null);
        
        if (inParameter.isHolder()) {
            if (inParameter.getHolderName() == null) {
                parameterType = env.getNames().holderClassName(inPort, inParameter.getType());
            } else {
                parameterType = inParameter.getHolderName();
            }
        } else {
            parameterType =inParameter.getType().getName();
        }
        
        return parameterType;
        
    }	
}
// END_NOI18N
