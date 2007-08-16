/*
 * PageContentProviderImpl.java
 *
 * Created on April 12, 2007, 9:23 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.visualweb.navigation;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
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
public class VWPContentModelProvider implements PageContentModelProvider {

    private Map<FileObject, VWPContentModel> map = Collections.synchronizedMap(new HashMap<FileObject, VWPContentModel>());

    /** Creates a new instance of PageContentProviderImpl */
    public VWPContentModelProvider() {
        //        System.out.println("You found me.");
    }

    public PageContentModel getPageContentModel(FileObject fileObject) {

        VWPContentModel model = map.get(fileObject);
        if (model != null) {
            return model;
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
                map.put(fileObject, model);
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
        Set<Entry<FileObject, VWPContentModel>> entrySet = map.entrySet();
        for (Entry<FileObject, VWPContentModel> entry : entrySet) {
            if (entry.getValue().equals(model)) {
                map.remove(entry.getKey());
                break;
            }
        }
    }
    
    
    public FileObject isNewPageContentModel(FileObject fileObject) {
        FileObject jsp = JsfProjectUtils.getJspForJava(fileObject);
        if( map.get(jsp) == null ){
            return jsp;
        }
        return null;
    }
}
