/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.test.editor.app.core;

import org.netbeans.modules.editor.java.JavaIndentEngine;
import org.w3c.dom.Element;
import javax.swing.text.Document;

/**
 *
 * @author  jlahoda
 * @version 
 */
public class TestSetJavaIEAction extends TestSetIEAction {
    
    private boolean leadingStarInComment;
    private boolean addNewLineBeforePar;
    private boolean addSpaceBeforeBrackets;
    private boolean expandTabs;
    private int     tabSize;
    
    public static String LEADING_STAR_IN_COMMENTS = "leadingStarInComment";
    public static String ADD_NEW_LINE_BEFORE_PAR = "addNewLineBeforePar";
    public static String ADD_SPACE_BEFORE_BRACKETS = "addSpaceBeforeBrackets";
    public static String EXPAND_TABS = "expandTabs";
    public static String TAB_SIZE = "tabSize";
    
    /** Creates new TestSetJavaIEAction */
    public TestSetJavaIEAction(int num) {
        this("setJavaIndentEngine"+Integer.toString(num));
    }
    
    public TestSetJavaIEAction(String name) {
        super(name);
        setIndentEngine(findIndentEngine(JavaIndentEngine.class));

        JavaIndentEngine engine = (JavaIndentEngine) getIndentEngine();
        
        setExpandTabs(engine.isExpandTabs());
        setLeadingStarInComment(engine.getJavaFormatLeadingStarInComment());
        setAddNewLineBeforePar(engine.getJavaFormatNewlineBeforeBrace());
        setAddSpaceBeforeBrackets(engine.getJavaFormatSpaceBeforeParenthesis());
        setTabSize(engine.getSpacesPerTab());
    }
    
    private boolean readBoolean(Element node, String name) {
        String attribute = node.getAttribute(name);
        
        if (attribute == null) {
            return false;
        }
	if ("true".equalsIgnoreCase(attribute)) {
	    return true;
	} else {
	    return false;
	}
    }
    
    private int readInt(Element node, String name) {
        String attribute = node.getAttribute(name);
        
        if (attribute == null) {
            return 0;
        }
        try {
            return Integer.parseInt(attribute);
        } catch (NumberFormatException e) {
            e.printStackTrace(System.err);
            return 0;
        }
    }
    
    public TestSetJavaIEAction(Element node) {
        super(node);
        setLeadingStarInComment(readBoolean(node, LEADING_STAR_IN_COMMENTS));
        setAddNewLineBeforePar(readBoolean(node, ADD_NEW_LINE_BEFORE_PAR));
        setAddSpaceBeforeBrackets(readBoolean(node, ADD_SPACE_BEFORE_BRACKETS));
        setExpandTabs(readBoolean(node, EXPAND_TABS));
        setTabSize(readInt(node, TAB_SIZE));
    }
    
    public Element toXML(Element node) {
        node = super.toXML(node);

        node.setAttribute(LEADING_STAR_IN_COMMENTS, String.valueOf(getLeadingStarInComment()));
        node.setAttribute(ADD_NEW_LINE_BEFORE_PAR, String.valueOf(getAddNewLineBeforePar()));
        node.setAttribute(ADD_SPACE_BEFORE_BRACKETS, String.valueOf(getAddSpaceBeforeBrackets()));
        node.setAttribute(EXPAND_TABS, String.valueOf(getExpandTabs()));
        node.setAttribute(TAB_SIZE, String.valueOf(getTabSize()));
        return node;
    }
    
    public boolean getLeadingStarInComment() {
        return leadingStarInComment;
    }

    public void setLeadingStarInComment(boolean value) {
        boolean old = getLeadingStarInComment();

        leadingStarInComment = value;
        firePropertyChange(LEADING_STAR_IN_COMMENTS, new Boolean(old), new Boolean(value));
    }

    public boolean getAddNewLineBeforePar() {
        return addNewLineBeforePar;
    }

    public void setAddNewLineBeforePar(boolean value) {
        boolean old = getAddNewLineBeforePar();
        
        addNewLineBeforePar = value;
        firePropertyChange(ADD_NEW_LINE_BEFORE_PAR, new Boolean(old), new Boolean(value));
    }

    public boolean getAddSpaceBeforeBrackets() {
        return addSpaceBeforeBrackets;
    }

    public void setAddSpaceBeforeBrackets(boolean value) {
        boolean old = getAddSpaceBeforeBrackets();
        
        addSpaceBeforeBrackets = value;
        firePropertyChange(ADD_SPACE_BEFORE_BRACKETS, new Boolean(old), new Boolean(value));
    }

    public boolean getExpandTabs() {
        return expandTabs;
    }

    public void setExpandTabs(boolean value) {
        boolean old = getExpandTabs();
        
        expandTabs = value;
        firePropertyChange(EXPAND_TABS, new Boolean(old), new Boolean(value));
    }

    public int     getTabSize() {
        return tabSize;
    }

    public void    setTabSize(int value) {
        int old = getTabSize();
        
        tabSize = value;
        firePropertyChange(TAB_SIZE, new Integer(old), new Integer(value));
    }
    
    public void perform() {
        super.perform();
        JavaIndentEngine engine = (JavaIndentEngine) getIndentEngine();
        
        engine.setExpandTabs(getExpandTabs());
        engine.setJavaFormatLeadingStarInComment(getLeadingStarInComment());
        engine.setJavaFormatNewlineBeforeBrace(getAddNewLineBeforePar());
        engine.setJavaFormatSpaceBeforeParenthesis(getAddSpaceBeforeBrackets());
        engine.setSpacesPerTab(getTabSize());
    }

}
