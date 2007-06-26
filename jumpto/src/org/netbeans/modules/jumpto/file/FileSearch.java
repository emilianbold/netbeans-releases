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
 * The Original Software is the Accelerators module.
 * The Initial Developer of the Original Software is Andrei Badea.
 * Portions Copyright 2005-2006 Andrei Badea.
 * All Rights Reserved.
 *
 * Contributor(s): Andrei Badea
 *               : Petr Hrebejk
 */

package org.netbeans.modules.jumpto.file;

import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.DefaultListModel;
import javax.swing.ListModel;
import org.openide.util.RequestProcessor;

/** Class which can start and stop the search and hold useful info about the
 *  current search. It is here not to contain all the logic in the form.
 * 
 * @author Andrei Badea, Petr Hrebejk
 */
public class FileSearch {
    
    private static final ListModel EMPTY_MODEL = new DefaultListModel();
   
    
    private FileSearchPanel panel;
    private SearchWorker worker;
    
    private String prefix;
    private String currentPrefix;
    
    private List<FileDescription> files;
    
    private static final RequestProcessor RP = new RequestProcessor("Jump To File Request Processor", 1); // NOI18N   
    private RequestProcessor.Task searchTask;
    
    private boolean isSearchWorker;
    
    public FileSearch( FileSearchPanel panel ) {
        this.panel = panel;        
    }
    
    public synchronized void search(String prefix, boolean caseSensitive) {
        
        cancel( true );
        
        worker = new SearchWorker( panel, prefix );        
        searchTask = RP.post(worker, 300);
        isSearchWorker = true;
        //System.out.println("Worker scheduled " + prefix );
        this.prefix = prefix;
                  
    }
    
    public synchronized void workerFinished() {
        worker = null;
        searchTask = null;
    }
    
    boolean cancel( boolean hard ) {
        
        if ( searchTask != null && searchTask.cancel() ) {
            searchTask = null;
            if ( worker != null ) {
                worker.cancel();
            }
            worker = null;
            return true;
        }
        
        if ( hard && worker != null ) {
            worker.cancel();
            worker = null;
            searchTask = null;        
        }
        
        return false;       
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public synchronized void setCurrentPrefix(String currentPrefix) {
        // System.out.println("New prefix " + currentPrefix);
        this.currentPrefix = currentPrefix;
        if ( currentPrefix != null ) {
            cancel( true );
            isSearchWorker = false;
            searchTask = RP.post(new Runnable() {
                public void run() {
                    panel.setModel(false, false);
                }
            }, 250);
        }
    }
    
    public synchronized boolean isNewSearchNeeded( String newPrefix ) {
       
        if ( cancel( false ) ) {
            //System.out.println("Canceled in shced in favor of " + newPrefix );
            return isSearchWorker;
        }
               
        return prefix == null || !newPrefix.startsWith(prefix.toLowerCase());
    }
    
    public void newSearchResults( List<FileDescription> files ) {
        this.files = files;
        
        if ( files == null ) {
            cancel( true );
            prefix = currentPrefix = null;
        }
    }
        
    /** Creates model for current settings.
     */
    public ListModel createModel( boolean caseSensitive, boolean preferedProject, boolean showHiddenFiles, boolean stillSearching ) {
            
        // long time = System.currentTimeMillis();
        
        if ( files == null || files.isEmpty() ) {
            //System.out.println("MODEL " + (System.currentTimeMillis() - time) );
            return EMPTY_MODEL; // Nothing to show
        }
        
        final List<FileDescription> tmp;
                
        if ( caseSensitive || currentPrefix != null ) { 
            tmp = new ArrayList<FileDescription>( files.size() );
            // Filter out items
            FilenameFilter filter = new RegexpFileFilter( currentPrefix == null ? prefix : currentPrefix, caseSensitive );
            for (FileDescription fileDescription : files) {
                if (filter.accept(null, fileDescription.getName())) {
                    tmp.add(fileDescription);
                }
            }            
        }
        else {
            // Just create new list;
            tmp = new ArrayList<FileDescription>( files );
        }
        
        if ( tmp != null ) {
            Collections.sort(tmp, new FileDescription.FDComarator( preferedProject, caseSensitive ) );
        }
        
        ListModel m = new ListListModel<FileDescription>( tmp, stillSearching ? FileDescription.SEARCH_IN_PROGRES : null );
        
        if ( !showHiddenFiles ) {
            m = LazyListModel.create(m, panel, 0.1, "Not computed yet.");
        }
        
        // System.out.println("MODEL " + (System.currentTimeMillis() - time) );
            
        
        return m.getSize() == 0 ? EMPTY_MODEL : m;
    }   
        
}
