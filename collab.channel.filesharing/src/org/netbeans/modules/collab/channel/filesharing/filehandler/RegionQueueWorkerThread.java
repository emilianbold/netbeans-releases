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

import com.sun.collablet.CollabException;

import org.openide.*;

import java.util.*;

import org.netbeans.modules.collab.channel.filesharing.FilesharingConstants;
import org.netbeans.modules.collab.core.Debug;


/**
 *
 * @author  Ayub Khan ayub.khan@sun.com
 */
public class RegionQueueWorkerThread extends TimerTask implements FilesharingConstants {
    ////////////////////////////////////////////////////////////////////////////
    // Instance variables
    ////////////////////////////////////////////////////////////////////////////

    /* RegionQueue */
    RegionQueue queue = null;

    /* interval count, initially set to value resolved through getInterval() */
    protected long delayToProcess = getInterval();

    /* collabFileHandler */
    private CollabFileHandlerSupport collabFileHandler = null;

    /**
     *
     *
     */
    public RegionQueueWorkerThread(RegionQueue queue, CollabFileHandler collabFileHandler) {
        super();
        this.queue = queue;
        this.collabFileHandler = (CollabFileHandlerSupport) collabFileHandler;
    }

    ////////////////////////////////////////////////////////////////////////////
    // methods
    ////////////////////////////////////////////////////////////////////////////                       

    /**
     * schedule
     *
     * @param delay
     */
    public void schedule(long delay) {
        collabFileHandler.getContext().schedule(this, delay);
    }

    /**
     * scheduleAtFixedRate
     *
     * @param delay
     * @param rate
     */
    public void scheduleAtFixedRate(long delay, long rate) {
        collabFileHandler.getContext().scheduleAtFixedRate(this, delay, rate);
    }

    /**
     * run
     *
     */
    public void run() {
        Debug.log("SendFileHandler", "RegionQueueWorkerThread, run"); //NoI18n

        if (!isReady()) {
            return;
        }

        try {
            RegionQueueItem lineitem = null;
            int beginLine = Integer.MAX_VALUE;
            int endLine = 0;
            int endOffsetCorrection = 0;
            int count = 0;

            while ((lineitem = queue.getItem()) != null) {
                int begin = lineitem.getBeginLine();
                int end = lineitem.getEndLine();
                endOffsetCorrection = lineitem.getEndOffsetCorrection();

                if (begin < beginLine) {
                    beginLine = begin;
                }

                if (end > endLine) {
                    endLine = end;
                }

                queue.removeItem(lineitem);
                count++;
            }

            Debug.log(
                "SendFileHandler",
                "RegionQueueWorkerThread, " + "createNewRegion beginLine: " + beginLine + " endLine: " + endLine
            ); //NoI18n

            if (count == 1) {
                collabFileHandler.createNewRegion(beginLine, endLine, endOffsetCorrection, true);
            } else {
                collabFileHandler.createNewRegion(beginLine, endLine, 0, true);
            }
        } catch (CollabException ce) {
            ErrorManager.getDefault().notify(ce);
        }
    }

    /**
     * cancel
     *
     */
    public boolean cancel() {
        boolean status = super.cancel();

        return status;
    }

    /**
     * test if region is ready to unlock (ready for removal)
     *
     * @return true if region is ready to unlock (ready for removal)
     */
    public boolean isReady() {
        delayToProcess -= (CREATELOCK_NEWITEM_INCREMENT_DELAY * 2);

        if (delayToProcess <= 0) {
            delayToProcess = getInterval();

            return true;
        }

        return false;
    }

    /**
     * incrementDelay
     *
     * param delayIncrement
     */
    public void incrementDelay(long delayIncrement) {
        delayToProcess += delayIncrement;
    }

    /**
     * getter for region unlock interval
     *
     * @return interval
     */
    public long getInterval() {
        return CREATELOCK_NEWITEM_INCREMENT_DELAY;
    }
}
