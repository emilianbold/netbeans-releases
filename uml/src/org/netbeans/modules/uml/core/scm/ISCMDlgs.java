/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
