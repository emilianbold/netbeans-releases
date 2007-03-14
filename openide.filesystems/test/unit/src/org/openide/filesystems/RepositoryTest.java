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

package org.openide.filesystems;

import junit.framework.TestCase;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 *
 * @author Jaroslav Tulach
 */
public class RepositoryTest extends TestCase {
    static {
        System.setProperty("org.openide.util.Lookup", MainLookup.class.getName());
    }
    
    
    public RepositoryTest(String testName) {
        super(testName);
    }

    
    public void testContentOfFileSystemIsInfluencedByLookup () throws Exception {
        FileSystem mem = FileUtil.createMemoryFileSystem();
        String dir = "/yarda/own/file";
        org.openide.filesystems.FileUtil.createFolder (mem.getRoot (), dir);
        
        assertNull ("File is not there yet", Repository.getDefault ().getDefaultFileSystem ().findResource (dir));
        MainLookup.ic.add(mem);
        try {
            assertNotNull ("The file is there now", Repository.getDefault ().getDefaultFileSystem ().findResource (dir));
        } finally {
            MainLookup.ic.remove(mem);
        }
        assertNull ("File is no longer there", Repository.getDefault ().getDefaultFileSystem ().findResource (dir));
    }
    
    
    public static final class MainLookup extends AbstractLookup {
        static final InstanceContent ic = new InstanceContent();
        
        
        public MainLookup() {
            super(ic);
        }
    }
}
