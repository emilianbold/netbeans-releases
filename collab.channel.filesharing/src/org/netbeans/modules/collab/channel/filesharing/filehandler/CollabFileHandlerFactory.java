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
package org.netbeans.modules.collab.channel.filesharing.filehandler;


/**
 * FileHandler Factory interface
 *
 * @author Ayub Khan, ayub.khan@sun.com
 * @version 1.0
 */
public interface CollabFileHandlerFactory {
    /**
     * getter for ID
     *
     * @return        ID
     */
    public String getID();

    /**
     * getter for displayName
     *
     * @return        displayName
     */
    public String getDisplayName();

    /**
     * create FileHandler instance
     *
     * @return        CollabFileHandler
     */
    public CollabFileHandler createCollabFileHandler();

    /**
     * test if the factory object support the given mimeType
     *
     * @param mimeType
     * @return
     */
    public boolean canHandleMIMEType(String mimeType);

    /**
     * test if the factory object support the given mimeType or fileExt
     *
     * @param mimeType
     * @param fileExt
     * @return
     */
    public boolean canHandleMIMEType(String mimeType, String fileExt);

    /**
     * getter for contentType
     *
     * @return        contentType
     */
    public String getContentType();
}
