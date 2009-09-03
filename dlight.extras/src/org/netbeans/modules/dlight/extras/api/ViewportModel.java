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
package org.netbeans.modules.dlight.extras.api;

import javax.swing.event.ChangeListener;

/**
 * Model for components that have viewport or are otherwise
 * involved in viewport management.
 *
 * @author Alexey Vladykin
 */
public interface ViewportModel {

    /**
     * Returns limits. Limits is the available data range,
     * which can be viewed through this viewport.
     *
     * @return current limits
     */
    Range<Long> getLimits();

    /**
     * Sets limits.
     *
     * @param limits  new limits
     */
    void setLimits(Range<Long> limits);

    /**
     * Returns viewport. Viewport's start and
     * end are in milliseconds since session start.
     *
     * @return current viewport
     */
    Range<Long> getViewport();

    /**
     * Sets viewport. Start and/or end can be <code>null</code>
     * to keep current values.
     *
     * @param viewport  new viewport
     */
    void setViewport(Range<Long> viewport);

    /**
     * Adds change listener. <code>ChangeEvent</code>s are sent
     * when limits or viewport are modified.
     *
     * @param listener  listener to add
     */
    void addChangeListener(ChangeListener listener);

    /**
     * Removes change listener.
     *
     * @param listener  listener to remove
     */
    void removeChangeListener(ChangeListener listener);
}
