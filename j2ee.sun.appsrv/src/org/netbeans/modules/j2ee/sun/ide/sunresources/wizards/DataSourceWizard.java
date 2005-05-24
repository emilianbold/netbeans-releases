/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
/*
 * DataSourceWizard.java
 *
 * Created on September 30, 2003, 10:05 AM
 */

package org.netbeans.modules.j2ee.sun.ide.sunresources.wizards;

/**
 *
 * @author  nityad
 */

import java.awt.Component;
import java.util.Set;
import javax.swing.JComponent;
import java.io.InputStream;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.util.Iterator;
import java.util.HashSet;

import org.openide.util.NbBundle;
import org.openide.WizardDescriptor;
import org.netbeans.api.project.Project;
import org.openide.filesystems.FileObject;
import org.netbeans.spi.project.ui.templates.support.Templates;

import org.netbeans.modules.j2ee.sun.ide.sunresources.beans.ResourceUtils;

import org.netbeans.modules.j2ee.sun.sunresources.beans.Wizard;
import org.netbeans.modules.j2ee.sun.sunresources.beans.WizardConstants;

public final class DataSourceWizard implements WizardDescriptor.InstantiatingIterator, ChangeListener, WizardConstants{
    
    private static Project project;
    /** An array of all wizard panels */
       
    private static final String DATAFILE = "org/netbeans/modules/j2ee/sun/sunresources/beans/DSWizard.xml";  //NOI18N
    private static final String CP_DATAFILE = "org/netbeans/modules/j2ee/sun/sunresources/beans/CPWizard.xml";  //NOI18N
    
    private transient int index;
    private transient WizardDescriptor.Panel[] panels;
    private transient WizardDescriptor wiz;
    private transient String[] steps;
    
    private ResourceConfigHelper helper;
    private Wizard wizardInfo;
        
    private boolean addSteps = false;
    private boolean firstTime = true;
    
    private ResourceConfigHelper cphelper;
    private ResourceConfigHelperHolder holder;
    private Wizard cpWizardInfo;
     
    private transient WizardDescriptor.Panel[] morePanels = null;
    private transient WizardDescriptor.Panel[] dsPanels = null;
    
    private transient String[] dsSteps = null;
    private transient String[] moreSteps = null;
    
    /** Creates a new instance of DataSourceWizard */
    public static DataSourceWizard create () {
        return new DataSourceWizard ();
    }
    
    private WizardDescriptor.Panel[] createPanels() {
        morePanels = null;
        WizardDescriptor.Panel panel = new CommonAttributePanel(helper, wizardInfo, new String[] {"general"});   //NOI18N
        panel.addChangeListener(this);
        return new WizardDescriptor.Panel[] {
            panel,
            new CommonPropertyPanel(this.helper, this.wizardInfo)
        };
    }
    
    private String[] createSteps() {
        return new String[] {
            __FirstStepChoose,
            NbBundle.getMessage(DataSourceWizard.class, "LBL_GeneralAttributes_DS"), // NOI18N
            NbBundle.getMessage(DataSourceWizard.class, "LBL_AddProperty") // NOI18N
        };
    }
    
    public Set instantiate(){
        try{
            if(this.holder.hasCPHelper()){
                String poolName = this.cphelper.getData().getString(__Name);
                this.helper.getData().setString(__PoolName, poolName);
                this.cphelper.getData().setTargetFile(poolName);
                this.cphelper.getData().setTargetFileObject(this.helper.getData().getTargetFileObject());
                ResourceUtils.saveJDBCResourceDatatoXml(this.helper.getData(), this.cphelper.getData());
            }else{
                ResourceUtils.saveJDBCResourceDatatoXml(this.helper.getData(), null);
            }    
        }catch (Exception ex){
            //System.out.println("Error in instantiate ");
        }
        return java.util.Collections.EMPTY_SET;
    }
    
    public void initialize(WizardDescriptor wiz){
        this.wizardInfo = getWizardInfo(DATAFILE);
        this.holder = new ResourceConfigHelperHolder();
        this.helper = holder.getDataSourceHelper();
        
        this.wiz = wiz;
        wiz.putProperty("NewFileWizard_Title", NbBundle.getMessage(ConnPoolWizard.class, "Templates/SunResources/JDBC_Resource")); //NOI18N
        index = 0;
                
        project = Templates.getProject(wiz);
        
        panels = createPanels();
        // Make sure list of steps is accurate.
        steps = createSteps();
        
        try{
            FileObject pkgLocation = project.getProjectDirectory();
            if (pkgLocation != null) {
                this.helper.getData().setTargetFileObject(pkgLocation);
            }
        }catch (Exception ex){
            //Unable to get project location
        }
        
        for (int i = 0; i < panels.length; i++) {
            Component c = panels[i].getComponent();
            if (c instanceof JComponent) { // assume Swing components
                JComponent jc = (JComponent)c;
                // Step #.
                jc.putClientProperty("WizardPanel_contentSelectedIndex", new Integer(i)); // NOI18N
                // Step name (actually the whole list for reference).
                jc.putClientProperty("WizardPanel_contentData", steps); // NOI18N
            }
        }
        
    }
    
    public void uninitialize(WizardDescriptor wiz){
        this.wiz = null;
        panels = null;
    }
    
    public Wizard getWizardInfo(String filePath){
        try{
            InputStream in = Wizard.class.getClassLoader().getResourceAsStream(filePath);
            this.wizardInfo = Wizard.createGraph(in);
        }catch(Exception ex){
            //System.out.println("Unable to get Wiz Info");
        }
        return this.wizardInfo;
    }
    
    public String name(){
        return NbBundle.getMessage(DataSourceWizard.class, "Templates/SunResources/JDBC_Resource"); //NOI18N
    }
    
    public boolean hasNext(){
        return index < panels.length - 1;
    }
    
    public boolean hasPrevious(){
        return index > 0;
    }
    
    public synchronized void nextPanel(){
        if (index + 1 == panels.length)
            throw new java.util.NoSuchElementException();
        
        if (index == 0) {
            ((CommonPropertyPanel) panels[1]).setInitialFocus();
        }else if (index == 1) {
            ((CPVendorPanel) panels[2]).setInitialFocus();
        }else if (index == 2){
            ((CPPropertiesPanelPanel) panels[3]).refreshFields();
        }else if (index == 3){
            ((CommonAttributePanel) panels[4]).setPropInitialFocus();
        }
        
        index ++;
    }
    
    public synchronized void previousPanel(){
        if (index == 0)
            throw new java.util.NoSuchElementException();
        
        index--;
    }
    
    public WizardDescriptor.Panel current(){
        return (WizardDescriptor.Panel)panels[index];
    }
    
    public final void addChangeListener(ChangeListener l) {
    }
    public final void removeChangeListener(ChangeListener l) {
    }

    public void setResourceConfigHelper(ResourceConfigHelper helper){
        this.helper = helper;
    }
    
    public ResourceConfigHelper getResourceConfigHelper(){
        return this.helper;
    }
    
    public void stateChanged(javax.swing.event.ChangeEvent e) {
        if( (e.getSource().getClass() == CommonAttributePanel.class) || (e.getSource().getClass() == CommonAttributeVisualPanel.class) ) {
            CommonAttributePanel commonPane = (CommonAttributePanel)this.current();
            CommonAttributeVisualPanel visPane = (CommonAttributeVisualPanel)commonPane.getComponent();
            boolean oldVal = addSteps;
            this.addSteps = visPane.isNewResourceSelected();
            
            if((!oldVal && addSteps) || (oldVal && !addSteps)){
                this.holder.setHasCPHelper(this.addSteps);
                if (addSteps && morePanels == null) {
                    addPanels();
                    addSteps();
                    for (int i = 0; i < panels.length; i++) {
                        Component c = panels[i].getComponent();
                        if (steps[i] == null) {
                            steps[i] = c.getName();
                        }
                        if (c instanceof JComponent) {
                            JComponent jc = (JComponent)c;
                            jc.putClientProperty("WizardPanel_contentSelectedIndex", new Integer(i)); // NOI18N
                            jc.putClientProperty("WizardPanel_contentData", steps); // NOI18N
                        }
                    }
                }else if((!addSteps) && (morePanels != null)){
                    if(dsPanels != null){
                        panels = dsPanels;
                        morePanels = null;
                    }else
                        panels = createPanels();
                    if(dsSteps != null)
                        steps = dsSteps;
                    else
                        steps = createSteps();
                    
                    for (int i = 0; i < panels.length; i++) {
                        Component c = panels[i].getComponent();
                        if (steps[i] == null) {
                            steps[i] = c.getName();
                        }
                        if (c instanceof JComponent) {
                            JComponent jc = (JComponent)c;
                            jc.putClientProperty("WizardPanel_contentSelectedIndex", new Integer(i)); // NOI18N
                            jc.putClientProperty("WizardPanel_contentData", steps); // NOI18N
                        }
                    }
                    ((CommonAttributePanel)panels[0]).setInitialFocus();
                }
            }
        }
    }
    
    protected void addPanels() {
        if (panels != null && morePanels == null) {
            this.cphelper = this.holder.addAssociatedHelper();
            this.cphelper.getData().setResourceName(__JdbcConnectionPool);
            this.holder.setHasCPHelper(true);
            this.cpWizardInfo = getWizardInfo(CP_DATAFILE);
            this.cphelper.getData().setTargetFileObject(this.helper.getData().getTargetFileObject());
            this.cphelper.getData().setString(__DynamicWizPanel, "true"); //NOI18N
            
            morePanels = new WizardDescriptor.Panel[] {
                panels[0],
                panels[1],
                new CPVendorPanel(this.cphelper, this.cpWizardInfo),
                new CPPropertiesPanelPanel(this.cphelper, this.cpWizardInfo),
                new CommonAttributePanel(this.cphelper, this.cpWizardInfo,  new String[] {"pool-setting", "pool-setting-2", "pool-setting-3"}), //NOI18N
            };
        }
        dsPanels = panels;
        panels = morePanels;
    }
        
    protected void addSteps() {
        if (steps != null && moreSteps == null) {
            moreSteps = new String[] {
                steps[0],
                steps[1],
                steps[2],
                NbBundle.getMessage(DataSourceWizard.class, "TITLE_ConnPoolWizardPanel_dbConn"), // NOI18N
                NbBundle.getMessage(DataSourceWizard.class, "TITLE_ConnPoolWizardPanel_properties"), // NOI18N
                NbBundle.getMessage(DataSourceWizard.class, "TITLE_ConnPoolWizardPanel_optionalProps") // NOI18N
            };
        }
        dsSteps = steps;
        steps = moreSteps;
    }     
} 
