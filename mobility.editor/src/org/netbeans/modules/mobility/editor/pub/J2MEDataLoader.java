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
 * J2MEDataLoader.java
 *
 * Created on February 20, 2004, 1:01 PM
 */
package org.netbeans.modules.mobility.editor.pub;

import org.netbeans.api.java.loaders.JavaDataSupport;
import org.netbeans.modules.mobility.editor.J2MENode;
import org.netbeans.modules.mobility.project.J2MEProject;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.FileEntry;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.MultiFileLoader;
import org.openide.util.NbBundle;

/**
 *
 * @author Adam Sotona
 */
public class J2MEDataLoader extends MultiFileLoader {
    
    /** Creates a new instance of J2MEDataLoader */
    public J2MEDataLoader() {
        super("org.netbeans.modules.mobility.editor.pub.J2MEDataObject"); // NOI18N
        
//        JMManager.ModifiedDOProvider.setModifiedDOProvider(new JMManager.ModifiedDOProvider() {
//            protected DataObject getModifiedDOImpl(final FileObject fo) {
//                if (FileOwnerQuery.getOwner(fo) instanceof J2MEProject) try {
//                    final DataObject dob = DataObject.find(fo);
//                    if (dob != null) {
//                        final EditorCookie ec = (EditorCookie)dob.getCookie(EditorCookie.class);
//                        if (ec != null) {
//                            if (ec.getDocument() != null) return dob;
//                            RequestProcessor.getDefault().post(new Runnable(){
//                                public void run() {
//                                    if (dob.isValid()) try {
//                                        ec.openDocument();
//                                        JavaMetamodel.getManager().addModified(fo);
//                                    } catch (IOException ioe) {
//                                        ErrorManager.getDefault().notify(ioe);
//                                    }
//                                }
//                            }, 5000);
//                        }
//                    }
//                } catch (Exception e) {}
//                return null;
//            }
//        });
    }
    
    public J2MEDataLoader(String recognizedObject) {
        super(recognizedObject);
    }
    
    @Override
    protected FileObject findPrimaryFile(FileObject fo) {
        return fo.isData() && "java".equals(fo.getExt()) && !fo.existsExt("form") && J2MEProject.isJ2MEFile(fo) ? fo : null; //NOI18N
    }
    
    
    @Override
    protected String defaultDisplayName() {
        return NbBundle.getMessage(J2MENode.class, "PROP_J2MELoader_Name"); //NOI18N
    }

    @Override
    protected String actionsContext () {
        return "Loaders/text/x-java/Actions/"; // NOI18N
    }
    
    @Override
    protected MultiDataObject createMultiObject(final FileObject primaryFile) throws DataObjectExistsException, java.io.IOException {
        return new J2MEDataObject(primaryFile, this);
    }

protected org.openide.loaders.MultiDataObject.Entry
createPrimaryEntry(MultiDataObject obj, FileObject primaryFile) {
        return JavaDataSupport.createJavaFileEntry(obj, primaryFile);
  }

protected org.openide.loaders.MultiDataObject.Entry
createSecondaryEntry(MultiDataObject obj, FileObject secondaryFile) {
        return new FileEntry.Numb(obj, secondaryFile);
  }
}
