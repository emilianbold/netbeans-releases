/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */
package org.netbeans.api.editor;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Composite;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
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
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.TransferHandler;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;
import javax.swing.text.StyleConstants;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.editor.document.AtomicLockDocument;
import org.netbeans.api.editor.document.AtomicLockEvent;
import org.netbeans.api.editor.document.AtomicLockListener;
import org.netbeans.api.editor.document.LineDocument;
import org.netbeans.api.editor.document.LineDocumentUtils;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.settings.FontColorNames;
import org.netbeans.api.editor.settings.FontColorSettings;
import org.netbeans.api.editor.settings.SimpleValueNames;
import org.netbeans.lib.editor.util.GapList;
import org.netbeans.lib.editor.util.ListenerList;
import org.netbeans.lib.editor.util.swing.DocumentListenerPriority;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.modules.editor.lib2.EditorPreferencesDefaults;
import org.netbeans.modules.editor.lib2.RectangularSelectionTransferHandler;
import org.netbeans.modules.editor.lib2.RectangularSelectionUtils;
import org.netbeans.modules.editor.lib2.actions.EditorActionUtilities;
import org.netbeans.modules.editor.lib2.view.DocumentView;
import org.netbeans.modules.editor.lib2.view.LockedViewHierarchy;
import org.netbeans.modules.editor.lib2.view.ViewHierarchy;
import org.netbeans.modules.editor.lib2.view.ViewHierarchyEvent;
import org.netbeans.modules.editor.lib2.view.ViewHierarchyListener;
import org.netbeans.modules.editor.lib2.view.ViewUtils;
import org.openide.util.Exceptions;
import org.openide.util.WeakListeners;

/**
 * Extension to standard Swing caret used by all NetBeans editors.
 * <br>
 * It supports multi-caret editing mode where an arbitrary number of carets
 * is placed at arbitrary positions throughout a document.
 * In this mode each caret is described by its <code>CaretInfo</code> object.
 *
 * @author Miloslav Metelka
 * @author Ralph Ruijs
 */
public final class EditorCaret implements Caret {

    // Temporary until rectangular selection gets ported to multi-caret support
    private static final String RECTANGULAR_SELECTION_PROPERTY = "rectangular-selection"; // NOI18N
    private static final String RECTANGULAR_SELECTION_REGIONS_PROPERTY = "rectangular-selection-regions"; // NOI18N
    
    // -J-Dorg.netbeans.editor.BaseCaret.level=FINEST
    private static final Logger LOG = Logger.getLogger(EditorCaret.class.getName());

    /**
     * Non-empty list of individual carets in the order they were created.
     * At least one item is always present.
     */
    private final @NonNull GapList<CaretInfo> carets;
    
    /**
     * Non-empty list of individual carets in the order they were created.
     * At least one item is always present.
     */
    private final @NonNull GapList<CaretInfo> sortedCarets;
    
    /** Component this caret is bound to */
    private JTextComponent component;
    
    /** List of individual carets */
    private boolean overwriteMode;

    private final ListenerList<EditorCaretListener> listenerList;

    private final ListenerList<ChangeListener> changeListenerList;

    /**
     * Implementor of various listeners.
     */
    private final ListenerImpl listenerImpl;
    
    /** Is the caret visible (after <code>setVisible(true)</code> call? */
    private boolean visible;

    /**
     * Whether blinking caret is currently visible on the screen.
     * <br/>
     * This changes from true to false after each tick of a timer
     * (assuming <code>visible == true</code>).
     */
    private boolean blinkVisible;

    /**
     * Determine if a possible selection would be displayed or not.
     */
    private boolean selectionVisible;

    /** Type of the caret */
    private CaretType type;

    private MouseState mouseState = MouseState.DEFAULT;
    
    /** Timer used for blinking the caret */
    private Timer flasher;

    /** Width of caret */
    private int thickCaretWidth;
    
    private Action selectWordAction;
    private Action selectLineAction;

    private AbstractDocument activeDoc;

    /** Whether the text is being modified under atomic lock.
     * If so just one caret change is fired at the end of all modifications.
     */
    private transient boolean inAtomicLock = false;
    private transient boolean inAtomicUnlock = false;
    
    /** Helps to check whether there was modification performed
     * and so the caret change needs to be fired.
     */
    private transient boolean modified;
    
    static final long serialVersionUID =-9113841520331402768L;

    /**
     * Set to true once the folds have changed. The caret should retain
     * its relative visual position on the screen.
     */
    private boolean updateAfterFoldHierarchyChange;
    
    /**
     * Whether at least one typing change occurred during possibly several atomic operations.
     */
    private boolean typingModificationOccurred;

    private Preferences prefs = null;

    private PreferenceChangeListener weakPrefsListener = null;
    
    private boolean caretUpdatePending;
    
    /**
     * Minimum selection start for word and line selections.
     * This helps to ensure that when extending word (or line) selections
     * the selection will always include at least the initially selected word (or line).
     */
    private int minSelectionStartOffset;
    
    private int minSelectionEndOffset;
    
    private boolean rectangularSelection;
    
    /**
     * Rectangle that corresponds to model2View of current point of selection.
     */
    private Rectangle rsDotRect;

    /**
     * Rectangle that corresponds to model2View of beginning of selection.
     */
    private Rectangle rsMarkRect;
    
    /**
     * Rectangle marking rectangular selection.
     */
    private Rectangle rsPaintRect;
    
    /**
     * List of start-pos and end-pos pairs that denote rectangular selection
     * on the selected lines.
     */
    private List<Position> rsRegions;

    /**
     * Used for showing the default cursor instead of the text cursor when the
     * mouse is over a block of selected text.
     * This field is used to prevent repeated calls to component.setCursor()
     * with the same cursor.
     */
    private boolean showingTextCursor = true;
    
    public EditorCaret() {
        carets = new GapList<>();
        sortedCarets = new GapList<>();
        listenerList = new ListenerList<>();
        changeListenerList = new ListenerList<>();
        listenerImpl = new ListenerImpl();
        CaretInfo caret = new CaretInfo(this);
        carets.add(caret);
        sortedCarets.add(caret);
    }

    @Override
    public int getDot() {
        return getLastCaret().getDot();
    }
    
    @Override
    public int getMark() {
        return getLastCaret().getMark();
    }
    
    /**
     * Get information about all existing carets in the order they were created.
     * <br/>
     * The list always has at least one item. The last caret (last item of the list)
     * is the most recent caret.
     * <br/>
     * The list is a copy of the current state of the carets. The list content itself will not change
     * but its contained caret infos may change due to subsequent calls to caret API or document modifications.
     * The list may be mutated by the client.
     * <br/>
     * This method should be called with document's read-lock acquired which will guarantee
     * stability of {@link CaretInfo#getDot() } and {@link CaretInfo#getMark() } and prevent
     * caret merging as a possible effect of document modifications.
     * 
     * @return copy of caret list with size &gt;= 1 containing information about all carets.
     */
    public @NonNull List<CaretInfo> getCarets() {
        synchronized (carets) {
            return carets.copy();
        }
    }
    
    /**
     * Get information about all existing carets sorted by dot positions in ascending order.
     * <br/>
     * The list is a copy of the current state of the carets. The list content itself will not change
     * but its contained caret infos may change due to subsequent calls to caret API or document modifications.
     * The list may be mutated by the client.
     * <br/>
     * This method should be called with document's read-lock acquired which will guarantee
     * stability of {@link CaretInfo#getDot() } and {@link CaretInfo#getMark() } and prevent
     * caret merging as a possible effect of document modifications.
     * 
     * @return copy of caret list with size &gt;= 1 sorted by dot positions in ascending order.
     */
    public @NonNull List<CaretInfo> getSortedCarets() {
        synchronized (carets) {
            return sortedCarets.copy();
        }
    }
    
    /**
     * Get info about the most recently created caret.
     * <br/>
     * For normal mode this is the only caret returned by {@link #getCarets() }.
     * <br/>
     * For multi-caret mode this is the last item in the list returned by {@link #getCarets() }.
     * 
     * @return last caret (the most recently added caret).
     */
    public @NonNull CaretInfo getLastCaret() {
        synchronized (carets) {
            return carets.get(carets.size() - 1);
        }
    }

    /**
     * Assign the caret a new offset in the underlying document.
     * <br/>
     * This method implicitly sets the selection range to zero.
     */
    public @Override void setDot(int offset) {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("setDot: offset=" + offset); //NOI18N
            if (LOG.isLoggable(Level.FINEST)) {
                LOG.log(Level.INFO, "setDot call stack", new Exception());
            }
        }
        setDotCaret(offset, getLastCaret(), true);
    }

    public void setDot(int offset, boolean expandFold) {
        // TODO get rid of this
    }
    
    public void addCaret(int offset) {
        JTextComponent c = component;
        AbstractDocument doc;
        if (c != null && (doc = activeDoc) != null) {
            boolean dotChanged = false;
            doc.readLock();
            try {
                if (doc != null && offset >= 0 && offset <= doc.getLength()) {
                    dotChanged = true;
                    try {
                        carets.add(new CaretInfo(this, doc.createPosition(offset), doc.createPosition(offset)));
                    } catch (BadLocationException e) {
                        throw new IllegalStateException(e.toString());
                        // setting the caret to wrong position leaves it at current position
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
    
    public @Override void moveDot(int offset) {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("moveDot: offset=" + offset); //NOI18N
        }
        
        moveDotCaret(offset, getLastCaret());
    }

    /**
     * Run a transaction over the editor caret that will typically modify one or more caret items.
     * <br/>
     * This method does not acquire a read lock over the component's document
     * but the caller of this method should acquire that lock to avoid instability of the offsets being used.
     *
     * @param caretTransaction non-null caret transaction to run.
     */
    public void runCaretTransaction(@NonNull CaretTransaction caretTransaction) {
        // [TODO] implement
    }

    /**
     * Create a new caret at the given position with a possible selection.
     * <br/>
     * The caret will become the last caret of the list returned by {@link #getCarets() }.
     * 
     * @param dotPos position of the newly created caret.
     * @param selectionStartPos beginning of the selection (the other end is dotPos) or <i>null</i> for no selection.
     *  The selectionStartPos may have higher offset than dotPos to select in a backward direction.
     * @return information about the newly created caret.
     */
    public @NonNull CaretInfo addCaret(@NonNull Position dotPos, @NonNull Position selectionStartPos) {
        CaretInfo caret = new CaretInfo(this, dotPos, selectionStartPos);
        // Test validity
        synchronized (carets) {
            carets.add(caret);
            // Add to sorted carets too
        }
        return caret;
    }
    
    /**
     * Add multiple carets at once.
     * <br/>
     * It is similar to calling {@link #addCaret(javax.swing.text.Position, javax.swing.text.Position) }
     * multiple times but this method is more efficient (it only fires caret change once).
     * 
     * @param dotAndSelectionStartPosPairs list of position pairs consisting of dot position and selection start position (may be null
     *  if the particular caret has no selection). The list must have even size.
     * @return list of caret infos. It has a half of the size of the dotAndSelectionStartPosPairs list.
     */
    public @NonNull List<CaretInfo> addCarets(@NonNull List<Position> dotAndSelectionStartPosPairs) {
        return Collections.emptyList(); // TBD
    }

    /**
     * Replace all current carets with the new ones.
     * <br/>
     * @param dotAndSelectionStartPosPairs list of position pairs consisting of dot position and selection start position (may be null
     *  if the particular caret has no selection). The list must have even size.
     * @return list of caret infos. It has a half of the size of the dotAndSelectionStartPosPairs list.
     */
    public @NonNull List<CaretInfo> replaceCarets(@NonNull List<Position> dotAndSelectionStartPosPairs) {
        return Collections.emptyList(); // TBD
    }

    /**
     * Remove last added caret (determined by {@link #getLastCaret() }).
     * <br/>
     * If there is just one caret the method has no effect.
     * 
     * @return the caret instance that was removed or null if there's just one caret.
     */
    public @NonNull CaretInfo removeLastCaret() {
        return new CaretInfo(this); // TBD
    }
    
    /**
     * Get information about the caret at the specified offset.
     *
     * @param offset the offset of the caret
     * @return CaretInfo for the caret at offset, null if there is no caret or
     * the offset is invalid
     */
    public @CheckForNull CaretInfo getCaretAt(int offset) {
        return null; // TBD
    }
    
    /**
     * Adds listener to track caret changes in detail.
     * 
     * @param listener 
     */
    public void addEditorCaretListener(@NonNull EditorCaretListener listener) {
        listenerList.add(listener);
    }
    
    /**
     * Adds listener to track caret position changes (to fulfil {@link Caret} interface).
     */
    @Override
    public void addChangeListener(@NonNull ChangeListener l) {
        changeListenerList.add(l);
    }

    public void removeEditorCaretListener(@NonNull EditorCaretListener listener) {
        listenerList.remove(listener);
    }
    
    /**
     * Removes listener to track caret position changes (to fulfil {@link Caret} interface).
     */
    @Override
    public void removeChangeListener(@NonNull ChangeListener l) {
        changeListenerList.remove(l);
    }

    private void fireEditorCaretChange(EditorCaretEvent evt) {
        for (EditorCaretListener listener : listenerList.getListeners()) {
            listener.caretChanged(evt);
        }
    }
    
    /** Notifies listeners that caret position has changed */
    private void fireStateChanged() {
        Runnable runnable = new Runnable() {
            public @Override void run() {
                JTextComponent c = component;
                if (c == null || c.getCaret() != EditorCaret.this) {
                    return;
                }
                ChangeEvent evt = new ChangeEvent(EditorCaret.this);
                List<ChangeListener> listeners = changeListenerList.getListeners();
                for (ChangeListener l : listeners) {
                    l.stateChanged(evt);
                }
            }
        };
        
        // Always fire in EDT
        if (inAtomicUnlock) { // Cannot fire within atomic lock9
            SwingUtilities.invokeLater(runnable);
        } else {
            ViewUtils.runInEDT(runnable);
        }
        updateSystemSelection();
    }

    /**
     * Determines if the caret is currently visible (it may be blinking depending on settings).
     * <p>
     * Caret becomes visible after <code>setVisible(true)</code> gets called on it.
     *
     * @return <code>true</code> if visible else <code>false</code>
     */
    @Override
    public boolean isVisible() {
        return visible;
    }

    /**
     * Sets the caret visibility, and repaints the caret.
     *
     * @param visible the visibility specifier
     * @see Caret#setVisible
     */
    @Override
    public void setVisible(boolean visible) {
        if (LOG.isLoggable(Level.FINER)) {
            LOG.finer("BaseCaret.setVisible(" + visible + ")\n");
            if (LOG.isLoggable(Level.FINEST)) {
                LOG.log(Level.INFO, "", new Exception());
            }
        }
        synchronized (this) {
            synchronized (listenerImpl) {
                if (flasher != null) {
                    if (this.visible) {
                        flasher.stop();
                    }
                    if (LOG.isLoggable(Level.FINER)) {
                        LOG.finer((visible ? "Starting" : "Stopping") + // NOI18N
                                " the caret blinking timer: " + dumpVisibility() + '\n'); // NOI18N
                    }
                    if (visible) {
                        flasher.start();
                    } else {
                        flasher.stop();
                    }
                }
            }

            this.visible = visible;
        }
        JTextComponent c = component;
        for (CaretInfo caret : carets) {
            if (c != null && caret.getCaretBounds() != null) {
                Rectangle repaintRect = caret.getCaretBounds();
                c.repaint(repaintRect);
            }
        }
    }

    /**
     * Is the selection visible?
     */
    @Override
    public boolean isSelectionVisible() {
        return selectionVisible;
    }

    /**
     * Sets the selection visibility
     */
    @Override
    public void setSelectionVisible(boolean v) {
        if (selectionVisible == v) {
            return;
        }
        JTextComponent c = component;
        Document doc;
        if (c != null && (doc = c.getDocument()) != null) {
            selectionVisible = v;
            // [TODO] ensure to repaint 
        }
    }

    /** Called when UI is being installed into JTextComponent */
    @Override
    public void install(JTextComponent c) {
        assert (SwingUtilities.isEventDispatchThread()); // must be done in AWT
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Installing to " + s2s(c)); //NOI18N
        }
        
        component = c;
        visible = true;
        blinkVisible = true;
        
        modelChanged(null, c.getDocument());

        // Attempt to assign initial bounds - usually here the component
        // is not yet added to the component hierarchy.
        updateCaretBounds();
        
        if(carets.isEmpty() || getLastCaret().getCaretBounds() == null) {
            // For null bounds wait for the component to get resized
            // and attempt to recompute bounds then
            component.addComponentListener(listenerImpl);
        }

        component.addPropertyChangeListener(listenerImpl);
        component.addFocusListener(listenerImpl);
        component.addMouseListener(listenerImpl);
        component.addMouseMotionListener(listenerImpl);
        ViewHierarchy.get(component).addViewHierarchyListener(listenerImpl);

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
        
        c.removeComponentListener(listenerImpl);
        c.removePropertyChangeListener(listenerImpl);
        c.removeFocusListener(listenerImpl);
        c.removeMouseListener(listenerImpl);
        c.removeMouseMotionListener(listenerImpl);
        ViewHierarchy.get(c).removeViewHierarchyListener(listenerImpl);

        
        modelChanged(activeDoc, null);
    }

    /**
     * Refreshes caret display on the screen.
     * Some height or view changes may result in the caret going off the screen. In some cases, this is not desirable,
     * as the user's work may be interrupted by e.g. an automatic refresh. This method repositions the view so the
     * caret remains blinkVisible.
     * <p/>
     * The method has two modes: it can reposition the view just if it originally displayed the caret and the caret became
     * invisible, and it can scroll the caret into view unconditionally.
     * @param retainInView true to scroll only if the caret was blinkVisible. False to refresh regardless of visibility.
     */
    public void refresh(boolean retainInView) {
        CaretInfo caret = getLastCaret();
        Rectangle b = caret.getCaretBounds();
        updateAfterFoldHierarchyChange = b != null;
        boolean wasInView = b != null && component.getVisibleRect().intersects(b);
        
        update(!retainInView || wasInView);
    }

    private void updateType() {
        JTextComponent c = component;
        if (c != null && prefs != null && !Boolean.TRUE.equals(c.getClientProperty("AsTextField"))) {
            String newTypeStr;
            int newWidth = 0;
            Color caretColor = Color.black;
            
            if (overwriteMode) {
                newTypeStr = prefs.get(SimpleValueNames.CARET_TYPE_OVERWRITE_MODE, EditorPreferencesDefaults.defaultCaretTypeOverwriteMode);
            } else { // insert mode
                newTypeStr = prefs.get(SimpleValueNames.CARET_TYPE_INSERT_MODE, EditorPreferencesDefaults.defaultCaretTypeInsertMode);
                newWidth = prefs.getInt(SimpleValueNames.THICK_CARET_WIDTH, EditorPreferencesDefaults.defaultThickCaretWidth);
            }

            FontColorSettings fcs = MimeLookup.getLookup(DocumentUtilities.getMimeType(c)).lookup(FontColorSettings.class);
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
            
            this.type = CaretType.decode(newTypeStr);
            this.thickCaretWidth = newWidth;
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
        final JTextComponent c = component;
        final boolean[] ret = { false };
        AbstractDocument doc;
        if (c != null && (doc = activeDoc) != null) {
            doc.readLock();
            try {
                for (CaretInfo caret : carets) {
                    ret[0] |= updateRealCaretBounds(caret, doc, c);
                }
            } finally {
                doc.readUnlock();
            }
        }
        LOG.log(Level.FINE, "updateCaretBounds: no change, old={0}", carets); //NOI18N
        return ret[0];
    }
    
    private boolean updateCaretBounds(CaretInfo caret) {
        JTextComponent c = component;
        boolean ret = false;
        AbstractDocument doc;
        if (c != null && (doc = activeDoc) != null) {
            doc.readLock();
            try {
                ret = updateRealCaretBounds(caret, doc, c);
            } finally {
                doc.readUnlock();
            }
        }
        LOG.log(Level.FINE, "updateCaretBounds: no change, old={0}", carets); //NOI18N
        return ret;
    }
    
    private boolean updateRealCaretBounds(CaretInfo caret, Document doc, JTextComponent c) {
        Position dotPos = caret.getDotPosition();
        int offset = dotPos == null? 0 : dotPos.getOffset();
        if (offset > doc.getLength()) {
            offset = doc.getLength();
        }
        CharSequence docText = DocumentUtilities.getText(doc);
        caret.setDotChar(docText.charAt(offset));
        Rectangle newCaretBounds;
        try {
            DocumentView docView = DocumentView.get(c);
            if (docView != null) {
                // docView.syncViewsRebuild(); // Make sure pending views changes are resolved
            }
            newCaretBounds = c.getUI().modelToView(
                    c, offset, Position.Bias.Forward);
            // [TODO] Temporary fix - impl should remember real bounds computed by paintCustomCaret()
            if (newCaretBounds != null) {
                newCaretBounds.width = Math.max(newCaretBounds.width, 2);
            }

        } catch (BadLocationException e) {
            
            newCaretBounds = null;
        }
        if (newCaretBounds != null) {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.log(Level.FINE, "updateCaretBounds: old={0}, new={1}, offset={2}",
                        new Object[]{caret.getCaretBounds(), newCaretBounds, offset}); //NOI18N
            }
            caret.setCaretBounds(newCaretBounds);
            return true;
        } else {
            return false;
        }
    }

    private void modelChanged(Document oldDoc, Document newDoc) {
        if (oldDoc != null) {
            // ideally the oldDoc param shouldn't exist and only listenDoc should be used
            assert (oldDoc == activeDoc);

            DocumentUtilities.removeDocumentListener(
                    oldDoc, listenerImpl, DocumentListenerPriority.CARET_UPDATE);
            AtomicLockDocument oldAtomicDoc = LineDocumentUtils.as(oldDoc, AtomicLockDocument.class);
            if (oldAtomicDoc != null) {
                oldAtomicDoc.removeAtomicLockListener(listenerImpl);
            }

            carets.clear();
            carets.add(new CaretInfo(this));

            activeDoc = null;
            if (prefs != null && weakPrefsListener != null) {
                prefs.removePreferenceChangeListener(weakPrefsListener);
            }
        }

        // EditorCaret only installs successfully into AbstractDocument based documents that carry a mime-type
        String mimeType;
        if ((newDoc instanceof AbstractDocument) && (mimeType = DocumentUtilities.getMimeType(newDoc)) != null) {
            activeDoc = (AbstractDocument) newDoc;
            DocumentUtilities.addDocumentListener(
                    newDoc, listenerImpl, DocumentListenerPriority.CARET_UPDATE);
            AtomicLockDocument newAtomicDoc = LineDocumentUtils.as(oldDoc, AtomicLockDocument.class);
            if (newAtomicDoc != null) {
                newAtomicDoc.addAtomicLockListener(listenerImpl);
            }

            // Leave caretPos and markPos null => offset==0
            prefs = MimeLookup.getLookup(mimeType).lookup(Preferences.class);
            if (prefs != null) {
                weakPrefsListener = WeakListeners.create(PreferenceChangeListener.class, listenerImpl, prefs);
                prefs.addPreferenceChangeListener(weakPrefsListener);
            }
            
            ViewUtils.runInEDT(
                new Runnable() {
                    public @Override void run() {
                        updateType();
                    }
                }
            );
        }
    }

    /**
     * Renders the caret
     */
    @Override
    public void paint(Graphics g) {
        JTextComponent c = component;
        if (c == null) return;
        
        CaretInfo lastCaret = getLastCaret();

        // Check whether the caret was moved but the component was not
        // validated yet and therefore the caret bounds are still null
        // and if so compute the bounds and scroll the view if necessary.
        if (getDot() != 0 && lastCaret.getCaretBounds() == null) {
            update(true);
        }
        
        for (CaretInfo caret : carets) {
            if (LOG.isLoggable(Level.FINEST)) {
                LOG.finest("BaseCaret.paint(): caretBounds=" + caret.getCaretBounds() + dumpVisibility() + '\n');
            }
            if (caret.getCaretBounds() != null && isVisible() && blinkVisible) {
                paintCaret(g, caret);
            }
            if (rectangularSelection && rsPaintRect != null && g instanceof Graphics2D) {
                Graphics2D g2d = (Graphics2D) g;
                Stroke stroke = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[] {4, 2}, 0);
                Stroke origStroke = g2d.getStroke();
                Color origColor = g2d.getColor();
                try {
                    // Render translucent rectangle
                    Color selColor = c.getSelectionColor();
                    g2d.setColor(selColor);
                    Composite origComposite = g2d.getComposite();
                    try {
                        g2d.setComposite(AlphaComposite.SrcOver.derive(0.2f));
                        g2d.fill(rsPaintRect);
                    } finally {
                        g2d.setComposite(origComposite);
                    }
                    // Paint stroked line around rectangular selection rectangle
                    g.setColor(c.getCaretColor());
                    g2d.setStroke(stroke);
                    Rectangle onePointSmallerRect = new Rectangle(rsPaintRect);
                    onePointSmallerRect.width--;
                    onePointSmallerRect.height--;
                    g2d.draw(onePointSmallerRect);

                } finally {
                    g2d.setStroke(origStroke);
                    g2d.setColor(origColor);
                }
            }
        }
    }

    private void paintCaret(Graphics g, CaretInfo caret) {
        JTextComponent c = component;
        if (c != null) {
            g.setColor(c.getCaretColor());
            Rectangle caretBounds = caret.getCaretBounds();
            switch (type) {
                case THICK_LINE_CARET:
                    g.fillRect(caretBounds.x, caretBounds.y, this.thickCaretWidth, caretBounds.height - 1);
                    break;

                case THIN_LINE_CARET:
                    int upperX = caret.getCaretBounds().x;
                    g.drawLine((int) upperX, caret.getCaretBounds().y, caret.getCaretBounds().x,
                            (caret.getCaretBounds().y + caret.getCaretBounds().height - 1));
                    break;

                case BLOCK_CARET:
                    // Use a CaretOverwriteModeHighlighting layer to paint the caret
                    break;

                default:
                    throw new IllegalStateException("Invalid caret type=" + type);
            }
        }
    }

    /** Update the caret's visual position */
    private void dispatchUpdate(final boolean scrollViewToCaret) {
        /* Ensure that the caret's document listener will be added AFTER the views hierarchy's
         * document listener so the code can run synchronously again
         * which should eliminate the problem with caret lag.
         * However the document can be modified from non-AWT thread
         * which is the case in #57316 and in that case the code
         * must run asynchronously in AWT thread.
         */
        ViewUtils.runInEDT(
            new Runnable() {
                public @Override void run() {
                    AbstractDocument doc = activeDoc;
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
    private void update(boolean scrollViewToCaret) {
        caretUpdatePending = false;
        JTextComponent c = component;
        if (c != null) {
            if (!c.isValid()) {
                c.validate();
            }
            Document doc = c.getDocument();
            if (doc != null) {
                for (CaretInfo caret : carets) {
                    Rectangle oldCaretBounds = caret.getCaretBounds(); // no need to deep copy
                    if (oldCaretBounds != null) {
                        c.repaint(oldCaretBounds);
                    }

                    // note - the order is important ! caret bounds must be updated even if the fold flag is true.
                    if (updateCaretBounds(caret) || updateAfterFoldHierarchyChange) {
                        Rectangle scrollBounds = new Rectangle(caret.getCaretBounds());

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
                                    JScrollBar hScrollBar = ((JScrollPane) scrollPane).getHorizontalScrollBar();
                                    if (hScrollBar != null) {
                                        int hScrollBarHeight = hScrollBar.getPreferredSize().height;
                                        Dimension extentSize = ((JViewport) viewport).getExtentSize();
                                        // If the extent size is high enough then extend
                                        // the scroll region by extra vertical space
                                        if (extentSize.height >= caret.getCaretBounds().height + hScrollBarHeight) {
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
                        boolean explicit = false;
                        if (oldCaretBounds != null && (!scrollViewToCaret || updateAfterFoldHierarchyChange)) {
                            int oldRelY = oldCaretBounds.y - visibleBounds.y;
                            // Only fix if the caret is within blinkVisible bounds and the new x or y coord differs from the old one
                            if (LOG.isLoggable(Level.FINER)) {
                                LOG.log(Level.FINER, "oldCaretBounds: {0}, visibleBounds: {1}, caretBounds: {2}",
                                        new Object[]{oldCaretBounds, visibleBounds, caret.getCaretBounds()});
                            }
                            if (oldRelY >= 0 && oldRelY < visibleBounds.height
                                    && (oldCaretBounds.y != caret.getCaretBounds().y || oldCaretBounds.x != caret.getCaretBounds().x)) {
                                doScroll = true; // Perform explicit scrolling
                                explicit = true;
                                int oldRelX = oldCaretBounds.x - visibleBounds.x;
                                // Do not retain the horizontal caret bounds by scrolling
                                // since many modifications do not explicitly say that they are typing modifications
                                // and this would cause problems like #176268
//                            scrollBounds.x = Math.max(caretBounds.x - oldRelX, 0);
                                scrollBounds.y = Math.max(caret.getCaretBounds().y - oldRelY, 0);
//                            scrollBounds.width = visibleBounds.width;
                                scrollBounds.height = visibleBounds.height;
                            }
                        }

                        // Historically the caret is expected to appear
                        // in the middle of the window if setDot() gets called
                        // e.g. by double-clicking in Navigator.
                        // If the caret bounds are more than a caret height below the present
                        // blinkVisible view bounds (or above the view bounds)
                        // then scroll the window so that the caret is in the middle
                        // of the blinkVisible window to see the context around the caret.
                        // This should work fine with PgUp/Down because these
                        // scroll the view explicitly.
                        if (scrollViewToCaret
                                && !explicit
                                && // #219580: if the preceding if-block computed new scrollBounds, it cannot be offset yet more
                                /* # 70915 !updateAfterFoldHierarchyChange && */ (caret.getCaretBounds().y > visibleBounds.y + visibleBounds.height + caret.getCaretBounds().height
                                || caret.getCaretBounds().y + caret.getCaretBounds().height < visibleBounds.y - caret.getCaretBounds().height)) {
                            // Scroll into the middle
                            scrollBounds.y -= (visibleBounds.height - caret.getCaretBounds().height) / 2;
                            scrollBounds.height = visibleBounds.height;
                        }
                        if (LOG.isLoggable(Level.FINER)) {
                            LOG.finer("Resetting fold flag, current: " + updateAfterFoldHierarchyChange);
                        }
                        updateAfterFoldHierarchyChange = false;

                        // Ensure that the viewport will be scrolled either to make the caret blinkVisible
                        // or to retain cart's relative visual position against the begining of the viewport's blinkVisible rectangle.
                        if (doScroll) {
                            if (LOG.isLoggable(Level.FINER)) {
                                LOG.finer("Scrolling to: " + scrollBounds);
                            }
                            c.scrollRectToVisible(scrollBounds);
                            if (!c.getVisibleRect().intersects(scrollBounds)) {
                                // HACK: see #219580: for some reason, the scrollRectToVisible may fail.
                                c.scrollRectToVisible(scrollBounds);
                            }
                        }
                        resetBlink();
                        c.repaint(caret.getCaretBounds());
                    }
                }
            }
        }
    }

    private void updateSystemSelection() {
        if (getDot() != getMark() && component != null) {
            Clipboard clip = component.getToolkit().getSystemSelection();
            
            if (clip != null) {
                clip.setContents(new java.awt.datatransfer.StringSelection(component.getSelectedText()), null);
            }
        }
    }

    private void updateRectangularSelectionPositionBlocks() {
        JTextComponent c = component;
        if (rectangularSelection) {
            AbstractDocument doc = activeDoc;
            if (doc != null) {
                doc.readLock();
                try {
                    if (rsRegions == null) {
                        rsRegions = new ArrayList<Position>();
                        component.putClientProperty(RECTANGULAR_SELECTION_REGIONS_PROPERTY, rsRegions);
                    }
                    synchronized (rsRegions) {
                        if (LOG.isLoggable(Level.FINE)) {
                            LOG.fine("Rectangular-selection position regions:\n");
                        }
                        rsRegions.clear();
                        if (rsPaintRect != null) {
                            LockedViewHierarchy lvh = ViewHierarchy.get(c).lock();
                            try {
                                float rowHeight = lvh.getDefaultRowHeight();
                                double y = rsPaintRect.y;
                                double maxY = y + rsPaintRect.height;
                                double minX = rsPaintRect.getMinX();
                                double maxX = rsPaintRect.getMaxX();
                                do {
                                    int startOffset = lvh.viewToModel(minX, y, null);
                                    int endOffset = lvh.viewToModel(maxX, y, null);
                                    // They could be swapped due to RTL text
                                    if (startOffset > endOffset) {
                                        int tmp = startOffset;
                                        startOffset = endOffset;
                                        endOffset = tmp;
                                    }
                                    Position startPos = activeDoc.createPosition(startOffset);
                                    Position endPos = activeDoc.createPosition(endOffset);
                                    rsRegions.add(startPos);
                                    rsRegions.add(endPos);
                                    if (LOG.isLoggable(Level.FINE)) {
                                        LOG.fine("  <" + startOffset + "," + endOffset + ">\n");
                                    }
                                    y += rowHeight;
                                } while (y < maxY);
                                c.putClientProperty(RECTANGULAR_SELECTION_REGIONS_PROPERTY, rsRegions);
                            } finally {
                                lvh.unlock();
                            }
                        }
                    }
                } catch (BadLocationException ex) {
                    Exceptions.printStackTrace(ex);
                } finally {
                    doc.readUnlock();
                }
            }
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
                    this.blinkVisible = true;
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

    /** Saves the current caret position.  This is used when
    * caret up or down actions occur, moving between lines
    * that have uneven end positions.
    *
    * @param p  the Point to use for the saved position
    */
    public @Override void setMagicCaretPosition(Point p) {
        if(carets.isEmpty()) {
            // There needs be at least one caret in the document.
            carets.add(new CaretInfo(this));
        }
        getLastCaret().setMagicCaretPosition(p);
    }

    /** Get position used to mark beginning of the selected block */
    public @Override final Point getMagicCaretPosition() {
        return carets.isEmpty() && carets.size() > 0 ? carets.iterator().next().getMagicCaretPosition() : null;
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
                flasher = new Timer(rate, listenerImpl);
            }
            if (flasher != null) {
                if (rate > 0) {
                    if (flasher.getDelay() != rate) {
                        flasher.setDelay(rate);
                    }
                } else { // zero rate - don't blink
                    flasher.stop();
                    flasher.removeActionListener(listenerImpl);
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

    void setDotCaret(final int offset, final CaretInfo caret, final boolean expandFold) throws IllegalStateException {
        JTextComponent c = component;
        AbstractDocument doc;
        if (c != null && (doc = activeDoc) != null) {
            doc.readLock();
            try {
                boolean dotChanged = false;
                if (offset >= 0 && offset <= doc.getLength()) {
                    dotChanged = true;
                    try {
                        caret.setDotPos(doc.createPosition(offset));
                        caret.setMarkPos(doc.createPosition(offset));

                        @SuppressWarnings("unchecked")
                        Callable<Boolean> cc = (Callable<Boolean>) c.getClientProperty("org.netbeans.api.fold.expander");
                        if (cc != null && expandFold) {
                            // the caretPos/markPos were already called.
                            // nothing except the document is locked at this moment.
                            try {
                                cc.call();
                            } catch (Exception ex) {
                                Exceptions.printStackTrace(ex);
                            }
                        }
//                        if (rectangularSelection) {
//                            setRectangularSelectionToDotAndMark();
//                        }

                    } catch (BadLocationException e) {
                        throw new IllegalStateException(e.toString());
                        // setting the caret to wrong position leaves it at current position
                    }
                }
                if (dotChanged) {
                    fireStateChanged();
                    dispatchUpdate(true);
                }
            } finally {
                doc.readUnlock();
            }
            
        }
    }
    
    void moveDotCaret(int offset, CaretInfo caret) throws IllegalStateException {
        JTextComponent c = component;
        AbstractDocument doc;
        if (c != null && (doc = activeDoc) != null) {
            if (offset >= 0 && offset <= doc.getLength()) {
                doc.readLock();
                try {
                    int oldCaretPos = caret.getDot();
                    if (offset == oldCaretPos) { // no change
                        return;
                    }
                    caret.setDotPos(doc.createPosition(offset));
                    // Selection highlighting should be handled automatically by highlighting layers
                    if (rectangularSelection) {
                        Rectangle r = c.modelToView(offset);
                        if (rsDotRect != null) {
                            rsDotRect.y = r.y;
                            rsDotRect.height = r.height;
                        } else {
                            rsDotRect = r;
                        }
                        updateRectangularSelectionPaintRect();
                    }
                } catch (BadLocationException e) {
                    throw new IllegalStateException(e.toString());
                    // position is incorrect
                } finally {
                    doc.readUnlock();
                }
            }
            fireStateChanged();
            dispatchUpdate(true);
        }
    }
    
    private void adjustRectangularSelectionMouseX(int x, int y) {
        if (!rectangularSelection) {
            return;
        }
        JTextComponent c = component;
        int offset = c.viewToModel(new Point(x, y));
        Rectangle r = null;;
        if (offset >= 0) {
            try {
                r = c.modelToView(offset);
            } catch (BadLocationException ex) {
                r = null;
            }
        }
        if (r != null) {
            float xDiff = x - r.x;
            if (xDiff > 0) {
                float charWidth;
                LockedViewHierarchy lvh = ViewHierarchy.get(c).lock();
                try {
                    charWidth = lvh.getDefaultCharWidth();
                } finally {
                    lvh.unlock();
                }
                int n = (int) (xDiff / charWidth);
                r.x += n * charWidth;
                r.width = (int) charWidth;
            }
            rsDotRect.x = r.x;
            rsDotRect.width = r.width;
            updateRectangularSelectionPaintRect();
            fireStateChanged();
        }
    }
    
    public void setRectangularSelectionToDotAndMark() {
        int dotOffset = getDot();
        int markOffset = getMark();
        try {
            rsDotRect = component.modelToView(dotOffset);
            rsMarkRect = component.modelToView(markOffset);
        } catch (BadLocationException ex) {
            rsDotRect = rsMarkRect = null;
        }
        updateRectangularSelectionPaintRect();
    }

    public void updateRectangularUpDownSelection() {
        JTextComponent c = component;
        int dotOffset = getDot();
        try {
            Rectangle r = c.modelToView(dotOffset);
            rsDotRect.y = r.y;
            rsDotRect.height = r.height;
        } catch (BadLocationException ex) {
            // Leave rsDotRect unchanged
        }
    }
    
    /**
     * Extend rectangular selection either by char in a specified selection
     * or by word (if ctrl is pressed).
     *
     * @param toRight true for right or false for left.
     * @param ctrl 
     */
    public void extendRectangularSelection(boolean toRight, boolean ctrl) {
        JTextComponent c = component;
        Document doc = c.getDocument();
        int dotOffset = getDot();
        Element lineRoot = doc.getDefaultRootElement();
        int lineIndex = lineRoot.getElementIndex(dotOffset);
        Element lineElement = lineRoot.getElement(lineIndex);
        float charWidth;
        LockedViewHierarchy lvh = ViewHierarchy.get(c).lock();
        try {
            charWidth = lvh.getDefaultCharWidth();
        } finally {
            lvh.unlock();
        }
        int newDotOffset = -1;
        try {
            int newlineOffset = lineElement.getEndOffset() - 1;
            Rectangle newlineRect = c.modelToView(newlineOffset);
            if (!ctrl) {
                if (toRight) {
                    if (rsDotRect.x < newlineRect.x) {
                        newDotOffset = dotOffset + 1;
                    } else {
                        rsDotRect.x += charWidth;
                    }
                } else { // toLeft
                    if (rsDotRect.x > newlineRect.x) {
                        rsDotRect.x -= charWidth;
                        if (rsDotRect.x < newlineRect.x) { // Fix on rsDotRect
                            newDotOffset = newlineOffset;
                        }
                    } else {
                        newDotOffset = Math.max(dotOffset - 1, lineElement.getStartOffset());
                    }
                }

            } else { // With Ctrl
                int numVirtualChars = 8; // Number of virtual characters per one Ctrl+Shift+Arrow press
                if (toRight) {
                    if (rsDotRect.x < newlineRect.x) {
//[TODO] fix                        newDotOffset = Math.min(Utilities.getNextWord(c, dotOffset), lineElement.getEndOffset() - 1);
                    } else { // Extend virtually
                        rsDotRect.x += numVirtualChars * charWidth;
                    }
                } else { // toLeft
                    if (rsDotRect.x > newlineRect.x) { // Virtually extended
                        rsDotRect.x -= numVirtualChars * charWidth;
                        if (rsDotRect.x < newlineRect.x) {
                            newDotOffset = newlineOffset;
                        }
                    } else {
//[TODO] fix                        newDotOffset = Math.max(Utilities.getPreviousWord(c, dotOffset), lineElement.getStartOffset());
                    }
                }
            }

            if (newDotOffset != -1) {
                rsDotRect = c.modelToView(newDotOffset);
                moveDot(newDotOffset); // updates rs and fires state change
            } else {
                updateRectangularSelectionPaintRect();
                fireStateChanged();
            }
        } catch (BadLocationException ex) {
            // Leave selection as is
        }
    }
    
    private void updateRectangularSelectionPaintRect() {
        // Repaint current rect
        JTextComponent c = component;
        Rectangle repaintRect = rsPaintRect;
        if (rsDotRect == null || rsMarkRect == null) {
            return;
        }
        Rectangle newRect = new Rectangle();
        if (rsDotRect.x < rsMarkRect.x) { // Swap selection to left
            newRect.x = rsDotRect.x; // -1 to make the visual selection non-empty
            newRect.width = rsMarkRect.x - newRect.x;
        } else { // Extend or shrink on right
            newRect.x = rsMarkRect.x;
            newRect.width = rsDotRect.x - newRect.x;
        }
        if (rsDotRect.y < rsMarkRect.y) {
            newRect.y = rsDotRect.y;
            newRect.height = (rsMarkRect.y + rsMarkRect.height) - newRect.y;
        } else {
            newRect.y = rsMarkRect.y;
            newRect.height = (rsDotRect.y + rsDotRect.height) - newRect.y;
        }
        if (newRect.width < 2) {
            newRect.width = 2;
        }
        rsPaintRect = newRect;

        // Repaint merged region with original rect
        if (repaintRect == null) {
            repaintRect = rsPaintRect;
        } else {
            repaintRect = repaintRect.union(rsPaintRect);
        }
        c.repaint(repaintRect);
        
        updateRectangularSelectionPositionBlocks();
    }

    private void selectEnsureMinSelection(int mark, int dot, int newDot) {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("selectEnsureMinSelection: mark=" + mark + ", dot=" + dot + ", newDot=" + newDot); // NOI18N
        }
        if (dot >= mark) { // Existing forward selection
            if (newDot >= mark) {
                moveDot(Math.max(newDot, minSelectionEndOffset));
            } else { // newDot < mark => swap mark and dot
                setDot(minSelectionEndOffset);
                moveDot(Math.min(newDot, minSelectionStartOffset));
            }

        } else { // Existing backward selection 
            if (newDot <= mark) {
                moveDot(Math.min(newDot, minSelectionStartOffset));
            } else { // newDot > mark => swap mark and dot
                setDot(minSelectionStartOffset);
                moveDot(Math.max(newDot, minSelectionEndOffset));
            }
        }
    }
    
    private boolean isLeftMouseButtonExt(MouseEvent evt) {
        return (SwingUtilities.isLeftMouseButton(evt)
                && !(evt.isPopupTrigger())
                && (evt.getModifiers() & (InputEvent.META_MASK/* | InputEvent.ALT_MASK*/)) == 0);
    }
    
    private boolean isMiddleMouseButtonExt(MouseEvent evt) {
        return (evt.getButton() == MouseEvent.BUTTON2) &&
                (evt.getModifiersEx() & (InputEvent.CTRL_DOWN_MASK | InputEvent.META_DOWN_MASK | /* cannot be tested bcs of bug in JDK InputEvent.ALT_DOWN_MASK | */ InputEvent.ALT_GRAPH_DOWN_MASK)) == 0;
    }

    private int mapDragOperationFromModifiers(MouseEvent e) {
        int mods = e.getModifiersEx();
        
        if ((mods & InputEvent.BUTTON1_DOWN_MASK) == 0) {
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
    private boolean isDragPossible(MouseEvent e) {
	Object src = e.getSource();
	if (src instanceof JComponent) {
	    JComponent comp = (JComponent) src;
            boolean possible =  (comp == null) ? false : (comp.getTransferHandler() != null);
            if (possible && comp instanceof JTextComponent) {
                JTextComponent c = (JTextComponent) comp;
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
        }
        return false;
    }
    
    void scheduleCaretUpdate() {
        if (!caretUpdatePending) {
            caretUpdatePending = true;
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    update(false);
                }
            });
        }
    }

    private void refresh() {
        updateType();
        SwingUtilities.invokeLater(new Runnable() {
            public @Override void run() {
                updateCaretBounds(); // the line height etc. may have change
            }
        });
    }
    
    private static String logMouseEvent(MouseEvent evt) {
        return "x=" + evt.getX() + ", y=" + evt.getY() + ", clicks=" + evt.getClickCount() //NOI18N
            + ", component=" + s2s(evt.getComponent()) //NOI18N
            + ", source=" + s2s(evt.getSource()) + ", button=" + evt.getButton() + ", mods=" + evt.getModifiers() + ", modsEx=" + evt.getModifiersEx(); //NOI18N
    }

    private static String s2s(Object o) {
        return o == null ? "null" : o.getClass().getName() + "@" + Integer.toHexString(System.identityHashCode(o)); //NOI18N
    }

    private final class ListenerImpl extends ComponentAdapter
    implements DocumentListener, AtomicLockListener, MouseListener, MouseMotionListener, FocusListener, ViewHierarchyListener,
            PropertyChangeListener, ActionListener, PreferenceChangeListener
    {

        ListenerImpl() {
        }

        public @Override void preferenceChange(PreferenceChangeEvent evt) {
            String setingName = evt == null ? null : evt.getKey();
            if (setingName == null || SimpleValueNames.CARET_BLINK_RATE.equals(setingName)) {
                int rate = prefs.getInt(SimpleValueNames.CARET_BLINK_RATE, -1);
                if (rate == -1) {
                    rate = EditorPreferencesDefaults.defaultCaretBlinkRate;
                }
                setBlinkRate(rate);
                refresh();
            }
        }

        public @Override void propertyChange(PropertyChangeEvent evt) {
            String propName = evt.getPropertyName();
            JTextComponent c = component;
            if ("document".equals(propName)) { // NOI18N
                if (c != null) {
                    modelChanged(activeDoc, c.getDocument());
                }

            } else if (EditorUtilities.CARET_OVERWRITE_MODE_PROPERTY.equals(propName)) {
                Boolean b = (Boolean) evt.getNewValue();
                overwriteMode = (b != null) ? b : false;
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
                        JScrollPane scrollPane = (JScrollPane) parent;
                        JScrollBar hScrollBar = scrollPane.getHorizontalScrollBar();
                        if (hScrollBar != null) {
                            // Add weak listener so that editor pane could be removed
                            // from scrollpane without being held by scrollbar
                            hScrollBar.addComponentListener(
                                    (ComponentListener) WeakListeners.create(
                                            ComponentListener.class, listenerImpl, hScrollBar));
                        }
                    }
                }
            } else if ("enabled".equals(propName)) {
                Boolean enabled = (Boolean) evt.getNewValue();
                if (component.isFocusOwner()) {
                    if (enabled == Boolean.TRUE) {
                        if (component.isEditable()) {
                            setVisible(true);
                        }
                        setSelectionVisible(true);
                    } else {
                        setVisible(false);
                        setSelectionVisible(false);
                    }
                }
            } else if (RECTANGULAR_SELECTION_PROPERTY.equals(propName)) {
                boolean origRectangularSelection = rectangularSelection;
                rectangularSelection = Boolean.TRUE.equals(component.getClientProperty(RECTANGULAR_SELECTION_PROPERTY));
                if (rectangularSelection != origRectangularSelection) {
                    if (rectangularSelection) {
                        setRectangularSelectionToDotAndMark();
                        RectangularSelectionTransferHandler.install(component);

                    } else { // No rectangular selection
                        RectangularSelectionTransferHandler.uninstall(component);
                    }
                    fireStateChanged();
                }
            }
        }

        // ActionListener methods
        /**
         * Fired when blink timer fires
         */
        public @Override void actionPerformed(ActionEvent evt) {
            JTextComponent c = component;
            if (c != null) {
                blinkVisible = !blinkVisible;
                for (CaretInfo caret : carets) {
                    if (caret.getCaretBounds() != null) {
                        Rectangle repaintRect = caret.getCaretBounds();
                        c.repaint(repaintRect);
                    }
                }
            }
        }

        // DocumentListener methods
        public @Override void insertUpdate(DocumentEvent evt) {
            JTextComponent c = component;
            if (c != null) {
                int offset = evt.getOffset();
                int endOffset = offset + evt.getLength();
                if (evt.getOffset() == 0) {
                    for (CaretInfo caret : carets) {
                        // Insert at offset 0 the marks would stay at offset == 0
                        if (caret.getMarkPosition() == null || caret.getMarkPosition().getOffset() == 0) {
                            try {
                                caret.setMarkPos(activeDoc.createPosition(endOffset));
                            } catch (BadLocationException ex) {
                                Exceptions.printStackTrace(ex);
                            }
                        }
                        if (caret.getDotPosition() == null || caret.getDotPosition().getOffset() == 0) {
                            try {
                                caret.setDotPos(activeDoc.createPosition(endOffset));
                            } catch (BadLocationException ex) {
                                Exceptions.printStackTrace(ex);
                            }
                        }
                    }
                }
                // [TODO] proper undo solution
                modified = true;
                modifiedUpdate(true);
                
            }
        }

        public @Override void removeUpdate(DocumentEvent evt) {
            JTextComponent c = component;
            if (c != null) {
                // [TODO] proper undo solution
                modified = true;

                modifiedUpdate(true);
            }
        }

        public @Override void changedUpdate(DocumentEvent evt) {
        }

        public @Override
        void atomicLock(AtomicLockEvent evt) {
            inAtomicLock = true;
        }

        public @Override
        void atomicUnlock(AtomicLockEvent evt) {
            inAtomicLock = false;
            inAtomicUnlock = true;
            try {
                modifiedUpdate(typingModificationOccurred);
            } finally {
                inAtomicUnlock = false;
                typingModificationOccurred = false;
            }
        }

        private void modifiedUpdate(boolean typingModification) {
            if (!inAtomicLock) {
                JTextComponent c = component;
                if (modified && c != null) {
                    fireStateChanged();
                    // Scroll to caret only for component with focus
                    dispatchUpdate(c.hasFocus() && typingModification);
                    modified = false;
                }
            } else {
                typingModificationOccurred |= typingModification;
            }
        }

        // MouseListener methods
        @Override
        public void mousePressed(MouseEvent evt) {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("mousePressed: " + logMouseEvent(evt) + ", state=" + mouseState + '\n'); // NOI18N
            }

            JTextComponent c = component;
            if (c != null && isLeftMouseButtonExt(evt)) {
                // Expand fold if offset is in collapsed fold
                int offset = mouse2Offset(evt);
                switch (evt.getClickCount()) {
                    case 1: // Single press
                        if (c.isEnabled() && !c.hasFocus()) {
                            c.requestFocus();
                        }
                        c.setDragEnabled(true);
                        if (evt.isAltDown() && evt.isShiftDown()) {
                            mouseState = MouseState.CHAR_SELECTION;
                            addCaret(offset);
                        } else if (evt.isShiftDown()) { // Select till offset
                            moveDot(offset);
                            adjustRectangularSelectionMouseX(evt.getX(), evt.getY()); // also fires state change
                            mouseState = MouseState.CHAR_SELECTION;
                        } else // Regular press
                        // check whether selection drag is possible
                        if (isDragPossible(evt) && mapDragOperationFromModifiers(evt) != TransferHandler.NONE) {
                            mouseState = MouseState.DRAG_SELECTION_POSSIBLE;
                        } else { // Drag not possible
                            mouseState = MouseState.CHAR_SELECTION;
                            setDot(offset);
                        }
                        break;

                    case 2: // double-click => word selection
                        mouseState = MouseState.WORD_SELECTION;
                        // Disable drag which would otherwise occur when mouse would be over text
                        c.setDragEnabled(false);
                        // Check possible fold expansion
                        try {
                            // hack, to get knowledge of possible expansion. Editor depends on Folding, so it's not really possible
                            // to have Folding depend on BaseCaret (= a cycle). If BaseCaret moves to editor.lib2, this contract
                            // can be formalized as an interface.
                            @SuppressWarnings("unchecked")
                            Callable<Boolean> cc = (Callable<Boolean>) c.getClientProperty("org.netbeans.api.fold.expander");
                            if (cc == null || !cc.equals(this)) {
                                if (selectWordAction == null) {
                                    selectWordAction = EditorActionUtilities.getAction(
                                            c.getUI().getEditorKit(c), DefaultEditorKit.selectWordAction);
                                }
                                if (selectWordAction != null) {
                                    selectWordAction.actionPerformed(null);
                                }
                                // Select word action selects forward i.e. dot > mark
                                minSelectionStartOffset = getMark();
                                minSelectionEndOffset = getDot();
                            }
                        } catch (Exception ex) {
                            Exceptions.printStackTrace(ex);
                        }
                        break;

                    case 3: // triple-click => line selection
                        mouseState = MouseState.LINE_SELECTION;
                        // Disable drag which would otherwise occur when mouse would be over text
                        c.setDragEnabled(false);
                        if (selectLineAction == null) {
                            selectLineAction = EditorActionUtilities.getAction(
                                            c.getUI().getEditorKit(c), DefaultEditorKit.selectLineAction);
                        }
                        if (selectLineAction != null) {
                            selectLineAction.actionPerformed(null);
                            // Select word action selects forward i.e. dot > mark
                            minSelectionStartOffset = getMark();
                            minSelectionEndOffset = getDot();
                        }
                        break;

                    default: // multi-click
                }
            }
        }

        @Override
        public void mouseReleased(MouseEvent evt) {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("mouseReleased: " + logMouseEvent(evt) + ", state=" + mouseState + '\n'); // NOI18N
            }

            int offset = mouse2Offset(evt);
            switch (mouseState) {
                case DRAG_SELECTION_POSSIBLE:
                    setDot(offset);
                    adjustRectangularSelectionMouseX(evt.getX(), evt.getY()); // also fires state change
                    break;

                case CHAR_SELECTION:
                    if (evt.isAltDown() && evt.isShiftDown()) {
                        moveDotCaret(offset, getLastCaret());
                    } else {
                        moveDot(offset); // Will do setDot() if no selection
                        adjustRectangularSelectionMouseX(evt.getX(), evt.getY()); // also fires state change
                    }
                    break;
            }
            // Set DEFAULT state; after next mouse press the state may change
            // to another state according to particular click count
            mouseState = MouseState.DEFAULT;
            component.setDragEnabled(true);
        }

        /**
         * Translates mouse event to text offset
         */
        int mouse2Offset(MouseEvent evt) {
            JTextComponent c = component;
            int offset = 0;
            if (c != null) {
                int y = evt.getY();
                if (y < 0) {
                    offset = 0;
                } else if (y > c.getSize().getHeight()) {
                    offset = c.getDocument().getLength();
                } else {
                    offset = c.viewToModel(new Point(evt.getX(), evt.getY()));
                }
            }
            return offset;
        }

        @Override
        public void mouseClicked(MouseEvent evt) {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("mouseClicked: " + logMouseEvent(evt) + ", state=" + mouseState + '\n'); // NOI18N
            }

            JTextComponent c = component;
            if (c != null) {
                if (isMiddleMouseButtonExt(evt)) {
                    if (evt.getClickCount() == 1) {
                        if (c == null) {
                            return;
                        }
                        Clipboard buffer = component.getToolkit().getSystemSelection();

                        if (buffer == null) {
                            return;
                        }

                        Transferable trans = buffer.getContents(null);
                        if (trans == null) {
                            return;
                        }

                        final Document doc = c.getDocument();
                        if (doc == null) {
                            return;
                        }

                        final int offset = c.getUI().viewToModel(c, new Point(evt.getX(), evt.getY()));

                        try {
                            final String pastingString = (String) trans.getTransferData(DataFlavor.stringFlavor);
                            if (pastingString == null) {
                                return;
                            }
                            Runnable pasteRunnable = new Runnable() {
                                public @Override
                                void run() {
                                    try {
                                        doc.insertString(offset, pastingString, null);
                                        setDot(offset + pastingString.length());
                                    } catch (BadLocationException exc) {
                                    }
                                }
                            };
                            AtomicLockDocument ald = LineDocumentUtils.as(doc, AtomicLockDocument.class);
                            if (ald != null) {
                                ald.runAtomic(pasteRunnable);
                            } else {
                                pasteRunnable.run();
                            }
                        } catch (UnsupportedFlavorException ufe) {
                        } catch (IOException ioe) {
                        }
                    }
                }
            }
        }

        @Override
        public void mouseEntered(MouseEvent evt) {
        }

        @Override
        public void mouseExited(MouseEvent evt) {
        }

        // MouseMotionListener methods
        @Override
        public void mouseMoved(MouseEvent evt) {
            if (mouseState == MouseState.DEFAULT) {
                boolean textCursor = true;
                int position = component.viewToModel(evt.getPoint());
                if (RectangularSelectionUtils.isRectangularSelection(component)) {
                    List<Position> positions = RectangularSelectionUtils.regionsCopy(component);
                    for (int i = 0; textCursor && i < positions.size(); i += 2) {
                        int a = positions.get(i).getOffset();
                        int b = positions.get(i + 1).getOffset();
                        if (a == b) {
                            continue;
                        }

                        textCursor &= !(position >= a && position <= b || position >= b && position <= a);
                    }
                } else // stream selection
                if (getDot() == getMark()) {
                    // empty selection
                    textCursor = true;
                } else {
                    int dot = getDot();
                    int mark = getMark();
                    if (position >= dot && position <= mark || position >= mark && position <= dot) {
                        textCursor = false;
                    } else {
                        textCursor = true;
                    }
                }

                if (textCursor != showingTextCursor) {
                    int cursorType = textCursor ? Cursor.TEXT_CURSOR : Cursor.DEFAULT_CURSOR;
                    component.setCursor(Cursor.getPredefinedCursor(cursorType));
                    showingTextCursor = textCursor;
                }
            }
        }

        @Override
        public void mouseDragged(MouseEvent evt) {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("mouseDragged: " + logMouseEvent(evt) + ", state=" + mouseState + '\n'); //NOI18N
            }

            if (isLeftMouseButtonExt(evt)) {
                JTextComponent c = component;
                int offset = mouse2Offset(evt);
                int dot = getDot();
                int mark = getMark();
                LineDocument lineDoc = LineDocumentUtils.asRequired(c.getDocument(), LineDocument.class);
                
                try {
                    switch (mouseState) {
                        case DEFAULT:
                        case DRAG_SELECTION:
                            break;

                        case DRAG_SELECTION_POSSIBLE:
                            mouseState = MouseState.DRAG_SELECTION;
                            break;

                        case CHAR_SELECTION:
                            if (evt.isAltDown() && evt.isShiftDown()) {
                                moveDotCaret(offset, getLastCaret());
                            } else {
                                moveDot(offset);
                                adjustRectangularSelectionMouseX(evt.getX(), evt.getY());
                            }
                            break; // Use the offset under mouse pointer

                        case WORD_SELECTION:
                            // Increase selection if at least in the middle of a word.
                            // It depends whether selection direction is from lower offsets upward or back.
                            if (offset >= mark) { // Selection extends forward.
                                offset = LineDocumentUtils.getWordEnd(lineDoc, offset);
                            } else { // Selection extends backward.
                                offset = LineDocumentUtils.getWordStart(lineDoc, offset);
                            }
                            selectEnsureMinSelection(mark, dot, offset);
                            break;

                        case LINE_SELECTION:
                            if (offset >= mark) { // Selection extends forward
                                offset = Math.min(LineDocumentUtils.getLineEnd(lineDoc, offset) + 1, c.getDocument().getLength());
                            } else { // Selection extends backward
                                offset = LineDocumentUtils.getLineStart(lineDoc, offset);
                            }
                            selectEnsureMinSelection(mark, dot, offset);
                            break;

                        default:
                            throw new AssertionError("Invalid state " + mouseState); // NOI18N
                    }
                } catch (BadLocationException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
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
            // Called when horizontal scrollbar gets blinkVisible
            // (but the same listener added to component as well so must check first)
            // Check whether present caret position will not get hidden
            // under horizontal scrollbar and if so scroll the view
            Component hScrollBar = e.getComponent();
            if (hScrollBar != component) { // really called for horizontal scrollbar
                Component scrollPane = hScrollBar.getParent();
                boolean needsUpdate = false;
                for (CaretInfo caret : carets) { // TODO This is wrong, but a quick prototype
                    if (caret.getCaretBounds() != null && scrollPane instanceof JScrollPane) {
                        Rectangle viewRect = ((JScrollPane)scrollPane).getViewport().getViewRect();
                        Rectangle hScrollBarRect = new Rectangle(
                                viewRect.x,
                                viewRect.y + viewRect.height,
                                hScrollBar.getWidth(),
                                hScrollBar.getHeight()
                                );
                        if (hScrollBarRect.intersects(caret.getCaretBounds())) {
                            // Update caret's position
                            needsUpdate = true;
                        }
                    }
                }
                if(needsUpdate) {
                    dispatchUpdate(true); // should be blinkVisible so scroll the view
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
                CaretInfo caret = getLastCaret();
                if (caret.getCaretBounds() == null) {
                    dispatchUpdate(true);
                    if (caret.getCaretBounds() != null) { // detach the listener - no longer necessary
                        c.removeComponentListener(this);
                    }
                }
            }
        }

        @Override
        public void viewHierarchyChanged(ViewHierarchyEvent evt) {
            scheduleCaretUpdate();
        }
        
    } // End of ListenerImpl class
    
    
    private enum CaretType {
        
        /**
         * Two-pixel line caret (the default).
         */
        THICK_LINE_CARET,
        
        /**
         * Thin one-pixel line caret.
         */
        THIN_LINE_CARET,

        /**
         * Rectangle corresponding to a single character.
         */
        BLOCK_CARET;
        
        static CaretType decode(String typeStr) {
            switch (typeStr) {
                case EditorPreferencesDefaults.THIN_LINE_CARET:
                    return THIN_LINE_CARET;
                case EditorPreferencesDefaults.BLOCK_CARET:
                    return BLOCK_CARET;
                default:
                    return THICK_LINE_CARET;
            }
        };

    }

    private static enum MouseState {

        DEFAULT, // Mouse released; not extending any selection
        CHAR_SELECTION, // Extending character selection after single mouse press 
        WORD_SELECTION, // Extending word selection after double-click when mouse button still pressed
        LINE_SELECTION, // Extending line selection after triple-click when mouse button still pressed
        DRAG_SELECTION_POSSIBLE,  // There was a selected text when mouse press arrived so drag is possible
        DRAG_SELECTION  // Drag is being done (text selection existed at the mouse press)
        
    }
    
    public interface CaretTransaction {
        
        void updateCaret(CaretTransactionContext context);

    }
    
    public final class CaretTransactionContext {
        
        public void setDot(CaretInfo caret, int offset) {
            
        }
        
        public void setDot(CaretInfo caret, Position pos) {
            
        }
        
        public void moveDot(CaretInfo caret, int offset) {
            
        }
        
        public void moveDot(CaretInfo caret, Position pos) {
            
        }

    }
    

}
