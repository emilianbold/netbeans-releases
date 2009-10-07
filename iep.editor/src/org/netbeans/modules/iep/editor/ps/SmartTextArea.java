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


package org.netbeans.modules.iep.editor.ps;

import java.awt.dnd.*;
import java.util.ArrayList;
import javax.swing.JTextArea;
import javax.swing.event.CaretListener;

/**
 * SmartTextArea.java
 *
 * Created on November 1, 2006, 1:52 PM
 *
 * @author Bing Lu
 */
public class SmartTextArea extends JTextArea {
    private static int mAcceptableActions = DnDConstants.ACTION_COPY;
    private DropTarget mDropTarget;
    
    
    public SmartTextArea(int rows, int columns) {
        super(rows, columns);
        mDropTarget = new DropTarget(this, new MyDropTargetAdapter());
    }
    
    public SmartTextArea() {
        this(0, 0);
    }
    
    public void addAttributeDropNotificationListener(AttributeDropNotificationListener l) {
        this.listenerList.add(AttributeDropNotificationListener.class, l);
    }
    
    public void removeAttributeDropNotificationListener(AttributeDropNotificationListener l) {
        this.listenerList.remove(AttributeDropNotificationListener.class, l);
    }
    
    class MyDropTargetAdapter extends DropTargetAdapter {
        public void dragEnter(DropTargetDragEvent e) {
            if(!isDragAcceptable(e)) {
                e.rejectDrag();
                return;
            } 
            SmartTextArea.this.grabFocus();
            int pos = viewToModel(e.getLocation());
            SmartTextArea.this.setCaretPosition(pos);
            e.acceptDrag(mAcceptableActions);
        }
        
        public void dragOver(DropTargetDragEvent e) {
            if(!isDragAcceptable(e)) {
                e.rejectDrag();
                return;
            } 
            int pos = viewToModel(e.getLocation());
            SmartTextArea.this.setCaretPosition(pos);
            e.acceptDrag(mAcceptableActions);
        }
        
        public void drop(DropTargetDropEvent e) {
            ArrayList data = null;
            try {
                data = (ArrayList)e.getTransferable().getTransferData(AttributeInfoDataFlavor.ATTRIBUTE_INFO_FLAVOR);
                Object obj = data.get(0);
                String msg = "";
                if(obj instanceof AttributeInfo) {
                    AttributeInfo ai = (AttributeInfo)obj;
                    msg = ai.getEntityAndColumnName();
                    
                    //if there is a handler then notify it
                    AttributeDropNotificationEvent evt = new AttributeDropNotificationEvent(ai);
                    fireOnDropComplete(evt);
                
                } else {
                    if(obj instanceof String) {
                        msg = (String)obj;
                    }
                }
                if(!msg.equals("")) {
                    int pos = viewToModel(e.getLocation());
                    getDocument().insertString(pos, msg, null);
                    SmartTextArea.this.setCaretPosition(pos + msg.length());
                    e.acceptDrop(mAcceptableActions);
                    e.dropComplete(true);
                } else {
                    e.rejectDrop();
                }
            } catch(Exception ex) {
                ex.printStackTrace();
                return;
            }
        }
        
        public void dropActionChanged(DropTargetDragEvent e) {
            if(!isDragAcceptable(e)) {
                e.rejectDrag();
                return;
            } 
            e.acceptDrag(mAcceptableActions);
        }
        
        private boolean isDragAcceptable(DropTargetDragEvent e) {
            return e.isDataFlavorSupported(AttributeInfoDataFlavor.ATTRIBUTE_INFO_FLAVOR);
        }
    }
    
    private void fireOnDropComplete(AttributeDropNotificationEvent evt) {
        Object[] listeners = listenerList.getListenerList();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length-2; i>=0; i-=2) {
            if (listeners[i]==AttributeDropNotificationListener.class) {
                ((AttributeDropNotificationListener)listeners[i+1]).onDropComplete(evt);
            }
        }
    }
}
