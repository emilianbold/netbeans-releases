/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
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
 * Contributor(s):
 */
package org.netbeans.lib.terminalemulator.support;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.lib.terminalemulator.Coord;
import org.netbeans.lib.terminalemulator.Extent;
import org.netbeans.lib.terminalemulator.LogicalLineVisitor;
import org.netbeans.lib.terminalemulator.Term;

/**
 *
 * @author Ilia Gromov
 */
public class HiglightMatchesFindState implements FindState {

    private final FindState findState;
    private final Term term;

    public HiglightMatchesFindState(FindState findState, Term term) {
        this.findState = findState;
        this.term = term;
    }

    @Override
    public void setPattern(String pattern) {
        findState.setPattern(pattern);
    }

    @Override
    public String getPattern() {
        return findState.getPattern();
    }

    @Override
    public void setVisible(boolean visible) {
        findState.setVisible(visible);
        if (!visible) {
            term.clearHighlight();
        }
    }

    @Override
    public boolean isVisible() {
        return findState.isVisible();
    }

    private void findAll() {
        String patternString = getPattern();
        if (patternString == null) {
            return;
        }
        Pattern pattern = Pattern.compile(patternString, Pattern.LITERAL);
        List<Extent> extents = new ArrayList<>();
        term.visitLogicalLines(null, null, new LogicalLineVisitor() {
            @Override
            public boolean visit(int line, Coord begin, Coord end, String text) {
                Matcher matcher = pattern.matcher(text);
                while (matcher.find()) {
                    Extent extent = term.extentInLogicalLine(begin, matcher.start(), patternString.length());
                    extents.add(extent);
                }
                return true;
            }
        });
        term.setHighlightExtents(extents.toArray(new Extent[0]));
    }

    @Override
    public void next() {
        if (getBoolean(FIND_HIGHLIGHT_SEARCH)) {
            findAll();
        }
        findState.next();
    }

    @Override
    public void prev() {
        if (getBoolean(FIND_HIGHLIGHT_SEARCH)) {
            findAll();
        }
        findState.prev();

    }

    @Override
    public Status getStatus() {
        return findState.getStatus();
    }

    @Override
    public Map<String, Object> getProperties() {
        return findState.getProperties();
    }

    @Override
    public void setProperties(Map<String, Object> properties) {
        findState.setProperties(properties);
        properties.forEach((key, value) -> {
            propertyChanged(key, value);
        });
    }

    private boolean getBoolean(String name) {
        Object property = getProperty(name);
        if (property instanceof Boolean) {
            return (boolean) property;
        }
        return false;
    }

    private void propertyChanged(String key, Object value) {
        if (FIND_HIGHLIGHT_SEARCH.equals(key) && value instanceof Boolean) {
            boolean newVal = (boolean) value;
            if (newVal) {
                findAll();
            } else {
                term.clearHighlight();
            }
        }
    }
}
