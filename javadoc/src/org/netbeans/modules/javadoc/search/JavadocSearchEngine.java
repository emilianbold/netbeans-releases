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
