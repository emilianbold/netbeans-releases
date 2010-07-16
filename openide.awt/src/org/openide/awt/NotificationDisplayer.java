/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.openide.awt;

import java.awt.event.ActionListener;
import javax.swing.Icon;
import javax.swing.JComponent;
import org.openide.awt.StatusDisplayer.Message;
import org.openide.util.Lookup;

/**
 * Creates and shows clickable notifications in the main status line.
 *
 * @since 7.6
 *
 * @author S. Aubrecht
 */
public abstract class NotificationDisplayer {

    /**
     * Priority of Notification
     */
    public static enum Priority {
        HIGH,
        NORMAL,
        LOW,
        /** Priority that shows the notification without details.
         * Details shall be shown only later, per user request.
         * @since 7.18
         */
        SILENT
    }

    /**
     * Looks for an implementation in global Lookup, if none is found then it falls
     * back to a primitive implementation which displays the notifications in main 
     * window's status line.
     * @return Default implementation.
     */
    public static NotificationDisplayer getDefault() {
        NotificationDisplayer res = Lookup.getDefault().lookup(NotificationDisplayer.class);
        if( null == res ) {
            res = new SimpleNotificationDisplayer();
        }
        return res;
    }

    /**
     * Create and show new notification with the default priority.
     * @param title Notification title. Html is not supported, any html tags will
     * be escaped.
     * @param icon Notification icon
     * @param detailsText Detailed description of the notification. If detailsAction
     * is non-null then this text will be presented as a clickable link. Html is
     * not supported, any html tags will be escaped.
     * @param detailsAction Action to invoke when user click details text or null.
     * @return New notification.
     */
    public Notification notify( String title, Icon icon,
            String detailsText, ActionListener detailsAction ) {
        return notify( title, icon, detailsText, detailsAction, Priority.NORMAL);
    }

    /**
     * Create and show new notification.
     * @param title Notification title. Html is not supported, any html tags will
     * be escaped.
     * @param icon Notification icon
     * @param detailsText Detailed description of the notification. If detailsAction
     * is non-null then this text will be presented as a clickable link. Html is
     * not supported, any html tags will be escaped.
     * @param detailsAction Action to invoke when user click details text or null.
     * @param priority Notification priority
     * @return New notification.
     */
    public abstract Notification notify( String title, Icon icon,
            String detailsText, ActionListener detailsAction, Priority priority);

    /**
     * Create and show new notification with customized content.
     * @param title Notification title. Html is not supported, any html tags will
     * be escaped.
     * @param icon Notification icon
     * @param balloonDetails Component that will show below notification title 
     * in a balloon.
     * @param popupDetails Component that will show below notification title
     * in notifications popup list. 
     * @param priority Notification priority.
     * @return New notification.
     */
    public abstract Notification notify( String title, Icon icon,
            JComponent balloonDetails, JComponent popupDetails, Priority priority);

    /**
     * Simple implementation of NotificationDisplayer which shows the notifications
     * on the main status line.
     */
    private static class SimpleNotificationDisplayer extends NotificationDisplayer {

        @Override
        public Notification notify(String title, Icon icon, String detailsText, ActionListener detailsAction, Priority priority) {
            return notify( title + " - " + detailsText, priority );
        }

        @Override
        public Notification notify(String title, Icon icon, JComponent balloonDetails, JComponent popupDetails, Priority priority) {
            return notify( title, priority );
        }

        private Notification notify( String text, Priority priority ) {
            int importance = 1;
            switch( priority ) {
                case HIGH:
                    importance = 100;
                    break;
                case NORMAL:
                    importance = 50;
                    break;
                case LOW:
                case SILENT:
                    importance = 1;
                    break;
            }
            Message msg = StatusDisplayer.getDefault().setStatusText( text, importance );
            return new NotificationImpl(msg);
        }
    }

    private static class NotificationImpl extends Notification {

        private final Message msg;

        public NotificationImpl( Message msg ) {
            this.msg = msg;
        }

        @Override
        public void clear() {
            msg.clear(0);
        }
    }
}
