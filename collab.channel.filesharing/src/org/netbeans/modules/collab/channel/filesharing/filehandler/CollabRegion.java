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

import com.sun.collablet.CollabException;

import org.openide.loaders.*;


/**
 * CollabRegion interface
 *
 * @author  Ayub Khan, ayub.khan@sun.com
 * @version 1.0
 */
public interface CollabRegion {
    ////////////////////////////////////////////////////////////////////////////
    // methods
    ////////////////////////////////////////////////////////////////////////////

    /**
     * getter for regionName
     *
     * @return regionName
     */
    public String getID();

    /**
     * getter for guarded
     *
     * @return guarded
     */
    public boolean isGuarded();

    /**
     * setter for guarded section
     *
     * @param sect
     */
    public void setGuard(Object sect);

    /**
     * getter for guarded section
     *
     * @return sect
     */
    public Object getGuard();

    /**
     * getter for regionBegin
     *
     * @return regionBegin
     */
    public int getBeginOffset();

    /**
     * getter for regionEnd
     *
     * @return regionEnd
     */
    public int getEndOffset();

    /**
     * getter for region content
     *
     * @throws CollabException
     * @return region content
     */
    public String getContent() throws CollabException;

    /**
     * test if region changed
     *
     * @return true if region changed
     */
    public boolean isChanged();

    /**
     * API to change status
     *
     * @param change, if true then isChanged() returns true
     */
    public void updateStatusChanged(boolean change);

    /**
     * test if region is ready to unlock (ready for removal)
     *
     * @return true if region is ready to unlock (ready for removal)
     */
    public boolean isReadyToUnlock();

    /**
     * set ready to unlock (ready for removal)
     *
     */
    public void setReadyToUnlock();

    /**
     * getter for region unlock interval
     *
     * @return interval
     */
    public int getInterval();

    /**
     * addAnnotation
     *
     * @param dataObject
     * @param annotation
     */
    public void addAnnotation(
        DataObject dataObject, CollabFileHandler fileHandler /*CollabRegionAnnotation annotation*/, int style,
        String annotationMessage
    ) throws CollabException;

    /**
     * removeAnnotation
     *
     */
    public void removeAnnotation() throws CollabException;

    /**
     * setValid
     *
     * @param        status                                        if false handler is invalid
     * @throws CollabException
     */
    public void setValid(boolean valid);

    /**
     * test if the filehandler is valid
     *
     * @return        true/false                                true if valid
     */
    public boolean isValid();
}
