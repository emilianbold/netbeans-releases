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
package org.netbeans.modules.css.editor.refactoring;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JEditorPane;
import javax.swing.JOptionPane;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.modules.css.editor.Css;
import org.netbeans.modules.css.editor.LexerUtils;
import org.netbeans.modules.css.gsf.api.CssParserResult;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.refactoring.spi.ui.ActionsImplementationProvider;
import org.netbeans.modules.refactoring.spi.ui.RefactoringUI;
import org.netbeans.modules.refactoring.spi.ui.UI;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.TopComponent;

/**
 *
 * @author marekfukala
 */
@ServiceProvider(service = ActionsImplementationProvider.class)
public class CssActionsImplementationProvider extends ActionsImplementationProvider {

    private static final Logger LOG = Logger.getLogger(CssActionsImplementationProvider.class.getName());

    @Override
    public boolean canRename(Lookup lookup) {
	Collection<? extends Node> nodes = lookup.lookupAll(Node.class);
	if (nodes.size() != 1) {
	    return false;
	}
	return isCssContext(nodes.iterator().next());
    }

    @Override
    public void doRename(Lookup selectedNodes) {
	EditorCookie ec = selectedNodes.lookup(EditorCookie.class);
	if (representsOpenedFile(ec)) {
	    //editor refactoring
	    new TextComponentTask(ec) {
		@Override
		protected RefactoringUI createRefactoringUI(CssElementContext context) {
		    return new CssRenameRefactoringUI(context);
		}
	    }.run();
	} else {
	    //file refactoring
	    
	}
    }

    private static boolean isCssContext(Node node) {
	//for the one thing check if the node represents a css file itself
	FileObject fo = getFileObjectFromNode(node);
	if (fo == null) {
	    return false;
	}
	if (Css.CSS_MIME_TYPE.equals(fo.getMIMEType())) { //NOI18N
	    return true;
	}

	//for the second check if the node represents a top level or embedded css element in the editor
	EditorCookie ec = getEditorCookie(node);
	if (representsOpenedFile(ec)) {
	    final Document doc = ec.getDocument();
	    JEditorPane pane = ec.getOpenedPanes()[0];
	    final int offset = pane.getCaretPosition();
	    final AtomicBoolean ref = new AtomicBoolean(false);
	    doc.render(new Runnable() {

		public void run() {
		    ref.set(null == LexerUtils.getJoinedTokenSequence(doc, offset));
		}
	    });
	    return ref.get();
	}

	return false;

    }

    private static FileObject getFileObjectFromNode(Node node) {
	DataObject dobj = node.getLookup().lookup(DataObject.class);
	return dobj != null ? dobj.getPrimaryFile() : null;
    }

    private static boolean representsOpenedFile(EditorCookie ec) {
	return ec != null ? ec.getOpenedPanes() != null : null;
    }

    private static EditorCookie getEditorCookie(Node node) {
	return node.getLookup().lookup(EditorCookie.class);
    }

    public static abstract class TextComponentTask extends UserTask implements Runnable {

	private final Document document;
	private final int caretOffset;
	private final int selectionStart;
	private final int selectionEnd;
	private RefactoringUI ui;

	public TextComponentTask(EditorCookie ec) {
	    JTextComponent textC = ec.getOpenedPanes()[0];
	    this.document = textC.getDocument();
	    this.caretOffset = textC.getCaretPosition();
	    this.selectionStart = textC.getSelectionStart();
	    this.selectionEnd = textC.getSelectionEnd();
	}

	public void run(ResultIterator ri) throws ParseException {
	    ResultIterator cssri = Css.getResultIterator(ri, Css.CSS_MIME_TYPE);
	    if (cssri != null) {
		CssParserResult result = (CssParserResult) cssri.getParserResult();
		CssElementContext context = new CssElementContext.Editor(result, caretOffset, selectionStart, selectionEnd);
		ui = context.isRefactoringAllowed() ? createRefactoringUI(context) : null;
	    }
	}

	public final void run() {
	    try {
		Source source = Source.create(document);
		ParserManager.parse(Collections.singleton(source), this);
	    } catch (ParseException e) {
		LOG.log(Level.WARNING, null, e);
		return;
	    }

	    TopComponent activetc = TopComponent.getRegistry().getActivated();

	    if (ui != null) {
		UI.openRefactoringUI(ui, activetc);
	    } else {
		JOptionPane.showMessageDialog(null, NbBundle.getMessage(CssActionsImplementationProvider.class, "ERR_CannotRenameLoc"));
	    }
	}

	protected abstract RefactoringUI createRefactoringUI(CssElementContext context);
	
    }
}
