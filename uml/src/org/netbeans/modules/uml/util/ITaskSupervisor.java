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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.uml.util;

/**
 *
 * @author Craig Conover, craig.conover@sun.com
 */
public interface ITaskSupervisor
{
    // message output levels
    public final static int SUMMARY = 0; // very high level messages
    public final static int TERSE = 1; // somewhat detailed messages
    public final static int VERBOSE = 2; // very detailed messages
    public final static int DEBUG = 3; // debug level information only

    public boolean start(int itemTotal);
    public boolean start(int contributor, int itemTotal);
    
    /**
     * Increment the item counter by one.
     *
     * @return the current count after incrementing by one
     */
    public int increment();

    /**
     * Increment the item counter by the amount of the step parameter.
     *
     * @param step the amount to increment the counter.
     *
     * @return the current count after incrementing by the amount 
     *         of the step parameter.
     */
    public int increment(int step);
    
    /**
     *  Called by task subclass to check confirm that the task hasn't been
     *  cancelled or failed. If there is a cancellation or failure, finish()
     *  is called.
     *
     *  @return true if the process hasn't failed or been canceled
     */
    public boolean proceed();
    
    /**
     *  Called by task subclass to check confirm that the task hasn't been
     *  cancelled or failed. If there is a cancellation or failure, finish()
     *  is called.
     *
     *  @param step the amount to increment the counter by.
     *
     *  @return true if the process hasn't failed or been canceled
     */
    public boolean proceed(int step);
    
    /**
     * Outputs a blank line
     */
    public void log();
    public void log(int level);

    /**
     * Outputs a message with and appends newline by default
     * 
     * @param msg the message to be output
     */
    public void log(String msg);
    public void log(int level, String msg);

    /**
     * Outputs a message
     * 
     * @param msg the message to be output
     * @param newline if true, appends newline
     */
    public void log(String msg, boolean newline);
    public void log(int level, String msg, boolean newline);
    
    
    /**
     *  Call this method when a the task is instructed to be canceled and it
     *  will set the cancelled flag to false. The next time proceed() is 
     *  called, it will invoke finish() return false.
     */
    public boolean cancel();
    
    /**
     *  Call this method when a failure in your task is detected and it will
     *  set the success flag to false. The next time proceed() is called,
     *  it will invoke finish() return false.
     */
    public void fail();

}
