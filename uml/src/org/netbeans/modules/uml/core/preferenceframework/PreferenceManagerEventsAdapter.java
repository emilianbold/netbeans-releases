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

package org.netbeans.modules.uml.core.preferenceframework;

import org.netbeans.modules.uml.core.support.umlsupport.IResultCell;
import org.netbeans.modules.uml.core.support.umlutils.IPropertyElement;

//import com.embarcadero.describe.gui.preference.framework.IPreferenceManagerEventsSink;
//import com.embarcadero.describe.umlsupport.IResultCell;
//import com.embarcadero.describe.umlutils.IPropertyElement;
//import com.embarcadero.describe.umlutils.IPropertyElements;

/**
 *
 * @author Trey Spiva
 */
public class PreferenceManagerEventsAdapter implements IPreferenceManagerEventsSink
{

   public void onPreferenceChange(String           name,
                                  IPropertyElement pElement,
                                  IResultCell      cell)
   {
   }

   public void onPreferenceAdd(String           name,
                               IPropertyElement pElement,
                               IResultCell      cell)
   {
   }

   public void onPreferenceRemove(String           name,
                                  IPropertyElement pElement,
                                  IResultCell      cell)
   {
   }

   public void onPreferencesChange(IPropertyElement[] pElements,
                                   IResultCell cell)
   {
   }

}
