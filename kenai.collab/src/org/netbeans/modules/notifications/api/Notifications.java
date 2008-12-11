/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.notifications.api;

import java.util.Collections;
import java.util.SortedSet;
import java.util.TreeSet;
import org.netbeans.modules.notifications.spi.Notification;

/**
 *
 * @author Jan Becicka
 */
public class Notifications {

    private static Notifications instance;

    //TODO: should not be public
    public final SortedSet<Notification> notifications;
    private Notifications() {
        notifications = Collections.synchronizedSortedSet(new TreeSet<Notification>());
    }

    /**
     * singleton instance
     * @return
     */
    public static synchronized Notifications getDefault() {
        if (instance==null)
            instance = new Notifications();
        return instance;
    }

    /**
     * adds notification to pool
     * @param notification
     * @return
     */
    public boolean add(Notification notification) {
        return notifications.add(notification);
    }

    /**
     * removes notification from pool
     * @param notification
     * @return
     */
    public boolean remove(Notification notification) {
        return notifications.remove(notification);
    }

    /**
     * Return Notification with highest priority
     * @return
     */
    public Notification top() {
        return notifications.first();
    }

}
