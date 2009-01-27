/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.apisupport.project.ui.customizer;

import java.io.File;
import org.netbeans.api.project.Project;
import org.netbeans.modules.apisupport.project.SuiteProvider;
import org.netbeans.modules.apisupport.project.spi.NbModuleProvider;

/**
 * Info class about external cluster added to cluster.path.
 *
 * Contains cluster dir and locations of sources and javadoc.
 * @author Richard Michalsky
 */
public final class ClusterInfo {
    private File clusterDir;
    private String rawPath;
    private boolean isPlatformCluster;
    private Project project;

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ClusterInfo other = (ClusterInfo) obj;
        if (this.clusterDir != other.clusterDir && (this.clusterDir == null || !this.clusterDir.equals(other.clusterDir))) {
            return false;
        }
        if (this.isPlatformCluster != other.isPlatformCluster) {
            return false;
        }
        if (this.project != other.project && (this.project == null || !this.project.equals(other.project))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 59 * hash + (this.clusterDir != null ? this.clusterDir.hashCode() : 0);
        hash = 59 * hash + (this.isPlatformCluster ? 1 : 0);
        hash = 59 * hash + (this.project != null ? this.project.hashCode() : 0);
        return hash;
    }

    public boolean isPlatformCluster() {
        return isPlatformCluster;
    }

    public String getRawPath() {
        return rawPath;
    }

    public File getClusterDir() {
        return clusterDir;
    }
    // TODO C.P sources & javadoc
    
    private ClusterInfo(File clusterDir) {
        this.clusterDir = clusterDir;
    }

    public static ClusterInfo createFromCP(String rawPath, File evaluatedPath, Project prj, boolean isPlatformCluster) {
        ClusterInfo ret = new ClusterInfo(evaluatedPath);
        ret.rawPath = rawPath;
        ret.isPlatformCluster = isPlatformCluster;
        ret.project = prj;
        return ret;
    }

    public static ClusterInfo create(File clusterDir, boolean isPlatformCluster) {
        ClusterInfo ret = new ClusterInfo(clusterDir);
        ret.isPlatformCluster = isPlatformCluster;
        return ret;
    }

    private static final String NO_NBORG_PROJECTS = "Only standalone module or suite projects allowed";
    
    public static ClusterInfo create(Project project) {
        NbModuleProvider nbmp = project.getLookup().lookup(NbModuleProvider.class);
        SuiteProvider sprv = project.getLookup().lookup(SuiteProvider.class);
        File clusterDir;
        if (nbmp != null) {
            if (nbmp.getModuleType() == NbModuleProvider.STANDALONE)
                clusterDir = ClusterUtils.getClusterDirectory(project);
            else
                throw new IllegalArgumentException(NO_NBORG_PROJECTS);
        } else if (sprv != null) {
            clusterDir = sprv.getClusterDirectory();
        } else {
            throw new IllegalArgumentException(NO_NBORG_PROJECTS);
        }
        ClusterInfo ret = new ClusterInfo(clusterDir);
        ret.project = project;
        return ret;
    }

    public Project getProject() {
        return project;
    }
}
