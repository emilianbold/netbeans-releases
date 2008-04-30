/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.core.output2.filtering;

import org.apache.tools.ant.module.spi.AntEvent;
import org.apache.tools.ant.module.spi.AntLogger;
import org.apache.tools.ant.module.spi.AntSession;


/**
 * Ant Logger which suppress (consumes) targetStarted, targetFinished events 
 * to avoid logging them in OutputWindow by StandardLogger (see #125930)
 * 
 * @author t_h
 */
public class FilterAntLogger extends AntLogger {

    /** Creates a new instance of SuppressorAntLogger */
    public FilterAntLogger() {
    }

    /**
     * We are interested in all targets.
     * 
     * @param session the relevant session
     * @return a nonempty (and non-null) list of target names; by default, {@link #NO_TARGETS}
     */
    @Override
    public String[] interestedInTargets(AntSession session) {
        return AntLogger.ALL_TARGETS;
    }

    /**
     * We are interested in every session.
     * 
     * @param session a session which is about to be start
     * @return true to receive events about it; by default, false
     */
    @Override
    public boolean interestedInSession(AntSession session) {
        return true;
    }

    /**
     * We are interested in receiving events for all scripts
     * 
     * @param session the relevant session
     * @return true to receive events for all scripts; by default, false
     */
    @Override
    public boolean interestedInAllScripts(AntSession session) {
        return true;
    }

    /**
     * Fired when a target is started. Just consume event.
     * 
     * @param event the associated event object
     */
    @Override
    public void targetStarted(AntEvent event) {
        if (event.isConsumed()) {
            return;
        }
        event.consume();
    }

    /**
     * Fired when a target is finished. Just consume event.
     * 
     * @param event the associated event object
     */
    @Override
    public void targetFinished(AntEvent event) {
        if (event.isConsumed()) {
            return;
        }
        event.consume();
    }
}

