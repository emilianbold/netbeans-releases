/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.

 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:

 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:  "This product includes  software
    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
    Alternately, this  acknowledgment may  appear in the software itself,  if
    and wherever such third-party acknowledgments normally appear.

 4. The names "Batik" and  "Apache Software Foundation" must  not  be
    used to  endorse or promote  products derived from  this software without
    prior written permission. For written permission, please contact
    apache@apache.org.

 5. Products  derived from this software may not  be called "Apache", nor may
    "Apache" appear  in their name,  without prior written permission  of the
    Apache Software Foundation.

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation. For more  information on the
 Apache Software Foundation, please see <http://www.apache.org/>.

*/

package org.netbeans.modules.visualweb.designer.cssengine;

import org.openide.ErrorManager;

/**
 * DOH! I NOW REALIZE THERE ALREADY WAS A STRINGINTMAP IN BATIK.
 * Try to merge the two versions!
 * <p>
 *
 * ORIGINALLY COPIED FROM Batik's org.apache.batik.css.engine.value.StringMap.
 * I tweaked it to reference ints rather than Value objects and made capacity
 * a parameter and ripped out some methods we won't use.
 * <p>
 * A simple hashtable, not synchronized, with fixed load factor and with
 * equality test made with '=='.
 *
 * @author <a href="mailto:stephane@hillion.org">Stephane Hillion</a>
 * @author Tor Norbye
 */
public class StringIntMap {

    /**
     * The underlying array
     */
    protected Entry[] table;
	
    /**
     * The number of entries
     */
    protected int count;
	
    /**
     * Creates a new table.
     */
    public StringIntMap(int capacity) {
	table = new Entry[capacity];
    }

    /**
     * Gets the int corresponding to the given string.
     * Return -1 if the int is not in the table.
     * @return the value or null
     */
    public int get(String key) {
	int hash  = key.hashCode() & 0x7FFFFFFF;
	int index = hash % table.length;
	
	for (Entry e = table[index]; e != null; e = e.next) {
	    if ((e.hash == hash) && e.key == key) {
		return e.value;
	    }
	}
	return -1;
    }
    
    /**
     * Sets a new value for the given variable
     * @return the old value or null
     */
    public int put(String key, int value) {
	int hash  = key.hashCode() & 0x7FFFFFFF;
	int index = hash % table.length;
	
	for (Entry e = table[index]; e != null; e = e.next) {
	    if ((e.hash == hash) && e.key == key) {
		int old = e.value;
		e.value = value;
		return old;
	    }
	}
	
	// The key is not in the hash table
        int len = table.length;
	if (count++ >= (len * 3) >>> 2) {
            // This shouldn't happen, we're using it for internal tables
            // whose sizes I control, so log a warning so that I look at this
            // and change the default sizes
            ErrorManager.getDefault().log("Had to rehash StringIntMap");
	    rehash();
	    index = hash % table.length;
	}
	Entry e = new Entry(hash, key, value, table[index]);
	table[index] = e;
	return -1;
    }

    /**
     * Rehash the table
     */
    protected void rehash () {
	Entry[] oldTable = table;
	
	table = new Entry[oldTable.length * 2 + 1];
	
	for (int i = oldTable.length-1; i >= 0; i--) {
	    for (Entry old = oldTable[i]; old != null;) {
		Entry e = old;
		old = old.next;
		
		int index = e.hash % table.length;
		e.next = table[index];
		table[index] = e;
	    }
	}
    }

    /**
     * To manage collisions
     */
    protected static class Entry {
	/**
	 * The hash code
	 */
	public int hash;
	
	/**
	 * The key
	 */
	public String key;
	
	/**
	 * The value
	 */
	public int value;
	
	/**
	 * The next entry
	 */
	public Entry next;
	
	/**
	 * Creates a new entry
	 */
	public Entry(int hash, String key, int value, Entry next) {
	    this.hash  = hash;
	    this.key   = key;
	    this.value = value;
	    this.next  = next;
	}
    }
}
