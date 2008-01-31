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
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.hibernate.loaders.mapping.HibernateMappingDataObject;
import org.netbeans.modules.hibernate.mapping.model.HibernateMapping;
import org.netbeans.modules.hibernate.mapping.model.MyClass;
import org.netbeans.modules.xml.multiview.ToolBarMultiViewElement;
import org.netbeans.modules.xml.multiview.ui.SectionView;
import org.netbeans.modules.xml.multiview.ui.ToolBarDesignEditor;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.netbeans.modules.xml.multiview.Error;
import org.netbeans.modules.xml.multiview.ui.SectionContainer;
import org.netbeans.modules.xml.multiview.ui.SectionContainerNode;
import org.netbeans.modules.xml.multiview.ui.SectionPanel;
import org.openide.util.NbBundle;

/**
 * ToolBarMultiView for Hibernate Configuration file
 * 
 * @author Dongmei Cao
 */
public class HibernateMappingToolBarMVElement extends ToolBarMultiViewElement {

    public static final String PROPERTIES = "Properties";
    public static final String JDBC_PROPS = "JDBC Properties";
    public static final String DATASOURCE_PROPS = "Datasource Properties";
    public static final String OPTIONAL_PROPS = "Optional Properties";
    public static final String CONFIGURATION_PROPS = "Configuration Properties";
    public static final String JDBC_CONNECTION_PROPS = "JDBC and Connection Properties";
    public static final String CACHE_PROPS = "Cache Properties";
    public static final String TRANSACTION_PROPS = "Transaction Properties";
    public static final String MISCELLANEOUS_PROPS = "Miscellaneous Properties";
    public static final String MAPPINGS = "Mappings";
    public static final String CLASS_CACHE = "Class Cache";
    public static final String COLLECTION_CACHE = "Collection Cache";
    public static final String CACHE = "Cache";
    public static final String EVENTS = "Events";
    public static final String EVENT = "Event";
    public static final String SECURITY = "Security";
    private MappingView view;
    private ToolBarDesignEditor comp;
    private HibernateMappingDataObject mappingDataObject;
    private HibernateMappingPanelFactory factory;
    private Project project;
    private Action addClassAction, removeClassAction;

    public HibernateMappingToolBarMVElement(HibernateMappingDataObject dObj) {
        super(dObj);
        this.mappingDataObject = dObj;
        this.project = FileOwnerQuery.getOwner(dObj.getPrimaryFile());
        addClassAction = new AddClassAction(NbBundle.getMessage(HibernateMappingToolBarMVElement.class, "LBL_Add"));
        removeClassAction = new RemoveClassAction(NbBundle.getMessage(HibernateMappingToolBarMVElement.class, "LBL_Remove"));

        comp = new ToolBarDesignEditor();
        factory = new HibernateMappingPanelFactory(comp, dObj);
        setVisualEditor(comp);

    /*repaintingTask = RequestProcessor.getDefault().create(new Runnable() {
    public void run() {
    javax.swing.SwingUtilities.invokeLater(new Runnable() {
    public void run() {
    repaintView();
    }
    });
    }
    });*/
    }

    public SectionView getSectionView() {
        return view;
    }

    @Override
    public void componentOpened() {
        super.componentOpened();

    }

    @Override
    public void componentClosed() {
        super.componentClosed();

    }

    @Override
    public void componentShowing() {
        super.componentShowing();

        // TODO: can have more logic to handle when the view can not be displayed. See Persistence
        view = new MappingView(mappingDataObject);
        view.initialize();
        comp.setContentView(view);

        Object lastActive = comp.getLastActive();
        if (lastActive != null) {
            view.openPanel(lastActive);
        } else {
            // Expand the first node in session factory if there is one
            Node childrenNodes[] = view.getClassesContainerNode().getChildren().getNodes();
            if (childrenNodes.length > 0) {
                view.selectNode(childrenNodes[0]);
            }
        }

        view.checkValidity();

    }

    private class MappingView extends SectionView {

        private HibernateMappingDataObject mappingDataObject;
        
        private Node classesContainerNode;
        
        private SectionContainer classesCont;

        public SectionContainer getClassesContainer() {
            return classesCont;
        }

        public Node getClassesContainerNode() {
            return classesContainerNode;
        }

        MappingView(HibernateMappingDataObject dObj) {
            super(factory);
            mappingDataObject = dObj;
        }

        /**
         * Initialize the view
         */
        void initialize() {

            HibernateMapping mapping = mappingDataObject.getHibernateMapping();

            // Node meta
            // Node typedef
            // Node import

            // Node for class | subclass | jointed-class | union-sublcass
            MyClass[] myClasses = mapping.getMyClass();

            // Nodes for each class
            Node classNodes[] = new Node[myClasses.length];
            for (int i = 0; i < myClasses.length; i++) {
                 // Use the class Name as the node display name
                String name = myClasses[i].getAttributeValue("Name"); // NOI18N
                classNodes[i] = new ElementLeafNode(name);
            }
            Children classesCh = new Children.Array();
            classesCh.add(classNodes);
            
            // Container Node for the classes 
            classesContainerNode = new SectionContainerNode(classesCh);
            classesContainerNode.setDisplayName(NbBundle.getMessage(HibernateMappingToolBarMVElement.class, "LBL_Classes"));
            classesCont = new SectionContainer(this, classesContainerNode,
                    NbBundle.getMessage(HibernateMappingToolBarMVElement.class, "LBL_Classes"));
            classesCont.setHeaderActions(new javax.swing.Action[]{addClassAction});
            SectionPanel classPanels[] = new SectionPanel[myClasses.length];
            for (int i = 0; i < myClasses.length; i++) {
                classPanels[i] = new SectionPanel(this, classNodes[i], classNodes[i].getDisplayName(), myClasses[i], false, false);
                classPanels[i].setHeaderActions(new javax.swing.Action[]{removeClassAction});
                classesCont.addSection(classPanels[i]);
            }
            

            // Node resultset
            // Node for query | sql-query
            // Node filter-def
            // Node for database-object

            Children rootChildren = new Children.Array();
            rootChildren.add(new Node[]{classesContainerNode});
            Node root = new AbstractNode(rootChildren);
            
             // Add sections for the nodes
            addSection(classesCont);
            
            setRoot(root);

        }

        @Override
        public Error validateView() {
            // TODO: valiation code here
            return null;
        }
    }

    private class ElementLeafNode extends org.openide.nodes.AbstractNode {

        ElementLeafNode(String displayName) {
            super(org.openide.nodes.Children.LEAF);
            setDisplayName(displayName);
        }

        @Override
        public HelpCtx getHelpCtx() {
            //return new HelpCtx(HibernateMappingDataObject.HELP_ID_DESIGN_HIBERNATE_MAPPING); //NOI18N
            return null;
        }
    }

    /**
     * For adding a new event in the configuration
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
     * For removing an event from the configuration
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
