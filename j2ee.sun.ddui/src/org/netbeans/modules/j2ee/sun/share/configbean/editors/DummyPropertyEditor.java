/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
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
