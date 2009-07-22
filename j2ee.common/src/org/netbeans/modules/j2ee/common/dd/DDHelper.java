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

package org.netbeans.modules.j2ee.common.dd;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import org.netbeans.api.j2ee.core.Profile;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * Support for creation of deployment descriptors
 * @author Petr Slechta
 */
public class DDHelper {

    private static final String RESOURCE_FOLDER = "org/netbeans/modules/j2ee/common/dd/resources/"; //NOI18N

    private DDHelper() {
    }

    /**
     * Creates web.xml deployment descriptor.
     * @param j2eeProfile Java EE profile to specify which version of web.xml should be created
     * @param dir Directory where web.xml should be created
     * @return web.xml file as FileObject
     * @throws java.io.IOException
     */
    public static FileObject createWebXml(Profile j2eeProfile, FileObject dir) throws IOException {
        return createWebXml(j2eeProfile, true, dir);
    }
    
    /**
     * Creates web.xml deployment descriptor.
     * @param j2eeProfile Java EE profile to specify which version of web.xml should be created
     * @param webXmlRequired true if web.xml should be created also for profiles where it is not required
     * @param dir Directory where web.xml should be created
     * @return web.xml file as FileObject
     * @throws java.io.IOException
     */
    public static FileObject createWebXml(Profile j2eeProfile, boolean webXmlRequired, FileObject dir) throws IOException {
        String template = null;
        if ((Profile.JAVA_EE_6_FULL == j2eeProfile || Profile.JAVA_EE_6_WEB == j2eeProfile) && webXmlRequired) {
            template = "web-3.0.xml"; //NOI18N
        } else if (Profile.JAVA_EE_5 == j2eeProfile) {
            template = "web-2.5.xml"; //NOI18N
        } else if (Profile.J2EE_14 == j2eeProfile) {
            template = "web-2.4.xml"; //NOI18N
        } else if (Profile.J2EE_13 == j2eeProfile) {
            template = "web-2.3.xml"; //NOI18N
        }

        if (template == null)
            return null;

        MakeFileCopy action = new MakeFileCopy(RESOURCE_FOLDER + template, dir, "web.xml");
        FileUtil.runAtomicAction(action);
        if (action.getException() != null)
            throw action.getException();
        else
            return action.getResult();
    }

    /**
     * Creates web-fragment.xml deployment descriptor.
     * @param j2eeProfile Java EE profile to specify which version of web-fragment.xml should be created
     * @param dir Directory where web-fragment.xml should be created
     * @return web-fragment.xml file as FileObject
     * @throws java.io.IOException
     */
    public static FileObject createWebFragmentXml(Profile j2eeProfile, FileObject dir) throws IOException {
        String template = null;
        if (Profile.JAVA_EE_6_FULL == j2eeProfile || Profile.JAVA_EE_6_WEB == j2eeProfile) {
            template = "web-fragment-3.0.xml"; //NOI18N
        }

        if (template == null)
            return null;

        MakeFileCopy action = new MakeFileCopy(RESOURCE_FOLDER + template, dir, "web-fragment.xml");
        FileUtil.runAtomicAction(action);
        if (action.getException() != null)
            throw action.getException();
        else
            return action.getResult();
    }
    
    // -------------------------------------------------------------------------
    private static class MakeFileCopy implements Runnable {
        private String fromFile;
        private FileObject toDir;
        private String toFile;
        private IOException exception;
        private FileObject result;

        MakeFileCopy(String fromFile, FileObject toDir, String toFile) {
            this.fromFile = fromFile;
            this.toDir = toDir;
            this.toFile = toFile;
        }

        IOException getException() {
            return exception;
        }

        FileObject getResult() {
            return result;
        }

        public void run() {
            try {
                // PENDING : should be easier to define in layer and copy related FileObject (doesn't require systemClassLoader)
                FileObject xml = FileUtil.createData(toDir, toFile);
                String content = readResource(Thread.currentThread().getContextClassLoader().getResourceAsStream(fromFile));
                if (content != null) {
                    FileLock lock = xml.lock();
                    BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(xml.getOutputStream(lock)));
                    try {
                        bw.write(content);
                    } finally {
                        bw.close();
                        lock.releaseLock();
                    }
                }
                result = xml;
            }
            catch (IOException e) {
                exception = e;
            }
        }

        private String readResource(InputStream is) throws IOException {
            StringBuilder sb = new StringBuilder();
            String lineSep = System.getProperty("line.separator"); // NOI18N
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            try {
                String line = br.readLine();
                while (line != null) {
                    sb.append(line);
                    sb.append(lineSep);
                    line = br.readLine();
                }
            } finally {
                br.close();
            }
            return sb.toString();
        }
    }

}
