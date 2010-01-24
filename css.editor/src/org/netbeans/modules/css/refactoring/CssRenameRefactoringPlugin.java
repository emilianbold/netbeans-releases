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
package org.netbeans.modules.css.refactoring;

import java.util.Collections;
import javax.swing.text.Position.Bias;
import org.netbeans.modules.csl.spi.support.ModificationResult;
import org.netbeans.modules.csl.spi.support.ModificationResult.Difference;
import org.netbeans.modules.css.editor.Css;
import org.netbeans.modules.css.parser.NodeVisitor;
import org.netbeans.modules.css.parser.SimpleNode;
import org.netbeans.modules.css.parser.SimpleNodeUtil;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.RenameRefactoring;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.netbeans.modules.refactoring.spi.RefactoringPlugin;
import org.openide.text.CloneableEditorSupport;
import org.openide.util.Lookup;

/**
 *
 * @author marekfukala
 */
public class CssRenameRefactoringPlugin implements RefactoringPlugin {

    private RenameRefactoring refactoring;

    public CssRenameRefactoringPlugin(RenameRefactoring refactoring) {
	this.refactoring = refactoring;
    }

    public Problem preCheck() {
	return null;
    }

    public Problem checkParameters() {
	return null;
    }

    public Problem fastCheckParameters() {
	return null;
    }

    public void cancelRequest() {
	//no-op
    }

    public Problem prepare(final RefactoringElementsBag refactoringElements) {
	Lookup lookup = refactoring.getRefactoringSource();
	CssElementContext context = lookup.lookup(CssElementContext.class);

	if (context instanceof CssElementContext.Editor) {
	    //find all occurances of selected element in (and only in) THIS file.
	    final CssElementContext.Editor econtext = (CssElementContext.Editor) context;

	    final SimpleNode element = econtext.getElement();
	    SimpleNode root = econtext.getParserResult().root();

	    final CloneableEditorSupport ces = Css.findCloneableEditorSupport(context.getFileObject());
	    final ModificationResult mr = new ModificationResult(); //per file???

	    //add the differences to the modificationResults
	    SimpleNodeUtil.visitChildren(root, new NodeVisitor() {

		public void visit(SimpleNode node) {
		    if (node.kind() == element.kind() && node.image().equals(element.image())) {
			int astFrom = econtext.getParserResult().getSnapshot().getOriginalOffset(node.startOffset());
			int astTo = econtext.getParserResult().getSnapshot().getOriginalOffset(node.endOffset());
			Difference diff = new Difference(Difference.Kind.CHANGE,
				ces.createPositionRef(astFrom, Bias.Forward),
				ces.createPositionRef(astTo, Bias.Backward),
				node.image(),
				refactoring.getNewName(),
				"Rename selector name");
			mr.addDifferences(econtext.getFileObject(), Collections.singletonList(diff));

		    }
		}
	    });

	    refactoringElements.registerTransaction(new RetoucheCommit(Collections.singletonList(mr)));

	    for (Difference diff : mr.getDifferences(context.getFileObject())) {
		refactoringElements.add(refactoring, DiffElement.create(diff, context.getFileObject(), mr));

	    }
	} else if (context instanceof CssElementContext.File) {
	    //refactor a file in explorer
	    CssElementContext.File fileContext = (CssElementContext.File)context;
	    System.out.println("refactor file " + fileContext.getFileObject().getPath() );
	} else if (context instanceof CssElementContext.Folder) {
	    //refactor a folder in explorer
	    CssElementContext.Folder fileContext = (CssElementContext.Folder)context;
	    System.out.println("refactor folder " + fileContext.getFileObject().getPath() );
	}

	return null;
    }
}
