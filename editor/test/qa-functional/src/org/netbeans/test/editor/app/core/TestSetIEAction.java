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
import org.netbeans.modules.editor.java.JavaIndentEngine;
import org.netbeans.modules.editor.java.JavaKit;
import org.netbeans.modules.editor.options.AllOptions;
import org.netbeans.modules.editor.options.BaseOptions;
import org.netbeans.test.editor.app.Main;
import org.netbeans.test.editor.app.core.properties.ArrayProperty;
import org.netbeans.test.editor.app.core.properties.BadPropertyNameException;
import org.netbeans.test.editor.app.core.properties.Properties;
import org.netbeans.test.editor.app.gui.actions.TestDeleteAction;
import org.netbeans.test.editor.app.gui.tree.ActionsCache;
import org.openide.util.Lookup;
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
        IndentEngine toSet = getIndentEngine();
        
        if (toSet == null) {
            System.err.println("TestSetIEAction: perform: Trying to set null indent engine!");
            return;
        }
        
        //        EditorKit kit = Main.frame.getEditor().getUI().getEditorKit(Main.frame.getEditor());
        EditorKit kit = Main.frame.getEditor().getEditorKit();
        
        if (kit == null) {
            System.err.println("TestSetIEAction: perform: kit == null!");
            return;
        }
        
        Class kitClass = kit.getClass();
        
        BaseOptions options = BaseOptions.getOptions(kitClass);
        
        if (options != null) {
//HACK --> workaround to Issue #25784                        
            Lookup.getDefault().lookup(toSet.getClass());
            
            options.setIndentEngine(toSet);
        } else {
            System.err.println("TestSetIEAction: perform kit class " + kitClass + " not found.");
        }
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
    
    public static void main(String[] args) {
        TestSetIEAction act=new TestSetIEAction("action");
        String[] names=act.getIndentEnginesNames();
        IndentEngine eng;
        String id=null;
        for (int i=0;i < names.length;i++) {
            eng=act.findIndentEngine(names[i]);
            Lookup.Template tmp = new Lookup.Template(null, null, eng);
            Lookup.Item item = Lookup.getDefault().lookupItem(tmp);
            if (item != null) id = item.getId();
            System.err.println("ID for "+names[i]+": "+id);
        }
    }
}
