/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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

package org.netbeans.modules.ruby.platform.spi;

import org.netbeans.modules.ruby.platform.execution.RubyExecutionDescriptor;

/**
 * Ability for Ruby project to debug Ruby scripts/applications.
 */
public interface RubyDebuggerImplementation {

    /**
     * Sets descriptor describing the process to be debugged.
     *
     * @param descriptor description of the process to be debugged
     */
    void describeProcess(final RubyExecutionDescriptor descriptor);

    /**
     * Checks whether the implementation is able to debug the {@link
     * #describeProcess described process}. If not it might try to interact with
     * the user to make the implementation ready for the process (i.e.
     * installing additional gems). If still not ready, returns false.
     * 
     * @return whether the implementation is able to debug described process.
     */
    boolean prepare();

    /**
     * Starts debugging of the given script.
     * 
     * @return debugger {@link java.lang.Process process}. Might be
     *         <tt>null</tt> if debugging cannot be started for some reason.
     *         E.g. interpreter cannot be obtained from preferences.
     */
    Process debug();

    /**
     * Attaches to the running debugger backend on the specified host and port.
     * 
     * @param host host to connect to
     * @param port port to connect to
     * @param timeout timeout until the attaching gives up
     */
    void attach(String host, int port, int timeout);

    /**
     * Action which shall be performed when a debugger is <em>forced</em> to
     * stop, like pressing output window's <em>stop button</em>.
     */
    Runnable getFinishAction();
}
