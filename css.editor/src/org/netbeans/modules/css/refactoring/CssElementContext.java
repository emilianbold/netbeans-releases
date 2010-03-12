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

import java.util.Collection;
import javax.swing.text.Document;
import org.netbeans.modules.css.gsf.api.CssParserResult;
import org.netbeans.modules.css.parser.CssParserTreeConstants;
import org.netbeans.modules.css.parser.SimpleNode;
import org.netbeans.modules.css.parser.SimpleNodeUtil;
import org.openide.filesystems.FileObject;

/**
 *
 * @author marekfukala
 */
public abstract class CssElementContext {

    public abstract boolean isRefactoringAllowed();

    public abstract String getElementName();

    public abstract FileObject getFileObject();

    public static abstract class AbstractFileContext extends CssElementContext {

	private FileObject fo;

	public AbstractFileContext(FileObject fo) {
	    this.fo = fo;
	}

	@Override
	public FileObject getFileObject() {
	    return fo;
	}

	@Override
	public String getElementName() {
	    return getFileObject().getName();
	}
    }

    public static class Folder extends AbstractFileContext {

	public Folder(FileObject folder) {
	    super(folder);
	}

	@Override
	public boolean isRefactoringAllowed() {
	    return true;
	}
    }

    public static class File extends AbstractFileContext {

	private Collection<CssParserResult> results;

	public File(FileObject fileObject, Collection<CssParserResult> result) {
	    super(fileObject);
	    this.results = result;
	}

	public Collection<CssParserResult> getParserResults() {
	    return results;
	}

	@Override
	public boolean isRefactoringAllowed() {
	    return true;
	}
    }

    public static class Editor extends CssElementContext {

	private int caretOffset;
	private int selectionFrom, selectionTo;
	private SimpleNode element;
	private CssParserResult result;

	public Editor(CssParserResult result, int caretOffset, int selectionFrom, int selectionTo) {
	    this.result = result;
	    this.caretOffset = caretOffset;
	    this.selectionFrom = selectionFrom;
	    this.selectionTo = selectionTo;
	    this.element = findCurrentElement();

	    assert element != null; //at least the root node should always be found
	}

	//XXX make it only caret position sensitive for now
	private SimpleNode findCurrentElement() {
	    SimpleNode root = getParserResult().root();
	    int astOffset = getParserResult().getSnapshot().getEmbeddedOffset(caretOffset);
	    return SimpleNodeUtil.findDescendant(root, astOffset);
	}

        public Document getDocument() {
            return result.getSnapshot().getSource().getDocument(false);
        }

	public CssParserResult getParserResult() {
	    return result;
	}

	@Override
	public FileObject getFileObject() {
	    return getParserResult().getSnapshot().getSource().getFileObject();
	}

	public int getCaret() {
	    return caretOffset;
	}

	public int getSelectionFrom() {
	    return selectionFrom;
	}

	public int getSelectionTo() {
	    return selectionTo;
	}

	public SimpleNode getElement() {
	    return element;
	}

        public SimpleNode getSimpleSelectorElement() {
            return  SimpleNodeUtil.getAncestorByType(getElement(), CssParserTreeConstants.JJTSIMPLESELECTOR);
        }

	@Override
	public String getElementName() {
	    return getElement().image().trim();
	}

	@Override
	public boolean isRefactoringAllowed() {
            //class, id or element selector
            //hex color
	    return null != getSimpleSelectorElement() || getElement().kind() == CssParserTreeConstants.JJTHEXCOLOR;
	}
    }
}
