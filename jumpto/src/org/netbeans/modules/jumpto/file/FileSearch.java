/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is the Accelerators module.
 * The Initial Developer of the Original Software is Andrei Badea.
 * Portions Copyright 2005-2006 Andrei Badea.
 * All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
    private static final RequestProcessor SWRP = new RequestProcessor("Jump To event collector", 1); // NOI18N   
    private RequestProcessor.Task searchTask;
    private final RequestProcessor.Task slidingTask = SWRP.create(new Runnable() {
        public void run() {
            panel.setModel(false, false);
        }
    });
    
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
        //System.out.println("New prefix " + currentPrefix);
        
        this.currentPrefix = currentPrefix;
        if ( currentPrefix != null ) {            
            if ( worker == null ) {
                //cancel( true );
                isSearchWorker = false; //Probably worng or useless
            }
            slidingTask.schedule(100);
        }
    }
    
    public synchronized boolean isNewSearchNeeded( String newPrefix ) {
       
        if ( cancel( false ) ) {
            //System.out.println("Canceled in shced in favor of " + newPrefix );
            return isSearchWorker;
        }
               
        return prefix == null || !newPrefix.startsWith(prefix.toLowerCase());   //Clearly wrong
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
            // System.out.println("MODEL " + (System.currentTimeMillis() - time) );
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
