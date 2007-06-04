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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.css.model;

/**
 * An immutable representation of a css rule item eg.:
 * 
 * color: red;
 *
 * @author Marek Fukala
 */
public final class CssRuleItem {

    private Item key, value;
    
    public CssRuleItem(String key, int keyOffset, String val, int valOffset) {
        this.key = new Item(key, keyOffset);
        this.value = new Item(val, valOffset);
    }
    
    /** @return representation of the key of the rule item. */
    public Item key() {
        return key;
    }

    /** @return representation of the value of the rule item. */
    public Item value() {
        return value;
    }
    
    public String toString() {
        return "CssRuleItem[" + key + "; " + value + "]";
    }
    
    /** A representation of the key or value of the rule item. 
     * Contains information about the item position in the model's document
     * and its string value.
     */
    public final class Item {
        
        private String name;
        private int offset;
        
        private Item(String name, int offset) {
            this.name = name;
            this.offset = offset;
        }
        
        /** @return text content of the attribute's item. */
        public String name() {
            return name;
        }
        
        /** @return offset in the model's document of the attribute's item. */
        public int offset() {
            return offset;
        }
    
        public String toString() {
            return "Item[" + name + "; " + offset + "]";
        }
        
    }
    
    
    
}
