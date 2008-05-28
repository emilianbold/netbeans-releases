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
package org.netbeans.performance.mobility.actions;

import org.netbeans.performance.mobility.MPUtilities;
import org.netbeans.performance.mobility.window.MIDletEditorOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JPopupMenuOperator;
import org.netbeans.modules.performance.guitracker.ActionTracker;
import org.netbeans.modules.performance.utilities.PerformanceTestCase;

/**
 *
 * @author mkhramov@netbeans.org
 */
public class MIDletViewsSwitch extends PerformanceTestCase {

    public String fromView;
    public String toView;
    private String targetProject;
    private String midletName;
    private Node openNode;
    protected static String OPEN = org.netbeans.jellytools.Bundle.getStringTrimmed("org.openide.actions.Bundle", "Open");
    private MIDletEditorOperator targetMIDletEditor;
    private static ActionTracker tr;

    public MIDletViewsSwitch(String testName) {
        super(testName);
        expectedTime = WINDOW_OPEN;
        WAIT_AFTER_PREPARE = 10000;
        WAIT_AFTER_CLOSE = 5000;
    }

    public MIDletViewsSwitch(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = WINDOW_OPEN;
        WAIT_AFTER_PREPARE = 10000;
        WAIT_AFTER_CLOSE = 5000;
    }

    @Override
    public void initialize() {
        log(":: initialize");
        tr = ActionTracker.getInstance();
        targetProject = "MobileApplicationVisualMIDlet";
        midletName = "VisualMIDletMIDP20.java";

        String documentPath = MPUtilities.SOURCE_PACKAGES + "|" + "allComponents" + "|" + midletName;
        openNode = new Node(new ProjectsTabOperator().getProjectRootNode(targetProject), documentPath);

        if (this.openNode == null) {
            throw new Error("Cannot find expected node ");
        }
        openNode.select();

        JPopupMenuOperator popup = this.openNode.callPopup();
        if (popup == null) {
            throw new Error("Cannot get context menu for node ");
        }
        log("------------------------- after popup invocation ------------");
        popup.getTimeouts().setTimeout("JMenuOperator.PushMenuTimeout", 90000);
        try {
            popup.pushMenu(OPEN);
        } catch (org.netbeans.jemmy.TimeoutExpiredException tee) {
            throw new Error("Cannot push menu item ");
        }
        targetMIDletEditor = MIDletEditorOperator.findMIDletEditorOperator(midletName);
    }

    public void prepare() {
        log(":: prepare");
        tr.add(ActionTracker.TRACK_TRACE_MESSAGE, "TEST:PREPARE");
        targetMIDletEditor.switchToViewByName(fromView);

    }

    public ComponentOperator open() {
        log(":: open");
        tr.add(ActionTracker.TRACK_TRACE_MESSAGE, "TEST:OPEN");
        targetMIDletEditor.switchToViewByName(toView);
        return null;
    }

    @Override
    public void close() {
        log(":: close");
        tr.add(ActionTracker.TRACK_TRACE_MESSAGE, "TEST:CLOSE");
    }

    @Override
    public void shutdown() {
        log(":: shutdown");
        targetMIDletEditor.closeDiscard();
    }

    public void testFlowToDesignSwitch() {
        fromView = "Flow";
        toView = "Screen";
        setJavaEditorCaretFilteringOn();
        doMeasurement();
    }

    public void testDesignToFlowSwitch() {
        fromView = "Screen";
        toView = "Flow";
        setJavaEditorCaretFilteringOn();
        doMeasurement();
    }

    public void testFlowToSourceSwitch() {
        fromView = "Flow";
        toView = "Source";
        setJavaEditorCaretFilteringOn();
        doMeasurement();
    }

    public void testSourceToFlowSwitch() {
        fromView = "Source";
        toView = "Flow";
        setJavaEditorCaretFilteringOn();
        doMeasurement();

    }
}
