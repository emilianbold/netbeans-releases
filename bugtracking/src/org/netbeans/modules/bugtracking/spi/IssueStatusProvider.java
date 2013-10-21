/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008-2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.bugtracking.spi;

import java.beans.PropertyChangeListener;

/**
 * 
 * Provides Issue Status information used by the Tasks Dashboard 
 * to appropriately render Issue status annotations (e.g. coloring).
 * <p>
 * An implementation of this interface is not mandatory for a 
 * NetBeans bugtracking plugin. The {@link Status#SEEN} status is default for
 * all issues in such a case.
 * </p>
 * <p>
 * Also note that it is not to mandatory to honor all status values in a 
 * particular implementation - e.g. it is ok for a plugin to handle only 
 * the INCOMING_NEW, INCOMING_MODIFIED and SEEN values.
 * </p>
 * @author Tomas Stupka
 * @param <I> the implementation specific issue type
 */
public interface IssueStatusProvider<I> {

    /**
     * Determines the {@link Issue} status.
     */
    public enum Status {
        /**
         * the user hasn't seen this issue yet
         */
        INCOMING_NEW,
        /**
         * the issue was modified since the issue was seen the last time
         */
        INCOMING_MODIFIED,
        /**
         * the issue is new on client and haven't been submited yet
         */
        OUTGOING_NEW,
        /**
         * there are outgoing changes in the issue
         */
        OUTGOING_MODIFIED,
        /**
         * there are incoming and outgoing changes at one
         */
        CONFLICT,        
        /**
         * the user has seen the issue and there haven't been any changes since then
         */
        SEEN
    }
        
    /**
     * Issue status has changed.
     */
    public static final String EVENT_STATUS_CHANGED = "issue.status_changed"; // NOI18N

    /**
     * 
     * @param issue
     * @return 
     */
    public Status getStatus(I issue);

    /**
     * DeterminesResets the INCOMING_XXX status
     * @param issue
     * @param seen 
     */
    public void setSeen(I issue, boolean seen);
    
    /**
     * 
     * XXX just a change listener maybe
     * @param issue
     * @param listener
     */
    public void removePropertyChangeListener(I issue, PropertyChangeListener listener);

    /**
     * 
     * XXX just a change listener maybe
     * @param issue
     * @param listener 
     */
    public void addPropertyChangeListener(I issue, PropertyChangeListener listener);

}
