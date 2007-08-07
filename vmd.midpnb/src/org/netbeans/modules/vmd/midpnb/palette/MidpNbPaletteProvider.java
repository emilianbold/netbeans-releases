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

package org.netbeans.modules.vmd.midpnb.palette;

import java.io.IOException;
import java.net.URL;
import org.netbeans.modules.vmd.api.palette.PaletteProvider;
import javax.swing.*;
import java.util.List;
import org.netbeans.modules.vmd.api.model.Debug;
import org.netbeans.modules.vmd.midp.components.MidpDocumentSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.Repository;

/**
 *
 * @author Anton Chechel
 */
public class MidpNbPaletteProvider implements PaletteProvider {

    public static final String CATEGORY_SVG = "svg"; // NOI18N
    public static final int CATEGORY_SVG_POSITION = 700;

    public MidpNbPaletteProvider() {
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

            FileObject catFO = paletteFolder.getFileObject(CATEGORY_SVG);
            if (catFO == null) {
                catFO = paletteFolder.createFolder(CATEGORY_SVG);
            }
            catFO.setAttribute("SystemFileSystem.localizingBundle", "org.netbeans.modules.vmd.midpnb.palette.Bundle"); // NOI18N
            catFO.setAttribute("SystemFileSystem.icon", new URL("nbresloc:/org/netbeans/modules/vmd/midp/resources/components/category_" + CATEGORY_SVG + "_16.png")); // NOI18N
            catFO.setAttribute("isExpanded", "true"); // NOI18N
            catFO.setAttribute("position", CATEGORY_SVG_POSITION); // NOI18N
        } catch (IOException e) {
            throw Debug.error(e);
        }
    }

    public List<? extends Action> getActions(String projectType) {
        return null;
    }
}
