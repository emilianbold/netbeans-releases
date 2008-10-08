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
import org.netbeans.modules.maven.NbMavenProjectImpl;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author  Milos Kleint 
 */
class TestCompileClassPathImpl extends AbstractProjectClassPathImpl {
    
    /** Creates a new instance of SrcClassPathImpl */
    public TestCompileClassPathImpl(NbMavenProjectImpl proj) {
        super(proj);
        
    }
    
   URI[] createPath() {
        List<URI> lst = new ArrayList<URI>();
        //TODO we shall add the test class output as well. how?
        // according the current 2.1 sources this is almost the same as getCompileClasspath()
        //except for the fact that multiproject references are not redirected to their respective
        // output folders.. we lways retrieve stuff from local repo..
        @SuppressWarnings("unchecked")
        List<Artifact> arts = getMavenProject().getOriginalMavenProject().getTestArtifacts();
        for (Artifact art : arts) {
            if (art.getFile() != null) {
                File fil = FileUtil.normalizeFile(art.getFile());
                lst.add(fil.toURI());
            } else { //NOPMD
                //null means dependencies were not resolved..
            } 
        }
        File fil = new File(getMavenProject().getOriginalMavenProject().getBuild().getOutputDirectory());
        fil = FileUtil.normalizeFile(fil);
        lst.add(0, fil.toURI());
        URI[] uris = new URI[lst.size()];
        uris = lst.toArray(uris);
        return uris;
    }    
    
}
