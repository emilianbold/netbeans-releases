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


package org.netbeans.progress.spi;

/**
 *
 * @author Milos Kleint (mkleint@netbeans.org)
 */
public final class ProgressEvent {

    public static final int TYPE_START = 0;
    public static final int TYPE_FINISH = 4;
    public static final int TYPE_REQUEST_STOP = 3;
    public static final int TYPE_PROGRESS = 1;
    public static final int TYPE_SWITCH = 5;

     private InternalHandle source;
     private long estimatedCompletion;
     private int percentageDone;
     private int workunitsDone;
     private String message;
     private int type;
     private boolean watched;
     private boolean switched;
     private String displayName;
    /** Creates a new instance of ProgressEvent 
     * @param type one of TYPE_START, TYPE_REQUEST_STOP, TYPE_FINISH, TYPE_SWITCHED
     */
    public ProgressEvent(InternalHandle src, int type, boolean isWatched) {
        source = src;
        estimatedCompletion = -1;
        percentageDone = -1;
        workunitsDone = -1;
        message = null;
        this.type = type;
        watched = isWatched;
        switched = (type == TYPE_SWITCH);
    }
    /**
     * @param percentage completed work percentage
     * @param estimate estimate of completion in seconds
     */
    public ProgressEvent(InternalHandle src, String msg, int units, int percentage, long estimate, boolean isWatched) {
        this(src, TYPE_PROGRESS, isWatched);
        workunitsDone = units;
        percentageDone = percentage;
        estimatedCompletion = estimate;
        message = msg;
    }
    public ProgressEvent(InternalHandle src, String msg, int units, int percentage, long estimate, boolean isWatched, String displayName) {
        this(src, msg, units, percentage, estimate, isWatched);
        this.displayName = displayName;
    }
    
    public InternalHandle getSource() {
        return source;
    }

    public long getEstimatedCompletion() {
        return estimatedCompletion;
    }

    public int getPercentageDone() {
        return percentageDone;
    }

    public int getWorkunitsDone() {
        return workunitsDone;
    }

    public String getMessage() {
        return message;
    }

    public int getType() {
        return type;
    }
    
    public boolean isWatched() {
        return watched;
    }
    
    /**
     * used in controller, preserve dynamic message from earlier events 
     * if this one doesn't have it's own.
     */
    public void copyMessageFromEarlier(ProgressEvent last) {
        if (message == null) {
            message = last.getMessage();
        }
        if (displayName == null) {
            displayName = last.getDisplayName();
        }
    }
    
    public void markAsSwitched() {
        switched = true;
    }
    
    public boolean isSwitched() {
        return switched;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
}
