/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
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
