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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

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
