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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
