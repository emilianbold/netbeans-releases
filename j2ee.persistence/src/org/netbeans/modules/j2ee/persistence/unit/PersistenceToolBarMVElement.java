/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.j2ee.persistence.unit;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import javax.swing.Action;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.persistence.dd.persistence.model_1_0.Persistence;
import org.netbeans.modules.j2ee.persistence.dd.persistence.model_1_0.PersistenceUnit;
import org.netbeans.modules.j2ee.persistence.provider.InvalidPersistenceXmlException;
import org.netbeans.modules.j2ee.persistence.wizard.Util;
import org.netbeans.modules.j2ee.persistence.wizard.entity.WrapperPanel;
import org.netbeans.modules.j2ee.persistence.wizard.unit.PersistenceUnitWizardDescriptor;
import org.netbeans.modules.j2ee.persistence.wizard.unit.PersistenceUnitWizardPanel;
import org.netbeans.modules.j2ee.persistence.wizard.unit.PersistenceUnitWizardPanelDS;
import org.netbeans.modules.j2ee.persistence.provider.ProviderUtil;
import org.netbeans.modules.j2ee.persistence.wizard.unit.PersistenceUnitWizardPanelJdbc;
import org.netbeans.modules.xml.multiview.*;
import org.netbeans.modules.xml.multiview.ui.*;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.nodes.*;
import org.netbeans.modules.xml.multiview.Error;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 * Multiview element for persistence.xml.
 *
 * @author Martin Adamek
 * @author Erno Mononen
 */
public class PersistenceToolBarMVElement extends ToolBarMultiViewElement implements PropertyChangeListener {
    
    private ToolBarDesignEditor comp;
    private PersistenceView view;
    private PUDataObject puDataObject;
    private PersistenceUnitPanelFactory factory;
    private Action addAction, removeAction;
    private Project project;
    private boolean needInit = true;
    private RequestProcessor.Task repaintingTask;
    
    /** Creates a new instance of DesignMultiViewElement */
    public PersistenceToolBarMVElement(PUDataObject dObj) {
        super(dObj);
        this.puDataObject=dObj;
        this.project = FileOwnerQuery.getOwner(dObj.getPrimaryFile());
        addAction = new AddAction(NbBundle.getMessage(PersistenceToolBarMVElement.class,"LBL_Add"));
        removeAction = new RemoveAction(NbBundle.getMessage(PersistenceToolBarMVElement.class,"LBL_Remove"));
        
        comp = new ToolBarDesignEditor();
        factory=new PersistenceUnitPanelFactory(comp,dObj);
        setVisualEditor(comp);
        repaintingTask = RequestProcessor.getDefault().create(new Runnable() {
            public void run() {
                javax.swing.SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        repaintView();
                    }
                });
            }
        });
        
    }
    
    public SectionView getSectionView() {
        return view;
    }
    
    public void componentOpened() {
        super.componentOpened();
        dObj.addPropertyChangeListener(this);
    }
    
    public void componentClosed() {
        super.componentClosed();
        dObj.removePropertyChangeListener(this);
    }
    
    public void componentShowing() {
        super.componentShowing();
        if (needInit){
            needInit = !repaintView();
        }
    }

    /**
     * Tries to repaint the current view.
     * 
     * @return true if repainting succeeded, false otherwise.
     */ 
    private boolean repaintView(){
        
        view = new PersistenceView();
        
        if (!puDataObject.viewCanBeDisplayed()){
            view.setRoot(Node.EMPTY);
            comp.setContentView(view);
            return false;
        }
        
        view.initialize(puDataObject);
        comp.setContentView(view);
        Object lastActive = comp.getLastActive();
        if (lastActive!=null) {
            view.openPanel(lastActive);
        } else {
            Node initialNode = view.getPersistenceUnitsNode();
            Children ch = initialNode.getChildren();
            if (ch.getNodesCount()>0) {
                initialNode = ch.getNodes()[0];
            }
            view.selectNode(initialNode);
        }
        view.checkValidity();
        return true;
    }
    
    public void propertyChange(PropertyChangeEvent evt) {
        String name = evt.getPropertyName();
        
        if (puDataObject.PROPERTY_DATA_MODIFIED.equals(name) || puDataObject.PROPERTY_DATA_UPDATED.equals(name)){
            
            if (this.equals(puDataObject.getActiveMultiViewElement0())){
                repaintingTask.schedule(100);
            } else {
                needInit = true;
            }
        }
    }
    
    
    private class PersistenceView extends SectionView {
        
        private SectionContainer persistenceUnitsCont;
        private Node persistenceUnitsNode;
        
        public SectionContainer getPersistenceUnitsCont(){
            return persistenceUnitsCont;
        }
        public Node getPersistenceUnitsNode(){
            return persistenceUnitsNode;
        }
        
        PersistenceView(){
            super(factory);
        }

        /**
         * Initializes the view.
         * 
         * @param pudo the <code>PUDataObject</code> that should be used
         * for initializing this view. Must represent a parseable persistence.xml 
         * deployment descriptor file.
         */ 
        void initialize(PUDataObject pudo){
            
            Persistence persistence = pudo.getPersistence();
            
            PersistenceUnit[] persistenceUnits = persistence.getPersistenceUnit();
            Node[] persistenceUnitNode = new Node[persistenceUnits.length];
            Children ch = new Children.Array();
            for (int i=0;i<persistenceUnits.length;i++) {
                persistenceUnitNode[i] = new PersistenceUnitNode(persistenceUnits[i]);
            }
            ch.add(persistenceUnitNode);
            persistenceUnitsNode = new SectionContainerNode(ch);
            persistenceUnitsNode.setDisplayName(NbBundle.getMessage(PersistenceToolBarMVElement.class,"LBL_PersistenceUnits"));
            // add panels
            persistenceUnitsCont = new SectionContainer(this,persistenceUnitsNode,
                    NbBundle.getMessage(PersistenceToolBarMVElement.class,"LBL_PersistenceUnits"));
            persistenceUnitsCont.setHeaderActions(new javax.swing.Action[]{addAction});
            
            Children rootChildren = new Children.Array();
            rootChildren.add(new Node[]{persistenceUnitsNode});
            Node root = new AbstractNode(rootChildren);
            
            // creatings section panels for Chapters
            SectionPanel[] pan = new SectionPanel[persistenceUnits.length];
            for (int i=0; i < persistenceUnits.length; i++) {
                pan[i] = new SectionPanel(this, persistenceUnitNode[i],
                        persistenceUnitNode[i].getDisplayName(), persistenceUnits[i], false, false);
                pan[i].setHeaderActions(new javax.swing.Action[]{removeAction});
                persistenceUnitsCont.addSection(pan[i]);
            }
            addSection(persistenceUnitsCont);
            setRoot(root);
        }

        public Error validateView() {
            PersistenceValidator validator = new PersistenceValidator((PUDataObject)dObj);
            List<Error> result = validator.validate();
            if (!result.isEmpty()){
                return result.get(0);
            }
            return null;
        }
    }
    
    
    private class PersistenceUnitNode extends org.openide.nodes.AbstractNode {
        PersistenceUnitNode(PersistenceUnit persistenceUnit) {
            super(org.openide.nodes.Children.LEAF);
            setDisplayName(persistenceUnit.getName());
            setIconBaseWithExtension("org/netbeans/modules/j2ee/persistence/unit/PersistenceIcon.gif"); //NOI18N
        }
        
        public HelpCtx getHelpCtx() {
            return new HelpCtx(PUDataObject.HELP_ID_DESIGN_PERSISTENCE_UNIT); //NOI18N
        }
        
    }
    
    /**
     * Handles adding of a new Persistence Unit via multiview.
     */
    private class AddAction extends javax.swing.AbstractAction {
        
        AddAction(String actionName) {
            super(actionName);
        }
        
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            boolean isContainer = Util.isSupportedJavaEEVersion(project);
            final PersistenceUnitWizardPanel panel;
            if (isContainer) {
                panel = new PersistenceUnitWizardPanelDS(project, null, true);
            } else {
                panel = new PersistenceUnitWizardPanelJdbc(project, null, true);
            }
            
            final NotifyDescriptor nd = new NotifyDescriptor(
                    new WrapperPanel(panel),
                    NbBundle.getMessage(PersistenceToolBarMVElement.class, "LBL_NewPersistenceUnit"),
                    NotifyDescriptor.OK_CANCEL_OPTION,
                    NotifyDescriptor.PLAIN_MESSAGE,
                    null, null
                    );
            panel.addPropertyChangeListener(new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent evt) {
                    if (evt.getPropertyName().equals(PersistenceUnitWizardPanel.IS_VALID)) {
                        Object newvalue = evt.getNewValue();
                        if (newvalue != null && newvalue instanceof Boolean) {
                            validateUnitName(panel);
                            nd.setValid((Boolean) newvalue);
                            
                        }
                    }
                }
            });
            if (!panel.isValidPanel()) {
                validateUnitName(panel);
                nd.setValid(false);
            }
            Object result = DialogDisplayer.getDefault().notify(nd);
            
            if (result == NotifyDescriptor.OK_OPTION) {
                PersistenceUnit punit = new PersistenceUnit();
                
                if (isContainer) {
                    PersistenceUnitWizardPanelDS puPanel = (PersistenceUnitWizardPanelDS) panel;
                    if (puPanel.getDatasource() != null && !"".equals(puPanel.getDatasource().trim())){
                        if (puPanel.isJTA()) {
                            punit.setJtaDataSource(puPanel.getDatasource());
                        } else {
                            punit.setNonJtaDataSource(puPanel.getDatasource());
                            punit.setTransactionType("RESOURCE_LOCAL");
                        }
                    }
                    if (puPanel.isNonDefaultProviderEnabled()) {
                        punit.setProvider(puPanel.getNonDefaultProvider());
                    }
                } else {
                    PersistenceUnitWizardPanelJdbc puJdbc = (PersistenceUnitWizardPanelJdbc) panel;
                    punit = ProviderUtil.buildPersistenceUnit(puJdbc.getPersistenceUnitName(), puJdbc.getSelectedProvider(), puJdbc.getPersistenceConnection());
                    punit.setTransactionType("RESOURCE_LOCAL");
                    if (puJdbc.getPersistenceLibrary() != null){
                        Util.addLibraryToProject(project, puJdbc.getPersistenceLibrary());
                    }
                }
                
                punit.setName(panel.getPersistenceUnitName());
                ProviderUtil.setTableGeneration(punit, panel.getTableGeneration(), project);
                puDataObject.addPersistenceUnit(punit);
                comp.setLastActive(punit);
            }
        }
    }
    
    /**
     * Checks that given <code>panel</code>'s persistence unit's name is unique; if
     * not, sets an appropriate error message to the panel.
     */
    private void validateUnitName(PersistenceUnitWizardPanel panel){
        try{
            if (!panel.isNameUnique()){
                panel.setErrorMessage(NbBundle.getMessage(PersistenceUnitWizardDescriptor.class,"ERR_PersistenceUnitNameNotUnique"));
            } else {
                panel.setErrorMessage(null);
            }
        } catch (InvalidPersistenceXmlException ipx){
            panel.setErrorMessage(NbBundle.getMessage(PersistenceUnitWizardDescriptor.class,"ERR_InvalidPersistenceXml", ipx.getPath()));
        }
        
    }
    
    /**
     * Handles removing of a Persistence Unit.
     */
    private class RemoveAction extends javax.swing.AbstractAction {
        
        RemoveAction(String actionName) {
            super(actionName);
        }
        
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            SectionPanel sectionPanel = ((SectionPanel.HeaderButton)evt.getSource()).getSectionPanel();
            PersistenceUnit punit = (PersistenceUnit) sectionPanel.getKey();
            org.openide.DialogDescriptor desc = new ConfirmDialog(NbBundle.getMessage(PersistenceToolBarMVElement.class,"LBL_ConfirmRemove", punit.getName()));
            java.awt.Dialog dialog = org.openide.DialogDisplayer.getDefault().createDialog(desc);
            dialog.setVisible(true);
            if (org.openide.DialogDescriptor.OK_OPTION.equals(desc.getValue())) {
                sectionPanel.getSectionView().removeSection(sectionPanel.getNode());
                puDataObject.removePersistenceUnit(punit);
            }
        }
    }
}
