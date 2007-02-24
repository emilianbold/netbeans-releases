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
