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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
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
package org.netbeans.modules.javacard.project;

import org.netbeans.modules.java.api.common.ui.PlatformUiSupport;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.HtmlRenderer;
import org.openide.modules.SpecificationVersion;
import org.openide.util.NbBundle;

import javax.swing.*;
import javax.swing.event.ListDataEvent;
import java.awt.*;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.modules.javacard.spi.JavacardPlatform;

/**
 * Copied with modifications from java.api.common
 *
 * @author Tomas Zezula
 */
final class SourceLevelComboBoxModel extends AbstractListModel implements ComboBoxModel {
    private static final SpecificationVersion JDK_1_5 = new SpecificationVersion("1.5"); //NOI18N
    private static final Logger LOGGER = Logger.getLogger(PlatformUiSupport.class.getName());
    private static final long serialVersionUID = 1L;
    private static final String VERSION_PREFIX = "1."; // the version prefix // NOI18N
    private static final int INITIAL_VERSION_MINOR = 2; // 1.2
    private final SpecificationVersion minimalSpecificationVersion;
    private SpecificationVersion selectedSourceLevel;
    private SpecificationVersion originalSourceLevel;
    private SourceLevelKey[] sourceLevelCache;
    private JavacardPlatform activePlatform;
    private final JCProjectProperties props;

    public SourceLevelComboBoxModel(JCProjectProperties props, String initialSourceLevel,
            String initialTargetLevel, SpecificationVersion minimalSpecificationVersion) {
        activePlatform = props.getPlatform();
        this.props = props;
        if (initialSourceLevel != null && initialSourceLevel.length() > 0) {
            try {
                originalSourceLevel = new SpecificationVersion(initialSourceLevel);
            } catch (NumberFormatException nfe) {
                // if the javac.source has invalid value, do not preselect and log it.
                LOGGER.warning("Invalid javac.source: " + initialSourceLevel); //NOI18N
            }
        }
        if (initialTargetLevel != null && initialTargetLevel.length() > 0) {
            try {
                SpecificationVersion originalTargetLevel = new SpecificationVersion(initialTargetLevel);
                if (originalSourceLevel == null || originalSourceLevel.compareTo(originalTargetLevel) < 0) {
                    originalSourceLevel = originalTargetLevel;
                }
            } catch (NumberFormatException nfe) {
                // if the javac.target has invalid value, do not preselect and log it
                LOGGER.warning("Invalid javac.target: " + initialTargetLevel); //NOI18N
            }
        }
        selectedSourceLevel = originalSourceLevel;
        this.minimalSpecificationVersion = minimalSpecificationVersion;
    }

    public int getSize() {
        SourceLevelKey[] sLevels = getSourceLevels();
        return sLevels.length;
    }

    public Object getElementAt(int index) {
        SourceLevelKey[] sLevels = getSourceLevels();
        assert index >= 0 && index < sLevels.length;
        return sLevels[index];
    }

    public Object getSelectedItem() {
        for (SourceLevelKey key : getSourceLevels()) {
            SpecificationVersion ver = key.getSourceLevel();
            if (ver.toString().equals(selectedSourceLevel == null ? "" :
                selectedSourceLevel.toString())) {
                return key;
            }
        }
        return null;
    }

    public void setSelectedItem(Object obj) {
        selectedSourceLevel = (obj == null ? null : ((SourceLevelKey) obj).getSourceLevel());
        fireContentsChanged(this, -1, -1);
        SourceLevelKey key = (SourceLevelKey) obj;
        if (key != null) {
            props.setJavacSourceLevel(key.getSourceLevel().toString());
            props.setJavacTargetLevel(key.getSourceLevel().toString());
        }
    }

    public void intervalAdded(ListDataEvent e) {
    }

    public void intervalRemoved(ListDataEvent e) {
    }
    private boolean inPlatformChanged = false;

    public void platformChanged() {
        if (inPlatformChanged) {
            return;
        }
        inPlatformChanged = true;
        try {
            JavacardPlatform platform = activePlatform;
            if (platform != null) {
                SpecificationVersion version = platform.getSpecification().getVersion();
                if (selectedSourceLevel != null && selectedSourceLevel.compareTo(version) > 0 && !shouldChangePlatform(selectedSourceLevel, version)) {
                    props.setPlatformName(platform.getSystemName());
                    // restore original
                    return;
                } else {
                    originalSourceLevel = null;
                }
            }
            activePlatform = props.getPlatform();
            resetCache();
        } finally {
            inPlatformChanged = false;
        }
    }

    private void resetCache() {
        synchronized (this) {
            sourceLevelCache = null;
        }
        fireContentsChanged(this, -1, -1);
    }

    private SourceLevelKey[] getSourceLevels() {
        if (sourceLevelCache == null) {
            JavacardPlatform platform = props.getPlatform();
            List<SourceLevelKey> sLevels = new ArrayList<SourceLevelKey>();
            // if platform == null => broken platform, the source level range is unknown
            // the source level combo box should be empty and disabled
            boolean selSourceLevelValid = false;
            SpecificationVersion version = platform == null ? 
                new SpecificationVersion("9.9") : //NOI18N
                platform.getSpecification().getVersion();
            Pattern p = Pattern.compile ("\\d*.\\.(\\d).*?"); //NOI18N
            int maxVersion = 6;
            Matcher m = p.matcher (version.toString());
            if (m.find() && m.groupCount() > 0) {
                maxVersion = Integer.parseInt(m.group(1));
            }

            int min = getMinimalIndex(version);
            for (int i = min; i < maxVersion + 1; i++) {
                SpecificationVersion ver = new SpecificationVersion("1." + i); //NOI18N
                sLevels.add(new SourceLevelKey(ver));
                selSourceLevelValid |= ver.equals(selectedSourceLevel);
            }
            sourceLevelCache = sLevels.toArray(new SourceLevelKey[sLevels.size()]);
            if (!selSourceLevelValid) {
                selectedSourceLevel = sourceLevelCache.length == 0
                        ? null : sourceLevelCache[sourceLevelCache.length - 1].getSourceLevel();
            }
        }
        return sourceLevelCache;
    }

    private int getMinimalIndex(SpecificationVersion platformVersion) {
        int index = INITIAL_VERSION_MINOR;
        if (minimalSpecificationVersion != null) {
            SpecificationVersion min = new SpecificationVersion(
                    VERSION_PREFIX + Integer.toString(index));
            while (min.compareTo(platformVersion) <= 0) {
                if (min.equals(minimalSpecificationVersion)) {
                    return index;
                }
                min = new SpecificationVersion(
                        VERSION_PREFIX + Integer.toString(++index));
            }
        }
        return index;
    }

    private boolean shouldChangePlatform(SpecificationVersion selectedSourceLevel,
            SpecificationVersion platformSourceLevel) {
        JButton changeOption = new JButton(NbBundle.getMessage(PlatformUiSupport.class,
                "CTL_ChangePlatform")); //NOI18N
        changeOption.getAccessibleContext().setAccessibleDescription(
                NbBundle.getMessage(PlatformUiSupport.class, "AD_ChangePlatform")); //NOI18N
        String message = MessageFormat.format(
                NbBundle.getMessage(PlatformUiSupport.class, "TXT_ChangePlatform"), //NOI18N
                selectedSourceLevel.toString(), platformSourceLevel.toString());
        return DialogDisplayer.getDefault().notify(new NotifyDescriptor(
                message,
                NbBundle.getMessage(PlatformUiSupport.class, "TXT_ChangePlatformTitle"), //NOI18N
                NotifyDescriptor.DEFAULT_OPTION,
                NotifyDescriptor.WARNING_MESSAGE,
                new Object[]{
                    changeOption,
                    NotifyDescriptor.CANCEL_OPTION
                },
                changeOption)) == changeOption;
    }

    static final class SourceLevelListCellRenderer implements ListCellRenderer {

        private ListCellRenderer delegate;

        public SourceLevelListCellRenderer() {
            delegate = HtmlRenderer.createRenderer();
        }

        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
                boolean cellHasFocus) {
            String message;
            if (value == null) {
                message = "";   //NOI18N
            } else {
                assert value instanceof SourceLevelKey;
                SourceLevelKey key = (SourceLevelKey) value;
                if (key.isBroken()) {
                    message = "<html><font color=\"#A40000\">" //NOI18N
                            + NbBundle.getMessage(
                            PlatformUiSupport.class, "TXT_InvalidSourceLevel",  //NOI18N
                            key.getDisplayName());
                } else {
                    message = key.getDisplayName();
                }
            }
            return delegate.getListCellRendererComponent(list, message, index,
                    isSelected, cellHasFocus);
        }
    }

    private static final class SourceLevelKey implements Comparable<SourceLevelKey> {

        private final SpecificationVersion sourceLevel;
        private final boolean broken;

        public SourceLevelKey(final SpecificationVersion sourceLevel) {
            this(sourceLevel, false);
        }

        public SourceLevelKey(final SpecificationVersion sourceLevel, final boolean broken) {
            assert sourceLevel != null : "Source level cannot be null"; //NOI18N
            this.sourceLevel = sourceLevel;
            this.broken = broken;
        }

        public SpecificationVersion getSourceLevel() {
            return this.sourceLevel;
        }

        public boolean isBroken() {
            return this.broken;
        }

        public int compareTo(final SourceLevelKey other) {
            SourceLevelKey otherKey = other;
            return this.sourceLevel.compareTo(otherKey.sourceLevel);
        }

        @Override
        public boolean equals(final Object other) {
            return (other instanceof SourceLevelKey) && ((SourceLevelKey) other).sourceLevel.equals(this.sourceLevel);
        }

        @Override
        public int hashCode() {
            return this.sourceLevel.hashCode();
        }

        @Override
        public String toString() {
            StringBuilder buffer = new StringBuilder();
            if (this.broken) {
                buffer.append("Broken: "); //NOI18N
            }
            buffer.append(this.sourceLevel.toString());
            return buffer.toString();
        }

        public String getDisplayName() {
            String tmp = sourceLevel.toString();
            if (JDK_1_5.compareTo(sourceLevel) <= 0) {
                tmp = tmp.replaceFirst("^1\\.([5-9]|\\d\\d+)$", "$1"); //NOI18N
            }
            return NbBundle.getMessage(PlatformUiSupport.class, "LBL_JDK", tmp); //NOI18N
        }
    }
}

