/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
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
package org.netbeans.modules.javacard.ri.bundle;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.javacard.Utils;
import org.netbeans.modules.javacard.constants.JCConstants;
import org.netbeans.modules.javacard.constants.JavacardPlatformKeyNames;
import org.netbeans.modules.javacard.constants.PlatformTemplateWizardKeys;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem.AtomicAction;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.modules.InstalledFileLocator;
import org.openide.modules.ModuleInstall;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.NbCollections;

public class Installer extends ModuleInstall {

    private static final String ATTR = "isRiBundle"; //NOI18N
    private static final String PLATFORM_PROPS_NAME = "platform.properties"; //NOI18N
    private static final String PLATFORM_DIRECTORY_NAME = "JCDK3.0.1_ConnectedEdition"; //NOI18N

    @Override
    public void restored() {
        final FileObject platformsFolder = Utils.sfsFolderForRegisteredJavaPlatforms();
        boolean hasDefaultPlatform = false;
        for (FileObject fo : platformsFolder.getChildren()) {
            if (Boolean.TRUE.equals(fo.getAttribute(ATTR))) {
                return;
            }
            hasDefaultPlatform |= JCConstants.DEFAULT_JAVACARD_PLATFORM_FILE_NAME.equals(
                    fo.getName()) && JCConstants.JAVACARD_PLATFORM_FILE_EXTENSION.equals(
                    fo.getExt());
        }
        StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(Installer.class,
                "MSG_UNPACKING_RUNTIME"));

        File runtime = InstalledFileLocator.getDefault().locate(PLATFORM_DIRECTORY_NAME,
                "org.netbeans.modules.javacard.ri", false); //NOI18N
        if (runtime != null && runtime.exists() && runtime.isDirectory()) {
            File platformProps = new File(runtime, PLATFORM_PROPS_NAME);
            if (!platformProps.exists() || !platformProps.isFile()) {
                throw new IllegalStateException("Runtime contains no platform.properties file"); //NOI18N
            }
            final EditableProperties props = new EditableProperties(true);
            try {
                InputStream in = new BufferedInputStream(new FileInputStream(platformProps));
                try {
                    props.load(in);
                } finally {
                    in.close();
                }
                final EditableProperties xlated = translatePaths(runtime, props);
                String runtimeName = NbBundle.getMessage(Installer.class, "BUNDLED_RUNTIME_NAME"); //NOI18N
                xlated.setProperty("javacard.instance.id", "javacard_default"); //NOI18N
                xlated.setProperty(JavacardPlatformKeyNames.PLATFORM_DISPLAYNAME,
                        runtimeName);
                xlated.setProperty(JavacardPlatformKeyNames.PLATFORM_HOME, runtime.getAbsolutePath());

                String fn = hasDefaultPlatform ? runtimeName : JCConstants.DEFAULT_JAVACARD_PLATFORM_FILE_NAME;

                final String filename = FileUtil.findFreeFileName(platformsFolder, fn, JCConstants.JAVACARD_PLATFORM_FILE_EXTENSION);
                final FileObject[] platformDef = new FileObject[1];
                platformsFolder.getFileSystem().runAtomicAction(new AtomicAction() {

                    public void run() throws IOException {
                        FileObject fo = platformsFolder.createData(filename, JCConstants.JAVACARD_PLATFORM_FILE_EXTENSION);
                        platformDef[0] = fo;
                        OutputStream out = new BufferedOutputStream(fo.getOutputStream());
                        try {
                            xlated.store(out);
                        } finally {
                            out.close();
                        }
                        fo.setAttribute(ATTR, Boolean.TRUE);
                    }
                });

                FileObject serversFolder = Utils.sfsFolderForDeviceConfigsForPlatformNamed(filename, true);
                //Now create a default device template
                String deviceTemplateName = "org-netbeans-modules-javacard/templates/ServerTemplate.jcard"; //NOI18N
                DataObject deviceTemplate = DataObject.find(FileUtil.getConfigFile(deviceTemplateName));
                DataFolder fld = DataFolder.findFolder(serversFolder);
                String defaultDevice = JCConstants.TEMPLATE_DEFAULT_DEVICE_NAME;
                defaultDevice = FileUtil.findFreeFileName(serversFolder, defaultDevice, JCConstants.JAVACARD_DEVICE_FILE_EXTENSION);
                Map<String, String> substitutions = new HashMap<String, String>();
                substitutions.put(PlatformTemplateWizardKeys.PROJECT_TEMPLATE_DEVICE_NAME_KEY,
                        JCConstants.TEMPLATE_DEFAULT_DEVICE_NAME); //NOI18N
                DataObject device = deviceTemplate.createFromTemplate(fld, defaultDevice, substitutions);
                device.getPrimaryFile().setAttribute (ATTR, Boolean.TRUE);

                EditableProperties globals = PropertyUtils.getGlobalProperties();
                File platformDefFile = FileUtil.toFile (platformDef[0]);

                globals.setProperty(JCConstants.GLOBAL_PROPERTIES_JCPLATFORM_DEFINITION_PREFIX +
                        filename, platformDefFile.getAbsolutePath()); //NOI18N

                String serversFolderPath = FileUtil.toFile (serversFolder).getAbsolutePath();
                globals.setProperty (JCConstants.GLOBAL_PROPERTIES_JCPLATFORM_DEFINITION_PREFIX + fn +
                        JCConstants.GLOBAL_PROPERTIES_DEVICE_FOLDER_PATH_KEY_SUFFIX, serversFolderPath);

                PropertyUtils.putGlobalProperties(globals);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    @Override
    public void uninstalled() {
        FileObject platformsFolder = Utils.sfsFolderForRegisteredJavaPlatforms();
        for (FileObject fo : platformsFolder.getChildren()) {
            if (Boolean.TRUE.equals(fo.getAttribute(ATTR))) {
                try {
                    FileObject serversFolder = Utils.sfsFolderForDeviceConfigsForPlatformNamed(fo.getName(), false);
                    if (serversFolder != null) {
                        serversFolder.delete();
                    }
                    FileObject eepromFolder = Utils.sfsFolderForDeviceEepromsForPlatformNamed(fo.getName(), false);
                    if (eepromFolder != null) {
                        eepromFolder.delete();
                    }
                    fo.delete();
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
    }

    @Override
    public void updated(int release, String specVersion) {
        uninstalled();
    }

    private EditableProperties translatePaths(File dir, EditableProperties props) {
        EditableProperties nue = new EditableProperties();
        Set<String> translatablePaths = JavacardPlatformKeyNames.getPathPropertyNames();
        for (String key : NbCollections.checkedSetByFilter(props.keySet(), String.class, false)) {
            String val = props.getProperty(key);
            if (translatablePaths.contains(key)) {
                String xlated = translatePath(dir, val);
                nue.put(key, xlated);
            } else {
                nue.put(key, val);
            }
        }
        return nue;
    }

    private String translatePath(File dir, String val) {
        if ("".equals(val) || val == null) { //NOI18N
            return ""; //NOI18N
        }
        if (val.startsWith("./")) {
            val = val.substring(2);
        }
        if (File.separatorChar != '/' && val.indexOf("/") >= 0) { //NOI18N
            val = val.replace('/', File.separatorChar); //NOI18N
        }
        if (val.indexOf(':') >= 0) { //NOI18N
            String[] paths = val.split(":"); //NOI18N
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < paths.length; i++) {
                String path = paths[i];
                if (sb.length() > 0) {
                    sb.append(File.pathSeparatorChar);
                }
                sb.append(translatePath(dir, path));
            }
            return sb.toString();
        }
        File nue = new File(dir, val);
        return nue.getAbsolutePath();
    }
}
