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

package org.netbeans.modules.mercurial.api;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.SwingUtilities;
import org.netbeans.modules.mercurial.ui.clone.CloneAction;
import org.openide.util.NbPreferences;

/**
 *
 * @author Marian Petras
 */
public class Mercurial {

    /**
     * Clones the given repository to the given directory. The method blocks
     * until the whole chcekout is done. Do not call in AWT.
     * 
     * @param  repositoryUrl  URL of the Mercurial repository to be cloned
     * @param  targetDir  target where  cloned repository should be created
     * @param  cloneName  name of the cloned repository
     *                    (name of the root folder of the clone)
     * @param  defaultPull  initial URL for pulling updates
     * @param  defaultPush  initial URL for pushing updates
     */
    public static void cloneRepository(String repositoryUrl,
                                       File targetDir,
                                       String cloneName,
                                       String defaultPull,
                                       String defaultPush) throws MalformedURLException {
        assert !SwingUtilities.isEventDispatchThread() : "Accessing remote repository. Do not call in awt!";
        cloneRepository(repositoryUrl,
                        targetDir,
                        cloneName,
                        defaultPull,
                        defaultPush,
                        null,
                        null);
    }

    /**
     * Clones the given repository to the given directory. The method blocks
     * until the whole chcekout is done. Do not call in AWT.
     *
     * @param  repositoryUrl  URL of the Mercurial repository to be cloned
     * @param  targetDir  target where  cloned repository should be created
     * @param  cloneName  name of the cloned repository
     *                    (name of the root folder of the clone)
     * @param  defaultPull  initial URL for pulling updates
     * @param  defaultPush  initial URL for pushing updates
     * @param  username  username for access to the given repository
     * @param  password  password for access to the given repository
     */
    public static void cloneRepository(String repositoryUrl,
                                       File targetDir,
                                       String cloneName,
                                       String pullUrl,
                                       String pushUrl,
                                       String username,
                                       String password)
            throws MalformedURLException {
        assert !SwingUtilities.isEventDispatchThread() : "Accessing remote repository. Do not call in awt!";

        File cloneFile = new File(targetDir, cloneName);
        CloneAction.performClone(repositoryUrl,
                                 cloneFile.getAbsolutePath(),
                                 true,
                                 null,
                                 pullUrl,
                                 pushUrl).waitFinished();

        try {
            storeWorkingDir(new URL(repositoryUrl), targetDir.toURI().toURL());
        } catch (Exception e) {
            Logger.getLogger(Mercurial.class.getName()).log(Level.FINE, "Cannot store mercurial workdir preferences", e);
        }
    }

    private static final String WORKINGDIR_KEY_PREFIX = "working.dir."; //NOI18N

    /**
     * Stores working directory for specified remote root
     * into NetBeans preferences.
     * These are later used in kenai.ui module
     */
    private static void storeWorkingDir(URL remoteUrl, URL localFolder) {
        Preferences prf = NbPreferences.forModule(Mercurial.class);
        prf.put(WORKINGDIR_KEY_PREFIX + remoteUrl, localFolder.toString());
    }
}
