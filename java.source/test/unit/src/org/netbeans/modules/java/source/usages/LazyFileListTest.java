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

package org.netbeans.modules.java.source.usages;

import java.util.ArrayList;
import java.util.Arrays;
import junit.framework.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author Tomas Zezula
 */
public class LazyFileListTest extends NbTestCase {

    private static final String[] EXPECTED_NAMES = {
        "a1.java",
        "a2.java",
        "d1.java",
        "d2.java",
        "b1.java",
        "b2.java",
        "e1.java",
        "e2.java",
        "c1.java",
        "c2.java"
    };
    
    private File root;
    
    public LazyFileListTest(String testName) {
        super(testName);
    }
    
    private void createNewFile(File f) throws Exception {
        f.createNewFile();
        
        if (f.length() == 0) {
            //the RepositoryUpdater.LazyFileList needs some content:
            OutputStream out = new FileOutputStream(f);
            
            try {
                out.write('\n');
            } finally {
                out.close();
            }
        }
    }

    protected void setUp() throws Exception {
        this.clearWorkDir();
        this.root = this.getWorkDir();
        File f = new File (new File(new File (root,"a"),"b"),"c");
        f.mkdirs();
        File t = new File (f,"c1.java");
        createNewFile(t);
        t = new File (f,"c2.java");
        createNewFile(t);
        t = new File (f,"c3.txt");
        createNewFile(t);
        f = f.getParentFile();
        t = new File (f,"b1.java");
        createNewFile(t);
        t = new File (f,"b2.java");
        createNewFile(t);
        t = new File (f,"b3.java");
        t.createNewFile();
        t = new File (f,"b3.txt");
        createNewFile(t);
        f = f.getParentFile();
        t = new File (f,"a1.java");
        createNewFile(t);
        t = new File (f,"a2.java");
        createNewFile(t);
        t = new File (f,"a3.txt");
        createNewFile(t);
        
        f = new File(new File (root,"d"),"e");
        f.mkdirs();
        t = new File (f,"e1.java");
        createNewFile(t);
        t = new File (f,"e2.java");
        createNewFile(t);
        f = f.getParentFile();
        t = new File (f,"d1.java");
        createNewFile(t);
        t = new File (f,"d2.java");
        createNewFile(t);
        
    }

    protected void tearDown() throws Exception {
    }

    public void testIterator() {
        RepositoryUpdater.LazyFileList lfl = new RepositoryUpdater.LazyFileList (this.root);
        List<String> fileNames = new ArrayList();
        for (File f :lfl) {
            fileNames.add(f.getName());
        }
        assertEquals(Arrays.asList(EXPECTED_NAMES),fileNames);
    }
    
    
    
}
