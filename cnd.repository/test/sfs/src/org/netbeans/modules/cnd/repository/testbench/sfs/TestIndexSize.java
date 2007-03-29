/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 
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

package org.netbeans.modules.cnd.repository.testbench.sfs;

import java.io.IOException;
import java.util.List;
import org.netbeans.modules.cnd.repository.sfs.CompactFileIndex;
import org.netbeans.modules.cnd.repository.sfs.FileIndex;
import org.netbeans.modules.cnd.repository.sfs.SimpleFileIndex;
import org.netbeans.modules.cnd.repository.spi.Key;
import org.netbeans.modules.cnd.repository.testbench.Stats;

/**
 *
 * @author Vladimir Kvashin
 */
public class TestIndexSize extends BaseTest {
    
    public void test(List<String> params) throws IOException {
	int size = 200000;
	Key[] keys = new Key[size];
	for (int i = 0; i < keys.length; i++) {
	    keys[i] = new TestKey("#" + i);
	}

	
	pause();
	
	FileIndex index = Stats.useCompactIndex ? new CompactFileIndex() : new SimpleFileIndex();
	for (int i = 0; i < keys.length; i++) {
	    index.put(keys[i], 0, 0);
	}
	
	pause();
    }
    
    private void pause() {
	System.out.printf("Press any key to continue\n");
	try {
	    System.in.read();
	} catch (IOException ex) {
	}
    }
}
