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
package org.netbeans.modules.subversion.ui.copy;

import java.io.File;
import java.net.MalformedURLException;
import javax.swing.JTextField;
import org.netbeans.modules.subversion.RepositoryFile;
import org.netbeans.modules.subversion.ui.browser.BrowserAction;
import org.netbeans.modules.subversion.ui.browser.CreateFolderAction;
import org.netbeans.modules.subversion.ui.browser.RepositoryPaths;
import org.openide.ErrorManager;
import org.openide.util.NbBundle;

/**
 *
 * @author Tomas Stupka
 */
public class SwitchTo extends CopyDialog {
    
    private RepositoryPaths repositoryPaths;
    
    public SwitchTo(RepositoryFile repositoryRoot, File root, boolean localChanges) {
        super(new SwitchToPanel(), NbBundle.getMessage(SwitchTo.class, "CTL_SwitchTo_Title", root.getName()), NbBundle.getMessage(SwitchTo.class, "CTL_SwitchTo_Action")); // NOI18N

        SwitchToPanel panel = getSwitchToPanel();
        panel.warningLabel.setVisible(localChanges);

        repositoryPaths = 
            new RepositoryPaths(
                repositoryRoot, 
                (JTextField) panel.urlComboBox.getEditor().getEditorComponent(),
                panel.browseRepositoryButton,
                panel.revisionTextField,
                panel.searchRevisionButton
            );
        repositoryPaths.addPropertyChangeListener(this);
        
        if(root.isFile()) {
            getSwitchToPanel().urlLabel.setText(NbBundle.getMessage(SwitchTo.class, "CTL_SwitchTo_RepositoryFile")); // NOI18N
            repositoryPaths.setupBrowserBehavior(true, true, true, null);
        } else {
            repositoryPaths.setupBrowserBehavior(true, false, false, null);
        }
                
        setupUrlComboBox(panel.urlComboBox, SwitchTo.class.getName());                
    }            
    
    RepositoryFile getRepositoryFile() {        
        try {
            return repositoryPaths.getRepositoryFiles()[0];
        } catch (MalformedURLException ex) {
            // should be already checked and 
            // not happen at this place anymore
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);            
        }
        return null;
    }    
    
    private SwitchToPanel getSwitchToPanel() {
        return (SwitchToPanel) getPanel();
    }    
}
