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

package org.netbeans.modules.editor.mimelookup.impl;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.spi.editor.mimelookup.InstanceProvider;
import org.openide.util.Lookup;
import org.openide.util.NbCollections;
import org.openide.util.WeakListeners;
import org.openide.util.lookup.ProxyLookup;

/**
 *
 * @author vita
 */
public class SwitchLookup extends Lookup {

    private static final Logger LOG = Logger.getLogger(SwitchLookup.class.getName());
    
    /* package */ static final String ROOT_FOLDER = "Editors"; //NOI18N

    private MimePath mimePath;

    private final String LOCK = new String("SwitchLookup.LOCK"); //NOI18N
    
    private MappingListener listener;
    
    private Map<String,UpdatableProxyLookup> classLookups = new HashMap<String,UpdatableProxyLookup>();
    private Map<List<String>,Lookup> pathsLookups = new HashMap<List<String>,Lookup>();

    private Map<String,ClassInfoStorage.Info> classInfos = new HashMap<String,ClassInfoStorage.Info>();
    private Map<List<String>,Set<String>> pathsToClasses = new HashMap<List<String>,Set<String>>();
    
    public SwitchLookup(MimePath mimePath) {
        super();
        
        this.mimePath = mimePath;
        
        this.listener = new MappingListener();
        ClassInfoStorage.getInstance().addPropertyChangeListener(
            WeakListeners.propertyChange(listener, ClassInfoStorage.getInstance()));
    }

    public <T> Lookup.Result<T> lookup(Lookup.Template<T> template) {
        return findLookup(template.getType()).lookup(template);
    }

    public <T> T lookup(Class<T> clazz) {
        return findLookup(clazz).lookup(clazz);
    }

    private Lookup findLookup(Class clazz) {
        synchronized (LOCK) {
            String className = clazz.getName();
            UpdatableProxyLookup lookup = classLookups.get(className);
            if (lookup == null) {
                // Get the the class info and remember it
                ClassInfoStorage.Info classInfo = ClassInfoStorage.getInstance().getInfo(className);
                classInfos.put(className, classInfo);
                
                // Create lookup
                Lookup innerLookup = createLookup(classInfo);
                lookup = new UpdatableProxyLookup(innerLookup);
                
                classLookups.put(className, lookup);
            }

            return lookup;
        }
    }

    private Lookup createLookup(ClassInfoStorage.Info classInfo) {
        List<String> paths = computePaths(mimePath, ROOT_FOLDER, classInfo.getExtraPath());
        Lookup lookup;
        
        if (classInfo.getInstanceProviderClass() != null) {
            // Get a lookup for the new instance provider
            lookup = getLookupForProvider(paths, classInfo.getInstanceProvider());
        } else {
            // Add the className to the list of users of the new paths
            Set<String> pathsUsers = pathsToClasses.get(paths);
            if (pathsUsers == null) {
                pathsUsers = new HashSet<String>();
                pathsToClasses.put(paths, pathsUsers);
            }
            pathsUsers.add(classInfo.getClassName());

            // Get a lookup for the new paths
            lookup = getLookupForPaths(paths);
        }
        
        return lookup;
    }
    
    private Lookup getLookupForPaths(List<String> paths) {
        Lookup lookup = pathsLookups.get(paths);
        if (lookup == null) {
            lookup = new FolderPathLookup(paths.toArray(new String[paths.size()]));
            pathsLookups.put(paths, lookup);
        }
        
        return lookup;
    }

    private Lookup getLookupForProvider(List<String> paths, InstanceProvider instanceProvider) {
        return new InstanceProviderLookup(paths.toArray(new String[paths.size()]), instanceProvider);
    }
    
    private void rebuildLookup(String className) {
        synchronized (LOCK) {
            UpdatableProxyLookup classLookup = classLookups.get(className);
            if (classLookup == null) {
                // no lookup for the class, nothing to do
                return;
            }

            ClassInfoStorage.Info currentClassInfo = classInfos.get(className);
            ClassInfoStorage.Info classInfo = ClassInfoStorage.getInstance().getInfo(className);
            
            if (currentClassInfo.equals(classInfo)) {
                // bogus change event, the class information hasn't changed, nothing to do
                return;
            }

            if (currentClassInfo.getInstanceProviderClass() == null) {
                List<String> currentPaths = computePaths(mimePath, ROOT_FOLDER, currentClassInfo.getExtraPath());

                // Remove the className from the list of users of the current paths
                Set<String> currentPathsUsers = pathsToClasses.get(currentPaths);
                currentPathsUsers.remove(className);

                if (currentPathsUsers.isEmpty()) {
                    pathsToClasses.remove(currentPaths);
                    pathsLookups.remove(currentPaths);
                }
            }

            // Remember the new class info
            classInfos.put(className, classInfo);
    
            // Update the classLookup
            Lookup innerLookup = createLookup(classInfo);
            classLookup.setLookupsEx(innerLookup);
        }
    }
    
    private static List<String> computePaths(MimePath mimePath, String prefixPath, String suffixPath) {
        try {
            Method m = MimePath.class.getDeclaredMethod("getInheritedPaths", String.class, String.class); //NOI18N
            m.setAccessible(true);
            @SuppressWarnings("unchecked")
            List<String> paths = (List<String>) m.invoke(mimePath, prefixPath, suffixPath);
            return paths;
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Can't call org.netbeans.api.editor.mimelookup.MimePath.getInheritedPaths method.", e); //NOI18N
        }
        
        // No inherited mimepaths, provide at least something
        StringBuilder sb = new StringBuilder();
        if (prefixPath != null && prefixPath.length() > 0) {
            sb.append(prefixPath);
        }
        if (mimePath.size() > 0) {
            if (sb.length() > 0) {
                sb.append('/'); //NOI18N
            }
            sb.append(mimePath.getPath());
        }
        if (suffixPath != null && suffixPath.length() > 0) {
            if (sb.length() > 0) {
                sb.append('/'); //NOI18N
            }
            sb.append(suffixPath);
        }
        return Collections.singletonList(sb.toString());
    }
    
    /**
     * An ordinary <code>ProxyLookup</code> except that it exposes the
     * <code>setLookupEx</code> method.
     */
    private static final class UpdatableProxyLookup extends ProxyLookup {
        public UpdatableProxyLookup() {
            super();
        }
        
        public UpdatableProxyLookup(Lookup... lookups) {
            super(lookups);
        }
        
        public void setLookupsEx(Lookup... lookups) {
            setLookups(lookups);
        }
    } // End of UpdatableProxyLookup class
    
    private final class MappingListener implements PropertyChangeListener {
        public void propertyChange(PropertyChangeEvent evt) {
            for (String className : NbCollections.checkedSetByFilter((Set) evt.getNewValue(), String.class, true)) {
                rebuildLookup(className);
            }
        }
    } // End of MappingListsner class
    
}
