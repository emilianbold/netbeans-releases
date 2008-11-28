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

package org.netbeans.modules.csl.source.usages;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.modules.csl.source.util.LowMemoryEvent;
import org.netbeans.modules.csl.source.util.LowMemoryListener;


/**
 * This file is originally from Retouche, the Java Support 
 * infrastructure in NetBeans. I have modified the file as little
 * as possible to make merging Retouche fixes back as simple as
 * possible. 
 *
 *
 * @author Petr Hrebejk, Tomas Zezula
 */
public class BinaryAnalyser implements LowMemoryListener {

    public void lowMemory(LowMemoryEvent event) {
//        throw new UnsupportedOperationException("Not supported yet.");
    }
//    
//    static final String OBJECT = Object.class.getName();                        
//    
//    private final Index index;
//    private final Map<String,List<String>> refs = new HashMap<String,List<String>>();
//    private final Set<String> toDelete = new HashSet<String> ();
//    private final AtomicBoolean lowMemory;
//    private boolean cacheCleared;
//
//    public BinaryAnalyser (Index index) {
//       assert index != null;
//       this.index = index;
//       this.lowMemory = new AtomicBoolean (false);
//    }
//    
//        /** Analyses a classpath root.
//     * @param URL the classpath root, either a folder or an archive file.
//     *     
//     */
//    public final void analyse (final URL root, final ProgressHandle handle) throws IOException, IllegalArgumentException  {
////        assert root != null;        
////            ClassIndexManager.getDefault().writeLock(new ClassIndexManager.ExceptionAction<Void> () {
////                public Void run () throws IOException {
////                LowMemoryNotifier.getDefault().addLowMemoryListener (BinaryAnalyser.this);
////                try {
////                    if (root.isDirectory()) {        //NOI18N                    
////                        String path = root.getAbsolutePath ();
////                        if (path.charAt(path.length()-1) != File.separatorChar) {
////                            path = path + File.separatorChar;
////                        }                    
////                        cacheCleared = false;
////                        analyseFolder(root, path);                    
////                    }
////                    else {
////                        if (root.exists() && root.canRead()) {
////                            if (!isUpToDate(null,root.lastModified())) {
////                                index.clear();
////                                if (handle != null) { //Tests don't provide handle
////                                    handle.setDisplayName (NbBundle.getMessage(RepositoryUpdater.class,"MSG_Analyzing",root.getAbsolutePath()));
////                                }
////                                final ZipFile zipFile = new ZipFile(root);
////                                try {
////                                    analyseArchive( zipFile );
////                                }
////                                finally {
////                                    zipFile.close();
////                                }
////                            }
////                        }
////                    }
////                } finally {
////                    LowMemoryNotifier.getDefault().removeLowMemoryListener(BinaryAnalyser.this);
////                }
////                store();
////                return null;
////            }});
//    }        
//    
//        /** Analyses a folder 
//     *  @param folder to analyze
//     *  @param rootURL the url of root, it would be nicer to pass an URL type,
//     *  but the {@link URL#toExternalForm} from some strange reason does not cache result,
//     *  the String type is faster.
//     */
//    private void analyseFolder (final File  folder, final String rootPath) throws IOException {
////        if (folder.exists() && folder.canRead()) {
////            File[] children = folder.listFiles();  
////            for (File file : children) {
////                if (file.isDirectory()) {
////                    analyseFolder(file, rootPath);
////                }
////                else if (this.accepts(file.getName())) {
////                    String filePath = file.getAbsolutePath();
////                    long fileMTime = file.lastModified();
////                    int dotIndex = filePath.lastIndexOf('.');
////                    int slashIndex = filePath.lastIndexOf('/');
////                    int endPos;
////                    if (dotIndex>slashIndex) {
////                        endPos = dotIndex;
////                    }
////                    else {
////                        endPos = filePath.length();
////                    }
////                    String relativePath = FileObjects.convertFolder2Package (filePath.substring(rootPath.length(), endPos));
////                    if (!isUpToDate (relativePath, fileMTime)) {
////                        if (!cacheCleared) {
////                            this.index.clear();                            
////                            cacheCleared = true;
////                        }
////                        InputStream in = new BufferedInputStream (new FileInputStream (file));
////                        analyse (in);
////                        if (this.lowMemory.getAndSet(false)) {
////                            this.store();
////                        }
////                    }
////                }
////            }
////        }
//    }
//    
//    public void lowMemory (final LowMemoryEvent event) {
//        this.lowMemory.set(true);
//    }
//
//    // Implementation of StreamAnalyser ----------------------------------------           
//    
//    private boolean accepts(String name) {
//        int index = name.lastIndexOf('.');
//        if (index == -1 || (index+1) == name.length()) {
//            return false;
//        }
//        return "CLASS".equalsIgnoreCase(name.substring(index+1));  // NOI18N
//    }
//
//    //Cleans up usages of deleted class
//    private final void delete (final String className) throws IOException {
//        assert className != null;
//        if (!this.index.isValid(false)) {
//            return;
//        }
//        this.toDelete.add(className);
//    }
//    
//    private void analyse (InputStream inputStream ) throws IOException {
////        throw new RuntimeException("Not yet implemented");
//    }
//    
//    
//    private final boolean isUpToDate(String resourceName, long resourceMTime) throws IOException {
//        return this.index.isUpToDate(resourceName, resourceMTime);
//    }
}
