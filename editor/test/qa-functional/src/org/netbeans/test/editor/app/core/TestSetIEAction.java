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

import java.util.ArrayList;
import org.netbeans.test.editor.app.gui.*;
import javax.swing.text.EditorKit;
import javax.swing.JEditorPane;
import org.netbeans.modules.editor.plain.PlainKit;
import javax.swing.text.PlainDocument;
import org.openide.text.IndentEngine;
import org.openide.options.SystemOption;
import org.netbeans.modules.editor.options.BaseOptions;
import java.util.Enumeration;
import org.netbeans.modules.editor.options.JavaOptions;
import org.netbeans.test.editor.app.util.Scheduler;
import javax.swing.SwingUtilities;

import javax.swing.text.Document;
import org.netbeans.modules.editor.NbEditorDocument;
import org.netbeans.modules.editor.options.AllOptions;
import org.netbeans.modules.editor.options.BaseOptions;
import org.netbeans.test.editor.app.Main;
import org.netbeans.test.editor.app.core.properties.ArrayProperty;
import org.netbeans.test.editor.app.core.properties.BadPropertyNameException;
import org.netbeans.test.editor.app.core.properties.Properties;
import org.netbeans.test.editor.app.gui.actions.TestDeleteAction;
import org.netbeans.test.editor.app.gui.tree.ActionsCache;
import org.w3c.dom.Element;

/**
 *
 * @author  jlahoda
 * @version
 */
public class TestSetIEAction extends TestSetAction {
    
    /** Holds value of property IndentEngine. */
    protected IndentEngine indentEngine;
    
    public static String INDENT_ENGINE = "IndentationEngine";
    
    public TestSetIEAction(int num) {
        this("setIE"+Integer.toString(num));
    }
    
    public TestSetIEAction(String name) {
        super(name);
        indentEngine=((BaseOptions)(SystemOption.findObject(JavaOptions.class, true))).getIndentEngine();
    }
    
    public TestSetIEAction(Element node) {
        super(node);
        indentEngine = findIndentEngine(node.getAttribute(INDENT_ENGINE));
    }
    
    public Element toXML(Element node) {
        node = super.toXML(node);
        
        node.setAttribute(INDENT_ENGINE, getIndentEngine().getName());
        return node;
    }
    
    public void fromXML(Element node) throws BadPropertyNameException {
        super.fromXML(node);
        indentEngine = findIndentEngine(node.getAttribute(INDENT_ENGINE));
    }
    
    public Properties getProperties() {
        Properties ret=super.getProperties();
        ret.put(INDENT_ENGINE, new ArrayProperty(indentEngine.getName(),getIndentEnginesNames()));
        return ret;
    }
    
    public Object getProperty(String name) throws BadPropertyNameException {
        if (name.compareTo(INDENT_ENGINE) == 0) {
            return new ArrayProperty(indentEngine.getName(),getIndentEnginesNames());
        } else {
            return super.getProperty(name);
        }
    }
    
    public void setProperty(String name, Object value)  throws BadPropertyNameException {
        if (name.compareTo(INDENT_ENGINE) == 0) {
            setIndentEngine(findIndentEngine(((ArrayProperty)value).getProperty()));
        } else {
            super.setProperty(name, value);
        }
    }
    
    public void perform() {
        super.perform();
        Scheduler.getDefault().addTask(new Thread() {
            public void run() {
                IndentEngine toSet = getIndentEngine();
                
                if (toSet == null) {
                    System.err.println("TestSetIEAction: perform: Trying to set null indent engine!");
                    return;
                }
                
                EditorKit kit = Main.frame.getEditor().getUI().getEditorKit(Main.frame.getEditor());
                
                if (kit == null) {
                    System.err.println("TestSetIEAction: perform: kit == null!");
                    return;
                }
                
                Class kitClass = kit.getClass();
                
                BaseOptions options = BaseOptions.getOptions(kitClass);
                
                if (options != null) {
                    options.setIndentEngine(toSet);
                } else {
                    System.err.println("TestSetIEAction: perform kit class " + kitClass + " not found.");
                }
            }
        });
    }
    
    /** Getter for property IndentEngine.
     * @return Value of property IndentEngine.
     */
    public IndentEngine getIndentEngine() {
        return indentEngine;
    }
    
    /** Setter for property IndentEngine.
     * @param IndentEngine New value of property IndentEngine.
     */
    public void setIndentEngine(IndentEngine indentEngine) {
        IndentEngine old = this.indentEngine;
        
        this.indentEngine = indentEngine;
        
        firePropertyChange(INDENT_ENGINE, old, indentEngine);
    }
    
    public String[] getIndentEngines() {
        String[] ret=null;
        int count=0;
        Enumeration e;
        IndentEngine en;
        
        e=IndentEngine.indentEngines();
        while (e.hasMoreElements()) {
            en=(IndentEngine)(e.nextElement());
            count++;
        }
        ret=new String[count];
        count=0;
        e=IndentEngine.indentEngines();
        while (e.hasMoreElements()) {
            ret[count++]=((IndentEngine)(e.nextElement())).getName();
        }
        return ret;
    }
    
    protected IndentEngine findIndentEngine(String name) {
        if (name == null)
            return null;
        
        Enumeration e=IndentEngine.indentEngines();
        while (e.hasMoreElements()) {
            IndentEngine item = (IndentEngine) e.nextElement();
            
            if (name.equals(item.getName())) {
                return item;
            }
        }
        return null;
    }
    
    protected IndentEngine findIndentEngine(Class clazz) {
        if (clazz == null)
            return null;
        
        Enumeration e=IndentEngine.indentEngines();
        while (e.hasMoreElements()) {
            IndentEngine item = (IndentEngine) e.nextElement();
            
            if (clazz.isInstance(item)) {
                return item;
            }
        }
        return null;
    }
    
    public static String[] getIndentEnginesNames() {
        ArrayList a=new ArrayList();
        Enumeration e=IndentEngine.indentEngines();
        while (e.hasMoreElements()) {
            IndentEngine item = (IndentEngine) e.nextElement();
            a.add(item.getName());
        }
        return (String[])(a.toArray(new String[a.size()]));
    }
}
