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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.apisupport.project.ui.wizard;
import java.awt.Component;
import org.openide.util.HelpCtx;

/**
 * first panel of  the library wrapper module wizard
 *
 * @author Milos Kleint
 */
final class LibraryStartWizardPanel extends BasicWizardPanel.NewTemplatePanel {

    /** Representing visual component for this step. */
    private LibraryStartVisualPanel visualPanel;

    /** Creates a new instance of BasicInfoWizardPanel */
    public LibraryStartWizardPanel(final NewModuleProjectData data) {
        super(data);
    }
    
    public void reloadData() {
        visualPanel.refreshData();
    }
    
    public void storeData() {
        visualPanel.storeData();
    }
    
    public Component getComponent() {
        if (visualPanel == null) {
            visualPanel = new LibraryStartVisualPanel(getData());
            visualPanel.addPropertyChangeListener(this);
            visualPanel.setName(getMessage("LBL_LibraryStartPanel_Title")); // NOI18N
        }
        return visualPanel;
    }
    
    public @Override HelpCtx getHelp() {
        return new HelpCtx(LibraryStartWizardPanel.class);
    }
    
}
