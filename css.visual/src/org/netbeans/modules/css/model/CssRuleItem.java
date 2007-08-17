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

    Item key;
    Item value;

    //offset of colon key-value separator and ending semicolon of the item
    private int colon_offset;
    private int semicolon_offset;

    public CssRuleItem(String key, int keyOffset, String val, int valOffset) {
        this(key, keyOffset, val, valOffset, -1, -1);
    }

    CssRuleItem(String key, int keyOffset, String val, int valOffset, int colon_offset, int semicolon_offset) {
        this.key = new Item(key, keyOffset);
        this.value = new Item(val, valOffset);
        this.colon_offset = colon_offset;
        this.semicolon_offset = semicolon_offset;
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

    /** Gets offset of the key - value separator in the css rule item.
     */
    public int colonOffset() {
        return colon_offset;
    }

    /** Gets offset of the ending semicolon in rule item or -1 if there is no ending semicolon.
     */
    public int semicolonOffset() {
        return semicolon_offset;
    }

    public boolean equals(Object o) {
        if (o instanceof CssRuleItem) {
            CssRuleItem ori = (CssRuleItem) o;
            return key().equals(ori.key()) 
                    && value().equals(ori.value())
                    && colonOffset() == ori.colonOffset() 
                    && semicolonOffset() == ori.semicolonOffset();
        }
        return false;
    }

/** A representation of the key or value of the rule item.
     * Contains information about the item position in the model's document
     * and its string value.
     */
    public static final class Item {

        private String name;
        private int offset;

        Item(String name, int offset) {
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

        public boolean equals(Object o) {
            if (o instanceof Item) {
                Item oi = (Item) o;
                return name().equals(oi.name()) && offset() == oi.offset();
            }
            return false;
        }
    }
}
