/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.jellytools;

import java.awt.Component;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.Iterator;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import org.netbeans.core.windows.ModeImpl;
import org.netbeans.core.windows.WindowManagerImpl;
import org.netbeans.core.windows.view.ui.tabcontrol.TabbedAdapter;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.QueueTool;
import org.netbeans.jemmy.operators.ContainerOperator;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JComponentOperator;
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
    
    /** Components operators. */
    private JButtonOperator _btLeft;
    private JButtonOperator _btRight;
    private JButtonOperator _btDown;
    
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

    /** Returns operator of left arrow button in top right corner intended to 
     * move tabs to be visible left ones.
     * @return JButtonOperator instance
     */
    public JButtonOperator btLeft() {
        if(_btLeft == null) {
            _btLeft = new JButtonOperator(
                        new ContainerOperator(getEditor().findTabbedAdapter()), 0);
        }
        return _btLeft;
    }

    /** Returns operator of right arrow button in top right corner intended to 
     * move tabs to be visible right ones.
     * @return JButtonOperator instance
     */
    public JButtonOperator btRight() {
        if(_btRight == null) {
            _btRight = new JButtonOperator(
                        new ContainerOperator(getEditor().findTabbedAdapter()), 1);
        }
        return _btRight;
    }
    
    /** Returns operator of down arrow button in top right corner intended to 
     * show list of opened documents and selects a document in the list.
     * @return JButtonOperator instance
     */
    public JButtonOperator btDown() {
        if(_btDown == null) {
            _btDown = new JButtonOperator(
                        new ContainerOperator(getEditor().findTabbedAdapter()), 2);
        }
        return _btDown;
    }

    /** Close all opened documents and discard all changes.
     * It works also if no file is modified, so it is a safe way how to close
     * documents and no block further execution.
     */
    public void closeDiscard() {
        Iterator iter = findEditorMode().getOpenedTopComponents().iterator();
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
        final ModeImpl mode = findEditorMode();
        // run in dispatch thread
        String name = (String)getQueueTool().invokeSmoothly(new QueueTool.QueueAction("getSelectedTopComponent().getName()") {    // NOI18N
            public Object launch() {
                return mode.getSelectedTopComponent().getName();
            }
        });
        return new EditorOperator(name);
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
    
    /** If the leftmost visible tab is partially hidden, it clicks on it. 
     * Otherwise it does nothing.
     * @return true if tabs were moved, false otherwise
     */
    public boolean jumpLeft() {
        TabbedAdapter ta = getEditor().findTabbedAdapter();
        
        if(btLeft().isEnabled()) {
            Rectangle r = new Rectangle();
            for (int i=0; i < ta.getModel().size(); i++) {
                ta.getTabRect(i, r);
                if (r.width > 0 && r.height > 0) {
                    //We've found the first visible tab
                    break;
                }
            }
            if (r.width < 0 || r.height < 0) {
                return false;
            }
            Point p = new Point (r.x + (r.width / 2), r.y + (r.width / 2));
            Component tabsComp = ta.getComponentAt(p);
            p = SwingUtilities.convertPoint (ta, p, tabsComp);
            
            // click left corner
            new JComponentOperator((JComponent)tabsComp).clickMouse(p.x, p.y, 1);
            return true;
        }
        return false;
    }

    /** Pushes rigth arrow control button in top right corner intended to 
     * move tabs to be visible right ones. If the button is not enabled, 
     * it does nothing.
     */
    public void moveTabsRight() {
        btRight().push();
    }

    /** Pushes left arrow control button in top right corner intended to 
     * move tabs to be visible left ones. If the button is not enabled, 
     * it does nothing.
     */
    public void moveTabsLeft() {
        btLeft().push();
    }

    /** Pushes down arrow control button in top right corner intended to 
     * show list of opened documents and selects index-th documents in the list.
     */
    public void selectDocument(int index) {
        btDown().push();
        JTableOperator tableOper = new JTableOperator(MainWindowOperator.getDefault());
        tableOper.selectCell(index, 0);
    }

    /** Pushes down arrow control button in top right corner intended to 
     * show list of opened documents and selects document with given name
     * in the list.
     */
    public void selectDocument(String name) {
        btDown().push();
        JTableOperator tableOper = new JTableOperator(MainWindowOperator.getDefault());
        int row = tableOper.findCellRow(name);
        if(row > -1) {
            tableOper.selectCell(row, 0);
        } else {
            throw new JemmyException("Cannot select document \""+name+"\".");
        }
    }
    
    /** Performs verification by accessing all sub-components */    
    public void verify() {
        getEditor().verify();
    }
    
    /** Finds editor mode within IDE window system.
     * @return editor mode instance
     */
    private ModeImpl findEditorMode() {
        // run in dispatch thread
        return (ModeImpl)getQueueTool().invokeSmoothly(new QueueTool.QueueAction("findMode") {    // NOI18N
            public Object launch() {
                return WindowManagerImpl.getInstance().findMode("editor"); //NOI18N
            }
        });
    }
}
