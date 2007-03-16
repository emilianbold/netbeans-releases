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

package test.sfs;

import java.io.IOException;
import java.util.*;
import org.netbeans.modules.cnd.repository.testbench.sfs.TestDataInputOutput;
import org.netbeans.modules.cnd.repository.testbench.sfs.TestSingleFileStorage;

/**
 * Main for org.netbeans.modules.cnd.repository.sfs package testing
 * @author Vladimir Kvashin
 */
public class TestMain {
    
    public static void main(String[] args) {
	new TestMain().run(args);
    }
    
    private void run(String[] args) {
	
	boolean testDataIO = false;
	
	List<String> params = new ArrayList<String>();
	for (int i = 0; i < args.length; i++) {
	    if( "-s".equals(args[i]) ) { // NOI18N
		testDataIO = true;
	    }
	    else {
		params.add(args[i]);
	    }
	}
	
	try {
	    if( testDataIO ) {
		new TestDataInputOutput().test();
	    }
	    else {
		new TestSingleFileStorage().test(params);
	    }
	}
	catch( Exception e ) {
	    e.printStackTrace(System.err);
	}
    }    
}
