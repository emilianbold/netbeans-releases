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

package org.netbeans.modules.vmd.midp.palette;

import org.netbeans.modules.vmd.api.model.Debug;
import org.netbeans.modules.vmd.api.palette.PaletteProvider;
import org.netbeans.modules.vmd.midp.components.MidpDocumentSupport;
import org.netbeans.modules.vmd.midp.palette.wizard.AddToPaletteWizardAction;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.Repository;
import org.openide.util.actions.SystemAction;
import javax.swing.*;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Anton Chechel
 */
public class MidpPaletteProvider implements PaletteProvider {

    public static final String CATEGORY_COMMANDS = "commands"; // NOI18N
    public static final String CATEGORY_DISPLAYABLES = "displayables"; // NOI18N
    public static final String CATEGORY_ELEMENTS = "elements"; // NOI18N
    public static final String CATEGORY_ITEMS = "items"; // NOI18N
    public static final String CATEGORY_PROCESS_FLOW = "flow"; // NOI18N
    public static final String CATEGORY_RESOURCES = "resources"; // NOI18N
    public static final String CATEGORY_CUSTOM = "custom"; // NOI18N
    private String[] paletteCategories = {CATEGORY_DISPLAYABLES, CATEGORY_COMMANDS, CATEGORY_ELEMENTS, CATEGORY_ITEMS, CATEGORY_PROCESS_FLOW, CATEGORY_RESOURCES, CATEGORY_CUSTOM};
    private int[] categoryPositions = {100, 200, 300, 400, 500, 600, 1000}; // custom should be the last

    public MidpPaletteProvider() {
    }

    public void initPaletteCategories(String projectType) {
        if (!MidpDocumentSupport.PROJECT_TYPE_MIDP.equals(projectType)) {
            return;
        }
        
        try {
            FileObject paletteFolder = Repository.getDefault().getDefaultFileSystem().findResource(projectType + "/palette"); // NOI18N
            if (paletteFolder == null) {
                FileObject root = Repository.getDefault().getDefaultFileSystem().getRoot();
                assert root != null;
                FileObject projectFolder = root.getFileObject(projectType);
                if (projectFolder == null) {
                    projectFolder = root.createFolder(projectType);
                }
                paletteFolder = projectFolder.createFolder("palette"); // NOI18N
            }
            paletteFolder.refresh(true);

            for (int i = 0; i < paletteCategories.length; i++) {
                String categoryName = paletteCategories[i];
                FileObject catFO = paletteFolder.getFileObject(categoryName);
                if (catFO == null) {
                    catFO = paletteFolder.createFolder(categoryName);
                }
                catFO.setAttribute("SystemFileSystem.localizingBundle", "org.netbeans.modules.vmd.midp.palette.Bundle"); // NOI18N
                catFO.setAttribute("SystemFileSystem.icon", new URL("nbresloc:/org/netbeans/modules/vmd/midp/resources/components/category_" + categoryName + "_16.png")); // NOI18N
                catFO.setAttribute("isExpanded", "true"); // NOI18N
                catFO.setAttribute("position", categoryPositions[i]); // NOI18N
            }
        } catch (IOException e) {
            throw Debug.error(e);
        }
    }

    public List<? extends Action> getActions(String projectType) {
        if (!MidpDocumentSupport.PROJECT_TYPE_MIDP.equals(projectType)) {
            return null;
        }

        return Collections.singletonList(SystemAction.get(AddToPaletteWizardAction.class));
    }
}
