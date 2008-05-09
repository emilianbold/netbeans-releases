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

package org.netbeans.modules.mobility.cldcplatform.wizard;
import org.netbeans.modules.mobility.cldcplatform.J2MEPlatform;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.spi.mobility.cldcplatform.CustomCLDCPlatformConfigurator;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.FileObject;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

import javax.swing.*;
import java.io.File;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.*;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * @author David Kaspar
 */
public class SearchRunnable implements Runnable {
    
    protected volatile boolean stop = false;
    protected Notifier notifier;
    private Set<File> roots;
    protected SearchPanel panel;
    protected ProgressHandle progress;
    private int maxLevel;
    private Collection<? extends CustomCLDCPlatformConfigurator> customConfigurators;
    
    public SearchRunnable(Notifier notifier, Set<File> rts, int maxLevel) {
        this.notifier = notifier;
        this.roots = new HashSet<File>();
        for ( File f : rts ) 
            this.roots.add(FileUtil.normalizeFile(f));
        this.maxLevel = maxLevel;
        this.customConfigurators = Lookup.getDefault().lookup(new Lookup.Template<CustomCLDCPlatformConfigurator>(CustomCLDCPlatformConfigurator.class)).allInstances();
    }
    
    public SearchRunnable(Notifier notifier, File root, int maxLevel) {
        this(notifier, Collections.singleton(root), maxLevel);
    }
    
    public void stop() {
        stop = true;
    }
    
    public void run() {
        final String titleString = NbBundle.getMessage(SearchRunnable.class, "Title_SearchRunnable_Searching");
        progress = ProgressHandleFactory.createHandle(titleString);
        panel = new SearchPanel(ProgressHandleFactory.createProgressComponent(progress));
        progress.start();
        progress.switchToIndeterminate();
        final Dialog[] dialog = new Dialog[1];
        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                    dialog[0] = DialogDisplayer.getDefault().createDialog(new DialogDescriptor(panel, titleString, true, new Object[]{
                        NotifyDescriptor.CANCEL_OPTION,
                    }, NotifyDescriptor.CANCEL_OPTION, DialogDescriptor.DEFAULT_ALIGN, new HelpCtx(SearchPanel.class), new ActionListener() {
                        public void actionPerformed(@SuppressWarnings("unused") ActionEvent e) {
                            stop();
                        }
                    }));
                    if (dialog[0] != null) {
                        dialog[0].getAccessibleContext().setAccessibleName(NbBundle.getMessage(SearchRunnable.class, "ACSN_searchingPlatform")); //NOI18N
                        dialog[0].getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SearchRunnable.class, "ACSD_searchingPlatform")); //NOI18N
                    }
                        
                }
            });
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    if (! stop)
                        dialog[0].setVisible(true);
                }
            });
            
            for (File f : roots)
            	checkDirectory(f, maxLevel);
            
            stop();
            
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    progress.finish();
                    dialog[0].setVisible(false);
                    notifier.notifySearchFinished();
                }
            });
        } catch (InterruptedException e) {
            e.printStackTrace(); // TODO
        } catch (InvocationTargetException e) {
            e.printStackTrace(); // TODO
        }
    }
    
    private void checkDirectory(final File directory, int maxLevel) {
        final LinkedList<File> list = new LinkedList<File>();
        list.addLast(directory);
        File file;
        int current = 0;
        int counter = 1;
        while (! stop  &&  ! list.isEmpty() && maxLevel != 0) {
            file = list.removeFirst();
            checkForPlatform(file);
            if (maxLevel != 1) {
                final File[] files = file.listFiles();
                if (files != null)
                    for (int i = 0; i < files.length; i++) {
                    final File subfile = files[i];
                    if (filter(subfile))
                        continue;
                    list.addLast(subfile);
                    }
            }
            current ++;
            if ((current % 100) == 0) {
                final File finalFile = file;
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        panel.setCurrentPath(finalFile);
                    }
                });
                
            }
            counter --;
            if (counter == 0) {
                maxLevel--;
                counter = list.size();
            }
        }
        
    }
    
    private boolean filter(final File file) {
        return ! file.isDirectory();
    }
    
    public boolean isPossibleJ2MEPlatform(final File directory) {
    	for ( CustomCLDCPlatformConfigurator pc : customConfigurators ) 
            if (pc.isPossiblePlatform(directory)) return true;
        final FileObject dir = FileUtil.toFileObject(directory);
        return dir != null && J2MEPlatform.findTool("emulator", Collections.singletonList(dir)) != null // NOI18N
                && J2MEPlatform.findTool("preverify", Collections.singletonList(dir)) != null; //NOI18N
    }
    
    private void checkForPlatform(final File directory) {
        if (directory == null || !directory.isDirectory()) return;
        if (isPossibleJ2MEPlatform(directory)) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    panel.increment();
                    notifier.notifyPossiblePlatformFound(directory);
                }
            });
        }
    }
    
    public interface Notifier {
        public void notifyPossiblePlatformFound(File directory);
        public void notifySearchFinished();
    }
    
    static class SearchPanel extends JPanel {
        
        private int count = 0;
        final private JLabel searchLabel;
        final private JLabel foundLabel;
        
        public SearchPanel(JComponent progressComponent) {
            setLayout(new GridBagLayout());
            GridBagConstraints gbc;
            
            searchLabel = new JLabel();
            gbc = new GridBagConstraints(0, 0, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(12, 12, 6, 12), 0, 0);
            add(searchLabel, gbc);
            
            gbc = new GridBagConstraints(0, 1, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 12, 6, 12), 0, 0);
            add(progressComponent, gbc);
            
            foundLabel = new JLabel();
            gbc = new GridBagConstraints(0, 2, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 12, 0, 12), 0, 0);
            add(foundLabel, gbc);
            
            updateMessageLabel();
            setPreferredSize(new Dimension(400, 70));
        }
        
        private void updateMessageLabel() {
            final int c = count;
            if (c <= 0)
                foundLabel.setText(NbBundle.getMessage(SearchRunnable.class, "LBL_SearchRunnable_No_platform_found")); //NOI18N
            else
                foundLabel.setText(NbBundle.getMessage(SearchRunnable.class, "LBL_SearchRunnable_Found_platforms", String.valueOf(count)));  //NOI18N
        }
        
        public void increment() {
            count ++;
            updateMessageLabel();
        }
        
        public void setCurrentPath(final File finalFile) {
            searchLabel.setText(NbBundle.getMessage(SearchRunnable.class, "LBL_SearchingPath", finalFile.getAbsolutePath())); // NOI18N
        }
        
    }
    
}
