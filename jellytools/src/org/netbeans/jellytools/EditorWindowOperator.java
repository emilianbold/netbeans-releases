/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.jellytools;

import java.awt.Rectangle;
import java.lang.reflect.Method;
import java.util.Iterator;
import org.netbeans.core.windows.ModeImpl;
import org.netbeans.core.windows.WindowManagerImpl;
import org.netbeans.core.windows.view.ui.tabcontrol.TabbedAdapter;
import org.netbeans.core.windows.view.ui.tabcontrol.ViewTabUI;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.operators.JComponentOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JFrameOperator;
import org.netbeans.jemmy.operators.JTableOperator;
import org.openide.windows.TopComponent;

/**
 * Handle documents area of NetBeans IDE. It hold editor top components
 * which can be tested by {@link EditorOperator}. This operator only enables
 * to switch between editors and to manipulate control buttons.
 * <p>
 * Usage:<br>
 * <pre>
        EditorWindowOperator ewo = new EditorWindowOperator();
        String filename = "MyClass";
        EditorOperator eo1 = ewo.selectPage(filename);
        // gets currently selected editor
        EditorOperator eo2 = ewo.getEditor();
        // switches to requested editor and gets EditorOperator instance
        EditorOperator eo3 = ewo.getEditor(filename);
        // manipulate control buttons
        ewo.moveTabsLeft();
        ewo.moveTabsRight();
        ewo.jumpLeft();
        // selects document from the list
        ewo.selectDocument(filename);
 * </pre>
 * @author Jiri.Skrivanek@sun.com
 */
public class EditorWindowOperator extends JFrameOperator {
    
    /** Creates new instance of EditorWindowOperator. It waits for frame underlying
     * of editor mode. It can be a JFrame with title Editor or MainWindow frame
     * depending on state of window system.
     * @param filename not used anymore
     * @deprecated Use {@link #EditorWindowOperator()} or 
     * {@link EditorOperator#EditorOperator(String) EditorOperator(String)}
     * instead
     */
    public EditorWindowOperator(String filename) {
        this();
    }
    
    /** Creates new instance of EditorWindowOperator. It waits for frame underlying
     * of editor mode. It can be a JFrame with title Editor or MainWindow frame
     * depending on state of window system.
     */
    public EditorWindowOperator() {
        super(NbFrameOperator.waitJFrame("editor")); // NOI18N
    }
    
    /** Close all opened documents and discard all changes.
     * It works also if no file is modified, so it is a safe way how to close
     * documents and no block further execution.
     */
    public void closeDiscard() {
        ModeImpl mode = (ModeImpl)WindowManagerImpl.getInstance().findMode("editor"); //NOI18N
        Iterator iter = mode.getOpenedTopComponents().iterator();
        while(iter.hasNext()) {
            EditorOperator.close((TopComponent)iter.next(), false);
        }
    }
    
    /** Selects page by its label. If only one file is open in Editor, it does
     * nothing.
     * @param label label of page to switch to
     * @return instance of selected EditorOperator
     */
    public EditorOperator selectPage(String label) {
        return new EditorOperator(label);
    }
    
    /** Selects page by its index. If only one file is open in Editor, it does
     * nothing.
     * @param index index of page to be selected
     * @return instance of selected EditorOperator
     * @see EditorOperator
     */
    public EditorOperator selectPage(int index) {
        try {
            // finds and selects index-th editor
            new TopComponentOperator((TopComponent)getEditor().findTabbedAdapter().getTopComponentAt(index));
        } catch (IndexOutOfBoundsException e) {
            throw new JemmyException("Index "+index+" out of bounds.", e); //NOI18N
        }
        return getEditor();
    }
    
    /** Returns EditorOperator instance of currently selected document.
     * @return EditorOperator instance of the selected document
     * @see EditorOperator
     */
    public EditorOperator getEditor() {
        ModeImpl mode = (ModeImpl)WindowManagerImpl.getInstance().findMode("editor"); //NOI18N
        return new EditorOperator(mode.getSelectedTopComponent().getName());
    }
    
    /** Selects page with given label and returns EditorOperator instance of
     * that page.
     * @param label label of page to be selected
     * @return EditorOperator instance of requested page
     * @see EditorOperator
     */
    public EditorOperator getEditor(String label) {
        return selectPage(label);
    }
    
    /** Selects page with given index and returns EditorOperator instance of
     * that page.
     * @param index index of page to be selected
     * @return EditorOperator instance of requested page
     * @see EditorOperator
     */
    public EditorOperator getEditor(int index) {
        return selectPage(index);
    }

    /********************** Control buttons ********************************/
    
    /** Pushes left arrow button in top left corner intended to jump to the first
     * hidden document's tab on the left from currently opened document. If 
     * there is no hidden tab, the arrow button is not visible and this method
     * does nothing.
     * @return true if left arrow button was visible and pushed, false otherwise
     */
    public boolean jumpLeft() {
        TabbedAdapter ta = getEditor().findTabbedAdapter();
        ViewTabUI tabsUI = ta.getTabsDisplayer().getTabsUI();
        Rectangle arrowButtonRect = new Rectangle();
        try {
            Class clazz = Class.forName("org.netbeans.core.windows.view.ui.tabcontrol.ScrollableTabsUI");  // NOI18N
            Method methodArrowButtonRect = clazz.getDeclaredMethod("arrowButtonRect", 
                                                                   new Class[] {Rectangle.class});
            methodArrowButtonRect.setAccessible(true);
            methodArrowButtonRect.invoke(tabsUI, new Object[] {arrowButtonRect});
        } catch (Exception e) {
            throw new JemmyException("ScrollableTabsUI.arrowButtonRect() by reflection failed.", e);
        }
        if(arrowButtonRect.width > 0) {
            // only when width > 0, i.e. arrow button is visible, click on it
            new JComponentOperator(ta.getTabsDisplayer().getComponent()).
                                            clickMouse(arrowButtonRect.width/2, 
                                                       arrowButtonRect.height/2, 1);
            return true;
        }
        return false;
    }

    /** Pushes rigth arrow control button in top right corner intended to 
     * move tabs to be visible right ones. If the button is not enabled, 
     * it does nothing.
     */
    public void moveTabsRight() {
        clickControlButton(2);
    }

    /** Pushes left arrow control button in top right corner intended to 
     * move tabs to be visible left ones. If the button is not enabled, 
     * it does nothing.
     */
    public void moveTabsLeft() {
        clickControlButton(0);
    }

    /** Pushes down arrow control button in top right corner intended to 
     * show list of opened documents and selects index-th documents in the list.
     */
    public void selectDocument(int index) {
        clickControlButton(1);
        JTableOperator tableOper = new JTableOperator(new JDialogOperator(this));
        tableOper.selectCell(index, 0);
    }

    /** Pushes down arrow control button in top right corner intended to 
     * show list of opened documents and selects document with given name
     * in the list.
     */
    public void selectDocument(String name) {
        clickControlButton(1);
        JTableOperator tableOper = new JTableOperator(new JDialogOperator(this));
        int row = tableOper.findCellRow(name);
        if(row > -1) {
            tableOper.selectCell(row, 0);
        } else {
            throw new JemmyException("Cannot select document \""+name+"\".");
        }
    }
    
    private void clickControlButton(int index) {
        // TODO - test when fixed 36183
        TabbedAdapter ta = getEditor().findTabbedAdapter();
        ViewTabUI tabsUI = ta.getTabsDisplayer().getTabsUI();
        Rectangle controlButtonRect = new Rectangle();
        Rectangle tabsControlsRect = new Rectangle();
        JComponentOperator jComponentOper = new JComponentOperator(ta.getTabsDisplayer().getComponent());
        try {
            Class clazz = Class.forName("org.netbeans.core.windows.view.ui.tabcontrol.ScrollableTabsUI");
            Method method = clazz.getDeclaredMethod("tabsControlsRect", new Class[] {Rectangle.class});
            method.setAccessible(true);
            method.invoke(tabsUI, new Object[] {tabsControlsRect});

            // This should return valid coordinates for control button 
            // but it doesn't work
            method = clazz.getDeclaredMethod("controlButtonRect", new Class[] {int.class, Rectangle.class});
            method.setAccessible(true);
            method.invoke(tabsUI, new Object[] {new Integer(0), controlButtonRect});
        } catch (Exception e) {
            throw new JemmyException("ScrollableTabsUI.controlButtonRect() by reflection failed.", e);
        }
        jComponentOper.clickMouse(tabsControlsRect.x+tabsControlsRect.width/2+(index-1)*controlButtonRect.width, 
                                  tabsControlsRect.y+tabsControlsRect.height/2, 1);
    }
    
    /** Performs verification by accessing all sub-components */    
    public void verify() {
        getEditor().verify();
    }
}
