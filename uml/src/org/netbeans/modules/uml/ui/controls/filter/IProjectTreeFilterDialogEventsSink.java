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



package org.netbeans.modules.uml.ui.controls.filter;

import org.netbeans.modules.uml.core.support.umlsupport.IResultCell;

/**
 * The sink is used to initialize the project tree and notified when the user
 * accecpts the filter settings.
 *
 * @author Trey Spiva
 */
public interface IProjectTreeFilterDialogEventsSink
{
   /**
    * The dialog is being initialized.  The listener can add new filter items to
    * the filter dialog during the initaization state.
    *
    * @param dialog The filter dialog.
    * @param cell The result.
    */
   public void onProjectTreeFilterDialogInit(IFilterDialog dialog, IResultCell cell);
   
   /**
    * The user accepted the filter dialog settings.
    * 
    * @param dialog The filter dialog.
    * @param cell The result.
    */
   public void onProjectTreeFilterDialogOKActivated(IFilterDialog dialog, IResultCell cell);
}
