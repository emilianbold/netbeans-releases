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

package org.netbeans.modules.hudson.kenai;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.netbeans.modules.hudson.spi.ProjectHudsonProvider;
import org.netbeans.modules.kenai.api.KenaiException;
import org.netbeans.modules.kenai.api.KenaiFeature;
import org.netbeans.modules.kenai.api.KenaiProject;
import org.netbeans.modules.kenai.api.KenaiService.Type;
import org.openide.util.Utilities;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service=ProjectHudsonProvider.class, position=300)
public class KenaiHudsonBuildAssociation extends ProjectHudsonProvider {

    private static final Logger LOG = Logger.getLogger(KenaiHudsonBuildAssociation.class.getName());

    public Association findAssociation(Project project) {
        Object loc = project.getProjectDirectory().getAttribute("ProvidedExtensions.RemoteLocation");
        if (loc instanceof String) {
            try {
                KenaiProject p = KenaiProject.forRepository((String) loc);
                Association a = findAssociation(p);
                if (a != null) {
                    return a;
                }
            } catch (KenaiException x) {
                LOG.log(Level.FINE, "Looking up association for " + project, x);
            }
        }
        /* XXX to be considered in case PE.RL proves unreliable:
        File wd = FileUtil.toFile(project.getProjectDirectory());
        if (wd != null) {
            for (ProjectHandle handle : Dashboard.getDefault().getOpenProjects()) {
                for (SourceHandle source : SourceAccessor.getDefault().getSources(handle)) {
                    if (wd.equals(source.getWorkingDirectory())) {
                        try {
                            KenaiProject p = Kenai.getDefault().getProject(handle.getId());
                            Association a = findAssociation(p);
                            if (a != null) {
                                return a;
                            }
                        } catch (KenaiException x) {
                            LOG.log(Level.FINE, "Looking up association for " + project, x);
                        }
                    }
                }
            }
        }
         */
        return null;
    }

    private Association findAssociation(KenaiProject p) throws KenaiException {
        if (p != null) {
            for (KenaiFeature feature : p.getFeatures(Type.HUDSON)) {
                String server = feature.getWebLocation().toString();
                return Association.fromString(server);
            }
        }
        return null;
    }

    public boolean recordAssociation(Project project, Association assoc) {
        return Utilities.compareObjects(assoc, findAssociation(project));
    }

}
