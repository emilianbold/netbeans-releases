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
package org.netbeans.modules.collab.channel.filesharing.util;

import java.util.*;

import org.netbeans.modules.collab.channel.filesharing.FilesharingContext;
import org.netbeans.modules.collab.channel.filesharing.mdc.CollabContext;
import org.netbeans.modules.collab.core.Debug;


/**
 * CollabQueue
 *
 * @author Ayub Khan, ayub.khan@sun.com
 * @version 1.0
 */
public class CollabQueue extends Object {
    ////////////////////////////////////////////////////////////////////////////
    // Class variables
    ////////////////////////////////////////////////////////////////////////////
    public static final long MULTI_FACTOR = 1;

    ////////////////////////////////////////////////////////////////////////////
    // Instance variables
    ////////////////////////////////////////////////////////////////////////////

    /* queue Items */
    private Vector qItems = new Vector();

    //private long previousItemSize = 0;

    /* valid */
    private boolean valid = false;

    /* context */
    private FilesharingContext context = null;
    private HashMap fileGroupToItemMap = new HashMap();

    /**
         *
         *
         */
    public CollabQueue(CollabContext context) {
        super();
        this.context = (FilesharingContext) context;
    }

    ////////////////////////////////////////////////////////////////////////////
    // methods
    ////////////////////////////////////////////////////////////////////////////

    /**
         * addItem
         *
         * @param item
         */
    public void addItem(QueueItem item) {
        qItems.add(item);

        //long previousItemSize=item.getSize();
        long previousItemSize = ((QueueItem) qItems.lastElement()).getSize();
        long rate = calculateDelay(item.getSize());
        QueueWorkerThread worker = new QueueWorkerThread(getContext(), item);

        synchronized (fileGroupToItemMap) {
            fileGroupToItemMap.put(item.getName(), worker);
        }

        if (qItems.size() == 1) {
            Debug.log(
                this,
                "CollabQueue, item: " + item.getName() + //NoI18n	
                " scheduling with nodelay and with rate: " + rate
            ); //NoI18n				
            worker.scheduleAtFixedRate(0, rate);
        } else {
            long delay = calculateDelay(previousItemSize);
            Debug.log(
                this,
                "CollabQueue, item: " + item.getName() + //NoI18n
                " scheduling for: " + delay + " with rate: " + rate
            ); //NoI18n			
            worker.scheduleAtFixedRate(delay, rate);
        }
    }

    /**
         * getItem
         *
         * @return QueueItem
         */
    public QueueItem getItem() {
        if ((qItems == null) || (qItems.size() == 0)) {
            return null;
        }

        QueueItem item = (QueueItem) qItems.firstElement();

        //qItems.removeElementAt(0);
        return item;
    }

    /**
         * removeItem
         *
         * @return status
         */
    public boolean removeItem(QueueItem item) {
        synchronized (fileGroupToItemMap) {
            fileGroupToItemMap.remove(item.getName());
        }

        return qItems.removeElement(item);
    }

    /**
         * removeItem
         *
         * @return status
         */
    public void removeItem(String fileGroupName) {
        Debug.log(this, "CollabQueue, removeItem: " + fileGroupName); //NoI18n	

        QueueWorkerThread wThread = null;

        synchronized (fileGroupToItemMap) {
            wThread = (QueueWorkerThread) fileGroupToItemMap.get(fileGroupName);
        }

        if (wThread != null) {
            wThread.cancel();
        }
    }

    /**
         * calculateDelay
         *
         * @return delay (in millis)
         */
    public static long calculateDelay(long itemSize) {
        /*long delay = itemSize*MULTI_FACTOR;
        if(delay>30000) //30 sec
        {
                //this limit is approx the time taken to send IMB file over
                //52KBPS line at average speed of 28KBPS (ie., (1/28000)*1000000
                delay=30000;
        }
        return delay;*/
        return 200;
    }

    public FilesharingContext getContext() {
        return this.context;
    }
}
