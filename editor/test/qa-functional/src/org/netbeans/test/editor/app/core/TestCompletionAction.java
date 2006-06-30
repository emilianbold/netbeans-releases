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

