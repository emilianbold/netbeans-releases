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

import java.util.prefs.Preferences;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.modules.editor.java.JavaKit;
import org.w3c.dom.Element;
import org.netbeans.api.editor.settings.SimpleValueNames;
import org.netbeans.test.editor.app.core.properties.BadPropertyNameException;
import org.netbeans.test.editor.app.core.properties.BooleanProperty;
import org.netbeans.test.editor.app.core.properties.Properties;
import org.netbeans.test.editor.app.util.ParsingUtils;

/**
 *
 * @author  jlahoda
 * @version
 */
public class TestSetCompletionAction extends TestSetAction {
    
    private boolean caseSensitive;
    private boolean instantSubstitution;
    private boolean naturalSort;
    
    public static String CASE_SENSITIVE = "CaseSensitive";
    public static String INSTANT_SUBSTITUTION = "InstantSubstitution";
    public static String NATURAL_SORT = "NaturalSort";
    
    /** Creates new TestSetJavaIEAction */
    public TestSetCompletionAction(int num) {
        this("setCompletion"+Integer.toString(num));
    }
    
    public TestSetCompletionAction(String name) {
        super(name);
    }
    
    public TestSetCompletionAction(Element node) {
        super(node);
        setCaseSensitive(ParsingUtils.readBoolean(node, CASE_SENSITIVE));
        setInstantSubstitution(ParsingUtils.readBoolean(node, INSTANT_SUBSTITUTION));
        setNaturalSort(ParsingUtils.readBoolean(node, NATURAL_SORT));
    }
    
    public Element toXML(Element node) {
        node = super.toXML(node);
        
        node.setAttribute(CASE_SENSITIVE, String.valueOf(getCaseSensitive()));
        node.setAttribute(INSTANT_SUBSTITUTION, String.valueOf(getInstantSubstitution()));
        node.setAttribute(NATURAL_SORT, String.valueOf(getNaturalSort()));
        return node;
    }
    
    public void fromXML(Element node) throws BadPropertyNameException {
        super.fromXML(node);
        setCaseSensitive(ParsingUtils.readBoolean(node, CASE_SENSITIVE));
        setInstantSubstitution(ParsingUtils.readBoolean(node, INSTANT_SUBSTITUTION));
        setNaturalSort(ParsingUtils.readBoolean(node, NATURAL_SORT));
    }
    
    public Properties getProperties() {
        Properties ret=super.getProperties();
        ret.put(CASE_SENSITIVE, new BooleanProperty(caseSensitive));
        ret.put(INSTANT_SUBSTITUTION, new BooleanProperty(instantSubstitution));
        ret.put(NATURAL_SORT, new BooleanProperty(naturalSort));
        return ret;
    }
    
    public Object getProperty(String name) throws BadPropertyNameException {
        if (name.compareTo(CASE_SENSITIVE) == 0) {
            return new BooleanProperty(caseSensitive);
        } else if (name.compareTo(INSTANT_SUBSTITUTION) == 0) {
            return new BooleanProperty(instantSubstitution);
        } else if (name.compareTo(NATURAL_SORT) == 0) {
            return new BooleanProperty(naturalSort);
        } else {
            return super.getProperty(name);
        }
    }
    
    public void setProperty(String name, Object value)  throws BadPropertyNameException {
        if (name.compareTo(CASE_SENSITIVE) == 0) {
            setCaseSensitive(((BooleanProperty)value).getValue());
        } else if (name.compareTo(INSTANT_SUBSTITUTION) == 0) {
            setInstantSubstitution(((BooleanProperty)value).getValue());
        } else if (name.compareTo(NATURAL_SORT) == 0) {
            setNaturalSort(((BooleanProperty)value).getValue());
        } else {
            super.setProperty(name, value);
        }
    }
    
    public boolean getCaseSensitive() {
        return caseSensitive;
    }
    
    public void setCaseSensitive(boolean value) {
        boolean old = getCaseSensitive();
        
        caseSensitive = value;
        firePropertyChange(CASE_SENSITIVE, old ? Boolean.TRUE : Boolean.FALSE, value ? Boolean.TRUE : Boolean.FALSE);
    }
    
    public boolean getInstantSubstitution() {
        return instantSubstitution;
    }
    
    public void setInstantSubstitution(boolean value) {
        boolean old = getInstantSubstitution();
        
        instantSubstitution = value;
        firePropertyChange(INSTANT_SUBSTITUTION, old ? Boolean.TRUE : Boolean.FALSE, value ? Boolean.TRUE : Boolean.FALSE);
    }
    
    public boolean getNaturalSort() {
        return naturalSort;
    }
    
    public void setNaturalSort(boolean value) {
        boolean old = getNaturalSort();
        
        naturalSort = value;
        firePropertyChange(NATURAL_SORT, old ? Boolean.TRUE : Boolean.FALSE, value ? Boolean.TRUE : Boolean.FALSE);
    }
    
    public void perform() {
        super.perform();
        
        Preferences prefs = MimeLookup.getLookup(JavaKit.JAVA_MIME_TYPE).lookup(Preferences.class);
        prefs.putBoolean(SimpleValueNames.COMPLETION_CASE_SENSITIVE, caseSensitive);
        prefs.putBoolean(SimpleValueNames.COMPLETION_INSTANT_SUBSTITUTION, instantSubstitution);
        prefs.putBoolean(SimpleValueNames.COMPLETION_NATURAL_SORT, naturalSort);
    }
}
