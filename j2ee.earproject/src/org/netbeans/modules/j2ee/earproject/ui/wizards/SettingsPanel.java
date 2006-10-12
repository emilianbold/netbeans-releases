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

package org.netbeans.modules.j2ee.earproject.ui.wizards;

import java.io.File;
import javax.swing.JPanel;
import javax.swing.event.DocumentListener;
import org.openide.WizardDescriptor;

abstract class SettingsPanel extends JPanel {

    abstract void store (WizardDescriptor settings);

    abstract void read (WizardDescriptor settings);

    abstract boolean valid (WizardDescriptor settings);
    
    abstract void addNameListener(DocumentListener dl);
    
    protected static File findExistingParent(String path) {
        File ret = new File(path).getParentFile();
        while (ret != null && !ret.exists()) {
            ret = ret.getParentFile();
        }
        return ret;
    }
    
    static boolean isValidProjectName(String projectName) {
        return projectName.length() != 0
                && projectName.indexOf('*')  < 0
                && projectName.indexOf('/')  < 0
                && projectName.indexOf('\\') < 0
                && projectName.indexOf(':')  < 0;
    }

}
