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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JEditorPane;
import javax.swing.JOptionPane;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.modules.css.editor.LexerUtils;
import org.netbeans.modules.css.gsf.CssLanguage;
import org.netbeans.modules.css.gsf.api.CssParserResult;
import org.netbeans.modules.parsing.api.Embedding;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.refactoring.spi.ui.ActionsImplementationProvider;
import org.netbeans.modules.refactoring.spi.ui.RefactoringUI;
import org.netbeans.modules.refactoring.spi.ui.UI;
import org.netbeans.modules.web.common.api.WebUtils;
import org.netbeans.modules.web.common.spi.ProjectWebRootQuery;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.text.CloneableEditorSupport;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.TopComponent;

/**
 * @todo AFAIR there should be a generic folder refactoring support which anyone could
 * just plug into. I just copied the way how java does that. It "eats" up
 * the "java folders" (those whose have the NonRecursiveFolder instance in the Node's lookup)
 * for its own refactoring. Anyone who wants to refactor folders (including
 * the java ones) need to create a plugin for the java refactoring and create
 * a new refactoring actions for the "non-java" folders + another plugin.
 * The same situation now happens with the css refactoring actions - it "eats up"
 * all folders (except the java ones since java ref. actions are registered before
 * the Css's ones) and anyone needs to write a plugin agains css refactoring :-(
 * to be able to handle folder renames. I recon this is not the way it is supposet to work.
 *
 * /2 hmm, this seems to be a temporary solution anyway since once I wanna enable
 * the html refactoring which includes the ability to handle folder renames I need a
 * better way than to code agains css... However for the time beeing lets keep it this way.
 *
 * @author marekfukala
 */
@ServiceProvider(service = ActionsImplementationProvider.class, position=1033)
public class CssActionsImplementationProvider extends ActionsImplementationProvider {

    private static final Logger LOG = Logger.getLogger(CssActionsImplementationProvider.class.getName());

    @Override
    public boolean canRename(Lookup lookup) {
	Collection<? extends Node> nodes = lookup.lookupAll(Node.class);
	//we are able to rename only one node selection [at least for now ;-) ]
	if (nodes.size() != 1) {
	    return false;
	}

	//check if the file is a file with .css extension or represents
	//an opened file which code embeds a css content on the caret position
	Node node = nodes.iterator().next();
	if (isCssContext(node)) {
	    return true;
	}

	//check if the node represents a folder which may potentially contain
	//files containg css files or embedded css in some other files (html, jsp ...)
        //XXX We will disable all refactoring actions possibly registered
        //behind us for folders!!!! Read my comment in the class javadoc
	FileObject fo = getFileObjectFromNode(node);
	if (fo != null && fo.isValid() && fo.isFolder()) {
            //check if the folder is a part of a web-like project using the
            //web root query
            if(ProjectWebRootQuery.getWebRoot(fo) != null) {
                //looks like the file is a web like project
                try {
                    return!fo.getFileSystem().isReadOnly();
                } catch (FileStateInvalidException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
	}

	return false; //we are not interested in refactoring this object/s

    }

    @Override
    public void doRename(Lookup selectedNodes) {
	EditorCookie ec = selectedNodes.lookup(EditorCookie.class);
	if (isFromEditor(ec)) {
	    //editor refactoring
	    new TextComponentTask(ec) {

		@Override
		protected RefactoringUI createRefactoringUI(CssElementContext context) {
		    return new CssRenameRefactoringUI(context);
		}
	    }.run();
	} else {
	    //file or folder refactoring
	    Collection<? extends Node> nodes = selectedNodes.lookupAll(Node.class);
	    assert nodes.size() == 1;
	    Node currentNode = nodes.iterator().next();
	    new NodeToFileTask(currentNode) {

		@Override
		protected RefactoringUI createRefactoringUI(CssElementContext context) {
		    return new CssRenameRefactoringUI(context);
		}
	    }.run();
	}
    }

    @Override
    public boolean canFindUsages(Lookup lookup) {
        Collection<? extends Node> nodes = lookup.lookupAll(Node.class);
	//we are able to rename only one node selection [at least for now ;-) ]
	if (nodes.size() != 1) {
	    return false;
	}

	//check if the file is a file with .css extension or represents
	//an opened file which code embeds a css content on the caret position
	Node node = nodes.iterator().next();
	if (isCssContext(node)) {
	    return true;
	}
        return false;
    }

    @Override
    public void doFindUsages(Lookup lookup) {
        EditorCookie ec = lookup.lookup(EditorCookie.class);
	if (isFromEditor(ec)) {
	    new TextComponentTask(ec) {
                //editor element context
		@Override
		protected RefactoringUI createRefactoringUI(CssElementContext context) {
		    return new WhereUsedUI(context);
		}
	    }.run();
	} else {
	    //file context
	    Collection<? extends Node> nodes = lookup.lookupAll(Node.class);
	    assert nodes.size() == 1;
	    Node currentNode = nodes.iterator().next();
	    new NodeToFileTask(currentNode) {

		@Override
		protected RefactoringUI createRefactoringUI(CssElementContext context) {
		    return new WhereUsedUI(context);
		}
	    }.run();
	}
    }



    private static boolean isCssContext(Node node) {
	//for the one thing check if the node represents a css file itself
	FileObject fo = getFileObjectFromNode(node);
	if (fo == null) {
	    return false;
	}
	if (CssLanguage.CSS_MIME_TYPE.equals(fo.getMIMEType())) { //NOI18N
	    return true;
	}

	//for the second check if the node represents a top level or embedded css element in the editor
	EditorCookie ec = getEditorCookie(node);
	if (isFromEditor(ec)) {
	    final Document doc = ec.getDocument();
	    JEditorPane pane = ec.getOpenedPanes()[0];
	    final int offset = pane.getCaretPosition();
	    final AtomicBoolean ref = new AtomicBoolean(false);
	    doc.render(new Runnable() {

                @Override
		public void run() {
		    ref.set(null != LexerUtils.getJoinedTokenSequence(doc, offset));
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

    private static boolean isFromEditor(EditorCookie ec) {
        if (ec != null && ec.getOpenedPanes() != null) {
            TopComponent activetc = TopComponent.getRegistry().getActivated();
            if (activetc instanceof CloneableEditorSupport.Pane) {
                return true;
            }
        }
        return false;
    }

    private static EditorCookie getEditorCookie(Node node) {
	return node.getLookup().lookup(EditorCookie.class);
    }

    public static abstract class NodeToFileTask extends UserTask implements Runnable {

	private final Node node;
	private CssElementContext context;
	private FileObject fileObject;

	public NodeToFileTask(Node node) {
	    this.node = node;
	}

	@Override
	public void run(ResultIterator resultIterator) throws Exception {
	    Collection<CssParserResult> results = new ArrayList<CssParserResult>();
	    Snapshot snapshot = resultIterator.getSnapshot();
	    try {
		if (CssLanguage.CSS_MIME_TYPE.equals(snapshot.getMimeType())) {
		    results.add((CssParserResult) resultIterator.getParserResult());
		    return;
		}
		for (Embedding e : resultIterator.getEmbeddings()) {
		    run(resultIterator.getResultIterator(e));
		}
	    } finally {
		context = new CssElementContext.File(fileObject, results);
	    }
	}

        @Override
	public void run() {
	    DataObject dobj = node.getLookup().lookup(DataObject.class);
	    if (dobj != null) {
		fileObject = dobj.getPrimaryFile();

		if (fileObject.isFolder()) {
		    //folder
		    UI.openRefactoringUI(createRefactoringUI(new CssElementContext.Folder(fileObject)));
		} else {
		    //css file
		    Source source = Source.create(fileObject);
		    try {
			ParserManager.parse(Collections.singletonList(source), this);
			UI.openRefactoringUI(createRefactoringUI(context));
		    } catch (ParseException ex) {
			Exceptions.printStackTrace(ex);
		    }
		}
	    }

	}

	protected abstract RefactoringUI createRefactoringUI(CssElementContext context);
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

        @Override
	public void run(ResultIterator ri) throws ParseException {
	    ResultIterator cssri = WebUtils.getResultIterator(ri, CssLanguage.CSS_MIME_TYPE);
	    if (cssri != null) {
		CssParserResult result = (CssParserResult) cssri.getParserResult();
                if(result.root() != null) {
                    //the parser result seems to be quite ok,
                    //in case of serious parse issue the parse root is null
                    CssElementContext context = new CssElementContext.Editor(result, caretOffset, selectionStart, selectionEnd);
                    ui = context.isRefactoringAllowed() ? createRefactoringUI(context) : null;
                }
	    }
	}

        @Override
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
		JOptionPane.showMessageDialog(null, NbBundle.getMessage(CssActionsImplementationProvider.class, "ERR_CannotRefactorLoc"));//NOI18N
	    }
	}

	protected abstract RefactoringUI createRefactoringUI(CssElementContext context);
    }
}
