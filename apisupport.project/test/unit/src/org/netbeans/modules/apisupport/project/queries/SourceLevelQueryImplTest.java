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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
