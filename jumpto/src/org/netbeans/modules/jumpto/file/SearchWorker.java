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
 *                 Petr Hrebejk
 */

package org.netbeans.modules.jumpto.file;

import java.io.File;
import java.io.FileFilter;
import java.util.LinkedList;
import java.util.List;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * This class is not thread-safe. All methods should be called from the
 * same thread.
 *
 *  XXX Maybe we should filter by VisibilityQuery but would probably slow down the thing a lot
 * 
 * @author Andrei Badea, Petr Hrebejk
 */
class SearchWorker implements Runnable {
    
    private volatile boolean isCanceled;
    
    
    private FileSearchPanel panel;
    private String prefix;
    private FileFilter fileFilter;
        
    
   
    public SearchWorker( FileSearchPanel panel, String prefix ) {
        this.panel = panel;
        this.prefix = prefix;
        this.fileFilter = new RegexpFileFilter( prefix, false ); // Always search case insensitive here      
    }
        
   
    public synchronized void cancel() {        
        // System.out.println("Worker CANCELED " + prefix);
        isCanceled = true;        
    }

    private long filesScanned;
        
    public void run() {        
        // System.out.println(" WORKER Started " + prefix);
        
        Project pp = panel.getPreferedProject();
        
        long time = System.currentTimeMillis();
        
        List<FileDescription> preferedResult = null;
        if ( pp != null ) {
            // Search the prefered project first
            preferedResult = doSearch( pp, false );
            if ( !isCanceled ) {
                panel.getSearch().setPrefix( prefix );
                panel.getSearch().newSearchResults(preferedResult);
                panel.setModel( false, true );
               
            }
        }
        
        List<FileDescription> result = doSearch( pp, true );
        
        if ( !isCanceled ) {
            if ( preferedResult != null && !preferedResult.isEmpty() ) {
                preferedResult.addAll(result);
            }
            else {
                preferedResult = result;
            }
            panel.getSearch().setPrefix( prefix );
            panel.getSearch().newSearchResults(preferedResult);
            panel.setModel( true, false );
            
        }
        panel.getSearch().workerFinished();
        // System.out.println("SEARCH TIME : " + ( System.currentTimeMillis() - time ) + " FILES SCANNED " + filesScanned);
    }
    
    private List<FileDescription> doSearch( Project p, boolean ignore ) {
        
        filesScanned = 0;
        
        Project[] projects = p == null || ignore ? panel.getProjects() : new Project[]{p};
        
        List<FileDescription> descriptions = new LinkedList<FileDescription>();
        
        for ( Project project : projects ) {
            
            if (isCanceled) { return null; }
            
            if ( p != null && ignore && ( project == p ||  project.equals(p) ) ) {
                continue;
            }
            
            SourceGroup[] groups = ProjectUtils.getSources(project).getSourceGroups(Sources.TYPE_GENERIC);        
            
            if (isCanceled) { return null; }
            
            
            for( SourceGroup group : groups ) {
                FileObject groupRoot = group.getRootFolder();
            
                if (isCanceled) { return null; }
            
                File rootFolder = FileUtil.toFile(groupRoot);                                
                if ( rootFolder == null || !rootFolder.isDirectory() ) {
                    continue; // Strange source group
                }

                findFiles( project, group, rootFolder, descriptions, !ignore, 0 );             
            }
        }
        
        return descriptions;               
    }
    
    private void findFiles( Project project, 
                            SourceGroup group, 
                            File folder, 
                            List<FileDescription> target, 
                            boolean preferred,
                            int level ) {

        if (isCanceled) { return; }
                    
        File files[] = folder.listFiles();
        
        for( File file : files ) {
            
            if (isCanceled) { return; }
            
            if (level <= 1) {
                FileObject fo = FileUtil.toFileObject(file);
                if (fo != null && !group.contains(fo)) {
                    continue;
                }
            }

            if ( file.isDirectory() ) {
                findFiles( project, group, file, target, preferred, level + 1 );
            }
            else if ( fileFilter.accept(file)) {
                target.add( new FileDescription(file, project, group, preferred )); 
            }
        }
        
        filesScanned += files.length;
        
    }
    
}
