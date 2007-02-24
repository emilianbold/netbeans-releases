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



package org.netbeans.modules.uml.ui.controls.newdialog;

import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Enumeration;
import org.netbeans.modules.uml.ui.support.commonresources.CommonResources;

/**
 *
 * @author Trey Spiva
 */
public class NewDialogResources extends CommonResources
{

   private static final String BUNDLE_NAME = "org.netbeans.modules.uml.ui.controls.newdialog.Bundle"; //$NON-NLS-1$

   private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);

   /**
    * 
    */
   private NewDialogResources()
   {

      // TODO Auto-generated constructor stub
   }
   /**
    * @param key
    * @return
    */
   public static String getString(String key)
   {
      // TODO Auto-generated method stub
      try
      {
         return RESOURCE_BUNDLE.getString(key);
      }
      catch (MissingResourceException e)
      {
         return '!' + key + '!';
      }
   }
   
	public static String getStringKey(String value)
	{
		Enumeration enumVal = RESOURCE_BUNDLE.getKeys();
		while (enumVal.hasMoreElements())
		{
			String tempKey = (String)enumVal.nextElement();
			if (RESOURCE_BUNDLE.getString(tempKey).equals(value))
			{
				return tempKey;
			}
		}
		return "";
	}
}
