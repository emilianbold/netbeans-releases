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
import org.netbeans.modules.bugtracking.api.Issue;

/**
 * 
 * Provides Issue Status information used by the Tasks Dashboard 
 * to appropriately render Issue status annotations (e.g. by coloring).
 * <p>
 * An implementation of this interface is not mandatory for a 
 * NetBeans bugtracking plugin. The {@link Status#SEEN} status is default for
 * all issues in such a case.
 * </p>
 * <p>
 * Also note that it is not to mandatory to honor all status values in a 
 * particular implementation - e.g. it is ok for a plugin to handle only 
 * the INCOMING_NEW, INCOMING_MODIFIED and SEEN values. In case that also 
 * outgoing changes are reflected then also CONFLICT should be taken in count.
 * </p>
 * <p>
 * Even though the status is entirely given by the particular implementation, 
 * the 
 * </p>
 * <p>
 * The precedence of Status values is expected to be the following:
 * <table border="1" cellpadding="3" cellspacing="0">
 * <tr bgcolor="#ccccff">
 * <td><b>Issue state</b></font></td>
 * <td><b>Expected Status</b></font></td>
 * </tr>
 *  <tr>
 *      <td>no changes</td>
 *      <td>SEEN</td>
 *  </tr>
 *  <tr>
 *      <td>only incoming changes</td>
 *      <td>INCOMING_NEW or INCOMING_MODIFIED</td>
 *  </tr>
  *  <tr>
 *      <td>only outgoing changes</td>
 *      <td>OUTGOING_NEW or OUTGOING_MODIFIED</td>
 *  </tr>
 *  <tr>
 *      <td>incoming and outgoing changes</td>
 *      <td>CONFLICT</td>
 *  </tr>
 * 
 * </table>
 * 
 * @author Tomas Stupka
 * @param <I> the implementation specific issue type
 */
public interface IssueStatusProvider<I> {

    /**
     * Determines the {@link Issue} status.
     */
    public enum Status {
        /**
         * The Issue appeared for the first time on the client and the user hasn't seen it yet.
         */
        INCOMING_NEW,
        /**
         * The Issue was modified (remotely) and the user hasn't seen it yet.
         */
        INCOMING_MODIFIED,
        /**
         * The Issue is new on client and haven't been submited yet.
         */
        OUTGOING_NEW,
        /**
         * There are outgoing changes in the Issue.
         */
        OUTGOING_MODIFIED,
        /**
         * There are incoming and outgoing changes at once.
         */
        CONFLICT,        
        /**
         * The user has seen the incoming changes and there haven't been any other incoming changes since then.
         */
        SEEN
    }
        
    /**
     * Issue status has changed.<br/>
     * Old value should be the status before the change, new value the Status after the change.
     */
    public static final String EVENT_STATUS_CHANGED = "issue.status_changed"; // NOI18N

    /**
     * 
     * @param issue
     * @return 
     */
    public Status getStatus(I issue);

    /**
     * Sets the information if the user has seen the incoming changes or 
     * wishes to mark them as seen (so that they aren't annotated anymore).<br/>
     * Called e.g. by the 'Mark as Seen/Unseen' action in the Tasks Dashboard or when an Issue was opened 
     * by the user.
     * 
     * <p>
     * The expected result of setting seen to <b><code>true</code></b>:
     * </p>
     * <p>
     * <table border="1" cellpadding="3" cellspacing="0">
     * <tr bgcolor="#ccccff">
     * <td><b>Status before</b></font></td>
     * <td><b>Status after </b></font></td>
     * </tr>
     *  <tr>
     *      <td>SEEN</td>
     *      <td>SEEN</td>
     *  </tr>
     *  <tr>
     *      <td>INCOMING_NEW or INCOMING_MODIFIED</td>
     *      <td>SEEN</td>
     *  </tr>
     *  <tr>
     *      <td>OUTGOING_NEW or OUTGOING_MODIFIED</td>
     *      <td>no effect</td>
     *  </tr>
     *  <tr>
     *      <td>CONFLICT</td>
     *      <td>OUTGOING_NEW or OUTGOING_MODIFIED</td>
     *  </tr>
     * 
     * </table>
     * </p>
     * 
     * <p>
     * It is up the particular implementation if and for how long the information 
     * about incoming changes will be preserved so that it can be restored after setting seen 
     * back to <b><code>false</code></b>. E.g. resulting to a status change from 
     * SEEN to INCOMMING_XXX or from OUTGOING_XXX to CONFLICT. Please note that doing so 
     * at least for a running IDE session would be considered as polite to the user.
     * </p>
     * 
     * @param issue
     * @param seen 
     */
    public void setSeenIncoming(I issue, boolean seen);
    
    /**
     * Registers a PropertyChangeListener to notify about status changes for an issue.
     * 
     * @param issue
     * @param listener
     */
    public void removePropertyChangeListener(I issue, PropertyChangeListener listener);

    /**
     * Unregisters a PropertyChangeListener.
     * 
     * @param issue
     * @param listener 
     */
    public void addPropertyChangeListener(I issue, PropertyChangeListener listener);

}
