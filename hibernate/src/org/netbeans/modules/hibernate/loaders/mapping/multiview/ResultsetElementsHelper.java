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
import org.netbeans.modules.hibernate.mapping.model.Resultset;
import org.netbeans.modules.hibernate.mapping.model.Return;
import org.netbeans.modules.xml.multiview.ui.SectionContainer;
import org.netbeans.modules.xml.multiview.ui.SectionContainerNode;
import org.netbeans.modules.xml.multiview.ui.SectionPanel;
import org.netbeans.modules.xml.multiview.ui.SectionView;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

/**
 * This class helps to build the section for the resultset elements
 *  
 * @author Dongmei Cao
 */
public class ResultsetElementsHelper {

    private SectionView view;
    private Resultset resultsets[];
    private Node resultsetsContainerNode;
    private SectionContainer resultsetsContianer;
    private Action addResultsetAction, removeResultsetAction;

    public ResultsetElementsHelper(SectionView view, Resultset sets[]) {
        this.view = view;
        this.resultsets = sets;
        
        addResultsetAction = new AddResultsetAction(NbBundle.getMessage(HibernateMappingToolBarMVElement.class, "LBL_Add"));
        removeResultsetAction = new RemoveResultsetAction(NbBundle.getMessage(HibernateMappingToolBarMVElement.class, "LBL_Remove"));
        
        // Build the node structure
        buildResultsetsSection();
    }

    public Node getResultsetsContainerNode() {
        return resultsetsContainerNode;
    }

    public SectionContainer getResultsetsContianer() {
        return resultsetsContianer;
    }

    private void buildResultsetsSection() {

        // Container Nodes for each resultset
        SectionContainerNode resultsetContainerNodes[] = new SectionContainerNode[resultsets.length];
        SectionContainer resultsetContainers[] = new SectionContainer[resultsets.length];
        for (int i = 0; i < resultsets.length; i++) {
            // Use the resultset name as the node display name
            String name = resultsets[i].getAttributeValue("Name"); // NO I18N


            // Node for return-scalar
            Node returnScalarNode = new HibernateMappingToolBarMVElement.ElementLeafNode("Return Scalar"); // TODO
            SectionPanel returnScalarSectionPanel = new SectionPanel(view, returnScalarNode, returnScalarNode.getDisplayName(), resultsets[i], false, false);

            // Nodes for return elements
            Return returns[] = resultsets[i].getReturn();
            Node returnNodes[] = new Node[returns.length];
            for( int ri = 0; ri < returns.length; ri ++ ) {
                // Use the alias as the display name
                String alias = returns[ri].getAttributeValue("Alias");
                returnNodes[ri] = new HibernateMappingToolBarMVElement.ElementLeafNode(alias);
            }
            Children returnCh = new Children.Array();
            returnCh.add( returnNodes);
            SectionContainerNode returnsContainerNode = new SectionContainerNode(returnCh);
            returnsContainerNode.setDisplayName("Returns"); // TODO: I18N
            SectionContainer returnsCont = new SectionContainer(view, returnsContainerNode, "Returns" ); // TODO: I18N
            //returnsCont.addHeaderAction(new javax.swing.Action[]{removeResultsetAction});
            for( int ri = 0; ri < returns.length; ri ++ ) {
                // Use the alias as the display name
                SectionPanel reutrnPanel = new SectionPanel(view, returnNodes[ri], returnNodes[ri].getDisplayName(), returns[ri], false, false);
                returnsCont.addSection( reutrnPanel );
            }
            
            // Node for return join
            //Node returnJoin = new ElementLeafNode( "Return Join" );
            
            // Node for load collection
            //Node loadCollection = new ElementLeafNode( "Load Collection" );

            Children resultsetCh = new Children.Array();
            resultsetCh.add(new Node[]{returnScalarNode, returnsContainerNode});
            resultsetContainerNodes[i] = new SectionContainerNode(resultsetCh);
            resultsetContainerNodes[i].setDisplayName(name);
            resultsetContainers[i] = new SectionContainer(view, resultsetContainerNodes[i], name);
            resultsetContainers[i].setHeaderActions(new javax.swing.Action[]{removeResultsetAction});
            resultsetContainers[i].addSection(returnScalarSectionPanel);
            resultsetContainers[i].addSection(returnsCont);

        }
        Children resultsetsCh = new Children.Array();
        resultsetsCh.add(resultsetContainerNodes);

        // Container node for the resultsets
        resultsetsContainerNode = new SectionContainerNode(resultsetsCh);
        resultsetsContainerNode.setDisplayName("Resultsets"); // TODO: I18N
        resultsetsContianer = new SectionContainer(view, resultsetsContainerNode, "Resultsets"); // TODO: I18N
        resultsetsContianer.setHeaderActions( new Action[] {addResultsetAction});

        for (int i = 0; i < resultsets.length; i++) {
            resultsetsContianer.addSection(resultsetContainers[i]);
        }
    }
    
    /**
     * For adding a new class element in the configuration
     */
    private class AddResultsetAction extends javax.swing.AbstractAction {

        AddResultsetAction(String actionName) {
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
    private class RemoveResultsetAction extends javax.swing.AbstractAction {

        RemoveResultsetAction(String actionName) {
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
