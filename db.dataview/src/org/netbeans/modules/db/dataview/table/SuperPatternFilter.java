/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.db.dataview.table;

/**
 *
 * @author ahimanikya
 */
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import org.jdesktop.swingx.decorator.Filter;
import static org.netbeans.modules.db.dataview.table.SuperPatternFilter.MODE.LITERAL_FIND;

public class SuperPatternFilter extends Filter {

    private List<Integer> toPrevious;
    Pattern pattern;
    String filterStr = "";
    MODE mode;
    private static final String UNKOWN_MODE = "unknown mode";

    public static enum MODE {

        LITERAL_FIND, REGEX_FIND, LITERAL_MATCH, REGEX_MATCH
    }

    public SuperPatternFilter(final int col) {
        super(col);
        setFilterStr(null, LITERAL_FIND);
    }

    public boolean isFilterSetTo(final String rack, final MODE matchMode) {
        return filterStr.equals(rack) && mode == matchMode;
    }

    public void setFilterStr(final String filterStr, final MODE mode) {
        if (filterStr == null || this.filterStr.equals(filterStr) && this.mode == mode) {
            return;
        }
        this.filterStr = filterStr;
        this.mode = mode;
        switch (mode) {
            case LITERAL_FIND:
            case LITERAL_MATCH:
                break;
            case REGEX_FIND:
            case REGEX_MATCH:
                final String filterStr2;
                if (filterStr == null || filterStr.length() == 0) {
                    filterStr2 = ".*";
                } else {
                    filterStr2 = Pattern.quote(filterStr);
                }
                pattern = Pattern.compile(filterStr2, 0);
                break;
            default:
                throw new RuntimeException(UNKOWN_MODE);
        }
        refresh();
    }

    @Override
    protected void reset() {
        toPrevious.clear();
        final int inputSize = getInputSize();
        fromPrevious = new int[inputSize];
        for (int i = 0; i < inputSize; i++) {
            fromPrevious[i] = -1;
        }
    }

    @Override
    protected void filter() {
        final int inputSize = getInputSize();
        int current = 0;
        for (int i = 0; i < inputSize; i++) {
            if (test(i)) {
                toPrevious.add(i);
                fromPrevious[i] = current++;
            }
        }
    }

    public boolean test(final int row) {
        final int colIdx = getColumnIndex();
        if (!adapter.isTestable(colIdx)) {
            return false;
        }
        return testValue((String) getInputValue(row, colIdx));
    }

    boolean testValue(final String valueStr) {
        if (valueStr == null) {
            return false;
        }
        switch (mode) {
            case LITERAL_FIND:
                if (filterStr == null || filterStr.length() == 0) {
                    return true;
                } else {
                    return valueStr.toUpperCase().contains(filterStr.toUpperCase());
                }
            case LITERAL_MATCH:
                if (filterStr == null || filterStr.length() == 0) {
                    return true;
                } else {
                    return filterStr.equals(valueStr);
                }
            case REGEX_FIND:
                return pattern.matcher(valueStr).find();
            case REGEX_MATCH:
                return pattern.matcher(valueStr).matches();
            default:
                throw new RuntimeException(UNKOWN_MODE);
        }
    }

    @Override
    public int getSize() {
        return toPrevious.size();
    }

    @Override
    protected int mapTowardModel(final int row) {
        return toPrevious.get(row);
    }

    @Override
    protected void init() {
        toPrevious = new ArrayList<Integer>();
    }
}


