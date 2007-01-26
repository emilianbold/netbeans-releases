/*
 * TestModelSourceProvider.java
 *
 * Created on January 22, 2007, 7:30 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.wsdl.ui;

import org.netbeans.modules.xml.wsdl.ui.extensibility.model.ModelSourceProvider;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.locator.CatalogModelException;
import org.openide.filesystems.FileObject;

/**
 *
 * @author radval
 */
public class TestModelSourceProvider implements ModelSourceProvider {
    
    /** Creates a new instance of TestModelSourceProvider */
    public TestModelSourceProvider() {
    }

    public ModelSource getModelSource(FileObject file, boolean editable) throws CatalogModelException {
        //return TestCatalogModel.getDefault().createModelSource(file, editable);
        return TestUtil.createModelSource(file, editable);
    }

}
