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

package com.sun.rave.designtime.ext.componentgroup;

import java.awt.Color;

/**
 * <p>Represents a group of components that are called out visually in the designer.
 * Models a virtual form, for instance.</p>
 * @author mbohm
 */
public interface ComponentGroup {
   /**
    * <p>Get the group's name. This is used in design context data keys associated with component group colors.</p>
    */
   String getName();
   
   /**
    * <p>Get the color associated with the group.</p>
    */ 
   Color getColor();
   
   /**
    * <p>Set the color associated with the group.</p>
    */ 
   void setColor(Color color);
   
   /**
    * <p>Get the label for the group's legend entry.</p>
    */ 
   String getLegendEntryLabel();
   
   /**
    * <p>Get the various component subsets in this group. For instance, a virtual form's participants would be one of the subsets.</p>
    */ 
   ComponentSubset[] getComponentSubsets();
}
