/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

package org.netbeans.modules.cnd.api.model;

import java.util.Collection;

/**
 * Source model
 *
 * @author Vladimir Kvashin
 */
public interface CsmModel {

    // TODO: write full description
    /** @param id Netbeans project */
    CsmProject getProject(Object id);

    Collection/*<CsmProject>*/ projects();

    void addModelListener(CsmModelListener listener);

    void removeModelListener(CsmModelListener listener);
    
    void addProgressListener(CsmProgressListener listener);
    
    void removeProgressListener(CsmProgressListener listener);
    
    void addModelStateListener(CsmModelStateListener listener);
    
    void removeModelStateListener(CsmModelStateListener listener);
    
    /**
     * Code model calls can be very expensive.
     * Therefore one can never call code model from event dispatching thread.
     * Moreover, to make code model able to effectively solve synchronization issues,
     * all callers shall use not their own threads but call enqueue method instead.
     *
     * The method creates a thread and runs the given task in this thread.
     *
     * Whether or not the thread be created immediately or the task
     * will be just enqueued and runned later on, depends on implementation.
     *
     * @param task task to run
     */
    void enqueue(Runnable task);

    /**
     * Code model calls can be very expensive. 
     * Therefore one can never call code model from event dispatching thread.
     * Moreover, to make code model able to effectively solve synchronization issues,
     * all callers shall use not their own threads but call enqueue method instead.
     *
     * The method creates a thread and runs the given task in this thread.
     *
     * Whether or not the thread be created immediately or the task
     * will be just enqueued and runned later on, depends on implementation.
     *
     * We recommend using this method rather than one without <code>name</code> parameter.
     *
     * @param task task to run
     * @param name name that would be added to the thread name
     */
    void enqueue(Runnable task, String name);
    
    /**
     * Find project that contains file.
     * Returns CsmFile if project is found.
     *
     * @param absPath absolute file path
     */
    CsmFile findFile(String absPath);
    
    /**
     * Returns the state of the model
     */
    CsmModelState getState();
}
