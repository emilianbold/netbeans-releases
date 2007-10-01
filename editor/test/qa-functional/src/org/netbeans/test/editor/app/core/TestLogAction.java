/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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

