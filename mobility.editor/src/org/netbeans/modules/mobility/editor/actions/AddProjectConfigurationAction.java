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

/*
 * AddProjectConfigurationAction.java
 *
 * Created on July 26, 2005, 6:59 PM
 *
 */
package org.netbeans.modules.mobility.editor.actions;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ProjectConfigurationProvider;
import org.netbeans.modules.mobility.project.J2MEProjectUtils;
import org.netbeans.modules.mobility.project.ui.J2MECustomizerProvider;
import org.openide.util.NbBundle;

/**
 *
 * @author Adam Sotona
 */
public class AddProjectConfigurationAction extends PreprocessorEditorContextAction {
    
    public static final String NAME = "add-project-configuration-action"; //NOI18N
    
    /** Creates a new instance of AddProjectConfigurationAction */
    public AddProjectConfigurationAction() {
        super(NAME); //NOI18N
    }
    
    public boolean isEnabled(final ProjectConfigurationProvider cfgProvider, @SuppressWarnings("unused")
	final ArrayList preprocessorLineList, @SuppressWarnings("unused")
	final JTextComponent target) {
        return cfgProvider != null;
    }
    
    public String getPopupMenuText(@SuppressWarnings("unused")
	final ProjectConfigurationProvider cfgProvider, @SuppressWarnings("unused")
	final ArrayList preprocessorLineList, @SuppressWarnings("unused")
	final JTextComponent target) {
        return NbBundle.getMessage(AddProjectConfigurationAction.class, "LBL_Add_Configuration_To_Project"); //NOI18N
    }
    
    public void actionPerformed(@SuppressWarnings("unused")
	final ActionEvent evt, final JTextComponent txt) {
        if (txt != null) {
            final Project p = J2MEProjectUtils.getProjectForDocument(txt.getDocument());
            if (p != null) {
                final J2MECustomizerProvider cp = p.getLookup().lookup(J2MECustomizerProvider.class);
                if (cp != null) SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        cp.showCustomizer(true);
                    }
                });
            }
        }
    }
    
}
