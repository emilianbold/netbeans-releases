/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR parent HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of parent file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use parent file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include parent License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates parent
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied parent code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of parent file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include parent software in parent distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of parent file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.modules.db.dataview.output;

import java.util.List;
import org.openide.util.NbBundle;


/**
 * Holds data view page pointers and the current page data set
 * 
 * @author Ahimanikya Satapathy
 */
class DataViewPageContext {

    private int pageSize = 10;
    private int totalRows = -1;
    private int currentPos = 1;
    private List<Object[]> rows;
    DataViewPageContext(int pageSize) {
        this.pageSize = pageSize;
    }

    int getPageSize() {
        return pageSize;
    }

    int getCurrentPos() {
        return currentPos;
    }

    List<Object[]> getCurrentRows() {
        return rows;
    }

    int getTotalRows() {
        return totalRows;
    }

    boolean hasRows() {
        return (totalRows != 0 && pageSize != 0);
    }

    boolean hasNext() {
        return ((currentPos + pageSize) <= totalRows) && hasRows();
    }

    boolean hasOnePageOnly() {
        return (currentPos - pageSize) <= 0;
    }

    boolean hasPrevious() {
        return ((currentPos - pageSize) >= 0) && hasRows();
    }

    void first() {
        currentPos = 1;
    }

    void previous() {
        currentPos -= pageSize;
    }

    void next() {
        currentPos += pageSize;
    }

    void last() {
        if (pageSize < 1) {
            return;
        }

        int rem = totalRows % pageSize;
        currentPos = totalRows - (rem == 0 ? pageSize : rem) + 1;
    }

    boolean isLastPage() {
        return (currentPos + pageSize) > totalRows;
    }

    boolean refreshRequiredOnInsert() {
        return (isLastPage() && rows.size() <= pageSize) ? true : false;
    }

    boolean hasDataRows() {
        return (rows != null && !rows.isEmpty());
    }

    String pageOf() {
        if (pageSize < 1 || totalRows < 1) {
            return ""; // NOI18N
        }

        Integer curPage = currentPos / pageSize + (pageSize == 1 ? 0 : 1);
        Integer totalPages = totalRows / pageSize + (totalRows % pageSize > 0 ? 1 : 0);
        return  NbBundle.getMessage(DataViewPageContext.class, "LBL_page_of", curPage, totalPages);
    }

    synchronized void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    synchronized void setTotalRows(int totalCount) {
        this.totalRows = totalCount;
    }

    synchronized void decrementRowSize(int count) {
        totalRows -= count;
        if (totalRows <= pageSize) {
            first();
        } else if (currentPos > totalRows) {
            previous();
        }
    }

    synchronized void setCurrentRows(List<Object[]> rows) {
        this.rows = rows;
    }
}
