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

/*
 * GlobalAbilitiesCache.java
 *
 */
package org.netbeans.modules.mobility.project;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.mobility.antext.preprocessor.CommentingPreProcessor;
import org.netbeans.modules.mobility.project.ui.customizer.J2MEProjectProperties;
import org.netbeans.modules.mobility.project.ProjectConfigurationsHelper;
import org.netbeans.spi.mobility.cfgfactory.ProjectConfigurationFactory;
import org.openide.ErrorManager;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Adam Sotona
 */
public class GlobalAbilitiesCache implements Runnable {
    
    private static GlobalAbilitiesCache instance = null;
    
    public static GlobalAbilitiesCache getDefault() {
        synchronized (GlobalAbilitiesCache.class) {
            if (instance == null) instance = new GlobalAbilitiesCache();
        }
        return instance;
    }
    
    protected final Set<String> globalAbilities = Collections.synchronizedSortedSet(new TreeSet<String>());
    
    /** Creates a new instance of GlobalAbilitiesCache */
    private GlobalAbilitiesCache() {
        L l = new L();
        OpenProjects.getDefault().addPropertyChangeListener(l);
        l.propertyChange(null);
        RequestProcessor.getDefault().post(this);
    }
    
    public Set<String> getAllAbilities() {
        return Collections.unmodifiableSet(globalAbilities);
    }
    
    public void addAbility(final String ability) {
        globalAbilities.add(ability);
    }

    public void run() {
        for (ProjectConfigurationFactory fac : Lookup.getDefault().lookupAll(ProjectConfigurationFactory.class)) {
            LinkedList<ProjectConfigurationFactory.Descriptor> list = new LinkedList();
            list.add(fac.getRootCategory());
            while (!list.isEmpty()) {
                ProjectConfigurationFactory.Descriptor des = list.removeFirst();
                if (des instanceof ProjectConfigurationFactory.CategoryDescriptor) {
                    list.addAll(((ProjectConfigurationFactory.CategoryDescriptor)des).getChildren());
                }
                if (des instanceof ProjectConfigurationFactory.ConfigurationTemplateDescriptor) {
                    Map<String, String> map = ((ProjectConfigurationFactory.ConfigurationTemplateDescriptor)des).getProjectConfigurationProperties();
                    if (map != null) {
                        map = CommentingPreProcessor.decodeAbilitiesMap(map.get(DefaultPropertiesDescriptor.ABILITIES));
                        if (map != null) globalAbilities.addAll(map.keySet());
                    }
                }
            }
        }
    }
    
    private class L extends FileChangeAdapter implements PropertyChangeListener {
        
        private L() {
            //Just to avoid creation of accessor class
        }
        
        public void propertyChange(@SuppressWarnings("unused")
		final PropertyChangeEvent evt) {
            final Project p[] = OpenProjects.getDefault().getOpenProjects();
            if (p == null) return;
            for (int i = 0; i < p.length; i++) {
                final ProjectConfigurationsHelper h = p[i].getLookup().lookup(ProjectConfigurationsHelper.class);

                if (h != null)
                    globalAbilities.addAll(h.getAllIdentifiers(false));
            }
        }
        
        public void fileDataCreated(final FileEvent fe) {
            final FileObject fo = fe.getFile();
            loadAbilities(fo);
        }
        
        public void loadAbilities(final FileObject fo) {
            if (fo == null || !fo.isData() || !(fo.getExt().equals(UserConfigurationTemplatesProvider.CFG_EXT))) return; //NOI28N
            try {
                final Properties p = new Properties();
                p.load(fo.getInputStream());
                final Map<String,String> abs = CommentingPreProcessor.decodeAbilitiesMap(p.getProperty(J2MEProjectProperties.CONFIG_PREFIX + fo.getName() + '.' + DefaultPropertiesDescriptor.ABILITIES));
                if (abs != null) globalAbilities.addAll(abs.keySet());
            } catch (IOException ioe) {
                ErrorManager.getDefault().notify(ioe);
            }
            
        }
    }
}
