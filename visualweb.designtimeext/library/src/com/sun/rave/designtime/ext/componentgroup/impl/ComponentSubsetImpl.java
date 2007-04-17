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
