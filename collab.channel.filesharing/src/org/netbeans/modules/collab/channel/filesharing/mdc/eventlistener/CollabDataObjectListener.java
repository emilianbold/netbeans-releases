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
package org.netbeans.modules.collab.channel.filesharing.mdc.eventlistener;

import org.openide.*;
import org.openide.loaders.*;
import org.netbeans.modules.collab.channel.filesharing.mdc.*;
import org.netbeans.modules.collab.channel.filesharing.mdc.event.*;


/**
 *
 * @author  Owner
 */
public class CollabDataObjectListener extends Object implements OperationListener {
    ////////////////////////////////////////////////////////////////////////////
    // Instance variables
    ////////////////////////////////////////////////////////////////////////////

    /* notifier */
    protected EventNotifier eventNotifier = null;

    /** Creates a new instance of DataObjectChangeListener */
    public CollabDataObjectListener(EventNotifier eventNotifier) {
        super();
        this.eventNotifier = eventNotifier;
    }

    /** Object has been recognized by
    * {@link DataLoaderPool#findDataObject}.
    * This allows listeners
    * to attach additional cookies, etc.
    *
    * @param ev event describing the action
    */
    public void operationPostCreate(OperationEvent ev) {
        //Do nothing
    }

    /** Object has been successfully copied.
    * @param ev event describing the action
    */
    public void operationCopy(OperationEvent.Copy ev) {
        takeAction(ev);
    }

    /** Object has been successfully moved.
    * @param ev event describing the action
    */
    public void operationMove(OperationEvent.Move ev) {
        takeAction(ev);
    }

    /** Object has been successfully deleted.
    * @param ev event describing the action
    */
    public void operationDelete(OperationEvent ev) {
        //Do nothing
    }

    /** Object has been successfully renamed.
    * @param ev event describing the action
    */
    public void operationRename(OperationEvent.Rename ev) {
        //Do nothing
    }

    /** A shadow of a data object has been created.
    * @param ev event describing the action
    */
    public void operationCreateShadow(OperationEvent.Copy ev) {
        //Do nothing
    }

    /** New instance of an object has been created.
    * @param ev event describing the action
    */
    public void operationCreateFromTemplate(OperationEvent.Copy ev) {
        takeAction(ev);
    }

    /** Object has been successfully copied.
    * @param ev event describing the action
    */
    private void takeAction(Object ev) {
        // I don't have time to fix this before code freeze; instead, I'm
        // going to make sure no one can use it.  This shouldn't be a problem
        // given that we don't use this method in the filesharing channel.
        if (true) {
            throw new RuntimeException(
                "This implementation may not be " + "safe; this message is a stopgap measure.  Contact todd.fast@" +
                "sun.com for complete info"
            );
        }

        if (ev instanceof OperationEvent.Copy || ev instanceof OperationEvent.Move) {
            Object obj = null;

            if (ev instanceof OperationEvent.Copy) {
                OperationEvent.Copy ev1 = (OperationEvent.Copy) ev;
                obj = ev1.getObject();
            } else {
                OperationEvent.Move ev1 = (OperationEvent.Move) ev;
                obj = ev1.getObject();
            }

            String fileGroupName = null;

            if (obj instanceof DataObject) {
                DataObject dataObject = (DataObject) obj;
                fileGroupName = dataObject.getName();

                EventContext evContext = new EventContext(DataObjectCreated.getEventID(), dataObject);
                CollabEvent ce = new DataObjectCreated(evContext);

                try {
                    eventNotifier.notify(ce);
                } catch (Exception e) {
                    ErrorManager.getDefault().notify(e);
                }
            }
        }
    }
}
