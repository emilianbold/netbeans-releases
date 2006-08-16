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
 * ResViewProvider.java
 *
 * Created on 21 July 2006, 16:25
 *
 */

package org.netbeans.modules.mobility.project.ui;

import org.netbeans.modules.mobility.project.J2MEProject;
import org.openide.nodes.Node;

/**
 *
 * @author Lukas Waldmann
 */
class ResViewProvider extends J2MEPhysicalViewProvider.ChildLookup
{   
    final private NodeCache cache;
    
    ResViewProvider(final NodeCache c)
    {
        cache=c;
    }
    
    public Node[] createNodes(J2MEProject project)
    {
        return cache.getClones(project.getConfigurationHelper().getActiveConfiguration());
    }    
}