/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package com.netbeans.enterprise.modules.db.util;

import javax.swing.JTextField;
import java.awt.Toolkit;

public class ValidableTextField extends JTextField
{
	private TextFieldValidator validator = null;
	
        static final long serialVersionUID =3686208002682293243L;
	public ValidableTextField(TextFieldValidator val)
	{
		super();
		setValidator(val);
	}
	
	public TextFieldValidator getValidator()
	{
		return validator;
	}
	
	public void setValidator(TextFieldValidator val)
	{
		validator = val;
	}

	protected void reflectInvalidValue(String oldval, String newval)
	{
		setText(oldval);
		Toolkit.getDefaultToolkit().beep();
	}

	public void replaceSelection(String s) 
	{
		String oldText = getText();
		super.replaceSelection(s);
		if (validator != null && !validator.accepts(getText())) {
			reflectInvalidValue(oldText, s);
		}
	}
}
/*
 * <<Log>>
 *  4    Gandalf   1.3         11/27/99 Patrik Knakal   
 *  3    Gandalf   1.2         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  2    Gandalf   1.1         5/21/99  Slavek Psenicka new version
 *  1    Gandalf   1.0         5/14/99  Slavek Psenicka 
 * $
 */
