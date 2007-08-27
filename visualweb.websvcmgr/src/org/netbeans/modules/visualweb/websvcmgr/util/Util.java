/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.visualweb.websvcmgr.util;


import com.sun.tools.ws.processor.model.java.JavaMethod;
import java.io.FileInputStream;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.*;
import java.text.*;
import java.io.*;
import org.netbeans.modules.visualweb.websvcmgr.WebServiceDescriptor;
import org.netbeans.modules.visualweb.websvcmgr.consumer.DesignerWebServiceExtData;
import org.netbeans.modules.visualweb.websvcmgr.consumer.DesignerWebServiceExtImpl;
import org.netbeans.modules.visualweb.websvcmgr.model.WebServiceData;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlPort;
import org.openide.ErrorManager;
import org.openide.util.Exceptions;

import org.w3c.dom.*;

import com.sun.tools.ws.processor.model.java.JavaParameter;

// import com.sun.tools.ws.wscompile.JavaCompilerHelper;
import com.sun.tools.ws.processor.model.java.JavaType;
// import com.sun.tools.ws.processor.util.ClientProcessorEnvironment;
import java.net.URLClassLoader;

/*
import com.sun.xml.rpc.util.JavaCompilerHelper;
import com.sun.xml.rpc.processor.util.ClientProcessorEnvironment;
import com.sun.xml.rpc.processor.model.Port;
import com.sun.xml.rpc.processor.model.java.JavaParameter;
 */

import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.modules.visualweb.api.designerapi.DesignerServiceHack;
/* SD
import org.netbeans.modules.visualweb.xml.rpc.util.JavaCompilerHelper;
import org.netbeans.modules.visualweb.xml.rpc.processor.util.ClientProcessorEnvironment;
import org.netbeans.modules.visualweb.xml.rpc.processor.model.Port;
import org.netbeans.modules.visualweb.xml.rpc.processor.model.java.JavaParameter;
 */
import org.netbeans.modules.visualweb.websvcmgr.NotFoundException;
import org.netbeans.modules.visualweb.websvcmgr.codegen.DataProviderMethod;
import org.netbeans.modules.visualweb.websvcmgr.codegen.DataProviderParameter;
import org.openide.util.NbBundle;
import org.openide.modules.InstalledFileLocator;
import org.netbeans.modules.visualweb.project.jsf.api.JsfProjectUtils;
import org.openide.filesystems.FileObject;

// BEGIN_NOI18N
/**
 * General utility methods for WSDL documents.
 */
public class Util {
    public static final String dataproviderJar = InstalledFileLocator.getDefault().locate("modules/ext/dataprovider.jar", null, false ).getAbsolutePath(); // NOI18N
    public static final String designTimeJar = InstalledFileLocator.getDefault().locate( "modules/org-netbeans-modules-visualweb-designtime.jar", null, false).getAbsolutePath(); // NOI18N
    
    public static final int BUFFER_SIZE = 4096;
    public static final String xsdNamespace = "xsd";
    final static public String WSDL_FILE_EXTENSION = "wsdl";
    
    public static boolean hasOutput(JavaMethod m) {
        JavaType type = m.getReturnType();
        
        if (!"void".equals(type.getRealName())) {
            return true;
        }else {
            // check for output Holders
            return getOutputHolderIndex(m) >= 0;
        }
    }
    
    public static int getOutputHolderIndex(JavaMethod m) {
        List<JavaParameter> params = m.getParametersList();
        if (params == null) return -1;
        
        for (int i = 0; i < params.size(); i++) {
            JavaParameter nextParam = params.get(i);
            if (nextParam.isHolder() && 
                    (nextParam.getParameter().isOUT() || nextParam.getParameter().isINOUT())) {
                return i;
            }
        }
        
        return -1;
    }
    
    public static String typeToString(Type type) {
        if (type instanceof ParameterizedType) {
            ParameterizedType paramType = (ParameterizedType)type;
            if (paramType.getOwnerType() != null) return null;
            
            Type rawType = paramType.getRawType();
            if (!(rawType instanceof Class)) {
                return null;
            }
            Class rawClass = (Class)rawType;
            
            Type[] argTypes = paramType.getActualTypeArguments();
            if (argTypes == null || argTypes.length == 0) {
                return null;
            }
            
            StringBuffer arguments = new StringBuffer();
            for (int i = 0; i < argTypes.length; i++) {
                String argument = typeToString(argTypes[0]);
                if (argument == null) {
                    return null;
                }else {
                    arguments.append(argument);
                }
                
                if (i != argTypes.length - 1) {
                    arguments.append(',');
                }
            }
            
            return rawClass.getCanonicalName() + "<" + arguments.toString() + ">";
        }else if (type instanceof GenericArrayType) {
            String component = typeToString(((GenericArrayType)type).getGenericComponentType());
            if (component != null) {
                return component + "[]";
            }
        }else if (type instanceof Class) {
            return ((Class)type).getCanonicalName();
        }
        
        return null;
    }
    
    
    /*
     * Removes any namespace prefix.
     *    eg: 'xsd:element' -> 'element'
     */
    public static String removeNamespace(String tagName) {
        int pos = tagName.indexOf(':');
        if (pos < 0)
            return tagName;
        return tagName.substring(pos+1);
    }
    
    public static String decapitalize(String name) {
        if( name == null || name.length() == 0 ) {
            return name;
        }
        
        char chars[] = name.toCharArray();
        chars[0] = Character.toLowerCase(chars[0]);
        return new String(chars);
    }
    
    
    /**
     * Return the Project that is currently active according to the Designer.
     *
     * @return currently active project or null, if none.
     */
    public static Project getActiveProject() {
        FileObject fileObject = DesignerServiceHack.getDefault()
        .getCurrentFile();
        if (fileObject == null) {
            return null;
        }
        return FileOwnerQuery.getOwner(fileObject);
    }
    
    private void addLibraryDefsAndRefs(String libName)
    throws IOException {
        
        Project project = getActiveProject();
        
        Library libDef = LibraryManager.getDefault().getLibrary(libName);
        if (libDef == null) {
            
        }
        
        // If needed, create new compile-time Library Ref
        if (!JsfProjectUtils.hasLibraryReference(project, libDef,
                ClassPath.COMPILE)) {
            if (!JsfProjectUtils.addLibraryReferences(project,
                    new Library[] { libDef }, ClassPath.COMPILE)) {
                
            }
        }
        
        // If needed, create new "deploy" Library Ref
        if (!JsfProjectUtils.hasLibraryReference(project, libDef,
                ClassPath.EXECUTE)) {
            if (!JsfProjectUtils.addLibraryReferences(project,
                    new Library[] { libDef }, ClassPath.EXECUTE)) {
            }
        }
    }
    
    /**
     */
    public static boolean isAcronyn( String name ) {
        // The name is consider as acronyn if it starts with two or more upper-case letters in a row
        if( name == null || name.length() == 0 ) {
            return false;
        }
        
        char chars[] = name.toCharArray();
        if( Character.isUpperCase(chars[0]) && Character.isUpperCase(chars[1]) )
            return true;
        else
            return false;
    }
    
    /**
     * Take an XML Schema data type and make a Java type out of it.
     *    eg: 'xsd:string' -> 'java.lang.String'
     */
    /*
    public static String xmlSchemaType2JavaType(String typeName) {
        if ((xsdNamespace+":string").equals(typeName))
            return "java.lang.String";
        if ("char_lb_rb".equals(typeName))
            return "char[]";
        if (typeName.startsWith(xsdNamespace+":"))
            return typeName.substring(xsdNamespace.length()+1);
        return typeName;
    }
     */
    public static void insertSimpleTypes(Map typeMapping) {
        typeMapping.put("java.lang.Integer", "java.lang.Integer");
        typeMapping.put("java.lang.Short", "java.lang.Short");
        typeMapping.put("java.lang.Long", "java.lang.Long");
        typeMapping.put("java.lang.Double", "java.lang.Double");
        typeMapping.put("java.lang.Boolean", "java.lang.Boolean");
        typeMapping.put("java.lang.Character", "java.lang.Character");
        typeMapping.put("java.lang.Float", "java.lang.Float");
        typeMapping.put("java.lang.StringBuffer", "java.lang.StringBuffer");
        typeMapping.put("java.lang.Byte", "java.lang.Byte");
        typeMapping.put("java.lang.String", "java.lang.String");
        typeMapping.put("java.math.BigInteger", "java.math.BigInteger");
        typeMapping.put("char_lb_rb", "char[]");
        typeMapping.put("string", "java.lang.String");
        typeMapping.put("int", "int");
        typeMapping.put("short", "short");
        typeMapping.put("long", "long");
        typeMapping.put("double", "double");
        typeMapping.put("boolean", "boolean");
        typeMapping.put("char", "char");
        typeMapping.put("float", "float");
        typeMapping.put("byte", "byte");
        typeMapping.put("decimal", "java.math.BigDecimal");
        typeMapping.put("dateTime", "java.util.Date");
        typeMapping.put((xsdNamespace+":string").intern(), "java.lang.String");
        typeMapping.put((xsdNamespace+":int").intern(), "int");
        typeMapping.put((xsdNamespace+":short").intern(), "short");
        typeMapping.put((xsdNamespace+":long").intern(), "long");
        typeMapping.put((xsdNamespace+":double").intern(), "double");
        typeMapping.put((xsdNamespace+":boolean").intern(), "boolean");
        typeMapping.put((xsdNamespace+":char").intern(), "char");
        typeMapping.put((xsdNamespace+":float").intern(), "float");
        typeMapping.put((xsdNamespace+":byte").intern(), "byte");
        typeMapping.put((xsdNamespace+":decimal").intern(), "java.math.BigDecimal");
        typeMapping.put((xsdNamespace+":dateTime").intern(), "java.util.Date");
    }
    
    private static Map baseTypes = null;
    public static boolean isSerializerNeeded(String type) {
        if (baseTypes == null) {
            baseTypes = new HashMap();
            insertSimpleTypes(baseTypes);
        }
        return !baseTypes.containsKey(type);
    }
    
    /**
     * Given an XML Schema Element, figure out the java type to use.
     * @param el
     * @param typeMapping
     * @param elementTypeMapping
     * @return
     */
    public static String getType(Element el, Map typeMapping,
            Map elementTypeMapping) {
        if (el == null)
            return null;
        String elementName = el.getAttribute("name");
        String type = null;
        if ((xsdNamespace+":element").equals(el.getNodeName())) {
            //System.out.println("element name="+elementName);
            String typeAttribute = el.getAttribute("type");
            if (typeAttribute != null && !typeAttribute.equals("") &&
                    !typeAttribute.equals("SOAP-ENC:Array")) {
                type = (String) typeMapping.get(typeAttribute);
            } else {
                // Recurse in and see what kind of type this thing is.
                type = getType(findFirstNode(el), typeMapping, elementTypeMapping);
            }
        } else if ((xsdNamespace+":simpleType").equals(el.getNodeName())) {
            type = (String) typeMapping.get(elementName);
            if (type == null) {
                try {
                    Element restrictionNode = findFirstNodeByName(el, xsdNamespace+":restriction");
                    type = (String) typeMapping.get(restrictionNode.getAttribute("base"));
                } catch (NotFoundException e) {
                    // It's okay (for now), just go with our default.
                }
            }
        } else if ((xsdNamespace+":complexType").equals(el.getNodeName())) {
            Element annotationNode = null;
            try {
                annotationNode = findFirstNodeByName(el, xsdNamespace+":annotation");
                Element appInfoNode = findFirstNodeByName(annotationNode,
                        xsdNamespace+":appinfo");
                try {
                    Node bindingNode = findFirstNodeByName(appInfoNode, "BINDING_TYPE");
                    type = bindingNode.getFirstChild().getNodeValue();
                } catch (NotFoundException e1) {
                    try {
                        Node oldbindingNode = findFirstNodeByName(appInfoNode, "OLDBINDING_TYPE");
                        type = oldbindingNode.getFirstChild().getNodeValue();
                        /*if (elementName != null) {
                            String baseName = GenPresentation.baseName(type).intern();
                            if (typeMapping.get(baseName) == null) {
                                // Put our baseName in there too, since
                                // we might look for it later by that name.
                                //System.out.println("Putting in "+baseName+" as "+type);
                                typeMapping.put(baseName, type);
                            }
                        }*/
                    } catch (NotFoundException e2) {
                        Node beanNode = findFirstNodeByName(appInfoNode, "BEAN_TYPE");
                        type = beanNode.getFirstChild().getNodeValue();
                    }
                }
            } catch (NotFoundException e) {
                //e.printStackTrace();
                type = "void";   // NOI18N
            }
            NodeList nodes = el.getChildNodes();
            for (int i = 0; i < nodes.getLength(); ++i) {
                Node node = nodes.item(i);
                if (!(node instanceof Element))
                    continue;
                Element childEl = (Element) node;
                if (childEl == annotationNode)
                    continue;
                // Insert our children's types into our mapping.
                getType(childEl, typeMapping, elementTypeMapping);
            }
        } else if ((xsdNamespace+":sequence").equals(el.getNodeName())) {
            NodeList nodes = el.getChildNodes();
            for (int i = 0; i < nodes.getLength(); ++i) {
                Node node = nodes.item(i);
                if (!(node instanceof Element))
                    continue;
                Element childEl = (Element) node;
                // Insert our children's types into our mapping.
                getType(childEl, typeMapping, elementTypeMapping);
            }
        } else if ((xsdNamespace+":import").equals(el.getNodeName())) {
            // Nothing for us to do here....
        } else {
            //System.out.println("Unfamiliar XML Schema element: "+el.getNodeName());	// NOI18N
            return null;
        }
        if (type == null)
            type = "void";
        //System.out.println("Found type: elementName="+elementName+" type="+type);
        type = type.intern();
        if (elementName != null) {
            typeMapping.put(elementName.intern(), type);
        }
        elementTypeMapping.put(el, type);
        return type;
    }
    
    /**
     * Return a Collection of all child Elements
     */
    public static Collection getChildElements(Element parent) {
        Collection result = new LinkedList();
        NodeList children = parent.getChildNodes();
        for (int i = 0; i < children.getLength(); ++i) {
            Node child = children.item(i);
            if (!(child instanceof Element))
                continue;
            result.add(child);
        }
        return result;
    }
    
    public static Element findFirstNode(Element node) {
        NodeList nodes = node.getChildNodes();
        for (int i = 0; i < nodes.getLength(); ++i) {
            Node n = nodes.item(i);
            if (n instanceof Element)
                return (Element) n;
        }
        return null;
    }
    
    public static Element findFirstNodeByName(Element node, String name) throws NotFoundException {
        return findFirstNodeByName(node.getChildNodes(), name);
    }
    
    /**
     * Search for a node named @param name.  Namespace is ignored.
     */
    public static Element findFirstNodeByName(NodeList nodes, String name) throws NotFoundException {
        name = removeNamespace(name);
        for (int i = 0; i < nodes.getLength(); ++i) {
            Node n = nodes.item(i);
            String localNodeName = n.getLocalName();
            if (localNodeName == null) {
                localNodeName = removeNamespace(n.getNodeName());
            }
            if (name.equals(localNodeName))
                return (Element) n;
        }
        
        
        throw new NotFoundException(MessageFormat.format(NbBundle.getMessage(Util.class,
                "MSG_UnableToFindNode"),
                new Object[] {name}), name);
    }
    
    public static Element findFirstNodeByNames(Element node, String[] names) throws NotFoundException {
        for (int i = 0; i < names.length; ++i)
            node = findFirstNodeByName(node, names[i]);
        return node;
    }
    
    public static Element findFirstNodeByName(Document doc, String name) throws NotFoundException {
        return findFirstNodeByName(doc.getChildNodes(), name);
    }
    
    //Following methods were copied from com.sun.forte4j.webdesigner.xmlcomponent.Util
    //because the wsdl module cannot be dependent on jwd.
    
    public static Class getClass(String className) {
        Class cls;
        if (className == null)
            return null;
        if (isClassArray(className)) {
            // Recursively figure out what our type is.
            // Is this the best way for getting an Array type?
            cls = getClass(className.substring(0, className.length()-2));
            Object arrayObject = java.lang.reflect.Array.newInstance(cls, 0);
            return arrayObject.getClass();
        }
        //ClassLoader cl = Top Manager.getDefault().currentClassLoader();
        //
        // Removing Top Manager calls (see Issuezilla 31753). Replace above with:
        ClassLoader cl = ClassPath.getClassPath(null, ClassPath.EXECUTE).getClassLoader(false);
        
        // BEGIN_NOI18N
        if ("int".equals(className))
            cls = Integer.TYPE;
        else if ("long".equals(className))
            cls = Long.TYPE;
        else if ("char".equals(className))
            cls = Character.TYPE;
        else if ("short".equals(className))
            cls = Short.TYPE;
        else if ("double".equals(className))
            cls = Double.TYPE;
        else if ("float".equals(className))
            cls = Float.TYPE;
        else if ("byte".equals(className))
            cls = Byte.TYPE;
        else if ("boolean".equals(className))
            cls = Boolean.TYPE;
        else if ("void".equals(className))
            cls= Void.TYPE;
        else {
            try {
                cls = cl.loadClass(className);
            } catch (java.lang.ClassNotFoundException exc) {
               ErrorManager.getDefault().notify(exc);
                cls = null;
            }
        }
        // END_NOI18N
        return cls;
    }
    
    static public boolean isClassArray(String returnClassName) {
        return returnClassName.endsWith("[]");                                  // NOI18N
    }
    
    static public boolean isCollectionType(String bindingType) {
        boolean isCollection = false;
        Class c = getClass(bindingType);
        if(c != null){
            if (java.util.Collection.class.isAssignableFrom(c))
                isCollection = true;
        }
        
        return isCollection;
    }
    
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
     
    
    public static String upperCaseFirstChar(String inString) {
        if(null == inString) {
            throw new IllegalArgumentException("Null string passed!");
        }
        String returnString =  new String(inString);
        String firstCharacter = returnString.substring(0,1);
        returnString = firstCharacter.toUpperCase() + returnString.substring(1);
        
        return returnString;
    }
    
    public static String getMethodSignatureAsString(DataProviderMethod method) {
        StringBuffer sig = new StringBuffer();
        sig.append( method.getMethodName() );
        sig.append( "(" );
        
        // Parameters
        boolean first = true;
        
        for (DataProviderParameter param : method.getParameters()) {            
            if( first )
                first = false;
            else
                sig.append( "," );
            
            // Only want the class name part
            String paramTypeName = param.getType();
            
            sig.append( paramTypeName );
        }
        
        sig.append( ")" );
        
        return sig.toString();    
    }

    public static Method getCorrespondingJaxRpcMethod(JavaMethod modelMethod, String portName, WebServiceData wsData) {
        WebServiceDescriptor descriptor = wsData.getJaxRpcDescriptor();
        if (descriptor == null) {
            return null;
        }
        try {
            List<URL> urlList = buildClasspath(null, "libs.jaxrpc16.classpath"); // NOI18N
            for (WebServiceDescriptor.JarEntry entry : descriptor.getJars()) {
                if (entry.getType().equals(WebServiceDescriptor.JarEntry.PROXY_JAR_TYPE)) {
                    File jarFile = new File(descriptor.getXmlDescriptorFile().getParent(), entry.getName());
                    urlList.add(jarFile.toURI().toURL());
                }
            }
            
            ClassLoader urlClassLoader = new URLClassLoader(urlList.toArray(new URL[urlList.size()]));
            WsdlPort port = wsData.getWsdlService().getPortByName(portName);
            
            DesignerWebServiceExtData data =
                    (DesignerWebServiceExtData)descriptor.getConsumerData().get(DesignerWebServiceExtImpl.CONSUMER_ID);
            String beanClassName = data.getPortToProxyBeanNameMap().get(port.getName());
            
            Class beanClass = urlClassLoader.loadClass(beanClassName);
            
            Method[] methods = beanClass.getDeclaredMethods();
            List<Method> candidateMethods = new ArrayList<Method>();
            
            for (int i = 0; i < methods.length; i++) {
                if (methods[i].getName().equalsIgnoreCase(modelMethod.getName())) {
                    candidateMethods.add(methods[i]);
                }
            }
            
            if (candidateMethods.size() == 1) {
                return candidateMethods.get(0);
            }
            
            Method result = null;
            int matchingParams = -1;
            for (Method m : candidateMethods) {
                int curMatchParams = getMatchingParametersCount(m, modelMethod);
                if (curMatchParams > matchingParams) {
                    result = m;
                    matchingParams = curMatchParams;
                }else if (curMatchParams == matchingParams && m.getParameterTypes().length == modelMethod.getParametersList().size()) {
                    result = m;
                }
            }
            
            return result;
        } catch (ClassNotFoundException ex) {
            return null;
        }catch (IOException ex) {
            return null;
        }
    }
    
    private static int getMatchingParametersCount(Method method, JavaMethod model) {
        List<JavaParameter> modelParams = model.getParametersList();
        Type[] params = method.getGenericParameterTypes();
        int matching = 0;
        
        for (int i = 0; i < params.length && i < modelParams.size(); i++) {
            String paramRealName = typeToString(params[i]);
            JavaParameter jParam = modelParams.get(i);
            
            if (!jParam.isHolder() && jParam.getType().getRealName().startsWith(paramRealName)) {
                matching += 1;
            }
        }
        
        return matching;
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
         * Fix Bug: 5092474
         * We also need to make sure the first and last characters of the entire package name are not "."
         *
         */
        if(inPackageName.charAt(0) == '.') {
            return false;
        } else if (inPackageName.charAt(inPackageName.length()-1) == '.') {
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
     * TODO: include in JAX-RPC API
     *
     * If the parameter is a "Holder" we need the holder type and not the JavaType.  This is
     * typically the case when there is no return type and the parameter's meant to be mutable, pass-by-reference
     * type parameters.  I took the code below directly from the JAX-RPC class:
     * "org.netbeans.modules.visualweb.xml.rpc.processor.generator.StubGenerator" except that I'm not checking the Operation for an Array type.
     * - David Botterill 6/8/2004
     * @param inParameter The JavaParameter to determine the type for.
     * @return String representing the class name for the type.  A null will be returned if the correct name cannot be resolved.
     */
    public static String getParameterType(JavaParameter inParameter) {
        
        String parameterType = null;
//        ClientProcessorEnvironment env = new ClientProcessorEnvironment(new ByteArrayOutputStream(), null, null);
        
        if (inParameter.isHolder()) {
            parameterType = inParameter.getHolderName();
        } else {
            parameterType =inParameter.getType().getName();
        }
        
        return parameterType;
        
    }
    
    public static String getFileName( String path ) {
        return new File(path).getName();
    }
    
    /**
     * Creates a classpath from a set of properties from the $userdir/build.properties file
     * 
     * @param srcPath
     * @param libProperties
     * @return a URL array containing the classpath jars and directories
     * @throws java.io.IOException 
     */
    public static List<URL> buildClasspath(File srcPath, String... libProperties) throws IOException {
        // The classpath needs to be equivalent to (plus the ws client package root):
        // classpath="${java.home}/../lib/tools.jar:${libs.jaxrpc16.classpath}:${libs.jsf12-support.classpath}"
        ArrayList<URL> urls = new ArrayList<URL>();
        Properties properties = new Properties();
        properties.load(new FileInputStream(System.getProperty("netbeans.user") + "/build.properties"));
        
        if (srcPath != null) {
            urls.add(srcPath.toURI().toURL());
        }
        
        File toolsJar = new File(System.getProperty("java.home")).getParentFile();
        toolsJar = new File(toolsJar, "lib" + File.separator + "tools.jar");
        urls.add(toolsJar.toURI().toURL());
        
        String pathSeparator = System.getProperty("path.separator");
        String longCP = properties.getProperty("libs.jsf12-support.classpath");
        for (int i = 0; libProperties != null && i < libProperties.length; i++) {
            longCP = properties.getProperty(libProperties[i]) + pathSeparator + longCP;
        }
        
        StringTokenizer st = new StringTokenizer(longCP, pathSeparator);
        
        while (st.hasMoreTokens()) {
            String next = st.nextToken();
            File nextFile = new File(next);
            urls.add(nextFile.toURI().toURL());
        }
        return urls;
    }
    
    
}

// END_NOI18N
