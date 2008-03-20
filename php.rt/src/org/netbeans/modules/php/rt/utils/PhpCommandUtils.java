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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.php.rt.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.openide.LifecycleManager;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Node;
import org.openide.windows.TopComponent;

/**
 *
 * @author avk
 */
public class PhpCommandUtils {

        // ASYNCHRONICITY adapted from org.openide.util.actions.CallableSystemAction
    private static final boolean DEFAULT_ASYNCH = !Boolean.getBoolean(
           "org.openide.util.actions.CallableSystemAction.synchronousByDefault"
    ); // NOI18N

    public static boolean defaultAsynchronous(){
        return DEFAULT_ASYNCH;
    }
    
    public static Node[] getActionNodes(){
         return TopComponent.getRegistry().getCurrentNodes();
    }
    
    public static boolean isInvokedForProject(){
         Node[] nodes = getActionNodes();
        if (nodes == null) {
            return false;
        }
             
        for (Node node : nodes){
            if (isProjectNode(node)){
                return true;
            }
        }
        return false;
    }
    
    public static FileObject[] getActionFiles(){
         Node[] nodes = getActionNodes();
         if ( nodes == null ){
             return new FileObject[0];
         }
         List<FileObject> list = new ArrayList<FileObject>( nodes.length );
         for (Node node : nodes) {
             FileObject fileObject = node.getLookup().lookup(FileObject.class);
             
             if ( fileObject == null ) {
                 fileObject = getFileObjectByDataObject(node);
             }
             
             if ( fileObject == null ) {
                 fileObject = getFileObjectForProject(node);
             }
             
             if ( fileObject != null ) {
                 list.add( fileObject );
             }
         }
         return list.toArray( new FileObject[ list.size()] );
     }
    
    /**
     * returns true if at one of selected nodes is src root node.
     */
    public static boolean isInvokedForSrcRoot(){
        Map<Project, FileObject[]> projectToSrc 
                = new HashMap<Project, FileObject[]>();
        
        FileObject[] files = getActionFiles();
        for (FileObject fileObject : files) {
            Project ownerProject = FileOwnerQuery.getOwner(fileObject);
            if (ownerProject != null) {
                // alresdy retrieved sources arecached
                FileObject[] sources = getAndCacheSources(projectToSrc, ownerProject);
                for (FileObject source : sources) {
                    if (source.equals(fileObject)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
    public static void saveAll(){
        // suggested by nb team as general approach
        // it seems that this will save files even in other projects.
        // It's not correct but temporarily use it as a general behaviour.
        LifecycleManager.getDefault().saveAll ();
    }
    
    /*
    public static void saveProject(Project project) throws IOException{
            ProjectManager.getDefault().saveProject(project);
    }
     */
    
    /**
     * saves specified fileObject
     */
    public static void saveFile(FileObject fileObject) throws IOException {
        try {
            DataObject dataObject = DataObject.find(fileObject);
            if (dataObject != null) {
                SaveCookie saveCookie = dataObject.getCookie(SaveCookie.class);
                if (saveCookie != null) {
                    saveCookie.save();
                }
            }
        } catch (DataObjectNotFoundException e) {
            // ignore saving for this file
        }
    }

    private static FileObject[] getAndCacheSources(
            Map<Project, FileObject[]> projectToSrcCache, Project project)
    {
        if (!projectToSrcCache.containsKey(project)) {
            FileObject[] sources = PhpProjectUtils.getSourceObjects(project);
            projectToSrcCache.put(project, sources);
        }
        return projectToSrcCache.get(project);
    }

    private static FileObject getFileObjectForProject( Node node ){
         // project node case
         Project project = node.getLookup().lookup( Project.class );
         if ( project != null ) {
             return project.getProjectDirectory();
         }
         return null;
     }

     private static FileObject getFileObjectByDataObject( Node node)
     {
         DataObject dobj = node.getLookup().lookup(DataObject.class);

         if (dobj != null) {
             return dobj.getPrimaryFile();
         }
         return null;
     }
    
    private static boolean isProjectNode(Node node) {
        Project project = node.getLookup().lookup(Project.class);
        return project != null;
    }
}
