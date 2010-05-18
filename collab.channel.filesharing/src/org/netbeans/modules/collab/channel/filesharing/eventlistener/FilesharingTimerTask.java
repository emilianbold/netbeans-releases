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
package org.netbeans.modules.collab.channel.filesharing.eventlistener;

import org.netbeans.modules.collab.channel.filesharing.FilesharingConstants;
import org.netbeans.modules.collab.channel.filesharing.FilesharingContext;
import org.netbeans.modules.collab.channel.filesharing.mdc.CollabContext;
import org.netbeans.modules.collab.channel.filesharing.mdc.CollabEvent;
import org.netbeans.modules.collab.channel.filesharing.mdc.EventNotifier;
import org.netbeans.modules.collab.channel.filesharing.mdc.eventlistener.CollabTimerTask;
import org.netbeans.modules.collab.core.Debug;


/**
 *
 * @author  Owner
 */
public class FilesharingTimerTask extends CollabTimerTask implements FilesharingConstants {
    /**
     *
     *
     */
    public FilesharingTimerTask(EventNotifier eventNotifier, CollabEvent event, CollabContext context) {
        super(eventNotifier, event, context);
    }

    ////////////////////////////////////////////////////////////////////////////
    // methods
    ////////////////////////////////////////////////////////////////////////////                       

    /**
     * run
     *
     */
    public void run() {
        getEventNotifier().notify(getEvent());
    }

    /**
     * schedule
     *
     * @param delay
     */
    public void schedule(long delay) {
        try {
            ((FilesharingContext) getContext()).getTimer().schedule(this, delay);
        } catch (java.lang.IllegalStateException ise) {
            try {
                //force creation of timer
                ((FilesharingContext) getContext()).getTimer(true).schedule(this, delay);
            } catch (java.lang.Throwable th) {
                Debug.log(
                    "CollabFileHandlerSupport", //NoI18n
                    "FilesharingTimerTask, timer null, cannot schedule task for delay: " + //NoI18n
                    delay
                );
            }
        } catch (java.lang.Throwable th) {
            try {
                //force creation of timer
                ((FilesharingContext) getContext()).getTimer(true).schedule(this, delay);
            } catch (java.lang.Throwable th1) {
                Debug.log(
                    "CollabFileHandlerSupport", //NoI18n
                    "FilesharingTimerTask, timer null, cannot schedule task for delay: " + //NoI18n
                    delay
                );
            }
        }
    }

    /**
     * scheduleAtFixedRate
     *
     * @param delay
     * @param rate
     */
    public void scheduleAtFixedRate(long delay, long rate) {
        try {
            ((FilesharingContext) getContext()).getTimer().scheduleAtFixedRate(this, delay, rate);
        } catch (java.lang.IllegalStateException ise) {
            try {
                //force creation of timer
                ((FilesharingContext) getContext()).getTimer(true).scheduleAtFixedRate(this, delay, rate);
            } catch (java.lang.Throwable th) {
                Debug.log(
                    "CollabFileHandlerSupport", //NoI18n
                    "FilesharingTimerTask, timer null, cannot scheduleAtFixedRate " + //NoI18n
                    "task for delay: " + delay + ", rate: " + rate
                ); //NoI18n
            }
        } catch (java.lang.Throwable th) {
            try {
                //force creation of timer
                ((FilesharingContext) getContext()).getTimer(true).scheduleAtFixedRate(this, delay, rate);
            } catch (java.lang.Throwable th1) {
                Debug.log(
                    "CollabFileHandlerSupport", //NoI18n
                    "FilesharingTimerTask, timer null, cannot scheduleAtFixedRate " + //NoI18n
                    "task for delay: " + delay + ", rate: " + rate
                ); //NoI18n
            }
        }
    }
}
