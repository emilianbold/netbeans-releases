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
package org.netbeans.spi.project;

import java.io.File;
import java.io.IOException;
import org.netbeans.api.project.Project;

/**
 * Project Copy Operation. Allows to gather information necessary for project
 * copy and also provides callbacks to the project type to handle special
 * checkpoints during the copy process.
 *
 * An implementation of this interface may be registered in the project's lookup to support
 * copy operation in the following cases:
 * <ul>
 *     <li>The project type wants to use
 * <a href="@org-netbeans-modules-projectuiapi@/org/netbeans/spi/project/ui/support/DefaultProjectOperations.html"><code>DefaultProjectOperations</code></a>
 *         to perform the copy operation.
 *    </li>
 *    <li>If this project may be part of of a compound project (like EJB project is a part of a J2EE project),
 *        and the compound project wants to copy all the sub-projects.
 *    </li>
 * </ul>
 *
 * The project type is not required to put an implementation of this interface into the project's
 * lookup if the above two cases should not be supported.
 *
 * @author Jan Lahoda
 * @since 1.7
 */
public interface CopyOperationImplementation extends DataFilesProviderImplementation {
    
    /**Pre-copy notification. The exact meaning is left on the project implementors, but
     * typically this means to undeloy the application and remove all artifacts
     * created by the build project.
     *
     * @throws IOException if an I/O operation fails.
     */
    public void notifyCopying() throws IOException;
    
    /**Notification that the copy operation has finished. Is supposed to fix the
     * newly created (copied) project into the correct state (including changing its display name
     * to nueName). Should be called on both original and newly created project (in this order).
     *
     * @param original <code>null</code> when called on the original project, the original project when called on the new project
     * @param originalPath the project folder of the original project (for consistency
     *                     with MoveOperationImplementation.notifyMoved)
     * @param nueName new name for the newly created project.
     *
     * @throws IOException if an I/O operation fails.
     */
    public void notifyCopied(Project original, File originalPath, String nueName)  throws IOException;
    
}
