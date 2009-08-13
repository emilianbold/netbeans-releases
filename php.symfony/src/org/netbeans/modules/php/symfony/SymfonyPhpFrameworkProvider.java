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

package org.netbeans.modules.php.symfony;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.api.phpmodule.PhpModuleProperties;
import org.netbeans.modules.php.spi.phpmodule.PhpFrameworkProvider;
import org.netbeans.modules.php.spi.phpmodule.PhpModuleActionsExtender;
import org.netbeans.modules.php.spi.phpmodule.PhpModuleExtender;
import org.netbeans.modules.php.spi.phpmodule.PhpModuleVisibilityExtender;
import org.netbeans.modules.php.symfony.commands.SymfonyCommandSupport;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 * @author Tomas Mysik
 */
public final class SymfonyPhpFrameworkProvider extends PhpFrameworkProvider {
    private static final SymfonyPhpFrameworkProvider INSTANCE = new SymfonyPhpFrameworkProvider();
    private static final List<String> SYMFONY_FILES = Arrays.asList(
            "apps",      // NOI18N
            "cache",     // NOI18N
            "config",    // NOI18N
            "data",      // NOI18N
            "test",      // NOI18N
            "web",       // NOI18N
            "symfony");  // NOI18N

    public static SymfonyPhpFrameworkProvider getInstance() {
        return INSTANCE;
    }

    private SymfonyPhpFrameworkProvider() {
        super(NbBundle.getMessage(SymfonyPhpFrameworkProvider.class, "LBL_FrameworkName"), NbBundle.getMessage(SymfonyPhpFrameworkProvider.class, "LBL_FrameworkDescription"));
    }

    @Override
    public boolean isInPhpModule(PhpModule phpModule) {
        FileObject sourceDirectory = phpModule.getSourceDirectory();
        for (String file : SYMFONY_FILES) {
            if (sourceDirectory.getFileObject(file) == null) {
                return false;
            }
        }
        return true;
    }

    @Override
    public File[] getConfigurationFiles(PhpModule phpModule) {
        return new File[0];
    }

    @Override
    public PhpModuleExtender createPhpModuleExtender(PhpModule phpModule) {
        return new SymfonyPhpModuleExtender();
    }

    @Override
    public PhpModuleProperties getPhpModuleProperties(PhpModule phpModule) {
        FileObject sourceDirectory = phpModule.getSourceDirectory();
        PhpModuleProperties properties = new PhpModuleProperties();
        FileObject web = sourceDirectory.getFileObject("web"); // NOI18N
        if (web != null) {
            properties = properties.setWebRoot(web);
        }
        FileObject testUnit = sourceDirectory.getFileObject("test/unit"); // NOI18N
        if (testUnit != null) {
            properties = properties.setTests(testUnit);
        }
        return properties;
    }

    @Override
    public PhpModuleActionsExtender getActionsExtender(PhpModule phpModule) {
        return new SymfonyPhpModuleActionsExtender();
    }

    @Override
    public PhpModuleVisibilityExtender getVisibilityExtender(PhpModule phpModule) {
        return new SymfonyPhpModuleVisibilityExtender(phpModule);
    }

    @Override
    public SymfonyCommandSupport getFrameworkCommandSupport(PhpModule phpModule) {
        return new SymfonyCommandSupport(phpModule);
    }
}
