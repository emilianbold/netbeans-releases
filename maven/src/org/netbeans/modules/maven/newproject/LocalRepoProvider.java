/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.maven.newproject;

import org.netbeans.modules.maven.api.archetype.Archetype;
import org.netbeans.modules.maven.api.archetype.ArchetypeProvider;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import org.netbeans.modules.maven.indexer.api.NBVersionInfo;
import org.netbeans.modules.maven.indexer.api.RepositoryInfo;
import org.netbeans.modules.maven.indexer.api.RepositoryPreferences;
import org.netbeans.modules.maven.indexer.api.RepositoryQueries;

/**
 * Lists archetypes found in local repository index. Will include both old archetypes
 * and archetypeng ones.
 * @author mkleint
 */
public class LocalRepoProvider implements ArchetypeProvider {
    
    /** Creates a new instance of LocalRepoProvider */
    public LocalRepoProvider() {
    }

    public List<Archetype> getArchetypes() {
        List<Archetype> lst = new ArrayList<Archetype>();
            RepositoryInfo info = RepositoryPreferences.getInstance().getRepositoryInfoById(RepositoryPreferences.LOCAL_REPO_ID);
            if (info == null) {
                Logger.getLogger(LocalRepoProvider.class.getName()).fine("Local repository info cannot be found, how come?");
                return lst;
            }
                    
            List<NBVersionInfo> archs = RepositoryQueries.findArchetypes(info);
            if (archs == null) {
                return lst;
            }
            Set<Archetype> prohibited = new HashSet<Archetype>();
            prohibited.addAll(Arrays.asList(ArchetypeWizardUtils.WEB_APP_ARCHS));
            prohibited.addAll(Arrays.asList(ArchetypeWizardUtils.EAR_ARCHS));
            prohibited.addAll(Arrays.asList(ArchetypeWizardUtils.EJB_ARCHS));
            prohibited.add(ArchetypeWizardUtils.EA_ARCH);

            for (NBVersionInfo art : archs) {
               //TODO FINDout  how to get contain matadata 
               // boolean ng = artifact.getFiles().contains("META-INF/maven/archetype-metadata.xml");
                Archetype arch = ( "maven-archetype".equalsIgnoreCase(art.getPackaging())) ? //NOI18N
                                 new Archetype(true) : new Archetype();
                arch.setArtifactId(art.getArtifactId());
                arch.setGroupId(art.getGroupId());
                arch.setVersion(art.getVersion());
                arch.setName(art.getProjectName());
                arch.setDescription(art.getProjectDescription());
                if (prohibited.contains(arch)) {
                    continue;
                }
                lst.add(arch);
            }
       
        return lst;
    }
    
}
