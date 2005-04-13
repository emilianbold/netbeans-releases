/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.projectimport.eclipse;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Collection;
import java.util.logging.Logger;
import org.netbeans.modules.projectimport.LoggerFactory;

/**
 * Represents classpath for an Eclipse project (.classpath file content)
 *
 * @author mkrauskopf
 */
final class ClassPath {
    
    /**
     * Logger for this class
     */
    private static final Logger logger =
            LoggerFactory.getDefault().createLogger(ClassPath.class);
    
    
    /** Represents link in Eclipse project's classpath. */
    static class Link {
        public static final int TYPE_INVALID = 0;
        public static final int TYPE_FILE = 1;
        public static final int TYPE_FOLDER = 2;
        
        private String name;
        private int type = TYPE_INVALID;
        private String location;
        
        String getName() {
            return name;
        }
        
        void setName(String name) {
            this.name = name;
        }
        
        int getType() {
            return type;
        }
        
        void setType(int type) {
            this.type = type;
        }
        
        String getLocation() {
            return location;
        }
        
        void setLocation(String location) {
            this.location = location;
        }
        
        public String toString() {
            return name + " = " + location + " (type: " + type + ")";
        }
        
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof Link)) return false;
            final Link link = (Link) obj;
            if (type != link.type) return false;
            if (name != null ? !name.equals(link.name) : link.name != null)
                return false;
            if (location != null ? !location.equals(link.location) : link.location != null)
                return false;
            return true;
        }
        
        public int hashCode() {
            int result = 17;
            result = 37 * result + type;
            result = 37 * result + System.identityHashCode(name);
            result = 37 * result + System.identityHashCode(location);
            return result;
        }
    }
    
    private static final String USER_LIBRARY_PREFIX
            = "org.eclipse.jdt.USER_LIBRARY/"; // NOI18N
    private static final int USER_LIBRARY_PREFIX_LENGTH = USER_LIBRARY_PREFIX.length();
    
    private ClassPathEntry output;
    private Collection pathEntries;
    
    private String jreContainer;
    private Collection sourceRoots;
    private Collection externalSourceRoots;
    private Collection libraries;
    private Collection externalLibraries;
    private Collection projects;
    private Collection variables;
    private Collection userLibraries;
    
    /**
     * Adds a given entry to entries list. If entry is output output member is
     * set.
     */
    void addEntry(ClassPathEntry entry) {
        if (entry != null){
            if (entry.getType() == ClassPathEntry.TYPE_OUTPUT) {
                output = entry;
            } else {
                addSource(entry);
            }
        }
    }
    
    private void addSource(ClassPathEntry path) {
        if (pathEntries == null) {
            pathEntries = new ArrayList();
        }
        pathEntries.add(path);
    }
    
    ClassPathEntry getOutput() {
        return output;
    }
    
    Collection getEntries() {
        return pathEntries;
    }
    
    private Collection getEntriesByType(ClassPathEntry.Type type) {
        Collection entries = new ArrayList();
        for (Iterator it = pathEntries.iterator(); it.hasNext(); ) {
            ClassPathEntry entry = (ClassPathEntry) it.next();
            if (entry.getType() == type) {
                entries.add(entry);
            }
        }
        return entries;
    }
    
    /**
     * Just provides more convenient access to source entries.
     *
     * @see #getEntries()
     */
    Collection getSourceRoots() {
        // lazy initialization
        if (sourceRoots == null && pathEntries != null) {
            sourceRoots = getEntriesByType(ClassPathEntry.TYPE_SOURCE);
        }
        return sourceRoots;
    }
    
    /**
     * Returns container classpath entry for JRE.
     *
     * @see #getEntries()
     */
    String getJREContainer() {
        // lazy initialization
        if (jreContainer == null && pathEntries != null) {
            Collection col = getEntriesByType(ClassPathEntry.TYPE_CONTAINER);
            for (Iterator it = col.iterator(); it.hasNext(); ) {
                ClassPathEntry cpe = (ClassPathEntry) it.next();
                if (cpe.getRawPath().startsWith(Workspace.DEFAULT_JRE_CONTAINER)) {
                    jreContainer = cpe.getRawPath();
                    logger.finest("jreContainer found: " + jreContainer); // NOI18N
                    break;
                }
            }
            if (jreContainer == null) {
                logger.fine("jreContainer wasn't found in classpath entries!"); // NOI18N
                logger.fine("Classpath entries" + this.getEntries()); // NOI18N
            }
        }
        return jreContainer;
    }
    
    /**
     * Just provides more convenient access to external source entries.
     *
     * @see #getEntries()
     */
    Collection getExternalSourceRoots() {
        // lazy initialization
        if (externalSourceRoots == null && pathEntries != null) {
            externalSourceRoots = getEntriesByType(ClassPathEntry.TYPE_LINK);
        }
        return externalSourceRoots;
    }
    
    /**
     * Just provides more convenient access to library entries.
     *
     * @see #getEntries()
     */
    Collection getLibraries() {
        // lazy initialization
        if (libraries == null && pathEntries != null) {
            libraries = getEntriesByType(ClassPathEntry.TYPE_LIBRARY);
        }
        return libraries;
    }
    
    /**
     * Just provides more convenient access to external library entries.
     *
     * @see #getEntries()
     */
    Collection getExternalLibraries() {
        // lazy initialization
        if (externalLibraries == null && pathEntries != null) {
            externalLibraries = getEntriesByType(ClassPathEntry.TYPE_EXTERNAL_LIBRARY);
        }
        return externalLibraries;
    }
    
    /**
     * Returns collection of names of user defined libraries.
     */
    Collection getUserLibraries() {
        // lazy initialization
        if (userLibraries == null && pathEntries != null) {
            Collection col = getEntriesByType(ClassPathEntry.TYPE_CONTAINER);
            userLibraries = new HashSet();
            for (Iterator it = col.iterator(); it.hasNext(); ) {
                ClassPathEntry cpe = (ClassPathEntry) it.next();
                String rawPath = cpe.getRawPath();
                if (rawPath.startsWith(USER_LIBRARY_PREFIX)) {
                    userLibraries.add(rawPath.substring(USER_LIBRARY_PREFIX_LENGTH));
                }
            }
        }
        return userLibraries;
    }
    
    /**
     * Just provides more convenient access to project entries.
     *
     * @see #getEntries()
     */
    Collection getProjects() {
        // lazy initialization
        if (projects == null && pathEntries != null) {
            projects = getEntriesByType(ClassPathEntry.TYPE_PROJECT);
        }
        return projects;
    }
    
    /**
     * Just provides more convenient access to project entries.
     *
     * @see #getEntries()
     */
    Collection getVariables() {
        // lazy initialization
        if (variables == null && pathEntries != null) {
            variables = getEntriesByType(ClassPathEntry.TYPE_VARIABLE);
        }
        return variables;
    }
}
