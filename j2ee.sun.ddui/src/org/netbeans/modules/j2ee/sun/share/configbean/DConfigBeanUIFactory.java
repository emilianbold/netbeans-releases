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

import javax.enterprise.deploy.spi.DConfigBean;

/**
 * Factory method for generating UI customizations of DConfigBeans
 *
 * @author  gfink
 */
public interface DConfigBeanUIFactory {
    
    /* @returns DConfigBeanProperties the additional UI customization of the DConfigBean */
    public DConfigBeanProperties getUICustomization(DConfigBean bean);
    
}
