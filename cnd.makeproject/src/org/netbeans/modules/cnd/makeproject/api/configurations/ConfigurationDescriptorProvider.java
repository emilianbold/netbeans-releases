/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.cnd.makeproject.api.configurations;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.netbeans.modules.cnd.api.compilers.CompilerSet;
import org.netbeans.modules.cnd.api.compilers.Tool;
import org.netbeans.modules.cnd.makeproject.api.platforms.Platforms;
import org.netbeans.modules.cnd.makeproject.configurations.ConfigurationXMLReader;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.util.Lookup;

public class ConfigurationDescriptorProvider {
    public static final String USG_PROJECT_CONFIG_CND = "USG_PROJECT_CONFIG_CND"; // NOI18N
    public static final String USG_PROJECT_OPEN_CND = "USG_PROJECT_OPEN_CND"; // NOI18N
    private FileObject projectDirectory;
    private ConfigurationDescriptor projectDescriptor = null;
    boolean hasTried = false;
    private String relativeOffset = null;

    private List<FileObject> trackedFiles;
    private volatile boolean needReload;
    
    public ConfigurationDescriptorProvider(FileObject projectDirectory) {
        this.projectDirectory = projectDirectory;
    }
    
    public void setRelativeOffset(String relativeOffset) {
        this.relativeOffset = relativeOffset;
    }
    
    private final Object readLock = new Object();
    public ConfigurationDescriptor getConfigurationDescriptor() {
        if (projectDescriptor == null || needReload) {
            // attempt to read configuration descriptor
            if (!hasTried) {
                // do this only once
                synchronized (readLock) {
                    // check again that someone already havn't read
                    if (!hasTried) {
                        // It's important to set needReload=false before calling
                        // projectDescriptor.assign(), otherwise there will be
                        // infinite recursion.
                        needReload = false;

                        if (trackedFiles == null) {
                            FileChangeListener fcl = new ConfigurationXMLChangeListener();
                            trackedFiles = new ArrayList<FileObject>();
                            for (String path : new String[] {
                                    "nbproject/configurations.xml", //NOI18N
                                    "nbproject/private/configurations.xml"}) { //NOI18N
                                FileObject fo = projectDirectory.getFileObject(path);
                                if (fo != null) {
                                    fo.addFileChangeListener(fcl);
                                    // We have to store tracked files somewhere.
                                    // Otherwise they will be GCed, and we won't get notifications.
                                    trackedFiles.add(fo);
                                }
                            }
                        }
                        ConfigurationXMLReader reader = new ConfigurationXMLReader(projectDirectory);
                        ConfigurationDescriptor newDescriptor = null;

                        //if (SwingUtilities.isEventDispatchThread()) {
                        //    new Exception("Not allowed to use EDT for reading XML descriptor of project!").printStackTrace(System.err); // NOI18N
                        //    // PLEASE DO NOT ADD HACKS like Task.waitFinished()
                        //    // CHANGE YOUR LOGIC INSTEAD
                        //
                        //    // FIXUP for IZ#146696: cannot open projects: Not allowed to use EDT...
                        //    // return null;
                        //}
                        try {
                            projectDescriptor = reader.read(relativeOffset);
                        } catch (java.io.IOException x) {
                            // most likely open failed
                        }
                        
                        hasTried = true;
                        recordMetrics(USG_PROJECT_OPEN_CND, projectDescriptor);
                    }
                }
            }
        }
        return projectDescriptor;
    }

    public boolean gotDescriptor() {
        return projectDescriptor != null;   
    }
    
    public static ConfigurationAuxObjectProvider[] getAuxObjectProviders() {
        HashSet auxObjectProviders = new HashSet();
        Lookup.Template template = new Lookup.Template(ConfigurationAuxObjectProvider.class);
        Lookup.Result result = Lookup.getDefault().lookup(template);
        Collection collection = result.allInstances();
//      System.err.println("-------------------------------collection " + collection);
        Iterator iterator = collection.iterator();
        while (iterator.hasNext()) {
            Object caop = iterator.next();
            if (caop instanceof ConfigurationAuxObjectProvider) {
                auxObjectProviders.add(caop);
            }
        }
//      System.err.println("-------------------------------auxObjectProviders " + auxObjectProviders);
        return (ConfigurationAuxObjectProvider[])auxObjectProviders.toArray(new ConfigurationAuxObjectProvider[auxObjectProviders.size()]);
    }

    public static void recordMetrics(String msg, ConfigurationDescriptor descr) {
        if (!(descr instanceof MakeConfigurationDescriptor)) {
            return;
        }
        Logger logger = Logger.getLogger("org.netbeans.ui.metrics.cnd"); // NOI18N
        if (logger.isLoggable(Level.INFO)) {
            LogRecord rec = new LogRecord(Level.INFO, msg);
                MakeConfiguration makeConfiguration = (MakeConfiguration) descr.getConfs().getActive();
                String type;
                switch (makeConfiguration.getConfigurationType().getValue()) {
                    case MakeConfiguration.TYPE_MAKEFILE:
                        type = "MAKEFILE"; // NOI18N
                        break;
                    case MakeConfiguration.TYPE_APPLICATION:
                        type = "APPLICATION"; // NOI18N
                        break;
                    case MakeConfiguration.TYPE_DYNAMIC_LIB:
                        type = "DYNAMIC_LIB"; // NOI18N
                        break;
                    case MakeConfiguration.TYPE_STATIC_LIB:
                        type = "STATIC_LIB"; // NOI18N
                        break;
                    default:
                        type = "UNKNOWN"; // NOI18N
                }
                String host;
                CompilerSet compilerSet;
                if (makeConfiguration.getDevelopmentHost().isLocalhost()) {
                    host = "LOCAL"; // NOI18N
                    compilerSet = makeConfiguration.getCompilerSet().getCompilerSet();
                } else {
                    host = "REMOTE"; // NOI18N
                    // do not force creation of compiler sets
                    compilerSet = null;
                }
                String flavor;
                String[] families;
                if (compilerSet != null) {
                    families = compilerSet.getCompilerFlavor().getToolchainDescriptor().getFamily();
                    flavor = compilerSet.getCompilerFlavor().toString();
                } else {
                    families = new String[0];
                    flavor = makeConfiguration.getCompilerSet().getFlavor();
                }
                String family;
                if (families.length == 0) {
                    family = "UKNOWN"; // NOI18N
                } else {
                    StringBuilder buffer = new StringBuilder();
                    for (int i = 0; i < families.length; i++) {
                        buffer.append(families[i]);
                        if (i < families.length - 1) {
                            buffer.append(","); // NOI18N
                        }
                    }
                    family = buffer.toString();
                }                
                String platform;
                if (Platforms.getPlatform(makeConfiguration.getCompilerSet().getPlatform()) != null) {
                    platform = Platforms.getPlatform(makeConfiguration.getCompilerSet().getPlatform()).getName();
                } else {
                    platform = "UNKNOWN_PLATFORM"; // NOI18N
                }
                makeConfiguration.reCountLanguages((MakeConfigurationDescriptor) descr);
                Item[] projectItems = ((MakeConfigurationDescriptor) descr).getProjectItems();
                int size = 0;
                int allItems = projectItems.length;
                boolean cLang = false;
                boolean ccLang = false;
                boolean fLang = false;
                for (Item item : projectItems) {
                    ItemConfiguration itemConfiguration = item.getItemConfiguration(makeConfiguration);
                    if (!itemConfiguration.getExcluded().getValue()) {
                        size++;
                        switch (itemConfiguration.getTool()) {
                            case Tool.CCompiler:
                                cLang = true;
                                break;
                            case Tool.CCCompiler:
                                ccLang = true;
                                break;
                            case Tool.FortranCompiler:
                                fLang = true;
                                break;
                        }
                    }
                }
                String ccUsage = ccLang ? "USE_CPP" : "NO_CPP"; // NOI18N
                String cUsage = cLang ? "USE_C" : "NO_C"; // NOI18N
                String fUsage = fLang ? "USE_FORTRAN" : "NO_FORTRAN"; // NOI18N
                rec.setParameters(new Object[] { type, flavor, family, host, platform, toSizeString(allItems), toSizeString(size), ccUsage, cUsage, fUsage});
                rec.setLoggerName(logger.getName());
                logger.log(rec);
        }
    }

    private static String toSizeString(int size) {
        String strSize;
        if (size < 25) {
            strSize = "25"; // NOI18N
        } else if (size < 100) {
            strSize = "100"; // NOI18N
        } else if (size < 500) {
            strSize = "500"; // NOI18N
        } else if (size < 1000) {
            strSize = "1000"; // NOI18N
        } else if (size < 2000) {
            strSize = "2000"; // NOI18N
        } else if (size < 5000) {
            strSize = "5000"; // NOI18N
        } else if (size < 10000) {
            strSize = "10000"; // NOI18N
        } else if (size < 20000) {
            strSize = "20000"; // NOI18N
        } else if (size < 50000) {
            strSize = "50000"; // NOI18N
        } else {
            strSize = "99999"; // NOI18N
        }
        return strSize;
    }

    /**
     * This listener will be notified about updates of files
     * <code>nbproject/configurations.xml</code> and
     * <code>nbproject/private/configurations.xml</code>.
     * These files should be reloaded when changed externally.
     * See IZ#146701: can't update project through subversion, or any other
     */
    private class ConfigurationXMLChangeListener implements FileChangeListener {

        private void resetConfiguration() {
            if (projectDescriptor != null && projectDescriptor.getModified()) {
                // Don't reload if descriptor is modified in memory.
                // This also prevents reloading when descriptor is being saved.
                return;
            }
            synchronized (readLock) {
                needReload = true;
                hasTried = false;
            }
        }

        public void fileFolderCreated(FileEvent fe) {
            resetConfiguration();
        }

        public void fileDataCreated(FileEvent fe) {
            resetConfiguration();
        }

        public void fileChanged(FileEvent fe) {
            resetConfiguration();
        }

        public void fileDeleted(FileEvent fe) {
            resetConfiguration();
        }

        public void fileRenamed(FileRenameEvent fe) {
            resetConfiguration();
        }

        public void fileAttributeChanged(FileAttributeEvent fe) {
            resetConfiguration();
        }

    }

}
