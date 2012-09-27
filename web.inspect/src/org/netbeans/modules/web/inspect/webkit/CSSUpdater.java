/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.inspect.webkit;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.css.live.LiveUpdater;
import org.netbeans.modules.web.common.api.ServerURLMapping;
import org.netbeans.modules.web.webkit.debugging.api.WebKitDebugging;
import org.netbeans.modules.web.webkit.debugging.api.css.StyleSheetHeader;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.ServiceProvider;

/**
 * Listens on parsing of CSS documents and propagates updates to associated
 * webkit. This class is singleton. Only one debugging session is allowed.
 *
 * @author Jan Becicka
 */
public class CSSUpdater {

    /**
     * Singleton instance.
     */
    private static CSSUpdater instance;
    
    /**
     * Current webkit session.
     */
    private WebKitDebugging webKit;
    
    /**
     * Mapping between url represented by string and StyleSheetHeader
     */
    private HashMap<String, StyleSheetHeader> sheetsMap = new HashMap<String, StyleSheetHeader>();

    private CSSUpdater() {
    }

    /**
     * Singleton instance.
     * @return 
     */
    static synchronized CSSUpdater getDefault() {
        if (instance == null) {
            instance = new CSSUpdater();
        }
        return instance;
    }

    /**
     * Start listening on CSS. Propagate changes to given webkit.
     * @param webKit 
     */
    synchronized void start(WebKitDebugging webKit) {
        assert webKit !=null : "webKit allready assigned";
        this.webKit = webKit;
        for (StyleSheetHeader header : webKit.getCSS().getAllStyleSheets()) {
            try {
                //need to convert file:///
                sheetsMap.put(new URL(header.getSourceURL()).toString(), header);
            } catch (MalformedURLException ex) {
                //ignore unknown sheets
            }
        }
    }

    /**
     * Stop listening on changes.
     */
    synchronized void stop() {
        this.webKit = null;
        sheetsMap.clear();
    }

    /**
     * @return true if listener is active. false otherwise. 
     */
    synchronized boolean isStarted() {
        return this.webKit != null;
    }

    /**
     * Updates css in browser using webKit.
     * @param snapshot 
     */
    synchronized void update(FileObject fileObject, String content) {
        assert webKit != null: "webKit not initialized";
        Project owner = FileOwnerQuery.getOwner(fileObject);
        if (owner == null) {
            return;
        }
        URL serverUrl = ServerURLMapping.toServer(owner, fileObject);
        if (serverUrl == null) {
            return;
        }
        StyleSheetHeader header = sheetsMap.get(serverUrl.toString());
        if (header != null) {
            webKit.getCSS().setStyleSheetText(header.getStyleSheetId(), content);
        }
    }

    @ServiceProvider(service = LiveUpdater.class)
    public static class LiveUpdaterImpl implements LiveUpdater {

        private RequestProcessor RP = new RequestProcessor(LiveUpdaterImpl.class);

        @Override
        public boolean update(final Document doc) {
            if (!CSSUpdater.getDefault().isStarted()) {
                return false;
            }
            RP.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        String text = doc.getText(0, doc.getLength());
                        CSSUpdater.getDefault().update(getDataObject(doc).getPrimaryFile(), text);
                    } catch (BadLocationException badLocationException) {
                        Exceptions.printStackTrace(badLocationException);
                    }
                }
            });
            return false;
        }
        
        private static DataObject getDataObject(Document doc) {
            Object sdp = doc == null ? null : doc.getProperty(Document.StreamDescriptionProperty);
            if (sdp instanceof DataObject) {
                return (DataObject) sdp;
            }
            return null;
        }
        
    }
}
