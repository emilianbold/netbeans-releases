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
import java.util.logging.Logger;
import javax.swing.JTextField;
import javax.swing.text.Document;
import javax.swing.text.Position;

/**
 * SmartTextField.java
 *
 * Created on November 1, 2006, 1:52 PM
 *
 * @author Bing Lu
 */
public class SmartTextField extends JTextField {
    private static final Logger mLog = Logger.getLogger(SmartTextField.class.getName());

    private static int mAcceptableActions = DnDConstants.ACTION_COPY;

    private boolean mTruncateColumn;
    private DropTarget mDropTarget;
    
    private AttributeDropNotificationListener mHandler;
    
    public SmartTextField(boolean truncateColumn) {
        mTruncateColumn = truncateColumn;
        mDropTarget = new DropTarget(this, new MyDropTargetAdapter());
    }
    
    public SmartTextField(boolean truncateColumn, AttributeDropNotificationListener handler) {
        this(truncateColumn);
        this.mHandler = handler;
    }
    
    class MyDropTargetAdapter extends DropTargetAdapter {
        public void dragEnter(DropTargetDragEvent e) {
            if(!isDragAcceptable(e)) {
                e.rejectDrag();
                return;
            }
            SmartTextField.this.grabFocus();
            int pos = viewToModel(e.getLocation());
            SmartTextField.this.setCaretPosition(pos);
            e.acceptDrag(mAcceptableActions);
        }
        
        public void dragOver(DropTargetDragEvent e) {
            if(!isDragAcceptable(e)) {
                e.rejectDrag();
                return;
            } 
            int pos = viewToModel(e.getLocation());
            SmartTextField.this.setCaretPosition(pos);
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
                    msg = mTruncateColumn? ai.getEntityName() : ai.getEntityAndColumnName();
                     //if there is a handler then notify it
                    if(mHandler != null) {
                        AttributeDropNotificationEvent evt = new AttributeDropNotificationEvent(ai);
                        mHandler.onDropComplete(evt);
                    }
                } else {
                    if(obj instanceof String) {
                        msg = (String)obj;
                    }
                } 
                if(!msg.equals("")) {
                    int pos = viewToModel(e.getLocation());
                    getDocument().insertString(pos, msg, null);
                    SmartTextField.this.setCaretPosition(pos + msg.length());
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
    
}
