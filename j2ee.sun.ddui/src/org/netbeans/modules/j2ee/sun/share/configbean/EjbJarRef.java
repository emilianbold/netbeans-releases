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
/*
 * EjbJarRef.java
 *
 * Created on June 27, 2003, 1:32 PM
 */

package org.netbeans.modules.j2ee.sun.share.configbean;

import java.util.ArrayList;
import java.util.Collection;

import javax.enterprise.deploy.model.DDBean;
import javax.enterprise.deploy.spi.exceptions.ConfigurationException;


/**
 *
 * @author Peter Williams
 */
public class EjbJarRef extends BaseModuleRef {

    /** -----------------------------------------------------------------------
     * Initialization
     */

    /** Creates new EjbJarRef 
     */
    public EjbJarRef() {
    }

    public String getHelpId() {
        return "AS_CFG_ModuleRef";                                  //NOI18N
    }

    Collection getSnippets() {
        // FIXME - create & initialize S2B representative here - may be nothing to do
        return new ArrayList();
    }

    boolean loadFromPlanFile(SunONEDeploymentConfiguration config) {
        return false;
    }

    /** Getter for customizer title fragment property
     * @return String fragment for use in customizer title
     *
     */
    public String getTitleFragment() {
        return bundle.getString("LBL_EjbTitleFragment"); // NOI18N
    }
}
