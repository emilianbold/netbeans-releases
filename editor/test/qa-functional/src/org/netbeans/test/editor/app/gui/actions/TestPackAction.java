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
package org.netbeans.test.editor.app.gui.actions;

import org.netbeans.test.editor.app.core.*;
import org.netbeans.test.editor.app.core.cookies.PackCookie;
import org.netbeans.test.editor.app.gui.tree.TestNodeDelegate;

/**
 *
 * @author  ehucka
 * @version
 */
public class TestPackAction extends TreeNodeAction {

    /** Creates new TestDownAction */
    public TestPackAction() {
    }

    /** Test whether the action should be enabled based
     * on the currently activated nodes.
     *
     * @param activatedNodes current activated nodes, may be empty but not <code>null</code>
     * @return <code>true</code> to be enabled, <code>false</code> to be disabled
     */
    public boolean enable(TestNodeDelegate[] activatedNodes) {
	boolean ret=false;
	for (int i=0;i < activatedNodes.length;i++) {
	    PackCookie pc = (PackCookie) (activatedNodes[i].getTestNode().getCookie(PackCookie.class));
	    
	    if (pc == null) {
		return false;
	    } else if (!pc.isPacked()) {
		ret = true;
	    }
	}
	return ret;
    }
    
    /** Perform the action based on the currently activated nodes.
     * Note that if the source of the event triggering this action was itself
     * a node, that node will be the sole argument to this method, rather
     * than the activated nodes.
     *
     * @param activatedNodes current activated nodes, may be empty but not <code>null</code>
     */
    public void performAction(TestNodeDelegate[] activatedNodes) {
	for(int i=0;i < activatedNodes.length;i++) {
	    PackCookie pc = (PackCookie) activatedNodes[i].getTestNode().getCookie(PackCookie.class);
	    
	    if (pc != null && !pc.isPacked()) {
		pc.pack();
	    }
	}
    }
    
    /** Get a help context for the action.
     * @return the help context for this action
     */
    public String getHelpCtx() {
	return "Pack Log Actions into String actions";
    }
    
    /** Get a human presentable name of the action.
     * This may be
     * presented as an item in a menu.
     * @return the name of the action
     */
    public String getName() {
	return "Pack";
    }
    
}
