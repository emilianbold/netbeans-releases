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
package org.netbeans.modules.collab.ui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import org.openide.awt.*;
import org.openide.util.*;
import org.openide.util.actions.SystemAction;
import org.openide.windows.*;

import com.sun.collablet.Conversation;
import org.netbeans.modules.collab.core.Debug;
import org.netbeans.modules.collab.ui.actions.*;
import org.netbeans.modules.collab.ui.options.NotificationSettings;

/**
 *
 * @author  todd
 */
public class NotificationBar extends JPanel implements NotificationListener, MouseListener, ActionListener {
    ////////////////////////////////////////////////////////////////////////////
    // Class fields
    ////////////////////////////////////////////////////////////////////////////
    private static final Image NORMAL_IMAGE = Utilities.loadImage(
            "org/netbeans/modules/collab/ui/resources/chat_png.gif"
        );
    private static final Image ALERT_IMAGE = Utilities.loadImage(
            "org/netbeans/modules/collab/ui/resources/conversation_notify_png.gif"
        );
    private static final Image CLOSE_IMAGE = Utilities.loadImage(
            "org/netbeans/modules/collab/ui/resources/xp-close-sel-normal.gif"
        );
    private static final Image CLOSE_ROLLOVER_IMAGE = Utilities.loadImage(
            "org/netbeans/modules/collab/ui/resources/xp-close-sel-rollover.gif"
        );
    private static final Icon NORMAL_ICON = (NORMAL_IMAGE != null) ? new ImageIcon(NORMAL_IMAGE) : new ImageIcon();
    private static final Icon ALERT_ICON = (ALERT_IMAGE != null) ? new ImageIcon(ALERT_IMAGE) : new ImageIcon();
    private static final Icon CLOSE_ICON = (CLOSE_IMAGE != null) ? new ImageIcon(CLOSE_IMAGE) : new ImageIcon();
    private static final Icon CLOSE_ROLLOVER_ICON = (CLOSE_ROLLOVER_IMAGE != null)
        ? new ImageIcon(CLOSE_ROLLOVER_IMAGE) : new ImageIcon();
    private static final Dimension PREFERRED_SIZE = new Dimension(200, 24);
    private static final Color XP_BG_COLOR = new Color(255, 255, 224);
    private static final int NUM_FRAMES = 5;
    private static NotificationBar instance;
    private static boolean installed;

    ////////////////////////////////////////////////////////////////////////////
    // Instance fields
    ////////////////////////////////////////////////////////////////////////////
    private JLabel label;
    private JLabel closeWidget;
    private int notifications;
    private Color flashBackground;
    private Color flashForeground;
    private Color foreground;
    private Color background;
    private javax.swing.Timer animationTimer;
    private int animatedHeight;
    private int animationFrameCount;
    private boolean animationDirection = true; // growing

    /**
     *
     *
     */
    protected NotificationBar() {
        super();
        initialize();
    }

    /**
     *
     *
     */
    private void initialize() {
        setLayout(new BorderLayout());
        setPreferredSize(PREFERRED_SIZE);
        setVisible(false);

        // Determine colors from L&F
        foreground = UIManager.getColor("Label.foreground"); // NOI18N
        background = DefaultUserInterface.isWindowsLF() ? XP_BG_COLOR : UIManager.getColor("TextField.background"); // NOI18N
        flashBackground = 
            //			UIManager.getColor("tab_highlight_header") : // NOI18N
            UIManager.getColor("TextField.selectionBackground"); // NOI18N
        flashForeground = DefaultUserInterface.isXPLF() ? Color.WHITE
                                                        : UIManager.getColor("TextField.selectionForeground"); // NOI18N

        setBackground(background);

        add(new JSeparator(), BorderLayout.NORTH);
        add(new JSeparator(), BorderLayout.SOUTH);
        add(Box.createHorizontalStrut(3), BorderLayout.WEST);

        label = new JLabel(ALERT_ICON, SwingConstants.LEFT);
        label.setForeground(foreground);
        label.setVerticalAlignment(SwingConstants.CENTER);
        label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        label.addMouseListener(this);
        add(label, BorderLayout.CENTER);

        JPanel closePanel = new JPanel(new BorderLayout());
        closePanel.setOpaque(false);
        closePanel.add(Box.createHorizontalStrut(5), BorderLayout.EAST);
        add(closePanel, BorderLayout.EAST);

        closeWidget = new JLabel(CLOSE_ICON);
        closeWidget.setToolTipText(NbBundle.getMessage(NotificationBar.class, "TT_NotificationBar_CloseWidget")); // NOI18N
        closeWidget.addMouseListener(this);
        closePanel.add(closeWidget, BorderLayout.CENTER);

        // Create the animation timer
        animationTimer = new javax.swing.Timer(40, this);
    }

    /**
     *
     *
     */
    protected String getText() {
        if (!SwingUtilities.isEventDispatchThread()) {
            // Create a holder
            final String[] RESULT = new String[1];

            try {
                SwingUtilities.invokeAndWait(
                    new Runnable() {
                        public void run() {
                            RESULT[0] = label.getText();
                        }
                    }
                );
            } catch (Exception e) {
                // Shouldn't happen
                Debug.debugNotify(e);
            }

            return RESULT[0];
        } else {
            return label.getText();
        }
    }

    /**
     *
     *
     */
    protected void setText(final String value) {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(
                new Runnable() {
                    public void run() {
                        setText(value);
                    }
                }
            );
        } else {
            label.setText(value);
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // NotificationListener methods
    ////////////////////////////////////////////////////////////////////////////

    /**
     *
     *
     */
    public void notificationStateChanged(boolean state) {
        if (!NotificationSettings.getDefault().getShowConversationNotificationBar().booleanValue()) {
            return;
        }

        if (state) {
            SwingUtilities.invokeLater(
                new Runnable() {
                    public void run() {
                        label.setIcon(ALERT_ICON);
                        updateText();
                    }
                }
            );
        } else {
            SwingUtilities.invokeLater(
                new Runnable() {
                    public void run() {
                        label.setIcon(NORMAL_ICON);
                        updateText();

                        setBackground(background);
                        label.setForeground(foreground);
                    }
                }
            );
        }
    }

    /**
     *
     *
     */
    public void notificationResumed() {
        if (!NotificationSettings.getDefault().getShowConversationNotificationBar().booleanValue()) {
            return;
        }

        SwingUtilities.invokeLater(
            new Runnable() {
                public void run() {
                    updateText();

                    // Shrink to zero height then make visible
                    if (useAnimation()) {
                        setPreferredSize(new Dimension(PREFERRED_SIZE.width, 0));
                        setMaximumSize(new Dimension(PREFERRED_SIZE.width, 0));
                        setVisible(true);

                        // Gradually show the notification bar
                        animationDirection = true;
                        resetAnimation(true);
                        animationTimer.restart();
                    } else {
                        setPreferredSize(PREFERRED_SIZE);
                        setMaximumSize(PREFERRED_SIZE);
                        setVisible(true);
                    }
                }
            }
        );
    }

    /**
     *
     *
     */
    public void notificationSuspended() {
        if (!NotificationSettings.getDefault().getShowConversationNotificationBar().booleanValue()) {
            return;
        }

        SwingUtilities.invokeLater(
            new Runnable() {
                public void run() {
                    if (useAnimation()) {
                        resetAnimation(false);
                        animationTimer.restart();
                    } else {
                        setVisible(false);
                        updateText();
                    }
                }
            }
        );
    }

    /**
     *
     *
     */
    protected void updateText() {
        String key = Actions.findKey(SystemAction.get(ShowNextNotificationAction.class));

        if ((key == null) || (key.trim().length() == 0)) {
            key = NbBundle.getMessage(NotificationBar.class, "MSG_NotificationBar_BlankShortcutKeyName"); // NOI18N
        }

        TopComponent[] components = NotificationRegistry.getDefault().getNotifyingComponents();

        if (components.length == 0) {
            setText(""); // NOI18N
        } else if (components.length == 1) {
            String conversationName = null;

            if (components[0] instanceof ConversationComponent) {
                Conversation conversation = ((ConversationComponent) components[0]).getConversation();
                conversationName = conversation.getDisplayName();
            }

            String labelText = null;

            if (conversationName != null) {
                labelText = NbBundle.getMessage(
                        NotificationBar.class, "MSG_NotificationBar_OneNamedNotification", // NOI18N
                        conversationName, key
                    );
            } else {
                labelText = NbBundle.getMessage(
                        NotificationBar.class, "MSG_NotificationBar_OneUnnamedNotification", // NOI18N
                        key
                    );
            }

            setText(labelText);
        } else {
            setText(NbBundle.getMessage(NotificationBar.class, "MSG_NotificationBar_MultipleNotifications", key)); // NOI18N
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // ActionListener methods
    ////////////////////////////////////////////////////////////////////////////

    /**
     *
     *
     */
    public void actionPerformed(ActionEvent event) {
        // Resize
        setPreferredSize(new Dimension(PREFERRED_SIZE.width, animatedHeight));
        setMaximumSize(new Dimension(PREFERRED_SIZE.width, animatedHeight));
        revalidate();

        // Modify the height
        final int HEIGHT_DELTA = PREFERRED_SIZE.height / NUM_FRAMES;

        if (animationDirection) {
            animatedHeight += HEIGHT_DELTA;
        } else {
            animatedHeight -= HEIGHT_DELTA;
        }

        if (animationFrameCount++ > NUM_FRAMES) {
            // Stop the timer and reset
            animationTimer.stop();

            // Settle on the final size
            if (animationDirection) {
                setPreferredSize(PREFERRED_SIZE);
                setMaximumSize(null);
            } else {
                setVisible(false);
            }

            revalidate();
        }
    }

    /**
     *
     *
     */
    private void resetAnimation(boolean grow) {
        animationDirection = grow;
        animatedHeight = grow ? 0 : PREFERRED_SIZE.height;
        animationFrameCount = 0;
    }

    /**
     *
     *
     */
    private boolean useAnimation() {
        Boolean result = NotificationSettings.getDefault().getAnimateConversationNotificationBar();

        return (result != null) ? result.booleanValue() : true;
    }

    ////////////////////////////////////////////////////////////////////////////
    // MouseListener methods
    ////////////////////////////////////////////////////////////////////////////

    /**
     *
     *
     */
    public void mouseClicked(MouseEvent event) {
        if (event.getSource() == closeWidget) {
            if ((event.getModifiersEx() & MouseEvent.SHIFT_DOWN_MASK) == MouseEvent.SHIFT_DOWN_MASK) {
                // Shift+Click disables the bar
                NotificationSettings.getDefault().setShowConversationNotificationBar(Boolean.FALSE);
            }

            // Close instantly
            setVisible(false);
        } else {
            if (event.getButton() == MouseEvent.BUTTON3) {
                // Show a pop-up menu of conversations to select
                JPopupMenu menu = new JPopupMenu();

                TopComponent[] components = NotificationRegistry.getDefault().getNotifyingComponents();

                for (int i = 0; i < components.length; i++) {
                    final TopComponent component = components[i];

                    if (component instanceof ConversationComponent) {
                        JMenuItem item = new JMenuItem(
                                (((ConversationComponent) components[i]).getConversation()).getDisplayName()
                            );
                        item.addActionListener(
                            new ActionListener() {
                                public void actionPerformed(ActionEvent event) {
                                    NotificationRegistry.getDefault().showComponent(component);
                                }
                            }
                        );
                        menu.add(item);
                    }
                }

                menu.show(this, event.getX(), event.getY());
            } else {
                NotificationRegistry.getDefault().showNextComponent();

                if (useAnimation()) {
                    // Workaround to hide the component immediately if there 
                    // are no more notifications to show; otherwise, there is a 
                    // pause before the component is hidden due to the 
                    // roundabout way in which notification listeners are 
                    // notified.
                    TopComponent[] components = NotificationRegistry.getDefault().getNotifyingComponents();

                    if (components.length == 0) {
                        setVisible(false);
                    }
                }
            }
        }
    }

    /**
     *
     *
     */
    public void mouseEntered(MouseEvent event) {
        if (event.getSource() == closeWidget) {
            closeWidget.setIcon(CLOSE_ROLLOVER_ICON);
        }
    }

    /**
     *
     *
     */
    public void mouseExited(MouseEvent event) {
        if (event.getSource() == closeWidget) {
            closeWidget.setIcon(CLOSE_ICON);
        }
    }

    /**
     *
     *
     */
    public void mousePressed(MouseEvent event) {
        // Do nothing
    }

    /**
     *
     *
     */
    public void mouseReleased(MouseEvent event) {
        // Do nothing
    }

    ////////////////////////////////////////////////////////////////////////////
    // Management methods
    ////////////////////////////////////////////////////////////////////////////

    /**
     *
     *
     */
    public static synchronized NotificationBar getDefault() {
        if (instance == null) {
            instance = new NotificationBar();
        }

        return instance;
    }

    /**
     *
     *
     */
    public static synchronized void install() {
        if (installed) {
            return;
        }

        SwingUtilities.invokeLater(
            new Runnable() {
                public void run() {
                    Container parent = WindowManager.getDefault().getMainWindow();
                    install(parent);
                }
            }
        );

        installed = true;
    }

    /**
     *
     *
     */
    private static boolean install(Container parent) {
        try {
            for (int i = 0; i < parent.getComponentCount(); i++) {
                Component component = parent.getComponent(i);

                final boolean ON_MENU_BAR = true;

                if (ON_MENU_BAR) {
                    // Insert the notification line right below the toolbar
                    if (component.getClass().getName().equals("org.openide.awt.ToolbarPool")) // NOI18N
                     {
                        // Note, by installing ourselves as the first child, we 
                        // take precedence over any component in the NORTH 
                        // position.  We specifically need to do this to avoid 
                        // some painting problems with the JSeparator that is 
                        // normally in this position.
                        Container statusLinePanel = (Container) component;
                        statusLinePanel.add(getDefault(), BorderLayout.SOUTH, 0);

                        // Listen to notifications
                        NotificationRegistry.getDefault().addNotificationListener(getDefault());

                        return true;
                    }
                } else {
                    // If we've found the status line component, insert 
                    // ourselves in its parent container
                    if (component.getClass().getName().equals("org.netbeans.core.windows.view.ui.StatusLine")) // NOI18N
                     {
                        // Note, by installing ourselves as the first child, we 
                        // take precedence over any component in the NORTH 
                        // position.  We specifically need to do this to avoid 
                        // some painting problems with the JSeparator that is 
                        // normally in this position.
                        Container statusLinePanel = component.getParent();
                        statusLinePanel.add(getDefault(), BorderLayout.NORTH, 0);

                        // Listen to notifications
                        NotificationRegistry.getDefault().addNotificationListener(getDefault());

                        return true;
                    }
                }

                // Recurse
                if (component instanceof Container) {
                    if (install((Container) component)) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            Debug.logDebugException("Could not install notification bar", e, true); // NOI18N
            Debug.debugNotify(e);
        }

        return false;
    }

    /**
     *
     *
     */
    public static synchronized void uninstall() {
        if (installed && (instance != null)) {
            final NotificationBar _component = instance;
            instance = null;
            installed = false;

            SwingUtilities.invokeLater(
                new Runnable() {
                    public void run() {
                        _component.getParent().remove(_component);
                        NotificationRegistry.getDefault().getNotificationThread().removeNotificationListener(
                            _component
                        );
                    }
                }
            );
        }
    }
}
