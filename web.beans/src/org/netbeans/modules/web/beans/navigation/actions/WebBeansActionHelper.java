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
package org.netbeans.modules.web.beans.navigation.actions;


import java.awt.Toolkit;
import java.io.IOException;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.util.ElementFilter;
import javax.swing.JDialog;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;

import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser.Result;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.beans.api.model.WebBeansModel;
import org.netbeans.modules.web.beans.navigation.EventsModel;
import org.netbeans.modules.web.beans.navigation.EventsPanel;
import org.netbeans.modules.web.beans.navigation.InjectablesModel;
import org.netbeans.modules.web.beans.navigation.InjectablesPanel;
import org.netbeans.modules.web.beans.navigation.ObserversModel;
import org.netbeans.modules.web.beans.navigation.ObserversPanel;
import org.netbeans.modules.web.beans.navigation.ResizablePopup;
import org.netbeans.modules.web.beans.navigation.actions.ModelActionStrategy.InspectActionId;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.Scope;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.TreePath;

/**
 * @author ads
 *
 */
public class WebBeansActionHelper {
    
    static final String FIRE = "fire";                          // NOI18N
    
    static final String EVENT_INTERFACE = 
        "javax.enterprise.event.Event";                         // NOI18N
    
    static final String OBSERVES_ANNOTATION = 
        "javax.enterprise.event.Observes";                      // NOI18N
    
    private static final Set<JavaTokenId> USABLE_TOKEN_IDS = 
        EnumSet.of(JavaTokenId.IDENTIFIER, JavaTokenId.THIS, JavaTokenId.SUPER);

    private WebBeansActionHelper(){
    }
    
    public static boolean isEnabled() {
        if (EditorRegistry.lastFocusedComponent() == null
                || !EditorRegistry.lastFocusedComponent().isShowing())
        {
            return false;
        }
        if ( OpenProjects.getDefault().getOpenProjects().length == 0 ){
            return false;
        }
        final FileObject fileObject = NbEditorUtilities.getFileObject( 
                EditorRegistry.lastFocusedComponent().getDocument());
        if ( fileObject == null ){
            return false;
        }
        WebModule webModule = WebModule.getWebModule(fileObject);
        if ( webModule == null ){
            return false;
        }
        Profile profile = webModule.getJ2eeProfile();
        return profile.equals(Profile.JAVA_EE_6_FULL) || 
            profile.equals(Profile.JAVA_EE_6_WEB);
    }
    
    
    /**
     * Compilation controller from metamodel could not be used for getting 
     * TreePath via dot because it is not based on one FileObject ( Document ).
     * So this method is required for searching Element at dot.
     * If appropriate element is found it's name is placed into list 
     * along with name of containing type.
     * Resulted element could not be used in metamodel for injectable
     * access. This is because element was gotten via other Compilation
     * controller so it is from other model.
     */
    static boolean getVariableElementAtDot( final JTextComponent component,
            final Object[] variable , final boolean showStatusOnError) 
    {
        
        JavaSource javaSource = JavaSource.forDocument(component.getDocument());
        if ( javaSource == null ){
            Toolkit.getDefaultToolkit().beep();
            return false;
        }
        try {
            javaSource.runUserActionTask(  new Task<CompilationController>(){
                public void run(CompilationController controller) throws Exception {
                    controller.toPhase( Phase.ELEMENTS_RESOLVED );
                    int dot = component.getCaret().getDot();
                    TreePath tp = controller.getTreeUtilities()
                        .pathFor(dot);
                    Element element = controller.getTrees().getElement(tp );
                    if ( element == null ){
                        StatusDisplayer.getDefault().setStatusText(
                                NbBundle.getMessage(
                                        WebBeansActionHelper.class, 
                                "LBL_ElementNotFound"));
                        return;
                    }
                    if ( !( element instanceof VariableElement) && showStatusOnError){
                        StatusDisplayer.getDefault().setStatusText(
                                NbBundle.getMessage(
                                WebBeansActionHelper.class, 
                                "LBL_NotVariableElement"));
                        return;
                    }
                    else {
                        if ( element.getKind() == ElementKind.FIELD ){
                            ElementHandle<VariableElement> handle = 
                                ElementHandle.create((VariableElement)element);
                            variable[0] = handle;
                            variable[1] = element.getSimpleName().toString();
                            variable[2] = InspectActionId.INJECTABLES;
                        }
                        else {
                            setVariablePath(variable, controller, element);
                        }
                    }
                }
            }, true );
        }
        catch(IOException e ){
            Logger.getLogger( GoToInjectableAtCaretAction.class.getName()).
                log( Level.WARNING, e.getMessage(), e);
        }
        return variable[1] !=null ;
    }
    
    static boolean getMethodAtDot(
            final JTextComponent component , final Object[] subject )
    {
        JavaSource javaSource = JavaSource.forDocument(component.getDocument());
        if ( javaSource == null ){
            Toolkit.getDefaultToolkit().beep();
            return false;
        }
        try {
            javaSource.runUserActionTask( new Task<CompilationController>(){
                public void run(CompilationController controller) throws Exception {
                    controller.toPhase( Phase.ELEMENTS_RESOLVED );
                    int dot = component.getCaret().getDot();
                    TreePath tp = controller.getTreeUtilities()
                        .pathFor(dot);
                    Element element = controller.getTrees().getElement(tp );
                    if ( element == null ){
                        StatusDisplayer.getDefault().setStatusText(
                                NbBundle.getMessage(
                                        WebBeansActionHelper.class, 
                                "LBL_ElementNotFound"));
                        return;
                    }
                    if ( element instanceof ExecutableElement ){
                        subject[0] = ElementHandle.create(element);
                        subject[1] =  element.getSimpleName();
                        subject[2] = InspectActionId.EVENTS;
                    }
                    else if ( element instanceof VariableElement ){
                        Element enclosingElement = element.getEnclosingElement();
                        if ( enclosingElement instanceof ExecutableElement){
                            List<? extends AnnotationMirror> annotations = 
                                controller.getElements().getAllAnnotationMirrors( 
                                    element);
                            for (AnnotationMirror annotationMirror : annotations) {
                                DeclaredType annotationType = 
                                    annotationMirror.getAnnotationType();
                                Element annotationElement = annotationType.asElement();
                                Name annotationName = ((TypeElement)annotationElement).
                                    getQualifiedName();
                                if ( OBSERVES_ANNOTATION.contentEquals( annotationName)){
                                    subject[0] = ElementHandle.create(enclosingElement);
                                    subject[1] =  enclosingElement.getSimpleName();
                                    subject[2] = InspectActionId.EVENTS;
                                    return;
                                }
                            }
                        }
                    }
                }
            }, true );
        }
        catch(IOException e ){
            Logger.getLogger( GoToInjectableAtCaretAction.class.getName()).
                log( Level.WARNING, e.getMessage(), e);
        }
                    
        return subject[0]!=null;
    }
    
    public static boolean getContextEventInjectionAtDot(
            final JTextComponent component, final Object[] variable )
    {
        try {
            ParserManager.parse(Collections.singleton (Source.create(
                    component.getDocument())), new UserTask() 
            {
                @Override
                public void run(ResultIterator resultIterator) throws Exception {
                    Result resuslt = resultIterator.getParserResult (component.getCaret().
                            getDot());
                    CompilationController controller = CompilationController.get(
                            resuslt);
                    if (controller == null || controller.toPhase(Phase.RESOLVED).
                            compareTo(Phase.RESOLVED) < 0)
                    {
                        return;
                    }
                    Token<JavaTokenId>[] token = new Token[1];
                    int[] span = getIdentifierSpan( component.getDocument(), 
                            component.getCaret().getDot(), token);

                    if (span == null) {
                        return ;
                    }

                    int exactOffset = controller.getSnapshot().
                        getEmbeddedOffset(span[0] + 1);
                    TreePath path = controller.getTreeUtilities().pathFor(exactOffset);
                    TreePath parent = path.getParentPath();
                    if (parent != null) {
                        Tree parentLeaf = parent.getLeaf();
                        if ( parentLeaf.getKind() == Kind.METHOD_INVOCATION){
                            ExpressionTree select = ((MethodInvocationTree)parentLeaf).
                                getMethodSelect();
                            /*
                             *  Identifier case should be ignored because in this case
                             *  method is called on 'this' instance . Which is never
                             *  managed by J2EE container as Event injectable.
                             */
                            if ( select.getKind() == Kind.MEMBER_SELECT ){
                                Scope scope = controller.getTrees().getScope(path);
                                Element subjectClass = scope.getEnclosingClass();
                                Element method = controller.getTrees().getElement(
                                        new TreePath(path, select));
                                Element caller = controller.getTrees().getElement(
                                        new TreePath(path, ((MemberSelectTree)select).getExpression()));
                                String methodName = method.getSimpleName().toString();
                                if ( FIRE.equals( methodName) && 
                                        method instanceof ExecutableElement  &&
                                        caller instanceof VariableElement )
                                {
                                    String variableName = caller.getSimpleName().toString();
                                    TypeElement enclosingTypeElement = 
                                        controller.getElementUtilities().
                                        enclosingTypeElement( method);
                                    String fqnMethodClass = enclosingTypeElement.
                                        getQualifiedName().toString();
                                    if( EVENT_INTERFACE.equals(fqnMethodClass)){
                                        List<VariableElement> fields = 
                                            ElementFilter.fieldsIn
                                            ( controller.getElements().getAllMembers(
                                                (TypeElement)subjectClass));
                                        for (VariableElement var :  fields) {
                                            String varName = var.getSimpleName().toString();
                                            if ( variableName.equals( varName )){
                                                ElementHandle<VariableElement> handle = 
                                                    ElementHandle.create(var);
                                                variable[0]= handle;
                                                variable[1]= varName;   
                                                variable[2]= InspectActionId.OBSERVERS;  
                                                return;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            });
        }
        catch (ParseException e) {
            throw new IllegalStateException(e);
        }
        return variable[1] !=null ;
    }
    
    static void showInjectablesDialog( MetadataModel<WebBeansModel> metamodel,
            WebBeansModel model, Object[] subject, 
            InjectablesModel uiModel , String name ) 
    {
        subject[2] = InspectActionId.INJECTABLES;
        StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(
                InjectablesModel.class, "LBL_WaitNode"));           // NOI18N
        JDialog dialog = ResizablePopup.getDialog();
        String title = NbBundle.getMessage(InspectInjectablesAtCaretAction.class,
                "TITLE_Injectables" , name );//NOI18N
        dialog.setTitle( title );
        dialog.setContentPane( new InjectablesPanel(subject, metamodel, model,
                uiModel ));
        dialog.setVisible( true );
    }
    
    static void showEventsDialog( MetadataModel<WebBeansModel> metaModel , 
            WebBeansModel model,Object[] subject, 
            EventsModel uiModel , String name ) 
    {
        subject[2] = InspectActionId.EVENTS;
        StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(
                InjectablesModel.class, "LBL_WaitNode"));                // NOI18N
        JDialog dialog = ResizablePopup.getDialog();
        String title = NbBundle.getMessage(InspectEventsAtCaretAction.class,
                "TITLE_Events" , name );//NOI18N
        dialog.setTitle( title );
        dialog.setContentPane( new EventsPanel(subject, metaModel, 
                model ,uiModel ));
        dialog.setVisible( true );
    }
    
    static void showObserversDialog( List<ExecutableElement> methods , 
            MetadataModel<WebBeansModel> metaModel , WebBeansModel model,
            Object[] subject, ObserversModel uiModel ,
            String name ) 
    {
        subject[2] = InspectActionId.OBSERVERS;
        StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(
                InjectablesModel.class, "LBL_WaitNode"));                // NOI18N
        JDialog dialog = ResizablePopup.getDialog();
        String title = NbBundle.getMessage(InspectObserversAtCaretAction.class,
                "TITLE_Observers" , name );//NOI18N
        dialog.setTitle( title );
        dialog.setContentPane( new ObserversPanel(subject, metaModel, 
                model ,uiModel ));
        dialog.setVisible( true );
        
    }
    
    public static VariableElement findVariable( final WebBeansModel model,
            final Object[] variablePath )
    {
        if ( variablePath[0] == null ){
            StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(
                    WebBeansActionHelper.class, 
                    "LBL_VariableNotFound", variablePath[1]));
            return null ;
        }
        Element element = ((ElementHandle<?>)variablePath[0]).resolve(
                model.getCompilationController());
        if ( element == null ){
            StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(
                    WebBeansActionHelper.class, 
                    "LBL_VariableNotFound", variablePath[1]));
            return null ;
        }
        VariableElement var = null;
        ExecutableElement method = null;
        if ( element.getKind() == ElementKind.FIELD){
            var = (VariableElement)element;
        }
        else {
            method = (ExecutableElement)element;
            List<? extends VariableElement> parameters = method.getParameters();
            for (VariableElement variableElement : parameters) {
                if (variableElement.getSimpleName().contentEquals(
                        variablePath[1].toString())) 
                {
                    var = variableElement;
                }
            }
        }
        
        if (var == null) {
            StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(
                    WebBeansActionHelper.class, 
                    "LBL_VariableNotFound", variablePath[1]));
        }
        return var;
    }

    static int[] getIdentifierSpan(Document doc, int offset, Token<JavaTokenId>[] token) {
        FileObject fileObject = NbEditorUtilities.getFileObject( doc);
        if (fileObject== null) {
            //do nothing if FO is not attached to the document - the goto would not work anyway:
            return null;
        }
        
        TokenHierarchy<?> th = TokenHierarchy.get(doc);
        TokenSequence<JavaTokenId> ts = SourceUtils.getJavaTokenSequence(th, offset);
        
        if (ts == null)
            return null;
        
        ts.move(offset);
        if (!ts.moveNext())
            return null;
        
        Token<JavaTokenId> t = ts.token();
        
        if (JavaTokenId.JAVADOC_COMMENT == t.id()) {
            return null;
        } else if (!USABLE_TOKEN_IDS.contains(t.id())) {
            ts.move(offset - 1);
            if (!ts.moveNext())
                return null;
            t = ts.token();
            if (!USABLE_TOKEN_IDS.contains(t.id()))
                return null;
        }
        
        if (token != null)
            token[0] = t;
        
        return new int [] {ts.offset(), ts.offset() + t.length()};
    }
    
    private static void setVariablePath( Object[] variableAtCaret,
            CompilationController controller, Element element )
    {
        Element parent = element.getEnclosingElement();
        if ( parent instanceof ExecutableElement ){
            ElementHandle<ExecutableElement> handle = ElementHandle.create( 
                    (ExecutableElement)parent ) ;
            variableAtCaret[0] = handle;
            variableAtCaret[1] = element.getSimpleName().toString();
            variableAtCaret[2] = InspectActionId.INJECTABLES;
        }
    }
}
