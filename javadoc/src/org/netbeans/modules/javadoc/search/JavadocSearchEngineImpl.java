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
 * JavadocSearchEngineImpl.java
 *
 * Created on 18. ?erven 2001, 14:55
 */

package org.netbeans.modules.javadoc.search;

import java.util.ArrayList;
import org.openide.ErrorManager;

import org.openide.filesystems.FileObject;

/**
 *
 * @author  Petr Suchomel
 * @version 0.1
 */
class JavadocSearchEngineImpl extends JavadocSearchEngine {

    private ArrayList tasks;

    private IndexSearchThread.DocIndexItemConsumer diiConsumer;

    /** Used to search for set elements in javadoc repository
     * @param items to search for
     * @throws NoJavadocException if no javadoc directory is mounted, nothing can be searched
     */
    public void search(String[] items, final SearchEngineCallback callback) throws NoJavadocException {
        FileObject docRoots[] = JavadocRegistry.getDefault().getDocRoots();
        tasks = new ArrayList( docRoots.length );

        diiConsumer = new IndexSearchThread.DocIndexItemConsumer() {
                          public void addDocIndexItem( final DocIndexItem dii ) {
                              callback.addItem(dii);
                          }

                          public void indexSearchThreadFinished( IndexSearchThread t ) {
                              tasks.remove( t );
                              if ( tasks.isEmpty() )
                                  callback.finished();
                          }
                      };
                      
        if ( docRoots.length <= 0 ) {            
            callback.finished();
            throw new NoJavadocException();            
        }
        String toFind = items[0];
        
        for( int i = 0; i < docRoots.length; i++ ) {
            
            JavadocSearchType st = JavadocRegistry.getDefault().findSearchType( docRoots[i] );
            if (st == null) {
                ErrorManager.getDefault().log ("NO Search type for " + docRoots[i]);
                continue;
            }
            FileObject indexFo = st.getDocFileObject( docRoots[i] );
            if (indexFo == null) {
                ErrorManager.getDefault().log ("NO Index files fot " + docRoots[i] );
                continue;
            }            
            
            IndexSearchThread searchThread = st.getSearchThread( toFind, indexFo, diiConsumer );

            tasks.add( searchThread );
            searchThread.go();            
        }
        //callback.finished();
    }
    
    /** Stops execution of Javadoc search thread
     */
    public void stop() {
        for( int i = 0; i < tasks.size(); i++ ) {
            SearchThreadJdk12 searchThread = (SearchThreadJdk12)tasks.get( i );
            searchThread.finish();
        }
    }    
}
