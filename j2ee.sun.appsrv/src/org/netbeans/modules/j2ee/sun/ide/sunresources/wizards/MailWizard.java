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
 * MailWizard.java
 *
 * Created on October 9, 2003, 11:39 AM
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

import org.openide.util.NbBundle;
import org.openide.WizardDescriptor;
import org.netbeans.api.project.Project;
import org.openide.filesystems.FileObject;
import org.netbeans.spi.project.ui.templates.support.Templates;

import org.netbeans.modules.j2ee.sun.ide.sunresources.beans.ResourceUtils;

import org.netbeans.modules.j2ee.sun.sunresources.beans.Wizard;
import org.netbeans.modules.j2ee.sun.sunresources.beans.WizardConstants;

public final class MailWizard implements WizardDescriptor.InstantiatingIterator, WizardConstants{
    
    private static Project project;
           
    private static final String DATAFILE = "org/netbeans/modules/j2ee/sun/sunresources/beans/MailWizard.xml";  //NOI18N
        
    /** An array of all wizard panels */
    private transient WizardDescriptor.Panel[] panels;
    private transient WizardDescriptor wiz;
    private transient String[] steps;
    private transient int index;
    
    private ResourceConfigHelper helper;
    private Wizard wizardInfo;
        
    /** Creates a new instance of MailWizard */
    public static MailWizard create () {
        return new MailWizard ();
    }
    
    private WizardDescriptor.Panel[] createPanels() {
        return new WizardDescriptor.Panel[] {
            new CommonGeneralFinishPanel(this.helper, this.wizardInfo, new String[] {"general", "advanced"}), //NOI18N
            new MailPropertyPanel(this.helper, this.wizardInfo)
        };
    }
    
    private String[] createSteps() {
        return new String[] {
            __FirstStepChoose,
            NbBundle.getMessage(MailWizard.class, "LBL_GeneralAttributes_MAIL"), //NOI18N
            NbBundle.getMessage(MailWizard.class, "LBL_AddProperty") //NOI18N
        };
    }
    
    public Set instantiate(){
        try{
            ResourceUtils.saveMailResourceDatatoXml(this.helper.getData());
        }catch (Exception ex){
            //System.out.println("Error in instantiate ");
        }
        return java.util.Collections.EMPTY_SET;
    }
    
    public void initialize(WizardDescriptor wiz){
        this.wizardInfo = getWizardInfo();
        this.helper = new ResourceConfigHelperHolder().getMailHelper();
        
        this.wiz = wiz;
        wiz.putProperty("NewFileWizard_Title", NbBundle.getMessage(MailWizard.class, "Templates/SunResources/JavaMail_Resource")); //NOI18N
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
    
    public Wizard getWizardInfo(){
        try{
            InputStream in = Wizard.class.getClassLoader().getResourceAsStream(DATAFILE);
            this.wizardInfo = Wizard.createGraph(in);
        }catch(Exception ex){
            //System.out.println("Unable to get Wiz Info");
        }
        return this.wizardInfo;
    }
    
    public String name(){
        return NbBundle.getMessage(MailWizard.class, "Templates/SunResources/JavaMail_Resource"); //NOI18N
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
        
        if (index == 0){
            ((MailPropertyPanel) panels[1]).setInitialFocus();
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
    
    public void addChangeListener(ChangeListener l){
    }
    
    public void removeChangeListener(ChangeListener l){
    }

     
    public void setResourceConfigHelper(ResourceConfigHelper helper){
        this.helper = helper;
    }
    
    public ResourceConfigHelper getResourceConfigHelper(){
        return this.helper;
    }
    
}
