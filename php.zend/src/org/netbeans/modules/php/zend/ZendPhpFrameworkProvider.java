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

package org.netbeans.modules.php.zend;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.api.phpmodule.PhpModuleProperties;
import org.netbeans.modules.php.spi.editor.EditorExtender;
import org.netbeans.modules.php.spi.phpmodule.PhpFrameworkProvider;
import org.netbeans.modules.php.spi.phpmodule.PhpModuleActionsExtender;
import org.netbeans.modules.php.spi.phpmodule.PhpModuleExtender;
import org.netbeans.modules.php.spi.phpmodule.PhpModuleIgnoredFilesExtender;
import org.netbeans.modules.php.zend.commands.ZendCommandSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.NbBundle;

/**
 * @author Tomas Mysik
 */
public final class ZendPhpFrameworkProvider extends PhpFrameworkProvider {
    private static final Logger LOGGER = Logger.getLogger(ZendPhpFrameworkProvider.class.getName());

    private static final ZendPhpFrameworkProvider INSTANCE = new ZendPhpFrameworkProvider();
    private static final String COMMANDS_PROVIDER_NAME = "NetBeansZendCommandsProvider.php"; // NOI18N
    private static final File COMMANDS_PROVIDER;

    static {
        COMMANDS_PROVIDER = InstalledFileLocator.getDefault().locate(COMMANDS_PROVIDER_NAME, "org.netbeans.modules.php.zend", false);  // NOI18N
        if (COMMANDS_PROVIDER == null || !COMMANDS_PROVIDER.isFile()) {
            throw new IllegalStateException("Could not locate file " + COMMANDS_PROVIDER_NAME);
        }
    }

    public static ZendPhpFrameworkProvider getInstance() {
        return INSTANCE;
    }

    private ZendPhpFrameworkProvider() {
        super(NbBundle.getMessage(ZendPhpFrameworkProvider.class, "LBL_FrameworkName"), NbBundle.getMessage(ZendPhpFrameworkProvider.class, "LBL_FrameworkDescription"));
    }

    @Override
    public boolean isInPhpModule(PhpModule phpModule) {
        FileObject zfProject = phpModule.getSourceDirectory().getFileObject(".zfproject.xml"); // NOI18N
        return zfProject != null && zfProject.isData() && zfProject.isValid();
    }

    @Override
    public File[] getConfigurationFiles(PhpModule phpModule) {
        return new File[0];
    }

    @Override
    public PhpModuleExtender createPhpModuleExtender(PhpModule phpModule) {
        return new ZendPhpModuleExtender();
    }

    @Override
    public PhpModuleProperties getPhpModuleProperties(PhpModule phpModule) {
        FileObject sourceDirectory = phpModule.getSourceDirectory();
        PhpModuleProperties properties = new PhpModuleProperties();
        FileObject web = sourceDirectory.getFileObject("public"); // NOI18N
        if (web != null) {
            properties = properties.setWebRoot(web);
        }
        FileObject tests = sourceDirectory.getFileObject("tests"); // NOI18N
        if (tests != null) {
            properties = properties.setTests(tests);
        }
        return properties;
    }

    @Override
    public PhpModuleActionsExtender getActionsExtender(PhpModule phpModule) {
        return new ZendPhpModuleActionsExtender();
    }

    @Override
    public PhpModuleIgnoredFilesExtender getIgnoredFilesExtender(PhpModule phpModule) {
        return null;
    }

    @Override
    public ZendCommandSupport getFrameworkCommandSupport(PhpModule phpModule) {
        return new ZendCommandSupport(phpModule);
    }

    @Override
    public EditorExtender getEditorExtender(PhpModule phpModule) {
        return null;
    }

    @Override
    public void phpModuleOpened(PhpModule phpModule) {
        FileObject library = phpModule.getSourceDirectory().getFileObject("library");
        if (library == null || !library.isValid()) {
            LOGGER.warning("Folder 'library' not found underneath sources for " + phpModule);
            return;
        }
        try {
            FileObject oldCommandsProvider = library.getFileObject(COMMANDS_PROVIDER_NAME);
            if (oldCommandsProvider != null) {
                oldCommandsProvider.delete();
                LOGGER.info("Existing " + COMMANDS_PROVIDER_NAME + " found and deleted in " + phpModule);
            }
            FileObject newCommandsProvider = FileUtil.toFileObject(COMMANDS_PROVIDER);
            if (newCommandsProvider == null || !newCommandsProvider.isValid()) {
                LOGGER.warning("NB Commands Provider '" + COMMANDS_PROVIDER + "' has no valid FileObject?!");
                return;
            }
            FileUtil.copyFile(newCommandsProvider, library, newCommandsProvider.getName());
            LOGGER.info(COMMANDS_PROVIDER_NAME + " copied to " + phpModule);
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, null, ex);
        }
    }
}
