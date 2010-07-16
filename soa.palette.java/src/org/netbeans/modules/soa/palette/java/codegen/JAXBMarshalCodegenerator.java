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
public class JAXBMarshalCodegenerator extends BaseCodegenerator {

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
        JAXBMarshalCodeGenTask<WorkingCopy> genTask =
                new JAXBMarshalCodeGenTask<WorkingCopy>(input);
        return genTask;
    }


    private class JAXBMarshalCodeGenTask<T extends WorkingCopy> extends BaseGenTask{
        Map<String, Object> input;
        String marshalType;

        JAXBMarshalCodeGenTask(Map<String, Object> input){
            this.input = input;
            this.marshalType = (String)input.get(
                    OTDImportConstants.MAP_KEY_MARSHAL_UNMARSHAL_TYPE);
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

        private String getReturnType(){
            String ret = "String" ;//NOI18N
            if (!"String".equalsIgnoreCase(marshalType)){
                ret = "void"; //NOI18N
            }
            return ret;
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

            List<ExpressionTree> throwsList = getExceptionList(make, marshalType);

            String name = (String) input.get(OTDImportConstants.MAP_KEY_METHOD_NAME);
            String returnType = getReturnType();

            String bodyText = getMethodBody(marshalType);

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

        private List<VariableTree> handleParams(WorkingCopy workingCopy) throws Exception {
            TreeMaker make = workingCopy.getTreeMaker();
            String className = (String) input.get(OTDImportConstants.MAP_KEY_QUAL_CLASS_NAME);
            List<VariableTree> paramList = new ArrayList<VariableTree>();
            paramList.add(make.Variable(
                        make.Modifiers(Collections.EMPTY_SET, Collections.EMPTY_LIST),
                        "jaxbObj", //NOI18N
                        JavaSourceUtil.createQualIdent(make, workingCopy, className),
                        null));
            // No need for String, it will be return value.

            if ("OutputStream".equalsIgnoreCase(marshalType)){ //NOI18N
                paramList.add(make.Variable(
                        make.Modifiers(Collections.EMPTY_SET, Collections.EMPTY_LIST),
                        "os", //NOI18N
                        JavaSourceUtil.createQualIdent(make, workingCopy, "java.io.OutputStream"), //NOI18N
                        null));
            }

            if ("Writer".equalsIgnoreCase(marshalType)){ //NOI18N
                paramList.add(make.Variable(
                        make.Modifiers(Collections.EMPTY_SET, Collections.EMPTY_LIST),
                        "wrtr", //NOI18N
                        JavaSourceUtil.createQualIdent(make, workingCopy, "java.io.Writer"), //NOI18N
                        null));
            }

            if ("File".equalsIgnoreCase(marshalType)){ //NOI18N
                paramList.add(make.Variable(
                        make.Modifiers(Collections.EMPTY_SET, Collections.EMPTY_LIST),
                        "file", //NOI18N
                        JavaSourceUtil.createQualIdent(make, workingCopy, "java.io.File"), //NOI18N
                        null));
            }

            if ("JMSTextMessage".equalsIgnoreCase(marshalType)){ //NOI18N
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
                ret.add(make.Identifier("javax.xml.bind.JAXBException"));//NOI18N
                ret.add(make.Identifier("java.io.IOException"));//NOI18N
            }else if ("OutputStream".equalsIgnoreCase(marshalType)){ //NOI18N
                ret.add(make.Identifier("javax.xml.bind.JAXBException"));//NOI18N
            }else if ("Writer".equalsIgnoreCase(marshalType)){ //NOI18N
                ret.add(make.Identifier("javax.xml.bind.JAXBException"));//NOI18N
            }else if ("File".equalsIgnoreCase(marshalType)){ //NOI18N
                ret.add(make.Identifier("java.io.FileNotFoundException"));//NOI18N
                ret.add(make.Identifier("javax.xml.bind.JAXBException"));//NOI18N
            }else if ("JMSTextMessage".equalsIgnoreCase(marshalType)){ //NOI18N
                ret.add(make.Identifier("javax.jms.JMSException"));//NOI18N
                ret.add(make.Identifier("javax.xml.bind.JAXBException"));//NOI18N
                ret.add(make.Identifier("java.io.IOException"));//NOI18N
            }

            return ret;
        }

        private String getMethodBody(String marshalType){
            String ret = null;
            if ("String".equalsIgnoreCase(marshalType)){ //NOI18N
                ret = getMethodBodyForString();
            }else if ("OutputStream".equalsIgnoreCase(marshalType)){ //NOI18N
                ret = getMethodBodyForStream();
            }else if ("Writer".equalsIgnoreCase(marshalType)){ //NOI18N
                ret = getMethodBodyForWriter();
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
        return
"       java.io.StringWriter sw = new java.io.StringWriter();\n" + //NOI18N
"       javax.xml.bind.JAXBContext jaxbCtx = javax.xml.bind.JAXBContext.newInstance(jaxbObj.getClass().getPackage().getName());\n" + //NOI18N
"       javax.xml.bind.Marshaller marshaller = jaxbCtx.createMarshaller();\n" + //NOI18N
"       marshaller.setProperty(javax.xml.bind.Marshaller.JAXB_ENCODING, \"UTF-8\"); //NOI18N\n" + //NOI18N
"       marshaller.setProperty(javax.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);\n" + //NOI18N
"       marshaller.marshal(jaxbObj, sw);\n" + //NOI18N
"       sw.close();\n" + //NOI18N
"       return sw.toString();\n" + //NOI18N
"       \n" ; //NOI18N
    }

        private String getMethodBodyForStream(){
            return
"        javax.xml.bind.JAXBContext jaxbCtx = javax.xml.bind.JAXBContext.newInstance(jaxbObj.getClass().getPackage().getName());\n" + //NOI18N
"        javax.xml.bind.Marshaller marshaller = jaxbCtx.createMarshaller();\n" + //NOI18N
"        marshaller.setProperty(javax.xml.bind.Marshaller.JAXB_ENCODING, \"UTF-8\"); //NOI18N\n" + //NOI18N
"        marshaller.setProperty(javax.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);\n" + //NOI18N
"        marshaller.marshal(jaxbObj, os);\n" + //NOI18N
"        \n" + //NOI18N
"        return;\n" ; //NOI18N
        }

        private String getMethodBodyForWriter(){
            return
"        javax.xml.bind.JAXBContext jaxbCtx = javax.xml.bind.JAXBContext.newInstance(jaxbObj.getClass().getPackage().getName());\n" + //NOI18N
"        javax.xml.bind.Marshaller marshaller = jaxbCtx.createMarshaller();\n" + //NOI18N
"        marshaller.setProperty(javax.xml.bind.Marshaller.JAXB_ENCODING, \"UTF-8\"); //NOI18N\n" + //NOI18N
"        marshaller.setProperty(javax.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);\n" + //NOI18N
"        marshaller.marshal(jaxbObj, wrtr);\n" + //NOI18N
"        \n" + //NOI18N
"        return;\n"; //NOI18N
        }

        private String getMethodBodyForFile(){
            return
"        java.io.OutputStream os = null;\n" + //NOI18N
"        try {\n" + //NOI18N
"            os = new java.io.FileOutputStream(file);\n" + //NOI18N
"            javax.xml.bind.JAXBContext jaxbCtx = javax.xml.bind.JAXBContext.newInstance(jaxbObj.getClass().getPackage().getName());\n" + //NOI18N
"            javax.xml.bind.Marshaller marshaller = jaxbCtx.createMarshaller();\n" + //NOI18N
"            marshaller.setProperty(javax.xml.bind.Marshaller.JAXB_ENCODING, \"UTF-8\"); //NOI18N\n" + //NOI18N
"            marshaller.setProperty(javax.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);\n" + //NOI18N
"            marshaller.marshal(jaxbObj, os);\n" + //NOI18N
"        } finally {\n" + //NOI18N
"            try {\n" + //NOI18N
"                os.close();\n" + //NOI18N
"            } catch (java.io.IOException ex) {\n" + //NOI18N
"                java.util.logging.Logger.getLogger(\"global\").log(java.util.logging.Level.SEVERE, null, ex);\n" + //NOI18N
"            }\n" + //NOI18N
"        }\n" + //NOI18N
"        \n" + //NOI18N
"        return;\n" ; //NOI18N
        }

        private String getMethodBodyForTextMessage(){
            return
"        java.io.StringWriter sw = new java.io.StringWriter();\n" + //NOI18N
"        javax.xml.bind.JAXBContext jaxbCtx = javax.xml.bind.JAXBContext.newInstance(jaxbObj.getClass().getPackage().getName());\n" + //NOI18N
"        javax.xml.bind.Marshaller marshaller = jaxbCtx.createMarshaller();\n" + //NOI18N
"        marshaller.setProperty(javax.xml.bind.Marshaller.JAXB_ENCODING, \"UTF-8\"); //NOI18N\n" + //NOI18N
"        marshaller.setProperty(javax.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);\n" + //NOI18N
"        marshaller.marshal(jaxbObj, sw);\n" + //NOI18N
"        sw.close();\n" + //NOI18N
"        tm.setText(sw.toString());\n" + //NOI18N
"        \n" + //NOI18N
"        return;\n"; //NOI18N
        }
    }
}
