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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.dd.api.ejb;

//
// This interface has all of the bean info accessor methods.
//
import org.netbeans.modules.j2ee.dd.api.common.CommonDDBean;
import org.netbeans.modules.j2ee.dd.api.common.DescriptionInterface;
import org.netbeans.modules.j2ee.dd.api.common.RunAs;

public interface SecurityIdentity extends CommonDDBean, DescriptionInterface {

    public static final String USE_CALLER_IDENTITY = "UseCallerIdentity";	// NOI18N
    public static final String RUN_AS = "RunAs";	// NOI18N

    public void setUseCallerIdentity(boolean value);
    
    public boolean isUseCallerIdentity();
    
    public void setRunAs(RunAs value);
    
    public RunAs getRunAs();
        
    public RunAs newRunAs();

}

