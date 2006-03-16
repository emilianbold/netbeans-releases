/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.apache.tools.ant.module.wizards.shortcut;

import java.util.Arrays;
import javax.swing.ListModel;
import org.openide.loaders.DataFolder;

/**
 * Test functionality of SelectFolderPanel.
 * @author Jesse Glick
 */
public final class SelectFolderPanelTest extends ShortcutWizardTestBase {
    
    public SelectFolderPanelTest(String name) {
        super(name);
    }
    
    private SelectFolderPanel.SelectFolderWizardPanel menuPanel;
    private SelectFolderPanel.SelectFolderWizardPanel toolbarsPanel;
    private ListModel menuListModel;
    private ListModel toolbarsListModel;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        wiz.putProperty(ShortcutWizard.PROP_SHOW_MENU, Boolean.TRUE);
        wiz.putProperty(ShortcutWizard.PROP_SHOW_TOOL, Boolean.TRUE);
        iter.nextPanel();
        iter.current().readSettings(wiz);
        menuPanel = (SelectFolderPanel.SelectFolderWizardPanel)iter.current();
        menuListModel = menuPanel.getPanel().getModel();
        iter.current().storeSettings(wiz);
        iter.nextPanel();
        iter.current().readSettings(wiz);
        toolbarsPanel = (SelectFolderPanel.SelectFolderWizardPanel)iter.current();
        toolbarsListModel = toolbarsPanel.getPanel().getModel();
        iter.current().storeSettings(wiz);
    }
    
    public void testFolderListDisplay() throws Exception {
        String[] names = new String[menuListModel.getSize()];
        for (int i = 0; i < names.length; i++) {
            names[i] = menuPanel.getPanel().getNestedDisplayName((DataFolder)menuListModel.getElementAt(i));
        }
        String[] expected = {
            "File",
            "Edit",
            "Build",
            "Build \u2192 Other",
            "Help",
        };
        assertEquals("right names in list", Arrays.asList(expected), Arrays.asList(names));
        names = new String[toolbarsListModel.getSize()];
        for (int i = 0; i < names.length; i++) {
            names[i] = toolbarsPanel.getPanel().getNestedDisplayName((DataFolder)toolbarsListModel.getElementAt(i));
        }
        expected = new String[] {
            "Build",
            "Help",
        };
        assertEquals("right names in list", Arrays.asList(expected), Arrays.asList(names));
    }
    
    // XXX test setting correct folder & display name in wizard data
    
}
