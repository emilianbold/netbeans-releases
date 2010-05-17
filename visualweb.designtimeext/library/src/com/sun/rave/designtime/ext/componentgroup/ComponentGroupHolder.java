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
