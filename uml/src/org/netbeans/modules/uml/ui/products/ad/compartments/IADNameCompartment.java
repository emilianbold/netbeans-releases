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



package org.netbeans.modules.uml.ui.products.ad.compartments;

/**
 *
 * @author Trey Spiva
 */
public interface IADNameCompartment extends IADEditableCompartment
{
   public final static int NCBK_DRAW_JUST_NAME             = 0;
   public final static int NCBK_DRAW_RECTANGLE             = 1;
   public final static int NCBK_DRAW_ELLIPSE               = 2;
   public final static int NCBK_DRAW_3DBOX                 = 3;
   public final static int NCBK_DRAW_ELONGATED_3DBOX       = 4;
   public final static int NCBK_DRAW_BRACKETS              = 5;
   public final static int NCBK_DRAW_ROUNDED_RECTANGLE     = 6;
   public final static int NCBK_DRAW_BOX_POINTING_TO_RIGHT = 7;
   public final static int NCBK_DRAW_BORDER_TOTAL       = 8; 
      
   /**
    * Sets the compartment border style to draw around the name compartment.
    * Normally the name compartment only draws the text.  For Use cases, nodes 
    * and other objects it's nice to have the name compartment also draw the 
    * rectangle or ellipse around the name compartment.  Control that drawing 
    * behavior here.  
    * <br>
    * The valid values are:
    * <ul>
    *    <li>IADNameCompartment.NCBK_DRAW_JUST_NAME</li>
    *    <li>IADNameCompartment.NCBK_DRAW_RECTANGLE</li>
    *    <li>IADNameCompartment.NCBK_DRAW_ELLIPSE</li>
    *    <li>IADNameCompartment.NCBK_DRAW_3DBOX</li>
    *    <li>IADNameCompartment.NCBK_DRAW_ELONGATED_3DBOX</li>
    *    <li>IADNameCompartment.NCBK_DRAW_BRACKETS</li>
    *    <li>IADNameCompartment.NCBK_DRAW_ROUNDED_RECTANGLE</li>
    *    <li>IADNameCompartment.NCBK_DRAW_BOX_POINTING_TO_RIGHT</li>
    * </ul>
    */
   public void setNameCompartmentBorderKind(int value);
   
   /**
    * Retrieves the compartment border style to draw around the name compartment.
    * Normally the name compartment only draws the text.  For Use cases, nodes 
    * and other objects it's nice to have the name compartment also draw the 
    * rectangle or ellipse around the name compartment.  Control that drawing 
    * behavior here.  
    * <br>
    * The valid values are:
    * <ul>
    *    <li>IADNameCompartment.NCBK_DRAW_JUST_NAME</li>
    *    <li>IADNameCompartment.NCBK_DRAW_RECTANGLE</li>
    *    <li>IADNameCompartment.NCBK_DRAW_ELLIPSE</li>
    *    <li>IADNameCompartment.NCBK_DRAW_3DBOX</li>
    *    <li>IADNameCompartment.NCBK_DRAW_ELONGATED_3DBOX</li>
    *    <li>IADNameCompartment.NCBK_DRAW_BRACKETS</li>
    *    <li>IADNameCompartment.NCBK_DRAW_ROUNDED_RECTANGLE</li>
    *    <li>IADNameCompartment.NCBK_DRAW_BOX_POINTING_TO_RIGHT</li>
    * </ul>
    */
   public int getNameCompartmentBorderKind();
}
