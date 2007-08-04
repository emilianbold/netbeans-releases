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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.ruby.hints;

import java.util.List;
import org.openide.filesystems.FileObject;

/**
 * Test the block-var hint
 * 
 * @author Tor Norbye
 */
public class BlockVarTest extends HintTestBase {

    public BlockVarTest(String testName) {
        super(testName);
    }

//    // Not working yet
//    public void testRegistered() throws Exception {
//        ensureRegistered(new BlockVarReuse());
//    }
    
    public void testHint1() throws Exception {
        findHints(this, new BlockVarReuse(), "testfiles/blockvars.rb", null);
    }

    public void testBlockVarReuse() throws Exception {
        List<FileObject> files = getBigSourceFiles();
        for (FileObject f : files) {
            findHints(this, new BlockVarReuse(), f, null);
        }
    }
}
