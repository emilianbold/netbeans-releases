/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.openide.filesystems;

import java.io.IOException;
import java.io.File;
import java.util.Enumeration;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;

import org.netbeans.performance.Benchmark;
import org.openide.filesystems.*;

/**
 * FSTest is a base class for FileSystem tests. It defines a lot of methods that
 * exploit interface of the FileSystem class. It tests operations that change
 * state of a FileSystem.
 */
public abstract class FSTest extends ReadOnlyFSTest {
    
    /** Creates new Tests */
    public FSTest(String name) {
        super(name);
    }
    
    //--------------------------------------------------------------------------
    //------------------------- attributes section -----------------------------
    
    /** Sets one random attribute for all FileObjects (their no. given by the
     * parameter). Attributes are added sequentially.
     */
    public void testSetOneAttributeSeq() throws IOException {
        FileObject[] files = this.files;
        String[][] pairs = this.pairs;
        int iterations = this.iterations;
        
        for (int it = 0; it < iterations; it++) {
            for (int i = 0; i < files.length; i++) {
                files[i].setAttribute(pairs[i][0], pairs[i][1]);
            }
        }
    }
    
    /** Sets many random attributes for all FileObjects (their no. given by the 
     * parameter). Attributes are added sequentially.
     */
    public void testSetManyAttributesSeq() throws IOException {
        FileObject[] files = this.files;
        String[][] pairs = this.pairs;
        int iterations = this.iterations;
        
        for (int it = 0; it < iterations; it++) {
            for (int i = 0; i < files.length; i++) {
                for (int j = 0; (j < pairs.length) && (j < attrsCount); j++) {
                    files[i].setAttribute(pairs[j][0], pairs[j][1]);
                }
            }
        }
    }
    
    /** Sets one random attribute for all FileObjects (their no. given by the
     * parameter). Attributes are added randomly.
     */
    public void testSetOneAttributeRnd() throws IOException {
        FileObject[] files = this.files;
        String[][] pairs = this.pairs;
        int iterations = this.iterations;
        int perm[] = this.perm;
        
        for (int it = 0; it < iterations; it++) {
            for (int i = 0; i < files.length; i++) {
                files[perm[i]].setAttribute(pairs[i][0], pairs[i][1]);
            }
        }
    }    
    
    /** Sets many random attributes for all FileObjects (their no. given by the 
     * parameter). Attributes are added randomly.
     */
    public void testSetManyAttributesRnd() throws IOException {
        FileObject[] files = this.files;
        String[][] pairs = this.pairs;
        int iterations = this.iterations;
        int perm[] = this.perm;
        
        for (int it = 0; it < iterations; it++) {
            for (int i = 0; i < files.length; i++) {
                for (int j = 0; (j < pairs.length) && (j < attrsCount); j++) {
                    files[perm[i]].setAttribute(pairs[j][0], pairs[j][1]);
                }
            }
        }
    }
}
