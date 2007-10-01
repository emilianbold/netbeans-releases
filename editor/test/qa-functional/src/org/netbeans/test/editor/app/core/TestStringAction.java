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
import java.util.Vector;
import org.netbeans.test.editor.app.gui.*;
import org.netbeans.test.editor.app.core.TestAction;
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
public class TestStringAction extends TestAction {
    
    public static final String STRINGED_NAME="default-typed";
    public static final String STRING="String";
    private String string;
    
    public TestStringAction(int num) {
        this("string"+Integer.toString(num),"");
    }
    
    public TestStringAction(int num, String string) {
        this("string"+Integer.toString(num),string);
    }
    
    /** Creates new TestLogAction */
    public TestStringAction(String name, String string) {
        super(name);
        setString(string);
    }
    
    public TestStringAction(Element node) {
        super(node);
        setString(ParsingUtils.fromSafeString(node.getAttribute(STRING)));
    }
    
    public Element toXML(Element node) {
        node = super.toXML(node);
        node.setAttribute(STRING, ParsingUtils.toSafeString(getString()));
        return node;
    }
    
    public void fromXML(Element node) throws BadPropertyNameException {
        super.fromXML(node);
        setString(ParsingUtils.fromSafeString(node.getAttribute(STRING)));
    }
    
    public Properties getProperties() {
        Properties ret=super.getProperties();
        ret.put(STRING, new StringProperty(string));
        return ret;
    }
    
    public Object getProperty(String name) throws BadPropertyNameException {
        if (name.compareTo(STRING) == 0) {
            return new StringProperty(string);
        } else {
            return super.getProperty(name);
        }
    }
    
    public void setProperty(String name, Object value)  throws BadPropertyNameException {
        if (name.compareTo(STRING) == 0) {
            setString(((StringProperty)value).getProperty());
        } else {
            super.setProperty(name, value);
        }
    }
    
    public void setString(String value) {
        String oldValue = string;
        string = value;
        firePropertyChange(STRING, oldValue, string);
    }
    
    public String getString() {
        return string;
    }
    
    public static TestAction[] generate(Vector acts) { //only "default-typed" will be "stringed" together
        ArrayList ret=new ArrayList();
        TestAction ta;
        TestStringAction tsa;
        StringBuffer sb=null;
        String com;
        boolean logging=false;
        
        for (int i=0;i < acts.size();i++) {
            ta=(TestAction)(acts.get(i));
            if (ta instanceof TestLogAction && ta.getName().compareTo(STRINGED_NAME) == 0) {
                com=((TestLogAction)ta).getCommand();
                if (!(com.compareTo("\0A") == 0 || com.compareTo("\0C") == 0)) { //break lines aren't text
                    if (!logging) {
                        sb=new StringBuffer(com);
                        logging=true;
                    } else {
                        sb.append(com);
                    }
                }
            } else {
                if (logging) {
                    logging=false;
                    if (sb.length() > 0)
                        ret.add(new TestStringAction(getNameCounter(),sb.toString()));
                }
                ret.add(ta);
            }
        }
        if (logging) {
            ret.add(new TestStringAction(getNameCounter(),sb.toString()));
        }
        return (TestAction[])(ret.toArray(new TestAction[] {}));
    }
    
    public void perform() {
        isPerforming=true;
        getLogger().performAction(this);
        isPerforming=false;
    }
    
    public void stop() {
        getLogger().stopPerforming();
    }
}
