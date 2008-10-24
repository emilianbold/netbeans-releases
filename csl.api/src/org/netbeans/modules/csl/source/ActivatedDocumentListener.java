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
package org.netbeans.modules.csl.source;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.EditorRegistry;

import java.io.IOException;
import javax.swing.text.Document;
import org.netbeans.modules.csl.api.DataLoadersBridge;
import org.netbeans.modules.gsfpath.api.classpath.ClassPath;
import org.netbeans.napi.gsfret.source.Source;
import org.netbeans.modules.csl.source.usages.RepositoryUpdater;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 * This file is originally from Retouche, the Java Support 
 * infrastructure in NetBeans. I have modified the file as little
 * as possible to make merging Retouche fixes back as simple as
 * possible. 
 *
 *
 * @author Jan Lahoda
 */
public class ActivatedDocumentListener implements PropertyChangeListener {
    
    private static ActivatedDocumentListener INSTANCE;
    
    public static void register() {
        INSTANCE = new ActivatedDocumentListener();
    }
    
    private FileObject lastValidFile;
    
    /**
     * Creates a new instance of ActivatedDocumentListener
     */
    private ActivatedDocumentListener() {
        EditorRegistry.addPropertyChangeListener(this);
    }

    public synchronized void propertyChange(PropertyChangeEvent evt) {
        JTextComponent activeComponent = EditorRegistry.lastFocusedComponent();
        
        if (activeComponent == null) {
            return ;
        }
        
        Document active = activeComponent.getDocument();
                
        if (active == null) {
            return;
        }
        
        FileObject activeFile = DataLoadersBridge.getDefault().getFileObject(active);
        
        if(activeFile==null){
            //could be
            return;
        }
        
        if (lastValidFile == activeFile) {
            return;
        }
        
        if (lastValidFile != null && RepositoryUpdater.isRelevantSource(lastValidFile)) {
            if (!IGNORE_COMPILE_REQUESTS) {
                ClassPath cp = ClassPath.getClassPath(lastValidFile, ClassPath.SOURCE);
                if (cp != null) {
                    FileObject owner = cp.findOwnerRoot(lastValidFile);
                    if (owner != null) {
                        try {
                            if ("file".equals(lastValidFile.getURL().getProtocol())) {  //NOI18N
                                RepositoryUpdater.getDefault().scheduleCompilation(lastValidFile, owner);
                            }
                        } catch (IOException ioe) {
                            Exceptions.printStackTrace(ioe);
                        }
                    }
                }
            }
            
            lastValidFile = null;
        }
        
        Source activeJS = Source.forFileObject(activeFile);
        
        if (activeJS == null) {
            //not a GSF document:
            return;
        }
        
        lastValidFile = activeFile;
        
        //TimesCollector.getDefault().select(activeFile);
        SourceAccessor.getINSTANCE().revalidate(activeJS);
    }

    /**Set to switch off compilation, useful in tests.
     * Use SourceUtilsTestUtil.ignoreCompileRequests to set it:
     */
    public static boolean IGNORE_COMPILE_REQUESTS = false;
}
