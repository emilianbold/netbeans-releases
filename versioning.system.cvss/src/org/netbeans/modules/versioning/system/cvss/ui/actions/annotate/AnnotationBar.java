/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.versioning.system.cvss.ui.actions.annotate;

import org.netbeans.editor.*;
import org.netbeans.editor.Utilities;
import org.netbeans.api.editor.fold.FoldHierarchy;
import org.netbeans.api.editor.fold.FoldHierarchyListener;
import org.netbeans.api.editor.fold.FoldHierarchyEvent;
import org.netbeans.api.diff.Difference;
import org.netbeans.api.xml.parsers.DocumentInputSource;
import org.netbeans.modules.versioning.system.cvss.ui.actions.log.LogOutputListener;
import org.netbeans.modules.versioning.system.cvss.ui.actions.diff.DiffExecutor;
import org.netbeans.lib.cvsclient.command.annotate.AnnotateLine;
import org.netbeans.spi.diff.DiffProvider;
import org.openide.ErrorManager;
import org.openide.cookies.LineCookie;
import org.openide.loaders.DataObject;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.text.Line;
import org.openide.util.Lookup;
import org.openide.text.NbDocument;
import org.openide.xml.XMLUtil;
import org.openide.text.Annotation;
import org.xml.sax.InputSource;

import javax.swing.*;
import javax.swing.Timer;
import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.util.*;
import java.util.List;
import java.io.CharConversionException;
import java.io.File;
import java.io.Reader;
import java.io.IOException;

/**
 * Represents annotation sidebar componnet in editor. It's
 * created by {@link AnnotationBarManager}.
 *
 * <p>It reponds to following external signals:
 * <ul>
 *   <li> {@link #annotate} message
 *   <li> {@link LogOutputListener} events
 * </ul>
 *
 * @author Petr Kuzel
 */
final class AnnotationBar extends JComponent implements FoldHierarchyListener, PropertyChangeListener, LogOutputListener, DocumentListener, ChangeListener, ActionListener {

    private final JTextComponent textComponent;

    private final EditorUI editorUI;

    private final FoldHierarchy foldHierarchy;

    private final BaseDocument doc;

    private final Caret caret;

    /**
     * Caret batch timer launched on receiving
     * annotation data structures (AnnotateLine).
     */
    private Timer caretTimer;

    /**
     * Controls annotation bar visibility.
     */
    private boolean annotated;

    /**
     * Maps document {@link Element}s (representing lines) to
     * {@link AnnotateLine}. <code>null</code> means that
     * no data are available, yet. So alternative
     * {@link #elementAnnotationsSubstitute} text shoudl be used.
     */
    private Map elementAnnotations;

    /**
     * Maps revision number (strings) to raw commit
     * messages (strings).
     */
    private Map commitMessages;

    private final Set errorStripeAnnotations = new HashSet();
    private int annotationsPerfomanceLimit = Integer.MAX_VALUE;

    /**
     * Represents text that should be displayed in
     * visible bar with yet <code>null</code> elementAnnotations.
     */
    private String elementAnnotationsSubstitute;

    private Color backgroundColor = Color.WHITE;
    private Color foregroundColor = Color.BLACK;
    private Color selectedColor = Color.BLUE;

    private String recentStatusMessage;

    /** Revision associted with caret line. */
    private String recentRevision;

    /**
     * Creates new instance initializing final fields.
     */
    public AnnotationBar(JTextComponent target) {
        this.textComponent = target;
        this.editorUI = Utilities.getEditorUI(target);
        this.foldHierarchy = FoldHierarchy.get(editorUI.getComponent());
        this.doc = editorUI.getDocument();
        this.caret = textComponent.getCaret();
    }

    // public contract ~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    /**
     * Makes the bar visible and sensitive to
     * LogOutoutListener events that should deliver
     * actual content to be displayed.
     */
    public void annotate() {
        annotated = true;
        elementAnnotations = null;
        commitMessages = null;
        elementAnnotationsSubstitute = "Computing...";
        revalidate();  // resize the component
    }

    /**
     * Result computed show it...
     * Takes AnnoateLines and shows them.
     */
    public void annotationLines(File file, List annotateLines) {

        // lazy listener registration

        caret.addChangeListener(this);
        this.caretTimer = new Timer(10, this);
        caretTimer.setRepeats(false);

        List lines = new LinkedList(annotateLines);
        int lineCount = lines.size();
        /** 0 based line numbers => 1 based line numbers*/
        int ann2editorPermutation[] = new int[lineCount];
        for (int i = 0; i< lineCount; i++) {
            ann2editorPermutation[i] = i+1;
        }

        DiffProvider diff = (DiffProvider) Lookup.getDefault().lookup(DiffProvider.class);
        if (diff != null) {
            Reader r = new LinesReader(lines);
            InputSource is = new DocumentInputSource(doc);
            Reader docReader = is.getCharacterStream();
            try {

                Difference[] differences = diff.computeDiff(r, docReader);

                // customize annotation line numbers to match different reality
                // compule line permutation

                for (int i = 0; i < differences.length; i++) {
                    Difference d = differences[i];
                    if (d.getType() == Difference.ADD) continue;

                    int editorStart;
                    int firstShift = d.getFirstEnd() - d.getFirstStart() +1;
                    if (d.getType() == Difference.CHANGE) {
                        int firstLen = d.getFirstEnd() - d.getFirstStart();
                        int secondLen = d.getSecondEnd() - d.getSecondStart();
                        if (secondLen >= firstLen) continue; // ADD or pure CHANGE
                        editorStart = d.getSecondStart();
                        firstShift = firstLen - secondLen;
                    } else {  // DELETE
                        editorStart = d.getSecondStart() + 1;
                    }

                    for (int c = editorStart + firstShift -1; c<lineCount; c++) {
                        ann2editorPermutation[c] -= firstShift;
                    }
                }

                for (int i = differences.length -1; i >= 0; i--) {
                    Difference d = differences[i];
                    if (d.getType() == Difference.DELETE) continue;

                    int firstStart;
                    int firstShift = d.getSecondEnd() - d.getSecondStart() +1;
                    if (d.getType() == Difference.CHANGE) {
                        int firstLen = d.getFirstEnd() - d.getFirstStart();
                        int secondLen = d.getSecondEnd() - d.getSecondStart();
                        if (secondLen <= firstLen) continue; // REMOVE or pure CHANGE
                        firstShift = secondLen - firstLen;
                        firstStart = d.getFirstStart();
                    } else {
                        firstStart = d.getFirstStart() + 1;
                    }

                    for (int k = firstStart-1; k<lineCount; k++) {
                        ann2editorPermutation[k] += firstShift;
                    }
                }

            } catch (IOException e) {
                ErrorManager err = ErrorManager.getDefault();
                err.annotate(e, "Cannot compute local diff required for annotations, ignoring...");
                err.notify(e);
            }
        }

        try {
            doc.atomicLock();
            StyledDocument sd = (StyledDocument) doc;
            Iterator it = lines.iterator();
            elementAnnotations = new HashMap(lines.size());
            while (it.hasNext()) {
                AnnotateLine line = (AnnotateLine) it.next();
                int lineNum = ann2editorPermutation[line.getLineNum() -1];
                try {
                    int lineOffset = NbDocument.findLineOffset(sd, lineNum -1);
                    Element element = sd.getParagraphElement(lineOffset);
                    elementAnnotations.put(element, line);
                } catch (IndexOutOfBoundsException ex) {
                    // TODO how could I get line behind document end?
                    // furtunately user does not spot it
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                }
            }
        } finally {
            doc.atomicUnlock();
        }
        onCurrentLine();
        revalidate();
        repaint();
    }

    /**
     * Takes commint messages and shows them as tooltips.
     */
    public void commitMessages(Map messages) {
        this.commitMessages = messages;
    }

    // implementation ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    /**
     * Registers "close" popup menu, tooltip manager
     * and repaint on documet change manager.
     */
    public void addNotify() {
        super.addNotify();
        final JPopupMenu popupMenu = new JPopupMenu();
        final JMenuItem diffMenu = new JMenuItem("Diff to Previous");
        diffMenu.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (recentRevision != null) {
                    String prevRevision = previousRevision(recentRevision);
                    if (prevRevision != null) {
                        DataObject dobj = (DataObject) doc.getProperty(Document.StreamDescriptionProperty);
                        if (dobj != null) {
                            FileObject fo = dobj.getPrimaryFile();
                            File file = FileUtil.toFile(fo);
                            if (file != null) {
                                DiffExecutor diffExecutor = new DiffExecutor("Diff to Previous");
                                diffExecutor.showDiff(file, prevRevision, recentRevision);
                            }
                        }
                    }
                }
            }
        });
        popupMenu.add(diffMenu);

        JMenuItem menu = new JMenuItem("Close Annotations");
        menu.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                annotated = false;
                revalidate();
                release();
            }
        });
        popupMenu.add(menu);

        this.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                maybeShowPopup(e);
            }

            public void mouseReleased(MouseEvent e) {
                maybeShowPopup(e);
            }

            private void maybeShowPopup(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    diffMenu.setVisible(false);
                    if (recentRevision != null) {
                        String prevRevision = previousRevision(recentRevision);
                        if (prevRevision != null) {
                            diffMenu.setText("Diff to " + prevRevision);
                            diffMenu.setVisible(true);
                        }
                    }
                    popupMenu.show(e.getComponent(),
                               e.getX(), e.getY());
                }
            }
        });

        // register with tooltip manager
        setToolTipText("");

        doc.addDocumentListener(this);
        foldHierarchy.addFoldHierarchyListener(this);
        editorUI.addPropertyChangeListener(this);
    }

    /**
     * Shows commit message in status bar
     * and or revision change repaints side bar
     * (to highlight same revision).
     *
     */
    private void onCurrentLine() {

        // determine current line

        int line = -1;
        int offset = caret.getDot();
        try {
            line = Utilities.getLineOffset(doc, offset);
        } catch (BadLocationException ex) {
            ErrorManager err = ErrorManager.getDefault();
            err.annotate(ex, "Can not get line for caret at offset " + offset); // NOI18N
            err.notify(ex);
            return;
        }

        // handle locally modified lines
        AnnotateLine al = getAnnotateLine(line);
        if (al == null) {
            detachStripeAnnotations();
            clearRecentFeedback();
            if (recentRevision != null) {
                recentRevision = null;
                repaint();
            }
            return;
        }

        // handle unchanged lines

        String revision = al.getRevision();
        if (revision.equals(recentRevision) == false) {
            recentRevision = revision;
            repaint();
            
            // error stripe support

            detachStripeAnnotations();

            DataObject dobj = (DataObject) doc.getProperty(Document.StreamDescriptionProperty);
            if (dobj != null) {
                LineCookie lineCookie = (LineCookie) dobj.getCookie(LineCookie.class);
                if (lineCookie != null) {
                    Set annotations = new HashSet();
                    Line.Set lines = lineCookie.getLineSet();
                    Iterator it2 = elementAnnotations.entrySet().iterator();
                    while (it2.hasNext()) {
                        Map.Entry next = (Map.Entry) it2.next();                        
                        AnnotateLine annotateLine = (AnnotateLine) next.getValue();
                        if (revision.equals(annotateLine.getRevision())) {
                            Element element = (Element) next.getKey();
                            int elementOffset = element.getStartOffset();
                            int lineNumber = NbDocument.findLineNumber((StyledDocument)doc, elementOffset);
                            Line currentLine = lines.getCurrent(lineNumber);
                            CvsAnnotation ann = new CvsAnnotation(revision, currentLine);
                            annotations.add(ann);
                        }
                    }

                    // do not trust annotations implementation scalability
                    // explictly monitor its performance and do not overload it
                    if (annotations.size() < annotationsPerfomanceLimit) {
                        long startTime = System.currentTimeMillis();
                        Iterator it = annotations.iterator();
                        int counter = 0;
                        while (it.hasNext()) {
                            CvsAnnotation cvsAnnotation = (CvsAnnotation) it.next();
                            cvsAnnotation.attach();
                            errorStripeAnnotations.add(cvsAnnotation);
                            counter++;
                            long ms = System.currentTimeMillis() - startTime;
                            if (ms > 703) {  // 0.7 sec
                                annotationsPerfomanceLimit = counter;
                                ErrorManager.getDefault().log(ErrorManager.WARNING, "#59721 should be reopened: " + counter + " of " + annotations.size() + " annotations attached in " + ms + "ms. Setting performance limit.");  // NOI18N
                                break;
                            }
                        }
                    }
                }
            }
        }

        if (commitMessages == null) return;
        String message = (String) commitMessages.get(revision);
        StatusBar statusBar = editorUI.getStatusBar();
        if (message != null) {
            recentStatusMessage = message;
            statusBar.setText(StatusBar.CELL_MAIN, al.getAuthor() + ": " + recentStatusMessage); // NOI18N
        } else {
            clearRecentFeedback();
        }

    }

    private void detachStripeAnnotations() {
        Iterator it = errorStripeAnnotations.iterator();
        while (it.hasNext()) {
            Annotation next = (Annotation) it.next();
            next.detach();
            it.remove();
        }
    }

    private void clearRecentFeedback() {
        StatusBar statusBar = editorUI.getStatusBar();
        if (statusBar.getText(StatusBar.CELL_MAIN) == recentStatusMessage) {
            statusBar.setText(StatusBar.CELL_MAIN, "");  // NOi18N
        }
    }

    /**
     * Components created by SibeBarFactory are positioned
     * using a Layout manager that determines componnet size
     * by retireving preferred size.
     *
     * <p>Once componnet needs resizing it simply calls
     * {@link #revalidate} that triggers new layouting
     * that consults prefered size.
     */
    public Dimension getPreferredSize() {
        Dimension dim = textComponent.getSize();
        int width = annotated ? getBarWidth() : 0;
        dim.width = width;
        return dim;
    }

    private int getBarWidth() {
        String longestString = "";  // NOI18N
        if (elementAnnotations == null) {
            longestString = elementAnnotationsSubstitute;
        } else {
            Iterator it = elementAnnotations.values().iterator();
            while (it.hasNext()) {
                AnnotateLine line = (AnnotateLine) it.next();
                if (line.getRevision().length() > longestString.length()) {
                    longestString = line.getRevision();
                }
            }
        }
        char[] data = longestString.toCharArray();
        int w = getGraphics().getFontMetrics().charsWidth(data, 0,  data.length);
        return w;
    }
    /**
     * Pair method to {@link #annotate}. It releases
     * all resources.
     */
    private void release() {
        editorUI.removePropertyChangeListener(this);
        foldHierarchy.removeFoldHierarchyListener(this);
        doc.removeDocumentListener(this);
        caret.removeChangeListener(this);
        if (caretTimer != null) {
            caretTimer.removeActionListener(this);
        }
        commitMessages = null;
        elementAnnotations = null;
        detachStripeAnnotations();
        annotationsPerfomanceLimit = Integer.MAX_VALUE;
        clearRecentFeedback();
    }

    /**
     * Paints one view that corresponds to a line (or
     * multiple lines if folding takes effect).
     */
    private void paintView(View view, Graphics g, int yBase) {
        JTextComponent component = editorUI.getComponent();
        if (component == null) return;
        BaseTextUI textUI = (BaseTextUI)component.getUI();

        Element rootElem = textUI.getRootView(component).getElement();
        int line = rootElem.getElementIndex(view.getStartOffset());

        String annotation = "";  // NOi18N
        if (elementAnnotations != null) {
            AnnotateLine al = getAnnotateLine(line);
            if (al != null) {
                annotation = al.getRevision();
            }
        } else {
            annotation = elementAnnotationsSubstitute;
        }

        if (annotation.equals(recentRevision)) {
            g.setColor(selectedColor);
        } else {
            g.setColor(foregroundColor);
        }
        g.drawString(annotation, 0, yBase + editorUI.getLineAscent());
    }

    /**
     * Presents commit message as tooltips.
     */
    public String getToolTipText (MouseEvent e) {
        if (editorUI == null)
            return null;
        int line = getLineFromMouseEvent(e);

        StringBuffer annotation = new StringBuffer();
        if (elementAnnotations != null) {
            AnnotateLine al = getAnnotateLine(line);

            if (al != null) {
                String escapedAuthor = "<unknown>";
                try {
                    escapedAuthor = XMLUtil.toElementContent(al.getAuthor());
                } catch (CharConversionException e1) {
                    ErrorManager err = ErrorManager.getDefault();
                    err.annotate(e1, "Can not HTML escape: " + al.getAuthor());
                    err.notify(e1);
                }

                // always return unique string to avoid tooltip sharing on mouse move over same revisions -->
                annotation.append("<html><!-- line=" + line++ + " -->" + al.getRevision()  + " <b>" + escapedAuthor + "</b> " + al.getDateString()); // NOI18N
                if (commitMessages != null) {
                    String message = (String) commitMessages.get(al.getRevision());
                    if (message != null) {
                        String escaped = null;
                        try {
                            escaped = XMLUtil.toElementContent(message);
                        } catch (CharConversionException e1) {
                            ErrorManager err = ErrorManager.getDefault();
                            err.annotate(e1, "Can not HTML escape: " + message);
                            err.notify(e1);
                        }
                        if (escaped != null) {
                            String lined = escaped.replaceAll(System.getProperty("line.separator"), "<br>");  // NOI18N
                            annotation.append("<p>" + lined); // NOI18N
                        }
                    }
                }
            }
        } else {
            annotation.append(elementAnnotationsSubstitute);
        }

        return annotation.toString();
    }

    /**
     * Locates AnnotateLine associated with given line. The
     * line is translated to Element that is used as map lookup key.
     * The map is initially filled up with Elements sampled on
     * annotate() method.
     *
     * <p>Key trick is that Element's identity is maintained
     * until line removal (and is restored on undo).
     *
     * @param line
     * @return found AnnotateLine or <code>null</code>
     */
    private AnnotateLine getAnnotateLine(int line) {
        StyledDocument sd = (StyledDocument) doc;
        int lineOffset = NbDocument.findLineOffset(sd, line);
        Element element = sd.getParagraphElement(lineOffset);
        AnnotateLine al = (AnnotateLine) elementAnnotations.get(element);

        if (al != null) {
            int startOffset = element.getStartOffset();
            int endOffset = element.getEndOffset();
            try {
                int len = endOffset - startOffset;
                String text = doc.getText(startOffset, len -1);
                String content = al.getContent();
                if (text.equals(content)) {
                    return al;
                }
            } catch (BadLocationException e) {
                ErrorManager err = ErrorManager.getDefault();
                err.annotate(e, "Can not locate line annotation.");
                err.notify(e);
            }
        }

        return null;
    }

    /**
     * GlyphGutter copy pasted bolerplate method.
     * It invokes {@link #paintView} that contains
     * actual business logic.
     */
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        Rectangle clip = g.getClipBounds();

        JTextComponent component = editorUI.getComponent();
        if (component == null) return;

        BaseTextUI textUI = (BaseTextUI)component.getUI();
        View rootView = Utilities.getDocumentView(component);
        if (rootView == null) return;

        g.setColor(backgroundColor);
        g.fillRect(clip.x, clip.y, clip.width, clip.height);

        AbstractDocument doc = (AbstractDocument)component.getDocument();
        doc.readLock();
        try{
            foldHierarchy.lock();
            try{
                int startPos = textUI.getPosFromY(clip.y);
                int startViewIndex = rootView.getViewIndex(startPos,Position.Bias.Forward);
                int rootViewCount = rootView.getViewCount();

                if (startViewIndex >= 0 && startViewIndex < rootViewCount) {
                    // find the nearest visible line with an annotation
                    Rectangle rec = textUI.modelToView(component, rootView.getView(startViewIndex).getStartOffset());
                    int y = (rec == null) ? 0 : rec.y;

                    int clipEndY = clip.y + clip.height;
                    for (int i = startViewIndex; i < rootViewCount; i++){
                        View view = rootView.getView(i);
                        paintView(view, g, y);
                        y += editorUI.getLineHeight();
                        if (y >= clipEndY) {
                            break;
                        }
                    }
                }

            } finally {
                foldHierarchy.unlock();
            }
        } catch (BadLocationException ble){
            ErrorManager.getDefault().notify(ble);
        } finally {
            doc.readUnlock();
        }
    }

    /** GlyphGutter copy pasted utility method. */
    private int getLineFromMouseEvent(MouseEvent e){
        int line = -1;
        if (editorUI != null) {
            try{
                JTextComponent component = editorUI.getComponent();
                BaseTextUI textUI = (BaseTextUI)component.getUI();
                int clickOffset = textUI.viewToModel(component, new Point(0, e.getY()));
                line = Utilities.getLineOffset(doc, clickOffset);
            }catch (BadLocationException ble){
            }
        }
        return line;
    }

    /**
     * Computes previous revision or <code>null</code>
     * for initial.
     *
     * @param revision num.dot revision or <code>null</code>
     */
    static String previousRevision(String revision) {
        if (revision == null) return null;
        String[] nums = revision.split("\\.");  // NOI18N
        assert (nums.length % 2) == 0 : "File revisions must consist from even tokens: " + revision; // NOI18N

        // eliminate branches
        int lastIndex = nums.length -1;
        while (lastIndex>1 && "1".equals(nums[lastIndex])) { // NOI18N
            lastIndex -= 2;
        }
        if (lastIndex <= 0) {
            return null;
        } else if (lastIndex == 1 && "1".equals(nums[lastIndex])) { // NOI18N
            return null;
        } else {
            int rev = Integer.parseInt(nums[lastIndex]);
            rev--;
            StringBuffer sb = new StringBuffer(nums[0]);
            for (int i = 1; i<lastIndex; i++) {
                sb.append(".").append(nums[i]); // NOI18N
            }
            sb.append(".").append("" + rev);  // NOI18N
            return sb.toString();
        }
    }

    /** Implementation */
    public void foldHierarchyChanged(FoldHierarchyEvent event) {
        repaint();
    }

    /** Implementation */
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt != null && EditorUI.COMPONENT_PROPERTY.equals(evt.getPropertyName())) {
            if (evt.getNewValue() == null){
                // component deinstalled, lets uninstall all isteners
                release();
            }
        }

    }

    /** Implementation */
    public void changedUpdate(DocumentEvent e) {
    }

    /** Implementation */
    public void insertUpdate(DocumentEvent e) {
        // handle new lines,  Enter hit at end of line changes
        // the line element instance
        // XXX Actually NB document implementation triggers this method two times
        //  - first time with one removed and two added lines
        //  - second time with two removed and two added lines
        if (elementAnnotations != null) {
            Element[] elements = e.getDocument().getRootElements();
            for (int i = 0; i < elements.length; i++) {
                Element element = elements[i];
                DocumentEvent.ElementChange change = e.getChange(element);
                if (change == null) continue;
                Element[] removed = change.getChildrenRemoved();
                Element[] added = change.getChildrenAdded();
                if (removed.length == added.length) {
                    for (int c = 0; c<removed.length; c++) {
                        Object recent = elementAnnotations.get(removed[c]);
                        if (recent != null) {
                            elementAnnotations.remove(removed[c]);
                            elementAnnotations.put(added[c], recent);
                        }
                    }
                } else if (removed.length == 1 && added.length > 0) {
                    Element key = removed[0];
                    Object recent = elementAnnotations.get(key);
                    if (recent != null) {
                        elementAnnotations.remove(key);
                        elementAnnotations.put(added[0], recent);
                    }
                }

            }
        }
        repaint();
    }

    /** Implementation */
    public void removeUpdate(DocumentEvent e) {
        repaint();
    }

    /** Caret */
    public void stateChanged(ChangeEvent e) {
        assert e.getSource() == caret;
        caretTimer.restart();
    }

    /** Timer */
    public void actionPerformed(ActionEvent e) {
        assert e.getSource() == caretTimer;
        onCurrentLine();
    }

    private static class CvsAnnotation extends Annotation {
        
        private final String text;

        private Line line;

        public CvsAnnotation(String tooltip, Line line) {
            text = tooltip;
            this.line = line;
        }

        public void attach() {
            attach(line);
            line = null;
        }

        public String getShortDescription() {
            return text;
        }

        public String getAnnotationType() {
            return "org-netbeans-modules-versioning-system-cvss-Annotation";  // NOI18N
        }
        
    }

}
