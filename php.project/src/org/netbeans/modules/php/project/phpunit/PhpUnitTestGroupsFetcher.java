/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.project.phpunit;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.SwingUtilities;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import org.netbeans.api.extexecution.ExecutionDescriptor;
import org.netbeans.api.extexecution.ExternalProcessBuilder;
import org.netbeans.api.extexecution.input.InputProcessor;
import org.netbeans.api.extexecution.input.InputProcessors;
import org.netbeans.api.extexecution.input.LineProcessor;
import org.netbeans.modules.php.project.deprecated.PhpProgram;
import org.netbeans.modules.php.api.util.StringUtils;
import org.netbeans.modules.php.project.PhpProject;
import org.netbeans.modules.php.project.phpunit.ui.PhpUnitTestGroupsPanel;
import org.netbeans.modules.php.project.ui.actions.support.CommandUtils;
import org.netbeans.modules.php.project.ui.customizer.PhpProjectProperties;
import org.openide.DialogDisplayer;
import org.openide.NotificationLineSupport;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import org.openide.windows.InputOutput;

/**
 * Fetcher for PhpUnit test groups.
 * <p>
 * This class is thread-safe.
 * @author Ondřej Brejla
 */
public final class PhpUnitTestGroupsFetcher {

    private static final Logger LOGGER = Logger.getLogger(PhpUnitTestGroupsFetcher.class.getName());
    private static final String TEST_GROUPS_DELIMITER = ","; // NOI18N

    private final PhpProject project;
    // @GuardedBy(phpPropertiesLock)
    private final PhpProjectProperties phpProperties;
    private final Lock phpPropertiesLock = new ReentrantLock();
    private final StringBuffer formattedTestGroups = new StringBuffer(100);
    // @GuardedBy(EDT)
    private final TestGroupsTableModel tableModel = new TestGroupsTableModel();
    private final Collection<String> testGroups = Collections.synchronizedList(new ArrayList<String>());

    private volatile boolean wasInterrupted = false;

    public PhpUnitTestGroupsFetcher(PhpProject project) {
        this.project = project;
        phpProperties = new PhpProjectProperties(project);
    }

    @NbBundle.Messages("PhpUnitTestGroupsFetcher.error.fetchGroups=Test groups cannot be listed. Review Output window for details.")
    public boolean fetch(final File workingDirectory, PhpUnit.ConfigFiles configFiles) {
        TestGroupsOutputProcessorFactory processorFactory = fetchAllTestGroups(workingDirectory, configFiles, false);
        if (processorFactory == null) {
            return false;
        }
        if (!processorFactory.hasTestGroups()) {
            if (processorFactory.hasOutput()) {
                // some error occured => rerun command to Output window and inform user
                fetchAllTestGroups(workingDirectory, configFiles, true);
                NotifyDescriptor descriptor = new NotifyDescriptor.Message(
                        Bundle.PhpUnitTestGroupsFetcher_error_fetchGroups(), NotifyDescriptor.ERROR_MESSAGE);
                DialogDisplayer.getDefault().notifyLater(descriptor);
                return false;
            }
        }
        testGroups.addAll(processorFactory.getTestGroups());

        if (testsContainCustomGroups()) {
            runInEdtAndWait(new Runnable() {
                @Override
                public void run() {
                    displayTestGroupsDialog();
                }
            });
        }
        return true;
    }

    @NbBundle.Messages("LBL_FetchingTestGroups=Fetching Test Groups...")
    private TestGroupsOutputProcessorFactory fetchAllTestGroups(File workingDirectory, PhpUnit.ConfigFiles configFiles, boolean frontWindow) {
        final PhpUnit phpUnit = CommandUtils.getPhpUnit(project, true);
        if (phpUnit == null) {
            // in fact, should not happen
            assert false : "Valid PhpUnit should already be found";
            return null;
        }
        TestGroupsOutputProcessorFactory outputProcessorFactory = new TestGroupsOutputProcessorFactory();
        ExternalProcessBuilder processBuilder = phpUnit.getProcessBuilder();
        if (configFiles.bootstrap != null) {
            processBuilder = processBuilder
                    .addArgument(PhpUnit.PARAM_BOOTSTRAP)
                    .addArgument(configFiles.bootstrap.getAbsolutePath());
        }
        if (configFiles.configuration != null) {
            processBuilder = processBuilder
                    .addArgument(PhpUnit.PARAM_CONFIGURATION)
                    .addArgument(configFiles.configuration.getAbsolutePath());
        }
        processBuilder = processBuilder
                .addArgument(PhpUnit.PARAM_LIST_GROUPS)
                .addArgument(workingDirectory.getAbsolutePath());

        ExecutionDescriptor executionDescriptor = new ExecutionDescriptor();
        if (frontWindow) {
            executionDescriptor = executionDescriptor
                    .frontWindow(true);
        } else {
            executionDescriptor = executionDescriptor
                    .inputOutput(InputOutput.NULL)
                    .outProcessorFactory(outputProcessorFactory);
        }

        try {
            PhpProgram.execute(processBuilder, executionDescriptor, Bundle.LBL_FetchingTestGroups(), Bundle.LBL_FetchingTestGroups());
        } catch (CancellationException ex) {
            return null;
        }

        return outputProcessorFactory;
    }

    private boolean testsContainCustomGroups() {
        return !testGroups.isEmpty();
    }

    @NbBundle.Messages("LBL_TestGroups=Test Groups")
    private void displayTestGroupsDialog() {
        assert SwingUtilities.isEventDispatchThread() : "Not in AWT thread";
        PhpUnitTestGroupsPanel phpUnitTestGroupsPanel = new PhpUnitTestGroupsPanel(getTableModel());
        NotifyDescriptor notifyDescriptor = new NotifyDescriptor(phpUnitTestGroupsPanel,
                Bundle.LBL_TestGroups(),
                NotifyDescriptor.OK_CANCEL_OPTION,
                NotifyDescriptor.PLAIN_MESSAGE, null, NotifyDescriptor.OK_OPTION);
        final NotificationLineSupport notificationLineSupport = notifyDescriptor.createNotificationLineSupport();

        getTableModel().addTableModelListener(new TestGroupsTableModelListener(notificationLineSupport));
        initTableModel();

        if (DialogDisplayer.getDefault().notify(notifyDescriptor) != NotifyDescriptor.OK_OPTION) {
            wasInterrupted = true;
        }
    }

    private void initTableModel() {
        assert SwingUtilities.isEventDispatchThread() : "Not in AWT thread";
        String phpUnitLastUsedTestGroups = null;
        phpPropertiesLock.lock();
        try {
            phpUnitLastUsedTestGroups = phpProperties.getPhpUnitLastUsedTestGroups();
        } finally {
            phpPropertiesLock.unlock();
        }

        List<String> lastUsedTestGroups = StringUtils.explode(phpUnitLastUsedTestGroups, TEST_GROUPS_DELIMITER);
        for (String testGroup : testGroups) {
            getTableModel().addRow(new Object[] {testGroup, lastUsedTestGroups.contains(testGroup)});
        }
    }

    public void saveSelectedTestGroups() {
        saveFormattedTestGroups();
    }

    private void saveFormattedTestGroups() {
        phpPropertiesLock.lock();
        try {
            phpProperties.setPhpUnitLastUsedTestGroups(getFormattedTestGroups());
            phpProperties.save();
        } finally {
            phpPropertiesLock.unlock();
        }
    }

    private String getFormattedTestGroups() {
        if (formattedTestGroups.length() == 0) {
            createFormatedTestGroups();
        }
        return formattedTestGroups.toString();
    }

    private void createFormatedTestGroups() {
        runInEdtAndWait(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < getTableModel().getRowCount(); i++) {
                    processTableRow(i);
                }
            }
        });
    }

    private void processTableRow(int i) {
        Boolean isChecked = (Boolean) getTableModel().getValueAt(i, 1);
        if (isChecked) {
            addRowToFormattedTestGroups(i);
        }
    }

    private void addRowToFormattedTestGroups(int i) {
        String groupName = (String) getTableModel().getValueAt(i, 0);
        formattedTestGroups.append(formattedTestGroups.length() == 0 ? groupName : TEST_GROUPS_DELIMITER + groupName);
    }

    public boolean wasInterrupted() {
        return wasInterrupted;
    }

    private DefaultTableModel getTableModel() {
        assert SwingUtilities.isEventDispatchThread() : "Not in AWT thread";
        return tableModel;
    }

    private void runInEdtAndWait(Runnable runnable) {
        try {
            SwingUtilities.invokeAndWait(runnable);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        } catch (InvocationTargetException ex) {
            LOGGER.log(Level.WARNING, null, ex);
        }
    }



    //~ Inner classes

    private static final class TestGroupsOutputProcessorFactory implements ExecutionDescriptor.InputProcessorFactory {

        private final Pattern testGroupName = Pattern.compile("^\\s-\\s(.*)$"); // NOI18N
        private final Collection<String> testGroups = Collections.synchronizedCollection(new ArrayList<String>());
        private volatile boolean hasOutput = false;


        @Override
        public InputProcessor newInputProcessor(InputProcessor defaultProcessor) {
            return InputProcessors.bridge(new LineProcessor() {

                @Override
                public void processLine(String line) {
                    hasOutput = true;
                    Matcher matcher = testGroupName.matcher(line);
                    if (matcher.matches()) {
                        testGroups.add(matcher.group(1).trim());
                    }
                }

                @Override
                public void reset() {
                }

                @Override
                public void close() {
                }

            });
        }

        public Collection<String> getTestGroups() {
            return testGroups;
        }

        public boolean hasTestGroups() {
            return !testGroups.isEmpty();
        }

        public boolean hasOutput() {
            return hasOutput;
        }

    }

    private static final class TestGroupsTableModel extends DefaultTableModel {

        private static final long serialVersionUID = 687644354211L;

        private final Class<?>[] types = new Class<?>[] {String.class, Boolean.class};

        public TestGroupsTableModel(Object[][] data, Object[] columnNames) {
            super(data, columnNames);
        }

        public TestGroupsTableModel() {
            this(new Object[][]{}, new Object[]{null, null});
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return types[columnIndex];
        }

        @Override
        public boolean isCellEditable(int row, int col) {
            return col == 0 ? false : true;
        }

    }

    private static final class TestGroupsTableModelListener implements TableModelListener {

        private final NotificationLineSupport notificationLineSupport;

        public TestGroupsTableModelListener(NotificationLineSupport notificationLineSupport) {
            this.notificationLineSupport = notificationLineSupport;
        }

        @Override
        @NbBundle.Messages("MSG_NoTestsForExecution=No tests will be executed.")
        public void tableChanged(TableModelEvent e) {
            TableModel tableModel = (TableModel) e.getSource();
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                Boolean isRowChecked = (Boolean) tableModel.getValueAt(i, 1);
                if (isRowChecked) {
                    notificationLineSupport.clearMessages();
                    return;
                }
            }
            notificationLineSupport.setInformationMessage(Bundle.MSG_NoTestsForExecution());
        }

    }

}
