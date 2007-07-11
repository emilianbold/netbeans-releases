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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.compapp.projects.jbi.anttasks;

import org.apache.tools.ant.Project;
import org.netbeans.modules.compapp.projects.jbi.ui.customizer.JbiProjectProperties;

/**
 *
 * @author jqian
 */
public class AntProjectHelper {

    public static String getServiceAssemblyID(Project p) {        
        String saID = p.getProperty(JbiProjectProperties.SERVICE_ASSEMBLY_ID);
        if (saID == null) { // for backward compatibility until project is updated            
            saID = p.getProperty(JbiProjectProperties.ASSEMBLY_UNIT_UUID);
        }
        return saID;
    }
    
    public static String getServiceAssemblyDescription(Project p) {    
        String saDescription = p.getProperty(JbiProjectProperties.SERVICE_ASSEMBLY_DESCRIPTION);
        if (saDescription == null) { // for backward compatibility until project is updated    
            saDescription = p.getProperty(JbiProjectProperties.ASSEMBLY_UNIT_DESCRIPTION);
        }
        return saDescription;
    }
    
    public static String getServiceUnitDescription(Project p) {    
        String saDescription = p.getProperty(JbiProjectProperties.SERVICE_UNIT_DESCRIPTION);
        if (saDescription == null) { // for backward compatibility until project is updated    
            saDescription = p.getProperty(JbiProjectProperties.APPLICATION_SUB_ASSEMBLY_DESCRIPTION);
        }
        return saDescription;
    }

}
