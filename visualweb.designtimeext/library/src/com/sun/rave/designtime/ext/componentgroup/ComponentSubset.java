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

/**
 * <p>Represents a subset of components in a Component Group. For instance, 
 * a virtual form's participants would be one such subset.</p>
 * @author mbohm
 */
public interface ComponentSubset {
   /**
    * <p>Get the name of the subset. For instance, "participants".</p>
    */ 
   String getName();
   
   /**
    * <p>Get the server-side style, possibly qualified, ids representing 
    * components in the subset.</p>
    */ 
   String[] getMembers();
   
   /**
    * <p>Represents the line type used to represent a subset in the designer.</p>
    */ 
   enum LineType {SOLID, DASHED, NONE}
   
   /**
    * <p>Get the line type used to represent the subset in the designer.</p>
    */ 
   LineType getLineType(); 
}
