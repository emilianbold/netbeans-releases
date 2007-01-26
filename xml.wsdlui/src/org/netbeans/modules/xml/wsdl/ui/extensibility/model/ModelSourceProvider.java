/*
 * ModelSourceProvider.java
 *
 * Created on January 22, 2007, 7:25 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.wsdl.ui.extensibility.model;

import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.locator.CatalogModelException;
import org.openide.filesystems.FileObject;

/**
 *
 * @author radval
 */
public interface ModelSourceProvider {
    
    ModelSource getModelSource(FileObject file, boolean editable) throws CatalogModelException;
    
}
