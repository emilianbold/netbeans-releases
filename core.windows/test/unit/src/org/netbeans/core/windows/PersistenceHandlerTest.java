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

package org.netbeans.core.windows;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.openide.filesystems.Repository;
import org.openide.modules.ModuleInfo;
import org.openide.util.Lookup;

/** 
 * Test PersistenceHandler functionality.
 * 
 * @author Marek Slama
 * 
 */
public class PersistenceHandlerTest extends NbTestCase {

    public PersistenceHandlerTest (String name) {
        super (name);
    }
    
    public static Test suite() {
        TestSuite suite = new NbTestSuite(PersistenceHandlerTest.class);
        
        return suite;
    }

    protected boolean runInEQ () {
        return true;
    }
    
    /**
     * Make sure that closed TCs are not deserialized during saving window system ie. also
     * during IDE exit. Test creates test TC and overwrites method readExternal. This method
     * should not be called.
     */
    public void testSaveWindowSystem () throws Exception {
        Lookup.getDefault().lookup(ModuleInfo.class);
        
        IDEInitializer.addLayers
        (new String [] {"org/netbeans/core/windows/resources/layer-PersistenceHandlerTest.xml"});
        
        //Verify that test layer was added to default filesystem
        assertNotNull(Repository.getDefault().getDefaultFileSystem().findResource
        ("Windows2/Modes/editor/component00.wstcref"));
        
        PersistenceHandler.getDefault().load();
                
        //Check that test TopComponent is not instantiated before
        assertFalse
        ("Closed TopComponent was instantiated before window system save but it should not.",
         Component00.wasDeserialized());
        
        PersistenceHandler.getDefault().save();
        
        //Check if test TopComponent was instantiated
        assertFalse
        ("Closed TopComponent was instantiated during window system save but it should not.",
         Component00.wasDeserialized());
        
        IDEInitializer.removeLayers();
    }
    
}

