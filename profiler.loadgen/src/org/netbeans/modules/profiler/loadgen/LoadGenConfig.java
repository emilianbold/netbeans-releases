/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2015 Oracle and/or its affiliates. All rights reserved.
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
package org.netbeans.modules.profiler.loadgen;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;
import org.netbeans.modules.profiler.api.ProfilerDialogs;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.Mnemonics;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Jiri Sedlacek
 */
@NbBundle.Messages({
    "LoadGenConfig_dialogCaption=Configure Load Generator",
    "LoadGenConfig_panelCaption=Select the source for load generator scripts:",
    "LoadGenConfig_projectChoice=&Project folder",
    "LoadGenConfig_projectProgress=Reading project folder...",
    "LoadGenConfig_servicesChoice=&Services | Load Generators",
    "LoadGenConfig_servicesProgress=Reading Services tab...",
    "LoadGenConfig_customChoice=&Custom script",
    "LoadGenConfig_customProgress=Reading custom script...",
    "LoadGenConfig_projectScripts={0} scripts found in project folder.",
    "LoadGenConfig_globalScripts={0} scripts found in Services tab.",
    "LoadGenConfig_scriptsFileFilter=Supported Load Generator Scripts ({0})",
    "LoadGenConfig_noFormatsFound=No supported file formats found.",
    "LoadGenConfig_scriptDialogCaption=Select Load Generator Script",
    "LoadGenConfig_selectScript=Select Load Generator Script",
    "LoadGenConfig_changeScript=click to change..."
})
abstract class LoadGenConfig {
    
    private int mode;
    
    private String[] projectScripts;
    private String[] globalScripts;
    private String[] customScripts;
    
    private final Object projectScriptsLock = new Object();
    private final Object globalScriptsLock = new Object();
    private final Object customScriptsLock = new Object();
    
    
    LoadGenConfig() {}
        
    
    boolean configure(boolean global) {
        Panel p = new Panel(global);
        
        DialogDescriptor dd = new DialogDescriptor(p, Bundle.LoadGenConfig_dialogCaption());
        boolean ret = DialogDisplayer.getDefault().notify(dd) == DialogDescriptor.OK_OPTION;
        
        if (p.pr.isSelected()) mode = 0;
        else if (p.gr.isSelected()) mode = 1;
        else if (p.cr.isSelected()) mode = 2;
        else mode = -1;
        
        return ret;
    }
    
    int getMode() {
        return mode;
    }
    
    String[] getProjectScripts() {
        synchronized (projectScriptsLock) {
            return projectScripts;
        }
    }
    
    String[] getGlobalScripts() {
        synchronized (globalScriptsLock) {
            return globalScripts;
        }
    }
    
    String[] getCustomScripts() {
        synchronized (customScriptsLock) {
            return customScripts;
        }
    }
    
    
    abstract int readMode();
    
    abstract String[] readProjectScripts();
    
    abstract String[] readGlobalScripts();
    
    abstract String[] readCustomScripts();
    
    abstract Set<String> readSupportedExtensions();
    
    
    private class Panel extends JPanel {
        
        final JRadioButton pr;
        final JRadioButton gr;
        final JRadioButton cr;
        
        Panel(boolean global) {
            
            setLayout(new GridBagLayout());
            
            int y = 0;
            GridBagConstraints c;
            
            ButtonGroup g = new ButtonGroup();
            
            JLabel hl = new JLabel(Bundle.LoadGenConfig_panelCaption(), JLabel.LEADING);
            hl.setFont(hl.getFont().deriveFont(Font.BOLD));
            hl.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 150));
            JPanel hp = new JPanel(new BorderLayout());
            hp.setOpaque(true);
            hp.setBackground(Color.WHITE);
            hp.add(hl, BorderLayout.CENTER);
            hp.add(new JSeparator(), BorderLayout.SOUTH);
            c = new GridBagConstraints();
            c.gridx = 0;
            c.gridy = y++;
            c.weightx = 1;
            c.gridwidth = GridBagConstraints.REMAINDER;
            c.anchor = GridBagConstraints.NORTHWEST;
            c.fill = GridBagConstraints.HORIZONTAL;
            c.insets = new Insets(0, 0, 20, 0);
            add(hp, c);
            
            pr = new JRadioButton();
            if (!global) {
                Mnemonics.setLocalizedText(pr, Bundle.LoadGenConfig_projectChoice());
                g.add(pr);
                c = new GridBagConstraints();
                c.gridx = 0;
                c.gridy = y++;
                c.gridwidth = GridBagConstraints.REMAINDER;
                c.anchor = GridBagConstraints.WEST;
                c.fill = GridBagConstraints.NONE;
                c.insets = new Insets(0, 20, 0, 10);
                add(pr, c);
            }
            
            final JLabel pl = new JLabel(Bundle.LoadGenConfig_projectProgress());
            if (!global) {
                pl.setEnabled(false);
                c = new GridBagConstraints();
                c.gridx = 0;
                c.gridy = y++;
                c.gridwidth = GridBagConstraints.REMAINDER;
                c.anchor = GridBagConstraints.WEST;
                c.fill = GridBagConstraints.NONE;
                c.insets = new Insets(0, 41, 15, 10);
                add(pl, c);
            }
            
            gr = new JRadioButton();
            Mnemonics.setLocalizedText(gr, Bundle.LoadGenConfig_servicesChoice());
            g.add(gr);
            c = new GridBagConstraints();
            c.gridx = 0;
            c.gridy = y++;
            c.gridwidth = GridBagConstraints.REMAINDER;
            c.anchor = GridBagConstraints.WEST;
            c.fill = GridBagConstraints.NONE;
            c.insets = new Insets(0, 20, 0, 10);
            add(gr, c);
            
            final JLabel gl = new JLabel(Bundle.LoadGenConfig_servicesProgress());
            gl.setEnabled(false);
            c = new GridBagConstraints();
            c.gridx = 0;
            c.gridy = y++;
            c.gridwidth = GridBagConstraints.REMAINDER;
            c.anchor = GridBagConstraints.WEST;
            c.fill = GridBagConstraints.NONE;
            c.insets = new Insets(0, 41, 15, 10);
            add(gl, c);
            
            cr = new JRadioButton();
            Mnemonics.setLocalizedText(cr, Bundle.LoadGenConfig_customChoice());
            g.add(cr);
            cr.setSelected(true);
            c = new GridBagConstraints();
            c.gridx = 0;
            c.gridy = y++;
            c.gridwidth = GridBagConstraints.REMAINDER;
            c.anchor = GridBagConstraints.WEST;
            c.fill = GridBagConstraints.NONE;
            c.insets = new Insets(0, 20, 0, 10);
            add(cr, c);
            
            final JButton cb = new JButton(Bundle.LoadGenConfig_customProgress()) {
                protected void fireActionPerformed(ActionEvent e) {
                    super.fireActionPerformed(e);
                    cr.setSelected(true);
                    final JButton b = this;
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() { selectCustomScript(Panel.this, b); }
                    });
                }
                public Dimension getMinimumSize() {
                    return getPreferredSize();
                }
                public Dimension getMaximumSize() {
                    return getPreferredSize();
                }
            };
            cb.setEnabled(false);
            cb.setContentAreaFilled(false);
            cb.setBorderPainted(true);
            cb.setMargin(new Insets(0, 0, 0, 0));
            cb.setBorder(BorderFactory.createEmptyBorder());
            cb.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            c = new GridBagConstraints();
            c.gridx = 0;
            c.gridy = y++;
            c.gridwidth = GridBagConstraints.REMAINDER;
            c.anchor = GridBagConstraints.WEST;
            c.fill = GridBagConstraints.NONE;
            c.insets = new Insets(0, 41, 20, 10);
            add(cb, c);
            
            c = new GridBagConstraints();
            c.gridx = 0;
            c.gridy = y++;
            c.weighty = 1;
            c.gridwidth = GridBagConstraints.REMAINDER;
            c.anchor = GridBagConstraints.NORTHWEST;
            c.fill = GridBagConstraints.BOTH;
            c.insets = new Insets(0, 0, 0, 0);
            add(new JPanel(null), c);
            
            final RequestProcessor processor = new RequestProcessor("LoadGenConfig Processor", global ? 2 : 3); // NOI18N
            
            if (!global) processor.post(new Runnable() {
                public void run() {
                    synchronized (projectScriptsLock) {
                        projectScripts = readProjectScripts();
                    }
                    final int number = projectScripts.length;
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            pl.setText(Bundle.LoadGenConfig_projectScripts(number));
                            pr.setEnabled(number > 0);
                            if (number > 0) {
                                StringBuilder sb = new StringBuilder("<html>"); // NOI18N
                                for (String script : projectScripts) sb.append(script).append("<br>"); // NOI18N
                                sb.append("</html>"); // NOI18N
                                pl.setToolTipText(sb.toString());
                                pr.setSelected(readMode() == 0);
                            } else {
                                pl.setToolTipText(null);
                            }
                        }
                    });
                }
            });
            
            processor.post(new Runnable() {
                public void run() {
                    synchronized (globalScriptsLock) {
                        globalScripts = readGlobalScripts();
                    }
                    final int number = globalScripts.length;
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            gl.setText(Bundle.LoadGenConfig_globalScripts(number));
                            gr.setEnabled(number > 0);
                            if (number > 0) {
                                StringBuilder sb = new StringBuilder("<html>"); // NOI18N
                                for (String script : globalScripts) sb.append(script).append("<br>"); // NOI18N
                                sb.append("</html>"); // NOI18N
                                gl.setToolTipText(sb.toString());
                                gr.setSelected(readMode() == 1);
                            } else {
                                gl.setToolTipText(null);
                            }
                        }
                    });
                }
            });
            
            processor.post(new Runnable() {
                public void run() {
                    synchronized (customScriptsLock) {
                        customScripts = readCustomScripts();
                        final String customScript = customScripts.length == 0 ? null : customScripts[0];
                        final File f = customScript != null ? new File(customScript) : null;
                        if (f != null && f.isFile()) {
                            SwingUtilities.invokeLater(new Runnable() {
                                public void run() {
                                    updateButton(cb, f.getName(), customScript);
                                    cb.setEnabled(true);
                                    cr.setSelected(readMode() == 2);
                                }
                            });
                        } else {
                            customScripts = new String[0];
                            SwingUtilities.invokeLater(new Runnable() {
                                public void run() {
                                    updateButton(cb, null, null);
                                    cb.setEnabled(true);
                                }
                            });
                        }
                    }
                }
            });
            
        }
        
    }
    
    private void selectCustomScript(Component parent, JButton button) {
        final Set<String> extensions = readSupportedExtensions();
        if (extensions.isEmpty()) {
            ProfilerDialogs.displayError(Bundle.LoadGenConfig_noFormatsFound());
            return;
        }
        
        StringBuilder sb = new StringBuilder();
        for (String ext : extensions) sb.append("*.").append(ext).append(", "); // NOI18N
        sb.setLength(sb.length() - 2);
        final String descr = Bundle.LoadGenConfig_scriptsFileFilter(sb.toString());
        final JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle(Bundle.LoadGenConfig_scriptDialogCaption());
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.setFileFilter(new FileFilter() {
            public boolean accept(File f) {
                if (f.isDirectory()) return true;
                else return extensions.contains(FileUtil.getExtension(f.getAbsolutePath()).toLowerCase());
            }
            public String getDescription() {
                return descr;
            }
        });
        
        if (customScripts != null && customScripts.length > 0)
            chooser.setCurrentDirectory(new File(customScripts[0]));
        
        if (chooser.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION) {
            File f = chooser.getSelectedFile();
            String custom = null;
            try { custom = f.getCanonicalPath(); }
            catch (IOException e) { custom = f.getAbsolutePath(); }
            updateButton(button, f.getName(), custom);
            synchronized (customScriptsLock) {
                customScripts = new String[] { custom };
            }
        }
    }
    
    private static void updateButton(JButton button, String name, String path) {
        if (path == null) {
            button.setText("<html><a href='#'>" + Bundle.LoadGenConfig_selectScript() + "</a></html>"); // NOI18N
            button.setToolTipText(null);
        } else {
            button.setText("<html>" + name + ", <a href='#'>" + Bundle.LoadGenConfig_changeScript() + "</a></html>");
            button.setToolTipText(path);
        }
    }
    
}
