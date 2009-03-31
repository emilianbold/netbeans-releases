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

package org.netbeans.modules.refactoring.api.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.refactoring.api.ProgressEvent;
import org.netbeans.modules.refactoring.api.ProgressListener;

/**
 * Support class for progress notifications
 * @author Martin Matula, Jan Becicka
 */
public final class ProgressSupport {
    /** Utility field holding list of ProgressListeners. */
    private final List<ProgressListener> progressListenerList = new ArrayList<ProgressListener>();
    private int counter;

    public synchronized void addProgressListener(ProgressListener listener) {
        progressListenerList.add(listener);
    }
    
    /** Removes ProgressListener from the list of listeners.
     * @param listener The listener to remove.
     *
     */
    public synchronized void removeProgressListener(ProgressListener listener) {
        progressListenerList.remove(listener);
    }
    
    /** Notifies all registered listeners about the event.
     *
     * @param type Type of operation that is starting.
     * @param count Number of steps the operation consists of.
     *
     */
    public void fireProgressListenerStart(Object source, int type, int count) {
        counter = -1;
        ProgressEvent event = new ProgressEvent(source, ProgressEvent.START, type, count);
        ProgressListener[] listeners = getListenersCopy();
        for (ProgressListener listener : listeners) {
            try {
                listener.start(event);
            } catch (RuntimeException e) {
                log(e);
            }
        }
    }
    
    /** Notifies all registered listeners about the event.
     *
     * @param type Type of operation that is starting.
     * @param count Number of steps the operation consists of.
     *
     */
    public void fireProgressListenerStart(int type, int count) {
        fireProgressListenerStart(this, type, count);
    }
    
    
    /** Notifies all registered listeners about the event.
     */
    public void fireProgressListenerStep(Object source, int count) {
        counter = count;
        ProgressEvent event = new ProgressEvent(source, ProgressEvent.STEP, 0, count);
        ProgressListener[] listeners = getListenersCopy();
        for (ProgressListener listener : listeners) {
            try {
                listener.step(event);
            } catch (RuntimeException e) {
                log(e);
            }
        }
    }
    /** Notifies all registered listeners about the event.
     */
    public void fireProgressListenerStep(Object source) {
        fireProgressListenerStep(source, counter+1);
    }
    /** Notifies all registered listeners about the event.
     */
    public void fireProgressListenerStop(Object source) {
        ProgressEvent event = new ProgressEvent(source, ProgressEvent.STOP);
        ProgressListener[] listeners = getListenersCopy();
        for (ProgressListener listener : listeners) {
            try {
                listener.stop(event);
            } catch (RuntimeException e) {
                log(e);
            }
        }
    }
    
    /** Notifies all registered listeners about the event.
     */
    public void fireProgressListenerStop() {
        fireProgressListenerStop(this);
    }

    private synchronized ProgressListener[] getListenersCopy() {
        return progressListenerList.toArray(new ProgressListener[progressListenerList.size()]);
    }

    private void log(Exception e) {
        Logger.getLogger(ProgressSupport.class.getName()).log(Level.INFO, e.getMessage(), e);
    }

}
