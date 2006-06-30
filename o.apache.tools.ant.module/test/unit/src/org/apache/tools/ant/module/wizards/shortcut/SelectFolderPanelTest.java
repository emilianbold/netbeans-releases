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
