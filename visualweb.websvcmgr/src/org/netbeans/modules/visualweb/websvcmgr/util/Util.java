/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.visualweb.websvcmgr.util;


import com.sun.tools.ws.processor.model.java.JavaMethod;
import com.sun.tools.ws.processor.model.java.JavaParameter;
import com.sun.tools.ws.processor.model.java.JavaType;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.modules.visualweb.api.designerapi.DesignerServiceHack;
import org.netbeans.modules.visualweb.websvcmgr.codegen.DataProviderMethod;
import org.netbeans.modules.visualweb.websvcmgr.codegen.DataProviderParameter;

// BEGIN_NOI18N
import org.netbeans.modules.visualweb.websvcmgr.consumer.DesignerWebServiceExtData;
import org.netbeans.modules.visualweb.websvcmgr.consumer.DesignerWebServiceExtImpl;
import org.netbeans.modules.websvc.jaxwsmodelapi.WSPort;
import org.netbeans.modules.websvc.manager.api.WebServiceDescriptor;
import org.netbeans.modules.websvc.manager.model.WebServiceData;
import org.netbeans.modules.websvc.manager.util.ManagerUtil;
import org.openide.filesystems.FileObject;
import org.openide.modules.InstalledFileLocator;
/**
 * General utility methods for WSDL documents.
 */
public class Util {
    public static final String dataproviderJar = InstalledFileLocator.getDefault().locate("modules/ext/dataprovider.jar", null, false ).getAbsolutePath(); // NOI18N
    public static final String designTimeJar = InstalledFileLocator.getDefault().locate( "modules/org-netbeans-modules-visualweb-designtime.jar", null, false).getAbsolutePath(); // NOI18N
    
    public static final int BUFFER_SIZE = 4096;
    public static final String xsdNamespace = "xsd";
    final static public String WSDL_FILE_EXTENSION = "wsdl";

    public static boolean isJAXRPCAvailable() {
        return getWebServiceSupportLibDef(false) != null;
    }

    /**
     * @return The library definition containing the web service support jar files, null if it does not exist
     */
    public static Library getWebServiceSupportLibDef(boolean isJ2EE_15) {
        String libraryName = (isJ2EE_15) ? "jaxws21" : "jaxrpc16";
        Library libDef = LibraryManager.getDefault().getLibrary(libraryName);

        return libDef;
    }

    public static Method getPropertyGetter(String type, String propName, ClassLoader loader) {
        try {
            Class typeClass = Class.forName(type, true, loader);
            
            char[] name = propName.toCharArray();
            String propCaps = null;
            
            Method method = null;
            
            for (int i = 0; i < propName.length() && method == null; i++ ) {
                name[i] = Character.toUpperCase(name[i]);
                propCaps = new String(name);
                try {
                    method = typeClass.getMethod("get" + propCaps, new Class[0]); // NOI18N
                } catch (NoSuchMethodException ex) {
                    try {
                        method = typeClass.getMethod("is" + propCaps, new Class[0]); // NOI18N
                    } catch (NoSuchMethodException nsme) {
                        continue;
                    }
                }
            }
            
            return method;
        }catch (Exception ex) {
            return null;
        }
    }
    
    private static final String[] PRIMITIVE_WRAPPER_CLASSES = 
    { "java.lang.Boolean", 
      "java.lang.Byte", 
      "java.lang.Double", 
      "java.lang.Float", 
      "java.lang.Integer", 
      "java.lang.Long", 
      "java.lang.Short", 
      "java.lang.Character", 
      "java.lang.String" };
    
    private static final String[] PRIMITIVE_TYPES = 
    { "boolean",
      "byte",
      "double",
      "float",
      "int",
      "long",
      "short",
      "char" };
    
    public static boolean isPrimitiveType(String typeName) {
        for (int i = 0; i < PRIMITIVE_WRAPPER_CLASSES.length; i++) {
            if (PRIMITIVE_WRAPPER_CLASSES[i].equals(typeName)) {
                return true;
            }
        }
        
        return isJavaPrimitive(typeName);
    }
    
    public static boolean isJavaPrimitive(String typeName) {
        for (int i = 0; i < PRIMITIVE_TYPES.length; i++) {
            if (PRIMITIVE_TYPES[i].equals(typeName)) {
                return true;
            }
        }
        
        return false;
    }
    
    public static String getWrapperForPrimitive(String javaPrimitive) {
        for (int i = 0; i < PRIMITIVE_TYPES.length; i++) {
            if (PRIMITIVE_TYPES[i].equals(javaPrimitive)) {
                return PRIMITIVE_WRAPPER_CLASSES[i];
            }     
        }
        
        return null;
    }

    
    
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
            List<URL> urlList = ManagerUtil.buildClasspath(null, false);
            for (WebServiceDescriptor.JarEntry entry : descriptor.getJars()) {
                if (entry.getType().equals(WebServiceDescriptor.JarEntry.PROXY_JAR_TYPE)) {
                    File jarFile = new File(descriptor.getXmlDescriptorFile().getParent(), entry.getName());
                    urlList.add(jarFile.toURI().toURL());
                }
            }
            
            ClassLoader urlClassLoader = new URLClassLoader(urlList.toArray(new URL[urlList.size()]), Util.class.getClassLoader());
            WSPort port = wsData.getWsdlService().getPortByName(portName);
            
            DesignerWebServiceExtData data =
                    (DesignerWebServiceExtData)descriptor.getConsumerData().get(DesignerWebServiceExtImpl.CONSUMER_ID);
            String beanClassName = data.getPortToProxyBeanNameMap().get(port.getName());
            
            Class beanClass = urlClassLoader.loadClass(beanClassName);
            
            Method[] methods = beanClass.getDeclaredMethods();
            List<Method> candidateMethods = new ArrayList<Method>();
            
            for (int i = 0; i < methods.length; i++) {
                if (isSimilarMethod(methods[i].getName(), modelMethod.getName())) {
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
    
    private static boolean isSimilarMethod(String jaxRpcName, String modelName) {
        if (jaxRpcName.equalsIgnoreCase(modelName)) {
            return true;
        }else if (jaxRpcName.replace("_", "").equalsIgnoreCase(modelName)) { // NOI18N
            return true;
        }
        
        return false;
    }
    
    private static int getMatchingParametersCount(Method method, JavaMethod model) {
        List<JavaParameter> modelParams = model.getParametersList();
        Type[] params = method.getGenericParameterTypes();
        int matching = 0;
        
        for (int i = 0; i < params.length && i < modelParams.size(); i++) {
            String paramRealName = ManagerUtil.typeToString(params[i]);
            JavaParameter jParam = modelParams.get(i);
            
            if (!jParam.isHolder() && jParam.getType().getRealName().startsWith(paramRealName)) {
                matching += 1;
            }
        }
        
        return matching;
    }
    
        }
// END_NOI18N
