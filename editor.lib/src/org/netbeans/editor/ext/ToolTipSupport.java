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

package org.netbeans.editor.ext;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Font;
import java.awt.Color;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.FocusListener;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeSupport;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.Timer;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.JEditorPane;
import javax.swing.UIManager;
import javax.swing.text.JTextComponent;
import javax.swing.text.BadLocationException;
import javax.swing.text.Keymap;
import org.netbeans.editor.Utilities;
import org.netbeans.editor.BaseKit;
import org.netbeans.editor.BaseTextUI;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.WeakTimerListener;
import org.netbeans.editor.PopupManager;
import javax.swing.JTextArea;
import org.netbeans.editor.GlyphGutter;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.TextAction;
import org.netbeans.editor.EditorUI;

/**
 * Support for editor tooltips. Once the user stops moving the mouse
 * for the {@link #INITIAL_DELAY} milliseconds the enterTimer fires
 * and the {@link #updateToolTip()} method is called which searches
 * for the action named {@link ExtKit#buildToolTipAction} and if found
 * it executes it. The tooltips can be displayed by either calling
 * {@link #setToolTipText(java.lang.String)}
 * or {@link #setToolTip(javax.swing.JComponent)}.<BR>
 * However only one of the above ways should be used
 * not a combination of both because in such case
 * the text could be propagated in the previously set
 * custom tooltip component. 
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public class ToolTipSupport extends MouseAdapter implements MouseMotionListener, ActionListener, PropertyChangeListener, FocusListener {

    private static final Logger LOG = Logger.getLogger(ToolTipSupport.class.getName());
    
    /** Property for the tooltip component change */
    public static final String PROP_TOOL_TIP = "toolTip"; // NOI18N

    /** Property for the tooltip text change */
    public static final String PROP_TOOL_TIP_TEXT = "toolTipText"; // NOI18N

    /** Property for the visibility status change. */
    public static final String PROP_STATUS = "status"; // NOI18N
    
    /** Property for the enabled flag change */
    public static final String PROP_ENABLED = "enabled"; // NOI18N

    /** Property for the initial delay change */
    public static final String PROP_INITIAL_DELAY = "initialDelay"; // NOI18N

    /** Property for the dismiss delay change */
    public static final String PROP_DISMISS_DELAY = "dismissDelay"; // NOI18N

    private static final String UI_PREFIX = "ToolTip"; // NOI18N

    /** Initial delay before the tooltip is shown in milliseconds. */
    public static final int INITIAL_DELAY = 200;

    /** Delay after which the tooltip will be hidden automatically
     * in milliseconds.
     */
    public static final int DISMISS_DELAY = 60000;
    
    /** Status indicating that  the tooltip is not showing on the screen. */
    public static final int STATUS_HIDDEN = 0;
    /** Status indicating that  the tooltip is not showing on the screen
     * but once either the {@link #setToolTipText(java.lang.String)}
     * or {@link #setToolTip(javax.swing.JComponent)} gets called
     * the tooltip will become visible.
     */
    public static final int STATUS_VISIBILITY_ENABLED = 1;
    /** Status indicating that the tooltip is visible
     * because {@link #setToolTipText(java.lang.String)}
     * was called.
     */
    public static final int STATUS_TEXT_VISIBLE = 2;
    /** Status indicating that the tooltip is visible
     * because {@link #setToolTip(javax.swing.JComponent)}
     * was called.
     */
    public static final int STATUS_COMPONENT_VISIBLE = 3;
    
    /** Extra height added to the rectangle of modelToView() for mouse
     * cursor coordinates.
     */
    private static final int MOUSE_EXTRA_HEIGHT = 5;

    private static final String HTML_PREFIX_LOWERCASE = "<html"; //NOI18N
    private static final String HTML_PREFIX_UPPERCASE = "<HTML"; //NOI18N

    private static final String MOUSE_MOVE_IGNORED_AREA = "mouseMoveIgnoredArea"; //NOI18N

    private static final Action NO_ACTION = new TextAction("tooltip-no-action") { //NOI18N
        public @Override void actionPerformed(ActionEvent e) {
            // no-op
        }
    };

    private final Action HIDE_ACTION = new TextAction("tooltip-hide-action") { //NOI18N
        public @Override void actionPerformed(ActionEvent e) {
            ToolTipSupport.this.setToolTipVisible(false);
        }
    };

    private EditorUI extEditorUI;

    private JComponent toolTip;

    private String toolTipText;
    
    private Timer enterTimer;

    private Timer exitTimer;

    private boolean enabled;
    
    /** Status of the tooltip visibility. */
    private int status;

    private MouseEvent lastMouseEvent;

    private PropertyChangeSupport pcs;
    
    private PopupManager.HorizontalBounds horizontalBounds = PopupManager.ViewPortBounds;
    private PopupManager.Placement placement = PopupManager.AbovePreferred;

    private int verticalAdjustment;
    private int horizontalAdjustment;
    
    private boolean glyphListenerAdded = false;

    /** Construct new support for tooltips.
     */
    @SuppressWarnings({"OverridableMethodCallInConstructor", "LeakingThisInConstructor"})
    public ToolTipSupport(EditorUI extEditorUI) {
        this.extEditorUI = extEditorUI;

        enterTimer = new Timer(INITIAL_DELAY, new WeakTimerListener(this));
        enterTimer.setRepeats(false);
        exitTimer = new Timer(DISMISS_DELAY, new WeakTimerListener(this));
        exitTimer.setRepeats(false);

        extEditorUI.addPropertyChangeListener(this);

        setEnabled(true);
    }

    /** @return the component that either contains the tooltip
     * or is responsible for displaying of text tooltips.
     */
    public final JComponent getToolTip() {
        if (toolTip == null) {
            setToolTip(createDefaultToolTip());
        }

        return toolTip;
    }
    
    /** Set the tooltip component.
     * It can be called either to set the custom component
     * that will display the text tooltips or to display
     * the generic component with the tooltip after
     * the tooltip timer has fired.
     * @param toolTip component that either contains the tooltip
     *  or that will display a text tooltip.
     */
    public void setToolTip(JComponent toolTip) {
        setToolTip(toolTip, PopupManager.ViewPortBounds, PopupManager.AbovePreferred);
    }

    public void setToolTip(JComponent toolTip, PopupManager.HorizontalBounds horizontalBounds, 
        PopupManager.Placement placement) {
        setToolTip(toolTip, PopupManager.ViewPortBounds, PopupManager.AbovePreferred, 0, 0);
    }
    
    public void setToolTip(JComponent toolTip, PopupManager.HorizontalBounds horizontalBounds, 
        PopupManager.Placement placement, int horizontalAdjustment, int verticalAdjustment) {
        JComponent oldToolTip = this.toolTip;

        LOG.log(Level.FINE, "setTooltip: {0}", toolTip); //NOI18N
        
        this.toolTip = toolTip;
        this.horizontalBounds = horizontalBounds;
        this.placement = placement;
        this.horizontalAdjustment = horizontalAdjustment;
        this.verticalAdjustment = verticalAdjustment;

        if (status >= STATUS_VISIBILITY_ENABLED) {
            ensureVisibility();
        }

        firePropertyChange(PROP_TOOL_TIP, oldToolTip, this.toolTip);
    }
    
    /** Create the default tooltip component.
     */
    protected JComponent createDefaultToolTip() {
        return createTextToolTip(false);
    }

    private JEditorPane createHtmlTextToolTip() {
        class HtmlTextToolTip extends JEditorPane {
            public @Override void setSize(int width, int height) {
                Dimension prefSize = getPreferredSize();
                if (width >= prefSize.width) {
                    width = prefSize.width;
                } else { // smaller available width
                    super.setSize(width, 10000); // the height is unimportant
                    prefSize = getPreferredSize(); // re-read new pref width
                }
                if (height >= prefSize.height) { // enough height
                    height = prefSize.height;
                }
                super.setSize(width, height);
            }
            @Override
            public void setKeymap(Keymap map) {
                //#181722: keymaps are shared among components with the same UI
                //a default action will be set to the Keymap of this component below,
                //so it is necessary to use a Keymap that is not shared with other components
                super.setKeymap(addKeymap(null, map));
            }
        }

        JEditorPane tt = new HtmlTextToolTip();

        // setup tooltip keybindings
        filterBindings(tt.getActionMap());
        tt.getActionMap().put(HIDE_ACTION.getValue(Action.NAME), HIDE_ACTION);
        tt.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), HIDE_ACTION.getValue(Action.NAME));
        tt.getKeymap().setDefaultAction(NO_ACTION);

        Font font = UIManager.getFont(UI_PREFIX + ".font"); // NOI18N
        Color backColor = UIManager.getColor(UI_PREFIX + ".background"); // NOI18N
        Color foreColor = UIManager.getColor(UI_PREFIX + ".foreground"); // NOI18N

        if (font != null) {
            tt.setFont(font);
        }
        if (foreColor != null) {
            tt.setForeground(foreColor);
        }
        if (backColor != null) {
            tt.setBackground(backColor);
        }

        tt.setOpaque(true);
        tt.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(tt.getForeground()),
            BorderFactory.createEmptyBorder(0, 3, 0, 3)
        ));
        tt.setContentType("text/html"); //NOI18N

        return tt;
    }
    
    private JTextArea createTextToolTip(final boolean wrapLines) {
        class TextToolTip extends JTextArea {
            public @Override void setSize(int width, int height) {
                Dimension prefSize = getPreferredSize();
                if (width >= prefSize.width) {
                    width = prefSize.width;
                } else { // smaller available width
                    // Set line wrapping and do super.setSize() to determine
                    // the real height (it will change due to line wrapping)
                    
                    if (wrapLines) {
                        setLineWrap(true);
                        setWrapStyleWord(true);
                    }
                    
                    super.setSize(width, 10000); // the height is unimportant
                    prefSize = getPreferredSize(); // re-read new pref width
                }
                if (height >= prefSize.height) { // enough height
                    height = prefSize.height;
                } else { // smaller available height
                    // Check how much can be displayed - cannot rely on line count
                    // because line wrapping may display single physical line
                    // into several visual lines
                    // Before using viewToModel() a setSize() must be called
                    // because otherwise the viewToModel() would return -1.
                    super.setSize(width, 10000);
                    int offset = viewToModel(new Point(0, height));
                    Document doc = getDocument();
                    Element root = doc.getDefaultRootElement();
                    int lineIndex = root.getElementIndex(offset);
                    lineIndex--; // go to previous line
                    if (lineIndex >= 0) {
                        Element lineElem = root.getElement(lineIndex);
                        if (lineElem != null) {
                            try {
                                offset = lineElem.getStartOffset();
                                doc.remove(offset, doc.getLength() - offset);
                                doc.insertString(offset, "...", null);
                            } catch (BadLocationException e) {
                                // "..." will likely not be displayed but otherwise should be ok
                            }
                            // Recalculate the prefSize as it may be smaller
                            // than the present preferred height
                            height = Math.min(height, getPreferredSize().height);
                        }
                    }
                }
                super.setSize(width, height);
            }
            @Override
            public void setKeymap(Keymap map) {
                //#181722: keymaps are shared among components with the same UI
                //a default action will be set to the Keymap of this component below,
                //so it is necessary to use a Keymap that is not shared with other JTextAreas
                super.setKeymap(addKeymap(null, map));
            }
        }

        JTextArea tt = new TextToolTip();

        // set up tooltip keybindings
        filterBindings(tt.getActionMap());
        tt.getActionMap().put(HIDE_ACTION.getValue(Action.NAME), HIDE_ACTION);
        tt.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), HIDE_ACTION.getValue(Action.NAME));
        tt.getKeymap().setDefaultAction(NO_ACTION);
        
        Font font = UIManager.getFont(UI_PREFIX + ".font"); // NOI18N
        Color backColor = UIManager.getColor(UI_PREFIX + ".background"); // NOI18N
        Color foreColor = UIManager.getColor(UI_PREFIX + ".foreground"); // NOI18N

        if (font != null) {
            tt.setFont(font);
        }
        if (foreColor != null) {
            tt.setForeground(foreColor);
        }
        if (backColor != null) {
            tt.setBackground(backColor);
        }

        tt.setOpaque(true);
        tt.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(tt.getForeground()),
            BorderFactory.createEmptyBorder(0, 3, 0, 3)
        ));
        
        return tt;
    }

    public @Override void propertyChange(PropertyChangeEvent evt) {
        String propName = evt.getPropertyName();

        if (EditorUI.COMPONENT_PROPERTY.equals(propName)) {
            JTextComponent component = (JTextComponent)evt.getNewValue();
            if (component != null) { // just installed

                component.addPropertyChangeListener(this);
                
                disableSwingToolTip(component);

                component.addFocusListener(this);
                if (component.hasFocus()) {
                    focusGained(new FocusEvent(component, FocusEvent.FOCUS_GAINED));
                }
                component.addMouseListener(this);
                component.addMouseMotionListener(this);

                GlyphGutter gg = extEditorUI.getGlyphGutter();
                if (gg != null && !glyphListenerAdded) {
                    glyphListenerAdded = true;
                    gg.addMouseListener(this);
                    gg.addMouseMotionListener(this);
                }
                
                
            } else if (null != (component = (JTextComponent)evt.getOldValue())) { // just deinstalled
                component.removeFocusListener(this);
                component.removePropertyChangeListener(this);
                
                component.removeMouseListener(this);
                component.removeMouseMotionListener(this);
                
                GlyphGutter gg = extEditorUI.getGlyphGutter();
                if (gg != null) {
                    gg.removeMouseListener(this);
                    gg.removeMouseMotionListener(this);
                }
                setToolTipVisible(false);

            }
        }
        
        if (JComponent.TOOL_TIP_TEXT_KEY.equals(propName)) {
            JComponent component = (JComponent)evt.getSource();
            disableSwingToolTip(component);
            
            componentToolTipTextChanged(evt);
        }
                        
    }

    private void disableSwingToolTip(final JComponent component) {
        javax.swing.SwingUtilities.invokeLater(
            new Runnable() {
                public @Override void run() {
                    // Prevent default swing tooltip manager
                    javax.swing.ToolTipManager.sharedInstance().unregisterComponent(component);
                    
                    // Also disable the swing tooltip manager on gutter component
                    GlyphGutter gg = extEditorUI.getGlyphGutter();
                    if (gg != null) {
                        javax.swing.ToolTipManager.sharedInstance().unregisterComponent(gg);
                    }
                }
            }
        );
    }
    
    /** Update the tooltip by running corresponding action
     * {@link ExtKit#buildToolTipAction}. This method gets
     * called once the enterTimer fires and it can be overriden
     * by children.
     */
    protected void updateToolTip() {
        EditorUI ui = extEditorUI;
        if (ui == null)
            return;
        JTextComponent comp = ui.getComponent();
        if (comp == null)
            return;
        
        if (isGlyphGutterMouseEvent(lastMouseEvent)) {
            setToolTipText(extEditorUI.getGlyphGutter().getToolTipText(lastMouseEvent));
        } else { // over the text component
            BaseKit kit = Utilities.getKit(comp);
            if (kit != null) {
                Action a = kit.getActionByName(ExtKit.buildToolTipAction);
                if (a != null) {
                    a.actionPerformed(new ActionEvent(comp, 0, "")); // NOI18N
                }
            }
        }
    }

    /** 
     * Set the visibility of the tooltip.
     *
     * @param visible whether tooltip should become visible or not.
     *   If true the status is changed to {@link #STATUS_VISIBILITY_ENABLED}
     *   and {@link #updateToolTip()} is called.
     *
     *   <p>It is still possible that the tooltip will not be showing
     *   on the screen in case the tooltip or tooltip text are left unchanged.
     *
     * @since 2.3
     */
    public void setToolTipVisible(boolean visible) {
        if (!visible) { // ensure the timers are stopped
            enterTimer.stop();
            exitTimer.stop();
        }

        if (visible && status < STATUS_VISIBILITY_ENABLED
            || !visible && status >= STATUS_VISIBILITY_ENABLED
        ) {
            if (visible) { // try to show the tooltip
                if (enabled) {
                    setStatus(STATUS_VISIBILITY_ENABLED);
                    updateToolTip();
                }

            } else { // hide tip
                if (toolTip != null) {
                    if (toolTip.isVisible()){
                        toolTip.setVisible(false);
                        toolTip.putClientProperty(MOUSE_MOVE_IGNORED_AREA, null);
                        PopupManager pm = extEditorUI.getPopupManager();
                        if (pm!=null){
                            pm.uninstall(toolTip);
                        }
                    }
                }

                setStatus(STATUS_HIDDEN);
                extEditorUI.getComponent().requestFocusInWindow();
            }
        }
    }
    
    /** @return Whether the tooltip is showing on the screen.
     * {@link #getStatus() } gives the exact visibility state.
     */
    public boolean isToolTipVisible() {
        return status > STATUS_VISIBILITY_ENABLED;
    }
    
    /** @return status of the tooltip visibility. It can
     * be {@link #STATUS_HIDDEN}
     * or {@link #STATUS_VISIBILITY_ENABLED}
     * or {@link #STATUS_TEXT_VISIBLE}
     * or {@link #STATUS_COMPONENT_VISIBLE}.
     */
    public final int getStatus() {
        return status;
    }
    
    private void setStatus(int status) {
        if (this.status != status) {
            int oldStatus = this.status;
            this.status = status;
            firePropertyChange(PROP_STATUS,
                new Integer(oldStatus), new Integer(this.status));
        }
    }

    /** @return the current tooltip text.
     */
    public String getToolTipText() {
        return toolTipText;
    }
    
    
    
    /**
     * Makes the given String displayble. Probably there doesn't exists
     * perfect solution for all situation. (someone prefer display those
     * squares for undisplayable chars, someone unicode placeholders). So lets
     * try do the best compromise.
     */
    private static String makeDisplayable(String str , Font f) {
        if( str == null || f == null){
            return str;
        }
        StringBuilder buf = new StringBuilder(str.length());
        char[] chars = str.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            switch (c) {
                case '\t': buf.append(c); break;
                case '\n': buf.append(c); break;
                case '\r': buf.append(c); break;
                case '\b': buf.append("\\b"); break; // NOI18N
                case '\f': buf.append("\\f"); break; // NOI18N
                default:
                    if( f == null || f.canDisplay( c ) ){
                        buf.append(c);
                    } else {
                        buf.append("\\u"); // NOI18N
                        String hex = Integer.toHexString(c);
                        for (int j = 0; j < 4 - hex.length(); j++){
                            buf.append('0'); //NOI18N
                        }
                        buf.append(hex);
                    }
            }
        }
        return buf.toString();
    }
    
    /** Set the tooltip text to make the tooltip
     * to be shown on the screen.
     * @param text tooltip text to be displayed.
     */
    public void setToolTipText(String text) {
        
        final String displayableText = makeDisplayable(text, UIManager.getFont(UI_PREFIX + ".font")); //NOI18N
        
        Utilities.runInEventDispatchThread(new Runnable() {
            public @Override void run() {
                String oldText = toolTipText;
                toolTipText = displayableText;

                firePropertyChange(PROP_TOOL_TIP_TEXT,  oldText, toolTipText);
                
                if (toolTipText != null) {
                    if (toolTipText.startsWith(HTML_PREFIX_LOWERCASE) || toolTipText.startsWith(HTML_PREFIX_UPPERCASE)) {
                        JEditorPane jep = createHtmlTextToolTip();
                        jep.setText(toolTipText);
                        setToolTip(jep);
                    } else {
                        boolean multiLineText = toolTipText.contains("\n"); //NOI18N
                        JTextArea ta = createTextToolTip(!multiLineText);
                        ta.setText(toolTipText);
                        setToolTip(ta);
                    }
                } else { // null text
                    if (status == STATUS_TEXT_VISIBLE) {
                        setToolTipVisible(false);
                    }
                }
            }
        });
    }
    
    private boolean isGlyphGutterMouseEvent(MouseEvent evt) {
        return (evt != null && evt.getSource() == extEditorUI.getGlyphGutter());
    }

    private void ensureVisibility() {
        // Find the visual position in the document
        JTextComponent component = extEditorUI.getComponent();
        if (component != null) {
            // Try to display the tooltip above (or below) the line it corresponds to
            int pos = component.viewToModel(getLastMouseEventPoint());
            Rectangle cursorBounds = null;
            Rectangle mouseMoveIgnoredArea = null;
            
            if (pos >= 0) {
                try {
                    cursorBounds = component.modelToView(pos);
                    extendBounds(cursorBounds);

                    int [] offsets = Utilities.getSelectionOrIdentifierBlock(component, pos);
                    if (offsets != null) {
                        Rectangle r1 = component.modelToView(offsets[0]);
                        Rectangle r2 = component.modelToView(offsets[1]);
                        mouseMoveIgnoredArea = new Rectangle(r1.x, r1.y, r2.x - r1.x, r1.height);
                        extendBounds(mouseMoveIgnoredArea);
                    }
                } catch (BadLocationException e) {
                    // ignore
                }
            }
            if (cursorBounds == null) { // get mose rect
                cursorBounds = new Rectangle(getLastMouseEventPoint(), new Dimension(1, 1));
            }

            // updateToolTipBounds();
            PopupManager pm = extEditorUI.getPopupManager();
            
            if (toolTip != null && toolTip.isVisible()) {
                toolTip.setVisible(false);
            }
            pm.install(toolTip, cursorBounds, placement, horizontalBounds, horizontalAdjustment, verticalAdjustment);
            if (toolTip != null) {
                toolTip.putClientProperty(MOUSE_MOVE_IGNORED_AREA, mouseMoveIgnoredArea);
                toolTip.setVisible(true);
            }
        }
        exitTimer.restart();
    }

    private Rectangle extendBounds(Rectangle r) {
        if (horizontalBounds == PopupManager.ScrollBarBounds) {
            // no extending
        } else {
            if (placement == PopupManager.AbovePreferred || placement == PopupManager.Above) {
                // Enlarge the height slightly to not interfere with mouse cursor
                r.y -= MOUSE_EXTRA_HEIGHT;
                r.height += 2 * MOUSE_EXTRA_HEIGHT; // above and below
            } else if (placement == PopupManager.BelowPreferred || placement == PopupManager.Below) {
                r.y = r.y + r.height + MOUSE_EXTRA_HEIGHT + 1;
                r.height += MOUSE_EXTRA_HEIGHT; // above and below
            }
        }

        return r;
    }

    /** Helper method to get the identifier
     * under the mouse cursor.
     * @return string containing identifier under
     * mouse cursor.
     */
    public String getIdentifierUnderCursor() {
        String word = null;
        if (!isGlyphGutterMouseEvent(lastMouseEvent)) {
            try {
                JTextComponent component = extEditorUI.getComponent();
                BaseTextUI ui = (BaseTextUI)component.getUI();
                Point lmePoint = getLastMouseEventPoint();
                int pos = ui.viewToModel(component, lmePoint);
                if (pos >= 0) {
                    BaseDocument doc = (BaseDocument)component.getDocument();
                    int eolPos = Utilities.getRowEnd(doc, pos);
                    Rectangle eolRect = ui.modelToView(component, eolPos);
                    int lineHeight = extEditorUI.getLineHeight();
                    if (lmePoint.x <= eolRect.x && lmePoint.y <= eolRect.y + lineHeight) {
                        word = Utilities.getIdentifier(doc, pos);
                    }
                }
            } catch (BadLocationException e) {
                // word will be null
            }
        }

        return word;
    }

    /** @return whether the tooltip support is enabled. If it's
     * disabled the tooltip does not become visible.
     */
    public boolean isEnabled() {
        return enabled;
    }
    
    /** Set whether the tooltip support is enabled. If it's
     * disabled the tooltip does not become visible.
     * @param enabled whether the tooltip will be enabled or not.
     */
    public void setEnabled(boolean enabled) {
        if (enabled != this.enabled) {
            this.enabled = enabled;

            firePropertyChange(PROP_ENABLED,
                enabled ? Boolean.FALSE : Boolean.TRUE,
                enabled ? Boolean.TRUE : Boolean.FALSE
            );

            if (!enabled) {
                setToolTipVisible(false);
            }
        }
    }

    /** @return the delay between stopping
     * mouse movement and displaying
     * of the tooltip in milliseconds.
     */
    public int getInitialDelay() {
        return enterTimer.getDelay();
    }

    /** Set the delay between stopping
     * mouse movement and displaying
     * of the tooltip in milliseconds.
     */
    public void setInitialDelay(int delay) {
        if (enterTimer.getDelay() != delay) {
            int oldDelay = enterTimer.getDelay();
            enterTimer.setDelay(delay);

            firePropertyChange(PROP_INITIAL_DELAY,
                new Integer(oldDelay), new Integer(enterTimer.getDelay()));
        }
    }

    /** @return the delay between displaying
     * of the tooltip and its automatic hiding
     * in milliseconds.
     */
    public int getDismissDelay() {
        return exitTimer.getDelay();
    }

    /** Set the delay between displaying
     * of the tooltip and its automatic hiding
     * in milliseconds.
     */
    public void setDismissDelay(int delay) {
        if (exitTimer.getDelay() != delay) {
            int oldDelay = exitTimer.getDelay();
            exitTimer.setDelay(delay);
            
            firePropertyChange(PROP_DISMISS_DELAY,
                new Integer(oldDelay), new Integer(exitTimer.getDelay()));
        }
    }

    // -----------------------------------------------------------------------
    // ActionListener implementation
    // -----------------------------------------------------------------------

    public @Override void actionPerformed(ActionEvent evt) {
        if (evt.getSource() == enterTimer) {
            setToolTipVisible(true);

        } else if (evt.getSource() == exitTimer) {
            setToolTipVisible(false);
        }
    }

    // -----------------------------------------------------------------------
    // MouseListener implementation
    // -----------------------------------------------------------------------

    public @Override void mouseClicked(MouseEvent evt) {
        lastMouseEvent = evt;
        setToolTipVisible(false);
    }

    public @Override void mousePressed(MouseEvent evt) {
        lastMouseEvent = evt;
        setToolTipVisible(false);
    }

    public @Override void mouseReleased(MouseEvent evt) {
        lastMouseEvent = evt;
        setToolTipVisible(false);
        
        // Check that if a selection becomes visible by dragging a mouse
        // the tooltip evaluation should be posted.
        EditorUI ui = extEditorUI;
        if (ui != null) {
            JTextComponent component = ui.getComponent();
            if (enabled && component != null && Utilities.isSelectionShowing(component)) {
                enterTimer.restart();
            }
        }
    }

    public @Override void mouseEntered(MouseEvent evt) {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE, "mouseEntered: x=" + evt.getX() + "; y=" + evt.getY()); //NOI18N
        }
        lastMouseEvent = evt;
    }

    public @Override void mouseExited(MouseEvent evt) {
        lastMouseEvent = evt;
        if (toolTip != null && toolTip.isShowing()) {
            Rectangle r = new Rectangle(toolTip.getLocationOnScreen(), toolTip.getSize());

            if (LOG.isLoggable(Level.FINE)) {
                LOG.log(Level.FINE, "mouseExited: screen-x=" + evt.getXOnScreen() + "; screen-y=" + evt.getYOnScreen() //NOI18N
                    + "; tooltip=" + r); //NOI18N
            }
            if (r.contains(evt.getLocationOnScreen())) {
                // inside the tooltip component -> do not hide
                return;
            }
        }
        setToolTipVisible(false);
    }

    // -----------------------------------------------------------------------
    // MouseMotionListener implementation
    // -----------------------------------------------------------------------

    public @Override void mouseDragged(MouseEvent evt) {
        lastMouseEvent = evt;
        setToolTipVisible(false);
    }

    public @Override void mouseMoved(MouseEvent evt) {
        if (toolTip != null) {
            Rectangle r = (Rectangle) toolTip.getClientProperty(MOUSE_MOVE_IGNORED_AREA);
            if (LOG.isLoggable(Level.FINE)) {
                LOG.log(Level.FINE, "Mouse-Move-Ignored-Area=" + r + "; mouse-x=" + evt.getX() + "; mouse-y=" + evt.getY() //NOI18N
                    + "; is-inside=" + (r != null ? r.contains(evt.getX(), evt.getY()) : null)); //NOI18N
            }
            if (r != null && r.contains(evt.getX(), evt.getY())) {
                return;
            }
        }

        setToolTipVisible(false);
        if (enabled) {
            enterTimer.restart();
        }
        lastMouseEvent = evt;
    }

    /** @return last mouse event captured by this support.
     * This method can be used by the action that evaluates
     * the tooltip.
     */
    public final MouseEvent getLastMouseEvent() {
        return lastMouseEvent;
    }
    
    /** Possibly do translation when over the gutter.
     */
    private Point getLastMouseEventPoint() {
        Point p = null;
        MouseEvent lme = lastMouseEvent;
        if (lme != null) {
            p = lme.getPoint();
            if (lme.getSource() == extEditorUI.getGlyphGutter()) {
                // Over glyph gutter - change coords
                JTextComponent c = extEditorUI.getComponent();
                if (c != null) {
                    if (c.getParent() instanceof JViewport) {
                        JViewport vp = (JViewport)c.getParent();
                        p = new Point(vp.getViewPosition().x, p.y);
                    }
                }
            }
        }

        return p;
    }
                
                

    /** Called automatically when the
     * {@link javax.swing.JComponent#TOOL_TIP_TEXT_KEY}
     * property of the corresponding editor component
     * gets changed.<BR>
     * By default it calls {@link #setToolTipText(java.lang.String)}
     * with the new tooltip text of the component.
     */
    protected void componentToolTipTextChanged(PropertyChangeEvent evt) {
        JComponent component = (JComponent)evt.getSource();
        setToolTipText(component.getToolTipText());
    }

    private synchronized PropertyChangeSupport getPCS() {
        if (pcs == null) {
            pcs = new PropertyChangeSupport(this);
        }
        return pcs;
    }

    /** Add the listener for the property changes. The names
     * of the supported properties are defined
     * as "PROP_" public static string constants.
     * @param listener listener to be added.
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        getPCS().addPropertyChangeListener(listener);
    }
    
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        getPCS().removePropertyChangeListener(listener);
    }
    
    /** Fire the change of the given property.
     * @param propertyName name of the fired property
     * @param oldValue old value of the property
     * @param newValue new value of the property.
     */
    protected void firePropertyChange(String propertyName,
    Object oldValue, Object newValue) {
        getPCS().firePropertyChange(propertyName, oldValue, newValue);
    }
    
    public @Override void focusGained(FocusEvent e) {
//        JComponent component = (JComponent)e.getSource();
//        component.addMouseListener(this);
//        component.addMouseMotionListener(this);
        GlyphGutter gg = extEditorUI.getGlyphGutter();
        if (gg != null && !glyphListenerAdded) {
            glyphListenerAdded = true;
            gg.addMouseListener(this);
            gg.addMouseMotionListener(this);
        }
    }

    public @Override void focusLost(FocusEvent e) {
        /*
        JComponent component = (JComponent)e.getSource();
        component.removeMouseListener(this);
        component.removeMouseMotionListener(this);
        GlyphGutter gg = extEditorUI.getGlyphGutter();
        if (gg != null) {
            gg.removeMouseListener(this);
            gg.removeMouseMotionListener(this);
        }
        setToolTipVisible(false);
         */
    }

    private static void filterBindings(ActionMap actionMap) {
        for(Object key : actionMap.allKeys()) {
            String actionName = key.toString().toLowerCase(Locale.ENGLISH);

            LOG.log(Level.FINE, "Action-name: {0}", actionName); //NOI18N
            if (actionName.contains("delete") || actionName.contains("insert") || //NOI18N
                actionName.contains("paste") || actionName.contains("default") //NOI18N
            ) {
                actionMap.put(key, NO_ACTION);
            }
        }
    }
}
