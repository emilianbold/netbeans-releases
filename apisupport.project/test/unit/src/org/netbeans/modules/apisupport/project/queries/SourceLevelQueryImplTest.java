/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.apisupport.project.queries;

import org.netbeans.api.java.queries.SourceLevelQuery;
import org.netbeans.modules.apisupport.project.TestBase;
import org.openide.filesystems.FileObject;

/**
 * Test {@link SourceLevelQueryImpl}.
 * @author Jesse Glick
 */
public class SourceLevelQueryImplTest extends TestBase {
    
    public SourceLevelQueryImplTest(String name) {
        super(name);
    }

    public void testGetSourceLevel() {
        String path = "java/src/org/netbeans/modules/java/JavaDataObject.java";
        FileObject f = nbroot.getFileObject(path);
        assertNotNull("found " + path, f);
        assertEquals("1.4 used for an average module", "1.4", SourceLevelQuery.getSourceLevel(f));
        path = "contrib/aspects/adaptable/src/org/netbeans/api/adaptable/Adaptable.java";
        f = nbroot.getFileObject(path);
        assertNotNull("found " + path, f);
        assertEquals("1.5 used when requested", "1.5", SourceLevelQuery.getSourceLevel(f));
    }
    
}
