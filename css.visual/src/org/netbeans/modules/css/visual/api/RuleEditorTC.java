/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.css.visual.api;

import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.SwingUtilities;
import org.netbeans.modules.css.model.api.Model;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;

/**
 * Css Rule Editor {@link TopComponent}.
 *
 * One may manipulate the content of this component by obtaining an instance of
 * {@link RuleEditorController} via {@link #getRuleEditorController() } method.
 *
 * @see RuleEditorController
 *
 * @author mfukala@netbeans.org
 */
@TopComponent.Description(
        preferredID = RuleEditorTC.ID,
persistenceType = TopComponent.PERSISTENCE_ALWAYS,
iconBase = "org/netbeans/modules/css/visual/resources/css_rule.png") // NOI18N
@TopComponent.Registration(
        mode = "properties", // NOI18N
openAtStartup = false)
@ActionID(
        category = "Window", // NOI18N
id = "org.netbeans.modules.css.visual.api.RuleEditorTC.OpenAction") // NOI18N
@ActionReference(
        path = "Menu/Window/Navigator", // NOI18N
position = 900)
@TopComponent.OpenActionRegistration(
        displayName = "#CTL_RuleEditorAction", // NOI18N
preferredID = RuleEditorTC.ID)
@NbBundle.Messages({
    "CTL_RuleEditorAction=Rule Editor", // NOI18N
    "CTL_RuleEditorTC=Rule Editor {0}", // NOI18N
    "HINT_RuleEditorTC=This window is an editor of CSS rule properties" // NOI18N
})
public final class RuleEditorTC extends TopComponent {

    /**
     * TopComponent ID.
     */
    public static final String ID = "RuleEditorTC"; // NOI18N
    /**
     * Panel shown in this {@code TopComponent}.
     */
    private RuleEditorController controller;

    public RuleEditorTC() {
        initComponents();
        setFileNameInTitle(null);
        setToolTipText(Bundle.HINT_RuleEditorTC());
    }

    /**
     * Returns the default {@link RuleEditorController} associated with this
     * rule editor top component.
     */
    public RuleEditorController getRuleEditorController() {
        return controller;
    }

    private void setFileNameInTitle(FileObject file) {
        String fileName = file == null ? "" : " - " + file.getNameExt();
        setName(Bundle.CTL_RuleEditorTC(fileName));
    }
    
    /**
     * Initializes the components in this {@code TopComponent}.
     */
    private void initComponents() {
        setLayout(new BorderLayout());
        controller = RuleEditorController.createInstance();
        controller.addRuleEditorListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if(evt.getPropertyName().equals(RuleEditorController.PropertyNames.MODEL_SET.name())) {
                    Model model = (Model)evt.getNewValue();
                    FileObject file = model == null ? null : model.getLookup().lookup(FileObject.class);
                    setFileNameInTitle(file);
                }
            }
        });
        
        add(controller.getRuleEditorComponent(), BorderLayout.CENTER);
    }

}
