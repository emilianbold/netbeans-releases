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
package org.netbeans.modules.visualweb.text;

import java.util.HashMap;
import org.netbeans.modules.visualweb.api.designer.DomProvider.DomPosition;
import org.netbeans.modules.visualweb.designer.WebForm;
import org.netbeans.modules.visualweb.text.actions.SelectLineAction;
import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Logger;

import javax.accessibility.Accessible;
import javax.accessibility.AccessibleContext;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.text.Keymap;
import org.netbeans.modules.visualweb.api.designer.DomProvider.DomRange;

import org.netbeans.modules.visualweb.text.actions.BeginLineAction;
import org.netbeans.modules.visualweb.text.actions.BeginWordAction;
import org.netbeans.modules.visualweb.text.actions.DefaultKeyTypedAction;
import org.netbeans.modules.visualweb.text.actions.DeleteNextCharAction;
import org.netbeans.modules.visualweb.text.actions.DeletePrevCharAction;
import org.netbeans.modules.visualweb.text.actions.EndLineAction;
import org.netbeans.modules.visualweb.text.actions.EndWordAction;
import org.netbeans.modules.visualweb.text.actions.NextVisualPositionAction;
import org.netbeans.modules.visualweb.text.actions.SelectAllAction;


/**
 * Base Rave page editor component. Based heavily on JTextComponent in
 * javax.swing.text. This is the superclass for DesignerPane which
 * handles most of the basic text editing (caret, selection, keyboard
 * handling). Conceivably the DesignerPane specific stuff could be
 * moved up into this class. (Earlier I was actually subclassing the
 * real JTextComponent in DesignerPane which is why I had this
 * separation).
 *
 * @todo Remove various unused methods, refactor
 * @todo Focus traversal keys (see Swing's JEditorPane) for tab/shifttab'ing
 *   in and out of this component, etc.
 * @todo Input method text composition support (see Swing's JTextComponent)
 * @todo Implement Accessible (see Swing's JTextComponent)
 * @todo Complete the getActions call to include tons of actions
 *         (see DefaultEditorKit)
 */
public abstract class DesignerPaneBase extends JComponent implements Scrollable, Accessible {
    /**
     * Name of the Action for moving the caret logically forward one position.
     *
     * @see #getActions
     */
    public static final String forwardAction = "caret-forward";

    /**
     * Name of the Action for moving the caret logically backward one position.
     *
     * @see #getActions
     */
    public static final String backwardAction = "caret-backward";

    /**
     * Name of the Action for moving the caret logically upward one position.
     *
     * @see #getActions
     */
    public static final String upAction = "caret-up";

    /**
     * Name of the Action for moving the caret logically downward one position.
     *
     * @see #getActions
     */
    public static final String downAction = "caret-down";

    /**
     * Name of the Action for extending the selection by moving the caret logically forward one
     * position.
     *
     * @see #getActions
     */
    public static final String selectionForwardAction = "selection-forward";

    /**
     * Name of the Action for extending the selection by moving the caret logically backward one
     * position.
     *
     * @see #getActions
     */
    public static final String selectionBackwardAction = "selection-backward";

    /**
     * Name of the Action for moving the caret logically upward one position, extending the
     * selection.
     *
     * @see #getActions
     */
    public static final String selectionUpAction = "selection-up";

    /**
     * Name of the Action for moving the caret logically downward one position, extending the
     * selection.
     *
     * @see #getActions
     */
    public static final String selectionDownAction = "selection-down";

    /**
     * Name of the action that is executed by default if a <em>key typed event</em> is received
     * and there is no keymap entry.
     *
     * @see #getActions
     */
    public static final String defaultKeyTypedAction = "default-typed";

    /**
     * Name of the action to delete the character of content that precedes the current caret
     * position.
     *
     * @see #getActions
     */
    public static final String deletePrevCharAction = "delete-previous";

    /**
     * Name of the action to delete the character of content that
     * follows the current caret position.
     * @see #getActions
     */
    public static final String deleteNextCharAction = "delete-next";

    /**
     * Name of the Action for selecting a word around the caret.
     *
     * @see #getActions
     */
    public static final String selectWordAction = "select-word";

    /**
     * Name of the <code>Action</code> for moving the caret to the beginning of a word.
     *
     * @see #getActions
     */
    public static final String beginWordAction = "caret-begin-word";

    /**
     * Name of the Action for moving the caret to the end of a word.
     *
     * @see #getActions
     */
    public static final String endWordAction = "caret-end-word";

    /**
     * Name of the <code>Action</code> for moving the caret to the beginning of a word, extending
     * the selection.
     *
     * @see #getActions
     */
    public static final String selectionBeginWordAction = "selection-begin-word";

    /**
     * Name of the Action for moving the caret to the end of a word, extending the selection.
     *
     * @see #getActions
     */
    public static final String selectionEndWordAction = "selection-end-word";

    /**
     * Name of the Action for selecting a line around the caret.
     *
     * @see #getActions
     */
    public static final String selectLineAction = "select-line";

    /**
     * Name of the Action for selecting the entire document
     * @see #getActions
     */
    public static final String selectAllAction = "select-all";

    /**
     * Name of the <code>Action</code> for moving the caret to the beginning of a line, extending
     * the selection.
     *
     * @see #getActions
     */
    public static final String selectionBeginLineAction = "selection-begin-line";

    /**
     * Name of the <code>Action</code> for moving the caret to the end of a line, extending the
     * selection.
     *
     * @see #getActions
     */
    public static final String selectionEndLineAction = "selection-end-line";

    /**
     * Name of the <code>Action</code> for moving the caret to the beginning of a line.
     *
     * @see #getActions
     */
    public static final String beginLineAction = "caret-begin-line";

    /**
     * Name of the <code>Action</code> for moving the caret to the end of a line.
     *
     * @see #getActions
     */
    public static final String endLineAction = "caret-end-line";

    /**
     * The most recent JTextComponent that has/had focus. The text actions use this via the
     * <code>getFocusedComponent</code> method as a fallback case when a JTextComponent doesn't
     * have focus.
     */
    private static DesignerPaneBase focusedComponent;
    private static final Action[] defaultActions =
        {
            new DefaultKeyTypedAction(), new BeginWordAction(beginWordAction, false),
            new EndWordAction(endWordAction, false),
            new BeginWordAction(selectionBeginWordAction, true),
            new EndWordAction(selectionEndWordAction, true),
            new BeginLineAction(beginLineAction, false), new EndLineAction(endLineAction, false),
            new BeginLineAction(selectionBeginLineAction, true),
            new EndLineAction(selectionEndLineAction, true), new DeletePrevCharAction(),
            new DeleteNextCharAction(),
            

            /*
             * , new InsertContentAction(), new ReadOnlyAction(), new
             * WritableAction(), new CutAction(), new CopyAction(), new PasteAction(), new
             * VerticalPageAction(pageUpAction, -1, false), new VerticalPageAction(pageDownAction,
             * 1, false), new VerticalPageAction(selectionPageUpAction, -1, true), new
             * VerticalPageAction(selectionPageDownAction, 1, true), new
             * PageAction(selectionPageLeftAction, true, true), new
             * PageAction(selectionPageRightAction, false, true), new InsertBreakAction(), new
             * BeepAction(),
             */
            new NextVisualPositionAction(forwardAction, false, SwingConstants.EAST),
            new NextVisualPositionAction(backwardAction, false, SwingConstants.WEST),
            new NextVisualPositionAction(upAction, false, SwingConstants.NORTH),
            new NextVisualPositionAction(downAction, false, SwingConstants.SOUTH),
            new NextVisualPositionAction(selectionForwardAction, true, SwingConstants.EAST),
            new NextVisualPositionAction(selectionBackwardAction, true, SwingConstants.WEST),
            new NextVisualPositionAction(selectionUpAction, true, SwingConstants.NORTH),
            new NextVisualPositionAction(selectionDownAction, true, SwingConstants.SOUTH),
            

            /*
             * new PreviousWordAction(previousWordAction, false), new NextWordAction(nextWordAction, false),
             * new PreviousWordAction(selectionPreviousWordAction, true), new
             * NextWordAction(selectionNextWordAction, true), new BeginParagraphAction(beginParagraphAction,
             * false), new EndParagraphAction(endParagraphAction, false), new
             * BeginParagraphAction(selectionBeginParagraphAction, true), new
             * EndParagraphAction(selectionEndParagraphAction, true), new BeginAction(beginAction, false),
             * new EndAction(endAction, false), new BeginAction(selectionBeginAction, true), new
             * EndAction(selectionEndAction, true), new InsertTabAction(), new SelectWordAction(), */
            new SelectLineAction(), /*new SelectParagraphAction(), */
            new SelectAllAction(), /*new UnselectAction(),
            * new ToggleComponentOrientationAction(),
            */
        /*
         * From HTMLEditorKit new DumpModelAction()
         *
         * new InsertHTMLTextAction("InsertTable", INSERT_TABLE_HTML, HTML.Tag.BODY, HTML.Tag.TABLE),
         * new InsertHTMLTextAction("InsertTableRow", INSERT_TABLE_HTML, HTML.Tag.TABLE, HTML.Tag.TR,
         * HTML.Tag.BODY, HTML.Tag.TABLE), new InsertHTMLTextAction("InsertTableDataCell",
         * INSERT_TABLE_HTML, HTML.Tag.TR, HTML.Tag.TD, HTML.Tag.BODY, HTML.Tag.TABLE), new
         * InsertHTMLTextAction("InsertUnorderedList", INSERT_UL_HTML, HTML.Tag.BODY, HTML.Tag.UL), new
         * InsertHTMLTextAction("InsertUnorderedListItem", INSERT_UL_HTML, HTML.Tag.UL, HTML.Tag.LI,
         * HTML.Tag.BODY, HTML.Tag.UL), new InsertHTMLTextAction("InsertOrderedList", INSERT_OL_HTML,
         * HTML.Tag.BODY, HTML.Tag.OL), new InsertHTMLTextAction("InsertOrderedListItem",
         * INSERT_OL_HTML, HTML.Tag.OL, HTML.Tag.LI, HTML.Tag.BODY, HTML.Tag.OL), new InsertHRAction(),
         * new InsertHTMLTextAction("InsertPre", INSERT_PRE_HTML, HTML.Tag.BODY, HTML.Tag.PRE),
         * nextLinkAction, previousLinkAction, activateLinkAction
         */
        };

    private static Map<String, Keymap> keymapTable = null;

    /**
     * The default keymap that will be shared by all <code>JTextComponent</code> instances unless
     * they have had a different keymap set.
     */
    public static final String DEFAULT_KEYMAP = "default";

    static {
        try {
            keymapTable = new Hashtable<String, Keymap>(17);

            Keymap binding = addKeymap(DEFAULT_KEYMAP, null);
            binding.setDefaultAction(new DefaultKeyTypedAction());
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    /**
     * @see #getUIClassID
     * @see #readObject
     */
    private static final String uiClassID = "EditorPaneUI";
//    private Document model = null;
//    private final Document model;

    // --- member variables ----------------------------------

    /**
     * The caret used to display the insert position and navigate throughout the document.
     *
     * PENDING(prinz) This should be serializable, default installed by UI.
     */
    private transient DesignerCaret caret;

    /**
     * The current key bindings in effect.
     *
     * PENDING(prinz) This should be serializable, default installed by UI.
     */
    private transient Keymap keymap;
    private Color caretColor;
    private Color selectionColor;
    private Color selectedTextColor;

    /**
     * Creates a new <code>JTextComponent</code>. Listeners for caret events are established, and
     * the pluggable UI installed. The component is marked as editable. No layout manager is used,
     * because layout is managed by the view subsystem of text. The document model is set to
     * <code>null</code>.
     */
    public DesignerPaneBase(/*Document document*/) {
        super();
//        this.model = document;

        // enable InputMethodEvent for on-the-spot pre-editing
        enableEvents(AWTEvent.KEY_EVENT_MASK | AWTEvent.INPUT_METHOD_EVENT_MASK);
        setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
        setLayout(null); // layout is managed by the box hierarchy

        // TODO: find out if I still need to defer this!
        // Necessary, because updateUI causes a huge cascade - down
        // to View creation etc. Some of the Subviews (FormView in
        // particular) consults the container for its page reference;
        // but our child classes' constructors (such as the one in
        // DesignerPane) has not yet had a chance to complete. So the
        // child will do it instead for now. HACK HACK HACK YUCK YUCK YUCK.
        //updateUI();
    }

    
    // XXX Temp only.
    public abstract WebForm getWebForm();
    
    /**
     * Fetches the user-interface factory for this text-oriented editor.
     *
     * @return the factory
     */
    public DesignerPaneBaseUI getUI() {
        return (DesignerPaneBaseUI)ui;
    }

    /**
     * Sets the user-interface factory for this text-oriented editor.
     *
     * @param ui
     *            the factory
     */
    public void setUI(DesignerPaneBaseUI ui) {
        super.setUI(ui);
    }

    /**
     * Reloads the pluggable UI. The key used to fetch the new interface is
     * <code>getUIClassID()</code>. The type of the UI is <code>DesignerPaneBaseUI</code>.
     * <code>invalidate</code> is called after setting the UI.
     */
    public void updateUI() {
        info("Updating UI, ui=" + ui); // TEMP
        setUI((DesignerPaneBaseUI)UIManager.getUI(this));
        info("after update, ui=" + ui); // TEMP
        invalidate();
    }

    /**
     * Returns the JTextComponent that most recently had focus. The returned value may currently
     * have focus.
     */
    public static final DesignerPaneBase getFocusedComponent() {
        return focusedComponent;
    }

//    /**
//     * Fetches the model associated with the editor. This is primarily for the UI to get at the
//     * minimal amount of state required to be a text editor. Subclasses will return the actual type
//     * of the model which will typically be something that extends Document.
//     *
//     * @return the model
//     */
//    public Document getDocument() {
//        return model;
//    }

    /**
     * Fetches the command list for the editor. This is the list of commands supported by the
     * plugged-in UI augmented by the collection of commands that the editor itself supports. These
     * are useful for binding to events, such as in a keymap.
     *
     * @return the command list
     */
    public Action[] getActions() {
        return defaultActions.clone();
    }

    /**
     * Fetches the caret that allows text-oriented navigation over the view.
     *
     * @return the caret
     */
    private /*public*/ DesignerCaret getCaret() {
        return caret;
    }

    /**
     * Sets the caret to be used.  By default this will be set
     * by the UI that gets installed.  This can be changed to
     * a custom caret if desired.  Setting the caret results in a
     * PropertyChange event ("caret") being fired.
     *
     * @param c the caret
     * @see #getCaret
     * @beaninfo
     *  description: the caret used to select/navigate
     *        bound: true
     *       expert: true
     */
    public void setCaret(DesignerCaret c) {
        if (caret != null) {
            caret.deinstall(this);
        }

        DesignerCaret old = caret;
        caret = c;

        if (caret != null) {
            caret.install(this);
        }

        firePropertyChange("caret", old, caret);
    }

    /**
     * Sets the keymap to use for binding events to
     * actions.  Setting to <code>null</code> effectively disables
     * keyboard input.
     * A PropertyChange event ("keymap") is fired when a new keymap
     * is installed.
     *
     * @param map the keymap
     * @see #getKeymap
     * @beaninfo
     *  description: set of key event to action bindings to use
     *        bound: true
     */
    public void setKeymap(Keymap map) {
        Keymap old = keymap;
        keymap = map;
        firePropertyChange("keymap", old, keymap);
        updateInputMap(old, map);
    }

    /**
     * Updates the <code>InputMap</code> s in response to a <code>Keymap</code> change.
     *
     * @param oldKm
     *            the old <code>Keymap</code>
     * @param newKm
     *            the new <code>Keymap</code>
     */
    void updateInputMap(Keymap oldKm, Keymap newKm) {
        // Locate the current KeymapWrapper.
        InputMap km = getInputMap(JComponent.WHEN_FOCUSED);
        InputMap last = km;

        while ((km != null) && !(km instanceof KeymapWrapper)) {
            last = km;
            km = km.getParent();
        }

        if (km != null) {
            // Found it, tweak the InputMap that points to it, as well
            // as anything it points to.
            if (newKm == null) {
                if (last != km) {
                    last.setParent(km.getParent());
                } else {
                    last.setParent(null);
                }
            } else {
                InputMap newKM = new KeymapWrapper(newKm);
                last.setParent(newKM);

                if (last != km) {
                    newKM.setParent(km.getParent());
                }
            }
        } else if (newKm != null) {
            km = getInputMap(JComponent.WHEN_FOCUSED);

            if (km != null) {
                // Couldn't find it.
                // Set the parent of WHEN_FOCUSED InputMap to be the new one.
                InputMap newKM = new KeymapWrapper(newKm);
                newKM.setParent(km.getParent());
                km.setParent(newKM);
            }
        }

        // Do the same thing with the ActionMap
        ActionMap am = getActionMap();
        ActionMap lastAM = am;

        while ((am != null) && !(am instanceof KeymapActionMap)) {
            lastAM = am;
            am = am.getParent();
        }

        if (am != null) {
            // Found it, tweak the Actionap that points to it, as well
            // as anything it points to.
            if (newKm == null) {
                if (lastAM != am) {
                    lastAM.setParent(am.getParent());
                } else {
                    lastAM.setParent(null);
                }
            } else {
                ActionMap newAM = new KeymapActionMap(newKm);
                lastAM.setParent(newAM);

                if (lastAM != am) {
                    newAM.setParent(am.getParent());
                }
            }
        } else if (newKm != null) {
            am = getActionMap();

            if (am != null) {
                // Couldn't find it.
                // Set the parent of ActionMap to be the new one.
                ActionMap newAM = new KeymapActionMap(newKm);
                newAM.setParent(am.getParent());
                am.setParent(newAM);
            }
        }
    }

    /**
     * Fetches the keymap currently active in this text component.
     *
     * @return the keymap
     */
    public Keymap getKeymap() {
        return keymap;
    }

    /**
     * Adds a new keymap into the keymap hierarchy. Keymap bindings resolve from bottom up so an
     * attribute specified in a child will override an attribute specified in the parent.
     *
     * @param nm
     *            the name of the keymap (must be unique within the collection of named keymaps in
     *            the document); the name may be <code>null</code> if the keymap is unnamed, but
     *            the caller is responsible for managing the reference returned as an unnamed keymap
     *            can't be fetched by name
     * @param parent
     *            the parent keymap; this may be <code>null</code> if unspecified bindings need
     *            not be resolved in some other keymap
     * @return the keymap
     */
    public static Keymap addKeymap(String nm, Keymap parent) {
        Keymap map = new DefaultKeymap(nm, parent);

        if (nm != null) {
            // add a named keymap, a class of bindings
            keymapTable.put(nm, map);
        }

        return map;
    }

    /**
     * Removes a named keymap previously added to the document. Keymaps with <code>null</code>
     * names may not be removed in this way.
     *
     * @param nm
     *            the name of the keymap to remove
     * @return the keymap that was removed
     */
    public static Keymap removeKeymap(String nm) {
        return keymapTable.remove(nm);
    }

    /**
     * Fetches a named keymap previously added to the document. This does not work with
     * <code>null</code> -named keymaps.
     *
     * @param nm
     *            the name of the keymap
     * @return the keymap
     */
    public static Keymap getKeymap(String nm) {
        return keymapTable.get(nm);
    }

    /**
     * <p>
     * Loads a keymap with a bunch of bindings. This can be used to take a static table of
     * definitions and load them into some keymap. The following example illustrates an example of
     * binding some keys to the cut, copy, and paste actions associated with a JTextComponent. A
     * code fragment to accomplish this might look as follows:
     *
     * <pre><code>
     * static final JTextComponent.KeyBinding[] defaultBindings = {
     *         new JTextComponent.KeyBinding(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_MASK),
     *                 DefaultEditorKit.copyAction),
     *         new JTextComponent.KeyBinding(KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.CTRL_MASK),
     *                 DefaultEditorKit.pasteAction),
     *         new JTextComponent.KeyBinding(KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.CTRL_MASK),
     *                 DefaultEditorKit.cutAction),};
     * JTextComponent c = new JTextPane();
     * Keymap k = c.getKeymap();
     * JTextComponent.loadKeymap(k, defaultBindings, c.getActions());
     * </code></pre>
     *
     * The sets of bindings and actions may be empty but must be non- <code>null</code>.
     *
     * @param map
     *            the keymap
     * @param bindings
     *            the bindings
     * @param actions
     *            the set of actions
     */
    public static void loadKeymap(Keymap map, KeyBinding[] bindings, Action[] actions) {
        Map<String, Action> h = new HashMap<String, Action>();

        for (int i = 0; i < actions.length; i++) {
            Action a = actions[i];
            String value = (String)a.getValue(Action.NAME);
            h.put(((value != null) ? value : ""), a);
        }

        for (int i = 0; i < bindings.length; i++) {
            Action a = h.get(bindings[i].actionName);

            if (a != null) {
                map.addActionForKeyStroke(bindings[i].key, a);
            }
        }
    }

    /**
     * Fetches the current color used to render the caret.
     *
     * @return the color
     */
    public Color getCaretColor() {
        return caretColor;
    }

    /**
     * Sets the current color used to render the caret.
     * Setting to <code>null</code> effectively restores the default color.
     * Setting the color results in a PropertyChange event ("caretColor")
     * being fired.
     *
     * @param c the color
     * @see #getCaretColor
     * @beaninfo
     *  description: the color used to render the caret
     *        bound: true
     *    preferred: true
     */
    public void setCaretColor(Color c) {
        Color old = caretColor;
        caretColor = c;
        firePropertyChange("caretColor", old, caretColor);
    }

    /**
     * Fetches the current color used to render the selection.
     *
     * @return the color
     */
    public Color getSelectionColor() {
        return selectionColor;
    }

    /**
     * Sets the current color used to render the selection.
     * Setting the color to <code>null</code> is the same as setting
     * <code>Color.white</code>.  Setting the color results in a
     * PropertyChange event ("selectionColor").
     *
     * @param c the color
     * @see #getSelectionColor
     * @beaninfo
     *  description: color used to render selection background
     *        bound: true
     *    preferred: true
     */
    public void setSelectionColor(Color c) {
        Color old = selectionColor;
        selectionColor = c;
        firePropertyChange("selectionColor", old, selectionColor);
    }

    /**
     * Fetches the current color used to render the selected text.
     *
     * @return the color
     */
    public Color getSelectedTextColor() {
        return selectedTextColor;
    }

    /**
     * Sets the current color used to render the selected text.
     * Setting the color to <code>null</code> is the same as
     * <code>Color.black</code>. Setting the color results in a
     * PropertyChange event ("selectedTextColor") being fired.
     *
     * @param c the color
     * @see #getSelectedTextColor
     * @beaninfo
     *  description: color used to render selected text
     *        bound: true
     *    preferred: true
     */
    public void setSelectedTextColor(Color c) {
        Color old = selectedTextColor;
        selectedTextColor = c;
        firePropertyChange("selectedTextColor", old, selectedTextColor);
    }

    /**
     * Fetches a portion of the text represented by the component. Returns an empty string if length
     * is 0.
     *
     * @param offs
     *            the offset >= 0
     * @param len
     *            the length >= 0
     * @return the text
     * @exception BadLocationException
     *                if the offset or length are invalid
     */
//    public String getText(Position offs, int len) {
    public String getText(DomPosition offs, int len) {
        throw new RuntimeException("Not yet implemented!"); // XXX

        //return "";
    }

//    /**
//     * Converts the given location in the model to a place in the view coordinate system. The
//     * component must have a positive size for this translation to be computed (i.e. layout cannot
//     * be computed until the component has been sized). The component does not have to be visible or
//     * painted.
//     *
//     * @param pos
//     *            the position >= 0
//     * @return the coordinates as a rectangle, with (r.x, r.y) as the location in the coordinate
//     *         system, or null if the component does not yet have a positive size.
//     * @exception BadLocationException
//     *                if the given position does not represent a valid location in the associated
//     *                document
//     * @see DesignerPaneBaseUI#modelToView
//     */
//    public Rectangle modelToView(Position pos) {
//        return getUI().modelToView(/*this,*/ pos);
//    }

//    /**
//     * Converts the given place in the view coordinate system to the nearest representative location
//     * in the model. The component must have a positive size for this translation to be computed
//     * (i.e. layout cannot be computed until the component has been sized). The component does not
//     * have to be visible or painted.
//     *
//     * @param pt
//     *            the location in the view to translate
//     * @return the offset >= 0 from the start of the document, or -1 if the component does not yet
//     *         have a positive size.
//     * @see DesignerPaneBaseUI#viewToModel
//     */
//    public Position viewToModel(Point pt) {
//        return getUI().viewToModel(this, pt);
//    }

//    /**
//     * Moves the caret to a new position, leaving behind a mark defined by the last time
//     * <code>setCaretPosition</code> was called. This forms a selection. If the document is
//     * <code>null</code>, does nothing. The position must be between 0 and the length of the
//     * component's text or else an exception is thrown.
//     *
//     * @param pos
//     *            the position
//     * @exception IllegalArgumentException
//     *                if the value supplied for <code>position</code> is less than zero or greater
//     *                than the component's text length
//     * @see #setCaretPosition
//     */
////    public void moveCaretPosition(Position pos) {
//    public void moveCaretPosition(DomPosition pos) {
//        if (caret != null) {
//            caret.moveDot(pos);
//        }
//    }
//
//    /**
//     * Sets the position of the text insertion caret for the
//     * <code>TextComponent</code>.  Note that the caret tracks change,
//     * so this may move if the underlying text of the component is changed.
//     * If the document is <code>null</code>, does nothing. The position
//     * must be between 0 and the length of the component's text or else
//     * an exception is thrown.
//     *
//     * @param position the position
//     * @exception    IllegalArgumentException if the value supplied
//     *               for <code>position</code> is less than zero or greater
//     *               than the component's text length
//     * @beaninfo
//     * description: the caret position
//     */
////    public void setCaretPosition(Position position) {
//    public void setCaretPosition(DomPosition position) {
//        if (caret != null) {
//            caret.setDot(position);
//        }
//    }

    /**
     * Returns the position of the text insertion caret for the text component.
     *
     * @return the position of the text insertion caret for the text component >= 0
     */
//    public Position getCaretPosition() {
    public DomPosition getCaretPosition() {
        if (caret == null) {
//            return Position.NONE;
            return DomPosition.NONE;
        }

        return caret.getDot();
    }

    /**
     * Selects the text between the specified start and end positions.
     * <p>
     * This method sets the start and end positions of the selected text, enforcing the restriction
     * that the start position must be greater than or equal to zero. The end position must be
     * greater than or equal to the start position, and less than or equal to the length of the text
     * component's text.
     * <p>
     * If the caller supplies values that are inconsistent or out of bounds, the method enforces
     * these constraints silently, and without failure. Specifically, if the start position or end
     * position is greater than the length of the text, it is reset to equal the text length. If the
     * start position is less than zero, it is reset to zero, and if the end position is less than
     * the start position, it is reset to the start position.
     * <p>
     * This call is provided for backward compatibility. It is routed to a call to
     * <code>setCaretPosition</code> followed by a call to <code>moveCaretPosition</code>. The
     * preferred way to manage selection is by calling those methods directly.
     *
     * @param selectionStart
     *            the start position of the text
     * @param selectionEnd
     *            the end position of the text
     * @see #setCaretPosition
     * @see #moveCaretPosition
     */
//    public void select(Position selectionStart, Position selectionEnd) {
    public void select(DomPosition selectionStart, DomPosition selectionEnd) {
        // argument adjustment done by java.awt.TextComponent
//        setCaretPosition(selectionStart);
//        moveCaretPosition(selectionEnd);
        setCaretDot(selectionStart);
        moveCaretDot(selectionEnd);
    }

    // --- Scrollable methods ---------------------------------------------

    /**
     * Returns the preferred size of the viewport for a view component. This is implemented to do
     * the default behavior of returning the preferred size of the component.
     *
     * @return the <code>preferredSize</code> of a <code>JViewport</code> whose view is this
     *         <code>Scrollable</code>
     */
    public Dimension getPreferredScrollableViewportSize() {
        return getPreferredSize();
    }

    /**
     * Components that display logical rows or columns should compute the scroll increment that will
     * completely expose one new row or column, depending on the value of orientation. Ideally,
     * components should handle a partially exposed row or column by returning the distance required
     * to completely expose the item.
     * <p>
     * The default implementation of this is to simply return 10% of the visible area. Subclasses
     * are likely to be able to provide a much more reasonable value.
     *
     * @param visibleRect
     *            the view area visible within the viewport
     * @param orientation
     *            either <code>SwingConstants.VERTICAL</code> or
     *            <code>SwingConstants.HORIZONTAL</code>
     * @param direction
     *            less than zero to scroll up/left, greater than zero for down/right
     * @return the "unit" increment for scrolling in the specified direction
     * @exception IllegalArgumentException
     *                for an invalid orientation
     * @see JScrollBar#setUnitIncrement
     */
    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
        switch (orientation) {
        case SwingConstants.VERTICAL:
            return visibleRect.height / 10;

        case SwingConstants.HORIZONTAL:
            return visibleRect.width / 10;

        default:
            throw new IllegalArgumentException("Invalid orientation: " + orientation);
        }
    }

    /**
     * Components that display logical rows or columns should compute the scroll increment that will
     * completely expose one block of rows or columns, depending on the value of orientation.
     * <p>
     * The default implementation of this is to simply return the visible area. Subclasses will
     * likely be able to provide a much more reasonable value.
     *
     * @param visibleRect
     *            the view area visible within the viewport
     * @param orientation
     *            either <code>SwingConstants.VERTICAL</code> or
     *            <code>SwingConstants.HORIZONTAL</code>
     * @param direction
     *            less than zero to scroll up/left, greater than zero for down/right
     * @return the "block" increment for scrolling in the specified direction
     * @exception IllegalArgumentException
     *                for an invalid orientation
     * @see JScrollBar#setBlockIncrement
     */
    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
        switch (orientation) {
        case SwingConstants.VERTICAL:
            return visibleRect.height;

        case SwingConstants.HORIZONTAL:
            return visibleRect.width;

        default:
            throw new IllegalArgumentException("Invalid orientation: " + orientation);
        }
    }

    /////////////////
    // Accessibility support
    ////////////////

    /**
     * Gets the <code>AccessibleContext</code> associated with this <code>DesignerPaneBase</code>.
     * @return an <code>AccessibleDesignerPaneBase</code> that serves as the
     *         <code>AccessibleContext</code> of this <code>DesignerPaneBase</code>
     */
    public AccessibleContext getAccessibleContext() {
        if (accessibleContext == null) {
             accessibleContext = new AccessibleDesignerPaneBase();
        }

        return accessibleContext;
    }

    /**
     * Gets the class ID for the UI.
     *
     * @return the string "EditorPaneUI"
     * @see JComponent#getUIClassID
     * @see UIDefaults#getUI
     */
    public String getUIClassID() {
        return uiClassID;
    }

    // --- java.awt.Component methods --------------------------

    /**
     * Returns the preferred size for the <code>JEditorPane</code>. The preferred size for
     * <code>JEditorPane</code> is slightly altered from the preferred size of the superclass. If
     * the size of the viewport has become smaller than the minimum size of the component, the
     * scrollable definition for tracking width or height will turn to false. The default viewport
     * layout will give the preferred size, and that is not desired in the case where the scrollable
     * is tracking. In that case the <em>normal</em> preferred size is adjusted to the minimum
     * size. This allows things like HTML tables to shrink down to their minimum size and then be
     * laid out at their minimum size, refusing to shrink any further.
     *
     * @return a <code>Dimension</code> containing the preferred size
     */
    public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();

        if (getParent() instanceof JViewport) {
            JViewport port = (JViewport)getParent();
            DesignerPaneBaseUI ui = getUI();
            int prefWidth = d.width;
            int prefHeight = d.height;

            if (!getScrollableTracksViewportWidth()) {
                int w = port.getWidth();
                Dimension min = ui.getMinimumSize(this);

                if ((w != 0) && (w < min.width)) {
                    // Only adjust to min if we have a valid size
                    prefWidth = min.width;
                }
            }

            if (!getScrollableTracksViewportHeight()) {
                int h = port.getHeight();
                Dimension min = ui.getMinimumSize(this);

                if ((h != 0) && (h < min.height)) {
                    // Only adjust to min if we have a valid size
                    prefHeight = min.height;
                }
            }

            if ((prefWidth != d.width) || (prefHeight != d.height)) {
                d = new Dimension(prefWidth, prefHeight);
            }
        }

        return d;
    }

    // --- Scrollable ----------------------------------------

    /**
     * Returns true if a viewport should always force the width of this <code>Scrollable</code> to
     * match the width of the viewport.
     *
     * @return true if a viewport should force the Scrollables width to match its own, false
     *         otherwise
     */
    public boolean getScrollableTracksViewportWidth() {
        if (getParent() instanceof JViewport) {
            JViewport port = (JViewport)getParent();
            DesignerPaneBaseUI ui = getUI();
            int w = port.getWidth();
            Dimension min = ui.getMinimumSize(this);
            Dimension max = ui.getMaximumSize(this);

            if ((w >= min.width) && (w <= max.width)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Returns true if a viewport should always force the height of this <code>Scrollable</code>
     * to match the height of the viewport.
     *
     * @return true if a viewport should force the <code>Scrollable</code>'s height to match its
     *         own, false otherwise
     */
    public boolean getScrollableTracksViewportHeight() {
        if (getParent() instanceof JViewport) {
            JViewport port = (JViewport)getParent();
            DesignerPaneBaseUI ui = getUI();
            int h = port.getHeight();
            Dimension min = ui.getMinimumSize(this);

            if (h >= min.height) {
                Dimension max = ui.getMaximumSize(this);

                if (h <= max.height) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Binding record for creating key bindings.
     * <p>
     * <strong>Warning: </strong> Serialized objects of this class will not be compatible with
     * future Swing releases. The current serialization support is appropriate for short term
     * storage or RMI between applications running the same version of Swing. As of 1.4, support for
     * long term storage of all JavaBeans <sup><font size="-2">TM </font> </sup> has been added to
     * the <code>java.beans</code> package. Please see {@link java.beans.XMLEncoder}.
     */
    public static class KeyBinding {
        /**
         * The key.
         */
        public KeyStroke key;

        /**
         * The name of the action for the key.
         */
        public String actionName;

        /**
         * Creates a new key binding.
         *
         * @param key
         *            the key
         * @param actionName
         *            the name of the action for the key
         */
        public KeyBinding(KeyStroke key, String actionName) {
            this.key = key;
            this.actionName = actionName;
        }
    }

    static class DefaultKeymap implements Keymap {
        String nm;
        javax.swing.text.Keymap parent;
        Hashtable<KeyStroke, Action>  bindings;
        Action defaultAction;

        DefaultKeymap(String nm, Keymap parent) {
            this.nm = nm;
            this.parent = parent;
            bindings = new Hashtable<KeyStroke, Action>();
        }

        /**
         * Fetch the default action to fire if a key is typed (ie a KEY_TYPED KeyEvent is received)
         * and there is no binding for it. Typically this would be some action that inserts text so
         * that the keymap doesn't require an action for each possible key.
         */
        public Action getDefaultAction() {
            if (defaultAction != null) {
                return defaultAction;
            }

            return (parent != null) ? parent.getDefaultAction() : null;
        }

        /**
         * Set the default action to fire if a key is typed.
         */
        public void setDefaultAction(Action a) {
            defaultAction = a;
        }

        public String getName() {
            return nm;
        }

        public Action getAction(KeyStroke key) {
            Action a = bindings.get(key);

            if ((a == null) && (parent != null)) {
                a = parent.getAction(key);
            }

            return a;
        }

        public KeyStroke[] getBoundKeyStrokes() {
            KeyStroke[] keys = new KeyStroke[bindings.size()];
            int i = 0;

            for (Enumeration<KeyStroke> e = bindings.keys(); e.hasMoreElements();) {
                keys[i++] = e.nextElement();
            }

            return keys;
        }

        public Action[] getBoundActions() {
            Action[] actions = new Action[bindings.size()];
            int i = 0;

            for (Enumeration<Action> e = bindings.elements(); e.hasMoreElements();) {
                actions[i++] = e.nextElement();
            }

            return actions;
        }

        public KeyStroke[] getKeyStrokesForAction(Action a) {
            if (a == null) {
                return null;
            }

            KeyStroke[] retValue = null;

            // Determine local bindings first.
            Vector<KeyStroke> keyStrokes = null;

            for (Enumeration<KeyStroke> enum_ = bindings.keys(); enum_.hasMoreElements();) {
                KeyStroke key = enum_.nextElement();

                if (bindings.get(key) == a) {
                    if (keyStrokes == null) {
                        keyStrokes = new Vector<KeyStroke>();
                    }

                    keyStrokes.addElement(key);
                }
            }

            // See if the parent has any.
            if (parent != null) {
                KeyStroke[] pStrokes = parent.getKeyStrokesForAction(a);

                if (pStrokes != null) {
                    // Remove any bindings defined in the parent that
                    // are locally defined.
                    int rCount = 0;

                    for (int counter = pStrokes.length - 1; counter >= 0; counter--) {
                        if (isLocallyDefined(pStrokes[counter])) {
                            pStrokes[counter] = null;
                            rCount++;
                        }
                    }

                    if ((rCount > 0) && (rCount < pStrokes.length)) {
                        if (keyStrokes == null) {
                            keyStrokes = new Vector<KeyStroke>();
                        }

                        for (int counter = pStrokes.length - 1; counter >= 0; counter--) {
                            if (pStrokes[counter] != null) {
                                keyStrokes.addElement(pStrokes[counter]);
                            }
                        }
                    } else if (rCount == 0) {
                        if (keyStrokes == null) {
                            retValue = pStrokes;
                        } else {
                            retValue = new KeyStroke[keyStrokes.size() + pStrokes.length];
                            keyStrokes.copyInto(retValue);
                            System.arraycopy(pStrokes, 0, retValue, keyStrokes.size(),
                                pStrokes.length);
                            keyStrokes = null;
                        }
                    }
                }
            }

            if (keyStrokes != null) {
                retValue = new KeyStroke[keyStrokes.size()];
                keyStrokes.copyInto(retValue);
            }

            return retValue;
        }

        public boolean isLocallyDefined(KeyStroke key) {
            return bindings.containsKey(key);
        }

        public void addActionForKeyStroke(KeyStroke key, Action a) {
            bindings.put(key, a);
        }

        public void removeKeyStrokeBinding(KeyStroke key) {
            bindings.remove(key);
        }

        public void removeBindings() {
            bindings.clear();
        }

        public javax.swing.text.Keymap getResolveParent() {
            return parent;
        }

        public void setResolveParent(javax.swing.text.Keymap parent) {
            this.parent = parent;
        }

        /**
         * String representation of the keymap... potentially a very long string.
         */
        @Override
        public String toString() {
            return super.toString() + "[name=" + nm + ", bindings=" + bindings + "]"; // NOI18N
        }
    }

    /**
     * KeymapWrapper wraps a Keymap inside an InputMap. For KeymapWrapper to be useful it must be
     * used with a KeymapActionMap. KeymapWrapper for the most part, is an InputMap with two
     * parents. The first parent visited is ALWAYS the Keymap, with the second parent being the
     * parent inherited from InputMap. If <code>keymap.getAction</code> returns null, implying the
     * Keymap does not have a binding for the KeyStroke, the parent is then visited. If the Keymap
     * has a binding, the Action is returned, if not and the KeyStroke represents a KeyTyped event
     * and the Keymap has a defaultAction, <code>DefaultActionKey</code> is returned.
     * <p>
     * KeymapActionMap is then able to transate the object passed in to either message the Keymap,
     * or message its default implementation.
     */
    static class KeymapWrapper extends InputMap {
        static final Object DefaultActionKey = new Object();
        private Keymap keymap;

        KeymapWrapper(Keymap keymap) {
            this.keymap = keymap;
        }

        public KeyStroke[] keys() {
            KeyStroke[] sKeys = super.keys();
            KeyStroke[] keymapKeys = keymap.getBoundKeyStrokes();
            int sCount = (sKeys == null) ? 0 : sKeys.length;
            int keymapCount = (keymapKeys == null) ? 0 : keymapKeys.length;

            if (sCount == 0) {
                return keymapKeys;
            }

            if (keymapCount == 0) {
                return sKeys;
            }

            KeyStroke[] retValue = new KeyStroke[sCount + keymapCount];

            // There may be some duplication here...
            System.arraycopy(sKeys, 0, retValue, 0, sCount);
            System.arraycopy(keymapKeys, 0, retValue, sCount, keymapCount);

            return retValue;
        }

        public int size() {
            // There may be some duplication here...
            KeyStroke[] keymapStrokes = keymap.getBoundKeyStrokes();
            int keymapCount = (keymapStrokes == null) ? 0 : keymapStrokes.length;

            return super.size() + keymapCount;
        }

        public Object get(KeyStroke keyStroke) {
            Object retValue = keymap.getAction(keyStroke);

            if (retValue == null) {
                retValue = super.get(keyStroke);

                if ((retValue == null) && (keyStroke.getKeyChar() != KeyEvent.CHAR_UNDEFINED) &&
                        (keymap.getDefaultAction() != null)) {
                    // Implies this is a KeyTyped event, use the default
                    // action.
                    retValue = DefaultActionKey;
                }
            }

            return retValue;
        }
    }

    /**
     * Wraps a Keymap inside an ActionMap. This is used with a KeymapWrapper. If <code>get</code>
     * is passed in <code>KeymapWrapper.DefaultActionKey</code>, the default action is returned,
     * otherwise if the key is an Action, it is returned.
     */
    static class KeymapActionMap extends ActionMap {
        private Keymap keymap;

        KeymapActionMap(Keymap keymap) {
            this.keymap = keymap;
        }

        public Object[] keys() {
            Object[] sKeys = super.keys();
            Object[] keymapKeys = keymap.getBoundActions();
            int sCount = (sKeys == null) ? 0 : sKeys.length;
            int keymapCount = (keymapKeys == null) ? 0 : keymapKeys.length;
            boolean hasDefault = (keymap.getDefaultAction() != null);

            if (hasDefault) {
                keymapCount++;
            }

            if (sCount == 0) {
                if (hasDefault) {
                    Object[] retValue = new Object[keymapCount];

                    if (keymapCount > 1) {
                        System.arraycopy(keymapKeys, 0, retValue, 0, keymapCount - 1);
                    }

                    retValue[keymapCount - 1] = KeymapWrapper.DefaultActionKey;

                    return retValue;
                }

                return keymapKeys;
            }

            if (keymapCount == 0) {
                return sKeys;
            }

            Object[] retValue = new Object[sCount + keymapCount];

            // There may be some duplication here...
            System.arraycopy(sKeys, 0, retValue, 0, sCount);

            if (hasDefault) {
                if (keymapCount > 1) {
                    System.arraycopy(keymapKeys, 0, retValue, sCount, keymapCount - 1);
                }

                retValue[(sCount + keymapCount) - 1] = KeymapWrapper.DefaultActionKey;
            } else {
                System.arraycopy(keymapKeys, 0, retValue, sCount, keymapCount);
            }

            return retValue;
        }

        public int size() {
            // There may be some duplication here...
            Object[] actions = keymap.getBoundActions();
            int keymapCount = (actions == null) ? 0 : actions.length;

            if (keymap.getDefaultAction() != null) {
                keymapCount++;
            }

            return super.size() + keymapCount;
        }

        public Action get(Object key) {
            Action retValue = super.get(key);

            if (retValue == null) {
                // Try the Keymap.
                if (key == KeymapWrapper.DefaultActionKey) {
                    retValue = keymap.getDefaultAction();
                } else if (key instanceof Action) {
                    // This is a little iffy, technically an Action is
                    // a valid Key. We're assuming the Action came from
                    // the InputMap though.
                    retValue = (Action)key;
                }
            }

            return retValue;
        }
    }
    
    
    /** #6326882 Minimalistic implementation of <code>AccessibleContext</code>.
     * It's needed to find out whether it is sufficient to satisfy A11Y. */
    private class AccessibleDesignerPaneBase extends AccessibleJComponent {
	public AccessibleDesignerPaneBase() {
	    super();
	}
    } // End of AccessibleDesignerPaneBase.

    
    
//    /** XXX Temporary, until the Document, Range and Position are moved */
//    public static DomDocument createDomDocument(WebForm webForm) {
//        return new Document(webForm);
//    }
//
//    /** XXX Temporary, until the Document, Range and Position are moved */
//    public static int compareBoundaryPoints(Node endPointA, int offsetA, Node endPointB, int offsetB) {
//        return Position.compareBoundaryPoints(endPointA, offsetA, endPointB, offsetB);
//    }
//
//    public static DomPosition createDomPosition(Node node, int offset, Bias bias) {
//        return Position.create(node, offset, bias);
//    }
//
//    public static DomPosition createDomPosition(Node node, boolean after) {
//        return Position.create(node, after);
//    }
//
//    public static DomPosition first(DomPosition dot, DomPosition mark) {
//        return Position.first(dot, mark);
//    }
//
//    public static DomPosition last(DomPosition dot, DomPosition mark) {
//        return Position.last(dot, mark);
//    }

    // XXX Moved from DesignerCaret.
    public void replaceSelection(String content) {
//        WebForm webform = component.getDocument().getWebForm();
        WebForm webform = getWebForm();

        // XXX Moving to DefaultKeyTypedAction.
//        InlineEditor editor = webform.getManager().getInlineEditor();
//        if ((content.equals("\n") || content.equals("\r\n")) // NOI18N
//        && (editor != null) && !editor.isMultiLine()) {
//            // Commit
//            // Should I look to see if the Shift key is pressed, and if so let
//            // you insert a newline?
//            webform.getManager().finishInlineEditing(false);
//            return;
//        }

        if (caret == null) {
            return;
        }
        // XXX Moving to DesigneCaret, and designer/jsf/../DomDocumentImpl.
//        /*
//        if (range.isReadOnlyRegion()) {
//            UIManager.getLookAndFeel().provideErrorFeedback(component);
//            return;
//        }
//         */
//        if (caret.hasSelection()) {
//            caret.removeSelection();
//        }
//
////        Position pos = getDot();
//        DomPosition pos = caret.getDot();
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
//            UIManager.getLookAndFeel().provideErrorFeedback(this);
//            return;
//        }
//
////        component.getDocument().insertString(this, pos, content);
////        component.getDocument().insertString(pos, content);
//        webform.getDomDocument().insertString(pos, content);
        boolean beep = !caret.replaceSelection(content);
        
        if (beep) {
            UIManager.getLookAndFeel().provideErrorFeedback(this);
        }
    }
    
    // XXX Moved from DesignerCaret.
    /** XXX Incorrect impl of cut/copy operation. Revise.
     * Return the text in the selection, if any (if not returns null).
     * If the cut parameter is true, then the selection is deleted too.
     */
    public Transferable copySelection(boolean cut) {
        if (caret == null) {
            return null;
        }
        
        if (caret.hasSelection()) {
//            String text = range.getText();
            String text = caret.getSelectedText();
//            assert text.length() > 0;

            Transferable transferable = new StringSelection(text);

            if (cut) {
                caret.removeSelection();
            }

            return transferable;
        } else {
            return new StringSelection(""); // NOI18N
        }
    }
    

    public boolean hasCaret() {
        return getCaret() != null;
    }
    
    public boolean hasCaretSelection() {
        return caret == null ? false : caret.hasSelection();
    }
    
    public DomPosition getFirstPosition() {
        return caret == null ? DomPosition.NONE : caret.getFirstPosition();
    }
            
    public DomPosition getLastPosition() {
        return caret == null ? DomPosition.NONE : caret.getLastPosition();
    }

    public boolean isCaretReadOnlyRegion() {
        return caret == null ? false : caret.isReadOnlyRegion();
    }
    
//    public boolean isCaretWithinEditableRegion(DomPosition domPosition) {
//        return caret == null ? false : caret.isWithinEditableRegion(domPosition);
//    }
    
    public boolean isCaretVisible() {
        return caret == null ? false : caret.isVisible();
    }
    
    public DomPosition getCaretDot() {
        return caret == null ? DomPosition.NONE : caret.getDot();
    }
    
    public DomPosition getCaretMark() {
        return caret == null ? DomPosition.NONE : caret.getMark();
    }
    
    public void createCaret() {
        DesignerCaret dc = getUI().createCaret();
        setCaret(dc);
    }

    public boolean removeNextChar() {
        return caret == null ? false : caret.removeNextChar();
    }
    
    public boolean removePreviousChar() {
        return caret == null ? false : caret.removePreviousChar();
    }

    public DomRange getCaretRange() {
        return caret == null ? null : caret.getRange();
    }
    
    public void setCaretDot(DomPosition dot) {
        if (caret != null) {
            caret.setDot(dot);
        }
    }
    
    public void moveCaretDot(DomPosition dot) {
        if (caret != null) {
            caret.moveDot(dot);
        }
    }
    
    public void setCaretVisible(boolean visible) {
        if (caret != null) {
            caret.setVisible(visible);
        }
    }

    public void paintCaret(Graphics g) {
        if (caret != null) {
            caret.paint(g);
        }
    }
    
    public void setCaretMagicPosition(Point magicPosition) {
        if (caret != null) {
            caret.setMagicCaretPosition(magicPosition);
        }
    }
    
    public Point getCaretMagicPosition() {
        return caret == null ? null : caret.getMagicCaretPosition();
    }
    
    // XXX Strange hack, consequence of messy InteractionManager.
    public void mousePressed(MouseEvent evt) {
        if (caret != null) {
            caret.mousePressed(evt);
        }
    }
    
    // XXX Strange hack, consequence of messy InteractionManager.
    public void mouseClicked(MouseEvent evt) {
        if (caret != null) {
            caret.mouseClicked(evt);
        }
    }
    
    // XXX Strange hack, consequence of messy InteractionManager.
    public void mouseDragged(MouseEvent evt) {
        if (caret != null) {
            caret.mouseDragged(evt);
        }
    }
    
    // XXX Strange hack, consequence of messy InteractionManager.
    public void mouseReleased(MouseEvent evt) {
        if (caret != null) {
            caret.mouseReleased(evt);
        }
    }

    // XXX ?? Bad architecture.
    public void caretDetachDom() {
        if (caret != null) {
            caret.detachDom();
        }
    }
    
    /** XXX Escaping.
     * TODO Revise */
    public abstract void escape(long when);
    
    
    private static void info(String message) {
        Logger logger = getLogger();
        logger.info(message);
    }
    
    private static Logger getLogger() {
        return Logger.getLogger(DesignerPaneBase.class.getName());
    }
}
