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
import org.netbeans.editor.Settings;
import org.w3c.dom.Element;

/**
 *
 * @author  jlahoda
 * @version
 */
public class TestSetIEAction extends TestSetAction {

    /** Holds value of property IndentEngine. */
    private IndentEngine indentEngine;
    
    public static String INDENT_ENGINE = "indentationengine";
    
    public TestSetIEAction(int num) {
        this("set"+Integer.toString(num));
    }
    
    public TestSetIEAction(String name) {
        super(name);
        indentEngine=((BaseOptions)(SystemOption.findObject(JavaOptions.class, true)))
        .getIndentEngine();
	System.err.println("TestSetIEAction: <init>(String name): indentEngine=" + indentEngine);
	System.err.println("TestSetIEAction: <init>(String name): indentEngine=" + indentEngine.getName());
    }
    
    public TestSetIEAction(Element node) {
        super(node);
        indentEngine = findIndentEngine(node.getAttribute(INDENT_ENGINE));
	System.err.println("TestSetIEAction: <init>(Element node): indentEngine=" + indentEngine);
	System.err.println("TestSetIEAction: <init>(Element node): indentEngine=" + indentEngine.getName());
    }
    
    public Element toXML(Element node) {
        node = super.toXML(node);
        
        node.setAttribute(INDENT_ENGINE, getIndentEngine().getName());
        return node;
    }
    
    public void perform() {
        Scheduler.getDefault().addTask(new Thread() {
            public void run() {
                IndentEngine toSet = getIndentEngine();
                
                if (toSet == null) {
                    System.err.println("TestSetIEAction: perform: Trying to set null indent engine!");
                    return;
                }
                
                EditorKit kit = Main.editor.getUI().getEditorKit(Main.editor);
                
                if (kit == null) {
                    System.err.println("TestSetIEAction: perform: kit == null!");
                    return;
                }
                
                Class kitClass = kit.getClass();
		System.err.println("TestSetIEAction: perform: kitClass= " + kitClass);
		
		BaseOptions options = BaseOptions.getOptions(kitClass);
		
		if (options != null) {
		    options.setIndentEngine(toSet);
		    System.err.println("TestSetIEAction: perform: options.getIndentEngine()= " + options.getIndentEngine());
		    System.err.println("TestSetIEAction: perform: options.getIndentEngine().getName()= " + options.getIndentEngine().getName());
		} else {
	            System.err.println("TestSetIEAction: perform kit class " + kitClass + " not found.");
		}
		
                System.err.println("TestSetIEAction: perform: I was not able to find proper options for kit class: " + kitClass);*/
            }
        });
    }
    
    /** Getter for property IndentEngine.
     * @return Value of property IndentEngine.
     */
    public IndentEngine getIndentEngine() {
        System.err.println("TestSetIEAction( " + this + " ): getIndentEngine: indentEngine=" + indentEngine);
        return indentEngine;
    }
    
    /** Setter for property IndentEngine.
     * @param IndentEngine New value of property IndentEngine.
     */
    public void setIndentEngine(IndentEngine indentEngine) {
        IndentEngine old = this.indentEngine;
        
        this.indentEngine = indentEngine;
        
        firePropertyChange (INDENT_ENGINE, old, indentEngine);
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

}
