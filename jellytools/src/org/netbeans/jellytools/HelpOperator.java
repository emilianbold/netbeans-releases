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

import java.awt.Component;
import java.awt.Container;
import org.netbeans.jellytools.actions.HelpAction;
import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.TestOut;
import org.netbeans.jemmy.operators.ContainerOperator;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JEditorPaneOperator;
import org.netbeans.jemmy.operators.JFrameOperator;
import org.netbeans.jemmy.operators.JSplitPaneOperator;
import org.netbeans.jemmy.operators.JTabbedPaneOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.jemmy.operators.JTreeOperator;

/** Class implementing all necessary methods for handling "IDE Help" Frame.
 * Normally the Help window is a JFrame and it even cannot be placed inside
 * MDI desktop. But be careful. Help window can be transformed to a JDialog 
 * when another modal dialog is shown. In such a case you have to use
 * JDialogOperator to find it.
 *
 * @author Adam.Sotona@sun.com
 * @author Jiri.Skrivanek@sun.com
 */
public class HelpOperator extends JFrameOperator {

    /** Creates new HelpOperator that can handle it. It tries to find a JFrame
     * which contains some javax.help.JHelp* sub component. Indeed it is
     * a JFrame with "Help - All" title.
     * @throws TimeoutExpiredException when JFrame not found
     */
    public HelpOperator() {
        super(helpWindowChooser);
    }

    /** Creates new HelpOperator that can handle it.
     * @throws TimeoutExpiredException when JFrame not found
     * @param title String help frame title */
    public HelpOperator(String title) {
        super(title);
    }

    private static final HelpAction helpAction = new HelpAction();
    
    private JButtonOperator _btBack;
    private JButtonOperator _btNext;
    private JButtonOperator _btPrint;
    private JButtonOperator _btPageSetup;
    private JSplitPaneOperator _splpHelpSplitPane;
    private JTabbedPaneOperator _tbpHelpTabPane;
    private JTreeOperator _treeContents;
    private JTreeOperator _treeIndex;
    private JTextFieldOperator _txtIndexFind;
    private JTreeOperator _treeSearch;
    private JTextFieldOperator _txtSearchFind;
    private JEditorPaneOperator _txtContentViewer;

    /** invokes default help
     * @return HelpOperator for invoked help */    
    public static HelpOperator invoke() {
        helpAction.perform();
        return new HelpOperator();
    }

    /** invokes help with defined help set
     * @param helpSet String help set name
     * @return HelpOperator for invoked help */    
    public static HelpOperator invoke(String helpSet) {
        new HelpAction(helpSet).perform();
        return new HelpOperator(helpSet);
    }

    /** Tries to find "" JButton in this dialog.
     * @throws TimeoutExpiredException when component not found
     * @return JButtonOperator
     */
    public JButtonOperator btBack() {
        if (_btBack==null) {
            _btBack = new JButtonOperator(this, helpPackageChooser, 0);
        }
        return _btBack;
    }

    /** Tries to find "" JButton in this dialog.
     * @throws TimeoutExpiredException when component not found
     * @return JButtonOperator
     */
    public JButtonOperator btNext() {
        if (_btNext==null) {
            _btNext = new JButtonOperator(this, helpPackageChooser, 1);
        }
        return _btNext;
    }

    /** Tries to find "" JButton in this dialog.
     * @throws TimeoutExpiredException when component not found
     * @return JButtonOperator
     */
    public JButtonOperator btPrint() {
        if (_btPrint==null) {
            _btPrint = new JButtonOperator(this, helpPackageChooser, 2);
        }
        return _btPrint;
    }

    /** Tries to find "" JSplitPaneOperator in this dialog.
     * @throws TimeoutExpiredException when component not found
     * @return JButtonOperator
     */
    public JSplitPaneOperator splpHelpSplitPane() {
        if (_splpHelpSplitPane==null) {
            _splpHelpSplitPane = new JSplitPaneOperator( this );
        }
        return _splpHelpSplitPane;
    }

    /** Tries to find "" JTabbedPane in this dialog.
     * @throws TimeoutExpiredException when component not found
     * @return JButtonOperator
     */
    public JTabbedPaneOperator tbpHelpTabPane() {
        if (_tbpHelpTabPane==null) {
            _tbpHelpTabPane = new JTabbedPaneOperator( splpHelpSplitPane() );
        }
        return _tbpHelpTabPane;
    }

    /** Tries to find "" JButton in this dialog.
     * @throws TimeoutExpiredException when component not found
     * @return JButtonOperator
     */
    public JButtonOperator btPageSetup() {
        if (_btPageSetup==null) {
            _btPageSetup = new JButtonOperator(this, helpPackageChooser, 3);
        }
        return _btPageSetup;
    }

    /** Tries to find JTree in Contents tab of this dialog.
     * @throws TimeoutExpiredException when component not found
     * @return JTreeOperator
     */
    public JTreeOperator treeContents() {
        selectPageContents();
        if (_treeContents==null) {
            _treeContents = new JTreeOperator( tbpHelpTabPane(), 0 );
        }
        return _treeContents;
    }

    /** Tries to find JTree in Index tab of this dialog.
     * @throws TimeoutExpiredException when component not found
     * @return JTreeOperator
     */
    public JTreeOperator treeIndex() {
        selectPageIndex();
        if (_treeIndex==null) {
            _treeIndex = new JTreeOperator( tbpHelpTabPane(), 0 );
        }
        return _treeIndex;
    }

    /** Tries to find JTextField Find in Index tab of this dialog.
     * @throws TimeoutExpiredException when component not found
     * @return JTextFieldOperator
     */
    public JTextFieldOperator txtIndexFind() {
        selectPageIndex();
        if (_txtIndexFind==null) {
            _txtIndexFind = new JTextFieldOperator( tbpHelpTabPane(), 0 );
        }
        return _txtIndexFind;
    }

    /** Tries to find JTree in Search tab of this dialog.
     * @throws TimeoutExpiredException when component not found
     * @return JTreeOperator
     */
    public JTreeOperator treeSearch() {
        selectPageSearch();
        if (_treeSearch==null) {
            _treeSearch = new JTreeOperator( tbpHelpTabPane(), 0 );
        }
        return _treeSearch;
    }

    /** Tries to find JTextField Find in Search tab of this dialog.
     * @throws TimeoutExpiredException when component not found
     * @return JTextFieldOperator
     */
    public JTextFieldOperator txtSearchFind() {
        selectPageSearch();
        if (_txtSearchFind==null) {
            _txtSearchFind = new JTextFieldOperator( tbpHelpTabPane(), 0 );
        }
        return _txtSearchFind;
    }

    /** Tries to find null BasicContentViewerUI$JHEditorPane in this dialog.
     * @throws TimeoutExpiredException when component not found
     * @return JEditorPaneOperator
     */
    public JEditorPaneOperator txtContentViewer() {
        if (_txtContentViewer==null) {
            _txtContentViewer = new JEditorPaneOperator( splpHelpSplitPane(), 0 );
        }
        return _txtContentViewer;
    }

    /** clicks on "Back" JButton
     * @throws TimeoutExpiredException when MetalSplitPaneDivider$1 not found
     */
    public void back() {
        btBack().push();
    }

    /** clicks on "Next" JButton
     * @throws TimeoutExpiredException when JButton not found
     */
    public void next() {
        btNext().push();
    }

    /** clicks on "Print" JButton
     * @throws TimeoutExpiredException when JButton not found
     */
    public void print() {
        btPrint().push();
    }

    /** clicks on "Page Setup" JButton
     * @throws TimeoutExpiredException when JButton not found
     */
    public void pageSetup() {
        btPageSetup().pushNoBlock();
    }

    /** selects page Contents */    
    public void selectPageContents() {
        tbpHelpTabPane().selectPage(0);
    }
    
    /** selects page Index */    
    public void selectPageIndex() {
        tbpHelpTabPane().selectPage(1);
    }

    /** selects page Search */    
    public void selectPageSearch() {
        tbpHelpTabPane().selectPage(2);
    }

    /** tries to find and set text of txtIndexFind
     * @param text String text
     */
    public void indexFind( String text ) {
        txtIndexFind().enterText(text);
    }

    /** tries to find and set text of txtSearchFind
     * @param text String text
     */
    public void searchFind( String text ) {
        txtSearchFind().enterText(text);
    }

    /** returns help content in plain text form
     * @return String text of help
     */
    public String getContentText() {
        return txtContentViewer().getText();
    }

    /** Performs verification by accessing all sub-components */    
    public void verify() {
        btBack();
        btNext();
        btPageSetup();
        btPrint();
        treeContents();
        txtContentViewer();
        treeIndex();
        txtIndexFind();
        treeSearch();
        txtSearchFind();
    }
    
    /** Implementation of ComponentChooser to choose component which 
     * is instance of javax.help.JHelp*. */
    private static final ComponentChooser jHelpChooser = new ComponentChooser() {
        public boolean checkComponent(Component comp) {
            return comp.getClass().getName().startsWith("javax.help.JHelp");
        }
        public String getDescription() {
            return("any javax.help");
        }
    };
    
    /** Implementation of ComponentChooser to choose such a frame which 
     * contains some javax.help.JHelp* sub component. */
    private static final ComponentChooser helpWindowChooser = new ComponentChooser() {
        public boolean checkComponent(Component comp) {
            ContainerOperator contOper = new ContainerOperator((Container)comp);
            contOper.setOutput(TestOut.getNullOutput());
            return contOper.findSubComponent(jHelpChooser) != null;
        }
        public String getDescription() {
            return("containing any javax.help.JHelp component");
        }
    };
    
    /** Implementation of ComponentChooser to choose component which 
     * commes from package javax.help.*. */
    private static final ComponentChooser helpPackageChooser = new ComponentChooser() {
        public boolean checkComponent(Component comp) {
            return comp.getClass().getName().startsWith("javax.help.");
        }
        public String getDescription() {
            return("any javax.help.");
        }
    };
}

