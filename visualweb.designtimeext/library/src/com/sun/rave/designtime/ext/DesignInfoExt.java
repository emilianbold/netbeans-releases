/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package com.sun.rave.designtime.ext;

import com.sun.rave.designtime.DesignBean;
import com.sun.rave.designtime.DisplayActionSet;

/**
 * <P>The DesignInfoExt interface is an extension to DesignInfo to provide additional
 * design-time functionality for a component. DesignInfoExt itself does not extends
 * DesignInfo. So it acts as a <code>mixin</code>. </p>
 *
 * <p>The implementation class should also implement DesignInfo
 *     Ex.   public class MyComponentDesignInfo implements DesignInfo, DesignInfoExt{
 * </P>
 *
 * <p>The DsignInfoExt is meant to provide additional auxillary context actions that
 * will be displayed in the designer as component decoration. Clicking on the decoration
 * popups a menu that contains the actions provided via this interface
 * </p>
 * @author Winston Prakash
 */
public interface DesignInfoExt {
    
    /**
     * Returns the ActionSet to be shown in the right-click context menu of the
     * component decoration.
     *
     * @param designBean The DesignBean that a user has right-clicked on the decoration
     * @return An DisplayActionSet object representing a context menu to display to the user
     *         while clicking on the component decoration
     *          DisplayActionSet.smallIcon (or largeIcon) - displays as the decoration
     *          DisplayActionSet.displayActions -  Used to create the popup menu
     *          DisplayActionSet.description - Provides tooltip when user places the mouse over the decoration   
     */
    public DisplayActionSet getContextItemsExt(DesignBean designBean);
}
