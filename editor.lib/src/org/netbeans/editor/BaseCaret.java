/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.editor;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.Font;
import java.awt.Point;
import java.awt.Component;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.FocusListener;
import java.awt.event.FocusEvent;
import java.awt.event.InputEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.Timer;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.Caret;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.EventListenerList;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.Position;
import javax.swing.text.StyleConstants;
import org.netbeans.api.editor.fold.Fold;
import org.netbeans.api.editor.fold.FoldHierarchy;
import org.netbeans.api.editor.fold.FoldHierarchyEvent;
import org.netbeans.api.editor.fold.FoldHierarchyListener;
import org.netbeans.api.editor.fold.FoldStateChange;
import org.netbeans.api.editor.fold.FoldUtilities;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.settings.FontColorNames;
import org.netbeans.api.editor.settings.FontColorSettings;
import org.netbeans.api.editor.settings.SimpleValueNames;
import org.netbeans.lib.editor.util.swing.DocumentListenerPriority;
import org.netbeans.modules.editor.lib2.EditorPreferencesDefaults;
import org.netbeans.modules.editor.lib.SettingsConversions;
import org.openide.util.WeakListeners;

/**
* Caret implementation
*
* @author Miloslav Metelka
* @version 1.00
*/

public class BaseCaret implements Caret,
MouseListener, MouseMotionListener, PropertyChangeListener,
DocumentListener, ActionListener, 
AtomicLockListener, FoldHierarchyListener {

    /** Caret type representing block covering current character */
    public static final String BLOCK_CARET = EditorPreferencesDefaults.BLOCK_CARET; // NOI18N

    /** Default caret type */
    public static final String LINE_CARET = EditorPreferencesDefaults.LINE_CARET; // NOI18N

    /** One dot thin line compatible with Swing default caret */
    public static final String THIN_LINE_CARET = "thin-line-caret"; // NOI18N

    /** @since 1.23 */
    public static final String THICK_LINE_CARET = "thick-line-caret"; // NOI18N

    // -J-Dorg.netbeans.editor.BaseCaret.level=FINEST
    private static final Logger LOG = Logger.getLogger(BaseCaret.class.getName());
    
    static {
        // Compatibility debugging flags mapping to logger levels
        if (Boolean.getBoolean("netbeans.debug.editor.caret.focus") && LOG.getLevel().intValue() < Level.FINE.intValue())
            LOG.setLevel(Level.FINE);
        if (Boolean.getBoolean("netbeans.debug.editor.caret.focus.extra") && LOG.getLevel().intValue() < Level.FINER.intValue())
            LOG.setLevel(Level.FINER);
    }

    /**
     * Implementation of various listeners.
     */
    private final ListenerImpl listenerImpl;
    
    /**
     * Present bounds of the caret. This rectangle needs to be repainted
     * prior the caret gets repainted elsewhere.
     */
    private Rectangle caretBounds;

    /** Component this caret is bound to */
    protected JTextComponent component;

    /** Position of the caret on the screen. This helps to compute
    * caret position on the next after jump.
    */
    Point magicCaretPosition;

    /** Draw mark designating the position of the caret.  */
    MarkFactory.ContextMark caretMark = new MarkFactory.ContextMark(Position.Bias.Forward, false);

    /** Draw mark that supports caret mark in creating selection */
    MarkFactory.ContextMark selectionMark = new MarkFactory.ContextMark(Position.Bias.Forward, false);

    /** Is the caret visible */
    boolean caretVisible;

    /** Whether blinking caret is currently visible.
     * <code>caretVisible</code> must be also true in order to paint the caret.
     */
    boolean blinkVisible;

    /** Is the selection currently visible? */
    boolean selectionVisible;

    /** Listeners */
    protected EventListenerList listenerList = new EventListenerList();

    /** Timer used for blinking the caret */
    protected Timer flasher;

    /** Type of the caret */
    String type;

    /** Width of thick caret */
    int width;
    
    /** Is the caret italic for italic fonts */
    boolean italic;

    private int xPoints[] = new int[4];
    private int yPoints[] = new int[4];
    private Action selectWordAction;
    private Action selectLineAction;

    /** Change event. Only one instance needed because it has only source property */
    protected ChangeEvent changeEvent;

    /** Dot array of one character under caret */
    protected char dotChar[] = {' '};

    private boolean overwriteMode;

    /** Remembering document on which caret listens avoids
    * duplicate listener addition to SwingPropertyChangeSupport
    * due to the bug 4200280
    */
    private BaseDocument listenDoc;

    /** Font of the text underlying the caret. It can be used
    * in caret painting.
    */
    protected Font afterCaretFont;

    /** Font of the text right before the caret */
    protected Font beforeCaretFont;

    /** Foreground color of the text underlying the caret. It can be used
    * in caret painting.
    */
    protected Color textForeColor;

    /** Background color of the text underlying the caret. It can be used
    * in caret painting.
    */
    protected Color textBackColor;

    private transient FocusListener focusListener;

    /** Whether the text is being modified under atomic lock.
     * If so just one caret change is fired at the end of all modifications.
     */
    private transient boolean inAtomicLock = false;
    private transient boolean inAtomicUnlock = false;
    
    /** Helps to check whether there was modification performed
     * and so the caret change needs to be fired.
     */
    private transient boolean modified;
    
    /** Whether there was an undo done in the modification and the offset of the modification */
    private transient int undoOffset = -1;
    
    static final long serialVersionUID =-9113841520331402768L;

    private MouseEvent dndArmedEvent = null;
    
    /**
     * Set to true once the folds have changed. The caret should retain
     * its relative visual position on the screen.
     */
    private boolean updateAfterFoldHierarchyChange;
    private FoldHierarchyListener weakFHListener;

    /**
     * Whether at least one typing change occurred during possibly several atomic operations.
     */
    private boolean typingModificationOccurred;

    private Preferences prefs = null;
    private final PreferenceChangeListener prefsListener = new PreferenceChangeListener() {
        public @Override void preferenceChange(PreferenceChangeEvent evt) {
            String setingName = evt == null ? null : evt.getKey();
            if (setingName == null || SimpleValueNames.CARET_BLINK_RATE.equals(setingName)) {
                SettingsConversions.callSettingsChange(BaseCaret.this);
                int rate = prefs.getInt(SimpleValueNames.CARET_BLINK_RATE, -1);
                if (rate == -1) {
                    JTextComponent c = component;
                    Integer rateI = c == null ? null : (Integer) c.getClientProperty(BaseTextUI.PROP_DEFAULT_CARET_BLINK_RATE);
                    rate = rateI != null ? rateI : EditorPreferencesDefaults.defaultCaretBlinkRate;
                }
                setBlinkRate(rate);
                refresh();
            }
        }
    };
    private PreferenceChangeListener weakPrefsListener = null;
    
    
    public BaseCaret() {
        listenerImpl = new ListenerImpl();
    }

    void updateType() {
        JTextComponent c = component;
        if (c != null && prefs != null) {
            
            String newType;
            int newWidth = 0;
            boolean newItalic;
            Color caretColor = Color.black;
            
            if (overwriteMode) {
                newType = prefs.get(SimpleValueNames.CARET_TYPE_OVERWRITE_MODE, EditorPreferencesDefaults.defaultCaretTypeOverwriteMode);
                newItalic = prefs.getBoolean(SimpleValueNames.CARET_ITALIC_OVERWRITE_MODE, EditorPreferencesDefaults.defaultCaretItalicOverwriteMode);
            } else { // insert mode
                newType = prefs.get(SimpleValueNames.CARET_TYPE_INSERT_MODE, EditorPreferencesDefaults.defaultCaretTypeInsertMode);
                newItalic = prefs.getBoolean(SimpleValueNames.CARET_ITALIC_INSERT_MODE, EditorPreferencesDefaults.defaultCaretItalicInsertMode);
                newWidth = prefs.getInt(SimpleValueNames.THICK_CARET_WIDTH, EditorPreferencesDefaults.defaultThickCaretWidth);
            }

            FontColorSettings fcs = MimeLookup.getLookup(org.netbeans.lib.editor.util.swing.DocumentUtilities.getMimeType(c)).lookup(FontColorSettings.class);
            if (fcs != null) {
                if (overwriteMode) {
                    AttributeSet attribs = fcs.getFontColors(FontColorNames.CARET_COLOR_OVERWRITE_MODE); //NOI18N
                    if (attribs != null) {
                        caretColor = (Color) attribs.getAttribute(StyleConstants.Foreground);
                    }
                } else {
                    AttributeSet attribs = fcs.getFontColors(FontColorNames.CARET_COLOR_INSERT_MODE); //NOI18N
                    if (attribs != null) {
                        caretColor = (Color) attribs.getAttribute(StyleConstants.Foreground);
                    }
                }
            }
            
            this.type = newType;
            this.italic = newItalic;
            this.width = newWidth;
            c.setCaretColor(caretColor);
            if (LOG.isLoggable(Level.FINER)) {
                LOG.finer("Updating caret color:" + caretColor + '\n'); // NOI18N
            }

            resetBlink();
            dispatchUpdate(false);
        }
    }

    /**
     * Assign new caret bounds into <code>caretBounds</code> variable.
     *
     * @return true if the new caret bounds were successfully computed
     *  and assigned or false otherwise.
     */
    private boolean updateCaretBounds() {
        JTextComponent c = component;
        if (c != null) {
            int offset = getDot();
            Rectangle newCaretBounds;
            try {
                newCaretBounds = c.getUI().modelToView(
                        c, offset, Position.Bias.Forward);
                // [TODO] Temporary fix - impl should remember real bounds computed by paintCustomCaret()
                if (newCaretBounds != null) {
                    newCaretBounds.width = Math.max(newCaretBounds.width, 2);
                }

                BaseDocument doc = Utilities.getDocument(c);
                if (doc != null) {
                    doc.getChars(offset, this.dotChar, 0, 1);
                }
            } catch (BadLocationException e) {
                newCaretBounds = null;
                Utilities.annotateLoggable(e);
            }
        
            if (newCaretBounds != null) {
                caretBounds = newCaretBounds;
                return true;
            }
        }
        return false;
    }

    /** Called when UI is being installed into JTextComponent */
    public @Override void install(JTextComponent c) {
        assert (SwingUtilities.isEventDispatchThread()); // must be done in AWT
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Installing to " + s2s(c)); //NOI18N
        }
        
        component = c;
        blinkVisible = true;
        
        // Assign dot and mark positions
        BaseDocument doc = Utilities.getDocument(c);
        if (doc != null) {
            modelChanged(null, doc);
        }

        // Attempt to assign initial bounds - usually here the component
        // is not yet added to the component hierarchy.
        updateCaretBounds();
        
        if (caretBounds == null) {
            // For null bounds wait for the component to get resized
            // and attempt to recompute bounds then
            component.addComponentListener(listenerImpl);
        }

        component.addPropertyChangeListener(this);
        component.addFocusListener(listenerImpl);
        component.addMouseListener(this);
        component.addMouseMotionListener(this);

        EditorUI editorUI = Utilities.getEditorUI(component);
        editorUI.addPropertyChangeListener( this );
        
        if (component.hasFocus()) {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("Component has focus, calling BaseCaret.focusGained(); doc=" // NOI18N
                    + component.getDocument().getProperty(Document.TitleProperty) + '\n');
            }
            listenerImpl.focusGained(null); // emulate focus gained
        }

        dispatchUpdate(false);
    }

    /** Called when UI is being removed from JTextComponent */
    @Override
    @SuppressWarnings("NestedSynchronizedStatement")
    public void deinstall(JTextComponent c) {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Deinstalling from " + s2s(c)); //NOI18N
        }
        
        component = null; // invalidate

        // No idea why the sync is done the way how it is, but the locks must
        // always be acquired in the same order otherwise the code will deadlock
        // sooner or later. See #100734
        synchronized (this) {
            synchronized (listenerImpl) {
                if (flasher != null) {
                    setBlinkRate(0);
                }
            }
        }
        
        c.removeMouseMotionListener(this);
        c.removeMouseListener(this);
        c.removeFocusListener(listenerImpl);
        c.removePropertyChangeListener(this);
        
        if (weakFHListener != null) {
            FoldHierarchy hierarchy = FoldHierarchy.get(c);
            if (hierarchy != null) {
                hierarchy.removeFoldHierarchyListener(weakFHListener);
            }
        }

        modelChanged(listenDoc, null);
    }

    protected void modelChanged(BaseDocument oldDoc, BaseDocument newDoc) {
        if (oldDoc != null) {
            // ideally the oldDoc param shouldn't exist and only listenDoc should be used
            assert (oldDoc == listenDoc);

            org.netbeans.lib.editor.util.swing.DocumentUtilities.removeDocumentListener(
                    oldDoc, this, DocumentListenerPriority.CARET_UPDATE);
            oldDoc.removeAtomicLockListener(this);

            try {
                caretMark.remove();
                selectionMark.remove();
            } catch (InvalidMarkException e) {
                Utilities.annotateLoggable(e);
            }

            listenDoc = null;
            if (prefs != null && weakPrefsListener != null) {
                prefs.removePreferenceChangeListener(weakPrefsListener);
            }
        }


        if (newDoc != null) {

            org.netbeans.lib.editor.util.swing.DocumentUtilities.addDocumentListener(
                    newDoc, this, DocumentListenerPriority.CARET_UPDATE);
            listenDoc = newDoc;
            newDoc.addAtomicLockListener(this);

            try {
                Utilities.insertMark(newDoc, caretMark, 0);
                Utilities.insertMark(newDoc, selectionMark, 0);
            } catch (InvalidMarkException e) {
                Utilities.annotateLoggable(e);
            } catch (BadLocationException e) {
                Utilities.annotateLoggable(e);
            }

            prefs = MimeLookup.getLookup(org.netbeans.lib.editor.util.swing.DocumentUtilities.getMimeType(newDoc)).lookup(Preferences.class);
            if (prefs != null) {
                weakPrefsListener = WeakListeners.create(PreferenceChangeListener.class, prefsListener, prefs);
                prefs.addPreferenceChangeListener(weakPrefsListener);
            }
            
            Utilities.runInEventDispatchThread(
                new Runnable() {
                    public @Override void run() {
                        updateType();
                    }
                }
            );
        }
    }

    /** Renders the caret */
    public @Override void paint(Graphics g) {
        JTextComponent c = component;
        if (c == null) return;
        EditorUI editorUI = Utilities.getEditorUI(c);

        // #70915 Check whether the caret was moved but the component was not
        // validated yet and therefore the caret bounds are still null
        // and if so compute the bounds and scroll the view if necessary.
        if (getDot() != 0 && caretBounds == null) {
            update(true);
        }
        if (LOG.isLoggable(Level.FINEST)) {
            LOG.finest("BaseCaret.paint(): caretBounds=" + caretBounds + dumpVisibility() + '\n');
        }
        if (caretBounds != null && isVisible() && blinkVisible) {
            paintCustomCaret(g);
        }
    }

    protected void paintCustomCaret(Graphics g) {
        JTextComponent c = component;
        if (c != null) {
            EditorUI editorUI = Utilities.getEditorUI(c);
            g.setColor(c.getCaretColor());
            if (THIN_LINE_CARET.equals(type)) { // thin line caret
                int upperX = caretBounds.x;
                if (beforeCaretFont != null && beforeCaretFont.isItalic() && italic) {
                    upperX += Math.tan(beforeCaretFont.getItalicAngle()) * caretBounds.height;
                }
                g.drawLine((int)upperX, caretBounds.y, caretBounds.x,
                        (caretBounds.y + caretBounds.height - 1));
            } else if (THICK_LINE_CARET.equals(type)) { // thick caret
                int blkWidth = this.width;
                if (blkWidth <= 0) blkWidth = 5; // sanity check
                if (afterCaretFont != null) g.setFont(afterCaretFont);
                Color textBackgroundColor = c.getBackground();
                if (textBackgroundColor != null) {
                    g.setXORMode( textBackgroundColor);
                }
                g.fillRect(caretBounds.x, caretBounds.y, blkWidth, caretBounds.height - 1);
            } else if (BLOCK_CARET.equals(type)) { // block caret
                if (afterCaretFont != null) g.setFont(afterCaretFont);
                if (afterCaretFont != null && afterCaretFont.isItalic() && italic) { // paint italic caret
                    int upperX = (int)(caretBounds.x
                            + Math.tan(afterCaretFont.getItalicAngle()) * caretBounds.height);
                    xPoints[0] = upperX;
                    yPoints[0] = caretBounds.y;
                    xPoints[1] = upperX + caretBounds.width;
                    yPoints[1] = caretBounds.y;
                    xPoints[2] = caretBounds.x + caretBounds.width;
                    yPoints[2] = caretBounds.y + caretBounds.height - 1;
                    xPoints[3] = caretBounds.x;
                    yPoints[3] = caretBounds.y + caretBounds.height - 1;
                    g.fillPolygon(xPoints, yPoints, 4);

                } else { // paint non-italic caret
                    g.fillRect(caretBounds.x, caretBounds.y, caretBounds.width, caretBounds.height);
                }
                
                if (!Character.isWhitespace(dotChar[0])) {
                    Color textBackgroundColor = c.getBackground();
                    if (textBackgroundColor != null)
                        g.setColor(textBackgroundColor);
                    // int ascent = FontMetricsCache.getFontMetrics(afterCaretFont, c).getAscent();
                    g.drawChars(dotChar, 0, 1, caretBounds.x,
                            caretBounds.y + editorUI.getLineAscent());
                }

            } else { // two dot line caret
                int blkWidth = 2;
                if (beforeCaretFont != null && beforeCaretFont.isItalic() && italic) {
                    int upperX = (int)(caretBounds.x 
                            + Math.tan(beforeCaretFont.getItalicAngle()) * caretBounds.height);
                    xPoints[0] = upperX;
                    yPoints[0] = caretBounds.y;
                    xPoints[1] = upperX + blkWidth;
                    yPoints[1] = caretBounds.y;
                    xPoints[2] = caretBounds.x + blkWidth;
                    yPoints[2] = caretBounds.y + caretBounds.height - 1;
                    xPoints[3] = caretBounds.x;
                    yPoints[3] = caretBounds.y + caretBounds.height - 1;
                    g.fillPolygon(xPoints, yPoints, 4);

                } else { // paint non-italic caret
                    g.fillRect(caretBounds.x, caretBounds.y, blkWidth, caretBounds.height - 1);
                }
            }
        }
    }

    /** Update the caret's visual position */
    void dispatchUpdate(final boolean scrollViewToCaret) {
        /* After using SwingUtilities.invokeLater() due to fix of #18860
         * there is another fix of #35034 which ensures that the caret's
         * document listener will be added AFTER the views hierarchy's
         * document listener so the code can run synchronously again
         * which should eliminate the problem with caret lag.
         * However the document can be modified from non-AWT thread
         * which is the case in #57316 and in that case the code
         * must run asynchronously in AWT thread.
         */
        Utilities.runInEventDispatchThread(
            new Runnable() {
                public @Override void run() {
                    JTextComponent c = component;
                    if (c != null) {
                        BaseDocument doc = Utilities.getDocument(c);
                        if (doc != null) {
                            doc.readLock();
                            try {
                                update(scrollViewToCaret);
                            } finally {
                                doc.readUnlock();
                            }
                        }
                    }
                }
            }
        );
    }

    /**
     * Update the caret's visual position.
     * <br/>
     * The document is read-locked while calling this method.
     *
     * @param scrollViewToCaret whether the view of the text component should be
     *  scrolled to the position of the caret.
     */
    protected void update(boolean scrollViewToCaret) {
        JTextComponent c = component;
        if (c != null) {
            BaseTextUI ui = (BaseTextUI)c.getUI();
            EditorUI editorUI = ui.getEditorUI();
            BaseDocument doc = Utilities.getDocument(c);
            if (doc != null) {
                Rectangle oldCaretBounds = caretBounds; // no need to deep copy
                if (oldCaretBounds != null) {
                    if (italic) { // caret is italic - add char height to the width of the rect
                        oldCaretBounds.width += oldCaretBounds.height;
                    }
                    c.repaint(oldCaretBounds);
                }

                if (updateCaretBounds()) {
                    Rectangle scrollBounds = new Rectangle(caretBounds);
                    
                    // Optimization to avoid extra repaint:
                    // If the caret bounds were not yet assigned then attempt
                    // to scroll the window so that there is an extra vertical space 
                    // for the possible horizontal scrollbar that may appear
                    // if the line-view creation process finds line-view that
                    // is too wide and so the horizontal scrollbar will appear
                    // consuming an extra vertical space at the bottom.
                    if (oldCaretBounds == null) {
                        Component viewport = c.getParent();
                        if (viewport instanceof JViewport) {
                            Component scrollPane = viewport.getParent();
                            if (scrollPane instanceof JScrollPane) {
                                JScrollBar hScrollBar = ((JScrollPane)scrollPane).getHorizontalScrollBar();
                                if (hScrollBar != null) {
                                    int hScrollBarHeight = hScrollBar.getPreferredSize().height;
                                    Dimension extentSize = ((JViewport)viewport).getExtentSize();
                                    // If the extent size is high enough then extend
                                    // the scroll region by extra vertical space
                                    if (extentSize.height >= caretBounds.height + hScrollBarHeight) {
                                        scrollBounds.height += hScrollBarHeight;
                                    }
                                }
                            }
                        }
                    }
                    
                    Rectangle visibleBounds = c.getVisibleRect();
                    
                    // If folds have changed attempt to scroll the view so that 
                    // relative caret's visual position gets retained
                    // (the absolute position will change because of collapsed/expanded folds).
                    boolean doScroll = scrollViewToCaret;
                    if (oldCaretBounds != null && (!scrollViewToCaret || updateAfterFoldHierarchyChange)) {
                        int oldRelY = oldCaretBounds.y - visibleBounds.y;
                        // Only fix if the caret is within visible bounds and the new x or y coord differs from the old one
                        if (oldRelY >= 0 && oldRelY < visibleBounds.height &&
                                (oldCaretBounds.y != caretBounds.y || oldCaretBounds.x != caretBounds.x))
                        {
                            doScroll = true; // Perform explicit scrolling
                            int oldRelX = oldCaretBounds.x - visibleBounds.x;
                            // Do not retain the horizontal caret bounds by scrolling
                            // since many modifications do not explicitly say that they are typing modifications
                            // and this would cause problems like #176268
//                            scrollBounds.x = Math.max(caretBounds.x - oldRelX, 0);
                            scrollBounds.y = Math.max(caretBounds.y - oldRelY, 0);
//                            scrollBounds.width = visibleBounds.width;
                            scrollBounds.height = visibleBounds.height;
                        }
                    }

                    // Historically the caret is expected to appear
                    // in the middle of the window if setDot() gets called
                    // e.g. by double-clicking in Navigator.
                    // If the caret bounds are more than a caret height below the present
                    // visible view bounds (or above the view bounds)
                    // then scroll the window so that the caret is in the middle
                    // of the visible window to see the context around the caret.
                    // This should work fine with PgUp/Down because these
                    // scroll the view explicitly.
                    if (scrollViewToCaret && /* # 70915 !updateAfterFoldHierarchyChange && */
                        (caretBounds.y > visibleBounds.y + visibleBounds.height + caretBounds.height
                            || caretBounds.y + caretBounds.height < visibleBounds.y - caretBounds.height)
                    ) {
                        // Scroll into the middle
                        scrollBounds.y -= (visibleBounds.height - caretBounds.height) / 2;
                        scrollBounds.height = visibleBounds.height;
                    }

                    updateAfterFoldHierarchyChange = false;
                    
                    // Ensure that the viewport will be scrolled either to make the caret visible
                    // or to retain cart's relative visual position against the begining of the viewport's visible rectangle.
                    if (doScroll) {
                        c.scrollRectToVisible(scrollBounds);
                    }

                    resetBlink();
                    c.repaint(caretBounds);
                }
            }
        }
    }

    private void updateSystemSelection() {
        if (getDot() != getMark() && component != null) {
            Clipboard clip = getSystemSelection();
            
            if (clip != null) {
                clip.setContents(new java.awt.datatransfer.StringSelection(component.getSelectedText()), null);
            }
        }
    }

    private Clipboard getSystemSelection() {
        return component.getToolkit().getSystemSelection();
    }
    
    /**
     * Redefine to Object.equals() to prevent defaulting to Rectangle.equals()
     * which would cause incorrect firing
     */
    @Override@SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
    public boolean equals(Object o) {
        return (this == o);
    }

    public @Override int hashCode() {
        return System.identityHashCode(this);
    }
    
    /** Adds listener to track when caret position was changed */
    public @Override void addChangeListener(ChangeListener l) {
        listenerList.add(ChangeListener.class, l);
    }

    /** Removes listeners to caret position changes */
    public @Override void removeChangeListener(ChangeListener l) {
        listenerList.remove(ChangeListener.class, l);
    }

    /** Notifies listeners that caret position has changed */
    protected void fireStateChanged() {
        Runnable runnable = new Runnable() {
            public @Override void run() {
                Object listeners[] = listenerList.getListenerList();
                for (int i = listeners.length - 2; i >= 0 ; i -= 2) {
                    if (listeners[i] == ChangeListener.class) {
                        if (changeEvent == null) {
                            changeEvent = new ChangeEvent(BaseCaret.this);
                        }
                        ((ChangeListener)listeners[i + 1]).stateChanged(changeEvent);
                    }
                }
            }
        };
        
        // Fix of #24336 - always do in AWT thread
        // Fix of #114649 - when under document's lock repost asynchronously
        if (inAtomicUnlock) {
            SwingUtilities.invokeLater(runnable);
        } else {
            Utilities.runInEventDispatchThread(runnable);
        }
        updateSystemSelection();
    }

    /**
     * Whether the caret currently visible.
     * <br>
     * Although the caret is visible it may be in a state when it's
     * not physically showing on screen in case when it's blinking.
     */
    public final @Override boolean isVisible() {
        return caretVisible;
    }

    protected void setVisibleImpl(boolean v) {
        if (LOG.isLoggable(Level.FINER)) {
            LOG.finer("BaseCaret.setVisible(" + v + ")\n");
            if (LOG.isLoggable(Level.FINEST)) {
                LOG.log(Level.INFO, "", new Exception());
            }
        }
        boolean visible = isVisible();
        synchronized (this) {
            synchronized (listenerImpl) {
                if (flasher != null) {
                    if (visible) {
                        flasher.stop();
                    }
                    if (LOG.isLoggable(Level.FINER)) {
                        LOG.finer((v ? "Starting" : "Stopping") + // NOI18N
                                " the caret blinking timer: " + dumpVisibility() + '\n'); // NOI18N
                    }
                    if (v) {
                        flasher.start();
                    } else {
                        flasher.stop();
                    }
                }
            }

            caretVisible = v;
        }
        JTextComponent c = component;
        if (c != null && caretBounds != null) {
            Rectangle repaintRect = caretBounds;
            if (italic) {
                repaintRect = new Rectangle(repaintRect); // copy
                repaintRect.width += repaintRect.height; // ensure enough horizontally
            }
            c.repaint(repaintRect);
        }
    }

    private String dumpVisibility() {
        return "visible=" + isVisible() + ", blinkVisible=" + blinkVisible;
    }

    @SuppressWarnings("NestedSynchronizedStatement")
    void resetBlink() {
        synchronized (this) {
            boolean visible = isVisible();
            synchronized (listenerImpl) {
                if (flasher != null) {
                    flasher.stop();
                    blinkVisible = true;
                    if (visible) {
                        if (LOG.isLoggable(Level.FINER)){
                            LOG.finer("Reset blinking (caret already visible)" + // NOI18N
                                    " - starting the caret blinking timer: " + dumpVisibility() + '\n'); // NOI18N
                        }
                        flasher.start();
                    } else {
                        if (LOG.isLoggable(Level.FINER)){
                            LOG.finer("Reset blinking (caret not visible)" + // NOI18N
                                    " - caret blinking timer not started: " + dumpVisibility() + '\n'); // NOI18N
                        }
                    }
                }
            }
        }
    }

    /** Sets the caret visibility */
    public @Override void setVisible(final boolean v) {
        Utilities.runInEventDispatchThread(
            new Runnable() {
                public @Override void run() {
                    setVisibleImpl(v);
                }
            }
        );
    }

    /** Is the selection visible? */
    public final @Override boolean isSelectionVisible() {
        return selectionVisible;
    }

    /** Sets the selection visibility */
    public @Override void setSelectionVisible(boolean v) {
        if (selectionVisible == v) {
            return;
        }
        JTextComponent c = component;
        if (c != null) {
            selectionVisible = v;

            // repaint the block
            BaseTextUI ui = (BaseTextUI)c.getUI();
            try {
                ui.getEditorUI().repaintBlock(caretMark.getOffset(), selectionMark.getOffset());
            } catch (BadLocationException e) {
                Utilities.annotateLoggable(e);
            } catch (InvalidMarkException e) {
                Utilities.annotateLoggable(e);
            }

        }
    }

    /** Saves the current caret position.  This is used when
    * caret up or down actions occur, moving between lines
    * that have uneven end positions.
    *
    * @param p  the Point to use for the saved position
    */
    public @Override void setMagicCaretPosition(Point p) {
        magicCaretPosition = p;
    }

    /** Get position used to mark begining of the selected block */
    public @Override final Point getMagicCaretPosition() {
        return magicCaretPosition;
    }

    /** Sets the caret blink rate.
    * @param rate blink rate in milliseconds, 0 means no blink
    */
    public @Override synchronized void setBlinkRate(int rate) {
        if (LOG.isLoggable(Level.FINER)) {
            LOG.finer("setBlinkRate(" + rate + ")" + dumpVisibility() + '\n'); // NOI18N
        }
        synchronized (listenerImpl) {
            if (flasher == null && rate > 0) {
                flasher = new Timer(rate, new WeakTimerListener(this));
            }
            if (flasher != null) {
                if (rate > 0) {
                    if (flasher.getDelay() != rate) {
                        flasher.setDelay(rate);
                    }
                } else { // zero rate - don't blink
                    flasher.stop();
                    flasher.removeActionListener(this);
                    flasher = null;
                    blinkVisible = true;
                    if (LOG.isLoggable(Level.FINER)){
                        LOG.finer("Zero blink rate - no blinking. flasher=null; blinkVisible=true"); // NOI18N
                    }
                }
            }
        }
    }

    /** Returns blink rate of the caret or 0 if caret doesn't blink */
    @Override
    @SuppressWarnings("NestedSynchronizedStatement")
    public int getBlinkRate() {
        synchronized (this) {
            synchronized (listenerImpl) {
                return (flasher != null) ? flasher.getDelay() : 0;
            }
        }
    }

    /** Gets the current position of the caret */
    public @Override int getDot() {
        if (component != null) {
            try {
                return caretMark.getOffset();
            } catch (InvalidMarkException e) {
            }
        }
        return 0;
    }

    /** Gets the current position of the selection mark.
    * If there's a selection this position will be different
    * from the caret position.
    */
    public @Override int getMark() {
        if (component != null) {
                try {
                    return selectionMark.getOffset();
                } catch (InvalidMarkException e) {
                }
            }
        return 0;
    }

    /**
     * Assign the caret a new offset in the underlying document.
     * <br/>
     * This method implicitly sets the selection range to zero.
     */
    public @Override void setDot(int offset) {
        // The first call to this method in NB is done when the component
        // is already connected to the component hierarchy but its size
        // is still (0,0,0,0) (although its preferred size is already non-empty).
        // This causes the TextUI.modelToView() to return null
        // because BasicTextUI.getVisibleEditorRect() returns null.
        // Thus caretBounds will be null in such case although
        // the offset in setDot() is already non-zero.
        // In such case the component listener listens for resizing
        // of the editor component and reassigns the caretBounds
        // once the component gets resized.
        setDot(offset, caretBounds, EditorUI.SCROLL_DEFAULT);
    }

    public void setDot(int offset, boolean expandFold) {
        setDot(offset, caretBounds, EditorUI.SCROLL_DEFAULT, expandFold);
    }
    
    
    /** Sets the caret position to some position. This
     * causes removal of the active selection. If expandFold set to true
     * fold containing offset position will be expanded.
     *
     * <p>
     * <b>Note:</b> This method is deprecated and the present implementation
     * ignores values of scrollRect and scrollPolicy parameters.
     *
     * @param offset offset in the document to which the caret should be positioned.
     * @param scrollRect rectangle to which the editor window should be scrolled.
     * @param scrollPolicy the way how scrolling should be done.
     *  One of <code>EditorUI.SCROLL_*</code> constants.
     * @param expandFold whether possible fold at the caret position should be expanded.
     *
     * @deprecated use #setDot(int, boolean) preceded by <code>JComponent.scrollRectToVisible()</code>.
     */
    
    public void setDot(int offset, Rectangle scrollRect, int scrollPolicy, boolean expandFold) {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("setDot: offset=" + offset); //NOI18N
            if (LOG.isLoggable(Level.FINEST)) {
                LOG.log(Level.INFO, "setDot call stack", new Exception());
            }
        }
        
        JTextComponent c = component;
        if (c != null) {
            BaseDocument doc = (BaseDocument)c.getDocument();
            boolean dotChanged = false;
            doc.readLock();
            try {
                if (doc != null && offset >= 0 && offset <= doc.getLength()) {
                    dotChanged = true;
                    try {
                        Utilities.moveMark(doc, caretMark, offset);
                        Utilities.moveMark(doc, selectionMark, offset);

                        FoldHierarchy hierarchy = FoldHierarchy.get(c);
                        // hook the listener if not already done
                        if (weakFHListener == null) {
                            weakFHListener = WeakListeners.create(FoldHierarchyListener.class, this, hierarchy);
                            hierarchy.addFoldHierarchyListener(weakFHListener);
                        }

                        // Unfold fold
                        hierarchy.lock();
                        try {
                            Fold collapsed = null;
                            while (expandFold && (collapsed = FoldUtilities.findCollapsedFold(hierarchy, offset, offset)) != null && collapsed.getStartOffset() < offset &&
                                collapsed.getEndOffset() > offset) {
                                hierarchy.expand(collapsed);
                            }
                        } finally {
                            hierarchy.unlock();
                        }
                    } catch (BadLocationException e) {
                        throw new IllegalStateException(e.toString());
                        // setting the caret to wrong position leaves it at current position
                    } catch (InvalidMarkException e) {
                        throw new IllegalStateException(e.toString());
                        // Caret not installed or inside the initial-read
                    }
                }
            } finally {
                doc.readUnlock();
            }
            
            if (dotChanged) {
                fireStateChanged();
                dispatchUpdate(true);
            }
        }
    }
    
    /** Sets the caret position to some position. This
     * causes removal of the active selection.
     *
     * <p>
     * <b>Note:</b> This method is deprecated and the present implementation
     * ignores values of scrollRect and scrollPolicy parameters.
     *
     * @param offset offset in the document to which the caret should be positioned.
     * @param scrollRect rectangle to which the editor window should be scrolled.
     * @param scrollPolicy the way how scrolling should be done.
     *  One of <code>EditorUI.SCROLL_*</code> constants.
     *
     * @deprecated use #setDot(int) preceded by <code>JComponent.scrollRectToVisible()</code>.
     */
    public void setDot(int offset, Rectangle scrollRect, int scrollPolicy) {
        setDot(offset, scrollRect, scrollPolicy, true);
    }

    public @Override void moveDot(int offset) {
        moveDot(offset, caretBounds, EditorUI.SCROLL_MOVE);
    }

    /** Makes selection by moving dot but leaving mark.
     * 
     * <p>
     * <b>Note:</b> This method is deprecated and the present implementation
     * ignores values of scrollRect and scrollPolicy parameters.
     *
     * @param offset offset in the document to which the caret should be positioned.
     * @param scrollRect rectangle to which the editor window should be scrolled.
     * @param scrollPolicy the way how scrolling should be done.
     *  One of <code>EditorUI.SCROLL_*</code> constants.
     *
     * @deprecated use #setDot(int) preceded by <code>JComponent.scrollRectToVisible()</code>.
     */
    public void moveDot(int offset, Rectangle scrollRect, int scrollPolicy) {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("moveDot: offset=" + offset); //NOI18N
        }

        JTextComponent c = component;
        if (c != null) {
            BaseDocument doc = (BaseDocument)c.getDocument();
            if (doc != null && offset >= 0 && offset <= doc.getLength()) {
                try {
                    int oldCaretPos = getDot();
                    if (offset == oldCaretPos) { // no change
                        return;
                    }
                    Utilities.moveMark(doc, caretMark, offset);
                    if (selectionVisible) { // selection already visible
                        Utilities.getEditorUI(c).repaintBlock(oldCaretPos, offset);
                        }
                } catch (BadLocationException e) {
                    throw new IllegalStateException(e.toString());
                    // position is incorrect
                } catch (InvalidMarkException e) {
                    throw new IllegalStateException(e.toString());
                }
            }
            fireStateChanged();
            dispatchUpdate(true);
        }
    }

    // DocumentListener methods
    public @Override void insertUpdate(DocumentEvent evt) {
        JTextComponent c = component;
        if (c != null) {
            BaseDocument doc = (BaseDocument)component.getDocument();
            BaseDocumentEvent bevt = (BaseDocumentEvent)evt;
            boolean typingModification;
            if ((bevt.isInUndo() || bevt.isInRedo())
                    && component == Utilities.getLastActiveComponent()
                    && !Boolean.TRUE.equals(org.netbeans.lib.editor.util.swing.
                        DocumentUtilities.getEventProperty(evt, "caretIgnore"))
               ) {
                // in undo mode and current component
                undoOffset = evt.getOffset() + evt.getLength();
                // Undo operations now cause the caret to move to the place where the undo occurs.
                // In future we should put additional info into the document event whether it was
                // a typing modification and if not the caret should not be relocated and scrolled.
                typingModification = true;
            } else {
                undoOffset = -1;
                typingModification = org.netbeans.lib.editor.util.swing.
                        DocumentUtilities.isTypingModification(component.getDocument());
            }

            modified = true;

            modifiedUpdate(typingModification);
        }
    }

    public @Override void removeUpdate(DocumentEvent evt) {
        JTextComponent c = component;
        if (c != null) {
            BaseDocument doc = (BaseDocument)c.getDocument();
            // make selection invisible if removal shrinked block to zero size
            BaseDocumentEvent bevt = (BaseDocumentEvent)evt;
            boolean typingModification;
            if ((bevt.isInUndo() || bevt.isInRedo())
                && c == Utilities.getLastActiveComponent()
                && !Boolean.TRUE.equals(org.netbeans.lib.editor.util.swing.
                    DocumentUtilities.getEventProperty(evt, "caretIgnore"))
            ) {
                // in undo mode and current component
                undoOffset = evt.getOffset();
                // Undo operations now cause the caret to move to the place where the undo occurs.
                // In future we should put additional info into the document event whether it was
                // a typing modification and if not the caret should not be relocated and scrolled.
                typingModification = true;
            } else { // Not undo or redo
                undoOffset = -1;
                typingModification = org.netbeans.lib.editor.util.swing.
                        DocumentUtilities.isTypingModification(component.getDocument());

            }

            modified = true;
            
            modifiedUpdate(typingModification);
        }
    }
    
    private void modifiedUpdate(boolean typingModification) {
        if (!inAtomicLock) {
            JTextComponent c = component;
            if (modified && c != null) {
                if (undoOffset >= 0) { // last modification was undo => set the dot to undoOffset
                    setDot(undoOffset);
                } else { // last modification was not undo
                    fireStateChanged();
                    // Scroll to caret only for component with focus
                    dispatchUpdate(c.hasFocus() && typingModification);
                }
                modified = false;
            }
        } else {
            typingModificationOccurred |= typingModification;
        }
    }
    
    public @Override void atomicLock(AtomicLockEvent evt) {
        inAtomicLock = true;
    }
    
    public @Override void atomicUnlock(AtomicLockEvent evt) {
        inAtomicLock = false;
        inAtomicUnlock = true;
        try {
            modifiedUpdate(typingModificationOccurred);
        } finally {
            inAtomicUnlock = false;
            typingModificationOccurred = false;
        }
    }
    
    public @Override void changedUpdate(DocumentEvent evt) {
        // XXX: used as a backdoor from HighlightingDrawLayer
        if (evt == null) {
            dispatchUpdate(false);
        }
    }

    // MouseListener methods
    public @Override void mouseClicked(MouseEvent evt) {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("mouseClicked: " + logMouseEvent(evt));
        }
        
        JTextComponent c = component;
        if (c != null) {
            if (SwingUtilities.isLeftMouseButton(evt)) {
                if (evt.getClickCount() == 2) {
                    BaseTextUI ui = (BaseTextUI)c.getUI();
                    // Expand fold if offset is in collapsed fold
                    int offset = ui.viewToModel(c,
                                    evt.getX(), evt.getY());
                    FoldHierarchy hierarchy = FoldHierarchy.get(c);
                    Document doc = c.getDocument();
                    if (doc instanceof AbstractDocument) {
                        AbstractDocument adoc = (AbstractDocument)doc;
                        adoc.readLock();
                        try {
                            hierarchy.lock();
                            try {
                                Fold collapsed = FoldUtilities.findCollapsedFold(
                                    hierarchy, offset, offset);
                                if (collapsed != null && collapsed.getStartOffset() <= offset &&
                                    collapsed.getEndOffset() >= offset) {
                                    hierarchy.expand(collapsed);
                                } else {
                                    if (selectWordAction == null) {
                                        selectWordAction = ((BaseKit)ui.getEditorKit(
                                                                c)).getActionByName(BaseKit.selectWordAction);
                                    }
                                    if (selectWordAction != null) {
                                        selectWordAction.actionPerformed(null);
                                    }
                                }
                            } finally {
                                hierarchy.unlock();
                            }
                        } finally {
                            adoc.readUnlock();
                        }
                    }
                } else if (evt.getClickCount() == 3) {
                    if (selectLineAction == null) {
                        BaseTextUI ui = (BaseTextUI)c.getUI();
                        selectLineAction = ((BaseKit)ui.getEditorKit(
                                                c)).getActionByName(BaseKit.selectLineAction);
                    }
                    if (selectLineAction != null) {
                        selectLineAction.actionPerformed(null);
                    }
                }
            } else if (SwingUtilities.isMiddleMouseButton(evt)){
		if (evt.getClickCount() == 1) {
		    if (c == null) return;
                    Toolkit tk = c.getToolkit();
                    Clipboard buffer = getSystemSelection();
                    
                    if (buffer == null) return;

                    Transferable trans = buffer.getContents(null);
                    if (trans == null) return;

                    final BaseDocument doc = (BaseDocument)c.getDocument();
                    if (doc == null) return;
                    
                    final int offset = ((BaseTextUI)c.getUI()).viewToModel(c,
                                    evt.getX(), evt.getY());

                    try{
                        final String pastingString = (String)trans.getTransferData(DataFlavor.stringFlavor);
                        if (pastingString == null) return;
                        doc.runAtomicAsUser (new Runnable () {
                            public @Override void run () {
                                 try {
                                     doc.insertString(offset, pastingString, null);
                                     setDot(offset+pastingString.length());
                                 } catch( BadLocationException exc ) {
                                 }
                            }
                        });
                    }catch(UnsupportedFlavorException ufe){
                    }catch(IOException ioe){
                    }
		}
            }
        }
    }

    private void mousePressedImpl(MouseEvent evt){
        JTextComponent c = component;
        if (c != null) {
            // Position the cursor at the appropriate place in the document
            if ((SwingUtilities.isLeftMouseButton(evt) && 
                !(evt.isPopupTrigger()) &&
                 (evt.getModifiers() & (InputEvent.META_MASK|InputEvent.ALT_MASK)) == 0) ||
               !(isSelectionVisible() && getMark() != getDot())
            ) {
                int offset = ((BaseTextUI)c.getUI()).viewToModel(c,
                          evt.getX(), evt.getY());
                if (offset >= 0) {
                    if ((evt.getModifiers() & InputEvent.SHIFT_MASK) != 0) {
                        moveDot(offset);
                    } else {
                        setDot(offset);
                    }
                    setMagicCaretPosition(null);
                }
                if (c.isEnabled()) {
                    c.requestFocus();
                }
            }
        }
    }
    
    public @Override void mousePressed(MouseEvent evt) {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("mousePressed: " + logMouseEvent(evt));
        }

        dndArmedEvent = null;

	if (isDragPossible(evt) && mapDragOperationFromModifiers(evt) != TransferHandler.NONE) {
            dndArmedEvent = evt;
	    evt.consume();
            return;
	}
        
        mousePressedImpl(evt);
    }

    public @Override void mouseReleased(MouseEvent evt) {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("mouseReleased: " + logMouseEvent(evt));
        }

        if (dndArmedEvent != null){
            mousePressedImpl(evt);
        }
        dndArmedEvent = null;
    }

    public @Override void mouseEntered(MouseEvent evt) {
    }

    public @Override void mouseExited(MouseEvent evt) {
    }

    
    protected int mapDragOperationFromModifiers(MouseEvent e) {
        int mods = e.getModifiersEx();
        
        if ((mods & InputEvent.BUTTON1_DOWN_MASK) != InputEvent.BUTTON1_DOWN_MASK) {
            return TransferHandler.NONE;
        }
        
        return TransferHandler.COPY_OR_MOVE;
    }    
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
        JComponent comp = getEventComponent(e);
        boolean possible =  (comp == null) ? false : (comp.getTransferHandler() != null);
        if (possible) {
            JTextComponent c = (JTextComponent) getEventComponent(e);
            if (c.getDragEnabled()) {
                Caret caret = c.getCaret();
                int dot = caret.getDot();
                int mark = caret.getMark();
                if (dot != mark) {
                    Point p = new Point(e.getX(), e.getY());
                    int pos = c.viewToModel(p);

                    int p0 = Math.min(dot, mark);
                    int p1 = Math.max(dot, mark);
                    if ((pos >= p0) && (pos < p1)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
    protected JComponent getEventComponent(MouseEvent e) {
	Object src = e.getSource();
	if (src instanceof JComponent) {
	    JComponent c = (JComponent) src;
	    return c;
	}
	return null;
    }
    
    // MouseMotionListener methods
    public @Override void mouseDragged(MouseEvent evt) {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("mouseDragged: " + logMouseEvent(evt)); //NOI18N
        }
        
        if (dndArmedEvent != null){
            evt.consume();
            return;
        }
        
        JTextComponent c = component;
        
        if (SwingUtilities.isLeftMouseButton(evt)) {
            if (c != null) {
                int offset = ((BaseTextUI)c.getUI()).viewToModel(c,
                          evt.getX(), evt.getY());
                // fix for #15204
                if (offset == -1)
                    offset = 0;
                // fix of #22846
                if (offset >= 0 && (evt.getModifiers() & InputEvent.SHIFT_MASK) == 0)
                    moveDot(offset);
            }
        }
    }

    public @Override void mouseMoved(MouseEvent evt) {
    }

    private static String logMouseEvent(MouseEvent evt) {
        return "x=" + evt.getX() + ", y=" + evt.getY() //NOI18N
            + ", component=" + s2s(evt.getComponent()) //NOI18N
            + ", source=" + s2s(evt.getSource()); //NOI18N
    }

    private static String s2s(Object o) {
        return o == null ? "null" : o.getClass().getName() + "@" + Integer.toHexString(System.identityHashCode(o)); //NOI18N
    }

    // PropertyChangeListener methods
    public @Override void propertyChange(PropertyChangeEvent evt) {
        String propName = evt.getPropertyName();

        if ("document".equals(propName)) { // NOI18N
            BaseDocument newDoc = (evt.getNewValue() instanceof BaseDocument)
                                  ? (BaseDocument)evt.getNewValue() : null;
            modelChanged(listenDoc, newDoc);

        } else if (EditorUI.OVERWRITE_MODE_PROPERTY.equals(propName)) {
            Boolean b = (Boolean)evt.getNewValue();
            overwriteMode = (b != null) ? b.booleanValue() : false;
            updateType();

        } else if ("ancestor".equals(propName) && evt.getSource() == component) { // NOI18N
            // The following code ensures that when the width of the line views
            // gets computed on background after the file gets opened
            // (so the horizontal scrollbar gets added after several seconds
            // for larger files) that the suddenly added horizontal scrollbar
            // will not hide the caret laying on the last line of the viewport.
            // A component listener gets installed into horizontal scrollbar
            // and if it's fired the caret's bounds will be checked whether
            // they intersect with the horizontal scrollbar
            // and if so the view will be scrolled.
            Container parent = component.getParent();
            if (parent instanceof JViewport) {
                parent = parent.getParent(); // parent of viewport
                if (parent instanceof JScrollPane) {
                    JScrollPane scrollPane = (JScrollPane)parent;
                    JScrollBar hScrollBar = scrollPane.getHorizontalScrollBar();
                    if (hScrollBar != null) {
                        // Add weak listener so that editor pane could be removed
                        // from scrollpane without being held by scrollbar
                        hScrollBar.addComponentListener(
                                (ComponentListener)WeakListeners.create(
                                ComponentListener.class, listenerImpl, hScrollBar));
                    }
                }
            }
        } else if("enabled".equals(evt.getPropertyName())) {
            Boolean enabled = (Boolean) evt.getNewValue();
            if(component.isFocusOwner()) {
                if(enabled == Boolean.TRUE) {
                    if(component.isEditable()) {
                        setVisible(true);
                    }
                    setSelectionVisible(true);
                } else {
                    setVisible(false);
                    setSelectionVisible(false);
                }
            }
        }
    }

    // ActionListener methods
    /** Fired when blink timer fires */
    public @Override void actionPerformed(ActionEvent evt) {
        JTextComponent c = component;
        if (c != null) {
            blinkVisible = !blinkVisible;
            if (caretBounds != null) {
                Rectangle repaintRect = caretBounds;
                if (italic) {
                    repaintRect = new Rectangle(repaintRect); // clone
                    repaintRect.width += repaintRect.height;
                }
                c.repaint(repaintRect);
            }
        }
    }

    public @Override void foldHierarchyChanged(FoldHierarchyEvent evt) {
        int caretOffset = getDot();
        int addedFoldCnt = evt.getAddedFoldCount();
        final boolean scrollToView;
        if (addedFoldCnt > 0) {
            FoldHierarchy hierarchy = (FoldHierarchy)evt.getSource();
            Fold collapsed = null;
            while ((collapsed = FoldUtilities.findCollapsedFold(hierarchy, caretOffset, caretOffset)) != null && collapsed.getStartOffset() < caretOffset &&
                    collapsed.getEndOffset() > caretOffset) {
                hierarchy.expand(collapsed);                
            }
            scrollToView = true;
        } else {
            int startOffset = Integer.MAX_VALUE;
            // Set the caret's offset to the end of just collapsed fold if necessary
            if (evt.getAffectedStartOffset() <= caretOffset && evt.getAffectedEndOffset() >= caretOffset) {
                for (int i = 0; i < evt.getFoldStateChangeCount(); i++) {
                    FoldStateChange change = evt.getFoldStateChange(i);
                    if (change.isCollapsedChanged()) {
                        Fold fold = change.getFold();
                        if (fold.isCollapsed() && fold.getStartOffset() <= caretOffset && fold.getEndOffset() >= caretOffset) {
                            if (fold.getStartOffset() < startOffset) {
                                startOffset = fold.getStartOffset();
                            }
                        }
                    }
                }
                if (startOffset != Integer.MAX_VALUE) {
                    setDot(startOffset, false);
                }
            }
            scrollToView = false;
        }        
        // Update caret's visual position
        // Post the caret update asynchronously since the fold hierarchy is updated before
        // the view hierarchy and the views so the dispatchUpdate() could be picking obsolete
        // view information.
        SwingUtilities.invokeLater(new Runnable() {
            public @Override void run() {
                updateAfterFoldHierarchyChange = true;
                dispatchUpdate(scrollToView); // do not scroll the window
            }
        });
    }
    
    private class ListenerImpl extends ComponentAdapter
    implements FocusListener {

        ListenerImpl() {
        }

        // FocusListener methods
        public @Override void focusGained(FocusEvent evt) {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine(
                        "BaseCaret.focusGained(); doc=" + // NOI18N
                        component.getDocument().getProperty(Document.TitleProperty) + '\n'
                );
            }
            
            JTextComponent c = component;
            if (c != null) {
                updateType();
                if (component.isEnabled()) {
                    if (component.isEditable()) {
                        setVisible(true);
                }
                    setSelectionVisible(true);
                }
                if (LOG.isLoggable(Level.FINER)) {
                    LOG.finer("Caret visibility: " + isVisible() + '\n'); // NOI18N
                }
            } else {
                if (LOG.isLoggable(Level.FINER)) {
                    LOG.finer("Text component is null, caret will not be visible" + '\n'); // NOI18N
                }
            }
        }

        public @Override void focusLost(FocusEvent evt) {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("BaseCaret.focusLost(); doc=" + // NOI18N
                        component.getDocument().getProperty(Document.TitleProperty) +
                        "\nFOCUS GAINER: " + evt.getOppositeComponent() + '\n' // NOI18N
                );
                if (LOG.isLoggable(Level.FINER)) {
                    LOG.finer("FOCUS EVENT: " + evt + '\n'); // NOI18N
                }
            }
	    setVisible(false);
            setSelectionVisible(evt.isTemporary());
        }

        // ComponentListener methods
        /**
         * May be called for either component or horizontal scrollbar.
         */
        public @Override void componentShown(ComponentEvent e) {
            // Called when horizontal scrollbar gets visible
            // (but the same listener added to component as well so must check first)
            // Check whether present caret position will not get hidden
            // under horizontal scrollbar and if so scroll the view
            Component hScrollBar = e.getComponent();
            if (hScrollBar != component) { // really called for horizontal scrollbar
                Component scrollPane = hScrollBar.getParent();
                if (caretBounds != null && scrollPane instanceof JScrollPane) {
                    Rectangle viewRect = ((JScrollPane)scrollPane).getViewport().getViewRect();
                    Rectangle hScrollBarRect = new Rectangle(
                            viewRect.x,
                            viewRect.y + viewRect.height,
                            hScrollBar.getWidth(),
                            hScrollBar.getHeight()
                            );
                    if (hScrollBarRect.intersects(caretBounds)) {
                        // Update caret's position
                        dispatchUpdate(true); // should be visible so scroll the view
                    }
                }
            }
        }

        
        /**
         * May be called for either component or horizontal scrollbar.
         */
        public @Override void componentResized(ComponentEvent e) {
            Component c = e.getComponent();
            if (c == component) { // called for component
                // In case the caretBounds are still null
                // (component not connected to hierarchy yet or it has zero size
                // so the modelToView() returned null) re-attempt to compute the bounds.
                if (caretBounds == null) {
                    dispatchUpdate(true);
                    if (caretBounds != null) { // detach the listener - no longer necessary
                        c.removeComponentListener(this);
                    }
                }
            }
        }

    } // End of ListenerImpl class

    public final void refresh() {
        updateType();
        SwingUtilities.invokeLater(new Runnable() {
            public @Override void run() {
                updateCaretBounds(); // the line height etc. may have change
            }
        });
    }
}
