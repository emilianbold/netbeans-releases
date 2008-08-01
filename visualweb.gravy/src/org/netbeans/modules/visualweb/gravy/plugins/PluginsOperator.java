/*
DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.


The contents of this file are subject to the terms of either the GNU
General Public License Version 2 only ("GPL") or the Common
Development and Distribution License("CDDL") (collectively, the
"License"). You may not use this file except in compliance with the
License. You can obtain a copy of the License at
http://www.netbeans.org/cddl-gplv2.html
or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
specific language governing permissions and limitations under the
License.  When distributing the software, include this License Header
Notice in each file and include the License file at
nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
particular file as subject to the "Classpath" exception as provided
by Sun in the GPL Version 2 section of the License file that
accompanied this code. If applicable, add the following below the
License Header, with the fields enclosed by brackets [] replaced by
your own identifying information:
"Portions Copyrighted [year] [name of copyright owner]"

Contributor(s):

The Original Software is NetBeans. The Initial Developer of the Original
Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
Microsystems, Inc. All Rights Reserved.

If you wish your version of this file to be governed by only the CDDL
or only the GPL Version 2, indicate your decision by adding
"[Contributor] elects to include this software in this distribution
under the [CDDL or GPL Version 2] license." If you do not indicate a
single choice of license, a recipient has the option to distribute
your version of this file under either the CDDL, the GPL Version 2 or
to extend the choice of license to its licensees as provided above.
However, if you add GPL Version 2 code and therefore, elected the GPL
Version 2 license, then the option applies only if the new code is
made subject to such option by the copyright holder.
*/
package org.netbeans.modules.visualweb.gravy.plugins;

import java.util.*;
import java.io.*;
import java.awt.*;
import javax.swing.*;
import org.netbeans.jemmy.*;
import org.netbeans.jemmy.operators.*;
import org.netbeans.modules.visualweb.gravy.*;
        
public class PluginsOperator extends JDialogOperator {
    private static final String 
        MAIN_MENU_ITEM_TOOLS = "Tools",
        MAIN_MENU_ITEM_TOOLS_PLUGINS = MAIN_MENU_ITEM_TOOLS + "|Plugins",
        DIALOG_TITLE_PLUGINS = "Plugins",
        //DIALOG_TITLE_NETBEANS_IDE_INSTALLER = "NetBeans IDE Installer",
        DIALOG_TITLE_NETBEANS_IDE_INSTALLER = org.netbeans.modules.visualweb.gravy.Bundle.getStringTrimmed(
            "org.netbeans.modules.autoupdate.ui.wizards.Bundle",
            "InstallUnitWizard_Title"),
        DIALOG_TITLE_VALIDATION_WARNING = "Validation Warning",
        
        TAB_TITLE_AVAILABLE_PLUGINS = "Available Plugins",
        TAB_TITLE_DOWNLOADED_PLUGINS = "Downloaded",

        COLUMN_TITLE_NAME = "Name",
        COLUMN_TITLE_INSTALL = "Install",
        
        RADIO_BUTTON_LICENSE_AGREEMENT_ACCEPT_TEXT = "I accept",        
        
        BUTTON_LABEL_ADD_PLUGINS = "Add Plugins",
        BUTTON_LABEL_RELOAD_CATALOG = "Reload Catalog",
        BUTTON_LABEL_INSTALL = "Install",
        BUTTON_LABEL_NEXT = "Next",
        BUTTON_LABEL_CONTINUE = "Continue",
        BUTTON_LABEL_FINISH = "Finish",
        BUTTON_LABEL_CLOSE = "Close";

    private JTabbedPaneOperator tabbedPane;
    private JButtonOperator btnClose;
    private boolean pluginsAvailable;
    
    public static PluginsOperator getInstance() {
        display(); // check whether the appropriate dialog is opened or not
        return (new PluginsOperator());
    }
    
    private PluginsOperator() {
        super(DIALOG_TITLE_PLUGINS);
        verify();
    }
    
    public void installAvailablePlugins() {
        installAvailablePlugins(Collections.EMPTY_LIST);
    }
    public void installAvailablePlugins(String pluginName) {
        installAvailablePlugins(new String[] {pluginName});
    }
    public void installAvailablePlugins(String[] pluginNames) {
        installAvailablePlugins(Arrays.asList(pluginNames));
    }
    public void installAvailablePlugins(java.util.List<String> pluginNames) {
        ContainerOperator containerOp = waitTabEnabled(TAB_TITLE_AVAILABLE_PLUGINS);
        JTableOperator pluginTable = new JTableOperator(containerOp);
        fillAvailablePluginsTable(containerOp, pluginTable);
        selectPlugins(pluginTable, pluginNames);
        if (pluginsAvailable) installSelectedPlugins(containerOp);
        else System.out.println("WARNING: Necessary plugins are absent!");
        closeDialog();
    }
    
    public void installDownloadedPlugins(String nbmPath) {
        ContainerOperator containerOp = waitTabEnabled(TAB_TITLE_DOWNLOADED_PLUGINS);
        new JButtonOperator(containerOp, BUTTON_LABEL_ADD_PLUGINS).pushNoBlock();
        Util.wait(500);
        new QueueTool().waitEmpty();

        addDownloadedPlugins(nbmPath, containerOp);
        JTableOperator pluginTable = new JTableOperator(containerOp);
        waitPluginsTableFilling(pluginTable);
        
        installSelectedPlugins(containerOp);
        closeDialog();
    }

    private ContainerOperator waitTabEnabled(String tabTitle) {
        for (int i = 0; i < 200; i++) {
            int cursorType = tabbedPane.getCursor().getType();
            //debugOutput("Cursor Name = " + tabbedPane.getCursor().getName());
            if (cursorType == Cursor.WAIT_CURSOR) {
                Util.wait(1000);
                new QueueTool().waitEmpty();
            } else {
                break;
            }
        }
        int tabIndex = findTab(tabTitle);
        if (tabIndex > -1) {
            tabbedPane.setSelectedIndex(tabIndex);
            ContainerOperator containerOp = new ContainerOperator(
                (Container) tabbedPane.getComponentAt(tabIndex));
            Util.wait(500);
            new QueueTool().waitEmpty();
            return containerOp;
        } else {
            throw new RuntimeException("Tab with title [" + tabTitle + 
                "] isn't found in the dialog [" + this.getTitle() + "]");
        }
    }
    
    private int findTab(String tabTitle) {
        // int tabIndex = tabbedPane.indexOfTab(tabTitle); // only j2sdk 1.6 
        // return tabIndex;
        int tabCount = tabbedPane.getTabCount();
        for (int i = 0; i < tabCount; i++) {
            // Component component = tabbedPane.getComponentAt(i);
            // only j2sdk 1.6: Component tabComponent = ((JTabbedPane) tabbedPane.getSource()).getTabComponentAt(i);
            String title = tabbedPane.getTitleAt(i);
            //debugOutput("+++ Tab Title = [" + title + "]"); 
            //if (title.equals(tabTitle)) {
            if (title.indexOf(tabTitle) > -1) {
                return i;
            }
        }
        return (-1);
    }
        
    private int getColumnIndexByName(JTableOperator tableOp, String columnName) {
        int colCount = tableOp.getColumnCount();
        for (int i = 0; i < colCount; i++) {
            String name = tableOp.getColumnName(i);
            if (name.indexOf(columnName) > -1) {
                return i;
            }
        }
        return (-1);
    }
    
    private void fillAvailablePluginsTable(ContainerOperator containerOp, JTableOperator pluginTable) {
        int rowCount = pluginTable.getRowCount();
        //debugOutput("pluginTable.getRowCount() = " + rowCount, 
        //            "pluginTable.getColumnCount() = " + pluginTable.getColumnCount());
        if (rowCount == 0) {
            new JButtonOperator(containerOp, BUTTON_LABEL_RELOAD_CATALOG).pushNoBlock();
            Util.wait(500);
            new QueueTool().waitEmpty();
            waitPluginsTableFilling(pluginTable);
        }
    }

    private void waitPluginsTableFilling(JTableOperator pluginTable) {
        for (int i = 0; i < 200; i++) {
            int rowCount = pluginTable.getRowCount();
            //debugOutput("pluginTable.getRowCount() = " + rowCount);
            if (rowCount < 1) {
                Util.wait(1000);
                new QueueTool().waitEmpty();
            } else {
                break;
            }
        }
    }
    
    private void selectPlugins(JTableOperator pluginTable, 
        java.util.List<String> pluginNames) {
        java.util.List<String> selectedPlugins = new ArrayList();
        int nameColumnIndex = getColumnIndexByName(pluginTable, COLUMN_TITLE_NAME);
        int installColumnIndex = getColumnIndexByName(pluginTable, COLUMN_TITLE_INSTALL),
            rowCount = pluginTable.getRowCount();
        for (int row = 0; row < rowCount; row++) {
            Object cellValue = pluginTable.getValueAt(row, nameColumnIndex);
            //debugOutput("cellValue[" + row + "] = [" + cellValue + "]");
            if (isPluginInList(pluginNames, cellValue.toString())) {
                int rowIndex = row;
                pluginTable.clickOnCell(rowIndex, installColumnIndex);
                Util.wait(500);
                new QueueTool().waitEmpty();
                selectedPlugins.add(cellValue.toString());
            }
        }
        if (selectedPlugins.isEmpty()) pluginsAvailable = false;
        else pluginsAvailable = true;
    }
     
    private boolean isPluginInList(java.util.List<String> pluginNames, String checkedPluginName) {
        // if a list of plugin names is empty, it means that
        // all plugins in a table should be selected
        if (pluginNames.isEmpty()) return true;
        return pluginNames.contains(checkedPluginName);
    }

    private void addDownloadedPlugins(String nbmPath, ContainerOperator containerOp) {
        new JButtonOperator(containerOp, BUTTON_LABEL_ADD_PLUGINS).pushNoBlock();
        Util.wait(500);
        new QueueTool().waitEmpty();
        
        JFileChooserOperator fileChooserOp = new JFileChooserOperator();
        fileChooserOp.setSelectedFile(new File(nbmPath));
        Util.wait(500);
        new QueueTool().waitEmpty();
        
        fileChooserOp.approve();
        Util.wait(500);
        new QueueTool().waitEmpty();
    }
        
    private void installSelectedPlugins(ContainerOperator containerOp) {
        JButtonOperator btnInstall = new JButtonOperator(containerOp, BUTTON_LABEL_INSTALL); 
        if (btnInstall.isEnabled()) {
            btnInstall.pushNoBlock();
            Util.wait(500);
            new QueueTool().waitEmpty();
        } else {
            throw new RuntimeException("The button [" + BUTTON_LABEL_INSTALL + 
            "] isn't enabled in the dialog [" + DIALOG_TITLE_PLUGINS + "]");
        }
        workInstallerDialog();
    }

    private void workInstallerDialog() {
        JDialogOperator installerDialog = new JDialogOperator(DIALOG_TITLE_NETBEANS_IDE_INSTALLER);
        Util.wait(3000);
        new QueueTool().waitEmpty();
        
        String timeoutName = "ComponentOperator.WaitComponentTimeout";
        long previousTimeoutValue = JemmyProperties.getCurrentTimeout(timeoutName),
             newTimeoutValue = 1000;
        JemmyProperties.getCurrentTimeouts().setTimeout(timeoutName, newTimeoutValue);
        try {
            new JButtonOperator(installerDialog, BUTTON_LABEL_NEXT).pushNoBlock();
            Util.wait(1000);
            new QueueTool().waitEmpty();
            
            JCheckBoxOperator cbLicenseAgreements = new JCheckBoxOperator(
                installerDialog, RADIO_BUTTON_LICENSE_AGREEMENT_ACCEPT_TEXT);
            cbLicenseAgreements.changeSelection(true);
            Util.wait(500);
            new QueueTool().waitEmpty();

            Util.wait(500);
            new QueueTool().waitEmpty();
        } catch(TimeoutExpiredException tee) {
            tee.printStackTrace(System.out);
        } catch(Exception e) {
            e.printStackTrace(System.out);
            throw new RuntimeException(e);
        } finally {
            JemmyProperties.getCurrentTimeouts().setTimeout(timeoutName, previousTimeoutValue);
        }
        new JButtonOperator(installerDialog, BUTTON_LABEL_INSTALL).pushNoBlock();
        Util.wait(500);
        new QueueTool().waitEmpty();

        waitInstallationFinish(installerDialog);
    }
    
    private void waitInstallationFinish(JDialogOperator dialogOp) {
        String timeoutName = "DialogWaiter.WaitDialogTimeout";
        long previousTimeoutValue = JemmyProperties.getCurrentTimeout(timeoutName),
             newTimeoutValue = 1000;
        JemmyProperties.getCurrentTimeouts().setTimeout(timeoutName, newTimeoutValue);

        //DialogWatcher warningDialogThread = new DialogWatcher(
        //    DIALOG_TITLE_VALIDATION_WARNING, BUTTON_LABEL_CONTINUE);
        //warningDialogThread.startWatch();
        
        JButtonOperator btnFinish = null;
        for (int i = 0; i < 600; i++) {
            btnFinish = checkButtonFinishAppearance(dialogOp);
            if ((btnFinish != null) && (btnFinish.isEnabled())) break;
        }
        //warningDialogThread.stopWatch();
        JemmyProperties.getCurrentTimeouts().setTimeout(timeoutName, 
            previousTimeoutValue > 0 ? previousTimeoutValue : 60000);
        //debugOutput("btnFinish = [" + btnFinish + "]", "btnFinish.isEnabled() = [" + btnFinish.isEnabled() + "]");                
        if ((btnFinish != null) && (btnFinish.isEnabled())) {
            btnFinish.pushNoBlock();
            Util.wait(500);
            new QueueTool().waitEmpty();
        } else {
            throw new RuntimeException("The button [" + BUTTON_LABEL_FINISH + 
            "] is not found or disabled in the dialog [" + DIALOG_TITLE_NETBEANS_IDE_INSTALLER + "]");
        }
    }

    private JButtonOperator checkButtonFinishAppearance(JDialogOperator dialogOp) {
        String timeoutName = "ComponentOperator.WaitComponentTimeout";
        long previousTimeoutValue = JemmyProperties.getCurrentTimeout(timeoutName),
             newTimeoutValue = 1000;
        JemmyProperties.getCurrentTimeouts().setTimeout(timeoutName, newTimeoutValue);
        JButtonOperator btnFinish = null;
        try {
            btnFinish = new JButtonOperator(dialogOp, BUTTON_LABEL_FINISH);
            return btnFinish;
        } catch(TimeoutExpiredException tee) {
            Util.wait(500);
            new QueueTool().waitEmpty();
        } catch(Exception e) {
            e.printStackTrace(System.out);
            throw new RuntimeException(e);
        } finally {
            JemmyProperties.getCurrentTimeouts().setTimeout(timeoutName, previousTimeoutValue);
        }
        return null;
    }
    
    private void closeDialog() {
        btnClose.pushNoBlock();
        Util.wait(500);
        new QueueTool().waitEmpty();
    }
    
    public static void display() {
        String timeoutName = "DialogWaiter.WaitDialogTimeout";
        long previousTimeoutValue = JemmyProperties.getCurrentTimeout(timeoutName),
             newTimeoutValue = 1500;
        JemmyProperties.getCurrentTimeouts().setTimeout(timeoutName, newTimeoutValue);
        try {
            new JDialogOperator(DIALOG_TITLE_PLUGINS);
        } catch(TimeoutExpiredException tee) { // dialog isn't open yet
            Util.getMainMenu().pushMenuNoBlock(MAIN_MENU_ITEM_TOOLS_PLUGINS);
            Util.wait(1000);
            new QueueTool().waitEmpty();
        } catch(Exception e) {
            e.printStackTrace(System.out);
            throw new RuntimeException(e);
        } finally {
            JemmyProperties.getCurrentTimeouts().setTimeout(timeoutName, 
                previousTimeoutValue > 0 ? previousTimeoutValue : 60000);
        }
    }

    public JTabbedPaneOperator checkTabbedPane() {
        if (tabbedPane == null) {
            tabbedPane = new JTabbedPaneOperator(this);
        }
        return tabbedPane;
    }

    private JButtonOperator checkButtonClose() {
        if (btnClose == null) {
            btnClose = new JButtonOperator(this, BUTTON_LABEL_CLOSE);
        }
        return btnClose;
    }

    /**
     * Initializes all necessary controls.
     */
    public void verify() {
        checkTabbedPane();
        checkButtonClose();
    }
    
    private void debugOutput(Object... dataArray) {
        System.out.println();
        for (Object obj : dataArray) {
            System.out.println(obj.toString());
        }
        System.out.println();
    }
//============================================================================//    
    private class DialogWatcher implements Runnable {
        private String dialogTitle, buttonLabel;
        private boolean stopWatch = true;
        private Thread watchThread = new Thread(this);
        
        public DialogWatcher(String dialogTitle, String buttonLabel) {
            this.dialogTitle = dialogTitle;
            this.buttonLabel = buttonLabel;
        }

        public void startWatch() {
            stopWatch = false;
            watchThread.start();
        }
        public void stopWatch() {
            stopWatch = true;
            do { // wait until a thread, which is used in this internal class, is died
                //debugOutput("+++ watchThread.isAlive() = " + watchThread.isAlive());
                Util.wait(500);
            } while (watchThread.isAlive());
            //debugOutput("+++ watchThread.isAlive() = " + watchThread.isAlive());
            Util.wait(1000);
        }
        
        public void run() {
            while (true) {
                if (stopWatch) return;
                synchronized (this) {
                    try {
                        wait(1000);
                    } catch (Exception e) {
                        e.printStackTrace(System.out);
                        throw new RuntimeException(e);
                    }
                }
                JDialogOperator dialogOp = checkDialogAppearance();
                if (dialogOp != null) {
                    new JButtonOperator(dialogOp, buttonLabel).pushNoBlock();
                    Util.wait(500);
                    new QueueTool().waitEmpty();
                    stopWatch = true;
                }
            }
        }
        
        private JDialogOperator checkDialogAppearance() {
            String timeoutName = "DialogWaiter.WaitDialogTimeout";
            long previousTimeoutValue = JemmyProperties.getCurrentTimeout(timeoutName),
                 newTimeoutValue = 1000;
            JemmyProperties.getCurrentTimeouts().setTimeout(timeoutName, newTimeoutValue);
            try {
                JDialogOperator dialogOp = new JDialogOperator(dialogTitle);
                return dialogOp;
            } catch(TimeoutExpiredException tee) {
                Util.wait(500);
                new QueueTool().waitEmpty();
            } catch(Exception e) {
                e.printStackTrace(System.out);
                throw new RuntimeException(e);
            } finally {
                JemmyProperties.getCurrentTimeouts().setTimeout(timeoutName, previousTimeoutValue);
            }
            return null;
        }
    }
}