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
import org.netbeans.modules.j2ee.dd.api.common.CommonDDBean;
import org.netbeans.modules.j2ee.dd.api.common.DescriptionInterface;

public interface SecurityIdentity extends CommonDDBean, DescriptionInterface {
    
    public static final String USE_CALLER_IDENTITY = "UseCallerIdentity";	// NOI18N
    public static final String RUN_AS = "RunAs";	// NOI18N
        
    public void setUseCallerIdentity(boolean value);
    
    public boolean isUseCallerIdentity();
    
    public void setRunAs(RunAs value);
    
    public RunAs getRunAs();
        
    public RunAs newRunAs();

}

