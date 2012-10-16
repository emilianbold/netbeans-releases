/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.symfony2;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import org.netbeans.modules.php.api.framework.BadgeIcon;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.api.phpmodule.PhpModuleProperties;
import org.netbeans.modules.php.spi.annotation.AnnotationCompletionTagProvider;
import org.netbeans.modules.php.spi.framework.commands.FrameworkCommandSupport;
import org.netbeans.modules.php.spi.framework.PhpFrameworkProvider;
import org.netbeans.modules.php.spi.framework.PhpModuleActionsExtender;
import org.netbeans.modules.php.spi.framework.PhpModuleCustomizerExtender;
import org.netbeans.modules.php.spi.framework.PhpModuleExtender;
import org.netbeans.modules.php.spi.framework.PhpModuleIgnoredFilesExtender;
import org.netbeans.modules.php.symfony2.annotations.extra.Symfony2ExtraAnnotationsProvider;
import org.netbeans.modules.php.symfony2.annotations.security.Symfony2SecurityAnnotationsProvider;
import org.netbeans.modules.php.symfony2.commands.Symfony2CommandSupport;
import org.netbeans.modules.php.symfony2.commands.Symfony2Script;
import org.netbeans.modules.php.symfony2.preferences.Symfony2Preferences;
import org.netbeans.modules.php.symfony2.ui.actions.Symfony2PhpModuleActionsExtender;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 * PHP framework provider for Symfony2 PHP framework.
 */
public final class Symfony2PhpFrameworkProvider extends PhpFrameworkProvider {

    private static final Symfony2PhpFrameworkProvider INSTANCE = new Symfony2PhpFrameworkProvider();
    private static final String ICON_PATH = "org/netbeans/modules/php/symfony2/ui/resources/symfony_badge_8.png"; // NOI18N
    private static final String CONFIG_DIRECTORY = "app/config"; // NOI18N

    private final BadgeIcon badgeIcon;


    @NbBundle.Messages({
        "LBL_FrameworkName=Symfony2 PHP Web Framework",
        "LBL_FrameworkDescription=Symfony2 PHP Web Framework"
    })
    private Symfony2PhpFrameworkProvider() {
        super("Symfony2 PHP Web Framework", Bundle.LBL_FrameworkName(), Bundle.LBL_FrameworkDescription()); // NOI18N
        badgeIcon = new BadgeIcon(
                ImageUtilities.loadImage(ICON_PATH),
                Symfony2PhpFrameworkProvider.class.getResource("/" + ICON_PATH)); // NOI18N
    }

    @PhpFrameworkProvider.Registration(position=99) // right before Symfony1
    public static Symfony2PhpFrameworkProvider getInstance() {
        return INSTANCE;
    }

    @Override
    public BadgeIcon getBadgeIcon() {
        return badgeIcon;
    }

    @Override
    public boolean isInPhpModule(PhpModule phpModule) {
        Boolean enabled = Symfony2Preferences.isEnabled(phpModule);
        if (enabled != null) {
            // set manually
            return enabled;
        }
        // autodetection
        FileObject console = Symfony2Script.getPath(phpModule);
        return console != null && console.isData();
    }

    @Override
    public File[] getConfigurationFiles(PhpModule phpModule) {
        FileObject sourceDirectory = phpModule.getSourceDirectory();
        if (sourceDirectory == null) {
            // broken project
            return new File[0];
        }
        FileObject configDir = sourceDirectory.getFileObject(CONFIG_DIRECTORY);
        if (configDir != null && configDir.isFolder() && configDir.isValid()) {
            File[] configFiles = FileUtil.toFile(configDir).listFiles();
            if (configFiles != null) {
                return configFiles;
            }
        }
        return new File[0];
    }

    @Override
    public PhpModuleExtender createPhpModuleExtender(PhpModule phpModule) {
        return new Symfony2PhpModuleExtender();
    }

    @Override
    public PhpModuleCustomizerExtender createPhpModuleCustomizerExtender(PhpModule phpModule) {
        return new Symfony2PhpModuleCustomizerExtender(phpModule);
    }

    @Override
    public PhpModuleProperties getPhpModuleProperties(PhpModule phpModule) {
        PhpModuleProperties properties = new PhpModuleProperties();
        FileObject sourceDirectory = phpModule.getSourceDirectory();
        if (sourceDirectory == null) {
            // broken project
            return properties;
        }
        FileObject web = sourceDirectory.getFileObject("web"); // NOI18N
        if (web != null) {
            properties = properties.setWebRoot(web);
        }
        // XXX unit tests?
        return properties;
    }

    @Override
    public PhpModuleActionsExtender getActionsExtender(PhpModule phpModule) {
        return new Symfony2PhpModuleActionsExtender();
    }

    @Override
    public PhpModuleIgnoredFilesExtender getIgnoredFilesExtender(PhpModule phpModule) {
        return new Symfony2PhpModuleIgnoredFilesExtender(phpModule);
    }

    @Override
    public FrameworkCommandSupport getFrameworkCommandSupport(PhpModule phpModule) {
        return new Symfony2CommandSupport(phpModule);
    }

    @Override
    public List<AnnotationCompletionTagProvider> getAnnotationsCompletionTagProviders(PhpModule phpModule) {
        return Arrays.<AnnotationCompletionTagProvider>asList(
                new Symfony2ExtraAnnotationsProvider(),
                new Symfony2SecurityAnnotationsProvider());
    }

}
