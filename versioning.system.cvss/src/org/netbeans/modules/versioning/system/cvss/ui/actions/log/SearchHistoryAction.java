/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.versioning.system.cvss.ui.actions.log;

import org.netbeans.modules.versioning.system.cvss.ui.actions.AbstractSystemAction;
import org.netbeans.modules.versioning.system.cvss.ui.history.SearchHistoryTopComponent;
import org.netbeans.modules.versioning.system.cvss.FileInformation;
import org.openide.util.NbBundle;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;

/**
 * Search History action.
 *
 * @author Maros Sandor
 */
public class SearchHistoryAction extends AbstractSystemAction  {

    protected String getBaseName() {
        return "CTL_MenuItem_SearchHistory";
    }

    protected int getFileEnabledStatus() {
        return FileInformation.STATUS_IN_REPOSITORY;
    }

    public void actionPerformed(ActionEvent ev) {
        String title = NbBundle.getMessage(SearchHistoryAction.class, "CTL_SearchHistory_Title", getContextDisplayName());
        openHistory(getFilesToProcess(), title);
    }

    private void openHistory(final File [] roots, final String title) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                SearchHistoryTopComponent tc = new SearchHistoryTopComponent(roots);
                tc.setName(title);
                tc.open();
                tc.requestActive();
            }
        });
    }
}
