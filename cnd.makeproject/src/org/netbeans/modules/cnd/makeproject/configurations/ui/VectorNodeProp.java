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
import java.io.File;
import java.util.StringTokenizer;
import java.util.Vector;
import org.netbeans.modules.cnd.makeproject.api.configurations.BooleanConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.VectorConfiguration;
import org.netbeans.modules.cnd.makeproject.ui.utils.DirectoryChooserPanel;
import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.nodes.PropertySupport;
import org.openide.util.HelpCtx;

public class VectorNodeProp extends PropertySupport {
    private VectorConfiguration vectorConfiguration;
    private BooleanConfiguration inheritValues;
    private String baseDir;
    private String[] texts;
    boolean addPathPanel;
    private HelpCtx helpCtx;
    
    public VectorNodeProp(VectorConfiguration vectorConfiguration, BooleanConfiguration inheritValues, String baseDir, String[] texts, boolean addPathPanel, HelpCtx helpCtx) {
        super(texts[0], Vector.class, texts[1], texts[2], true, true);
        this.vectorConfiguration = vectorConfiguration;
        this.inheritValues = inheritValues;
	this.baseDir = baseDir;
	this.texts = texts;
	this.addPathPanel = addPathPanel;
        this.helpCtx = helpCtx;
    }

    public String getHtmlDisplayName() {
        if (vectorConfiguration.getModified())
            return "<b>" + getDisplayName(); // NOI18N
        else
            return null;
    }
    
    public Object getValue() {
        return vectorConfiguration.getValue();
    }
    
    public void setValue(Object v) {
        vectorConfiguration.setValue((Vector)v);
    }
    
    public void restoreDefaultValue() {
        vectorConfiguration.reset();
    }
    
    public boolean supportsDefaultValue() {
        return true;
    }
    
    public boolean isDefaultValue() {
        return vectorConfiguration.getValue().size() == 0;
    }

    public PropertyEditor getPropertyEditor() {
	return new DirectoriesEditor((Vector)vectorConfiguration.getValue().clone());
    }

    /*
    public Object getValue(String attributeName) {
        if (attributeName.equals("canEditAsText")) // NOI18N
            return Boolean.FALSE;
        return super.getValue(attributeName);
    }
    */

    private class DirectoriesEditor extends PropertyEditorSupport implements ExPropertyEditor {
        private Vector value;
        private PropertyEnv env;
        
        public DirectoriesEditor(Vector value) {
            this.value = value;
        }
        
        public void setAsText(String text) {
	    Vector newList = new Vector();
	    StringTokenizer st = new StringTokenizer(text, File.pathSeparator); // NOI18N
	    while (st.hasMoreTokens()) {
		newList.add(st.nextToken());
	    }
	    setValue(newList);
        }
        
        public String getAsText() {
	    boolean addSep = false;
	    StringBuilder ret = new StringBuilder();
	    for (int i = 0; i < value.size(); i++) {
		if (addSep)
		    ret.append(File.pathSeparator);
		ret.append((String)value.elementAt(i));
		addSep = true;
	    }
	    return ret.toString();
        }
        
        public java.awt.Component getCustomEditor() {
	    String text = null;
	    if (inheritValues != null)
		text = texts[3];
            return new DirectoryChooserPanel(baseDir, (String[])value.toArray(new String[value.size()]), addPathPanel, inheritValues, text, this, env, helpCtx);
        }
        
        public boolean supportsCustomEditor() {
            return true;
        }
        
        public void attachEnv(PropertyEnv env) {
            this.env = env;
        }
    }
}
