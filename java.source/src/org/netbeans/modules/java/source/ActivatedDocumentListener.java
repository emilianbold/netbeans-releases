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

import java.io.IOException;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.Document;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.timers.TimesCollector;
import org.netbeans.editor.Registry;
import org.netbeans.modules.java.source.usages.RepositoryUpdater;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;

/**
 *
 * @author Jan Lahoda
 */
public class ActivatedDocumentListener implements ChangeListener {
    
    private static ActivatedDocumentListener INSTANCE;
    
    public static void register() {
        INSTANCE = new ActivatedDocumentListener();
    }
    
    private FileObject lastValidFile;
    
    /**
     * Creates a new instance of ActivatedDocumentListener
     */
    private ActivatedDocumentListener() {
        Registry.addChangeListener(this);
    }

    public synchronized void stateChanged(ChangeEvent e) {
        Document active = Registry.getMostActiveDocument();
        
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
        
        if (lastValidFile != null) {
            if (!IGNORE_COMPILE_REQUESTS) {
                ClassPath cp = ClassPath.getClassPath(lastValidFile, ClassPath.SOURCE);
                if (cp != null) {
                    FileObject owner = cp.findOwnerRoot(lastValidFile);
                    assert owner != null;
                    try {
                        if ("file".equals(lastValidFile.getURL().getProtocol())) {  //NOI18N
                            RepositoryUpdater.getDefault().scheduleCompilation(lastValidFile, owner);
                        }
                    } catch (IOException ioe) {
                        Exceptions.printStackTrace(ioe);
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
        
        TimesCollector.getDefault().select(activeFile);
        JavaSourceAccessor.INSTANCE.revalidate(activeJS);
    }

    /**Set to switch off compilation, usefull in tests.
     * Use SourceUtilsTestUtil.ignoreCompileRequests to set it:
     */
    public static boolean IGNORE_COMPILE_REQUESTS = false;

}
