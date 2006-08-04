/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.mobility.antext.preprocessor.CommentingPreProcessor;
import org.netbeans.modules.mobility.cldcplatform.PlatformConvertor;
import org.netbeans.modules.mobility.project.ui.customizer.J2MEProjectProperties;
import org.netbeans.spi.mobility.project.support.DefaultPropertyParsers;
import org.netbeans.modules.mobility.project.ProjectConfigurationsHelper;
import org.openide.ErrorManager;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;

/**
 *
 * @author Adam Sotona
 */
public class GlobalAbilitiesCache {
    
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
        try {
            FileObject fo = FileUtil.createFolder(Repository.getDefault().getDefaultFileSystem().getRoot(), PlatformConvertor.CFG_TEMPLATES_PATH);
            fo.addFileChangeListener(l);
            FileObject ch[] = fo.getChildren();
            for (int i=0; i<ch.length; i++) l.loadAbilities(ch[i]);
        } catch (IOException ioe) {
            ErrorManager.getDefault().notify(ioe);
        }
    }
    
    public Set<String> getAllAbilities() {
        return Collections.unmodifiableSet(globalAbilities);
    }
    
    public void addAbility(final String ability) {
        globalAbilities.add(ability);
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
            if (fo == null || !fo.isData() || !(fo.getExt().equals(PlatformConvertor.CFG_EXT) || fo.getExt().equals(PlatformConvertor.GCFG_EXT))) return; //NOI28N
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
