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

