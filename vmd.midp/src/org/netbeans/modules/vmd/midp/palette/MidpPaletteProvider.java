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
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.util.actions.SystemAction;

import javax.swing.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.net.URL;

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

    private String[] paletteCategories = {CATEGORY_DISPLAYABLES, CATEGORY_COMMANDS, CATEGORY_ELEMENTS,
                                          CATEGORY_ITEMS, CATEGORY_PROCESS_FLOW, CATEGORY_RESOURCES,
                                          CATEGORY_CUSTOM};

    public MidpPaletteProvider() {
    }

    public void initPaletteCategories(String projectType, DataFolder rootFolder) {
        if (MidpDocumentSupport.PROJECT_TYPE_MIDP.equals(projectType)) {
            List<DataObject> order = new ArrayList<DataObject>(Arrays.asList(rootFolder.getChildren()));
            // create category folders
            for (String categoryName : paletteCategories) {
                try {
                    FileObject catFO = rootFolder.getPrimaryFile().getFileSystem().findResource(rootFolder.getName() + '/' + categoryName); // NOI18N
                    if (catFO == null) {
                        DataFolder categoryFolder = DataFolder.create(rootFolder, categoryName);
                        categoryFolder.getPrimaryFile().setAttribute("SystemFileSystem.localizingBundle", "org.netbeans.modules.vmd.midp.palette.Bundle"); // NOI18N
                        categoryFolder.getPrimaryFile().setAttribute("SystemFileSystem.icon", new URL ("nbresloc:/org/netbeans/modules/vmd/midp/resources/components/category_" + categoryName + "_16.png")); // NOI18N
                        categoryFolder.getPrimaryFile().setAttribute("isExpanded", "true"); // NOI18N
                        order.add(categoryFolder);
                    }
                } catch (IOException e) {
                    Debug.error("Can't create directory for palette category: " + e); // NOI18N
                }
            }
            try {
                rootFolder.setOrder(order.toArray(new DataObject[order.size()]));
            } catch (IOException e) {
                Debug.error("Can't set attribute for palette category directory: " + e); // NOI18N
            }
        }
    }

    public List<? extends Action> getActions (String projectType) {
        if (! MidpDocumentSupport.PROJECT_TYPE_MIDP.equals (projectType))
            return null;

        return Collections.singletonList(SystemAction.get (AddToPaletteWizardAction.class));
    }

}
