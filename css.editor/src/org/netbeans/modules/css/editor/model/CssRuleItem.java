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

package org.netbeans.modules.css.editor.model;

/**
 * An immutable representation of a css rule item eg.:
 *
 * color: red;
 *
 * @author Marek Fukala
 */
public final class CssRuleItem {

    public Item key;
    public Item value;

    //offset of colon key-value separator and ending semicolon of the item
    private int colon_offset;
    private int semicolon_offset;

    public CssRuleItem(String key, String value) {
        this(key, -1, value, -1, -1, -1);
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

    @Override
    public String toString() {
        return "CssRuleItem[" + key + "; " + value + "]"; //NOI18N
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


/** A representation of the key or value of the rule item.
     * Contains information about the item position in the model's document
     * and its string value.
     */
    public static final class Item {

        private String name;
        private int offset;

        public Item(String name) {
            this(name, -1);
        }

        Item(String name, int offset)  {
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

        @Override
        public String toString() {
            return "Item[" + name + "; " + offset + "]"; //NOI18N
        }

    }
}
