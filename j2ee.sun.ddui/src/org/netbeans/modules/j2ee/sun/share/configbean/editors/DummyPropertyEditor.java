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
