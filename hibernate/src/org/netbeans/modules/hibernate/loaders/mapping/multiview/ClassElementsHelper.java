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

import java.util.HashMap;
import java.util.Map;
import javax.swing.Action;
import org.netbeans.modules.hibernate.mapping.model.CompositeId;
import org.netbeans.modules.hibernate.mapping.model.Id;
import org.netbeans.modules.hibernate.mapping.model.MyClass;
import org.netbeans.modules.xml.multiview.ui.SectionContainer;
import org.netbeans.modules.xml.multiview.ui.SectionContainerNode;
import org.netbeans.modules.xml.multiview.ui.SectionPanel;
import org.netbeans.modules.xml.multiview.ui.SectionView;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

/**
 * This class helps to build the section for the class elements
 * 
 * @author Dongmei
 */
public class ClassElementsHelper {

    private SectionView view;
    private MyClass myClasses[];
    private Node classesContainerNode, classContainerNode;
    private SectionContainer classesContainer, classCont;
    private Action addClassAction,  removeClassAction;

    public ClassElementsHelper(SectionView view, MyClass[] myClasses) {
        this.view = view;
        this.myClasses = myClasses;


        addClassAction = new AddClassAction(NbBundle.getMessage(HibernateMappingToolBarMVElement.class, "LBL_Add"));
        removeClassAction = new RemoveClassAction(NbBundle.getMessage(HibernateMappingToolBarMVElement.class, "LBL_Remove"));

        // Build the section
        buildClassesSection();
    }

    public SectionContainer getClassesContainer() {
        return classesContainer;
    }

    public Node getClassesContainerNode() {
        return classesContainerNode;
    }

    private void buildClassesSection() {
        // Node for class | subclass | jointed-class | union-sublcass

        // Nodes for each class
        Node classNodes[] = new Node[myClasses.length];
        SectionContainer classContainers[] = new SectionContainer[myClasses.length];
        
        for (int i = 0; i < myClasses.length; i++) {
            
            buildClassSection(myClasses[i]);
            classNodes[i]= classContainerNode;
            classContainers[i] = classCont;
            
        }
        Children classesCh = new Children.Array();
        classesCh.add(classNodes);

        // Container Node for the classes 
        classesContainerNode = new SectionContainerNode(classesCh);
        classesContainerNode.setDisplayName(NbBundle.getMessage(HibernateMappingToolBarMVElement.class, "LBL_Classes"));
        classesContainer = new SectionContainer(view, classesContainerNode,
                NbBundle.getMessage(HibernateMappingToolBarMVElement.class, "LBL_Classes"));
        classesContainer.setHeaderActions(new javax.swing.Action[]{addClassAction});
        
        for (int i = 0; i < myClasses.length; i++) {
            classesContainer.addSection(classContainers[i]);
        }

    }
    
    private void buildClassSection( MyClass cls ) {
        
        String clsName = cls.getAttributeValue("Name");
        
        // Node for id or composite-id
        Node idNode = new HibernateMappingToolBarMVElement.ElementLeafNode("Id"); // TODO I18N
        SectionPanel idPanel = new SectionPanel(view, idNode, idNode.getDisplayName(), cls.getId(), false, false );
        
        if( cls.getId() != null )
            buildIdSection( cls.getId() );
        else
            buildCompositeIdSection( cls.getCompositeId() );
        
        
        // Node for other elements later
        Children classCh = new Children.Array();
        classCh.add( new Node[] {idNode } );
        classContainerNode = new SectionContainerNode(classCh);
        classContainerNode.setDisplayName( clsName );
        
        classCont = new SectionContainer(view, classContainerNode, clsName );
        classCont.addSection(idPanel);
    }
    
    private void buildIdSection(Id clsId){
        // Node for meta
        
        // Node for column
        
        // Node for type
        
        // Node for generator
        
        
    }
    
    private void buildCompositeIdSection(CompositeId clsId) {
        // Node for meta
        
        // Container node for key-property
        
        // Container node for key-many-to-one
    }

    /**
     * For adding a new class element in the configuration
     */
    private class AddClassAction extends javax.swing.AbstractAction {

        AddClassAction(String actionName) {
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
    private class RemoveClassAction extends javax.swing.AbstractAction {

        RemoveClassAction(String actionName) {
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
