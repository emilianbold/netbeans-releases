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

package org.netbeans.modules.soa.mappercore.vertexitemeditor;

import java.awt.Component;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import javax.swing.tree.TreePath;
import org.netbeans.modules.soa.mappercore.Mapper;
import org.netbeans.modules.soa.mappercore.model.VertexItem;
import org.netbeans.modules.soa.mappercore.utils.MetalTextFieldBorder;

/**
 *
 * @author anjeleevich
 */
public class NumberVertexItemEditor extends 
        AbstractTextVertexItemEditor 
{
    
    public NumberVertexItemEditor() {
        MetalTextFieldBorder.installIfItIsNeeded(this);
        setHorizontalAlignment(RIGHT);
        ((AbstractDocument) getDocument()).setDocumentFilter(
                new NumberDocumentFilter());
    }
    

    public Component getVertexItemEditorComponent(Mapper mapper, 
            TreePath treePath, VertexItem vertexItem) 
    {
        Number number = toNumber(vertexItem.getValue());
        setText((number == null) ? "" : number.toString());
        return this;
    }

    
    public Object getVertexItemEditorValue() {
        return toNumber(getText());
    }
    
    
    private static class NumberDocumentFilter extends DocumentFilter {
        public void replace(DocumentFilter.FilterBypass fb, int offset, 
                int length, String text, AttributeSet attrs) 
                throws BadLocationException 
        {
            text = filterString(text);
            super.replace(fb, offset, length, text, attrs);
        }

        public void insertString(DocumentFilter.FilterBypass fb, int offset, 
                String string, AttributeSet attr) throws BadLocationException 
        {
            string = filterString(string);
            super.insertString(fb, offset, string, attr);
        }

    }

    
    private static String filterString(String string) {
        StringBuilder builder = new StringBuilder();
        
        if (string != null) {
            for (int i = 0; i < string.length(); i++) {
                char c = string.charAt(i);
                if (FILTER_CHARS.indexOf(c) >= 0) {
                    builder.append(c);
                }
            }
            string = builder.toString();
        }
        
        return string;
    }

    
    private static Number toNumber(Object value) {
        Number number = null;
        
        if (value instanceof Number) {
            number = (Number) value;
        } else if (value != null) {
            String text = value.toString();
            
            if (text != null) {
                try {
                    number = Long.parseLong(text);
                } catch (NumberFormatException ex) {}

                if (number == null) {
                    try {
                        number = Double.parseDouble(text);
                    } catch (NumberFormatException ex) {} 
                }
            }
        }
        
        return number;
    }
    
    
    private static final String FILTER_CHARS = "0123456789+-eE.";
}
