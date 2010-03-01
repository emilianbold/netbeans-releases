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
package org.netbeans.modules.web.jsf.editor;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.ext.html.parser.AstNode;
import org.netbeans.editor.ext.html.parser.AstNodeUtils;
import org.netbeans.editor.ext.html.parser.AstNodeVisitor;
import org.netbeans.modules.csl.api.Hint;
import org.netbeans.modules.csl.api.HintFix;
import org.netbeans.modules.csl.api.HintSeverity;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.api.Rule;
import org.netbeans.modules.csl.api.Rule.SelectionRule;
import org.netbeans.modules.csl.api.RuleContext;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.editor.indent.api.Indent;
import org.netbeans.modules.html.editor.api.HtmlKit;
import org.netbeans.modules.html.editor.api.Utils;
import org.netbeans.modules.html.editor.api.gsf.HtmlParserResult;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.web.common.api.WebUtils;
import org.netbeans.modules.web.jsf.editor.facelets.FaceletsLibrary;
import org.netbeans.spi.editor.codegen.CodeGenerator;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.loaders.TemplateWizard;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author marekfukala
 */
public class InjectCompositeComponent {

    private static final int HINT_PRIORITY = 60; //magic number
    private static final String TEMPLATES_FOLDER = "JSF";   //NOI18N
    private static final String TEMPLATE_NAME = "out.xhtml";  //NOI18N
 
    public static void inject(Document document, int from, int to) {
	try {
	    FileObject fileObject = NbEditorUtilities.getFileObject(document);
	    Project project = FileOwnerQuery.getOwner(fileObject);

	    instantiateTemplate(project, fileObject, document, from, to);

	} catch (ParseException ex) {
	    Exceptions.printStackTrace(ex);
	} catch (DataObjectNotFoundException ex) {
	    Exceptions.printStackTrace(ex);
	} catch (IOException ex) {
	    Exceptions.printStackTrace(ex);
	} catch (BadLocationException ex) {
	    Exceptions.printStackTrace(ex);
	}
    }

    public static Hint getHint(final RuleContext context, final int from, final int to) {
	return new Hint(injectCCRule,
		NbBundle.getMessage(InjectCompositeComponent.class, "MSG_InjectCompositeComponentSelectionHintDescription"), //NOI18N
		context.parserResult.getSnapshot().getSource().getFileObject(),
		new OffsetRange(from, to),
		Collections.<HintFix>singletonList(new HintFix() {

	    public String getDescription() {
		return NbBundle.getMessage(InjectCompositeComponent.class, "MSG_InjectCompositeComponentSelectionHintDescription"); //NOI18N
	    }

	    public void implement() throws Exception {
		inject(context.doc, from, to);
	    }

	    public boolean isSafe() {
		return true;
	    }

	    public boolean isInteractive() {
		return true;
	    }
	}),
		HINT_PRIORITY);
    }

    private static void instantiateTemplate(Project project, FileObject file, final Document document, final int startOffset, final int endOffset) throws BadLocationException, DataObjectNotFoundException, IOException, ParseException {
	String selectedText = startOffset == endOffset ? null : document.getText(startOffset, endOffset - startOffset);

	TemplateWizard templateWizard = new TemplateWizard();
	templateWizard.putProperty("project", project); //NOI18N
	templateWizard.putProperty("selectedText", selectedText); //NOI18N
	templateWizard.setTitle("Insert Composite Component"); //NOI18N
	DataFolder templatesFolder = templateWizard.getTemplatesFolder();
	FileObject template = templatesFolder.getPrimaryFile().getFileObject(TEMPLATES_FOLDER + "/" + TEMPLATE_NAME);   //NOI18N
	DataObject templateDO;
	FileObject projectDir = project.getProjectDirectory();
	DataFolder targetFolder = DataFolder.findFolder(projectDir);

	final Logger logger = Logger.getLogger(InjectCompositeComponent.class.getSimpleName());
	final JsfSupport jsfs = JsfSupport.findFor(file);
	if (jsfs == null) {
	    logger.warning("Cannot find JsfSupport instance for file " + file.getPath()); //NOI18N
	    return;
	}

	final SnippetContext context = getSnippetContext(document, startOffset, endOffset, jsfs);
	if (!context.isValid()) {
	    templateWizard.putProperty("incorrectActionContext", true); //NOI18N
	}

	//get list of used declarations, which needs to be passed to the wizard
	Source source = Source.create(document);
	final AtomicReference<Map<String, String>> declaredPrefixes = new AtomicReference<Map<String, String>>();
	ParserManager.parse(Collections.singleton(source), new UserTask() {
	    @Override
	    public void run(ResultIterator resultIterator) throws Exception {
		ResultIterator ri = WebUtils.getResultIterator(resultIterator, HtmlKit.HTML_MIME_TYPE);
		if (ri != null) {
		    HtmlParserResult result = (HtmlParserResult) ri.getParserResult();
		    if(result != null) {
			declaredPrefixes.set(result.getNamespaces());
		    }
		}
	    }
	});
	templateWizard.putProperty("declaredPrefixes", declaredPrefixes.get()); //NOI18N

	templateDO = DataObject.find(template);
	Set<DataObject> result = templateWizard.instantiate(templateDO, targetFolder);
	final String prefix = (String)templateWizard.getProperty("selectedPrefix"); //NOI18N
	if (result != null && result.size() > 0) {
	    final String compName = result.iterator().next().getName();
	    //TODO XXX Replace selected text by created component in editor
	    FileObject tF = Templates.getTargetFolder(templateWizard);
	    final BaseDocument doc = (BaseDocument) document;
	    final Indent indent = Indent.get(doc);
	    indent.lock();
	    try {
		doc.runAtomic(new Runnable() {

		    public void run() {
			try {
			    doc.remove(startOffset, endOffset - startOffset);
			    String text = "<" + prefix + ":" + compName + "/>"; //NOI18N
			    doc.insertString(startOffset, text, null);
			    indent.reindent(startOffset, startOffset + text.length());
			} catch (BadLocationException ex) {
			    Exceptions.printStackTrace(ex);
			}
		    }
		});
	    } finally {
		indent.unlock();
	    }
	    String compFolder = tF.getPath();
	    compFolder = FileUtil.getRelativePath(projectDir, tF);
	    if (compFolder.endsWith(File.separator)) {
		compFolder = compFolder.substring(0, compFolder.lastIndexOf(File.separator));
	    }
	    compFolder = compFolder.substring(compFolder.lastIndexOf(File.separator) + 1);

	    //now we need to import the library if not already done,
	    //but since the library has just been created by adding an xhtml file
	    //to the resources/xxx/ folder we need to wait until the files
	    //get indexed and the library is created
	    final String compositeLibURL = JsfUtils.getCompositeLibraryURL(compFolder);
	    Source documentSource = Source.create(document);
	    ParserManager.parseWhenScanFinished(Collections.singletonList(documentSource), new UserTask() { //NOI18N

		@Override
		public void run(ResultIterator resultIterator) throws Exception {
		    FaceletsLibrary lib = jsfs.getFaceletsLibraries().get(compositeLibURL);
		    if (lib != null) {
			if (!JsfUtils.importLibrary(document, lib, prefix)) { //XXX: fix the damned static prefix !!!
			    logger.warning("Cannot import composite components library " + compositeLibURL); //NOI18N
			}
		    } else {
			//error
			logger.warning("Composite components library for uri " + compositeLibURL + " seems not to be created."); //NOI18N
		    }
		}
	    });

	    //now we need to import all the namespaces refered in the snipet
	    DataObject templateInstance = result.iterator().next();
	    EditorCookie ec = templateInstance.getCookie(EditorCookie.class);
	    final Document templateInstanceDoc = ec.openDocument();
	    ParserManager.parseWhenScanFinished(Collections.singletonList(documentSource), new UserTask() { //NOI18N

		@Override
		public void run(ResultIterator resultIterator) throws Exception {
		    final Map<FaceletsLibrary, String> importsMap = new LinkedHashMap<FaceletsLibrary, String>();
		    for (String uri : context.getDeclarations().keySet()) {
			String prefix = context.getDeclarations().get(uri);
			FaceletsLibrary lib = jsfs.getFaceletsLibraries().get(uri);
			if (lib != null) {
			    importsMap.put(lib, prefix);
			}
		    }
		    //do the import under atomic lock in different thread,
		    RequestProcessor.getDefault().post(new Runnable() {
			public void run() {
			    ((BaseDocument)templateInstanceDoc).runAtomic(new Runnable() {
				public void run() {
				    JsfUtils.importLibrary(templateInstanceDoc, importsMap);
				}
			    });
			}
		    });
		}
	    });
	    ec.saveDocument(); //save the template instance after imports
	}
    }

    private static SnippetContext getSnippetContext(Document doc, final int from, final int to, final JsfSupport jsfs) {
	final SnippetContext context = new SnippetContext();
	context.setValid(true);

	final Source source = Source.create(doc);
	try {
	    ParserManager.parse(Collections.singleton(source), new UserTask() {

		@Override
		public void run(ResultIterator resultIterator) throws Exception {
		    final HtmlParserResult result = (HtmlParserResult) JsfUtils.getEmbeddedParserResult(resultIterator, "text/html"); //NOI18N
		    if (result == null) {
			return;
		    }
		    final int astFrom = result.getSnapshot().getEmbeddedOffset(from);
		    final int astTo = result.getSnapshot().getEmbeddedOffset(to);

		    try {
			for (final String libUri : result.getNamespaces().keySet()) {
			    //is the declared uri a faceler library?
			    if (!jsfs.getFaceletsLibraries().containsKey(libUri)) {
				continue; //no facelets stuff, skip it
			    }

			    AstNode root = result.root(libUri);
			    AstNodeUtils.visitChildren(root, new AstNodeVisitor() {

				public void visit(AstNode node) {
				    int node_logical_from = node.logicalStartOffset();
				    int node_logical_to = node.logicalEndOffset();

				    if (node_logical_from >= astFrom && node_logical_from <= astTo
					    || node_logical_to >= astFrom && node_logical_to <= astTo) {
					//the node start or end is in the selection
					//such info is enough to add the node's namespace
					//to the list of future imports for the snippet
					context.addDeclaration(libUri, result.getNamespaces().get(libUri));
				    }

				    //todo: optimize me a bit please :-)

				    //the node must either contain both offsets or none
				    if ((astFrom > node_logical_from && astFrom < node_logical_to && !(astTo > node_logical_from && astTo < node_logical_to))
					    || (astTo > node_logical_from && astTo < node_logical_to && !(astFrom > node_logical_from && astFrom < node_logical_to))) {
					//crossing tags - quit
					fail();
				    }

				    //and the offset must not fall into the tag itself
				    AstNode closeTag = node.getMatchingTag();
				    if (closeTag == null) {
					//broken source, error
					if (!node.isEmpty()) {
					    fail();
					}
				    }
				    if (isInTagItself(node, astFrom) || isInTagItself(node, astTo)
					    || isInTagItself(closeTag, astFrom) || isInTagItself(closeTag, astTo)) {
					fail();
				    }

				}

				private boolean isInTagItself(AstNode node, int offset) {
				    return node != null && node.startOffset() < offset && node.endOffset() > offset;
				}

				private void fail() {
				    context.setValid(false);
				    throw new AstTreeVisitingBreakException();
				}
			    }, AstNode.NodeType.OPEN_TAG);
			}

		    } catch (AstTreeVisitingBreakException e) {
			//no-op, we just need to stop the visition once we find first problem
			context.setValid(false);
		    }
		}
	    });

	} catch (ParseException ex) {
	    Exceptions.printStackTrace(ex);
	}
	return context;
    }
    private static final Rule injectCCRule = new InjectCCSelectionRule();

    private static class AstTreeVisitingBreakException extends RuntimeException {
    };

    private static class SnippetContext {

	private boolean valid;
	private Map<String, String> relatedDeclarations = new HashMap<String, String>();

	public void setValid(boolean valid) {
	    this.valid = valid;
	}

	public void addDeclaration(String uri, String prefix) {
	    relatedDeclarations.put(uri, prefix);
	}

	/** uri2prefix map of related declarations */
	public Map<String, String> getDeclarations() {
	    return relatedDeclarations;
	}

	public boolean isValid() {
	    return valid;
	}
    }

    private static class InjectCCSelectionRule implements SelectionRule {

	public boolean appliesTo(RuleContext context) {
	    return true;
	}

	public String getDisplayName() {
	    return null;
	}

	public boolean showInTasklist() {
	    return false;
	}

	public HintSeverity getDefaultSeverity() {
	    return HintSeverity.CURRENT_LINE_WARNING; //???
	}
    }

    public static class InjectCCCodeGen implements CodeGenerator {

	public String getDisplayName() {
	    return NbBundle.getMessage(InjectCompositeComponent.class, "MSG_InjectCompositeComponentHint"); //NOI18N
	}

	public void invoke() {
	    JTextComponent textComponent = EditorRegistry.lastFocusedComponent();
	    Document doc = textComponent.getDocument();
	    int from = textComponent.getSelectionStart();
	    int to = textComponent.getSelectionEnd();

	    inject(doc, from, to);

	}
    }
}
