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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.wsdlextensions.resources;

import java.awt.Component;
import java.awt.Container;
import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.text.JTextComponent;

import org.openide.WizardDescriptor;

import java.awt.Dimension;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Collections;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.project.JavaProjectConstants;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.WizardDescriptor;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.TemplateWizard;
import org.openide.util.NbBundle;

import org.netbeans.spi.project.ui.templates.support.Templates;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.Sources;
import org.netbeans.api.project.SourceGroup;
import org.openide.ErrorManager;

/**
 * A template wizard iterator (sequence of panels).
 * Used to fill in the second and subsequent panels in the New wizard.
 * Associate this to a template inside a layer using the
 * Sequence of Panels extra property.
 * Create one or more panels from template as needed too.
 */
public class NewFileIterator implements TemplateWizard.Iterator {
    
    private static final long serialVersionUID = 1L;
    public static final Dimension PREF_SIZE = new Dimension(560,350);
    
    protected WizardDescriptor.Panel[] createPanels(Project project, TemplateWizard wizard) {
        Sources sources = (Sources) project.getLookup().lookup(Sources.class);
        sourceGroups = sources.getSourceGroups(Sources.TYPE_GENERIC);
        
        if(sourceGroups.length == 0 ) {  
            sourceGroups = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        }
        folderPanel=new NewFilePanel(project,sourceGroups);
        
        if(sourceGroups.length > 0 && sourceGroups[0] != null) {
            DataFolder folder = DataFolder.findFolder(sourceGroups[0].getRootFolder());
            DataFolder projectFolder = DataFolder.findFolder(project.getProjectDirectory());
            try {
                if (wizard.getTargetFolder().equals(projectFolder)) {
                    wizard.setTargetFolder(folder);
                }
            } catch (IOException ioe) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ioe);
            }
        }
        WizardDescriptor.Panel firstPanel = Templates.createSimpleTargetChooser(project,sourceGroups,folderPanel);
        JComponent c = (JComponent)firstPanel.getComponent();
        String fileNameLabel = NbBundle.getMessage(NewFileIterator.class, "LBL_SimpleTargetChooserPanel_FileName_Label"); // NOI18N
        ((NewFilePanel)folderPanel).setNameTF((JTextField) findTextFieldForLabel(c,fileNameLabel));
        
        return new WizardDescriptor.Panel[] {
            firstPanel
        };
    }
    
    public Set instantiate(TemplateWizard aWiz) throws IOException {
      NewFilePanel panel = (NewFilePanel)folderPanel;
      org.openide.filesystems.FileObject dir = Templates.getTargetFolder(wiz);
      DataObject data = createProcess(Templates.getTargetName(wiz), Templates.getTemplate(wiz), dir);
      return Collections.singleton(data);
    }

    public void initialize(TemplateWizard aWiz) {
        this.wiz = aWiz;
        index = 0;
        Project project = Templates.getProject(wiz);
        panels = createPanels(project, wiz);
        
        // Creating steps.
        Object prop = wiz.getProperty(WizardDescriptor.PROP_CONTENT_DATA); // NOI18N
        String[] beforeSteps = null;
        if (prop != null && prop instanceof String[]) {
            beforeSteps = (String[])prop;
        }
        String[] steps = createSteps(beforeSteps, panels);
        
        for (int i = 0; i < panels.length; i++) {
            Component c = panels[i].getComponent();
            if (steps[i] == null) {
                // Default step name to component name of panel.
                // Mainly useful for getting the name of the target
                // chooser to appear in the list of steps.
                steps[i] = c.getName();
            }
            if (c instanceof JComponent) { // assume Swing components
                JComponent jc = (JComponent) c;
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, Integer.valueOf(i));
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, steps);
            }
        }
    }
    public void uninitialize(TemplateWizard aWiz) {
        this.wiz = null;
        panels = null;
    }
    
    public String name() {
        return NbBundle.getMessage(NewFileIterator.class, "TITLE_x_of_y", index + 1, panels.length); // NOI18N
    }
    
    public boolean hasNext() {
        return index < panels.length - 1;
    }
    public boolean hasPrevious() {
        return index > 0;
    }
    public void nextPanel() {
        if (! hasNext()) {
            throw new NoSuchElementException();
        }
        index++;
    }
    public void previousPanel() {
        if (! hasPrevious()) {
            throw new NoSuchElementException();
        }
        index--;
    }
    public WizardDescriptor.Panel current() {
        return panels[index];
    }

    public final void addChangeListener(ChangeListener l) {}
    public final void removeChangeListener(ChangeListener l) {}
    
    private static DataObject createProcess(String processName, FileObject template, FileObject srcFolder) throws IOException {
//System.out.println();
        DataFolder src = DataFolder.findFolder(srcFolder);
//System.out.println("       src: " + src);
        DataObject dTemplate = DataObject.find(template);
        DataObject data = dTemplate.createFromTemplate(src, processName);
//System.out.println("  Name: " + processName);
        return data;
    }
    
    private static FileObject findTemplate() {
        return FileUtil.getConfigFile("Templates/SOA_BPEL/LTemplate.ldif"); // NOI18N
    }


    static String[] createSteps(String[] before,
            WizardDescriptor.Panel[] panels) {
        //assert panels != null;
        // hack to use the steps set before this panel processed
        int diff = 0;
        if (before == null) {
            before = new String[0];
        } else if (before.length > 0) {
            diff = ("...".equals(before[before.length - 1])) ? 1 : 0; // NOI18N
        }
        String[] res = new String[ (before.length - diff) + panels.length];
        for (int i = 0; i < res.length; i++) {
            if (i < (before.length - diff)) {
                res[i] = before[i];
            } else {
                res[i] = panels[i - before.length + diff].getComponent().getName();
            }
        }
        return res;
    }
    
    /*
     * Changes the text of a JLabel in component from oldLabel to newLabel
     */
    static JTextComponent findTextFieldForLabel(JComponent component,
            String text) {
        JLabel label = findLabel(component, text);
        if(label != null) {
            Component comp = label.getLabelFor();
            if (comp!=null && (comp instanceof JTextComponent)) {
                return (JTextComponent)comp;
            }
        }
        return null;
    }
    
    
    /*
     * taken from j2ee module
     * Recursively gets all components in the components array and puts it in allComponents
     */
    static void getAllComponents( Component[] components, Collection allComponents) {
        for( int i = 0; i < components.length; i++ ) {
            if( components[i] != null ) {
                allComponents.add( components[i] );
                if( ( ( Container )components[i] ).getComponentCount() != 0 ) {
                    getAllComponents( 
                            ( ( Container )components[i] ).getComponents(), 
                            allComponents );
                }
            }
        }
    }
    
    /*
     *  taken from j2ee module
     *  Recursively finds a JLabel that has labelText in comp
     */
    static JLabel findLabel(JComponent comp, String labelText) {
        Vector allComponents = new Vector();
        getAllComponents(comp.getComponents(), allComponents);
        Iterator iterator = allComponents.iterator();
        while(iterator.hasNext()) {
            Component c = (Component)iterator.next();
            if(c instanceof JLabel) {
                JLabel label = (JLabel)c;
                if(label.getText().equals(labelText)) {
                    return label;
                }
            }
        }
        return null;
    }
    
    private transient int index;
    private transient TemplateWizard wiz;
    private WizardDescriptor.Panel folderPanel;
    private transient SourceGroup[] sourceGroups;
    private transient WizardDescriptor.Panel[] panels;
}
