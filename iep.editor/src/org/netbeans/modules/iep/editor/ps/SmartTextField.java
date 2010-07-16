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

import java.awt.Cursor;
import java.awt.dnd.*;
import java.util.ArrayList;
import java.util.logging.Logger;
import javax.swing.JTextField;

import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

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
//            //if the dragging over an existing text
//            //then we do not allow drop to happen unless previous position
//            //is a comma or start of the text (0 position) or at then end of 
//            //existing text
//            //
//            
//            String existingText = getText();
//            if(existingText != null) {
//                if(pos > 0 && pos < existingText.length()) {
//                    //get char before pos and see if it is comma
//                    int previousCommaPos = findPreviousCommaPosition(pos);
//                    int nextCommaPos = findNextCommaPosition(pos);
//                    if(previousCommaPos != -1 && nextCommaPos == -1) {
//                        e.acceptDrag(mAcceptableActions);
//                    } else {
//                        e.rejectDrag();
//                        
//                    }
//                }
//            } else {
//                e.acceptDrag(mAcceptableActions);
//            }
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
                    
                    StringBuffer textToInsert = new StringBuffer();
                    //if there is an existing string
                    //then we append comma before inserting this text
                    String existingText = getText();
                    if(existingText != null && !existingText.trim().equals("")) {
                        if(pos > 0 && pos < existingText.length()) {
                            int previousCommaPos = findPreviousCommaPosition(pos);
                            int nextCommaPos = findNextCommaPosition(pos);
                            
                            if(previousCommaPos != -1) {
                                textToInsert.append(msg);
                                if(nextCommaPos == -1) {
                                    textToInsert.append(",");
                                }
                            }else if(nextCommaPos != -1) {
                                
                                if(previousCommaPos == -1) {
                                    textToInsert.append(",");
                                }
                                
                                textToInsert.append(msg);
                            } else {
                                NotifyDescriptor nd = new NotifyDescriptor.Message(NbBundle.getMessage(SmartTextField.class, "SmartTextField_WrongDropLocation"), NotifyDescriptor.INFORMATION_MESSAGE);
                                DialogDisplayer.getDefault().notify(nd);
                                e.rejectDrop();
                            }
                            
                        } else if (pos == 0){
                            textToInsert.append(msg);
                            textToInsert.append(",");
                        } else {
                            textToInsert.append(",");
                            textToInsert.append(msg);
                        }
                            
                        
                        
                        
                        
                    } else {
                        textToInsert.append(msg);
                    }
                    getDocument().insertString(pos, textToInsert.toString(), null);
                    SmartTextField.this.setCaretPosition(pos + textToInsert.toString().length());
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
        
        private int findPreviousCommaPosition(int pos) {
//          get char before pos (exclude white space) and see if it is comma
            int previousCommaPos = pos -1;
            String existingText = getText();
            
            while(previousCommaPos != 0) {
                char prevChar =existingText.charAt(previousCommaPos);
                if(prevChar == ',') {
                    break;
                } else if (prevChar == ' ') {
                    previousCommaPos = previousCommaPos -1;
                } else {
                    previousCommaPos = -1;
                    break;
                }
            }
            
            return previousCommaPos;
        }
        
        private int findNextCommaPosition(int pos) {
//          get char after pos (exclude white space) and see if it is comma
            int nextCommaPos = pos;
            String existingText = getText();
            
            while(nextCommaPos != 0) {
                char prevChar =existingText.charAt(nextCommaPos);
                if(prevChar == ',') {
                     break;
                } else if (prevChar == ' ') {
                    nextCommaPos = nextCommaPos + 1;
                } else {
                    nextCommaPos = -1;
                    break;
                }
            }
            
            return nextCommaPos;
        }
    }
    
}