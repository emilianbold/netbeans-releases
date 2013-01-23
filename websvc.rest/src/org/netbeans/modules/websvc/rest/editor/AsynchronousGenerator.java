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
package org.netbeans.modules.websvc.rest.editor;

import java.awt.Toolkit;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.swing.text.JTextComponent;

import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelException;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.websvc.rest.RestUtils;
import org.netbeans.modules.websvc.rest.model.api.RestMethodDescription;
import org.netbeans.modules.websvc.rest.model.api.RestServiceDescription;
import org.netbeans.modules.websvc.rest.model.api.RestServices;
import org.netbeans.modules.websvc.rest.model.api.RestServicesMetadata;
import org.netbeans.modules.websvc.rest.model.api.RestServicesModel;
import org.netbeans.spi.editor.codegen.CodeGenerator;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.BlockTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;


/**
 * @author ads
 *
 */
public class AsynchronousGenerator implements CodeGenerator {
    
    private final Logger log = Logger.getLogger(AsynchronousGenerator.class.getName()); 

    private AsynchronousGenerator( CompilationController controller,
            JTextComponent component )
    {
        this.controller = controller;
        this.textComponent = component;
    }

    /* (non-Javadoc)
     * @see org.netbeans.spi.editor.codegen.CodeGenerator#getDisplayName()
     */
    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(AsynchronousGenerator.class,"LBL_ConvertMethod");    // NOI18N
    }

    /* (non-Javadoc)
     * @see org.netbeans.spi.editor.codegen.CodeGenerator#invoke()
     */
    @Override
    public void invoke() {
        Project project = FileOwnerQuery.getOwner(controller.getFileObject());
        if (project == null) {
            return;
        }
        WebModule webModule = WebModule.getWebModule(project
                .getProjectDirectory());
        if (webModule == null) {
            return;
        }
        Profile profile = webModule.getJ2eeProfile();
        if (!Profile.JAVA_EE_7_WEB.equals(profile)
                && !Profile.JAVA_EE_7_FULL.equals( profile))
        {
            Toolkit.getDefaultToolkit().beep();
            StatusDisplayer.getDefault().setStatusText(
                    NbBundle.getMessage(AsynchronousGenerator.class, 
                            "MSG_NotJee7Profile"));                  // NOI18N
            return;
        }
        
        int position = textComponent.getCaret().getDot();
        TreePath tp = controller.getTreeUtilities().pathFor(position);
        Element contextElement = controller.getTrees().getElement(tp );
        if ( contextElement == null || contextElement.getKind() != ElementKind.METHOD){
            Toolkit.getDefaultToolkit().beep();
            StatusDisplayer.getDefault().setStatusText(
                    NbBundle.getMessage(AsynchronousGenerator.class, 
                            "MSG_NotRestMethod"));                  // NOI18N
            return;
        }
        
        Element enclosingElement = contextElement.getEnclosingElement();
        if ( enclosingElement== null || !(enclosingElement instanceof TypeElement)){
            Toolkit.getDefaultToolkit().beep();
            StatusDisplayer.getDefault().setStatusText(
                    NbBundle.getMessage(AsynchronousGenerator.class, 
                            "MSG_NotRestMethod"));                  // NOI18N
            return;
        }
        
        TypeElement clazz = (TypeElement)enclosingElement;
        final String fqn = clazz.getQualifiedName().toString();
        final String methodName = contextElement.getSimpleName().toString();
        
        if ( !checkRestMethod(fqn,methodName) ){
            Toolkit.getDefaultToolkit().beep();
            StatusDisplayer.getDefault().setStatusText(
                    NbBundle.getMessage(AsynchronousGenerator.class, 
                            "MSG_NotRestMethod"));                  // NOI18N
            return;
        }
        
        convertMethod(contextElement);
    }

    private void convertMethod( Element contextElement ) {
        final ElementHandle<Element> handle = ElementHandle.create(contextElement);
        FileObject classFile = controller.getFileObject();  
        JavaSource javaSource = JavaSource.forFileObject(classFile);
        if ( javaSource == null ){
            return;
        }
        try {
            ModificationResult task = javaSource.runModificationTask( 
                    new Task<WorkingCopy>() {

                @Override
                public void run( WorkingCopy copy ) throws Exception {
                    copy.toPhase(Phase.ELEMENTS_RESOLVED);
                    
                    Element restMethod = handle.resolve(copy);
                    if ( restMethod== null){
                        return;
                    }
                    Element enclosingElement = restMethod.getEnclosingElement();
                    if ( !(enclosingElement instanceof TypeElement )){
                        return;
                    }
                    ClassTree classTree= (ClassTree)copy.getTrees().getTree(
                            enclosingElement);
                    
                    MethodTree method = (MethodTree)copy.getTrees().getTree(
                            restMethod);
                    String name = restMethod.getSimpleName().toString();
                    String asyncName = findFreeName(name, enclosingElement, restMethod);
                    String movedName = findFreeName(convertMethodName(name), 
                            enclosingElement, restMethod);
                    
                    TreeMaker maker = copy.getTreeMaker();
                    ClassTree newTree = addExecutionService(maker,copy,enclosingElement,
                            classTree);
                    newTree = createAsyncMethod(maker, asyncName, serviceField,
                            method, movedName, copy, newTree);
                    newTree =moveRestMethod(maker, movedName, method, copy, 
                            newTree);
                    copy.rewrite(classTree, newTree);
                }
                
                private ClassTree addExecutionService( TreeMaker maker,
                        WorkingCopy copy, Element clazz, ClassTree classTree )
                {
                    List<VariableElement> fields = 
                            ElementFilter.fieldsIn(clazz.getEnclosedElements());
                    Set<String> fieldNames = new HashSet<String>();
                    for (VariableElement field : fields) {
                        fieldNames.add(field.getSimpleName().toString());
                        TypeMirror fieldType = field.asType();
                        Element fieldTypeElement = copy.getTypes().asElement(fieldType);
                        if ( fieldTypeElement instanceof TypeElement ){
                            TypeElement type = (TypeElement)fieldTypeElement;
                            if (ExecutorService.class.getName().contentEquals(
                                    type.getQualifiedName()))
                            {
                                serviceField = field.getSimpleName().toString();
                            }
                        }
                    }
                    if ( serviceField == null){
                        String name = "executorService";            // NOI18N
                        serviceField = name;
                        int i=0;
                        while(fieldNames.contains(serviceField)){
                            serviceField = name+i;
                            i++;
                        }
                    }
                    else {
                        return classTree;
                    }
                    MethodInvocationTree init = maker.MethodInvocation(
                            Collections.<ExpressionTree>emptyList(), 
                            maker.QualIdent(Executors.class.getName()+
                                    ".newCachedThreadPool"), 
                                    Collections.<ExpressionTree>emptyList());
                    VariableTree service = maker.Variable(
                            maker.Modifiers(EnumSet.of(Modifier.PRIVATE)), 
                            serviceField, 
                            maker.QualIdent(ExecutorService.class.getName()), 
                            init);
                    return maker.addClassMember(classTree, service);
                }
                
                private String serviceField;

            });
            task.commit();
        }
        catch (IOException e) {
            Toolkit.getDefaultToolkit().beep();
            log.log(Level.INFO, null, e);
        }
    }
    
    private ClassTree createAsyncMethod( TreeMaker maker,
            String asyncName, String service, MethodTree method, String movedName , 
            WorkingCopy copy, ClassTree classTree)
    {
        ModifiersTree modifiers = method.getModifiers();
        List<? extends VariableTree> parameters = method.getParameters();
        String asyncReponseParam = getAsynParam("asyncResponse",parameters);
        
        ModifiersTree paramModifier = maker.Modifiers(EnumSet.of(Modifier.FINAL));
        AnnotationTree annotation = maker.Annotation(
                maker.QualIdent("javax.ws.rs.container.Suspended"), 
                Collections.<ExpressionTree>emptyList());
        paramModifier = maker.Modifiers(paramModifier, 
                Collections.singletonList(annotation));
        VariableTree asyncParam = maker.Variable(paramModifier, asyncReponseParam, 
                maker.QualIdent("javax.ws.rs.container.AsyncResponse"), null);
        List<VariableTree> params = new ArrayList<VariableTree>(parameters.size()+1);
        params.add(asyncParam);
        
        
        StringBuilder body = new StringBuilder("{");
        body.append(service);
        body.append(".submit(new Runnable() { public void run() {");
        body.append(asyncReponseParam);
        body.append(".resume(");
        body.append(movedName);
        body.append("(");
        for (VariableTree param : parameters) {
            ModifiersTree modifier = maker.addModifiersModifier(param.getModifiers(), 
                    Modifier.FINAL);
            VariableTree newParam = maker.Variable(modifier, param.getName(), 
                    param.getType(), param.getInitializer());
            params.add(newParam);
            TreePath pathParam = copy.getTrees().getPath(
                    copy.getCompilationUnit(), param);
            body.append(copy.getTrees().getElement(pathParam).getSimpleName());
            body.append(',');
        }
        if ( !parameters.isEmpty()){
            body.deleteCharAt(body.length()-1);
        }
        body.append("));");
        body.append('}');
        
        MethodTree newMethod = maker.Method(modifiers, asyncName, 
                maker.Type("void"),             // NOI18N
                Collections.<TypeParameterTree> emptyList(),
                params,
                Collections.<ExpressionTree> emptyList(),
                body.toString(),null);
        return maker.addClassMember(classTree, newMethod);
    }
    
    private String getAsynParam(String paramName, List<? extends VariableTree> parameters ) {
        for (VariableTree variableTree : parameters) {
            if ( paramName.equals(variableTree.getName())){
                return getAsynParam(paramName+1 ,parameters);
            }
        }
        return paramName;
    }

    private ClassTree moveRestMethod( TreeMaker maker, String movedName,
            MethodTree method, WorkingCopy copy, ClassTree classTree)
    {
        List<? extends VariableTree> parameters = method.getParameters();
        Tree returnType = method.getReturnType();
        BlockTree body = method.getBody();  
        
        ModifiersTree modifiers = maker.Modifiers(EnumSet.of(Modifier.PRIVATE));
        MethodTree newMethod = maker.Method(modifiers, movedName, 
                returnType,
                Collections.<TypeParameterTree> emptyList(),
                parameters,
                Collections.<ExpressionTree> emptyList(),body,null);
        
        ClassTree newClass = maker.addClassMember(classTree, newMethod);
        newClass = maker.removeClassMember(newClass, method);
        return newClass;
    }
    
    private String convertMethodName(String name){
        if ( name.length()<=1){
            return "do"+name;       // NOI18N
        }
        else {
            return "do"+Character.toUpperCase(name.charAt(0))+name.substring(1);
        }
    }
    
    private String findFreeName( String name,Element enclosingElement,
            Element havingName)
    {
        for(ExecutableElement method:
            ElementFilter.methodsIn(enclosingElement.getEnclosedElements()))
        {
            if (method.equals(havingName)){
                continue;
            }
            if ( method.getSimpleName().contentEquals(name)){
                return findFreeName(name+1,enclosingElement, havingName);
            }
        }
        return name;
    }

    private boolean checkRestMethod( final String fqn , final String methodName) {
        Project project = FileOwnerQuery.getOwner(controller.getFileObject());
        RestServicesModel model = RestUtils.getRestServicesMetadataModel(project);
        if ( model == null){
            Toolkit.getDefaultToolkit().beep();
            StatusDisplayer.getDefault().setStatusText(
                    NbBundle.getMessage(AsynchronousGenerator.class, 
                            "MSG_NotRestMethod"));                  // NOI18N
            return false;
        }
        try {
            return model.runReadAction(
                    new MetadataModelAction<RestServicesMetadata, Boolean>()
            {

                @Override
                public Boolean run( RestServicesMetadata metadata )
                        throws Exception
                {
                    RestServices services = metadata.getRoot();
                    RestServiceDescription[] descriptions = 
                            services.getRestServiceDescription();
                    for (RestServiceDescription description : descriptions) {
                        if ( fqn.equals(description.getClassName())){
                            List<RestMethodDescription> methods = 
                                    description.getMethods();
                            for (RestMethodDescription method : methods){
                                if ( methodName.equals(method.getName())){
                                    return true;
                                }
                            }
                        }
                    }
                    return false;
                }
            });
        }
        catch(MetadataModelException e ){
            log.log(Level.INFO, null,  e);
        }
        catch(IOException e ){
            log.log(Level.INFO, null,  e);
        }
        return false;
    }
    
    public static class Factory implements CodeGenerator.Factory {

        @Override
        public List<? extends CodeGenerator> create(Lookup context) {
            CompilationController controller = context.lookup(CompilationController.class);

            List<CodeGenerator> ret = new ArrayList<CodeGenerator>();
            if (controller != null) {
                try {
                    controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                    FileObject targetSource = controller.getFileObject();
                    if (targetSource != null) {
                        JTextComponent targetComponent = context.lookup(JTextComponent.class);
                        ret.add(new AsynchronousGenerator(controller, targetComponent));
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
            return ret;
        }
    }
    
    private CompilationController controller;
    private JTextComponent textComponent;

}
