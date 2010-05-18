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
package org.netbeans.modules.soa.pojo.util;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.BlockTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.StringTokenizer;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.swing.text.JTextComponent;
import javax.xml.namespace.QName;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.project.classpath.ProjectClassPathModifier;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.modules.soa.pojo.model.api.JavaModel;
import org.netbeans.modules.soa.pojo.model.impl.JavaModelImpl;
import org.netbeans.modules.soa.pojo.wizards.OperationMethodChooserPanel;
import org.netbeans.spi.project.support.GenericSources;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author Sreeni Genipudi
 */
public class GeneratorUtil  {

    public static final String POJO_OPERATION_PATTERN="MessageExchangePattern"; //NOI18N
    public static final String POJO_INPUT_TYPE="POJO_INPUT";//NOI18N
    public static final String POJO_OUTPUT_TYPE="POJO_OUTPUT";//NOI18N
    public static final String POJO_ENDPOINT_NAME="POJO_END_POINTNAME";//NOI18N
    public static final String POJO_OPERATION_METHOD_NAME="POJO_OP_METHODNAME";//NOI18N
    public static final String POJO_INTERFACE_NAME="POJO_INTF_NAME";    //NOI18N
    public static final String POJO_INTERFACE_NS="POJO_INTF_NS";    //NOI18N    
    public static final String POJO_SERVICE_NS="POJO_SERVICE_NS";    //NOI18N    
    public static final String POJO_SERVICE_NAME="POJO_SERVICE_NAME";    //NOI18N    

    public static final String POJO_ADVANCED_SAVED="POJO_ADVANCED_SAVED";  //NOI18N    
    public static final String POJO_USE_DEFAULT="POJO_USE_DEFAULT";  //NOI18N  
    public static final String POJO_OUTMSG_TYPE_NAME="POJO_OUTMSG_TYPE_NAME";    //NOI18N    
    public static final String POJO_OUTMSG_TYPE_NS="POJO_OUTMSG_TYPE_NS";    //NOI18N    
    public static final String POJO_METHOD_NAME="receive";//NOI18N
    public static final String POJO_SERVICE_SUFFIX="Service";//NOI18N
    public static final String POJO_INTERFACE_SUFFIX="Interface";//NOI18N
    public static final String POJO_IN_MESSAGE_SUFFIX="OperationRequest";//NOI18N    
    public static final String POJO_OUT_MESSAGE_SUFFIX="OperationResponse";//NOI18N
    public static final String PROJECT_INSTANCE="ProjectInstance";//NOI18N
    public static final String POJO_CLASS_NAME="POJOCLASSNAME";//NOI18N    
    public static final String POJO_PACKAGE_NAME="POJOPACKAGENAME";//NOI18N  
    public static final String POJO_PROJECT_NAME="POJOPROJECTNAME";//NOI18N     
    public static final String POJO_FILE_LOCATION="POJOFILELOCATION";//NOI18N         
    public static final String GENERATE_POJO_ANNOTATIONS="POJOANNOTATIONS";//NOI18N
    public static final String GENERATE_OPERATION_ANNOTATIONS="OPERATIONANNOTATIONS";//NOI18N
    public static final String GENERATE_VOID="void";//NOI18N   
    public static final String VOID_CLASS_CONST="Void";//NOI18N       
    public static final String POJO_VARIABLE_NAME_PREFIX="var";//NOI18N      
    public static final String POJO_QUAL_OPERATION_ANNOTATION_CLASS="org.glassfish.openesb.pojose.api.annotation.Operation";//NOI18N
    public static final String POJO_QUAL_CLASS_ANNOTATION="org.glassfish.openesb.pojose.api.annotation.POJO";//NOI18N
    public static final String PROVIDER_QUAL_CLASS_ANNOTATION="org.glassfish.openesb.pojose.api.annotation.Provider";//NOI18N

    //public static final String POJO_CTX_QUAL_CLASS_ANNOTATION="org.glassfish.openesb.pojose.api.res.POJOContext";//NOI18N
    public static final String CTX_QUAL_CLASS_ANNOTATION="org.glassfish.openesb.pojose.api.res.Context";//NOI18N
    //public static final String POJO_RSRC_QUAL_CLASS_ANNOTATION="org.glassfish.openesb.pojose.api.annotation.POJOResource";//NOI18N
    public static final String RSRC_QUAL_CLASS_ANNOTATION="org.glassfish.openesb.pojose.api.annotation.Resource";//NOI18N
    //public static final String POJO_EP_QUAL_CLASS_ANNOTATION="org.glassfish.openesb.pojose.api.annotation.Endpoint";//NOI18N
    public static final String CONS_EP_QUAL_CLASS_ANNOTATION="org.glassfish.openesb.pojose.api.annotation.ConsumerEndpoint";//NOI18N
    //public static final String POJO_SEP_QUAL_CLASS_ANNOTATION="javax.jbi.servicedesc.ServiceEndpoint";//NOI18N
    public static final String CONS_QUAL_CLASS_ANNOTATION="org.glassfish.openesb.pojose.api.Consumer";//NOI18N

    //public static final String POJO_CTX_ANNOTATION="POJOContext";//NOI18N
    public static final String CTX_ANNOTATION="Context";//NOI18N
    //public static final String POJO_RSRC_ANNOTATION="POJOResource";//NOI18N
    public static final String RSRC_ANNOTATION="Resource";//NOI18N
    //public static final String POJO_EP_ANNOTATION="Endpoint";//NOI18N
    public static final String CONS_EP_ANNOTATION="ConsumerEndpoint";//NOI18N
    //public static final String POJO_SEP_ANNOTATION="ServiceEndpoint";//NOI18N
    public static final String CONSUMER_ANNOTATION="Consumer";//NOI18N
    
    
    public static final String POJO_OPERATION_ANNOTATION="Operation";//NOI18N
    public static final String POJO_CLASS_ANNOTATION="POJO";//NOI18N
    public static final String PROVIDER_CLASS_ANNOTATION="Provider";//NOI18N
    public static final String POJO_JAVA_SOURCE_INSTANCE="javaSource";//NOI18N      
    public static final String POJO_DEFAULT_RECEIVE_OPERATION="receive";//NOI18N      
    public static final String POJO_GET_METHOD_LIST="POJOGETMETHODLIST";//NOI18N
    public static final String POJO_RESOURCE_ANNOTATION="org.glassfish.openesb.pojose.api.annotation.POJOResource";//NOI18N
    public static final String POJO_SERVICE_ENDPOINT_ANNOTATION="org.glassfish.openesb.pojose.api.annotation.Endpoint";//NOI18N
    
    public static final String VOID_CONST="void";  //NOI18N    
    public static final String NAME_CONST="name";  //NOI18N    
    public static final String INTF_NAME_CONST="interfaceName";  //NOI18N    
    public static final String INTF_NS_CONST="interfaceNS";  //NOI18N    
    
    
    public static final String SERVICE_NS_CONST="serviceNS";  //NOI18N    
    public static final String SERVICE_CONST="serviceName";  //NOI18N    
    public static final String SERVICE_QNAME="serviceQN";  //NOI18N    
    public static final String INTF_QNAME_CONST="interfaceQN";  //NOI18N    
    public static final String IN_MSGTYPE_QNAME_CONST="inMessageTypeQN";//NOI18N    
    public static final String OPN_QNAME_CONST="operationQN";//NOI18N    
    public static final String OUT_MSGTYPE_QNAME_CONST="outMessageTypeQN";//NOI18N    
    
    
    public static final String OUT_MSGTYPE_CONST="outMessageType";//NOI18N    
    public static final String IN_MSGTYPE_CONST="inMessageType";//NOI18N    
    public static final String OUT_MSGTYPE_NS_CONST="outMessageTypeNS";//NOI18N  
    public static final String OPERATION_CONST="operationName";//NOI18N  
    public static final String OPERATION_NS_CONST="operationNameNS";//NOI18N      
    
    public static final char ARRAY_MARKER_START='8';//NOI18N   
    public static final char ARRAY_MARKER_END='9';//NOI18N   
    public static final char ARRAY_MARKER_ACTUAL_START='[';//NOI18N   
    public static final char ARRAY_MARKER_ACTUAL_END=']';//NOI18N
    public static final String POJO_GENERATE_WSDL ="POJO_GENERATE_WSDL";//NOI18N
    public static final String WSDL_EXT =".wsdl";//NOI18N
    public static final String WSDL_ALREADY_FOUND ="WSDL_ALREADY_FOUND";//NOI18N
    private static final String WSDL_TEMPLATE_FILE="org/netbeans/modules/soa/pojo/resources/POJOProviderWSDL.template";//NOI18N
    public static String POJO_TEMP_FOLDER = "POJO_TEMP_FOLDER";//NOI18N
    public static String POJO_REPLY_METHOD_NAME = "onReply";//NOI18N
    public static String POJO_DONE_METHOD_NAME = "onDone";//NOI18N
    public static String POJO_BC_WSDL_LOC = "POJO_BC_WSDL_LOC";//NOI18N
    public static String POJO_WSDL_LOC_PREFIX ="http://";//NOI18N
    public static Object POJO_USE_WSDL="POJO_USE_WSDL";//NOI18N
    public static Object POJO_WSDL_FILE_LOC="POJO_WSDL_FILE_LOC";//NOI18N
    public static Object POJO_WSDL_URL_LOC="POJO_WSDL_URL_LOC";//NOI18N
    public static Object POJO_WSDL_OPERATION_NAME="POJO_WSDL_OPERATION_NAME";//NOI18N
    public static Object POJO_WSDL_PORTTYPE_NAME="POJO_WSDL_PORTTYPE_NAME";//NOI18N
    public static Object POJO_WSDL_INTF_LIST="POJO_WSDL_INTF_LIST";//NOI18N
    public static Object POJO_WSDL_OPN_LIST="POJO_WSDL_OPN_LIST";//NOI18N
    public static String PROJECT_PROPERTY_PACKAGE_ALL="pojo.packageall";//NOI18N
    public static String CONST_TRUE="true";//NOI18N
    public static String CONST_FALSE="false";//NOI18N
    public static String POJO_DEST_FOLDER="POJO_DEST_FOLDER";//NOI18N
    public static String POJO_DEST_NAME="POJO_DEST_NAME";//NOI18N
    public static String CURSOR_LOC="CURSOR_LOC";//NOI18N
    public static String POJO_JAVAC_MODEL="POJO_JAVAC_MODEL";//NOI18N
    public static String POJO_SELECTED_METHOD="POJO_SELECTED_METHOD";//NOI18N

    public static final POJOSupportedDataTypes[] POJO_IN_TYPES = new POJOSupportedDataTypes[]{
      POJOSupportedDataTypes.String,
      POJOSupportedDataTypes.Source,
      POJOSupportedDataTypes.NormalizedMessage,
      POJOSupportedDataTypes.Node,
      POJOSupportedDataTypes.Document,
      POJOSupportedDataTypes.MessageExchange
    };

    public static final POJOSupportedDataTypes[] POJO_OUT_TYPES = new POJOSupportedDataTypes[]{
      POJOSupportedDataTypes.String,
      POJOSupportedDataTypes.Source,
      POJOSupportedDataTypes.NormalizedMessage,
      POJOSupportedDataTypes.Node,
      POJOSupportedDataTypes.Document,
      POJOSupportedDataTypes.Void
    };

    public static final String POJO_CONSUMER_INPUT_TYPE="POJO_CONSUMER_INPUT_TYPE";//NOI18N
    public static final String POJO_CONSUMER_ENDPOINT_NAME="POJO_CONSUMER_END_POINTNAME";//NOI18N
    public static final String POJO_CONSUMER_INTERFACE_NAME="POJO_CONSUMER_INTERFACE_NAME";    //NOI18N
    public static final String POJO_CONSUMER_INTERFACE_NS="POJO_CONSUMER_INTERFACE_NS";    //NOI18N    
    public static final String POJO_CONSUMER_SERVICE_NS="POJO_CONSUMER_SERVICE_NS";    //NOI18N    
    public static final String POJO_CONSUMER_SERVICE_NAME="POJO_CONSUMER_SERVICE_NAME";    //NOI18N
    public static String POJO_CONSUMER_INVOKE_TYPE= "POJO_CONSUMER_INVOKE_TYPE";//NOI18N
    public static String POJO_CONSUMER_INPUT_MESSAGE_TYPE="POJO_CONSUMER_INPUT_MESSAGE_TYPE";//NOI18N
    public static String POJO_CONSUMER_OPERATION_NAME="POJO_CONSUMER_OPERATION_NAME";//NOI18N
    public static String POJO_CONSUMER_INPUT_MESSAGE_TYPE_NS="POJO_CONSUMER_INPUT_MESSAGE_TYPE_NS";//NOI18N
    public static String POJO_CONSUMER_REPLY_METHOD_NAME="POJO_CONSUMER_REPLY_METHOD_NAME";//NOI18N
    public static String POJO_CONSUMER_DONE_METHOD_NAME="POJO_CONSUMER_DONE_METHOD_NAME";//NOI18N
    public static String HIDE_ADVANCED="HIDE_ADVANCED";//NOI18N
    public static String POJO_CONSUMER_OUTPUT_TYPE="POJO_CONSUMER_OUTPUT_TYPE";//NOI18N
    
    public static String SYNCH_CONST="Synchronous";//NOI18N
    public static String ASYNCH_CONST="ASynchronous";//NOI18N
    public static String POJO_CONSUMER_DROP ="POJO_CONSUMER_DROP";//NOI18N
    public static String POJO_CTX_VARIABLE="jbiCtx";//NOI18N
    
    public static String findNewFile(File newFileDir, String pojoClassName) {
        File checkFile = new File(newFileDir, pojoClassName);
        if ( !checkFile.exists()) {
            return  pojoClassName;
        } else {
            final String pojoClassNameFinal = pojoClassName;
            File[] fileList=newFileDir.listFiles(new FilenameFilter(){

                public boolean accept(File dir, String name) {
                    if ( name.endsWith(".java") && name.startsWith(pojoClassNameFinal)) {
                        return true;
                    }
                    return false;
                }
            });
            List<String> fileNames = new ArrayList<String>();
            for ( File file: fileList) {
                fileNames.add(file.getParent());
            }

            if ( fileNames.size() > 0) {
                Collections.sort(fileNames);
                pojoClassName = fileNames.get(fileNames.size()-1);
                int javaSuffInd = pojoClassName.lastIndexOf(".java");
                pojoClassName = pojoClassName.substring(0, javaSuffInd);
                do {
                    int pojoClassLength = pojoClassName.length();
                    char c =pojoClassName.charAt(pojoClassLength-1);
                    c =  (char) (((int) c) + 1);

                    pojoClassName = pojoClassName.substring(0, pojoClassLength-2) + c;
                } while( fileNames.contains(pojoClassName));

            } else {
                pojoClassName = pojoClassName+System.currentTimeMillis();
            }
            return pojoClassName;            
        }
    }

    public static String generateWSDL(Project proj, FileObject folder, Map map, boolean inOut) {
        InputStream is = GeneratorUtil.class.getClassLoader().getResourceAsStream(WSDL_TEMPLATE_FILE);
        String className = (String) map.get(GeneratorUtil.POJO_CLASS_NAME);
        File outputFile = new File(FileUtil.toFile(folder),className+WSDL_EXT);
        String stringData = createString(is);

        stringData = stringData.replaceAll("\\$\\{portTypeNamespace\\}",(String) map.get(GeneratorUtil.POJO_INTERFACE_NS));
        stringData = stringData.replaceAll("\\$\\{pojoporttype\\}",(String) map.get(GeneratorUtil.POJO_INTERFACE_NAME));
        if (inOut) {
            stringData = stringData.replaceAll("\\$\\{pojowdloutinput\\}", "<output name=\"output1\" message=\"tns:"+  map.get(GeneratorUtil.POJO_OUTMSG_TYPE_NAME)+"\"/>");
        } else {
            stringData = stringData.replaceAll("\\$\\{pojowdloutinput\\}", "");
        }
        stringData = stringData.replaceAll("\\$\\{pojoOutputResponse\\}",(String) map.get(GeneratorUtil.POJO_OUTMSG_TYPE_NAME));
        stringData = stringData.replaceAll("\\$\\{pojoendpointname\\}",(String) map.get(GeneratorUtil.POJO_ENDPOINT_NAME));        
        createFile(stringData, outputFile);
        return FileUtil.getRelativePath(proj.getProjectDirectory(), FileUtil.toFileObject(outputFile));
    }
    
    public static boolean createFile(String data, File targetFile) {
        PrintWriter pw = null;
        boolean bReturn =true;
        try {
            pw = new PrintWriter(targetFile);
            pw.write(data);
            pw.flush();

        } catch (Exception ex) {
           ex.printStackTrace();
           bReturn = false;
        } finally {
            pw.close();
            pw = null;
        }
        return bReturn;
    }    
    //"89";
    
    public static boolean isJavaIdentifier(String s) {
        if (s.length() == 0 || !Character.isJavaIdentifierStart(s.charAt(0))) {
            return false;
        }
        for (int i = 1; i < s.length(); i++) {
            if (!Character.isJavaIdentifierPart(s.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public static boolean isDuplicateLocalVariable(TreePath methodTree, String variableName) {
        final List<Name> localVariables = new ArrayList<Name> ();
        (new TreePathScanner<Void, Void> () {

            private int blockLevel = 0;

            @Override
            public Void visitVariable(VariableTree t, Void v) {
                super.visitVariable(t, v);
                if (blockLevel <= 1) {
                    localVariables.add(t.getName());
                }
                return null;
            }

            @Override
            public Void visitBlock(BlockTree t, Void v) {
                blockLevel++;
                super.visitBlock(t, v);
                blockLevel--;
                return null;
            }

        }).scan(methodTree, null);

        for (Name name : localVariables) {
            if (name.contentEquals(variableName)) {
                return true;
            }
        }
        return false;
    }

    public static List<VariableTree> getMethodLevelVariables(TreePath methodTree) {
        final List<VariableTree> variables = new ArrayList<VariableTree> ();
        (new TreePathScanner<Void, Void> () {

            @Override
            public Void visitVariable(VariableTree t, Void v) {
                super.visitVariable(t, v);
                variables.add(t);
                return null;
            }

        }).scan(methodTree, null);

        return variables;
    }

    public static AnnotationMirror findAnnotation(Element element, String annotationClass) {
        for (AnnotationMirror ann : element.getAnnotationMirrors()) {
            if (annotationClass.equals(ann.getAnnotationType().toString())) {
                return ann;
            }
        }

        return null;
    }

    public static AnnotationValue getAnnotationAttrValue(AnnotationMirror ann, String attrName) {
        if (ann != null) {
            for (ExecutableElement attr : ann.getElementValues().keySet()) {
                if (attrName.equals(attr.getSimpleName().toString())) {
                    return ann.getElementValues().get(attr);
                }
            }
        }

        return null;
    }

    // ********************* RetoucheUtil *********************************

    public static Tree createType(TreeMaker make, WorkingCopy workingCopy, String typeName) throws Exception {
        TypeKind primitiveTypeKind = null;
        if ("void".equals(typeName)) { // NOI18N
            primitiveTypeKind = TypeKind.VOID;
        } else if ("boolean".equals(typeName)) { // NOI18N
            primitiveTypeKind = TypeKind.BOOLEAN;
        } else if ("byte".equals(typeName)) { // NOI18N
            primitiveTypeKind = TypeKind.BYTE;
        } else if ("short".equals(typeName)) { // NOI18N
            primitiveTypeKind = TypeKind.SHORT;
        } else if ("int".equals(typeName)) { // NOI18N
            primitiveTypeKind = TypeKind.INT;
        } else if ("long".equals(typeName)) { // NOI18N
            primitiveTypeKind = TypeKind.LONG;
        } else if ("char".equals(typeName)) { // NOI18N
            primitiveTypeKind = TypeKind.CHAR;
        } else if ("float".equals(typeName)) { // NOI18N
            primitiveTypeKind = TypeKind.FLOAT;
        } else if ("double".equals(typeName)) { // NOI18N
            primitiveTypeKind = TypeKind.DOUBLE;
        }
        if (primitiveTypeKind != null) {
            return make.PrimitiveType(primitiveTypeKind);
        }
        Tree typeTree = createQualIdent(make, workingCopy, typeName);

        return typeTree;
    }

    public static ModifiersTree createModifiers(TreeMaker make, Modifier modifier,
            AnnotationTree annotation) {
        return make.Modifiers(EnumSet.of(modifier), Collections.<AnnotationTree>singletonList(annotation));
    }

    public static AnnotationTree createAnnotation(TreeMaker make, WorkingCopy workingCopy,
            String annotationType, List<? extends ExpressionTree> arguments) throws Exception {
        ExpressionTree annotationTypeTree = createQualIdent(make, workingCopy, annotationType);
        return make.Annotation(annotationTypeTree, arguments);
    }

    public static ExpressionTree createAnnotationArgument(TreeMaker make, String argumentName,
            Object argumentValue) {
        ExpressionTree argumentValueTree = make.Literal(argumentValue);
        return make.Assignment(make.Identifier(argumentName), argumentValueTree);
    }

    public static VariableTree createField(TreeMaker make, WorkingCopy workingCopy,
            ModifiersTree modifiersTree, String fieldName,
            String fieldType, ExpressionTree expressionTree) throws Exception {
        return make.Variable(
                modifiersTree,
                fieldName,
                createType(make, workingCopy, fieldType),
                expressionTree);
    }

    public static IdentifierTree createQualIdent(TreeMaker make, WorkingCopy workingCopy,
            String typeName) throws Exception {
        //TypeElement typeElement = workingCopy.getElements().getTypeElement(typeName);
        //if (typeElement == null) {
        //    throw new Exception("Cannot resolve type: " + typeName);
        //}
        return make.Identifier(typeName);
    }

    public static ExecutableElement findMethodByName(WorkingCopy workingCopy, String methodName) {
        List<? extends TypeElement> elements = workingCopy.getTopLevelElements();
        if (elements.size() == 0) {
            return null;
        }
        TypeElement topElement = elements.get(0);
        for (Element element : topElement.getEnclosedElements()) {
            if (element.getKind() == ElementKind.METHOD) {
                ExecutableElement method = (ExecutableElement) element;
                if (method.getSimpleName().toString().equals(methodName)) {
                    return method;
                }
            }
        }

        return null;
    }

    public static void addLibrary(String libName, Project prj) {
        Library lib = LibraryManager.getDefault().getLibrary(
                libName);
        Sources srcs = getSources(prj);
        if (srcs != null) {
            SourceGroup[] srg = srcs.getSourceGroups(
                    JavaProjectConstants.SOURCES_TYPE_JAVA);
            if ((srg != null) && (srg.length > 0)) {
                try {
                    ProjectClassPathModifier.addLibraries(
                            new Library[]{lib}, srg[0].getRootFolder(),
                            ClassPath.COMPILE);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
    }

    public static Sources getSources(Project p) {
        Sources s = p.getLookup().lookup(Sources.class);
        if (s != null) {
            return s;
        } else {
            return GenericSources.genericOnly(p);
        }
    }
    
    public static FileObject createFile(InputStream is, File targetFile) {
        FileWriter fw = null;
        FileObject foReturn =null;
        try {
            fw = new FileWriter(targetFile);
            BufferedInputStream bid = new BufferedInputStream(is);
            int c = -1;
            while ((c = bid.read()) != -1) {
                fw.write((char)c);
            }
            fw.flush();
            
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        } finally {
            try {
                fw.close();
                foReturn = FileUtil.toFileObject(targetFile);
            } catch (IOException ee) {
                
            }
            fw = null;
        }
        return foReturn;
    }
    
       public static JavaModel createJavacTreeModel(JTextComponent target) {
        try {
            final JavaModelImpl model = new JavaModelImpl();
            JavaSource javaSource = JavaSource.forDocument(target.getDocument());
            javaSource.runUserActionTask(new CancellableTask<CompilationController> () {
                public void cancel() {}
                public void run(CompilationController cc) throws Exception {
                    cc.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                    model.setCompilationController(cc);
                    model.scan(cc.getCompilationUnit(), null);
                }
            }, true);

            return model;
        } catch (IOException ioe) {
            return null;
        }
    }
    /*
       public static String returnType(String outputType) {       
        return returnType(outputType, false);
    }
       
    public static String returnType(String outputType, boolean inType) {
        if ( outputType.equals(VOID_CONST)) {
            if ( inType) {
                return "";
            } else {
                return GENERATE_VOID;
            }
        }
        if ( outputType.indexOf('_') != -1) { //NOI18N
            outputType = outputType.replace('_', '.');//NOI18N
        } else if ( outputType.indexOf(GeneratorUtil.ARRAY_MARKER_START) != -1) {//NOI18N
            outputType = outputType.replace(GeneratorUtil.ARRAY_MARKER_START, GeneratorUtil.ARRAY_MARKER_ACTUAL_START);
            outputType = outputType.replace(GeneratorUtil.ARRAY_MARKER_END, GeneratorUtil.ARRAY_MARKER_ACTUAL_END);
        }
        return outputType;
    } */          
    
    public static void addVariable(JTextComponent target, String varType, String varName,
            String annotationType, Map<String, Object> annotationArguments) throws Throwable {

        JavaSource javaSource = JavaSource.forDocument(target.getDocument());
        ModificationTask<WorkingCopy> task = new AddVariableTask<WorkingCopy> (
                varType, varName, annotationType, annotationArguments);

        ModificationResult result = null;
        try {
            result = javaSource.runModificationTask(task);
        } catch (IOException ioe) {
            Exception taskException = task.getException();
            throw new Exception("error adding variable to JavacTreeModel", taskException);  // NOI18N
        }
        result.commit();
    }
    
   public static void addConsumer(JavaSource javaSource, ExecutableElement method, Map<String,Object> props ) throws Throwable {

        AddConsumerTask<WorkingCopy> task = new AddConsumerTask<WorkingCopy> (
                method, props);

        ModificationResult result = null;
        try {
            result = javaSource.runModificationTask(task);
        } catch (Throwable ioe) {
            Exception taskException = task.getException();
            throw new Exception("error adding POJO to JavaTreeModel", taskException);  // NOI18N
        }
        result.commit();       
    
   }
 

   public static void addPOJO(JavaSource javaSource, String methodName, String methodReturnType,List<String> methodArgumentType,
            String annotationType, Map<String, Object> annotationArguments, Map<String,Object> operationArguments) throws Throwable {

        ModificationTask<WorkingCopy> task = new AddPOJOTask<WorkingCopy> (
                methodName, methodReturnType,methodArgumentType, annotationType, annotationArguments, operationArguments);

        ModificationResult result = null;
        try {
            result = javaSource.runModificationTask(task);
        } catch (IOException ioe) {
            Exception taskException = task.getException();
            throw new Exception("error adding POJO to JavaTreeModel", taskException);  // NOI18N
        }
        result.commit();
    }
    
    /**
     * Get the namespace formatted for POJO wizard.
     * @param pkg
     * @return String
     */
    public static String getNamespace(String pkg, String endpointName) {
        Stack<String> dotName = new Stack<String>();

        StringTokenizer stk = new StringTokenizer(pkg, ".");
        while (stk.hasMoreTokens()){
            dotName.push(stk.nextToken());
        }

        StringBuilder sb = new StringBuilder();
        sb.append("http://");
        boolean first = true;
        while (!dotName.empty()){
            if (!first){
                sb.append("."); //NOI18N
            } else {
                first = false;
            }
            sb.append(dotName.pop());
        }
        sb.append("/");
        sb.append(endpointName);
        sb.append("/");               
                
        return sb.toString();
    }    

    public static String generatePOJOAnnotations(WizardDescriptor wizard) {
        String ann= "";
        String className = (String)wizard.getProperty(GeneratorUtil.POJO_CLASS_NAME);
        String endpointName = (String)wizard.getProperty(GeneratorUtil.POJO_ENDPOINT_NAME);
        String packageName = GeneratorUtil.getNamespace((String) wizard.getProperty(GeneratorUtil.POJO_PACKAGE_NAME), className);
        String interfaceNs = (String) wizard.getProperty(GeneratorUtil.POJO_INTERFACE_NS);
        String interfaceName = (String) wizard.getProperty(GeneratorUtil.POJO_INTERFACE_NAME);
        String serviceName = (String) wizard.getProperty(GeneratorUtil.POJO_SERVICE_NAME);
        String serviceNs = (String) wizard.getProperty(GeneratorUtil.POJO_SERVICE_NS);
        
        
        
        if (!( ( className.equals(endpointName)) && packageName.equals(interfaceNs) && 
             serviceNs.equals(interfaceNs) && interfaceName.equals(className+GeneratorUtil.POJO_INTERFACE_SUFFIX) &&
              serviceName.equals(className+GeneratorUtil.POJO_SERVICE_SUFFIX))) {
            
             StringBuilder strBuild = new StringBuilder();
             strBuild.append("("); //NOI18N
             prepareAnnotationParameter(strBuild,GeneratorUtil.NAME_CONST, endpointName);
             strBuild.append(",");//NOI18N
             prepareAnnotationParameter(strBuild,GeneratorUtil.INTF_QNAME_CONST, new QName(interfaceNs,interfaceName).toString());
             strBuild.append(",");//NOI18N
             prepareAnnotationParameter(strBuild,GeneratorUtil.SERVICE_QNAME, new QName(serviceNs,serviceName).toString());
             strBuild.append(")");//NOI18N
             ann = strBuild.toString();
        }
        return ann;
    }

    private static String createString(InputStream is) {
        StringWriter stw = new StringWriter();
        BufferedInputStream bis = new BufferedInputStream(is);
        int c = -1;
        try {
            while ((c = bis.read()) != -1) {
                stw.write((char) c);
            }
            stw.flush();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }finally  {
            try {
                is.close();
                is = null;
            } catch (Exception ex) {
                
            }
        }
        return stw.toString();
        
    }
        
    private static void prepareAnnotationParameter(StringBuilder strBuild,String key, String value) {
         strBuild.append(key);
         strBuild.append("=");
         strBuild.append('"');
         strBuild.append(value);
         strBuild.append('"');             
    }
    
    public static String isValidNamespace(String namespace, String msgKey)  {
        String message = null;
        if ( namespace == null || namespace.trim().equals("")) {
            return NbBundle.getMessage (OperationMethodChooserPanel.class, msgKey);
        }
        try {
            new URI(namespace);
        } catch ( Exception ex) {
            message = NbBundle.getMessage (OperationMethodChooserPanel.class, msgKey);
        }
        return message;

    }
        
    public static String getHungarianNotation(String data) {
        if ( data != null && data.length() > 2) {
            data =data.substring(0, 1).toUpperCase()+data.substring(1);
        }
        return data.trim();
    }
    
    public static String getVariableName(List<String>listOfVariables, String resourceVar) {
        if ( listOfVariables == null || listOfVariables.size() == 0) {
            return resourceVar;
        }
        if ( ! listOfVariables.contains(resourceVar)) {
            return resourceVar;
        }
        
        List<String> listOfMatchedVariables= new ArrayList<String>();
        for ( String var:listOfVariables) {
           if (  var.startsWith(resourceVar))  {
              listOfMatchedVariables.add(var);
           }
        }
        Collections.sort(listOfMatchedVariables);
        String closeMatch = listOfMatchedVariables.get(listOfMatchedVariables.size() -1);
        char ch = (char)closeMatch.charAt(closeMatch.length()-1);
        if ( Character.isDigit(ch)) {
            return closeMatch.substring(0,closeMatch.length()-1)+((char) (ch+1));
        } else {
            return closeMatch + '0';
        }
        
    }
}

