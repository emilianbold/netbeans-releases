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

package org.netbeans.modules.web.project.ui.wizards;

import java.awt.Component;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.MessageFormat;
import java.util.*;
import java.util.Collections;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.swing.*;

import javax.swing.JComponent;
import javax.swing.event.*;
import javax.swing.event.ChangeListener;

import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.TemplateWizard;

import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;

import org.netbeans.modules.web.project.WebProjectGenerator;
import org.openide.ErrorManager;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.util.NbBundle;

/**
 * Wizard to create a new Web project for an existing web module.
 * @author Pavel Buzek
 */
public class ImportWebProjectWizardIterator implements TemplateWizard.Iterator {
    
    private static final long serialVersionUID = 1L;
    
    /** Create a new wizard iterator. */
    public ImportWebProjectWizardIterator () {}
    
    private WizardDescriptor.Panel[] createPanels() {
        return new WizardDescriptor.Panel[] {
            new ImportWebProjectWizardIterator.ThePanel ()
        };
    }
    
    private String[] createSteps() {
        return new String[] {
            NbBundle.getBundle("org/netbeans/modules/web/project/ui/wizards/Bundle").getString("LBL_Configure_Project") //NOI18N
        };
    }
    
    
    public Set/*<DataObject>*/ instantiate(TemplateWizard wiz) throws IOException/*, IllegalStateException*/ {
        File dirF = (File) wiz.getProperty(WizardProperties.PROJECT_DIR);
        File dirSrcF = (File) wiz.getProperty (WizardProperties.SOURCE_ROOT);
        String codename = (String) wiz.getProperty(WizardProperties.CODE_NAME);
        String displayName = (String) wiz.getProperty(WizardProperties.DISPLAY_NAME);
        
        FileObject wmFO = FileUtil.toFileObject (dirSrcF);
        assert wmFO != null : "No such dir on disk: " + dirSrcF;
        assert wmFO.isFolder() : "Not really a dir: " + dirSrcF;
        FileObject javaRoot = guessJavaRoot (wmFO);
        FileObject docBase = guessDocBase (wmFO);
        FileObject libFolder = guessLibrariesFolder (wmFO);
        WebProjectGenerator.importProject (dirF, codename, displayName, wmFO, javaRoot, docBase, libFolder, WizardProperties.J2EE_14_LEVEL); //PENDING detect spec level
        FileObject dir = FileUtil.toFileObject (dirF);
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
        return MessageFormat.format(NbBundle.getBundle("org/netbeans/modules/web/project/ui/wizards/Bundle").getString("LBL_WizardStepsCount"), new String[] {(new Integer(index + 1)).toString(), (new Integer(panels.length)).toString()}); //NOI18N
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

    private FileObject guessDocBase (FileObject dir) {
        Enumeration ch = dir.getChildren (true);
        while (ch.hasMoreElements ()) {
            FileObject f = (FileObject) ch.nextElement ();
            if (f.isFolder () && f.getName ().equals ("WEB-INF")) {
                if (f.getFileObject ("web.xml").isData ()) {
                    return f.getParent ();
                }
            }
        }
        return null;
    }
    
    private FileObject guessLibrariesFolder (FileObject dir) {
        FileObject docBase = guessDocBase (dir);
        if (docBase != null) {
            FileObject lib = docBase.getFileObject ("WEB-INF/lib"); //NOI18N
            if (lib != null) {
                return lib;
            }
        }
        Enumeration ch = dir.getChildren (true);
        while (ch.hasMoreElements ()) {
            FileObject f = (FileObject) ch.nextElement ();
            if (f.getExt ().equals ("jar")) { //NOI18N
                return f.getParent ();
            }
        }
        return null;
    }
    
    private FileObject guessJavaRoot (FileObject dir) {
        Enumeration ch = dir.getChildren (true);
        try {
            while (ch.hasMoreElements ()) {
                FileObject f = (FileObject) ch.nextElement ();
                if (f.getExt ().equals ("java")) { //NOI18N
                    String pckg = guessPackageName (f);
                    String pkgPath = f.getParent ().getPath (); 
                    if (pckg != null && pkgPath.endsWith (pckg.replace ('.', '/'))) {
                        String rootName = pkgPath.substring (0, pkgPath.length () - pckg.length ());
                        return f.getFileSystem ().findResource (rootName);
                    }
                }
            }
        } catch (FileStateInvalidException fsie) {
            ErrorManager.getDefault ().notify (ErrorManager.INFORMATIONAL, fsie);
        }
        return null;
    }
    
    private String guessPackageName (FileObject f) {
        java.io.Reader r = null;
        try {
            r = new BufferedReader (new InputStreamReader (f.getInputStream (), "utf-8")); // NOI18N
            StringBuffer sb = new StringBuffer ();
            final char[] BUFFER = new char[4096];
            int len;

            for (;;) {
                len = r.read (BUFFER);
                if (len == -1) break;
                sb.append (BUFFER, 0, len);
            }
            int idx = sb.indexOf ("package"); // NOI18N
            if (idx >= 0) {
                int idx2 = sb.indexOf (";", idx);  // NOI18N
                if (idx2 >= 0) {
                    return sb.substring (idx + "package".length (), idx2).trim ();
                }
            }
        } catch (java.io.IOException ioe) {
            ErrorManager.getDefault ().notify (ErrorManager.INFORMATIONAL, ioe);
        } finally {
            try { if (r != null) r.close (); } catch (java.io.IOException ioe) { // ignore this 
            }
        }
        return null;
    }
    
    public final class ThePanel implements WizardDescriptor.FinishPanel {

        private ImportLocationVisual panel;
        
        private ThePanel () {
        }
        
        public java.awt.Component getComponent () {
            if (panel == null) {
                panel = new ImportLocationVisual (this);
            }
            return panel;
        }
        
        public org.openide.util.HelpCtx getHelp () {
            return null;
        }
        
        public boolean isValid () {
            File f = new File (panel.moduleLocationTextField.getText ());
            return f.isDirectory () && isWebModule (FileUtil.toFileObject (f));
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
        public void readSettings (Object settings) {
        }
        
        public void storeSettings (Object settings) {
            WizardDescriptor d = (WizardDescriptor)settings;
            String name = panel.projectNameTextField.getText().trim();

            d.putProperty(WizardProperties.PROJECT_DIR, new File(panel.createdFolderTextField.getText()));
            d.putProperty(WizardProperties.SOURCE_ROOT, new File(panel.moduleLocationTextField.getText()));
            d.putProperty(WizardProperties.DISPLAY_NAME, name);
            d.putProperty(WizardProperties.CODE_NAME, name.replace(' ', '-')); //NOI18N
        }
        
        private boolean isWebModule (FileObject dir) {
            return guessDocBase (dir) != null && guessJavaRoot (dir) != null;
        }
    
        //use it as a project root iff it is not sources or document root
        public boolean isSuitableProjectRoot (FileObject dir) {
            FileObject docRoot = guessDocBase (dir);
            FileObject srcRoot = guessJavaRoot (dir);
            return (docRoot == null || FileUtil.isParentOf (dir, docRoot))
                && (srcRoot == null || FileUtil.isParentOf (dir, srcRoot));
        }
    
    }
}
