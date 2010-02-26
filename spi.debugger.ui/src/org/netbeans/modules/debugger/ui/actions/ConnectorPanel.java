/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.debugger.ui.actions;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.border.EmptyBorder;

import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.Properties;

import org.netbeans.spi.debugger.ui.AttachType;
import org.netbeans.spi.debugger.ui.Controller;
import org.openide.awt.Mnemonics;

import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;


public class ConnectorPanel extends JPanel implements ActionListener, HelpCtx.Provider {

    public static final String PROP_TYPE = "type";
    
    private static final String FIRST_ATTACH_TYPE = "org.netbeans.modules.debugger.jpda.ui.JPDAAttachType"; // NOI18N

    private static final int TOTAL_SLOTS = 4; // TOTAL_SLOTS - 1 is the maximal number of items that can appear in the attach history

    /** Contains list of AttachType names.*/
    private JComboBox             cbAttachTypes;
    /** Switches off listenning on cbAttachTypes.*/
    private boolean               doNotListen;
    /** Contains list of installed AttachTypes.*/
    private List                  attachTypes;
    /** Currentlydisplayed panel.*/
    private Controller            controller;
    /** Current attach type, which is stored into settings for the next invocation. */
    private AttachType            currentAttachType;
    /** Help for the dialog */
    private HelpCtx               help = HelpCtx.DEFAULT_HELP;


    public ConnectorPanel ()  {
        getAccessibleContext ().setAccessibleDescription (
            NbBundle.getMessage (ConnectorPanel.class, "ACSD_ConnectorPanel")
        );
        cbAttachTypes = new JComboBox ();
        cbAttachTypes.getAccessibleContext ().setAccessibleDescription (
            NbBundle.getMessage (ConnectorPanel.class, 
                "ACSD_CTL_Connect_through")// NOI18N
        ); 
        List types = DebuggerManager.getDebuggerManager ().lookup (
            null, AttachType.class
        );
        attachTypes = new ArrayList(types);
        for (Object t : types) {
            AttachType att = (AttachType) t;
            if (att.getTypeDisplayName() == null) {
                attachTypes.remove(t);
            }
        }
        String defaultAttachTypeName =
                Properties.getDefault ().getProperties ("debugger").getString ("last_attach_type", null);
        int defaultIndex = 0;
        int i, k = attachTypes.size ();
        Collections.sort(attachTypes, new Comparator() {
            public int compare(Object o1, Object o2) {
                if (!(o1 instanceof AttachType) || !(o2 instanceof AttachType)) return 0;
                if (FIRST_ATTACH_TYPE.equals(o1.getClass().getName())) {
                    return -1;
                }
                if (FIRST_ATTACH_TYPE.equals(o2.getClass().getName())) {
                    return +1;
                }
                return ((AttachType) o1).getTypeDisplayName().compareTo(((AttachType) o2).getTypeDisplayName());
            }
        });
        for (i = 0; i < k; i++) {
            AttachType at = (AttachType) attachTypes.get (i);
            String displayName = at.getTypeDisplayName();
            cbAttachTypes.addItem (displayName);
            if (displayName.equals(defaultAttachTypeName)) {
                defaultIndex = i;
            }
        }

        cbAttachTypes.setActionCommand ("SwitchMe!"); // NOI18N
        cbAttachTypes.addActionListener (this);

        setLayout (new GridBagLayout ());
        setBorder (new EmptyBorder (11, 11, 0, 10));
        refresh (defaultIndex);
    }
    
    private void refresh (int index) {
        JLabel cbLabel = new JLabel();
        Mnemonics.setLocalizedText(cbLabel,
                NbBundle.getMessage (ConnectorPanel.class, "CTL_Connect_through"));
        cbLabel.getAccessibleContext ().setAccessibleDescription (
            NbBundle.getMessage (ConnectorPanel.class, 
                "ACSD_CTL_Connect_through")// NOI18N
        ); 
        cbLabel.setLabelFor (cbAttachTypes);

        GridBagConstraints c = new GridBagConstraints ();
        c.insets = new Insets (0, 0, 6, 6);
        add (cbLabel, c);
        c = new GridBagConstraints ();
        c.weightx = 1.0;
        c.fill = java.awt.GridBagConstraints.HORIZONTAL;
        c.gridwidth = 0;
        c.insets = new Insets (0, 3, 6, 0);
        doNotListen = true;
        cbAttachTypes.setSelectedIndex (index);
        doNotListen = false;
        add (cbAttachTypes, c);
        c.insets = new Insets (0, 0, 6, 0);
        add (new JSeparator(), c);
        c = new GridBagConstraints ();
        c.weightx = 1.0;
        c.weighty = 1.0;
        c.fill = java.awt.GridBagConstraints.BOTH;
        c.gridwidth = 0;
        AttachType attachType = (AttachType) attachTypes.get (index);
        JComponent customizer = attachType.getCustomizer ();
        help = HelpCtx.findHelp (customizer);
        controller = attachType.getController();
        if (controller == null && (customizer instanceof Controller)) {
            Exceptions.printStackTrace(new IllegalStateException("FIXME: JComponent "+customizer+" must not implement Controller interface!"));
            controller = (Controller) customizer;
        }
        firePropertyChange(PROP_TYPE, null, customizer);
        this.currentAttachType = attachType;
        add (customizer, c);
    }


    /**
     * Called when a user selects debugger type in a combo-box.
     */
    public void actionPerformed (ActionEvent e) {
        if (doNotListen) return;
        if (e.getActionCommand ().equals ("SwitchMe!")); // NOI18N
        removeAll ();
        refresh (((JComboBox) e.getSource ()).getSelectedIndex ());
        Component w = getParent ();
        while (!(w instanceof Window))
            w = w.getParent ();
        if (w != null) ((Window) w).pack (); // ugly hack...
        return;
    }
    
    Controller getController() {
        return controller;
    }
    
    boolean cancel () {
        if (controller == null) return true;
        return controller.cancel ();
    }
    
    boolean ok () {
        String defaultAttachTypeName = currentAttachType.getTypeDisplayName();
        Properties props = Properties.getDefault().getProperties("debugger");
        props.setString("last_attach_type", defaultAttachTypeName);
        if (controller == null) return true;
        props = props.getProperties("last_attaches");
        Integer[] usedSlots = (Integer[]) props.getArray("used_slots", new Integer[0]);
        int freeSlot = -1;
        if (usedSlots.length >= TOTAL_SLOTS) {
            freeSlot = usedSlots[TOTAL_SLOTS - 1];
        } else {
            for (int x = 0; x < TOTAL_SLOTS; x++) {
                boolean found = true;
                for (int y = 0; y < usedSlots.length; y++) {
                    if (x == usedSlots[y]) {
                        found = false;
                        break;
                    }
                } // for
                if (found) {
                    freeSlot = x;
                    break;
                }
            } // for
        }
        Method saveMethod = null;
        Method displayNameMethod = null;
        try {
            saveMethod = controller.getClass().getMethod("save", Properties.class);
            displayNameMethod = controller.getClass().getMethod("getDisplayName");
        } catch (NoSuchMethodException ex) {
        } catch (SecurityException ex) {
        }
        String dispName = null;
        if (saveMethod != null && displayNameMethod != null) {
            Properties slot = props.getProperties("slot_" + freeSlot);
            try {
                dispName = (String) displayNameMethod.invoke(controller);
                if (dispName != null && dispName.trim().length() > 0) {
                    slot.setString("display_name", dispName);
                    saveMethod.invoke(controller, slot.getProperties("values"));
                    slot.setString("attach_type", defaultAttachTypeName);
                }
            } catch (IllegalAccessException ex) {
                Exceptions.printStackTrace(ex); // [TODO]
            } catch (IllegalArgumentException ex) {
                Exceptions.printStackTrace(ex); // [TODO]
            } catch (InvocationTargetException ex) {
                Exceptions.printStackTrace(ex); // [TODO]
            }
        }

        boolean ok = controller.ok ();

        if (ok) {
            GestureSubmitter.logAttach(defaultAttachTypeName);
            if (dispName != null && dispName.trim().length() > 0) {
                int newLength = Math.min(TOTAL_SLOTS - 1, usedSlots.length + 1);
                int excludeIndex = -1;
                // remove duplicities having the same display name
                for (int x = 0; x < usedSlots.length; x++) {
                    String str = props.getProperties("slot_" + usedSlots[x]).getString("display_name", "");
                    if (dispName.equals(str) && x < TOTAL_SLOTS - 1) {
                        excludeIndex = x;
                        newLength--;
                        break;
                    }
                }
                Integer[] newUsedSlots = new Integer[newLength];
                int copyFrom = 0;
                for (int x = 1; x < newLength; x++) {
                    if (copyFrom == excludeIndex) {
                        copyFrom++;
                    }
                    newUsedSlots[x] = usedSlots[copyFrom];
                    copyFrom++;
                } // for
                newUsedSlots[0] = freeSlot;
                props.setArray("used_slots", newUsedSlots);
            } // if
            DebugMainProjectAction.attachHistoryChanged();
        } // if
        return ok;
    }

    @Override
    public HelpCtx getHelpCtx() {
        return help;
    }
}



