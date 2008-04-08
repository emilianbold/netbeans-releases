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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.sql.framework.ui.view.graph;

import java.util.Vector;

import javax.swing.AbstractListModel;


import com.sun.sql.framework.exception.BaseException;
import net.java.hulp.i18n.Logger;
import org.netbeans.modules.etl.logger.Localizer;


/**
 * This class represents model for SQLCaseArea
 * 
 * @author Ritesh Adval
 */
public class CaseListModel extends AbstractListModel {

    private Vector listItems = new Vector();
    private static transient final Logger mLogger = Logger.getLogger(CaseListModel.class.getName());
    private static transient final Localizer mLoc = Localizer.get();

    /** Creates a new instance of CaseListModel */
    public CaseListModel() {
    }

    /**
     * Returns the value at the specified index.
     * 
     * @param index the requested index
     * @return the value at <code>index</code>
     */
    public Object getElementAt(int index) {
        if (index < listItems.size()) {
            return listItems.get(index);
        }

        return null;
    }

    /**
     * Returns the length of the list.
     * 
     * @return the length of the list
     */
    public int getSize() {
        return listItems.size();
    }

    /**
     * always add element not at the end but before last element
     * 
     * @param val value to add
     */
    public void add(Object val) {
        if (!listItems.contains(val)) {
            listItems.add(val);
            int idx = listItems.indexOf(val);
            fireIntervalAdded(this, idx, idx);
        }
    }

    /**
     * add element at the specified index
     * 
     * @param row row index
     * @param val object value
     */
    public void add(int row, Object val) {
        listItems.add(row, val);
        fireIntervalAdded(this, row, row);
    }

    /**
     * get index of an object
     * 
     * @param val object whose index needs to be found
     * @param return index of object
     */
    public int indexOf(Object val) {
        return listItems.indexOf(val);
    }

    /**
     * remove an object for the model
     * 
     * @param val object to be removed
     */
    public boolean remove(Object val) throws BaseException {
        if (listItems.size() == 1) {
            return false;
        }

        if (!listItems.contains(val)) {
            String nbBundle1 = mLoc.t("BUND407: Cannot remove selected when condition.");
            throw new BaseException(nbBundle1.substring(15));
        }

        int idx = listItems.indexOf(val);
        listItems.remove(idx);
        fireIntervalRemoved(this, idx, idx);

        return true;
    }
}

