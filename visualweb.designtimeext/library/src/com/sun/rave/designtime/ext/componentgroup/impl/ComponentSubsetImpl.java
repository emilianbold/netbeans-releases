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
 *
 * @author mbohm
 */
public class ComponentSubsetImpl implements ComponentSubset {
    private String name;
    private String[] members;
    private ComponentSubset.LineType lineType;
    
    public ComponentSubsetImpl(String name, String[] members, LineType lineType) {
        this.name = name;
        this.members = members;
        this.lineType = lineType;
    }

    //e.g., "participants", "submitters", "inputs", "execute", "render"
    public String getName() {
        return this.name;
    }

    //e.g., contains participants or submitters in a particular virtual form. contains inputs, executes, or renders of a particular ajax transaction.
    public String[] getMembers() {
        return this.members;
    } 

    //SOLID OR DASHED
    public LineType getLineType() {
        return this.lineType;
    }
}
