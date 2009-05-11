/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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

package org.netbeans.modules.java.source.ant;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Arrays;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Javac;
import org.netbeans.modules.java.source.usages.BuildArtifactMapperImpl;
import org.netbeans.modules.parsing.api.indexing.IndexingManager;
import org.netbeans.spi.project.support.ant.PropertyUtils;

/**
 *
 * @author Jan Lahoda
 */
public class JavacTask extends Javac {

    @Override
    public void execute() throws BuildException {
        Project p = getProject();
        
        p.log("Overridden Javac task called", Project.MSG_DEBUG);

        boolean ensureBuilt =    p.getProperty("ensure.built.source.roots") != null
                              || Boolean.valueOf(p.getProperty("deploy.on.save"));
        
        if (ensureBuilt) {
            String[] srcdir = getSrcdir().list();
            boolean noBin = false;
            boolean wasBuilt = false;
            
            for (String path : srcdir) {
                File f = PropertyUtils.resolveFile(p.getBaseDir().getAbsoluteFile(), path);
                
                try {
                    Boolean built = BuildArtifactMapperImpl.ensureBuilt(f.toURI().toURL(), false);

                    if (built == null) {
                        noBin = true;
                        
                        if (wasBuilt) {
                            throw new BuildException("Cannot build classfiles for source directories: " + Arrays.asList(srcdir));
                        }
                    } else {
                        wasBuilt = true;

                        if (noBin) {
                            throw new BuildException("Cannot build classfiles for source directories: " + Arrays.asList(srcdir));
                        }
                        
                        if (!built) {
                            throw new UserCancel();
                        }
                    }
                } catch (IOException ex) {
                    throw new BuildException(ex);
                }
            }

            if (!wasBuilt) {
                super.execute();
            }
        } else {
            if (CheckForCleanBuilds.cleanBuild.get() && getSrcdir() != null) {
                for (String path : getSrcdir().list()) {
                    File f = PropertyUtils.resolveFile(p.getBaseDir().getAbsoluteFile(), path);

                    try {
                        p.log("Forcing rescan of: " + f.getAbsolutePath(), Project.MSG_VERBOSE);
                        IndexingManager.getDefault().refreshIndex(f.toURI().toURL(), null);
                    } catch (MalformedURLException ex) {
                        p.log(ex.getMessage(), ex, Project.MSG_VERBOSE);
                    }
                }
            }

            super.execute();
        }
    }

}
