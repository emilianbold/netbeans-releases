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
 * J2MEUnitProjectLookupProvider.java
 *
 * Created on 23 August 2007, 12:00
 *
 */

package org.netbeans.modules.mobility.j2meunit;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.mobility.project.J2MEProject;
import org.netbeans.modules.mobility.project.ProjectConfigurationsHelper;
import org.netbeans.spi.mobility.project.ProjectLookupProvider;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.ReferenceHelper;

/**
 *
 * @author Lukas Waldmann
 */
public class J2MEUnitProjectLookupProvider implements ProjectLookupProvider
{
    public List createLookupElements(J2MEProject project, AntProjectHelper helper, ReferenceHelper refHelper, ProjectConfigurationsHelper profHelper)
    {
        List lookups=new ArrayList();
        lookups.add(new J2MEUnitPlugin(project,helper));
        lookups.add(new UnitTestForSourceQueryImpl(helper));
        return lookups;
    }   
}
