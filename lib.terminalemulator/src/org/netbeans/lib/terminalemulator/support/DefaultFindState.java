/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.lib.terminalemulator.support;

import org.netbeans.lib.terminalemulator.Coord;
import org.netbeans.lib.terminalemulator.Extent;
import org.netbeans.lib.terminalemulator.LogicalLineVisitor;
import org.netbeans.lib.terminalemulator.Term;

public class DefaultFindState implements FindState {

    private final Term term;
    private boolean visible = false;
    private String pattern;
    private boolean tentative;
    private Coord dot = null;
    private Status status = Status.EMPTYPATTERN;
    private boolean found = false;

    public DefaultFindState(Term term) {
        super();
        this.term = term;
    }

    private LogicalLineVisitor forwardVisitor = new LogicalLineVisitor() {

        public boolean visit(int line, Coord begin, Coord end, String text) {
            int i = text.indexOf(pattern);
            if (i == -1) {
                return true;    // keep going
            }
            if (!tentative) {
                Extent extent = term.extentInLogicalLine(begin, i, pattern.length());
                term.setSelectionExtent(extent);
                dot = term.advance(extent.end);
            }
            found = true;
            return false;       // stop
        }
    };

    private LogicalLineVisitor backwardVisitor = new LogicalLineVisitor() {

        public boolean visit(int line, Coord begin, Coord end, String text) {
            int i = text.lastIndexOf(pattern);
            if (i == -1) {
                return true;        // keep going
            }
            if (!tentative) {
                Extent extent = term.extentInLogicalLine(begin, i, pattern.length());
                term.setSelectionExtent(extent);
                dot = term.backup(extent.end);
            }
            found = true;
            return false;          // stop
        }
    };

    public void setPattern(String pattern) {
        this.pattern = pattern;
        this.found = false;
        if (pattern == null || pattern.equals(""))
            status = Status.EMPTYPATTERN;
        else
            status = Status.OK;
    }

    public String getPattern() {
        return pattern;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public boolean isVisible() {
        return visible;
    }

    public void next() {
        if (status == Status.EMPTYPATTERN)
            return;

        found = false;      // dot and found set by forwardVisitor

        tentative = false;
        term.visitLogicalLines(dot, null, forwardVisitor);

        if (found) {
            term.possiblyNormalize(dot);
            status = Status.OK;
        } else {
            // see if it will be found if wrapped
            tentative = true;
            term.visitLogicalLines(null, null, forwardVisitor);
            if (found) {
                dot = null;
                status = Status.WILLWRAP;
            } else {
                status = Status.NOTFOUND;
            }
        }
    }

    public void prev() {
        if (status == Status.EMPTYPATTERN)
            return;

        found = false;      // dot and found set by forwardVisitor

        tentative = false;
        term.reverseVisitLogicalLines(null, dot, backwardVisitor);

        if (found) {
            term.possiblyNormalize(dot);
            status = Status.OK;
        } else {
            // see if it will be found if wrapped
            tentative = true;
            term.reverseVisitLogicalLines(null, null, backwardVisitor);
            if (found) {
                dot = null;
                status = Status.WILLWRAP;
            } else {
                status = Status.NOTFOUND;
            }
        }
    }

    public Status getStatus() {
        return status;
    }
}
