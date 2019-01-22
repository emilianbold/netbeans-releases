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

package org.netbeans.modules.versioning.system.cvss.ui.actions.annotate;

import org.netbeans.editor.*;
import org.netbeans.editor.Utilities;
import org.netbeans.api.editor.fold.*;
import org.netbeans.api.diff.*;
import org.netbeans.api.project.*;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.versioning.system.cvss.ui.actions.log.*;
import org.netbeans.modules.versioning.system.cvss.ui.actions.diff.*;
import org.netbeans.modules.versioning.system.cvss.ui.actions.update.GetCleanAction;
import org.netbeans.modules.versioning.system.cvss.util.*;
import org.netbeans.modules.versioning.system.cvss.util.Utils;
import org.netbeans.lib.cvsclient.command.annotate.*;
import org.netbeans.spi.diff.*;
import org.openide.*;
import org.openide.loaders.*;
import org.openide.filesystems.*;
import org.openide.text.*;
import org.openide.util.*;
import org.openide.xml.*;

import javax.swing.*;
import javax.swing.Timer;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.accessibility.Accessible;
import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.util.*;
import java.util.List;
import java.io.*;
import java.text.MessageFormat;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.settings.EditorStyleConstants;
import org.netbeans.api.editor.settings.FontColorNames;
import org.netbeans.api.editor.settings.FontColorSettings;
import org.netbeans.modules.editor.NbEditorUtilities;

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
 * 
 */
final class AnnotationBar extends JComponent implements Accessible, PropertyChangeListener, LogOutputListener, DocumentListener, ChangeListener, ActionListener, Runnable, ComponentListener {

    /**
     * Target text component for which the annotation bar is aiming.
     */
    private final JTextComponent textComponent;

    /**
     * User interface related to the target text component.
     */
    private final EditorUI editorUI;

    /**
     * Fold hierarchy of the text component user interface.
     */
    private final FoldHierarchy foldHierarchy;

    /** 
     * Document related to the target text component.
     */
    private final BaseDocument doc;

    /**
     * Caret of the target text component.
     */
    private Caret caret;

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
     *
     * @thread it is accesed from multiple threads all mutations
     * and iterations must be under elementAnnotations lock,
     */
    private Map elementAnnotations;

    /**
     * Maps revision number (strings) to raw commit
     * messages (strings).
     */
    private Map commitMessages;

    /**
     * Represents text that should be displayed in
     * visible bar with yet <code>null</code> elementAnnotations.
     */
    private String elementAnnotationsSubstitute;
    
    private Color backgroundColor = Color.WHITE;
    private Color foregroundColor = Color.BLACK;
    private Color selectedColor = Color.BLUE;

    /**
     * Most recent status message.
     */
    private String recentStatusMessage;
    
    /**
     * Revision associated with caret line.
     */
    private String recentRevision;
    
    /**
     * Request processor to create threads that may be cancelled.
     */
    RequestProcessor requestProcessor = null;
    
    /**
     * Latest annotation comment fetching task launched.
     */
    private RequestProcessor.Task latestAnnotationTask = null;

    /**
     * Rendering hints for annotations sidebar inherited from editor settings.
     */
    private final Map renderingHints;

    /**
     * Creates new instance initializing final fields.
     */
    public AnnotationBar(JTextComponent target) {
        this.textComponent = target;
        this.editorUI = Utilities.getEditorUI(target);
        this.foldHierarchy = FoldHierarchy.get(editorUI.getComponent());
        this.doc = editorUI.getDocument();
        if (textComponent instanceof JEditorPane) {
            String mimeType = NbEditorUtilities.getMimeType(textComponent);
            FontColorSettings fcs = MimeLookup.getLookup(mimeType).lookup(FontColorSettings.class);
            renderingHints = (Map) fcs.getFontColors(FontColorNames.DEFAULT_COLORING).getAttribute(EditorStyleConstants.RenderingHints);
        } else {
            renderingHints = null;
        }
        setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        elementAnnotationsSubstitute = "";                              //NOI18N
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
        ResourceBundle loc = NbBundle.getBundle(AnnotationBar.class);
        elementAnnotationsSubstitute = loc.getString("CTL_AnnotationSubstitute");

        doc.addDocumentListener(this);
        textComponent.addComponentListener(this);
        editorUI.addPropertyChangeListener(this);

        revalidate();  // resize the component
    }

    /**
     * Result computed show it...
     * Takes AnnotateLines and shows them.
     */
    public void annotationLines(File file, List annotateLines) {
        final List lines = new LinkedList(annotateLines);
        int lineCount = lines.size();
        /** 0 based line numbers => 1 based line numbers*/
        final int ann2editorPermutation[] = new int[lineCount];
        for (int i = 0; i< lineCount; i++) {
            ann2editorPermutation[i] = i+1;
        }

        DiffProvider diff = (DiffProvider) Lookup.getDefault().lookup(DiffProvider.class);
        if (diff != null) {
            Reader r = new LinesReader(lines);
            Reader docReader = org.netbeans.modules.versioning.util.Utils.getDocumentReader(doc);
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
                err.annotate(e, "Cannot compute local diff required for annotations, ignoring...");  // NOI18N
                err.notify(ErrorManager.INFORMATIONAL, e);
            }
        }

        doc.render(new Runnable() {
            public void run() {
                StyledDocument sd = (StyledDocument) doc;
                Iterator it = lines.iterator();
                elementAnnotations = Collections.synchronizedMap(new HashMap(lines.size()));
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
            }
        });

        // lazy listener registration
        caret = textComponent.getCaret();
        if (caret != null) {
            caret.addChangeListener(this);
        }
        textComponent.addPropertyChangeListener(this);
        this.caretTimer = new Timer(500, this);
        caretTimer.setRepeats(false);

        onCurrentLine();
        revalidate();
        repaint();
    }

    /**
     * Takes commit messages and shows them as tooltips.
     *
     * @param a hashmap containing commit messages
     */
    public void commitMessages(Map messages) {
        this.commitMessages = messages;
    }

    // implementation ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    /**
     * Gets a the file related to the document
     *
     * @return the file related to the document, <code>null</code> if none
     * exists.
     */
    private File getCurrentFile() {
        File result = null;
        
        DataObject dobj = (DataObject)doc.getProperty(Document.StreamDescriptionProperty);
        if (dobj != null) {
            FileObject fo = dobj.getPrimaryFile();
            result = FileUtil.toFile(fo);
        }
        
        return result;
    }

    private String getCommitMessage (AnnotateLine al, boolean escape) {
        StringBuilder annotation=new StringBuilder();
        if (commitMessages != null) {
            String message = (String) commitMessages.get(al.getRevision());
            if (message != null) {
                if (escape) {
                    String escaped = null;
                    try {
                        escaped = XMLUtil.toElementContent(message);
                    } catch (CharConversionException e1) {
                        ErrorManager err = ErrorManager.getDefault();
                        err.annotate(e1, "CVS.AB: can not HTML escape: " + message); // NOI18N
                        err.notify(ErrorManager.INFORMATIONAL, e1);
                    }
                    if (escaped != null) {
                        String lined = escaped.replaceAll(System.getProperty("line.separator"), "<br>");  // NOI18N
                        annotation.append(lined); // NOI18N
                    }
                } else {
                    annotation.append(message);
                }
            }
        }
        return annotation.toString();
    }
    /**
     *
     * @return
     */
    JTextComponent getTextComponent () {
        return textComponent;
    }
    
    /**
     * Registers "close" popup menu, tooltip manager
     * and repaint on documet change manager.
     */
    public void addNotify() {
        super.addNotify();


        this.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                maybeShowPopup(e);
            }

            public void mouseReleased(MouseEvent e) {
                maybeShowPopup(e);
            }

            private void maybeShowPopup(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    e.consume();
                    createPopup(e).show(e.getComponent(),
                               e.getX(), e.getY());
                } else if (e.getID() == MouseEvent.MOUSE_RELEASED && e.getButton() == MouseEvent.BUTTON1) {
                    e.consume();
                    showTooltipWindow(e);
                }
            }
        });

        // register with tooltip manager
        setToolTipText(""); // NOI18N

    }

    private JPopupMenu createPopup(MouseEvent e) {
        final ResourceBundle loc = NbBundle.getBundle(AnnotationBar.class);
        final JPopupMenu popupMenu = new JPopupMenu();
        final JMenuItem diffMenu = new JMenuItem(loc.getString("CTL_MenuItem_DiffToRevision"));
        diffMenu.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (recentRevision != null) {
                    String prevRevision = Utils.previousRevision(recentRevision);
                    if (prevRevision != null) {
                        File file = getCurrentFile();
                        if (file != null) {
                            DiffExecutor diffExecutor = new DiffExecutor(file.getName());
                            diffExecutor.showDiff(file, prevRevision, recentRevision);
                        }
                    }
                }
            }
        });
        popupMenu.add(diffMenu);

        final JMenuItem rollbackMenu = new JMenuItem(loc.getString("CTL_MenuItem_RollbackToRevision"));
        rollbackMenu.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                File file = getCurrentFile();
                GetCleanAction.rollback(file, recentRevision);
            }
        });
        popupMenu.add(rollbackMenu);

        // add action 'rollback to previous revision'
        final JMenuItem rollbackToPreviousMenu = new JMenuItem(loc.getString("CTL_MenuItem_RollbackToPreviousRevision"));
        rollbackToPreviousMenu.addActionListener(new ActionListener() {
            public void actionPerformed (ActionEvent e) {
                final String previousRevision = Utils.previousRevision(recentRevision);
                if (null != previousRevision) {
                    File file = getCurrentFile();
                    GetCleanAction.rollback(file, previousRevision);
                }
            }
        });
        popupMenu.add(rollbackToPreviousMenu);

        Project prj = Utils.getProject(getCurrentFile());
        if (prj != null) {
            String prjName = ProjectUtils.getInformation(prj).getDisplayName();
            JMenuItem menu = new JMenuItem(NbBundle.getMessage(AnnotationBar.class, "CTL_MenuItem_FindCommitInProject", prjName));
            menu.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    int line = getCurrentLine();
                    if (line == -1) return;
                    AnnotateLine al = getAnnotateLine(line);
                    if (al == null || commitMessages == null) return;
                    String message = (String) commitMessages.get(al.getRevision());
                    File file = getCurrentFile();
                    Project project = Utils.getProject(file);                
                    Context context = Utils.getProjectContext(project, file);
                    SearchHistoryAction.openSearch(
                            context, 
                            ProjectUtils.getInformation(project).getDisplayName(),
                            message, al.getAuthor(), al.getDate());
                }
            });
            popupMenu.add(menu);
        }

        JMenuItem menu = new JMenuItem(loc.getString("CTL_MenuItem_FindCommitInProjects"));
        menu.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int line = getCurrentLine();
                if (line == -1) return;
                AnnotateLine al = getAnnotateLine(line);
                if (al == null || commitMessages == null) return;
                String message = (String) commitMessages.get(al.getRevision());
                Project [] projects  = OpenProjects.getDefault().getOpenProjects();
                int n = projects.length;
                SearchHistoryAction.openSearch(
                        (n == 1) ? ProjectUtils.getInformation(projects[0]).getDisplayName() : 
                        NbBundle.getMessage(AnnotationBar.class, "CTL_FindAssociateChanges_OpenProjects_Title", Integer.toString(n)),
                        message, al.getAuthor(), al.getDate());
            }
        });
        popupMenu.add(menu);

        menu = new JMenuItem(loc.getString("CTL_MenuItem_CloseAnnotations"));
        menu.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                hideBar();
            }
        });
        popupMenu.addSeparator();
        popupMenu.add(menu);

        // dynamic labels an dvisibility

        diffMenu.setVisible(false);
        rollbackMenu.setVisible(false);
        rollbackToPreviousMenu.setVisible(false);
        if (recentRevision != null) {
            String prevRevision = Utils.previousRevision(recentRevision);
            if (prevRevision != null) {
                String format = loc.getString("CTL_MenuItem_DiffToRevision");
                diffMenu.setText(MessageFormat.format(format, new Object [] { recentRevision, prevRevision }));
                diffMenu.setVisible(true);
                
                String format2 = loc.getString("CTL_MenuItem_RollbackToPreviousRevision");
                rollbackToPreviousMenu.setText(MessageFormat.format(format2, new Object [] { prevRevision}));
                rollbackToPreviousMenu.setVisible(true);
                
            }
            String format = loc.getString("CTL_MenuItem_RollbackToRevision");
            rollbackMenu.setText(MessageFormat.format(format, new Object [] { recentRevision }));
            rollbackMenu.setVisible(true);
        }

        return popupMenu;
    }

    /**
     * Hides the annotation bar from user. 
     */
    void hideBar() {
        annotated = false;
        revalidate();
        release();
    }

    /**
     * Gets the line number of the caret's current position. The first line
     * will return a line number of 0 (zero). If it's impossible to determine
     * the caret's current line number -1 will be returned.
     *
     * @return the line number of the caret's current position
     */
    private int getCurrentLine() {
        int result = 0;
        
        int offset = caret.getDot();
        try {
            result = Utilities.getLineOffset(doc, offset);
        } catch (BadLocationException ex) {
            result = -1;
        }
        
        return result;
     }
    /**
     * Gets a request processor which is able to cancel tasks.
     */
    private RequestProcessor getRequestProcessor() {
        if (requestProcessor == null) {
            requestProcessor = new RequestProcessor("AnnotationBarRP", 1, true);  // NOI18N
        }
        
        return requestProcessor;
    }
    
    /**
     * Shows commit message in status bar and or revision change repaints side
     * bar (to highlight same revision). This process is started in a
     * seperate thread.
     */
    private void onCurrentLine() {
        if (latestAnnotationTask != null) {
            latestAnnotationTask.cancel();
        }
        
        if (isAnnotated()) {
            latestAnnotationTask = getRequestProcessor().post(this);
        }
    }

    // latestAnnotationTask business logic
    @Override
    public void run() {
        Caret carett = this.caret;
        if (carett == null || !isAnnotated()) {
            // closed in the meantime
            return;
        }
        // get resource bundle
        ResourceBundle loc = NbBundle.getBundle(AnnotationBar.class);
        // give status bar "wait" indication
        StatusBar statusBar = editorUI.getStatusBar();
        recentStatusMessage = loc.getString("CTL_StatusBar_WaitFetchAnnotation");
        statusBar.setText(StatusBar.CELL_MAIN, recentStatusMessage);
        
        // determine current line
        int line = -1;
        int offset = carett.getDot();
        try {
            line = Utilities.getLineOffset(doc, offset);
        } catch (BadLocationException ex) {
            ErrorManager err = ErrorManager.getDefault();
            err.annotate(ex, "Can not get line for caret at offset " + offset); // NOI18N
            err.notify(ex);
            clearRecentFeedback();
            return;
        }

        // handle locally modified lines
        AnnotateLine al = getAnnotateLine(line);
        if (al == null) {
            AnnotationMarkProvider amp = AnnotationMarkInstaller.getMarkProvider(textComponent);
            if (amp != null) {
                amp.setMarks(Collections.EMPTY_LIST);
            }
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

            AnnotationMarkProvider amp = AnnotationMarkInstaller.getMarkProvider(textComponent);
            if (amp != null) {
            
                List marks = new ArrayList(elementAnnotations.size());
                // I cannot affort to lock elementAnnotations for long time
                // it's accessed from editor thread too
                Iterator it2;
                synchronized(elementAnnotations) {
                    it2 = new HashSet(elementAnnotations.entrySet()).iterator();
                }
                while (it2.hasNext()) {
                    Map.Entry next = (Map.Entry) it2.next();                        
                    AnnotateLine annotateLine = (AnnotateLine) next.getValue();
                    if (revision.equals(annotateLine.getRevision())) {
                        Element element = (Element) next.getKey();
                        if (elementAnnotations.containsKey(element) == false) {
                            continue;
                        }
                        int elementOffset = element.getStartOffset();
                        int lineNumber = NbDocument.findLineNumber((StyledDocument)doc, elementOffset);
                        AnnotationMark mark = new AnnotationMark(lineNumber, revision);
                        marks.add(mark);
                    }

                    if (Thread.interrupted()) {
                        clearRecentFeedback();
                        return;
                    }
                }
                amp.setMarks(marks);
            }
        }

        if (commitMessages != null) {
            String message = (String) commitMessages.get(revision);
            if (message != null) {
                recentStatusMessage = message;
                statusBar.setText(StatusBar.CELL_MAIN, al.getAuthor() + ": " + recentStatusMessage); // NOI18N
            } else {
                clearRecentFeedback();
            }
        } else {
            clearRecentFeedback();
        };
    }
    
    /**
     * Clears the status bar if it contains the latest status message
     * displayed by this annotation bar.
     */
    private void clearRecentFeedback() {
        StatusBar statusBar = editorUI.getStatusBar();
        if (statusBar.getText(StatusBar.CELL_MAIN) == recentStatusMessage) {
            statusBar.setText(StatusBar.CELL_MAIN, "");  // NOI18N
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
        dim.height *=2;  // XXX
        return dim;
    }

    /**
     * Gets the preferred width of this component.
     *
     * @return the preferred width of this component
     */
    private int getBarWidth() {
        if (elementAnnotations == null) {
            char[] data = elementAnnotationsSubstitute.toCharArray();
            int w = getGraphics().getFontMetrics().charsWidth(data, 0, data.length);
            return w;
        } else {
            synchronized(elementAnnotations) {
                Iterator it = elementAnnotations.values().iterator();

                // collect all possible strings
                Set<String> allUniqueDisplayNames = new HashSet<String>();
                while (it.hasNext()) {
                    AnnotateLine line = (AnnotateLine) it.next();
                    String displayName = line.getRevision() + " " + line.getAuthor(); // NOI18N
                    allUniqueDisplayNames.add(displayName);
                }
                if (!allUniqueDisplayNames.isEmpty()) {
                    // calculate the largest width

                    // NOTE: texts with the same number of chars may have different widths in non-monospaced fonts
                    // for example: 'nr' and 'nm' - so we have to calculate the widest one
                    FontMetrics fontMetrics = getGraphics().getFontMetrics();
                    int maxWidth = -1;
                    for (String displayName : allUniqueDisplayNames) {
                        char[] data = displayName.toCharArray();
                        int w = fontMetrics.charsWidth(data, 0, data.length);
                        maxWidth = Math.max(maxWidth, w);
                    }
                    return maxWidth;
                } else {
                    return 0;
                }
            }
        }
    }
    /**
     * Pair method to {@link #annotate}. It releases
     * all resources.
     */
    private void release() {
        editorUI.removePropertyChangeListener(this);
        textComponent.removeComponentListener(this);
        textComponent.removePropertyChangeListener(this);
        doc.removeDocumentListener(this);
        if (caret != null) {
            caret.removeChangeListener(this);
        }
        if (caretTimer != null) {
            caretTimer.removeActionListener(this);
        }
        commitMessages = null;
        elementAnnotations = null;
        // cancel running annotation task if active
        if(latestAnnotationTask != null) {
            latestAnnotationTask.cancel();
        }
        AnnotationMarkProvider amp = AnnotationMarkInstaller.getMarkProvider(textComponent);
        if (amp != null) {
            amp.setMarks(Collections.EMPTY_LIST);
        }

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

        String annotation = "";  // NOI18N
        AnnotateLine al = null;
        if (elementAnnotations != null) {
            al = getAnnotateLine(line);
            if (al != null) {
                annotation = al.getRevision() + " " + al.getAuthor();  // NOI18N
            }
        } else {
            annotation = elementAnnotationsSubstitute;
        }

        if (al != null && al.getRevision().equals(recentRevision)) {
            g.setColor(selectedColor());
        } else {
            g.setColor(foregroundColor());
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

        StringBuilder annotation = new StringBuilder();
        if (elementAnnotations != null) {
            AnnotateLine al = getAnnotateLine(line);

            if (al != null) {
                String escapedAuthor = NbBundle.getMessage(AnnotationBar.class, "BK0001");
                try {
                    escapedAuthor = XMLUtil.toElementContent(al.getAuthor());
                } catch (CharConversionException e1) {
                    ErrorManager err = ErrorManager.getDefault();
                    err.annotate(e1, "CVS.AB: can not HTML escape: " + al.getAuthor());  // NOI18N
                    err.notify(ErrorManager.INFORMATIONAL, e1);
                }

                // always return unique string to avoid tooltip sharing on mouse move over same revisions -->
                annotation.append("<html><!-- line=" + line++ + " -->" + al.getRevision()  + " <b>" + escapedAuthor + "</b> " + al.getDateString()); // NOI18N
                annotation.append("<p>" + getCommitMessage(al, true));
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
                err.annotate(e, "CVS.AB: can not locate line annotation."); // NOI18N
                err.notify(ErrorManager.INFORMATIONAL, e);
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

        g.setColor(backgroundColor());
        g.fillRect(clip.x, clip.y, clip.width, clip.height);

        if (renderingHints != null) {
            ((Graphics2D) g).addRenderingHints(renderingHints);
        }
        
        AbstractDocument doc = (AbstractDocument)component.getDocument();
        doc.readLock();
        try{
            foldHierarchy.lock();
            try{
                int startPos = textUI.getPosFromY(clip.y);
                int startViewIndex = rootView.getViewIndex(startPos,Position.Bias.Forward);
                int rootViewCount = rootView.getViewCount();

                if (startViewIndex >= 0 && startViewIndex < rootViewCount) {
                    int clipEndY = clip.y + clip.height;
                    for (int i = startViewIndex; i < rootViewCount; i++){
                        View view = rootView.getView(i);
                        Rectangle rec = component.modelToView(view.getStartOffset());
                        if (rec == null) {
                            break;
                        }
                        int y = rec.y;
                        paintView(view, g, y);
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
    
    /**
     *
     * @param event
     */
    private void showTooltipWindow (MouseEvent event) {
        Point p = new Point(event.getPoint());
        SwingUtilities.convertPointToScreen(p, this);
        Point p2 = new Point(p);
        SwingUtilities.convertPointFromScreen(p2, textComponent);
        
        // annotation for target line
        AnnotateLine al = null;
        if (elementAnnotations != null) {
            al = getAnnotateLine(getLineFromMouseEvent(event));
        }

        /**
         * al.getCommitMessage() != null - since commit messages are initialized separately from the AL constructor
         */
        if (al != null && al.getRevision() != null) {
            String commitMessage = getCommitMessage(al, false);
            TooltipWindow ttw = new TooltipWindow(this, al, commitMessage);
            ttw.show(new Point(p.x - p2.x, p.y));
        }
    }


    private Color backgroundColor() {
        if (textComponent != null) {
            return textComponent.getBackground();
        }
        return backgroundColor;
    }

    private Color foregroundColor() {
        if (textComponent != null) {
            return textComponent.getForeground();
        }
        return foregroundColor;
    }

    private Color selectedColor() {
        if (backgroundColor.equals(backgroundColor())) {
            return selectedColor;
        }
        if (textComponent != null) {
            return textComponent.getCaretColor();
        }
        return selectedColor;

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

    /** Implementation */
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt == null) return;
        String id = evt.getPropertyName();
        if (evt.getSource() == textComponent) {
            if ("caret".equals(id)) {
                if (caret != null) {
                    caret.removeChangeListener(this);
                }
                caret = textComponent.getCaret();
                if (caret != null) {
                    caret.addChangeListener(this);
                }
            }
            return;
        }
        if (EditorUI.COMPONENT_PROPERTY.equals(id)) {  // NOI18N
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
            synchronized(elementAnnotations) { // atomic change
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
        }
        repaint();
    }

    /** Implementation */
    @Override
    public void removeUpdate(DocumentEvent e) {
        final int length = e.getDocument().getLength();
        Mutex.EVENT.readAccess(new Runnable() {
            @Override
            public void run () {
                if (length == 0) { // external reload
                    hideBar();
                }
                repaint();
            }
        });
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

    /** on JTextPane */
    public void componentHidden(ComponentEvent e) {
    }

    /** on JTextPane */
    public void componentMoved(ComponentEvent e) {
    }

    /** on JTextPane */
    public void componentResized(ComponentEvent e) {
        revalidate();
    }

    /** on JTextPane */
    public void componentShown(ComponentEvent e) {
    }

    boolean isAnnotated() {
        return annotated;
    }

}
