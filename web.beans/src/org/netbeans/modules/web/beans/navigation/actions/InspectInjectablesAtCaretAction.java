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
package org.netbeans.modules.web.beans.navigation.actions;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.VariableElement;
import javax.swing.JDialog;
import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;

import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.editor.BaseAction;
import org.netbeans.editor.ext.ExtKit;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelException;
import org.netbeans.modules.web.beans.MetaModelSupport;
import org.netbeans.modules.web.beans.api.model.InjectionPointDefinitionError;
import org.netbeans.modules.web.beans.api.model.Result;
import org.netbeans.modules.web.beans.api.model.WebBeansModel;
import org.netbeans.modules.web.beans.navigation.InjectablesModel;
import org.netbeans.modules.web.beans.navigation.InjectablesPanel;
import org.netbeans.modules.web.beans.navigation.ResizablePopup;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;


/**
 * @author ads
 *
 */
public final class InspectInjectablesAtCaretAction extends BaseAction {

    private static final long serialVersionUID = 1857528107859448216L;
    
    private static final String INSPECT_INJACTABLES_AT_CARET =
        "inspect-injactables-at-caret";                     // NOI18N
    
    private static final String INSPECT_INJACTABLES_AT_CARET_POPUP =
        "inspect-injactables-at-caret-popup";               // NOI18N

    public InspectInjectablesAtCaretAction() {
        super(NbBundle.getMessage(InspectInjectablesAtCaretAction.class, 
                INSPECT_INJACTABLES_AT_CARET), 0);
        
        putValue(ACTION_COMMAND_KEY, INSPECT_INJACTABLES_AT_CARET);
        putValue(SHORT_DESCRIPTION, getValue(NAME));
        putValue(ExtKit.TRIMMED_TEXT,getValue(NAME));
        putValue(POPUP_MENU_TEXT, NbBundle.getMessage(
                InspectInjectablesAtCaretAction.class,
                INSPECT_INJACTABLES_AT_CARET_POPUP));

        putValue("noIconInMenu", Boolean.TRUE); // NOI18N*/
    }


    /* (non-Javadoc)
     * @see org.netbeans.editor.BaseAction#actionPerformed(java.awt.event.ActionEvent, javax.swing.text.JTextComponent)
     */
    @Override
    public void actionPerformed( ActionEvent event, final JTextComponent component ) {
        if ( component == null ){
            Toolkit.getDefaultToolkit().beep();
            return;
        }
        final FileObject fileObject = NbEditorUtilities.getFileObject( 
                component.getDocument());
        if ( fileObject == null ){
            Toolkit.getDefaultToolkit().beep();
            return;
        }
        Project project = FileOwnerQuery.getOwner( fileObject );
        if ( project == null ){
            Toolkit.getDefaultToolkit().beep();
            return;
        }
        MetaModelSupport support = new MetaModelSupport(project);
        final MetadataModel<WebBeansModel> metaModel = support.getMetaModel();
        if ( metaModel == null ){
            Toolkit.getDefaultToolkit().beep();
            return;
        }
        
        /*
         *  this list will contain variable element name and TypeElement 
         *  qualified name which contains variable element. 
         */
        final Object[] variableAtCaret = new Object[2];
        if ( !WebBeansActionHelper.getVariableElementAtDot( component, 
                variableAtCaret , true  ))
        {
            return;
        }
        
        try {
            metaModel.runReadAction( new MetadataModelAction<WebBeansModel, Void>() {

                public Void run( WebBeansModel model ) throws Exception {
                    inspectInjectables(component, fileObject, 
                            model , metaModel, variableAtCaret );
                    return null;
                }
            });
        }
        catch (MetadataModelException e) {
            Logger.getLogger( InspectInjectablesAtCaretAction.class.getName()).
                log( Level.WARNING, e.getMessage(), e);
        }
        catch (IOException e) {
            Logger.getLogger( InspectInjectablesAtCaretAction.class.getName()).
                log( Level.WARNING, e.getMessage(), e);
        }
    }
    
    /* (non-Javadoc)
     * @see javax.swing.AbstractAction#isEnabled()
     */
    @Override
    public boolean isEnabled() {
        return WebBeansActionHelper.isEnabled();
    }
    
    
    /* (non-Javadoc)
     * @see org.netbeans.editor.BaseAction#asynchonous()
     */
    @Override
    protected boolean asynchonous() {
        return true;
    }
    
    /**
     * Variable element is resolved based on containing type element 
     * qualified name and simple name of variable itself.
     * Model methods are used further for injectable resolution.   
     */
    private void inspectInjectables( final JTextComponent component,
            final FileObject fileObject, final WebBeansModel model,
            final MetadataModel<WebBeansModel> metaModel,
            final Object[] variablePath )
    {
        VariableElement var = WebBeansActionHelper.findVariable(model, variablePath);
        if (var == null) {
            return;
        }
        try {
            if (!model.isInjectionPoint(var)) {
                StatusDisplayer.getDefault().setStatusText(
                        NbBundle.getMessage(GoToInjectableAtCaretAction.class,
                                "LBL_NotInjectionPoint"), // NOI18N
                        StatusDisplayer.IMPORTANCE_ERROR_HIGHLIGHT);
                return;
            }
        }
        catch (InjectionPointDefinitionError e) {
            StatusDisplayer.getDefault().setStatusText(e.getMessage(),
                    StatusDisplayer.IMPORTANCE_ERROR_HIGHLIGHT);
        }
        final Result result = model.getInjectable(var, null);
        if (result == null) {
            StatusDisplayer.getDefault().setStatusText(
                    NbBundle.getMessage(GoToInjectableAtCaretAction.class,
                            "LBL_InjectableNotFound"), // NOI18N
                    StatusDisplayer.IMPORTANCE_ERROR_HIGHLIGHT);
            return;
        }
        if (result instanceof Result.Error) {
            StatusDisplayer.getDefault().setStatusText(
                    ((Result.Error) result).getMessage(),
                    StatusDisplayer.IMPORTANCE_ERROR_HIGHLIGHT);
        }
        if (result.getKind() == Result.ResultKind.DEFINITION_ERROR) {
            return;
        }
        final CompilationController controller = model
                .getCompilationController();
        final List<AnnotationMirror> bindings = model.getQualifiers(var);
        if (SwingUtilities.isEventDispatchThread()) {
            showDialog(result, bindings, controller, metaModel);
        }
        else {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    showDialog(result, bindings, controller, metaModel);
                }
            });
        }
    }

    private void showDialog( Result result , List<AnnotationMirror> bindings , 
            CompilationController controller, MetadataModel<WebBeansModel> model ) 
    {
        StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(
                InjectablesModel.class, "LBL_WaitNode"));
        JDialog dialog = ResizablePopup.getDialog();
        String title = NbBundle.getMessage(InspectInjectablesAtCaretAction.class,
                "TITLE_Injectables" , result.getVariable().getSimpleName().toString() );
        dialog.setTitle( title );
        dialog.setContentPane( new InjectablesPanel(result, bindings, 
                controller , model));
        dialog.setVisible( true );
    }

}
