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

package org.netbeans.modules.quicksearch.recent;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;
import java.util.prefs.Preferences;
import org.netbeans.modules.quicksearch.CommandEvaluator;
import org.netbeans.modules.quicksearch.ResultsModel;
import org.netbeans.modules.quicksearch.ResultsModel.ItemResult;
import org.openide.util.Exceptions;
import org.openide.util.NbPreferences;


/**
 * Recent Searches items storage and its persistance
 *
 * @author Jan Becicka
 * @authoe Max Sauer
 */
public class RecentSearches {
    
    private static final int MAX_ITEMS = 5;
    private static final long FIVE_DAYS = 86400000 * 5;

    private LinkedList<ItemResult> recent;
    private static RecentSearches instance;

    private RecentSearches() {
        recent = new LinkedList<ItemResult>() {

            @Override
            public String toString() {
                StringBuffer buf = new StringBuffer();
                for(ItemResult td : this) {
                    buf.append(td.getDisplayName() + ":" + td.getDate().getTime() + ",");
                }
                return buf.toString();
            }

        };
        readRecentFromPrefs(); //read recent searhces from preferences
    }
    
    public static RecentSearches getDefault() {
        if (instance==null) {
            instance = new RecentSearches();
        }
        return instance;
    } 
    
    public void add(ItemResult result) {
        Date now = new GregorianCalendar().getTime();

        // don't create duplicates, however poor-man's test only
        for (ItemResult ir : recent) {
            if (stripHTMLnames(ir.getDisplayName()).equals(
                    stripHTMLnames(result.getDisplayName()))) {
                ir.setDate(now);
                return;
            }
        }

        // ugly hack to not include special Maven setup search item
        if ("SearchSetup".equals(result.getAction().getClass().getSimpleName())) {
            return;
        }
        
        if (recent.size()>=MAX_ITEMS) {
            recent.removeLast();
        }
        result.setDate(now);
        recent.addFirst(result);
        prefs().put(RECENT_SEARCHES, stripHTMLnames(recent.toString()));
    }
    
    public List<ItemResult> getSearches() {
        LinkedList<ItemResult> fiveDayList = new LinkedList<ItemResult>();
        for (ItemResult ir : recent) {
            if ((new GregorianCalendar().getTime().getTime() - ir.getDate().getTime()) < FIVE_DAYS)
                fiveDayList.add(ir);
        }
        //provide only recent searches newer than five days
        return fiveDayList;
    }

    //preferences
    private static final String RECENT_SEARCHES = "recentSearches"; // NOI18N

    private Preferences prefs() {
        return NbPreferences.forModule(RecentSearches.class);
    }

    private void readRecentFromPrefs() {
        String[] items = prefs().get(RECENT_SEARCHES, "").split(","); // NOI18N
        if (items[0].length() != 0) {
            for (int i = 0; i < items.length; i++) {
                int semicolonPos = items[i].lastIndexOf(":"); // NOI18N
                if (semicolonPos >= 0) {
                    final String name = items[i].substring(0, semicolonPos);
                    final long time = Long.parseLong(items[i].substring(semicolonPos + 1));
                    ItemResult incomplete = new ItemResult(null, new FakeAction(name), name, new Date(time));
                    recent.add(incomplete);
                }
            }
        }
    }

    /**
     * Lazy initied action used for recent searches
     * In order to not init all recent searched item
     */
    public final class FakeAction implements Runnable {

        private String name; //display name to search for
        private Runnable action; //remembered action

        private FakeAction(String name) {
            this.name = name;
        }

        public void run() {
            if (action == null || action instanceof FakeAction) {
                ResultsModel model = ResultsModel.getInstance();
                CommandEvaluator.evaluate(stripHTMLandPackageNames(name), model);
                try {
                    Thread.sleep(350);
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                }
                int rSize = model.getSize();
                for (int j = 0; j < rSize; j++) {
                    ItemResult res = (ItemResult) model.getElementAt(j);
                    if (stripHTMLnames(res.getDisplayName()).equals(stripHTMLnames(name))) {
                        action = res.getAction();
                        if (!(action instanceof FakeAction)) {
                            action.run();
                            break;
                        }
                    }
                }
            } else {
                action.run();
            }
        }

        private String stripHTMLandPackageNames(String s) {
            s = stripHTMLnames(s);
            return s.replaceAll("\\(.*\\)", "").trim();
        }
    }

    private String stripHTMLnames(String s) {
        return s.replaceAll("<.*?>", "").trim();
    }

}
