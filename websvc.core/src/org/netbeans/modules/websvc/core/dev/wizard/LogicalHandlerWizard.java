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

package org.netbeans.modules.websvc.core.dev.wizard;

import java.io.IOException;
import java.util.Collections;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.swing.JComponent;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.websvc.core.CreatorProvider;
import org.netbeans.modules.websvc.core.HandlerCreator;
import org.netbeans.spi.java.project.support.ui.templates.JavaTemplates;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import javax.swing.event.ChangeListener;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.netbeans.modules.j2ee.common.Util;

public class LogicalHandlerWizard implements WizardDescriptor.InstantiatingIterator {
    public int currentPanel = 0;
    private WizardDescriptor.Panel [] wizardPanels;
    private WizardDescriptor.Panel firstPanel; //special case: use Java Chooser
    private WizardDescriptor wiz;
    private Project project;
    private String handlerName;
    public static final String JAVAC_CLASSPATH = "javac.classpath"; //NOI18N
    
    public static LogicalHandlerWizard create() {
        return new LogicalHandlerWizard();
    }
    
    private static final String [] HANDLER_STEPS =
            new String [] {
        NbBundle.getMessage(LogicalHandlerWizard.class, "LBL_SpecifyLogicalHandlerInfo") //NOI18N
    };
    
    public void initialize(WizardDescriptor wizard) {
        
        wiz = wizard;
        project = Templates.getProject(wiz);
        SourceGroup[] sourceGroups = Util.getJavaSourceGroups(project);
        
        //create the Java Project chooser
//        firstPanel = JavaTemplates.createPackageChooser(project, sourceGroups, new BottomPanel());
        
        if (sourceGroups.length == 0)
            firstPanel = new FinishableProxyWizardPanel(Templates.createSimpleTargetChooser(project, sourceGroups, new BottomPanel()));
        else
            firstPanel = new FinishableProxyWizardPanel(JavaTemplates.createPackageChooser(project, sourceGroups, new BottomPanel(), true));
        
        JComponent c = (JComponent) firstPanel.getComponent();
        Util.changeLabelInComponent(c, NbBundle.getMessage(Util.class, "LBL_JavaTargetChooserPanelGUI_ClassName_Label"), //NOI18N
                NbBundle.getMessage(LogicalHandlerWizard.class, "LBL_LogicalHandler_Name") ); //NOI18N
        c.putClientProperty("WizardPanel_contentData", //NOI18N
                HANDLER_STEPS);
        c.putClientProperty("WizardPanel_contentSelectedIndex", //NOI18N
                new Integer(0));
        c.getAccessibleContext().setAccessibleDescription
                (HANDLER_STEPS[0]);
        wizardPanels = new WizardDescriptor.Panel[] {firstPanel};
    }
    
    public void uninitialize(WizardDescriptor wizard) {
    }
    
    public Set instantiate() throws IOException {
        //new WebServiceCreator(project, wiz).createLogicalHandler();
        HandlerCreator creator = CreatorProvider.getHandlerCreator(project, wiz);
        if (creator!=null) creator.createLogicalHandler();        
        return Collections.EMPTY_SET;
    }
    
    
    public WizardDescriptor.Panel current() {
        return wizardPanels[currentPanel];
    }
    
    public boolean hasNext() {
        return currentPanel < wizardPanels.length -1;
    }
    
    public boolean hasPrevious() {
        return currentPanel > 0;
    }
    
    public String name() {
        return NbBundle.getMessage(LogicalHandlerWizard.class, "LBL_Create_LogicalHandler_Title"); //NOI18N
    }
    
    public void nextPanel() {
        if(!hasNext()){
            throw new NoSuchElementException();
        }
        currentPanel++;
    }
    
    public void previousPanel() {
        if(!hasPrevious()){
            throw new NoSuchElementException();
        }
        currentPanel--;
    }
    
    public void addChangeListener(javax.swing.event.ChangeListener l) {
    }
    
    public void removeChangeListener(ChangeListener l) {
    }
    
    
    protected int getCurrentPanelIndex() {
        return currentPanel;
    }
    
    /** Dummy implementation of WizardDescriptor.Panel required in order to provide Help Button
     */
    private class BottomPanel implements WizardDescriptor.Panel {
        
        public void storeSettings(Object settings) {
        }
        
        public void readSettings(Object settings) {
        }
        
        public java.awt.Component getComponent() {
            return new javax.swing.JPanel();
        }
        
        public void addChangeListener(ChangeListener l) {
        }
        
        public void removeChangeListener(ChangeListener l) {
        }
        
        public boolean isValid() {
            ProjectInfo creator = new ProjectInfo(project);
            int projectType = creator.getProjectType();
            
            if (projectType == ProjectInfo.JSE_PROJECT_TYPE && Util.isSourceLevel16orHigher(project))
                return true;
            
            if (projectType == ProjectInfo.JSE_PROJECT_TYPE && Util.getSourceLevel(project).equals("1.5")) { //NOI18N
                //test JAX-WS library
                if (!PlatformUtil.hasJAXWSLibrary(project)) {
                    wiz.putProperty("WizardPanel_errorMessage", NbBundle.getMessage(BottomPanel.class, "LBL_LogicalHandlerWarning")); // NOI18N
                    return false;
                } else
                    return true;
            }
            
            if (Util.isJavaEE5orHigher(project) && (projectType == ProjectInfo.WEB_PROJECT_TYPE || projectType == ProjectInfo.EJB_PROJECT_TYPE)) { //NOI18N
                return true;
            }
            
            //if platform is Tomcat, source level must be jdk 1.5 and jaxws library must be in classpath
            if(!Util.isJavaEE5orHigher(project) && projectType == ProjectInfo.WEB_PROJECT_TYPE
                    && !PlatformUtil.isJsr109Supported(project) 
                    && !PlatformUtil.isJsr109OldSupported(project) ){
                //has to be at least jdk 1.5
                if (Util.isSourceLevel14orLower(project)) {
                    wiz.putProperty("WizardPanel_errorMessage",
                            NbBundle.getMessage(LogicalHandlerWizard.class, "ERR_HandlerNeedProperSourceLevel")); // NOI18N
                    return false;
                }
                if (!PlatformUtil.hasJAXWSLibrary(project)) { //must have jaxws library
                    wiz.putProperty("WizardPanel_errorMessage", NbBundle.getMessage(BottomPanel.class, "LBL_LogicalHandlerWarning")); // NOI18N
                    return false;
                } else
                    return true;
            }
            
            wiz.putProperty("WizardPanel_errorMessage", NbBundle.getMessage(BottomPanel.class, "LBL_LogicalHandlerWarning")); // NOI18N
            
            return false;
        }
        
        public HelpCtx getHelp() {
            return new HelpCtx(LogicalHandlerWizard.class);
        }
    }
}
