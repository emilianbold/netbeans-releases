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



package org.netbeans.modules.uml.ui.swing.drawingarea;

import com.tomsawyer.editor.TSEDrawingPreferencesDialog;
import com.tomsawyer.editor.TSEPreferences;

import java.awt.Frame;

import javax.swing.JCheckBox;
import com.tomsawyer.editor.TSEGraphWindow;

/**
 * @author KevinM
 *
 */
public class ETDrawingPreferencesDialog extends TSEDrawingPreferencesDialog implements IETSecondaryWindow
{
	private boolean hasBeenVisible = false;
	
	//public ETDrawingPreferencesDialog(Frame arg0, String arg1, TSEPreferences arg2) //TSEPreferences(TSEGraphWindow graphWindow)
        public ETDrawingPreferencesDialog(Frame arg0, String arg1, TSEGraphWindow arg2)
	{
		super(arg0, arg1, arg2);
	}

	/*
	 * Checks the CheckBoxCode returns true if this checkbox shouldn't be allowed on the dialog.
	 */
	protected boolean excludeCheckBox(final String CheckBoxCode)
	{
		if (CheckBoxCode != null && CheckBoxCode.equals("In_Place_Editing"))
			return true;
		else
			return false;		
	}
	
	/* (non-Javadoc)
	 * @see com.tomsawyer.editor.TSEDrawingPreferencesDialog#createCheckbox(java.lang.String, java.lang.String)
	 */
	public JCheckBox createCheckbox(String arg0, String arg1)
	{
		JCheckBox checkBox = super.createCheckbox(arg0, arg1);
		
		if (excludeCheckBox(arg0))
			checkBox.setVisible(false); 
		return checkBox;
	}

	/* (non-Javadoc)
	 * @see com.tomsawyer.editor.TSEDrawingPreferencesDialog#createCheckbox(java.lang.String)
	 */
	public JCheckBox createCheckbox(String arg0)
	{	
		JCheckBox checkBox = super.createCheckbox(arg0);
		
		if (excludeCheckBox(arg0))
			checkBox.setVisible(false); 
		return checkBox;
	}

	/*
	 *  (non-Javadoc)
	 * @see java.awt.Component#setVisible(boolean)
	 */
	public void setVisible(boolean bShow)
	{
		super.setVisible(bShow);
		if (hasBeenVisible == false && bShow)
		{
			// There is a bug the bottom the dlg is grey the first time its shown.
			super.setVisible(false);
			super.setVisible(true);
			hasBeenVisible = true;
		}
	}
	
	/* (non-Javadoc)
	 * @see java.awt.Component#show()
	 */
        @SuppressWarnings("deprecation") // TODO: change to setVisible(boolean)?
	public void show()
	{
		super.show();

		// Hide the help button.
		hideHelpButton();
	}

	/*
	 * Hides the help button if its visible.
	 */
	protected void hideHelpButton()
	{
		// Hide the help button until we have help.
            /*
		if (helpButton != null && helpButton.isVisible())
		{
			helpButton.setVisible(false);
		}
             */
	}	
}
