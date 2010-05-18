/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.soa.palette.java.codegen;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.ImportTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.VariableTree;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.soa.palette.java.constructs.ConstructsProgressReporter;
import org.netbeans.modules.soa.palette.java.constructs.JaxbCodeConstructor;
import org.netbeans.modules.soa.palette.java.util.JavaSourceUtil;
import org.netbeans.modules.soa.palette.java.util.OTDImportConstants;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.RequestProcessor;

/**
 *
 * @author gpatil
 */
public class JAXBConstructsCodegenerator extends BaseCodegenerator {
    @Override
    public void generateCode(JTextComponent doc, Map<String, Object> input) throws IOException {
        ConstructsProgressReporter h = new ConstructsProgressReporter();
        h.start();
        GenConstructCode gcc = new GenConstructCode(doc, input, h);
        RequestProcessor.getDefault().post(gcc);
    }

    class GenerateTask<T extends WorkingCopy> implements Task<WorkingCopy> {
        private Map<String, Object> usrIn;
        private Exception myException = null;
        private Map constCgOpt = null;

        public GenerateTask(Map<String, Object> input, Map codegenOut){
            this.usrIn = input;
            this.constCgOpt = codegenOut;
        }

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

        private void createImportTree(WorkingCopy workingCopy, List<String> imps) throws Exception {
            CompilationUnitTree cut = workingCopy.getCompilationUnit();
            CompilationUnitTree copy = null;
            TreeMaker make = workingCopy.getTreeMaker();
            ImportTree it = null;
            IdentifierTree id = null;
            copy = cut;
            for (String imp: imps){
                id = make.Identifier(imp);
                it = make.Import(id, false);
                copy = make.addCompUnitImport(copy, it);
            }
            workingCopy.rewrite(cut, copy);
        }

        private ClassTree createMethodTree(WorkingCopy workingCopy, ClassTree classTree) throws Exception {
            TreeMaker make = workingCopy.getTreeMaker();

            //Method Modifiers
            Set<Modifier> modifierSet = EnumSet.noneOf(Modifier.class);
            modifierSet.add(Modifier.PRIVATE);

            List<AnnotationTree> annotationTreeList = new ArrayList<AnnotationTree> ();
            make.Modifiers(modifierSet, annotationTreeList);

            ModifiersTree modifiers = make.Modifiers(modifierSet, annotationTreeList);

            List<VariableTree> params = new ArrayList<VariableTree>();
            List<ExpressionTree> throwsList = new ArrayList<ExpressionTree> ();
            List<String> ts = (List<String>) constCgOpt.get(OTDImportConstants.MAP_KEY_METHOD_THROWS);
            if (ts != null){
                TypeElement element = null;
                ExpressionTree throwsClause = null;
                for (String c : ts){
                    element = workingCopy.getElements().getTypeElement(c);
                    throwsClause = make.QualIdent(element);
                    throwsList.add(throwsClause);
                }
            }
            String name = (String) usrIn.get(OTDImportConstants.MAP_KEY_METHOD_NAME);
            String className = (String) usrIn.get(OTDImportConstants.MAP_KEY_QUAL_CLASS_NAME);
            
            String returnType = className;
            String bodyText = ""; //NOI18N

            createImportTree(workingCopy,(List<String>) constCgOpt.get(OTDImportConstants.MAP_KEY_IMPORTS));
            bodyText = (String) constCgOpt.get(OTDImportConstants.MAP_KEY_METHOD_BODY);

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
    }

    // Executed in non-ADT thread
    class GenConstructCode implements Runnable {
        private JTextComponent doc;
        private Map<String, Object> input;
        private ConstructsProgressReporter h;
        
        public GenConstructCode(JTextComponent doc, Map<String, Object> input,
                ConstructsProgressReporter h){
            this.doc = doc;
            this.input = input;
            this.h = h;
        }

        public void run() {
            try {
                if (h != null){
                    h.readingXMLFile();
                }
                GenerateTask<WorkingCopy> genTask = null;
                ClassLoader cl = (ClassLoader) input.get(OTDImportConstants.MAP_KEY_CLASSLOADER);
                String className = (String) input.get(OTDImportConstants.MAP_KEY_QUAL_CLASS_NAME);
                File file = (File) input.get(OTDImportConstants.MAP_KEY_SAMPLE_XML);
                
                if (h != null){
                    h.readXMLFile();
                }

                Map map = JaxbCodeConstructor.constructCode(className, 
                        (file == null) ? null : file.getAbsolutePath(), cl, h);

                if (h != null){
                    h.updatingSource();
                }
                
                genTask = new GenerateTask<WorkingCopy>(input, map);
                UpdateDoc ud = new UpdateDoc(doc, genTask, null, h);
                SwingUtilities.invokeLater(ud);
            } catch (Exception ex) {
                UpdateDoc ud = new UpdateDoc(doc, null, ex, h);
                SwingUtilities.invokeLater(ud);
            }
        }
    }

    //Executed in ADT
    class UpdateDoc implements Runnable {
        private GenerateTask<WorkingCopy> genTask;
        private Exception ex;
        private JTextComponent doc;
        private ConstructsProgressReporter h;
        
        public UpdateDoc(JTextComponent doc, GenerateTask<WorkingCopy> genTask, 
                Exception ex, ConstructsProgressReporter h){
            this.doc = doc;
            this.ex = ex;
            this.genTask = genTask;
            this.h = h;
        }

        public void run() {
            ModificationResult result = null;
            JavaSource javaSource = JavaSource.forDocument(doc.getDocument());
            try {
                if (this.ex == null){
                    result = javaSource.runModificationTask(genTask);
                    result.commit();
                } else {
                    throw ex;
                }
            } catch (Exception exp) {
                if ((genTask != null) && (genTask.getException() != null)) {
                    Exception taskException = genTask.getException();
                    NotifyDescriptor d = new NotifyDescriptor.Exception(taskException);
                    DialogDisplayer.getDefault().notifyLater(d);
                } else {
                    NotifyDescriptor d = new NotifyDescriptor.Exception(exp);
                    DialogDisplayer.getDefault().notifyLater(d);                    
                }
            } finally {
                if (h != null){
                    h.updatedSource();
                }
            }
        }
    }
}
