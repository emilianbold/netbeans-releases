/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.openide.loaders;

import junit.textui.TestRunner;

import org.openide.filesystems.*;
import java.io.IOException;
import java.util.*;
import org.netbeans.junit.*;
import org.openide.util.Lookup;

/** This is the same test as DataLoaderPoolTest, just intead of 
 * calling setPreferredLoader, it only changes the attribute of filesystem
 * to see if listening on attributes works fine.
 * 
 * @author Jaroslav Tulach
 */
public class DataLoaderPoolOnlyEventsTest extends DataLoaderPoolTest {
    
    public DataLoaderPoolOnlyEventsTest(String name) {
        super(name);
    }
    
    public static void main(String[] args) {
        TestRunner.run(new NbTestSuite(DataLoaderPoolOnlyEventsTest.class));
    }

    /** Changes directly the filesystem attribute.
     */
    protected void doSetPreferredLoader(FileObject fo, DataLoader loader) throws IOException {
        if (loader == null) {
            fo.setAttribute("NetBeansAttrAssignedLoader", null);
        } else {
            Class c = loader.getClass();
            fo.setAttribute ("NetBeansAttrAssignedLoader", c.getName ());
        }
    }
    
    
}
