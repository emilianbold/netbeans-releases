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

package javax.help.plaf.basic;

import java.util.Arrays;
import java.util.Comparator;
import javax.help.*;
import java.util.Enumeration;
import java.util.Hashtable;
import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.plaf.ComponentUI;

public class ModifiedBasicSearchNavigatorUI extends BasicSearchNavigatorUI {

    public ModifiedBasicSearchNavigatorUI(JHelpSearchNavigator b) {
        super(b);
    }

    public static ComponentUI createUI(JComponent x) {
        return new ModifiedBasicSearchNavigatorUI((ModifiedJHelpSearchNavigator) x);
    }

    // The purpose is to find reasons of I127368
    @Override
    protected void addSubHelpSets(HelpSet hs) {
        for( Enumeration e = hs.getHelpSets(); e.hasMoreElements(); ) {
            HelpSet ehs = (HelpSet) e.nextElement();
                NavigatorView[] views = ehs.getNavigatorViews();
                for (int i = 0; i < views.length; i++) {
                    if(searchnav.canMerge(views[i])) {
                        try {
                            searchnav.merge(views[i]);
                        } catch (IllegalArgumentException ex) {
                            Hashtable params = views[i].getParameters();
                            Object data = null;
                            if (params != null)
                                data = params.get("data");
                            throw new IllegalArgumentException("View is invalid:\n" +
                                                               "   View Name: " +  views[i].getName() +
                                                               "   View Class: " + views[i].getClass().getName() +
                                                               "   View Params: " + params +
                                                               "   View Data: " + data +
                                                               "   HelpSet URL: " + views[i].getHelpSet().getHelpSetURL()
                                                               );
                        }
                    }
                }
                addSubHelpSets( ehs );
        }
    }

    @Override
    protected void setCellRenderer(NavigatorView view, JTree tree) {
        if (view == null) {
            return;
        }
        Map map = view.getHelpSet().getCombinedMap();
        tree.setCellRenderer(new ModifiedBasicSearchCellRenderer(map));
    }

    private boolean isTagged(SearchTOCItem item) {
        Enumeration searchHits = item.getSearchHits();
        while (searchHits.hasMoreElements()) {
            SearchHit hit = (SearchHit) searchHits.nextElement();
            if (hit.getBegin() >= Integer.MAX_VALUE / 4) {
                return true;
            }
        }
        return false;
    }

    @Override
    void quickSort(DefaultMutableTreeNode a[], int lo0, int hi0) {
        if (hi0 > lo0)
        Arrays.sort(a, lo0, hi0, new Comparator() {

            public int compare(Object node1, Object node2) {
                SearchTOCItem item1, item2;
                double confidence1, confidence2;
                int hits1, hits2;

                item1 = (SearchTOCItem) ((DefaultMutableTreeNode) node1).getUserObject();
                confidence1 = item1.getConfidence();
                hits1 = item1.hitCount();

                item2 = (SearchTOCItem) ((DefaultMutableTreeNode) node2).getUserObject();
                confidence2 = item2.getConfidence();
                hits2 = item2.hitCount();

                boolean tagged1 = isTagged(item1);
                boolean tagged2 = isTagged(item2);

                if (tagged1 && !tagged2) {
                    return -1;
                }
                if (tagged2 && !tagged1) {
                    return 1;
                }

                // confidence is a penality. The lower the better
                if (confidence1 > confidence2) {
                    // node1 is less than node2
                    return 1;
                } else if (confidence1 < confidence2) {
                    // node1 is greater than node2
                    return -1;
                } else {
                    // confidences are the same check the hits
                    if (hits1 < hits2) {
                        // node1 is less than node2
                        return 1;
                    } else if (hits1 > hits2) {
                        // node2 is greater than node2
                        return -1;
                    }
                }
                // nodes1 and nodes2 are equivalent
                return item1.getName().compareTo(item2.getName());
            }
        });
    }

    private void swap(DefaultMutableTreeNode a[], int i, int j) {
        DefaultMutableTreeNode T;
        T = a[i];
        a[i] = a[j];
        a[j] = T;

    }
}



