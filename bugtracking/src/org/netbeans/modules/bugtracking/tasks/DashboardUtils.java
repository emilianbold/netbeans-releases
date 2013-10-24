/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.bugtracking.tasks;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Image;
import java.io.CharConversionException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.bugtracking.IssueImpl;
import org.netbeans.modules.bugtracking.RepositoryImpl;
import org.netbeans.modules.bugtracking.RepositoryRegistry;
import org.netbeans.modules.bugtracking.spi.IssueScheduleInfo;
import org.netbeans.modules.bugtracking.spi.IssueStatusProvider;
import org.netbeans.modules.bugtracking.tasks.cache.DashboardStorage;
import org.netbeans.modules.bugtracking.tasks.cache.TaskEntry;
import org.netbeans.modules.bugtracking.tasks.dashboard.CategoryNode;
import org.netbeans.modules.bugtracking.tasks.dashboard.DashboardViewer;
import org.netbeans.modules.bugtracking.tasks.dashboard.RepositoryNode;
import org.netbeans.modules.bugtracking.tasks.dashboard.TaskNode;
import org.netbeans.modules.bugtracking.ui.issue.IssueAction;
import org.netbeans.modules.bugtracking.util.UIUtils;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.actions.FindAction;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.SharedClassObject;
import org.openide.xml.XMLUtil;

/**
 *
 * @author jpeska
 */
public class DashboardUtils {

    private final static int VISIBLE_START_CHARS = 5;
    private final static String BOLD_START_SUBSTITUTE = "$$$BOLD_START$$$"; //NOI18
    private final static String BOLD_END_SUBSTITUTE = "$$$BOLD_END$$$"; //NOI18
    private static final String NEW_COLOR = UIUtils.getColorString(UIUtils.getTaskNewColor());
    private static final String mODIFIED_COLOR = UIUtils.getColorString(UIUtils.getTaskModifiedColor());

    private static final Image SCHEDULE_ICON = ImageUtilities.loadImage("org/netbeans/modules/bugtracking/tasks/resources/schedule.png", true); //NOI18
    private static final Image SCHEDULE_WARNING_ICON = ImageUtilities.loadImage("org/netbeans/modules/bugtracking/tasks/resources/schedule_warning.png", true); //NOI18

    private static final int SCHEDULE_NOT_IN_SCHEDULE = 0;
    private static final int SCHEDULE_IN_SCHEDULE = 1;
    private static final int SCHEDULE_AFTER_DUE = 2;

    public static String getCategoryDisplayText(CategoryNode categoryNode) {
        String categoryName = categoryNode.getCategory().getName();
        boolean containsActiveTask = DashboardViewer.getInstance().containsActiveTask(categoryNode);
        return getTopLvlDisplayText(containsActiveTask, categoryName, categoryNode.isOpened());
    }

    public static String getRepositoryDisplayText(RepositoryNode repositoryNode) {
        String repositoryName = repositoryNode.getRepository().getDisplayName();
        boolean containsActiveTask = DashboardViewer.getInstance().containsActiveTask(repositoryNode);
        return getTopLvlDisplayText(containsActiveTask, repositoryName, repositoryNode.isOpened());
    }

    private static String getTopLvlDisplayText(boolean containsActiveTask, String name, boolean isOpened) {
        String displayName;
        try {
            name = XMLUtil.toElementContent(name);
        } catch (CharConversionException ex) {
        }
        String activeText = containsActiveTask ? "<b>" + name + "</b>" : name; //NOI18N
        if (!isOpened) {
            displayName = "<html><strike>" + activeText + "</strike><html>"; //NOI18N
        } else {
            displayName = "<html>" + activeText + "<html>";
        }
        return displayName;
    }

    public static String getTaskPlainDisplayText(IssueImpl task, JComponent component, int maxWidth) {
        return computeFitText(component, maxWidth, getTaskDisplayName(task), false);
    }

    public static String getTaskDisplayText(IssueImpl task, JComponent component, int maxWidth, boolean active, boolean hasFocus) {
        String fitText = computeFitText(component, maxWidth, getTaskDisplayName(task), active); //NOI18N

        boolean html = false;
        String activeText = getFilterBoldText(fitText);
        if (activeText.length() != fitText.length()) {
            html = true;
        }
        if (active) {
            activeText = BOLD_START_SUBSTITUTE + fitText + BOLD_END_SUBSTITUTE;
            html = true;
        }

        activeText = replaceSubstitutes(activeText);
        if (task.isFinished()) {
            activeText = "<strike>" + activeText + "</strike>"; //NOI18N
            html = true;
        }
        return getTaskAnotatedText(activeText, task.getStatus(), hasFocus, html);
    }

    public static String computeFitText(JComponent component, int maxWidth, String text, boolean bold) {
        if (text == null) {
            text = ""; // NOI18N
        }
        if (text.length() <= VISIBLE_START_CHARS + 3) {
            return text;
        }
        FontMetrics fm;
        if (bold) {
            fm = component.getFontMetrics(component.getFont().deriveFont(Font.BOLD));
        } else {
            fm = component.getFontMetrics(component.getFont());
        }
        int width = maxWidth;

        String sufix = "..."; // NOI18N
        int sufixLength = fm.stringWidth(sufix + " "); //NOI18N
        int desired = width - sufixLength;
        if (desired <= 0) {
            return text;
        }

        for (int i = 0; i <= text.length() - 1; i++) {
            String prefix = text.substring(0, i);
            int swidth = fm.stringWidth(prefix);
            if (swidth >= desired) {
                if (fm.stringWidth(text.substring(i + 1)) <= fm.stringWidth(sufix)) {
                    return text;
                }
                return prefix.length() > 0 ? prefix + sufix : text;
            }
        }
        return text;
    }

    public static String getTaskAnotatedText(IssueImpl task) {
        return getTaskAnotatedText(getTaskDisplayName(task), task.getStatus(), false, false);
    }

    private static String getTaskAnotatedText(String text, IssueStatusProvider.Status status, boolean hasFocus, boolean isHTML) {
        if (status == IssueStatusProvider.Status.INCOMING_NEW && !hasFocus) {
            text = escapeXmlChars(text);
            text = "<html><font color=\"" + NEW_COLOR + "\">" + text + "</font></html>"; //NOI18N
        } else if (status == IssueStatusProvider.Status.INCOMING_MODIFIED && !hasFocus) {
            text = escapeXmlChars(text);
            text = "<html><font color=\"" + mODIFIED_COLOR + "\">" + text + "</font></html>"; //NOI18N
        } else if (isHTML) {
            text = escapeXmlChars(text);
            text = "<html>" + text + "</html>"; //NOI18N
        }
        return text;
    }

    private static String escapeXmlChars(String text) {
        String result = text;
        try {
            result = XMLUtil.toElementContent(text);
        } catch (CharConversionException ex) {
        }
        return result;
    }

    private static String getTaskDisplayName(IssueImpl task) {
        String displayName = task.getDisplayName();
        if (displayName.startsWith("#")) {
            displayName = displayName.replaceFirst("#", "");
        }
        return displayName;
    }

    private static String getFilterBoldText(String fitText) {
        String filterText = DashboardTopComponent.findInstance().getFilterText();
        if (!filterText.equals("")) { //NOI18N
            int searchIndex = 0;
            StringBuilder sb = new StringBuilder(fitText);

            int index = sb.toString().toLowerCase().indexOf(filterText.toLowerCase(), searchIndex);
            while (index != -1) {
                sb.insert(index, BOLD_START_SUBSTITUTE);
                index = index + BOLD_START_SUBSTITUTE.length() + filterText.length();
                sb.insert(index, BOLD_END_SUBSTITUTE);
                searchIndex = index + BOLD_END_SUBSTITUTE.length();
                index = sb.toString().toLowerCase().indexOf(filterText.toLowerCase(), searchIndex);
            }
            return sb.toString();
        } else {
            return fitText;
        }
    }

    public static String getFindActionMapKey() {
        return SharedClassObject.findObject(FindAction.class, true).getActionMapKey().toString();
    }

    private static String replaceSubstitutes(String text) {
        text = text.replace(BOLD_START_SUBSTITUTE, "<b>"); //NOI18N
        return text.replace(BOLD_END_SUBSTITUTE, "</b>"); //NOI18N
    }

    public static void quickSearchTask(RepositoryImpl repositoryImpl) {
        JButton open = new JButton(NbBundle.getMessage(DashboardTopComponent.class, "OPTION_Open"));
        open.setEnabled(false);
        JButton cancel = new JButton(NbBundle.getMessage(DashboardTopComponent.class, "OPTION_Cancel"));

        QuickSearchPanel quickSearchPanel = new QuickSearchPanel(repositoryImpl);
        NotifyDescriptor quickSearchDialog = new NotifyDescriptor(
                quickSearchPanel,
                NbBundle.getMessage(DashboardTopComponent.class, "LBL_QuickTitle", repositoryImpl.getDisplayName()), //NOI18N
                NotifyDescriptor.OK_CANCEL_OPTION,
                NotifyDescriptor.PLAIN_MESSAGE,
                new Object[]{open, cancel},
                open);
        quickSearchDialog.setValid(false);
        QuickSearchListener quickSearchListener = new QuickSearchListener(quickSearchPanel, open);
        quickSearchPanel.addQuickSearchListener(quickSearchListener);
        Object result = DialogDisplayer.getDefault().notify(quickSearchDialog);
        if (result == open) {
            IssueImpl issueImpl = quickSearchPanel.getSelectedTask();
            IssueAction.openIssue(issueImpl.getRepositoryImpl(), issueImpl.getID());
            Category selectedCategory = quickSearchPanel.getSelectedCategory();
            if (selectedCategory != null) {
                DashboardViewer.getInstance().addTaskToCategory(selectedCategory, new TaskNode(issueImpl, null));
            }
        }
        quickSearchPanel.removeQuickSearchListener(quickSearchListener);
    }

    private static class QuickSearchListener implements ChangeListener {

        private QuickSearchPanel quickSearchPanel;
        private JButton open;

        public QuickSearchListener(QuickSearchPanel quickSearchPanel, JButton open) {
            this.quickSearchPanel = quickSearchPanel;
            this.open = open;
        }

        @Override
        public void stateChanged(ChangeEvent e) {
            IssueImpl selectedTask = quickSearchPanel.getSelectedTask();
            open.setEnabled(selectedTask != null);
        }
    }

    public static boolean isRepositoryOpened(String repositoryId) {
        List<String> closedIds = DashboardStorage.getInstance().readClosedRepositories();
        return !closedIds.contains(repositoryId);
    }

    public static void loadCategory(Category category) {
        DashboardStorage storage = DashboardStorage.getInstance();
        List<TaskEntry> taskEntries = storage.readCategory(category.getName());
        category.setTasks(loadTasks(taskEntries));
    }

    public static Collection<RepositoryImpl> getRepositories() {
        return RepositoryRegistry.getInstance().getKnownRepositories(false, true);
    }

    private static List<IssueImpl> loadTasks(List<TaskEntry> taskEntries) {
        List<IssueImpl> tasks = new ArrayList<IssueImpl>(taskEntries.size());
        Map<String, List<String>> repository2Ids = new HashMap<String, List<String>>();

        for (TaskEntry taskEntry : taskEntries) {
            List<String> idList = repository2Ids.get(taskEntry.getRepositoryId());
            if (idList == null) {
                idList = new LinkedList<String>();
                repository2Ids.put(taskEntry.getRepositoryId(), idList);
            }
            idList.add(taskEntry.getIssueId());
        }
        for (Entry<String, List<String>> e : repository2Ids.entrySet()) {
            RepositoryImpl repository = getRepository(e.getKey());
            if (repository != null) {
                List<String> l = e.getValue();
                Collection<IssueImpl> issues = repository.getIssueImpls(l.toArray(new String[l.size()]));
                if (issues != null) {
                    tasks.addAll(issues);
                }
            }
        }
        return tasks;
    }

    private static RepositoryImpl getRepository(String repositoryId) {
        Collection<RepositoryImpl> repositories = getRepositories();
        for (RepositoryImpl repository : repositories) {
            if (repository.getId().equals(repositoryId)) {
                return repository;
            }
        }
        return null;
    }

    public static Icon getTaskIcon(IssueImpl issue) {
        Image priorityIcon = issue.getPriorityIcon();
        Image scheduleIcon = getScheduleIcon(issue);
        if (scheduleIcon != null) {
            return ImageUtilities.image2Icon(ImageUtilities.mergeImages(priorityIcon, scheduleIcon, 0, 0));
        }
        return ImageUtilities.image2Icon(priorityIcon);
    }

    public static int getScheduleIndex(IssueImpl issue) {
        boolean afterDue = isAfterDue(issue);
        boolean scheduleNow = isInSchedule(issue);
        if (afterDue) {
            return SCHEDULE_AFTER_DUE;
        } else if (scheduleNow) {
            return SCHEDULE_IN_SCHEDULE;
        }
        return SCHEDULE_NOT_IN_SCHEDULE;
    }

    private static Image getScheduleIcon(IssueImpl issue) {
        boolean afterDue = isAfterDue(issue);
        boolean scheduleNow = isInSchedule(issue);
        if (afterDue) {
            return SCHEDULE_WARNING_ICON;
        } else if (scheduleNow) {
            return SCHEDULE_ICON;
        }
        return null;
    }

    private static boolean isAfterDue(IssueImpl issue) {
        Calendar now = Calendar.getInstance();
        Date dueDate = issue.getDueDate();
        return dueDate == null ? false : now.getTime().getTime() >= dueDate.getTime();
    }

    private static boolean isInSchedule(IssueImpl issue) {
        Calendar now = Calendar.getInstance();
        IssueScheduleInfo scheduleInfo = issue.getSchedule();
        if (scheduleInfo == null) {
            return false;
        }
        Calendar scheduleStart = Calendar.getInstance();
        scheduleStart.setTime(scheduleInfo.getDate());

        Calendar scheduleEnd = Calendar.getInstance();
        scheduleEnd.setTime(scheduleInfo.getDate());
        scheduleEnd.add(Calendar.DATE, scheduleInfo.getInterval());

        if (now.getTimeInMillis() >= scheduleStart.getTimeInMillis() && now.getTimeInMillis() <= scheduleEnd.getTimeInMillis()) {
            return true;
        }
        return false;
    }

    public static int compareTaskIds(String id1, String id2) {
        int id = 0;
        boolean isIdNumeric = true;
        try {
            id = Integer.parseInt(id1);
        } catch (NumberFormatException numberFormatException) {
            isIdNumeric = false;
        }
        int idOther = 0;
        boolean isIdOtherNumberic = true;
        try {
            idOther = Integer.parseInt(id2);
        } catch (NumberFormatException numberFormatException) {
            isIdOtherNumberic = false;
        }
        if (isIdNumeric && isIdOtherNumberic) {
            return compareNumericId(id, idOther);
        } else if (isIdNumeric) {
            return 1;
        } else if (isIdOtherNumberic) {
            return -1;
        } else {
            return compareComplexId(id1, id2);
        }
    }

    private static int compareNumericId(int id, int idOther) {
        if (id < idOther) {
            return -1;
        } else if (id > idOther) {
            return 1;
        } else {
            return 0;
        }
    }

    private static int compareComplexId(String id1, String id2) {
        int dividerIndex1 = id1.lastIndexOf("-"); //NOI18
        int dividerIndex2 = id2.lastIndexOf("-"); //NOI18
        if (dividerIndex1 == -1 || dividerIndex2 == -1) {
            DashboardViewer.LOG.log(Level.WARNING, "Unsupported ID format - id1: {0}, id2: {1}", new Object[]{id1, id2});
            return id1.compareTo(id2);
        }
        String prefix1 = id1.subSequence(0, dividerIndex1).toString();
        String suffix1 = id1.substring(dividerIndex1 + 1);

        String prefix2 = id2.subSequence(0, dividerIndex2).toString();
        String suffix2 = id2.substring(dividerIndex2 + 1);

        //compare prefix, alphabetically
        int comparePrefix = prefix1.compareTo(prefix2);
        if (comparePrefix != 0) {
            return comparePrefix;
        }
        //compare number suffix
        int suffixInt1;
        int suffixInt2;
        try {
            suffixInt1 = Integer.parseInt(suffix1);
            suffixInt2 = Integer.parseInt(suffix2);
        } catch (NumberFormatException nfe) {
            //compare suffix alphabetically if it is not convertable to number
            DashboardViewer.LOG.log(Level.WARNING, "Unsupported ID format - id1: {0}, id2: {1}", new Object[]{id1, id2});
            return suffix1.compareTo(suffix2);
        }
        return compareNumericId(suffixInt1, suffixInt2);
    }

     public static boolean confirmDelete(String title, String message) {
        NotifyDescriptor nd = new NotifyDescriptor(
                message,
                title,
                NotifyDescriptor.YES_NO_OPTION,
                NotifyDescriptor.QUESTION_MESSAGE,
                null,
                NotifyDescriptor.YES_OPTION);
        if (DialogDisplayer.getDefault().notify(nd) == NotifyDescriptor.YES_OPTION) {
            return true;
        }
        return false;
    }
}
