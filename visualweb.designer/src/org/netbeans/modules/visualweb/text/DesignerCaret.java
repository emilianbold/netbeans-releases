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

package org.netbeans.modules.visualweb.text;


import java.awt.Graphics;
import java.awt.HeadlessException;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.TransferHandler;
import javax.swing.UIManager;

import org.netbeans.modules.visualweb.api.designer.DomProvider.DomDocument;
import org.netbeans.modules.visualweb.api.designer.DomProvider.DomDocumentEvent;
import org.netbeans.modules.visualweb.api.designer.DomProvider.DomDocumentListener;
import org.netbeans.modules.visualweb.api.designer.DomProvider.DomPosition;
import org.netbeans.modules.visualweb.api.designer.DomProvider.DomRange;
import org.netbeans.modules.visualweb.api.designer.markup.MarkupService;
import org.netbeans.modules.visualweb.css2.ModelViewMapper;
import org.netbeans.modules.visualweb.css2.PageBox;
import org.netbeans.modules.visualweb.designer.WebForm;
import org.netbeans.modules.visualweb.text.actions.SelectLineAction;
import org.netbeans.modules.visualweb.text.actions.SelectWordAction;

import org.openide.ErrorManager;

import org.openide.util.WeakListeners;
import org.w3c.dom.Node;


/**
 * Caret to use in flow mode editing in the designer.
 * Derived from Swing's DesignerCaret code but changed integer dot/mark
 * based code to use DOM Range objects, changed document update semantics,
 * and mouse operations since we can't blindly listen for mouse clicks
 * we have to take directions from the SelectionManager.
 *
 * @todo Stop using Position objects in calls to modelToView etc;
 *   instead pass node and offset.
 *
 * @author  Timothy Prinzing
 * @author  Tor Norbye
 */
public class DesignerCaret extends Rectangle implements FocusListener, MouseListener,
    MouseMotionListener {
    private static transient Action selectWord = null;
    private static transient Action selectLine = null;
    private transient boolean installed;

    // ---- member variables ------------------------------------------
    // package-private to avoid inner classes private member
    // access bug
    DesignerPaneBase component;

    /**
     * flag to indicate if async updates should move the caret.
     */
    boolean async;
    boolean visible;
    boolean flashOn;

    //Position dot = Position.NONE;
    //Position mark = Position.NONE;
//    Range range = null;
    DomRange range = null;
    
    Object selectionTag;
    boolean selectionVisible;
    Timer flasher;
    Point magicCaretPosition;
    private final Handler handler = new Handler();

    /**
     * This is used to indicate if the caret currently owns the selection. This is always false if
     * the system does not support the system clipboard.
     */
    private boolean ownsSelection;

    /**
     * If this is true, the location of the dot is updated regardless of the current location. This
     * is set in the DocumentListener such that even if the model location of dot hasn't changed
     * (perhaps do to a forward delete) the visual location is updated.
     */
    private boolean forceCaretPositionChange;

    /**
     * Whether or not mouseReleased should adjust the caret and focus. This flag is set by
     * mousePressed if it wanted to adjust the caret and focus but couldn't because of a possible
     * DnD operation.
     */
    private transient boolean shouldHandleRelease;

    /**
     * Constructs a default caret.
     */
    public DesignerCaret() {
        async = false;
    }

    
    public DomRange getRange() {
        return range;
    }
    
    /**
     * Get the flag that determines whether or not asynchronous updates will move the caret.
     * Normally the caret is moved by events from the event thread such as mouse or keyboard events.
     * Changes from another thread might be used to load a file, or show changes from another user.
     * This flag determines whether those changes will move the caret.
     */
    boolean getAsynchronousMovement() {
        return async;
    }

    /**
     * Set the flag that determines whether or not asynchronous updates will move the caret.
     * Normally the caret is moved by events from the event thread such as mouse or keyboard events.
     * Changes from another thread might be used to load a file, or show changes from another user.
     * This flag determines whether those changes will move the caret.
     *
     * @param m
     *            move the caret on asynchronous updates if true.
     */
    void setAsynchronousMovement(boolean m) {
        async = m;
    }

    /**
     * Gets the text editor component that this caret is is bound to.
     *
     * @return the component
     */
    protected final DesignerPaneBase getComponent() {
        return component;
    }

    /**
     * Cause the caret to be painted. The repaint area is the bounding box of the caret (i.e. the
     * caret rectangle or <em>this</em>).
     * <p>
     * This method is thread safe, although most Swing methods are not. Please see <A
     * HREF="http://java.sun.com/products/jfc/swingdoc-archive/threads.html"> Threads and Swing </A>
     * for more information.
     */
    protected final synchronized void repaint() {
        if (component != null) {
            component.repaint(x, y, width, height);
        }
    }

    /**
     * Cause the selection region to be painted.
     */
    protected final synchronized void repaintSelection() {
        if (component != null) {
            component.repaint();
        }
    }

    /**
     * Damages the area surrounding the caret to cause it to be repainted in a new location. If
     * paint() is reimplemented, this method should also be reimplemented. This method should update
     * the caret bounds (x, y, width, and height).
     *
     * @param r
     *            the current location of the caret
     * @see #paint
     */
    protected synchronized void damage(Rectangle r) {
        if (r != null) {
            x = r.x - 4;
            y = r.y;
            width = 10;
            height = r.height;
            repaint();
        }
    }

    /**
     * Scrolls the associated view (if necessary) to make the caret visible. Since how this should
     * be done is somewhat of a policy, this method can be reimplemented to change the behavior. By
     * default the scrollRectToVisible method is called on the associated component.
     *
     * @param nloc
     *            the new position to scroll to
     */
    protected void adjustVisibility(Rectangle nloc) {
        if (component == null) {
            return;
        }

        if (SwingUtilities.isEventDispatchThread()) {
            component.scrollRectToVisible(nloc);
        } else {
            SwingUtilities.invokeLater(new SafeScroller(nloc));
        }
    }

    /**
     * Tries to set the position of the caret from the coordinates of a mouse event, using
     * viewToModel().
     *
     * @param e
     *            the mouse event
     */
    private /*protected*/ void positionCaret(MouseEvent e) { // XXX When is this used?

        Point pt = new Point(e.getX(), e.getY());
//        Position pos = component.getUI().viewToModel(component, pt);
//        WebForm webform = component.getDocument().getWebForm();
        WebForm webform = component.getWebForm();
        
//        Position pos = webform.viewToModel(pt);
        DomPosition pos = webform.viewToModel(pt);

        if ((webform.getManager().getInlineEditor() == null) ||
                !webform.getManager().getInlineEditor().isDocumentEditor()) {
//            pos = DesignerUtils.checkPosition(pos, true, /*webform*/webform.getManager().getInlineEditor());
//            pos = ModelViewMapper.findValidPosition(pos, true, /*webform*/webform.getManager().getInlineEditor());
            pos = ModelViewMapper.findValidPosition(webform, pos, true, /*webform*/webform.getManager().getInlineEditor());
        }

//        if (pos != Position.NONE) {
        if (pos != DomPosition.NONE) {
            setDot(pos);
        }
    }

    /**
     * Tries to move the position of the caret from the coordinates of a mouse event, using
     * viewToModel(). This will cause a selection if the dot and mark are different.
     *
     * @param e
     *            the mouse event
     */
    private /*protected*/ void moveCaret(MouseEvent e) {
        Point pt = new Point(e.getX(), e.getY());
//        Position pos = component.getUI().viewToModel(component, pt);
//        WebForm webform = component.getDocument().getWebForm();
        WebForm webform = component.getWebForm();
        
//        Position pos = webform.viewToModel(pt);
        DomPosition pos = webform.viewToModel(pt);        

        if ((webform.getManager().getInlineEditor() == null) ||
                !webform.getManager().getInlineEditor().isDocumentEditor()) {
//            pos = DesignerUtils.checkPosition(pos, true, /*webform*/webform.getManager().getInlineEditor());
//            pos = ModelViewMapper.findValidPosition(pos, true, /*webform*/webform.getManager().getInlineEditor());
            pos = ModelViewMapper.findValidPosition(webform, pos, true, /*webform*/webform.getManager().getInlineEditor());
        }

//        if (pos != Position.NONE) {
        if (pos != DomPosition.NONE) {
            moveDot(pos);
        }
    }

    // --- FocusListener methods --------------------------

    /**
     * Called when the component containing the caret gains focus. This is implemented to set the
     * caret to visible if the component is editable.
     *
     * @param e
     *            the focus event
     * @see FocusListener#focusGained
     */
    public void focusGained(FocusEvent e) {
        if (component.isEnabled()) {
            setVisible(true);
            setSelectionVisible(true);
        }
    }

    /**
     * Called when the component containing the caret loses focus. This is implemented to set the
     * caret to visibility to false.
     *
     * @param e
     *            the focus event
     * @see FocusListener#focusLost
     */
    public void focusLost(FocusEvent e) {
        setVisible(false);
        setSelectionVisible(ownsSelection || e.isTemporary());
    }

    // --- MouseListener methods -----------------------------------

    /**
     * Called when the mouse is clicked. If the click was generated from button1, a double click
     * selects a word, and a triple click the current line.
     *
     * @param e
     *            the mouse event
     * @see MouseListener#mouseClicked
     */
    public void mouseClicked(MouseEvent e) {
        if (!e.isConsumed()) {
            int nclicks = e.getClickCount();

            if (SwingUtilities.isLeftMouseButton(e)) {
                // mouse 1 behavior
                if (e.getClickCount() == 2) {
                    Action a = null;
                    ActionMap map = getComponent().getActionMap();

                    if (map != null) {
                        a = map.get(DesignerPaneBase.selectWordAction);
                    }

                    if (a == null) {
                        if (selectWord == null) {
                            selectWord = new SelectWordAction();
                        }

                        a = selectWord;
                    }

                    a.actionPerformed(new ActionEvent(getComponent(), ActionEvent.ACTION_PERFORMED,
                            null, e.getWhen(), e.getModifiers()));
                } else if (e.getClickCount() == 3) {
                    Action a = null;
                    ActionMap map = getComponent().getActionMap();

                    if (map != null) {
                        a = map.get(DesignerPaneBase.selectLineAction);
                    }

                    if (a == null) {
                        if (selectLine == null) {
                            selectLine = new SelectLineAction();
                        }

                        a = selectLine;
                    }

                    a.actionPerformed(new ActionEvent(getComponent(), ActionEvent.ACTION_PERFORMED,
                            null, e.getWhen(), e.getModifiers()));
                }
            } else if (SwingUtilities.isMiddleMouseButton(e)) {
                // mouse 2 behavior
                if (nclicks == 1) {
                    // paste system selection, if it exists
                    DesignerPaneBase c = (DesignerPaneBase)e.getSource();

                    if (c != null) {
                        try {
                            Toolkit tk = c.getToolkit();
                            Clipboard buffer = tk.getSystemSelection();

                            if (buffer != null) {
                                // platform supports system selections, update it.
                                adjustCaret(e);

                                TransferHandler th = c.getTransferHandler();

                                if (th != null) {
                                    Transferable trans = buffer.getContents(null);

                                    if (trans != null) {
                                        th.importData(c, trans);
                                    }
                                }

                                adjustFocus(true);
                            }
                        } catch (HeadlessException he) {
                            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, he);

                            // do nothing... there is no system clipboard
                        }
                    }
                }
            }
        }
    }

    /**
     * If button 1 is pressed, this is implemented to request focus on the associated text
     * component, and to set the caret position. If the shift key is held down, the caret will be
     * moved, potentially resulting in a selection, otherwise the caret position will be set to the
     * new location. If the component is not enabled, there will be no request for focus.
     *
     * @param e
     *            the mouse event
     * @see MouseListener#mousePressed
     */
    public void mousePressed(MouseEvent e) {
        if (SwingUtilities.isLeftMouseButton(e)) {
            if (e.isConsumed()) {
                shouldHandleRelease = true;
            } else {
                shouldHandleRelease = false;
                adjustCaretAndFocus(e);
            }
        }
    }

    private void adjustCaretAndFocus(MouseEvent e) {
        adjustCaret(e);
        adjustFocus(false);
    }

    /**
     * Adjusts the caret location based on the MouseEvent.
     */
    private void adjustCaret(MouseEvent e) {
//        if (((e.getModifiers() & ActionEvent.SHIFT_MASK) != 0) && (getDot() != Position.NONE)) {
        if (((e.getModifiers() & ActionEvent.SHIFT_MASK) != 0) && (getDot() != DomPosition.NONE)) {
            moveCaret(e);
        } else {
            positionCaret(e);
        }
    }

    /**
     * Adjusts the focus, if necessary.
     *
     * @param inWindow
     *            if true indicates requestFocusInWindow should be used
     */
    private void adjustFocus(boolean inWindow) {
        if ((component != null) && component.isEnabled() && component.isRequestFocusEnabled()) {
            if (inWindow) {
                component.requestFocusInWindow();
            } else {
                component.requestFocus();
            }
        }
    }

    /**
     * Called when the mouse is released.
     *
     * @param e
     *            the mouse event
     * @see MouseListener#mouseReleased
     */
    public void mouseReleased(MouseEvent e) {
        if (shouldHandleRelease && SwingUtilities.isLeftMouseButton(e)) {
            adjustCaretAndFocus(e);
        }
    }

    /**
     * Called when the mouse enters a region.
     *
     * @param e
     *            the mouse event
     * @see MouseListener#mouseEntered
     */
    public void mouseEntered(MouseEvent e) {
    }

    /**
     * Called when the mouse exits a region.
     *
     * @param e
     *            the mouse event
     * @see MouseListener#mouseExited
     */
    public void mouseExited(MouseEvent e) {
    }

    // --- MouseMotionListener methods -------------------------

    /**
     * Moves the caret position according to the mouse pointer's current location. This effectively
     * extends the selection. By default, this is only done for mouse button 1.
     *
     * @param e
     *            the mouse event
     * @see MouseMotionListener#mouseDragged
     */
    public void mouseDragged(MouseEvent e) {
        if ((!e.isConsumed()) && SwingUtilities.isLeftMouseButton(e)) {
            moveCaret(e);
        }
    }

    /**
     * Called when the mouse is moved.
     *
     * @param e
     *            the mouse event
     * @see MouseMotionListener#mouseMoved
     */
    public void mouseMoved(MouseEvent e) {
    }

    // ---- Caret methods ---------------------------------

    /**
     * Renders the caret as a vertical line. If this is reimplemented the damage method should also
     * be reimplemented as it assumes the shape of the caret is a vertical line. Sets the caret
     * color to the value returned by getCaretColor().
     * <p>
     *
     * @param g
     *            the graphics context
     * @see #damage
     */
    public void paint(Graphics g) {
        if (isVisible() && flashOn) {
            DesignerPaneBaseUI mapper = component.getUI();

            if (range == null) {
                return;
            }

//            Position dot = range.getDot();
            DomPosition dot = range.getDot();
//            Rectangle r = mapper.modelToView(/*component,*/ dot);
//            WebForm webForm = component.getDocument().getWebForm();
            WebForm webForm = component.getWebForm();
            
            Rectangle r = webForm.modelToView(dot);

            if ((r == null) || ((r.width == 0) && (r.height == 0))) {
                return;
            }

            if ((width > 0) && (height > 0) && !this._contains(r.x, r.y, r.width, r.height)) {
                // We seem to have gotten out of sync and no longer
                // contain the right location, adjust accordingly.
                Rectangle clip = g.getClipBounds();

                if ((clip != null) && !clip.contains(this)) {
                    // Clip doesn't contain the old location, force it
                    // to be repainted lest we leave a caret around.
                    repaint();
                }

                // This will potentially cause a repaint of something
                // we're already repainting, but without changing the
                // semantics of damage we can't really get around this.
                damage(r);
            }

            g.setColor(component.getCaretColor());

            //g.setColor(java.awt.Color.RED);
            g.drawLine(r.x, r.y, r.x, (r.y + r.height) - 1);
            g.drawLine(r.x + 1, r.y, r.x + 1, (r.y + r.height) - 1);

            // see if we should paint a flag to indicate the bias
            // of the caret.
            // PENDING(prinz) this should be done through
            // protected methods so that alternative LAF
            // will show bidi information.
        }
    }

    /**
     * Called when the UI is being installed into the interface of a JTextComponent. This can be
     * used to gain access to the model that is being navigated by the implementation of this
     * interface. Sets the dot and mark to 0, and establishes document, property change, focus,
     * mouse, and mouse motion listeners.
     *
     * @param c
     *            the component
     */
    public void install(DesignerPaneBase c) {
//        assert !installed;
        if (installed) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
                    new IllegalStateException("The designer caret is installed already!")); // NOI18N
            return;
        }

        installed = true;
        component = c;

        // NO caret until you click to set one (switching to flow mode
        // should also do that
        //dot = mark = Position.NONE;
        c.addFocusListener(this);
        
//        c.getDocument().addDomDocumentListener(handler);
//        c.getWebForm().getDocument().addDomDocumentListener(handler);
        // XXX #123003 Another memory leak via the listener (missing call to deinstall), using weak listener instead.
//        c.getWebForm().getDomDocument().addDomDocumentListener(handler);
        DomDocument domDocument = c.getWebForm().getDomDocument();
        DomDocumentListener weakListener = WeakListeners.create(DomDocumentListener.class, handler, domDocument);
        domDocument.addDomDocumentListener(weakListener);

        // if the component already has focus, it won't
        // be notified.
        if (component.hasFocus()) {
            focusGained(null);
        }
    }

    /**
     * Called when the UI is being removed from the interface of a JTextComponent. This is used to
     * unregister any listeners that were attached.
     *
     * @param c
     *            the component
     */
    public void deinstall(DesignerPaneBase c) {
//        assert installed;
        if (!installed) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
                    new IllegalStateException("The designer caret was not installed before!")); // NOI18N
            return;
        }

        installed = false;
        c.removeFocusListener(this);
        
//        c.getDocument().removeDomDocumentListener(handler);
//        c.getWebForm().getDocument().removeDomDocumentListener(handler);
//        c.getWebForm().getDomDocument().removeDomDocumentListener(handler);

        synchronized (this) {
            component = null;
        }

        if (flasher != null) {
            flasher.stop();
            flasher = null;
        }

        if (range != null) {
            range.detach();
            range = null;
        }
    }

    /**
     * Changes the selection visibility.
     *
     * @param vis
     *            the new visibility
     */
    public void setSelectionVisible(boolean vis) {
        if (vis != selectionVisible) {
            selectionVisible = vis;

            if (!selectionVisible || hasSelection()) {
                // XXX this causes a global repaint... notagood!
                repaintSelection();
            }
        }
    }

    /**
     * Checks whether the current selection is visible.
     *
     * @return true if the selection is visible
     */
    public boolean isSelectionVisible() {
        return selectionVisible;
    }

    /**
     * Determines if the caret is currently visible.
     *
     * @return true if visible else false
     */
    public boolean isVisible() {
        return visible;
    }

    /**
     * Sets the caret visibility, and repaints the caret.
     *
     * @param e
     *            the visibility specifier
     */
    public void setVisible(boolean e) {
        // focus lost notification can come in later after the
        // caret has been deinstalled, in which case the component
        // will be null.
        if (component != null) {
            DesignerPaneBaseUI mapper = component.getUI();

            if (visible != e) {
                visible = e;

                // repaint the caret
                if (range != null) {
//                    Position dot = range.getDot();
                    DomPosition dot = range.getDot();
//                    Rectangle loc = mapper.modelToView(/*component,*/ dot);
//                    WebForm webForm = component.getDocument().getWebForm();
                    WebForm webForm = component.getWebForm();
                    
                    Rectangle loc = webForm.modelToView(dot);
                    damage(loc);
                }
            }
        }

        if (flasher != null) {
            if (visible) {
                flashOn = true;
                flasher.start();
            } else {
                flasher.stop();
            }
        }
    }

    /**
     * Sets the caret blink rate.
     *
     * @param rate
     *            the rate in milliseconds, 0 to stop blinking
     */
    public void setBlinkRate(int rate) {
        if (rate != 0) {
            if (flasher == null) {
                flasher = new Timer(rate, handler);
            }

            flasher.setDelay(rate);
        } else {
            if (flasher != null) {
                flasher.stop();
                flasher.removeActionListener(handler);
                flasher = null;
                flashOn = true;
            }
        }
    }

    /**
     * Gets the caret blink rate.
     *
     * @return the delay in milliseconds. If this is zero the caret will not blink.
     */
    public int getBlinkRate() {
        return (flasher == null) ? 0 : flasher.getDelay();
    }

    /**
     * Fetches the current position of the caret.
     * The caret dot position is always a source position.
     *
     * @return the position >= 0
     */
//    public Position getDot() {
    public DomPosition getDot() {
        if (range == null) {
//            return Position.NONE;
            return DomPosition.NONE;
        }

        return range.getDot();
    }

    /**
     * Fetches the current position of the mark. If there is a selection, the dot and mark will not
     * be the same.
     * The caret mark position is always a source position.
     *
     * @return the position >= 0
     */
//    public Position getMark() {
    public DomPosition getMark() {
        if (range == null) {
//            return Position.NONE;
            return DomPosition.NONE;
        }

        return range.getMark();
    }

    /**
     * Return the first endpoint of the range in the document.
     * This is always a source position.
     */
//    public Position getFirstPosition() {
    public DomPosition getFirstPosition() {
        if (range == null) {
//            return Position.NONE;
            return DomPosition.NONE;
        }

        return range.getFirstPosition();
    }

    /**
     * Return the second/last endpoint of the range in the document.
     * This is always a source position.
     */
//    public Position getLastPosition() {
    public DomPosition getLastPosition() {
        if (range == null) {
//            return Position.NONE;
            return DomPosition.NONE;
        }

        return range.getLastPosition();
    }

    /**
     * Sets the caret position and mark to some position. This implicitly sets the selection range
     * to zero.
     *
     * @param dot
     *            the position >= 0
     */
//    public void setDot(Position dot) {
    public void setDot(DomPosition dot) {
        handleSetDot(dot);
    }

    /**
     * Moves the caret position to some other position.
     *
     * @param dot
     *            the position >= 0
     */
//    public void moveDot(Position dot) {
    public void moveDot(DomPosition dot) {
        /*
         * We don't have disabled designer panes... if (! component.isEnabled()) { // don't allow
         * selection on disabled components. setDot(dot, dotBias); return; }
         */
//        if ((dot == Position.NONE) && (range == null)) {
        if ((dot == DomPosition.NONE) && (range == null)) {
            return;
        } else if ((range != null) && range.isDot(dot)) {
            return;
        }

        handleMoveDot(dot);
    }

//    void handleMoveDot(Position dot) {
    void handleMoveDot(DomPosition dot) {
//        assert !dot.isRendered();
//        if (MarkupService.isRenderedNode(dot.getNode())) {
        if (component.getWebForm().isRenderedNode(dot.getNode())) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
                    new IllegalArgumentException("The node is expected not rendered" + dot.getNode())); // NOI18N
        }

        // XXX 142785 Possible NPE
        PageBox pageBox = component.getWebForm().getPane().getPageBox();
        if (pageBox == null) {
            return;
        }
        // XXX Very suspicious assertion.
//        assert component.getDocument().getWebForm().getPane().getPageBox().getElement().getOwnerDocument() == component.getDocument().getWebForm().getJspDom();
//        if (component.getDocument().getWebForm().getPane().getPageBox().getElement().getOwnerDocument() != component.getDocument().getWebForm().getHtmlDom()) {
//            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
//                    new IllegalStateException("Owner document is expected to be html dom=" + component.getDocument().getWebForm().getHtmlDom() // NOI18N
//                    + ", but it is dom=" + component.getDocument().getWebForm().getPane().getPageBox().getElement().getOwnerDocument())); // NOI18N
//        }
        if (pageBox.getElement().getOwnerDocument() != component.getWebForm().getHtmlDom()) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
                    new IllegalStateException("Owner document is expected to be html dom=" + component.getWebForm().getHtmlDom() // NOI18N
                    + ", but it is dom=" + pageBox.getElement().getOwnerDocument())); // NOI18N
        }

        changeCaretPosition(dot);

        setSelectionVisible(hasSelection());

        if (hasSelection()) {
            repaintSelection();
        }
    }

//    void handleSetDot(Position dot) {
    void handleSetDot(DomPosition dot) {
        // move dot, if it changed

        /*
         * Document doc = component.getDocument(); if (doc != null) { dot = Position.first(dot,
         * doc.getEndPosition()); } dot = Position.last(dot, doc.getStartPosition());
         */
        // XXX #124732 Possible NPE.
//        if (dot == Position.NONE) {
        if (component == null || dot == null || dot == DomPosition.NONE) {
            if (range != null) {
                range.detach();
                range = null;
            }

            // TODO: Gotta clear selection highlights here!
            return;
        }

//        assert !dot.isRendered() ||
//        component.getDocument().getWebForm().getManager().isInlineEditing() : dot;
//        if (MarkupService.isRenderedNode(dot.getNode())
        if (component.getWebForm().isRenderedNode(dot.getNode())
//        && !component.getDocument().getWebForm().getManager().isInlineEditing()) {
        && !component.getWebForm().getManager().isInlineEditing()) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
                    new IllegalStateException("It is not in inline editing, and node is rendered node=" + dot.getNode())); // NOI18N
        }
        
        // XXX 142785 Possible NPE
        PageBox pageBox = component.getWebForm().getPane().getPageBox();
        if (pageBox == null) {
            return;
        }
        // XXX Very suspicious assertion.
//        assert component.getDocument().getWebForm().getPane().getPageBox().getElement().getOwnerDocument() == component.getDocument().getWebForm().getJspDom();
//        if (component.getDocument().getWebForm().getPane().getPageBox().getElement().getOwnerDocument() != component.getDocument().getWebForm().getHtmlDom()) {
//            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
//                    new IllegalStateException("Owner document is expected to be html dom=" + component.getDocument().getWebForm().getHtmlDom() // NOI18N
//                    + ", but it is dom=" + component.getDocument().getWebForm().getPane().getPageBox().getElement().getOwnerDocument())); // NOI18N
//        }
        if (pageBox.getElement().getOwnerDocument() != component.getWebForm().getHtmlDom()) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
                    new IllegalStateException("Owner document is expected to be html dom=" + component.getWebForm().getHtmlDom() // NOI18N
                    + ", but it is dom=" + pageBox.getElement().getOwnerDocument())); // NOI18N
        }

        Node node = dot.getNode();
        int offset = dot.getOffset();

        // Set the mark to the new dot position
        boolean dotChanged;

        if (range == null) {
//            range = new Range(component.getDocument().getWebForm(), node, offset, node, offset);
//            range = Range.create(component.getDocument().getWebForm(), node, offset, node, offset);
//            range = Range.create(component.getWebForm(), node, offset, node, offset);
            range = component.getWebForm().createDomRange(node, offset, node, offset);
            
            dotChanged = true;
        } else {
            range.setMark(node, offset, dot.getBias());

            //            dotChanged = range.isDot(dot);
            // When we're using dom ranges they offset will always
            // have been preadjusted so we don't 
            dotChanged = true;
        }

        if (dotChanged || (selectionTag != null) || forceCaretPositionChange) {
            changeCaretPosition(dot);
        }

        setSelectionVisible(hasSelection());

        //        if (System.getProperty("rave.debugLayout") != null) {
        //            org.openide.awt.StatusDisplayer.getDefault().setStatusText("Caret=" + getDot());
        //        }
    }

    // ---- local methods --------------------------------------------

    /**
     * Sets the caret position (dot) to a new location. This causes the old and new location to be
     * repainted. It also makes sure that the caret is within the visible region of the view, if the
     * view is scrollable.
     */
//    void changeCaretPosition(Position dot) {
    void changeCaretPosition(DomPosition dot) {
        // repaint the old position and set the new value of
        // the dot.
        repaint();

        // Make sure the caret is visible if this window has the focus.
        if ((flasher != null) && flasher.isRunning()) {
            flashOn = true;
            flasher.restart();
        }

        // notify listeners at the caret moved
        //this.dot = dot;
        //NPE: Why can range be null sometimes
        // XXX #94270 I would like that know too. For now just hacking the issue.
//        range.setDot(dot.getNode(), dot.getOffset(), dot.getBias());
        if (range == null) {
            // XXX Log the problem?
//            range = new Range(component.getDocument().getWebForm(), dot.getNode(), dot.getOffset(), dot.getNode(), dot.getOffset());
//            range = Range.create(component.getDocument().getWebForm(), dot.getNode(), dot.getOffset(), dot.getNode(), dot.getOffset());
//            range = Range.create(component.getWebForm(), dot.getNode(), dot.getOffset(), dot.getNode(), dot.getOffset());
            range = component.getWebForm().createDomRange(dot.getNode(), dot.getOffset(), dot.getNode(), dot.getOffset());
        } else {
            range.setDot(dot.getNode(), dot.getOffset(), dot.getBias());
        }

        updateSystemSelection();

        setMagicCaretPosition(null);

        // We try to repaint the caret later, since things
        // may be unstable at the time this is called
        // (i.e. we don't want to depend upon notification
        // order or the fact that this might happen on
        // an unsafe thread).
        Runnable callRepaintNewCaret =
            new Runnable() {
                public void run() {
                    repaintNewCaret();
                }
            };

        SwingUtilities.invokeLater(callRepaintNewCaret);
    }

    /**
     * Repaints the new caret position, with the assumption that this is happening on the event
     * thread so that calling <code>modelToView</code> is safe.
     */
    void repaintNewCaret() {
        if (component != null) {
            DesignerPaneBaseUI mapper = component.getUI();
//            Document doc = component.getDocument();

            if ((mapper != null) /*&& (doc != null)*/ && (range != null)) {
                // determine the new location and scroll if
                // not visible.
//                Position dot = range.getDot();
                DomPosition dot = range.getDot();
//                Rectangle newLoc = mapper.modelToView(/*component,*/ dot);
//                WebForm webForm = component.getDocument().getWebForm();
                WebForm webForm = component.getWebForm();
                
                Rectangle newLoc = webForm.modelToView(dot);

                if (newLoc != null) {
                    adjustVisibility(newLoc);

                    // If there is no magic caret position, make one
                    if (getMagicCaretPosition() == null) {
                        setMagicCaretPosition(new Point(newLoc.x, newLoc.y));
                    }
                }

                // repaint the new position
                damage(newLoc);
            }
        }
    }

    private void updateSystemSelection() {
        if (!range.isEmpty() && (component != null)) {
            Clipboard clip = getSystemSelection();

            if (clip != null) {
                String rangeText = component.getWebForm().getDomDocument().getRangeText(range);
                
                clip.setContents(new StringSelection(rangeText), getClipboardOwner());
                ownsSelection = true;
            }
        }
    }

    private Clipboard getSystemSelection() {
        try {
            return component.getToolkit().getSystemSelection();
        } catch (HeadlessException he) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, he);

            // do nothing... there is no system clipboard
        }

        return null;
    }

    private ClipboardOwner getClipboardOwner() {
        return handler;
    }

    /**
     * Saves the current caret position. This is used when caret up/down actions occur, moving
     * between lines that have uneven end positions.
     *
     * @param p
     *            the position
     * @see #getMagicCaretPosition
     */
    public void setMagicCaretPosition(Point p) {
        magicCaretPosition = p;
    }

    /**
     * Gets the saved caret position.
     *
     * @return the position see #setMagicCaretPosition
     */
    public Point getMagicCaretPosition() {
        return magicCaretPosition;
    }

    /** Notify caret that the range it is attached to is no longer valid */
    public void detachDom() {
        if (range != null) {
            range.detach();
            range = null;
        }
    }

    /**
     * Compares this object to the specified object. The superclass behavior of comparing rectangles
     * is not desired, so this is changed to the Object behavior.
     *
     * @param obj
     *            the object to compare this font with
     * @return <code>true</code> if the objects are equal; <code>false</code> otherwise
     */
    public boolean equals(Object obj) {
        return (this == obj);
    }

    // XXX This class should override hashCode too! I recently alerted the Swing group to this too.
    public String toString() {
        if (range != null) {
            return range.toString();
        } else {
            return "Caret-range is nul";
        }
    }

    // Rectangle.contains returns false if passed a rect with a w or h == 0,
    // this won't (assuming X,Y are contained with this rectangle).
    private boolean _contains(int X, int Y, int W, int H) {
        int w = this.width;
        int h = this.height;

        if ((w | h | W | H) < 0) {
            // At least one of the dimensions is negative...
            return false;
        }

        // Note: if any dimension is zero, tests below must return false...
        int x = this.x;
        int y = this.y;

        if ((X < x) || (Y < y)) {
            return false;
        }

        if (W > 0) {
            w += x;
            W += X;

            if (W <= X) {
                // X+W overflowed or W was zero, return false if...
                // either original w or W was zero or
                // x+w did not overflow or
                // the overflowed x+w is smaller than the overflowed X+W
                if ((w >= x) || (W > w)) {
                    return false;
                }
            } else {
                // X+W did not overflow and W was not zero, return false if...
                // original w was zero or
                // x+w did not overflow and x+w is smaller than X+W
                if ((w >= x) && (W > w)) {
                    return false;
                }
            }
        } else if ((x + w) < X) {
            return false;
        }

        if (H > 0) {
            h += y;
            H += Y;

            if (H <= Y) {
                if ((h >= y) || (H > h)) {
                    return false;
                }
            } else {
                if ((h >= y) && (H > h)) {
                    return false;
                }
            }
        } else if ((y + h) < Y) {
            return false;
        }

        return true;
    }

    /*private*/ void removeSelection() {
//        assert range != null;
        if (range == null) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
                    new IllegalStateException("Range is null.")); // NOI18N
            return;
        }
//        range.deleteContents();
        boolean success = component.getWebForm().getDomDocument().deleteRangeContents(range);
        if (!success) {
            // Read-only - can't edit!!
//            UIManager.getLookAndFeel().provideErrorFeedback(webform.getPane());
            UIManager.getLookAndFeel().provideErrorFeedback(component);
        }
    }
    
    String getSelectedText() {
        if (range == null) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
                    new IllegalStateException("Range is null.")); // NOI18N
            return ""; // NOI18N
        }
//        return range.getText();
        if (component == null) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
                    new IllegalStateException("Component is null.")); // NOI18N
            return ""; // NOI18N
        }
        return component.getWebForm().getDomDocument().getRangeText(range);
    }

    /**
     * Return true iff we have a selection - or put differently, if the dot is at a different
     * location than the mark.
     */
    public boolean hasSelection() {
        return (range != null) && !range.isEmpty();
    }

    // XXX Moved to DesignerPaneBase.
//    /** Return the text in the selection, if any (if not returns null).
//     * If the cut parameter is true, then the selection is deleted too.
//     */
//    public Transferable copySelection(boolean cut) {
//        if (hasSelection()) {
//            String text = range.getText();
//            assert text.length() > 0;
//
//            Transferable transferable = new StringSelection(text);
//
//            if (cut) {
//                removeSelection();
//            }
//
//            return transferable;
//        } else {
//            return new StringSelection("");
//        }
//    }

    // XXX Moved to DesignerPaneBase.
//    /**
//     * Replace the selection. Beep if within a read-only region.
//     */
//    public void replaceSelection(String content) {
////        WebForm webform = component.getDocument().getWebForm();
//        WebForm webform = component.getWebForm();
//        
//        InlineEditor editor = webform.getManager().getInlineEditor();
//
//        if ((content.equals("\n") || content.equals("\r\n")) && // NOI18N
//                (editor != null) && !editor.isMultiLine()) {
//            // Commit
//            // Should I look to see if the Shift key is pressed, and if so let
//            // you insert a newline?
//            webform.getManager().finishInlineEditing(false);
//
//            return;
//        }
//
//        /*
//        if (range.isReadOnlyRegion()) {
//            UIManager.getLookAndFeel().provideErrorFeedback(component);
//            return;
//        }
//         */
//        if (hasSelection()) {
//            removeSelection();
//        }
//
////        Position pos = getDot();
//        DomPosition pos = getDot();
//
//        if (editor == null) {
////            assert (pos == Position.NONE) || !pos.isRendered();
////            if (pos != Position.NONE && MarkupService.isRenderedNode(pos.getNode())) {
//            if (pos != DomPosition.NONE && MarkupService.isRenderedNode(pos.getNode())) {
//                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
//                        new IllegalStateException("Node is expected to be not rendered, node=" + pos.getNode())); // NOI18N
//            }
//        } // else: Stay in the DocumentFragment; don't jump to the source DOM (there is none)
//
////        if (pos == Position.NONE) {
//        if (pos == DomPosition.NONE) {
//            UIManager.getLookAndFeel().provideErrorFeedback(component);
//
//            return;
//        }
//
////        component.getDocument().insertString(this, pos, content);
////        component.getDocument().insertString(pos, content);
//        component.getWebForm().getDomDocument().insertString(pos, content);
//    }

    // XXX Moved to designer/jsf/../DomDocumentIpmpl.
    /**
     * @todo Check deletion back to first char in <body> !
     * @todo Refactor document mutation into the document class
     * @todo Check read-only state etc
     */
    public boolean removeNextChar() {
        // TODO - compute previous visual position, decide if it's
//        //    isWithinEditableRegion(Position pos) 
//        // and if so, set the range to it and delete the range.
//        if (hasSelection()) {
//            removeSelection();
//
//            return true;
//        }
//
////        Document doc = component.getDocument();
////        Position mark = range.getMark();
//        DomPosition mark = range.getMark();
////        Position dot = ModelViewMapper.computeArrowRight(doc.getWebForm(), mark);
////        Position dot = ModelViewMapper.computeArrowRight(component.getWebForm(), mark);
//        DomPosition dot = ModelViewMapper.computeArrowRight(component.getWebForm(), mark);
//
////        if ((dot == Position.NONE) || !isWithinEditableRegion(dot)) {
//        if ((dot == DomPosition.NONE) || !isWithinEditableRegion(dot)) {
//            UIManager.getLookAndFeel().provideErrorFeedback(component); // beep
//
//            return false;
//        }
//
//        range.setRange(dot.getNode(), dot.getOffset(), mark.getNode(), mark.getOffset());
////        range.deleteContents();
//        removeSelection();
//
//        return true;
        WebForm webForm = component.getWebForm();
        return webForm.getDomDocument().deleteNextChar(webForm, range);
    }

    /**
     * @todo Check deletion back to first char in <body> !
     * @todo Refactor document mutation into the document class
     * @todo Check read-only state etc
     */
    public boolean removePreviousChar() {
        // TODO - compute previous visual position, decide if it's
//        //    isWithinEditableRegion(Position pos) 
//        // and if so, set the range to it and delete the range.
//        if (hasSelection()) {
//            removeSelection();
//
//            return true;
//        }
//
////        Document doc = component.getDocument();
////        Position mark = range.getMark();
//        DomPosition mark = range.getMark();
////        Position dot = ModelViewMapper.computeArrowLeft(doc.getWebForm(), mark);
////        Position dot = ModelViewMapper.computeArrowLeft(component.getWebForm(), mark);
//        DomPosition dot = ModelViewMapper.computeArrowLeft(component.getWebForm(), mark);
//
////        if ((dot == Position.NONE) || !isWithinEditableRegion(dot)) {
//        if ((dot == DomPosition.NONE) || !isWithinEditableRegion(dot)) {
//            UIManager.getLookAndFeel().provideErrorFeedback(component); // beep
//
//            return false;
//        }
//
//        range.setRange(dot.getNode(), dot.getOffset(), mark.getNode(), mark.getOffset());
//
//        // XXX DEBUGGING ONLY
//        /*
//        Element element = doc.getBody();
//        if (element != null) {
//            System.out.println("BEFORE DELETION: " + org.netbeans.modules.visualweb.css2.FacesSupport.getHtmlStream(element));
//        }
//        */
////        range.deleteContents();
//        removeSelection();
//
//        // XXX DEBUGGING ONLY
//
//        /*
//        if (element != null) {
//            System.out.println("BEFORE DELETION: " + org.netbeans.modules.visualweb.css2.FacesSupport.getHtmlStream(element));
//        }
//        */
//        return true;
        WebForm webForm = component.getWebForm();
        return webForm.getDomDocument().deletePreviousChar(webForm, range);
    }
    
    boolean replaceSelection(String content) {
        // XXX Moved to DomDocumentImpl.
        /*
        if (range.isReadOnlyRegion()) {
            UIManager.getLookAndFeel().provideErrorFeedback(component);
            return;
        }
         */
//        if (hasSelection()) {
//            removeSelection();
//        }
//
////        Position pos = getDot();
//        DomPosition pos = getDot();
//
////        if (editor == null) {
//        if (!component.getWebForm().isInlineEditing()) {
////            assert (pos == Position.NONE) || !pos.isRendered();
////            if (pos != Position.NONE && MarkupService.isRenderedNode(pos.getNode())) {
//            if (pos != DomPosition.NONE && MarkupService.isRenderedNode(pos.getNode())) {
//                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
//                        new IllegalStateException("Node is expected to be not rendered, node=" + pos.getNode())); // NOI18N
//                return false;
//            }
//        } // else: Stay in the DocumentFragment; don't jump to the source DOM (there is none)
//
////        if (pos == Position.NONE) {
//        if (pos == DomPosition.NONE) {
////            UIManager.getLookAndFeel().provideErrorFeedback(this);
//            return false;
//        }
//
////        component.getDocument().insertString(this, pos, content);
////        component.getDocument().insertString(pos, content);
//        return component.getWebForm().getDomDocument().insertString(pos, content);
        return component.getWebForm().getDomDocument().insertString(component.getWebForm(), range, content);
    }

    // XXX Moved to WebForm.
//    /** Return true iff the position is within the editable portion of the document. */
////    public boolean isWithinEditableRegion(Position pos) {
//    public boolean isWithinEditableRegion(DomPosition pos) {
////        WebForm webform = component.getDocument().getWebForm();
//        WebForm webform = component.getWebForm();
//
//        InlineEditor editor = webform.getManager().getInlineEditor();
//
//        if (editor != null) {
////            Position editableRegionStart = editor.getBegin();
////            Position editableRegionEnd = editor.getEnd();
//            DomPosition editableRegionStart = editor.getBegin();
//            DomPosition editableRegionEnd = editor.getEnd();
//
////            assert editableRegionStart != Position.NONE;
////            assert editableRegionEnd != Position.NONE;
//
//            return pos.isLaterThan(editableRegionStart) && pos.isEarlierThan(editableRegionEnd);
//        }
//
////        assert !pos.isRendered() : pos;
//        if (MarkupService.isRenderedNode(pos.getNode())) {
//            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
//                    new IllegalStateException("Node is expected to be not rendered, node=" + pos.getNode()));
//        }
//        
//
//        if (!webform.isGridMode()) {
//            // In page flow mode, all regions are editable. Note - this
//            // may not be true when I start allowing sub-grids
//            return true;
//        }
//
//        CssBox box = webform.getManager().getInsertModeBox();
//
//        if (box == null) {
//            return false;
//        }
//
//        //Position editableRegionStart = Position.create(box.getSourceElement());
//        //Position editableRegionEnd = new Position(null, editableRegionStart.getNode(),
//        //                                               editableRegionStart.getOffset()+1);
//        Element componentRootElement = CssBox.getElementForComponentRootCssBox(box);
////        Position editableRegionStart =
////            new Position(box.getDesignBean().getElement(), 0, Bias.FORWARD);
//                // XXX Possible NPE?
////                new Position(CssBox.getMarkupDesignBeanForCssBox(box).getElement(), 0, Bias.FORWARD);
////                new Position(WebForm.getDomProviderService().getSourceElement(componentRootElement), 0, Bias.FORWARD);
////        DomPosition editableRegionStart = DesignerPaneBase.createDomPosition(WebForm.getDomProviderService().getSourceElement(componentRootElement), 0, Bias.FORWARD);
//        DomPosition editableRegionStart = component.getWebForm().createDomPosition(WebForm.getDomProviderService().getSourceElement(componentRootElement), 0, Bias.FORWARD);
//        
////        Position editableRegionEnd =
////            new Position(editableRegionStart.getNode(),
////                editableRegionStart.getNode().getChildNodes().getLength(), Bias.BACKWARD);
////        DomPosition editableRegionEnd = DesignerPaneBase.createDomPosition(editableRegionStart.getNode(), editableRegionStart.getNode().getChildNodes().getLength(), Bias.BACKWARD);
//        DomPosition editableRegionEnd = component.getWebForm().createDomPosition(editableRegionStart.getNode(), editableRegionStart.getNode().getChildNodes().getLength(), Bias.BACKWARD);
//
//        return pos.isLaterThan(editableRegionStart) && pos.isEarlierThan(editableRegionEnd);
//    }

    /** Return true iff the caret is within a read only region */
    public boolean isReadOnlyRegion() {
        return (range != null) ? range.isReadOnlyRegion() : false;
    }

    class SafeScroller implements Runnable {
        Rectangle r;

        SafeScroller(Rectangle r) {
            this.r = r;
        }

        public void run() {
            if (component != null) {
                component.scrollRectToVisible(r);
            }
        }
    }

    class Handler implements ActionListener, ClipboardOwner, DomDocumentListener {
        // --- ActionListener methods ----------------------------------

        /**
         * Invoked when the blink timer fires. This is called asynchronously. The simply changes the
         * visibility and repaints the rectangle that last bounded the caret.
         *
         * @param e
         *            the action event
         */
        public void actionPerformed(ActionEvent e) {
            if (((width == 0) || (height == 0)) && isVisible()) {
                // setVisible(true) will cause a scroll, only do this if the
                // new location is really valid.
                if (component != null) {
                    DesignerPaneBaseUI mapper = component.getUI();

                    if (range != null) {
//                        Position dot = range.getDot();
                        DomPosition dot = range.getDot();
//                        Rectangle r = mapper.modelToView(/*component,*/ dot);
//                        WebForm webForm = component.getDocument().getWebForm();
                        WebForm webForm = component.getWebForm();
                        
                        Rectangle r = webForm.modelToView(dot);

                        if ((r != null) && (r.width != 0) && (r.height != 0)) {
                            damage(r);
                        }
                    }
                }
            }

            flashOn = !flashOn;
            repaint();
        }

        //
        // ClipboardOwner
        //

        /**
         * Toggles the visibility of the selection when ownership is lost.
         */
        public void lostOwnership(Clipboard clipboard, Transferable contents) {
            if (ownsSelection) {
                ownsSelection = false;

                if ((component != null) && !component.hasFocus()) {
                    setSelectionVisible(false);
                }
            }
        }

        //////////////////////////
        // DomDocumentListener >>>
        
        public void insertUpdate(DomDocumentEvent evt) {
            DesignerCaret.this.setDot(evt.getDomPosition());
        }
        
        public void componentMoved(DomDocumentEvent evt) {
            // XXX 126234 Possible NPE.
            if (component == null) {
                return;
            }
            
            if (component.hasCaret()) {
                DomPosition pos = ModelViewMapper.getFirstDocumentPosition(component.getWebForm(), false);
//                webform.getPane().getCaret().setDot(pos);
//                webForm.getPane().setCaretDot(pos);
                setDot(pos);
            }
        }
        
        public void componentsMoved(DomDocumentEvent evt) {
            // XXX #91531 User didn't want to have this kind of autoscroll behavior.
//            final Rectangle rect = boundingBox;
//            // #6331237 NPE.
//            if(rect != null) {
//                SwingUtilities.invokeLater(new Runnable() {
//                    public void run() {
//                        editor.scrollRectToVisible(rect);
//                    }
//                });
//            }
        }
        
        public void componentMovedTo(DomDocumentEvent evt) {
            // XXX For not nothing here.
        }
        // DomDocumentListener <<<
        //////////////////////////
    }
}
