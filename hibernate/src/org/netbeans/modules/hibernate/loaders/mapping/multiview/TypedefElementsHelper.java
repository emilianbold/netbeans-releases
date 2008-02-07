/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.hibernate.loaders.mapping.multiview;

import javax.swing.Action;
import org.netbeans.modules.hibernate.mapping.model.Typedef;
import org.netbeans.modules.xml.multiview.ui.SectionContainer;
import org.netbeans.modules.xml.multiview.ui.SectionContainerNode;
import org.netbeans.modules.xml.multiview.ui.SectionPanel;
import org.netbeans.modules.xml.multiview.ui.SectionView;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

/**
 * This class helps to build the section for the typedef elements
 * 
 * @author Dongmei Cao
 */
public class TypedefElementsHelper {

    private SectionView view;
    private Typedef typedefs[];
    private Node typedefsContainerNode;
    private SectionContainer typedefsContainer;
    private Action addTypedefAction, removeTypedefAction;

    public TypedefElementsHelper(SectionView view, Typedef[] defs) {
        this.view = view;
        this.typedefs = defs;
        
        addTypedefAction = new AddTypedefAction(NbBundle.getMessage(HibernateMappingToolBarMVElement.class, "LBL_Add"));
        removeTypedefAction = new RemoveTypedefAction(NbBundle.getMessage(HibernateMappingToolBarMVElement.class, "LBL_Remove"));

        // Build the section
        buildTypedefsSection();
    }

    public SectionContainer getTypedefsContainer() {
        return typedefsContainer;
    }

    public Node getTypedefsContainerNode() {
        return typedefsContainerNode;
    }

    private void buildTypedefsSection() {
        // Node for each typedef
        Node typedefNodes[] = new Node[typedefs.length];
        for (int i = 0; i < typedefs.length; i++) {
            // Use the name as the displayname
            String name = typedefs[i].getAttributeValue("Name");
            String className = typedefs[i].getAttributeValue("Class");
            typedefNodes[i] = new HibernateMappingToolBarMVElement.ElementLeafNode(name);
           //SectionPanel typedefSectionPanel = new SectionPanel(this, metaNode, metaNode.getDisplayName(), mapping.getTypedef(0), false, false);
        }
        Children typedefsCh = new Children.Array();
        typedefsCh.add(typedefNodes);

        // Container node for all the typedefs
        typedefsContainerNode = new SectionContainerNode(typedefsCh);
        typedefsContainerNode.setDisplayName("Type Definitions"); // TODO: I18N
        typedefsContainer = new SectionContainer(view, typedefsContainerNode, "Type Definitions"); // TODO: I18N
        typedefsContainer.setHeaderActions( new Action[] {addTypedefAction});
        SectionPanel typedefPanels[] = new SectionPanel[typedefs.length];
        for (int i = 0; i < typedefs.length; i++) {
            typedefPanels[i] = new SectionPanel(view, typedefNodes[i], typedefNodes[i].getDisplayName(), typedefs[i], false, false);
            typedefPanels[i].setHeaderActions(new javax.swing.Action[]{removeTypedefAction});
            typedefsContainer.addSection(typedefPanels[i]);
        }
    }
    
    /**
     * For adding a new class element in the configuration
     */
    private class AddTypedefAction extends javax.swing.AbstractAction {

        AddTypedefAction(String actionName) {
            super(actionName);
        }

        public void actionPerformed(java.awt.event.ActionEvent evt) {

        /*NewEventPanel dialogPanel = new NewEventPanel();
        EditDialog dialog = new EditDialog(dialogPanel, NbBundle.getMessage(HibernateMappingToolBarMVElement.class, "LBL_Event"), true) {
        protected String validate() {
        // Nothing to validate
        return null;
        }
        };
        java.awt.Dialog d = org.openide.DialogDisplayer.getDefault().createDialog(dialog);
        d.setVisible(true);
        if (dialog.getValue().equals(EditDialog.OK_OPTION)) {
        String eventType = dialogPanel.getEventType();
        Event event = new Event();
        event.setAttributeValue("Type", eventType);
        configDataObject.getHibernateConfiguration().getSessionFactory().addEvent(event);
        configDataObject.modelUpdatedFromUI();
        ConfigurationView view = (ConfigurationView) comp.getContentView();
        Node eventNode = new ElementLeafNode(eventType);
        view.getEventsContainerNode().getChildren().add(new Node[]{eventNode});
        SectionPanel pan = new SectionPanel(view, eventNode, eventNode.getDisplayName(), event, false, false);
        pan.setHeaderActions(new javax.swing.Action[]{removeEventAction});
        view.getEventsContainer().addSection(pan, true);
        }*/
        }
    }

    /**
     * For removing a class elment from the configuration
     */
    private class RemoveTypedefAction extends javax.swing.AbstractAction {

        RemoveTypedefAction(String actionName) {
            super(actionName);
        }

        public void actionPerformed(java.awt.event.ActionEvent evt) {

        /*SectionPanel sectionPanel = ((SectionPanel.HeaderButton) evt.getSource()).getSectionPanel();
        Event event = (Event) sectionPanel.getKey();
        org.openide.DialogDescriptor desc = new ConfirmDialog(NbBundle.getMessage(HibernateMappingToolBarMVElement.class,
        "TXT_Remove_Event",
        event.getAttributeValue("Type"))); // NOI18N
        java.awt.Dialog dialog = org.openide.DialogDisplayer.getDefault().createDialog(desc);
        dialog.setVisible(true);
        if (org.openide.DialogDescriptor.OK_OPTION.equals(desc.getValue())) {
        sectionPanel.getSectionView().removeSection(sectionPanel.getNode());
        configDataObject.getHibernateConfiguration().getSessionFactory().removeEvent(event);
        configDataObject.modelUpdatedFromUI();
        }*/
        }
    }

}
