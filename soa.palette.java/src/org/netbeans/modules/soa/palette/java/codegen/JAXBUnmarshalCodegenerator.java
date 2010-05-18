/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.soa.palette.java.codegen;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.VariableTree;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.lang.model.element.Modifier;
import javax.swing.text.JTextComponent;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.soa.palette.java.util.JavaSourceUtil;
import org.netbeans.modules.soa.palette.java.util.OTDImportConstants;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;

/**
 *
 * @author gpatil
 */
public class JAXBUnmarshalCodegenerator extends BaseCodegenerator {
    @Override
    public void generateCode(JTextComponent doc, Map<String, Object> input) throws IOException {
        JavaSource javaSource = JavaSource.forDocument(doc.getDocument());
        BaseGenTask<WorkingCopy> genTask = getGenTask(input);
        ModificationResult result = null;
        try {
            //TODO run in non-awt thread
            result = javaSource.runModificationTask(genTask);
        } catch (IOException ioe) {
            Exception taskException = genTask.getException();
            if (taskException != null) {
                NotifyDescriptor d = new NotifyDescriptor.Exception(taskException);
                DialogDisplayer.getDefault().notifyLater(d);
                return;
            } else {
                throw ioe;
            }
        }
        result.commit();
    }

    private BaseGenTask<WorkingCopy> getGenTask(Map<String, Object> input){
        JAXBUnmarshalCodeGenTask<WorkingCopy> genTask =
                new JAXBUnmarshalCodeGenTask<WorkingCopy>(input);
        return genTask;
    }

    private class JAXBUnmarshalCodeGenTask<T extends WorkingCopy> extends BaseGenTask{
        Map<String, Object> input;
        String unmarshalType;
        String classType;

        JAXBUnmarshalCodeGenTask(Map<String, Object> input){
            this.input = input;
            this.unmarshalType = (String)input.get(
                    OTDImportConstants.MAP_KEY_MARSHAL_UNMARSHAL_TYPE);
            this.classType = (String)input.get(
                    OTDImportConstants.MAP_KEY_QUAL_CLASS_NAME);
        }

        @Override
        public Exception getException() {
            return myException;
        }

        public void run(WorkingCopy workingCopy) throws Exception {
            try {
                workingCopy.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);

                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                ClassTree classTree = (ClassTree) cut.getTypeDecls().get(0);
                ClassTree newClassTree = classTree;

                ClassTree result = createMethodTree(workingCopy, newClassTree);
                if (result != null) {
                    newClassTree = result;
                }

                workingCopy.rewrite(classTree, newClassTree);
            } catch (Exception e) {
                myException = e;
                throw e;
            }
        }

        private ClassTree createMethodTree(WorkingCopy workingCopy, ClassTree classTree) throws Exception {
            TreeMaker make = workingCopy.getTreeMaker();

            //Method Modifiers
            Set<Modifier> modifierSet = EnumSet.noneOf(Modifier.class);
            modifierSet.add(Modifier.PRIVATE);

            List<AnnotationTree> annotationTreeList = new ArrayList<AnnotationTree> ();
            make.Modifiers(modifierSet, annotationTreeList);

            ModifiersTree modifiers = make.Modifiers(modifierSet, annotationTreeList);

            List<VariableTree> params = handleParams(workingCopy);

            List<ExpressionTree> throwsList = getExceptionList(make, unmarshalType);

            String name = (String) input.get(OTDImportConstants.MAP_KEY_METHOD_NAME);
            String returnType = getReturnType();

            String bodyText = getMethodBody(unmarshalType);

            // make a new tree
            MethodTree methodTree = make.Method(
                    modifiers, // modifiers and annotations
                    name, // name
                    JavaSourceUtil.createType(make, workingCopy, returnType), // return type
                    Collections.EMPTY_LIST, // type parameters for parameters
                    params, // parameters
                    throwsList, // throws
                    //make.Block(Collections.<StatementTree>emptyList(), false), // body
                    "{" + bodyText + "}", // body text //NOI18N
                    null // default value - not applicable here, used by annotations
                    );

            return make.addClassMember(classTree, methodTree);
        }

        private String getReturnType(){
            return this.classType;
        }

        private List<VariableTree> handleParams(WorkingCopy workingCopy) throws Exception {
            TreeMaker make = workingCopy.getTreeMaker();
            List<VariableTree> paramList = new ArrayList<VariableTree>();

            if ("String".equalsIgnoreCase(unmarshalType)){ //NOI18N
                paramList.add(make.Variable(
                        make.Modifiers(Collections.EMPTY_SET, Collections.EMPTY_LIST),
                        "str", //NOI18N
                        JavaSourceUtil.createQualIdent(make, workingCopy, "String"), //NOI18N
                        null));
            }

            if ("InputStream".equalsIgnoreCase(unmarshalType)){ //NOI18N
                paramList.add(make.Variable(
                        make.Modifiers(Collections.EMPTY_SET, Collections.EMPTY_LIST),
                        "is", //NOI18N
                        JavaSourceUtil.createQualIdent(make, workingCopy, "java.io.InputStream"), //NOI18N
                        null));
            }

            if ("Reader".equalsIgnoreCase(unmarshalType)){ //NOI18N
                paramList.add(make.Variable(
                        make.Modifiers(Collections.EMPTY_SET, Collections.EMPTY_LIST),
                        "rdr", //NOI18N
                        JavaSourceUtil.createQualIdent(make, workingCopy, "java.io.Reader"), //NOI18N
                        null));
            }

            if ("File".equalsIgnoreCase(unmarshalType)){ //NOI18N
                paramList.add(make.Variable(
                        make.Modifiers(Collections.EMPTY_SET, Collections.EMPTY_LIST),
                        "file", //NOI18N
                        JavaSourceUtil.createQualIdent(make, workingCopy, "java.io.File"), //NOI18N
                        null));
            }

            if ("JMSTextMessage".equalsIgnoreCase(unmarshalType)){ //NOI18N
                paramList.add(make.Variable(
                        make.Modifiers(Collections.EMPTY_SET, Collections.EMPTY_LIST),
                        "tm", //NOI18N
                        JavaSourceUtil.createQualIdent(make, workingCopy, "javax.jms.TextMessage"), //NOI18N
                        null));
            }

            return paramList;
        }

       private List<ExpressionTree> getExceptionList(TreeMaker make, String marshalType){
            List<ExpressionTree> ret = new ArrayList<ExpressionTree> ();
            if ("String".equalsIgnoreCase(marshalType)){ //NOI18N
                ret.add(make.Identifier("javax.xml.bind.JAXBException"));
            }else if ("InputStream".equalsIgnoreCase(marshalType)){ //NOI18N
                ret.add(make.Identifier("javax.xml.bind.JAXBException"));
            }else if ("Reader".equalsIgnoreCase(marshalType)){ //NOI18N
                ret.add(make.Identifier("javax.xml.bind.JAXBException"));
            }else if ("File".equalsIgnoreCase(marshalType)){ //NOI18N
                ret.add(make.Identifier("java.io.FileNotFoundException"));
                ret.add(make.Identifier("javax.xml.bind.JAXBException"));
            }else if ("JMSTextMessage".equalsIgnoreCase(marshalType)){ //NOI18N
                ret.add(make.Identifier("javax.jms.JMSException"));
                ret.add(make.Identifier("javax.xml.bind.JAXBException"));
            }

            return ret;
        }

        private String getMethodBody(String marshalType){
            String ret = null;
            if ("String".equalsIgnoreCase(marshalType)){ //NOI18N
                ret = getMethodBodyForString();
            }else if ("InputStream".equalsIgnoreCase(marshalType)){ //NOI18N
                ret = getMethodBodyForStream();
            }else if ("Reader".equalsIgnoreCase(marshalType)){ //NOI18N
                ret = getMethodBodyForReader();
            }else if ("File".equalsIgnoreCase(marshalType)){ //NOI18N
                ret = getMethodBodyForFile();
            }else if ("JMSTextMessage".equalsIgnoreCase(marshalType)){ //NOI18N
                ret = getMethodBodyForTextMessage();
            }

            return ret;
        }

        // Code templates
        // TODO move to a file.
        private String getMethodBodyForString(){
            String ret =
"        ${className} ret = null;\n" + //NOI18N
"        javax.xml.bind.JAXBContext jaxbCtx = javax.xml.bind.JAXBContext.newInstance(${className}.class.getPackage().getName());\n" + //NOI18N
"        javax.xml.bind.Unmarshaller unmarshaller = jaxbCtx.createUnmarshaller();\n" + //NOI18N
"        ret = (${className}) unmarshaller.unmarshal(new java.io.StringReader(str)); //NOI18N\n" + //NOI18N
"        return ret;\n" ; //NOI18N
            ret = ret.replaceAll("\\$\\{className\\}", this.classType); // NOI18N
            return ret;
        }

        private String getMethodBodyForStream(){
            String ret =
"        ${className} ret = null;\n" + //NOI18N
"        try {\n" + //NOI18N
"            javax.xml.bind.JAXBContext jaxbCtx = javax.xml.bind.JAXBContext.newInstance(${className}.class.getPackage().getName());\n" + //NOI18N
"            javax.xml.bind.Unmarshaller unmarshaller = jaxbCtx.createUnmarshaller();\n" + //NOI18N
"            ret = (${className}) unmarshaller.unmarshal(is); //NOI18N\n" + //NOI18N
"        } finally {\n" + //NOI18N
"            try {\n" + //NOI18N
"                is.close();\n" + //NOI18N
"            }catch (Exception ex){\n" + //NOI18N
"                java.util.logging.Logger.getLogger(\"global\").log(java.util.logging.Level.SEVERE, null, ex); //NOI18N\n" + //NOI18N
"            }\n" + //NOI18N
"        }\n" +	//NOI18N
"        return ret;\n" ; //NOI18N
            ret = ret.replaceAll("\\$\\{className\\}", this.classType); // NOI18N
            return ret;
        }

        private String getMethodBodyForReader(){
            String ret =
"        ${className} ret = null;\n" +  //NOI18N
"        try {\n" + //NOI18N
"            javax.xml.bind.JAXBContext jaxbCtx = javax.xml.bind.JAXBContext.newInstance(${className}.class.getPackage().getName());\n" + //NOI18N
"            javax.xml.bind.Unmarshaller unmarshaller = jaxbCtx.createUnmarshaller();\n" + //NOI18N
"            ret = (${className}) unmarshaller.unmarshal(rdr); //NOI18N\n" + //NOI18N
"        } finally {\n" + //NOI18N
"            try {\n" + //NOI18N
"                rdr.close();\n" + //NOI18N
"            }catch (Exception ex){\n" + //NOI18N
"                java.util.logging.Logger.getLogger(\"global\").log(java.util.logging.Level.SEVERE, null, ex); //NOI18N\n" + //NOI18N
"            }\n" + //NOI18N
"        }\n" + //NOI18N
"        return ret;\n" ; //NOI18N

            ret = ret.replaceAll("\\$\\{className\\}", this.classType); // NOI18N
            return ret;
        }

        private String getMethodBodyForFile(){
            String ret =
"        ${className} ret = null;\n" + //NOI18N
"        java.io.InputStream is = null;\n" + //NOI18N
"        try {\n" + //NOI18N
"            javax.xml.bind.JAXBContext jaxbCtx = javax.xml.bind.JAXBContext.newInstance(${className}.class.getPackage().getName());\n" + //NOI18N
"            javax.xml.bind.Unmarshaller unmarshaller = jaxbCtx.createUnmarshaller();\n" + //NOI18N
"            is = new java.io.FileInputStream(file);\n" + //NOI18N
"            ret = (${className}) unmarshaller.unmarshal(is); //NOI18N\n" + //NOI18N
"        } finally {\n" + //NOI18N
"            try {\n" + //NOI18N
"                is.close();\n" + //NOI18N
"            }catch (Exception ex){\n" + //NOI18N
"                java.util.logging.Logger.getLogger(\"global\").log(java.util.logging.Level.SEVERE, null, ex); //NOI18N\n" + //NOI18N
"            }\n" + //NOI18N
"        }\n" + //NOI18N
"        return ret;\n" ; //NOI18N

            ret = ret.replaceAll("\\$\\{className\\}", this.classType); // NOI18N
            return ret;
        }

        private String getMethodBodyForTextMessage(){
            String ret =
"        ${className} ret = null;\n" + //NOI18N
"        javax.xml.bind.JAXBContext jaxbCtx = javax.xml.bind.JAXBContext.newInstance(${className}.class.getPackage().getName());\n" + //NOI18N
"        javax.xml.bind.Unmarshaller unmarshaller = jaxbCtx.createUnmarshaller();\n" + //NOI18N
"        String msg = tm.getText();\n" + //NOI18N
"        ret = (${className}) unmarshaller.unmarshal(new java.io.StringReader(msg)); //NOI18N\n" + //NOI18N
"        return ret;\n" ; //NOI18N

            ret = ret.replaceAll("\\$\\{className\\}", this.classType); // NOI18N
            return ret;
        }
    }
}
