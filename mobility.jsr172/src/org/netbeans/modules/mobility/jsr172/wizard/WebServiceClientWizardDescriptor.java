/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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

package org.netbeans.modules.mobility.jsr172.wizard;
import java.util.HashSet;
import java.util.Set;

import java.awt.Component;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.openide.WizardDescriptor;
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
    
    public static final HelpCtx HELP_CTX = new HelpCtx( "me.wcb_clientinformation" ); // NOI18N
    
    public boolean isFinishPanel(){
        return true;
    }
    
    private final Set<ChangeListener> listeners = new HashSet<ChangeListener>(1);
    public final void addChangeListener(final ChangeListener l) {
        synchronized (listeners) {
            listeners.add(l);
        }
    }
    
    public final void removeChangeListener(final ChangeListener l) {
        synchronized (listeners) {
            listeners.remove(l);
        }
    }
    
    protected final void fireChangeEvent() {
    	ChangeListener[] lst;
        synchronized (listeners) {
            lst = listeners.toArray(new ChangeListener[listeners.size()]);
        }
        final ChangeEvent ev = new ChangeEvent(this);
        for ( ChangeListener cl : lst )
            cl.stateChanged(ev);
    }
    
    public Component getComponent() {
        if(component == null) {
            component = new ClientInfo(this);
        }
        
        return component;
    }
    
    public HelpCtx getHelp() {
        return WebServiceClientWizardDescriptor.HELP_CTX;
    }
    
    public boolean isValid() {
        boolean projectDirValid=true;
        String illegalChar = null;
        if (projectPath.indexOf('%')>=0) {
            projectDirValid=false;
            illegalChar="%";
        } else if (projectPath.indexOf('&')>=0) {
            projectDirValid=false;
            illegalChar="&";
        } else if (projectPath.indexOf('?')>=0) {
            projectDirValid=false;
            illegalChar="?";
        }
        if (!projectDirValid) {
            wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE,
                    NbBundle.getMessage(WebServiceClientWizardDescriptor.class,"MSG_InvalidProjectPath",projectPath,illegalChar));
            return false;
        } else if (!testClassPath() ) {
            wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE,
                    NbBundle.getMessage(WebServiceClientWizardDescriptor.class,"MSG_MissingXMLJars"));
            return false;
        } else {
            return component.valid(wizardDescriptor);
        }
    }
    
    public void readSettings(final Object settings) {
        wizardDescriptor = (WizardDescriptor) settings;
        component.read(wizardDescriptor);
        project = Templates.getProject(wizardDescriptor);
        projectPath = project.getProjectDirectory().getPath();
        
        // XXX hack, TemplateWizard in final setTemplateImpl() forces new wizard's title
        // this name is used in NewFileWizard to modify the title
        wizardDescriptor.putProperty("NewFileWizard_Title", //NOI18N
                NbBundle.getMessage(WebServiceClientWizardDescriptor.class, "LBL_WebServiceClient"));// NOI18N
    }
    
    public void storeSettings(final Object settings) {
        final WizardDescriptor d = (WizardDescriptor) settings;
        component.store(d);
        d.putProperty("NewFileWizard_Title", null); // NOI18N
    }
    
    @SuppressWarnings("unused")
	public void validate() throws org.openide.WizardValidationException {
    }
    
    
    private boolean testClassPath() {
//        SourceGroup[] sgs = WebServiceClientWizardIterator.getJavaSourceGroups(project);
//        ClassPath classPath = ClassPath.getClassPath(sgs[0].getRootFolder(),ClassPath.COMPILE);
//        WebServicesClientSupport clientSupport = WebServicesClientSupport.getWebServicesClientSupport(project.getProjectDirectory());
//        if (clientSupport==null) {
//            String mes = NbBundle.getMessage(WebServiceClientWizardDescriptor.class, "ERR_NoWebServiceClientSupport"); // NOI18N
//            NotifyDescriptor desc = new NotifyDescriptor.Message(mes, NotifyDescriptor.Message.ERROR_MESSAGE);
//            DialogDisplayer.getDefault().notify(desc);
//            return true;
//        }
//        if (clientSupport.getDeploymentDescriptor()==null) { // testing java project type
//            // test for the platform
//            String javaVersion = System.getProperty("java.version"); //NOI18N
//            if (javaVersion!=null && javaVersion.startsWith("1.4")) { //NOI18N
//                FileObject documentRangeFO = classPath.findResource("org/w3c/dom/ranges/DocumentRange.class"); //NOI18N
//                FileObject saxParserFO = classPath.findResource("com/sun/org/apache/xerces/internal/jaxp/SAXParserFactoryImpl.class"); //NOI18N
//                if (documentRangeFO == null || saxParserFO == null) {
//                    ProjectClassPathExtender pce = (ProjectClassPathExtender)project.getLookup().lookup(ProjectClassPathExtender.class);
//                    Library jaxrpclib_ext = LibraryManager.getDefault().getLibrary("jaxrpc16_xml"); //NOI18N
//                    if (pce==null || jaxrpclib_ext == null) {
//                        return false;
//                    }
//                }
//            }
//        }
        return true;
    }
}
