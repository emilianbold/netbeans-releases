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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.apache.tools.ant.module.wizards.shortcut;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.util.ChangeSupport;
import org.openide.util.NbBundle;

final class ShortcutIterator implements WizardDescriptor.Iterator<ShortcutWizard> {

    ShortcutIterator() {}

    // You should define what panels you want to use here:

    private List<WizardDescriptor.Panel<ShortcutWizard>> createPanels() {
        List<WizardDescriptor.Panel<ShortcutWizard>> panels = new ArrayList<WizardDescriptor.Panel<ShortcutWizard>>();
        panels.add(new IntroPanel.IntroWizardPanel());
        panels.add(new SelectFolderPanel.SelectFolderWizardPanel(
                NbBundle.getMessage(ShortcutIterator.class, "SI_LBL_select_menu_to_add_to"),
                NbBundle.getMessage(ShortcutIterator.class, "SI_TEXT_menu_locn"),
                NbBundle.getMessage(ShortcutIterator.class, "SI_LBL_display_name_for_menu"),
                DataFolder.findFolder(Repository.getDefault().getDefaultFileSystem().findResource("Menu")), // NOI18N
                true, ShortcutWizard.PROP_FOLDER_MENU));
        panels.add(new SelectFolderPanel.SelectFolderWizardPanel(
                NbBundle.getMessage(ShortcutIterator.class, "SI_LBL_select_toolbar"),
                NbBundle.getMessage(ShortcutIterator.class, "SI_TEXT_toolbar_locn"),
                NbBundle.getMessage(ShortcutIterator.class, "SI_LBL_display_name_for_toolbar"),
                DataFolder.findFolder(Repository.getDefault().getDefaultFileSystem().findResource("Toolbars")), // NOI18N
                false, ShortcutWizard.PROP_FOLDER_TOOL));
        panels.add(new SelectKeyboardShortcutPanel.SelectKeyboardShortcutWizardPanel());
        panels.add(new CustomizeScriptPanel.CustomizeScriptWizardPanel());
        return panels;
    }

    // And the list of step names:

    private String[] createSteps () {
        return new String[] {
            NbBundle.getMessage (ShortcutIterator.class, "SI_LBL_choose_options"),
            NbBundle.getMessage (ShortcutIterator.class, "SI_LBL_add_to_menu"),
            NbBundle.getMessage (ShortcutIterator.class, "SI_LBL_add_to_toolbar"),
            NbBundle.getMessage (ShortcutIterator.class, "SI_LBL_make_keyboard_shortcut"),
            NbBundle.getMessage (ShortcutIterator.class, "SI_LBL_cust_script"),
        };
    }
    
    private transient int index;
    private transient List<WizardDescriptor.Panel<ShortcutWizard>> panels;
    private transient ShortcutWizard wiz;

    // You can keep a reference to the TemplateWizard which can
    // provide various kinds of useful information such as
    // the currently selected target name.
    // Also the panels will receive wiz as their "settings" object.
    void initialize(ShortcutWizard wiz) {
        this.wiz = wiz;
        index = 0;
        panels = createPanels ();
        // #44409: make sure IntroWizardPanel knows about wiz
        // XXX workaround should no longer be necessary...
        ((IntroPanel.IntroWizardPanel) panels.get(0)).initialize(wiz);
        // Make sure list of steps is accurate.
        String[] steps = createSteps ();
        for (int i = 0; i < panels.size(); i++) {
            Component c = panels.get(i).getComponent();
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

    // --- WizardDescriptor.Iterator METHODS: ---

    public String name () {
        return NbBundle.getMessage (ShortcutIterator.class, "TITLE_x_of_y",
            index + 1, panels.size());
    }

    boolean showing(String prop) {
        Boolean s = (Boolean) wiz.getProperty (prop);
        return (s == null) || s.booleanValue ();
    }
    private boolean showing (int index) throws NoSuchElementException {
        switch (index) {
        case 0:
            return true;
        case 1:
            return showing(ShortcutWizard.PROP_SHOW_MENU);
        case 2:
            return showing(ShortcutWizard.PROP_SHOW_TOOL);
        case 3:
            return showing(ShortcutWizard.PROP_SHOW_KEYB);
        case 4:
            return showing(ShortcutWizard.PROP_SHOW_CUST);
        default:
            throw new NoSuchElementException ();
        }
    }
    public boolean hasNext () {
        for (int i = index + 1; i < panels.size(); i++) {
            if (showing (i)) {
                return true;
            }
        }
        return false;
    }
    public boolean hasPrevious () {
        return index > 0;
    }
    public void nextPanel() throws NoSuchElementException {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        index++;
        while (! showing (index)) index++;
        if (index == 1) {
            // User finished intro panel, list of panels may have changed:
            fireChangeEvent ();
        }
    }
    public void previousPanel() throws NoSuchElementException {
        if (!hasPrevious()) {
            throw new NoSuchElementException();
        }
        index--;
        while (! showing (index)) index--;
    }
    public WizardDescriptor.Panel<ShortcutWizard> current() {
        return panels.get(index);
    }

    private transient ChangeSupport cs = new ChangeSupport(this);
    public final void addChangeListener (ChangeListener l) {
        cs.addChangeListener(l);
    }
    public final void removeChangeListener (ChangeListener l) {
        cs.removeChangeListener(l);
    }
    protected final void fireChangeEvent () {
        cs.fireChange();
    }

}
