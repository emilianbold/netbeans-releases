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

package org.netbeans.modules.maven.classpath;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.project.MavenProject;
import org.netbeans.modules.maven.NbMavenProjectImpl;
import org.openide.filesystems.FileUtil;


/**
 *
 * @author  Milos Kleint 
 */
public class TestRuntimeClassPathImpl extends AbstractProjectClassPathImpl {
    
    /**
     * Creates a new instance of TestRuntimeClassPathImpl
     */
    public TestRuntimeClassPathImpl(NbMavenProjectImpl proj) {
        super(proj);
    }

   URI[] createPath() {
        List<URI> lst = createPath(getMavenProject().getOriginalMavenProject());
        URI[] uris = new URI[lst.size()];
        uris = lst.toArray(uris);
        return uris;
   }
    
   public static List<URI>createPath(MavenProject prj) {
       assert prj != null;
        List<URI> lst = new ArrayList<URI>();
        if (prj.getBuild() != null) {
            File fil = new File(prj.getBuild().getOutputDirectory());
            fil = FileUtil.normalizeFile(fil);
            lst.add(fil.toURI());
            fil = new File(prj.getBuild().getTestOutputDirectory());
            fil = FileUtil.normalizeFile(fil);
            lst.add(fil.toURI());
        }
        @SuppressWarnings("unchecked")
        List<Artifact> arts = prj.getTestArtifacts();
        for (Artifact art : arts) {
            if (art.getFile() != null) {
                File fil = FileUtil.normalizeFile(art.getFile());
                lst.add(fil.toURI());
            } else {
              //NOPMD   //null means dependencies were not resolved..
            }
        }
        return lst;
    }    
    
}
