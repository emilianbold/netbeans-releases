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

/*
 * FixVersionConflictPanel.java
 *
 * Created on Apr 7, 2009, 2:57:30 PM
 */

package org.netbeans.modules.maven.graph;

import java.awt.Component;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.apache.maven.shared.dependency.tree.DependencyNode;
import org.openide.util.NbBundle;

/**
 *
 * @author Dafe Simonek
 */
public class FixVersionConflictPanel extends javax.swing.JPanel {

    private DependencyGraphScene scene;
    private ArtifactGraphNode conflictNode;
    private List<ArtifactVersion> clashingVersions;
    private ExclusionTargets eTargets;

    /** Creates new form FixVersionConflictPanel */
    public FixVersionConflictPanel (DependencyGraphScene scene, ArtifactGraphNode node) {
        this.scene = scene;
        this.conflictNode = node;

        initComponents();

        ExclTargetRenderer render = new ExclTargetRenderer(excludesList, this);
        excludesList.setCellRenderer(render);
        excludesList.addMouseListener(render);
        excludesList.addKeyListener(render);

        eTargets = new ExclusionTargets(conflictNode, getClashingVersions().get(0));

        visualizeRecommandations(computeRecommandations());
    }

    FixDescription getResult() {
        FixDescription res = new FixDescription();
        res.isSet = addSetCheck.isSelected();
        res.version2Set = res.isSet ? (ArtifactVersion) versionList.getSelectedValue() : null;
        res.isExclude = excludeCheck.isSelected();
        if (res.isExclude) {
            res.exclusionTargets = new HashSet<Artifact>();
            res.conflictParents = new HashSet<DependencyNode>();
            ListModel lm = excludesList.getModel();
            for (int i = 0; i < lm.getSize(); i++) {
                ExclTargetEntry entry = (ExclTargetEntry) lm.getElementAt(i);
                if (entry.isSelected) {
                    res.exclusionTargets.add(entry.artif);
                    res.conflictParents.addAll(eTargets.getConflictParents(entry.artif));
                }
            }
        }
        return res;
    }

    private void addSetCheckChanged() {
        boolean isSel = addSetCheck.isSelected();
        versionL.setEnabled(isSel);
        versionList.setEnabled(isSel);
        if (isSel && versionList.getSelectedValue() == null) {
            versionList.setSelectedIndex(0);
        }
    }

    private void excludeCheckChanged() {
        boolean isSel = excludeCheck.isSelected();
        fromDirectL.setEnabled(isSel);
        excludesList.setEnabled(isSel);
    }

    private String getClashingVersionsAsText () {
        StringBuilder sb = new StringBuilder();
        boolean isFirst = true;
        for (ArtifactVersion av : getClashingVersions()) {
            if (!isFirst) {
                sb.append(", "); //NOI18N
            } else {
                isFirst = false;
            }
            sb.append(av.toString());
        }
        return sb.toString();
    }

    private List<ArtifactVersion> getClashingVersions () {
        if (clashingVersions == null) {
            clashingVersions = new ArrayList<ArtifactVersion>();
            clashingVersions.add(new DefaultArtifactVersion(conflictNode.getArtifact().getArtifact().getVersion()));
            Set<DependencyNode> deps = conflictNode.getDuplicatesOrConflicts();
            ArtifactVersion av = null;
            for (DependencyNode dn : deps) {
                if (dn.getState() == DependencyNode.OMITTED_FOR_CONFLICT) {
                    av = new DefaultArtifactVersion(dn.getArtifact().getVersion());
                    if (!clashingVersions.contains(av)) {
                        clashingVersions.add(av);
                    }
                }
            }
            Collections.sort(clashingVersions);
            Collections.reverse(clashingVersions);
        }
        return clashingVersions;
    }


    static class FixDescription {
        boolean isSet = false;
        boolean isExclude = false;
        ArtifactVersion version2Set = null;
        Set<Artifact> exclusionTargets = null;
        Set<DependencyNode> conflictParents = null;
    }

    /** Checks the circumstances of version conflict and offers solution.
     *
     * @return description of found recommended solution
     */
    private FixDescription computeRecommandations () {
        FixDescription recs = new FixDescription();

        boolean isDirect = conflictNode.getPrimaryLevel() == 1;
        ArtifactVersion usedVersion = new DefaultArtifactVersion(
                conflictNode.getArtifact().getArtifact().getVersion());
        ArtifactVersion newAvailVersion = getClashingVersions().get(0);

        // case: direct dependency to older version -> recommend update to newer
        if (isDirect && usedVersion.compareTo(newAvailVersion) < 0) {
            recs.isSet = true;
            recs.version2Set = newAvailVersion;
        }

        // case: more then one exclusion target, several of them are "good guys"
        // which means they have non conflicting dependency on newer version ->
        // recommend adding dependency exclusion to all but mentioned "good" targets
        Set<Artifact> nonConf = eTargets.getNonConflicting();
        if (!nonConf.isEmpty() && eTargets.getAll().size() > 1) {
            recs.isExclude = true;
            recs.exclusionTargets = eTargets.getConflicting();
        }

        // last try - brute force -> recommend exclude all and add dependency in some cases
        if (!recs.isSet && !recs.isExclude) {
            if (usedVersion.compareTo(newAvailVersion) < 0) {
                recs.isSet = true;
                recs.version2Set = newAvailVersion;
                recs.isExclude = true;
                recs.exclusionTargets = eTargets.getAll();
            }
        }

        return recs;
    }

    private void visualizeRecommandations(FixDescription recs) {
        addSetCheck.setText(NbBundle.getMessage(FixVersionConflictPanel.class,
                "FixVersionConflictPanel.addSetCheck.text", getSetText())); // NOI18N
        addSetCheck.setSelected(recs.isSet);
        addSetCheckChanged();

        List<ArtifactVersion> versions = getClashingVersions();
        DefaultListModel model = new DefaultListModel();
        for (ArtifactVersion av : versions) {
            model.addElement(av);
        }
        versionList.setModel(model);
        versionList.setSelectedIndex(0);

        if (recs.version2Set != null) {
            versionList.setSelectedValue(recs.version2Set, true);
        }

        excludeCheck.setText(NbBundle.getMessage(FixVersionConflictPanel.class,
                "FixVersionConflictPanel.excludeCheck.text")); // NOI18N
        excludeCheck.setSelected(recs.isExclude);
        excludeCheckChanged();

        Set<Artifact> exclTargets = eTargets.getAll();
        if (!exclTargets.isEmpty()) {
            DefaultListModel lModel = new DefaultListModel();
            for (Artifact exc : exclTargets) {
                lModel.addElement(new ExclTargetEntry(exc,
                        recs.exclusionTargets != null && recs.exclusionTargets.contains(exc)));
            }
            excludesList.setModel(lModel);
        } else {
            excludeCheck.setEnabled(false);
        }

        updateSummary();
    }

    private String getSetText () {
        return conflictNode.getPrimaryLevel() == 1 ?
            NbBundle.getMessage(FixVersionConflictPanel.class, "LBL_SetDep")
            : NbBundle.getMessage(FixVersionConflictPanel.class, "LBL_AddDep");
    }

    private void updateSummary () {
        FixDescription curFix = getResult();
        String part1 = "", part2 = "";
        if (curFix.isSet && curFix.version2Set != null) {
            part1 = NbBundle.getMessage(FixVersionConflictPanel.class,
                    "FixVersionConflictPanel.sumPart1.text",
                    getSetText(), curFix.version2Set.toString(),
                    conflictNode.getArtifact().getArtifact().getArtifactId());
        }
        if (curFix.isExclude && !curFix.exclusionTargets.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            boolean isFirst = true;
            for (Artifact art : curFix.exclusionTargets) {
                if (!isFirst) {
                    sb.append(", ");
                } else {
                    isFirst = false;
                }
                sb.append(art.getArtifactId());
            }
            part2 = NbBundle.getMessage(FixVersionConflictPanel.class,
                    "FixVersionConflictPanel.sumPart2.text",
                    conflictNode.getArtifact().getArtifact().getArtifactId(),
                    sb.toString());
        }

        if (part1.equals("") && part2.equals("")) {
            part1 = NbBundle.getMessage(FixVersionConflictPanel.class, "FixVersionConflictPanel.noChanges");
        }

        if (!part1.equals("") && !part2.equals("")) {
            part1 = part1 + " ";
        }

        sumContent.setText(NbBundle.getMessage(FixVersionConflictPanel.class,
                "FixVersionConflictPanel.sumContent.text", part1, part2));
    }


    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jSeparator1 = new javax.swing.JSeparator();
        fixesP = new javax.swing.JPanel();
        addSetP = new javax.swing.JPanel();
        addSetCheck = new javax.swing.JCheckBox();
        versionL = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        versionList = new javax.swing.JList();
        excludeP = new javax.swing.JPanel();
        excludeCheck = new javax.swing.JCheckBox();
        fromDirectL = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        excludesList = new javax.swing.JList();
        fixPossibL = new javax.swing.JLabel();
        summaryL = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        sumContent = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        conflictL = new javax.swing.JLabel();

        addSetCheck.setText(org.openide.util.NbBundle.getMessage(FixVersionConflictPanel.class, "FixVersionConflictPanel.addSetCheck.text")); // NOI18N
        addSetCheck.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addSetCheckActionPerformed(evt);
            }
        });

        versionL.setText(org.openide.util.NbBundle.getMessage(FixVersionConflictPanel.class, "FixVersionConflictPanel.versionL.text")); // NOI18N

        versionList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                versionListValueChanged(evt);
            }
        });
        jScrollPane1.setViewportView(versionList);

        org.jdesktop.layout.GroupLayout addSetPLayout = new org.jdesktop.layout.GroupLayout(addSetP);
        addSetP.setLayout(addSetPLayout);
        addSetPLayout.setHorizontalGroup(
            addSetPLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(addSetPLayout.createSequentialGroup()
                .add(addSetPLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(addSetCheck)
                    .add(versionL))
                .add(46, 46, 46))
            .add(addSetPLayout.createSequentialGroup()
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 208, Short.MAX_VALUE)
                .addContainerGap())
        );
        addSetPLayout.setVerticalGroup(
            addSetPLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(addSetPLayout.createSequentialGroup()
                .add(addSetCheck)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(versionL)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 156, Short.MAX_VALUE)
                .addContainerGap())
        );

        excludeCheck.setText(org.openide.util.NbBundle.getMessage(FixVersionConflictPanel.class, "FixVersionConflictPanel.excludeCheck.text")); // NOI18N
        excludeCheck.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                excludeCheckActionPerformed(evt);
            }
        });

        fromDirectL.setText(org.openide.util.NbBundle.getMessage(FixVersionConflictPanel.class, "FixVersionConflictPanel.fromDirectL.text")); // NOI18N

        jScrollPane2.setViewportView(excludesList);

        org.jdesktop.layout.GroupLayout excludePLayout = new org.jdesktop.layout.GroupLayout(excludeP);
        excludeP.setLayout(excludePLayout);
        excludePLayout.setHorizontalGroup(
            excludePLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(excludePLayout.createSequentialGroup()
                .add(excludePLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(excludeCheck)
                    .add(fromDirectL))
                .addContainerGap(46, Short.MAX_VALUE))
            .add(jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 268, Short.MAX_VALUE)
        );
        excludePLayout.setVerticalGroup(
            excludePLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(excludePLayout.createSequentialGroup()
                .add(excludeCheck)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(fromDirectL)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 156, Short.MAX_VALUE)
                .addContainerGap())
        );

        org.jdesktop.layout.GroupLayout fixesPLayout = new org.jdesktop.layout.GroupLayout(fixesP);
        fixesP.setLayout(fixesPLayout);
        fixesPLayout.setHorizontalGroup(
            fixesPLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(fixesPLayout.createSequentialGroup()
                .add(addSetP, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(excludeP, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        fixesPLayout.setVerticalGroup(
            fixesPLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(addSetP, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .add(excludeP, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        fixPossibL.setText(org.openide.util.NbBundle.getMessage(FixVersionConflictPanel.class, "FixVersionConflictPanel.fixPossibL.text")); // NOI18N

        summaryL.setText(org.openide.util.NbBundle.getMessage(FixVersionConflictPanel.class, "FixVersionConflictPanel.summaryL.text")); // NOI18N

        jPanel1.setLayout(new java.awt.GridBagLayout());

        sumContent.setText(org.openide.util.NbBundle.getMessage(FixVersionConflictPanel.class, "FixVersionConflictPanel.sumContent.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        jPanel1.add(sumContent, gridBagConstraints);

        jPanel2.setLayout(new java.awt.GridBagLayout());

        conflictL.setText(org.openide.util.NbBundle.getMessage(FixVersionConflictPanel.class, "FixVersionConflictPanel.conflictL.text", new Object[] {conflictNode.getArtifact().getArtifact().getArtifactId(), getClashingVersionsAsText()})); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        jPanel2.add(conflictL, gridBagConstraints);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(summaryL)
                    .add(fixPossibL)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 494, Short.MAX_VALUE)
                            .add(jPanel2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 494, Short.MAX_VALUE)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, jSeparator1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 494, Short.MAX_VALUE)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, fixesP, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addContainerGap())))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jSeparator1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 14, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(fixPossibL)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(fixesP, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(summaryL)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void excludeCheckActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_excludeCheckActionPerformed
        excludeCheckChanged();
        updateSummary();
}//GEN-LAST:event_excludeCheckActionPerformed

    private void addSetCheckActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addSetCheckActionPerformed
        addSetCheckChanged();
        updateSummary();
    }//GEN-LAST:event_addSetCheckActionPerformed

    private void versionListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_versionListValueChanged
        updateSummary();
    }//GEN-LAST:event_versionListValueChanged


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox addSetCheck;
    private javax.swing.JPanel addSetP;
    private javax.swing.JLabel conflictL;
    private javax.swing.JCheckBox excludeCheck;
    private javax.swing.JPanel excludeP;
    private javax.swing.JList excludesList;
    private javax.swing.JLabel fixPossibL;
    private javax.swing.JPanel fixesP;
    private javax.swing.JLabel fromDirectL;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JLabel sumContent;
    private javax.swing.JLabel summaryL;
    private javax.swing.JLabel versionL;
    private javax.swing.JList versionList;
    // End of variables declaration//GEN-END:variables


    static class ExclusionTargets {

        // mapping; target artifact for exclusion -> set of versions of conflicting
        // artifact that target currently define by its dependencies
        Map<Artifact, Set<ArtifactVersion>> targets2Versions;

        // mapping; target artifact for exclusion -> related set of parents of conflicting
        // artifact in dependency graph
        Map<Artifact, Set<DependencyNode>> targets2ConfPar;

        ArtifactGraphNode conflictNode;
        ArtifactVersion usedVersion, newestVersion;

        public ExclusionTargets (ArtifactGraphNode conflictNode, ArtifactVersion newestVersion) {
            this.conflictNode = conflictNode;
            this.newestVersion = newestVersion;
            this.usedVersion = new DefaultArtifactVersion(
                conflictNode.getArtifact().getArtifact().getVersion());

            initialize ();
        }

        private void initialize () {
            targets2Versions = new HashMap<Artifact, Set<ArtifactVersion>>();
            targets2ConfPar = new HashMap<Artifact, Set<DependencyNode>>();
            DependencyNode curDn = null;
            DependencyNode parent = null;

            List<DependencyNode> allDNs = new ArrayList<DependencyNode>(
                    conflictNode.getDuplicatesOrConflicts());
            
            // prevent conflictNode itself to be included in exclusion targets
            if (conflictNode.getPrimaryLevel() > 1) {
                allDNs.add(conflictNode.getArtifact());
            }

            for (DependencyNode dn : allDNs) {
                curDn = dn;
                parent = curDn.getParent();
                // bad luck with no parent...
                if (parent == null) {
                    continue;
                }
                while (parent.getParent() != null) {
                    parent = parent.getParent();
                    curDn = curDn.getParent();
                }
                
                Set<DependencyNode> confPar = targets2ConfPar.get(curDn.getArtifact());
                if (confPar == null) {
                    confPar = new HashSet<DependencyNode>();
                    targets2ConfPar.put(curDn.getArtifact(), confPar);
                }
                confPar.add(dn.getParent());

                Set<ArtifactVersion> versions = targets2Versions.get(curDn.getArtifact());
                if (versions == null) {
                    versions = new HashSet<ArtifactVersion>();
                    targets2Versions.put(curDn.getArtifact(), versions);
                }
                versions.add(new DefaultArtifactVersion(dn.getArtifact().getVersion()));
            }
        }

        public Set<Artifact> getAll () {
            return targets2Versions.keySet();
        }

        /**
         * Find "good guys' between exclusion targets, which means they have non
         * conflicting dependency on newer version and doesn't contribute to conflict
         */
        public Set<Artifact> getNonConflicting () {
            Set<Artifact> result = new HashSet<Artifact>();
            for (Artifact art : getAll()) {
                if (isNonConflicting(art)) {
                    result.add(art);
                }
            }
            return result;
        }

        public Set<Artifact> getConflicting () {
            Set<Artifact> result = new HashSet<Artifact>();
            for (Artifact art : getAll()) {
                if (!isNonConflicting(art)) {
                    result.add(art);
                }
            }
            return result;
        }

        public boolean isNonConflicting (Artifact art) {
            Set<ArtifactVersion> versions = targets2Versions.get(art);
            if (versions != null && versions.size() == 1) {
                if (newestVersion.equals(versions.iterator().next())) {
                    return true;
                }
            }
            return false;
        }

        public Set<DependencyNode> getConflictParents (Artifact art) {
            return targets2ConfPar.get(art);
        }

    } // ExclusionTargets

    private static class ExclTargetEntry {
        Artifact artif;
        boolean isSelected = false;

        public ExclTargetEntry(Artifact artif, boolean isSelected) {
            this.artif = artif;
            this.isSelected = isSelected;
        }
    }

    private static class ExclTargetRenderer extends JCheckBox
            implements ListCellRenderer, MouseListener, KeyListener {

        private JList parentList;
        private FixVersionConflictPanel parentPanel;

        public ExclTargetRenderer (JList list, FixVersionConflictPanel parentPanel) {
            this.parentList = list;
            this.parentPanel = parentPanel;
        }

        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            ExclTargetEntry entry = (ExclTargetEntry)value;

            setText(entry.artif.getArtifactId());
            setSelected(entry.isSelected);
            setEnabled(list.isEnabled());
            setOpaque(isSelected && list.isEnabled());

            if (isSelected && list.isEnabled()) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
            } else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }
            return this;
        }

        public void mouseClicked(MouseEvent e) {
            int idx = parentList.locationToIndex(e.getPoint());
            if (idx == -1) {
                return;
            }
            Rectangle rect = parentList.getCellBounds(idx, idx);
            if (rect.contains(e.getPoint())) {
                doCheck();
            }
        }

        public void keyPressed(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                doCheck();
            }
        }

        private void doCheck() {
            int index = parentList.getSelectedIndex();
            if (index < 0) {
                return;
            }
            ExclTargetEntry ge = (ExclTargetEntry) parentList.getModel().getElementAt(index);
            ge.isSelected = !ge.isSelected;
            parentList.repaint();
            parentPanel.updateSummary();
        }

        public void mousePressed(MouseEvent e) {
        }

        public void mouseReleased(MouseEvent e) {
        }

        public void mouseEntered(MouseEvent e) {
        }

        public void mouseExited(MouseEvent e) {
        }

        public void keyTyped(KeyEvent e) {
        }

        public void keyReleased(KeyEvent e) {
        }

    } // ExclTargetRenderer

}
