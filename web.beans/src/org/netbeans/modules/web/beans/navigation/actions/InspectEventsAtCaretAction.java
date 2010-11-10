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

import java.util.List;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;

import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.web.beans.api.model.WebBeansModel;
import org.netbeans.modules.web.beans.navigation.EventsModel;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;


/**
 * @author ads
 *
 */
public class InspectEventsAtCaretAction extends AbstractEventAction {
    
    private static final long serialVersionUID = -4645226875434261917L;

    private static final String INSPECT_EVENTS_AT_CARET =
        "inspect-events-at-caret";                     // NOI18N
    
    private static final String INSPECT_EVENTS_AT_CARET_POPUP =
        "inspect-events-at-caret-popup";               // NOI18N

    public InspectEventsAtCaretAction( ) {
        super(NbBundle.getMessage(InspectEventsAtCaretAction.class, 
                INSPECT_EVENTS_AT_CARET));
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.navigation.actions.AbstractWebBeansAction#getActionCommand()
     */
    @Override
    protected String getActionCommand() {
        return INSPECT_EVENTS_AT_CARET;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.navigation.actions.AbstractWebBeansAction#getPopupMenuKey()
     */
    @Override
    protected String getPopupMenuKey() {
        return INSPECT_EVENTS_AT_CARET_POPUP;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.navigation.actions.AbstractWebBeansAction#modelAcessAction(org.netbeans.modules.web.beans.api.model.WebBeansModel, org.netbeans.modules.j2ee.metadata.model.api.MetadataModel, java.lang.Object[], javax.swing.text.JTextComponent, org.openide.filesystems.FileObject)
     */
    @Override
    protected void modelAcessAction( WebBeansModel model,
            final MetadataModel<WebBeansModel> metaModel, final Object[] subject,
            JTextComponent component, FileObject fileObject )
    {
        final Object handle = subject[0];
        if ( handle == null ){
            return;
        }
        Element element = ((ElementHandle<?>)handle).resolve( 
                model.getCompilationController());
        ExecutableElement method = (ExecutableElement)element;
        if ( model.getObserverParameter( method ) == null ){
            StatusDisplayer.getDefault().setStatusText(
                    NbBundle.getMessage(GoToInjectableAtCaretAction.class,
                            "LBL_NotObserverContext"), // NOI18N
                    StatusDisplayer.IMPORTANCE_ERROR_HIGHLIGHT);
            return;
        }
        List<VariableElement> eventInjectionPoints = model.getEventInjectionPoints( 
                method , null );
        CompilationController controller = model
            .getCompilationController();
        final EventsModel uiModel = new EventsModel(eventInjectionPoints, 
                controller, metaModel);
        final String name = method.getSimpleName().toString();
        if (SwingUtilities.isEventDispatchThread()) {
            WebBeansActionHelper.showEventsDialog( metaModel, model, 
                    subject , uiModel , name );
        }
        else {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    WebBeansActionHelper.showEventsDialog(metaModel, null , 
                            subject ,uiModel , name );
                }
            });
        }
    }

}
