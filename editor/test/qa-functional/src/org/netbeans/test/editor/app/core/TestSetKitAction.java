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
import org.netbeans.test.editor.app.Main;
import org.netbeans.test.editor.app.core.TestSetAction;
import org.netbeans.test.editor.app.core.properties.ArrayProperty;
import org.netbeans.test.editor.app.core.properties.BadPropertyNameException;
import org.netbeans.test.editor.app.core.properties.Properties;
import org.netbeans.test.editor.app.gui.actions.TestDeleteAction;
import org.netbeans.test.editor.app.gui.tree.ActionsCache;
import org.w3c.dom.Element;
/**
 *
 * @author  ehucka
 * @version
 */
public class TestSetKitAction extends TestSetAction {
    
    private String kit;
    
    public static final String KIT="Kit";
    
    public static String[] editorKitsNames={"PlainKit","JavaKit","HTMLKit"};
    public static String[] kitsTypes={PlainKit.PLAIN_MIME_TYPE,
    JavaKit.JAVA_MIME_TYPE,HTMLKit.HTML_MIME_TYPE};
    
    
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
    
    public void fromXML(Element node) throws BadPropertyNameException {
        super.fromXML(node);
        kit = node.getAttribute(KIT);
    }
    
    public Properties getProperties() {
        Properties ret=super.getProperties();
        ret.put(KIT, new ArrayProperty(kit, editorKitsNames));
        return ret;
    }
    
    public Object getProperty(String name) throws BadPropertyNameException {
        if (name.compareTo(KIT) == 0) {
            return new ArrayProperty(kit, editorKitsNames);
        } else {
            return super.getProperty(name);
        }
    }
    
    public void setProperty(String name, Object value)  throws BadPropertyNameException {
        if (name.compareTo(KIT) == 0) {
            setKit(((ArrayProperty)value).getProperty());
        } else {
            super.setProperty(name, value);
        }
    }
    
    public void setKit(String value) {
        String oldValue = kit;
        kit = value;
        firePropertyChange(KIT, oldValue, kit);
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
        super.perform();
        Main.frame.getEditor().setEditorKit(getKitI());
    }
    
    public void stop() {
    }
}
