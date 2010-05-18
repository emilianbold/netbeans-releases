/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.maven.newproject;

import org.netbeans.modules.maven.api.archetype.Archetype;
import org.netbeans.modules.maven.api.archetype.ArchetypeProvider;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/**
 * Archetype provider that lists the 3 basic ones to have something in the list
 * when the user never used any archetypes before..
 * @author mkleint
 */
@SuppressWarnings("deprecation")
@org.openide.util.lookup.ServiceProvider(service=ArchetypeProvider.class)
public class LayerBasedArchetypeProvider implements ArchetypeProvider {
    
    /** Creates a new instance of LayerBasedArchetypeProvider */
    public LayerBasedArchetypeProvider() {
    }

    public List<Archetype> getArchetypes() {
        FileObject root = FileUtil.getConfigFile("Projects/org-netbeans-modules-maven/Archetypes"); //NOI18N
        List<Archetype> toRet = new ArrayList<Archetype>();
        for (FileObject fo : FileUtil.getOrder(Arrays.asList(root.getChildren()), false)) {
            String groupId = (String) fo.getAttribute("groupId"); //NOI18N
            String artifactId = (String) fo.getAttribute("artifactId"); //NOI18N
            String version = (String) fo.getAttribute("version"); //NOI18N
            String repository = (String) fo.getAttribute("repository"); //NOI18N
            String nameKey = (String) fo.getAttribute("nameBundleKey"); //NOI18N
            String descKey = (String) fo.getAttribute("descriptionBundleKey"); //NOI18N
            String bundleLocation = (String) fo.getAttribute("SystemFileSystem.localizingBundle"); //NOI18N
            if (groupId != null && artifactId != null && version != null) {
                Archetype simple = new Archetype(false);
                simple.setGroupId(groupId);
                simple.setArtifactId(artifactId);
                simple.setVersion(version);
                simple.setRepository(repository);
                if (bundleLocation != null) {
                    ResourceBundle bundle = NbBundle.getBundle(bundleLocation);
                    if (bundle != null && nameKey != null) {
                        simple.setName(bundle.getString(nameKey));
                    }
                    if (bundle != null && descKey != null) {
                        simple.setDescription(bundle.getString(descKey));
                    }
                }
                if (simple.getName() == null) {
                    simple.setName(simple.getArtifactId());
                }
                toRet.add(simple);
            }
        }
        return toRet;
    }
    
}
