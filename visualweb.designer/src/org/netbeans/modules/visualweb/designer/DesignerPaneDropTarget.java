/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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

package org.netbeans.modules.visualweb.designer;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.util.TooManyListenersException;
import javax.swing.JComponent;
import javax.swing.TransferHandler;
import javax.swing.event.EventListenerList;

/**
 * XXX #101880 In order to avoid rescheduling the drop. The default Swing impl, first
 * processes the default listener (which handles the TransferHandler), and only
 * after notifies the other listeners, which causes an issue when doing drop.
 * We first need to notify the other listeners in order to provide the update
 * of the drop position.
 * 
 * XXX Be aware that in the other listeners one can't call accept/rejectDrag(Drop) otherwise it will breaks.
 * 
 *
 * @author Peter Zavadsky
 */
public class DesignerPaneDropTarget extends DropTarget {

    
private static final DropTargetListener DEFAULT_TRANSFER_HANDLER_DROP_TARGET_LISTENER = new DefaultTransferHandlerDropTargetListener();
    
    /** Hold the 'secondary' listeners. */
    private EventListenerList listenerList;
    
    
    public DesignerPaneDropTarget(DesignerPane designerPane) {
        super();
        setComponent(designerPane);
        try {
            super.addDropTargetListener(DEFAULT_TRANSFER_HANDLER_DROP_TARGET_LISTENER);
        } catch (TooManyListenersException tmle) {
            // No op.
        }
    }

    public void addDropTargetListener(DropTargetListener dtl) throws TooManyListenersException {
        // Since the super class only supports one DropTargetListener,
        // and we add one from the constructor, we always add to the
        // extended list.
        if (listenerList == null) {
            listenerList = new EventListenerList();
        }
        listenerList.add(DropTargetListener.class, dtl);
    }

    public void removeDropTargetListener(DropTargetListener dtl) {
        if (listenerList != null) {
            listenerList.remove(DropTargetListener.class, dtl);
        }
    }

    // --- DropTargetListener methods (multicast) --------------------------

    public void dragEnter(DropTargetDragEvent e) {
        super.dragEnter(e);
        if (listenerList != null) {
            Object[] listeners = listenerList.getListenerList();
            for (int i = listeners.length-2; i>=0; i-=2) {
                if (listeners[i]==DropTargetListener.class) {
                    ((DropTargetListener)listeners[i+1]).dragEnter(e);
                }	       
            }
        }
    }

    public void dragOver(DropTargetDragEvent e) {
        super.dragOver(e);
        if (listenerList != null) {
            Object[] listeners = listenerList.getListenerList();
            for (int i = listeners.length-2; i>=0; i-=2) {
                if (listeners[i]==DropTargetListener.class) {
                    ((DropTargetListener)listeners[i+1]).dragOver(e);
                }	       
            }
        }
    }

    public void dragExit(DropTargetEvent e) {
        super.dragExit(e);
        if (listenerList != null) {
            Object[] listeners = listenerList.getListenerList();
            for (int i = listeners.length-2; i>=0; i-=2) {
                if (listeners[i]==DropTargetListener.class) {
                    ((DropTargetListener)listeners[i+1]).dragExit(e);
                }	       
            }
        }
    }

    public void drop(DropTargetDropEvent e) {
//        super.drop(e);
        if (listenerList != null) {
            Object[] listeners = listenerList.getListenerList();
            for (int i = listeners.length-2; i>=0; i-=2) {
                if (listeners[i]==DropTargetListener.class) {
                    ((DropTargetListener)listeners[i+1]).drop(e);
                }	       
            }
        }
        // XXX The default is proceeded after the other has a chance to update the position.
        super.drop(e);
    }

    public void dropActionChanged(DropTargetDragEvent e) {
        super.dropActionChanged(e);
        if (listenerList != null) {
            Object[] listeners = listenerList.getListenerList();
            for (int i = listeners.length-2; i>=0; i-=2) {
                if (listeners[i]==DropTargetListener.class) {
                    ((DropTargetListener)listeners[i+1]).dropActionChanged(e);
                }	       
            }
        }
    }

    
    // XXX Copied from TransferHandler.DropHandler
    // XXX Does it need to be Serializable?
    private static class DefaultTransferHandlerDropTargetListener implements DropTargetListener /*, Serializable*/ {
        
        private boolean canImport;
        
        private boolean actionSupported(int action) {
//            return (action & (COPY_OR_MOVE | LINK)) != NONE;
            return (action & (DnDConstants.ACTION_COPY_OR_MOVE | DnDConstants.ACTION_LINK)) != DnDConstants.ACTION_NONE;
        }

	// --- DropTargetListener methods -----------------------------------

        public void dragEnter(DropTargetDragEvent e) {
	    DataFlavor[] flavors = e.getCurrentDataFlavors();

	    JComponent c = (JComponent)e.getDropTargetContext().getComponent();
	    TransferHandler importer = c.getTransferHandler();
            
//            if (importer != null && importer.canImport(c, flavors)) {
            // XXX #99457 Internally enhanced TransferHandler to use also Transferable to compare.
            boolean canImporterImport;
            if (importer instanceof DesignerTransferHandler) {
                canImporterImport = ((DesignerTransferHandler)importer).canImport(c, flavors, e.getTransferable());
            } else {
                canImporterImport = importer == null ? false : importer.canImport(c, flavors);
            }
            if (canImporterImport) {
                canImport = true;
            } else {
                canImport = false;
            }
            
            int dropAction = e.getDropAction();
            
            if (canImport && actionSupported(dropAction)) {
		e.acceptDrag(dropAction);
	    } else {
		e.rejectDrag();
	    }
	}

        public void dragOver(DropTargetDragEvent e) {
            int dropAction = e.getDropAction();
            
            if (canImport && actionSupported(dropAction)) {
                e.acceptDrag(dropAction);
            } else {
                e.rejectDrag();
            }
	}

        public void dragExit(DropTargetEvent e) {
	}

        public void drop(DropTargetDropEvent e) {
            int dropAction = e.getDropAction();

            JComponent c = (JComponent)e.getDropTargetContext().getComponent();
            TransferHandler importer = c.getTransferHandler();

	    if (canImport && importer != null && actionSupported(dropAction)) {
		e.acceptDrop(dropAction);
                
                try {
                    Transferable t = e.getTransferable();
		    e.dropComplete(importer.importData(c, t));
                } catch (RuntimeException re) {
                    e.dropComplete(false);
                }
	    } else {
		e.rejectDrop();
	    }
	}

        public void dropActionChanged(DropTargetDragEvent e) {
            int dropAction = e.getDropAction();
            
            if (canImport && actionSupported(dropAction)) {
                e.acceptDrag(dropAction);
            } else {
                e.rejectDrag();
            }
	}
    }
}
