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
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.spi.editor.mimelookup.InstanceProvider;
import org.openide.util.Lookup;
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
    
    private HashMap classLookups = new HashMap();
    private HashMap pathsLookups = new HashMap();

    private HashMap classInfos = new HashMap();
    private HashMap pathsToClasses = new HashMap();
    
    /** Creates a new instance of SwitchLookup */
    public SwitchLookup(MimePath mimePath) {
        super();
        
        this.mimePath = mimePath;
        
        this.listener = new MappingListener();
        ClassInfoStorage.getInstance().addPropertyChangeListener(
            WeakListeners.propertyChange(listener, ClassInfoStorage.getInstance()));
    }

    public Lookup.Result lookup(Lookup.Template template) {
        return findLookup(template.getType()).lookup(template);
    }

    public Object lookup(Class clazz) {
        return findLookup(clazz).lookup(clazz);
    }

    private Lookup findLookup(Class clazz) {
        synchronized (LOCK) {
            String className = clazz.getName();
            Lookup lookup = (Lookup) classLookups.get(className);
            if (lookup == null) {
                // Get the the class info and remember it
                ClassInfoStorage.Info classInfo = ClassInfoStorage.getInstance().getInfo(className);
                classInfos.put(className, classInfo);
                
                // Create lookup
                Lookup innerLookup = createLookup(classInfo);
                lookup = new UpdatableProxyLookup(new Lookup [] { innerLookup });
                
                classLookups.put(className, lookup);
            }

            return lookup;
        }
    }

    private Lookup createLookup(ClassInfoStorage.Info classInfo) {
        List paths = computePaths(mimePath, ROOT_FOLDER, classInfo.getExtraPath());
        Lookup lookup;
        
        if (classInfo.getInstanceProviderClass() != null) {
            // Get a lookup for the new instance provider
            lookup = getLookupForProvider(classInfo.getClassName(), paths, classInfo.getInstanceProvider());
        } else {
            // Add the className to the list of users of the new paths
            Set pathsUsers = (Set) pathsToClasses.get(paths);
            if (pathsUsers == null) {
                pathsUsers = new HashSet();
                pathsToClasses.put(paths, pathsUsers);
            }
            pathsUsers.add(classInfo.getClassName());

            // Get a lookup for the new paths
            lookup = getLookupForPaths(paths);
        }
        
        return lookup;
    }
    
    private Lookup getLookupForPaths(List paths) {
        Lookup lookup = (Lookup) pathsLookups.get(paths);
        if (lookup == null) {
            lookup = new FolderPathLookup((String []) paths.toArray(new String[paths.size()]));
            pathsLookups.put(paths, lookup);
        }
        
        return lookup;
    }

    private Lookup getLookupForProvider(String className, List paths, InstanceProvider instanceProvider) {
        return new InstanceProviderLookup((String [])paths.toArray(new String[paths.size()]), instanceProvider);
    }
    
    private void rebuildLookup(String className) {
        synchronized (LOCK) {
            UpdatableProxyLookup classLookup = (UpdatableProxyLookup) classLookups.get(className);
            if (classLookup == null) {
                // no lookup for the class, nothing to do
                return;
            }

            ClassInfoStorage.Info currentClassInfo = (ClassInfoStorage.Info) classInfos.get(className);
            ClassInfoStorage.Info classInfo = ClassInfoStorage.getInstance().getInfo(className);
            
            if (currentClassInfo.equals(classInfo)) {
                // bogus change event, the class information hasn't changed, nothing to do
                return;
            }

            if (currentClassInfo.getInstanceProviderClass() == null) {
                List currentPaths = computePaths(mimePath, ROOT_FOLDER, currentClassInfo.getExtraPath());

                // Remove the className from the list of users of the current paths
                Set currentPathsUsers = (Set) pathsToClasses.get(currentPaths);
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
            classLookup.setLookupsEx(new Lookup [] { innerLookup });
        }
    }
    
    // XXX: This is currently called from editor/settings/storage (SettingsProvider)
    // via reflection. We will eventually make it friend API. In the meantime just
    // make sure that any changes here still work for e/s/s module.

    /* package */ static List computePaths(MimePath mimePath, String prefixPath, String suffixPath) {
        ArrayList arrays = new ArrayList(mimePath.size());
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
                    List genericPaths = forkPaths(arrays, genericMimeType, i - 1);
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
        
        ArrayList paths = new ArrayList(arrays.size());

        for (Iterator i = arrays.iterator(); i.hasNext(); ) {
            String [] path = (String []) i.next();
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

    private static String getGenericPartOfCompoundMimeType(String mimeType) {
        int plusIdx = mimeType.indexOf('+'); //NOI18N
        if (plusIdx != -1) {
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
    private static List forkPaths(List paths, String genericMimeType, int elementIdx) {
        ArrayList forkedPaths = new ArrayList(paths.size());
        
        for (Iterator i = paths.iterator(); i.hasNext(); ) {
            String [] path = (String []) i.next();
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
        
        public UpdatableProxyLookup(Lookup [] lookups) {
            super(lookups);
        }
        
        public void setLookupsEx(Lookup [] lookups) {
            setLookups(lookups);
        }
    } // End of UpdatableProxyLookup class
    
    private final class MappingListener implements PropertyChangeListener {
        public void propertyChange(PropertyChangeEvent evt) {
            Set classNames = (Set)evt.getNewValue();
            
            for (Iterator i = classNames.iterator(); i.hasNext(); ) {
                String className = (String) i.next();
                rebuildLookup(className);
            }
        }
    } // End of MappingListsner class
    
}
