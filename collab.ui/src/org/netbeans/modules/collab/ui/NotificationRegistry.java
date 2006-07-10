/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
