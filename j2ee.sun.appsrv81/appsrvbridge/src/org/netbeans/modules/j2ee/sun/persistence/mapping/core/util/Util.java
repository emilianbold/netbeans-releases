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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */


/*
 * Util.java
 *
 * Created on March 13, 2000, 4:59 PM
 */

package org.netbeans.modules.j2ee.sun.persistence.mapping.core.util;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.openide.*;
import org.openide.util.*;
import org.openide.filesystems.FileObject;

import com.sun.jdo.spi.persistence.utility.StringHelper;

/** 
 *
 * @author Rochelle Raccah
 * @version %I%
 */
public class Util extends Object
{
	/** A special message used where an exception must be thrown (ie. to 
	 * veto a model change), but the user should not be shown an error.
	 */
	public static final String SUPPRESS_MESSAGE = "suppressDialog";	// NOI18N

	private static final String ICON_ROOT =
		"org/netbeans/modules/j2ee/sun/persistence/mapping/core/resources/"; //NOI18N

	private static Icon _illegalIcon;

	// ===================== icon utilities ===========================

	/** Gets the icon with the specified name.  If there is no extension, the 
	 * icon is assumed to be in the resources directory with the extension 
	 * "gif".  If the extension is provided, the entire path must also 
	 * be provided.  This uses the OpenAPI's Utilities class for the caching.
	 * @param name The name of the icon.
	 * @param description The description of the icon to be used.
	 * @return the icon.
	 */
	public static Icon getIcon (String name, String description)
	{
		String fullPath = ((name.indexOf('.') == -1) ? 
			(ICON_ROOT + name + ".gif") : name);		// NOI18N

		return new ImageIcon(Utilities.loadImage(fullPath), description);
	}

	/** Gets the illegal icon (shared by the property marking implementation) 
	 * and sets its description from the bundle.
	 * @return the icon.
	 */
	public static synchronized Icon getIllegalIcon ()
	{
		if (_illegalIcon == null)
		{
			_illegalIcon = getIcon(
				"org/openide/resources/propertysheet/invalid.gif", // NOI18N
				MappingContextFactory.getDefault().getString("ICON_illegal"));
		}

		return _illegalIcon;
	}

	// ===================== loader utilities ===========================

	/** Finds file with the same name and specified extension
	 * in the same folder as param f
	 * @param f the FileObject to be used as a base in the search.
	 * @param ext the extension of the file to be found.
	 * @return the found fileobject or null.
	 */
	public static FileObject findFile (FileObject f, String ext)
	{
		if (f != null)
		{
			String name = f.getName();
			int index = name.indexOf('$');

			if (index > 0)
				name = name.substring(0, index);

			return f.getParent().getFileObject(name, ext);
		}

		return null;
	}

	// ===================== warning utilities ===========================

	/** Accepts a warning string and displays it in a warning dialog if it is 
	 * not empty and not null.  If the user chooses to apply the action despite 
	 * the warning, or if there is no warning, returns the default,
	 * otherwise returns whichever option was chosen.
	 * @param aWarning The warning string.
	 * @param options Button options are supplied here
	 * @param helpID The id string for context sensitive help
	 * @return the first option (usually 
	 * <code>NotifyDescriptor.OK_OPTION</code> if the user chooses to apply 
	 * the action despite the warning, or if there is no warning, the option 
	 * that caused the message box to be closed otherwise.
	 */
	public static Object notifyWarning (String aWarning, Object[] options, 
		String helpID)
	{
		Object returnTest = options[0];

		if ((aWarning != null) && (aWarning.length() > 0))
		{
			DialogDescriptor descriptor = new DialogDescriptor(
				aWarning, NbBundle.getBundle(DialogDescriptor.class).
				getString("NTF_QuestionTitle"), true, options, 
				returnTest, DialogDescriptor.DEFAULT_ALIGN, 
				new HelpCtx(helpID), null);

			descriptor.setMessageType(DialogDescriptor.QUESTION_MESSAGE);
			descriptor.setClosingOptions(null);
			DialogDisplayer.getDefault().createDialog(descriptor).
				setVisible(true);
			returnTest = descriptor.getValue();
		}

		return returnTest;
	}

	/** Accepts a warning string and displays it in a warning dialog if it is 
	 * not empty and not null.  If the user chooses to apply the action despite 
	 * the warning, or if there is no warning, returns <code>true</code>, 
	 * otherwise returns <code>false</code>.  This is used for two-phased 
	 * commits.
	 * @param aWarning The warning string.
	 * @return <code>true</code> if the user chooses to apply the action 
	 * despite the warning, or if there is no warning, <code>false</code> 
	 * otherwise.
	 */
	public static boolean checkForWarning (String aWarning)
	{
		if ((aWarning != null) && (aWarning.length() > 0))
		{
			NotifyDescriptor desc = new NotifyDescriptor.Confirmation(
				 aWarning, NotifyDescriptor.OK_CANCEL_OPTION);

			return NotifyDescriptor.OK_OPTION.equals(
				DialogDisplayer.getDefault().notify(desc));
		}

		return true;
	}

	/** Accepts an exception and displays its message in an error dialog.  
	 * This method is meant primarily for ModelExceptions, but there is no 
	 * reason it cannot be used for other types.
	 * @param e The exception.
	 */
	public static void showError (Exception e)
	{
		String message = getErrorMessage(e);
	
		// filter out cancelled actions by the user which end up as
		// ModelExceptions after veto
		if (message != null)
		{
			NotifyDescriptor desc = new NotifyDescriptor.Message(
				message, NotifyDescriptor.ERROR_MESSAGE);

			DialogDisplayer.getDefault().notify(desc);
		}
	}

	/** Accepts an exception returns its message or <code>null</code> if 
	 * it should be suppressed.  If the actual message is <code>null</code>
	 * or empty, the class name of the exception is returned.
	 * This method is meant primarily for ModelExceptions, but there is no 
	 * reason it cannot be used for other types.
	 * @param e The exception.
	 */
	public static String getErrorMessage (Exception e)
	{
		String message = e.getMessage();
	
		// filter out cancelled actions by the user which end up as
		// ModelExceptions after veto
		if (!SUPPRESS_MESSAGE.equals(message))
		{
			if (!StringHelper.isEmpty(message))
				return message;
			else		// a blank message, return the exception type
				return e.getClass().getName();
		}

		return null;
	}
}
