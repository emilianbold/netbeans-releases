/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.quiz;

import java.awt.EventQueue;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.modules.ModuleInstall;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.SystemAction;

/**
 *
 * @author  Jindrich Sedek
 */
public class Installer extends ModuleInstall implements Runnable{
    private static final String LATEST_ANNOUNCEMENT = "anouncement";
    
    @Override
    public void restored() {
        Long announcement = prefs().getLong(LATEST_ANNOUNCEMENT, 0);
        Long now = new Date().getTime();
        if ((now - announcement) > 8 * 24 * 3600 * 1000){ // 8 days
            RequestProcessor.getDefault().post(this, 5000, Thread.MIN_PRIORITY);
            prefs().putLong(LATEST_ANNOUNCEMENT, now);
        }
    }

    public void run() {
        String url = NbBundle.getMessage(QuizComponentTopComponent.class, "NotifyPageURL") + QuizComponentTopComponent.findIdentity();
        try {
            Logger.getLogger(Installer.class.getName()).info("Checking notification");
            URLConnection connection = new URL(url).openConnection();
            connection.setDoOutput(false);
            connection.setDoInput(true);
            connection.setConnectTimeout(10000);
            connection.setUseCaches(false);
            Scanner scanner = new Scanner(connection.getInputStream());
            boolean notify = scanner.nextBoolean();
            scanner.close();
            if (notify){
                EventQueue.invokeLater(new Runnable(){

                    public void run() {
                        String message = NbBundle.getMessage(QuizAction.class, "NewQuizMessage");
                        showNotification(message);
                    }

                });
            }
        } catch (IOException ex) {
            Logger.getLogger(Installer.class.getName()).log(Level.INFO, "Network unreachable", ex);
            EventQueue.invokeLater(new Runnable() {

                public void run() {
                    String message = NbBundle.getMessage(QuizAction.class, "MaybeNewMessage");
                    showNotification(message);
                }
            });
        }
    }
    
    private void showNotification(String message) {
        Object[] buttons = {NotifyDescriptor.YES_OPTION, NotifyDescriptor.NO_OPTION};
        NotifyDescriptor newQuizDD = new NotifyDescriptor.Message(message, NotifyDescriptor.QUESTION_MESSAGE);
        newQuizDD.setOptions(buttons);
        Object notification = DialogDisplayer.getDefault().notify(newQuizDD);
        if (notification.equals(NotifyDescriptor.YES_OPTION)){
            QuizAction quizAction = SystemAction.get(QuizAction.class);
            quizAction.performAction();
        }
    }
                    
    private Preferences prefs() {
        return NbPreferences.forModule(Installer.class);
    }
}
