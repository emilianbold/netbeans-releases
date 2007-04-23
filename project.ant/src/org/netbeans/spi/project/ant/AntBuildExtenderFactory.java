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

package org.netbeans.spi.project.ant;

import org.netbeans.spi.project.ant.AntBuildExtenderImplementation;
import org.netbeans.api.project.ant.AntBuildExtender;
import org.netbeans.modules.project.ant.AntBuildExtenderAccessor;

/**
 * Factory class for creation of AntBuildExtender instances
 * @author mkleint
 * @since org.netbeans.modules.project.ant 1.16
 */
public final class AntBuildExtenderFactory {
    
    /** Creates a new instance of AntBuildExtenderSupport */
    private AntBuildExtenderFactory() {
    }
    
    /**
     * Create instance of {@link org.netbeans.api.project.ant.AntBuildExtender} that is
     * to be included in project's lookup.
     * @param implementation project type's spi implementation
     * @return resulting <code>AntBuildExtender</code> instance
     */
    public static AntBuildExtender createAntExtender(AntBuildExtenderImplementation implementation) {
        return AntBuildExtenderAccessor.DEFAULT.createExtender(implementation);
    }
    
}
