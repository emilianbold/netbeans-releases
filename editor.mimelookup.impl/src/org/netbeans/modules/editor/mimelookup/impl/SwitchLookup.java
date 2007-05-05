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

package org.netbeans.modules.editor.mimelookup.impl;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
    
    // XXX: This is currently called from editor/settings/storage (SettingsProvider)
    // via reflection. We will eventually make it friend API. In the meantime just
    // make sure that any changes here still work for e/s/s module.

    /* package */ static List<String> computePaths(MimePath mimePath, String prefixPath, String suffixPath) {
        List<String[]> arrays = new ArrayList<String[]>(mimePath.size());
        String innerMimeType = null;

        if (mimePath.size() > 1) {
            innerMimeType = mimePath.getMimeType(mimePath.size() - 1);
        }
        
        for (int i = mimePath.size(); i >= 0 ; i--) {
            MimePath currentPath = mimePath.getPrefix(i);

            // Skip the top level mime type if it's the same as the inner mime type
            // to avoid duplicities.
            if (currentPath.size() != 1 || innerMimeType == null ||
                !currentPath.getMimeType(0).equals(innerMimeType)
            ) {
                // Add the current mime path
                arrays.add(split(currentPath));
            }

            // For compound mime types fork the existing paths and add their
            // variant for the generic part of the mime type as well.
            // E.g. text/x-ant+xml adds both text/x-ant+xml and text/xml
            if (currentPath.size() > 0) {
                String mimeType = currentPath.getMimeType(currentPath.size() - 1);
                String genericMimeType = getGenericPartOfCompoundMimeType(mimeType);

                if (genericMimeType != null) {
                    List<String[]> genericPaths = forkPaths(arrays, genericMimeType, i - 1);
                    arrays.addAll(genericPaths);
                }
            }
        }

        // Add the inner type on a prominent position
        if (innerMimeType != null) {
            arrays.add(1, new String [] { innerMimeType });
            
            String genericInnerMimeType = getGenericPartOfCompoundMimeType(innerMimeType);
            if (genericInnerMimeType != null) {
                arrays.add(2, new String [] { genericInnerMimeType });
            }
        }
        
        List<String> paths = new ArrayList<String>(arrays.size());

        for (String[] path : arrays) {
            StringBuffer sb = new StringBuffer(10 * path.length + 20);

            if (prefixPath != null && prefixPath.length() > 0) {
                sb.append(prefixPath);
            }
            for (int ii = 0; ii < path.length; ii++) {
                if (path[ii].length() > 0) {
                    if (sb.length() > 0) {
                        sb.append('/'); //NOI18N
                    }
                    sb.append(path[ii]);
                }
            }
            if (suffixPath != null && suffixPath.length() > 0) {
                if (sb.length() > 0) {
                    sb.append('/'); //NOI18N
                }
                sb.append(suffixPath);
            }

            paths.add(sb.toString());
        }

        return paths;
    }

    // See http://tools.ietf.org/html/rfc4288#section-4.2 for the structure of
    // mime type strings.
    // package private just for tests
    /* package */ static String getGenericPartOfCompoundMimeType(String mimeType) {
        int plusIdx = mimeType.lastIndexOf('+'); //NOI18N
        if (plusIdx != -1 && plusIdx < mimeType.length() - 1) {
            int slashIdx = mimeType.indexOf('/'); //NOI18N
            String prefix = mimeType.substring(0, slashIdx + 1);
            String suffix = mimeType.substring(plusIdx + 1);

            // fix for #61245
            if (suffix.equals("xml")) { //NOI18N
                prefix = "text/"; //NOI18N
            }

            return prefix + suffix;
        } else {
            return null;
        }
    }

    private static String [] split(MimePath mimePath) {
        String [] array = new String[mimePath.size()];
        
        for (int i = 0; i < mimePath.size(); i++) {
            array[i] = mimePath.getMimeType(i);
        }
        
        return array;
    }
    
    // Remember the paths list contains string arrays such as { 'text/x-jsp', 'text/x-ant+xml', 'text/x-java' },
    // the elementIdx points to the 'text/x-ant+xml' part and genericMimeType is 'text/xml'.
    private static List<String[]> forkPaths(List<String[]> paths, String genericMimeType, int elementIdx) {
        List<String[]> forkedPaths = new ArrayList<String[]>(paths.size());
        
        for (String[] path : paths) {
            String [] forkedPath = new String [path.length];
            
            for (int ii = 0; ii < path.length; ii++) {
                if (ii != elementIdx) {
                    forkedPath[ii] = path[ii];
                } else {
                    forkedPath[ii] = genericMimeType;
                }
            }
            
            forkedPaths.add(forkedPath);
        }
        
        return forkedPaths;
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
