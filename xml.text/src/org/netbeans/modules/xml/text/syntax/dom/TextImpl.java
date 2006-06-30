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

package org.netbeans.modules.xml.text.syntax.dom;

import org.w3c.dom.*;
import org.netbeans.modules.xml.text.syntax.*;
import org.netbeans.modules.xml.spi.dom.*;
import org.netbeans.editor.*;

/**
 * DOM Text implementation. Note that it is automatically
 * coalesced with <code>Text</code> siblings.
 * <p>
 * The implementation handles differently content and attribute
 * text nodes because there is no syntax element for attribute.
 *
 * @author Petr Kuzel
 */
public class TextImpl extends SyntaxNode implements Text {

    // if attribute text node then parent otherwise null
    private AttrImpl parent;
    
    /**
     * Create content text node.
     */
    public TextImpl(XMLSyntaxSupport support, TokenItem from, int to) {
        super( support, from, to );
    }
    
    /**
     * Create attribute text node.
     */
    TextImpl(XMLSyntaxSupport syntax, TokenItem from, AttrImpl parent) {
        super( syntax, from, 0);
        if (parent == null) throw new IllegalArgumentException();
        this.parent = parent;
    }
    
    /**
     * Get parent node. For content text nodes may be <code>null</code>
     */
    public Node getParentNode() {
        if (parent != null) {
            return parent;
        } else {
            return super.getParentNode();
        }
    }

    public Node getPreviousSibling() {
        if (parent == null) return super.getPreviousSibling();
        return parent.getPreviousSibling(this);
    }
    
    public Node getNextSibling() {
        if (parent == null) return super.getNextSibling();
        return parent.getNextSibling(this);
    }
    
    public short getNodeType() {
        return Node.TEXT_NODE;
    }
    
    public String getNodeValue() {
        return getData();
    }
        
    public Text splitText(int offset) {
        throw new ROException();
    }
 
    public String getData() {
        return first.getImage();
    }

    public void setData(String data) {
        throw new ROException();
    }
    
    public int getLength() {
        return getData().length();
    }
    
    public String substringData(int offset, int count) {
        return getData().substring(offset, offset + count + 1);
    }

    public void appendData(String arg) {
        throw new ROException();
    }
    
    public void insertData(int offset, String arg) {
        throw new ROException();
    }


    public void deleteData(int offset, int count) {
        throw new ROException();
    }                           

    public void replaceData(int offset, int count, String arg) {
        throw new ROException();
    }


    /**
     * Dump content of the nod efor debug purposes.
     */
    public String toString() {
        return "Text" + super.toString() + " value: '" + getNodeValue() + "'";
    }

    
}

