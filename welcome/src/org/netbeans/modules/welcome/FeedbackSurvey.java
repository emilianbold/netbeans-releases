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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.welcome;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.JButton;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.HtmlBrowser;
import org.openide.awt.Mnemonics;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

/** Shows feedback survey dialog and sends the user to predefined page.
 *
 * @author Jaroslav Tulach
 */
final class FeedbackSurvey {
    private static final Logger LOG = Logger.getLogger(FeedbackSurvey.class.getName());
    
    private FeedbackSurvey() {
    }
    
    public static void start() {
        final Preferences p = NbPreferences.root().node("/org/netbeans/modules/autoupdate"); // NOI18N
        String id = p.get ("ideIdentity", "unknown"); // NOI18N
        long mem = -1L;
        try {
            OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
            // w/o dependency on Sun's JDK
            // long freeMem = ((com.sun.management.OperatingSystemMXBean)osBean).getTotalPhysicalMemorySize();
            Method m = osBean.getClass().getMethod("getTotalPhysicalMemorySize");
            m.setAccessible(true);
            mem = (Long)m.invoke(osBean);
        }
        catch (IllegalAccessException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IllegalArgumentException ex) {
            Exceptions.printStackTrace(ex);
        } catch (InvocationTargetException ex) {
            Exceptions.printStackTrace(ex);
        } catch (NoSuchMethodException ex) {
            Exceptions.printStackTrace(ex);
        } catch (SecurityException ex) {
            Exceptions.printStackTrace(ex);
        } catch (NoSuchMethodError ex) {
            Exceptions.printStackTrace(ex);
        }
        String nbBuildNumber = System.getProperty("netbeans.buildnumber"); //NOI18N
        if( null == nbBuildNumber )
            nbBuildNumber=""; //NOI18N
        String url = NbBundle.getMessage(FeedbackSurvey.class, "MSG_FeedbackSurvey_URL", id, mem, nbBuildNumber);
        if (url.length() == 0) {
            return;
        }
        try {
            Preferences prefs = NbPreferences.forModule(FeedbackSurvey.class);
            
            long time = prefs.getLong("feedback.survey.show.at", 0); // NOI18N
            if (time == 0) {
                time = System.currentTimeMillis() + bundledInt("MSG_FeedbackSurvey_Delay"); // NOI18N
                prefs.putLong("feedback.survey.show.at", time); // NOI18N
            }
            
            int invocations = prefs.getInt("feedback.survey.startups", 0); // NOI18N
            if (invocations < bundledInt("MSG_FeedbackSurvey_MinimalStartups")) { // NOI18N
                prefs.putInt("feedback.survey.startups", invocations + 1); // NOI18N
                return;
            }
            
            if (System.currentTimeMillis() < time) {
                LOG.log(Level.FINE, "Not enough time passed");
                return;
            }

            int counts = prefs.getInt("feedback.survey.show.count", 0); // NOI18N
            if (counts >= bundledInt("MSG_FeedbackSurvey_AskTimes")) { // NOI18N
                return;
            }
            
            URL u = new URL(url);
            URLConnection conn = u.openConnection();
            String type = conn.getContentType();
            if (type == null || !type.startsWith("text/html")) { // NOI18N
                LOG.log(Level.INFO, "Wrong mimetype - {0} - skipping survey", conn.getContentType()); // NOI18N
                return;
            }
            
            prefs.putInt("feedback.survey.show.count", counts + 1); // NOI18N
            
            if (showDialog(u)) {
                // ok
                prefs.putLong("feedback.survey.show.at", Long.MAX_VALUE); // NOI18N
            } else {
                prefs.putLong("feedback.survey.show.at", System.currentTimeMillis() + bundledInt("MSG_FeedbackSurveyAgain")); // NOI18N
            }
        } catch (IOException ex) {
            LOG.log(Level.INFO, "Cannot connect to {0}, skipping feedback survey", url); // NOI18N
            LOG.log(Level.FINE, ex.getMessage(), ex);
        }
    }
    
    private static long bundledInt(String msg) {
        return Long.parseLong(NbBundle.getMessage(FeedbackSurvey.class, msg));
    }
    
    private static boolean showDialog(URL whereTo) {
        String msg = NbBundle.getMessage(FeedbackSurvey.class, "MSG_FeedbackSurvey_Message");
        String tit = NbBundle.getMessage(FeedbackSurvey.class, "MSG_FeedbackSurvey_Title");
        String yes = NbBundle.getMessage(FeedbackSurvey.class, "MSG_FeedbackSurvey_Yes");
        String later = NbBundle.getMessage(FeedbackSurvey.class, "MSG_FeedbackSurvey_Later");
        String never = NbBundle.getMessage(FeedbackSurvey.class, "MSG_FeedbackSurvey_Never");
        
        NotifyDescriptor nd = new NotifyDescriptor.Message(msg, NotifyDescriptor.QUESTION_MESSAGE);
        nd.setTitle(tit);
        //Object[] buttons = { yes, later, never };
        JButton yesButton = new JButton();
        yesButton.getAccessibleContext().setAccessibleDescription( 
                NbBundle.getMessage(FeedbackSurvey.class, "ACSD_FeedbackSurvey_Yes"));
        Mnemonics.setLocalizedText(yesButton, yes);
        JButton laterButton = new JButton();
        laterButton.getAccessibleContext().setAccessibleDescription( 
                NbBundle.getMessage(FeedbackSurvey.class, "ACSD_FeedbackSurvey_Later"));
        Mnemonics.setLocalizedText(laterButton, later);
        JButton neverButton = new JButton();
        neverButton.getAccessibleContext().setAccessibleDescription( 
                NbBundle.getMessage(FeedbackSurvey.class, "ACSD_FeedbackSurvey_Never"));
        Mnemonics.setLocalizedText(neverButton, never);
        Object[] buttons = { yesButton, laterButton, neverButton };
        nd.setOptions(buttons);
        Object res = DialogDisplayer.getDefault().notify(nd);
        
        if (res == yesButton) {
            HtmlBrowser.URLDisplayer.getDefault().showURL(whereTo);
            return true;
        } else {
            if( res == neverButton ) {
                Preferences prefs = NbPreferences.forModule(FeedbackSurvey.class);
                prefs.putInt("feedback.survey.show.count", (int)bundledInt("MSG_FeedbackSurvey_AskTimes")); // NOI18N
            }
            return false;
        }
    }
}
