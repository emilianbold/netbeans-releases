/*
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
 * by Oracle in the GPL Version 2 section of the License file that
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

/*
 * SimpleCancellableTask.java
 *
 * Created on August 15, 2005, 2:25 PM
 *
 */

package org.netbeans.microedition.util;

/**
 * A simple implementation of <code>CancellableTask</code>. This implementation uses a supplied
 * Runnable object, which is being run when this task starts.
 * @author breh
 */
public class SimpleCancellableTask implements CancellableTask {
    
    private Executable executable;
    private Throwable caughtThrowable;
    
    
    /**
     * Creates a new instance of SimpleCancellableTask
     */
    public SimpleCancellableTask() {
    }
    
    
    /**
     * Creates a new instance of SimpleCancellableTask with supplied executable
     * object
     * @param executable Executable to be used for execution.
     */
    public SimpleCancellableTask(Executable executable) {
        this.executable = executable;
    }
    
    /**
     * Sets the executable object for this task. Also resets the failure message
     * and the failure state.
     *
     * @param executable Executable to be used for execution.
     */
    public void setExecutable(Executable executable) {
        caughtThrowable = null;
        this.executable = executable;
    }
    
    /**
     * Cancel this task. In this implementation this method does not cancel the runnable
     * task, this it always returns false.
     * @return always returns false
     */
    public boolean cancel() {
        // cancel does nothing in this simple implementation - always return false
        return false;
    }
    
    /**
     * Gets the failure message of the failed task. Since this implementation considers
     * as a failure an exception from the Runnable object (more exactly <code>run()</code>
     * method), this methods returns a message from this exception.
     * @return Message from failure exception
     */
    public String getFailureMessage() {
        if (caughtThrowable != null) {
            return caughtThrowable.getMessage();
        } else {
            return null;
        }
    }
    
    /**
     * Checks whether the task has failed. In this implementation this means
     * the the <code>execute()</code> method of the supplied <code>Executable</code> object has
     * thrown an exception.
     * @return true when the task has failed.
     */
    public boolean hasFailed() {
        return caughtThrowable != null;
    }
    
    /**
     * Implementation of run method. This method basically calls <code>execute()</code> method
     * from the suplied <code>Executable</code> object.
     */
    public void run() {
        caughtThrowable = null;
        if (executable != null) {
            try {
                executable.execute();
            } catch (Throwable t) {
                caughtThrowable = t;
            }
        }
    }
    
    
    
}
