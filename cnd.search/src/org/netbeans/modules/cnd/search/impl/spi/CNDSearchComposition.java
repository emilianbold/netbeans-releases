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
package org.netbeans.modules.cnd.search.impl.spi;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import org.netbeans.api.search.SearchRoot;
import org.netbeans.api.search.provider.SearchListener;
import org.netbeans.modules.cnd.search.Searcher;
import org.netbeans.modules.cnd.search.MatchingFileData;
import org.netbeans.modules.cnd.search.SearchParams;
import org.netbeans.modules.cnd.search.SearchResult;
import org.netbeans.modules.cnd.search.impl.UnixFindBasedSearcher;
import org.netbeans.modules.cnd.search.ui.SearchResultNode;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.NativeProcess;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;
import org.netbeans.modules.remote.spi.FileSystemProvider;
import org.netbeans.spi.search.provider.SearchComposition;
import org.netbeans.spi.search.provider.SearchProvider;
import org.netbeans.spi.search.provider.SearchProvider.Presenter;
import org.netbeans.spi.search.provider.SearchResultsDisplayer;
import org.netbeans.spi.search.provider.SearchResultsDisplayer.NodeDisplayer;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Cancellable;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;

/**
 *
 * @author akrasny
 */
public final class CNDSearchComposition extends SearchComposition<SearchResult> {

    private static final RequestProcessor RP = new RequestProcessor(CNDSearchComposition.class.getName(), 1);
    private final AtomicBoolean terminated = new AtomicBoolean(false);
    private final SearchParams params;
    private SearchResultsDisplayer<SearchResult> displayer;
    private final String title;
    private final Presenter presenter;
    private Cancellable cancel;

    public CNDSearchComposition(String title, SearchProvider.Presenter presenter, SearchParams params) {
        this.title = title;
        this.presenter = presenter;
        this.params = params;
    }

    @Override
    public void start(SearchListener listener) {
        for (SearchRoot root : params.getSearchRoots()) {
            try {
                searchInRoot(root, listener);
            } catch (Exception e) {
                listener.generalError(e);
            } finally {
                terminate();
            }
        }
    }

    @Override
    public void terminate() {
        if (terminated.compareAndSet(false, true)) {
            if (cancel != null) {
                cancel.cancel();
                cancel = null;
            }
        }
    }

    @Override
    public boolean isTerminated() {
        return terminated.get();
    }

    @Override
    public synchronized SearchResultsDisplayer<SearchResult> getSearchResultsDisplayer() {
        if (displayer == null) {
            displayer = SearchResultsDisplayer.createDefault(new DisplayerHelper(), this, presenter, title);
        }
        return displayer;
    }

    private void searchInRoot(SearchRoot root, SearchListener listener) {
        if (isTerminated()) {
            return;
        }

        final ExecutionEnvironment env = FileSystemProvider.getExecutionEnvironment(root.getFileObject());

        if (!ConnectionManager.getInstance().isConnectedTo(env)) {
            return;
        }

        if (!HostInfoUtils.isHostInfoAvailable(env)) {
            return;
        }

        final Searcher find;
        try {
            if (HostInfoUtils.getHostInfo(env).getOSFamily().isUnix()) {
                find = new UnixFindBasedSearcher(root, params);
            } else {
                find = null;
            }
        } catch (Exception ex) {
            return;
        }

        if (find == null) {
            return;
        }

        NativeProcessBuilder npb = NativeProcessBuilder.newProcessBuilder(env);
        npb.setExecutable(find.getCommand()).setArguments(find.getCommandArguments());

        try {
            final NativeProcess process = npb.call();
            final Runnable readOutputTask = new Runnable() {

                @Override
                public void run() {
                    try {
                        BufferedReader br = ProcessUtils.getReader(process.getInputStream(), env.isRemote());
                        String line;
                        while ((line = br.readLine()) != null) {
                            MatchingFileData data = find.processOutputLine(line);

                            if (data != null) {
                                displayer.addMatchingObject(new SearchResult(env, data));
                            }
                        }
                    } catch (IOException ex) {
                        displayer.addMatchingObject(new SearchResult(ex));
                    }
                }
            };

            final Task task = RP.post(readOutputTask);

            cancel = new Cancellable() {

                @Override
                public boolean cancel() {
                    process.destroy();
                    return task.cancel();
                }
            };

            int status = process.waitFor();

            if (status != 0) {
                String error = ProcessUtils.readProcessErrorLine(process);
                Throwable ex = new Throwable(error);
                listener.generalError(ex);
                displayer.addMatchingObject(new SearchResult(ex));
            }
        } catch (InterruptedException ex) {
            // Exceptions.printStackTrace(ex);
        } catch (IOException ex) {
            listener.generalError(ex);
            displayer.addMatchingObject(new SearchResult(ex));
        }
    }

    private static class DisplayerHelper extends NodeDisplayer<SearchResult> {

        @Override
        public Node matchToNode(final SearchResult result) {
            if (result.exception == null) {
                return new SearchResultNode(result);
            } else {
                return new AbstractNode(Children.LEAF) {

                    @Override
                    public String getName() {
                        return result.exception.getMessage();
                    }
                };
            }
        }
    }
}
