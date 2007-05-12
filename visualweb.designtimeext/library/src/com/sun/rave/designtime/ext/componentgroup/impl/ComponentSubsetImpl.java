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

/*
 * ComponentSubsetImpl.java
 *
 * Created on April 2, 2007, 11:39 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.sun.rave.designtime.ext.componentgroup.impl;

import com.sun.rave.designtime.ext.componentgroup.ComponentSubset;
import com.sun.rave.designtime.ext.componentgroup.ComponentSubset.LineType;

/**
 * <p>Implementation that represents a subset of components in a Component Group. For instance, 
 * a virtual form's participants would be one such subset.</p>
 * @author mbohm
 */
public class ComponentSubsetImpl implements ComponentSubset {
    private String name;
    private String[] members;
    private ComponentSubset.LineType lineType;
    
    
    /**
     *  <p>Constructor.</p>
     */ 
    public ComponentSubsetImpl(String name, String[] members, LineType lineType) {
        this.name = name;
        this.members = members;
        this.lineType = lineType;
    }

   /**
    * <p>Get the name of the subset. For instance, "participants".</p>
    */ 
    public String getName() {
        return this.name;
    }

   /**
    * <p>Get the server-side style, possibly qualified, ids representing 
    * components in the subset.</p>
    */ 
    public String[] getMembers() {
        return this.members;
    } 

   /**
    * <p>Get the line type used to represent the subset in the designer.</p>
    */ 
    public LineType getLineType() {
        return this.lineType;
    }
}
