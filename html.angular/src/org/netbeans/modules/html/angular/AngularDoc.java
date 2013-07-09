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

import java.io.IOException;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.EnumMap;
import java.util.Enumeration;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.html.angular.model.Directive;
import org.openide.util.Enumerations;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author marekfukala
 */
@NbBundle.Messages({
    "doc.not.found=No documentation found",
    "doc.loading=Loading documentation in progress",
    "doc.building=Loading AngularJS Documentation"
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
        LOG.fine("start loading doc"); //NOI18N
        Directive[] dirs = Directive.values();
        directives = Enumerations.array(dirs);
        progress = ProgressHandleFactory.createHandle(Bundle.doc_building());
        progress.start(dirs.length);

        buildDoc();
    }

    private void buildDoc() {
        if (directives.hasMoreElements()) {
            directive = directives.nextElement();
            try {
                String docURL = directive.getExternalDocumentationURL_partial();

                URL url = new URI(docURL).toURL();
                StringWriter writer = new StringWriter();
                Utils.loadURL(url, writer, null);

                LOG.log(Level.FINE, "Loaded content of URL ", docURL); //NOI18N

                directive2doc.put(directive, writer.getBuffer().toString());
            } catch (URISyntaxException | IOException ex) {
                LOG.log(Level.INFO, String.format("Can't load doc from %s", directive.getExternalDocumentationURL_partial()), ex); //NOI18N
            }

            progress.progress(++loaded);

            //start next task
            RP.post(new Runnable() {
                @Override
                public void run() {
                    buildDoc();
                }
            });
        } else {
            //stop loading
            progress.finish();
            progress = null;

            loadingFinished = true;
            LOG.log(Level.FINE, "Loading doc finished."); //NOI18N
        }
    }
    private Directive directive;
    private Enumeration<Directive> directives;
    private ProgressHandle progress;
    private int loaded = 0;
}
