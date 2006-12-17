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

package org.netbeans.modules.java;

import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.ErrorManager;
import org.openide.awt.StatusDisplayer;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.MultiFileLoader;
import org.openide.nodes.Node;
import org.openide.nodes.Node.Cookie;
import org.openide.util.NbBundle;

    
public final class ClassDataObject extends MultiDataObject {
    
    public ClassDataObject(FileObject pf, MultiFileLoader loader) throws DataObjectExistsException {
        super(pf, loader);
    }

    public @Override Node createNodeDelegate() {
        return new JavaNode (this, false);
    }

    public @Override <T extends Cookie> T getCookie(Class<T> type) {
        if (type.isAssignableFrom(OpenSourceCookie.class)) {
            return type.cast(new OpenSourceCookie());
        } else {
            return super.getCookie (type);
        }
    }
    
    private final class OpenSourceCookie implements OpenCookie {
        
        public void open() {
            try {
                FileObject fo = getPrimaryFile();
                FileObject binaryRoot = null;
                String resourceName = null;
                ClassPath cp = ClassPath.getClassPath(fo, ClassPath.COMPILE);
                if (cp == null || (binaryRoot = cp.findOwnerRoot(fo))==null) {
                    cp = ClassPath.getClassPath(fo, ClassPath.EXECUTE);
                    if (cp != null) {
                        binaryRoot = cp.findOwnerRoot(fo);
                        resourceName = cp.getResourceName(fo,'/',false);  //NOI18N
                    }
                } else if (binaryRoot != null) {
                    resourceName = cp.getResourceName(fo,'/',false);  //NOI18N
                }
                FileObject[] sourceRoots = null;
                if (binaryRoot != null) {
                    sourceRoots = SourceForBinaryQuery.findSourceRoots(binaryRoot.getURL()).getRoots();
                }
                FileObject resource = null;
                if (sourceRoots != null && sourceRoots.length>0) {
                    cp = ClassPathSupport.createClassPath(sourceRoots);
                    resource = cp.findResource(resourceName+ ".java"); //NOI18N
                }
                if (resource !=null ) {
                    DataObject sourceFile = DataObject.find(resource);
                    OpenCookie oc = sourceFile.getCookie(OpenCookie.class);
                    if (oc != null) {
                        oc.open();
                    } else {
                        ErrorManager.getDefault().log("SourceFile: "+FileUtil.getFileDisplayName (resource) +" has no OpenCookie"); //NOI18N
                    }
                } else {
                    if (resourceName == null)
                        resourceName = fo.getName();
                    StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(ClassDataObject.class,"TXT_NoSources",
                            resourceName.replace('/','.'))); //NOI18N
                }
            } catch (FileStateInvalidException e) {
                ErrorManager.getDefault().notify(e);
            } catch (DataObjectNotFoundException nf) {
                ErrorManager.getDefault().notify(nf);
            }
        }
    }
}
