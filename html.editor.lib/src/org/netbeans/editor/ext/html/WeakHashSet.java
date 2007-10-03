/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

// XXX what is wrong with org.openide.util.WeakSet??

package org.netbeans.editor.ext.html;

import java.lang.ref.WeakReference;

/** This is a special set-like (not java.util.Set-like) class.
 * It holds a set of objects referenced only weakly, and which
 * can be get() by an equivalent object. It can be used e.g.
 * as a lightweight (gc()-able) intern() for String or as a temporal storage
 * for an algorithm creating a lot of long-lasting equals() immutables.
 *
 * @author  Petr Nejedly
 * @version 1.0
 */
public class WeakHashSet {

    Entry[] data;
    // count of (possibly) active Entries
    int count = 0;
    // Number of Entries at which we rehash
    int treshold;
    float loadFactor;
    
    
    /** Creates new WeakHashSet */
    public WeakHashSet( int capacity, float loadFactor ) {
        this.loadFactor = loadFactor;
        treshold = (int)(capacity * loadFactor);
        data = new Entry[capacity];
    }
    
    /** Return the object equals to this object */
    public Object get( Object obj ) {
        if( obj == null ) return null;

        Entry[] tab = data;
        Entry prev = null; 
        int hash = obj.hashCode();
        int index = (hash & 0x7FFFFFFF) % tab.length;
        
        for( Entry e = tab[index]; e != null; prev = e, e = e.next )
            if( e.hash == hash ) {
                Object value = e.value.get();
                if( value == null ) {
                    // remove this entry from chain
                    count--;
                    if( prev == null ) tab[index] = e.next;
                    else prev.next = e.next;
                } else {
                    if( value.equals( obj ) ) return value;
                }
            }
	return null;
    }
    
    public Object put( Object obj ) {
        if( obj == null ) return null;
        
	Entry[] tab = data;
        Entry prev = null;
        int hash = obj.hashCode();
        int index = (hash & 0x7FFFFFFF) % tab.length;

        for( Entry e = tab[index] ; e != null ; prev = e, e = e.next )
            if( e.hash == hash ) {
                Object value = e.value.get();
                if( value == null ) {
                    count--;
                    if( prev == null ) tab[index] = e.next;
                    else prev.next = e.next;
                } else {
                    if( value.equals( obj ) ) return value;
                }
            }

        
        if( count >= treshold ) {
            rehash();
            tab = data;
            index = (hash & 0x7FFFFFFF) % tab.length;
        }

        Entry e = new Entry( hash, obj, tab[index] );
        tab[index] = e;
        count++;
            
        return obj;
    }    

    private void rehash() {
	int oldCapacity = data.length;
	Entry oldMap[] = data;

	int newCapacity = oldCapacity * 2 + 1;
	Entry newMap[] = new Entry[newCapacity];

	treshold = (int)(newCapacity * loadFactor);
	data = newMap;

	for( int i = oldCapacity ; i-- > 0 ; ) {
	    for( Entry old = oldMap[i] ; old != null ; ) {
		Entry e = old;
		old = old.next;

		int index = (e.hash & 0x7FFFFFFF) % newCapacity;
		e.next = newMap[index];
		newMap[index] = e;
	    }
	}
    }

    
    
    
    /**
     * WeakHashSet collision list entry.
     */
    private static class Entry {
	int hash;
        WeakReference value;
	Entry next;

	Entry(int hash, Object value, Entry next) {
	    this.hash = hash;
	    this.value = new WeakReference( value );
	    this.next = next;
	}
    }

}
