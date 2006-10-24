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

import java.util.EventObject;

/** Progress event object.
 *
 * @author Martin Matula
 */
public final class ProgressEvent extends EventObject {
    /** Start event id */
    public static final int START = 1;
    /** Step event id */
    public static final int STEP = 2;
    /** Stop event id */
    public static final int STOP = 4;

    // event id
    private final int eventId;
    // type of opreation that is being processed (source-specific number)
    private final int operationType;
    // number of steps of the operation being processed
    private final int count;

    /** Creates ProgressEvent instance.
     * @param source Source of the event.
     * @param eventId ID of the event.
     */
    public ProgressEvent(Object source, int eventId) {
        this(source, eventId, 0, 0);
    }

    /** Creates ProgressEvent instance.
     * @param source Source of the event.
     * @param eventId ID of the event.
     * @param operationType Source-specific number identifying source operation that
     * is being processed.
     * @param count Number of steps that the processed opration consists of.
     */
    public ProgressEvent(Object source, int eventId, int operationType, int count) {
        super(source);
        this.eventId = eventId;
        this.operationType = operationType;
        this.count = count;
    }

    /** Returns ID of the event.
     * @return ID of the event.
     */
    public int getEventId() {
        return eventId;
    }

    /** Returns operation type.
     * @return Source-specific number identifying operation being processed. Needs to
     * be valid for START events, can be 0 for STEP and STOP events.
     */
    public int getOperationType() {
        return operationType;
    }

    /** Returns step count.
     * @return Number of step that the operation being processed consists of. Needs to
     * be valid for START events, can be 0 for STEP and STOP events. If it is not 0
     * for STEP events, it is a new progress.
     */
    public int getCount() {
        return count;
    }
}
