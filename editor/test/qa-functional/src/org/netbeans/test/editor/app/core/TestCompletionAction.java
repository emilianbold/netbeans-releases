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
import java.util.Arrays;
import org.netbeans.editor.Utilities;
import org.netbeans.editor.ext.Completion;
import org.netbeans.editor.ext.ExtEditorUI;
import org.netbeans.test.editor.app.Main;
import org.netbeans.test.editor.app.gui.*;
import org.netbeans.test.editor.app.core.TestAction;
import org.netbeans.test.editor.app.core.properties.ArrayProperty;
import org.netbeans.test.editor.app.core.properties.BadPropertyNameException;
import org.netbeans.test.editor.app.core.properties.Properties;
import org.netbeans.test.editor.app.gui.actions.TestDeleteAction;
import org.netbeans.test.editor.app.gui.tree.ActionsCache;
import org.netbeans.test.editor.app.util.ParsingUtils;
import org.w3c.dom.Element;
/**
 *
 * @author  ehucka
 * @version
 */
public class TestCompletionAction extends TestAction {
    
    public static final String COMMAND="Command";
    private String command;
    
    public TestCompletionAction(int num) {
	this("completion"+Integer.toString(num),"completion-down");
    }
    
    /** Creates new TestLogAction */
    public TestCompletionAction(String name, String command) {
	super(name);
	setCommand(command);
    }
    
    public TestCompletionAction(Element node) {
	super(node);
	setCommand(ParsingUtils.fromSafeString(node.getAttribute(COMMAND))); //???
    }
    
    public Element toXML(Element node) {
	node = super.toXML(node);
	node.setAttribute(COMMAND, ParsingUtils.toSafeString(getCommand()));
	return node;
    }
    
    public void fromXML(Element node) throws BadPropertyNameException {
	super.fromXML(node);
	setCommand(ParsingUtils.fromSafeString(node.getAttribute(COMMAND)));
    }
    
    public Properties getProperties() {
	Properties ret=super.getProperties();
	ret.put(COMMAND, new ArrayProperty(command,getCompletionActions()));
	return ret;
    }
    
    public Object getProperty(String name) throws BadPropertyNameException {
	if (name.compareTo(COMMAND) == 0) {
	    return new ArrayProperty(command,getCompletionActions());
	} else {
	    return super.getProperty(name);
	}
    }
    
    public void setProperty(String name, Object value)  throws BadPropertyNameException {
	if (name.compareTo(COMMAND) == 0) {
	    setCommand(((ArrayProperty)(value)).getProperty());
	} else {
	    super.setProperty(name, value);
	}
    }
    
    public void setCommand(String value) {
	String oldValue = command;
	command = value;
	firePropertyChange(COMMAND, oldValue, command);
    }
    
    public String getCommand() {
	return command;
    }
    
    public void perform() {
	isPerforming=true;
	getLogger().performAction(this);
	isPerforming=false;
    }
    
    public void stop() {
    }
    
    private Completion getCompletion() {
	return ((ExtEditorUI)(Utilities.getEditorUI(Main.frame.getEditor()))).getCompletion();
    }
    
    public String[] getCompletionActions() {
	ArrayList ret=new ArrayList();
	Object[] keys=getCompletion().getJDCPopupPanel().getActionMap().keys();
	for (int i=0;i < keys.length;i++) {
	    if (((String)keys[i]).indexOf("completion") == 0 ) {
		ret.add(keys[i]);
	    }
	}
	String[] rets=(String[])(ret.toArray(new String[] {}));
	Arrays.sort(rets);
	
	return rets;
    }
}

