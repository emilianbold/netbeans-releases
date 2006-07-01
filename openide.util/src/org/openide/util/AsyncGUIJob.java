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
package org.openide.util;


/** Service provider interface (SPI) for executing of time consuming task which
 * results are visible in UI.
 *
 * Typical usage is post-initialization of UI components or various long lasting
 * operations like network accessing invoked directly or indirectly by user
 * from UI.
 *
 * Note that it's often desirable to provide cancel support, at least for
 * longer lasting jobs. See {@link org.openide.util.Cancellable} support.
 * Keep in mind that methods {@link #construct} and
 * {@link org.openide.util.Cancellable#cancel} can be called concurrently and
 * require proper synchronization as such.
 *
 * @author  Dafe Simonek
 *
 * @see org.openide.util.Utilities#attachInitJob
 * @since 3.36
 */
public interface AsyncGUIJob {
    /** Worker method, can be called in any thread but event dispatch thread.
     * Implement your time consuming work here.
     * Always called and completed before {@link #finished} method.
     */
    public void construct();

    /** Method to update UI using given data constructed in {@link #construct}
     * method. Always called in event dispatch thread, after {@link #construct}
     * method completed its execution.
     */
    public void finished();
}
