/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.makeproject.ui.customizer;

import java.util.ArrayList;
import org.netbeans.modules.cnd.makeproject.api.configurations.Folder;
import org.netbeans.modules.cnd.makeproject.api.configurations.ui.CustomizerNode;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author as204739
 */
public class FolderNodeFactory {
    private FolderNodeFactory() {
    }

    public static  Node createRootNodeFolder(Lookup lookup) {
        MakeContext context = lookup.lookup(MakeContext.class);
        int compilerSet = context.selectedCompilerSet();
        ArrayList<CustomizerNode> descriptions = new ArrayList<CustomizerNode>(); //new CustomizerNode[2];
        descriptions.add(createGeneralFolderDescription(lookup));
        if (compilerSet >= 0) {
            descriptions.add(ItemNodeFactory.createCCompilerDescription(lookup));
            descriptions.add(ItemNodeFactory.createCCCompilerDescription(lookup));
        }

        Folder folder = lookup.lookup(Folder.class);
        if(folder != null && (folder.isTest() || folder.isTestLogicalFolder() || folder.isTestRootFolder())) {
            descriptions.add(createLinkerDescription(lookup));
        }

        CustomizerNode rootDescription = new CustomizerNode(
                "Configuration Properties", getString("CONFIGURATION_PROPERTIES"), descriptions.toArray(new CustomizerNode[descriptions.size()]), lookup);  // NOI18N

        return new PropertyNode(rootDescription);
    }

    private static CustomizerNode createGeneralFolderDescription(Lookup lookup) {
        return new GeneralFolderCustomizerNode(
                "GeneralItem", getString("LBL_Config_General"), null, lookup); // NOI18N
    }

    // Linker
    private static CustomizerNode createLinkerDescription(Lookup lookup) {
        return new LinkerGeneralCustomizerNode("Linker", getString("LBL_LINKER_NODE"), null, lookup); // NOI18N
    }

    private static String getString(String s) {
        return NbBundle.getBundle(MakeCustomizer.class).getString(s);
    }

}
