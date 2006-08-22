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

import org.netbeans.modules.java.editor.options.JavaOptions;
import org.w3c.dom.Element;
import javax.swing.text.Document;
import org.netbeans.test.editor.app.core.properties.BadPropertyNameException;
import org.netbeans.test.editor.app.core.properties.BooleanProperty;
import org.netbeans.test.editor.app.core.properties.Properties;
import org.netbeans.test.editor.app.gui.actions.TestDeleteAction;
import org.netbeans.test.editor.app.gui.tree.ActionsCache;
import org.netbeans.test.editor.app.util.ParsingUtils;
import org.openide.options.SystemOption;

/**
 *
 * @author  jlahoda
 * @version
 */
public class TestSetCompletionAction extends TestSetAction {
    
    private boolean caseSensitive;
    private boolean instantSubstitution;
    private boolean naturalSort;
    
    public static String CASE_SENSITIVE = "CaseSensitive";
    public static String INSTANT_SUBSTITUTION = "InstantSubstitution";
    public static String NATURAL_SORT = "NaturalSort";
    
    /** Creates new TestSetJavaIEAction */
    public TestSetCompletionAction(int num) {
        this("setCompletion"+Integer.toString(num));
    }
    
    public TestSetCompletionAction(String name) {
        super(name);
    }
    
    public TestSetCompletionAction(Element node) {
        super(node);
        setCaseSensitive(ParsingUtils.readBoolean(node, CASE_SENSITIVE));
        setInstantSubstitution(ParsingUtils.readBoolean(node, INSTANT_SUBSTITUTION));
        setNaturalSort(ParsingUtils.readBoolean(node, NATURAL_SORT));
    }
    
    public Element toXML(Element node) {
        node = super.toXML(node);
        
        node.setAttribute(CASE_SENSITIVE, String.valueOf(getCaseSensitive()));
        node.setAttribute(INSTANT_SUBSTITUTION, String.valueOf(getInstantSubstitution()));
        node.setAttribute(NATURAL_SORT, String.valueOf(getNaturalSort()));
        return node;
    }
    
    public void fromXML(Element node) throws BadPropertyNameException {
        super.fromXML(node);
        setCaseSensitive(ParsingUtils.readBoolean(node, CASE_SENSITIVE));
        setInstantSubstitution(ParsingUtils.readBoolean(node, INSTANT_SUBSTITUTION));
        setNaturalSort(ParsingUtils.readBoolean(node, NATURAL_SORT));
    }
    
    public Properties getProperties() {
        Properties ret=super.getProperties();
        ret.put(CASE_SENSITIVE, new BooleanProperty(caseSensitive));
        ret.put(INSTANT_SUBSTITUTION, new BooleanProperty(instantSubstitution));
        ret.put(NATURAL_SORT, new BooleanProperty(naturalSort));
        return ret;
    }
    
    public Object getProperty(String name) throws BadPropertyNameException {
        if (name.compareTo(CASE_SENSITIVE) == 0) {
            return new BooleanProperty(caseSensitive);
        } else if (name.compareTo(INSTANT_SUBSTITUTION) == 0) {
            return new BooleanProperty(instantSubstitution);
        } else if (name.compareTo(NATURAL_SORT) == 0) {
            return new BooleanProperty(naturalSort);
        } else {
            return super.getProperty(name);
        }
    }
    
    public void setProperty(String name, Object value)  throws BadPropertyNameException {
        if (name.compareTo(CASE_SENSITIVE) == 0) {
            setCaseSensitive(((BooleanProperty)value).getValue());
        } else if (name.compareTo(INSTANT_SUBSTITUTION) == 0) {
            setInstantSubstitution(((BooleanProperty)value).getValue());
        } else if (name.compareTo(NATURAL_SORT) == 0) {
            setNaturalSort(((BooleanProperty)value).getValue());
        } else {
            super.setProperty(name, value);
        }
    }
    
    public boolean getCaseSensitive() {
        return caseSensitive;
    }
    
    public void setCaseSensitive(boolean value) {
        boolean old = getCaseSensitive();
        
        caseSensitive = value;
        firePropertyChange(CASE_SENSITIVE, old ? Boolean.TRUE : Boolean.FALSE, value ? Boolean.TRUE : Boolean.FALSE);
    }
    
    public boolean getInstantSubstitution() {
        return instantSubstitution;
    }
    
    public void setInstantSubstitution(boolean value) {
        boolean old = getInstantSubstitution();
        
        instantSubstitution = value;
        firePropertyChange(INSTANT_SUBSTITUTION, old ? Boolean.TRUE : Boolean.FALSE, value ? Boolean.TRUE : Boolean.FALSE);
    }
    
    public boolean getNaturalSort() {
        return naturalSort;
    }
    
    public void setNaturalSort(boolean value) {
        boolean old = getNaturalSort();
        
        naturalSort = value;
        firePropertyChange(NATURAL_SORT, old ? Boolean.TRUE : Boolean.FALSE, value ? Boolean.TRUE : Boolean.FALSE);
    }
    
    public void perform() {
        super.perform();
        JavaOptions opts = (JavaOptions)(SystemOption.findObject(JavaOptions.class));
        opts.setCompletionCaseSensitive(caseSensitive);
        opts.setCompletionInstantSubstitution(instantSubstitution);
        opts.setCompletionNaturalSort(naturalSort);
    }
}
