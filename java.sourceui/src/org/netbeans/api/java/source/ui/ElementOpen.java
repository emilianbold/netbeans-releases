/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.api.java.source.ui;

import com.sun.source.tree.*;
import com.sun.source.util.TreePathScanner;
import org.netbeans.api.java.source.*;
import org.netbeans.modules.java.BinaryElementOpen;
import org.netbeans.modules.parsing.api.indexing.IndexingManager;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.LineCookie;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.text.Line;
import org.openide.text.Line.ShowOpenType;
import org.openide.text.Line.ShowVisibilityType;
import org.openide.text.NbDocument;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

import javax.lang.model.element.Element;
import javax.swing.text.StyledDocument;
import java.io.IOException;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;

/** Utility class for opening elements in editor.
 *
 * @author Jan Lahoda
 */
public final class ElementOpen {
    private static Logger log = Logger.getLogger(ElementOpen.class.getName());

    private ElementOpen() {
    }
    
    /**
     * Opens {@link Element} corresponding to the given {@link ElementHandle}.
     * 
     * @param cpInfo ClasspathInfo which should be used for the search
     * @param el     declaration to open
     * @return true  if and only if the declaration was correctly opened,
     *                false otherwise
     * @since 1.5
     */
    public static boolean open(final ClasspathInfo cpInfo, final ElementHandle<? extends Element> el) {
        FileObject fo = SourceUtils.getFile(el, cpInfo);
        Object[] openInfo = fo != null ? getOpenInfo(fo, el) : null;
        if (openInfo != null) {
            assert openInfo[0] instanceof FileObject;
            assert openInfo[1] instanceof Integer;
            return doOpen((FileObject) openInfo[0], (Integer) openInfo[1]);
        }

        BinaryElementOpen beo = Lookup.getDefault().lookup(BinaryElementOpen.class);

        if (beo != null) {
            return beo.open(cpInfo, el);
        } else {
            return false;
        }        
    }
    
    /**
     * Opens given {@link Element}.
     * 
     * @param cpInfo ClasspathInfo which should be used for the search
     * @param el    declaration to open
     * @return true if and only if the declaration was correctly opened,
     *                false otherwise
     */
    public static boolean open(final ClasspathInfo cpInfo, final Element el) {
        return open(cpInfo, ElementHandle.create(el));
    }
    
    /**
     * Opens given {@link Element}.
     * 
     * @param toSearch fileobject whose {@link ClasspathInfo} will be used
     * @param toOpen   {@link ElementHandle} of the element which should be opened.
     * @return true if and only if the declaration was correctly opened,
     *                false otherwise
     */
    public static boolean open(final FileObject toSearch, final ElementHandle<? extends Element> toOpen) {
        if (toSearch == null || toOpen == null) {
            throw new IllegalArgumentException("null not supported");
        }
        
        Object[] openInfo = getOpenInfo (toSearch, toOpen);
        if (openInfo != null) {
            assert openInfo[0] instanceof FileObject;
            assert openInfo[1] instanceof Integer;
            return doOpen((FileObject)openInfo[0],(Integer)openInfo[1]);
        }
        
        BinaryElementOpen beo = Lookup.getDefault().lookup(BinaryElementOpen.class);

        if (beo != null) {
            return beo.open(ClasspathInfo.create(toSearch), toOpen);
        } else {
            return false;
        }
    }
    
    
    // Private methods ---------------------------------------------------------
        
    private static Object[] getOpenInfo(final FileObject fo, final ElementHandle<? extends Element> handle) {
        assert fo != null;
        
        try {
            int offset = getOffset(fo, handle);
            return new Object[] {fo, offset};
        } catch (IOException e) {
            Exceptions.printStackTrace(e);
            return null;
        }
    }

                    
    private static boolean doOpen(FileObject fo, int offset) {
        try {
            DataObject od = DataObject.find(fo);
            EditorCookie ec = od.getCookie(org.openide.cookies.EditorCookie.class);
            LineCookie lc = od.getCookie(org.openide.cookies.LineCookie.class);
            
            if (ec != null && lc != null && offset != -1) {                
                StyledDocument doc = ec.openDocument();                
                if (doc != null) {
                    int line = NbDocument.findLineNumber(doc, offset);
                    int lineOffset = NbDocument.findLineOffset(doc, line);
                    int column = offset - lineOffset;
                    
                    if (line != -1) {
                        Line l = lc.getLineSet().getCurrent(line);
                        
                        if (l != null) {
                            doShow(l, column);
                            return true;
                        }
                    }
                }
            }
            
            OpenCookie oc = od.getCookie(org.openide.cookies.OpenCookie.class);
            
            if (oc != null) {
                oc.open();                
                return true;
            }
        } catch (IOException e) {
            Exceptions.printStackTrace(e);
        }
        
        return false;
    }
    
    private static void doShow(final Line l, final int column) {
        if (SwingUtilities.isEventDispatchThread()) {
            l.show(ShowOpenType.OPEN, ShowVisibilityType.FOCUS, column);
        } else {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    l.show(ShowOpenType.OPEN, ShowVisibilityType.FOCUS, column);
                }
            });
        }
    }

    private static int getOffset(FileObject fo, final ElementHandle<? extends Element> handle) throws IOException {
        if (IndexingManager.getDefault().isIndexing()) {
            log.info("Skipping location of element offset within file, Scannig in progress");
            return 0; //we are opening @ 0 position. Fix #160478
        }

        final int[]  result = new int[] {-1};
        
        JavaSource js = JavaSource.forFileObject(fo);
        if (js != null) {
            js.runUserActionTask(new Task<CompilationController>() {
                public void run(CompilationController info) {
                    try {
                        info.toPhase(JavaSource.Phase.RESOLVED);
                    } catch (IOException ioe) {
                        Exceptions.printStackTrace(ioe);
                    }
                    Element el = handle.resolve(info);                
                    if (el == null) {
                        log.severe("Cannot resolve " + handle + ". " + info.getClasspathInfo());
                        return;
                    }

                    FindDeclarationVisitor v = new FindDeclarationVisitor(el, info);

                    CompilationUnitTree cu = info.getCompilationUnit();

                    v.scan(cu, null);                
                    Tree elTree = v.declTree;

                    if (elTree != null)
                        result[0] = (int)info.getTrees().getSourcePositions().getStartPosition(cu, elTree);
                }
            },true);
        }
        return result[0];
    }
    
    // Private innerclasses ----------------------------------------------------
    
    private static class FindDeclarationVisitor extends TreePathScanner<Void, Void> {
        
        private Element element;
        private Tree declTree;
        private CompilationInfo info;
        
        public FindDeclarationVisitor(Element element, CompilationInfo info) {
            this.element = element;
            this.info = info;
        }
        
	@Override
        public Void visitClass(ClassTree tree, Void d) {
            handleDeclaration();
            super.visitClass(tree, d);
            return null;
        }
        
	@Override
        public Void visitMethod(MethodTree tree, Void d) {
            handleDeclaration();
            super.visitMethod(tree, d);
            return null;
        }
        
	@Override
        public Void visitVariable(VariableTree tree, Void d) {
            handleDeclaration();
            super.visitVariable(tree, d);
            return null;
        }
    
        public void handleDeclaration() {
            Element found = info.getTrees().getElement(getCurrentPath());
            
            if ( element.equals( found ) ) {
                declTree = getCurrentPath().getLeaf();
            }
        }
    
    }
    
}
