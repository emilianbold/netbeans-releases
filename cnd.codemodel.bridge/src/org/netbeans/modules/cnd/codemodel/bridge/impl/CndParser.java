/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.codemodel.bridge.impl;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeListener;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.editor.mimelookup.MimeRegistrations;
import org.netbeans.modules.cnd.api.codemodel.CMIndex;
import org.netbeans.modules.cnd.api.codemodel.CMTranslationUnit;
import org.netbeans.modules.cnd.api.codemodel.query.CMQuery;
import org.netbeans.modules.cnd.api.codemodel.query.CMUtilities;
import org.netbeans.modules.cnd.api.codemodel.query.CndParserResult;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.netbeans.modules.cnd.spi.codemodel.providers.CMCompilationDataBase;
import org.netbeans.modules.cnd.spi.codemodel.providers.CMUnsavedFileImplementation;
import org.netbeans.modules.cnd.spi.codemodel.support.SPIUtilities;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.Task;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.parsing.spi.ParserFactory;
import org.netbeans.modules.parsing.spi.SourceModificationEvent;

/**
 *
 * @author vv159170
 */
public class CndParser extends Parser {
    public final static Logger LOG = Logger.getLogger("cnd.codemodel.highlight");
    private final Collection<CMTranslationUnit> tus = new ArrayList<>();
    private Snapshot snapshot;

    private CndParser(Collection<Snapshot> snapshots) {
    }

    @Override
    public void parse(Snapshot snapshot, Task task, SourceModificationEvent event) throws ParseException {
        LOG.log(Level.INFO, "parse called for {0}", snapshot);
        if (snapshot == null) {
            return;
        }
        
        this.snapshot = snapshot;
        
        URI uri = snapshot.getSource().getFileObject().toURI();
        Collection<CMUnsavedFileImplementation> unsaved = Arrays.<CMUnsavedFileImplementation>asList(new UnsavedFileImpl(uri, snapshot.getText()));
        
        Collection<CMTranslationUnit> newTus = new ArrayList<>();
        try {
            Collection<NativeProject> nativeProjects = CMUtilities.getNativeProjects(snapshot.getSource().getFileObject());
            for (NativeProject p : nativeProjects) {
                Collection<CMIndex> indices = CMQuery.getIndices(p);
                NativeProjectCompilationDataBase cdb = new NativeProjectCompilationDataBase(p);
                //get from the database the list of tu URI's
                Collection<URI> translationUnitsURIs = SPIUtilities.getTranslationUnitsURI(uri);
                if (!translationUnitsURIs.isEmpty()) {
                    // first check if we have translation unit already
                    boolean reparse = false;
                    for (CMTranslationUnit tu : tus) {
                        //FIXME: there should be better way to get MainFile URI
                        URI mainFileURI = new File(tu.getMainFilePath().toString()).toURI();
                        if (translationUnitsURIs.contains(mainFileURI) && SPIUtilities.reparseTranslationUnit(tu, unsaved) == 0) {
                            newTus.add(tu);
                            reparse = true;
                            break;
                        }
                    }
                    if (!reparse) {
                        // otherwise take only the first one
                        URI mainFileURI = translationUnitsURIs.iterator().next();
                        CMCompilationDataBase.Entry fileEntry = cdb.getFileEntry(mainFileURI);
                        if (fileEntry != null) { // I excluded a file from build and got NPE since entry == null in this case
                            newTus.addAll(SPIUtilities.reparseFile(indices, fileEntry, unsaved));
                        }
                    }
                }
            }
        } finally {
            setNewTranslationUnits(newTus);
        }
    }
    
    private void setNewTranslationUnits(Collection<CMTranslationUnit> newTus) {
        for (CMTranslationUnit tu : tus) {
            if (!newTus.contains(tu)) {
                SPIUtilities.disposeTranslationUnit(tu);
            }
        }
        tus.clear();
        tus.addAll(newTus);
    }

    @Override
    public void cancel(CancelReason reason, SourceModificationEvent event) {
        super.cancel(reason, event);
    }

    @Override
    public CndParserResult getResult(Task task) throws ParseException {
        return new CndParserResult(tus, snapshot);
    }

    @Override
    public void addChangeListener(ChangeListener changeListener) {
    }

    @Override
    public void removeChangeListener(ChangeListener changeListener) {
    }

    // FIXME: disable for now
//    @MimeRegistrations({
//        @MimeRegistration(mimeType=MIMENames.C_MIME_TYPE, service=ParserFactory.class),
//        @MimeRegistration(mimeType=MIMENames.CPLUSPLUS_MIME_TYPE, service=ParserFactory.class),
//        @MimeRegistration(mimeType=MIMENames.HEADER_MIME_TYPE, service=ParserFactory.class)
//    })
    public static class FactoryImpl extends ParserFactory {
        @Override
        public Parser createParser(Collection<Snapshot> snapshots) {
          if (NativeProjectBridge.ENABLED) {
            return new CndParser(snapshots);
          }
          return null;
        }
    }
   
    private static class UnsavedFileImpl implements CMUnsavedFileImplementation {
        private final URI uri;
        private final CharSequence content;

        public UnsavedFileImpl(URI uri, CharSequence content) {
            this.uri = uri;
            this.content = content;
        }
        
        @Override
        public URI getURI() {
            return uri;
        }

        @Override
        public long getLength() {
            return content.length();
        }

        @Override
        public CharSequence getFileContent() {
            return content;
        }
    }
}
