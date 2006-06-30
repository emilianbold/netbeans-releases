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
import javax.swing.Action;
import javax.swing.text.Keymap;
import org.netbeans.test.editor.app.Main;
import org.netbeans.test.editor.app.gui.*;
import org.netbeans.test.editor.app.core.TestAction;
import org.netbeans.test.editor.app.core.properties.ArrayProperty;
import org.netbeans.test.editor.app.core.properties.BadPropertyNameException;
import org.netbeans.test.editor.app.core.properties.Properties;
import org.netbeans.test.editor.app.core.properties.StringProperty;
import org.netbeans.test.editor.app.gui.actions.TestDeleteAction;
import org.netbeans.test.editor.app.gui.tree.ActionsCache;
import org.netbeans.test.editor.app.util.ParsingUtils;
import org.w3c.dom.Element;
/**
 *
 * @author  ehucka
 * @version
 */
public class TestLogAction extends TestAction {
    
    public static final String COMMAND="Command";
    private String command;
    
    public TestLogAction(int num) {
	this(TestStringAction.STRINGED_NAME,Integer.toString(num));
    }
    
    /** Creates new TestLogAction */
    public TestLogAction(String name,String command) {
	super(name);
	setCommand(command);
    }
    
    public TestLogAction(Element node) {
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
	Properties ret=new Properties();
	ret.put(NAME,new ArrayProperty(name,getKeyMaps()));
	ret.put(COMMAND, new StringProperty(command));
	return ret;
    }
    
    public Object getProperty(String name) throws BadPropertyNameException {
	if (name.compareTo(COMMAND) == 0) {
	    return new StringProperty(command);
	} else if (name.compareTo(NAME) == 0) {
            return new ArrayProperty(name,getKeyMaps());
        } else {
	    return super.getProperty(name);
	}
    }
    
    public void setProperty(String name, Object value)  throws BadPropertyNameException {
	if (name.compareTo(COMMAND) == 0) {
	    setCommand(((StringProperty)(value)).getProperty());
	} else if (name.compareTo(NAME) == 0) {
            setName(((ArrayProperty)value).getProperty());
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
    
    public String[] getKeyMaps() {
	ArrayList ret=new ArrayList();
	Keymap map=Main.frame.getEditor().getKeymap();
	String name,t;
	Action[] acs=map.getBoundActions();
	boolean found;
	for (int i=0;i < acs.length;i++) {
	    name=(String)(acs[i].getValue(Action.NAME));
	    if (name != null) {
		found=false;
		for (int j=0;j < ret.size();j++) {
		    t=(String)(ret.get(j));
		    if (t.compareTo(name) == 0) {
			found=true;
			break;
		    }
		}
		if (!found) {
		    ret.add(name);
		}
	    }
	}
	ret.add(TestStringAction.STRINGED_NAME);
	String[] rets=(String[])(ret.toArray(new String[] {}));
	Arrays.sort(rets);
	
	return rets;
    }
}

