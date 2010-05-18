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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
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
package org.netbeans.modules.javacard.spi;

import org.openide.util.NbBundle;

/**
 * Enum of states a Card can be in.  Not all card implementations will support
 * all states - for example, a Card which is interfaced to via HTTP is always
 * running if reachable.
 */
public enum CardState {
    /**
     * Card has been created and has never been started
     */
    NEW,
    /**
     * Card is running
     */
    RUNNING,
    /**
     * Card is running in debug mode
     */
    RUNNING_IN_DEBUG_MODE,
    /**
     * Card is preparing to resume, process start pending
     */
    BEFORE_RESUMING,
    /**
     * Card is preparing to resume, process is starting
     */
    RESUMING,
    /**
     * Card is stopped, either by stop action or unexpected process exit
     */
    NOT_RUNNING,
    /**
     * Card is preparing to start, process start pending
     */
    BEFORE_STARTING,
    /**
     * Card is preparing to start, process is starting
     */
    STARTING,
    /**
     * Preparing to stop card, background stop task not yet launched
     */
    BEFORE_STOPPING,
    /**
     * Preparing to stop card, probably waiting for processes to exit
     */
    STOPPING;

    /**
     * If true, the card is running.  False if preparing to run, resume or stop.
     * @return
     */
    public boolean isRunning() {
        return this == RUNNING_IN_DEBUG_MODE || this == RUNNING;
    }

    /**
     * If true, the card is definitely not running (previous runs cleaned up,
     * no outstanding processes)
     * @return
     */
    public boolean isNotRunning() {
        return this == NEW || this == NOT_RUNNING;
    }

    /**
     * If true, the card may or may not have an associated process, but the
     * IDE is in the process of changing its state.
     * @return
     */
    public boolean isTransitionalState() {
        return this == RESUMING || this == STARTING || this == STOPPING || 
                this == BEFORE_STARTING || this == BEFORE_STOPPING;
    }

    /**
     * The card is about to be started/stopped/resumed, but the background job
     * to do that operation has not started yet
     * @return
     */
    public boolean isPreparing() {
        return this == BEFORE_STARTING || this == BEFORE_STOPPING;
    }

    /**
     * Get a status message about what the card is doing, appropriate for
     * use in the status bar
     * @param args The name of the card
     * @return A localized status description
     */
    public String statusMessage (String args) {
        return NbBundle.getMessage(CardState.class, name(), args);
    }

    @Override
    public String toString() {
        return NbBundle.getMessage(CardState.class, name() + ".name");
    }

    public boolean isTransitionToStart() {
        return this == BEFORE_STARTING || this == STARTING;
    }

    public boolean isTransitionToStop() {
        return this == BEFORE_STOPPING || this == STOPPING;
    }
}
