/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2014 Oracle and/or its affiliates. All rights reserved.
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

package org.netbeans.modules.profiler.options.ui.v2.impl;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.netbeans.modules.profiler.api.ProfilerIDESettings;
import org.netbeans.modules.profiler.options.ui.v2.ProfilerOptionsPanel;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Jiri Sedlacek
 */
@ServiceProvider( service = ProfilerOptionsPanel.class, position = 20 )
public final class SnapshotsOptionsPanel extends ProfilerOptionsPanel {
    
    public SnapshotsOptionsPanel() {
        initUI();
    }
    
    public String getDisplayName() {
        return "Snapshots";
    }

    public void storeTo(ProfilerIDESettings settings) {
    }

    public void loadFrom(ProfilerIDESettings settings) {
    }

    public boolean equalsTo(ProfilerIDESettings settings) {
        return true;
    }
    
    
    private void initUI() {
        setLayout(new GridBagLayout());
        
        GridBagConstraints c;
        int y = 0;
        int htab = 8;
        int hgap = 10;
        int vgap = 5;
        
        Separator snapshotsSeparator = new Separator("Snapshots");
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = y++;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(0, 0, vgap * 2, 0);
        add(snapshotsSeparator, c);
        
        JLabel onSnapshotLabel = new JLabel("On Take Snapshot:");
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = y;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(0, htab, vgap, 0);
        add(onSnapshotLabel, c);
        
        JComboBox onSnapshotCombo = new JComboBox(new String[] { "Open snapshot", "Save snapshot", "Save and open snapshot" });
        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = y;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(0, hgap, vgap, 0);
        add(onSnapshotCombo, c);
        
        JPanel filler1 = new JPanel(null);
        c = new GridBagConstraints();
        c.gridx = 2;
        c.gridy = y++;
        c.weightx = 1;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.fill = GridBagConstraints.HORIZONTAL;
        add(filler1, c);
        
        JLabel onThreadDumpLabel = new JLabel("On Thread Dump:");
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = y;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(vgap * 3, htab, vgap, 0);
        add(onThreadDumpLabel, c);
        
        JComboBox onThreadDumpCombo = new JComboBox(new String[] { "Open snapshot", "Save snapshot", "Save and open snapshot" });
        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = y;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(vgap * 3, hgap, vgap, 0);
        add(onThreadDumpCombo, c);
        
        JPanel filler2 = new JPanel(null);
        c = new GridBagConstraints();
        c.gridx = 2;
        c.gridy = y++;
        c.weightx = 1;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.fill = GridBagConstraints.HORIZONTAL;
        add(filler2, c);
        
        JLabel onHeapDumpLabel = new JLabel("On Heap Dump:");
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = y;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(vgap * 3, htab, vgap, 0);
        add(onHeapDumpLabel, c);
        
        JComboBox onHeapDumpCombo = new JComboBox(new String[] { "Save snapshot", "Save and open snapshot", "Save and confirm open" });
        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = y;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(vgap * 3, hgap, vgap, 0);
        add(onHeapDumpCombo, c);
        
        JPanel filler3 = new JPanel(null);
        c = new GridBagConstraints();
        c.gridx = 2;
        c.gridy = y++;
        c.weightx = 1;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.fill = GridBagConstraints.HORIZONTAL;
        add(filler3, c);
        
        JLabel onOOMEHeapDumpLabel = new JLabel("On OOME Heap Dump:");
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = y;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(0, htab, vgap, 0);
        add(onOOMEHeapDumpLabel, c);
        
        JComboBox onOOMEHeapDumpCombo = new JComboBox(new String[] { "Do nothing", "Save snapshot", "Save and open snapshot", "Save and confirm open" });
        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = y;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(0, hgap, vgap, 0);
        add(onOOMEHeapDumpCombo, c);
        
        JPanel filler4 = new JPanel(null);
        c = new GridBagConstraints();
        c.gridx = 2;
        c.gridy = y++;
        c.weightx = 1;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.fill = GridBagConstraints.HORIZONTAL;
        add(filler4, c);
        
        JLabel saveHeapDumpLabel = new JLabel("Save Heap Dump to:");
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = y;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(0, htab, vgap, 0);
        add(saveHeapDumpLabel, c);
        
        JComboBox saveHeapDumpCombo = new JComboBox(new String[] { "Project", "Temporary directory", "Custom directory:" });
        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = y;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(0, hgap, vgap, 0);
        add(saveHeapDumpCombo, c);
        
        JPanel filler5 = new JPanel(null);
        c = new GridBagConstraints();
        c.gridx = 2;
        c.gridy = y++;
        c.weightx = 1;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.fill = GridBagConstraints.HORIZONTAL;
        add(filler5, c);
        
        Separator snapshotsWindowSeparator = new Separator("Snapshots Window");
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = y++;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(vgap * 4, 0, vgap * 2, 0);
        add(snapshotsWindowSeparator, c);
        
        JLabel openSnapshotsWindowLabel = new JLabel("Open automatically:");
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = y;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(0, htab, vgap, 0);
        add(openSnapshotsWindowLabel, c);
        
        JComboBox openSnapshotsWindowCombo = new JComboBox(new String[] { "Never", "On new profiling session", "On first snapshot", "On each snapshot" });
        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = y;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(0, hgap, vgap, 0);
        add(openSnapshotsWindowCombo, c);
        
        JPanel filler6 = new JPanel(null);
        c = new GridBagConstraints();
        c.gridx = 2;
        c.gridy = y++;
        c.weightx = 1;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.fill = GridBagConstraints.HORIZONTAL;
        add(filler6, c);
        
        JLabel closeSnapshotsWindowLabel = new JLabel("Close automatically:");
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = y;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(0, htab, vgap, 0);
        add(closeSnapshotsWindowLabel, c);
        
        JComboBox closeSnapshotsWindowCombo = new JComboBox(new String[] { "Never", "On close profiling session" });
        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = y;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(0, hgap, vgap, 0);
        add(closeSnapshotsWindowCombo, c);
        
        JPanel filler7 = new JPanel(null);
        c = new GridBagConstraints();
        c.gridx = 2;
        c.gridy = y++;
        c.weightx = 1;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.fill = GridBagConstraints.HORIZONTAL;
        add(filler7, c);
        
        JPanel filler = new JPanel(null);
        filler.setOpaque(false);
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = y++;
        c.weightx = 1;
        c.weighty = 1;
        c.anchor = GridBagConstraints.NORTHWEST;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.fill = GridBagConstraints.BOTH;
        add(filler, c);
        
    }
    
}
