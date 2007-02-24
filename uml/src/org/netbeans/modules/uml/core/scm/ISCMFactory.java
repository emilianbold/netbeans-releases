/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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
 * ISCMFactory.java
 *
 * Created on July 13, 2004, 12:03 PM
 */

package org.netbeans.modules.uml.core.scm;

import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.IFacility;

/**
 * The SCM facility that plugs into the facilitiy manager.
 *
 * @author  Trey Spiva
 */
public interface ISCMFactory extends IFacility
{
   /**
    * Retrieves the ISCMTool by name. The name matches the value in the
    * FacilityConfig.etc file.
    *
    * @param toolName The name of the tool to retrieve.
    * @return The SCM Tool.  If the SCM tool name did was not in the configuration
    *         file then <code>null</code> will be returned.
    */
	public ISCMTool retrieveSCMTool( String toolName);
}
