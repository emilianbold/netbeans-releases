/*
 * PageContentProviderImpl.java
 *
 * Created on April 12, 2007, 9:23 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.visualweb.navigation;

import org.netbeans.modules.visualweb.insync.models.FacesModel;
import org.netbeans.modules.visualweb.insync.models.FacesModelSet;
import org.netbeans.modules.web.jsf.navigation.pagecontentmodel.PageContentModel;
import org.netbeans.modules.web.jsf.navigation.pagecontentmodel.PageContentModelProvider;
import org.openide.filesystems.FileObject;

/**
 *
 * @author joelle
 */
public class VWPContentModelProvider implements PageContentModelProvider{
    private FacesModel facesModel;
    private VWPContentModel vwpContentModel;
    
    /** Creates a new instance of PageContentProviderImpl */
    public VWPContentModelProvider() {
//        System.out.println("You found me.");
    }
    
    public PageContentModel getPageContentModel(FileObject fileObject) {
        FacesModelSet modelset = FacesModelSet.getInstance(fileObject);
        if( modelset !=  null ){
            facesModel = modelset.getFacesModel(fileObject);
            if ( facesModel != null ) {
                return new VWPContentModel(facesModel, fileObject.getName());
            }
        }
        return null;        
    }
    
    
}
