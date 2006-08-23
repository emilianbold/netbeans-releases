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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.makeproject.configurations.ui;

import java.beans.PropertyEditor;
import java.beans.PropertyEditorSupport;
import org.netbeans.modules.cnd.makeproject.api.configurations.IntConfiguration;
import org.openide.nodes.Node;

public class IntNodeProp extends Node.Property {
    private IntConfiguration intConfiguration;
    private boolean canWrite;
    private String txt1;
    private String txt2;
    private String txt3;

    public IntNodeProp(IntConfiguration intConfiguration, boolean canWrite, String txt1, String txt2, String txt3) {
        super(Integer.class);
        this.intConfiguration = intConfiguration;
	this.canWrite = canWrite;
	this.txt1 = txt1;
	this.txt2 = txt2;
	this.txt3 = txt3;
    }

    public String getName() {
	return txt2;
    }

    public String getShortDescription() {
	return txt3;
    }
    
    public String getHtmlDisplayName() {
        if (intConfiguration.getModified())
            return "<b>" + getDisplayName(); // NOI18N
        else
            return null;
    }
    
    public Object getValue() {
        return new Integer(intConfiguration.getValue());
    }
    
    public void setValue(Object v) {
        intConfiguration.setValue((String)v);
    }
    
    public void restoreDefaultValue() {
        intConfiguration.reset();
    }
    
    public boolean supportsDefaultValue() {
        return true;
    }
    
    public boolean isDefaultValue() {
        return !intConfiguration.getModified();
    }

    public boolean canWrite() {
        return canWrite;
    }
    
    public boolean canRead() {
        return true;
    }

    public PropertyEditor getPropertyEditor() {
	return new IntEditor();
    }

    private class IntEditor extends PropertyEditorSupport {
        public String getJavaInitializationString() {
            return getAsText();
        }
        
        public String getAsText() {
            return intConfiguration.getName();
        }
        
        public void setAsText(String text) throws java.lang.IllegalArgumentException {
            setValue(text);
        }
        
        public String[] getTags() {
            return intConfiguration.getNames();
        }
    }
}
