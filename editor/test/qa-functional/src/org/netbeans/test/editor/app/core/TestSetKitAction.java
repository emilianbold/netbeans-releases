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
import org.netbeans.modules.editor.java.JavaKit;
import org.netbeans.modules.editor.html.HTMLKit;
import org.netbeans.modules.editor.plain.PlainKit;
//import org.netbeans.modules.web.core.syntax.JSPKit;
import javax.swing.text.PlainDocument;
//import org.netbeans.modules.web.core.jsploader.JspLoader;
import org.openide.text.IndentEngine;
import org.openide.options.SystemOption;
import org.netbeans.modules.editor.options.BaseOptions;
import java.util.Enumeration;
import org.netbeans.modules.editor.options.JavaOptions;
import org.netbeans.test.editor.app.util.Scheduler;
import javax.swing.SwingUtilities;
import org.netbeans.test.editor.app.core.TestSetAction;
import org.w3c.dom.Element;
/**
 *
 * @author  ehucka
 * @version
 */
public class TestSetKitAction extends TestSetAction {
    
    private String kit;
    private String indentationEngine;
/**
 *Abbreviations
 *Font Size
 *Fonts and Colors
 *Indentation Engine
 *Key Bindings
 *Line Number
 *Tab Size
 ********************************Java + HTML************************************
 *Auto Popup of Java Completion
 *Delay of Java Completion Auto Popup
 **/
    
    public static final String KIT="Kit";
    public static final String INDENTATION_ENGINE="Indentation_engine";
    
    public static String[] editorKitsNames={"PlainKit","JavaKit","HTMLKit"/*,"JSPKit"*/};
    public static String[] kitsTypes={PlainKit.PLAIN_MIME_TYPE,
    JavaKit.JAVA_MIME_TYPE,HTMLKit.HTML_MIME_TYPE/*,JspLoader.JSP_MIME_TYPE*/};
    
    
    /** Creates new TestSetAction */
    public TestSetKitAction(int num) {
        this("set"+Integer.toString(num));
    }
    
    public TestSetKitAction(String name) {
        super(name);
        kit=editorKitsNames[0];
    }
    
    public TestSetKitAction(Element node) {
        super(node);
        kit = node.getAttribute(KIT);
    }
    
    public Element toXML(Element node) {
        node = super.toXML(node);
        
        node.setAttribute(KIT, kit);
        return node;
    }
    
    public String getIndentationEngine() {
        return indentationEngine;
    }
    
    public void setIndentationEngine(String value) {
        String oldValue = indentationEngine;
        indentationEngine = value;
        firePropertyChange (KIT, oldValue, value);
    }
    
    public void setKit(String value) {
        String oldValue = kit;
        kit = value;
        firePropertyChange (KIT, oldValue, kit);
    }
    
    public String getKit() {
        return kit;
    }
    
    public int getKitI() {
        for (int i=0;i < editorKitsNames.length;i++) {
            if (kit.compareTo(editorKitsNames[i]) == 0) {
                return i;
            }
        }
        return 0;
    }
    
    public String[] getKits() {
        return editorKitsNames;
    }
    
    public void perform() {
        Scheduler.getDefault().addTask(new Thread() {
            public void run() {
//                Main.editor.lock(getLogger());
                Main.editor.setEditorKit(getKitI());
                Main.log("Set Action "+getName()+" sets kit to "+getKit());
//                Main.editor.unlock(getLogger());
            }
        });
    }
    
    public void stop() {
    }
}
