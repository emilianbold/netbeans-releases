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

package org.netbeans.modules.j2ee.dd.api.ejb;

// 
// This interface has all of the bean info accessor methods.
// 

import org.netbeans.api.web.dd.common.CommonDDBean;
import org.netbeans.api.web.dd.common.DescriptionInterface;
import org.netbeans.api.web.dd.common.VersionNotSupportedException;

public interface ActivationConfig extends CommonDDBean, DescriptionInterface {
    
        public static final String ACTIVATION_CONFIG_PROPERTY = "ActivationConfigProperty";	// NOI18N
        
        public void setActivationConfigProperty(int index, org.netbeans.modules.j2ee.dd.api.ejb.ActivationConfigProperty value);

        public org.netbeans.modules.j2ee.dd.api.ejb.ActivationConfigProperty getActivationConfigProperty(int index);

        public void setActivationConfigProperty(org.netbeans.modules.j2ee.dd.api.ejb.ActivationConfigProperty[] value);

        public org.netbeans.modules.j2ee.dd.api.ejb.ActivationConfigProperty[] getActivationConfigProperty();
        
	public int addActivationConfigProperty(org.netbeans.modules.j2ee.dd.api.ejb.ActivationConfigProperty value);

	public int sizeActivationConfigProperty();

	public int removeActivationConfigProperty(org.netbeans.modules.j2ee.dd.api.ejb.ActivationConfigProperty value);
        
        public ActivationConfigProperty newActivationConfigProperty();

}

