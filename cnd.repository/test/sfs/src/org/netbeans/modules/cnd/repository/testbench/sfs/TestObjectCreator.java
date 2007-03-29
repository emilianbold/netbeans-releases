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

import java.io.*;
import java.util.*;

/**
 *
 * @author Vladimir Kvashin
 */
public class TestObjectCreator {
    
    public Collection<TestObject> createTestObjects(List<String> args) {
	Collection<TestObject> objects = new ArrayList<TestObject>();
	for( String path : args ) {
	    createTestObjects(new File(path), objects);
	}
	return objects;
    }
    
    private void createTestObjects(File file, Collection<TestObject> objects) {
	if( file.exists() ) {
	    TestObject  obj = new TestObject(file.getAbsolutePath());
	    obj.lData = file.length();
	    objects.add(obj);
	    if( file.isDirectory() ) {
		obj.sData = file.list();
		 File[] children = file.listFiles();
		 if( children != null ) {
		     for (int i = 0; i < children.length; i++) {
			 createTestObjects(children[i], objects);
		     }
		 }
	    }
	}
    }
    
}
