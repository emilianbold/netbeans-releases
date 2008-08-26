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
 * Contributor(s): theanuradha@netbeans.org
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.maven.indexer.api;

import java.util.Collection;
import java.util.logging.Logger;
import org.apache.maven.artifact.Artifact;
import org.netbeans.modules.maven.indexer.spi.RepositoryIndexerImplementation;
import org.openide.util.Lookup;

/**
 *
 * @author Anuradha G
 */
public final class RepositoryIndexer {

    public static void indexRepo(RepositoryInfo repo) {
        assert repo != null;
        RepositoryIndexerImplementation impl = findImplementation(repo);
        if (impl == null) {
            return;
        }
        impl.indexRepo(repo);
    }
    
    public static void updateIndexWithArtifacts(RepositoryInfo repo, Collection<Artifact> artifacts) {
        assert repo != null;
        if (artifacts == null || artifacts.size() == 0) {
            return;
        }
        RepositoryIndexerImplementation impl = findImplementation(repo);
        if (impl == null) {
            return;
        }
        impl.updateIndexWithArtifacts(repo, artifacts);
    }

    public static void deleteArtifactFromIndex(RepositoryInfo repo, Artifact artifact) {
        assert repo != null;
        if (artifact == null) {
            return;
        }
        RepositoryIndexerImplementation impl = findImplementation(repo);
        if (impl == null) {
            return;
        }
        impl.deleteArtifactFromIndex(repo, artifact);
    }
    
    static RepositoryIndexerImplementation findImplementation(RepositoryInfo repo) {
        Collection<? extends RepositoryIndexerImplementation> res = Lookup.getDefault().lookupAll(RepositoryIndexerImplementation.class);
        for (RepositoryIndexerImplementation impl : res) {
            if (impl.getType().equals(repo.getType())) {
                return impl;
            }
        }
        Logger.getLogger(RepositoryIndexer.class.getName()).info("Cannot find repository indexer type:" + repo.getType() + " for repository " + repo.getName());
        return null;
    }
    
    public static String[] getAvailableTypes() {
        Collection<? extends RepositoryIndexerImplementation> res = Lookup.getDefault().lookupAll(RepositoryIndexerImplementation.class);
        String[] toRet = new String[res.size()];
        int index = 0;
        for (RepositoryIndexerImplementation impl : res) {
            toRet[index] = impl.getType();
            index++;
        }
        return toRet;
        
    }

}
