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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.welcome;

import java.awt.Dialog;
import java.net.URL;
import java.util.Locale;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.HtmlBrowser;

/**
 *
 * @author Jaroslav Tulach
 */
public class FeedbackSurveyTest extends NbTestCase {
    
    public FeedbackSurveyTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        MockServices.setServices(DD.class, UD.class);
        Locale.setDefault(new Locale("te", "ST"));
        
        DD.nd = null;
        DD.toReturn = -1;
        UD.url = null;
    }
    
    public void testStartMultipleTimesBeforeTimeout() throws Exception {
        MemoryURL.registerURL("memory://survey", "ok");
        
        for (int i = 0; i < 10; i++) {

            FeedbackSurvey.start();
            assertNull("NO dialog", DD.nd);
            assertNull("NO url", UD.url);
        
        }
        
        Thread.sleep(3000);
        DD.toReturn = 0;
            
        FeedbackSurvey.start();
        assertNotNull("Time passed, dialog shown", DD.nd);
        assertNotNull("Time passed, url shown", UD.url);
    }
    
    public void testStartAfterTimeout() throws Exception {
        MemoryURL.registerURL("memory://survey", "ok");
        
        for (int i = 0; i < 5; i++) {

            FeedbackSurvey.start();
            assertNull("NO dialog", DD.nd);
            assertNull("NO url", UD.url);
        
        }
        
        Thread.sleep(3000);
        DD.toReturn = 0;

        for (int i = 0; i < 3; i++) {

            FeedbackSurvey.start();
            assertNull("No dialog" + i, DD.nd);
            assertNull("No url" + i, UD.url);
        
        }
            
        FeedbackSurvey.start();
        assertNotNull("Time passed, dialog shown", DD.nd);
        assertNotNull("Time passed, url shown", UD.url);
    }
    
    public void testJustThreeReminds() throws Exception {
        MemoryURL.registerURL("memory://survey", "ok");
        
        for (int i = 0; i < 10; i++) {

            FeedbackSurvey.start();
            assertNull("NO dialog", DD.nd);
            assertNull("NO url", UD.url);
        
        }
        
        Thread.sleep(3000);

        for (int i = 0; i < 3; i++) {
            DD.toReturn = 1;
            DD.nd = null;
            FeedbackSurvey.start();
            assertNotNull("Dialog shown" + i, DD.nd);
            assertNull("but no browser" + i, UD.url);
        
        }
            
        DD.toReturn = 0;
        DD.nd = null;
        FeedbackSurvey.start();
        assertNull("No dialogs, three times canceled", DD.nd);
        assertNull("No dialogs, three times canceled", UD.url);
    }
    
    public static final class DD extends DialogDisplayer {
        static NotifyDescriptor nd;
        static int toReturn = -1;
        
        public Object notify(NotifyDescriptor descriptor) {
            assertNull("No dialog yet", nd);
            nd = descriptor;
            
            
            Object r = descriptor.getOptions()[toReturn];
            toReturn = -1;
            
            return r;
        }

        public Dialog createDialog(DialogDescriptor descriptor) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
    }
    
    public static final class UD extends HtmlBrowser.URLDisplayer {
        static URL url;
        
        public void showURL(URL u) {
            assertNull("no url yet", url);
            url = u;
        }
        
    }
}
