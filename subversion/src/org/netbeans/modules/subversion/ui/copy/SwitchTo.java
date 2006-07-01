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
        getSwitchToPanel().getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SwitchTo.class, "CTL_SwitchTo_RepositoryFile"));
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
