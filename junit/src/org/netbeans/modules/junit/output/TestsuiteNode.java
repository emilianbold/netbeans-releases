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

package org.netbeans.modules.junit.output;

import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.NbBundle;

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
               || (this.suiteName == ResultDisplayHandler.ANONYMOUS_SUITE);
        
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
        String displayName;
        if (report == null) {
            if (suiteName != ResultDisplayHandler.ANONYMOUS_SUITE) {
                displayName = NbBundle.getMessage(
                                          getClass(),
                                          "MSG_TestsuiteRunning",       //NOI18N
                                          suiteName);
            } else {
                displayName = NbBundle.getMessage(
                                          getClass(),
                                          "MSG_TestsuiteRunningNoname");//NOI18N
            }
        } else {
            boolean containsFailed = containsFailed();
            displayName = containsFailed
                          ? NbBundle.getMessage(
                                          getClass(),
                                          "MSG_TestsuiteFailed",        //NOI18N
                                          suiteName)
                          : suiteName;
        }
        setDisplayName(displayName);
    }
    
    /**
     */
    public String getHtmlDisplayName() {
        
        assert suiteName != null;
        
        StringBuffer buf = new StringBuffer(60);
        if (suiteName != ResultDisplayHandler.ANONYMOUS_SUITE) {
            buf.append(suiteName);
            buf.append("&nbsp;&nbsp;");                                 //NOI18N
        } else {
            buf.append(NbBundle.getMessage(getClass(),
                                           "MSG_TestsuiteNoname"));     //NOI18N
            buf.append("&nbsp;");
        }
        if (report != null) {
            final boolean containsFailed = containsFailed();

            buf.append("<font color='#");                               //NOI18N
            buf.append(containsFailed ? "FF0000'>" : "00CC00'>");       //NOI18N
            buf.append(NbBundle.getMessage(
                                    getClass(),
                                    containsFailed
                                    ? "MSG_TestsuiteFailed_HTML"        //NOI18N
                                    : "MSG_TestsuitePassed_HTML"));     //NOI18N
            buf.append("</font>");                                      //NOI18N
        } else {
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
    
}
