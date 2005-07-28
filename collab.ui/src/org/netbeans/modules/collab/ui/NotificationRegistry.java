/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.collab.ui;

import org.openide.*;
import org.openide.awt.*;
import org.openide.util.*;
import org.openide.util.actions.*;
import org.openide.windows.*;

import java.util.*;

import org.netbeans.modules.collab.*;
import org.netbeans.modules.collab.ui.actions.*;


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
