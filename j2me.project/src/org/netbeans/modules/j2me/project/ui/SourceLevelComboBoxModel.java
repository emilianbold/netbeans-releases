/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.j2me.project.ui;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.modules.java.api.common.ui.PlatformUiSupport;
import org.openide.awt.HtmlRenderer;
import org.openide.modules.SpecificationVersion;
import org.openide.util.NbBundle;

/**
 *
 * @author Roman Svitanic
 */
public class SourceLevelComboBoxModel extends AbstractListModel implements ComboBoxModel, ListDataListener {

    private static final SpecificationVersion VERSION_8 = new SpecificationVersion("8.0"); //NOI18N
    private static final String VERSION_PREFIX = "1."; // the version prefix // NOI18N
    private static final int INITIAL_VERSION_MINOR = 3; // 1.3

    private final ComboBoxModel j2sePlatformModel;
    private final ComboBoxModel j2mePlatformModel;
    private String originalSourceLevel;
    private String selectedSourceLevel;
    private SourceLevel[] sourceLevelCache;

    @SuppressWarnings("LeakingThisInConstructor")
    public SourceLevelComboBoxModel(@NonNull ComboBoxModel j2sePlatform, @NonNull ComboBoxModel j2mePlatform, @NonNull String originalSourceLevel) {
        this.j2sePlatformModel = j2sePlatform;
        this.j2mePlatformModel = j2mePlatform;
        this.originalSourceLevel = originalSourceLevel;
        this.selectedSourceLevel = originalSourceLevel;

        this.j2sePlatformModel.addListDataListener(this);
        this.j2mePlatformModel.addListDataListener(this);
    }

    private SourceLevel[] getSourceLevels() {
        if (sourceLevelCache == null) {
            SpecificationVersion lower = getLowerSpecificationVersion();
            List<SourceLevel> sourceLevels = new ArrayList<>();
            if (lower != null) {
                int index = INITIAL_VERSION_MINOR;
                SpecificationVersion template
                        = new SpecificationVersion(VERSION_PREFIX + Integer.toString(index));
                while (template.compareTo(lower) <= 0) {
                    sourceLevels.add(new SourceLevel(VERSION_PREFIX + Integer.toString(index)));
                    template = new SpecificationVersion(VERSION_PREFIX + Integer.toString(++index));
                }
                if (originalSourceLevel != null) {
                    SpecificationVersion originalVersion = new SpecificationVersion(originalSourceLevel);
                    if (originalVersion.compareTo(lower) > 0) {
                        sourceLevels.add(new SourceLevel(originalSourceLevel, false));
                    }
                }
            }
            sourceLevelCache = sourceLevels.toArray(new SourceLevel[0]);
        }
        return sourceLevelCache;
    }

    private SpecificationVersion getLowerSpecificationVersion() {
        final JavaPlatform selectedJ2SE = PlatformUiSupport.getPlatform(j2sePlatformModel.getSelectedItem());
        final JavaPlatform selectedJ2ME = (JavaPlatform) j2mePlatformModel.getSelectedItem();
        SpecificationVersion specVersionJ2SE = null;
        SpecificationVersion specVersionJ2ME = null;
        if (selectedJ2SE != null) {
            specVersionJ2SE = selectedJ2SE.getSpecification().getVersion();
        }
        if (selectedJ2ME != null) {
            specVersionJ2ME = VERSION_8.compareTo(selectedJ2ME.getSpecification().getVersion()) <= 0
                    ? new SpecificationVersion("1.8") : //NOI18N
                    new SpecificationVersion("1.3"); //NOI18N
        }
        if (specVersionJ2SE != null && specVersionJ2ME != null) {
            SpecificationVersion lower = specVersionJ2SE.compareTo(specVersionJ2ME) <= 0 ? specVersionJ2SE : specVersionJ2ME;
            return lower;
        }
        return null;
    }

    private void resetCache() {
        synchronized (this) {
            sourceLevelCache = null;
        }
        fireContentsChanged(this, -1, -1);
    }

    @Override
    public int getSize() {
        return getSourceLevels().length;
    }

    @Override
    public Object getElementAt(int index) {
        SourceLevel[] sourceLevels = getSourceLevels();
        assert index >= 0 && index < sourceLevels.length;
        return sourceLevels[index];
    }

    @Override
    public void setSelectedItem(Object anItem) {
        SourceLevel level = (SourceLevel) anItem;
        selectedSourceLevel = level == null ? null : level.getSourceLevel();
        fireContentsChanged(this, -1, -1);
    }

    @Override
    public Object getSelectedItem() {
        for (SourceLevel level : getSourceLevels()) {
            if (level.getSourceLevel().equals(selectedSourceLevel)) {
                return level;
            }
        }
        return null;
    }

    @Override
    public void intervalAdded(ListDataEvent e) {
    }

    @Override
    public void intervalRemoved(ListDataEvent e) {
    }

    @Override
    public void contentsChanged(ListDataEvent e) {
        resetCache();
    }

    public static class SourceLevel {

        private String sourceLevel;
        private boolean valid;

        public SourceLevel(String sourceLevel) {
            this(sourceLevel, true);
        }

        public SourceLevel(String sourceLevel, boolean valid) {
            this.sourceLevel = sourceLevel;
            this.valid = valid;
        }

        public String getSourceLevel() {
            return sourceLevel;
        }

        public boolean isValid() {
            return valid;
        }

        public void setSourceLevel(String sourceLevel) {
            this.sourceLevel = sourceLevel;
        }

        public void setValid(boolean valid) {
            this.valid = valid;
        }

        public String getDisplayName() {
            Double levelValue = Double.valueOf(sourceLevel);
            if (levelValue < 1.5) {
                return "JDK " + sourceLevel; //NOI18N
            } else {
                return "JDK " + sourceLevel.substring(sourceLevel.lastIndexOf(".") + 1); //NOI18N
            }
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof SourceLevel) {
                return this.getSourceLevel().equals(((SourceLevel) obj).getSourceLevel());
            }
            return false;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 31 * hash + Objects.hashCode(this.sourceLevel);
            return hash;
        }

    }

    public static final class SourceLevelListCellRenderer implements ListCellRenderer {

        private final ListCellRenderer delegate;

        public SourceLevelListCellRenderer() {
            delegate = HtmlRenderer.createRenderer();
        }

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            String message = null;
            if (value == null || value.equals(" ")) { // NOI18N
                message = " ";   //NOI18N
            } else {
                assert value instanceof SourceLevel;
                SourceLevel sourceLevel = (SourceLevel) value;
                if (sourceLevel.isValid()) {
                    message = sourceLevel.getDisplayName();
                } else {
                    message = "<html><font color=\"#A40000\">" //NOI18N
                            + NbBundle.getMessage(
                                    PlatformUiSupport.class, "TXT_InvalidSourceLevel", sourceLevel.getDisplayName()); //NOI18N
                }
            }
            return delegate.getListCellRendererComponent(list, message, index, isSelected, cellHasFocus);
        }
    }
}
