/*****************************************************************************
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
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):

 * The Original Software is the CVS Client Library.
 * The Initial Developer of the Original Software is Robert Greig.
 * Portions created by Robert Greig are Copyright (C) 2000.
 * All Rights Reserved.
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

 * Contributor(s): Robert Greig.
 *****************************************************************************/
package org.netbeans.lib.cvsclient.event;

import org.netbeans.lib.cvsclient.ClientServices;

import java.io.File;

/**
 * This class is responsible for firing CVS events to registered listeners.
 * It can either fire events as they are generated or wait until a suitable
 * checkpoint and fire many events at once. This can prevent event storms
 * from degrading system performance.
 * @author  Robert Greig
 */
public class EventManager {
    /**
     * Registered listeners for events. This is an array for performance when
     * firing events. We take the hit when adding or removing listeners - that
     * should be a relatively rare occurrence.
     */
    private CVSListener[] listeners;

    /**
     * Holds value of property fireEnhancedEventSet.
     * If true, the library fires the EnhancedMessageEvents.
     * Default is true. Some builders might work badly, if set to false.
     */
    private boolean fireEnhancedEventSet = true;
    
    private final ClientServices services;

    /**
     * Construct a new EventManager
     */
    public EventManager(ClientServices services) {
        this.services = services;
    }

    /**
     * Returns Client services implementation tied to this event manager.
     * 
     * @return a ClientServices implementation
     */ 
    public ClientServices getClientServices() {
        return services;
    }

    /**
     * Add a listener to the list.
     * @param listener the listener to add
     */
    public synchronized void addCVSListener(CVSListener listener) {
        if (listeners == null || listeners.length == 0) {
            listeners = new CVSListener[1];
        }
        else {
            // allocate a new array and copy existing listeners
            CVSListener[] l = new CVSListener[listeners.length + 1];
            for (int i = 0; i < listeners.length; i++) {
                l[i] = listeners[i];
            }
            listeners = l;
        }
        listeners[listeners.length - 1] = listener;
    }

    /**
     * Remove a listeners from the list
     * @param listener the listener to remove
     */
    public synchronized void removeCVSListener(CVSListener listener) {
        // TODO: test this method!!
        if (listeners.length == 1) {
            listeners = null;
        }
        else {
            CVSListener[] l = new CVSListener[listeners.length - 1];
            int i = 0;
            while (i < l.length) {
                if (listeners[i] == listener) {
                    for (int j = i + 1; j < listeners.length; j++) {
                        l[j - 1] = listeners[j];
                    }
                    break;
                }
                else {
                    l[i] = listeners[i];
                }
                i++;
            }
            listeners = l;
        }
    }

    /**
     * Fire a CVSEvent to all the listeners
     * @param e the event to send
     */
    public void fireCVSEvent(CVSEvent e) {
        // if we have no listeners, then there is nothing to do
        if (listeners == null || listeners.length == 0)
            return;
        if (e instanceof FileInfoEvent) {
            File file = ((FileInfoEvent) e).getInfoContainer().getFile();
            if (services.getGlobalOptions().isExcluded(file)) return;
        }
        CVSListener[] l = null;
        synchronized (listeners) {
            l = new CVSListener[listeners.length];
            System.arraycopy(listeners, 0, l, 0, l.length);
        }

        for (int i = 0; i < l.length; i++) {
            e.fireEvent(l[i]);
        }
    }

    /** Getter for property fireEnhancedEventSet.
     * @return Value of property fireEnhancedEventSet.
     */
    public boolean isFireEnhancedEventSet() {
        return fireEnhancedEventSet;
    }

    /** Setter for property fireEnhancedEventSet.
     * @param fireEnhancedEventSet New value of property fireEnhancedEventSet.
     */
    public void setFireEnhancedEventSet(boolean fireEnhancedEventSet) {
        this.fireEnhancedEventSet = fireEnhancedEventSet;
    }

}
