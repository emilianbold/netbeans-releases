/*
 * PageContentModelProvider.java
 *
 * Created on March 27, 2007, 5:31 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.web.jsf.navigation.pagecontentmodel;

import org.openide.filesystems.FileObject;

/**
 *
 * @author joelle lam
 */
public interface PageContentModelProvider {
    /**
     * Returns the Page Content Model
     * @param fileObject 
     * @return PageContentModel for a given fileobject, null if none exists.
     */
    public PageContentModel getPageContentModel(FileObject fileObject);
    /**
     * This method is tricky and sort of a hack.
     * Given a new page or modification, does there now exist a model
     * unlike before.  This method was primarily completed for VWP 
     * functionality which sometimes needs to know about a new JAVA 
     * file being updated.
     * @param fileObject of the new page 
     * @return FileObject of the model that should be updated 
     **/
    public FileObject isNewPageContentModel(FileObject fileObject);
}
