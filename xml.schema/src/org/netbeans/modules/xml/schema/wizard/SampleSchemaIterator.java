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
package org.netbeans.modules.xml.schema.wizard;

//java imports
import java.awt.Component;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.xml.xam.ui.ProjectConstants;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.util.NbBundle;
import org.openide.loaders.TemplateWizard;
import org.netbeans.spi.project.ui.templates.support.Templates;


/**
 * Sample schema wizard iterator. See layer.xml for template declaration.
 *
 * @author  Samaresh (Samaresh.Panda@Sun.Com)
 */
public class SampleSchemaIterator extends Object implements TemplateWizard.Iterator {
        
    private static final long serialVersionUID = 1L;
    private int index;
    private final Set<ChangeListener> changeListeners = new HashSet<ChangeListener>();
    private transient WizardDescriptor.Panel[] panels;
        
    /**
     * You should define what panels you want to use here:
     */
    protected WizardDescriptor.Panel[] createPanels (Project project,
						     final TemplateWizard wizard) {
            DataFolder df = null;
            Sources sources = ProjectUtils.getSources(project);
            SourceGroup[] folders = sources.getSourceGroups(ProjectConstants.JAVA_SOURCES_TYPE);
            if (folders == null || folders.length == 0) {
                folders = sources.getSourceGroups(Sources.TYPE_GENERIC);
            }
            try {
                df = wizard.getTargetFolder();
            } catch (IOException ex) {
                //just catch
            }
            if (df != null) {
                wizard.setTargetFolder(df);
                org.openide.WizardDescriptor.Panel panel = Templates.createSimpleTargetChooser(project, folders);
                return new org.openide.WizardDescriptor.Panel[]{panel};
            }

            //make the first one as the default target folder. IZ: 98643
            if (folders != null && folders.length != 0) {
                df = DataFolder.findFolder(folders[0].getRootFolder());
                wizard.setTargetFolder(df);
            }
            WizardDescriptor.Panel panel = Templates.createSimpleTargetChooser(project, folders);
            return new WizardDescriptor.Panel[] {panel};
    }
    
    /**
     * Initialization of the wizard iterator.
     */
    public void initialize(TemplateWizard wizard) {
        index = 0;
        Project project = Templates.getProject( wizard );
        panels = createPanels (project, wizard);
        
        // Creating steps.
        Object prop = wizard.getProperty ("WizardPanel_contentData"); // NOI18N
        String[] beforeSteps = null;
        if (prop instanceof String[]) {
            beforeSteps = (String[])prop;
        }
        String[] steps = createSteps (beforeSteps, panels);
        
        for (int i = 0; i < panels.length; i++) {
            Component c = panels[i].getComponent ();
            if (steps[i] == null) {
                // Default step name to component name of panel.
                // Mainly useful for getting the name of the target
                // chooser to appear in the list of steps.
                steps[i] = c.getName ();
            }
            if (c instanceof JComponent) { // assume Swing components
                JComponent jc = (JComponent) c;
                // Step #.
                jc.putClientProperty ("WizardPanel_contentSelectedIndex", Integer.valueOf(i)); // NOI18N
                // Step name (actually the whole list for reference).
                jc.putClientProperty ("WizardPanel_contentData", steps); // NOI18N
            }
        }        
    }

    /**
     * Cleanup.
     */
    public void uninitialize (TemplateWizard wiz) {
        panels = null;
    }

    /**
     * This is where, the schema gets instantiated from the template.
     */
    public Set instantiate (TemplateWizard wizard) throws IOException {
        FileObject dir = Templates.getTargetFolder( wizard );
        DataFolder df = DataFolder.findFolder( dir );
        FileObject template = Templates.getTemplate( wizard );        
        DataObject dTemplate = DataObject.find( template );                
        DataObject dobj = dTemplate.createFromTemplate( df, Templates.getTargetName( wizard )  );
        if (dobj == null)
            return Collections.emptySet();
        return Collections.singleton(dobj.getPrimaryFile());
    }
                
    /**
     *
     */
    public void addChangeListener(ChangeListener listener) {
        changeListeners.add(listener);
    }

    /**
     *
     *
     */
    public void removeChangeListener(ChangeListener listener) {
        changeListeners.remove(listener);
    }
    
    /**
     *
     */
    public void fireStateChanged() {
        ChangeEvent event = new ChangeEvent(this);

        Iterator<ChangeListener> i = changeListeners.iterator();
        while (i.hasNext()) {
            try {
                i.next().stateChanged(event);
            } catch (Exception e) {
                //Debug.debugNotify(e);
            }
        }
    }
    
    /**
     *
     */
    public String name () {
        return NbBundle.getMessage(SchemaWizardIterator.class, "TITLE_x_of_y",
            Integer.valueOf(index + 1), Integer.valueOf(panels.length));
    }
    
    /**
     *
     */
    public boolean hasNext () {
        return index < panels.length - 1;
    }
    
    /**
     *
     */
    public boolean hasPrevious () {
        return index > 0;
    }
    
    /**
     *
     */
    public void nextPanel () {
        if (! hasNext ()) throw new NoSuchElementException ();
        index++;
    }
    
    /**
     *
     */
    public void previousPanel () {
        if (! hasPrevious ()) throw new NoSuchElementException ();
        index--;
    }
    
    /**
     * Returns the current panel.
     */
    public WizardDescriptor.Panel current () {
        return panels[index];
    }

    /**
     * Create steps.
     */
    private String[] createSteps(String[] before, WizardDescriptor.Panel[] panels) {
        //assert panels != null;
        // hack to use the steps set before this panel processed
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
}
