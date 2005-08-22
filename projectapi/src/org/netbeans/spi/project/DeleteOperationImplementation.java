/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.spi.project;

import java.io.IOException;

/**
 * Project Delete Operation. Allows to gather information necessary for project
 * delete and also provides callbacks to the project type to handle special
 * checkpoints during the delete.
 *
 * An implementation of this interface may be registered in the project's lookup to support
 * delete operation in the following cases:
 * <ul>
 *     <li>The project type wants to use the {@link org.netbeans.spi.project.ui.support.DefaultProjectOperationsImplementation}
 *         to perform the delete operation.
 *    </li>
 *    <li>If this project may be part of of a compound project (like EJB project is a part of a J2EE project),
 *        and the compound project wants to delete all the sub-projects.
 *    </li>
 * </ul>
 *
 * The project type is not required to put an implementation of this interface into the project's
 * lookup if the above two cases should not be supported.
 *
 * @author Jan Lahoda
 * @since 1.7
 */
public interface DeleteOperationImplementation extends DataFilesProviderImplementation {
    
    /**Pre-delete notification. The exact meaning is left on the project implementors, but
     * typically this means to undeloy the application and remove all artifacts
     * created by the build project.
     *
     * @throws IOException if an I/O operation fails.
     */
    public void notifyDeleting() throws IOException;
    
    /**Notification that the delete operation has finished. Is supposed to perform
     * final cleanup and to call {@link ProjectState#notifyDeleted}.
     *
     * @throws IOException if an I/O operation fails.
     */
    public void notifyDeleted() throws IOException;

}
