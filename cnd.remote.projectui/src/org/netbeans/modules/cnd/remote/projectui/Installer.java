/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.cnd.remote.projectui;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import org.openide.awt.Toolbar;
import org.openide.awt.ToolbarPool;
import org.openide.modules.ModuleInstall;
import org.openide.util.Lookup;
import org.openide.util.NbPreferences;
import org.openide.windows.WindowManager;

public class Installer extends ModuleInstall {

    private  static final Logger logger = Logger.getLogger(Installer.class.getName());
    
    private final String CONFIGURATION_FILENAME = "remotehosts.xml"; //NOI18N
    private final String LOCK_PROPERTIES_KEY = "remote.toolbar.visibility.corrected"; //NOI18N
    private final Preferences modulePreferences = NbPreferences.forModule(Installer.class);
    
    
    @Override
    public void restored() {
        final boolean isDesktopDistribution = (new File(System.getProperty("netbeans.home"), CONFIGURATION_FILENAME)).exists(); //NOI18N
        if (!locked(LOCK_PROPERTIES_KEY)) {
            lock(LOCK_PROPERTIES_KEY);
            WindowManager.getDefault().invokeWhenUIReady(new Runnable() {

                @Override
                public void run() {
                    Toolbar toolbar = ToolbarPool.getDefault().findToolbar("Remote"); //NOI18N
                    if (isDesktopDistribution) {
                        setToolbarVisible(toolbar, true, "Standard"); //NOI18N
                    } else {
                        setToolbarVisible(toolbar, false, "Standard"); //NOI18N
                        // The above line disables the toolbar at all starting from NetBeans 7.1 
                        // setToolbarVisible(toolbar, false, "Debugging"); //NOI18N
                    }
                }
            });
        }
    }

    private void lock(String key) {
        modulePreferences.putBoolean(key, true);
        try {
            modulePreferences.sync();
        } catch (BackingStoreException ex) {
            logger.log(Level.INFO, "Failed to sync module preferences", ex); //NOI18N
        }
    }
    
    private boolean locked(String key) {
        return modulePreferences.getBoolean(key, false);
    }
    
    private void setToolbarVisible(Toolbar toolbar, boolean visible, String configuration) {
        try {
            ClassLoader cl = Lookup.getDefault().lookup(ClassLoader.class);
            Class cToolbarConfiguration = cl.loadClass("org.netbeans.core.windows.view.ui.toolbars.ToolbarConfiguration"); //NOI18N
            Object toolbarConfig = cToolbarConfiguration.getMethod("findConfiguration", String.class).invoke(cToolbarConfiguration, configuration); //NOI18N
            toolbarConfig.getClass().getMethod("setToolbarVisible", Toolbar.class, boolean.class).invoke(toolbarConfig, toolbar, visible); //NOI18N
        } catch (IllegalAccessException ex) {
            logger.log(Level.INFO, "Failed to make toolbar visible using reflection", ex); //NOI18N
        } catch (IllegalArgumentException ex) {
            logger.log(Level.INFO, "Failed to make toolbar visible using reflection", ex); //NOI18N
        } catch (InvocationTargetException ex) {
            logger.log(Level.INFO, "Failed to make toolbar visible using reflection", ex); //NOI18N
        } catch (NoSuchMethodException ex) {
            logger.log(Level.INFO, "Failed to make toolbar visible using reflection", ex); //NOI18N
        } catch (SecurityException ex) {
            logger.log(Level.INFO, "Failed to make toolbar visible using reflection", ex); //NOI18N
        } catch (ClassNotFoundException ex) {
            logger.log(Level.INFO, "Failed to make toolbar visible using reflection", ex); //NOI18N
        }

    }
}
