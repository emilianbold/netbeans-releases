/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.java.j2seproject.ui.wizards;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.java.j2seproject.J2SEProjectGenerator;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.TemplateWizard;

/**
 * Wizard to create a new J2SE project.
 * @author Jesse Glick
 */
public class NewJ2SEProjectWizardIterator implements TemplateWizard.Iterator {
    
    private static final long serialVersionUID = 1L;
    
    private boolean isLibrary;
    
    /** Create a new wizard iterator. */
    public NewJ2SEProjectWizardIterator() {
        this( false );
    }
    
    public NewJ2SEProjectWizardIterator( boolean isLibrary ) {
        this.isLibrary = isLibrary;
    }
        
    public static NewJ2SEProjectWizardIterator library() {
        return new NewJ2SEProjectWizardIterator( true );
    }
    
    public static NewJ2SEProjectWizardIterator trada() {
        System.out.println("Trada");
        return new NewJ2SEProjectWizardIterator();
    }
    
    private WizardDescriptor.Panel[] createPanels() {
        return new WizardDescriptor.Panel[] {
            new PanelConfigureProject( isLibrary ),
        };
    }
    
    private String[] createSteps() {
        return new String[] {
            "Configure Project", // XXX I18N
        };
    }
    
    
    public Set/*<DataObject>*/ instantiate(TemplateWizard wiz) throws IOException/*, IllegalStateException*/ {
        File dirF = (File)wiz.getProperty("projdir");
        String codename = (String)wiz.getProperty("codename");
        String displayName = (String)wiz.getProperty("displayName");
        String mainClass = (String)wiz.getProperty("mainClass");
        J2SEProjectGenerator.createProject(dirF, codename, displayName, mainClass );                        
        FileObject dir = FileUtil.fromFile(dirF)[0];
        Project p = ProjectManager.getDefault().findProject(dir);
        
        // Returning set of DataObject of project diretory. 
        // Project will be open and set as main
        return Collections.singleton(DataObject.find(dir));
    }
    
        
    private transient int index;
    private transient WizardDescriptor.Panel[] panels;
    private transient TemplateWizard wiz;
    
    public void initialize(TemplateWizard wiz) {
        this.wiz = wiz;
        index = 0;
        panels = createPanels();
        // Make sure list of steps is accurate.
        String[] steps = createSteps();
        for (int i = 0; i < panels.length; i++) {
            Component c = panels[i].getComponent();
            if (steps[i] == null) {
                // Default step name to component name of panel.
                // Mainly useful for getting the name of the target
                // chooser to appear in the list of steps.
                steps[i] = c.getName();
            }
            if (c instanceof JComponent) { // assume Swing components
                JComponent jc = (JComponent)c;
                // Step #.
                jc.putClientProperty("WizardPanel_contentSelectedIndex", new Integer(i)); // NOI18N
                // Step name (actually the whole list for reference).
                jc.putClientProperty("WizardPanel_contentData", steps); // NOI18N
            }
        }
    }
    public void uninitialize(TemplateWizard wiz) {
        this.wiz = null;
        panels = null;
    }
    
    public String name() {
        return "" + (index + 1) + " of " + panels.length; // XXX I18N
    }
    
    public boolean hasNext() {
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
    public WizardDescriptor.Panel current() {
        return panels[index];
    }
    
    // If nothing unusual changes in the middle of the wizard, simply:
    public final void addChangeListener(ChangeListener l) {}
    public final void removeChangeListener(ChangeListener l) {}
    // If something changes dynamically (besides moving between panels),
    // e.g. the number of panels changes in response to user input, then
    // uncomment the following and call when needed:
    // fireChangeEvent();
    /*
    private transient Set listeners = new HashSet(1); // Set<ChangeListener>
    public final void addChangeListener(ChangeListener l) {
        synchronized(listeners) {
            listeners.add(l);
        }
    }
    public final void removeChangeListener(ChangeListener l) {
        synchronized(listeners) {
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
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        listeners = new HashSet(1);
    }
     */
    
}
