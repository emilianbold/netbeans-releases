/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.spi.project.support.ant;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.AbstractMap;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * Similar to {@link Properties} but designed to retain additional
 * information needed for safe hand-editing.
 * Useful for various <samp>*.properties</samp> in a project:
 * <ol>
 * <li>Can associate comments with particular entries.
 * <li>Order of entries preserved during modifications whenever possible.
 * <li>VCS-friendly: lines which are not semantically modified are not textually modified.
 * <li>Can automatically insert line breaks in new or modified values at positions
 *     that are likely to be semantically meaningful, e.g. between path components
 * </ol>
 * The file format (including encoding etc.) is compatible with the regular JRE implementation.
 * Only (non-null) String is supported for keys and values.
 * This class is not thread-safe; use only from a single thread, or use {@link Collections#synchronizedMap}.
 * @author Jesse Glick
 */
public final class EditableProperties extends AbstractMap implements Cloneable {
    
    // XXX temporary implementation which is not safe for hand-editing
    
    private final Properties p;
    
    /**
     * Create an empty properties list.
     */
    public EditableProperties() {
        p = new Properties();
    }
    
    /**
     * Create a properties list from an existing map.
     * No comments will be defined.
     * Any order from the existing map will be retained.
     * @param map a map from String to String
     */
    public EditableProperties(Map map) {
        this();
        // XXX type check keys and values
        p.putAll(map);
    }
    
    /**
     * Returns properties ordered according to their file position.
     */
    public Set entrySet() {
        return p.entrySet();
    }
    
    /**
     * Load properties from a stream.
     * @param stream an input stream
     * @throws IOException if the contents are malformed or the stream could not be read
     */
    public void load(InputStream stream) throws IOException {
        p.load(stream);
    }
    
    /**
     * Store properties to a stream.
     * @param stream an output stream
     * @throws IOException if the stream could not be written to
     */
    public void store(OutputStream stream) throws IOException {
        p.store(stream, null);
    }
    
    public Object put(Object key, Object value) {
        if (key == null || value == null) {
            throw new NullPointerException();
        }
        return (String)p.put((String)key, (String)value);
    }
    
    /**
     * Convenience method to get a property as a string.
     * Same behavior as <code>get</code> but has the correct return type.
     * @param key a property name
     * @return the property value, or null if it was not defined
     */
    public String getProperty(String key) {
        return (String)get(key);
    }
    
    /**
     * Convenience method to set a property.
     * Same behavior as <code>put</code> but has the correct argument types.
     * @param key a property name
     * @param value the desired value (may not be null)
     */
    public void setProperty(String key, String value) {
        put(key, value);
    }
    
    public Object clone() {
        return cloneProperties();
    }
    
    /**
     * Create an exact copy of this properties object.
     * @return a clone of this object
     */
    public EditableProperties cloneProperties() {
        // XXX could use copy-on-write for efficiency
        return new EditableProperties(this);
    }
    
}
