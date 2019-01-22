/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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

package org.netbeans.modules.vmd.midp.palette;

import org.netbeans.modules.vmd.api.model.Debug;
import org.netbeans.modules.vmd.api.palette.PaletteProvider;
import org.netbeans.modules.vmd.midp.components.MidpDocumentSupport;
import org.netbeans.modules.vmd.midp.palette.wizard.AddToPaletteWizardAction;
import org.openide.filesystems.FileObject;
import org.openide.util.actions.SystemAction;
import javax.swing.*;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import org.openide.filesystems.FileUtil;

/**
 *
 * 
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.vmd.api.palette.PaletteProvider.class)
public class MidpPaletteProvider implements PaletteProvider {

    public static final String CATEGORY_COMMANDS = "commands"; // NOI18N
    public static final String CATEGORY_DISPLAYABLES = "displayables"; // NOI18N
    public static final String CATEGORY_ELEMENTS = "elements"; // NOI18N
    public static final String CATEGORY_ITEMS = "items"; // NOI18N
    public static final String CATEGORY_PROCESS_FLOW = "flow"; // NOI18N
    public static final String CATEGORY_RESOURCES = "resources"; // NOI18N
    public static final String CATEGORY_CUSTOM = "custom"; // NOI18N
    public static final String CATEGORY_DATABINDING = "databinding"; // NOI18N

    private String[] paletteCategories = {CATEGORY_DISPLAYABLES, CATEGORY_COMMANDS, CATEGORY_ELEMENTS, CATEGORY_ITEMS, CATEGORY_PROCESS_FLOW, CATEGORY_RESOURCES, CATEGORY_CUSTOM, CATEGORY_DATABINDING};
    private int[] categoryPositions = {100, 200, 300, 400, 500, 600, 1000, 710}; // custom should be the last

    public MidpPaletteProvider() {
    }

    public void initPaletteCategories(String projectType) {
        if (!MidpDocumentSupport.PROJECT_TYPE_MIDP.equals(projectType)) {
            return;
        }
        
        try {
            FileObject paletteFolder = FileUtil.getConfigFile(projectType + "/palette"); // NOI18N
            if (paletteFolder == null) {
                FileObject root = FileUtil.getConfigRoot();
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
            Debug.warning("Can't set attributes for palette category folder", e); // NOI18N
        }
    }

    public List<? extends Action> getActions(String projectType) {
        if (!MidpDocumentSupport.PROJECT_TYPE_MIDP.equals(projectType)) {
            return null;
        }

        return Collections.singletonList(SystemAction.get(AddToPaletteWizardAction.class));
    }
}
