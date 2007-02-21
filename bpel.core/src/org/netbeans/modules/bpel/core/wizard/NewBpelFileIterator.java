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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.modules.bpel.core.wizard;

import java.awt.Component;
import java.awt.Dimension;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.project.JavaProjectConstants;

import org.openide.filesystems.FileObject;
import org.openide.WizardDescriptor;
import org.openide.loaders.*;
import org.openide.util.NbBundle;

import org.netbeans.spi.project.ui.templates.support.Templates;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.Sources;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.bpel.project.BpelproProject;
import org.openide.ErrorManager;
import org.openide.filesystems.FileUtil;

/** A template wizard iterator (sequence of panels).
 * Used to fill in the second and subsequent panels in the New wizard.
 * Associate this to a template inside a layer using the
 * Sequence of Panels extra property.
 * Create one or more panels from template as needed too.
 */
public class NewBpelFileIterator implements TemplateWizard.Iterator {
    
    private static final long serialVersionUID = 1L;
    
    public static final Dimension PREF_SIZE = new Dimension(560,350);
    
    // You should define what panels you want to use here:
    protected WizardDescriptor.Panel[] createPanels(Project project, 
            TemplateWizard wizard) 
    {
        Sources sources = (Sources) project.getLookup().lookup(Sources.class);
        // First look for the BPEL project defined sources type.
        sourceGroups = sources.getSourceGroups(BpelproProject.SOURCES_TYPE_BPELPRO);
        
        if(sourceGroups.length == 0 ) {  
            // Default to Java sources type.
            sourceGroups = sources.getSourceGroups(
                    JavaProjectConstants.SOURCES_TYPE_JAVA);
        }
        folderPanel=new NewBpelFilePanel(project,sourceGroups);
        
        if(sourceGroups.length > 0 && sourceGroups[0] != null) {
            DataFolder folder = 
                DataFolder.findFolder(sourceGroups[0].getRootFolder());
            DataFolder projectFolder =
                    DataFolder.findFolder(project.getProjectDirectory());
            try {
                if (wizard.getTargetFolder().equals(projectFolder)) {
                    wizard.setTargetFolder(folder);
                }
            } catch (IOException ioe) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ioe);
            }
        }
        
        
        // creates simple wizard panel with bottom panel
        WizardDescriptor.Panel firstPanel = Templates.createSimpleTargetChooser(
                project,sourceGroups,folderPanel);
       
        JComponent c = (JComponent)firstPanel.getComponent();
        // the bottom panel should listen to changes on file name text field
        String fileNameLabel = NbBundle.getMessage(NewBpelFileIterator.class,
                "LBL_SimpleTargetChooserPanel_FileName_Label"); // NOI18N
        ((NewBpelFilePanel)folderPanel).setNameTF((JTextField)Utilities.
                findTextFieldForLabel(c,fileNameLabel));
        
        return new WizardDescriptor.Panel[] {
            firstPanel
        };
        
        
    }
    
    public Set instantiate(TemplateWizard wiz) throws IOException {
        // Here is the default plain behavior. Simply takes the selected
        // template (you need to have included the standard second panel
        // in createPanels(), or at least set the properties targetName and
        // targetFolder correctly), instantiates it in the provided
        // position, and returns the result.
        // More advanced wizards can create multiple objects from template
        // (return them all in the result of this method), populate file
        // contents on the fly, etc.
        
        NewBpelFilePanel panel = (NewBpelFilePanel)folderPanel;
        final org.openide.filesystems.FileObject dir = Templates.getTargetFolder(
                wiz );
        
        DataObject dobj = createBpelFile(Templates.getTargetName(wiz), dir,
                panel.getNS());
        
        return Collections.singleton(dobj);
    }
    
    // You can keep a reference to the TemplateWizard which can
    // provide various kinds of useful information such as
    // the currently selected target name.
    // Also the panels will receive wiz as their "settings" object.
    public void initialize(TemplateWizard wiz) {
        this.wiz = wiz;
        index = 0;
        Project project = Templates.getProject( wiz );
        panels = createPanels(project, wiz);
        
        // Creating steps.
        Object prop = wiz.getProperty("WizardPanel_contentData"); // NOI18N
        String[] beforeSteps = null;
        if (prop != null && prop instanceof String[]) {
            beforeSteps = (String[])prop;
        }
        String[] steps = Utilities.createSteps(beforeSteps, panels);
        
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
                // Step #.
                jc.putClientProperty("WizardPanel_contentSelectedIndex", // NOI18N
                        Integer.valueOf(i));
                // Step name (actually the whole list for reference).
                jc.putClientProperty("WizardPanel_contentData", steps); // NOI18N
            }
        }
    }
    public void uninitialize(TemplateWizard wiz) {
        this.wiz = null;
        panels = null;
    }
    
    // --- WizardDescriptor.Iterator METHODS: ---
    // Note that this is very similar to WizardDescriptor.Iterator, but with a
    // few more options for customization. If you e.g. want to make panels appear
    // or disappear dynamically, go ahead.
    
    public String name() {
        return NbBundle.getMessage(NewBpelFileIterator.class, "TITLE_x_of_y",
                index + 1, panels.length);
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
    
    // If nothing unusual changes in the middle of the wizard, simply:
    public final void addChangeListener(ChangeListener l) {
        
    }
    public final void removeChangeListener(ChangeListener l) {
        
    }
    // If something changes dynamically (besides moving between panels),
    // e.g. the number of panels changes in response to user input, then
    // uncomment the following and call when needed:
    // fireChangeEvent ();
    
    
    
    private DataObject createBpelFile(String bpelFileName, FileObject srcFolder,
            String namespace) throws IOException {
        
        DataFolder df = DataFolder.findFolder( srcFolder );
        FileObject template = Templates.getTemplate( wiz );
        
        boolean importSchemas=false;
        
        DataObject dTemplate = DataObject.find( template );
        DataObject dobj = dTemplate.createFromTemplate( df, Templates.
                getTargetName( wiz )  );
        
        initialiseNames(dobj.getPrimaryFile(), bpelFileName, namespace, "url1");
        
        return dobj;
    }
    
    
    /**
     *   Basically acts like a xslt tranformer by
     *   replacing _PROCNAME_ in fileObject contents with 'name'.
     *   replaceing _NS_ in fileObject contents with 'namespace'
     */
    private void initialiseNames(FileObject fileObject, String name,
            String namespace, String url) {
        String line;
        StringBuffer buffer = new StringBuffer();
        
        try {
            InputStream inputStream = fileObject.getInputStream();
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(inputStream, "UTF-8")); // NOI18N
            
            while((line = reader.readLine()) != null) {
                line = line.replace("_PROCNAME_", name); // NOI18N
                line = line.replace("_NS_", namespace); // NOI18N
                line = line.replace("_URL_", url); // NOI18N
                buffer.append(line);
                buffer.append("\n"); // NOI18N
            }
            
            File file = FileUtil.toFile(fileObject);
            PrintWriter writer = new PrintWriter(file, "UTF-8"); // NOI18N
            writer.write(buffer.toString());
            writer.flush();
            writer.close();
        } catch (IOException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        }
    }
    
    // --- The rest probably does not need to be touched. ---
    
    private transient int index;
    private transient WizardDescriptor.Panel[] panels;
    private transient TemplateWizard wiz;
    
    private WizardDescriptor.Panel folderPanel;
    private transient SourceGroup[] sourceGroups;
}
