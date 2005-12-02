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

import java.io.IOException;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;

/**
 *
 * @author Marian Petras
 */
final class TestsuiteNode extends AbstractNode implements ChangeListener {
    
    /** */
    final Report report;
    final TestsuiteNodeChildren children;
    boolean containsFailed;
    
    /**
     * Creates a new instance of TestsuiteNode
     */
    TestsuiteNode(final Report report) {
        super(new TestsuiteNodeChildren(report));
        
        this.children = (TestsuiteNodeChildren) getChildren();
        this.report = report;
        
        setDisplayName();
        setIconBaseWithExtension(
                "org/netbeans/modules/junit/output/res/class.gif");     //NOI18N
        
        if (!report.isClosed()) {
            report.addChangeListener(WeakListeners.change(this, report));
        }
    }
    
    /**
     */
    Report getReport() {
        return report;
    }
    
    /**
     */
    private void setDisplayName() {
        containsFailed = (report.failures + report.errors != 0);
        
        String displayName;
        if (!report.isClosed()) {
            if (report.suiteClassName != null) {
                displayName = NbBundle.getMessage(
                                          getClass(),
                                          "MSG_TestsuiteRunning",       //NOI18N
                                          report.suiteClassName);
            } else {
                displayName = NbBundle.getMessage(
                                          getClass(),
                                          "MSG_TestsuiteRunningNoname");//NOI18N
            }
        } else {
            displayName = containsFailed
                          ? NbBundle.getMessage(
                                          getClass(),
                                          "MSG_TestsuiteFailed",        //NOI18N
                                          report.suiteClassName)
                          : report.suiteClassName;
        }
        setDisplayName(displayName);
    }
    
    /**
     */
    public String getHtmlDisplayName() {
        final boolean closed = report.isClosed();

        StringBuffer buf = new StringBuffer(60);
        String suiteName = report.suiteClassName;
        if (suiteName != null) {
            buf.append(suiteName);
            buf.append("&nbsp;&nbsp;");                                 //NOI18N
        } else {
            buf.append(NbBundle.getMessage(getClass(),
                                           "MSG_TestsuiteNoname"));     //NOI18N
            buf.append("&nbsp;");
        }
        if (closed) {
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
        Children children = getChildren();
        if (children != Children.LEAF) {
            ((TestsuiteNodeChildren) children).setFiltered(filtered);
        }
    }
    
    /**
     */
    public void stateChanged(ChangeEvent e) {
        if (report.isClosed()) {
            setDisplayName();
            children.update();
        }
    }
    
}
