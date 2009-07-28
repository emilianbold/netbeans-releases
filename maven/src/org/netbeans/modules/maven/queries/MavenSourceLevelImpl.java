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

package org.netbeans.modules.maven.queries;

import java.io.File;
import java.net.URI;
import java.util.logging.Logger;
import org.netbeans.modules.maven.NbMavenProjectImpl;
import org.netbeans.modules.maven.api.Constants;
import org.netbeans.modules.maven.api.PluginPropertyUtils;
import org.netbeans.spi.java.queries.SourceLevelQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * maven implementation of SourceLevelQueryImplementation.
 * checks a property of maven-compiler-plugin
 * @author Milos Kleint
 */
public class MavenSourceLevelImpl implements SourceLevelQueryImplementation {
    private final NbMavenProjectImpl project;
    private static Logger LOG = Logger.getLogger(MavenSourceLevelImpl.class.getName());
    /** Creates a new instance of MavenSourceLevelImpl */
    public MavenSourceLevelImpl(NbMavenProjectImpl proj) {
        project = proj;
    }
    
    public String getSourceLevel(FileObject javaFile) {
//        LOG.info("SLQ for " + javaFile);
        //TODO generated source are now assumed to be the same level as sources.
        // that's the most common scenario, not sure if sources are generated for tests that often..
        
        //MEVENIDE-573
        assert javaFile != null;
        if (javaFile == null) {
            return null;
        }
        File file = FileUtil.toFile(javaFile);
        if (file == null) {
            //#128609 something in jar?
            return null;
        }
        URI[] tests = project.getSourceRoots(true);
        URI uri = file.toURI();
        assert "file".equals(uri.getScheme());
        String goal = "compile"; //NOI18N
        for (URI testuri : tests) {
            if (uri.getPath().startsWith(testuri.getPath())) {
                goal = "testCompile"; //NOI18N
            } 
        }
        String toRet = PluginPropertyUtils.getPluginProperty(project, Constants.GROUP_APACHE_PLUGINS,  //NOI18N
                                                              Constants.PLUGIN_COMPILER,  //NOI18N
                                                              "source",  //NOI18N
                                                              goal);
        //null is allowed to be returned but junit tests module asserts not null
//        LOG.info("  returning " + toRet);
        return toRet == null ? "1.3" : toRet; //NOI18N
    }
    
}