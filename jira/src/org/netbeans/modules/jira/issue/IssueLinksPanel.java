/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.jira.issue;

import java.awt.EventQueue;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;
import org.jdesktop.layout.GroupLayout;
import org.jdesktop.layout.LayoutStyle;
import org.netbeans.modules.bugtracking.spi.Issue;
import org.netbeans.modules.bugtracking.spi.Repository;
import org.netbeans.modules.bugtracking.ui.issue.cache.IssueCache;
import org.netbeans.modules.bugtracking.util.LinkButton;
import org.netbeans.modules.jira.repository.JiraRepository;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 * Panel showing information about issue links.
 *
 * @author Jan Stola
 */
public class IssueLinksPanel extends JPanel {
    /** Issue whose links should be shown. */
    private NbJiraIssue issue;
    /** Maps linkId to the lists of outward and inward links. */
    private SortedMap<String,List<NbJiraIssue.LinkedIssue>[]> map = new TreeMap<String,List<NbJiraIssue.LinkedIssue>[]>();
    /** Pattern for the title of linked issue sections. */
    private String titlePattern;
    /** Icon for inward links. */
    private Icon inwardIcon;
    /** Icon for outward links. */
    private Icon outwardIcon;

    public IssueLinksPanel() {
        setLayout(new GridLayout(0,2));
        setBackground(UIManager.getColor("EditorPane.background")); // NOI18N
        titlePattern = NbBundle.getMessage(IssueLinksPanel.class, "IssueLinksPanel.thisIssue"); // NOI18N
        inwardIcon = new ImageIcon(ImageUtilities.loadImage("org/netbeans/modules/jira/resources/inwardLink.png")); // NOI18N
        outwardIcon = new ImageIcon(ImageUtilities.loadImage("org/netbeans/modules/jira/resources/subtask.png")); // NOI18N
    }

    void setIssue(NbJiraIssue issue) {
        this.issue = issue;
        recalculateMap();
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                reloadIssueDetails();
                EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        rebuildLayout();
                    }
                });
            }
        });
    }

    /** Maps issue key to issue summary. */
    private Map<String,String> summaryMap = new HashMap<String,String>();
    private void reloadIssueDetails() {
        summaryMap.clear();
        JiraRepository repository = issue.getRepository();
        IssueCache cache = repository.getIssueCache();
        for (NbJiraIssue.LinkedIssue linkedIssue : issue.getLinkedIssues()) {
            String issueKey = linkedIssue.getIssueKey();
            Issue izzue = cache.getIssue(issueKey);
            if (izzue == null) {
                izzue = repository.getIssue(issueKey);
            }
            String summary = izzue.getSummary();
            summaryMap.put(issueKey, summary);
        }
    }

    private void recalculateMap() {
        map.clear();
        for (NbJiraIssue.LinkedIssue linkedIssue : issue.getLinkedIssues()) {
            String linkId = linkedIssue.getLinkId();
            List<NbJiraIssue.LinkedIssue>[] lists = map.get(linkId);
            if (lists == null) {
                lists = new List[2];
                lists[0] = new LinkedList<NbJiraIssue.LinkedIssue>();
                lists[1] = new LinkedList<NbJiraIssue.LinkedIssue>();
                map.put(linkId, lists);
            }
            lists[linkedIssue.isInward() ? 1 : 0].add(linkedIssue);
        }
    }

    private void rebuildLayout() {
        removeAll();
        for (List<NbJiraIssue.LinkedIssue>[] lists : map.values()) {
            // Titles e.g. the first row
            for (int i=0; i<2; i++) {
                JLabel label = new JLabel();
                if (!lists[i].isEmpty()) {
                    String title = MessageFormat.format(titlePattern, lists[i].get(0).getLabel());
                    label.setText(title);
                }
                add(label);
            }
            // Linked issues
            for (int i=0; i<Math.max(lists[0].size(),lists[1].size()); i++) {
                for (int j=0; j<2; j++) {
                    if (i<lists[j].size()) {
                        JLabel iconLabel = new JLabel((j==0) ? outwardIcon : inwardIcon);
                        String issueKey = lists[j].get(i).getIssueKey();
                        LinkButton issueButton = new LinkButton(new OpenIssueAction(issue.getRepository(), issueKey));
                        JLabel summaryLabel = new JLabel(summaryMap.get(issueKey));
                        JPanel panel = new JPanel();
                        panel.setBackground(getBackground());
                        GroupLayout layout = new GroupLayout(panel);
                        layout.setHorizontalGroup(
                            layout.createSequentialGroup()
                                .add(iconLabel)
                                .add(issueButton)
                                .addPreferredGap(LayoutStyle.RELATED)
                                .add(summaryLabel, 0, 0, summaryLabel.getPreferredSize().width)
                        );
                        layout.setVerticalGroup(
                            layout.createParallelGroup(GroupLayout.BASELINE)
                                .add(iconLabel)
                                .add(issueButton)
                                .add(summaryLabel)
                        );
                        panel.setLayout(layout);
                        add(panel);
                    } else {
                        add(new JLabel());
                    }
                }
            }
        }
        ((JComponent)getParent()).revalidate();
        repaint();
    }

    static class OpenIssueAction extends AbstractAction {
        private Repository repository;
        private String issueKey;

        public OpenIssueAction(Repository repository, String issueKey) {
            this.repository = repository;
            this.issueKey = issueKey;
            putValue(Action.NAME, issueKey);
        }

        public void actionPerformed(ActionEvent e) {
            RequestProcessor.getDefault().post(new Runnable() {
               public void run() {
                   repository.getIssue(issueKey).open();
               }
            });
        }
    }

}
