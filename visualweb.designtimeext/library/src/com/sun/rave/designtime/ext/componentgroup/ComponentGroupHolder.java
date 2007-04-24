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

import com.sun.rave.designtime.DesignBean;
import com.sun.rave.designtime.DesignContext;
import com.sun.rave.designtime.DisplayAction;

/**
 * <p>Holds Component Groups. For instance, an implemenation exists to hold 
 * all the Component Groups that represent virtual forms on a page.</p>
 * @author mbohm
 */
public interface ComponentGroupHolder {
  
   /**
    * <p>Prefix used in context data keys associated with component group colors.</p>
    */ 
   String COLOR_KEY_PREFIX = "componentGroupColor:"; //NOI81N
   
   /**
    * <p>Legacy prefix used in context data keys associated with virtual form colors.</p>
    */ 
   String VIRTUAL_FORM_COLOR_KEY_PREFIX = "virtualFormColor:"; // NOI18N
   
   /**
    * <p>Get the name of the holder. This is used in design context data keys 
    * associated with component group colors.</p>
    */ 
   String getName();
   
   /**
    * <p>Get all the groups in the holder.</p>
    */ 
   ComponentGroup[] getComponentGroups(DesignContext dcontext);
   
   /**
    * <p>Get the tooltip of the toolbar button associated with the holder.</p>
    */ 
   String getToolTip();
   
   /**
    * <p>Get the legend label associated with the holder.</p>
    */ 
   String getLegendLabel();
   
   /**
    * <p>Get the context menu items associated with the holder.</p>
    * @param dcontext The page's design context.
    * @param dbeans The design beans that have been selected in the designer.
    */ 
   DisplayAction[] getDisplayActions(DesignContext dcontext, DesignBean[] dbeans);
}
