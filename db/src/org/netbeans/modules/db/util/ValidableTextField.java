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

package org.netbeans.modules.db.util;

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
