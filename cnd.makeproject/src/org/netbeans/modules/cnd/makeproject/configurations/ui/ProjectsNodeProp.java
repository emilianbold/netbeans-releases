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
import java.util.Vector;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.VectorConfiguration;
import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.nodes.PropertySupport;

public class ProjectsNodeProp extends PropertySupport {
    private VectorConfiguration vectorConfiguration;
    Project project;
    MakeConfiguration conf;
    String baseDir;
    String[] texts;
    
    public ProjectsNodeProp(VectorConfiguration vectorConfiguration, Project project, MakeConfiguration conf, String baseDir, String[] texts) {
        super(texts[0], Vector.class, texts[1], texts[2], true, true);
        this.vectorConfiguration = vectorConfiguration;
	this.project = project;
	this.conf = conf;
	this.baseDir = baseDir;
	this.texts = texts;
    }

    @Override
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
    
    @Override
    public void restoreDefaultValue() {
        vectorConfiguration.reset();
    }
    
    @Override
    public boolean supportsDefaultValue() {
        return true;
    }
    
    @Override
    public boolean isDefaultValue() {
        return vectorConfiguration.getValue().size() == 0;
    }

    @Override
    public PropertyEditor getPropertyEditor() {
	return new DirectoriesEditor((Vector)vectorConfiguration.getValue().clone());
    }

    @Override
    public Object getValue(String attributeName) {
        if (attributeName.equals("canEditAsText")) // NOI18N
            return Boolean.FALSE;
        return super.getValue(attributeName);
    }

    private class DirectoriesEditor extends PropertyEditorSupport implements ExPropertyEditor {
        private Vector value;
        private PropertyEnv env;
        
        public DirectoriesEditor(Vector value) {
            this.value = value;
        }
        
        @Override
        public void setAsText(String text) {
        }
        
        @Override
        public String getAsText() {
	    boolean addSep = false;
	    String ret = ""; // NOI18N
	    for (int i = 0; i < value.size(); i++) {
		if (addSep)
		    ret += ", "; // NOI18N
		ret += value.elementAt(i).toString();
		addSep = true;
	    }
	    return ret;
        }
        
        @Override
        public java.awt.Component getCustomEditor() {
            return new ProjectsPanel(project, conf, baseDir, value.toArray(), this, env);
        }
        
        @Override
        public boolean supportsCustomEditor() {
            return true;
        }

        public void attachEnv(PropertyEnv env) {
            this.env = env;
        }
    }
}
