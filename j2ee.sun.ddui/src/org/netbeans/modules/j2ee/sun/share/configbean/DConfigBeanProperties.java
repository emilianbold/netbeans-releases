/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.sun.share.configbean;


/**
 * Provide additional UI customizations for a DConfigBean.
 *
 * @author  gfink
 */
public interface DConfigBeanProperties {
    
    static final String PROP_DISPLAY_NAME = "displayName";
    
    /* @returns String to be used in node display of this DConfigBean */
    public String getDisplayName();
    
    /* @returns String to be used as a helpid for this DConfigBean */
    public String getHelpId();
 
}
