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
package org.netbeans.modules.php.smarty;

import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import org.netbeans.api.progress.ProgressUtils;
import org.netbeans.modules.php.api.phpmodule.BadgeIcon;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.api.phpmodule.PhpModuleProperties;
import org.netbeans.modules.php.editor.parser.astnodes.ClassInstanceCreation;
import org.netbeans.modules.php.editor.parser.astnodes.NamespaceName;
import org.netbeans.modules.php.editor.parser.astnodes.visitors.DefaultVisitor;
import org.netbeans.modules.php.smarty.editor.TplDataLoader;
import org.netbeans.modules.php.smarty.ui.options.SmartyOptions;
import org.netbeans.modules.php.spi.commands.FrameworkCommandSupport;
import org.netbeans.modules.php.spi.editor.EditorExtender;
import org.netbeans.modules.php.spi.phpmodule.PhpFrameworkProvider;
import org.netbeans.modules.php.spi.phpmodule.PhpModuleActionsExtender;
import org.netbeans.modules.php.spi.phpmodule.PhpModuleCustomizerExtender;
import org.netbeans.modules.php.spi.phpmodule.PhpModuleExtender;
import org.netbeans.modules.php.spi.phpmodule.PhpModuleIgnoredFilesExtender;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 * @author Martin Fousek
 */
public final class SmartyPhpFrameworkProvider extends PhpFrameworkProvider {

    /** Preferences property if the given {@link PhpModule} contains Smarty framework or not. */
    public static final String PROP_SMARTY_AVAILABLE = "smarty-framework"; // NOI18N

    private static final String ICON_PATH = "org/netbeans/modules/php/smarty/resources/smarty-badge-8.png"; // NOI18N
    private static final SmartyPhpFrameworkProvider INSTANCE = new SmartyPhpFrameworkProvider();

    private final BadgeIcon badgeIcon;

    @PhpFrameworkProvider.Registration(position=300)
    public static SmartyPhpFrameworkProvider getInstance() {
        return INSTANCE;
    }

    private SmartyPhpFrameworkProvider() {
        super("Smarty PHP Web Framework", //NOI18N
                NbBundle.getMessage(SmartyPhpFrameworkProvider.class, "LBL_FrameworkName"),  //NOI18N
                NbBundle.getMessage(SmartyPhpFrameworkProvider.class, "LBL_FrameworkDescription")); //NOI18N

        badgeIcon = new BadgeIcon(
                ImageUtilities.loadImage(ICON_PATH),
                SmartyPhpFrameworkProvider.class.getResource("/" + ICON_PATH)); // NOI18N
    }

    @Override
    public BadgeIcon getBadgeIcon() {
        return badgeIcon;
    }

    public static boolean isSmartyTemplateExtension(String ext) {
        for (String mimeExt : FileUtil.getMIMETypeExtensions(TplDataLoader.MIME_TYPE)) { // NOI18N
            if (ext.equals(mimeExt)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Try to locate (find) a TPL files in source directory.
     * Currently, it searches source dir and its subdirs.
     * @return {@code false} if not found
     */
    public static boolean locatedTplFiles(FileObject fo, int maxDepth, int actualDepth) {
        while (actualDepth <= maxDepth) {
            for (FileObject child : fo.getChildren()) {
                if (!child.isFolder()) {
                    if (isSmartyTemplateExtension(child.getExt())) {
                        return true;
                    }
                } else if (child.isFolder() && actualDepth < maxDepth) {
                    if (locatedTplFiles(child, maxDepth, actualDepth + 1)) {
                        return true;
                    }
                }
            }
            actualDepth++;
        }
        return false;
    }

    public static FileObject locate(PhpModule phpModule, String relativePath, boolean subdirs) {
        FileObject sourceDirectory = phpModule.getSourceDirectory();

        FileObject fileObject = sourceDirectory.getFileObject(relativePath);
        if (fileObject != null || !subdirs) {
            return fileObject;
        }
        for (FileObject child : sourceDirectory.getChildren()) {
            fileObject = child.getFileObject(relativePath);
            if (fileObject != null) {
                return fileObject;
            }
        }
        return null;
    }

    @Override
    public boolean isInPhpModule(final PhpModule phpModule) {
        // get php files within the module
        long time = System.currentTimeMillis();
        try {
            final AtomicBoolean isSmartyFound = new AtomicBoolean(false);
            final Preferences preferences = phpModule.getPreferences(SmartyPhpFrameworkProvider.class, true);

            // TODO - can be removed one release after NB71
            updateSmartyAvailableProperty(preferences);

            if (preferences.getBoolean(PROP_SMARTY_AVAILABLE, false)) {
                return true;
            }

            // search for appropriate MIME types inside source directory
            ProgressUtils.runOffEventDispatchThread(new Runnable() {

                @Override
                public void run() {
                    FileObject sourceDirectory = phpModule.getSourceDirectory();
                    if (locatedTplFiles(sourceDirectory, SmartyOptions.getInstance().getScanningDepth(), 0)) {
                        isSmartyFound.set(true);
                    }
                }
            }, NbBundle.getMessage(SmartyPhpFrameworkProvider.class, "MSG_SearchingForSmartyExt"),  // NOI18N
               new AtomicBoolean(false), false, 1000, 10000);

            if (isSmartyFound.get()) {
                preferences.putBoolean(PROP_SMARTY_AVAILABLE, true);
            }

            return isSmartyFound.get();
        } finally {
            Logger.getLogger(SmartyPhpFrameworkProvider.class.getName()).log(
                    Level.FINE, "Smarty.isInPhpModule total time spent={0} ms", (System.currentTimeMillis() - time)); //NOI18N
        }
    }

    /**
     * Temporary method for updating Smarty php module preferences to use
     * boolean value instead of flag for {@code #PROP_SMARTY_AVAILABLE).
     */
    private void updateSmartyAvailableProperty(Preferences preferences) {
        if (preferences.get(PROP_SMARTY_AVAILABLE, "0").equals("1")) { //NOI18N
            preferences.putBoolean(PROP_SMARTY_AVAILABLE, true);
        }
    }

    @Override
    public File[] getConfigurationFiles(PhpModule phpModule) {
        return new File[0];
    }

    @Override
    public PhpModuleExtender createPhpModuleExtender(PhpModule phpModule) {
        return null;
    }

    @Override
    public PhpModuleProperties getPhpModuleProperties(PhpModule phpModule) {
        PhpModuleProperties properties = new PhpModuleProperties();
        FileObject web = locate(phpModule, "web", true); // NOI18N
        if (web != null) {
            properties = properties.setWebRoot(web);
        }
        FileObject testUnit = locate(phpModule, "test/unit", true); // NOI18N
        if (testUnit != null) {
            properties = properties.setTests(testUnit);
        }
        return properties;
    }

    @Override
    public PhpModuleActionsExtender getActionsExtender(PhpModule phpModule) {
        return null;
    }

    @Override
    public PhpModuleIgnoredFilesExtender getIgnoredFilesExtender(PhpModule phpModule) {
        return null;
    }

    @Override
    public FrameworkCommandSupport getFrameworkCommandSupport(PhpModule phpModule) {
        return null;
    }

    @Override
    public EditorExtender getEditorExtender(PhpModule phpModule) {
        return null;
    }

    @Override
    public PhpModuleCustomizerExtender createPhpModuleCustomizerExtender(PhpModule phpModule) {
        return new SmartyPhpModuleCustomizerExtender(phpModule);
    }

    private static final class SmartyVerificationVisitor extends DefaultVisitor {

        private boolean foundSmarty;

        public SmartyVerificationVisitor() {
            foundSmarty = false;
        }

        @Override
        public void visit(ClassInstanceCreation node) {
            super.visit(node);
            if (node.getClassName().getName() instanceof NamespaceName) {
                NamespaceName name = ((NamespaceName) node.getClassName().getName());
                if (!name.getSegments().isEmpty()) {
                    if (name.getSegments().iterator().next().getName().equals(SmartyFramework.BASE_CLASS_NAME)) {
                        foundSmarty = true;
                    }
                }
            }
        }

        public boolean isFoundSmarty() {
            return foundSmarty;
        }
    }
}
