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
