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

import java.util.*;

import org.netbeans.modules.collab.channel.filesharing.FilesharingConstants;
import org.netbeans.modules.collab.core.Debug;


/**
 * CollabQueue
 *
 * @author Ayub Khan, ayub.khan@sun.com
 * @version 1.0
 */
public class RegionQueue extends Object implements FilesharingConstants {
    ////////////////////////////////////////////////////////////////////////////
    // Class variables
    ////////////////////////////////////////////////////////////////////////////
    public static final long MULTI_FACTOR = 1;

    ////////////////////////////////////////////////////////////////////////////
    // Instance variables
    ////////////////////////////////////////////////////////////////////////////

    /* queue Items */
    private Vector qItems = new Vector();

    /* valid */
    private boolean valid = false;
    private RegionQueueWorkerThread worker = null;

    /* collabFileHandler */
    private CollabFileHandler collabFileHandler = null;

    /**
         *
         *
         */
    public RegionQueue(CollabFileHandler collabFileHandler) {
        super();
        this.collabFileHandler = collabFileHandler;
    }

    ////////////////////////////////////////////////////////////////////////////
    // methods
    ////////////////////////////////////////////////////////////////////////////

    /**
         * addItem
         *
         * @param item
         */
    public void addItem(RegionQueueItem item) {
        qItems.add(item);

        //long previousItemSize=item.getSize();
        if ((worker == null) && (qItems.size() > 0)) {
            worker = new RegionQueueWorkerThread(this, collabFileHandler);

            long delay = CREATELOCK_TIMER_START_DELAY;
            long rate = CREATELOCK_TIMER_RATE;
            Debug.log(
                this,
                "CollabQueue, item: " + item.getBeginLine() + //NoI18n
                " scheduling for: " + delay + " with rate: " + rate
            ); //NoI18n			
            worker.scheduleAtFixedRate(delay, rate);
        } else {
            worker.incrementDelay(CREATELOCK_NEWITEM_INCREMENT_DELAY);
        }
    }

    /**
         * getItem
         *
         * @return QueueItem
         */
    public RegionQueueItem getItem() {
        if ((qItems == null) || (qItems.size() == 0)) {
            return null;
        }

        RegionQueueItem item = (RegionQueueItem) qItems.firstElement();

        //qItems.removeElementAt(0);
        return item;
    }

    /**
         * removeItem
         *
         * @return status
         */
    public boolean removeItem(RegionQueueItem item) {
        Debug.log("RegionQueue", "RegionQueueWorkerThread, cancel: " + item.getBeginLine()); //NoI18n				

        boolean status = qItems.removeElement(item);

        if (qItems.size() == 0) {
            worker.cancel();
            worker = null;
        }

        return status;
    }
}
