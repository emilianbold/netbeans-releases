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

import java.awt.Point;
import java.awt.Rectangle;
import java.lang.reflect.InvocationTargetException;
import javax.swing.table.JTableHeader;
import javax.swing.tree.TreePath;

import org.netbeans.core.projects.SettingChildren.FileStateProperty;

import org.netbeans.jellytools.actions.Action;
import org.netbeans.jellytools.actions.OptionsViewAction;
import org.netbeans.jellytools.properties.PropertySheetOperator;

import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.Timeouts;
import org.netbeans.jemmy.Waitable;
import org.netbeans.jemmy.Waiter;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.Operator;

/**
 * Provides access to the Options window and it's subcomponents.
 * Use PropertySheet class to access properties. 
 * treeTable() method returns TreeTable operator for
 * options list accessing.
 */
public class OptionsOperator extends NbDialogOperator {

    /** 
     * Constant used for indication of project property definition level
     * (first column after ">>").
     */
    public static final int PROJECT_LEVEL = 2;

    /** 
     * Constant used for indication of user property definition level
     * (second column after ">>").
     */
    public static final int USER_LEVEL = 3;

    /** 
     * Constant used for indication of default property definition level
     * (third column after ">>").
     */
    public static final int DEFAULT_LEVEL = 4;

    private static final Action invokeAction = new OptionsViewAction();

    private static final long BEFORE_EDITING_TIMEOUT = 2000;

    private static int DEFINE_HERE = 0;

    private TreeTableOperator _treeTable;
    
    /**
     * Waits for the Options window opened
     */
    public OptionsOperator() {
        super(getTitleToFind());
        setComparator(oldComparator);
        setDefaultStringComparator(oldComparator);
    }
    
    private static StringComparator oldComparator;

    /** Method to set exactly matching comparator to be used in constructor.
     * @return "Options" - title of window to be found
     */
    private static String getTitleToFind() {
        oldComparator = Operator.getDefaultStringComparator();
        DefaultStringComparator comparator = new DefaultStringComparator(true, true);
        setDefaultStringComparator(comparator);
        return Bundle.getString("org.netbeans.core.Bundle", "UI/Services"); 
    }
    /**
     * Invoces Options window by the menu operation.
     * @return OptionsOperator instance
     */
    public static OptionsOperator invoke() {
        invokeAction.perform();
        return new OptionsOperator();
    }

    static {
	Timeouts.initDefault("OptionsOperator.BeforeEditingTimeout", BEFORE_EDITING_TIMEOUT);
    }

    //subcomponents

    /** Getter for table containing property list and
     * property definition levels.
     * @return TreeTableOperator instance
     */
    public TreeTableOperator treeTable() {
        if(_treeTable == null) {
            _treeTable = new TreeTableOperator(this);
        }
        return _treeTable;
    }

    //shortcuts
    /** Selects an option in the options tree.
     * @param optionPath Path to the option in left (tree-like) column.
     * @return row index of selected node (starts at 0)
     */
    public int selectOption(String optionPath) {
        TreePath path = treeTable().tree().findPath(optionPath, "|");
        if(!treeTable().tree().isPathSelected(path)) {
            treeTable().tree().selectPath(path);
        }
        int result = treeTable().tree().getRowForPath(path);
        treeTable().scrollToCell(result, 0);
        new EventTool().waitNoEvent(500);
        return(result);
    }

    /** Selects an option in the options tree, waits for property sheet
     * corresponding to selected node and returns instance of PropertySheetOperator.
     * @param optionPath Path to the option in left (tree-like) column.
     * @return PropertySheetOperator of selected option
     */
    public PropertySheetOperator getPropertySheet(String optionPath) {
        selectOption(optionPath);
        // wait for property sheet corresponding with selected node
        final String nodeName = treeTable().tree().getSelectionPath().getLastPathComponent().toString();
        try {
            return (PropertySheetOperator)new Waiter(new Waitable() {
                public Object actionProduced(Object optionsOper) {
                    PropertySheetOperator pso = new PropertySheetOperator((OptionsOperator)optionsOper);
                    return pso.getDescriptionHeader().equals(nodeName) ? pso: null;
                }
                public String getDescription() {
                    return("Wait Property sheet for \""+nodeName+"\" is showing.");
                }
            }
            ).waitAction(this);
        } catch (InterruptedException e) {
            throw new JemmyException("Interrupted.", e);
        }
    }
    
    //definition levels

    /**
     * Shows definition levels column by clicking on the "<<" table
     * column title.
     */
    public void showLevels() {
        if(treeTable().getColumnCount() == 2) {
            clickOnSecondHeader();
        }
    }

    /**
     * Hides definition levels column by clicking on the ">>" table
     * column title.
     */
    public void hideLevels() {
        if(treeTable().getColumnCount() > 2) {
            clickOnSecondHeader();
        }
    }

    /**
     * Sets definition level for the option.
     * @param optionPath Path to the option in left (tree-like) column.
     * @param level One of the PROJECT_LEVEL, USER_LEVEL or DEFAULT_LEVEL
     */
    public void setLevel(String optionPath, final int level) {
        showLevels();
        int curLevel = getLevel(optionPath);
        getOutput().printLine("Setting " + level + " level for \"" +
                              optionPath + "\" option. \nCurrent level: " + curLevel);
        final int row = selectOption(optionPath);
        if(level > curLevel) {
            produceNoBlocking(new NoBlockingAction("Setting property definition level") {
                    public Object doAction(Object param) {
                        setLevel(row, level);
                        return(null);
                    }
                });
            JDialogOperator question = new JDialogOperator(Bundle.getString("org.openide.Bundle", 
                                                                            "NTF_QuestionTitle"));
            new JButtonOperator(question, Bundle.getString("org.openide.Bundle", 
                                                           "CTL_YES")).push();
        } else if(level < curLevel) {
            setLevel(row, level);
        }
    }

    /**
     * Gets definition level for the option.
     * @param optionPath Path to the option in left (tree-like) column.
     * @return level One of the PROJECT_LEVEL, USER_LEVEL or DEFAULT_LEVEL
     */
    public int getLevel(String optionPath) {
        int row = selectOption(optionPath);
        if       (getValue(row, PROJECT_LEVEL) == DEFINE_HERE) {
            return PROJECT_LEVEL;
        } else if(getValue(row, USER_LEVEL)    == DEFINE_HERE) {
            return USER_LEVEL;
        } else if(getValue(row, DEFAULT_LEVEL) == DEFINE_HERE) {
            return DEFAULT_LEVEL;
        }
        return -1;
    }
    
    /** Make an option to be difined on the project level.
     * @param optionPath Path to the option in left (tree-like) column.
     */
    public void setProjectLevel(String optionPath) {
        setLevel(optionPath, PROJECT_LEVEL);
    }

    /** Make an option to be difined on the user level.
     * @param optionPath Path to the option in left (tree-like) column.
     */
    public void setUserLevel(String optionPath) {
        setLevel(optionPath, USER_LEVEL);
    }

    /** Make an option to be difined on the default level.
     * @param optionPath Path to the option in left (tree-like) column.
     */
    public void setDefaultLevel(String optionPath) {
        setLevel(optionPath, DEFAULT_LEVEL);
    }


    //protected

    /** Sets a level for the row index.
     * @param row row index in the table
     * @param level level value
     */
    protected void setLevel(int row, int level) {
        if       (level == PROJECT_LEVEL) {
            defineHere(row, level);
        } else if(level == USER_LEVEL) {
            defineHere(row, level);
        } else if(level == DEFAULT_LEVEL) {
            revertLevel(row, level);
        }
    }

    /** Gets a value of the level definition mark.
     * @param row row index in the table
     * @param column column index in the table
     * @return value of the level definition mark
     */
    protected int getValue(int row, int column) {
        try { 
            FileStateProperty property = ((FileStateProperty)treeTable().getValueAt(row, column));
            return(((Integer)property.getValue()).intValue());
        } catch(IllegalAccessException e) {
            throw new JemmyException("Can not access value!", e);
        } catch(InvocationTargetException e) {
            throw new JemmyException("Can not access value!", e);
        }
    }

    /** Chooses "Revert Def" from the combobox.
     * @param row row index in the table
     * @param colIndex column index in the table
     */
    protected void revertLevel(final int row, final int colIndex) {
        editLevel(row, colIndex, Bundle.getString("org.netbeans.core.projects.Bundle", 
                                                  "LBL_action_revert"));
    }

    /** Chooses "Define Here" from the combobox.
     * @param row row index in the table
     * @param colIndex column index in the table
     */
    protected void defineHere(int row, int colIndex) {
        editLevel(row, colIndex, Bundle.getString("org.netbeans.core.projects.Bundle", 
                                                  "LBL_action_define"));
    }

    /**
     * Causes table editing and chooses a value in the combobox.
     * @param rowIndex Row index.
     * @param colIndex Column index. One of the columns containing 
     * level definition marks.
     * @param value String value to be choosed in the combobox.
     */
    protected void editLevel(int rowIndex, int colIndex, String value) {
        Point pnt = treeTable().getPointToClick(rowIndex, colIndex);
        treeTable().clickOnCell(rowIndex, colIndex);
        JComboBoxOperator combo = new JComboBoxOperator(treeTable());
        getTimeouts().sleep("OptionsOperator.BeforeEditingTimeout");
        combo.selectItem(value);
    }

    /**
     * Clicks on "<<" column header.
     */
    protected void clickOnSecondHeader() {
        JTableHeader header = treeTable().getTableHeader();
        Rectangle rect = header.getHeaderRect(1);
        new ComponentOperator(header).clickMouse(rect.x + rect.width/2, 
                                                 rect.y + rect.height/2,
                                                 1);
    }

    /** Performs verification by accessing all sub-components */    
    public void verify() {
        btClose();
        btHelp();
        treeTable().verify();
    }
}
