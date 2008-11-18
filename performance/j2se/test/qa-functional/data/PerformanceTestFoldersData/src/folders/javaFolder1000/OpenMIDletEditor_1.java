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
package folders.javaFolder1000;

import org.netbeans.performance.mobility.MPUtilities;
import org.netbeans.performance.mobility.window.MIDletEditorOperator;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JPopupMenuOperator;
import org.netbeans.modules.performance.utilities.PerformanceTestCase;

/**
 *
 * @author mkhramov@netbeans.org
 */
public class OpenMIDletEditor_1 extends PerformanceTestCase {

    private Node openNode;
    private String targetProject;
    private String midletName;
    private ProjectsTabOperator pto;
    // Since Loading Document label is shown it is okay to load document for 10 sec
    public final static long EXPECTED_TIME = 10000;
    protected static String OPEN = org.netbeans.jellytools.Bundle.getStringTrimmed("org.openide.actions.Bundle", "Open");

    /**
     * Creates a new instance of OpenMIDletEditor
     * @param testName the name of the test
     */
    public OpenMIDletEditor_1(String testName) {
        super(testName);
        targetProject = "MobileApplicationVisualMIDlet";
        midletName = "VisualMIDletMIDP20.java";
        expectedTime = EXPECTED_TIME;
        WAIT_AFTER_OPEN = 20000;
    }

    /**
     * Creates a new instance of OpenMIDletEditor
     * @param testName the name of the test
     * @param performanceDataName measured values will be saved under this name
     */
    public OpenMIDletEditor_1(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        targetProject = "MobileApplicationVisualMIDlet";
        midletName = "VisualMIDletMIDP20.java";
        expectedTime = EXPECTED_TIME;
        WAIT_AFTER_OPEN = 20000;
    }

    @Override
    public void initialize() {
        log(":: initialize");
        EditorOperator.closeDiscardAll();
        pto = ProjectsTabOperator.invoke();
    }

    public void prepare() {
        log(":: prepare");
        String documentPath = MPUtilities.SOURCE_PACKAGES + "|" + "allComponents" + "|" + midletName;

        long nodeTimeout = pto.getTimeouts().getTimeout("ComponentOperator.WaitStateTimeout");
        pto.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", 60000);

        try {
            openNode = new Node(pto.getProjectRootNode(targetProject), documentPath);
        } catch (TimeoutExpiredException ex) {
            pto.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", nodeTimeout);
            throw new Error("Cannot find expected node because of Timeout");
        }
        pto.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", nodeTimeout);

        if (this.openNode == null) {
            throw new Error("Cannot find expected node ");
        }
        openNode.select();
    }

    public ComponentOperator open() {
        log(":: open");
        JPopupMenuOperator popup = this.openNode.callPopup();
        if (popup == null) {
            throw new Error("Cannot get context menu for node ");
        }
        log("   -- after popup invocation --");
        popup.getTimeouts().setTimeout("JMenuOperator.PushMenuTimeout", 90000);
        try {
            popup.pushMenu(OPEN);
        } catch (org.netbeans.jemmy.TimeoutExpiredException tee) {
            throw new Error("Cannot push menu item ");
        }

        return MIDletEditorOperator.findMIDletEditorOperator(midletName);
    }

    @Override
    public void close() {
        log(":: close");
        if (testedComponentOperator != null) {
            new Thread("Question dialog discarder") {

                @Override
                public void run() {
                    try {
                        new JButtonOperator(new JDialogOperator("Question"), "Discard").push();
                    } catch (Exception e) {
                        // There is no need to care about this exception as this dialog is optional 
                        e.printStackTrace();
                    }
                }
            }.start();
            ((MIDletEditorOperator) testedComponentOperator).close();
        }
    }

    @Override
    public void shutdown() {
        log("::shutdown");
    }
}
