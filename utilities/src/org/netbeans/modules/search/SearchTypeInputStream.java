/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.search;

import java.io.IOException;
import java.io.InputStream;

/**
 * Object input stream with a customized class resolution.
 * 
 * @see  #resolveClass
 * @author  Marian Petras
 */
class SearchTypeInputStream extends java.io.ObjectInputStream {
    
    /**
     * holds the resolved <code>SearchType</code> class so that its classloader
     * may be used for resolution of other classes
     *
     * @see  #extClassLoader
     * @see  #resolveExtClass
     */
    private Class extSearchType = null;
    /**
     * classloader of the resolved <code>SearchType</code>
     *
     * @see  #extSearchType
     */
    private ClassLoader extClassLoader = null;

    /** Creates a new instance of SearchTypeInputStream */
    public SearchTypeInputStream(InputStream in) throws IOException {
        super(in);
    }
    
    /**
     * Loads the local class equivalent of the specified stream class
     * description. Uses the default
     * {@link ObjectInputStream ObjectInputStream}'s method first and
     * if it fails, tries to lookup the class among the registered
     * <code>SearchType</code>s or to use their classloaders.
     */
    protected Class resolveClass(java.io.ObjectStreamClass objectStreamClass)
            throws IOException, ClassNotFoundException {
        try {
            return super.resolveClass(objectStreamClass);
        } catch (ClassNotFoundException ex) {
            Class extClass = resolveExtClass(objectStreamClass.getName());
            if (extClass != null) {
                return extClass;
            } else {
                throw ex;
            }
        }
    }

    /**
     * Resolves the specified class.
     * If this method is used for the first time (on this instance), it just
     * tries to lookup the class among the registed <code>SearchType</code>s.
     * Next time this method is called, it uses the <code>SearchType</code>'s
     * classloader for loading classes.
     * 
     * @param  className  name of a class to be resolved
     * @return  local class matching the specified class name
     *          or <code>null</code> if no matching class was found
     */
    private Class resolveExtClass(final String className) {
        if (extSearchType == null) {

            /* first time class resolution must be for the ext. SearchType */
            extSearchType = Utils.searchTypeForName(className);
            return extSearchType;
        }

        /*
         * We expect that the classloader that was used for loading
         * the Search Type class is suitable also for other classes:
         */
        if (extClassLoader == null) {
            try {
                extClassLoader = extSearchType.getClassLoader();
            } catch (SecurityException ex) {
                return null;
            }
        }
        try {
            return extClassLoader.loadClass(className);
        } catch (ClassNotFoundException ex) {
            return null;
        }
    }
    
}
