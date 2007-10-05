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

import org.netbeans.jemmy.EventDispatcher;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.TestOut;
import org.netbeans.jemmy.TimeoutExpiredException;

/**
 *
 * Class to display step comments.
 *
 * @author Alexandre Iline (alexandre.iline@sun.com)
 *
 */

public class Demonstrator {

    private static CommentWindow displayer;
    private static CommentWindow nonDisplayer;

    /**
     * Notifies current CommentWindow implementation to change title.
     * @param title new CommentWindow's title
     */
    public static void setTitle(String title) {
	displayer.setTitle(title);
    }

    /**
     * Changes current CommentWindow.
     * @param cw CommentWindow instance.
     */
    public static void setCommentWindow(CommentWindow cw) {
	displayer = cw;
    }

    /**
     * Notifies current CommentWindow implementation to display comments for a new step.
     * @param stepComment New step comments
     */
    public static void nextStep(String stepComment) {
	getDisplayer().nextStep(stepComment);
	while(getDisplayer().isStopped()) {
	    try {
		Thread.currentThread().sleep(100);
	    } catch (InterruptedException e) {
	    }
	}
	if(getDisplayer() != nonDisplayer) {
	    try {
		EventDispatcher.waitQueueEmpty(TestOut.getNullOutput(),
					       JemmyProperties.getCurrentTimeouts());
	    } catch(TimeoutExpiredException e) {
		e.printStackTrace();
	    }
	}
	if(getDisplayer().isInterrupted()) {
	    getDisplayer().close();
	    throw(new DemoInterruptedException(getDisplayer().getInterruptMessage()));
	}
    }

    /**
     * Notifies current CommentWindow implementation to display final comments.
     * @param stepComment New step comments
     */
    public static void showFinalComment(String stepComment) {
	getDisplayer().showFinalComment(stepComment);
	while(getDisplayer().isStopped()) {
	    try {
		Thread.currentThread().sleep(100);
	    } catch (InterruptedException e) {
	    }
	}
	getDisplayer().close();
	if(getDisplayer() != nonDisplayer) {
	    try {
		EventDispatcher.waitQueueEmpty(TestOut.getNullOutput(),
					       JemmyProperties.getCurrentTimeouts());
	    } catch(TimeoutExpiredException e) {
		e.printStackTrace();
	    }
	}
    }
    
    static {
	setCommentWindow(new DefaultCommentWindow());
	setTitle("Step comments");
	nonDisplayer = new NonWindow(); 
    }

    private static class NonWindow implements CommentWindow {
	public void setTitle(String title) {
	}
	public boolean isStopped() {
	    return(false);
	}
	public void nextStep(String stepComment) {
	    JemmyProperties.getCurrentOutput().printLine("Step comments:\n" + 
							 stepComment);
	}
	public void showFinalComment(String stepComment) {
	    JemmyProperties.getCurrentOutput().printLine("Final comments:\n" + 
							 stepComment);
	}
	public boolean isInterrupted() {
	    return(false);
	}
	public String getInterruptMessage() {
	    return("");
	}
	public void close() {
	}
    }

    private static CommentWindow getDisplayer() {
	if(System.getProperty("jemmy.demo") != null &&
	   System.getProperty("jemmy.demo").equals("on")) {
	    return(displayer);
	} else {
	    return(nonDisplayer);
	}
    }
}
