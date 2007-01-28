/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.visualweb.insync.live;

import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.*;

import org.openide.util.NbBundle;

import com.sun.rave.designtime.*;

public class EventEditPanel extends JPanel {

    /**
     *
     */
    private static final long serialVersionUID = 3256728376969606451L;
    protected EventPropertyEditor epe;
    protected DesignEvent liveEvent;

    protected JLabel label = new JLabel();
    protected JComboBox selectionList = new JComboBox();
    protected GridBagLayout gridBagLayout1 = new GridBagLayout();

    public EventEditPanel(EventPropertyEditor epe, DesignEvent le) {
        this.epe = epe;
        this.liveEvent = le;
        try {
            jbInit();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        // find the current value
        String current = le.getHandlerName();

        String[] handlers = epe.getMethods();
        selectionList.addItem("");  // 0th item is always blank/null/unset
        int sel = 0;   // blank gets selection by default
        for (int i = 0; i < handlers.length; i++) {
            selectionList.addItem(handlers[i]);
            if (handlers[i].equals(current))
                sel = i + 1;  // off by one for first blank
        }

        if (sel == 0) {
            // add the current item if it is set but the method is not in the list
            if (current != null)
                selectionList.addItem(current);
            // provide default name if no handler method currently exists (even if unbound current)
            selectionList.addItem(liveEvent.getDefaultHandlerName());
        }
        selectionList.setSelectedIndex(sel);

        initializing = false;
    }

    protected boolean initializing = true;

    public void setHandler(String handler) {
        selectionList.setSelectedItem(handler);
        if (initializing)
            return;
        firePropertyChange(null, null, null);
        if (epe != null)
            epe.setValue(handler);
    }

    public String getHandler() {
        Object o = selectionList.getSelectedItem();
        return o != null ? o.toString() : null;
    }

    private void jbInit() throws Exception {
        this.setLayout(gridBagLayout1);
        label.setText(NbBundle.getMessage(BeansDesignEvent.class, "Handler"));   //NOI18N
        selectionList.setEditable(true);
        this.add(label, new GridBagConstraints(0, 0, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER,
                GridBagConstraints.HORIZONTAL, new Insets(8, 8, 2, 8), 0, 0));
        this.add(selectionList, new GridBagConstraints(0, 1, 1, 1, 1.0, 0.0,
                GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 8, 4, 8), 0, 0));
        label.setLabelFor(selectionList);
        selectionList.getAccessibleContext().setAccessibleName(
                NbBundle.getMessage(BeansDesignEvent.class, "selectionListAccessibleName"));
        selectionList.getAccessibleContext().setAccessibleDescription(
                NbBundle.getMessage(BeansDesignEvent.class, "selectionListAccessibleDescription"));
        selectionList.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if (!initializing && epe != null && e.getStateChange() == ItemEvent.SELECTED) {
                    firePropertyChange(null, null, null);
                    Object item = selectionList.getSelectedItem();
                    epe.setValue(item);
                }
            }
        });
    }
}
