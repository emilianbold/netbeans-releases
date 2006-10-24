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
package org.netbeans.modules.refactoring.api;

import java.util.EventListener;

/** Progress listener. Enables objects to listen to a progress of long operations.
 *
 * @author  Martin Matula
 */
public interface ProgressListener extends EventListener {
    /** Signals that an operation has started.
     * @param event Event object describing this event.
     */
    public void start(ProgressEvent event);

    /** Signals that an operation has progressed.
     * @param event Event object describing this event.
     */
    public void step(ProgressEvent event);

    /** Signals that an operation has finished.
     * @param event Event object describing this event.
     */
    public void stop(ProgressEvent event);
}
