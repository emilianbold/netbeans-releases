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

package org.netbeans.modules.soa.mapper.basicmapper.literal;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Window;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JComboBox;

import org.netbeans.modules.soa.mapper.common.basicmapper.IBasicMapper;
import org.netbeans.modules.soa.mapper.common.basicmapper.literal.ILiteralUpdater;
import org.netbeans.modules.soa.mapper.common.basicmapper.methoid.IFieldNode;


/**
 * Represents a true-or-false combo box styled boolean literal editor. 
 * 
 * @author Josh Sandusky
 */
public class BasicBooleanLiteralEditor extends AbstractLiteralEditor {

    private JComboBox mEditorComponent;

    public BasicBooleanLiteralEditor(Window owner,
                                     IBasicMapper basicMapper, 
                                     IFieldNode fieldNode,
                                     ILiteralUpdater updateListener) {
        super(owner, basicMapper, fieldNode, updateListener);
        
        Boolean fieldValue = new Boolean(fieldNode.getLiteralName());
        
        mEditorComponent = new JComboBox(new Object[] {Boolean.TRUE, Boolean.FALSE});
        mEditorComponent.setSelectedItem(fieldValue);
        Box box = Box.createHorizontalBox();
        Component c = Box.createHorizontalGlue();
        box.add(c);
        box.add(mEditorComponent);
        c = Box.createHorizontalGlue();
        box.add(Box.createHorizontalGlue());
        
        initializeLiteralComponent(basicMapper.getMapperViewManager().getCanvasView(), 
                                   box, 
                                   mEditorComponent);
        
        int fontHeight = 10;
        Rectangle rect = mCanvasFieldNode.getBounding();
        int inset = ((rect.height - (fontHeight+4))/2);
        box.setBorder(BorderFactory.createEmptyBorder(inset, inset, inset, inset));
        getContentPane().setBackground(Color.white);
        getContentPane().add(box);
    }

    protected Dimension getInitialSize() {
        Dimension dim = mCanvasFieldNode.getBounding().getSize();
        dim.width -= 4;
        dim.height -= 1;
        return dim;
    }

    protected String updateLiteral() {
        Boolean selectionState = (Boolean) mEditorComponent.getSelectedItem();
        return selectionState.toString();
    }
}
