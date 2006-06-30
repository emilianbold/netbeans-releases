/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.test.editor.app.core;

import org.netbeans.modules.editor.java.JavaIndentEngine;
import org.w3c.dom.Element;
import javax.swing.text.Document;
import org.netbeans.test.editor.app.core.properties.BadPropertyNameException;
import org.netbeans.test.editor.app.core.properties.BooleanProperty;
import org.netbeans.test.editor.app.core.properties.IntegerProperty;
import org.netbeans.test.editor.app.core.properties.Properties;
import org.netbeans.test.editor.app.gui.actions.TestDeleteAction;
import org.netbeans.test.editor.app.gui.tree.ActionsCache;
import org.netbeans.test.editor.app.util.ParsingUtils;
import org.openide.text.IndentEngine;

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
    
    public static String LEADING_STAR_IN_COMMENTS = "LeadingStarInComment";
    public static String ADD_NEW_LINE_BEFORE_PAR = "AddNewLineBeforeParenthesis";
    public static String ADD_SPACE_BEFORE_BRACKETS = "AddSpaceBeforeBrackets";
    public static String EXPAND_TABS = "ExpandTabs";
    public static String TAB_SIZE = "TabSize";
    
    /** Creates new TestSetJavaIEAction */
    public TestSetJavaIEAction(int num) {
        this("setJavaIE"+Integer.toString(num));
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
    
    public TestSetJavaIEAction(Element node) {
        super(node);
        setLeadingStarInComment(ParsingUtils.readBoolean(node, LEADING_STAR_IN_COMMENTS));
        setAddNewLineBeforePar(ParsingUtils.readBoolean(node, ADD_NEW_LINE_BEFORE_PAR));
        setAddSpaceBeforeBrackets(ParsingUtils.readBoolean(node, ADD_SPACE_BEFORE_BRACKETS));
        setExpandTabs(ParsingUtils.readBoolean(node, EXPAND_TABS));
        setTabSize(ParsingUtils.readInt(node, TAB_SIZE));
    }
    
    public void setIndentEngine(IndentEngine indentEngine) {  //no indent engine except Java IE
        this.indentEngine = findIndentEngine(JavaIndentEngine.class);
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
    
    public void fromXML(Element node) throws BadPropertyNameException {
        super.fromXML(node);
        setLeadingStarInComment(ParsingUtils.readBoolean(node, LEADING_STAR_IN_COMMENTS));
        setAddNewLineBeforePar(ParsingUtils.readBoolean(node, ADD_NEW_LINE_BEFORE_PAR));
        setAddSpaceBeforeBrackets(ParsingUtils.readBoolean(node, ADD_SPACE_BEFORE_BRACKETS));
        setExpandTabs(ParsingUtils.readBoolean(node, EXPAND_TABS));
        setTabSize(ParsingUtils.readInt(node, TAB_SIZE));
    }
    
    public Properties getProperties() {
        Properties ret=super.getProperties();
        ret.put(LEADING_STAR_IN_COMMENTS, new BooleanProperty(getLeadingStarInComment()));
        ret.put(ADD_NEW_LINE_BEFORE_PAR, new BooleanProperty(getAddNewLineBeforePar()));
        ret.put(ADD_SPACE_BEFORE_BRACKETS, new BooleanProperty(getAddSpaceBeforeBrackets()));
        ret.put(EXPAND_TABS, new BooleanProperty(getExpandTabs()));
        ret.put(TAB_SIZE, new IntegerProperty(getTabSize()));
        return ret;
    }
    
    public Object getProperty(String name) throws BadPropertyNameException {
        if (name.compareTo(LEADING_STAR_IN_COMMENTS) == 0) {
            return  new BooleanProperty(leadingStarInComment);
        } else if (name.compareTo(ADD_NEW_LINE_BEFORE_PAR) == 0) {
            return  new BooleanProperty(addNewLineBeforePar);
        } else if (name.compareTo(ADD_SPACE_BEFORE_BRACKETS) == 0) {
            return  new BooleanProperty(addSpaceBeforeBrackets);
        } else if (name.compareTo(EXPAND_TABS) == 0) {
            return  new BooleanProperty(expandTabs);
        } else if (name.compareTo(TAB_SIZE) == 0) {
            return new IntegerProperty(tabSize);
        } else {
            return super.getProperty(name);
        }
    }
    
    public void setProperty(String name, Object value)  throws BadPropertyNameException {
        if (name.compareTo(LEADING_STAR_IN_COMMENTS) == 0) {
            setLeadingStarInComment(((BooleanProperty)value).getValue());
        } else if (name.compareTo(ADD_NEW_LINE_BEFORE_PAR) == 0) {
            setAddNewLineBeforePar(((BooleanProperty)value).getValue());
        } else if (name.compareTo(ADD_SPACE_BEFORE_BRACKETS) == 0) {
            setAddSpaceBeforeBrackets(((BooleanProperty)value).getValue());
        } else if (name.compareTo(EXPAND_TABS) == 0) {
            setExpandTabs(((BooleanProperty)value).getValue());
        } else if (name.compareTo(TAB_SIZE) == 0) {
            setTabSize(((IntegerProperty)value).getValue());
        } else {
            super.setProperty(name, value);
        }
    }
    
    public boolean getLeadingStarInComment() {
        return leadingStarInComment;
    }
    
    public void setLeadingStarInComment(boolean value) {
        boolean old = getLeadingStarInComment();
        
        leadingStarInComment = value;
        firePropertyChange(LEADING_STAR_IN_COMMENTS, old ? Boolean.TRUE : Boolean.FALSE, value ? Boolean.TRUE : Boolean.FALSE);
    }
    
    public boolean getAddNewLineBeforePar() {
        return addNewLineBeforePar;
    }
    
    public void setAddNewLineBeforePar(boolean value) {
        boolean old = getAddNewLineBeforePar();
        
        addNewLineBeforePar = value;
        firePropertyChange(ADD_NEW_LINE_BEFORE_PAR, old ? Boolean.TRUE : Boolean.FALSE, value ? Boolean.TRUE : Boolean.FALSE);
    }
    
    public boolean getAddSpaceBeforeBrackets() {
        return addSpaceBeforeBrackets;
    }
    
    public void setAddSpaceBeforeBrackets(boolean value) {
        boolean old = getAddSpaceBeforeBrackets();
        
        addSpaceBeforeBrackets = value;
        firePropertyChange(ADD_SPACE_BEFORE_BRACKETS, old ? Boolean.TRUE : Boolean.FALSE, value ? Boolean.TRUE : Boolean.FALSE);
    }
    
    public boolean getExpandTabs() {
        return expandTabs;
    }
    
    public void setExpandTabs(boolean value) {
        boolean old = getExpandTabs();
        
        expandTabs = value;
        firePropertyChange(EXPAND_TABS, old ? Boolean.TRUE : Boolean.FALSE, value ? Boolean.TRUE : Boolean.FALSE);
    }
    
    public int getTabSize() {
        return tabSize;
    }
    
    public void setTabSize(int value) {
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
