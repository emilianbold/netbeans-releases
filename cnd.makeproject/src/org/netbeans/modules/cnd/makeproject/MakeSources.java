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

package org.netbeans.modules.cnd.makeproject;

import java.io.File;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.cnd.api.utils.IpeUtils;
import org.netbeans.modules.cnd.makeproject.api.configurations.Configuration;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptor;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptorProvider;
import org.netbeans.modules.cnd.makeproject.api.configurations.Item;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfigurationDescriptor;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakefileConfiguration;
import org.netbeans.modules.cnd.makeproject.api.remote.FilePathAdaptor;
import org.netbeans.spi.project.support.ant.AntProjectEvent;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.AntProjectListener;
import org.netbeans.spi.project.support.ant.SourcesHelper;
import org.netbeans.spi.project.support.ant.SourcesHelper.SourceRootConfig;
import org.openide.util.ChangeSupport;

/**
 * Handles source dir list for a freeform project.
 * XXX will not correctly unregister released external source roots
 */
public class MakeSources implements Sources, AntProjectListener {

    private MakeProject project;
    private AntProjectHelper helper;

    public MakeSources(MakeProject project, AntProjectHelper helper) {
        this.project = project;
        this.helper = helper;
        helper.addAntProjectListener(this);
        changeSupport = new ChangeSupport(this);
    }
    private Sources delegate;
    private final ChangeSupport changeSupport;
    private long eventID = 0;
    
    public SourceGroup[] getSourceGroups(String str) {
        if (!str.equals("generic")) { // NOI18N
            return new SourceGroup[0];
        }
        Sources srcs;
        long curEvent;
        synchronized (this) {
            srcs = delegate;
            curEvent = eventID;
        }

        if (srcs == null) {
            srcs = initSources();
        }

        synchronized (this){
            if (curEvent == eventID) {
                delegate = srcs;
            }
        }
        SourceGroup[] sg = srcs.getSourceGroups(str);
        return sg;
    }

    private Sources initSources() {
        SourcesHelper h = new SourcesHelper(project, helper, project.evaluator());
        ConfigurationDescriptorProvider pdp = project.getLookup().lookup(ConfigurationDescriptorProvider.class);
        ConfigurationDescriptor pd = pdp.getConfigurationDescriptor(!MakeProjectConfigurationProvider.ASYNC_LOAD);
        if (pd != null) {
            MakeConfigurationDescriptor epd = (MakeConfigurationDescriptor) pd;
            Set<String> set = new LinkedHashSet<String>();
            
            // Add external folders to sources.
            if (epd.getVersion() < 41) {
                Item[] projectItems = epd.getProjectItems();
                if (projectItems != null) {
                    for (int i = 0; i < projectItems.length; i++) {
                        Item item = projectItems[i];
                        String name = item.getPath();
                        if (!IpeUtils.isPathAbsolute(name)) {
                            continue;
                        }
                        File file = new File(name);
                        if (!file.exists()) {
                            continue;
                        }
                        if (!file.isDirectory()) {
                            file = file.getParentFile();
                        }
                        name = file.getPath();
                        set.add(name);
                        epd.addSourceRootRaw(IpeUtils.toRelativePath(epd.getBaseDir(), name));
                    }
                }
            }
            // Add source roots to set (>= V41)
            List<String> list = epd.getAbsoluteSourceRoots();
            for (String sr : list) {
                set.add(sr);
            }
            
            // Add buildfolder from makefile projects to sources. See IZ 90190.
            if (epd.getVersion() < 41) {
                Configuration[] confs = epd.getConfs().getConfs();
                for (int i = 0; i < confs.length; i++) {
                    MakeConfiguration makeConfiguration = (MakeConfiguration) confs[i];
                    if (makeConfiguration.isMakefileConfiguration()) {
                        MakefileConfiguration makefileConfiguration = makeConfiguration.getMakefileConfiguration();
                        String path = makefileConfiguration.getAbsBuildCommandWorkingDir();
                        set.add(path);
                        epd.addSourceRootRaw(IpeUtils.toRelativePath(epd.getBaseDir(), path));
                    }
                }
            }
            
            for (String name : set) {
                String displayName = IpeUtils.toRelativePath(epd.getBaseDir(), name);
//                int index1 = displayName.lastIndexOf(File.separatorChar);
//                if (index1 > 0) {
//                    int index2 = displayName.substring(0, index1).lastIndexOf(File.separatorChar);
//                    if (index2 > 0) {
//                        displayName = "..." + displayName.substring(index2); // NOI18N
//                    }
//                }
                displayName = FilePathAdaptor.naturalize(displayName);
                h.sourceRoot(name).displayName(displayName).add();
                h.sourceRoot(name).type("generic").displayName(displayName).add(); // NOI18N
            }
        }
        h.registerExternalRoots(FileOwnerQuery.EXTERNAL_ALGORITHM_TRANSIENT);
        return h.createSources();
    }

    public void addChangeListener(ChangeListener changeListener) {
        changeSupport.addChangeListener(changeListener);
    }

    public void removeChangeListener(ChangeListener changeListener) {
        changeSupport.removeChangeListener(changeListener);
    }

    private void fireChange() {
        delegate = null;
        changeSupport.fireChange();
    }

    public void configurationXmlChanged(AntProjectEvent ev) {
        // fireChange(); // ignore - cnd projects don't keep source file info in project.xml
    }

    public void descriptorChanged() {
        // fireChange(); // ignore
    }

    public void propertiesChanged(AntProjectEvent ev) {
        // ignore
    }
    
    public void sourceRootsChanged() {
        fireChange();
    }
}