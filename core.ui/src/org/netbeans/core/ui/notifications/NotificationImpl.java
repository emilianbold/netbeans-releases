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

package org.netbeans.core.ui.notifications;

import javax.swing.Icon;
import javax.swing.JComponent;
import org.openide.awt.Notification;
import org.openide.awt.NotificationDisplayer.Priority;

/**
 * Notification implementation.
 *
 * @author S. Aubrecht
 */
class NotificationImpl extends Notification implements Comparable<NotificationImpl> {

    private String title;
    private Icon icon;
    private Priority priority;
    private JComponent balloonComp;
    private JComponent popupComponent;

    void init( String title, Icon icon, Priority priority, JComponent balloonComp, JComponent popupComponent ) {
        this.title = title;
        this.icon = icon;
        this.priority = priority;
        this.balloonComp = balloonComp;
        this.popupComponent = popupComponent;
    }

    @Override
    public void clear() {
        NotificationDisplayerImpl.getInstance().remove(this);
    }

    public int compareTo(NotificationImpl n) {
        int res = priority.compareTo(n.priority);
        if( 0 == res )
            res = title.compareTo(n.title);
        return res;
    }

    public JComponent getBalloonComp() {
        return balloonComp;
    }

    public Icon getIcon() {
        return icon;
    }

    public JComponent getPopupComponent() {
        return popupComponent;
    }

    public String getTitle() {
        return title;
    }
}
