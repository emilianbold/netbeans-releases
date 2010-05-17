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

package org.netbeans.modules.worklist.editor.nodes;

import org.netbeans.modules.worklist.editor.utils.DisplayNameBuilder;
import org.netbeans.modules.worklist.editor.utils.StringUtils;
import org.netbeans.modules.wlm.model.api.TLocalNotification;
import org.netbeans.modules.wlm.model.api.TNotification;
import org.netbeans.modules.wlm.model.api.WLMReference;
import org.openide.nodes.Children;
import org.openide.util.Lookup;

/**
 *
 * @author anjeleevich
 */
public class LocalNotificationNode extends WLMNode<TLocalNotification> {

    private String cachedNotificationName = null;
    private Status cachedStatus = null;

    public LocalNotificationNode(TLocalNotification component, Lookup lookup) {
        this(component, Children.LEAF, lookup);
    }

    public LocalNotificationNode(TLocalNotification component, 
            Children children, Lookup lookup)
    {
        super(component, children, lookup);
        updateDisplayName();
    }

    @Override
    public void updateDisplayName() {
        TLocalNotification localNotification = getWLMComponent();
        
        WLMReference<TNotification> notificationRef = localNotification
                .getNotification();

        TNotification notification = (notificationRef == null) ? null
                : notificationRef.get();

        String notificationName = (notificationRef == null) ? null
                : notificationRef.getRefString();

        if (notificationName == null) {
            notificationName = "";
        } else {
            notificationName = notificationName.trim();
        }

        Status status = null;
        if (notificationRef == null) {
            status = Status.UNDEFINED_NOTIFICATION;
        } else if (notificationName.length() == 0) {
            status = Status.UNDEFINED_NOTIFICATION;
        } else if (notification == null) {
            status = Status.NOTIFICATION_DOES_NOT_EXISTS;
        } else {
            status = Status.OK;
        }

        if ((cachedStatus != status) || !StringUtils.equals(notificationName,
                cachedNotificationName))
        {
            cachedStatus = status;
            cachedNotificationName = notificationName;

            DisplayNameBuilder builder = new DisplayNameBuilder();

            if (status == Status.OK) {
                builder.append(notificationName);
            } else if (status == Status.UNDEFINED_NOTIFICATION) {
                builder.startColor("#888888");
                builder.append("Undefined notification");
                builder.endColor();
            } else if (status == Status.NOTIFICATION_DOES_NOT_EXISTS) {
                builder.startColor("#FF0000");
                builder.append(notificationName);
                builder.endColor();
                builder.append(" ");
                builder.startColor("#888888");
                builder.append("[Unable to resolve]");
                builder.endColor();
            }

            setDisplayName(builder);
        }
    }

    @Override
    public WLMNodeType getType() {
        return WLMNodeType.LOCAL_NOTIFICATION;
    }

    private static enum Status {
        UNDEFINED_NOTIFICATION,
        NOTIFICATION_DOES_NOT_EXISTS,
        OK;
    }
}
