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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.openide.awt;

import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.dnd.*;
import java.awt.event.*;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EventObject;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.plaf.synth.Region;
import javax.swing.plaf.synth.SynthConstants;
import javax.swing.plaf.synth.SynthContext;
import javax.swing.plaf.synth.SynthLookAndFeel;
import javax.swing.plaf.synth.SynthStyle;
import javax.swing.plaf.synth.SynthStyleFactory;
import org.openide.cookies.InstanceCookie;
import org.openide.loaders.*;
import org.openide.nodes.Node;
import org.openide.util.*;
import org.openide.util.actions.Presenter;
import org.openide.util.datatransfer.ExTransferable;

/**
 * Toolbar provides a component which is useful for displaying commonly used
 * actions.  It can be dragged inside its <code>ToolbarPanel</code> to
 * customize its location.
 *
 * @author  David Peroutka, Libor Kramolis
 */
public class Toolbar extends JToolBar /*implemented by patchsuperclass MouseInputListener*/ {
    /** Basic toolbar height.
     @deprecated Use getBasicHeight instead. */
    @Deprecated
    public static final int BASIC_HEIGHT = 34;

    static final Logger LOG = Logger.getLogger(Toolbar.class.getName());
    
    /** 5 pixels is tolerance of toolbar height so toolbar can be high (BASIC_HEIGHT + HEIGHT_TOLERANCE)
        but it will be set to BASIC_HEIGHT high. */
    static int HEIGHT_TOLERANCE = 5;
    /** TOP of toolbar empty border. */
    static int TOP = 2;
    /** LEFT of toolbar empty border. */
    static int LEFT = 3;
    /** BOTTOM of toolbar empty border. */
    static int BOTTOM = 2;
    /** RIGHT of toolbar empty border. */
    static int RIGHT = 3;
    /** Residual size of the toolbar when dragged far right */
    static int RESIDUAL_WIDTH = 16;
   

    /** is toolbar floatable */
    private boolean floatable;
    /** Toolbar DnDListener */
    private DnDListener listener;
    /** Toolbar mouse listener */
    private ToolbarMouseListener mouseListener;
    /** display name of the toolbar */
    private String displayName;
    
    /** Used for lazy creation of Folder and DisplayName */
    private DataFolder backingFolder;
    /* FolderInstance that will track all the changes in backingFolder */
    private Folder processor;

    //needed to turn off the painting of toolbar button borders on ocean/jdk1.5
    private static final boolean isMetalLaF = 
            MetalLookAndFeel.class.isAssignableFrom(UIManager.getLookAndFeel().getClass());
    private static final boolean isJdk15;
    private static final boolean isJdk16;
    
    static final long serialVersionUID = 5011742660516204764L;

    static {
        String javaVersion = System.getProperty( "java.version" );
        isJdk15 = javaVersion.startsWith( "1.5" );
        isJdk16 = javaVersion.startsWith( "1.6" );
    }
    
    private static final int customFontHeightCorrection;
    
    static {
        int customFontSize = UIManager.getInt( "customFontSize" );
        if( customFontSize < 1 ) 
            customFontSize = 1;
            
        int defaultFontSize = UIManager.getInt( "nbDefaultFontSize" );
        if( defaultFontSize <= 0 ) 
            defaultFontSize = 11;
        
        customFontHeightCorrection = Math.max( customFontSize - defaultFontSize, 0 );
    }
    
    private static Class synthIconClass = null;
        
    private static boolean testExecuted = false;
    
    /** Create a new Toolbar with empty name. */
    public Toolbar () {
        this (""); // NOI18N
    }

    /** Create a new not floatable Toolbar with programmatic name.
     * Display name is set to be the same as name */
    public Toolbar (String name) {
        this (name, name, false);
    }

    /** Create a new not floatable Toolbar with specified programmatic name
     * and display name */
    public Toolbar (String name, String displayName) {
        this (name, displayName, false);
    }
    
    /** Create a new <code>Toolbar</code>.
     * @param name a <code>String</code> containing the associated name
     * @param f specified if Toolbar is floatable
     * Display name of the toolbar is set equal to the name.
     */
    public Toolbar (String name, boolean f) {
        this (name, name, f);
    }
        
    Toolbar(DataFolder folder, boolean f) {
        super();
        backingFolder = folder;
        initAll(folder.getName(), f);
        initDnD();
    }
    
    /** 
     * Test if SynthIcon is available and can be used for painting native Toolbar
     * D&D handle. If not use our own handle. Reflection is used here as it is Sun
     * proprietary API.
     */
    private static boolean useSynthIcon () {
        if (!testExecuted) {
            testExecuted = true;
            try {
                synthIconClass = Class.forName("sun.swing.plaf.synth.SynthIcon");
            } catch (ClassNotFoundException exc) {
                LOG.log(Level.INFO, null, exc);
            }
        }
        return (synthIconClass != null);
    }
    
    private void initDnD() {
        DropTarget dt = new DropTarget(this, getDnd());
    }
    
    DataFolder getFolder() {
        return backingFolder;
    }
    

    @Override
    public void paint( Graphics g ) {
        super.paint( g );
        if( -1 != dropTargetButtonIndex ) {
            paintDropGesture( g );
        }
    }
    
    private void updateDropGesture( DropTargetDragEvent e ) {
        Point p = e.getLocation();
        Component c = getComponentAt(p);
        int index = Toolbar.this.getComponentIndex(c);
        if( index == 0 ) {
            //dragging over toolbar's grip
            resetDropGesture();
        } else {
            //find out whether we want to drop before or after this component
            boolean b = p.x <= c.getLocation().x + c.getWidth() / 2;
            if( index != dropTargetButtonIndex || b != insertBefore ) {
                dropTargetButtonIndex = index;
                insertBefore = b;
                repaint();
            }
        }
    }
    
    private void resetDropGesture() {
        dropTargetButtonIndex = -1;
        repaint();
    }
    
    private void paintDropGesture( Graphics g ) {
        Component c = getComponentAtIndex( dropTargetButtonIndex );
        if( null == c )
            return;
        
        Point location = c.getLocation();
        int cursorLocation = location.x;
        if( !insertBefore ) {
            cursorLocation += c.getWidth();
            if( dropTargetButtonIndex == getComponentCount()-1 )
                cursorLocation -= 3;
        }
        drawDropLine( g, cursorLocation );
    }
    
    private void drawDropLine( Graphics g, int x ) {
        Color oldColor = g.getColor();
        g.setColor( Color.black );
        int height = getHeight();
        g.drawLine( x, 3, x, height-4 );
        g.drawLine( x-1, 3, x-1, height-4 );

        g.drawLine( x+1, 2, x+1+2, 2 );
        g.drawLine( x+1, height-3, x+1+2, height-3 );

        g.drawLine( x-2, 2, x-2-2, 2 );
        g.drawLine( x-2, height-3, x-2-2, height-3 );
        g.setColor( oldColor );
    }
    
    /**
     * Remove a toolbar button represented by the given Transferable.
     */
    private void removeButton( Transferable t ) {
        try {
            Object o = null;
            if( t.isDataFlavorSupported( buttonDataFlavor ) ) {
                o = t.getTransferData( buttonDataFlavor ); //XXX
            }
            if( null != o && o instanceof DataObject ) {
                ((DataObject) o).delete();
                repaint();
                if( backingFolder.getChildren().length == 0 ) {
                    javax.swing.SwingUtilities.invokeLater(new java.lang.Runnable() {

                                                               public void run() {
                                                                   try {
                                                                       backingFolder.delete();
                                                                   }
                                                                   catch (java.io.IOException e) {
                                                                       LOG.log(Level.WARNING,
                                                                                         null,
                                                                                         e);
                                                                   }
                                                               }
                                                           });
                }
            }
        } catch( UnsupportedFlavorException ex ) {
            Exceptions.printStackTrace(ex);
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
        }
    }
    
    /**
     * Perform the drop operation.
     *
     * @return True if the drop has been successful.
     */
    private boolean handleDrop( Transferable t ) {
        try {
            Object o;
            if( t.isDataFlavorSupported( actionDataFlavor ) ) {
                o = t.getTransferData( actionDataFlavor );
                if( o instanceof Node ) {
                    DataObject dobj = ((Node)o).getLookup().lookup( DataObject.class );
                    return addButton( dobj, dropTargetButtonIndex-1, insertBefore );
                }
            } else {
                o = t.getTransferData( buttonDataFlavor );
                if( o instanceof DataObject ) {
                    return moveButton( (DataObject)o, dropTargetButtonIndex-1, insertBefore );
                }
            }
        } catch (UnsupportedFlavorException e) {
            Exceptions.printStackTrace(e);
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
        }
        return false;
    }

    /**
     * Component index of the button under the drag cursor, or -1 when the cursor
     * is above the toolbar drag handle
     */
    int dropTargetButtonIndex = -1;
    /**
     * Component index of the button being dragged, only used when dragging a button
     * within the same toolbar.
     */
    int dragSourceButtonIndex = -1;
    /**
     * True if the button being dragged should be dropped BEFORE the button 
     * under the drag cursor.
     */
    boolean insertBefore = true;
    /**
     * True indicates the toolbar instance whose button is being dragged.
     */
    boolean isDragSourceToolbar = false;
    
    private static DataFlavor buttonDataFlavor = new DataFlavor( DataObject.class, "Toolbar Item" );
    private static DataFlavor actionDataFlavor = new DataFlavor( Node.class, "Action Node" );

    private DnDSupport dnd;
    private class DnDSupport implements DragSourceListener, DragGestureListener, DropTargetListener, DragSourceMotionListener {
        private DragSource dragSource = new DragSource();
        
        private Cursor dragMoveCursor = DragSource.DefaultMoveDrop;
        private Cursor dragNoDropCursor = DragSource.DefaultMoveNoDrop;
        private Cursor dragRemoveCursor = Utilities.createCustomCursor( Toolbar.this, ImageUtilities.loadImage( "org/openide/loaders/delete.gif"), "NO_ACTION_MOVE" );
        private Map<Component, DragGestureRecognizer> recognizers = new HashMap<Component, DragGestureRecognizer>();
        
        public DnDSupport() {
            dragSource.addDragSourceMotionListener(this);
        }
        
        public void register(Component c) {
            DragGestureRecognizer dgr = recognizers.get( c );
            if( null == dgr ) {
                dgr = dragSource.createDefaultDragGestureRecognizer(c, DnDConstants.ACTION_MOVE, this);
                recognizers.put( c, dgr );
            }
        }

        public void unregister(Component c) {
            DragGestureRecognizer dgr = recognizers.get( c );
            if( null != dgr ) {
                dgr.removeDragGestureListener( this );
                recognizers.remove( c );
            }
        }
        
        public void dragEnter(DragSourceDragEvent e) {
            //handled in dragMouseMoved
        }

        public void dragOver(DragSourceDragEvent e) {
            //handled in dragMouseMoved
        }
        
        public void dragExit(DragSourceEvent e) {
            //handled in dragMouseMoved
            resetDropGesture();
        }

        public void dragDropEnd(DragSourceDropEvent e) {
            isDragSourceToolbar = false;
            Component sourceComponent = e.getDragSourceContext().getComponent();
            if( sourceComponent instanceof JButton ) {
                ((JButton)sourceComponent).getModel().setRollover( false );
            }
            sourceComponent.repaint();
            resetDropGesture();
            if ( e.getDropSuccess() == false && !isInToolbarPanel( e.getLocation() ) ) {
                removeButton( e.getDragSourceContext().getTransferable() );
            }
        }
        
        public void dragGestureRecognized(DragGestureEvent e) {
            if( !ToolbarPool.getDefault().isInEditMode()
                    || "QuickSearch".equals(getName()) )  //HACK (137286)- there's not better way...
                return;
            try {
                 Component c = e.getComponent();
                 //do not allow to drag toolbar separators
                 if( c instanceof JToolBar.Separator || "grip".equals( c.getName() ) )
                     return;
                 Transferable t = null;
                 if (c instanceof JComponent) {
                     final DataObject dob = (DataObject) ((JComponent) c).getClientProperty("file");
                     if (dob != null) {
                         t = new ExTransferable.Single( buttonDataFlavor ) {
                             public Object getData() {
                                 return dob;
                             }
                         };
                     }
                 }
                 if( c instanceof JButton ) {
                     ((JButton)c).getModel().setArmed( false );
                     ((JButton)c).getModel().setPressed( false );
                     ((JButton)c).getModel().setRollover( true );
                 }
                 if (t != null) {
                    dragSourceButtonIndex = Toolbar.this.getComponentIndex( c );
                    isDragSourceToolbar = true;
                    dragSource.startDrag(e, dragMoveCursor, t, this);
                 }
                
              } catch ( InvalidDnDOperationException idoe ) {
                    Exceptions.printStackTrace(idoe);
              }
        }

        public void dropActionChanged (DragSourceDragEvent e) {
            //ignore
        }
        
        public void drop(DropTargetDropEvent dtde) {
            boolean res = false;
            try {
                if( validateDropPosition() ) {
                    res = handleDrop( dtde.getTransferable() );
                }
            } finally {
                dtde.dropComplete(res);
            }
            resetDropGesture();
        }
        
        public void dragExit(DropTargetEvent dte) {
            resetDropGesture();
        }
        
        public void dropActionChanged(DropTargetDragEvent dtde) {
            //ignore
        }

        public void dragEnter(DropTargetDragEvent e) {
            if( e.isDataFlavorSupported( buttonDataFlavor ) 
                || e.isDataFlavorSupported( actionDataFlavor ) ) {
                e.acceptDrag(DnDConstants.ACTION_COPY_OR_MOVE);
            } else {
                e.rejectDrag();
            }
        }

        public void dragOver(DropTargetDragEvent e) {
            if( e.isDataFlavorSupported( buttonDataFlavor ) 
                || e.isDataFlavorSupported( actionDataFlavor ) ) {
                updateDropGesture( e );
                if( !validateDropPosition() ) {
                    e.rejectDrag();
                } else {
                    e.acceptDrag( DnDConstants.ACTION_COPY_OR_MOVE );
                }
            } else {
                e.rejectDrag();
            }
        }

        public void dragMouseMoved(DragSourceDragEvent e) {
            DragSourceContext context = e.getDragSourceContext();
            int action = e.getDropAction();
            if ((action & DnDConstants.ACTION_MOVE) != 0) {
                context.setCursor( dragMoveCursor );
            } else {
                if( isInToolbarPanel( e.getLocation() ) ) 
                    context.setCursor( dragNoDropCursor );
                else
                    context.setCursor( dragRemoveCursor );
            }
        }
    }
    
    private boolean isInToolbarPanel( Point p ) {
        Component c = ToolbarPool.getDefault();
        SwingUtilities.convertPointFromScreen( p, c );
        return c.contains( p );
    }
    
    /**
     * Add a new toolbar button represented by the given DataObject.
     */
    private boolean addButton( DataObject dobj, int dropIndex, boolean dropBefore ) throws IOException {
        if( null == dobj )
            return false;
        //check if the dropped button (action) already exists in this toolbar
        String objName = dobj.getName();
        DataObject[] children = backingFolder.getChildren();
        for( int i=0; i<children.length; i++ ) {
            //TODO is comparing DataObject names ok?
            if( objName.equals( children[i].getName() ) ) {
                //user dropped to toolbat a new button that already exists in this toolbar
                //just move the existing button to a new position
                isDragSourceToolbar = true;
                return moveButton( children[i], dropIndex, dropBefore );
            }
        }

        DataObject objUnderCursor = getDataObjectUnderDropCursor( dropIndex, dropBefore );

        DataShadow shadow = DataShadow.create( backingFolder, dobj );
        
        //find the added object
        DataObject newObj = null;
        children = backingFolder.getChildren();
        for( int i=0; i<children.length; i++ ) {
            if( objName.equals( children[i].getName() ) ) {
                newObj = children[i];
                break;
            }
        }
        
        if( null != newObj )
            reorderButtons( newObj, objUnderCursor ); //put the button to its proper position
        
        return true;
    }
    
    /**
     * Move toolbar button to a new position.
     */
    private boolean moveButton( DataObject ob, int dropIndex, boolean dropBefore ) throws IOException {
        //find out which button is currently under the drag cursor
        DataObject objUnderCursor = getDataObjectUnderDropCursor( dropIndex, dropBefore );

        if( !isDragSourceToolbar ) {
            //move button to the new toolbar
            ob.move(backingFolder);                 
        }

        reorderButtons( ob, objUnderCursor );
        //else we're dragging a button to an empty toolbar
        return true;
    }
    
    private void reorderButtons( DataObject objToMove, DataObject objUnderCursor ) throws IOException {
        java.util.List<DataObject> children = 
                new ArrayList<DataObject>( Arrays.asList( backingFolder.getChildren() ) );
        if( null == objUnderCursor ) {
            children.remove( objToMove );
            children.add( objToMove );
        } else {
            int targetIndex = children.indexOf( objUnderCursor );
            int currentIndex = children.indexOf( objToMove );
            if( currentIndex < targetIndex )
                targetIndex--;
            children.remove( objToMove );
            children.add( targetIndex, objToMove );
        }

        backingFolder.setOrder( children.toArray( new DataObject[children.size()]) );
    }
    
    private DataObject getDataObjectUnderDropCursor( int dropIndex, boolean dropBefore ) {
        DataObject[] buttons = backingFolder.getChildren();
        DataObject objUnderCursor = null;
        if( buttons.length > 0 ) {
            if( !dropBefore )
                dropIndex++;
            if( dropIndex < buttons.length && dropIndex >= 0 ) {
                objUnderCursor = buttons[dropIndex];
            }
        }
        return objUnderCursor;
    }
    
    private boolean validateDropPosition() {
               //the drag cursor cannot be positioned above toolbar's drag handle
        return dropTargetButtonIndex >= 0
               //when toolbar has buttons '1 2 3 4 5' and we're dragging button 3,
               //do not allow drop between buttons 2 and 3 and also between buttons 3 and 3
               && !(isDragSourceToolbar && (dragSourceButtonIndex == dropTargetButtonIndex  //drop index 3
                                        || (dropTargetButtonIndex == dragSourceButtonIndex-1 && !insertBefore) //drop index 2
                                        || (dropTargetButtonIndex == dragSourceButtonIndex+1 && insertBefore))) //drop index 4
               //dragging a button to an empty toolbar
               || (dropTargetButtonIndex < 0 && getComponentCount() == 1);
    }

    /** Start tracking content of the underlaying folder if not doing so yet */
    final Folder waitFinished() {
        // check for too early call (from constructor and UI.setUp...)
        if (backingFolder == null) return null;
        
        if(processor == null && isVisible()) {
            processor = new Folder(); // It will start tracking immediatelly
        }
        return processor;
    }    
    
    @Override
    public void addNotify() {
        super.addNotify();
        waitFinished();
    }
    
    @Override
    public Component[] getComponents () {
        waitFinished ();
        return super.getComponents ();
    }
    
    @Override
    public void setVisible(boolean b) {
	super.setVisible(b);
	waitFinished();	
    }
    
    private static final Insets emptyInsets = new Insets(1,1,1,1);
    /** Overridden to set focusable to false for any AbstractButton
     * subclasses which are added */
    @Override
    protected void addImpl(Component c, Object constraints, int idx) {
        //issue 39896, after opening dialog from toolbar button, focus
        //remains on toolbar button.  Does not create an accessibility issue - 
        //all standard toolbar buttons are also available via the keyboard
        if (c instanceof AbstractButton) {
            c.setFocusable(false);
            ((JComponent) c).setOpaque(false);
            if( isMetalLaF && (isJdk15 || isJdk16)) {
                //JDK 1.5 metal/ocean resets borders, so fix it this way
                ((AbstractButton) c).setBorderPainted(false);
                ((AbstractButton) c).setOpaque(false);
            }
            //This is active for GTK L&F. It should be fixed in JDK
            //but it is not fixed in JDK 6.0.
            if( isJdk16 && !isMetalLaF ) {
                ((AbstractButton) c).setMargin( emptyInsets );
            }
        } else if( c instanceof JToolBar.Separator ) {
            JToolBar.Separator separator = (JToolBar.Separator)c;
            if (getOrientation() == VERTICAL) {
                separator.setOrientation(JSeparator.HORIZONTAL);
            } else {
                separator.setOrientation(JSeparator.VERTICAL);
            }
        }
        
        super.addImpl (c, constraints, idx);
        if( !("grip".equals(c.getName()) || (c instanceof JToolBar.Separator)) ) {
            getDnd().register(c);
        }
    }

    /**
     * Create a new <code>Toolbar</code>.
     * @param name a <code>String</code> containing the associated name
     * @param f specified if Toolbar is floatable
     */
    public Toolbar (String name, String displayName, boolean f) {
        super();
        setDisplayName (displayName);
        initAll(name, f);
    }
    
    /** Returns basic toolbar height according to preferred icons size. Used by
     * toolbar layout manager.
     * @return basic toolbar height
     * @since 4.15
     */
    public static int getBasicHeight () {
        if (ToolbarPool.getDefault().getPreferredIconSize() == 24) {
            return 44;
        } else {
            return 34;
        }
    }
    
    private void initAll(String name, boolean f) {
        floatable = f;
        mouseListener = null;

        setName (name);
        
        setFloatable (false);
        String lAndF = UIManager.getLookAndFeel().getName();
        
        if (lAndF.equals("Windows")) {
            //Get rid of extra height, also allow for minimalist main
            //window
            setBorder(Boolean.getBoolean("netbeans.small.main.window") ?
                BorderFactory.createEmptyBorder(1,1,1,1) : 
                BorderFactory.createEmptyBorder()); //NOI18N
        } else if (!"Aqua".equals(UIManager.getLookAndFeel().getID()) && !"GTK".equals(UIManager.getLookAndFeel().getID())){
            Border b = UIManager.getBorder ("ToolBar.border"); //NOI18N
            
            if ((b==null) || (b instanceof javax.swing.plaf.metal.MetalBorders.ToolBarBorder))  
                b=BorderFactory.createEtchedBorder (EtchedBorder.LOWERED);
            setBorder (new CompoundBorder ( 
                   b,
                   new EmptyBorder (TOP, LEFT, BOTTOM, RIGHT))
                   );  
             
        }
        
        if (!"Aqua".equals(UIManager.getLookAndFeel().getID())) {
            putClientProperty("JToolBar.isRollover", Boolean.TRUE); // NOI18N
        }
        addGrip();

        getAccessibleContext().setAccessibleName(displayName == null ? getName() : displayName);
        getAccessibleContext().setAccessibleDescription(getName());
    }

    @Override
    public String getUIClassID() {
        if (UIManager.get("Nb.Toolbar.ui") != null) { //NOI18N
            return "Nb.Toolbar.ui"; //NOI18N
        } else {
            return super.getUIClassID();
        }
    }
    
    @Override
    public Dimension getPreferredSize() {
        String lfid = UIManager.getLookAndFeel().getID();
        int minheight;
        
        if (ToolbarPool.getDefault().getPreferredIconSize() == 24) {
            if ("Aqua".equals(lfid)) {
                minheight = 29 + 8;
            } else if ("Metal".equals(lfid)) {
                minheight = 36 + 8;
            } else if ("Windows".equals(lfid)) {
                minheight = isXPTheme() ? (23 + 8) : (27 + 8);
            } else if ("GTK".equals(lfid)) {
                minheight = 32 + 8;
            } else {
                minheight = 28 + 8;
            }
        } else {
            if ("Aqua".equals(lfid)) {
                minheight = 29;
            } else if ("Metal".equals(lfid)) {
                minheight = 36;
            } else if ("Windows".equals(lfid)) {
                minheight = isXPTheme() ? 23 : 27;
            } else if ("GTK".equals(lfid)) {
                minheight = 32;
            } else {
                minheight = 28;
            }
        }
        Dimension result = super.getPreferredSize();
        result.height = Math.max (result.height, minheight);
        return result;
    }

    /** Removes all ACTION components. */
    @Override
    public void removeAll () {
        for( int i=0; i<getComponentCount(); i++ ) {
            getDnd().unregister( getComponent(i) );
        }
        super.removeAll();
        addGrip();
    }

    /**
     * When Toolbar is floatable, ToolbarBump is added as Grip as first toolbar component
     * modified by Michael Wever, to use l&f's grip/bump. */
    void addGrip () {
        //HACK (137286)- there's not better way...
        if (floatable && !"QuickSearch".equals(getName()) ) { //NOI18N
            /** Uses L&F's grip **/
            String lfID = UIManager.getLookAndFeel().getID();
            JPanel dragarea = null;
            // #98888: recognize JGoodies L&F properly
            if (lfID.endsWith("Windows")) {
                if (isXPTheme()) {
                    dragarea = (JPanel) new ToolbarXP();
                } else {
                    dragarea = (JPanel) new ToolbarGrip();
                }
            } else if (lfID.equals("Aqua")) {
                dragarea = (JPanel) new ToolbarAqua();
            } else if (lfID.equals("GTK")) {
                dragarea = (JPanel) new ToolbarGtk();
                //setFloatable(true);
            } else {
                //Default for Metal and uknown L&F
                dragarea = (JPanel)new ToolbarBump();
            }
            if (mouseListener == null) {
                mouseListener = new ToolbarMouseListener ();
            }
            
            if (dragarea != null) {
                dragarea.addMouseListener (mouseListener);
                dragarea.addMouseMotionListener (mouseListener);

                dragarea.setName ("grip");
                add (dragarea);
            }
        }
    }

    /** Compute with HEIGHT_TOLERANCE number of rows for specific toolbar height.
     * @param height of some toolbar
     * @return number of rows
     */
    static public int rowCount (int height) {
        return 1 + height / (getBasicHeight() + HEIGHT_TOLERANCE+customFontHeightCorrection);
    }

    /** Set DnDListener to Toolbar.
     * @param l DndListener for toolbar
     */
    public void setDnDListener (DnDListener l) {
        listener = l;
    }
    
    /** @return Display name of this toolbar. Display name is localizable,
     * on the contrary to the programmatic name */
    public String getDisplayName () {
        if (displayName == null) {
            if (backingFolder.isValid()) {
                try {
                    return backingFolder.getNodeDelegate ().getDisplayName ();
                } catch (IllegalStateException ex) {
                    // OK: #141387
                }
            }
            // #17020
            return backingFolder.getName();
        }
        return displayName;
    }
    
    /** Sets new display name of this toolbar. Display name is localizable,
     * on the contrary to the programmatic name */
    public void setDisplayName (String displayName) {
        this.displayName = displayName;
    }
    
    private static final void setToolTipText (JComponent comp, String text) {
        comp.setToolTipText(Actions.cutAmpersand(text));
    }

    /** Fire drag of Toolbar
     * @param dx distance of horizontal dragging
     * @param dy distance of vertical dragging
     * @param type type of toolbar dragging
     */
    protected void fireDragToolbar (int dx, int dy, int type) {
        if (listener != null)
            listener.dragToolbar (new DnDEvent (this, getName(), dx, dy, type));
    }

    /** Fire drop of Toolbar
     * @param dx distance of horizontal dropping
     * @param dy distance of vertical dropping
     * @param type type of toolbar dropping
     */
    protected void fireDropToolbar (int dx, int dy, int type) {
        if (listener != null)
            listener.dropToolbar (new DnDEvent (this, getName(), dx, dy, type));
    }

    synchronized final MouseInputListener mouseDelegate () {
        if (mouseListener == null) mouseListener = new ToolbarMouseListener ();
        return mouseListener;
    }

    /** Toolbar mouse listener. */
    class ToolbarMouseListener extends MouseInputAdapter {
        /** Is toolbar dragging now. */
        private boolean dragging = false;
        /** Start point of dragging. */
        private Point startPoint = null;

        /** Invoked when a mouse button has been pressed on a component. */
        @Override
        public void mousePressed (MouseEvent e) {
            startPoint = e.getPoint();
        }

        /** Invoked when a mouse button has been released on a component. */
        @Override
        public void mouseReleased (MouseEvent e) {
            if (dragging) {
                
                int dx = getX() + e.getX() - startPoint.x > getParent().getWidth() - RESIDUAL_WIDTH ?
                0 : e.getX() - startPoint.x;
                
                fireDropToolbar (dx,
                                 e.getY() - startPoint.y,
                                 DnDEvent.DND_ONE);
                dragging = false;
            }
        }

        /** Invoked when a mouse button is pressed on a component and then dragged. */
        @Override
        public void mouseDragged (MouseEvent e) {
            int m = e.getModifiers();
            int type = DnDEvent.DND_ONE;
            int dx;
            
            if (e.isControlDown())
                type = DnDEvent.DND_LINE;
            else if (((m & InputEvent.BUTTON2_MASK) != 0) ||
                     ((m & InputEvent.BUTTON3_MASK) != 0))
                type = DnDEvent.DND_END;
            if (startPoint == null) {
                startPoint = new Point (e.getX(), e.getY());
            }
            
            if ( getX() + e.getX() + startPoint.x > getParent().getWidth() - RESIDUAL_WIDTH ) {
                if ( getX() >= getParent().getWidth() - RESIDUAL_WIDTH ) {
                    dx = 0;
                }
                else {
                    dx = getParent().getWidth() - RESIDUAL_WIDTH - getX();
                }
            }
            else {
                dx = e.getX() - startPoint.x; 
            }
            
            fireDragToolbar ( dx,
                             e.getY() - startPoint.y,
                             type);
            dragging = true;
        }

    } // end of inner class ToolbarMouseListener

    /**
     * This class can be used to produce a <code>Toolbar</code> instance from
     * the given <code>DataFolder</code>.
     */
    final class Folder extends FolderInstance {

        /**
         * Creates a new folder on the specified <code>DataFolder</code>.
         *
         */
        public Folder () {
            super (backingFolder);
            recreate ();
        }

        /**
         * Full name of the data folder's primary file separated by dots.
         * @return the name
         */
        @Override
        public String instanceName () {
            return Toolbar.this.getClass().getName();
        }

        /**
         * Returns the root class of all objects.
         * @return Object.class
         */
        @Override
        public Class instanceClass ()
        throws java.io.IOException, ClassNotFoundException {
            return Toolbar.this.getClass();
        }

        /** If no instance cookie, tries to create execution action on the
         * data object.
         */
        @Override
        protected InstanceCookie acceptDataObject (DataObject dob) {
            InstanceCookie ic = super.acceptDataObject (dob);
            if (ic == null) {
                JButton button = ExecBridge.createButton (dob);
                if (button != null) {
                    button.putClientProperty ("file", dob);
                }
                return button != null ? new InstanceSupport.Instance (button) : null;
            } else {
                return ic;
            }
        }
        
    private Map<Object, Object> cookiesToObjects = new HashMap<Object, Object>();
    
        @Override
    protected Object instanceForCookie (DataObject obj, InstanceCookie cookie)
    throws IOException, ClassNotFoundException {
        Object result = super.instanceForCookie(obj, cookie);
        cookiesToObjects.put (result, obj);
        return result;
    }
        

        /**
         * Accepts only cookies that can provide <code>Toolbar</code>.
         * @param cookie an <code>InstanceCookie</code> to test
         * @return true if the cookie can provide accepted instances
         */
        @Override
        protected InstanceCookie acceptCookie (InstanceCookie cookie)
        throws java.io.IOException, ClassNotFoundException {
            boolean is;
            
            if (cookie instanceof InstanceCookie.Of) {
                InstanceCookie.Of of = (InstanceCookie.Of)cookie;
                is = of.instanceOf (Component.class) ||
                     of.instanceOf (Presenter.Toolbar.class) ||
                     of.instanceOf (Action.class);
            } else {
                Class c = cookie.instanceClass();
                is = Component.class.isAssignableFrom(c) ||
                     Presenter.Toolbar.class.isAssignableFrom(c) ||
                     Action.class.isAssignableFrom (c);
            }
            return is ? cookie : null;
        }

        /**
         * Returns a <code>Toolbar.Folder</code> cookie for the specified
         * <code>DataFolder</code>.
         * @param df a <code>DataFolder</code> to create the cookie for
         * @return a <code>Toolbar.Folder</code> for the specified folder
         */
        @Override
        protected InstanceCookie acceptFolder(DataFolder df) {
            return null; // PENDING new Toolbar.Folder(df);
        }

        /**
         * Updates the <code>Toolbar</code> represented by this folder.
         *
         * @param cookies array of instance cookies for the folder
         * @return the updated <code>ToolbarPool</code> representee
         */
        protected Object createInstance(final InstanceCookie[] cookies)
        throws java.io.IOException, ClassNotFoundException {
            // refresh the toolbar's content
            Toolbar.this.removeAll();
            for (int i = 0; i < cookies.length; i++) {
                try {
                    java.lang.Object obj = cookies[i].instanceCreate();
                    java.lang.Object file = cookiesToObjects.get(obj);

                    if (obj instanceof org.openide.util.actions.Presenter.Toolbar) {
                        obj = ((org.openide.util.actions.Presenter.Toolbar) obj).getToolbarPresenter();
                    }
                    if (obj instanceof java.awt.Component) {
                        // remove border and grip if requested. "Fixed" toolbar
                        // item has to live alone in toolbar now
                        if ((obj instanceof javax.swing.JComponent) &&
                            "Fixed".equals(((javax.swing.JComponent) obj).getClientProperty("Toolbar"))) {
                            floatable = false;
                            org.openide.awt.Toolbar.this.removeAll();
                            setBorder(null);
                        }
                        if (obj instanceof javax.swing.JComponent) {
                            if (org.openide.awt.ToolbarPool.getDefault().getPreferredIconSize() ==
                                24) {
                                ((javax.swing.JComponent) obj).putClientProperty("PreferredIconSize",
                                                                                 new java.lang.Integer(24));
                            }
                            ((javax.swing.JComponent) obj).putClientProperty("file",
                                                                             file);
                        }
                        org.openide.awt.Toolbar.this.add((java.awt.Component) obj);
                        continue;
                    }
                    if (obj instanceof javax.swing.Action) {
                        javax.swing.Action a = (javax.swing.Action) obj;
                        javax.swing.JButton b = new org.openide.awt.Toolbar.DefaultIconButton();

                        if (org.openide.awt.ToolbarPool.getDefault().getPreferredIconSize() ==
                            24) {
                            b.putClientProperty("PreferredIconSize",
                                                new java.lang.Integer(24));
                        }
                        if (null == a.getValue(javax.swing.Action.SMALL_ICON) &&
                            (null == a.getValue(javax.swing.Action.NAME) ||
                             a.getValue(javax.swing.Action.NAME).toString().length() ==
                             0)) {
                            a.putValue(javax.swing.Action.SMALL_ICON,
                                       new ImageIcon( ImageUtilities.loadImage( "org/openide/loaders/unknown.gif") ));
                        }
                        org.openide.awt.Actions.connect(b, a);
                        b.putClientProperty("file", file);
                        org.openide.awt.Toolbar.this.add(b);
                        continue;
                    }
                }
                catch (java.io.IOException ex) {
                    LOG.log(Level.WARNING, null, ex);
                }
                catch (java.lang.ClassNotFoundException ex) {
                    LOG.log(Level.WARNING, null, ex);
                }
                finally {
                    cookiesToObjects.clear();
                }
            }

            // invalidate the toolbar, trigger proper relayout
            Toolbar.this.invalidate ();
            return Toolbar.this;
        }

        /** Recreate the instance in AWT thread.
        */
        @Override
        protected Task postCreationTask (Runnable run) {
            return new AWTTask (run);
        }

    } // end of inner class Folder

    /** Bumps for floatable toolbar */
    private final class ToolbarBump extends JPanel {
        /** Top gap. */
        static final int TOPGAP = 2;
        /** Bottom gap. */
        static final int BOTGAP = 2;
        /** Width of bump element. */
        static final int WIDTH = 6;
        
        /** Minimum size. */
        Dimension dim;
        /** Maximum size. */
        Dimension max;

        static final long serialVersionUID =-8819972936203315277L;

        /** Create new ToolbarBump. */
        public ToolbarBump () {
            super();
            int width = WIDTH;
            dim = new Dimension (width, width);
            max = new Dimension (width, Integer.MAX_VALUE);
            Toolbar.setToolTipText (this, Toolbar.this.getDisplayName());
        }

        /** Paint bumps to specific Graphics. */
        @Override
        public void paint (Graphics g) {
            Dimension size = this.getSize ();
            int height = size.height - BOTGAP;
            g.setColor (this.getBackground ());

            for (int x = 0; x+1 < size.width; x+=4) {
                for (int y = TOPGAP; y+1 < height; y+=4) {
                    g.setColor (this.getBackground ().brighter ());
                    g.drawLine (x, y, x, y);
                    if (x+5 < size.width && y+5 < height) {
                        g.drawLine (x+2, y+2, x+2, y+2);
                    }
                    g.setColor (this.getBackground ().darker ().darker ());
                    g.drawLine (x+1, y+1, x+1, y+1);
                    if (x+5 < size.width && y+5 < height) {
                        g.drawLine (x+3, y+3, x+3, y+3);
                    }
                }
            }
        }

        /** @return minimum size */
        @Override
        public Dimension getMinimumSize () {
            return dim;
        }

        /** @return preferred size */
        @Override
        public Dimension getPreferredSize () {
            return this.getMinimumSize ();
        }

        @Override
        public Dimension getMaximumSize () {
            return max;
        }
    } // end of inner class ToolbarBump

    /** Bumps for floatable toolbar GTK L&F */
    private final class ToolbarGtk extends JPanel {
        /** Top gap. */
        int TOPGAP;
        /** Bottom gap. */
        int BOTGAP;
        /** Width of bump element. */
        static final int WIDTH = 6;
        
        /** Minimum size. */
        Dimension dim;
        /** Maximum size. */
        Dimension max;

        static final long serialVersionUID = -8819972936203315277L;
        
        /** Create new ToolbarBump. */
        public ToolbarGtk () {
            super();
            int width = WIDTH;
            if (useSynthIcon()) {
                TOPGAP = 0;
                BOTGAP = 0;
            } else {
                TOPGAP = 2;
                BOTGAP = 2;
            }
            dim = new Dimension (width, width);
            max = new Dimension (width, Integer.MAX_VALUE);
            Toolbar.setToolTipText (this, Toolbar.this.getDisplayName());
        }
        
        /** Paint bumps to specific Graphics. */
        @Override
        public void paint (Graphics g) {
            if (useSynthIcon()) {
                int height = Toolbar.this.getHeight() - BOTGAP;
                Icon icon = UIManager.getIcon("ToolBar.handleIcon");
                Region region = Region.TOOL_BAR;
                SynthStyleFactory sf = SynthLookAndFeel.getStyleFactory();
                SynthStyle style = sf.getStyle(Toolbar.this, region);
                SynthContext context = new SynthContext(Toolbar.this, region, style, SynthConstants.DEFAULT);

                // for vertical toolbar, you'll need to ask for getIconHeight() instead
                Method m = null;
                try {
                    m = synthIconClass.getMethod("getIconWidth",Icon.class, SynthContext.class);
                } catch (NoSuchMethodException exc) {
                    LOG.log(Level.WARNING, null, exc);
                }
                int width = 0;
                //width = SynthIcon.getIconWidth(icon, context);
                try {
                    width = (Integer) m.invoke(null, new Object [] {icon, context});
                } catch (IllegalAccessException exc) {
                    LOG.log(Level.WARNING, null, exc);
                } catch (InvocationTargetException exc) {
                    LOG.log(Level.WARNING, null, exc);
                }
                try {
                    m = synthIconClass.getMethod("paintIcon",Icon.class,SynthContext.class,                            
                    Graphics.class,Integer.TYPE,Integer.TYPE,Integer.TYPE,Integer.TYPE);
                } catch (NoSuchMethodException exc) {
                    LOG.log(Level.WARNING, null, exc);
                }
                //SynthIcon.paintIcon(icon, context, g, 0, 0, width, height);
                try {
                    m.invoke(null, new Object [] {icon,context,g,new Integer(0),new Integer(-1),
                    new Integer(width),new Integer(height)});
                } catch (IllegalAccessException exc) {
                    LOG.log(Level.WARNING, null, exc);
                } catch (InvocationTargetException exc) {
                    LOG.log(Level.WARNING, null, exc);
                }                    
            } else {
                Dimension size = this.getSize();
                int height = size.height - BOTGAP;
                g.setColor (this.getBackground ());

                for (int x = 0; x+1 < size.width; x+=4) {
                    for (int y = TOPGAP; y+1 < height; y+=4) {
                        g.setColor (this.getBackground ().brighter ());
                        g.drawLine (x, y, x, y);
                        if (x+5 < size.width && y+5 < height) {
                            g.drawLine (x+2, y+2, x+2, y+2);
                        }
                        g.setColor (this.getBackground ().darker ().darker ());
                        g.drawLine (x+1, y+1, x+1, y+1);
                        if (x+5 < size.width && y+5 < height) {
                            g.drawLine (x+3, y+3, x+3, y+3);
                        }
                    }
                }
            }
        }

        /** @return minimum size */
        @Override
        public Dimension getMinimumSize () {
            return dim;
        }

        /** @return preferred size */
        @Override
        public Dimension getPreferredSize () {
            return new Dimension(WIDTH,Toolbar.this.getHeight() - BOTGAP - TOPGAP);
        }

        @Override
        public Dimension getMaximumSize () {
            return max;
        }
    } // end of inner class ToolbarGtk
    
    /** Recognizes if XP theme is set.
     * @return true if XP theme is set, false otherwise
     */
    private static Boolean isXP = null;
    private static boolean isXPTheme () {
        if (isXP == null) {
            Boolean xp = (Boolean)Toolkit.getDefaultToolkit().
            getDesktopProperty("win.xpstyle.themeActive"); //NOI18N
            isXP = Boolean.TRUE.equals(xp)? Boolean.TRUE : Boolean.FALSE;
        }
        return isXP.booleanValue();
    }    
    
    private final class ToolbarAqua extends JPanel {
        /** Width of grip */
        static final int WIDTH = 8;
        /** Minimum size. */
        Dimension dim;
        /** Maximum size. */
        Dimension max;
        static final long serialVersionUID =-8819972972003315277L;

        public ToolbarAqua() {
            dim = new Dimension (WIDTH, WIDTH);
            max = new Dimension (WIDTH, Integer.MAX_VALUE);
            Toolbar.setToolTipText (this, Toolbar.this.getDisplayName());
        }
        
        @Override
        public void paintComponent (Graphics g) {
            super.paintComponent(g);
            java.awt.Graphics2D g2d = (Graphics2D) g;
            g2d.addRenderingHints(getHints());
            
            int sz = 5;
            
            int y = ((getHeight() / 2) - (sz / 2)) - 2;
            int x = ((getWidth() / 2) - (sz / 2)) - 2;
            
            GradientPaint gradient = new GradientPaint(x+1, y+1, Color.BLACK,
            x+sz-1, y+sz-1, Color.WHITE);
            
            Paint paint = g2d.getPaint();
            
            g2d.setPaint(gradient);
            g2d.drawArc(x,y,sz,sz,0,359);
            
            g.setColor(new Color(240,240,240));
            g.drawLine(x+(sz/2), y + (sz/2),x+(sz/2), y + (sz/2));

            g2d.setPaint(paint);
        }
        
        /** @return minimum size */
        @Override
        public Dimension getMinimumSize () {
            return dim;
        }
        
        /** @return preferred size */
        @Override
        public Dimension getPreferredSize () {
            return this.getMinimumSize ();
        }
        
        @Override
        public Dimension getMaximumSize () {
            return max;
        }
    }    

    private static java.util.Map<RenderingHints.Key, Object> hintsMap = null;
    @SuppressWarnings("unchecked")
    static final Map getHints() {
        //XXX We REALLY need to put this in a graphics utils lib
        if (hintsMap == null) {
            //Thanks to Phil Race for making this possible
            hintsMap = (Map<RenderingHints.Key, Object>)(Toolkit.getDefaultToolkit().getDesktopProperty("awt.font.desktophints")); //NOI18N
            if (hintsMap == null) {
                hintsMap = new HashMap<RenderingHints.Key, Object>();
                hintsMap.put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            }
        }
        return hintsMap;
    }

    @Override
    public void setUI(javax.swing.plaf.ToolBarUI ui) {
        super.setUI(ui);
        if( null != backingFolder && null != processor ) {
            //recreate the toolbar buttons as their borders need to be reset
            processor.recreate();
        }
    }
    
    private final class ToolbarXP extends JPanel {
        /** Width of grip */
        static final int WIDTH = 7;
        /** Minimum size. */
        Dimension dim;
        /** Maximum size. */
        Dimension max;
        
        static final long serialVersionUID =-8819972936203315277L;
        public ToolbarXP() {
            dim = new Dimension (WIDTH, WIDTH);
            max = new Dimension (WIDTH, Integer.MAX_VALUE);
            Toolbar.setToolTipText (this, Toolbar.this.getDisplayName());
        }
        
        @Override
        public void paintComponent (Graphics g) {
            super.paintComponent(g);
            int x = 3;
            for (int i=4; i < getHeight()-4; i+=4) {
                //first draw the rectangular highlight below each dot
                g.setColor(UIManager.getColor("controlLtHighlight")); //NOI18N
                g.fillRect(x + 1, i + 1, 2, 2);
                //Get the shadow color.  We'll paint the darkest dot first,
                //and work our way to the lightest
                Color col = UIManager.getColor("controlShadow"); //NOI18N
                g.setColor(col);
                //draw the darkest dot
                g.drawLine(x+1, i+1, x+1, i+1);
                
                //Get the color components and calculate the amount each component
                //should increase per dot
                int red = col.getRed();
                int green = col.getGreen();
                int blue = col.getBlue();
                
                //Get the default component background - we start with the dark
                //color, and for each dot, add a percentage of the difference
                //between this and the background color
                Color back = getBackground();
                int rb = back.getRed();
                int gb = back.getGreen();
                int bb = back.getBlue();
                
                //Get the amount to increment each component for each dot
                int incr = (rb - red) / 5;
                int incg = (gb - green) / 5;
                int incb = (bb - blue) / 5;
                
                //Increment the colors
                red += incr;
                green += incg;
                blue += incb;
                //Create a slightly lighter color and draw the dot
                col = new Color(red, green, blue);
                g.setColor(col);
                g.drawLine(x+1, i, x+1, i);
                
                //And do it for the next dot, and so on, for all four dots
                red += incr;
                green += incg;
                blue += incb;
                col = new Color(red, green, blue);
                g.setColor(col);
                g.drawLine(x, i+1, x, i+1);
                
                red += incr;
                green += incg;
                blue += incb;
                col = new Color(red, green, blue);
                g.setColor(col);
                g.drawLine(x, i, x, i);
            }
        }
        
        /** @return minimum size */
        @Override
        public Dimension getMinimumSize() {
            return dim;
        }
        
        /** @return preferred size */
        @Override
        public Dimension getPreferredSize () {
            return this.getMinimumSize ();
        }
        
        @Override
        public Dimension getMaximumSize () {
            return max;
        }
    }
    
  /*
    public static void main(String[] args) {
        JFrame jf = new JFrame();
        jf.getContentPane().add (new ToolbarXP());
        jf.setSize(new java.awt.Dimension(200,200));
        jf.setLocation(20,20);
        jf.show();
    }
   */

    
    /** Grip for floatable toolbar, used for Windows Classic L&F */
    private final class ToolbarGrip extends JPanel {
        /** Horizontal gaps. */
        static final int HGAP = 1;
        /** Vertical gaps. */
        static final int VGAP = 2;
        /** Step between two grip elements. */
        static final int STEP = 1;
        /** Width of grip element. */
        static final int WIDTH = 2;

        /** Number of grip elements. */
        int columns;
        /** Minimum size. */
        Dimension dim;
        /** Maximum size. */
        Dimension max;

        static final long serialVersionUID =-8819972936203315276L;

        /** Create new ToolbarGrip for default number of grip elements. */
        public ToolbarGrip () {
            this(1);
        }

        /** Create new ToolbarGrip for specific number of grip elements.
         * @param col number of grip elements
         */
        public ToolbarGrip (int col) {
            super ();
            columns = col;
            int width = (col - 1) * STEP + col * WIDTH + 2 * HGAP;
            dim = new Dimension (width, width);
            max = new Dimension (width, Integer.MAX_VALUE);
            this.setBorder (new EmptyBorder (VGAP, HGAP, VGAP, HGAP));
            Toolbar.setToolTipText (this, Toolbar.this.getDisplayName());
        }

        /** Paint grip to specific Graphics. */
        @Override
        public void paint (Graphics g) {
            Dimension size = this.getSize();
            int top = VGAP;
            int bottom = size.height - 1 - VGAP;
            int height = bottom - top;
            g.setColor ( this.getBackground() );

            for (int i = 0, x = HGAP; i < columns; i++, x += WIDTH + STEP) {
                g.draw3DRect (x, top, WIDTH, height, true); // grip element is 3D rectangle now
            }

        }

        /** @return minimum size */
        @Override
        public Dimension getMinimumSize () {
            return dim;
        }

        /** @return preferred size */
        @Override
        public Dimension getPreferredSize () {
            return this.getMinimumSize();
        }
        
        @Override
        public Dimension getMaximumSize () {
            return max;
        }
        
    } // end of inner class ToolbarGrip

    /** DnDListener is Drag and Drop listener for Toolbar motion events. */
    public interface DnDListener extends java.util.EventListener {
        /** Invoced when toolbar is dragged. */
        public void dragToolbar (DnDEvent e);

        /** Invoced when toolbar is dropped. */
        public void dropToolbar (DnDEvent e);
    } // end of interface DnDListener


    /** DnDEvent is Toolbar's drag and drop event. */
    public static class DnDEvent extends EventObject {
        /** Type of DnDEvent. Dragging with only one Toolbar. */
        public static final int DND_ONE  = 1;
        /** Type of DnDEvent. Only horizontal dragging with Toolbar and it's followers. */
        public static final int DND_END  = 2;
        /** Type of DnDEvent. Only vertical dragging with whole lines. */
        public static final int DND_LINE = 3;

        /** Name of toolbar where event occured. */
        private String name;
        /** distance of horizontal dragging */
        private int dx;
        /** distance of vertical dragging */
        private int dy;
        /** Type of event. */
        private int type;

        static final long serialVersionUID =4389530973297716699L;
        public DnDEvent (Toolbar toolbar, String name, int dx, int dy, int type) {
            super (toolbar);

            this.name = name;
            this.dx = dx;
            this.dy = dy;
            this.type = type;
        }

        /** @return name of toolbar where event occured. */
        public String getName () {
            return name;
        }

        /** @return distance of horizontal dragging */
        public int getDX () {
            return dx;
        }

        /** @return distance of vertical dragging */
        public int getDY () {
            return dy;
        }

        /** @return type of event. */
        public int getType () {
            return type;
        }
    } // end of class DnDEvent
    
    /**
     * A button that provides a default icon when no text and no custom icon have been set.
     */
    private static class DefaultIconButton extends JButton {
        private Icon unknownIcon;
        
        @Override
        public Icon getIcon() {
            Icon retValue = super.getIcon();
            if( null == retValue && (null == getText() || getText().length() == 0 ) ) {
                if (unknownIcon == null) {
                    unknownIcon = new ImageIcon( ImageUtilities.loadImage( "org/openide/loaders/unknown.gif") );
                }
                retValue = unknownIcon;
            }
            return retValue;
        }
    }

    private DnDSupport getDnd() {
        if (dnd == null) {
            dnd = new DnDSupport();
        }
        return dnd;
    }
} // end of class Toolbar
