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

package org.netbeans.editor.ext;

import java.awt.Dimension;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.AbstractListModel;
import org.openide.util.NbBundle;


/**
* Code completion view component interface. It best fits the <tt>JList</tt>
* but some users may require something else e.g. JTable.
*
* @author Miloslav Metelka
* @version 1.00
*/

public class ListCompletionView extends JList implements CompletionView {

    ListCellRenderer renderer;
    ListCellRenderer defaultRenderer;
    
    public ListCompletionView() {
        this(null);
    }

    public ListCompletionView(ListCellRenderer renderer) {
        setSelectionMode( javax.swing.ListSelectionModel.SINGLE_SELECTION );
        this.renderer = renderer;
        defaultRenderer = getCellRenderer();
        /*
        if (renderer != null) {
            setCellRenderer(renderer);
        }
         */
        ResourceBundle bundle = NbBundle.getBundle(org.netbeans.editor.BaseKit.class);
        getAccessibleContext().setAccessibleName(bundle.getString("ACSN_CompletionView"));
        getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_CompletionView"));
        
        
    }

    /** Populate the view with the result from a query. */
    public void setResult(CompletionQuery.Result result) {
        if (result != null) {
            setResult(result.getData());
        } else {
            setResult(Collections.EMPTY_LIST);
        }
    }
    
    public void setResult(List data) {
        if (data != null) {
            if (data.size() == 0) {
                setCellRenderer(defaultRenderer);
                data = new LinkedList();
//                data.add("<" + LocaleSupport.getString("no-matching-item-found") + ">"); // NOI18N
                setModel(new Model(data));
                clearSelection();
            } else {
                if (renderer != null) {
                    setCellRenderer(renderer);
                }
                setModel(new Model(data));
                setSelectedIndex(0);
            }
        }
    }

    public void displayWaitStatus() {
        if (getCellRenderer() == defaultRenderer) {
            List data = new LinkedList();
            data.add(NbBundle.getBundle(org.netbeans.editor.BaseKit.class).
                    getString("please-wait")); // NOI18N
            setModel(new Model(data));
            clearSelection();
        }
    }
    
    public boolean showingData() {
        return getCellRenderer() == renderer;
    }
    
    /** Force the list to ignore the visible-row-count property */
    public Dimension getPreferredScrollableViewportSize() {
        return getPreferredSize();
    }

    public void up() {
        if (getModel().getSize() > 0) {
            setSelectedIndex(getSelectedIndex() - 1);
            ensureIndexIsVisible(getSelectedIndex());
        }
    }

    public void down() {
        int lastInd = getModel().getSize() - 1;
        if (lastInd >= 0) {
            setSelectedIndex(Math.min(getSelectedIndex() + 1, lastInd));
            ensureIndexIsVisible(getSelectedIndex());
        }
    }

    public void pageUp() {
        if (getModel().getSize() > 0) {
            int pageSize = Math.max(getLastVisibleIndex() - getFirstVisibleIndex(), 0);
            int ind = Math.max(getSelectedIndex() - pageSize, 0);

            setSelectedIndex(ind);
            ensureIndexIsVisible(ind);
        }
    }

    public void pageDown() {
        int lastInd = getModel().getSize() - 1;
        if (lastInd >= 0) {
            int pageSize = Math.max(getLastVisibleIndex() - getFirstVisibleIndex(), 0);
            int ind = Math.min(getSelectedIndex() + pageSize, lastInd);

            setSelectedIndex(ind);
            ensureIndexIsVisible(ind);
        }
    }

    public void begin() {
        if (getModel().getSize() > 0) {
            setSelectedIndex(0);
            ensureIndexIsVisible(0);
        }
    }

    public void end() {
        int lastInd = getModel().getSize() - 1;
        if (lastInd >= 0) {
            setSelectedIndex(lastInd);
            ensureIndexIsVisible(lastInd);
        }
    }

    public void setVisible(boolean visible) {
        // ??? never called
//        System.err.println("ListCompletionView.setVisible(" + visible + ")");
        super.setVisible(visible);
    }
    
    static class Model extends AbstractListModel {

        List data;

        static final long serialVersionUID = 3292276783870598274L;

        public Model(List data) {
            this.data = data;
        }

        public int getSize() {
            return data.size();
        }

        public Object getElementAt(int index) {
            return (index >= 0 && index < data.size()) ? data.get(index) : null;
        }

        List getData() {
            return data;
        }

    }

}
