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
 * <p>Implementation that represents a group of components that are called 
 * out visually in the designer. Models a virtual form, for instance.</p>
 * @author mbohm
 */
public class ComponentGroupImpl implements ComponentGroup {

    private String name;
    private Color color;
    private ComponentSubset[] componentSubsets;

    /**
     * <p>Constructor.</p>
     */ 
    public ComponentGroupImpl(String name, Color color, ComponentSubset[] componentSubsets) {
        this.name = name;
        this.color = color;
        this.componentSubsets = componentSubsets;
    }

   /**
    * <p>Get the group's name. This is used in design context data keys associated with component group colors.</p>
    */
    public String getName() {
        return this.name;
    }
    
   /**
    * <p>Get the color associated with the group.</p>
    */ 
    public Color getColor() {
        return this.color;
    }
    
   /**
    * <p>Set the color associated with the group.</p>
    */ 
    public void setColor(Color color) {
        this.color = color;
    }
    
   /**
    * <p>Get the label for the group's legend entry.</p>
    */ 
    public String getLegendEntryLabel() {
        //default to name
        return this.name;
    }
    
   /**
    * <p>Get the various component subsets in this group. For instance, a virtual form's participants would be one of the subsets.</p>
    */ 
    public ComponentSubset[] getComponentSubsets() {
        return this.componentSubsets;
    }
}
