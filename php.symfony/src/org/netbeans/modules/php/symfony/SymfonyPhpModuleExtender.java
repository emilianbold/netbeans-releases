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

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.api.util.Pair;
import org.netbeans.modules.php.spi.commands.FrameworkCommandSupport;
import org.netbeans.modules.php.spi.phpmodule.PhpModuleExtender;
import org.netbeans.modules.php.symfony.SymfonyScript.InvalidSymfonyScriptException;
import org.netbeans.modules.php.symfony.ui.wizards.NewProjectConfigurationPanel;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * @author Tomas Mysik
 */
public class SymfonyPhpModuleExtender extends PhpModuleExtender {
    //@GuardedBy(this)
    private NewProjectConfigurationPanel panel = null;

    @Override
    public Set<FileObject> extend(PhpModule phpModule) throws ExtendingException {
        // init project
        SymfonyScript symfonyScript = null;
        try {
            symfonyScript = SymfonyScript.getDefault();
        } catch (InvalidSymfonyScriptException ex) {
            // should not happen, must be handled in the wizard
            Exceptions.printStackTrace(ex);
        }
        assert symfonyScript.isValid() : "Symfony script has to be valid!";

        if (!symfonyScript.initProject(phpModule)) {
            // can happen if symfony script was not chosen
            Logger.getLogger(SymfonyPhpModuleExtender.class.getName())
                    .info("Framework Symfony not found in newly created project " + phpModule.getDisplayName());
            throw new ExtendingException(NbBundle.getMessage(SymfonyPhpModuleExtender.class, "MSG_NotExtended"));
        }

        // generate apps
        for (Pair<String, String[]> app : getPanel().getApps()) {
            symfonyScript.initApp(phpModule, app.first, app.second);
        }

        // prefetch commands
        FrameworkCommandSupport.forPhpModule(phpModule).refreshFrameworkCommandsLater(null);

        // return files
        Set<FileObject> files = new HashSet<FileObject>();
        FileObject databases = phpModule.getSourceDirectory().getFileObject("config/databases.yml"); // NOI18N
        assert databases != null;
        files.add(databases);
        return files;
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
        return getErrorMessage() == null;
    }

    @Override
    public String getErrorMessage() {
        try {
            SymfonyScript.getDefault();
        } catch (InvalidSymfonyScriptException ex) {
            return NbBundle.getMessage(SymfonyPhpModuleExtender.class, "MSG_CannotExtend", ex.getMessage());
        }
        return getPanel().getErrorMessage();
    }

    @Override
    public String getWarningMessage() {
        return getPanel().getWarningMessage();
    }

    private synchronized NewProjectConfigurationPanel getPanel() {
        if (panel == null) {
            panel = new NewProjectConfigurationPanel();
        }
        return panel;
    }
}
