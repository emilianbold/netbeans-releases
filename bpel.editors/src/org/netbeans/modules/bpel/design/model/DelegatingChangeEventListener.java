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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.bpel.design.model;

import javax.swing.SwingUtilities;
import org.netbeans.modules.bpel.model.api.events.ArrayUpdateEvent;
import org.netbeans.modules.bpel.model.api.events.ChangeEventListener;
import org.netbeans.modules.bpel.model.api.events.EntityInsertEvent;
import org.netbeans.modules.bpel.model.api.events.EntityRemoveEvent;
import org.netbeans.modules.bpel.model.api.events.EntityUpdateEvent;
import org.netbeans.modules.bpel.model.api.events.PropertyRemoveEvent;
import org.netbeans.modules.bpel.model.api.events.PropertyUpdateEvent;

/**
 *
 * @author Alexey
 */
public class DelegatingChangeEventListener implements ChangeEventListener {

    private ChangeEventListener delegate;

    public DelegatingChangeEventListener(ChangeEventListener delegate) {
        this.delegate = delegate;
    }

    public void notifyPropertyRemoved(final PropertyRemoveEvent event) {
        run(new Runnable() {

            public void run() {
                delegate.notifyPropertyRemoved(event);
            }
        });
    }

    public void notifyEntityInserted(final EntityInsertEvent event) {
        run(new Runnable() {

            public void run() {
                delegate.notifyEntityInserted(event);
            }
        });
    }

    public void notifyPropertyUpdated(final PropertyUpdateEvent event) {
        run(new Runnable() {

            public void run() {
                delegate.notifyPropertyUpdated(event);
            }
        });
    }

    public void notifyEntityRemoved(final EntityRemoveEvent event) {
        run(new Runnable() {

            public void run() {
                delegate.notifyEntityRemoved(event);
            }
        });
    }

    public void notifyEntityUpdated(final EntityUpdateEvent event) {
        run(new Runnable() {

            public void run() {
                delegate.notifyEntityUpdated(event);
            }
        });
    }

    public void notifyArrayUpdated(final ArrayUpdateEvent event) {
        run(new Runnable() {

            public void run() {
                delegate.notifyArrayUpdated(event);
            }
        });
    }

    private void run(Runnable r) {
        SwingUtilities.isEventDispatchThread();
        if (SwingUtilities.isEventDispatchThread()) {
            r.run();
        } else {
            SwingUtilities.invokeLater(r);
        }
    }
}
