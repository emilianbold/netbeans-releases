/*
 * TestLookup.java
 *
 * Created on January 22, 2007, 2:15 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.wsdl.ui;

import org.netbeans.modules.xml.wsdl.ui.extensibility.model.impl.ExtensibilityUtils;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.Repository;
import org.openide.filesystems.XMLFileSystem;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author radval
 */
public class TestLookup extends AbstractLookup {
    
    private org.openide.util.lookup.InstanceContent mContent;
    
    private Repository mRepository = null;
    
    /** Creates a new instance of TestLookup */
    public TestLookup() {
        this(new org.openide.util.lookup.InstanceContent());
    }

    private TestLookup(org.openide.util.lookup.InstanceContent ic) {
        super(ic);
        mContent = ic;
    }
    
    public void setup(FileSystem fs) {
        try {
            //XMLFileSystem x = new XMLFileSystem(TestLookup.class.getResource("/org/netbeans/modules/xml/wsdl/ui/netbeans/module/resources/layer.xml"));
            mRepository = new Repository(fs);
            mContent.add(mRepository);
            ExtensibilityUtils.setModelSourceProvider(new TestModelSourceProvider());    
        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }

}
