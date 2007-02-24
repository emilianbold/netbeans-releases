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



package org.netbeans.modules.uml.ui.swing.pulldownbutton;

import javax.swing.JComponent;

/**
 *
 * @author Trey Spiva
 */
public interface IPulldownButtonInvoker
{
   /**
    * Makes the pull down visible to the user.
    *
    * @param owner The component that is associated with the pulldown.
    */
   public void showPulldown(JComponent owner);

   /**
    * Hides the pull down from the user.
    *
    * @param owner The component that is associated with the pulldown.
    */
   public void hidePulldown(JComponent owner);

   /**
    * Determines if the component is visibible to the user.
    * 
    * @return <b>true</b> if the pulldown component is visibile.
    */
   public boolean isPulldownVisible();
}
