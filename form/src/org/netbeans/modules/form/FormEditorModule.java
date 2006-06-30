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

package org.netbeans.modules.form;

import java.beans.Beans;
import java.beans.Introspector;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.openide.modules.ModuleInstall;

/**
 * Module installation class for Form Editor
 */
public class FormEditorModule extends ModuleInstall {

    private static final String BEANINFO_PATH_AWT = "org.netbeans.modules.form.beaninfo.awt"; // NOI18N
    private static final String BEANINFO_PATH_SWING = "org.netbeans.modules.form.beaninfo.swing"; // NOI18N

    public void restored() {
        Beans.setDesignTime(true);

        FormPropertyEditorManager.registerEditor(
            javax.swing.KeyStroke.class,
            org.netbeans.modules.form.editors.KeyStrokeEditor.class);

        // Add beaninfo search path.
        String[] sp = Introspector.getBeanInfoSearchPath();
        List paths = new ArrayList(Arrays.asList(sp));
        if (!paths.contains(BEANINFO_PATH_AWT)) {
            paths.add(BEANINFO_PATH_AWT);
        }
        if (!paths.contains(BEANINFO_PATH_SWING)) {
            paths.add(BEANINFO_PATH_SWING);
        }
        Introspector.setBeanInfoSearchPath((String[])paths.toArray(new String[paths.size()]));
    }

    public void uninstalled() {
        // Remove beaninfo search path.
        String[] sp = Introspector.getBeanInfoSearchPath();
        List paths = new ArrayList(Arrays.asList(sp));
        paths.remove(BEANINFO_PATH_AWT);
        paths.remove(BEANINFO_PATH_SWING);
        Introspector.setBeanInfoSearchPath((String[])paths.toArray(new String[paths.size()]));
    }

}
