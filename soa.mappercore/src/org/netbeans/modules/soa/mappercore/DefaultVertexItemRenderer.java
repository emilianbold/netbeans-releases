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

package org.netbeans.modules.soa.mappercore;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import javax.swing.JLabel;
import javax.swing.tree.TreePath;
import org.netbeans.modules.soa.mappercore.model.VertexItem;
import org.netbeans.modules.soa.mappercore.utils.Utils;

/**
 *
 * @author anjeleevich
 */
public class DefaultVertexItemRenderer extends JLabel 
        implements VertexItemRenderer 
{
    public DefaultVertexItemRenderer() {
        setBorder(null);
    }

    public Component getVertexItemRendererComponent(Mapper mapper, 
            TreePath treePath, VertexItem vertexItem) 
    {
        setFont(getFont().deriveFont(Font.PLAIN));

        Object value = vertexItem.getValue();
        Class type = vertexItem.getValueType();
        
        String text = null;
        
        if (type == null) {
            prepareCommentRenderer();
            text = (value == null) ? null : value.toString();
        } else if (value == null) {
            prepareCommentRenderer();
            if (Utils.equal(type, Number.class)) {
                text = "Number";
            } else if (Utils.equal(type, String.class)) {
                text = "String";
            } else if (type != null) {
                text = type.getCanonicalName();
            }
        } else {
            text = value.toString();
            if (Utils.equal(type, Number.class)) {
                prepareNumberRenderer();
            } else {
                prepareTextRenderer();
            }
        }
        
        setText((text == null) ? "" : text);
        
        return this;
    }
    
    
    protected void prepareCommentRenderer() {
        setHorizontalAlignment(LEFT);
        setForeground(Color.GRAY);
    }

    protected void prepareTextRenderer() {
        setHorizontalAlignment(LEFT);
        setForeground(Color.BLACK);
    }
    
    
    protected void prepareNumberRenderer() {
        setHorizontalAlignment(RIGHT);
        setForeground(Color.BLACK);
    }
    
    
    protected void prepareBooleanRenderer() {
        setHorizontalAlignment(LEFT);
    }
}
 