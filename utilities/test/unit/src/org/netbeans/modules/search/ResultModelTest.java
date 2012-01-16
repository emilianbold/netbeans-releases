/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU General
 * Public License Version 2 only ("GPL") or the Common Development and
 * Distribution License("CDDL") (collectively, the "License"). You may not use
 * this file except in compliance with the License. You can obtain a copy of the
 * License at http://www.netbeans.org/cddl-gplv2.html or
 * nbbuild/licenses/CDDL-GPL-2-CP. See the License for the specific language
 * governing permissions and limitations under the License. When distributing
 * the software, include this License Header Notice in each file and include the
 * License file at nbbuild/licenses/CDDL-GPL-2-CP. Oracle designates this
 * particular file as subject to the "Classpath" exception as provided by Oracle
 * in the GPL Version 2 section of the License file that accompanied this code.
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL or only
 * the GPL Version 2, indicate your decision by adding "[Contributor] elects to
 * include this software in this distribution under the [CDDL or GPL Version 2]
 * license." If you do not indicate a single choice of license, a recipient has
 * the option to distribute your version of this file under either the CDDL, the
 * GPL Version 2 or to extend the choice of license to its licensees as provided
 * above. However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is made
 * subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.search;

import java.awt.EventQueue;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;
import javax.swing.event.ChangeListener;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openidex.search.SearchInfo;
import org.openidex.search.SearchType;

/**
 *
 * @author jhavlin
 */
public class ResultModelTest extends NbTestCase {

    public ResultModelTest(String name) {
        super(name);
    }
    ResultTreeModel rtm;

    /**
     * Bug 203883 - [71cat] AssertionError at
     * org.netbeans.modules.search.ResultTreeModel.updateRootNodeSelection.
     *
     * If one file object is found twice (it can happen on some filesystems) and
     * then deleted, it should throw no error.
     */
    public void testObjectFoundTwice() throws Exception {

        FileObject root = FileUtil.createMemoryFileSystem().getRoot();
        final FileObject fo = root.createData("test.txt");

        final BasicSearchCriteria criteria = new BasicSearchCriteria();
        final SearchScope scope = new CustomSearchScope();
        final List<SearchType> types = Collections.emptyList();

        final SearchTask st = new SearchTask(scope, criteria, types);
        final ResultModel rm = st.getResultModel();

        EventQueue.invokeAndWait(new Runnable() {

            @Override
            public void run() {
                rtm = new ResultTreeModel(rm);
                rm.setObserver(rtm);
                ResultViewPanel rvm = new ResultViewPanel(st);
                rm.setObserver(rvm);
            }
        });

        rm.objectFound(fo, Charset.defaultCharset());
        rm.objectFound(fo, Charset.defaultCharset());

        assertEquals(1, rm.getMatchingObjects().size());

        EventQueue.invokeAndWait(new Runnable() {

            @Override
            public void run() {
                for (MatchingObject mo : rm.getMatchingObjects()) {
                    try {
                        mo.getFileObject().delete();
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }
        });
    }

    private static class CustomSearchScope extends SearchScope {

        @Override
        public String getTypeId() {
            return "TEST";
        }

        @Override
        protected String getDisplayName() {
            return "Test";
        }

        @Override
        protected boolean isApplicable() {
            return true;
        }

        @Override
        protected void addChangeListener(ChangeListener l) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        protected void removeChangeListener(ChangeListener l) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        protected SearchInfo getSearchInfo() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }
}
