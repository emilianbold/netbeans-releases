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

package org.netbeans.api.editor.settings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;

/**
 * A collection of utility menthods for working with <code>AttributeSet</code>s.
 *
 * @author Vita Stejskal
 */
public final class AttributesUtilities {

    private static final String ATTR_DISMANTLED_STRUCTURE = "dismantled-structure"; //NOI18N
    
    /**
     * Creates an immutable <code>AttributeSet</code>, which will contain the
     * <code>keyValuePairs</code> attributes. If the pairs
     * contain attributes with the same name the resulting <code>AttributeSet</code>
     * will return value of the first attribute it will find going through
     * the pairs in the order as they were passed in.
     *
     * @param keyValuePairs    The contents of the <code>AttributeSet</code> created
     *                         by this method. This parameter should list pairs
     *                         of key-value objects; each pair defining one attribute.
     *
     * @return The new immutable <code>AttributeSet</code>.
     */
    public static AttributeSet createImmutable(Object... keyValuePairs) {
        assert keyValuePairs.length % 2 == 0 : "There must be even number of prameters. " +
            "They are key-value pairs of attributes that will be inserted into the set.";

        HashMap<Object, Object> map = new HashMap<Object, Object>();
        
        for(int i = keyValuePairs.length / 2 - 1; i >= 0 ; i--) {
            Object attrKey = keyValuePairs[2 * i];
            Object attrValue = keyValuePairs[2 * i + 1];

            map.put(attrKey, attrValue);
        }
        
        return new Immutable(map);
    }

    /**
     * Creates an immutable <code>AttributeSet</code> as a copy of <code>AttributeSet</code>s
     * passed into this method. If the <code>AttributeSet</code>s
     * contain attributes with the same name the resulting <code>AttributeSet</code>
     * will return value of the first attribute it will find going through
     * the sets in the order as they were passed in.
     *
     * @param sets    The <code>AttributeSet</code>s which attributes will become
     *                a contents of the newly created <code>AttributeSet</code>.
     *
     * @return The new immutable <code>AttributeSet</code>.
     */
    public static AttributeSet createImmutable(AttributeSet... sets) {
        HashMap<Object, Object> map = new HashMap<Object, Object>();
        
        for(int i = sets.length - 1; i >= 0; i--) {
            AttributeSet set = sets[i];
            for(Enumeration<?> keys = set.getAttributeNames(); keys.hasMoreElements(); ) {
                Object attrKey = keys.nextElement();
                Object attrValue = set.getAttribute(attrKey);

                map.put(attrKey, attrValue);
            }
        }
        
        return new Immutable(map);
    }

    /**
     * Creates a proxy <code>AttributeSet</code> that will delegate to the
     * <code>AttributeSet</code>s passed in as a parameter. If the <code>AttributeSet</code>s
     * contain attributes with the same name the composite <code>AttributeSet</code>
     * will return value of the first attribute it will find going through
     * the sets in the order as they were passed in.
     *
     * @param sets    The <code>AttributeSet</code>s to delegate to.
     *
     * @return The new composite <code>AttributeSet</code> that will delegate
     *         to the <code>sets</code> passed in.
     */
    public static AttributeSet createComposite(AttributeSet... sets) {
        if (sets.length == 0) {
            return SimpleAttributeSet.EMPTY;
        } else if (sets.length == 1) {
            return sets[0];
        } else {
            ArrayList<AttributeSet> all = new ArrayList<AttributeSet>();

            for(AttributeSet s : sets) {
                if (s instanceof AttributesUtilities.Composite) {
                    all.addAll(((AttributesUtilities.Composite) s).getDelegates());
                } else if (s instanceof AttributesUtilities.Proxy) {
                    all.add(((AttributesUtilities.Proxy) s).getDelegate());
                } else if (s != null && s != SimpleAttributeSet.EMPTY) {
                    all.add(s);
                }
            }

            if (all.size() == 0) {
                return SimpleAttributeSet.EMPTY;
            } else {
                return new Composite(all.toArray(new AttributeSet[all.size()]));
            }
        }
    }

    private static List<AttributeSet> dismantle(AttributeSet set) {
        ArrayList<AttributeSet> sets = new ArrayList<AttributeSet>();
        
        if (set instanceof Proxy) {
            sets.addAll(dismantle(((Proxy) set).getDelegate()));
        } else if (set instanceof Composite) {
            List<AttributeSet> delegates = ((Composite) set).getDelegates();
            for(AttributeSet delegate : delegates) {
                sets.addAll(dismantle(delegate));
            }
        } else {
            sets.add(set);
        }
        
        return sets;
    }
    
    // ----------------------------------------------------------------------
    //  Private implementation
    // ----------------------------------------------------------------------

    private AttributesUtilities() {
        // no-op, just to prevent instantiation
    }
    
    private static class Immutable implements AttributeSet {
        
        private final HashMap<Object, Object> attribs;
        private AttributeSet parent = null;

        /** Creates a new instance of SmartAttributeSet */
        private Immutable(HashMap<Object, Object> attribs) {
            this.attribs = attribs == null ? new HashMap<Object, Object>() : attribs;
        }

        public synchronized void setResolveParent(AttributeSet parent) {
            this.parent = parent;
        }

        public synchronized boolean containsAttributes(AttributeSet attributes) {
            for(Enumeration names = attributes.getAttributeNames(); names.hasMoreElements(); ) {
                Object name = names.nextElement();
                Object value = attributes.getAttribute(name);

                if (!containsAttribute(name, value)) {
                    return false;
                }
            }

            return true;
        }

        public synchronized boolean isEqual(AttributeSet attr) {
            return containsAttributes(attr) && attr.containsAttributes(this);
        }

        public synchronized Object getAttribute(Object key) {

            // Somebody is asking for the parent
            if (AttributeSet.ResolveAttribute == key) {
                return parent;
            }

            // Get the normal value
            if (attribs.containsKey(key)) {
                return attribs.get(key);
            }

            // Value not found, try parent if we have any
            if (parent != null) {
                return parent.getAttribute(key);
            } else {
                return null;
            }
        }

        public synchronized boolean isDefined(Object key) {
            return attribs.containsKey(key);
        }

        public synchronized boolean containsAttribute(Object key, Object value) {
            if (attribs.containsKey(key)) {
                Object attrValue = attribs.get(key);
                if ((value == null && attrValue == null) || 
                    (value != null && attrValue != null && value.equals(attrValue))
                ) {
                    return true;
                }
            }

            return false;
        }

        public AttributeSet copyAttributes() {
            return new Proxy(this);
        }

        /**
         * This is really slow don't use it!
         */
        public synchronized int getAttributeCount() {
            return attribs.size();
        }

        /**
         * This is really slow don't use it!
         */
        public synchronized Enumeration<?> getAttributeNames() {
            return Collections.enumeration(attribs.keySet());
        }

        public synchronized AttributeSet getResolveParent() {
            return parent;
        }

    } // End of Immutable class
    
    private static final class Proxy implements AttributeSet {
        
        private AttributeSet original;
        
        public Proxy(AttributeSet original) {
            this.original = original;
        }

        public AttributeSet getDelegate() {
            return original;
        }
        
        public boolean isEqual(AttributeSet attr) {
            return original.isEqual(attr);
        }

        public boolean containsAttributes(AttributeSet attributes) {
            return original.containsAttributes(attributes);
        }

        public boolean isDefined(Object attrName) {
            return original.isDefined(attrName);
        }

        public Object getAttribute(Object key) {
            if (key instanceof String && key.equals(ATTR_DISMANTLED_STRUCTURE)) {
                return dismantle(this);
            } else {
                return original.getAttribute(key);
            }
        }

        public AttributeSet getResolveParent() {
            return original.getResolveParent();
        }

        public Enumeration<?> getAttributeNames() {
            return original.getAttributeNames();
        }

        public int getAttributeCount() {
            return original.getAttributeCount();
        }

        public AttributeSet copyAttributes() {
            return original.copyAttributes();
        }

        public boolean containsAttribute(Object name, Object value) {
            return original.containsAttribute(name, value);
        }
    } // End of Proxy class

    private static final class Composite implements AttributeSet {
        
        private final AttributeSet [] delegates;
        
        public Composite(AttributeSet... delegates) {
            this.delegates = delegates;
        }

        public List<AttributeSet> getDelegates() {
            return Arrays.asList(delegates);
        }
        
        public boolean isEqual(AttributeSet attr) {
            return containsAttributes(attr) && attr.containsAttributes(this);
        }

        public boolean containsAttributes(AttributeSet attributes) {
            for(Enumeration<?> keys = attributes.getAttributeNames(); keys.hasMoreElements(); ) {
                Object key = keys.nextElement();
                Object value = attributes.getAttribute(key);
                
                if (!containsAttribute(key, value)) {
                    return false;
                }
            }
            
            return true;
        }

        public boolean isDefined(Object key) {
            for(AttributeSet delegate : delegates) {
                if (delegate.isDefined(key)) {
                    return true;
                }
            }
            
            return false;
        }

        public Object getAttribute(Object key) {
            if (key instanceof String && key.equals(ATTR_DISMANTLED_STRUCTURE)) {
                return dismantle(this);
            }
            
            for(AttributeSet delegate : delegates) {
                if (delegate.isDefined(key)) {
                    return delegate.getAttribute(key);
                }
            }
            
            return null;
        }

        public AttributeSet getResolveParent() {
            return null;
        }

        public Enumeration<?> getAttributeNames() {
            return Collections.enumeration(getAllKeys());
        }

        public int getAttributeCount() {
            return getAllKeys().size();
        }

        public AttributeSet copyAttributes() {
            return createImmutable(delegates);
        }

        public boolean containsAttribute(Object key, Object value) {
            for(AttributeSet delegate : delegates) {
                if (delegate.containsAttribute(key, value)) {
                    return true;
                }
            }
            
            return false;
        }
        
        private Collection<?> getAllKeys() {
            HashSet<Object> allKeys = new HashSet<Object>();
            
            for(AttributeSet delegate : delegates) {
                for(Enumeration<?> keys = delegate.getAttributeNames(); keys.hasMoreElements(); ) {
                    Object key = keys.nextElement();
                    allKeys.add(key);
                }
            }
            
            return allKeys;
        }
    } // End of Composite class
}
