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

package org.netbeans.modules.mercurial.search;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.api.actions.Openable;
import org.netbeans.api.extexecution.ExternalProcessBuilder;
import org.netbeans.api.extexecution.input.InputProcessors;
import org.netbeans.api.extexecution.input.InputReaderTask;
import org.netbeans.api.extexecution.input.InputReaders;
import org.netbeans.api.extexecution.input.LineProcessor;
import org.netbeans.api.search.provider.SearchListener;
import org.netbeans.spi.search.provider.SearchComposition;
import org.netbeans.spi.search.provider.SearchProvider;
import org.netbeans.spi.search.provider.SearchResultsDisplayer;
import org.netbeans.spi.search.provider.SearchResultsDisplayer.NodeDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;

class HgGrep extends SearchComposition<String> {

    private static final RequestProcessor RP = new RequestProcessor(HgGrep.class.getName(), 5);

    private final SearchProvider.Presenter presenter;
    private final File repo;
    private final String pattern;
    private final String text;
    private final SearchResultsDisplayer<String> displayer;

    HgGrep(SearchProvider.Presenter presenter, File repo, String pattern, String text) {
        this.presenter = presenter;
        this.repo = repo;
        this.pattern = pattern;
        this.text = text;
        displayer = SearchResultsDisplayer.createDefault(new FileDisplayer(), this, presenter, "Hg Grep");
    }

    @Override public void start(SearchListener listener) {
        try {
            Process proc = new ExternalProcessBuilder("sh").
                    addArgument("-c").
                    addArgument("hg locate --rev . --print0 '" + pattern + "' | xargs --null egrep --files-with-matches '" + text + "'").
                    workingDirectory(repo).
                    call();
            RP.post(InputReaderTask.newTask(InputReaders.forStream(proc.getInputStream(), Charset.defaultCharset()), InputProcessors.bridge(new LineProcessor() {
                @Override public void processLine(String line) {
                    getSearchResultsDisplayer().addMatchingObject(line);
                }
                @Override public void reset() {}
                @Override public void close() {}
            })));
        } catch (IOException x) {
           listener.generalError(x);
        }
    }

    @Override public void terminate() {
        // XXX
    }

    @Override public boolean isTerminated() {
        return false; // XXX
    }

    @Override public SearchResultsDisplayer<String> getSearchResultsDisplayer() {
        return displayer;
    }

    // XXX could use -n rather than -l and produce one result per line (but then trickier to group them)
    private class FileDisplayer extends NodeDisplayer<String> {

        @Override public Node matchToNode(final String match) {
            return new AbstractNode(Children.LEAF) {
                @Override public String getName() {
                    return match;
                }
                @Override public Action getPreferredAction() {
                    return new AbstractAction() {
                        @Override public void actionPerformed(ActionEvent e) {
                            FileObject f = FileUtil.toFileObject(new File(repo, match));
                            if (f == null) {
                                return;
                            }
                            try {
                                Openable open = DataObject.find(f).getLookup().lookup(Openable.class);
                                if (open != null) {
                                    open.open();
                                }
                            } catch (DataObjectNotFoundException x) {
                                Exceptions.printStackTrace(x);
                            }
                        }
                    };
                }
            };
        }

    }

}
