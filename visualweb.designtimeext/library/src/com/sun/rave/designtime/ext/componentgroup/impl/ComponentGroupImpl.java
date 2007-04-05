/*
 * ComponentGroupImpl.java
 *
 * Created on April 2, 2007, 11:38 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.sun.rave.designtime.ext.componentgroup.impl;

import com.sun.rave.designtime.ext.componentgroup.ComponentGroup;
import com.sun.rave.designtime.ext.componentgroup.ComponentSubset;
import java.awt.Color;

/**
 *
 * @author mbohm
 */
public class ComponentGroupImpl implements ComponentGroup {
    private String name;
    private Color color;
    private ComponentSubset[] componentSubsets;
    //private DesignBean associatedBean;

    public ComponentGroupImpl(String name, Color color, ComponentSubset[] componentSubsets) {
        this.name = name;
        this.color = color;
        this.componentSubsets = componentSubsets;
        //this.associatedBean = associatedBean;
    }

    public String getName() {
        return this.name;
    }
    public Color getColor() {
        return this.color;
    }
    public void setColor(Color color) {
        this.color = color;
    }
    public ComponentSubset[] getComponentSubsets() {
        return this.componentSubsets;
    }
    public String getLegendEntryLabel() {
        //default to name
        return this.name;
    }
    //public DesignBean getAssociatedBean() {
    //    return this.associatedBean;
    //}
    }
