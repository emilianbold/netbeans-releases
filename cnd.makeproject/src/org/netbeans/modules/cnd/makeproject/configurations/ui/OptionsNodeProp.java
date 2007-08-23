/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.makeproject.configurations.ui;

import java.beans.PropertyEditor;
import java.beans.PropertyEditorSupport;
import java.util.StringTokenizer;
import org.netbeans.modules.cnd.makeproject.api.configurations.BooleanConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.OptionsConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.AllOptionsProvider;
import org.netbeans.modules.cnd.api.utils.CppUtils;
import org.netbeans.modules.cnd.makeproject.api.compilers.BasicCompiler;
import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.nodes.PropertySupport;

public class OptionsNodeProp extends PropertySupport {
    private OptionsConfiguration commandLineConfiguration;
    private BooleanConfiguration inheritValues;
    private AllOptionsProvider optionsProvider;
    private BasicCompiler compiler;
    private String delimiter = ""; // NOI18N
    private String txt1;
    private String txt2;
    private String txt3;
    private String[] texts;
    
    public OptionsNodeProp(OptionsConfiguration commandLineConfiguration, BooleanConfiguration inheritValues, AllOptionsProvider optionsProvider, BasicCompiler compiler, String delimiter, String[] texts) {
        super("ID", String.class, texts[0], texts[1], true, true); // NOI18N
        this.commandLineConfiguration = commandLineConfiguration;
        this.inheritValues = inheritValues;
        this.optionsProvider = optionsProvider;
        this.compiler = compiler;
	if (delimiter != null)
	    this.delimiter = delimiter;
	this.texts = texts;
    }
    
    public String getHtmlDisplayName() {
        if (commandLineConfiguration.getModified())
            return "<b>" + getDisplayName(); // NOI18N
        else
            return null;
    }
    
    public Object getValue() {
        return commandLineConfiguration.getValue();
    }
    
    public void setValue(Object v) {
	String s = CppUtils.reformatWhitespaces((String)v);
        commandLineConfiguration.setValue(s);
    }
    
    public PropertyEditor getPropertyEditor() {
        return new CommandLinePropEditor();
    }
    
    /*
    public Object getValue(String attributeName) {
        if (attributeName.equals("canEditAsText")) // NOI18N
            return Boolean.FALSE;
        else
	    return super.getValue(attributeName);
    }
    */
    
    public void restoreDefaultValue() {
        commandLineConfiguration.optionsReset();
    }
    
    public boolean supportsDefaultValue() {
        return true;
    }
    
    public boolean isDefaultValue() {
        return !commandLineConfiguration.getModified();
    }
    
    private class CommandLinePropEditor extends PropertyEditorSupport implements ExPropertyEditor {
        private PropertyEnv env;
        
        public void setAsText(String text) {
	    StringBuilder newText = new StringBuilder();
	    if (delimiter.length() > 0) {
		// Remove delimiter
		StringTokenizer st = new StringTokenizer(text, delimiter);
		while (st.hasMoreTokens()) {
		    newText.append(st.nextToken());
		}
	    }
	    else {
		newText.append(text);
	    }
	    setValue(newText.toString());
        }
        
        public String getAsText() {
	    String s = (String)getValue();
	    return CppUtils.reformatWhitespaces(s, "", delimiter); // NOI18N
        }
        
        public java.awt.Component getCustomEditor() {
	    OptionsEditorPanel commandLineEditorPanel = new OptionsEditorPanel(texts, inheritValues, this, env);
	    commandLineEditorPanel.setAllOptions(optionsProvider.getAllOptions(compiler));
	    commandLineEditorPanel.setAdditionalOptions((String)getValue());
            return commandLineEditorPanel;
        }
        
        public boolean supportsCustomEditor() {
            return true;
        }
        
        public void attachEnv(PropertyEnv env) {
            this.env = env;
        }
    }
}
