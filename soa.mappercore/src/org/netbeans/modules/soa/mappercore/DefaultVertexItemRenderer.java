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
        String text = null;
        //
        // Indicates if the VertexItem's value is going to be shown
        boolean valuePrepared = false; 
        //
        if (value != null) {
            text = value.toString();
            if (text != null) {
                Class type = vertexItem.getValueType();
                if (Utils.equal(type, Number.class)) {
                    prepareNumberRenderer();
                } else {
                    if (Utils.equal(type, String.class)) {
                        text = "\'" + text + "\'"; // NOI18N
                    }
                    prepareTextRenderer();
                }
                valuePrepared = true;
            }
        } 
        //
        if (!valuePrepared) {
            // value == null here
            prepareCommentRenderer();
            //
            String shortDescr = vertexItem.getShortDescription();
            if (shortDescr != null && shortDescr.length() != 0) {
                text = shortDescr;
            } else {
                Class type = vertexItem.getValueType();
                if (type != null) {
                    if (Utils.equal(type, Number.class)) {
                        text = "Number";
                    } else if (Utils.equal(type, String.class)) {
                        text = "String";
                    } else if (type != null) {
                        text = type.getCanonicalName();
                    }
                }
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
 
