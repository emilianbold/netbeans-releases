/*
 * Properties.java
 *
 * Created on November 25, 2002, 7:06 PM
 */

package org.netbeans.test.editor.app.core.properties;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Vector;

/**
 *
 * @author  eh103527
 */
public class Properties {
    
    Vector entries;
    
    /** Creates a new instance of Properties */
    public Properties() {
	entries=new Vector(30);
    }
    
    public Object getProperty(java.lang.String name) {
	if (name == null)  throw new NullPointerException("Null name property.");
	Entry e;
	for (Iterator it=entries.iterator();it.hasNext();) {
	    e=(Entry)(it.next());
	    if (e.key.compareTo(name) == 0) {
		return e.value;
	    }
	}
	return null;
    }
    
    public Object put(String name, Object value) {
	Object ret=null;
	Entry e=null;
	if (name == null)  throw new NullPointerException("Null name property.");
	if (value == null) throw new NullPointerException("Null property value.");
	for (Iterator it=entries.iterator();it.hasNext();) {
	    e=(Entry)(it.next());
	    if (e.key.compareTo(name) == 0) {
		ret=e.value;
		break;
	    }
	}
	if (ret == null) {
	    entries.add(new Entry(name,value));
	} else {
	    e.value=value;
	}
	return ret;
    }
    
    public Enumeration propertyNames() {
	return new Enumeration() {
	    String[] names=getNames();
	    int i=0;
	    
	    public boolean hasMoreElements() {
		return (i < names.length);
	    }
	    
	    public Object nextElement() {
		return names[i++];
	    }
	};
    }
    
    private String[] getNames() {
	ArrayList ar=new ArrayList();
	Entry e;
	for (Iterator it=entries.iterator();it.hasNext();) {
	    e=(Entry)(it.next());
	    ar.add(e.key);
	}
	return (String[])(ar.toArray(new String[] {}));
    }
    
    /** Removes all mappings from this map (optional operation).
     *
     * @throws UnsupportedOperationException clear is not supported by this
     * 		  map.
     *
     */
    public void clear() {
	entries.removeAllElements();
    }
    
    /** Returns <tt>true</tt> if this map contains no key-value mappings.
     *
     * @return <tt>true</tt> if this map contains no key-value mappings.
     *
     */
    public boolean isEmpty() {
	return entries.size() == 0;
    }
    
    /** Removes the mapping for this key from this map if it is present
     * (optional operation).   More formally, if this map contains a mapping
     * from key <tt>k</tt> to value <tt>v</tt> such that
     * <code>(key==null ?  k==null : key.equals(k))</code>, that mapping
     * is removed.  (The map can contain at most one such mapping.)
     *
     * <p>Returns the value to which the map previously associated the key, or
     * <tt>null</tt> if the map contained no mapping for this key.  (A
     * <tt>null</tt> return can also indicate that the map previously
     * associated <tt>null</tt> with the specified key if the implementation
     * supports <tt>null</tt> values.)  The map will not contain a mapping for
     * the specified  key once the call returns.
     *
     * @param key key whose mapping is to be removed from the map.
     * @return previous value associated with specified key, or <tt>null</tt>
     * 	       if there was no mapping for key.
     *
     * @throws ClassCastException if the key is of an inappropriate type for
     * 		  this map (optional).
     * @throws NullPointerException if the key is <tt>null</tt> and this map
     *            does not not permit <tt>null</tt> keys (optional).
     * @throws UnsupportedOperationException if the <tt>remove</tt> method is
     *         not supported by this map.
     *
     */
    public Object remove(String key) {
	Entry e;
	for (Iterator it=entries.iterator();it.hasNext();) {
	    e=(Entry)(it.next());
	    if (e.key.compareTo(key) == 0) {
		entries.remove(e);
		return e.value;
	    }
	}
	return null;
    }
    
    /** Returns the number of key-value mappings in this map.  If the
     * map contains more than <tt>Integer.MAX_VALUE</tt> elements, returns
     * <tt>Integer.MAX_VALUE</tt>.
     *
     * @return the number of key-value mappings in this map.
     *
     */
    public int size() {
	return entries.size();
    }
    
    static class Entry {
	public String key;
	public Object value;
	
	public Entry(String key,Object value) {
	    this.key=key;
	    this.value=value;
	}
    }
}
