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
package org.netbeans.modules.uihandler;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Queue;
import java.util.logging.LogRecord;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.openide.awt.Notification;
import org.openide.awt.NotificationDisplayer;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 *
 * @author Jindrich Sedek
 */
class SlownessReporter {

    private final Queue<NotifySnapshot> pending;
    private static final String UI_ACTION_BUTTON_PRESS = "UI_ACTION_BUTTON_PRESS";  //NOI18N
    private static final String UI_ACTION_EDITOR = "UI_ACTION_EDITOR";  //NOI18N
    private static final String UI_ACTION_KEY_PRESS = "UI_ACTION_KEY_PRESS";    //NOI18N
    private static final String DELEGATE_PATTERN = "delegate=.*@";         // NOI18N
    static final long LATEST_ACTION_LIMIT = 1000;//ms
    private static final int CLEAR = Integer.getInteger("org.netbeans.modules.uihandler.SlownessReporter.clear", 60000); // NOI18N

    public SlownessReporter() {
        pending = new LinkedList<NotifySnapshot>();
    }

    private String getParam(LogRecord rec, int index) {
        if (rec.getParameters() != null && rec.getParameters().length > index) {
            return rec.getParameters()[index].toString();
        } else {
            assert rec.getParameters() != null;
            assert rec.getParameters().length > index : Integer.toString(rec.getParameters().length);
        }
        return null;
    }

    String getLatestAction(List<LogRecord> recs, long time) {
        long now = System.currentTimeMillis();
        ListIterator<LogRecord> it = recs.listIterator(recs.size());
        String latestActionClassName = null;
        while (it.hasPrevious()) {
            LogRecord rec = it.previous();
            if (Installer.IDE_STARTUP.equals(rec.getMessage())) {
                latestActionClassName = NbBundle.getMessage(SlownessReporter.class, "IDE_STARTUP");
            } else if (now - rec.getMillis() - time > LATEST_ACTION_LIMIT) {
                break;
            }
            if (UI_ACTION_EDITOR.equals(rec.getMessage()) ||
                    (UI_ACTION_BUTTON_PRESS.equals(rec.getMessage())) ||
                    (UI_ACTION_KEY_PRESS.equals(rec.getMessage()))) {
                latestActionClassName = getParam(rec, 4);
            }
            if (latestActionClassName != null) {
                latestActionClassName = latestActionClassName.replaceAll("&", ""); // NOI18N
                Pattern p = Pattern.compile(DELEGATE_PATTERN);
                Matcher m = p.matcher(latestActionClassName);
                if (m.find()) {
                    String delegate = m.group();
                    latestActionClassName = delegate.substring(9, delegate.length() - 1);
                }
                break;
            }
        }
        return latestActionClassName;
    }

    void notifySlowness(List<LogRecord> recs, byte[] nps, long time) {
        String latestActionName = getLatestAction(recs, time);
        pending.add(new NotifySnapshot(new SlownessData(time, nps, latestActionName)));
        if (pending.size() > 5) {
            pending.remove().clear();
        }
    }

    private static final class NotifySnapshot implements ActionListener, Runnable {

        private final Notification note;
        private final SlownessData data;

        NotifySnapshot(SlownessData data) {
            this.data = data;
            note = NotificationDisplayer.getDefault().notify(
                    NbBundle.getMessage(NotifySnapshot.class, "TEQ_LowPerformance"),
                    ImageUtilities.loadImageIcon("org/netbeans/modules/uihandler/vilik.png", true),
                    createPanel(), createPanel(),
                    NotificationDisplayer.Priority.LOW);
            if (CLEAR > 0) {
                Installer.RP.post(new Runnable() {

                    public void run() {
                        clear();
                    }
                }, CLEAR, Thread.MIN_PRIORITY);
            }
        }

        public void actionPerformed(ActionEvent e) {
            Installer.RP.post(NotifySnapshot.this);
        }

        public void clear() {
            note.clear();
        }

        private JComponent createPanel() {
            JPanel result = new JPanel();
            result.setOpaque(false);
            result.setLayout(new BoxLayout(result, BoxLayout.Y_AXIS));
            result.add(new JLabel(NbBundle.getMessage(NotifySnapshot.class, "TEQ_BlockedFor", data.getTime(), data.getTime() / 1000)));
            result.add(createDetails(NbBundle.getMessage(NotifySnapshot.class, "TEQ_Report")));
            return result;
        }

        private JButton createDetails(String text) {
            text = "<html><u>" + text; //NOI18N
            JButton btn = new JButton(text);
            btn.setFocusable(false);
            btn.setBorder(BorderFactory.createEmptyBorder());
            btn.setBorderPainted(false);
            btn.setFocusPainted(false);
            btn.setOpaque(false);
            btn.setContentAreaFilled(false);
            btn.addActionListener(this);
            btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            btn.setForeground(Color.blue);
            return btn;
        }

        public void run() {
            Installer.displaySummary("ERROR_URL", true, false, true, data); // NOI18N
        }
    }
}
