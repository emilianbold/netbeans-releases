/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.javacard.project;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.queries.SourceForBinaryQuery.Result;
import org.netbeans.spi.java.queries.SourceForBinaryQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.util.ChangeSupport;

/**
 * Merges multiple SFBQs together.
 *
 * @author Tim Boudreau
 */
final class ProxySourceForBinaryQuery implements SourceForBinaryQueryImplementation {
    private final SourceForBinaryQueryImplementation[] impls;
    ProxySourceForBinaryQuery (SourceForBinaryQueryImplementation... impl) {
        this.impls = impl;
    }

    public Result findSourceRoots(URL binaryRoot) {
        List<Result> l = new ArrayList<Result>(impls.length);
        for (int i = 0; i < impls.length; i++) {
            Result r = impls[i].findSourceRoots(binaryRoot);
            if (r != null) {
                l.add (r);
            }
        }
        return new ProxyResult(l.toArray(new Result[l.size()]));
    }

    private static final class ProxyResult implements Result, ChangeListener {
        private final Result[] rs;
        private final ChangeSupport supp = new ChangeSupport(this);
        ProxyResult (Result[] rs) {
            this.rs = rs;
        }

        public FileObject[] getRoots() {
            List<FileObject> l = new ArrayList<FileObject> (rs.length * 3);
            for (Result r : rs) {
                l.addAll (Arrays.asList(r.getRoots()));
            }
            return l.toArray(new FileObject[l.size()]);
        }

        public synchronized void addChangeListener(ChangeListener l) {
            boolean first = !supp.hasListeners();
            supp.addChangeListener(l);
            if (first) {
                for (Result r : rs) {
                    r.addChangeListener(this);
                }
            }
        }

        public synchronized void removeChangeListener(ChangeListener l) {
            supp.removeChangeListener(l);
            boolean last = !supp.hasListeners();
            if (last) {
                for (Result r : rs) {
                    r.removeChangeListener(this);
                }
            }
        }

        public void stateChanged(ChangeEvent e) {
            supp.fireChange();
        }
    }
}
