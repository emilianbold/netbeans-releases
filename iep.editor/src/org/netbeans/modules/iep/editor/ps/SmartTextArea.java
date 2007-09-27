/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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


package org.netbeans.modules.iep.editor.ps;

import java.awt.dnd.*;
import java.util.ArrayList;
import javax.swing.JTextArea;

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
    
}