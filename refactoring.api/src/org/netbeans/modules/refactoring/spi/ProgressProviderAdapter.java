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

package org.netbeans.modules.refactoring.spi;

import org.netbeans.modules.refactoring.api.impl.ProgressSupport;
import org.netbeans.modules.refactoring.api.ProgressListener;

/**
 * Simple implementation of ProgressProvider
 * @see ProgressProvider
 * @author Jan Becicka
 */
public class ProgressProviderAdapter implements ProgressProvider {

    private ProgressSupport progressSupport;

    /**
     * Default constructor
     */
    protected ProgressProviderAdapter() {
    }
    
    /** Registers ProgressListener to receive events.
     * @param listener The listener to register.
     *
     */
    public synchronized void addProgressListener(ProgressListener listener) {
        if (progressSupport == null ) {
            progressSupport = new ProgressSupport();
        }
        progressSupport.addProgressListener(listener);
    }
    
    /** Removes ProgressListener from the list of listeners.
     * @param listener The listener to remove.
     *
     */
    public synchronized void removeProgressListener(ProgressListener listener) {
        if (progressSupport != null ) {
            progressSupport.removeProgressListener(listener); 
        }
    }
    
    /** Notifies all registered listeners about the event.
     *
     * @param type Type of operation that is starting.
     * @param count Number of steps the operation consists of.
     *
     */
    protected final void fireProgressListenerStart(int type, int count) {
        if (progressSupport != null) 
            progressSupport.fireProgressListenerStart(this, type, count);
    }
    
    /** Notifies all registered listeners about the event.
     */
    protected final void fireProgressListenerStep() {
        if (progressSupport != null)
            progressSupport.fireProgressListenerStep(this);
    }
    
    /**
     * Notifies all registered listeners about the event.
     * @param count 
     */
    protected final void fireProgressListenerStep(int count) {
        if (progressSupport != null)
            progressSupport.fireProgressListenerStep(this, count);
    }
    
    /** Notifies all registered listeners about the event.
     */
    protected final void fireProgressListenerStop() {
        if (progressSupport != null)
            progressSupport.fireProgressListenerStop(this);
    }
}
