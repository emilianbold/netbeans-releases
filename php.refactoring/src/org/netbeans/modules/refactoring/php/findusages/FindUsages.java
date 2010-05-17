/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.refactoring.php.findusages;

import java.util.Collection;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.JTextComponent;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.refactoring.spi.ui.UI;
import org.netbeans.modules.refactoring.spi.ui.RefactoringUI;
import org.netbeans.modules.php.editor.parser.astnodes.Program;
import org.openide.ErrorManager;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.windows.TopComponent;

/**
 * @author Radek Matous
 */
public class FindUsages  {
    private static final Logger LOG = Logger.getLogger(FindUsages.class.getName());   
    private FindUsages() {
    }

    public static boolean canFindUsages(Lookup lookup) {
        //TODO: is from editor? review, improve
        EditorCookie ec = lookup.lookup(EditorCookie.class);
        if ((ec == null || ec.getOpenedPanes() == null)) {
            return false;
        }
        Collection<? extends Node> nodes = lookup.lookupAll(Node.class);
        if (nodes.size() != 1) {
            return false;
        }
        Node n = nodes.iterator().next();
        DataObject dob = n.getCookie(DataObject.class);
        if (dob == null) {
            return false;
        }
        FileObject fo = dob.getPrimaryFile();
        return RefactoringUtils.isRefactorable(fo) ? !RefactoringUtils.isOutsidePhp(lookup, fo) : false;
    }

    public static void doFindUsages(Lookup lookup) {
        EditorCookie ec = lookup.lookup(EditorCookie.class);
        if (RefactoringUtils.isFromEditor(ec)) {
            FindUsagesTask.start(ec);
        }
    }

    private static class FindUsagesTask extends UserTask {
        private JTextComponent textC;
        private int caret;
        private RefactoringUI ui;

        private FindUsagesTask(EditorCookie ec) {
            this.textC = ec.getOpenedPanes()[0];
            this.caret = textC.getCaretPosition();
            assert caret != -1;
        }
        
        public static void start(EditorCookie ec) {
            FindUsagesTask t = new FindUsagesTask(ec);
            try {
                ParserManager.parse(Collections.singleton(Source.create(ec.getDocument())), t);
            } catch (ParseException ex) {
                ErrorManager.getDefault().notify(ex);
                return;
            }

            TopComponent activetc = TopComponent.getRegistry().getActivated();
            if (t.ui != null) {
                UI.openRefactoringUI(t.ui, activetc);
            } else {
                //TODO: handle if cannot find usages
            }
        }

        public void cancel() {
        }

        @Override
        public void run(ResultIterator resultIterator) throws Exception {
            ParserResult cc = (ParserResult)resultIterator.getParserResult();
            Program root = RefactoringUtils.getRoot(cc);
            if (root == null) {
                // TODO How do I add some kind of error message?
                LOG.log(Level.FINE, "FAILURE - can't refactor uncompileable sources");
                return;
            }
            WhereUsedSupport ctx = WhereUsedSupport.getInstance(cc, caret);
            if (ctx != null && ctx.getName() != null) {
                ui = new WhereUsedQueryUI(ctx, cc);
            }
        }
    }
}
