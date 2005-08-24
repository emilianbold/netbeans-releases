/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.websvc.core.client.wizard;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import java.awt.Component;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.modules.websvc.api.client.WebServicesClientSupport;
import org.netbeans.spi.java.project.classpath.ProjectClassPathExtender;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;

import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ui.templates.support.Templates;


/**
 *
 * @author Peter Williams
 */
public class WebServiceClientWizardDescriptor implements WizardDescriptor.FinishablePanel, WizardDescriptor.ValidatingPanel {

    private WizardDescriptor wizardDescriptor;
    private ClientInfo component = null;
    private String projectPath;
    private Project project;

    public WebServiceClientWizardDescriptor() {
    }

    public boolean isFinishPanel(){
        return true;
    }

    private final Set/*<ChangeListener>*/ listeners = new HashSet(1);
        public final void addChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.add(l);
        }
    }

    public final void removeChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.remove(l);
        }
    }

    protected final void fireChangeEvent() {
        Iterator it;
        synchronized (listeners) {
            it = new HashSet(listeners).iterator();
        }
        ChangeEvent ev = new ChangeEvent(this);
        while (it.hasNext()) {
            ((ChangeListener)it.next()).stateChanged(ev);
        }
    }

    public Component getComponent() {
        if(component == null) {
            component = new ClientInfo(this);
        }

        return component;
    }

    public HelpCtx getHelp() {
        return null;
    }

    public boolean isValid() { 
        boolean projectDirValid=true;
        String illegalChar = null;
        if (projectPath.indexOf("%")>=0) {
            projectDirValid=false;
            illegalChar="%";
        } else if (projectPath.indexOf("&")>=0) {
            projectDirValid=false;
            illegalChar="&";
        } else if (projectPath.indexOf("?")>=0) {
            projectDirValid=false;
            illegalChar="?";
        }
        if (!projectDirValid) {
            wizardDescriptor.putProperty("WizardPanel_errorMessage",
                    NbBundle.getMessage(WebServiceClientWizardDescriptor.class,"MSG_InvalidProjectPath",projectPath,illegalChar));
            return false;
        } else if (!testClassPath() ) {
            wizardDescriptor.putProperty("WizardPanel_errorMessage",
                    NbBundle.getMessage(WebServiceClientWizardDescriptor.class,"MSG_MissingXMLJars"));
            return false;
        } else {
            return component.valid(wizardDescriptor);   
        }
    }

    public void readSettings(Object settings) {
        wizardDescriptor = (WizardDescriptor) settings;
        component.read(wizardDescriptor);
        project = Templates.getProject(wizardDescriptor);
        projectPath = project.getProjectDirectory().getPath();

        // XXX hack, TemplateWizard in final setTemplateImpl() forces new wizard's title
        // this name is used in NewFileWizard to modify the title
        wizardDescriptor.putProperty("NewFileWizard_Title", 
        NbBundle.getMessage(WebServiceClientWizardDescriptor.class, "LBL_WebServiceClient"));// NOI18N        
    }

    public void storeSettings(Object settings) {
        WizardDescriptor d = (WizardDescriptor) settings;
        component.store(d);
        ((WizardDescriptor) d).putProperty("NewFileWizard_Title", null); // NOI18N
    }

    public void validate() throws org.openide.WizardValidationException {
    }
    
    private boolean testClassPath() {
        SourceGroup[] sgs = WebServiceClientWizardIterator.getJavaSourceGroups(project);
        ClassPath classPath = ClassPath.getClassPath(sgs[0].getRootFolder(),ClassPath.COMPILE);
        WebServicesClientSupport clientSupport = WebServicesClientSupport.getWebServicesClientSupport(project.getProjectDirectory());
        if (clientSupport==null) {
            String mes = NbBundle.getMessage(WebServiceClientWizardDescriptor.class, "ERR_NoWebServiceClientSupport"); // NOI18N
            NotifyDescriptor desc = new NotifyDescriptor.Message(mes, NotifyDescriptor.Message.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(desc);
            return true;
        }
        if (clientSupport.getDeploymentDescriptor()==null) { // testing java project type
            // test for the platform
            String javaVersion = System.getProperty("java.version"); //NOI18N   
            if (javaVersion!=null && javaVersion.startsWith("1.4")) { //NOI18N
                FileObject documentRangeFO = classPath.findResource("org/w3c/dom/ranges/DocumentRange.class"); //NOI18N
                FileObject saxParserFO = classPath.findResource("com/sun/org/apache/xerces/internal/jaxp/SAXParserFactoryImpl.class"); //NOI18N
                if (documentRangeFO == null || saxParserFO == null) {
                    ProjectClassPathExtender pce = (ProjectClassPathExtender)project.getLookup().lookup(ProjectClassPathExtender.class);
                    Library jaxrpclib_ext = LibraryManager.getDefault().getLibrary("jaxrpc16_xml"); //NOI18N
                    if (pce==null || jaxrpclib_ext == null) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
}
