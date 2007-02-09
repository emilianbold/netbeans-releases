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
 *  
 * $Id$
 */
package org.netbeans.installer.utils.progress;

/**
 * The interface which needs to be implemented by classes which need to listen to
 * a progress' state change events. 
 * 
 * <p>
 * They will need to register themselves with the progress object by either 
 * constructing the progress with a specialized constructor or calling the 
 * {@link Progress#addProgressListener(ProgressListener)} method.
 * 
 * @see Progress#Progress(ProgressListener)
 * @see Progress#addProgressListener(ProgressListener)
 * 
 * @author Kirill Sorokin
 * 
 * @since 1.0
 */
public interface ProgressListener {
    /**
     * This method will be called when a {@link Progress} being listened changes 
     * its state.
     * 
     * <p>
     * The actual {@link Progress} which has changed will be passed in as the only 
     * parameter. A progress' state is considered changed when any of its core 
     * properties (<code>title</code>, <code>detail</code>, <code>percentage</code>, 
     * <code>canceled</code>) change.
     * 
     * @param progress The {@link Progress} whose state has changed. 
     */
    void progressUpdated(final Progress progress);
}
