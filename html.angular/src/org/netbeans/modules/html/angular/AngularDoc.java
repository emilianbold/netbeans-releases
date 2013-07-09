/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.html.angular;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.EnumMap;
import java.util.Enumeration;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.core.browser.api.EmbeddedBrowserFactory;
import org.netbeans.core.browser.api.WebBrowser;
import org.netbeans.modules.html.angular.model.Directive;
import org.openide.util.Enumerations;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author marekfukala
 */
@NbBundle.Messages({
    "doc.not.found=No documentation found",
    "doc.loading=Loading documentation in progress",
    "doc.building=Building AngularJS Documentation"
})
public class AngularDoc {

    private static final Logger LOG = Logger.getLogger(AngularDoc.class.getSimpleName()); //NOI18N
    private static RequestProcessor RP = new RequestProcessor(AngularDoc.class);
    private static AngularDoc INSTANCE;

    public static synchronized AngularDoc getDefault() {
        if (INSTANCE == null) {
            INSTANCE = new AngularDoc();
        }
        return INSTANCE;
    }
    private Map<Directive, String> directive2doc = new EnumMap<>(Directive.class);
    private boolean loadingFinished;

    public AngularDoc() {
        startLoading();
    }

    /**
     * Gets an html documentation for the given {@link Directive}.
     *
     * @param directive
     * @return the help or null if the help is not yet loaded
     */
    public String getDirectiveDocumentation(Directive directive) {
        String doc = directive2doc.get(directive);
        if (doc != null) {
            return doc;
        } else {
            return loadingFinished ? Bundle.doc_not_found() : Bundle.doc_loading();
        }
    }

    private void startLoading() {
        LOG.fine("startLoading"); //NOI18N
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                //init the browser in EDT
                browser = EmbeddedBrowserFactory.getDefault().createEmbeddedBrowser();
                LOG.fine("got browser instance"); //NOI18N
                try {
                    browser.getComponent();
                    LOG.fine("browser component obtained"); //NOI18N
                    Directive[] dirs = Directive.values();
                    directives = Enumerations.array(dirs);
                    progress = ProgressHandleFactory.createHandle(Bundle.doc_building());
                    progress.start(dirs.length);
                    browser.addPropertyChangeListener(BROWSER_PROP_LISTENER);

                    buildDoc();
                } catch (RuntimeException e) {
                    Exceptions.printStackTrace(e);
                    browser.dispose(); //dispose if anything went wrong
                    LOG.info("browser instance released due to runtime error"); //NOI18N
                }
            }
        });
    }

    private void buildDoc() {
        if (directives.hasMoreElements()) {
            directive = directives.nextElement();
            setURL();
        } else {
            //stop loading
            progress.finish();
            progress = null;
            
            //https://netbeans.org/bugzilla/show_bug.cgi?id=229689#c4
            //some tricks how to stop the exception from http://statistics.netbeans.org/exceptions/exception.do?id=679302 to pop up
            browser.stopLoading();
            browser.setContent("");
            
            browser.removePropertyChangeListener(BROWSER_PROP_LISTENER);
            browser.dispose();
            browser = null;
            
            loadingFinished = true;
            LOG.log(Level.FINE, "Loading doc finished."); //NOI18N
        }
    }

    private void setURL() {
        String docURL = directive.getExternalDocumentationURL();
        browser.setURL(docURL); //this will startl loading and subsequently fire property change to the BROWSER_PROP_LISTENER
        LOG.log(Level.FINE, "Set URL {0}", docURL); //NOI18N
    }

    private void urlLoaded() {
        //can I run it safely from the notifier thread?
        Object result = browser.executeJavaScript("document.getElementsByClassName('content').item(0).innerHTML"); //NOI18N

        if (result == null) {
            LOG.log(Level.INFO, "js execution result null"); //NOI18N
            return;

        }
        String content = result.toString();
        LOG.log(Level.FINE, "js execution result obtained, len={0}", content.length()); //NOI18N

        if (content.length() == 0) {
            if (++retry < 3) {
                LOG.log(Level.FINE, "empty result, retrying...", content.length()); //NOI18N
                browser.reloadDocument();
                return ;
            } else {
                LOG.log(Level.INFO, "empty result, given up after three attempts", content.length()); //NOI18N
                retry = 0;
            }
        } else {
            if (retry > 0) {
                retry = 0; //reset
            }
        }

        StringBuilder sb = new StringBuilder();
        sb.append("<html><head><title>AngularJS Doc</title></head><body>"); //NOI18N
        sb.append(content);
        sb.append("</body></html>"); //NOI18N

        directive2doc.put(directive, sb.toString());

        progress.progress(++loaded);

        //start next task
        RP.post(new Runnable() {
            @Override
            public void run() {
                buildDoc();
            }
        });
    }
    private WebBrowser browser;
    private Directive directive;
    private Enumeration<Directive> directives;
    private ProgressHandle progress;
    private int loaded = 0;
    private int retry = 0;
    private PropertyChangeListener BROWSER_PROP_LISTENER = new PropertyChangeListener() {
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            //loading finished
            if ("loading".equals(evt.getPropertyName()) //NOI18N
                    && Boolean.TRUE.equals(evt.getOldValue())
                    && Boolean.FALSE.equals(evt.getNewValue())) {
                LOG.log(Level.FINE, "URL {0} loaded", directive.getExternalDocumentationURL()); //NOI18N

                RP.post(new Runnable() {
                    @Override
                    public void run() {
                        urlLoaded();
                    }
                });

            }
        }
    };
}
