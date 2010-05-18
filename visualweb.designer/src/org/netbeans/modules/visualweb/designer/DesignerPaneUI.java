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

import java.awt.AWTKeyStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.dnd.DropTarget;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.HashSet;
import java.util.Set;
import java.util.TooManyListenersException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.plaf.ActionMapUIResource;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.InputMapUIResource;
import javax.swing.plaf.UIResource;
import javax.swing.text.Keymap;

import org.w3c.dom.Element;

import org.netbeans.modules.visualweb.api.designer.DomProvider.DomPosition;
import org.netbeans.modules.visualweb.css2.CssBox;
import org.netbeans.modules.visualweb.css2.ModelViewMapper;
import org.netbeans.modules.visualweb.css2.PageBox;
import org.netbeans.modules.visualweb.text.DesignerPaneBase;
import org.netbeans.modules.visualweb.text.DesignerPaneBaseUI;


/**
 * Same as DesignerPaneUI, but rewritten to deal with Boxes directly.
 * Provides the look and feel for a DesignerPane.
 * Based -heavily- on BasicEditorPaneUI in javax.swing.
 */
public class DesignerPaneUI extends DesignerPaneBaseUI {
//    private static final TransferHandler defaultTransferHandler = null; // = new TextTransferHandler();
    private static TextDropTargetListener defaultDropTargetListener = null;
//    private boolean deferred;
    private long prev1 = 0;
    private long prev2 = 0;
    private long prev3 = 0;

    // ----- member variables ---------------------------------------
    transient PageBox pageBox;
    transient DesignerPane editor;
    transient boolean painted;

    /**
     * Creates a new UI.
     */
    public DesignerPaneUI() {
        super();

        painted = false;
    }

    // FROM BASICEDITORPANEUI:

    /**
     * Creates a UI for the DesignerPane.
     *
     * @param c the DesignerPane component
     * @return the UI
     */
    public static ComponentUI createUI(JComponent c) {
        return new DesignerPaneUI();
    }

    /** Return the initial containing block used by the editor */
    public PageBox getPageBox() {
        return pageBox;
    }

    /**
     * Fetches the name used as a key to lookup properties through the
     * UIManager.  This is used as a prefix to all the standard
     * text properties.
     *
     * @return the name ("EditorPane")
     */
    protected String getPropertyPrefix() {
        // Not changed: we want to use the same colors etc.
        // as JEditorPane
        return "EditorPane";
    }

    /**
     * Fetch an action map to use.  The map for a JEditorPane
     * is not shared because it changes with the EditorKit.
     */
    ActionMap getActionMap() {
        ActionMap am = new ActionMapUIResource();
        am.put("requestFocus", new FocusAction());

        Action[] actions = getComponent().getActions();

        if (actions != null) {
            addActions(am, actions);
        }

        am.put(TransferHandler.getCutAction().getValue(Action.NAME), TransferHandler.getCutAction());
        am.put(TransferHandler.getCopyAction().getValue(Action.NAME),
                TransferHandler.getCopyAction());
        am.put(TransferHandler.getPasteAction().getValue(Action.NAME),
                TransferHandler.getPasteAction());
        
        return am;
    }
    
    void removeActions(ActionMap map, Action[] actions) {
        if(DesignerUtils.DEBUG) {
            DesignerUtils.debugLog(getClass().getName() + ".removeActions(ActionMap, Action[])");
        }
        if(map == null) {
            throw(new IllegalArgumentException("Null action map."));
        }
        if(actions == null) {
            throw(new IllegalArgumentException("Null action array."));
        }
        
        int n = actions.length;
        
        for (int i = 0; i < n; i++) {
            Action a = actions[i];
            map.remove(a.getValue(Action.NAME));
        }
    }
    
    void addActions(ActionMap map, Action[] actions) {
        if(DesignerUtils.DEBUG) {
            DesignerUtils.debugLog(getClass().getName() + ".addActions(ActionMap, Action[])");
        }
        if(map == null) {
            throw(new IllegalArgumentException("Null action map."));
        }
        if(actions == null) {
            throw(new IllegalArgumentException("Null action array."));
        }
        int n = actions.length;
        
        for (int i = 0; i < n; i++) {
            Action a = actions[i];
            map.put(a.getValue(Action.NAME), a);
        }
    }

    // XXX Moved to DesignerPaneBaseUI
//    /**
//     * Creates the object to use for a caret.  By default an
//     * instance of BasicCaret is created.  This method
//     * can be redefined to provide something else that implements
//     * the InputPosition interface or a subclass of JCaret.
//     *
//     * @return the caret object
//     */
//    protected DesignerCaret createCaret() {
//        DesignerCaret caret = new DesignerCaret();
//        
//        String prefix = getPropertyPrefix();
//        Object o = UIManager.get(prefix + ".caretBlinkRate");
//        
//        if ((o != null) && (o instanceof Integer)) {
//            Integer rate = (Integer)o;
//            caret.setBlinkRate(rate.intValue());
//        }
//        
//        return caret;
//    }
    
    /**
     * Fetches the name of the keymap that will be installed/used
     * by default for this UI. This is implemented to create a
     * name based upon the classname.  The name is the the name
     * of the class with the package prefix removed.
     *
     * @return the name
     */
    protected String getKeymapName() {
        String nm = getClass().getName();
        int index = nm.lastIndexOf('.');
        
        if (index >= 0) {
            nm = nm.substring(index + 1, nm.length());
        }
        
        return nm;
    }
    
    /**
     * Creates the keymap to use for the text component, and installs
     * any necessary bindings into it.  By default, the keymap is
     * shared between all instances of this type of DesignerPaneBaseUI. The
     * keymap has the name defined by the getKeymapName method.  If the
     * keymap is not found, then DEFAULT_KEYMAP from JTextComponent is used.
     * <p>
     * The set of bindings used to create the keymap is fetched
     * from the UIManager using a key formed by combining the
     * {@link #getPropertyPrefix} method
     * and the string <code>.keyBindings</code>.  The type is expected
     * to be <code>JTextComponent.KeyBinding[]</code>.
     *
     * @return the keymap
     * @see #getKeymapName
     * @see com.sun.rave.text.JTextComponent
     */
    protected Keymap createKeymap() {
        String nm = getKeymapName();
        Keymap map = DesignerPaneBase.getKeymap(nm);
        
        if (map == null) {
            Keymap parent = DesignerPaneBase.getKeymap(DesignerPaneBase.DEFAULT_KEYMAP);
            map = DesignerPaneBase.addKeymap(nm, parent);
            
            String prefix = getPropertyPrefix();
            Object o = UIManager.get(prefix + ".keyBindings");
            
            if ((o != null) && (o instanceof DesignerPaneBase.KeyBinding[])) {
                DesignerPaneBase.KeyBinding[] bindings = (DesignerPaneBase.KeyBinding[])o;
                DesignerPaneBase.loadKeymap(map, bindings, getComponent().getActions());
            }
        }
        
        return map;
    }
    
    // FROM BASICTEXTUI:
    
    /**
     * Initializes component properties, e.g. font, foreground,
     * background, caret color, selection color, selected text color,
     * disabled text color, and border color.  The font, foreground, and
     * background properties are only set if their current value is either null
     * or a UIResource, other properties are set if the current
     * value is null.
     *
     * @see #uninstallDefaults
     * @see #installUI
     */
    protected void installDefaults() {
        // We don't support text dragging in the designer surface yet
        //editor.addMouseListener(defaultDragRecognizer);
        //editor.addMouseMotionListener(defaultDragRecognizer);
        WebForm form = editor.getWebForm();
        editor.addMouseListener(form.getManager().getMouseHandler());
        editor.addMouseMotionListener(form.getManager().getMouseHandler());
        
        String prefix = getPropertyPrefix();
        Font f = editor.getFont();
        
        if ((f == null) || (f instanceof UIResource)) {
            editor.setFont(UIManager.getFont(prefix + ".font"));
        }
        
        Color bg = editor.getBackground();
        
        if ((bg == null) || (bg instanceof UIResource)) {
            editor.setBackground(UIManager.getColor(prefix + ".background"));
        }
        
        Color fg = editor.getForeground();
        
        if ((fg == null) || (fg instanceof UIResource)) {
            editor.setForeground(UIManager.getColor(prefix + ".foreground"));
        }
        
        Color color = editor.getCaretColor();
        
        if ((color == null) || (color instanceof UIResource)) {
            editor.setCaretColor(UIManager.getColor(prefix + ".caretForeground"));
        }
        
        Color s = editor.getSelectionColor();
        
        if ((s == null) || (s instanceof UIResource)) {
            editor.setSelectionColor(UIManager.getColor(prefix + ".selectionBackground"));
        }
        
        Color sfg = editor.getSelectedTextColor();
        
        if ((sfg == null) || (sfg instanceof UIResource)) {
            editor.setSelectedTextColor(UIManager.getColor(prefix + ".selectionForeground"));
        }
        
        Border b = editor.getBorder();
        
        if ((b == null) || (b instanceof UIResource)) {
            editor.setBorder(UIManager.getBorder(prefix + ".border"));
        }
        
        /* Caret is only set when we go into flow mode editing
        DesignerCaret caret = editor.getCaret();
        if (caret == null || caret instanceof UIResource) {
            caret = createCaret();
            editor.setCaret(caret);
         
            Object o = UIManager.get(prefix + ".caretBlinkRate");
            if ((o != null) && (o instanceof Integer)) {
                Integer rate = (Integer) o;
                caret.setBlinkRate(rate.intValue());
                System.out.println("BLINK RATE WAS " + rate);
            }
        }
         */
//        TransferHandler th = editor.getTransferHandler();
//        if ((th == null) || th instanceof UIResource) {
//            editor.setTransferHandler(getTransferHandler());
//            editor.setTransferHandler(null);
//        }
        
        DropTarget dropTarget = editor.getDropTarget();
        
//        if (dropTarget instanceof UIResource) {
        if (dropTarget != null) {
            getDropTargetListener();
            
            try {
                dropTarget.addDropTargetListener(defaultDropTargetListener);
            } catch (TooManyListenersException ex) {
                // should not happen... swing drop target is multicast
                log(ex);
            }
        }
    }
    
    private TextDropTargetListener getDropTargetListener() {
        if (defaultDropTargetListener == null) {
            defaultDropTargetListener = new TextDropTargetListener();
        }
        
        return defaultDropTargetListener;
    }
    
    /**
     * Sets the component properties that haven't been explicitly overridden to
     * null.  A property is considered overridden if its current value
     * is not a UIResource.
     *
     * @see #installDefaults
     * @see #uninstallUI
     */
    protected void uninstallDefaults() {
        //editor.removeMouseListener(defaultDragRecognizer);
        //editor.removeMouseMotionListener(defaultDragRecognizer);
        WebForm form = editor.getWebForm();
        editor.removeMouseListener(form.getManager().getMouseHandler());
        editor.removeMouseMotionListener(form.getManager().getMouseHandler());
        
        if (editor.getCaretColor() instanceof UIResource) {
            editor.setCaretColor(null);
        }
        
        if (editor.getSelectionColor() instanceof UIResource) {
            editor.setSelectionColor(null);
        }
        
        if (editor.getSelectedTextColor() instanceof UIResource) {
            editor.setSelectedTextColor(null);
        }
        
        if (editor.getBorder() instanceof UIResource) {
            editor.setBorder(null);
        }

        // DesignerCaret is not UIResource instance.
//        if (editor.getCaret() instanceof UIResource) {
//            editor.setCaret(null);
//        }
        
        if (editor.getTransferHandler() instanceof UIResource) {
            editor.setTransferHandler(null);
        }

        // Unintall drop target listener too.        
        DropTarget dropTarget = editor.getDropTarget();
        if (dropTarget != null && defaultDropTargetListener != null) {
            dropTarget.removeDropTargetListener(defaultDropTargetListener);
       }
    }
    
    /**
     * Installs listeners for the UI.
     */
    protected void installListeners() {
    }
    
    /**
     * Uninstalls listeners for the UI.
     */
    protected void uninstallListeners() {
    }
    
    protected void installKeyboardActions() {
        // backward compatibility support... keymaps for the UI
        // are now installed in the more friendly input map.
        editor.setKeymap(createKeymap());
        
        InputMap km = getInputMap();
        
        if (km != null) {
            SwingUtilities.replaceUIInputMap(editor, JComponent.WHEN_FOCUSED, km);
        }
        
        ActionMap map = getActionMap();
        
        if (map != null) {
            SwingUtilities.replaceUIActionMap(editor, map);
        }
        
        // XXX DesignerPaneUI.installKeyboardActions: TODO: updateFocusAcceleratorBinding
        //updateFocusAcceleratorBinding(false);
    }
    
    /**
     * Get the InputMap to use for the UI.
     */
    InputMap getInputMap() {
        InputMap map = new InputMapUIResource();
        InputMap shared = (InputMap)UIManager.get(getPropertyPrefix() + ".focusInputMap");
        
        if (shared != null) {
            map.setParent(shared);
        }
        
        return map;
    }
    
    /**
     * Invoked when the focus accelerator changes, this will update the
     * key bindings as necessary.
     */
    
    /*
    void updateFocusAcceleratorBinding(boolean changed) {
        char accelerator = editor.getFocusAccelerator();
     
        if (changed || accelerator != '\0') {
            InputMap km = SwingUtilities.getUIInputMap
                        (editor, JComponent.WHEN_IN_FOCUSED_WINDOW);
     
            if (km == null && accelerator != '\0') {
                km = new ComponentInputMapUIResource(editor);
                SwingUtilities.replaceUIInputMap(editor, JComponent.
                                                 WHEN_IN_FOCUSED_WINDOW, km);
                ActionMap am = getActionMap();
                SwingUtilities.replaceUIActionMap(editor, am);
            }
            if (km != null) {
                km.clear();
                if (accelerator != '\0') {
                    km.put(KeyStroke.getKeyStroke(accelerator,
                                                  ActionEvent.ALT_MASK),
                           "requestFocus");
                }
            }
        }
    }
     */
    
    /**
     * Invoked when editable property is changed.
     *
     * removing 'TAB' and 'SHIFT-TAB' from traversalKeysSet in case
     * editor is editable
     * adding 'TAB' and 'SHIFT-TAB' to traversalKeysSet in case
     * editor is non editable
     */
    void updateFocusTraversalKeys() {
        /*
         * Fix for 4514331 Non-editable JTextArea and similar
         * should allow Tab to keyboard - accessibility
         */
        Set<? extends AWTKeyStroke> storedForwardTraversalKeys =
                editor.getFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS);
        Set<? extends AWTKeyStroke> storedBackwardTraversalKeys =
                editor.getFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS);
        Set<? extends AWTKeyStroke> forwardTraversalKeys = new HashSet<AWTKeyStroke>(storedForwardTraversalKeys);
        Set<? extends AWTKeyStroke> backwardTraversalKeys = new HashSet<AWTKeyStroke>(storedBackwardTraversalKeys);
        
        forwardTraversalKeys.remove(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0));
        backwardTraversalKeys.remove(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, InputEvent.SHIFT_MASK));
        
        editor.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS,
                forwardTraversalKeys);
        editor.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS,
                backwardTraversalKeys);
    }
    
//    /**
//     * Returns the <code>TransferHandler</code> that will be installed if
//     * their isn't one installed on the <code>JTextComponent</code>.
//     */
//    TransferHandler getTransferHandler() {
//        return defaultTransferHandler;
//    }
    
    /**
     * Create a default action map.  This is basically the
     * set of actions found exported by the component.
     */
    ActionMap createActionMap() {
        ActionMap map = new ActionMapUIResource();
        Action[] actions = editor.getActions();
        int n = actions.length;
        
        for (int i = 0; i < n; i++) {
            Action a = actions[i];
            map.put(a.getValue(Action.NAME), a);
        }
        
        map.put(TransferHandler.getCutAction().getValue(Action.NAME), TransferHandler.getCutAction());
        map.put(TransferHandler.getCopyAction().getValue(Action.NAME),
                TransferHandler.getCopyAction());
        map.put(TransferHandler.getPasteAction().getValue(Action.NAME),
                TransferHandler.getPasteAction());
        
        return map;
    }
    
    protected void uninstallKeyboardActions() {
        editor.setKeymap(null);
        SwingUtilities.replaceUIInputMap(editor, JComponent.WHEN_IN_FOCUSED_WINDOW, null);
        SwingUtilities.replaceUIActionMap(editor, null);
    }
    
    /**
     * Fetches the text component associated with this
     * UI implementation.  This will be null until
     * the ui has been installed.
     *
     * @return the editor component
     */
    protected final DesignerPaneBase getComponent() {
        return editor;
    }
    
//    /**
//     * I'm delaying model updates while the editor is not showing. Call the
//     * update method when the editor is made visible to update it with deferred
//     * changes
//     */
//    public void update() {
//        if (deferred) {
//            resetPageBox();
//            deferred = false;
//        }
//    }
    
    /**
     * Flags model changes.
     * This is called whenever the model has changed.
     * It is implemented to rebuild the view hierarchy
     * to represent the default root element of the
     * associated model.
     */
    /*protected*/public void /*modelChanged*/resetPageBox() {
        //        if (!editor.isShowing() && pageBox != null) { // we need pagebox to get sizes
        //            deferred = true;
        //            return;
        //        }
        WebForm webform = editor.getWebForm();
        Element elem = webform.getHtmlBody();
        
        if (elem == null) {
//            return;
            // XXX #6270621
            setPageBox(null);
        } else {
            setPageBox(PageBox.getPageBox(editor, webform, elem));
        }
        
        webform.getManager().setInsertBox(null, null);
    }
    
    /**
     * Sets the current root of the view hierarchy and calls invalidate().
     * If there were any child components, they will be removed (i.e.
     * there are assumed to have come from components embedded in views).
     *
     * @param v the root view
     */
    protected final void setPageBox(PageBox box) {
        if(DesignerUtils.DEBUG) {
            DesignerUtils.debugLog(getClass().getName() + ".setPageBox(PageBox)");
        }
        // XXX #6461287.
//        if(box == null) {
//            throw(new IllegalArgumentException("Null page box."));
//        }
        
        if (pageBox != null) {
            pageBox.boxRemoved();
        }
        
        //editor.removeAll();
        pageBox = box;
        
        updateViewport();
        painted = false;
        
        //editor.revalidate();
        editor.repaint();
        
        if (pageBox != null) {
            pageBox.boxAdded();
        }
    }
    
    /**
     * Paints the interface safely with a guarantee that
     * the model won't change from the view of this thread.
     * This does the following things, rendering from
     * back to front.
     * <ol>
     * <li>
     * If the component is marked as opaque, the background
     * is painted in the current background color of the
     * component.
     * <li>
     * The highlights (if any) are painted.
     * <li>
     * The box hierarchy is painted.
     * <li>
     * The caret is painted.
     * </ol>
     *
     * @param g the graphics context
     */
    protected void paintSafely(Graphics g) {
        painted = true;
        
        long start = 0;
        
        if (DesignerPane.DEBUG_REPAINT) {
            start = System.currentTimeMillis();
        }
        
        // paint the box hierarchy
        Graphics2D g2d = (Graphics2D)g;
        
//        Document doc = editor.getDocument();
//        WebForm webform = doc.getWebForm();
        WebForm webform = editor.getWebForm();
        
        g.getClipBounds(DesignerPane.clip);
        DesignerPane.clipBr.x = DesignerPane.clip.x + DesignerPane.clip.width;
        DesignerPane.clipBr.y = DesignerPane.clip.y + DesignerPane.clip.height;
        
        pageBox.paint(g2d);
        
        // Draw selection handles, marquee selection, etc.
        webform.getManager().paint(g2d);
        
        // paint the caret
//        DesignerCaret caret = editor.getCaret();
//        
//        if (caret != null) {
//            caret.paint(g);
//        }
        if (editor.hasCaret()) {
            editor.paintCaret(g);
        }
        
        if (DesignerPane.DEBUG_REPAINT) {
            long end = System.currentTimeMillis();
            long time = end - start;
            long avg = (time + prev1 + prev2 + prev3) / 4;
            System.out.println("Paint Hierarchy: = " + (end - start) + " ms" + "; average=" + avg +
                    " and clip was " + DesignerPane.clip);
            prev3 = prev2;
            prev2 = prev1;
            prev1 = avg;
        }
    }
    
    // --- ComponentUI methods --------------------------------------------
    
    /**
     * Installs the UI for a component.  This does the following
     * things.
     * <ol>
     * <li>
     * Set the associated component to opaque (can be changed
     * easily by a subclass or on JTextComponent directly),
     * which is the most common case.  This will cause the
     * component's background color to be painted.
     * <li>
     * Install the default caret and highlighter into the
     * associated component.
     * <li>
     * Attach to the editor and model.  If there is no
     * model, a default one is created.
     * <li>
     * create the the box hierarchy used to represent the model.
     * </ol>
     *
     * @param c the editor component
     * @see ComponentUI#installUI
     */
    public void installUI(JComponent c) {
        if(DesignerUtils.DEBUG) {
            DesignerUtils.debugLog(getClass().getName() + ".installUI(JComponent)");
        }
        if(c == null) {
            throw(new IllegalArgumentException("Null component."));
        }
        if (c instanceof DesignerPane) {
            editor = (DesignerPane)c;
            
            // install defaults
            installDefaults();
            
            // Don't paint backgrounds - views will do that so
            // don't waste time painting behind the views
            editor.setOpaque(false);
            editor.setAutoscrolls(true);
            
//            // attach to the model and editor
//            //editor.addPropertyChangeListener(updateHandler);
////            Document doc = editor.getDocument();
//            
////            if (doc == null) {
////                // no model, create a default one.  This will
////                // fire a notification to the updateHandler
////                // which takes care of the rest.
////                Thread.dumpStack();
////                throw new RuntimeException("Not yet implemented");
////                
////                //editor.setDocument(getEditorKit(editor).createDefaultDocument());
////            } else {
//                resetPageBox();
////            }
            
            //            if (DesignerPane.debugclip) {
            //                editor.addKeyListener(updateHandler);
            //            }
            // install keymap
            installListeners();
            installKeyboardActions();
            
            // XXX This should no longer be necessary!
            
            /*
            LayoutManager oldLayout = editor.getLayout();
            if ((oldLayout == null) || (oldLayout instanceof UIResource)) {
                // by default, use default LayoutManger implementation that
                // will position the components associated with a View object.
                editor.setLayout(updateHandler);
            }
             */
        } else {
            throw new Error("DesignerPaneBaseUI needs DesignerPaneBase");
        }
    }
    
    /**
     * Deinstalls the UI for a component.  This removes the listeners,
     * uninstalls the highlighter, removes views, and nulls out the keymap.
     *
     * @param c the editor component
     * @see ComponentUI#uninstallUI
     */
    public void uninstallUI(JComponent c) {
        if(DesignerUtils.DEBUG) {
            DesignerUtils.debugLog(getClass().getName() + ".uninstallUI(JComponent)");
        }
        if(c == null) {
            throw(new IllegalArgumentException("Null component."));
        }
        
// detach from the model
        //        if (DesignerPane.debugclip) {
        //            editor.removeKeyListener(updateHandler);
        //        }
        // view part
        painted = false;
        uninstallDefaults();
        
        /*
        c.removeAll();
        LayoutManager lm = c.getLayout();
        if (lm instanceof UIResource) {
            c.setLayout(null);
        }
         */
        // controller part
        uninstallKeyboardActions();
        uninstallListeners();
    }
    
    /**
     * Superclass paints background in an uncontrollable way
     * (i.e. one might want an image tiled into the background).
     * To prevent this from happening twice, this method is
     * reimplemented to simply paint.
     * <p>
     * <em>NOTE:</em> Superclass is also not thread-safe in
     * it's rendering of the background, although that's not
     * an issue with the default rendering.
     */
    public void update(Graphics g, JComponent c) {
        paint(g, c);
    }
    
    /**
     * Paints the interface.  This is routed to the
     * paintSafely method under the guarantee that
     * the model won't change from the view of this thread
     * while it's rendering (if the associated model is
     * derived from AbstractDocument).  This enables the
     * model to potentially be updated asynchronously.
     *
     * @param g the graphics context
     * @param c the editor component
     */
    public final void paint(Graphics g, JComponent c) {
        if (pageBox != null) {
//            Document doc = editor.getDocument();
//            WebForm webform = doc.getWebForm();
            WebForm webform = editor.getWebForm();
            
//            if (webform.getMarkup() == null) {
//                return;
//            }
//            
//            if (!webform.getModel().isValid()) {
//                return;
//            }
            // XXX Model validity shouldn't be checked here.
//            if (!webform.isModelValid()) {
//                return;
//            }
  
            // XXX There should be no locking here, the designer is not thread safe (it should run in AWT thread only).
//            // XXX Gotta lock using InsyncDocument instead!!!
////            doc.readLock();
//            // XXX Why locking when painting??
////            webform.getMarkup().readLock();
//            webform.readLock();
            
            try {
                paintSafely(g);
            } catch (Exception ex) {
                log(ex);
//            } finally {
//                // XXX Gotta unlock using InsyncDocument instead!!!
//                // IF YOU GET HERE DURING DEBUGGING you just stepped over
//                // an assertion that failed! Check console/log.
////                doc.readUnlock();
////                webform.getMarkup().readUnlock();
//                webform.readUnlock();
            }
        }
    }
    
    /**
     * Gets the preferred size for the editor component.  If the component
     * has been given a size prior to receiving this request, it will
     * set the size of the box hierarchy to reflect the size of the component
     * before requesting the preferred size of the view hierarchy.  This
     * allows formatted views to format to the current component size before
     * answering the request.  Other views don't care about currently formatted
     * size and give the same answer either way.
     *
     * @param c the editor component
     * @return the size
     */
    public Dimension getPreferredSize(JComponent c) {
        if(DesignerUtils.DEBUG) {
            DesignerUtils.debugLog(getClass().getName() + ".getPreferredSize(JComponent)");
        }
        if(c == null) {
            throw(new IllegalArgumentException("Null component."));
        }
        
//        Document doc = editor.getDocument();
        Insets i = c.getInsets();
        Dimension d = c.getSize();
        
//        if ((pageBox == null) || (doc.getWebForm().getMarkup() == null)) {
        if (pageBox == null) {
            return d;
        }
        
        //System.out.println("DesignerPaneUI.getPreferredSize()");
        //System.out.println("  i=" + i);
        //System.out.println("  d=" + d);
        //System.out.println("  d.width=" + d.width);
        //System.out.println("  i.left=" + i.left);
        //System.out.println("  i.right=" + i.right);
        //System.out.println("  d.width-i.left-i.right=" + (d.width-i.left-i.right));
        // XXX There should be no locking here, the designer is not thread safe (it should run in AWT thread only).
//        // XXX Lock insync
////        doc.readLock();
//        WebForm webform = editor.getWebForm();
////        webform.getMarkup().readLock();
//        webform.readLock();
//        
//        try {
            if ((d.width > (i.left + i.right)) && (d.height > (i.top + i.bottom))) {
                //System.out.println("SETTING SIZE first");
                pageBox.setSize(d.width - i.left - i.right, d.height - i.top - i.bottom);
            } else if ((d.width == 0) && (d.height == 0)) {
                //System.out.println("width=0 and height=0 - so setting to MAX_VALUE = " + Integer.MAX_VALUE);
                // Probably haven't been layed out yet, force some sort of
                // initial sizing.
                pageBox.setSize(Integer.MAX_VALUE, Integer.MAX_VALUE);
                
                //pageBox.setSize(1024, 768);
                //pageBox.setSize(50,50);
            }
            
            d.width =
                    (int)Math.min((long)pageBox.getPreferredSpan(CssBox.X_AXIS) + (long)i.left +
                    (long)i.right, Integer.MAX_VALUE);
            d.height =
                    (int)Math.min((long)pageBox.getPreferredSpan(CssBox.Y_AXIS) + (long)i.top +
                    (long)i.bottom, Integer.MAX_VALUE);
            
            //System.out.println("Newly computed preferred span: " + d);
//        } finally {
//            // XXX Unlock insync
////            doc.readUnlock();
////            webform.getMarkup().readUnlock();
//            webform.readUnlock();
//        }
        
        return d;
    }
    
    /**
     * Gets the minimum size for the editor component.
     *
     * @param c the editor component
     * @return the size
     */
    public Dimension getMinimumSize(JComponent c) {
        if(DesignerUtils.DEBUG) {
            DesignerUtils.debugLog(getClass().getName() + ".getMinimumSize(JComponent)");
        }
        if(c == null) {
            throw(new IllegalArgumentException("Null component."));
        }
        
//        Document doc = editor.getDocument();
        Insets i = c.getInsets();
        Dimension d = new Dimension();
        
//        if ((pageBox == null) || (doc.getWebForm().getMarkup() == null)) {
        if (pageBox == null) {
            return d;
        }

        // XXX There should be no locking here, the designer is not thread safe (it should run in AWT thread only).
//        // XXX Lock insync
////        doc.readLock();
//        WebForm webform = editor.getWebForm();
////        webform.getMarkup().readLock();
//        webform.readLock();
//        
//        try {
            d.width = (int)pageBox.getMinimumSpan(CssBox.X_AXIS) + i.left + i.right;
            d.height = (int)pageBox.getMinimumSpan(CssBox.Y_AXIS) + i.top + i.bottom;
//        } finally {
//            // XXX Unlock insync
////            doc.readUnlock();
////            webform.getMarkup().readUnlock();
//            webform.readUnlock();
//        }
        
        return d;
    }
    
    /**
     * Gets the maximum size for the editor component.
     *
     * @param c the editor component
     * @return the size
     */
    public Dimension getMaximumSize(JComponent c) {
        if(DesignerUtils.DEBUG) {
            DesignerUtils.debugLog(getClass().getName() + ".getMaximumSize(JComponent)");
        }
        if(c == null) {
            throw(new IllegalArgumentException("Null component."));
        }
        
//        Document doc = editor.getDocument();
        Insets i = c.getInsets();
        Dimension d = new Dimension();
        
//        if ((pageBox == null) || (doc.getWebForm().getMarkup() == null)) {
        if (pageBox == null) {
            return d;
        }

        // XXX There should be no locking here, the designer is not thread safe (it should run in AWT thread only).
//        // XXX Lock insync
////        doc.readLock();
//        WebForm webform = editor.getWebForm();
////        webform.getMarkup().readLock();
//        webform.readLock();
//        
//        try {
            d.width =
                    (int)Math.min((long)pageBox.getMaximumSpan(CssBox.X_AXIS) + (long)i.left +
                    (long)i.right, Integer.MAX_VALUE);
            d.height =
                    (int)Math.min((long)pageBox.getMaximumSpan(CssBox.Y_AXIS) + (long)i.top +
                    (long)i.bottom, Integer.MAX_VALUE);
//        } finally {
//            // XXX Unlock insync
////            doc.readUnlock();
////            webform.getMarkup().readUnlock();
//            webform.readUnlock();
//        }
        
        return d;
    }
    
    // ---- DesignerPaneBaseUI methods -------------------------------------------
  
    // XXX Moved to WebForm.
//    /**
//     * Converts the given location in the model to a place in
//     * the view coordinate system.
//     * The component must have a non-zero positive size for
//     * this translation to be computed.
//     *
//     * @param tc the text component for which this UI is installed
//     * @param pos the local location in the model to translate >= 0
//     * @return the coordinates as a rectangle, null if the model is not painted
//     * @exception BadLocationException  if the given position does not
//     *   represent a valid location in the associated document
//     * @see DesignerPaneBaseUI#modelToView
//     */
//    public Rectangle modelToView(/*DesignerPaneBase tc,*/ Position pos) {
//        if(DesignerUtils.DEBUG) {
//            DesignerUtils.debugLog(getClass().getName() + ".modelToView(DesignerPaneBase, Position)");
//        }
////        if(tc == null) {
////            throw(new IllegalArgumentException("Null designer pane."));
////        }
//        if(pos == null) {
//            throw(new IllegalArgumentException("Null position."));
//        }
//        
//        WebForm webform = editor.getWebForm();
//        
////        if (!webform.getModel().isValid()) {
//        if (!webform.isModelValid()) {
//            return null;
//        }
//        
////        Document doc = editor.getDocument();
//        
//        // XXX Lock insync
////        doc.readLock();
////        webform.getMarkup().readLock();
//        webform.readLock();
//        
//        try {
////            return pageBox.modelToView(pos);
//            return ModelViewMapper.modelToView(pageBox, pos);
//        } finally {
//            // XXX Unlock insync
////            doc.readUnlock();
////            webform.getMarkup().readUnlock();
//            webform.readUnlock();
//        }
//    }
//    
//    /**
//     * Converts the given place in the view coordinate system
//     * to the nearest representative location in the model.
//     * The component must have a non-zero positive size for
//     * this translation to be computed.
//     *
//     * @param tc the text component for which this UI is installed
//     * @param pt the location in the view to translate.  This
//     *  should be in the same coordinate system as the mouse events.
//     * @return the offset from the start of the document >= 0,
//     *   -1 if not painted
//     * @see DesignerPaneBaseUI#viewToModel
//     */
//    public Position viewToModel(DesignerPaneBase tc, Point pt) {
//        Position pos = Position.NONE;
//        Document doc = editor.getDocument();
//        
//        // XXX Lock insync
////        doc.readLock();
//        WebForm webform = editor.getWebForm();
////        webform.getMarkup().readLock();
//        webform.readLock();
//        
//        try {
//            pos = ModelViewMapper.viewToModel(doc.getWebForm(), pt.x, pt.y); //, alloc, biasReturn);
//            
//            // I'm now relying on clients to do this themselves!
//            //assert offs == Position.NONE || Position.isSourceNode(offs.getNode());
//        } finally {
////            doc.readUnlock();
////            webform.getMarkup().readUnlock();
//            webform.readUnlock();
//        }
//        
//        return pos;
//    }
    
    /**
     * Provides a way to determine the next visually represented model
     * location that one might place a caret.  Some views may not be visible,
     * they might not be in the same order found in the model, or they just
     * might not allow access to some of the locations in the model.
     *
     * @param pos the position to convert >= 0
     * @param a the allocated region to render into
     * @param direction the direction from the current position that can
     *  be thought of as the arrow keys typically found on a keyboard.
     *  This may be SwingConstants.WEST, SwingConstants.EAST,
     *  SwingConstants.NORTH, or SwingConstants.SOUTH.
     * @return the location within the model that best represents the next
     *  location visual position.
     * @exception BadLocationException
     * @exception IllegalArgumentException for an invalid direction
     */
//    public Position getNextVisualPositionFrom(DesignerPaneBase t, Position pos, int direction) {
    public DomPosition getNextVisualPositionFrom(DesignerPaneBase t, DomPosition pos, int direction) {
        if(DesignerUtils.DEBUG) {
            DesignerUtils.debugLog(getClass().getName() + ".getNextVisualPositionFrom(DesignerPaneBase, Position, int)");
        }
        if(t == null) {
            throw(new IllegalArgumentException("Null designer pane."));
        }
        if(pos == null) {
            throw(new IllegalArgumentException("Null position."));
        }
        
//        Document doc = editor.getDocument();

        // XXX There should be no locking here, the designer is not thread safe (it should run in AWT thread only).
//        // XXX Lock insync
////        doc.readLock();
//        WebForm webform = editor.getWebForm();
////        webform.getMarkup().readLock();
//        webform.readLock();
//        
//        try {
            if (painted) {
//                ModelViewMapper mapper = doc.getWebForm().getMapper();
                WebForm webform = editor.getWebForm();
                
                switch (direction) {
                    case SwingConstants.WEST:
//                        return ModelViewMapper.computeArrowLeft(doc.getWebForm(), pos);
                        return ModelViewMapper.computeArrowLeft(webform, pos);
                    case SwingConstants.EAST:
//                        return ModelViewMapper.computeArrowRight(doc.getWebForm(), pos);
                        return ModelViewMapper.computeArrowRight(webform, pos);                        
                    case SwingConstants.NORTH:
//                        return ModelViewMapper.computeArrowUp(doc.getWebForm(), pos);
                        return ModelViewMapper.computeArrowUp(webform, pos);
                    case SwingConstants.SOUTH:
//                        return ModelViewMapper.computeArrowDown(doc.getWebForm(), pos);
                        return ModelViewMapper.computeArrowDown(webform, pos);
                }
                
//                return Position.NONE;
                return DomPosition.NONE;
            }
//        } finally {
////            doc.readUnlock();
////            webform.getMarkup().readUnlock();
//            webform.readUnlock();
//        }
        
//        return Position.NONE;
        return DomPosition.NONE;
    }
    
    /** Check the parent and see if we have a view port - if so, notify
     * the page box.
     */
    void updateViewport() {
        JViewport viewport = null;
        Component parent = editor.getParent();
        
        //System.out.println("updateViewport: Component parent=" + parent + " and it's an instanceof JViewport: " + (parent instanceof JViewport));
        if (parent instanceof JViewport) {
            viewport = (JViewport)parent;
            
            //System.out.println("--------------------------------------------------------------------------------\nFOUND VIEWPORT:" + viewport);
        }
        
        if (pageBox != null) {
            pageBox.setViewport(viewport);
        }
    }
    
    //private static final TextDragGestureRecognizer defaultDragRecognizer = new TextDragGestureRecognizer();
    
    /**
     * Registered in the ActionMap.
     */
    class FocusAction extends AbstractAction {
        public void actionPerformed(ActionEvent e) {
            editor.requestFocus();
        }
        
        public boolean isEnabled() {
            return true;
        }
    }
    
    /**
     * Drag gesture recognizer for text components.
     */
    static class TextDragGestureRecognizer extends BasicDragGestureRecognizer {
        /**
         * Determines if the following are true:
         * <ul>
         * <li>the press event is located over a selection
         * <li>the dragEnabled property is true
         * <li>A TranferHandler is installed
         * </ul>
         * <p>
         * This is implemented to check for a TransferHandler.
         * Subclasses should perform the remaining conditions.
         */
        protected boolean isDragPossible(MouseEvent e) {
            if (super.isDragPossible(e)) {
                DesignerPaneBase c = (DesignerPaneBase)this.getComponent(e);
                
//                DesignerCaret caret = c.getCaret();
//                if (caret == null) {
//                    return false;
//                }
                if (!c.hasCaret()) {
                    return false;
                }
                
//                Position dot = caret.getDot();
//                Position mark = caret.getMark();
//                DomPosition dot = caret.getDot();
//                DomPosition mark = caret.getMark();
                DomPosition dot = c.getCaretDot();
                DomPosition mark = c.getCaretMark();
                
                if (!dot.equals(mark)) {
                    Point p = new Point(e.getX(), e.getY());
//                    Position pos = c.viewToModel(p);
                    WebForm webform = ((DesignerPane)c).getWebForm();
//                    Position pos = webform.viewToModel(p);
                    DomPosition pos = webform.viewToModel(p);
                    
                    if ((webform.getManager().getInlineEditor() == null) ||
                            !webform.getManager().getInlineEditor().isDocumentEditor()) {
                        boolean findNearest = !webform.isGridMode();
//                        pos = DesignerUtils.checkPosition(pos, findNearest, /*webform*/webform.getManager().getInlineEditor());
//                        pos = ModelViewMapper.findValidPosition(pos, findNearest, /*webform*/webform.getManager().getInlineEditor());
                        pos = ModelViewMapper.findValidPosition(webform, pos, findNearest, /*webform*/webform.getManager().getInlineEditor());
                    }
                    
//                    Position p0 = Position.first(dot, mark);
//                    Position p1 = Position.last(dot, mark);
                    DomPosition p0 = webform.first(dot, mark);
                    DomPosition p1 = webform.last(dot, mark);
                    
                    if ((pos.isLaterThan(p0)) && (pos.isStrictlyEarlierThan(p1))) {
                        return true;
                    }
                }
            }
            
            return false;
        }
    }
    
    /**
     * Show a caret under the nearest insert location. Returns the
     * nearest caret location, or Position.NONE if you're not over
     * a flow text area. Call with p = null to remove the caret
     * when done.
     */
    
    /*
    public Position showInsertionLocation(Point p) {
        TextDropTargetListener dropListener = getDropTargetListener();
        return dropListener.updateInsertionLocation(getComponent(), p);
    }
     */
    
    /**
     * A DropTargetListener to extend the default Swing handling of drop operations
     * by moving the caret to the nearest location to the mouse pointer.
     */
    static class TextDropTargetListener extends BasicDropTargetListener {
        //        /**
        //         * called to set the insertion location to match the current
        //         * mouse pointer coordinates.
        //         */
        //        protected Position updateInsertionLocation(JComponent comp, Point p) {
        //            // In grid mode, no visible caret
        //            // J1 HACK: TODO - look up mode from DesignerPane instance instead
        //            DesignerPane pane = (DesignerPane)comp;
        ////            if (pane.getWebForm().isGridMode()) {
        ////                // XXX what about scrolling view when you get near the bottom
        ////                // or near the top?
        ////                return Position.NONE;
        ////            }
        //            Position pos = Position.NONE;
        //            if (p != null && Utilities.isCaretArea(pane.getWebForm(), p.x, p.y)) {
        //                pos = pane.viewToModel(p);
        //                WebForm webform = ((DesignerPane)comp).getWebForm();
        //                if (webform.getSelection().getInlineEditor() == null ||
        //                        !webform.getSelection().getInlineEditor().isDocumentEditor()) {
        //                    boolean findNearest = !webform.isGridMode();
        //                    pos = Utilities.checkPosition(pos, findNearest, webformwebform.getManager().getInlineEditor());
        //                }
        //            }
        //            if (pos != Position.NONE && FacesSupport.isBelowRendersChildren(pos)) {
        //                pos = Position.NONE;
        //            }
        //            if (pos != Position.NONE) {
        //                if (pane.getCaret() == null) {
        //                    DesignerCaret dc = pane.getPaneUI().createCaret();
        //                    pane.setCaret(dc);
        //                }
        //                pane.getCaret().setVisible(true);
        //                pane.setCaretPosition(pos);
        //            } else if (pane.getWebForm().getDocument().isGridMode()) {
        //                if (pane.getCaret() != null) {
        //                    pane.setCaret(null);
        //                }
        //            }
        //            return pos;
        //        }
//        Position dot = Position.NONE; // XXX Shouldn't that be startPos instead?
//        Position mark = Position.NONE; // XXX Shouldn't that be startPos instead?
        DomPosition dot = DomPosition.NONE; // XXX Shouldn't that be startPos instead?
        DomPosition mark = DomPosition.NONE; // XXX Shouldn't that be startPos instead?

        boolean visible;
        
        /**
         * called to save the state of a component in case it needs to
         * be restored because a drop is not performed.
         * @todo Anything else we should do here for the designer?
         */
        protected void saveComponentState(JComponent comp) {
            DesignerPaneBase c = (DesignerPaneBase)comp;
//            DesignerCaret caret = c.getCaret();
//            if (caret != null) {
//                dot = caret.getDot();
//                mark = caret.getMark();
//                visible = caret.isVisible();
//            }
            if (c.hasCaret()) {
                dot = c.getCaretPosition();
                mark = c.getCaretMark();
                visible = c.isCaretVisible();
            }
            
            // In grid mode, no visible caret
            // J1 HACK: TODO - look up mode from DesignerPane instance instead
            DesignerPane pane = (DesignerPane)comp;
            
//            if ((caret != null) && !pane.getWebForm().isGridMode()) {
//                caret.setVisible(true);
//            }
            if (c.hasCaret() && !pane.getWebForm().isGridMode()) {
                c.setCaretVisible(true);
            }
        }
        
        /**
         * called to restore the state of a component
         * because a drop was not performed.
         */
        protected void restoreComponentState(JComponent comp) {
            DesignerPaneBase c = (DesignerPaneBase)comp;
//            DesignerCaret caret = c.getCaret();
//            if (caret != null) {
//                caret.setDot(mark);
//                caret.moveDot(dot);
//                caret.setVisible(visible);
//            }
            if (c.hasCaret()) {
                c.setCaretDot(mark);
                c.moveCaretDot(dot);
                c.setCaretVisible(visible);
            }
        }
        
        /**
         * called to restore the state of a component
         * because a drop was performed.
         */
        protected void restoreComponentStateForDrop(JComponent comp) {
            DesignerPaneBase c = (DesignerPaneBase)comp;
//            DesignerCaret caret = c.getCaret();
//            if (caret != null) {
//                caret.setVisible(visible);
//            }
            if (c.hasCaret()) {
                c.setCaretVisible(visible);
            }
        }
    }
    
    
    private static void log(Exception ex) {
        Logger logger = getLogger();
        logger.log(Level.INFO, null, ex);
    }
    
    private static Logger getLogger() {
        return Logger.getLogger(DesignerPaneUI.class.getName());
    }
}
