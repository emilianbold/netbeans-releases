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
 * JavadocSearchEngineImpl.java
 *
 * Created on 18. èerven 2001, 14:55
 */

package org.netbeans.modules.javadoc.search;

import java.util.ArrayList;

/**
 *
 * @author  Petr Suchomel
 * @version 0.1
 */
class JavadocSearchEngineImpl extends JavadocSearchEngine {
    
    private ArrayList tasks;

    private DocFileSystem[] docSystems;
    private IndexSearchThread.DocIndexItemConsumer diiConsumer;
    
    /** Used to search for set elements in javadoc repository
     * @param items to search for
     * @throws NoJavadocException if no javadoc directory is mounted, nothing can be searched
     */
    public void search(String[] items, final SearchEngineCallback callback) throws NoJavadocException {
        docSystems = DocFileSystem.getFolders();
        tasks = new ArrayList( docSystems.length );

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
                      
        if ( docSystems.length <= 0 ) {            
            callback.finished();
            throw new NoJavadocException();            
        }
        String toFind = items[0];
        
        for( int i = 0; i < docSystems.length; i++ ) {
            try {
                JavaDocFSSettings setting = new JavaDocFSSettings( docSystems[i].getIndexFile().getFileSystem() );
                //System.out.println(setting.getSearchTypeEngine().getName());
                IndexSearchThread searchThread = setting.getSearchTypeEngine().getSearchThread( toFind,  docSystems[i].getIndexFile() , diiConsumer );

                tasks.add( searchThread );
                searchThread.go();
            }
            catch(org.openide.filesystems.FileStateInvalidException fsEx){
                fsEx.printStackTrace();
            }
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
