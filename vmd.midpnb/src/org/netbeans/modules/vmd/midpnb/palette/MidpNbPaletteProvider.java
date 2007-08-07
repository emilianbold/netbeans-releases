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

import org.netbeans.modules.vmd.api.palette.PaletteProvider;

import javax.swing.*;
import java.util.List;

/**
 *
 * @author Anton Chechel
 */
public class MidpNbPaletteProvider implements PaletteProvider {

    public static final String CATEGORY_SVG = "svg"; // NOI18N

    public MidpNbPaletteProvider() {
    }

    public void initPaletteCategories(String projectType) {
//        if (MidpDocumentSupport.PROJECT_TYPE_MIDP.equals(projectType)) {
//            try {
//                FileObject catFO = rootFolder.getPrimaryFile().getFileSystem().findResource(rootFolder.getName() + '/' + CATEGORY_SVG); // NOI18N
//                if (catFO == null) {
//                    DataFolder categoryFolder = DataFolder.create(rootFolder, CATEGORY_SVG);
//                    categoryFolder.getPrimaryFile().setAttribute("SystemFileSystem.localizingBundle", "org.netbeans.modules.vmd.midpnb.palette.Bundle"); // NOI18N
//                    categoryFolder.getPrimaryFile().setAttribute("SystemFileSystem.icon", new URL ("nbresloc:/org/netbeans/modules/vmd/midpnb/resources/category_svg_16.png")); // NOI18N
//                    categoryFolder.getPrimaryFile().setAttribute("isExpanded", "true"); // NOI18N
//                }
//            } catch (IOException e) {
//                Debug.error("Can't create directory for palette category: " + e); // NOI18N
//            }
//        }
    }

    public List<? extends Action> getActions (String projectType) {
        return null;
    }

}
