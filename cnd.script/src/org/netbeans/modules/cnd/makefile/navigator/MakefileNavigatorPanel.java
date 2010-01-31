/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.makefile.navigator;

import java.util.Collections;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import org.netbeans.modules.cnd.makefile.parser.MakefileModel;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser.Result;
import org.netbeans.spi.navigator.NavigatorPanel;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 * @author Alexey Vladykin
 */
public class MakefileNavigatorPanel implements NavigatorPanel, LookupListener {

    private Lookup.Result<FileObject> lookupResult;
    private MakefileNavigatorPanelUI panel;
    private final UpdaterTask updaterTask;

    public MakefileNavigatorPanel() {
        updaterTask = new UpdaterTask();
    }

    @Override
    public String getDisplayName() {
        return getMessage("navigator.title"); // NOI18N
    }

    @Override
    public String getDisplayHint() {
        return getMessage("navigator.hint"); // NOI18N
    }

    @Override
    public JComponent getComponent() {
        if (panel == null) {
            panel = new MakefileNavigatorPanelUI();
        }
        return panel;
    }

    @Override
    public void panelActivated(Lookup context) {
        lookupResult = context.lookupResult(FileObject.class);
        lookupResult.addLookupListener(this);
        scheduleUpdate(findMakefile(lookupResult));
    }

    @Override
    public void panelDeactivated() {
        lookupResult.removeLookupListener(this);
        scheduleUpdate(null);
    }

    @Override
    public Lookup getLookup() {
        return null;
    }

    @Override
    public void resultChanged(LookupEvent ev) {
        scheduleUpdate(findMakefile(lookupResult));
    }

    private void scheduleUpdate(final FileObject makefile) {
        if (makefile != null) {
            uiSetWaiting();
            RequestProcessor.getDefault().post(new Runnable() {
                public void run() {
                    Source source = Source.create(makefile);
                    try {
                        ParserManager.parse(Collections.singletonList(source), updaterTask);
                    } catch (ParseException ex) {
                        ex.printStackTrace();
                    }
                }
            });
        } else {
            uiSetModel(null);
        }
    }

    private void uiSetWaiting() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                panel.setWaiting();
            }
        });
    }

    private void uiSetModel(final MakefileModel model) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                panel.setModel(model);
            }
        });
    }

    private FileObject findMakefile(Lookup.Result<FileObject> result) {
        for (FileObject file : result.allInstances()) {
            if (file.getMIMEType().equals(MIMENames.MAKEFILE_MIME_TYPE)) {
                return file;
            }
        }
        return null;
    }

    private static String getMessage(String key) {
        return NbBundle.getMessage(MakefileNavigatorPanel.class, key);
    }

    private class UpdaterTask extends UserTask {
        @Override
        public void run(ResultIterator resultIterator) throws ParseException {
            Result r = resultIterator.getParserResult();
            uiSetModel((MakefileModel) r);
        }
    }
}
