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

package org.netbeans.modules.gsf.testrunner.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import org.netbeans.modules.gsf.testrunner.api.Report;
import org.netbeans.modules.gsf.testrunner.api.Testcase;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;

/**
 *
 * @author Marian Petras
 */
final class TestsuiteNodeChildren extends Children.Keys<Testcase> {

    /** */
    private static final Node[] EMPTY_NODE_ARRAY = new Node[0];

    /** */
    private final Report report;
    /** */
    private boolean filtered;
    /** */
    private boolean live = true;         //PENDING - temporary (should be false)

    /*
     * PENDING - threading, sychronization
     */
    
    /**
     * Creates a new instance of TestsuiteNodeChildren
     */
    TestsuiteNodeChildren(final Report report, final boolean filtered) {
        this.report = report;
        this.filtered = filtered;
    }
    
    /**
     */
    @Override
    protected void addNotify() {
        super.addNotify();
        
        if (live) {
            setKeys(report.getTests());
        }
        //live = true;                          //PENDING
    }
    
    /**
     */
    @Override
    protected void removeNotify() {
        super.removeNotify();
        
        final Collection<Testcase> emptySet = Collections.emptySet();
        setKeys(emptySet);
        //live = false;                         //PENDING
    }
    
    /**
     */
    protected Node[] createNodes(final Testcase testcase) {
        if (filtered && !Status.isFailure(testcase.getStatus())) {
            return EMPTY_NODE_ARRAY;
        }
        return new Node[] {testcase.getSession().getNodeFactory().createTestMethodNode(testcase, report.getProject())};
    }
    
    /**
     */
    void setFiltered(final boolean filtered) {
        if (filtered == this.filtered) {
            return;
        }
        this.filtered = filtered;
        
        if ((report.getErrors() + report.getFailures()) == report.getTotalTests()) {
            return;
        }
                
        if (isInitialized()) {
            for (Testcase testcase : report.getTests()) {
                if (!Status.isFailure(testcase.getStatus())) {
                    refreshKey(testcase);
                }
            }
        }
    }

}
