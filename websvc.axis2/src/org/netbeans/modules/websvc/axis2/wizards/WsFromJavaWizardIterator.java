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

package org.netbeans.modules.websvc.axis2.wizards;

import java.awt.Component;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.NoSuchElementException;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;

import org.netbeans.modules.websvc.axis2.Axis2ModelProvider;
import org.netbeans.modules.websvc.axis2.AxisUtils;
import org.netbeans.modules.websvc.axis2.config.model.Axis2Model;
import org.netbeans.modules.websvc.axis2.services.model.ServicesModel;
import org.netbeans.modules.websvc.axis2.services.model.ServicesUtils;
import org.openide.WizardDescriptor;

import org.netbeans.spi.java.project.support.ui.templates.JavaTemplates;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.TemplateWizard;
import org.openide.util.NbBundle;

/**
 * Wizard to create a new Web project.
 * @author Jesse Glick, Radko Najman
 */
public class WsFromJavaWizardIterator implements TemplateWizard.Iterator /*, ItemListener*/ {
    
    private Project project;

    /** Create a new wizard iterator. */
    public WsFromJavaWizardIterator() {
    }
        
//    public static WsFromJavaWizardIterator create() {
//        return new WsFromJavaWizardIterator();
//    }

    public Set<DataObject> instantiate(TemplateWizard wiz) throws IOException {
        DataObject dObj = null;
        
        WizardUtils.addAxis2Library(project);
        
        if (WizardProperties.JAVA_TYPE_EXISTING.equals(wiz.getProperty(WizardProperties.PROP_FROM_JAVA_TYPE))) {
            FileObject fo = (FileObject)wiz.getProperty(WizardProperties.PROP_JAVA_CLASS);
            if (fo != null) {
                dObj = DataObject.find(fo);
            }
        } else {
            FileObject template = Templates.getTemplate( wiz );
            if (!((Boolean)wiz.getProperty(WizardProperties.PROP_GENERATE_SAMPLE_METHOD)).booleanValue()) {
                template = template.getParent().getFileObject("EmptyWebService","java"); //NOI18N
            }
            DataObject dTemplate = DataObject.find( template );
            org.openide.filesystems.FileObject dir = Templates.getTargetFolder( wiz );
            DataFolder df = DataFolder.findFolder( dir );
            dObj = dTemplate.createFromTemplate( df, Templates.getTargetName( wiz )  );
        }
        if (dObj != null) {
            generateConfigFile(dObj.getPrimaryFile());
            addService(dObj.getPrimaryFile());
            return Collections.singleton(dObj);
        }
        else return Collections.<DataObject>emptySet();
    }
    
    private void generateConfigFile(FileObject serviceFo) throws IOException {
        Axis2ModelProvider axis2ModelProvider = project.getLookup().lookup(Axis2ModelProvider.class);
        ServicesModel servicesModel = axis2ModelProvider.getServicesModel();
        if (servicesModel == null) {            
            FileObject configFolder = AxisUtils.getServicesFolder(project.getProjectDirectory(), true);
            if (configFolder != null) {
                FileObject servicesFo = configFolder.getFileObject("services.xml");
                if (servicesFo == null) {
                    AxisUtils.retrieveServicesFromResource(configFolder, true);
                }
                servicesFo = configFolder.getFileObject("services.xml"); //NOI18N
                servicesModel = ServicesUtils.getServicesModel(servicesFo, true);
                axis2ModelProvider.setServicesModel(servicesModel);
            }
        }
        if (servicesModel != null) {
            ClassPath classPath = ClassPath.getClassPath(serviceFo, ClassPath.SOURCE);
            String serviceClass = classPath.getResourceName(serviceFo, '.', false);
            WizardUtils.addService(servicesModel, serviceClass, serviceFo);
        }
    }
    
    private void addService(FileObject serviceFo) throws IOException {
        FileObject axis2Folder = AxisUtils.getNbprojectFolder(project.getProjectDirectory());
        if (axis2Folder != null) {
            FileObject axis2Fo = axis2Folder.getFileObject("axis2.xml"); //NOI18N
            if (axis2Fo == null) {
                AxisUtils.retrieveAxis2FromResource(axis2Folder);
                axis2Fo = axis2Folder.getFileObject("axis2.xml"); //NOI18N
            }
            ClassPath classPath = ClassPath.getClassPath(serviceFo, ClassPath.SOURCE);
            String serviceClass = classPath.getResourceName(serviceFo, '.', false);
            
            axis2Fo = axis2Folder.getFileObject("axis2.xml"); //NOI18N
            //Axis2Model axis2Model = Axis2Utils.getAxis2Model(axis2Fo, true);
            
            Axis2ModelProvider axis2ModelProvider = project.getLookup().lookup(Axis2ModelProvider.class);
            Axis2Model axis2Model = axis2ModelProvider.getAxis2Model();
            if (axis2Model != null) {
                WizardUtils.addService(axis2Model, serviceClass, serviceFo, 
                        ((Boolean)wiz.getProperty(WizardProperties.PROP_GENERATE_WSDL)).booleanValue());
                DataObject dObj = DataObject.find(axis2Fo);
                if (dObj != null) {
                    SaveCookie save = dObj.getCookie(SaveCookie.class);
                    if (save != null) save.save();
                }
            }
        }
    }
    
    
    private transient int index;
    private transient WizardDescriptor.Panel<WizardDescriptor>[] panels;
    private transient TemplateWizard wiz;
    private transient WizardDescriptor.Panel<WizardDescriptor> bottomPanel;

    @SuppressWarnings("unchecked")
    public void initialize(TemplateWizard wiz) {
        this.wiz = wiz;
        index = 0;

        project = Templates.getProject(wiz);

        SourceGroup[] sourceGroups = ProjectUtils.getSources(project).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        bottomPanel = new WsFromJavaPanel1(project, wiz);
        WizardDescriptor.Panel<WizardDescriptor> secondPanel; //special case: use Java Chooser
        if (sourceGroups.length == 0)
            secondPanel = new FinishableProxyWizardPanel(Templates.createSimpleTargetChooser(project, sourceGroups, bottomPanel));
        else
            secondPanel = new FinishableProxyWizardPanel(JavaTemplates.createPackageChooser(project, sourceGroups, bottomPanel, true));

        //JComponent comp = (JComponent) secondPanel.getComponent();
        
        
        panels = new WizardDescriptor.Panel[] {
            new WsFromJavaPanel0(project, wiz),
            secondPanel
        };
        
        // Creating steps.
        Object prop = this.wiz.getProperty("WizardPanel_contentData"); // NOI18N
        String[] beforeSteps = null;
        if (prop != null && prop instanceof String[]) {
            beforeSteps = (String[]) prop;
        }

        String[] steps = createSteps(beforeSteps, panels);
        
        // Make sure list of steps is accurate.
        for (int i = 0; i < panels.length; i++) {
            Component c = panels[i].getComponent();
            if (c instanceof JComponent) { // assume Swing components
                JComponent jc = (JComponent) c;
                // Step #.
                jc.putClientProperty("WizardPanel_contentSelectedIndex", Integer.valueOf(i)); // NOI18N
                // Step name (actually the whole list for reference).
                jc.putClientProperty("WizardPanel_contentData", steps); // NOI18N
            }
        }
    }
    
    public void uninitialize(TemplateWizard wiz) {
        if (this.wiz != null) {
//            this.wiz.putProperty(WizardProperties.WEB_SERVICE_TYPE, null);
        }    
        panels = null;
    }
    
    private String[] createSteps(String[] before, WizardDescriptor.Panel[] panels) {
        int diff = 0;
        if (before == null) {
            before = new String[0];
        } else if (before.length > 0) {
            diff = ("...".equals (before[before.length - 1])) ? 1 : 0; // NOI18N
        }
        String[] res = new String[ (before.length - diff) + panels.length];
        for (int i = 0; i < res.length; i++) {
            if (i < (before.length - diff)) {
                res[i] = before[i];
            } else {
                res[i] = panels[i - before.length + diff].getComponent ().getName ();
            }
        }
        return res;
    }

    public String name() {
        return MessageFormat.format(NbBundle.getMessage(WsFromJavaWizardIterator.class, "LBL_WizardStepsCount"), Integer.valueOf(index + 1).toString(), Integer.valueOf(panels.length).toString()); //NOI18N
    }
    
    public boolean hasNext() {
        if (!((WsFromJavaPanel0)panels[0]).isFromScratch()) return false;
        return index < panels.length - 1;
    }
    
    public boolean hasPrevious() {
        return index > 0;
    }
    
    public void nextPanel() {
        if (!hasNext()) throw new NoSuchElementException();
        index++;
    }
    
    public void previousPanel() {
        if (!hasPrevious()) throw new NoSuchElementException();
        index--;
    }
    
    public WizardDescriptor.Panel<WizardDescriptor> current() {
        return panels[index];
    }
    

    public final void addChangeListener(ChangeListener l) {}
    public final void removeChangeListener(ChangeListener l) {}

}
