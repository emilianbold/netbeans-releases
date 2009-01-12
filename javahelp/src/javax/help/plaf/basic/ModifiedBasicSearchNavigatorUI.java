/*
 * @(#)BasicSearchNavigatorUI.java	1.86 06/10/30
 * 
 * Copyright (c) 2006 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 * 
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the LICENSE file that accompanied this code.
 * 
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 * 
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 * 
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 */
/*
 * @(#) BasicSearchNavigatorUI.java 1.86 - last change made 10/30/06
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

    /** This is a version of C.A.R Hoare's Quick Sort
     * algorithm.  This will handle arrays that are already
     * sorted, and arrays with duplicate keys.<BR>
     *
     * If you think of a one dimensional array as going from
     * the lowest index on the left to the highest index on the right
     * then the parameters to this function are lowest index or
     * left and highest index or right.  The first time you call
     * this function it will be with the parameters 0, a.length - 1.
     *
     * @param a       a DefaultMutableTreeNode array
     * @param lo0     left boundary of array partition
     * @param hi0     right boundary of array partition
     */
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



