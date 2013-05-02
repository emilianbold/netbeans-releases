/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.makeproject.launchers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

/**
 *
 * @author Henk
 */
public final class LaunchersRegistry {

    private final List<Launcher> launchers;
    private static final Object lock = "LaunchersRegistryLock"; //NOI18N
    private static final String LAUNCHER_TAG = "launcher";  // NOI18N
    private static final String COMMON_TAG = "common";  // NOI18N
    private static final String COMMAND_TAG = "runCommand"; // NOI18N
    private static final String NAME_TAG = "displayName";   // NOI18N
    private static final String DIRECTORY_TAG = "runDir";   // NOI18N
    private static final String SYMFILES_TAG = "symbolFiles";// NOI18N    
    private static final String ENV_TAG = "env";// NOI18N

    LaunchersRegistry() {
        launchers = new ArrayList<Launcher>();
    }

    public void add(Launcher launcher) {
        synchronized (lock) {
            if (launchers.contains(launcher)) {
                return;
            }
            launchers.add(launcher);
        }
    }

    public void remove(Launcher launcher) {
        synchronized (lock) {
            launchers.remove(launcher);
        }
    }

    public boolean hasLaunchers() {
        return !launchers.isEmpty();
    }

    /**
     * Returns unmodified collection
     *
     * @return
     */
    public Collection<Launcher> getLaunchers() {
        return Collections.unmodifiableCollection(launchers);
    }

    void load(Properties properties) {
        synchronized (lock) {
            launchers.clear();
            Launcher common = create(COMMON_TAG, properties, null);
            for (String key : properties.stringPropertyNames()) {
                if (key.matches(LAUNCHER_TAG + "\\d*[.]" + COMMAND_TAG)) {//NOI18N
                    Launcher l = create(key.substring(0, key.indexOf("." + COMMAND_TAG)), properties, common);//NOI18N
                    if (l != null) {
                        launchers.add(l);
                    }
                }
            }
        }
    }

    private Launcher create(String name, Properties properties, Launcher common) {
        boolean commonLauncher = name.equals(COMMON_TAG);
        assert !commonLauncher || common == null : "common launcher can not have other common";//NOI18N
        final String command = properties.getProperty(name + "." + COMMAND_TAG);//NOI18N
        assert commonLauncher || command != null : "usual laucnher without command " + name;//NOI18N
        Launcher launcher = new Launcher(command, common);
        final String displayName = properties.getProperty(name + "." + NAME_TAG);//NOI18N
        if (displayName != null) {
            launcher.setName(displayName);
        } else {
            launcher.setName(command);
        }
        String directory = properties.getProperty(name + "." + DIRECTORY_TAG);//NOI18N
        //directory can be null and this is OK
        launcher.setRunDir(directory);
        final String symFiles = properties.getProperty(name + "." + SYMFILES_TAG);//NOI18N
        //symbol files can be null and this is OK
        launcher.setSymbolFiles(symFiles);
        for (String key : properties.stringPropertyNames()) {
            if (key.matches(name + "[.]" + ENV_TAG + "[.]\\w+")) {    //NOI18N
                launcher.putEnv(key.substring(key.lastIndexOf(".") + 1), properties.getProperty(key));
            }
        }
        
        return launcher;
    }
}
