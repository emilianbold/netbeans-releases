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
import org.netbeans.test.editor.app.core.TestAction;
import org.w3c.dom.Element;
/**
 *
 * @author  ehucka
 * @version 
 */
public class TestLogAction extends TestAction {

    public static final String COMMAND="Command";
    private String command;
    
    /** Creates new TestLogAction */
    public TestLogAction(String name,String command) {
        super(name);
        setCommand(command);
    }
    
    public TestLogAction(Element node) {
        super(node);
        setCommand(Test.fromSafeString(node.getAttribute(COMMAND))); //???
    }
    
    public Element toXML(Element node) {
        node = super.toXML(node);
        
        node.setAttribute(COMMAND, Test.toSafeString(getCommand()));
        return node;
    }
    
    public void setCommand(String value) {
        String oldValue = command;
        command = value;
        firePropertyChange (COMMAND, oldValue, command);
    }
    
    public String getCommand() {
        return command;
    }
    
    public void perform() {
//        Main.editor.lock(getLogger());
        isPerforming=true;
        getLogger().performAction(this);
        Main.log("Log action: "+getName()+" was separatelly performed.");        
//        Main.editor.unlock(getLogger());
        isPerforming=false;
    }
    
    public void stop() {
    }

}
