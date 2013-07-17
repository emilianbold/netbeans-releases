/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.libs.oracle.cloud.api;

import java.io.File;
import java.lang.reflect.Constructor;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import org.netbeans.libs.oracle.cloud.scanningwrapper.ConfigurationFactory;
import org.netbeans.libs.oracle.cloud.scanningwrapper.IClassConfiguration;
import org.netbeans.libs.oracle.cloud.sdkwrapper.api.ApplicationManagerConnectionFactory;
import org.netbeans.libs.oracle.cloud.sdkwrapper.exception.SDKException;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileUtil;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

@NbBundle.Messages({"CloudSDKHelper.title=Select Oracle Cloud SDK installation folder",
    "CloudSDKHelper.wrong.folder=Wrong folder selected for Oracle Cloud SDK. File {0} does not exist."})
public final class CloudSDKHelper {

    private static final String SDK_WELLKNOWN_FILE = "lib/oracle.cloud.paas.api.jar"; // NOI18N
    
    public static final String SDK_FOLDER = "oracle.cloud.sdk.installation.folder"; // NOI18N

    public static Preferences getSDKFolderPreferences() {
        return NbPreferences.forModule(CloudSDKHelper.class);
    }
    
    public static String getSDKFolder() {
        return NbPreferences.forModule(CloudSDKHelper.class).get(SDK_FOLDER, "");
    }
    
    public static void persistSDKFolder(String folder) {
        NbPreferences.forModule(CloudSDKHelper.class).put(SDK_FOLDER, folder);
    }
    
    public static File showConfigureSDKDialog(JComponent parent) {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setDialogTitle(NbBundle.getMessage(CloudSDKHelper.class, "CloudSDKHelper.title")); // NOI18N
        String folder = getSDKFolder();
        if (folder.trim().length() != 0) {
            File f = new File(folder);
            if (f.exists()) {
                chooser.setSelectedFile(f);
            }
        }
        if (JFileChooser.APPROVE_OPTION == chooser.showOpenDialog(parent)) {
            File selected = FileUtil.normalizeFile(chooser.getSelectedFile());
            File sdkFile = new File(selected, SDK_WELLKNOWN_FILE);
            if (isValidSDKFolder(selected)) {
                CloudSDKHelper.persistSDKFolder(selected.getAbsolutePath());
                return selected;
            } else {
                DialogDisplayer.getDefault().notify(
                        new NotifyDescriptor.Message(NbBundle.getMessage(CloudSDKHelper.class, "CloudSDKHelper.wrong.folder", sdkFile.getAbsolutePath()))); // NOI18N
                return null;
            }
        }
        return null;
    }
    
    public static boolean isValidSDKFolder(File folder) {
        return new File(folder, SDK_WELLKNOWN_FILE).exists(); // NOI18N
    }
    
    public static ApplicationManagerConnectionFactory createSDKFactory(String sdkDirectory) {
        ClassLoader cl = getClassLoader(sdkDirectory);
        try {
            if (cl != null) {
                Class impl = cl.loadClass("org.netbeans.libs.oracle.cloud.ext.ApplicationManagerConnectionFactoryImpl"); // NOI18N
                Constructor c = impl.getConstructor(new Class[] {ClassLoader.class});
                ApplicationManagerConnectionFactory f = (ApplicationManagerConnectionFactory)c.newInstance(new Object[] {cl});
                return f;
            }
        } catch (Throwable ex) {
            throw new SDKException("cannot instantiate ApplicationManagerConnectionFactoryImpl", ex); // NOI18N
        }
        return null;
    }
    
    public static IClassConfiguration createScanningConfiguration(String sdkDirectory) {
        ClassLoader cl = getClassLoader(sdkDirectory);
        try {
            if (cl != null) {
                Class impl = cl.loadClass("org.netbeans.libs.oracle.cloud.ext.ConfigurationFactoryImpl"); // NOI18N
                Constructor c = impl.getConstructor(new Class[] {ClassLoader.class});
                ConfigurationFactory f = (ConfigurationFactory)c.newInstance(new Object[] {cl});
                return f.getDefaultClassConfiguration();
            }
        } catch (Throwable ex) {
            throw new SDKException("cannot instantiate ApplicationManagerConnectionFactoryImpl", ex); // NOI18N
        }
        return null;
    }
    
    private static ClassLoader getClassLoader(String sdkDirectory) {
        File extjar = InstalledFileLocator.getDefault().locate("modules/ext/libs.oracle.cloud-ext.jar", "org.netbeans.libs.oracle.cloud", false); // NOI18N
        if (extjar == null) {
            throw new SDKException("libs.oracle.cloud-ext.jar not found"); // NOI18N
        }
        List<URL> urls = new ArrayList<URL>();
        try {
            urls.add(extjar.toURI().toURL());
            urls.add(new File(sdkDirectory, "lib/oracle.cloud.paas.api.jar").toURI().toURL()); // NOI18N
            urls.add(new File(sdkDirectory, "lib/whitelist.jar").toURI().toURL()); // NOI18N
            urls.add(new File(sdkDirectory, "lib/jersey-client.jar").toURI().toURL()); // NOI18N
            urls.add(new File(sdkDirectory, "lib/jersey-core.jar").toURI().toURL()); // NOI18N
            urls.add(new File(sdkDirectory, "lib/jersey-multipart.jar").toURI().toURL()); // NOI18N
            ClassLoader classLoader = new URLClassLoader(urls.toArray(new URL[] {}), 
                    CloudSDKHelper.class.getClassLoader()/*ClassLoader.getSystemClassLoader()*/);
            return classLoader;
        } catch (MalformedURLException m) {
            throw new SDKException("cannot create classloader", m); // NOI18N
        }
    }
    
}
