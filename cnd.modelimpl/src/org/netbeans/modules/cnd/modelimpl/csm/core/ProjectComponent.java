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

package org.netbeans.modules.cnd.modelimpl.csm.core;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import org.netbeans.modules.cnd.modelimpl.repository.RepositoryUtils;
import org.netbeans.modules.cnd.repository.spi.Key;
import org.netbeans.modules.cnd.repository.spi.Persistent;
import org.netbeans.modules.cnd.repository.support.KeyFactory;
import org.netbeans.modules.cnd.repository.support.SelfPersistent;

/**
 * A common ancestor for project components that 
 * 1) has key (most likely a project-based one);
 * 2) are able to put themselves into repository.
 *
 * It similar to Identifiable, but doesn't involve UIDs:
 * UIDs are unnecessary for such internal components as different project parts.
 *
 * @author Vladimir Kvashin
 */

//package-local
abstract class ProjectComponent implements Persistent, SelfPersistent {
    
    private Key key;
    
    public ProjectComponent(Key key) {
	this.key = key;
    }
    
    public ProjectComponent(DataInput in) throws IOException {
	key = KeyFactory.getDefaultFactory().readKey(in);
	//System.err.printf("<<< Reading %s key %s\n", this, key);
    }
    
    public Key getKey() {
	return key;
    }
    
    public void put() {
	//System.err.printf(">>> Putting %s by key %s\n", this, key);
	RepositoryUtils.put(key, this);
    }
    
    public void write(DataOutput out) throws IOException {
	//System.err.printf(">>> Writing %s by key %s\n", this, key);
	writeKey(key, out);
    }
    
    public static Key readKey(DataInput in) throws IOException {
	return KeyFactory.getDefaultFactory().readKey(in);
    }
    
    public static void writeKey(Key key, DataOutput out) throws IOException {
	KeyFactory.getDefaultFactory().writeKey(key, out);
    }
}

