/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.api.codemodel.query;

import java.awt.Container;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.logging.Logger;
import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;
import javax.swing.text.StyledDocument;
import org.netbeans.api.editor.document.LineDocument;
import org.netbeans.api.editor.document.LineDocumentUtils;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.LanguagePath;
//import org.netbeans.editor.JumpList;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.modules.cnd.api.codemodel.CMCursor;
import org.netbeans.modules.cnd.api.codemodel.CMFile;
import org.netbeans.modules.cnd.api.codemodel.CMLocation;
import org.netbeans.modules.cnd.api.codemodel.CMSourceLocation;
import org.netbeans.modules.cnd.api.codemodel.CMSourceRange;
import org.netbeans.modules.cnd.api.codemodel.CMTranslationUnit;
import org.netbeans.modules.cnd.api.codemodel.visit.CMEntity;
import org.netbeans.modules.cnd.api.codemodel.visit.CMReference;
import org.netbeans.modules.cnd.api.project.NativeFileItem;
import org.netbeans.modules.cnd.api.project.NativeFileItemSet;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.netbeans.modules.cnd.codemodel.bridge.spi.FileObjectRedirector;
import org.netbeans.modules.cnd.spi.codemodel.support.SPIUtilities;
import org.netbeans.modules.cnd.spi.codemodel.trace.CMTraceUtils;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser;
import org.openide.awt.StatusDisplayer;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.CloneableEditorSupport;
import org.openide.text.NbDocument;
import org.openide.text.PositionBounds;
import org.openide.text.PositionRef;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.util.UserQuestionException;
import org.openide.windows.TopComponent;

/**
 *
 * @author Vladimir Kvashin
 */
public class CMUtilities {

    private static final RequestProcessor RP = new RequestProcessor(CMUtilities.class.getName(), 1);

    private static final Logger LOGGER = Logger.getLogger("org.netbeans.modules.cnd.codemodel.bridge.Logger"); //NOI18N

    public static Logger getLogger() {
        return LOGGER;
    }

    public static URI getURI(Document doc) {
        FileObject fo = getFileObject(doc);
        return (fo == null) ? null : fo.toURI();
    }

    public static FileObject getFileObject(Document doc) {
        FileObject fo = (FileObject)doc.getProperty(FileObject.class);
        if(fo == null) {
            DataObject dobj = NbEditorUtilities.getDataObject(doc);
            if (dobj != null) {
                fo = dobj.getPrimaryFile();
            } else {
//                CMFile csmFile = getCMFile(doc, false, false);
//                if (csmFile != null) {
//                    fo = getFileObject(csmFile);
//                }
            }
        }
        return fo;
    }

    public static JEditorPane findRecentEditorPaneInEQ(final EditorCookie ec) {
        assert ec != null;
        final JEditorPane[] panes = {null};
        if (SwingUtilities.isEventDispatchThread()) {
            panes[0] = NbDocument.findRecentEditorPane(ec);
        } else {
            try {
                SwingUtilities.invokeAndWait(new Runnable() {

                    @Override
                    public void run() {
                        panes[0] = NbDocument.findRecentEditorPane(ec);
                    }
                });
            } catch (InterruptedException | InvocationTargetException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return panes[0];
    }

    public static CMFile getCMFile(JTextComponent comp, boolean waitParsing, boolean snapShot) {
        return comp == null ? null : getCMFile(comp.getDocument(), waitParsing, snapShot);
    }

    public static CMFile getCMFile(Document bDoc, boolean waitParsing, boolean snapShot) {
        CMFile cmFile = null;
        if (bDoc != null) {
            try {
                cmFile = (CMFile) bDoc.getProperty(CMFile.class);
                if (cmFile == null) {
                    cmFile = getCMFile(NbEditorUtilities.getDataObject(bDoc), waitParsing, snapShot);
                }
                if (cmFile == null) {
                    String mimeType = DocumentUtilities.getMimeType(bDoc);
                    if ("text/x-dialog-binding".equals(mimeType)) { // NOI18N
                        // this is context from dialog
                        InputAttributes inputAttributes = (InputAttributes) bDoc.getProperty(InputAttributes.class);
                        if (inputAttributes != null) {
                            LanguagePath path = LanguagePath.get(MimeLookup.getLookup(mimeType).lookup(Language.class));
                            FileObject fileObject = (FileObject) inputAttributes.getValue(path, "dialogBinding.fileObject"); //NOI18N
                            cmFile = CMUtilities.getCMFile(fileObject, waitParsing, snapShot);
                            if (cmFile == null) {
                                Document d = (Document) inputAttributes.getValue(path, "dialogBinding.document"); //NOI18N
                                cmFile = d == null ? null : CMUtilities.getCMFile(d, waitParsing, snapShot);
                            }
                        }
                    }
                }
            } catch (NullPointerException exc) {
                exc.printStackTrace(System.err);
            }
        }
        return cmFile;
    }

    public static CMFile getCMFile(FileObject fobj, boolean waitParsing, boolean snapShot) {
        CMTranslationUnit tu = getTranslationUnit(fobj, waitParsing, snapShot);
        return (tu == null) ? null : tu.getFile(fobj.toURI());
    }    
    
    public static interface ModelTask {
        
        void run(Collection<CMTranslationUnit> units);
        
    }    
    
    public static void submitModelTask(URI uri, ModelTask task) throws ParseException {
        submitModelTask(uri, 0, task);
    }    
    
    public static void submitModelTask(URI uri, int offset, ModelTask task) throws ParseException {
        try {
            submitModelTask(URLMapper.findFileObject(uri.toURL()), offset, task);
        } catch (MalformedURLException ex) {
            Exceptions.printStackTrace(ex);
        }  
    }    
    
    public static void submitModelTask(FileObject fobj, ModelTask task) throws ParseException {
        submitModelTask(fobj, 0, task);
    }    
    
    public static void submitModelTask(FileObject fobj, int offset, ModelTask task) throws ParseException {
        Source source = Source.create(fobj);
        ParserTask parserTask = new ParserTask(offset, task);
        ParserManager.parse(Collections.singletonList(source), parserTask);
    }
    
    public static void submitModelTask(Document document, ModelTask task) throws ParseException {
        submitModelTask(document, 0, task);
    }
    
    public static void submitModelTask(Document document, int offset, ModelTask task) throws ParseException {
        Source source = Source.create(document);
        ParserTask parserTask = new ParserTask(offset, task);
        ParserManager.parse(Collections.singletonList(source), parserTask);       
    }    
    
    private static class ParserTask extends UserTask {
        
        private final int lastOffset;
        
        private final ModelTask userTask;
        
//        private Collection<CMTranslationUnit> translationUnits;

        public ParserTask(int lastOffset, ModelTask userTask) {
            this.lastOffset = lastOffset;
            this.userTask = userTask;
        }

        @Override
        public void run(ResultIterator resultIterator) throws Exception {
            Parser.Result result = resultIterator.getParserResult(lastOffset);
            if (result instanceof CndParserResult) {
                final CndParserResult cndResult = (CndParserResult)result;
                userTask.run(cndResult.getTranslationUnits());
            }
        }
    }    
    
    public static void getTranslationUnits(FileObject fobj, boolean waitParsing, boolean snapShot) {
        URI uri = fobj.toURI();
        SPIUtilities.getTranslationUnits(uri);
    }    
    
    public static CMTranslationUnit getTranslationUnit(Document doc, boolean waitParsing, boolean snapShot) {
        FileObject fo = getFileObject(doc);
        return (fo == null) ? null :getTranslationUnit(fo, waitParsing, snapShot);
    }

    public static CMTranslationUnit getTranslationUnit(FileObject fobj, boolean waitParsing, boolean snapShot) {
        URI uri = fobj.toURI();
        Collection<CMTranslationUnit> translationUnits = SPIUtilities.getTranslationUnits(uri);
        Iterator<CMTranslationUnit> it = translationUnits.iterator();
        return it.hasNext() ? it.next() : null;
    }

    public static CMFile getCMFile(DataObject dobj, boolean waitParsing, boolean snapShot) {
        return getCMFile(dobj.getPrimaryFile(), waitParsing, snapShot); //TODO: implement properly!
    }

    public static StyledDocument openDocument(CloneableEditorSupport ces) {
        if (ces == null) {
            return null;
        }
        StyledDocument document = null;
        try {
            try {
                document = ces.openDocument();
            } catch (UserQuestionException e) {
                e.confirmed();
                document = ces.openDocument();
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return document;
    }

    public static CloneableEditorSupport findCloneableEditorSupport(CMReference ref) {
        return findCloneableEditorSupport(ref.getRange());
    }

    public static CloneableEditorSupport findCloneableEditorSupport(CMSourceRange cursor) {
        return findCloneableEditorSupport(cursor.getStart());
    }

    public static CloneableEditorSupport findCloneableEditorSupport(CMSourceLocation loc) {
        return findCloneableEditorSupport(loc.getFile());
    }

    public static CloneableEditorSupport findCloneableEditorSupport(CMCursor cursor) {
        return (cursor == null) ? null : findCloneableEditorSupport(cursor.getLocation());
    }

    public static CloneableEditorSupport findCloneableEditorSupport(CMFile csmFile) {
        DataObject dob = getDataObject(csmFile);
        return findDataObjectCloneableEditorSupport(dob);
    }

    public static CloneableEditorSupport findDataObjectCloneableEditorSupport(DataObject dob) {
        if (dob == null) {
            return null;
        }
        Object obj = dob.getLookup().lookup(org.openide.cookies.OpenCookie.class);
        if (obj instanceof CloneableEditorSupport) {
            return (CloneableEditorSupport) obj;
        }
        obj = dob.getLookup().lookup(org.openide.cookies.EditorCookie.class);
        if (obj instanceof CloneableEditorSupport) {
            return (CloneableEditorSupport) obj;
        }
        return null;
    }

    public static DataObject getDataObject(CMSourceRange range) {
        return getDataObject(range.getStart());
    }
    public static DataObject getDataObject(CMSourceLocation loc) {
        return getDataObject(loc.getFile());
    }
    public static DataObject getDataObject(CMFile csmFile) {
        return getDataObject(getFileObject(csmFile));
    }

    public static DataObject getDataObject(FileObject fo) {
        DataObject dob = null;
        if (fo != null) {
            try {
                dob = DataObject.find(fo);
            } catch (DataObjectNotFoundException e) {
            }
        }
        return dob;
    }

    public static FileObject getFileObject(CMSourceRange range) {
        return range.isDummy() ? null : getFileObject(range.getStart());
    }

    public static FileObject getFileObject(CMSourceLocation loc) {
        return loc.isValid() ? getFileObject(loc.getFile()) : null;
    }

    public static FileObject getFileObject(CMReference ref) {
        return getFileObject(ref.getRange());
    }

    public static FileObject getFileObject(CMCursor cursor) {
        return cursor.isDummy() ? null : getFileObject(cursor.getLocation().getFile());
    }

    public static FileObject getFileObject(CMFile file) {
        if (file == null) {
            return null;
        }
        try {
            return URLMapper.findFileObject(file.getURI().toURL());
        } catch (MalformedURLException ex) {
            Exceptions.printStackTrace(ex);
            return null;
        }
    }

    public static StyledDocument getDocument(FileObject fo) {
        if (fo != null && fo.isValid()) {
            try {
                DataObject dob = DataObject.find(fo);
                if (dob != null && dob.isValid()) {
                    EditorCookie ec = dob.getLookup().lookup(EditorCookie.class);
                    if (ec != null) {
                        return ec.getDocument();
                    }
                }
            } catch (IOException ex) {
                // file can be removed or became invalid
                // we catch IOException, because FileStateInvalidException is IOException
                // but is not declared to be thrown from DataObject.find
                if (fo.isValid() && !fo.isVirtual()) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
        return null;
    }

    public static PositionBounds createPositionBounds(CMSourceRange sourceRange) {
        CloneableEditorSupport ces = findCloneableEditorSupport(sourceRange);
        if (ces != null) {
            PositionRef beg = ces.createPositionRef(sourceRange.getStart().getOffset(), Position.Bias.Forward);
            PositionRef end = ces.createPositionRef(sourceRange.getEnd().getOffset(), Position.Bias.Backward);
            return new PositionBounds(beg, end);
        }
        return null;
    }

    public static PositionBounds createPositionBounds(CMReference ref) {
        return (ref == null) ? null : createPositionBounds(ref.getRange());
    }

    public static PositionBounds createPositionBounds(CMCursor cursor) {
        return (cursor == null) ? null : createPositionBounds(cursor.getExtent());
    }

    public static boolean openSource(CMCursor cursor) {
        return openSource(cursor.getExtent().getStart());
    }

    public static boolean openSource(CMEntity entity) {
        return openSource(entity.getCanonical().getLocation());
    }

    public static boolean openSource(CMSourceRange range) {
        return openSource(range.getStart());
    }

    public static boolean openSource(CMSourceLocation loc) {
        if (loc.isValid()) {
            FileObject fo = getFileObject(loc);
            if (fo != null) {
                return openSource(fo, loc.getLine(), loc.getColumn()-1);
            } else {
                CndUtils.assertTrueInConsole(false, "no file for ", CMTraceUtils.toString(loc));
            }
        }
        return false;
    }

    public static boolean openSource(FileObject fo, int line, int column) {
        DataObject dob;
        try {
            dob = DataObject.find(fo);
            return openAtElement(dob, new PointOrOffsetable(line, column));
        } catch (DataObjectNotFoundException ex) {
            return false;
        }
    }

    public static boolean openSource(FileObject fo, int offset) {
        DataObject dob;
        try {
            dob = DataObject.find(fo);
            return openAtElement(dob, new PointOrOffsetable(offset));
        } catch (DataObjectNotFoundException ex) {
            return false;
        }
    }
    
    private static DataObject redirect(DataObject dob) {
        if (dob == null) {
            return null;
        }
        Collection<? extends FileObjectRedirector> redirectors = Lookup.getDefault().lookupAll(FileObjectRedirector.class);
        for (FileObjectRedirector redirector : redirectors) {
            DataObject newDO = redirector.redirect(dob);
            if(newDO != null) {
                dob = newDO;
            }
        }
        return dob;
    }

    private static boolean openAtElement(final DataObject orig, final PointOrOffsetable element) {
        final DataObject dob = redirect(orig);
        if (dob != null) {
            final EditorCookie.Observable ec = dob.getLookup().lookup(EditorCookie.Observable.class);
            if (ec != null) {
                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
//                        JumpList.checkAddEntry();
                        JEditorPane pane = findRecentEditorPaneInEQ(ec);
                        if (pane != null) {
                            //editor already opened, so just select
                            selectElementInPane(pane, element, false);
                        } else {
                            // editor not yet opened, attach listener and open from there
                            ec.addPropertyChangeListener(new PropertyChangeListenerImpl(ec, element));
                            ec.open();
                        }
                    }

                    class PropertyChangeListenerImpl implements PropertyChangeListener, Runnable {

                        private final EditorCookie.Observable ec;
                        private final PointOrOffsetable element;
                        private boolean detach = false;

                        public PropertyChangeListenerImpl(EditorCookie.Observable ec, PointOrOffsetable element) {
                            this.ec = ec;
                            this.element = element;
                        }

                        @Override
                        public void propertyChange(PropertyChangeEvent evt) {
                            if (EditorCookie.Observable.PROP_OPENED_PANES.equals(evt.getPropertyName())) {
                                // IZ#199820 - 10 sec freezing while hyperlink to non UTF-8 include files
                                // we need event with already opened pane, unfortunately evt always have old/new params as null
                                // so we check if pane is opened ourselves
                                JEditorPane pane = findRecentEditorPaneInEQ(ec);
                                // we use detach, because user can answer "No" and we'd like to detach listener anyway
                                if (pane != null || detach) {
                                    ec.removePropertyChangeListener(this);
                                }
                                if (pane != null) {
                                    // redirect to jump on position after showing document content
                                    SwingUtilities.invokeLater(this);
                                } else if (detach) {
                                    // last try hack due to bug with PROP_OPENED_PANES (IZ#202242)
                                    SwingUtilities.invokeLater(this);
                                }
                                detach = true;
                            }
                        }

                        @Override
                        public void run() {
                            // do not use getOpenedPanes, because it holds AWT lock which
                            // prevents to show UserQuestionException based dialogs
                            // use non-blocking findRecentEditorPane instead
                            JEditorPane pane = findRecentEditorPaneInEQ(ec);
                            if (pane != null) {
                                selectElementInPane(pane, element, false);
                            }
                        }
                    }
                });
            }
            return true;
        }
        return false;
    }

    /** Jumps to element in given editor pane. When delayProcessing is
     * specified, waits for real visible open before jump
     */
    private static void selectElementInPane(final JEditorPane pane, final PointOrOffsetable element, boolean delayProcessing) {
        //final Cursor editCursor = pane.getCursor();
        //pane.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            // immediate processing
        RP.post(new Runnable() {

            @Override
            public void run() {
                jumpToElement(pane, element);
            }
        });
        // try to activate outer TopComponent
        Container temp = pane;
        while (temp != null && !(temp instanceof TopComponent)) {
            temp = temp.getParent();
        }
        if (temp instanceof TopComponent) {
            ((TopComponent) temp).open();
            ((TopComponent) temp).requestActive();
            ((TopComponent) temp).requestVisible();
        }
    }

    private static void jumpToElement(JEditorPane pane, PointOrOffsetable pointOrOffsetable) {
        //start = jumpLineStart ? lineToPosition(pane, element.getStartPosition().getLine()-1) : element.getStartOffset();
        int start;
        Offsetable element = pointOrOffsetable.getOffsetable();
        Point point = pointOrOffsetable.getPoint();
        if (element == null) {
            start = LineDocumentUtils.getLineStartFromIndex((LineDocument) pane.getDocument(), point.line - 1);
            start += point.column;
        } else {
            start = element.getOffset();
        }
        if (pane.getDocument() != null && start >= 0 && start < pane.getDocument().getLength()) {
            pane.setCaretPosition(start);
        }
        StatusDisplayer.getDefault().setStatusText(""); // NOI18N
    }


    private static class Point {

        public final int line;
        public final int column;

        public Point(int line, int column) {
            this.line = line;
            this.column = column;
        }
    }

    private static interface Offsetable {
        int getOffset();
    }

    private static class PointOrOffsetable {

        private final Object content;

        public PointOrOffsetable(Point point) {
            content = point;
        }

        public PointOrOffsetable(final CMCursor cursor) {
            content = new Offsetable() {
                @Override
                public int getOffset() {
                    return cursor.getExtent().getStart().getOffset();
                }
            };
        }

        public PointOrOffsetable(final int line, final int column) {
            content = new Point(line, column);
        }
        
        public PointOrOffsetable(final int offset) {
            content = new Offsetable() {
                @Override
                public int getOffset() {
                    return offset;
                }
            };
        }

        public Point getPoint() {
            return (content instanceof Point) ? (Point) content : null;
        }

        public Offsetable getOffsetable() {
            return (content instanceof Offsetable) ? (Offsetable) content : null;
        }

        @Override
        public String toString() {
            if (content instanceof Point) {
                Point point = (Point) content;
                return String.format("[%d:%d]", point.line, point.column); // NOI18N
            } else {
                return String.format("[%d]", ((Offsetable) content).getOffset()); // NOI18N
            }
        }
    }

    public static int getOffset(CMLocation loc) {
        if (loc != null) {
            return loc.getOffset();
        }
        return 0;
    }

    public static int getStartOffset(CMReference ref) {
        if (ref != null) {
            CMSourceRange extent = ref.getRange();
            if (extent != null) {
                return getOffset(extent.getStart());
            }
        }
        return 0;
    }

    public static int getEndOffset(CMReference ref) {
        if (ref != null) {
            CMSourceRange extent = ref.getRange();
            if (extent != null) {
                return getOffset(extent.getStart());
            }
        }
        return 0;
    }

    public static Collection<NativeProject> getNativeProjects(DataObject dobj) {
        Collection<NativeProject> out = new ArrayList<>();
        if (dobj != null && dobj.isValid()) {
            NativeFileItemSet set = dobj.getLookup().lookup(NativeFileItemSet.class);
            if (set != null && !set.isEmpty()) {
                for (NativeFileItem item : set.getItems()) {
                    out.add(item.getNativeProject());
                }
            }
        }
        return out;
    }

    public static Collection<NativeProject> getNativeProjects(FileObject dobj) {
        try {
            return getNativeProjects(DataObject.find(dobj));
        } catch (DataObjectNotFoundException ex) {
            ex.printStackTrace();
            return Collections.emptyList();
        }
    }
    
    public static String getElementJumpName(CMCursor element) {
        String text = "";
        if (element != null) {
            text += element.getDisplayName();
            if (text.length() > 0) {
                text = "\"" + text + "\""; // NOI18N

            }
        }
        return text;
    }    
}
