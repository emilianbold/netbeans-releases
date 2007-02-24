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

import org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment;

/**
 * This is a marker interface that specifies an application designer compartment.
 *
 * @author Trey Spiva
 */
public interface IADCompartment extends ICompartment
{
   public final static int LEFT         = 1;
   public final static int CENTER       = 2;
   public final static int RIGHT        = 4;

   public final static int TOP          = 8;
   public final static int BOTTOM       = 16;

   public final static int SINGLELINE   = 32;
   public final static int END_ELLIPSIS = 64;
   
   /**
    * Sets the horizontal alignment property of the compartment.  Valid
    * values are:
    * <ul>
    *    <li>IADEditableCompartment.LEFT</li>
    *    <li>IADEditableCompartment.CENTER</li>
    *    <li>IADEditableCompartment.RIGHT</li>
    * </ul>
    * @param alignment The horizonal alignment.
    */
   public void setHorizontalAlignment(int alignment);
   
   /**
    * Retrieves the horizontal alignment property of the compartment.  Valid
    * values are:
    * <ul>
    *    <li>IADEditableCompartment.LEFT</li>
    *    <li>IADEditableCompartment.CENTER</li>
    *    <li>IADEditableCompartment.RIGHT</li>
    * </ul>
    * @return The horizonal alignment.
    */
   public int getHorizontalAlignment();
   
   /**
    * Sets the vertical alignment property of the compartment.  Valid
    * values are:
    * <ul>
    *    <li>IADEditableCompartment.TOP</li>
    *    <li>IADEditableCompartment.CENTER</li>
    *    <li>IADEditableCompartment.BOTTOM</li>
    * </ul>
    * @param alignment The vertical alignment.
    */
   public void setVerticalAlignment(int alignment);
   
   /**
    * Retrieves the vertical alignment property of the compartment.  Valid
    * values are:
    * <ul>
    *    <li>IADEditableCompartment.TOP</li>
    *    <li>IADEditableCompartment.CENTER</li>
    *    <li>IADEditableCompartment.BOTTOM</li>
    * </ul>
    * @return The vertical alignment.
    */
   public int getVerticalAlignment();
}
