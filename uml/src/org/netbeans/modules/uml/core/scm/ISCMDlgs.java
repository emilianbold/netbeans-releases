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

/*
 * ISCMDlgs.java
 *
 * Created on July 19, 2004, 8:52 AM
 */

package org.netbeans.modules.uml.core.scm;


import java.awt.Frame;

/**
 *
 * @author  Trey Spiva
 */
public interface ISCMDlgs
{
   /** Gets static text located above the listbox.*/
   public String getListBox();

   /** Sets static text located above the listbox.*/
   public void setListBox(String newVal);

   /** Gets first checkbox text.*/
   public String getCheckBox1();

   /** Sets first checkbox text. */
   public void setCheckBox1(String newVal);

   /** Gets second checkbox text. */
   public String getCheckBox2();

   /** Sets second checkbox text. */
   public void setCheckBox2(String newVal);

   /** n IDispatch to be used by SCMGuiUtils. Not Currently used. */
   public void initDlg(Object Disp);

   /** Get Column1 header text. */
   public String getColumn1();

   /** Set Column1 header text. */
   public void setColumn1(String newVal);

   /** Get Column2 header text. */
   public String getColumn2();

   /** Set Column2 header text. */
   public void setColumn2(String newVal);

   /** Get Column3 header text. */
   public String getColumn3();

   /** Set Column3 header text. */
   public void setColumn3(String newVal);

   /** Gets SCMFeatureAvailability previously passed in. */
   public ISCMFeatureAvailability getSCMTool();

   /** Sets SCMFeatureAvailability to be used by dialogs. */
   public void setSCMTool(ISCMFeatureAvailability newVal);

   /** Gets ISCMItemGroup that was passed in for processing. */
   public ISCMItemGroups getInputItems();

   /** Sets input ISCMItemGroup that contains the items to be displayed in listbox. */
   public void setInputItems(ISCMItemGroups newVal);

   /** Gets the resultant ISCMItemGroup based on user selection. */
   public ISCMItemGroups getOutputItems();

   /** Gets the options object. */
   public ISCMOptions getOptions();

   /** Sets the options object. */
   public void setOptions(ISCMOptions newVal);

   /**
    * Show the dialog.  The parent of the window will be determined by using
    * the IProxyUserInterface implementation.
    */
   public boolean showDlg();

   /**
    * Show the dialog.
    *
    * @param hWnd The parent of the SCM dialog.
    */
   public boolean showDlg(Frame hWnd);

   /**
    * Gets the dialog enumeration type.  The dialog type will be one of the
    * SCMFeatureKind values.
    *
    * @see SCMFeatureKind
    */
   public int getDlgType();

   /**
    * Sets the dialog enumeration type. The dialog type must be one of the
    * SCMFeatureKind values.
    */
   public void setDlgType(int newVal);

   /** Gets whether the dialog was cancled. */
   public boolean getCanceled();

   /** Not implemented. */
   public void setCanceled(boolean newVal);

   /** Returns whether the first dialog is checked. */
   public boolean getBox1Checked();

   /** Checks the first dialog checkbox. */
   public void setBox1Checked(boolean newVal);

   /** Returns whether the second dialog is checked. */
   public boolean getBox2Checked();

   /** Checks the second dialog checkbox. */
   public void setBox2Checked(boolean newVal);
}
