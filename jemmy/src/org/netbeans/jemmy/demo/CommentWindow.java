/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s): Alexandre Iline.
 *
 * The Original Software is the Jemmy library.
 * The Initial Developer of the Original Software is Alexandre Iline.
 * All Rights Reserved.
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
 *
 *
 *
 * $Id$ $Revision$ $Date$
 *
 */

package org.netbeans.jemmy.demo;

/**
 *
 * Interface implementation defines a way to display step comments
 * during demo or test debug.
 *
 * @author Alexandre Iline (alexandre.iline@sun.com)
 *
 *	
 */

public interface CommentWindow {
    /**
     * Defines either test(demo) has been stopped or not.
     * @return true if test (demo) execution has been stopped.
     * Like when user is reading step comments.
     * false if test execution can be continued.
     */
    public boolean isStopped();

    /**
     * Defines either test execution should be interrupted or not.
     * @return If true, execution will be interrupted.
     */
    public boolean isInterrupted();

    /**
     * Defines window title.
     * @param title Title to display.
     */
    public void setTitle(String title);

    /**
     * Should display next step comment.
     * @param stepComment Comments to be displayed.
     */
    public void nextStep(String stepComment);

    /**
     * Method is invoked at the end of test(demo).
     * @param stepComment Comment to be displayed.
     */
    public void showFinalComment(String stepComment);

    /**
     * Closes the window.
     */
    public void close();

    /**
     * Returns a message for a case when test needs to be interrupted.
     * @return Interrupted message if test should be interrupted.
     */
    public String getInterruptMessage();
}
