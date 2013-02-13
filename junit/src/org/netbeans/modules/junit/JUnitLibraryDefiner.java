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

package org.netbeans.modules.junit;

import javax.swing.JButton;
import java.util.logging.Logger;
import java.util.concurrent.Callable;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.modules.autoupdate.ui.api.PluginManager;
import org.netbeans.spi.java.project.support.ui.BrokenReferencesSupport.LibraryDefiner;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle.Messages;
import org.openide.util.lookup.ServiceProvider;
import static org.netbeans.modules.junit.Bundle.*;

/**
 * Defines JUnit 3.x/4.x libraries by downloading their defining modules.
 */
@ServiceProvider(service=LibraryDefiner.class)
public class JUnitLibraryDefiner implements LibraryDefiner {

    @Messages({
        "library_button=&Library Manager...",
        "library_name=junit"
    })
    
    public @Override Callable<Library> missingLibrary(final String name) {
        if (!name.matches("junit(_4)?")) {
            return null;
        }
        return new Callable<Library>() {

            @SuppressWarnings("SleepWhileInLoop")
            public @Override Library call() throws Exception {
                JButton libraryManager = new JButton();
                Mnemonics.setLocalizedText(libraryManager, library_button());
                Object ret = PluginManager.installSingle("org.netbeans.modules.junitlib", library_name(), libraryManager);
                if (libraryManager.equals(ret)) {
                    throw new Exception("junitlib failed/canceled to install properly, open library manager instaed");
                }
                // XXX new library & build.properties apparently do not show up immediately... how to listen properly?
                for (int i = 0; i < 20; i++) {
                    Library lib = LibraryManager.getDefault().getLibrary(name);
                    if (lib != null) {
                        return lib;
                    }
                    Thread.sleep(1000);
                }
                return null;
            }
        };
    }

}
