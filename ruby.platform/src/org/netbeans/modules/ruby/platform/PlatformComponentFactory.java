/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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
package org.netbeans.modules.ruby.platform;

import java.awt.Color;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.text.Collator;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.plaf.UIResource;
import org.netbeans.api.ruby.platform.RubyPlatform;
import org.netbeans.api.ruby.platform.RubyPlatformManager;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

public final class PlatformComponentFactory {

    /** Generally usable in conjuction with {@link #createComboWaitModel}. */
    private static final String DETECTING_VALUE =
            NbBundle.getMessage(PlatformComponentFactory.class, "PlatformComponentFactory.detetctingPlatforms");
    
    private static final Logger LOGGER = Logger.getLogger(PlatformComponentFactory.class.getName());
    
    public static final Color INVALID_PLAF_COLOR = UIManager.getColor("nb.errorForeground"); // NOI18N

    private PlatformComponentFactory() {
        // don't allow instances
    }
    
    /**
     * Returns <code>JComboBox</code> initialized with {@link
     * RubyPlatformListModel} which contains all Ruby platform.
     */
    public static JComboBox getRubyPlatformsComboxBox() {
        final JComboBox plafComboBox = new JComboBox();
        plafComboBox.setRenderer(new RubyPlatformListRenderer());
        if (RubyPreferences.isFirstPlatformTouch()) {
            plafComboBox.setModel(createComboWaitModel());
            plafComboBox.setEnabled(false);
            RequestProcessor.getDefault().post(new Runnable() {
                public void run() {
                    RubyPlatformManager.performPlatformDetection();
                    EventQueue.invokeLater(new Runnable() {
                        public void run() {
                            plafComboBox.setModel(new RubyPlatformListModel());
                            plafComboBox.setEnabled(true);
                        }
                    });
                }
            });
        } else {
            plafComboBox.setModel(new RubyPlatformListModel());
        }
        return plafComboBox;
    }

    public static RubyPlatform getPlatform(final JComboBox platforms) {
        Object value = platforms.getModel().getSelectedItem();
        return (value == DETECTING_VALUE) ? null : (RubyPlatform) value;
    }

    public static boolean isLoadingPlatforms(JComboBox platforms) {
        Object value = platforms.getModel().getSelectedItem();
        return value == DETECTING_VALUE;
    }

    /**
     * Returns <code>JList</code> initialized with {@link RubyPlatformListModel}
     * which contains all Ruby platform.
     */
    public static JList getRubyPlatformsList() {
        JList plafList = new JList(new RubyPlatformListModel());
        plafList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        plafList.setCellRenderer(new RubyPlatformListRenderer());
        return plafList;
    }

    /**
     * Use this model in situation when you need to populate combo in the
     * background. The only item in this model is {@link #WAIT_VALUE}.
     */
    public static ComboBoxModel createComboWaitModel() {
        return new DefaultComboBoxModel(new Object[]{ DETECTING_VALUE });
    }

    public static void addPlatformChangeListener(final JComboBox platforms, final PlatformChangeListener pcl) {
        platforms.addItemListener(pcl);
        platforms.addPropertyChangeListener(pcl);
    }

    public static void removePlatformChangeListener(final JComboBox platforms, final PlatformChangeListener pcl) {
        platforms.removeItemListener(pcl);
        platforms.removePropertyChangeListener(pcl);
    }

    public static abstract class PlatformChangeListener implements ItemListener, PropertyChangeListener {
        
        public abstract void platformChanged();

        public void itemStateChanged(ItemEvent e) {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                platformChanged();
            }
        }

        public void propertyChange(PropertyChangeEvent evt) {
            // when the model has changed from "Detectin platform" to valid
            // platform model itemStateChanged is not fired(??)
            if (evt.getPropertyName().equals("model")) { // NOI18N
                platformChanged();
            }
        }
    }

    /**
     * Returns model containing all <em>currently</em> registered RubyPlatforms.
     * See also {@link RubyPlatform#getPlatforms}.
     * <p>Use in conjuction with {@link RubyPlatformListRenderer}</p>
     */
    public static class RubyPlatformListModel extends AbstractListModel
            implements ComboBoxModel {

        private static RubyPlatform[] getSortedPlatforms(RubyPlatform extra) {
            Set<RubyPlatform> _platforms = RubyPlatformManager.getPlatforms();
            if (extra != null) {
                _platforms.add(extra);
            }
            RubyPlatform[] platforms = _platforms.toArray(new RubyPlatform[_platforms.size()]);
            Arrays.sort(platforms, new Comparator<RubyPlatform>() {
                public int compare(RubyPlatform p1, RubyPlatform p2) {
                    int res = Collator.getInstance().compare(p1.getInfo().getLongDescription(), p2.getInfo().getLongDescription());
                    if (res != 0) {
                        return res;
                    } else {
                        return System.identityHashCode(p1) - System.identityHashCode(p2);
                    }
                }
            });
            return platforms;
        }
        private RubyPlatform[] nbPlafs;
        private Object selectedPlaf;

        public RubyPlatformListModel() {
            this(null);
        }

        public RubyPlatformListModel(final RubyPlatform initiallySelected) {
            nbPlafs = getSortedPlatforms(initiallySelected);
            if (initiallySelected == null) {
                if (nbPlafs.length > 0) {
                    selectedPlaf = nbPlafs[0];
                }
            } else {
                selectedPlaf = initiallySelected;
            }
        }

        public int getSize() {
            return nbPlafs.length;
        }

        public Object getElementAt(int index) {
            return index < nbPlafs.length ? nbPlafs[index] : null;
        }

        public void setSelectedItem(Object plaf) {
            assert plaf == null || plaf instanceof RubyPlatform;
            if (selectedPlaf != plaf) {
                selectedPlaf = plaf;
                fireContentsChanged(this, -1, -1);
            }
        }

        public Object getSelectedItem() {
            return selectedPlaf;
        }

        void removePlatform(RubyPlatform plaf) {
            try {
                RubyPlatformManager.removePlatform(plaf);
                nbPlafs = getSortedPlatforms(null); // refresh
                fireContentsChanged(this, 0, nbPlafs.length - 1);
            } catch (IOException e) {
                // tell the user that something goes wrong
                LOGGER.log(Level.WARNING, e.getLocalizedMessage(), e);
            }
        }

        RubyPlatform addPlatform(final File interpreter) {
            try {
                RubyPlatform platform = RubyPlatformManager.addPlatform(interpreter);
                if (platform != null) {
                    nbPlafs = getSortedPlatforms(null); // refresh
                    fireContentsChanged(this, 0, nbPlafs.length - 1);
                }
                return platform;
            } catch (IOException e) {
                // tell the user that something goes wrong
                LOGGER.log(Level.WARNING, e.getLocalizedMessage(), e);
            }
            return null;
        }
    }
    
    /**
     * Render {@link RubyPlatform}.
     * <p>Use in conjuction with {@link RubyPlatformListModel}</p>
     */
    private static class RubyPlatformListRenderer extends JLabel implements ListCellRenderer, UIResource {

        public RubyPlatformListRenderer() {
            setOpaque(true);
        }

        public Component getListCellRendererComponent(JList list, Object value,
                int index, boolean isSelected, boolean cellHasFocus) {
            // #93658: GTK needs name to render cell renderer "natively"
            setName("ComboBox.listRenderer"); // NOI18N

            if (isSelected) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
            } else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }

            String label;
            if (value instanceof String) {
                label = (String) value;
            } else {
                RubyPlatform plaf = ((RubyPlatform) value);
                label = plaf.getLabel();
                if (plaf != null && !plaf.isValid()) {
                    setForeground(INVALID_PLAF_COLOR);
                }
            }
            setText(label);

            return this;
        }

        // #93658: GTK needs name to render cell renderer "natively"
        public @Override String getName() {
            String name = super.getName();
            return name == null ? "ComboBox.renderer" : name;  // NOI18N
        }
    }

}
