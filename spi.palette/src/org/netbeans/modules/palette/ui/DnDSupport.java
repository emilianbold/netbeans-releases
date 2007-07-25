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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.palette.ui;

import java.awt.Component;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragGestureRecognizer;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceAdapter;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.dnd.InvalidDnDOperationException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import org.netbeans.modules.palette.Category;
import org.netbeans.modules.palette.Item;

/**
 * Drag and drop support for palette items and categories.
 *
 * @author S. Aubrecht
 */
public class DnDSupport  implements DragGestureListener, DropTargetListener {
    
    final static private int DELAY_TIME_FOR_EXPAND = 1000;

    private Set<DragGestureRecognizer> recognizers = new HashSet<DragGestureRecognizer>( 5 );
    private Set<DropTarget> dropTargets = new HashSet<DropTarget>( 5 );

    private Category draggingCategory;
    private Item draggingItem;
    private CategoryList dragSourceCategoryList;
    private Item targetItem;
    
    private boolean dropBefore;
    
    private DragSourceListener dragSourceListener;
    
    private DropGlassPane dropPane;
    
    private PalettePanel palette;
    
    private Timer timer;
    
    private static final Logger ERR = Logger.getLogger("org.netbeans.modules.palette"); // NOI18N
    
    /** Creates a new instance of DnDSupport */
    public DnDSupport( PalettePanel palette ) {
        this.palette = palette;
    }
    
    void add( CategoryDescriptor descriptor ) {
        CategoryList list = descriptor.getList();
        list.setTransferHandler( null );
        list.setDragEnabled(false);
        recognizers.add( DragSource.getDefaultDragSource().createDefaultDragGestureRecognizer( list, DnDConstants.ACTION_MOVE, this ) );
        dropTargets.add( new DropTarget( list, this ) );
        
        CategoryButton button = descriptor.getButton();
        button.setTransferHandler( null );
        recognizers.add( DragSource.getDefaultDragSource().createDefaultDragGestureRecognizer( button, DnDConstants.ACTION_MOVE, this ) );
        dropTargets.add( new DropTarget( button, this ) );
    }

    void remove( CategoryDescriptor descriptor ) {
        ArrayList<DragGestureRecognizer> recognizersToRemove = new ArrayList<DragGestureRecognizer>( 2 );
        for( Iterator<DragGestureRecognizer> i=recognizers.iterator(); i.hasNext(); ) {
            DragGestureRecognizer dgr = i.next();
            if( dgr.getComponent() == descriptor.getButton()
                || dgr.getComponent() == descriptor.getList() ) {
                recognizersToRemove.add( dgr );
                dgr.removeDragGestureListener( this );
            }
        }
        recognizers.removeAll( recognizersToRemove );
        
        ArrayList<DropTarget> dropTargetsToRemove = new ArrayList<DropTarget>( 2 );
        for( Iterator<DropTarget> i=dropTargets.iterator(); i.hasNext(); ) {
            DropTarget dt = i.next();
            if( dt.getComponent() == descriptor.getButton()
                || dt.getComponent() == descriptor.getList() ) {
                dropTargetsToRemove.add( dt );
                dt.removeDropTargetListener( this );
            }
        }
        dropTargets.removeAll( dropTargetsToRemove );
    }

    public void dragGestureRecognized( DragGestureEvent dge ) {
        Transferable t = null;
        
        if( dge.getComponent() instanceof CategoryButton ) {
            //trying to drag a palette category
            CategoryButton button = (CategoryButton)dge.getComponent();
            draggingCategory = button.getCategory();
            t = draggingCategory.getTransferable();
            
        } else if( dge.getComponent() instanceof CategoryList ) {
            //trying to drag a palette item
            CategoryList list = (CategoryList)dge.getComponent();
            int selIndex = list.locationToIndex( dge.getDragOrigin() );
            draggingItem = list.getItemAt( selIndex );
            if( null == draggingItem ) {
                return;
            }
            t = draggingItem.drag();
            dragSourceCategoryList = list;
        }
        if( null != t ) {
            dge.getDragSource().addDragSourceListener( getDragSourceListener() );
            try {
                dge.startDrag( null, t );
            } catch( InvalidDnDOperationException idndE ) {
                ERR.log( Level.INFO, idndE.getMessage(), idndE );
            }
        }
    }

    public void drop( DropTargetDropEvent dtde ) {
        Component target = dtde.getDropTargetContext().getComponent();
        Category targetCategory = null;
        if( target instanceof CategoryList ) {
            targetCategory = ((CategoryList)target).getCategory();
        } else if( target instanceof CategoryButton ) {
            targetCategory = ((CategoryButton)target).getCategory();
        }
        if( null != draggingCategory ) {
            //dragging a category to reorder
            boolean res = false;
            if( null != targetCategory && (target instanceof CategoryButton) ) {
                res = palette.getModel().moveCategory( draggingCategory, targetCategory, dropBefore );
            }
            dtde.dropComplete( res );
        } else {
            //dragging an item to reorder or move to a different category
            //or dragging something from outside the palette to create a new item
            dtde.acceptDrop( dtde.getDropAction() );
            boolean res = false;
            if( null != targetCategory ) {
                Transferable t;
                if( null != draggingItem ) {
                    //internal drag'n'drop - an item is being moved from a different category
                    t = draggingItem.cut();
                } else {
                    //a new item is being dropped to the palette from e.g. editor area
                    t = dtde.getTransferable();
                }
                res = targetCategory.dropItem( t, dtde.getDropAction(), targetItem, dropBefore );
            }
            dtde.dropComplete( res );
        }
        cleanupAfterDnD();
    }

    public void dragExit( DropTargetEvent dte ) {
        removeDropLine();
        if (DropGlassPane.isOriginalPaneStored()) {
            DropGlassPane.putBackOriginal();
        }
        removeTimer();
    }

    public void dropActionChanged( DropTargetDragEvent dtde ) {
    }

    public void dragOver( DropTargetDragEvent dtde ) {
        checkStoredGlassPane();
        
        doDragOver( dtde );
    }

    public void dragEnter( DropTargetDragEvent dtde ) {
        checkStoredGlassPane();
        
        Component target = dtde.getDropTargetContext().getComponent();
        if( target instanceof CategoryButton && null == draggingCategory ) {
            final CategoryButton button = (CategoryButton)target;
            if( !button.isSelected() && (null == timer || !timer.isRunning()) ) {
                removeTimer();
                timer = new Timer(
                    DELAY_TIME_FOR_EXPAND,
                    new ActionListener() {
                        final public void actionPerformed(ActionEvent e) {
                            button.setExpanded( true );
                        }
                    }
                );
                timer.setRepeats(false);
                timer.start();
            }
        }
        doDragOver( dtde );
    }
    

    /** Removes timer and all listeners. */
    private void removeTimer() {
        if (timer != null) {
            ActionListener[] l = (ActionListener[]) timer.getListeners(ActionListener.class);

            for (int i = 0; i < l.length; i++) {
                timer.removeActionListener(l[i]);
            }

            timer.stop();
            timer = null;
        }
    }

    
    private void doDragOver( DropTargetDragEvent dtde ) {
        Component target = dtde.getDropTargetContext().getComponent();
        if( null != draggingCategory ) {
            //a whole category is being dragged within the palette panel
            Category targetCategory = null;
            if( target instanceof CategoryButton ) {
                CategoryButton button = (CategoryButton)target;
                targetCategory = button.getCategory();
            } 
            if( null == targetCategory || !palette.getModel().canReorderCategories() ) {
                dtde.rejectDrag();
                removeDropLine();
                return;
            }
            dropBefore = dtde.getLocation().y < (target.getHeight()/2);
            Point p1 = target.getLocation();
            Point p2 = target.getLocation();
            p2.x += target.getWidth();
            if( !dropBefore ) {
                p1.y += target.getHeight();
                p2.y += target.getHeight();
            }
            p1 = SwingUtilities.convertPoint( target, p1, palette.getRootPane() );
            p2 = SwingUtilities.convertPoint( target, p2, palette.getRootPane() );
            Line2D line = new Line2D.Double( p1.x, p1.y, p2.x, p2.y );
            dropPane.setDropLine( line );
        } else {
            //dragging an existing item to reorder 
            //or somebody is trying to drop a new item from outside the palette panel
            Category targetCategory = null;
            if( target instanceof CategoryList ) {
                CategoryList list = (CategoryList)target;
                targetCategory = list.getCategory();
            } else if( target instanceof CategoryButton ) {
                CategoryButton button = (CategoryButton)target;
                targetCategory = button.getCategory();
            } 
            if( null != targetCategory && targetCategory.dragOver( dtde ) ) {
                dtde.acceptDrag( dtde.getDropAction() );
            } else {
                dtde.rejectDrag();
                removeDropLine();
                targetItem = null;
                return;
            }

            if( target instanceof CategoryList ) {
                CategoryList list = (CategoryList)target;
                int dropIndex = list.locationToIndex( dtde.getLocation() );
                if( dropIndex < 0 ) {
                    dropPane.setDropLine( null );
                    targetItem = null;
                } else {
                    boolean verticalDropBar = list.getColumnCount() > 1;
                    Rectangle rect = list.getCellBounds( dropIndex, dropIndex );
                    if( verticalDropBar )
                        dropBefore = dtde.getLocation().x < (rect.x + rect.width/2);
                    else
                        dropBefore = dtde.getLocation().y < (rect.y + rect.height/2);
                    Point p1 = rect.getLocation();
                    Point p2 = rect.getLocation();
                    if( verticalDropBar ) {
                        p2.y += rect.height;
                        if( !dropBefore ) {
                            p1.x += rect.width;
                            p2.x += rect.width;
                        }
                    } else {
                        p2.x += rect.width;
                        if( !dropBefore ) {
                            p1.y += rect.height;
                            p2.y += rect.height;
                        }
                    }
                    p1 = SwingUtilities.convertPoint( list, p1, palette.getRootPane() );
                    p2 = SwingUtilities.convertPoint( list, p2, palette.getRootPane() );
                    Line2D line = new Line2D.Double( p1.x, p1.y, p2.x, p2.y );
                    dropPane.setDropLine( line );
                    targetItem = (Item)list.getModel().getElementAt( dropIndex );
                }
            } else {
                targetItem = null;
                dropBefore = false;
            }
        }
        //parent.repaint();
    }
    
    private DragSourceListener getDragSourceListener() {
        if( null == dragSourceListener ) {
            dragSourceListener = new DragSourceAdapter() {
                public void dragDropEnd( DragSourceDropEvent dsde ) {
                    dsde.getDragSourceContext().getDragSource().removeDragSourceListener( this );
                    cleanupAfterDnD();
                }
            };
        } 
        return dragSourceListener;
    }
    
    private void cleanupAfterDnD() {
        draggingItem = null;
        draggingCategory = null;
        targetItem = null;
        if( null != dragSourceCategoryList ) {
            dragSourceCategoryList.resetRollover();
        }
        dragSourceCategoryList = null;
        removeDropLine();
        if (DropGlassPane.isOriginalPaneStored()) {
            DropGlassPane.putBackOriginal();
        }
        removeTimer();
    }

    private void checkStoredGlassPane() {
        // remember current glass pane to set back at end of dragging over this compoment
        if( !DropGlassPane.isOriginalPaneStored() ) {
            Component comp = palette.getRootPane().getGlassPane();
            DropGlassPane.setOriginalPane( palette, comp, comp.isVisible() );

            // set glass pane for paint selection line
            dropPane = DropGlassPane.getDefault( palette );
            palette.getRootPane().setGlassPane( dropPane );
            dropPane.revalidate();
            dropPane.validate();
            dropPane.setVisible(true);
        }
    }
    
    private void removeDropLine() {
        if( null != dropPane )
            dropPane.setDropLine( null );
    }
}
