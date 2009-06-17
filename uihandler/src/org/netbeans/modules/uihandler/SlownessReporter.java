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
import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.Queue;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.openide.awt.Notification;
import org.openide.awt.NotificationDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.ContextAwareAction;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;


/**
 *
 * @author Jindrich Sedek
 */
class SlownessReporter {
    private final Queue<NotifySnapshot> pending;

    public SlownessReporter() {
        pending = new LinkedList<NotifySnapshot>();
    }

    void notifySlowness(byte[] nps, long time){
        pending.add(new NotifySnapshot(new SlownessData(time, nps)));
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
        }

        public void actionPerformed(ActionEvent e) {
            try {
                FileObject fo = FileUtil.createMemoryFileSystem().getRoot().createData("slow.nps");
                OutputStream os = fo.getOutputStream();
                os.write(data.getNpsContent());
                os.close();
                final Node obj = DataObject.find(fo).getNodeDelegate();
                Action a = obj.getPreferredAction();
                if (a instanceof ContextAwareAction) {
                    a = ((ContextAwareAction)a).createContextAwareInstance(Lookups.singleton(obj));
                }
                a.actionPerformed(e);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        public void clear() {
            note.clear();
        }

        private JComponent createPanel(){
            JPanel result = new JPanel();
            result.setOpaque(false);
            result.setLayout(new BoxLayout(result, BoxLayout.Y_AXIS));
            result.add(createDetails(NbBundle.getMessage(NotifySnapshot.class, "TEQ_BlockedFor", data.getTime(), data.getTime() / 1000), this));
            result.add(createDetails(NbBundle.getMessage(NotifySnapshot.class, "TEQ_Report"), new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    Installer.RP.post(NotifySnapshot.this);
                }
            }));
            return result;
        }

        private JButton createDetails(String text, ActionListener action ) {
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

        public void run() {
            Installer.displaySummary("ERROR_URL", true, false, true, data); // NOI18N
        }
    }

}
