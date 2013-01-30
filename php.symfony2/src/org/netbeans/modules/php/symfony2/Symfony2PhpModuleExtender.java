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
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.api.util.FileUtils;
import org.netbeans.modules.php.spi.framework.PhpModuleExtender;
import org.netbeans.modules.php.symfony2.options.Symfony2Options;
import org.netbeans.modules.php.symfony2.ui.wizards.NewProjectConfigurationPanel;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * Symfony2 PHP module extender.
 */
public class Symfony2PhpModuleExtender extends PhpModuleExtender {

    static final String SYMFONY_ZIP_ENTRY_PREFIX = "Symfony/"; // NOI18N

    //@GuardedBy(this)
    private NewProjectConfigurationPanel panel = null;


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
        return getPanel().getErrorMessage();
    }

    @Override
    public String getWarningMessage() {
        return null;
    }

    @NbBundle.Messages("MSG_NotExtended=<html>Symfony2 project not created!<br>(verify Symfony Edition in Tools > Options > PHP > Symfony2 or review IDE log)")
    @Override
    public Set<FileObject> extend(PhpModule phpModule) throws ExtendingException {
        try {
            unpackSandbox(phpModule);
        } catch (IOException ex) {
            Logger.getLogger(Symfony2PhpModuleExtender.class.getName())
                    .log(Level.INFO, "Cannot unpack Symfony Standard Edition.", ex);
            throw new ExtendingException(Bundle.MSG_NotExtended(), ex);
        }

        // prefetch commands
        Symfony2PhpFrameworkProvider.getInstance().getFrameworkCommandSupport(phpModule).refreshFrameworkCommandsLater(null);

        return getInitialFiles(phpModule);
    }

    private void unpackSandbox(PhpModule phpModule) throws IOException {
        String sandbox = Symfony2Options.getInstance().getSandbox();
        final File sourceDir = FileUtil.toFile(phpModule.getSourceDirectory());
        FileUtils.unzip(sandbox, sourceDir, new FileUtils.ZipEntryFilter() {
            @Override
            public boolean accept(ZipEntry zipEntry) {
                return !SYMFONY_ZIP_ENTRY_PREFIX.equals(zipEntry.getName());
            }
            @Override
            public String getName(ZipEntry zipEntry) {
                String entryName = zipEntry.getName();
                if (entryName.startsWith(SYMFONY_ZIP_ENTRY_PREFIX)) {
                    entryName = entryName.replaceFirst(SYMFONY_ZIP_ENTRY_PREFIX, ""); // NOI18N
                }
                return entryName;
            }
        });
    }

    private Set<FileObject> getInitialFiles(PhpModule phpModule) {
        Set<FileObject> files = new HashSet<FileObject>();
        addSourceFile(files, phpModule, "app/config/parameters.ini"); // NOI18N
        addSourceFile(files, phpModule, "src/Acme/DemoBundle/Controller/DemoController.php"); // NOI18N
        addSourceFile(files, phpModule, "src/Acme/DemoBundle/Resources/views/Demo/index.html.twig"); // NOI18N
        if (files.isEmpty()) {
            addSourceFile(files, phpModule, "web/app_dev"); // NOI18N
        }
        return files;
    }

    private void addSourceFile(Set<FileObject> files, PhpModule phpModule, String relativePath) {
        FileObject sourceDirectory = phpModule.getSourceDirectory();
        if (sourceDirectory == null) {
            // broken project
            assert false : "Module extender for no sources of: " + phpModule.getName();
            return;
        }
        FileObject fileObject = sourceDirectory.getFileObject(relativePath);
        if (fileObject != null) {
            files.add(fileObject);
        }
    }

    private synchronized NewProjectConfigurationPanel getPanel() {
        if (panel == null) {
            panel = new NewProjectConfigurationPanel();
        }
        return panel;
    }

}
