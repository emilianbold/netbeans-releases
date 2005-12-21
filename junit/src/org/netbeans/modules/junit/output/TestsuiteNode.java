/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
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
    
    private final String suiteName;
    private Report report;
    private boolean filtered;
    
    /**
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
     */
    private TestsuiteNode(final Report report,
                          final String suiteName,
                          final boolean filtered) {
        super(report != null ? new TestsuiteNodeChildren(report, filtered)
                             : Children.LEAF);
        
        this.report = report;
        this.suiteName = (report != null) ? report.suiteClassName : suiteName;
        this.filtered = filtered;
        
        setDisplayName();
        setIconBaseWithExtension(
                "org/netbeans/modules/junit/output/res/class.gif");     //NOI18N
    }
    
    /**
     */
    void displayReport(final Report report) {
        assert (this.report == null) && (report != null);
        assert report.suiteClassName.equals(this.suiteName);
        
        this.report = report;
        
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
            if (suiteName != null) {
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
        StringBuffer buf = new StringBuffer(60);
        if (suiteName != null) {
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
