/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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

package org.netbeans.modules.mobility.cldcplatform.wizard;

import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.RequestProcessor;
import org.openide.util.NbBundle;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;
import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.*;
import javax.swing.filechooser.FileFilter;
import org.netbeans.modules.mobility.cldcplatform.J2MEPlatform;
import org.netbeans.spi.mobility.cldcplatform.CustomCLDCPlatformConfigurator;
import org.openide.filesystems.FileChooserBuilder;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.Utilities;

/**
 *
 * @author Adam Sotona
 */
final public class FindPanel extends javax.swing.JPanel implements SearchRunnable.Notifier {
    
    static final String PROP_PLATFORM_FOLDERS = "PlatformFolders"; // NOI18N
    
    private static final String ALREADY_INSTALLED = NbBundle.getMessage(FindPanel.class, "msg_already_installed"); //NOI18N
    
    private final DefaultListModel platformsListModel = new DefaultListModel();
    private final CheckListener checkListener = new CheckListener();
    private final FoldersRenderer foldersRenderer = new FoldersRenderer();
    private final Set<File> selectedFolders = new HashSet<File>();
    private final Set<File> installedFolders = new HashSet<File>();
    private final FindWizardPanel wizardPanel;
    
    private RequestProcessor.Task searchTask;
    private SearchRunnable searchRunnable;
    private Set<File> visitedDirectories = null;
    private WizardDescriptor wizardDescriptor;
    
    /** Creates new form FindPanel */
    public FindPanel(FindWizardPanel wizardPanel) {
        this.wizardPanel = wizardPanel;
        JavaPlatform p[] = JavaPlatformManager.getDefault().getInstalledPlatforms();
        for ( JavaPlatform jp : p ) {
        	for ( FileObject fo : (Collection<FileObject>)jp.getInstallFolders()) {
                installedFolders.add(FileUtil.toFile(fo));
            }
        }
        initComponents();
        platformsList.addMouseListener(checkListener);
        platformsList.addKeyListener(checkListener);
    }
    
    
    public void showError(final String message) {
        if (wizardDescriptor != null) {
            wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, message); // NOI18N
        }
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jLabel5 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        platformsList = new javax.swing.JList();
        jButton1 = new javax.swing.JButton();

        setName(NbBundle.getMessage(FindPanel.class, "LBL_FindPanel_Platforms")); // NOI18N
        setPreferredSize(new java.awt.Dimension(540, 450));
        setLayout(new java.awt.GridBagLayout());

        jLabel5.setLabelFor(platformsList);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel5, NbBundle.getMessage(FindPanel.class, "LBL_FindPanel_Select_Platforms")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(jLabel5, gridBagConstraints);
        jLabel5.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FindPanel.class, "ACSD_FindPanel_Select_Platform")); // NOI18N

        jScrollPane1.setPreferredSize(new java.awt.Dimension(300, 150));

        platformsList.setModel(platformsListModel);
        platformsList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        platformsList.setCellRenderer(foldersRenderer);
        jScrollPane1.setViewportView(platformsList);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        add(jScrollPane1, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jButton1, NbBundle.getMessage(FindPanel.class, "LBL_FindPanel_Find_More")); // NOI18N
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        add(jButton1, gridBagConstraints);
        jButton1.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FindPanel.class, "ACSD_FindPanel_Find_More")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents
    
    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        if (searchTask != null && !searchTask.isFinished()) return;
        String aDescription = NbBundle.getMessage(FindPanel.class, "AD_LocationChooser"); //NOI18N
        String title = NbBundle.getMessage(FindPanel.class, "Title_FindPanel_Choose_To_Search"); //NOI18N
        Badger badger = new Badger();
        File dir;
        if ((dir = new FileChooserBuilder(getClass().getName()).setDirectoriesOnly(true).
                setTitle(title).
                setAccessibleDescription(aDescription).
                setBadgeProvider(badger).
                setDefaultWorkingDirectory(File.listRoots().length > 0 ? File.listRoots()[0] : null).
                setFileFilter(badger).showOpenDialog()) != null) {

            searchRunnable = new SearchRunnable(this, dir, -1);
            jButton1.setEnabled(false);
            showError(NbBundle.getMessage(FindPanel.class, "WARN_SearchInProgress"));//NOI18N
            searchTask = RequestProcessor.getDefault().post(searchRunnable);
        }
    }//GEN-LAST:event_jButton1ActionPerformed

    private static final class Badger extends FileFilter implements FileChooserBuilder.BadgeProvider {
        final Icon badge = ImageUtilities.loadImageIcon("org/netbeans/modules/java/platform/resources/platformBadge.gif", false);

        public boolean accept(File f) {
            return f.isDirectory();
        }

        public String getDescription() {
            return NbBundle.getMessage(Badger.class, "TXT_PlatformFolder"); //NOI18N
        }
        
        public Icon getBadge(File file) {
            if (isPlatformDir (file)) {
                return badge;
            }
            return null;
        }

        public int getXOffset() {
            return -1;
        }

        public int getYOffset() {
            return -1;
        }

        private boolean isPlatformDir(File f) {
            //XXX: Workaround of hard NFS mounts on Solaris.
            final int osId = Utilities.getOperatingSystem();
            if (osId == Utilities.OS_SOLARIS || osId == Utilities.OS_SUNOS) {
                return false;
            }
            FileObject fo = (f != null) ? convertToValidDir(f) : null;
            if (fo != null) {
                //XXX: Workaround of /net folder on Unix, the folders in the root are not badged as platforms.
                // User can still select them.
                try {
                    if (Utilities.isUnix() && (fo.getParent() == null ||
                        fo.getFileSystem().getRoot().equals(fo.getParent()))) {
                        return false;
                    }
                } catch (FileStateInvalidException e) {
                    return false;
                }
                return isPossibleJ2MEPlatform(FileUtil.toFile(fo));
            }
            return false;
        }

        public boolean isPossibleJ2MEPlatform(final File directory) {
            Collection <? extends CustomCLDCPlatformConfigurator> customConfigurators = Lookup.getDefault().lookupAll(CustomCLDCPlatformConfigurator.class);
            for ( CustomCLDCPlatformConfigurator pc : customConfigurators )
                if (pc.isPossiblePlatform(directory)) return true;
            final FileObject dir = FileUtil.toFileObject(directory);
            return dir != null && J2MEPlatform.findTool("emulator", Collections.singletonList(dir)) != null // NOI18N
                    && J2MEPlatform.findTool("preverify", Collections.singletonList(dir)) != null; //NOI18N
        }

        private static FileObject convertToValidDir(File f) {
            FileObject fo;
            File testFile = new File(f.getPath());
            if (testFile == null || testFile.getParent() == null) {
                // BTW this means that roots of file systems can't be project
                // directories.
                return null;
            }

            /**ATTENTION: on Windows may occure dir.isDirectory () == dir.isFile () == true then
             * its used testFile instead of dir.
             */
            if (!testFile.isDirectory()) {
                return null;
            }

            fo = FileUtil.toFileObject(FileUtil.normalizeFile(f));
            return fo;
        }

    }

    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JList platformsList;
    // End of variables declaration//GEN-END:variables
    
    
    public void readSettings(final WizardDescriptor descriptor) {
        this.wizardDescriptor = descriptor;
        if (visitedDirectories == null) {
            visitedDirectories = new HashSet<File>();
            jButton1.setEnabled(false);
            synchronized (platformsListModel) {
                platformsListModel.clear();
            }
            showError(NbBundle.getMessage(FindPanel.class, "WARN_SearchInProgress"));//NOI18N
            searchRunnable = new SearchRunnable(this, WindowsRegistryReader.traverseRegistry(), 3);
            searchTask = RequestProcessor.getDefault().post(searchRunnable);
        }
    }
    
    public void storeSettings(final WizardDescriptor descriptor) {
        // WARNING - called twice - probably bug in Dialog API
        if (searchRunnable != null) {
            searchRunnable.stop();
            searchTask.waitFinished();
            searchRunnable = null;
            searchTask = null;
        }
        descriptor.putProperty(PROP_PLATFORM_FOLDERS, selectedFolders);
    }
    
    public void notifyPossiblePlatformFound(final File directory) {
        if (visitedDirectories.contains(directory)) return;
        visitedDirectories.add(directory);
        if (!installedFolders.contains(directory)) selectedFolders.add(directory);
        synchronized (platformsListModel) {
            platformsListModel.addElement(directory);
        }
        final int i = platformsListModel.size() - 1;
        final Rectangle r = platformsList.getCellBounds(i, i);
        if (r != null) platformsList.scrollRectToVisible(r);
        wizardPanel.fireChanged();
    }
    
    public void notifySearchFinished() {
        jButton1.setEnabled(true);
        wizardPanel.fireChanged();
        if (platformsListModel.isEmpty()) jButton1ActionPerformed(null);
    }
    
    public boolean isStateValid() {
        if (selectedFolders.isEmpty()) {
            showError(NbBundle.getMessage(FindPanel.class, "ERR_NothingSelected"));//NOI18N
            return false;
        }
        showError(null);
        return true;
    }
    
    private class CheckListener implements MouseListener, KeyListener {
        
        CheckListener() {
            // To avoid creation of accessor class
        }
        
        public void mouseClicked(final MouseEvent e) {
            if (e.getX() < 20)
                check();
        }
        
        public void mouseEntered(@SuppressWarnings("unused")
		final MouseEvent e) {
        }
        
        public void mouseExited(@SuppressWarnings("unused")
		final MouseEvent e) {
        }
        
        public void mousePressed(@SuppressWarnings("unused")
		final MouseEvent e) {
        }
        
        public void mouseReleased(@SuppressWarnings("unused")
		final MouseEvent e) {
        }
        
        public void keyPressed(final KeyEvent e) {
            if (e.getKeyChar() == ' ')
                check();
        }
        
        public void keyReleased(@SuppressWarnings("unused")
		final KeyEvent e) {
        }
        
        public void keyTyped(@SuppressWarnings("unused")
		final KeyEvent e) {
        }
        
        @SuppressWarnings("synthetic-access")
		private void check() {
            final File o = (File)platformsList.getSelectedValue();
            if (o != null) {
                if (!selectedFolders.remove(o)) selectedFolders.add(o);
                platformsList.repaint();
                wizardPanel.fireChanged();
            }
        }
        
    }
    
    private class FoldersRenderer extends JCheckBox implements ListCellRenderer {
        
        Border emptyBorder = BorderFactory.createEmptyBorder(1, 1, 1, 1);
        
        public FoldersRenderer() {
            setOpaque(true);
        }
        
        @SuppressWarnings("synthetic-access")
		public Component getListCellRendererComponent(@SuppressWarnings("unused")
		final JList list, final Object value, @SuppressWarnings("unused")
		final int index, final boolean isSelected, final boolean cellHasFocus) {
            setBackground(isSelected ? platformsList.getSelectionBackground() : platformsList.getBackground());
            setForeground(isSelected ? platformsList.getSelectionForeground() : platformsList.getForeground());
            Border border = null;
            if (cellHasFocus)
                border = UIManager.getBorder("List.focusCellHighlightBorder"); // NOI18N
            setBorder(border != null ? border : emptyBorder);
            setSelected(selectedFolders.contains(value));
            if (installedFolders.contains(value)) {
                setText(value.toString() + ALREADY_INSTALLED);
            } else {
                setText(value.toString());
            }
            return this;
        }
        
    }
    
}
