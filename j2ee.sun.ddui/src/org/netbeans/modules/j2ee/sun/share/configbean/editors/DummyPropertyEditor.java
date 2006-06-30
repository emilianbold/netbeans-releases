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
/*
 * WebPropertyEditor.java
 *
 * Created on August 25, 2003, 12:23 PM
 */

package org.netbeans.modules.j2ee.sun.share.configbean.editors;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.FontMetrics;

import javax.swing.JPanel;

import java.beans.PropertyEditorSupport;

import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.explorer.propertysheet.PropertyEnv;

import org.netbeans.modules.j2ee.sun.dd.api.web.WebProperty;


//import org.openide.explorer.propertysheet.editors.*;

/**
 *
 * @author  User
 */
public class DummyPropertyEditor extends PropertyEditorSupport implements ExPropertyEditor {

	private WebProperty curValue;

	public DummyPropertyEditor() {
		curValue = null;
	}
	
	public DummyPropertyEditor(WebProperty object) {
		curValue = object;
	}
	
	public String getAsText() {
		return null;
	}

	public void setAsText(String text) {
		 throw new IllegalArgumentException();
	}

	public void setValue(Object value) {
		if(value == null) {
			curValue = null;
			return;
		}

		if(value instanceof WebProperty) {
			curValue = (WebProperty) value;
		} else if(curValue != null) {
			curValue.setDescription(value.toString());
		}
	}

	public Object getValue () {
		return curValue;
	}

	public String getJavaInitializationString() {
		return getAsText();
	}

	public String[] getTags() {
		return null;
	}

	public Component getInPlaceCustomEditor() {
		return null;
	}

	public boolean hasInPlaceCustomEditor() {
		return false;
	}

	protected String getPaintableString() {
		if(curValue == null) {
			return "(null)";
		} else {
			return curValue.toString();
		}
	}

	public boolean isPaintable() {
		return true;
	}
	
	public void paintValue(Graphics gfx, Rectangle box) {
		FontMetrics fm = gfx.getFontMetrics();
		gfx.setColor(Color.red);
		gfx.drawString(getPaintableString(), 4, (box.height - fm.getHeight()) / 2 + 1 + fm.getMaxAscent());    
	}
	
	public boolean supportsCustomEditor() {
		return true;
	}
	
    public Component getCustomEditor () {
        //return new WebPropertyEditorPanel();
		return new JPanel();
    }
	
	/** -----------------------------------------------------------------------
	 *  ExPropertyEditor support
	 */
    private PropertyEnv myPropertyEnv = null;

	public void attachEnv(PropertyEnv env) {
		myPropertyEnv = env;
    }
}
