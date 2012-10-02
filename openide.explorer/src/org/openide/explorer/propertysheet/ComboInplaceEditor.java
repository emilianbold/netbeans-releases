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
package org.openide.explorer.propertysheet;

import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyEditor;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.*;
import javax.swing.event.AncestorListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.plaf.ComboBoxUI;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.text.JTextComponent;
import org.openide.explorer.propertysheet.editors.EnhancedPropertyEditor;
import org.openide.util.Lookup;


/** A combo box inplace editor.  Does a couple of necessary things:
 * 1.  It does not allow the UI delegate to install a focus listener on
 * it - it will manage opening and closing the popup on its own - this
 * is to avoid a specific problem - that if the editor is moved to a
 * different cell and updated, the focus lost event will arrive after
 * it has been moved, and the UI delegate will try to close the popup
 * when it should be opening.  2.  Contains a replacement renderer for
 * use on GTK look and feel - on JDK 1.4.2, combo boxes do not respect
 * the value assigned by setBackground() (there is a fixme note about this
 * in SynthComboBoxUI, so presumably this will be fixed at some point).
 */
class ComboInplaceEditor extends JComboBox implements InplaceEditor, FocusListener, AncestorListener {
    /*Keystrokes this inplace editor wants to consume */
    static final KeyStroke[] cbKeyStrokes = new KeyStroke[] {
            KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0, false), KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0, false),
            KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0, true), KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0, true),
            KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN, 0, false),
            KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP, 0, false),
            KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN, 0, true), KeyStroke.getKeyStroke(
                KeyEvent.VK_PAGE_UP, 0, true
            )
        };
    private static PopupChecker checker = null;
    protected PropertyEditor editor;
    protected PropertyEnv env;
    private ListCellRenderer originalRenderer;
    protected PropertyModel mdl;
    boolean inSetUI = false;
    private boolean tableUI;
    private boolean connecting = false;
    private boolean hasBeenEditable = false;
    private boolean needLayout = false;

    private boolean popupCancelled = false;

    /** Create a ComboInplaceEditor - the tableUI flag will tell it to use
     * less borders & such */
    public ComboInplaceEditor(boolean tableUI) {
        if (tableUI) {
            putClientProperty("JComboBox.isTableCellEditor", Boolean.TRUE); //NOI18N
        }

        if (Boolean.getBoolean("netbeans.ps.combohack")) { //NOI18N
            setLightWeightPopupEnabled(false);
        }

        if (getClass() == ComboInplaceEditor.class) {
            enableEvents(AWTEvent.FOCUS_EVENT_MASK);
        }

        this.tableUI = tableUI;

        if (tableUI) {
            updateUI();
        }
        
        addPopupMenuListener(new PopupMenuListener() {

            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent pme) {
                popupCancelled = false;
            }

            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent pme) {
                if( !popupCancelled ) {
                    ComboInplaceEditor.super.fireActionEvent();
                }
            }

            @Override
            public void popupMenuCanceled(PopupMenuEvent pme) {
                popupCancelled = true;
            }
        });
        
        originalRenderer = getRenderer();
    }

    /** Overridden to add a listener to the editor if necessary, since the
     * UI won't do that for us without a focus listener */
    public void addNotify() {
        super.addNotify();

        if (isEditable() && (getClass() == ComboInplaceEditor.class)) {
            getEditor().getEditorComponent().addFocusListener(this);
        }

        getLayout().layoutContainer(this);
    }

    public void setEditable(boolean val) {
        boolean hadBeenEditable = hasBeenEditable;
        hasBeenEditable |= val;
        super.setEditable(val);

        if (hadBeenEditable != hasBeenEditable) {
            log("Combo editor for " + editor + " setEditable (" + val + ")");
            needLayout = true;
        }
    }

    /** Overridden to hide the popup and remove any listeners from the
     * combo editor */
    public void removeNotify() {
        log("Combo editor for " + editor + " removeNotify forcing popup close");
        setPopupVisible(false);
        super.removeNotify();
        getEditor().getEditorComponent().removeFocusListener(this);
    }

    public Insets getInsets() {
        if ("Aqua".equals(UIManager.getLookAndFeel().getID())) {
            return new Insets(0, 0, 0, 0);
        } else {
            return super.getInsets();
        }
    }

    public void clear() {
        editor = null;
        env = null;
    }
    
    static void disable_VK_UP_VK_DOWN_Keystrokes(JComponent component) {
        String nonExistingActionName = "bleble";
        component.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), nonExistingActionName);
        component.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), nonExistingActionName);
        
        component.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), nonExistingActionName);
        component.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), nonExistingActionName);
        
        component.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), nonExistingActionName);
        component.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), nonExistingActionName);
    }

    public void connect(PropertyEditor pe, PropertyEnv env) {
        connecting = true;
        
        try {
            log("Combo editor connect to " + pe + " env=" + env);

            this.env = env;
            this.editor = pe;
            setModel(new DefaultComboBoxModel(pe.getTags()));

            boolean editable = (editor instanceof EnhancedPropertyEditor)
                ? ((EnhancedPropertyEditor) editor).supportsEditingTaggedValues()
                : ((env != null) && Boolean.TRUE.equals(env.getFeatureDescriptor().getValue("canEditAsText"))); //NOI18N

            setEditable(editable);
            setActionCommand(COMMAND_SUCCESS);
            setupAutoComplete();
            
            //Support for custom ListCellRenderer injection via PropertyEnv
            //The instance obtained from the env by the "customListCellRendererSupport" key
            //must both implement ListCellRenderer and extend AtomicReference<ListCellRenderer> 
            //The AtomicReference workaround it necessary since we somehow need to put 
            //reference to the original ListCellRenderer to the custom one.
            Object customRendererSupport = env.getFeatureDescriptor().getValue("customListCellRendererSupport"); //NOI18N
            if(customRendererSupport != null) {
                //set the actual renrerer to the custom one so it may delegate
                AtomicReference<ListCellRenderer> ref = (AtomicReference<ListCellRenderer>)customRendererSupport;
                ref.set(originalRenderer);
                setRenderer((ListCellRenderer)customRendererSupport);
            } 
            
            if(SheetTable.isValueIncrementEnabled(env)) {
                disable_VK_UP_VK_DOWN_Keystrokes(this);
                disable_VK_UP_VK_DOWN_Keystrokes(((JComponent)getEditor().getEditorComponent()));
                
                Object incrementSupport = env.getFeatureDescriptor().getValue( SheetTable.VALUE_INCREMENT );
                if( null != incrementSupport && incrementSupport instanceof SpinnerModel ) {
                    this.incrementSupport = (SpinnerModel)incrementSupport;
                }
                
            }
            
            
            
            
            reset();
        } finally {
            connecting = false;
        }
    }
    
    private SpinnerModel incrementSupport;
    
    SpinnerModel getIncrementSupport() {
        return incrementSupport;
    }

    private void log(String s) {
        if (PropUtils.isLoggable(ComboInplaceEditor.class) && (getClass() == ComboInplaceEditor.class)) {
            PropUtils.log(ComboInplaceEditor.class, s); //NOI18N
        }
    }

    /**
     * Prevent the "autocomplete decorated" combobox to call setSelectedItem with empty
     * value when one explicitly call InlineEditor.setValue(...)
     */
    private boolean in_setSelectedItem = false;
    
    public void setSelectedItem(Object o) {
        try {
            if(in_setSelectedItem) {
                in_setSelectedItem = false;
                if(SheetTable.isValueIncrementEnabled(env)) {
                    //return only when we are in the hack mode
                    return ;
                }
            }
            
            in_setSelectedItem = true;

            //Some property editors (i.e. IMT's choice editor) treat
            //null as 0.  Probably not the right way to do it, but needs to
            //be handled.
            if ((o == null) && (editor != null) && (editor.getTags() != null) && (editor.getTags().length > 0)) {
                o = editor.getTags()[0];
            }

            if (o != null) {
                super.setSelectedItem(o);
            }
        } finally {
            in_setSelectedItem = false;
        }
    }

    /** Overridden to not fire changes is an event is called inside the
     * connect method */
    public void fireActionEvent() {
        if (connecting || (editor == null)) {
            return;
        } else {
            if (editor == null) {
                return;
            }

            if( isAutoComplete() && isPopupVisible()) {
                return;
            }

            if ("comboBoxEdited".equals(getActionCommand())) {
                log("Translating comboBoxEdited action command to COMMAND_SUCCESS");
                setActionCommand(COMMAND_SUCCESS);
            }

            log("Combo editor firing ActionPerformed command=" + getActionCommand());
            super.fireActionEvent();
        }
    }

    public void reset() {
        String targetValue = null;

        if (editor != null) {
            log("Combo editor reset setting selected item to " + editor.getAsText());
            targetValue = editor.getAsText();

            //issue 26367, form editor needs ability to set a custom value
            //when editing is initiated (event handler combos, part of them
            //cleaning up their EnhancedPropertyEditors).  
        }

        if ((getClass() == ComboInplaceEditor.class) && (env != null) && (env.getFeatureDescriptor() != null)) {
            String initialEditValue = (String) env.getFeatureDescriptor().getValue("initialEditValue"); //NOI18N

            if (initialEditValue != null) {
                targetValue = initialEditValue;
            }
        }

        setSelectedItem(targetValue);
    }

    public Object getValue() {
        if (isEditable()) {
            return getEditor().getItem();
        } else {
            return getSelectedItem();
        }
    }

    public PropertyEditor getPropertyEditor() {
        return editor;
    }

    public PropertyModel getPropertyModel() {
        return mdl;
    }

    public void setPropertyModel(PropertyModel pm) {
        log("Combo editor set property model to " + pm);
        this.mdl = pm;
    }

    public JComponent getComponent() {
        return this;
    }

    public KeyStroke[] getKeyStrokes() {
        return cbKeyStrokes;
    }

    public void handleInitialInputEvent(InputEvent e) {
        //do nothing, this should get deprecated in InplaceEditor
    }

    /** Overridden to use CleanComboUI on Metal L&F to avoid extra borders */
    public void updateUI() {
        LookAndFeel lf = UIManager.getLookAndFeel();
        String id = lf.getID();
        boolean useClean = tableUI && (lf instanceof MetalLookAndFeel 
                || "GTK".equals(id) //NOI18N
                || ("Aqua".equals(id) && "10.5".compareTo(System.getProperty("os.version")) <= 0) //NOI18N
                || PropUtils.isWindowsVistaLaF() //#217957
                || "Kunststoff".equals(id)); //NOI18N

        if (useClean) {
            super.setUI(PropUtils.createComboUI(this, tableUI));
        } else {
            super.updateUI();
        }

        if (tableUI & getEditor().getEditorComponent() instanceof JComponent) {
            ((JComponent) getEditor().getEditorComponent()).setBorder(null);
        }
    }

    /** Overridden to set a flag used to block the UI from adding a focus
     * listener, and to use an alternate renderer class on GTK look and feel
     * to work around a painting bug in SynthComboUI (colors not set correctly)*/
    public void setUI(ComboBoxUI ui) {
        inSetUI = true;

        try {
            super.setUI(ui);
        } finally {
            inSetUI = false;
        }
    }

    /** Overridden to handle a corner case - an NPE if the UI tries to display
     * the popup, but the combo box is removed from the parent before that can
     * happen - only happens on very rapid clicks between popups */
    public void showPopup() {
        try {
            log(" Combo editor show popup");
            super.showPopup();
        } catch (NullPointerException e) {
            //An inevitable consequence - the look and feel will queue display
            //of the popup, but it can be processed after the combo box is
            //offscreen
            log(" Combo editor show popup later due to npe");

            SwingUtilities.invokeLater(
                new Runnable() {
                    public void run() {
                        ComboInplaceEditor.super.showPopup();
                    }
                }
            );
        }
    }

    private void prepareEditor() {
        Component c = getEditor().getEditorComponent();

        if (c instanceof JTextComponent) {
            JTextComponent jtc = (JTextComponent) c;
            String s = jtc.getText();

            if ((s != null) && (s.length() > 0)) {
                jtc.setSelectionStart(0);
                jtc.setSelectionEnd(s.length());
            }

            if (tableUI) {
                jtc.setBackground(getBackground());
            } else {
                jtc.setBackground(PropUtils.getTextFieldBackground());
            }
            if( tableUI )
                jtc.requestFocus();
        }

        if (getLayout() != null) {
            getLayout().layoutContainer(this);
        }

        repaint();
    }

    /** Overridden to do the focus-popup handling that would normally be done
     * by the look and feel */
    public void processFocusEvent(FocusEvent fe) {
        if ((fe.getID() == fe.FOCUS_LOST) &&
            fe.getOppositeComponent() == getEditor().getEditorComponent() &&
            isPopupVisible()) {
            
            return ; // If the popup is visible and the focus is transferred to the editor component,
                     // ignore the event - it would close the popup.
        }
        super.processFocusEvent(fe);

        if (PropUtils.isLoggable(ComboInplaceEditor.class)) {
            PropUtils.log(ComboInplaceEditor.class, "Focus event on combo " + "editor"); //NOI18N
            PropUtils.log(ComboInplaceEditor.class, fe);
        }

        Component focusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();

        if (isDisplayable() && (fe.getID() == fe.FOCUS_GAINED) && (focusOwner == this) && !isPopupVisible()) {
            if (isEditable()) {
                prepareEditor();

                if( tableUI )
                    SwingUtilities.invokeLater(new PopupChecker());
            } else {
                if (tableUI) {
                    showPopup();

                    //Try to beat the event mis-ordering at its own game
                    SwingUtilities.invokeLater(new PopupChecker());
                }
            }

            repaint();
        } else if ((fe.getID() == fe.FOCUS_LOST) && isPopupVisible() && !isDisplayable()) {
            if (!PropUtils.psCommitOnFocusLoss) {
                setActionCommand(COMMAND_FAILURE);
                fireActionEvent();
            }

            //We were removed, but we may be immediately added. See if that's the
            //case after other queued events run
            SwingUtilities.invokeLater(
                new Runnable() {
                    public void run() {
                        if (!isDisplayable()) {
                            hidePopup();
                        }
                    }
                }
            );
        }

        repaint();
    }

    public boolean isKnownComponent(Component c) {
        return (c == getEditor().getEditorComponent());
    }

    public void setValue(Object o) {
        setSelectedItem(o);
    }

    /** Returns true if the combo box is editable */
    public boolean supportsTextEntry() {
        return isEditable();
    }

    /** Overridden to install an ancestor listener which will ensure the
     * popup is always opened correctly */
    protected void installAncestorListener() {
        //Use a replacement which will check to ensure the popup is 
        //displayed
        if (tableUI) {
            addAncestorListener(this);
        } else {
            super.installAncestorListener();
        }
    }

    /** Overridden to block the UI from adding its own focus listener, which
     * will close the popup at the wrong times.  We will manage focus
     * ourselves instead */
    public void addFocusListener(FocusListener fl) {
        if (!inSetUI || !tableUI) {
            super.addFocusListener(fl);
        }
    }

    public void focusGained(FocusEvent e) {
        //do nothing
        prepareEditor();
    }

    /** If the editor loses focus, we're done editing - fire COMMAND_FAILURE */
    public void focusLost(FocusEvent e) {
        Component c = e.getOppositeComponent();

        if (!isAncestorOf(c) && (c != getEditor().getEditorComponent())) {
            if ((c == this) || (c instanceof SheetTable && ((SheetTable) c).isAncestorOf(this))) {
                //workaround for issue 38029 - editable combo editor can lose focus to ...itself
                return;
            }

            setActionCommand(COMMAND_FAILURE);
            log(" Combo editor lost focus - setting action command to " + COMMAND_FAILURE);
            getEditor().getEditorComponent().removeFocusListener(this);

            if (checker == null) {
                log("No active popup checker, firing action event");
                fireActionEvent();
            }
        }
    }

    /** Overridden to ensure the editor gets focus if editable */
    public void firePopupMenuCanceled() {
        super.firePopupMenuCanceled();

        if (isEditable()) {
            Component focus = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();

            if (isDisplayable() && (focus == this)) {
                log("combo editor popup menu canceled.  Requesting focus on editor component");
                getEditor().getEditorComponent().requestFocus();
            }
        }
    }

    /** Overridden to fire COMMAND_FAILURE on Escape */
    public void processKeyEvent(KeyEvent ke) {
        super.processKeyEvent(ke);

        if ((ke.getID() == ke.KEY_PRESSED) && (ke.getKeyCode() == ke.VK_ESCAPE)) {
            setActionCommand(COMMAND_FAILURE);
            fireActionEvent();
        }
    }

    public void ancestorAdded(javax.swing.event.AncestorEvent event) {
        //This is where we typically have a problem with popups not showing,
        //and below is the cure... Problem is that the popup is hidden
        //because the combo's ancestor is changed (even though we blocked
        //the normal ancestor listener from being added)
        checker = new PopupChecker();
        SwingUtilities.invokeLater(checker);
    }

    public void ancestorMoved(javax.swing.event.AncestorEvent event) {
        //do nothing
        if (needLayout && (getLayout() != null)) {
            getLayout().layoutContainer(this);
        }
    }

    public void ancestorRemoved(javax.swing.event.AncestorEvent event) {
        //do nothing
    }

    public void paintChildren(Graphics g) {
        if ((editor != null) && !hasFocus() && editor.isPaintable()) {
            return;
        } else {
            super.paintChildren(g);
        }
    }

    public void paintComponent(Graphics g) {
        //For property panel usage, allow the editor to paint
        if ((editor != null) && !hasFocus() && editor.isPaintable()) {
            Insets ins = getInsets();
            Color c = g.getColor();

            try {
                g.setColor(getBackground());
                g.fillRect(0, 0, getWidth(), getHeight());
            } finally {
                g.setColor(c);
            }

            ins.left += PropUtils.getTextMargin();
            editor.paintValue(
                g,
                new Rectangle(
                    ins.left, ins.top, getWidth() - (ins.right + ins.left), getHeight() - (ins.top + ins.bottom)
                )
            );
        } else {
            g.setColor(Color.red);
            super.paintComponent(g);
        }
    }

    /** A handy runnable which will ensure the popup is really displayed */
    private class PopupChecker implements Runnable {
        public void run() {
            Window w = KeyboardFocusManager.getCurrentKeyboardFocusManager().getActiveWindow();

            //in Java 1.5+ KeyboardFocusManager.getActiveWindow() may return null
            if (null != w && w.isAncestorOf(ComboInplaceEditor.this)) {
                if (isShowing() && !isPopupVisible()) {
                    log("Popup checker ensuring editor prepared or popup visible");

                    if (isEditable()) {
                        prepareEditor();
                    }
                    showPopup();
                }

                checker = null;
            }
        }
    }

    private boolean autoComplete = false;
    /**
     * Use reflection to check if SwingX library is on class path and add auto-complete.
     */
    private void setupAutoComplete() {
        if( Boolean.getBoolean( "nb.propertysheet.combobox.autocomplete.disable") ) //NOI18N
            return;
        try {
            ClassLoader cl = Lookup.getDefault().lookup( ClassLoader.class );
            Class c = cl.loadClass( "org.jdesktop.swingx.autocomplete.AutoCompleteDecorator" ); //NOI18N
            Method m = c.getMethod( "decorate", JComboBox.class );
            m.invoke( null, this );
            autoComplete = true;
        } catch( Exception e ) {
            //ignore, SwingX is either not available or unsupported version
        }
    }

    boolean isAutoComplete() {
        return autoComplete;
    }
}
