/*
 *                         Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License Version
 * 1.0 (the "License"). You may not use this file except in compliance with 
 * the License. A copy of the License is available at http://www.sun.com/
 *
 * The Original Code is the Ant module
 * The Initial Developer of the Original Code is Jayme C. Edwards.
 * Portions created by Jayme C. Edwards are Copyright (c) 2000.
 * All Rights Reserved.
 *
 * Contributor(s): Jesse Glick.
 */

package org.apache.tools.ant.module.wizards.shortcut;

import java.awt.Component;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.openide.WizardDescriptor;
import org.openide.cookies.OpenCookie;
import org.openide.loaders.*;
import org.openide.util.NbBundle;
import java.awt.Dimension;
import org.openide.TopManager;
import javax.swing.KeyStroke;
import org.openide.filesystems.FileLock;
import java.io.OutputStream;
import org.openide.util.Utilities;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;

public class ShortcutIterator implements TemplateWizard.Iterator {
    
    // Attributes stored on the template wizard:
    
    /** type String */
    public static final String PROP_CODE_NAME = "wizdata.codeName"; // NOI18N
    /** type String */
    public static final String PROP_CONTENTS = "wizdata.contents"; // NOI18N
    /** type Boolean */
    public static final String PROP_SHOW_CUST = "wizdata.show.cust"; // NOI18N
    /** type Boolean */
    public static final String PROP_SHOW_MENU = "wizdata.show.menu"; // NOI18N
    /** type Boolean */
    public static final String PROP_SHOW_TOOL = "wizdata.show.tool"; // NOI18N
    /** type Boolean */
    public static final String PROP_SHOW_PROJ = "wizdata.show.proj"; // NOI18N
    /** type Boolean */
    public static final String PROP_SHOW_KEYB = "wizdata.show.keyb"; // NOI18N
    /** type DataFolder */
    public static final String PROP_FOLDER_MENU = "wizdata.folder.menu"; // NOI18N
    /** type DataFolder */
    public static final String PROP_FOLDER_TOOL = "wizdata.folder.tool"; // NOI18N
    /** type DataFolder */
    public static final String PROP_FOLDER_PROJ = "wizdata.folder.proj"; // NOI18N
    /** type KeyStroke */
    public static final String PROP_STROKE = "wizdata.stroke"; // NOI18N

    private static final long serialVersionUID = 47387529866399027L;

    // You should define what panels you want to use here:

    protected WizardDescriptor.Panel[] createPanels () {
        return new WizardDescriptor.Panel[] {
            new IntroPanel (),
            new SelectTargetPanel (),
            new CustomizeScriptPanel (),
            new SelectFolderPanel (NbBundle.getMessage (ShortcutIterator.class, "SI_LBL_select_menu_to_add_to"), NbBundle.getMessage (ShortcutIterator.class, "SI_TEXT_menu_locn"), TopManager.getDefault ().getPlaces ().folders ().menus ().getNodeDelegate (), false, true, PROP_FOLDER_MENU),
            new SelectFolderPanel (NbBundle.getMessage (ShortcutIterator.class, "SI_LBL_select_toolbar"), NbBundle.getMessage (ShortcutIterator.class, "SI_TEXT_toolbar_locn"), TopManager.getDefault ().getPlaces ().folders ().toolbars ().getNodeDelegate (), false, false, PROP_FOLDER_TOOL),
            new SelectFolderPanel (NbBundle.getMessage (ShortcutIterator.class, "SI_LBL_select_proj_folder"), NbBundle.getMessage (ShortcutIterator.class, "SI_TEXT_select_project_locn"), TopManager.getDefault ().getPlaces ().nodes ().projectDesktop (), true, false, PROP_FOLDER_PROJ),
            new SelectKeyboardShortcutPanel (),
        };
    }

    // And the list of step names:

    protected String[] createSteps () {
        return new String[] {
            NbBundle.getMessage (ShortcutIterator.class, "SI_LBL_choose_options"),
            NbBundle.getMessage (ShortcutIterator.class, "SI_LBL_select_ant_target"),
            NbBundle.getMessage (ShortcutIterator.class, "SI_LBL_cust_script"),
            NbBundle.getMessage (ShortcutIterator.class, "SI_LBL_add_to_menu"),
            NbBundle.getMessage (ShortcutIterator.class, "SI_LBL_add_to_toolbar"),
            NbBundle.getMessage (ShortcutIterator.class, "SI_LBL_add_to_proj"),
            NbBundle.getMessage (ShortcutIterator.class, "SI_LBL_make_keyboard_shortcut"),
        };
    }
    
    private void dumpContents () {
        String[] keys = {"wizdata.codeName", "wizdata.contents", "wizdata.show.cust", "wizdata.show.menu", "wizdata.show.tool", "wizdata.show.proj", "wizdata.show.keyb", "wizdata.folder.menu", "wizdata.folder.tool", "wizdata.folder.proj", "wizdata.stroke"}; // NOI18N
        System.err.println("TemplateWizard:"); // NOI18N
        for (int i = 0; i < keys.length; i++) {
            System.err.println("\t" + keys[i] + " = " + wiz.getProperty (keys[i])); // NOI18N
        }
    }


    public Set instantiate (TemplateWizard wiz) throws IOException/*, IllegalStateException*/ {
        //dumpContents ();
        if (showing (PROP_SHOW_KEYB)) {
            FileObject shortcutsFolder = TopManager.getDefault ().getRepository ().getDefaultFileSystem ().findResource ("Shortcuts"); // NOI18N
            KeyStroke stroke = (KeyStroke) wiz.getProperty (PROP_STROKE);
            create (DataFolder.findFolder (shortcutsFolder), Utilities.keyToString (stroke));
        }
        if (showing (PROP_SHOW_MENU)) {
            create ((DataFolder) wiz.getProperty (PROP_FOLDER_MENU), null);
        }
        if (showing (PROP_SHOW_TOOL)) {
            create ((DataFolder) wiz.getProperty (PROP_FOLDER_TOOL), null);
        }
        if (showing (PROP_SHOW_PROJ)) {
            return Collections.singleton (create ((DataFolder) wiz.getProperty (PROP_FOLDER_PROJ), null));
        } else {
            return Collections.EMPTY_SET;
        }
    }
    private DataObject create (DataFolder f, String name) throws IOException {
        final String fname = (name != null) ? name : (String) wiz.getProperty (PROP_CODE_NAME);
        final String contents = (String) wiz.getProperty (PROP_CONTENTS);
        final FileObject folder = f.getPrimaryFile ();
        final FileObject[] shortcut = new FileObject[1];
        folder.getFileSystem ().runAtomicAction (new FileSystem.AtomicAction () {
            public void run () throws IOException {
                shortcut[0] = folder.createData (fname, "xml"); // NOI18N
                FileLock lock = shortcut[0].lock ();
                try {
                    OutputStream os = shortcut[0].getOutputStream (lock);
                    try {
                        os.write (contents.getBytes ("UTF-8")); // NOI18N
                    } finally {
                        os.close ();
                    }
                } finally {
                    lock.releaseLock ();
                }
            }
        });
        return DataObject.find (shortcut[0]);
    }

    private transient int index;
    private transient WizardDescriptor.Panel[] panels;
    private transient WizardDescriptor.Panel[] currentPanels;
    private transient TemplateWizard wiz;

    // You can keep a reference to the TemplateWizard which can
    // provide various kinds of useful information such as
    // the currently selected target name.
    // Also the panels will receive wiz as their "settings" object.
    public void initialize (TemplateWizard wiz) {
        this.wiz = wiz;
        index = 0;
        panels = createPanels ();
        // Make sure list of steps is accurate.
        String[] steps = createSteps ();
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
                jc.putClientProperty ("WizardPanel_contentSelectedIndex", new Integer (i)); // NOI18N
                // Step name (actually the whole list for reference).
                jc.putClientProperty ("WizardPanel_contentData", steps); // NOI18N
            }
        }
    }
    public void uninitialize (TemplateWizard wiz) {
        this.wiz = null;
        panels = null;
    }

    // --- WizardDescriptor.Iterator METHODS: ---

    public String name () {
        return NbBundle.getMessage (ShortcutIterator.class, "TITLE_x_of_y",
            new Integer (index + 1), new Integer (panels.length));
    }

    private boolean showing (String prop) {
        Boolean s = (Boolean) wiz.getProperty (prop);
        return (s == null) || s.booleanValue ();
    }
    private boolean showing (int index) throws NoSuchElementException {
        switch (index) {
        case 0:
        case 1:
            return true;
        case 2:
            return showing (PROP_SHOW_CUST);
        case 3:
            return showing (PROP_SHOW_MENU);
        case 4:
            return showing (PROP_SHOW_TOOL);
        case 5:
            return showing (PROP_SHOW_PROJ);
        case 6:
            return showing (PROP_SHOW_KEYB);
        default:
            throw new NoSuchElementException ();
        }
    }
    public boolean hasNext () {
        for (int i = index + 1; i < panels.length; i++) {
            if (showing (i)) {
                return true;
            }
        }
        return false;
    }
    public boolean hasPrevious () {
        return index > 0;
    }
    public void nextPanel () {
        index++;
        while (! showing (index)) index++;
        if (index == 1) {
            // User finished intro panel, list of panels may have changed:
            fireChangeEvent ();
        }
    }
    public void previousPanel () {
        index--;
        while (! showing (index)) index--;
    }
    public WizardDescriptor.Panel current () {
        return panels[index];
    }

    private transient Set listeners = new HashSet (1); // Set<ChangeListener>
    public final void addChangeListener (ChangeListener l) {
        synchronized (listeners) {
            listeners.add (l);
        }
    }
    public final void removeChangeListener (ChangeListener l) {
        synchronized (listeners) {
            listeners.remove (l);
        }
    }
    protected final void fireChangeEvent () {
        Iterator it;
        synchronized (listeners) {
            it = new HashSet (listeners).iterator ();
        }
        ChangeEvent ev = new ChangeEvent (this);
        while (it.hasNext ()) {
            ((ChangeListener) it.next ()).stateChanged (ev);
        }
    }
    private Object readObject (ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject ();
        listeners = new HashSet (1);
        return this;
    }

}
