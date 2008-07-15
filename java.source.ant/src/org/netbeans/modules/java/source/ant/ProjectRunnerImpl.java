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

package org.netbeans.modules.java.source.ant;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.tools.ant.module.api.support.ActionUtils;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.spi.java.project.runner.ProjectRunnerImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

/**
 *
 * @author Jan Lahoda
 */
public class ProjectRunnerImpl implements ProjectRunnerImplementation{

    private static final Logger LOG = Logger.getLogger(ProjectRunnerImpl.class.getName());
    
    public boolean isSupported(String command, FileObject file) {
        return locateScript(command) != null && checkRunSupported(file);
    }

    public void execute(String command, Properties props, FileObject toRun) throws IOException {
        ClassPath exec = ClassPath.getClassPath(toRun, ClassPath.EXECUTE);
        ClassPath source = ClassPath.getClassPath(toRun, ClassPath.SOURCE);

        LOG.log(Level.FINE, "execute classpath={0}", exec);

        String cp = exec.toString(ClassPath.PathConversionMode.FAIL);

        Properties antProps = (Properties) props.clone();

        antProps.setProperty("classpath", cp);
        antProps.setProperty("classname", source.getResourceName(toRun, '.', false));

        ActionUtils.runTarget(buildScript(command), null, antProps);
    }

    private static boolean checkRunSupported(FileObject file) {
        //XXX: finish
        return true;
    }

    private static URL locateScript(String actionName) {
        return ProjectRunnerImpl.class.getResource("/org/netbeans/modules/java/source/ant/resources/" + actionName + "-snippet.xml");
    }

    private static FileObject buildScript(String actionName) throws IOException {
        URL script = locateScript(actionName);

        if (script == null) {
            return null;
        }

        File scriptFile = new File(getCacheFolder(), actionName + ".xml");
        URLConnection connection = script.openConnection();
        
        if (!scriptFile.canRead() || connection.getLastModified() > scriptFile.lastModified()) {
            try {
                scriptFile.delete();

                FileObject target = FileUtil.createData(scriptFile);

                copyFile(connection, target);
                return target;
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
                return null;
            }
        }

        return FileUtil.toFileObject(scriptFile);
    }

    private static final String NB_USER_DIR = "netbeans.user";   //NOI18N
    private static final String SNIPPETS_CACHE_DIR = "var"+File.separatorChar+"cache"+File.separatorChar+"executor-snippets";    //NOI18N


    private static String getNbUserDir () {
        final String nbUserProp = System.getProperty(NB_USER_DIR);
        return nbUserProp;
    }

    private static File cacheFolder;

    private static synchronized File getCacheFolder () {
        if (cacheFolder == null) {
            final String nbUserDirProp = getNbUserDir();
            assert nbUserDirProp != null;
            final File nbUserDir = new File (nbUserDirProp);
            cacheFolder = FileUtil.normalizeFile(new File (nbUserDir, SNIPPETS_CACHE_DIR));
            if (!cacheFolder.exists()) {
                boolean created = cacheFolder.mkdirs();
                assert created : "Cannot create cache folder";  //NOI18N
            }
            else {
                assert cacheFolder.isDirectory() && cacheFolder.canRead() && cacheFolder.canWrite();
            }
        }
        return cacheFolder;
    }

    private static void copyFile(URLConnection source, FileObject target) throws IOException {
        InputStream ins = null;
        OutputStream out = null;

        try {
            ins = source.getInputStream();
            out = target.getOutputStream();

            FileUtil.copy(ins, out);
        } finally {
            if (ins != null) {
                try {
                    ins.close();
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }

            if (out != null) {
                try {
                    out.close();
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
    }
}
