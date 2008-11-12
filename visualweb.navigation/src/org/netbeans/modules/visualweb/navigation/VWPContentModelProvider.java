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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
 * PageContentProviderImpl.java
 *
 * Created on April 12, 2007, 9:23 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.visualweb.navigation;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.WeakHashMap;
import org.netbeans.modules.visualweb.insync.models.FacesModel;
import org.netbeans.modules.visualweb.insync.models.FacesModelSet;
import org.netbeans.modules.visualweb.project.jsf.api.JsfProjectUtils;
import org.netbeans.modules.web.jsf.navigation.pagecontentmodel.PageContentModel;
import org.netbeans.modules.web.jsf.navigation.pagecontentmodel.PageContentModelProvider;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;

/**
 *
 * @author joelle
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.web.jsf.navigation.pagecontentmodel.PageContentModelProvider.class)
public class VWPContentModelProvider implements PageContentModelProvider {

    private Map<FileObject, Reference<VWPContentModel>> map = Collections.synchronizedMap(new WeakHashMap<FileObject, Reference<VWPContentModel>>());

    /** Creates a new instance of PageContentProviderImpl */
    public VWPContentModelProvider() {
        //        System.out.println("You found me.");
    }

    public PageContentModel getPageContentModel(FileObject fileObject) {
        Reference<VWPContentModel> ref = map.get(fileObject);
        VWPContentModel model = null;
        if (ref != null) {
            model = ref.get();
            if (model != null) {
                return model;
            }
        }
        if (JsfProjectUtils.isJsfProjectFile(fileObject)) {
            FacesModelSet modelset = FacesModelSet.getInstance(fileObject);
            FacesModel facesModel = null;
            if (modelset != null) {
                synchronized (modelset) {
                    facesModel = modelset.getFacesModel(fileObject);
                    if (facesModel == null) {
                        return null;
                    }
                }
            } else {
                return null; //REMINDER
            }
            model = new VWPContentModel(this, facesModel);
            if (model != null) {
                map.put(fileObject, new WeakReference<VWPContentModel>(model));
                fileObject.addFileChangeListener(new FileChangeAdapter() {

                    @Override
                    public void fileDeleted(FileEvent fe) {
                        FileObject fileObj = fe.getFile();
                        map.remove(fileObj);
                        super.fileDeleted(fe);
                    }
                });
            }
        }
        return model;
    }

    public void removeModel(VWPContentModel model) {
        Set<Entry<FileObject, Reference<VWPContentModel>>> entrySet = map.entrySet();
        for (Entry<FileObject, Reference<VWPContentModel>> entry : entrySet) {
            if (entry != null && entry.getValue() != null 
                    && entry.getValue().get() != null 
                    && entry.getValue().get().equals(model)) {
                map.remove(entry.getKey());
                break;
            }
        }
    }

    public FileObject isNewPageContentModel(FileObject fileObject) {
        FileObject jsp = JsfProjectUtils.getJspForJava(fileObject);
        if (map.get(jsp) == null) {
            return jsp;
        }
        return null;
    }
}
