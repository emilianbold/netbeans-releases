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
package org.netbeans.modules.debugger.jpda.actions;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.Location;
import com.sun.jdi.ReferenceType;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.StyleConstants;

import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;
import org.netbeans.api.editor.settings.AttributesUtilities;
import org.netbeans.api.editor.settings.EditorStyleConstants;
import org.netbeans.editor.BaseCaret;
import org.netbeans.editor.Coloring;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.debugger.jpda.ExpressionPool.Expression;
import org.netbeans.spi.debugger.jpda.EditorContext.Operation;
import org.netbeans.spi.editor.highlighting.HighlightAttributeValue;
import org.netbeans.spi.editor.highlighting.support.OffsetsBag;
import org.netbeans.modules.debugger.jpda.EditorContextBridge;

import org.netbeans.modules.debugger.jpda.jdi.ClassNotPreparedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.InternalExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ReferenceTypeWrapper;
import org.netbeans.modules.debugger.jpda.jdi.VMDisconnectedExceptionWrapper;
import org.netbeans.spi.debugger.jpda.EditorContext;
import org.openide.ErrorManager;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.Annotation;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

public class MethodChooser implements KeyListener, MouseListener,
        MouseMotionListener, PropertyChangeListener, FocusListener {

    private static AttributeSet defaultHyperlinkHighlight;
    
    private JPDADebuggerImpl debugger;
    private JPDAThread currentThread;
    private String url;
    private ReferenceType clazzRef;
    private int methodLine;
    private int methodOffset;
    
    private AttributeSet attribsLeft = null;
    private AttributeSet attribsRight = null;
    private AttributeSet attribsMiddle = null;
    private AttributeSet attribsAll = null;
    
    private AttributeSet attribsArea = null;
    private AttributeSet attribsMethod = null;
    private AttributeSet attribsHyperlink = null;

    private Cursor handCursor;
    private Cursor arrowCursor;
    private Cursor originalCursor;
    
    private JEditorPane editorPane;
    private Document doc;

    private int startLine;
    private int endLine;
    private Operation[] operations;
    private Location[] locations;
    private ArrayList<Annotation> annotations;
    private boolean performAction = false;
    private int selectedIndex = -1;
    private int mousedIndex = -1;
    private ActionListener releaseListener;
    private boolean isInSelectMode = false;

    MethodChooser(JPDADebuggerImpl debugger, String url, ReferenceType clazz, int methodLine, int methodOffset) {
        this.debugger = debugger;
        this.currentThread = debugger.getCurrentThread();
        this.url = url;
        this.clazzRef = clazz;
        //this.methodLine = methodLine; [TODO]
        this.methodOffset = methodOffset;
        
        //Operation currOp = currentThread.getCurrentOperation();
        //List<Operation> lastOps = currentThread.getLastOperations();
        //Operation lastOp = lastOps != null && lastOps.size() > 0 ? lastOps.get(lastOps.size() -1) : null;
    }

    public void setReleaseListener(ActionListener releaseListener) {
        this.releaseListener = releaseListener;
    }

    public static OffsetsBag getHighlightsBag(Document doc) {
        OffsetsBag bag = (OffsetsBag) doc.getProperty(MethodChooser.class);
        if (bag == null) {
            doc.putProperty(MethodChooser.class, bag = new OffsetsBag(doc, true));
        }
        return bag;
    }
    
    public boolean run() {
        DataObject dobj = getDataObject(url);
        final EditorCookie ec = (EditorCookie) dobj.getCookie(EditorCookie.class);
        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                    JEditorPane[] openedPanes = ec.getOpenedPanes();
                    if (openedPanes != null) {
                        editorPane = openedPanes[0];
                    }
                }
            });
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        } catch (InvocationTargetException ex) {
            Exceptions.printStackTrace(ex);
        }
        if (editorPane == null) {
            return false;  // Can not do anything without editor
        }
        doc = editorPane.getDocument();
        
        if (debugger.getState() == JPDADebugger.STATE_DISCONNECTED) {
            return false;
        }
        
        boolean selectionIsFinal = collectOperations();
        if (selectedIndex == -1) {
            // [TODO] perform classical Step Into
            return false;
        }
        if (selectionIsFinal || operations.length == 1) {
            // perform action directly
            String name = operations[selectedIndex].getMethodName();
            boolean success = RunIntoMethodActionProvider.doAction(debugger, name, locations[selectedIndex], true);
            return success;
        }
        // continue by showing method selection ui
        
        // hack - disable org.netbeans.modules.debugger.jpda.projects.ToolTipAnnotation
        System.setProperty("org.netbeans.modules.debugger.jpda.doNotShowTooltips", "true"); // NOI18N
        debugger.addPropertyChangeListener(this);
        debugger.getThreadsCollector().addPropertyChangeListener(this);
        editorPane.putClientProperty(MethodChooser.class, this);
        editorPane.addKeyListener(this);
        editorPane.addMouseListener(this);
        editorPane.addMouseMotionListener(this);
        editorPane.addFocusListener(this);
        originalCursor = editorPane.getCursor();
        handCursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
        arrowCursor = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
        editorPane.setCursor(arrowCursor);
        Caret caret = editorPane.getCaret();
        if (caret instanceof BaseCaret) {
            ((BaseCaret)caret).setVisible(false);
        }
        annotateLines();
        requestRepaint();
        Coloring coloring = new Coloring(null, 0, null, Color.CYAN);
        Utilities.setStatusText(editorPane, " " + NbBundle.getMessage(
                MethodChooser.class, "MSG_RunIntoMethod_Status_Line_Help"), coloring);
        isInSelectMode = true;
        return true;
    }

    private synchronized void release() {
        debugger.removePropertyChangeListener(this);
        debugger.getThreadsCollector().removePropertyChangeListener(this);
        getHighlightsBag(doc).clear();
        editorPane.removeKeyListener(this);
        editorPane.removeMouseListener(this);
        editorPane.removeMouseMotionListener(this);
        editorPane.removeFocusListener(this);
        editorPane.putClientProperty(MethodChooser.class, null);
        editorPane.setCursor(originalCursor);
        Caret caret = editorPane.getCaret();
        if (caret instanceof BaseCaret) {
            ((BaseCaret)caret).setVisible(true);
        }
        clearAnnotations();
        // hack - enable org.netbeans.modules.debugger.jpda.projects.ToolTipAnnotation
        System.clearProperty("org.netbeans.modules.debugger.jpda.doNotShowTooltips"); // NOI18N
        Utilities.clearStatusText(editorPane);
        
        if (performAction) {
            performAction = false;
            final String name = operations[selectedIndex].getMethodName();
            debugger.getRequestProcessor().post(new Runnable() {
                public void run() {
                    RunIntoMethodActionProvider.doAction(debugger, name, locations[selectedIndex], true);
                }
            });
        }
        if (releaseListener != null) {
            releaseListener.actionPerformed(null);
            releaseListener = null;
        }
        isInSelectMode = false;
    }

    boolean isInSelectMode() {
        return isInSelectMode;
    }

    void doStepIntoCurrentSelection() {
        performAction = true;
        release();
    }
    
    private DataObject getDataObject(String url) {
        FileObject file;
        try {
            file = URLMapper.findFileObject(new URL(url));
        } catch (MalformedURLException e) {
            return null;
        }
        if (file == null) {
            return null;
        }
        try {
            return DataObject.find(file);
        } catch (DataObjectNotFoundException ex) {
            return null;
        }
    }

    private boolean collectOperations() {
        methodLine = currentThread.getLineNumber(null); // [TODO]
        
        List<Location> locs = java.util.Collections.emptyList();
        try {
            while (methodLine > 0 && (locs = ReferenceTypeWrapper.locationsOfLine(clazzRef, methodLine)).isEmpty()) {
                methodLine--;
            }
        } catch (InternalExceptionWrapper aiex) {
        } catch (VMDisconnectedExceptionWrapper aiex) {
        } catch (ClassNotPreparedExceptionWrapper aiex) {
        } catch (AbsentInformationException aiex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, aiex);
        }
        if (locs.isEmpty()) {
            return false;
        }
        Expression expr = debugger.getExpressionPool().getExpressionAt(locs.get(0), url);
        if (expr == null) {
            return false;
        }
        Operation currOp = currentThread.getCurrentOperation();
        List<Operation> lastOpsList = currentThread.getLastOperations();
        Operation lastOp = lastOpsList != null && lastOpsList.size() > 0 ? lastOpsList.get(lastOpsList.size() - 1) : null;
        Operation selectedOp = null;
        Operation[] tempOps = expr.getOperations();
        if (tempOps.length == 0) {
            return false;
        }
        Location[] tempLocs = expr.getLocations();
        operations = new Operation[tempOps.length];
        locations = new Location[tempOps.length];
        for (int x = 0; x < tempOps.length; x++) {
            operations[x] = tempOps[x];
            locations[x] = tempLocs[x];
        }
        startLine = operations[0].getMethodStartPosition().getLine();
        endLine = operations[operations.length - 1].getMethodEndPosition().getLine();
        for (int i = 1; i < (operations.length - 1); i++) {
            int line = operations[i].getMethodStartPosition().getLine();
            if (line < startLine) {
                startLine = line;
            }
            if (line > endLine) {
                endLine = line;
            }
        }

        int currOpIndex = -1;
        int lastOpIndex = -1;

        if (currOp != null) {
            int index = currOp.getBytecodeIndex();
            for (int x = 0; x < operations.length; x++) {
                if (operations[x].getBytecodeIndex() == index) {
                    currOpIndex = x;
                    break;
                }
            }
        }
        if (lastOp != null) {
            int index = lastOp.getBytecodeIndex();
            for (int x = 0; x < operations.length; x++) {
                if (operations[x].getBytecodeIndex() == index) {
                    lastOpIndex = x;
                    break;
                }
            }
        }

        if (currOpIndex == -1) {
            selectedOp = operations[operations.length - 1];
        } else if (currOpIndex == lastOpIndex) {
            tempOps = new Operation[operations.length - 1 - currOpIndex];
            tempLocs = new Location[operations.length - 1 - currOpIndex];
            for (int x = 0; x < tempOps.length; x++) {
                tempOps[x] = operations[x + currOpIndex + 1];
                tempLocs[x] = locations[x + currOpIndex + 1];
            }
            operations = tempOps;
            locations = tempLocs;
            if (operations.length == 0) {
                return false;
            }
            selectedOp = operations[0];
        } else {
            selectedIndex = currOpIndex;
            // do not show UI, continue directly using the selection
            return true;
        }

        Object[][] elems = new Object[operations.length][2];
        for (int i = 0; i < operations.length; i++) {
            elems[i][0] = operations[i];
            elems[i][1] = locations[i];
        }
        Arrays.sort(elems, new OperatorsComparator());
        selectedIndex = 0;
        for (int i = 0; i < operations.length; i++) {
            operations[i] = (Operation)elems[i][0];
            locations[i] = (Location)elems[i][1];
            if (operations[i].equals(selectedOp)) {
                selectedIndex = i;
            }
        }
        return false;
    }

    private void requestRepaint() {
        if (attribsLeft == null) {
            Color foreground = Color.BLACK;

            attribsLeft = createAttribs(EditorStyleConstants.LeftBorderLineColor, foreground, EditorStyleConstants.TopBorderLineColor, foreground, EditorStyleConstants.BottomBorderLineColor, foreground);
            attribsRight = createAttribs(EditorStyleConstants.RightBorderLineColor, foreground, EditorStyleConstants.TopBorderLineColor, foreground, EditorStyleConstants.BottomBorderLineColor, foreground);
            attribsMiddle = createAttribs(EditorStyleConstants.TopBorderLineColor, foreground, EditorStyleConstants.BottomBorderLineColor, foreground);
            attribsAll = createAttribs(EditorStyleConstants.LeftBorderLineColor, foreground, EditorStyleConstants.RightBorderLineColor, foreground, EditorStyleConstants.TopBorderLineColor, foreground, EditorStyleConstants.BottomBorderLineColor, foreground);

            attribsHyperlink = getHyperlinkHighlight();
            
            attribsMethod = createAttribs(StyleConstants.Foreground, Color.BLACK,
                    StyleConstants.Bold, Boolean.TRUE);
            
            attribsArea = createAttribs(
                    StyleConstants.Foreground, Color.BLACK,
                    StyleConstants.Italic, Boolean.FALSE,
                    StyleConstants.Bold, Boolean.FALSE);
        }
        
        OffsetsBag newBag = new OffsetsBag(doc, true);
        int start = operations[0].getStartPosition().getOffset();
        int end = operations[operations.length - 1].getEndPosition().getOffset();
        newBag.addHighlight(start, end, attribsArea);
        
        for (int i = 0; i < operations.length; i++) {
            int startOffset = operations[i].getMethodStartPosition().getOffset();
            int endOffset = operations[i].getMethodEndPosition().getOffset();
            newBag.addHighlight(startOffset, endOffset, attribsMethod);
            if (selectedIndex == i) {
                int size = endOffset - startOffset;
                if (size == 1) {
                    newBag.addHighlight(startOffset, endOffset, attribsAll);
                } else if (size > 1) {
                    newBag.addHighlight(startOffset, startOffset + 1, attribsLeft);
                    newBag.addHighlight(endOffset - 1, endOffset, attribsRight);
                    if (size > 2) {
                        newBag.addHighlight(startOffset + 1, endOffset - 1, attribsMiddle);
                    }
                }
            }
            if (mousedIndex == i) {
                AttributeSet attr = AttributesUtilities.createComposite(
                    attribsHyperlink,
                    AttributesUtilities.createImmutable(EditorStyleConstants.Tooltip, new TooltipResolver())
                );
                newBag.addHighlight(startOffset, endOffset, attr);
            }
        }
        
        OffsetsBag bag = getHighlightsBag(doc);
        bag.setHighlights(newBag);
    }

    private void annotateLines() {
        annotations = new ArrayList<Annotation>();
        EditorContext context = EditorContextBridge.getContext();
        JPDAThread thread = debugger.getCurrentThread();
        Operation currOp = thread.getCurrentOperation();
        int currentLine = currOp != null ? currOp.getStartPosition().getLine() : thread.getLineNumber(null);
        String annoType = currOp != null ?
            EditorContext.CURRENT_EXPRESSION_CURRENT_LINE_ANNOTATION_TYPE :
            EditorContext.CURRENT_LINE_ANNOTATION_TYPE;
        for (int lineNum = startLine; lineNum <= endLine; lineNum++) {
            if (lineNum != currentLine) {
                Object anno = context.annotate(url, lineNum, annoType, null);
                if (anno instanceof Annotation) {
                    annotations.add((Annotation)anno);
                }
            } // if
        } // for
    }
    
    private void clearAnnotations() {
        if (annotations != null) {
            for (Annotation anno : annotations) {
                anno.detach();
            }
        }
    }
    
    private AttributeSet createAttribs(Object... keyValuePairs) {
        List<Object> list = new ArrayList<Object>();
        for (int i = keyValuePairs.length / 2 - 1; i >= 0; i--) {
            Object attrKey = keyValuePairs[2 * i];
            Object attrValue = keyValuePairs[2 * i + 1];

            if (attrKey != null && attrValue != null) {
                list.add(attrKey);
                list.add(attrValue);
            }
        }
        return AttributesUtilities.createImmutable(list.toArray());
    }

    private AttributeSet getHyperlinkHighlight() {
        //FontColorSettings fcs = MimeLookup.getLookup(MimePath.EMPTY).lookup(FontColorSettings.class);
        //AttributeSet hyperlinksHighlight = fcs.getFontColors("hyperlinks"); //NOI18N
        synchronized(this) {
            if (defaultHyperlinkHighlight == null) {
                defaultHyperlinkHighlight = AttributesUtilities.createImmutable(
                        StyleConstants.Foreground, Color.BLUE, StyleConstants.Underline, Color.BLUE);
            }
        }
        return defaultHyperlinkHighlight;
    }
    
    // **************************************************************************
    // KeyListener implementation
    // **************************************************************************
    
    public void keyTyped(KeyEvent e) {
        e.consume();
    }

    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();
        boolean consumeEvent = true;
        switch (code) {
            case KeyEvent.VK_ENTER:
            case KeyEvent.VK_SPACE:
            case KeyEvent.VK_F7: // [TODO]
                if (e.isControlDown() || e.isShiftDown()) {
                    release();
                    consumeEvent = false;
                } else {
                    // selection confirmed
                    performAction = true;
                    release();
                }
                break;
            case KeyEvent.VK_F8: // [TODO]
                // step over
                release();
                consumeEvent = false;
                break;
            case KeyEvent.VK_ESCAPE:
                // action canceled
                release();
                break;
            case KeyEvent.VK_RIGHT:
            case KeyEvent.VK_DOWN:
            case KeyEvent.VK_TAB:
                selectedIndex++;
                if (selectedIndex == operations.length) {
                    selectedIndex = 0;
                }
                requestRepaint();
                break;
            case KeyEvent.VK_LEFT:
            case KeyEvent.VK_UP:    
                selectedIndex--;
                if (selectedIndex < 0) {
                    selectedIndex = operations.length - 1;
                }
                requestRepaint();
                break;
            case KeyEvent.VK_HOME:
                selectedIndex = 0;
                requestRepaint();
                break;
            case KeyEvent.VK_END:
                selectedIndex = operations.length - 1;
                requestRepaint();
                break;
        }
        if (consumeEvent) {
            e.consume();
        }
    }

    public void keyReleased(KeyEvent e) {
        e.consume();
    }

    // **************************************************************************
    // MouseListener and MouseMotionListener implementation
    // **************************************************************************
    
    public void mouseClicked(MouseEvent e) {
        if (e.isPopupTrigger()) {
            return;
        }
        e.consume();
        int position = editorPane.viewToModel(e.getPoint());
        if (e.getClickCount() == 1 && e.getButton() == MouseEvent.BUTTON1) {
            if (position < 0) {
                return;
            }
            if (mousedIndex != -1) {
                selectedIndex = mousedIndex;
                performAction = true;
                release();
                return;
            }
        }
        try {
            int line = Utilities.getLineOffset((BaseDocument) doc, position) + 1;
            if (line < startLine || line > endLine) {
                release();
                return;
            }
        } catch (BadLocationException ex) {
        }
    }
    
    public void mouseMoved(MouseEvent e) {
        e.consume();
        int position = editorPane.viewToModel(e.getPoint());
        int newIndex = -1;
        if (position >= 0) {
            for (int x = 0; x < operations.length; x++) {
                int start = operations[x].getMethodStartPosition().getOffset();
                int end = operations[x].getMethodEndPosition().getOffset();
                if (position >= start && position <= end) {
                    newIndex = x;
                    break;
                }
            } // for
        } // if
        if (newIndex != mousedIndex) {
            if (newIndex == -1) {
                editorPane.setCursor(arrowCursor);
            } else {
                editorPane.setCursor(handCursor);
            }
            mousedIndex = newIndex;
            requestRepaint();
        }
    }

    public void mouseReleased(MouseEvent e) {
        e.consume();
    }

    public void mousePressed(MouseEvent e) {
        e.consume();
    }

    public void mouseExited(MouseEvent e) {
        e.consume();
    }

    public void mouseEntered(MouseEvent e) {
        e.consume();
    }
    
    public void mouseDragged(MouseEvent e) {
        e.consume();
    }

    // **************************************************************************
    // FocusListener implementation
    // **************************************************************************
    
    public void focusGained(FocusEvent e) {
        editorPane.getCaret().setVisible(false);
    }

    public void focusLost(FocusEvent e) {
    }
    
    // **************************************************************************
    // PropertyChangeListener implementation
    // **************************************************************************
    
    public void propertyChange(PropertyChangeEvent evt) {
        if (debugger.getState() == JPDADebugger.STATE_DISCONNECTED ||
                currentThread != debugger.getCurrentThread() || !currentThread.isSuspended()) {
            release();
        }
    }
    
    // **************************************************************************
    // inner classes
    // **************************************************************************
    
    private static final class TooltipResolver implements HighlightAttributeValue<String> {

        public TooltipResolver() {
        }

        public String getValue(JTextComponent component, Document document, Object attributeKey, int startOffset, int endOffset) {
            return NbBundle.getMessage(MethodChooser.class, "MSG_Step_Into_Method");
        }
        
    }
    
    private static final class OperatorsComparator implements Comparator {

        public int compare(Object o1, Object o2) {
            Object[] a1 = (Object[])o1;
            Object[] a2 = (Object[])o2;
            Operation op1 = (Operation)a1[0];
            Operation op2 = (Operation)a2[0];
            return op1.getMethodStartPosition().getOffset() - op2.getMethodStartPosition().getOffset();
        }
        
    }
    
}
