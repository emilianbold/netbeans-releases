/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.versioning.util;

import org.netbeans.modules.versioning.spi.VCSContext;
import org.netbeans.modules.versioning.spi.FlatFolder;

import java.io.File;
import java.util.*;
import java.util.prefs.Preferences;

/**
 * Collection of Files that has special contracts for add, remove and contains methods, see below.
 * 
 * @author Maros Sandor
 */
public class FileCollection {
    
    private static final char FLAT_FOLDER_MARKER = '*';
    
    private final Set<File> storage = new HashSet<File>(1);
    
    public synchronized void load(Preferences prefs, String key) {
        List<String> paths = Utils.getStringList(prefs, key);
        storage.clear();        
        for (String path : paths) {
            if (path.charAt(0) == FLAT_FOLDER_MARKER) {
                storage.add(new FlatFolder(path.substring(1)));        
            } else {
                storage.add(new File(path));        
            }
        }
    }

    public synchronized void save(Preferences prefs, String key) {
        List<String> paths = new ArrayList<String>(storage.size());
        for (File file : storage) {
            if (VCSContext.isFlat(file)) {
                paths.add(FLAT_FOLDER_MARKER + file.getAbsolutePath());        
            } else {
                paths.add(file.getAbsolutePath());        
            }
        }
        Utils.put(prefs, key, paths);
    }

    /**
     * A file is contained in the collection either if it is in the colelction itself or there is any of its parents. 
     * 
     * @param file a file to query
     * @return true if the file is contained in the collection, false otherwise
     */
    public synchronized boolean contains(File file) {
        for (File element : storage) {
            if (Utils.isParentOrEqual(element, file)) return true;
        }
        return false;
    }

    /**
     * Adds a file to the collection. If any of its parent files is already in the collection, the file is NOT added.
     * All children of the supplied file are removed from the collection.
     * 
     * @param file a file to add
     */
    public synchronized void add(File file) {
        for (Iterator<File> i = storage.iterator(); i.hasNext(); ) {
            File element = i.next();
            if (Utils.isParentOrEqual(element, file)) return;
            if (Utils.isParentOrEqual(file, element)) {
                i.remove();
            }
        }
        storage.add(file);
    }

    /**
     * Removes a file from the collection. This method also removes all its parents and also all its children.
     * 
     * @param file a file to remove
     */
    public synchronized void remove(File file) {
        for (Iterator<File> i = storage.iterator(); i.hasNext(); ) {
            File element = i.next();
            if (Utils.isParentOrEqual(element, file) || Utils.isParentOrEqual(file, element)) {
                i.remove();
            }
        }
    }
}
