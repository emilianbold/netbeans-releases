package org.netbeans.modules.debugger.jpda.actions;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.Location;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.VirtualMachine;

import com.sun.jdi.event.Event;
import com.sun.jdi.request.BreakpointRequest;
import com.sun.jdi.request.EventRequest;
import java.awt.Color;
import java.awt.Cursor;
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
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import javax.swing.text.AttributeSet;
import javax.swing.text.Caret;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.StyleConstants;

import org.netbeans.api.debugger.ActionsManager;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.JPDAStep;
import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;
import org.netbeans.api.editor.settings.AttributesUtilities;
import org.netbeans.api.editor.settings.EditorStyleConstants;
import org.netbeans.editor.BaseCaret;
import org.netbeans.modules.debugger.jpda.ExpressionPool.Expression;
import org.netbeans.spi.debugger.jpda.EditorContext.Operation;
import org.netbeans.spi.editor.highlighting.HighlightAttributeValue;
import org.netbeans.spi.editor.highlighting.support.OffsetsBag;
import org.netbeans.modules.debugger.jpda.models.CallStackFrameImpl;
import org.netbeans.modules.debugger.jpda.util.Executor;
import org.netbeans.modules.debugger.jpda.EditorContextBridge;

import org.netbeans.spi.debugger.jpda.EditorContext;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
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
    private Session session;
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
    
    private Operation[] operations;
    private Location[] locations;
    private ArrayList<Annotation> annotations;
    private boolean performAction = false;
    private int selectedIndex = -1;
    private int mousedIndex = -1;

    MethodChooser(JPDADebuggerImpl debugger, Session session, String url, ReferenceType clazz, int methodLine, int methodOffset) {
        this.session = session;
        this.debugger = debugger;
        this.currentThread = debugger.getCurrentThread();
        this.url = url;
        this.clazzRef = clazz;
        //this.methodLine = methodLine; [TODO]
        this.methodOffset = methodOffset;
    }

    public static OffsetsBag getHighlightsBag(Document doc) {
        OffsetsBag bag = (OffsetsBag) doc.getProperty(MethodChooser.class);
        if (bag == null) {
            doc.putProperty(MethodChooser.class, bag = new OffsetsBag(doc, true));
        }
        return bag;
    }
    
    public void run() {
        DataObject dobj = getDataObject(url);
        final EditorCookie ec = (EditorCookie) dobj.getCookie(EditorCookie.class);
        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                    editorPane = ec.getOpenedPanes()[0];
                }
            });
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        } catch (InvocationTargetException ex) {
            Exceptions.printStackTrace(ex);
        }
        doc = editorPane.getDocument();
        
        if (debugger.getState() == JPDADebugger.STATE_DISCONNECTED) {
            return;
        }
        // hack - disable org.netbeans.modules.debugger.jpda.projects.ToolTipAnnotation
        System.setProperty("org.netbeans.modules.debugger.jpda.doNotShowTooltips", "true"); // NOI18N
        debugger.addPropertyChangeListener(JPDADebugger.PROP_STATE, this);
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
        if (collectOperations()) {
            annotateLines();
            requestRepaint();
        } else {
            release();
        }
    }

    private void release() {
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
        
        if (performAction) {
            String name = operations[selectedIndex].getMethodName();
            doAction(locations[selectedIndex], name);
        }
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
            while (methodLine > 0 && (locs = clazzRef.locationsOfLine(methodLine)).isEmpty()) {
                methodLine--;
            }
        } catch (AbsentInformationException aiex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, aiex);
        }
        if (locs.isEmpty()) {
            String message = NbBundle.getMessage(RunIntoMethodActionProvider.class,
                                                 "MSG_RunIntoMeth_absentInfo",
                                                 clazzRef.name());
            NotifyDescriptor.Message descriptor = new NotifyDescriptor.Message(message);
            DialogDisplayer.getDefault().notify(descriptor);
            return false;
        }
        Expression expr = debugger.getExpressionPool().getExpressionAt(locs.get(0), url);
        if (expr != null) {
            operations = expr.getOperations();
            locations = expr.getLocations();
            
            Object[][] elems = new Object[operations.length][2];
            for (int i = 0; i < operations.length; i++) {
                elems[i][0] = operations[i];
                elems[i][1] = locations[i];
            }
            Arrays.sort(elems, new OperatorsComparator());
            selectedIndex = 0;
            Operation currOp = currentThread.getCurrentOperation();
            for (int i = 0; i < operations.length; i++) {
                operations[i] = (Operation)elems[i][0];
                locations[i] = (Location)elems[i][1];
                if (operations[i].equals(currOp)) {
                    selectedIndex = i == operations.length - 1 ? 0 : i + 1;
                }
            }
            for (int i = 0; i < operations.length; i++) {
                Operation op = operations[i];
                if (op.getMethodStartPosition().getOffset() <= methodOffset && methodOffset <= op.getMethodEndPosition().getOffset()) {
                    selectedIndex = i;
                    break;
                }
            }
        }
        if (selectedIndex < 0) {
            NotifyDescriptor.Message descriptor = new NotifyDescriptor.Message(
                NbBundle.getMessage(RunIntoMethodActionProvider.class, "MSG_No_operations_at_line")
            );
            DialogDisplayer.getDefault().notify(descriptor);
            return false;
        }
        return true;
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
        int startLine = operations[0].getMethodStartPosition().getLine();
        int endLine = operations[operations.length - 1].getMethodStartPosition().getLine();
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
    
    private void doAction(Location bpLocation, final String methodName) {
        final VirtualMachine vm = debugger.getVirtualMachine();
        if (vm == null) return ;
        final int line = bpLocation.lineNumber("Java");
        CallStackFrameImpl csf = (CallStackFrameImpl) debugger.getCurrentCallStackFrame();
        if (csf != null && csf.getStackFrame().location().equals(bpLocation)) {
            // We're on the line from which the method is called
            traceLineForMethod(methodName, line);
        } else {
            // Submit the breakpoint to get to the point from which the method is called
            final BreakpointRequest brReq = vm.eventRequestManager().createBreakpointRequest(bpLocation);
            debugger.getOperator().register(brReq, new Executor() {

                public boolean exec(Event event) {
                    Logger.getLogger(RunIntoMethodActionProvider.class.getName()).
                        fine("Calling location reached, tracing for "+methodName+"()");
                    vm.eventRequestManager().deleteEventRequest(brReq);
                    debugger.getOperator().unregister(brReq);
                    traceLineForMethod(methodName, line);
                    return true;
                }
                
                public void removed(EventRequest eventRequest) {}
                
            });
            brReq.setSuspendPolicy(debugger.getSuspend());
            brReq.enable();
        }
        resume();
    }
    
    private void resume() {
        if (debugger.getSuspend() == JPDADebugger.SUSPEND_EVENT_THREAD) {
            debugger.getCurrentThread().resume();
            //((JPDADebuggerImpl) debugger).resumeCurrentThread();
        } else {
            //((JPDADebuggerImpl) debugger).resume();
            session.getEngineForLanguage ("Java").getActionsManager ().doAction (
                ActionsManager.ACTION_CONTINUE
            );
        }
    }
    
    private void traceLineForMethod(final String method, final int methodLine) {
        final int depth = debugger.getCurrentThread().getStackDepth();
        final JPDAStep step = debugger.createJPDAStep(JPDAStep.STEP_LINE, JPDAStep.STEP_INTO);
        step.setHidden(true);
        step.addPropertyChangeListener(JPDAStep.PROP_STATE_EXEC, new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if (Logger.getLogger(RunIntoMethodActionProvider.class.getName()).isLoggable(Level.FINE)) {
                    Logger.getLogger(RunIntoMethodActionProvider.class.getName()).
                        fine("traceLineForMethod("+method+") step is at "+debugger.getCurrentThread().getClassName()+":"+debugger.getCurrentThread().getMethodName());
                }
                //System.err.println("RunIntoMethodActionProvider: Step fired, at "+
                //                   debugger.getCurrentThread().getMethodName()+"()");
                JPDAThread t = debugger.getCurrentThread();
                int currentDepth = t.getStackDepth();
                Logger.getLogger(RunIntoMethodActionProvider.class.getName()).
                        fine("  depth = "+currentDepth+", target = "+depth);
                if (currentDepth == depth) { // We're in the outer expression
                    try {
                        if (t.getCallStack()[0].getLineNumber("Java") != methodLine) {
                            // We've missed the method :-(
                            step.setHidden(false);
                        } else {
                            step.setDepth(JPDAStep.STEP_INTO);
                            step.addStep(debugger.getCurrentThread());
                        }
                    } catch (AbsentInformationException aiex) {
                        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, aiex);
                        // We're somewhere strange...
                        step.setHidden(false);
                    }
                } else {
                    if (t.getMethodName().equals(method)) {
                        // We've found it :-)
                        step.setHidden(false);
                    } else if (t.getMethodName().equals("<init>") && (t.getClassName().endsWith("."+method) || t.getClassName().equals(method))) {
                        // The method can be a constructor
                        step.setHidden(false);
                    } else {
                        step.setDepth(JPDAStep.STEP_OUT);
                        step.addStep(debugger.getCurrentThread());
                    }
                }
            }
        });
        step.addStep(debugger.getCurrentThread());
    }
    
    // **************************************************************************
    // KeyListener implementation
    // **************************************************************************
    
    public void keyTyped(KeyEvent e) {
        e.consume();
    }

    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();
        e.consume();
        switch (code) {
            case KeyEvent.VK_ENTER:
            case KeyEvent.VK_SPACE:
            case KeyEvent.VK_F7: // [TODO]
                // selection confirmed
                performAction = true;
                release();
                break;
            case KeyEvent.VK_ESCAPE:
                // action canceled
                release();
                break;
            case KeyEvent.VK_RIGHT:
            case KeyEvent.VK_DOWN:
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
    }

    public void keyReleased(KeyEvent e) {
        e.consume();
    }

    // **************************************************************************
    // MouseListener and MouseMotionListener implementation
    // **************************************************************************
    
    public void mouseClicked(MouseEvent e) {
        e.consume();
        if (!e.isPopupTrigger() && e.getClickCount() == 1 && e.getButton() == MouseEvent.BUTTON1) {
            int position = editorPane.viewToModel(e.getPoint());
            if (position < 0) {
                return ;
            }
            if (mousedIndex != -1) {
                selectedIndex = mousedIndex;
                performAction = true;
                release();
            }
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
        if (debugger.getState() == JPDADebugger.STATE_DISCONNECTED || !currentThread.isSuspended()) {
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
