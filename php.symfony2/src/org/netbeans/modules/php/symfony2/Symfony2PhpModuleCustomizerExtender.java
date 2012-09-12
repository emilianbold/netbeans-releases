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

import java.util.EnumSet;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.api.util.StringUtils;
import org.netbeans.modules.php.spi.framework.PhpModuleCustomizerExtender;
import org.netbeans.modules.php.symfony2.commands.Symfony2Script;
import org.netbeans.modules.php.symfony2.preferences.Symfony2Preferences;
import org.netbeans.modules.php.symfony2.ui.customizer.Symfony2CustomizerPanel;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle.Messages;

/**
 * Customizer extender.
 */
public class Symfony2PhpModuleCustomizerExtender extends PhpModuleCustomizerExtender {

    private final PhpModule phpModule;
    private final boolean originalEnabled;
    private final String originalAppDir;
    private final boolean originalCacheDirIgnored;

    // @GuardedBy(EDT)
    private Symfony2CustomizerPanel component;
    // @GuardedBy(EDT)
    private boolean valid = false;
    // @GuardedBy(EDT)
    private String errorMessage = null;


    Symfony2PhpModuleCustomizerExtender(PhpModule phpModule) {
        this.phpModule = phpModule;

        originalEnabled = Symfony2PhpFrameworkProvider.getInstance().isInPhpModule(phpModule);
        originalAppDir = Symfony2Preferences.getAppDir(phpModule);
        originalCacheDirIgnored = Symfony2Preferences.isCacheDirIgnored(phpModule);
    }

    @Messages("LBL_Symfony2=Symfony2")
    @Override
    public String getDisplayName() {
        return Bundle.LBL_Symfony2();
    }

    @Override
    public void addChangeListener(ChangeListener listener) {
        getPanel().addChangeListener(listener);
    }

    @Override
    public void removeChangeListener(ChangeListener listener) {
        getPanel().removeChangeListener(listener);
    }

    @Override
    public JComponent getComponent() {
        return getPanel();
    }

    @Override
    public HelpCtx getHelp() {
        return null;
    }

    @Override
    public boolean isValid() {
        validate();
        return valid;
    }

    @Override
    public String getErrorMessage() {
        validate();
        return errorMessage;
    }

    @Override
    public EnumSet<Change> save(PhpModule phpModule) {
        EnumSet<Change> changes = EnumSet.noneOf(Change.class);
        saveEnabled(changes);
        saveAppDir(changes);
        saveCacheIgnored(changes);
        if (changes.isEmpty()) {
            return null;
        }
        return changes;
    }

    private void saveEnabled(EnumSet<Change> changes) {
        boolean newEnabled = getPanel().isSupportEnabled();
        if (newEnabled != originalEnabled) {
            Symfony2Preferences.setEnabled(phpModule, newEnabled);
            changes.add(Change.FRAMEWORK_CHANGE);
        }
    }

    private void saveAppDir(EnumSet<Change> changes) {
        String newAppDir = getPanel().getAppDirectory();
        if (!newAppDir.equals(originalAppDir)) {
            Symfony2Preferences.setAppDir(phpModule, newAppDir);
            changes.add(Change.FRAMEWORK_CHANGE);
        }
    }

    private void saveCacheIgnored(EnumSet<Change> changes) {
        boolean newIgnored = getPanel().isIgnoreCacheDirectory();
        if (newIgnored != originalCacheDirIgnored) {
            Symfony2Preferences.setCacheDirIgnored(phpModule, newIgnored);
            changes.add(Change.IGNORED_FILES_CHANGE);
        }
    }

    private Symfony2CustomizerPanel getPanel() {
        if (component == null) {
            component = new Symfony2CustomizerPanel(phpModule.getSourceDirectory());
            component.setSupportEnabled(originalEnabled);
            component.setAppDirectory(originalAppDir);
            component.setIgnoreCacheDirectory(originalCacheDirIgnored);
        }
        return component;
    }

    @Messages({
        "Symfony2PhpModuleCustomizerExtender.error.appDir.empty=App directory must be set.",
        "Symfony2PhpModuleCustomizerExtender.error.appDir.notChild=App directory must be underneath Source Files.",
        "Symfony2PhpModuleCustomizerExtender.error.appDir.consoleNotFound=Console script not found underneath App directory."
    })
    private void validate() {
        Symfony2CustomizerPanel panel = getPanel();
        if (!panel.isSupportEnabled()) {
            // nothing to validate
            valid = true;
            errorMessage = null;
            return;
        }
        // check app dir
        String appDir = panel.getAppDirectory();
        if (!StringUtils.hasText(appDir)) {
            valid = false;
            errorMessage = Bundle.Symfony2PhpModuleCustomizerExtender_error_appDir_empty();
            return;
        }
        FileObject sources = phpModule.getSourceDirectory();
        FileObject fo = sources.getFileObject(appDir);
        if (fo == null
                || !FileUtil.isParentOf(sources, fo)) {
            valid = false;
            errorMessage = Bundle.Symfony2PhpModuleCustomizerExtender_error_appDir_notChild();
            return;
        }
        if (Symfony2Script.getPath(phpModule, appDir) == null) {
            valid = false;
            errorMessage = Bundle.Symfony2PhpModuleCustomizerExtender_error_appDir_consoleNotFound();
            return;
        }
        // everything ok
        valid = true;
        errorMessage = null;
    }

}
