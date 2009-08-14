/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.dlight.core.stack.dataprovider;

/**
 *
 * @author Alexander Simon
 */
public final class ThreadMapDataQuery {

    private final long timeFrom;
    private final long timeTo;
    private final boolean fullState;

    /**
     * @param timeUnit time unit.
     * @param timeFrom start time in time units.
     * @param timeTo end time in time units.
     * @param step aggregation time in time units.
     * @param fullState state aggregation. True - no aggregation by state (see FullThreadState enumeration). False - aggregate to ShortThreadState.
     * @return list threads data about all threads that alive in selected time period.
     */
    private ThreadMapDataQuery(long timeFrom, long timeTo, boolean fullState) {
        this.timeFrom = timeFrom;
        this.timeTo = timeTo;
        this.fullState = fullState;
    }

    public ThreadMapDataQuery(long timeFrom, boolean fullState) {
        this(timeFrom, Long.MAX_VALUE, fullState);
    }

    /**
     * @return start time in time units.
     */
    public long getTimeFrom() {
        return timeFrom;
    }

    /**
     * @return end time in time units.
     */
    public long getTimeTo() {
        return timeTo;
    }

    /**
     * @return state aggregation. True - no aggregation by state (see FullThreadState enumeration). False - aggregate to ShortThreadState.
     */
    public boolean isFullState() {
        return fullState;
    }
}
