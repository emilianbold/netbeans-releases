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

package org.netbeans.core.ui.notifications;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.CharConversionException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import org.openide.awt.Notification;
import org.openide.awt.NotificationDisplayer;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;
import org.openide.xml.XMLUtil;

/**
 * Implementation of NotificationDisplayer which shows new Notifications as
 * balloon-like tooltips and the list of all Notifications in a popup window.
 *
 * @since 1.14
 * @author S. Aubrecht
 */
@ServiceProvider(service=NotificationDisplayer.class)
public final class NotificationDisplayerImpl extends NotificationDisplayer {

    static final String PROP_NOTIFICATION_ADDED = "notificationAdded"; //NOI18N
    static final String PROP_NOTIFICATION_REMOVED = "notificationRemoved"; //NOI18N

    private final List<NotificationImpl> model = new LinkedList<NotificationImpl>();
    private final PropertyChangeSupport propSupport = new PropertyChangeSupport(this);

    static NotificationDisplayerImpl getInstance() {
        return (NotificationDisplayerImpl) Lookup.getDefault().lookup(NotificationDisplayer.class);
    }

    @Override
    public Notification notify(String title, Icon icon, String detailsText, ActionListener detailsAction, Priority priority) {
        if( null == detailsText )
            throw new NullPointerException("detailsText cannot be null."); //NOI18N
        if( null == detailsAction )
            throw new NullPointerException("detailsAction cannot be null."); //NOI18N

        try {
            detailsText = XMLUtil.toElementContent(detailsText);
        } catch( CharConversionException ex ) {
            throw new IllegalArgumentException(ex);
        }
        JComponent detailsComp1 = createDetails( detailsText, detailsAction );
        JComponent detailsComp2 = createDetails( detailsText, detailsAction );

        return notify(title, icon, detailsComp1, detailsComp2, priority);
    }

    @Override
    public Notification notify(String title, Icon icon, JComponent balloonDetails, JComponent popupDetails, Priority priority) {
        NotificationImpl n = new NotificationImpl();

        return notify( n, title, icon, balloonDetails, popupDetails, priority );
    }

    private Notification notify( NotificationImpl n, String title, Icon icon, JComponent balloonDetails, JComponent popupDetails, Priority priority) {
        if( null == title )
            throw new NullPointerException("title cannot be null."); //NOI18N
        if( null == icon )
            throw new NullPointerException("icon cannot be null."); //NOI18N
        if( null == balloonDetails )
            throw new NullPointerException("balloonDetails cannot be null."); //NOI18N
        if( null == popupDetails )
            throw new NullPointerException("popupDetails cannot be null."); //NOI18N
        if( null == priority )
            throw new NullPointerException("priority cannot be null."); //NOI18N

        try {
            title = XMLUtil.toElementContent(title);
        } catch( CharConversionException ex ) {
            throw new IllegalArgumentException(ex);
        }
        JComponent titleComp = createTitle(title);
        JComponent balloon = createContent( icon, titleComp, balloonDetails, n );
        balloon.setBorder(BorderFactory.createEmptyBorder(8, 5, 0, 0));
        
        titleComp = createTitle(title);
        JComponent popup = createContent( icon, titleComp, popupDetails, n );
        
        n.init(title, icon, priority, balloon, popup);
        add( n );
        return n;
    }

    /**
     * Adds given Notification to the model, fires property change.
     * @param n
     */
    void add( NotificationImpl n ) {
        synchronized( model ) {
            model.add(n);
            Collections.sort(model);
        }
        firePropertyChange( PROP_NOTIFICATION_ADDED, n );
    }

    /**
     * Removes given Notification from the model, fires property change.
     * @param n
     */
    void remove( NotificationImpl n ) {
        synchronized( model ) {
            if( !model.contains(n) )
                return;
            model.remove(n);
        }
        firePropertyChange( PROP_NOTIFICATION_REMOVED, n );
    }

    /**
     * @return The count of active notifications.
     */
    int size() {
        synchronized( model ) {
            return model.size();
        }
    }

    /**
     * @return List of all notifications.
     */
    List<NotificationImpl> getNotifications() {
        List<NotificationImpl> res = null;
        synchronized( model ) {
            res = new ArrayList<NotificationImpl>(model);
        }
        return res;
    }

    /**
     * @return The most important notification.
     */
    NotificationImpl getTopNotification() {
        NotificationImpl res = null;
        synchronized( model ) {
            if( !model.isEmpty() )
                res = model.get(0);
        }
        return res;
    }

    void addPropertyChangeListener( PropertyChangeListener l ) {
        propSupport.addPropertyChangeListener(l);
    }

    void removePropertyChangeListener( PropertyChangeListener l ) {
        propSupport.removePropertyChangeListener(l);
    }

    private JComponent createContent(Icon icon, JComponent titleComp, JComponent popupDetails, final Notification n) {
        JPanel panel = new JPanel( new GridBagLayout() );
        panel.setOpaque(false);
        panel.add( new JLabel(icon), new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(3,3,3,3), 0, 0));
        panel.add( titleComp, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(3,3,3,3), 0, 0));
        panel.add( popupDetails, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(3,3,3,3), 0, 0));
        panel.add( new JLabel(), new GridBagConstraints(2, 2, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0,0,0,0), 0, 0));

        ActionListener al = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                n.clear();
                PopupList.dismiss();
            }
        };
        addActionListener( popupDetails, al );
        return panel;
    }

    private void addActionListener(Container c, ActionListener al) {
        if( c instanceof AbstractButton ) {
            ((AbstractButton)c).addActionListener(al);
        }
        for( Component child : c.getComponents() ) {
            if( child instanceof Container ) {
                addActionListener((Container)child, al);
            }
        }
    }

    private JComponent createTitle( String title ) {
        return new JLabel("<html>" + title); // NOI18N
    }

    private JComponent createDetails( String text, ActionListener action ) {
        text = "<html><u>" + text; //NOI18N
        JButton btn = new JButton(text);
        btn.setFocusable(false);
        btn.setBorder(BorderFactory.createEmptyBorder());
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.addActionListener(action);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setForeground(Color.blue);
        return btn;
    }

    private void firePropertyChange(final String propName, final NotificationImpl notification) {
        Runnable r = new Runnable() {
            public void run() {
                propSupport.firePropertyChange(propName, null, notification);
            }
        };
        if( SwingUtilities.isEventDispatchThread() ) {
            r.run();
        } else {
            SwingUtilities.invokeLater(r);
        }
    }
}
