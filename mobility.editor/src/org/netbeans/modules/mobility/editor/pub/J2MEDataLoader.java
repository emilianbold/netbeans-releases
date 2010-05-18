/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
        return fo.isData() && "java".equals(fo.getExt()) && J2MEProject.isJ2MEFile(fo) && !fo.existsExt("form")&& !fo.existsExt("jwt") ? fo : null; //NOI18N
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
