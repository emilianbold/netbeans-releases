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
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import javax.swing.text.JTextComponent;

import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.beans.api.model.WebBeansModel;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

import com.sun.source.util.TreePath;

/**
 * @author ads
 *
 */
class WebBeansActionHelper {

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
     * access. I believe this is because element was gotten via other Compilation
     * controller so it is from other model.
     * As result this trick is used.  
     */
    static boolean getVariableElementAtDot( final JTextComponent component,
            final Object[] variable ) 
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
                                GoToInjectableAtCaretAction.class, 
                                "LBL_ElementNotFound"));
                        return;
                    }
                    if ( !( element instanceof VariableElement) ){
                        StatusDisplayer.getDefault().setStatusText(
                                NbBundle.getMessage(
                                GoToInjectableAtCaretAction.class, 
                                "LBL_NotVariableElement"));
                        return;
                    }
                    else {
                        if ( element.getKind() == ElementKind.FIELD ){
                            ElementHandle<VariableElement> handle = 
                                ElementHandle.create((VariableElement)element);
                            variable[0] = handle;
                            variable[1] = element.getSimpleName().toString();
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
    
    private static void setVariablePath( Object[] variableAtCaret,
            CompilationController controller, Element element )
    {
        Element parent = element.getEnclosingElement();
        if ( parent instanceof ExecutableElement ){
            ElementHandle<ExecutableElement> handle = ElementHandle.create( 
                    (ExecutableElement)parent ) ;
            variableAtCaret[0] = handle;
            variableAtCaret[1] = element.getSimpleName().toString();
        }
    }
    
    static VariableElement findVariable( final WebBeansModel model,
            final Object[] variablePath )
    {
        if ( variablePath[0] == null ){
            StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(
                    InspectInjectablesAtCaretAction.class, 
                    "LBL_VariableNotFound", variablePath[1]));
            return null ;
        }
        Element element = ((ElementHandle<?>)variablePath[0]).resolve(
                model.getCompilationController());
        if ( element == null ){
            StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(
                    InspectInjectablesAtCaretAction.class, 
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
                    InspectInjectablesAtCaretAction.class, 
                    "LBL_VariableNotFound", variablePath[1]));
        }
        return var;
    }
}
