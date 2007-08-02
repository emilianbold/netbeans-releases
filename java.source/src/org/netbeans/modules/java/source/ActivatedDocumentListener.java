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
package org.netbeans.modules.java.source;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.modules.java.JavaDataLoader;
import org.netbeans.modules.java.source.usages.RepositoryUpdater;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;

/**
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
                
        if (active == null)
            return ;
        
        Object sourceProperty = active.getProperty(Document.StreamDescriptionProperty);
        
        if (!(sourceProperty instanceof DataObject))
            return ;
        
        DataObject source = (DataObject) sourceProperty;
        
        if (source == null)
            return ;
        
        FileObject activeFile = source.getPrimaryFile();
        
        if (lastValidFile == activeFile)
            return;
        
        if (lastValidFile != null && isJava(lastValidFile)) {
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
        
        JavaSource activeJS = JavaSource.forFileObject(activeFile);
        
        if (activeJS == null) {
            //not a Java document:
            return ;
        }
        
        lastValidFile = activeFile;
        
        JavaSourceAccessor.INSTANCE.revalidate(activeJS);
    }

    /**Set to switch off compilation, usefull in tests.
     * Use SourceUtilsTestUtil.ignoreCompileRequests to set it:
     */
    public static boolean IGNORE_COMPILE_REQUESTS = false;

    //needs to be the same as in RepositoryUpdater:
    private static boolean isJava(final FileObject fo) {
        if (fo.isFolder()) {
            return false;
        } else if (JavaDataLoader.JAVA_EXTENSION.equals(fo.getExt().toLowerCase())) {
            return true;
        } else {
            return JavaDataLoader.JAVA_MIME_TYPE.equals(fo.getMIMEType());
        }
    }

}
