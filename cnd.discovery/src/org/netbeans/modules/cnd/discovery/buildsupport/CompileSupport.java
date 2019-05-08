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
package org.netbeans.modules.cnd.discovery.buildsupport;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.Iterator;
import java.util.logging.Level;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.netbeans.modules.cnd.api.toolchain.Tool;
import org.netbeans.modules.cnd.makeproject.api.configurations.Item;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfigurationDescriptor;
import org.netbeans.modules.cnd.makeproject.spi.configurations.AllOptionsProvider;
import org.netbeans.modules.cnd.makeproject.spi.configurations.CompileOptionsProvider;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup.Provider;
import org.openide.util.NbBundle;

/**
 *
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.cnd.makeproject.spi.configurations.CompileOptionsProvider.class)
public class CompileSupport extends CompileOptionsProvider {
    private static final String STORAGE_SUFFIX = "properties"; // NOI18N
    
    public CompileSupport() {
    }

    @Override
    public AllOptionsProvider getOptions(Item item) {
        NativeProject nativeProject = item.getNativeProject();
        Provider project = nativeProject.getProject();
        if (project instanceof Project) {
            final MakeConfigurationDescriptor cd = item.getFolder().getConfigurationDescriptor();
            MakeConfiguration makeConfiguration = cd.getActiveConfiguration();
            if (makeConfiguration != null) {
                String confName = makeConfiguration.getName();
                String itemPath = item.getAbsolutePath();
                return getOptions(cd, confName, itemPath);
            }
        }
        return null;
    }

    @Override
    public void onRename(MakeConfigurationDescriptor cd, MakeConfiguration makeConfiguration, String newName) {
        FileObject nbPrivateProjectFileObject = cd.getNbPrivateProjectFileObject();
        String confName = makeConfiguration.getName();
        FileObject properties = nbPrivateProjectFileObject.getFileObject(confName+"."+STORAGE_SUFFIX); // NOI18N
        if (properties != null && properties.isValid()) {
            FileLock lock = null;
            try {
                lock = properties.lock();
                properties.rename(lock, newName, STORAGE_SUFFIX);
            } catch (IOException ex) {
                BuildProjectActionHandler.logger.log(Level.WARNING, 
                        NbBundle.getMessage(CompileSupport.class, "CANNOT_RENAME_COMPILE_LINES"), properties.getPath()); // NOI18N
            } finally {
                if (lock != null) {
                    lock.releaseLock();
                }
            }
        }
    }

    @Override
    public void onRemove(MakeConfigurationDescriptor cd, MakeConfiguration makeConfiguration) {
        FileObject nbPrivateProjectFileObject = cd.getNbPrivateProjectFileObject();
        String confName = makeConfiguration.getName();
        FileObject properties = nbPrivateProjectFileObject.getFileObject(confName+"."+STORAGE_SUFFIX); // NOI18N
        if (properties != null && properties.isValid()) {
            FileLock lock = null;
            try {
                lock = properties.lock();
                properties.delete(lock);
            } catch (IOException ex) {
                BuildProjectActionHandler.logger.log(Level.WARNING, 
                        NbBundle.getMessage(CompileSupport.class, "CANNOT_REMOVE_COMPILE_LINES"), properties.getPath()); // NOI18N
            } finally {
                if (lock != null) {
                    lock.releaseLock();
                }
            }
        }
    }
    
    public void putOptions(MakeConfigurationDescriptor cd, MakeConfiguration makeConfiguration, Iterator<String> it) {
        FileObject nbPrivateProjectFileObject = cd.getNbPrivateProjectFileObject();
        String confName = makeConfiguration.getName();
        FileObject properties = nbPrivateProjectFileObject.getFileObject(confName+"."+STORAGE_SUFFIX); // NOI18N
        if (properties == null) {
            try {
                properties = FileUtil.createData(nbPrivateProjectFileObject, confName+"."+STORAGE_SUFFIX); // NOI18N
            } catch (IOException ex) {
                BuildProjectActionHandler.logger.log(Level.WARNING, 
                        NbBundle.getMessage(CompileSupport.class, "CANNOT_SAVE_COMPILE_LINES"), // NOI18N
                        nbPrivateProjectFileObject.getPath()+"/"+confName+"."+STORAGE_SUFFIX); // NOI18N
                return;
            }
        }
        if (properties != null && properties.isValid()) {
            FileLock lock = null;
            PrintStream outputStream = null;
            try {
                lock = properties.lock();
                outputStream = new PrintStream(properties.getOutputStream(lock), false, "UTF-8"); // NOI18N
                while(it.hasNext()){
                    String next = it.next();
                    if (next != null && next.length()>0) {
                        outputStream.println(next);
                    }
                }
                
            } catch (IOException ex) {
                BuildProjectActionHandler.logger.log(Level.WARNING, 
                        NbBundle.getMessage(CompileSupport.class, "CANNOT_SAVE_COMPILE_LINES"), properties.getPath()); // NOI18N
            } finally {
                if (outputStream != null) {
                    outputStream.close();
                }
                if (lock != null) {
                    lock.releaseLock();
                }
            }
        }
    }
    
    private AllOptionsProvider getOptions(MakeConfigurationDescriptor cd, String confName, String itemPath) {
        FileObject nbPrivateProjectFileObject = cd.getNbPrivateProjectFileObject();
        FileObject properties = nbPrivateProjectFileObject.getFileObject(confName+"."+STORAGE_SUFFIX); // NOI18N
        if (properties != null && properties.isValid()) {
            BufferedReader in = null;
            try {
                in = new BufferedReader(new InputStreamReader(properties.getInputStream(), "UTF-8")); // NOI18N
                String line = null;
                String normItemPath = null;
                if (itemPath.indexOf('\\') >=0 ) { // NOI18N
                    normItemPath = itemPath.replace('\\', '/'); // NOI18N
                }
                while (true) {
                    line = in.readLine();
                    if (line == null) {
                        break;
                    }
                    if (line.startsWith(itemPath)) {
                        if (line.startsWith(itemPath+"=")) { // NOI18N
                            break;
                        }
                    } else if (normItemPath != null && line.startsWith(normItemPath)) {
                        if (line.startsWith(normItemPath+"=")) { // NOI18N
                            break;
                        }
                    }
                }
                if (line != null) {
                    final String resLine = line.substring(line.indexOf('=')+1); // NOI18N
                    return new AllOptionsProvider() {

                        @Override
                        public String getAllOptions(Tool tool) {
                            return resLine;
                        }
                    };
                }
            } catch (IOException ex) {
                BuildProjectActionHandler.logger.log(Level.WARNING, 
                        NbBundle.getMessage(CompileSupport.class, "CANNOT_READ_COMPILE_LINES"), properties.getPath()); // NOI18N
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException ex) {
                        // do nothing
                    }
                }
            }
        }
        return null;
    }
}
