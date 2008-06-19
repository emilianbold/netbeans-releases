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
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.test.editor.app.gui.*;
import javax.swing.text.EditorKit;
import javax.swing.JEditorPane;
import org.netbeans.modules.editor.plain.PlainKit;
import javax.swing.text.PlainDocument;
import org.openide.text.IndentEngine;
import org.openide.options.SystemOption;
import org.netbeans.modules.editor.options.BaseOptions;
import java.util.Enumeration;
import java.util.prefs.Preferences;
import org.netbeans.test.editor.app.util.Scheduler;
import javax.swing.SwingUtilities;

import javax.swing.text.Document;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.modules.editor.NbEditorDocument;
import org.netbeans.modules.editor.java.JavaIndentEngine;
import org.netbeans.modules.editor.java.JavaKit;
import org.netbeans.modules.editor.lib.SettingsConversions;
import org.netbeans.modules.editor.options.AllOptions;
import org.netbeans.modules.editor.options.BaseOptions;
import org.netbeans.test.editor.app.Main;
import org.netbeans.test.editor.app.core.properties.ArrayProperty;
import org.netbeans.test.editor.app.core.properties.BadPropertyNameException;
import org.netbeans.test.editor.app.core.properties.Properties;
import org.netbeans.test.editor.app.gui.actions.TestDeleteAction;
import org.netbeans.test.editor.app.gui.tree.ActionsCache;
import org.openide.util.Lookup;
import org.w3c.dom.Element;

/**
 *
 * @author  jlahoda
 * @version
 */
public class TestSetIEAction extends TestSetAction {
    
    /** Holds value of property IndentEngine. */
    protected IndentEngine indentEngine;
    
    public static String INDENT_ENGINE = "IndentationEngine";
    
    public TestSetIEAction(int num) {
        this("setIE"+Integer.toString(num));
    }
    
    public TestSetIEAction(String name) {
        super(name);
        MimePath mimePath = MimePath.parse("text/x-java");
        Preferences prefs = MimeLookup.getLookup(mimePath).lookup(Preferences.class);
        SettingsConversions.callFactory(prefs, mimePath, org.netbeans.modules.editor.NbEditorDocument.INDENT_ENGINE, null);
    }
    
    public TestSetIEAction(Element node) {
        super(node);
        indentEngine = findIndentEngine(node.getAttribute(INDENT_ENGINE));
    }
    
    public Element toXML(Element node) {
        node = super.toXML(node);
        
        node.setAttribute(INDENT_ENGINE, getIndentEngine().getName());
        return node;
    }
    
    public void fromXML(Element node) throws BadPropertyNameException {
        super.fromXML(node);
        indentEngine = findIndentEngine(node.getAttribute(INDENT_ENGINE));
    }
    
    public Properties getProperties() {
        Properties ret=super.getProperties();
        ret.put(INDENT_ENGINE, new ArrayProperty(indentEngine.getName(),getIndentEnginesNames()));
        return ret;
    }
    
    public Object getProperty(String name) throws BadPropertyNameException {
        if (name.compareTo(INDENT_ENGINE) == 0) {
            return new ArrayProperty(indentEngine.getName(),getIndentEnginesNames());
        } else {
            return super.getProperty(name);
        }
    }
    
    public void setProperty(String name, Object value)  throws BadPropertyNameException {
        if (name.compareTo(INDENT_ENGINE) == 0) {
            setIndentEngine(findIndentEngine(((ArrayProperty)value).getProperty()));
        } else {
            super.setProperty(name, value);
        }
    }
    
    public void perform() {
        super.perform();
        IndentEngine toSet = getIndentEngine();
        
        if (toSet == null) {
            System.err.println("TestSetIEAction: perform: Trying to set null indent engine!");
            return;
        }
        
        //        EditorKit kit = Main.frame.getEditor().getUI().getEditorKit(Main.frame.getEditor());
        EditorKit kit = Main.frame.getEditor().getEditorKit();
        
        if (kit == null) {
            System.err.println("TestSetIEAction: perform: kit == null!");
            return;
        }
        
        Class kitClass = kit.getClass();
        
        BaseOptions options = BaseOptions.getOptions(kitClass);
        
        if (options != null) {
//HACK --> workaround to Issue #25784                        
            Lookup.getDefault().lookup(toSet.getClass());
            
            options.setIndentEngine(toSet);
        } else {
            System.err.println("TestSetIEAction: perform kit class " + kitClass + " not found.");
        }
    }
    
    /** Getter for property IndentEngine.
     * @return Value of property IndentEngine.
     */
    public IndentEngine getIndentEngine() {
        return indentEngine;
    }
    
    /** Setter for property IndentEngine.
     * @param IndentEngine New value of property IndentEngine.
     */
    public void setIndentEngine(IndentEngine indentEngine) {
        IndentEngine old = this.indentEngine;
        
        this.indentEngine = indentEngine;
        
        firePropertyChange(INDENT_ENGINE, old, indentEngine);
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
    
    public static String[] getIndentEnginesNames() {
        ArrayList a=new ArrayList();
        Enumeration e=IndentEngine.indentEngines();
        while (e.hasMoreElements()) {
            IndentEngine item = (IndentEngine) e.nextElement();
            a.add(item.getName());
        }
        return (String[])(a.toArray(new String[a.size()]));
    }
    
    public static void main(String[] args) {
        TestSetIEAction act=new TestSetIEAction("action");
        String[] names=act.getIndentEnginesNames();
        IndentEngine eng;
        String id=null;
        for (int i=0;i < names.length;i++) {
            eng=act.findIndentEngine(names[i]);
            Lookup.Template tmp = new Lookup.Template(null, null, eng);
            Lookup.Item item = Lookup.getDefault().lookupItem(tmp);
            if (item != null) id = item.getId();
            System.err.println("ID for "+names[i]+": "+id);
        }
    }
}
