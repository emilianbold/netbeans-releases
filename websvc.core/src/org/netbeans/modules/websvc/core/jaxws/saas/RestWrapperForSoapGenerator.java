/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.websvc.core.jaxws.saas;

import com.sun.source.tree.ClassTree;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.Modifier;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.websvc.api.support.java.SourceUtils;
import org.netbeans.modules.websvc.core.jaxws.actions.JaxWsCodeGenerator;
import org.netbeans.modules.websvc.core.jaxws.saas.Constants.HttpMethodType;
import org.netbeans.modules.websvc.jaxwsmodelapi.WSOperation;
import org.netbeans.modules.websvc.jaxwsmodelapi.WSParameter;
import org.netbeans.modules.websvc.jaxwsmodelapi.WSPort;
import org.netbeans.modules.websvc.jaxwsmodelapi.WSService;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.openide.filesystems.FileObject;



/**
 *
 * @author rico
 */
public class RestWrapperForSoapGenerator {

    private FileObject targetFile;
    private WSService service;
    private WSPort port;
    private WSOperation operation;
    private Project project;
    private Map<String, Class> primitiveTypes;
    public static final Modifier[] PUBLIC = new Modifier[]{Modifier.PUBLIC};
    public static final String GET_ANNOTATION = "GET";   //NOI18N

    public static final String INDENT = "        ";
    public static final String INDENT_2 = "             ";
    public static final String PRODUCE_MIME_ANNOTATION = "ProduceMime"; //NOI18N

    public static final String PATH_ANNOTATION = "Path"; //NOI18N

    public static final String QUERY_PARAM_ANNOTATION = "QueryParam";       //NOI18N

    public static final String DEFAULT_VALUE_ANNOTATION = "DefaultValue";       //NOI18N


    public RestWrapperForSoapGenerator(WSService service, WSPort port,
            WSOperation operation, FileObject targetFile) {
        this.service = service;
        this.port = port;
        this.operation = operation;
        this.targetFile = targetFile;
        this.project = FileOwnerQuery.getOwner(targetFile);
    }

    public Set<FileObject> generate() throws IOException {
        JavaSource targetSource = JavaSource.forFileObject(targetFile);
        final String returnType = operation.getReturnTypeName();
        CancellableTask<WorkingCopy> task = new CancellableTask<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                ClassTree javaClass = SourceUtils.getPublicTopLevelTree(workingCopy);
                ClassTree modifiedJavaClass = addGetMethod("application/xml", returnType, workingCopy, javaClass);
                workingCopy.rewrite(javaClass, modifiedJavaClass);
            }

            public void cancel() {
            }
        };
        targetSource.runModificationTask(task).commit();


        return new HashSet<FileObject>(Collections.EMPTY_LIST);
    }

    public List<WSParameter> getOutputParameters() {
        ArrayList<WSParameter> params = new ArrayList<WSParameter>();
        for (WSParameter p : operation.getParameters()) {
            if (p.isHolder()) {
                params.add(p);
            }
        }
        return params;
    }

    private ClassTree addGetMethod(String mime, String returnType, WorkingCopy copy, ClassTree tree) throws IOException {
        Modifier[] modifiers = PUBLIC;
        String variableName = "result";  //name of variable that will be returned

        String retType = returnType;
        if (retType.equals("void")) {  //if return type is void, find out if there are Holder paramters


            List<WSParameter> parms = getOutputParameters();
            for (WSParameter parm : parms) {
                if (parm.isHolder()) {//TODO pick the first one right now. 
                    //Should let user pick if there are multiple OUT parameters.

                    String holderType = parm.getTypeName();
                    int leftbracket = holderType.indexOf("<");
                    int rightbracket = holderType.lastIndexOf(">");
                    retType = holderType.substring(leftbracket + 1, rightbracket);
                    variableName = parm.getName() + ".value";
                    break;
                }
            }
        }
        String[] annotations = new String[]{
            GET_ANNOTATION,
            PRODUCE_MIME_ANNOTATION,
            PATH_ANNOTATION
        };



        Object[] annotationAttrs = new Object[]{
            null,
            "application/xml",
            operation.getName() + "/"
        };

        if (returnType == null) {
            returnType = String.class.getName();
        }

        String bodyText = getSOAPClientInvocation(retType, variableName);

        List<WSParameter> queryParams = operation.getParameters();
        String[] parameters = getGetParamNames(queryParams);
        Object[] paramTypes = getGetParamTypes(queryParams);
        String[][] paramAnnotations = getGetParamAnnotations(queryParams);
        Object[][] paramAnnotationAttrs = getGetParamAnnotationAttrs(queryParams);

        String comment = "Invokes the SOAP method " + operation.getName() + "\n";
        for (String param : parameters) {
            comment += "@param $PARAM$ resource URI parameter\n".replace("$PARAM$", param);
        }
        comment += "@return an instance of " + retType;

        return JavaSourceHelper.addMethod(copy, tree,
                modifiers, annotations, annotationAttrs,
                getMethodName(HttpMethodType.GET), retType, parameters, paramTypes,
                paramAnnotations, paramAnnotationAttrs,
                bodyText, comment);      //NOI18N

    }

    private String[] getGetParamTypes(List<WSParameter> queryParams) {
        List<String> types = new ArrayList<String>();
        for (WSParameter queryParam : queryParams) {
            if (!queryParam.isHolder()) {
                types.add(queryParam.getTypeName());
            }
        }
        return types.toArray(new String[types.size()]);
    }

    private String[] getGetParamNames(List<WSParameter> queryParams) {
        List<String> names = new ArrayList<String>();
        for (WSParameter queryParam : queryParams) {
            if (!queryParam.isHolder()) {
                names.add(queryParam.getName());
            }
        }
        return names.toArray(new String[names.size()]);
    }

    private Object generateDefaultValue(Class type) {
        if (type == Integer.class || type == Short.class || type == Long.class ||
                type == Float.class || type == Double.class) {
            try {
                return type.getConstructor(String.class).newInstance("0"); //NOI18N

            } catch (Exception ex) {
                return null;
            }
        }

        if (type == Boolean.class) {
            return Boolean.FALSE;
        }

        if (type == Character.class) {
            return new Character('\0');
        }

        return null;
    }

    private String[][] getGetParamAnnotations(List<WSParameter> queryParams) {
        ArrayList<String[]> annos = new ArrayList<String[]>();
        String[] annotations = null;
        for (WSParameter param : queryParams) {
            Class type = getType(project, param.getTypeName());
            if (generateDefaultValue(type) != null) {
                annotations = new String[]{
                            QUERY_PARAM_ANNOTATION,
                            DEFAULT_VALUE_ANNOTATION
                        };
            } else {
                annotations = new String[]{QUERY_PARAM_ANNOTATION};
            }
            annos.add(annotations);
        }

        return annos.toArray(new String[annos.size()][]);
    }

    public Class getGenericRawType(String typeName, ClassLoader loader) {
        int i = typeName.indexOf('<');
        if (i < 1) {
            return null;
        }
        String raw = typeName.substring(0, i);
        try {
            return loader.loadClass(raw);
        } catch (ClassNotFoundException ex) {
            Logger.global.log(Level.INFO, "", ex);
            return null;
        }
    }

    public Class getType(Project project, String typeName) {
        List<ClassPath> classPaths = getClassPath(project);


        for (ClassPath cp : classPaths) {
            try {
                Class ret = getPrimitiveType(typeName);
                if (ret != null) {
                    return ret;
                }
                ClassLoader cl = cp.getClassLoader(true);
                ret = getGenericRawType(typeName, cl);
                if (ret != null) {
                    return ret;
                }
                if (cl != null) {
                    return cl.loadClass(typeName);
                }
            } catch (ClassNotFoundException ex) {
                //Logger.global.log(Level.INFO, ex.getLocalizedMessage(), ex);
            }
        }
        return null;
    }

    public Class[] getInputParameterTypes() {
        ArrayList<Class> types = new ArrayList<Class>();

        for (WSParameter p : operation.getParameters()) {
            if (!p.isHolder()) {
                int repeatCount = 0;
                Class type = null;

                // This is a hack to wait for the complex type to become
                // available. We will give up after 120 seconds.
                synchronized (this) {
                    try {
                        while (repeatCount < 60) {
                            type = getType(project, p.getTypeName());

                            if (type != null) {
                                break;
                            }

                            repeatCount++;
                            this.wait(2000);
                        }
                    } catch (InterruptedException ex) {
                    }
                }

                // RESOLVE:
                // Need to fail gracefully by displaying an error dialog.
                // For now, set it to Object.class.
                if (type == null) {
                    type = Object.class;
                }

                types.add(type);
            }
        }
        return types.toArray(new Class[types.size()]);
    }

    private Object[][] getGetParamAnnotationAttrs(List<WSParameter> queryParams) {
        ArrayList<Object[]> attrs = new ArrayList<Object[]>();

        Object[] annotationAttrs = null;

        for (WSParameter param : queryParams) {
            Class type = getType(project, param.getTypeName());
            Object defaultValue = this.generateDefaultValue(type);
            if (generateDefaultValue(type) != null) {
                annotationAttrs = new Object[]{
                            param.getName(), defaultValue.toString()
                        };
            } else {
                annotationAttrs = new Object[]{param.getName()};
            }
            attrs.add(annotationAttrs);
        }

        return attrs.toArray(new Object[attrs.size()][]);
    }

    private String getMethodName(HttpMethodType methodType) {
        String methodName = camelize(operation.getName(), true);
        if (methodName.startsWith(methodType.prefix())) {
            return methodName;
        }
        return methodType.prefix() + camelize(methodName, false);
    }

    protected String getCustomMethodBody() throws IOException {
        String methodBody = INDENT;
        methodBody += JaxWsCodeGenerator.getWSInvocationCode(targetFile, false, service, port, operation);

        return methodBody;
    }

    private String getSOAPClientInvocation(String typeName, String variableName) throws IOException {
        String code = "{\n";
        code += INDENT + "try {\n";
        code += getCustomMethodBody() + "\n";
        if (!typeName.equals(Constants.VOID)) {
            code += "return " + variableName + ";\n";
        }
        code += INDENT + "} catch (Exception ex) {\n";
        code += INDENT_2 + "ex.printStackTrace();\n";
        code += INDENT + "}\n";
        if (!typeName.equals(Constants.VOID)) {
            code += "return null;\n";  //TODO: will there be a case for primitive return types?

        }
        code += "}\n";
        return code;

    }

    public Class getPrimitiveType(String typeName) {
        if (primitiveTypes == null) {
            primitiveTypes = new HashMap<String, Class>();
            primitiveTypes.put("int", Integer.class);
            primitiveTypes.put("int[]", Integer[].class);
            primitiveTypes.put("boolean", Boolean.class);
            primitiveTypes.put("boolean[]", Boolean[].class);
            primitiveTypes.put("byte", Byte.class);
            primitiveTypes.put("byte[]", Byte[].class);
            primitiveTypes.put("char", Character.class);
            primitiveTypes.put("char[]", Character[].class);
            primitiveTypes.put("double", Double.class);
            primitiveTypes.put("double[]", Double[].class);
            primitiveTypes.put("float", Float.class);
            primitiveTypes.put("float[]", Float[].class);
            primitiveTypes.put("long", Long.class);
            primitiveTypes.put("long[]", Long[].class);
            primitiveTypes.put("short", Short.class);
            primitiveTypes.put("short[]", Short[].class);
        }
        return primitiveTypes.get(typeName);
    }

    public static List<ClassPath> getClassPath(Project project) {
        List<ClassPath> paths = new ArrayList<ClassPath>();
        List<SourceGroup> groups = new ArrayList<SourceGroup>();
        groups.addAll(Arrays.asList(ProjectUtils.getSources(project).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA)));
        ClassPathProvider cpp = project.getLookup().lookup(ClassPathProvider.class);
        for (SourceGroup group : groups) {
            ClassPath cp = cpp.findClassPath(group.getRootFolder(), ClassPath.COMPILE);
            if (cp != null) {
                paths.add(cp);
            }
            cp = cpp.findClassPath(group.getRootFolder(), ClassPath.SOURCE);
            if (cp != null) {
                paths.add(cp);
            }
        }
        return paths;
    }

    public String camelize(String word, boolean flag) {
        if (word.length() == 0) {
            return word;
        }
        StringBuffer sb = new StringBuffer(word.length());
        if (flag) {
            sb.append(Character.toLowerCase(word.charAt(0)));
        } else {
            sb.append(Character.toUpperCase(word.charAt(0)));
        }
        boolean capitalize = false;
        for (int i = 1; i < word.length(); i++) {
            char ch = word.charAt(i);
            if (capitalize) {
                sb.append(Character.toUpperCase(ch));
                capitalize = false;
            } else if (ch == '_') {
                capitalize = true;
            } else if (ch == '/') {
                capitalize = true;
                sb.append('.');
            } else {
                sb.append(ch);
            }
        }
        return sb.toString();

    }
}
