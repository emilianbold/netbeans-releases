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

import java.awt.Point;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.util.Iterator;

import org.netbeans.modules.soa.mapper.common.basicmapper.IBasicMapper;
import org.netbeans.modules.soa.mapper.common.basicmapper.IBasicMapperEvent;
import org.netbeans.modules.soa.mapper.common.basicmapper.IBasicMapperLiteralUpdateEventInfo;
import org.netbeans.modules.soa.mapper.common.basicmapper.canvas.gtk.ICanvasFieldNode;
import org.netbeans.modules.soa.mapper.common.basicmapper.canvas.gtk.ICanvasMethoidNode;
import org.netbeans.modules.soa.mapper.common.basicmapper.canvas.gtk.ICanvasView;
import org.netbeans.modules.soa.mapper.common.basicmapper.literal.ILiteralEditor;
import org.netbeans.modules.soa.mapper.common.basicmapper.literal.ILiteralUpdater;
import org.netbeans.modules.soa.mapper.common.basicmapper.methoid.IField;
import org.netbeans.modules.soa.mapper.common.basicmapper.methoid.IFieldNode;
import org.netbeans.modules.soa.mapper.common.basicmapper.methoid.IMethoidNode;
import org.netbeans.modules.soa.mapper.common.IMapperEvent;
import org.netbeans.modules.soa.mapper.common.IMapperLink;
import org.netbeans.modules.soa.mapper.common.IMapperListener;
import org.netbeans.modules.soa.mapper.common.gtk.ICanvasMouseData;
import org.netbeans.modules.soa.mapper.common.gtk.ICanvasMouseListener;


/**
 * Handles opening up literal editors upon double-clicking a
 * field node. 
 * 
 * @author Josh Sandusky
 */
public class BasicLiteralEditListener 
implements ICanvasMouseListener, IMapperListener {

    private IBasicMapper mBasicMapper;
    private ICanvasFieldNode lastHoverCanvasFieldNode;
    
    
    public BasicLiteralEditListener(IBasicMapper basicMapper) {
        mBasicMapper = basicMapper;
        mBasicMapper.addMapperListener(this);
    }
    

    // MOUSE LISTENER
    
    // Listen for when we need to pop-up the literal editor
    // when the user double-clicks on a field node.
    public boolean doMouseDblClick(ICanvasMouseData event) {
        int mods = event.getMouseModifier();
        if ((mods & InputEvent.BUTTON1_MASK) == 0) {
            return false;
        }
        ICanvasView canvas = 
            mBasicMapper.getMapperViewManager().getCanvasView().getCanvas();
        Point point = event.getViewLocation();
        ICanvasMethoidNode canvasMethoidNode = 
            canvas.getCanvasMethoidNodeByPoint(point);
        if (canvasMethoidNode == null) {
            return false;
        }
        IMethoidNode methoidNode = canvasMethoidNode.getMethoidNode();
        if (methoidNode == null) {
            return false;
        }
        ICanvasFieldNode canvasFieldNode = 
            canvas.getCanvasFieldNodeByPoint(point);
        if (canvasFieldNode == null) {
            return false;
        }
        IFieldNode fieldNode = canvasFieldNode.getFieldNode();
        if (fieldNode == null) {
            return false;
        }
        IField field = (IField) fieldNode.getFieldObject();
        if (field == null) {
            return false;
        }
        ILiteralUpdater literalUpdater = field.getLiteralUpdater();
        if (literalUpdater == null) {
            return false;
        }
        
        ILiteralEditor editor = 
            literalUpdater.getEditor(mBasicMapper, fieldNode);
        if (editor != null) {
            editor.show();
        }
        
        return true;
    }

    // Listen for when we need to show that a field node
    // is highlighted when the user mouses over. Only field
    // nodes that can have in-place editable literals will
    // be highlighted.
    public boolean doMouseMove(ICanvasMouseData event) {
        int mods = event.getMouseModifier();
        if (mods != MouseEvent.NOBUTTON) {
            // We cannot set highlighting during a drag operation, or we
            // will get corrupted paint effects over our methoids.
            // Highlighting during a drag operation doesn't make sense either.
            
            // return false or bpel methoids won't be moveable
            return false;
        }
        
        ICanvasView canvas = 
            mBasicMapper.getMapperViewManager().getCanvasView().getCanvas();
        Point point = event.getViewLocation();
        
        ICanvasFieldNode editableCanvasFieldNode = null;
        ICanvasFieldNode canvasFieldNode = 
            canvas.getCanvasFieldNodeByPoint(point);
        if (canvasFieldNode != null) {
            IFieldNode fieldNode = canvasFieldNode.getFieldNode();
            if (fieldNode != null) {
                IField field = (IField) fieldNode.getFieldObject();
                if (field != null) {
                    ILiteralUpdater literalUpdater = field.getLiteralUpdater();
                    if (literalUpdater != null && literalUpdater.hasEditor()) {
                        editableCanvasFieldNode = canvasFieldNode;
                    }
                }
            }
        }
        
        if (
                lastHoverCanvasFieldNode != null && 
                lastHoverCanvasFieldNode != editableCanvasFieldNode) {
            lastHoverCanvasFieldNode.setHighlight(false);
            lastHoverCanvasFieldNode = null;
        }
        
        if (editableCanvasFieldNode != null) {
            editableCanvasFieldNode.setHighlight(true);
            lastHoverCanvasFieldNode = editableCanvasFieldNode;
        }
        
        // return false or bpel methoids won't be moveable
        return false;
    }
    
    public boolean doMouseDown(ICanvasMouseData event) {
        return false;
    }
    
    public boolean doMouseUp(ICanvasMouseData event) {
        return false;
    }
    
    public boolean doMouseClick(ICanvasMouseData event) {
        return false;
    }
    
    
    // MAPPER LISTENER
    
    public void eventInvoked(IMapperEvent e) {
        if (e.getEventType().equals(IBasicMapperEvent.FIELD_LITERAL_SET)) {
            IBasicMapperLiteralUpdateEventInfo info = 
                (IBasicMapperLiteralUpdateEventInfo) e.getTransferObject();
            ILiteralUpdater updater = info.getLiteralUpdater();
            IFieldNode fieldNode = info.getFieldNode();
            String newValueAfterSet = 
                updater.literalSet(fieldNode, info.getNewValue());
            if (newValueAfterSet != null) {
                fieldNode.setLiteralName(newValueAfterSet);
            }
            
            if (!info.isLiteralMethoid()) {
                // editor is on a methoid that is not a literal methoid, so
                // remove any currently connected input links last (after
                // we update the literal, so that the links can still be
                // traversed during the update).
                for (Iterator iter=fieldNode.getLinks().iterator(); iter.hasNext();) {
                    IMapperLink link = (IMapperLink) iter.next();
                    mBasicMapper.removeLink(link);
                }
            }
        }
    }
}
