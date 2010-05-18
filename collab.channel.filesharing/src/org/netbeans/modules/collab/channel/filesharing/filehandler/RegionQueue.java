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
        Debug.log("RegionQueue", "CollabQueue, item: " + item.getBeginLine());  // NOI18N

        //long previousItemSize=item.getSize();
        if ((worker == null) && (qItems.size() > 0)) {
            worker = new RegionQueueWorkerThread(this, collabFileHandler);

            long delay = CREATELOCK_TIMER_START_DELAY;
            long rate = CREATELOCK_TIMER_RATE;
            Debug.log(
                "RegionQueue",
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
