/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
