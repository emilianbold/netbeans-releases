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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.collab.ui;

import java.util.*;

import org.openide.util.actions.*;
import org.openide.windows.TopComponent;

import org.netbeans.modules.collab.ui.actions.ShowNextNotificationAction;

/**
 *
 * @author  Todd Fast, todd.fast@sun.com
 */
public class NotificationRegistry extends Object implements ActionPerformer {
    ////////////////////////////////////////////////////////////////////////////
    // Class fields
    ////////////////////////////////////////////////////////////////////////////
    private static NotificationRegistry instance;

    ////////////////////////////////////////////////////////////////////////////
    // Instance fields
    ////////////////////////////////////////////////////////////////////////////
    private List components = new ArrayList();
    private NotificationThread notificationThread;

    /**
     *
     *
     */
    public NotificationRegistry() {
        super();

        notificationThread = new NotificationThread();
        notificationThread.start();
    }

    /**
     *
     *
     */
    public NotificationThread getNotificationThread() {
        return notificationThread;
    }

    /**
     *
     *
     */
    public void addComponent(TopComponent component) {
        synchronized (components) {
            if (!components.contains(component)) {
                components.add(component);

                if (component instanceof NotificationListener) {
                    addNotificationListener((NotificationListener) component);
                }

                update();
            }
        }
    }

    /**
     *
     *
     */
    public void removeComponent(TopComponent component) {
        synchronized (components) {
            if (components.remove(component)) {
                if (component instanceof NotificationListener) {
                    removeNotificationListener((NotificationListener) component);
                }

                update();
            }
        }
    }

    /**
     *
     *
     */
    protected void update() {
        ShowNextNotificationAction action = (ShowNextNotificationAction) SystemAction.get(
                ShowNextNotificationAction.class
            );

        if (components.size() == 0) {
            action.setActionPerformer(null);
            notificationThread.setPaused(true);
        } else {
            action.setActionPerformer(this);
            notificationThread.setPaused(false);
        }
    }

    /**
     *
     *
     */
    public TopComponent[] getNotifyingComponents() {
        return (TopComponent[]) components.toArray(new TopComponent[components.size()]);
    }

    /**
     *
     *
     */
    public void performAction(SystemAction systemAction) {
        showNextComponent();
    }

    /**
     *
     *
     */
    public void showComponent(TopComponent component) {
        synchronized (components) {
            if (components.contains(component)) {
                if (!component.isOpened()) {
                    component.open();
                }

                component.requestActive();
            }
        }
    }

    /**
     *
     *
     */
    public void showNextComponent() {
        TopComponent component = null;

        synchronized (components) {
            if (components.size() > 0) {
                component = (TopComponent) components.get(0);
            }
        }

        if (component != null) {
            if (!component.isOpened()) {
                component.open();
            }

            component.requestActive();
        }
    }

    /**
     *
     *
     */
    public void addNotificationListener(NotificationListener listener) {
        getNotificationThread().addNotificationListener(listener);
    }

    /**
     *
     *
     */
    public void removeNotificationListener(NotificationListener listener) {
        getNotificationThread().removeNotificationListener(listener);
    }

    ////////////////////////////////////////////////////////////////////////////
    // Instance management
    ////////////////////////////////////////////////////////////////////////////

    /**
     *
     *
     */
    public static synchronized NotificationRegistry getDefault() {
        if (instance == null) {
            instance = new NotificationRegistry();
        }

        return instance;
    }

    /**
     *
     *
     */
    public static synchronized void deinitialize() {
        if (instance != null) {
            // Remove all components from our notification list
            TopComponent[] _components = new TopComponent[0];

            synchronized (instance.components) {
                _components = (TopComponent[]) instance.components.toArray(new TopComponent[0]);
            }

            for (int i = 0; i < _components.length; i++)
                instance.removeComponent(_components[i]);
        }
    }
}
