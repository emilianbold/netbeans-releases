/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
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
package org.netbeans.modules.j2ee.persistence.unit;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import javax.swing.Action;
import org.netbeans.api.db.explorer.JDBCDriver;
import org.netbeans.api.db.explorer.JDBCDriverManager;
import org.netbeans.api.java.classpath.JavaClassPathConstants;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.modules.j2ee.persistence.dd.common.Persistence;
import org.netbeans.modules.j2ee.persistence.dd.common.PersistenceUnit;
import org.netbeans.modules.j2ee.persistence.provider.InvalidPersistenceXmlException;
import org.netbeans.modules.j2ee.persistence.provider.Provider;
import org.netbeans.modules.j2ee.persistence.wizard.Util;
import org.netbeans.modules.j2ee.persistence.wizard.entity.WrapperPanel;
import org.netbeans.modules.j2ee.persistence.wizard.unit.PersistenceUnitWizardDescriptor;
import org.netbeans.modules.j2ee.persistence.wizard.unit.PersistenceUnitWizardPanel;
import org.netbeans.modules.j2ee.persistence.wizard.unit.PersistenceUnitWizardPanelDS;
import org.netbeans.modules.j2ee.persistence.provider.ProviderUtil;
import org.netbeans.modules.j2ee.persistence.wizard.library.PersistenceLibrarySupport;
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
            @Override
            public void run() {
                javax.swing.SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        repaintView();
                    }
                });
            }
        });
        
    }
    
    @Override
    public SectionView getSectionView() {
        return view;
    }
    
    @Override
    public void componentOpened() {
        super.componentOpened();
        dObj.addPropertyChangeListener(this);
    }
    
    @Override
    public void componentClosed() {
        super.componentClosed();
        dObj.removePropertyChangeListener(this);
    }
    
    @Override
    public void componentShowing() {
        super.componentShowing();
        if (needInit){
            if (!puDataObject.viewCanBeDisplayed()) {
                view = new PersistenceView();
                view.setRoot(Node.EMPTY);
                comp.setContentView(view);
                return;
            }
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
    
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        String name = evt.getPropertyName();
        if (PUDataObject.PERSISTENCE_UNIT_ADDED_OR_REMOVED.equals(name)){
            repaintingTask.schedule(100);
        } else if ((PUDataObject.PROPERTY_DATA_MODIFIED.equals(name)
                || PUDataObject.PROPERTY_DATA_UPDATED.equals(name))
                && !this.equals(puDataObject.getActiveMultiViewElement0())) {
            needInit = true;
        } else if (PUDataObject.NO_UI_PU_CLASSES_CHANGED.equals(name) && this.equals(puDataObject.getActiveMultiViewElement0())) {
            //need to refresh classes view of specific persistence unit
            //TODO: review if it can be done easier as it looks quite complex
            PersistenceUnit pu = evt.getNewValue() instanceof PersistenceUnit ? (PersistenceUnit) evt.getNewValue() : null;
            if(pu != null) {
                SectionContainer sc = view.getPersistenceUnitsCont();
                SectionContainerNode sn = (SectionContainerNode) sc.getNode();
                Children ch=sn.getChildren();
                NodeSectionPanel nsp = null;
                for(Node n:ch.getNodes()) {
                    PersistenceUnitNode pun = (PersistenceUnitNode) n;
                    String pusecname = pun.getDisplayName();
                    if(pusecname.equals(pu.getName()))
                    {
                        nsp = sc.getSection(n);
                        break;
                    }
                }
                SectionPanel sp = nsp!=null && nsp instanceof SectionPanel ? ((SectionPanel)nsp) : null;
                PersistenceUnitPanel up = (PersistenceUnitPanel) (sp.getInnerPanel() != null && sp.getInnerPanel() instanceof PersistenceUnitPanel ? sp.getInnerPanel() : null);
                if(up != null) {
                    up.initEntityList();
                }
                else {
                    needInit = true;//at least mark as required to be refreshed
                }
            } else {
                needInit = true;//at least mark as required to be refreshed
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

        @Override
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
        
        @Override
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
        
        @Override
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
                @Override
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
                String version=puDataObject.getPersistence().getVersion();
                PersistenceUnit punit = null;
                if(Persistence.VERSION_2_0.equals(version))
                {
                    punit = new org.netbeans.modules.j2ee.persistence.dd.persistence.model_2_0.PersistenceUnit();
                }
                else//currently default 1.0
                {
                    punit = new org.netbeans.modules.j2ee.persistence.dd.persistence.model_1_0.PersistenceUnit();
                }
                
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
                    Provider provider = puPanel.getSelectedProvider();
                    if (puPanel.isNonDefaultProviderEnabled()) {
                        punit.setProvider(puPanel.getNonDefaultProvider());
                        Library lib = PersistenceLibrarySupport.getLibrary(provider);
                        if (lib != null && !Util.isDefaultProvider(project, provider)) {
                            Util.addLibraryToProject(project, lib);
                            provider = null;//to avoid one more addition
                        }
                    }
                    if(provider != null && provider.getAnnotationProcessor() != null){
                        Library lib = PersistenceLibrarySupport.getLibrary(provider);
                        if (lib != null){
                            Util.addLibraryToProject(project, lib, JavaClassPathConstants.PROCESSOR_PATH);
                        }
                    }
                } else {
                    PersistenceUnitWizardPanelJdbc puJdbc = (PersistenceUnitWizardPanelJdbc) panel;
                    punit = ProviderUtil.buildPersistenceUnit(puJdbc.getPersistenceUnitName(), puJdbc.getSelectedProvider(), puJdbc.getPersistenceConnection(), version);
                    punit.setTransactionType("RESOURCE_LOCAL");
                    Library lib = PersistenceLibrarySupport.getLibrary(puJdbc.getSelectedProvider());
                    if (lib != null){
                        Util.addLibraryToProject(project, lib);
                    }
                    JDBCDriver[] driver = JDBCDriverManager.getDefault().getDrivers(puJdbc.getPersistenceConnection().getDriverClass());
                    PersistenceLibrarySupport.addDriver(project, driver[0]);
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
        
        @Override
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

        @Override
        public boolean isEnabled() {
            //according to jpa 2.0 there should be at least one persistence unit
            boolean disable=puDataObject.getPersistence().sizePersistenceUnit()<=1 && puDataObject.getPersistence().getVersion().equals(Persistence.VERSION_2_0);
            return !disable;
        }
    }
}
