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

package org.netbeans.modules.junit.output;

import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;
import static org.netbeans.modules.junit.output.HtmlMarkupUtils.COLOR_OK;
import static org.netbeans.modules.junit.output.HtmlMarkupUtils.COLOR_WARNING;
import static org.netbeans.modules.junit.output.HtmlMarkupUtils.COLOR_FAILURE;

/**
 *
 * @author Marian Petras
 */
final class TestsuiteNode extends AbstractNode {

    private String suiteName;
    private Report report;
    private boolean filtered;

    /**
     *
     * @param  suiteName  name of the test suite, or {@code ANONYMOUS_SUITE}
     *                    in the case of anonymous suite
     * @see  ResultDisplayHandler#ANONYMOUS_SUITE
     */
    TestsuiteNode(final String suiteName, final boolean filtered) {
        this(null, suiteName, filtered);
    }
    
    /**
     * Creates a new instance of TestsuiteNode
     */
    TestsuiteNode(final Report report, final boolean filtered) {
        this(report, null, filtered);
    }
    
    /**
     *
     * @param  suiteName  name of the test suite, or {@code ANONYMOUS_SUITE}
     *                    in the case of anonymous suite
     * @see  ResultDisplayHandler#ANONYMOUS_SUITE
     */
    private TestsuiteNode(final Report report,
                          final String suiteName,
                          final boolean filtered) {
        super(report != null ? new TestsuiteNodeChildren(report, filtered)
                             : Children.LEAF);
        
        this.report = report;
        this.suiteName = (report != null) ? report.suiteClassName : suiteName;
        this.filtered = filtered;
        
        assert this.suiteName != null;
        
        setDisplayName();
        setIconBaseWithExtension(
                "org/netbeans/modules/junit/output/res/class.gif");     //NOI18N
    }
    
    /**
     */
    void displayReport(final Report report) {
        assert (this.report == null) && (report != null);
        assert report.suiteClassName.equals(this.suiteName)
               || (this.suiteName.equals(ResultDisplayHandler.ANONYMOUS_SUITE));
        
        this.report = report;
        suiteName = report.suiteClassName;
        
        setDisplayName();
        setChildren(new TestsuiteNodeChildren(report, filtered));
    }
    
    /**
     * Returns a report represented by this node.
     *
     * @return  the report, or <code>null</code> if this node represents
     *          a running test suite (no report available yet)
     */
    Report getReport() {
        return report;
    }
    
    /**
     */
    private void setDisplayName() {
        String bundleKey;
        boolean suiteNameKnown = true;

        String displayName;
        if (report != null) {
            boolean failed = containsFailed();
            boolean interrupted = report.isSuiteInterrupted();
            bundleKey = interrupted
                        ? (failed ? "MSG_TestsuiteFailedInterrupted"    //NOI18N
                                  : "MSG_TestsuiteInterrupted")         //NOI18N
                        : (failed ? "MSG_TestsuiteFailed"               //NOI18N
                                  : null);
        } else {
            if (!suiteName.equals(ResultDisplayHandler.ANONYMOUS_SUITE)) {
                bundleKey = "MSG_TestsuiteRunning";                     //NOI18N
            } else {
                bundleKey = "MSG_TestsuiteRunningNoname";               //NOI18N
                suiteNameKnown = false;
            }
        }

        if (bundleKey == null) {
            assert suiteName != null;
            displayName = suiteName;
        } else {
            displayName = suiteNameKnown
                          ? NbBundle.getMessage(getClass(), bundleKey, suiteName)
                          : NbBundle.getMessage(getClass(), bundleKey);
        }
        setDisplayName(displayName);
    }
    
    /**
     */
    @Override
    public String getHtmlDisplayName() {
        
        assert suiteName != null;
        
        StringBuilder buf = new StringBuilder(60);
        if (!suiteName.equals(ResultDisplayHandler.ANONYMOUS_SUITE)) {
            buf.append(suiteName);
        } else {
            buf.append(NbBundle.getMessage(getClass(),
                                           "MSG_TestsuiteNoname"));     //NOI18N
        }
        if (report != null) {
            final boolean containsFailed = containsFailed();
            final boolean interrupted = report.isSuiteInterrupted();

            if (containsFailed) {
                buf.append("&nbsp;&nbsp;");                             //NOI18N
                HtmlMarkupUtils.appendColourText(
                        buf, COLOR_FAILURE, "MSG_TestsuiteFailed_HTML");//NOI18N
            }
            if (interrupted) {
                buf.append("&nbsp;&nbsp;");                             //NOI18N
                HtmlMarkupUtils.appendColourText(
                        buf, COLOR_OK, "MSG_TestsuiteInterrupted_HTML");   //NOI18N
            }
            if (!containsFailed && !interrupted) {
                buf.append("&nbsp;&nbsp;");                             //NOI18N
                HtmlMarkupUtils.appendColourText(
                        buf, COLOR_OK, "MSG_TestsuitePassed_HTML");     //NOI18N
            }
        } else {
            buf.append("&nbsp;&nbsp;");                                 //NOI18N
            buf.append(NbBundle.getMessage(
                                    getClass(),
                                    "MSG_TestsuiteRunning_HTML"));      //NOI18N
        }
        return buf.toString();
    }
    
    /**
     */
    void setFiltered(final boolean filtered) {
        if (filtered == this.filtered) {
            return;
        }
        this.filtered = filtered;
        
        Children children = getChildren();
        if (children != Children.LEAF) {
            ((TestsuiteNodeChildren) children).setFiltered(filtered);
        }
    }
    
    /**
     */
    private boolean containsFailed() {
        return (report != null) && (report.failures + report.errors != 0);
    }
    
    @Override
    public SystemAction[] getActions(boolean context) {
        return new SystemAction[0];
    }
}
