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



package org.netbeans.modules.uml.ui.support.viewfactorysupport;

import org.netbeans.modules.uml.ui.products.ad.viewfactory.IETGraphObjectUI;
import com.tomsawyer.editor.TSEFont;

/**
 * @author KevinM
 * Used for typecasting UI's to nodes instead of talking to ETGenericNodeUI.
 */
public interface IETNodeUI extends IETGraphObjectUI
{
   /**
    * This method returns the pixel size of the nodes grapples. The size of the
    * grapples will be scaled according to the node graph zoom level.
    */
   public int getGrappleSize();
   public void setResizable(boolean resizeable);
   public boolean resizable();   
   
   public boolean isTransparent();
   public boolean isBorderDrawn();
   public TSEFont getFont();
	public void setFont(TSEFont font);
}
