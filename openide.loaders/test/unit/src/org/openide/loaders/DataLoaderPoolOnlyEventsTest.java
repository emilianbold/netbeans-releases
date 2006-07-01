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
