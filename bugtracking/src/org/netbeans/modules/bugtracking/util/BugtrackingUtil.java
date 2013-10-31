/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008-2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.bugtracking.util;

import java.awt.event.ActionEvent;
import java.io.File;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.event.ChangeListener;
import javax.swing.text.DateFormatter;
import javax.swing.text.DefaultFormatterFactory;
import org.netbeans.api.keyring.Keyring;
import org.netbeans.modules.bugtracking.APIAccessor;
import org.netbeans.modules.bugtracking.BugtrackingManager;
import org.netbeans.modules.bugtracking.DelegatingConnector;
import org.netbeans.modules.bugtracking.RepositoryImpl;
import org.netbeans.modules.bugtracking.api.Repository;
import org.netbeans.modules.bugtracking.spi.BugtrackingConnector;
import org.netbeans.modules.bugtracking.spi.IssueScheduleInfo;
import org.netbeans.modules.bugtracking.ui.selectors.RepositorySelector;
import org.netbeans.modules.team.ide.spi.IDEServices;
import org.netbeans.modules.team.ide.spi.IDEServices.DatePickerDialog;
import org.netbeans.modules.team.ide.spi.ProjectServices;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.actions.Presenter;
import org.openide.util.ChangeSupport;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Tomas Stupka, Jan Stola
 * @author Marian Petras
 */
public class BugtrackingUtil {
    private static RequestProcessor parallelRP;

    public static void notifyError (final String title, final String message) {
        NotifyDescriptor nd = new NotifyDescriptor(message, title, NotifyDescriptor.DEFAULT_OPTION, NotifyDescriptor.ERROR_MESSAGE, new Object[] {NotifyDescriptor.OK_OPTION}, NotifyDescriptor.OK_OPTION);
        DialogDisplayer.getDefault().notifyLater(nd);
    }          
        
    public static RepositoryImpl createRepository() {
        return createRepository(true);
    }
    
    public static RepositoryImpl createRepository(boolean selectNode) {
        RepositorySelector rs = new RepositorySelector();
        RepositoryImpl repo = rs.create(selectNode);
        return repo;
    }

    public static boolean editRepository(RepositoryImpl repository, String errorMessage) {
        RepositorySelector rs = new RepositorySelector();
        return rs.edit(repository, errorMessage);
    }

    public static boolean editRepository(Repository repository) {
        return editRepository(APIAccessor.IMPL.getImpl(repository), null);
    }

    public static BugtrackingConnector[] getBugtrackingConnectors() {
        DelegatingConnector[] dcs = BugtrackingManager.getInstance().getConnectors();
        BugtrackingConnector[] cons = new BugtrackingConnector[dcs.length];
        for (int i = 0; i < cons.length; i++) {
            cons[i] = dcs[i].getDelegate();
        }
        return cons;
    }

    public static void savePassword(char[] password, String prefix, String user, String url) throws MissingResourceException {
        if (password != null && password.length != 0) {                  
            Keyring.save(getPasswordKey(prefix, user, url), password, NbBundle.getMessage(BugtrackingUtil.class, "password_keyring_description", url)); // NOI18N
        } else {
            Keyring.delete(getPasswordKey(prefix, user, url));
        }
    }

    /**
     *
     * @param scrambledPassword
     * @param keyPrefix
     * @param url
     * @param user
     * @return
     */
    public static char[] readPassword(String scrambledPassword, String keyPrefix, String user, String url) {
        char[] password = Keyring.read(getPasswordKey(keyPrefix, user, url));
        return password != null ? password : new char[0];
    }

    public static RequestProcessor getParallelRP () {
        if (parallelRP == null) {
            parallelRP = new RequestProcessor("Bugtracking parallel tasks", 5, true); //NOI18N
        }
        return parallelRP;
    }
    
    private static String getPasswordKey(String prefix, String user, String url) {
        return (prefix != null ? prefix + "-" : "") + user + "@" + url;         // NOI18N
    }

    public static File getLargerSelection() {
        FileObject[] fos = BugtrackingUtil.getCurrentSelection();
        if(fos == null) {
            return null;
        }
        for (FileObject fo : fos) {
            FileObject ownerDirectory = BugtrackingUtil.getFileOwnerDirectory(fo);
            if (ownerDirectory != null) {
                fo = ownerDirectory;
        }
            File file = FileUtil.toFile(fo);
            if(file != null) {
                return file;
        }
        }        
        return null;
    }
    
    public static FileObject getFileOwnerDirectory(FileObject fileObject) {
        ProjectServices projectServices = BugtrackingManager.getInstance().getProjectServices();
        return projectServices != null ? projectServices.getFileOwnerDirectory(fileObject): null;
    }
    
    public static FileObject[] getCurrentSelection() {
        ProjectServices projectServices = BugtrackingManager.getInstance().getProjectServices();
        return projectServices != null ? projectServices.getCurrentSelection() : null;
    }

    // XXX NOI
    public static IDEServices.DatePickerComponent createDatePickerComponent () {
        IDEServices.DatePickerComponent picker = null;
        IDEServices services = Lookup.getDefault().lookup(IDEServices.class);
        if (services != null) {
            picker = services.createDatePicker();
        }
        if (picker == null) {
            picker = new DummyDatePickerComponent();
        }
        return picker;
    }

    private static class DummyDatePickerComponent extends JFormattedTextField implements IDEServices.DatePickerComponent {

        private static final DateFormatter formatter = new javax.swing.text.DateFormatter() {
            
            @Override
            public Object stringToValue (String text) throws java.text.ParseException {
                if (text == null || text.trim().isEmpty()) {
                    return null;
                }
                return super.stringToValue(text);
            }
        };
        private final ChangeSupport support;

        DummyDatePickerComponent () {
            super(new DefaultFormatterFactory(formatter));
            support = new ChangeSupport(this);
        }

        @Override
        public JComponent getComponent () {
            return this;
        }

        @Override
        public void setDate (Date date) {
            try {
                setText(formatter.valueToString(date));
            } catch (ParseException ex) {
                Logger.getLogger(BugtrackingUtil.class.getName()).log(Level.INFO, null, ex);
            }
        }

        @Override
        public Date getDate () {
            try {
                return (Date) formatter.stringToValue(getText());
            } catch (ParseException ex) {
                Logger.getLogger(BugtrackingUtil.class.getName()).log(Level.INFO, null, ex);
                return null;
            }
        }

        @Override
        public void addChangeListener (ChangeListener listener) {
            support.addChangeListener(listener);
        }

        @Override
        public void removeChangeListener (ChangeListener listener) {
            support.removeChangeListener(listener);
        }

    }
    
    public static boolean show(JPanel panel, String title, String okName) {
        JButton ok = new JButton(okName);
        ok.getAccessibleContext().setAccessibleDescription(ok.getText());
        JButton cancel = new JButton(NbBundle.getMessage(BugtrackingUtil.class, "LBL_Cancel")); // NOI18N
        cancel.getAccessibleContext().setAccessibleDescription(cancel.getText());
        final DialogDescriptor dd =
            new DialogDescriptor(
                    panel,
                    title,
                    true,
                    new Object[]{ok, cancel},
                    ok,
                    DialogDescriptor.DEFAULT_ALIGN,
                    new HelpCtx(panel.getClass()),
                    null);
        return DialogDisplayer.getDefault().notify(dd) == ok;
    }

    public static SchedulingMenu createScheduleMenu(IssueScheduleInfo previousSchedule) {
        return new SchedulingMenu(previousSchedule);
    }

    public static boolean isMatchingInterval(IssueScheduleInfo interval1, IssueScheduleInfo interval2) {
        if (interval2 == null || interval1 == null) {
            return false;
        }
        Calendar interval1Start = Calendar.getInstance();
        interval1Start.setTime(interval1.getDate());
        Calendar interval1End = Calendar.getInstance();
        interval1End.setTime(interval1.getDate());
        interval1End.add(Calendar.DATE, interval1.getInterval());

        Calendar interval2Start = Calendar.getInstance();
        interval2Start.setTime(interval2.getDate());
        Calendar interva2End = Calendar.getInstance();
        interva2End.setTime(interval2.getDate());
        interva2End.add(Calendar.DATE, interval2.getInterval());
        
        return interval2Start.get(Calendar.YEAR) == interval1Start.get(Calendar.YEAR)
                && interval2Start.get(Calendar.DAY_OF_YEAR) == interval1Start.get(Calendar.DAY_OF_YEAR)
                && interva2End.get(Calendar.YEAR) == interval1End.get(Calendar.YEAR)
                && interva2End.get(Calendar.DAY_OF_YEAR) == interval1End.get(Calendar.DAY_OF_YEAR);
    }

    public static final class SchedulingMenu {
        private final JMenu menu;
        private final List<JMenuItem> menuItems;
        private final ChangeSupport support;
        private IssueScheduleInfo scheduleInfo;

        public SchedulingMenu(final IssueScheduleInfo previousSchedule) {
            this.support = new ChangeSupport(this);
            this.menu = new JMenu(NbBundle.getMessage(BugtrackingUtil.class, "LBL_ScheduleFor"));
            this.menuItems = new ArrayList<JMenuItem>();

            for (int i = 0; i < 7; i++) {
                final Calendar calendar = Calendar.getInstance();
                calendar.add(Calendar.DATE, i);
                String itemName = calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault());
                if (i == 0) {
                    itemName += " - " +NbBundle.getMessage(BugtrackingUtil.class, "CTL_Today");
                }
                JMenuItem item = new JCheckBoxMenuItem(new ScheduleItemAction(itemName, new IssueScheduleInfo(calendar.getTime())));
                menu.add(item);
                menuItems.add(item);
            }
            menu.addSeparator();

            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());
            JMenuItem thisWeek = new JCheckBoxMenuItem(new ScheduleItemAction(
                    NbBundle.getMessage(BugtrackingUtil.class, "CTL_ThisWeek"),
                    new IssueScheduleInfo(calendar.getTime(), 7)));

            menu.add(thisWeek);
            menuItems.add(thisWeek);

            calendar = Calendar.getInstance();
            calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());
            calendar.add(Calendar.DATE, 7);
            JMenuItem nextWeek = new JCheckBoxMenuItem(new ScheduleItemAction(
                    NbBundle.getMessage(BugtrackingUtil.class, "CTL_NextWeek"),
                    new IssueScheduleInfo(calendar.getTime(), 7)));

            menu.add(nextWeek);
            menuItems.add(nextWeek);
            menu.addSeparator();

            JMenuItem chooseDate = new JCheckBoxMenuItem(new ScheduleItemAction(
                    NbBundle.getMessage(BugtrackingUtil.class, "CTL_ChooseDate"),
                    null) {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            Date date = showChooseDateDialog(previousSchedule == null ? new Date() : previousSchedule.getDate());
                            if (date != null) {
                                scheduleInfo = new IssueScheduleInfo(date);
                                support.fireChange();
                            }
                        }
                    });
            menu.add(chooseDate);
            menuItems.add(chooseDate);

            JMenuItem notScheduled = new JCheckBoxMenuItem(new ScheduleItemAction(
                    NbBundle.getMessage(BugtrackingUtil.class, "CTL_NotScheduled"),
                    null));
            menu.add(notScheduled);
            menuItems.add(notScheduled);

            // select already schedule item
            if (previousSchedule == null) {
                notScheduled.setSelected(true);
                return;
            }
            boolean findPrevious = true;
            for (JMenuItem item : menuItems) {
                if (item.getAction() instanceof ScheduleItemAction) {
                    IssueScheduleInfo assignedSchedule = ((ScheduleItemAction) item.getAction()).getAssignedSchedule();
                    if (findPrevious && isMatchingInterval(assignedSchedule, previousSchedule)) {
                        item.setSelected(true);
                        return;
                    }
                }
            }
            chooseDate.setSelected(true);
        }

        public Action getMenuAction() {
            return new ScheduleMenuAction(menu);
        }

        public List<JMenuItem> getMenuItems() {
            return menuItems;
        }

        public IssueScheduleInfo getScheduleInfo() {
            return scheduleInfo;
        }

        public void addChangeListener(ChangeListener listener) {
            support.addChangeListener(listener);
        }

        public void removeChangeListener(ChangeListener listener) {
            support.removeChangeListener(listener);
        }

        private class ScheduleItemAction extends AbstractAction {

            private final IssueScheduleInfo assignedSchedule;

            public ScheduleItemAction(String name, IssueScheduleInfo assignedSchedule) {
                super(name);
                this.assignedSchedule = assignedSchedule;
            }

            public IssueScheduleInfo getAssignedSchedule() {
                return assignedSchedule;
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                scheduleInfo = assignedSchedule;
                support.fireChange();
            }
        }
    }


    private static class ScheduleMenuAction extends AbstractAction implements Presenter.Popup {

        private final JMenu menu;

        public ScheduleMenuAction(JMenu menu) {
            super();
            this.menu = menu;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            menu.getAction().actionPerformed(e);
        }

        @Override
        public JMenuItem getPopupPresenter() {
            return menu;
        }
    }

    private static Date showChooseDateDialog(Date scheduled) {
        DatePickerDialog dialog = createDatePickerDialog(scheduled);
        NotifyDescriptor nd = new NotifyDescriptor(
                dialog.getComponent(),
                NbBundle.getMessage(BugtrackingUtil.class, "LBL_ChooseDate"),
                NotifyDescriptor.OK_CANCEL_OPTION,
                NotifyDescriptor.PLAIN_MESSAGE,
                null,
                NotifyDescriptor.OK_OPTION);
        if (DialogDisplayer.getDefault().notify(nd) == NotifyDescriptor.OK_OPTION) {
            return dialog.getDate();
        }
        return null;
    }

    private static IDEServices.DatePickerDialog createDatePickerDialog(Date scheduled) {
        IDEServices.DatePickerDialog dialog = null;
        IDEServices services = Lookup.getDefault().lookup(IDEServices.class);
        if (services != null) {
            dialog = services.createDatePickerDialog(scheduled);
        }
        if (dialog == null) {
        }
        return dialog;
    }

}
