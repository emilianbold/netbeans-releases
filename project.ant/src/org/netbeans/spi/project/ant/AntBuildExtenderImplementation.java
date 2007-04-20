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

import java.util.List;
import org.netbeans.api.project.Project;

/**
 * A project type's spi for {@link org.netbeans.api.project.ant.AntBuildExtender}'s wiring.
 * A typical setup in the project type includes:
 * <ul>
 * <li>Provide an instance of {@link org.netbeans.api.project.ant.AntBuildExtender} in project's lookup for use by 3rd
 * party modules.</<li>
 * <li>Use the new {@link org.netbeans.spi.project.support.ant.GeneratedFilesHelper#GeneratedFilesHelper(AntProjectHelper,AntBuildExtender)} constructor to
 *  create the helper for generating build related files.</<li>
 * </ul>
 * @author mkleint
 * @since org.netbeans.modules.project.ant 1.16
 */
public interface AntBuildExtenderImplementation {
    
    
    
    /**
     * A declarative list of targets that are intended by the project type to be used
     * for extensions to plug into.
     * @return list of target names
     */
    List<String> getExtensibleTargets();

    /**
     * Returns Ant Project instance.
     * @return The project that this instance of AntBuildExtenderImplementation describes
     */
    Project getOwningProject();
}
