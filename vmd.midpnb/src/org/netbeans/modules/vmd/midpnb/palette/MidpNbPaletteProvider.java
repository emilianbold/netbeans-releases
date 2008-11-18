/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.vmd.api.palette.PaletteProvider.class)
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
            catFO.setAttribute("SystemFileSystem.icon", new URL("nbresloc:/org/netbeans/modules/vmd/midpnb/resources/category_" + CATEGORY_SVG + "_16.png")); // NOI18N
            catFO.setAttribute("isExpanded", "true"); // NOI18N
            catFO.setAttribute("position", CATEGORY_SVG_POSITION); // NOI18N
        } catch (IOException e) {
            Debug.warning("Can't set attributes for palette category folder", e); // NOI18N
        }
    }

    public List<? extends Action> getActions(String projectType) {
        return null;
    }
}
