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
import java.nio.*;
import java.nio.channels.*;
import java.util.*;
import org.netbeans.modules.cnd.repository.sfs.*;

/**
 * Test object to store in a SingleFileStorage
 * @author Vladimir Kvashin
 */
public class TestObject implements SingleFileStorage.Reader, SingleFileStorage.Writer {
    
    public String key;
    public String[] sData;
    public int iData;
    public long lData;
    
    public TestObject(String key, String... data) {
	this.key = key;
	this.sData = data;
    }
    
    String getKey() {
	return key;
    }

    public void write(DataOutput out) throws IOException {
	out.writeUTF(key);
	if( sData == null ) {
	    out.writeInt(-1);
	}
	else {
	    out.writeInt(sData.length);
	    for (int i = 0; i < sData.length; i++) {
		out.writeUTF(sData[i]);
	    }
	}
	out.writeInt(iData);
	out.writeLong(lData);
    }
    
    public void read(DataInput in) throws IOException {
	key = in.readUTF();
	int cnt = in.readInt();
	if( cnt == -1 ) {
	    sData = null;
	}
	else {
	    sData = new String[cnt];
	    for (int i = 0; i < sData.length; i++) {
		sData[i] = in.readUTF();
	    }
	}
	iData = in.readInt();
	lData = in.readLong();
    }
    
    public String toString() {
	StringBuilder sb = new StringBuilder("key="); // NOI18N
	sb.append(key);
	sb.append(" sData="); // NOI18N
	if( sData == null ) {
	    sb.append("null"); // NOI18N
	}
	else {
	    for (int i = 0; i < sData.length; i++) {
		if( i == 0) {
		    sb.append('[');
		}
		else {
		    sb.append(","); // NOI18N
		}
		sb.append(sData[i]);
	    }
	}
	sb.append("] iData="); // NOI18N
	sb.append(iData);
	sb.append(" lData="); // NOI18N
	sb.append(lData);
	return sb.toString();
    }

    public int hashCode() {
	int hash = iData + (int) lData + key.hashCode();
	for (int i = 0; i < sData.length; i++) {
	    hash += sData.hashCode();
	}
	return hash;
    }
    
    public boolean equals(Object obj) {
	if( obj == null ) {
	    return false;
	}
	if( ! obj.getClass().equals(TestObject.class) ) {
	    return false;
	}
	TestObject other = (TestObject) obj;
	return	equals(this.key, other.key) &&
		equals(this.sData, other.sData) &&
		this.lData == other.lData &&
		this.iData == other.iData;
    }
    
    private boolean equals(String s1, String s2) {
	if( s1 == null ) {
	    return s2 == null;
	}
	else {
	    return s1.equals(s2);
	}
    }
    
    private boolean equals(String[] s1, String[] s2) {
	if( s1 == null ) {
	    return s2 == null;
	}
	else if( s2 == null ) {
	    return false;
	}
	else {
	    if( s1.length != s2.length ) {
		return false;
	    }
	    else {
		for (int i = 0; i < s1.length; i++) {
		    if( ! equals(s1[i], s2[i]) ) {
			return false;
		    }
		}
	    }
	    return true;
	}
    }
}
