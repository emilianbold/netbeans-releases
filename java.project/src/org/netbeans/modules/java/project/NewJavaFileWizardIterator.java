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

package org.netbeans.modules.java.project;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.spi.java.project.support.ui.templates.JavaTemplates;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;

/**
 * Wizard to create a new Java file.
 */
public class NewJavaFileWizardIterator implements WizardDescriptor.InstantiatingIterator {
    
    private static final long serialVersionUID = 1L;
    
    private boolean isLibrary;
    
    /** Create a new wizard iterator. */
    public NewJavaFileWizardIterator() {}
    
    public static NewJavaFileWizardIterator singleton() {
        return new NewJavaFileWizardIterator();
    }
            
    private WizardDescriptor.Panel[] createPanels( WizardDescriptor wizardDescriptor ) {
        
        // Ask for Java folders
        Project project = Templates.getProject( wizardDescriptor );
        Sources sources = ProjectUtils.getSources(project);
        SourceGroup[] groups = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        if (groups == null) {
            groups = sources.getSourceGroups( Sources.TYPE_GENERIC ); 
            return new WizardDescriptor.Panel[] {            
                Templates.createSimpleTargetChooser( project, groups ),
            };
        }
        else {            
            return new WizardDescriptor.Panel[] {
                JavaTemplates.createPackageChooser( project, groups ),
            };
        }
               
    }
    
    private String[] createSteps() {
        return new String[] {
            "Configure Project", // XXX I18N
        };
    }
        
    public Set/*<FileObject>*/ instantiate () throws IOException {
        FileObject dir = Templates.getTargetFolder( wiz );
        
        DataFolder df = DataFolder.findFolder( dir );
        FileObject template = Templates.getTemplate( wiz );
        
        //FileUtil.copy( template.getInputStream(), System.out );
        
        DataObject dTemplate = DataObject.find( template );                
        DataObject dobj = dTemplate.createFromTemplate( df, Templates.getTargetName( wiz )  );

        // Same what template wizard does - not vey nice
        // run default action (hopefully should be here)
        final Node node = dobj.getNodeDelegate ();
        final Action a = node.getPreferredAction();
        if (a != null) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    a.actionPerformed(new ActionEvent(node, ActionEvent.ACTION_PERFORMED, "")); // NOI18N
                }
            });
        }
        
        return Collections.singleton(dobj.getPrimaryFile ());
    }
    
        
    private transient int index;
    private transient WizardDescriptor.Panel[] panels;
    private transient WizardDescriptor wiz;
    
    public void initialize(WizardDescriptor wiz) {
        this.wiz = wiz;
        index = 0;
        panels = createPanels( wiz );
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
    public void uninitialize (WizardDescriptor wiz) {
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
     
    
}
