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

package org.netbeans.modules.maven.embedder;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import org.apache.maven.embedder.ConfigurationValidationResult;
import org.apache.maven.embedder.DefaultConfiguration;
import org.apache.maven.profiles.ProfilesRoot;
import org.apache.maven.profiles.io.xpp3.ProfilesXpp3Reader;
import org.apache.maven.settings.Settings;
import org.apache.maven.settings.io.xpp3.SettingsXpp3Reader;
import hidden.org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * a workaround for the fact that one cannot access the settings values the embedder is using.
 * nice thing to do would be to have access to 1. the merged global/user settings for retrieval of used values
 * 2. the model of user settings for reading/writing in UI.
 * @author mkleint
 */
public class MavenSettingsSingleton {
    private static MavenSettingsSingleton instance;
    private SettingsXpp3Reader builder;
    /** Creates a new instance of MavenSettingsSingleton */
    private MavenSettingsSingleton() {
        builder = new SettingsXpp3Reader();
    }
    
    public static synchronized MavenSettingsSingleton getInstance() {
        if (instance == null) {
            instance = new MavenSettingsSingleton();
        }
        return instance;
    }
    /**
     * the location of ${user.home}/.m2
     */
    public File getM2UserDir() {
        return FileUtil.normalizeFile(new File(System.getProperty("user.home"), ".m2"));
    }
    
    /**
     * this method  should rather use the embedder's settings, however there's no clear
     * way of retrieving/using them.
     * @deprecated rather not use, doesn't contain the global setting values
     */
    @Deprecated
    public Settings getSettings() {
        //TODO need probably some kind of caching..
        Settings sets = createUserSettingsModel();
        if (sets.getLocalRepository() == null) {
            sets.setLocalRepository(new File(getM2UserDir(), "repository").toString());
        }
        return sets;
    }
    
    public Settings createUserSettingsModel() {
        Settings sets = null;
        File dir = getM2UserDir();
        try {
            File fil = new File(dir, "settings.xml");
            if (fil.exists()) {
                sets = builder.read(new InputStreamReader(new FileInputStream(fil)));
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (XmlPullParserException ex) {
            ex.printStackTrace();
        }
        if (sets == null) {
            sets = new Settings();
        }
        return sets;
    }
    
    public static ProfilesRoot createProfilesModel(FileObject projectDir) {
        FileObject profiles = projectDir.getFileObject("profiles.xml");
        ProfilesRoot prof = null;
        if (profiles != null) {
            InputStreamReader read = null;
            try {
                read = new InputStreamReader(profiles.getInputStream());
                prof = new ProfilesXpp3Reader().read(read);
            } catch (IOException ex) {
                ex.printStackTrace();
            } catch (XmlPullParserException ex) {
                ex.printStackTrace();
            } finally {
                IOUtil.close(read);
            }
        } 
        if (prof == null) {
            prof = new ProfilesRoot();
        }
        return prof;
    }
    
}
