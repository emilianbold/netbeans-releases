/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/*
 * JavadocSearchEngine.java
 *
 * Created on 18. èerven 2001, 14:50
 */

package org.netbeans.modules.javadoc.search;

/**
 *
 * @author Petr Suchomel
 * @version 0.1
 */
public abstract class JavadocSearchEngine extends java.lang.Object {
    
    /** Used to search for set elements in javadoc repository
     * @param callback Callback giving new items and finished event
     * @param items to search for
     * @throws NoJavadocException if no javadoc directory is mounted, nothing can be searched
     */    
    public abstract void search(String[] items, SearchEngineCallback callback) throws NoJavadocException;
    
    /** Stops execution of Javadoc search thread
     */    
    public abstract void stop();
    
    /** Gets default engine
     * @return default Javadoc search engine
     */    
    public static JavadocSearchEngine getDefault(){
        return new JavadocSearchEngineImpl();
    }    
    
    /** Call back interface for Javadoc search engine
     */    
    public static interface SearchEngineCallback {
        /**
         * Called if search process finished
         */
        public void finished();
        
        /** Called if javadoc item found
         * @param item  DocIndexItem with found data
         */        
        public void addItem(DocIndexItem item);
    }
    
}
