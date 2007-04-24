/*
 * ComponentGroupDesignInfo.java
 *
 * Created on April 23, 2007, 9:42 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.sun.rave.designtime.ext.componentgroup;

import com.sun.rave.designtime.DesignInfo;

/**
 *
 * @author Matt
 */
public interface ComponentGroupDesignInfo extends DesignInfo {
    
    /**
     * <p>Get the <code>ComponentGroupHolder</code> instances
     * associated with this <code>DesignInfo</code>.</p>
     */
     ComponentGroupHolder[] getComponentGroupHolders();
    
}
