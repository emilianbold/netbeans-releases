/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2016 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
 */
package org.netbeans.modules.debugger.jpda.projectsui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import static javax.swing.text.JTextComponent.addKeymap;
import javax.swing.text.Keymap;
import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.LazyDebuggerManagerListener;
import org.netbeans.api.debugger.Pin;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.debugger.Watch;
import org.netbeans.api.debugger.jpda.*;
import org.netbeans.editor.EditorUI;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.spi.debugger.DebuggerServiceRegistration;
import org.netbeans.spi.debugger.jpda.EditorContext;
import org.netbeans.spi.debugger.ui.EditorContextDispatcher;
import org.openide.awt.CloseButtonFactory;
import org.openide.cookies.LineCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.Annotation;
import org.openide.text.AnnotationProvider;
import org.openide.text.Line;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Ralph Benjamin Ruijs
 */
@org.openide.util.lookup.ServiceProvider(service=org.openide.text.AnnotationProvider.class)
@DebuggerServiceRegistration(types={LazyDebuggerManagerListener.class})
public class WatchAnnotationProvider implements AnnotationProvider, LazyDebuggerManagerListener {
    
    private static final Map<Watch, Annotation> watchToAnnotation = new IdentityHashMap<>();
    private static final Map<Watch, JComponent> watchToWindow = new IdentityHashMap<>();
    private Set<PropertyChangeListener> dataObjectListeners;

    @Override
    public void annotate(Line.Set lines, Lookup context) {
        DataObject dobj = context.lookup(DataObject.class);
        if(dobj == null) return;
        final JEditorPane ep = EditorContextDispatcher.getDefault().getMostRecentEditor ();
        if(ep == null) return;
        EditorUI eui = Utilities.getEditorUI(ep);
        if(eui == null)  return;
        
        synchronized (watchToAnnotation) {
            Watch[] watches = DebuggerManager.getDebuggerManager().getWatches();
            for (Watch watch : watches) {
                if(watch.getPin() == null || !dobj.getPrimaryFile().equals(watch.getPin().getFile())) continue;
                Line line = lines.getOriginal(watch.getPin().getLine());
                pin(watch, eui, line);
            }
        }
    }

    @SuppressWarnings("AssignmentToMethodParameter")
    private void pin(Watch watch, EditorUI eui, Line line) throws IndexOutOfBoundsException {
        Annotation ann = watchToAnnotation.remove(watch); // just to be sure
        if(ann != null) ann.detach();
        JComponent frame = watchToWindow.remove(watch);
        if(frame != null) {
            frame.setVisible(false);
            eui.getStickyWindowSupport().removeWindow(frame);
        }
        
        Pin pin = watch.getPin();
        if (pin == null) return;

        if(line == null) {
            line = getLine(pin.getFile(), pin.getLine());
        }
        
        DebuggerAnnotation annotation = new DebuggerAnnotation(EditorContext.WATCH_ANNOTATION_TYPE, line, null);
        watchToAnnotation.put(watch, annotation);
        annotation.attach(line);
        
        JComponent window = new StickyPanel(watch, eui);
        eui.getStickyWindowSupport().addWindow(window, pin.getLocation());
        watchToWindow.put(watch, window);
    }
    
    private Line getLine (FileObject file, int lineNumber) {
        if (file == null) return null;
        DataObject dataObject;
        try {
            dataObject = DataObject.find (file);
        } catch (DataObjectNotFoundException ex) {
            return null;
        }
        if (dataObject == null) return null;
        LineCookie lineCookie = dataObject.getLookup().lookup(LineCookie.class);
        if (lineCookie == null) return null;
        Line.Set ls = lineCookie.getLineSet ();
        if (ls == null) return null;
        try {
            return ls.getCurrent (lineNumber);
        } catch (IndexOutOfBoundsException | IllegalArgumentException e) {
        }
        return null;
    }

    @Override
    public String[] getProperties() {
        return new String [] {
            DebuggerManager.PROP_WATCHES,
        };
    }

    @Override
    public Breakpoint[] initBreakpoints() {
        return null;
    }

    @Override
    public void breakpointAdded(Breakpoint breakpoint) {
    }

    @Override
    public void breakpointRemoved(Breakpoint breakpoint) {
    }

    @Override
    public void initWatches() {
    }

    @Override
    public void watchAdded(Watch watch) {
    }

    @Override
    public void watchRemoved(Watch watch) {
        synchronized(watchToAnnotation) {
            Annotation annotation = watchToAnnotation.remove(watch);
            if(annotation != null) {
                annotation.detach();
            }
            JComponent frame = watchToWindow.get(watch);
            if(frame != null) {
                frame.setVisible(false);
            }
        }
    }

    @Override
    public void watchPinned(Watch watch) {
        final JEditorPane ep = EditorContextDispatcher.getDefault().getMostRecentEditor();
        if(ep == null) return;
        EditorUI eui = Utilities.getEditorUI(ep);
        if(eui == null)  return;
        synchronized(watchToAnnotation) {
            pin(watch, eui, null);
        }
    }

    @Override
    public void watchUnpinned(Watch watch) {
    }

    @Override
    public void sessionAdded(Session session) {
    }

    @Override
    public void sessionRemoved(Session session) {
    }

    @Override
    public void engineAdded(DebuggerEngine engine) {
    }

    @Override
    public void engineRemoved(DebuggerEngine engine) {
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
    }

    private static final class StickyPanel extends JPanel {
        private static final String UI_PREFIX = "ToolTip"; // NOI18N
        private final JLabel label;
        private RequestProcessor annotationProcessor = new RequestProcessor("Annotation Refresh", 1);

        @SuppressWarnings("OverridableMethodCallInConstructor")
        public StickyPanel(final Watch watch, final EditorUI eui) {
            Font font = UIManager.getFont(UI_PREFIX + ".font"); // NOI18N
            Color backColor = UIManager.getColor(UI_PREFIX + ".background"); // NOI18N
            Color foreColor = UIManager.getColor(UI_PREFIX + ".foreground"); // NOI18N
            
            if (backColor != null) {
                setBackground(backColor);
            }
            
            setOpaque(true);
            
            setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(getForeground()),
                BorderFactory.createEmptyBorder(0, 3, 0, 3)
            ));
            
            setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
            
            Icon expIcon = ImageUtilities.loadImageIcon("org/netbeans/swing/tabcontrol/resources/win8_popup_enabled.png", false);    // NOI18N
            JButton expButton = new JButton(expIcon);
            expButton.setBorder(new javax.swing.border.EmptyBorder(0, 0, 0, 5));
            expButton.setBorderPainted(false);
            expButton.setContentAreaFilled(false);
            add(expButton);
            
            annotationProcessor.post(new Runnable() {
                @Override
                public void run() {
                    evaluateExpression(watch);
                }
            });
//            label = createMultiLineToolTip(value, true);
            label = new JLabel("Evaluating ...");
            if (font != null) {
                label.setFont(font);
            }
            if (foreColor != null) {
                label.setForeground(foreColor);
            }
            if (backColor != null) {
                label.setBackground(backColor);
            }
            label.setBorder(new javax.swing.border.EmptyBorder(0, 3, 0, 3));
            add(label);
            
            JButton closeButton = org.openide.awt.CloseButtonFactory.createBigCloseButton();
            closeButton.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    eui.getStickyWindowSupport().removeWindow(StickyPanel.this);
                }
            });
            add(closeButton);
            
            MouseAdapter mouseAdapter = new java.awt.event.MouseAdapter() {
                private Point orig;
                @Override
                public void mouseDragged(MouseEvent e) {
                    if(orig == null) {
                        orig = e.getPoint();
                    }
                    e.translatePoint(-orig.x, -orig.y);
                    Point p = getLocation();
                    Point deltaP = e.getPoint();
                    p.translate(deltaP.x, deltaP.y);
                    setLocation(p);
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    orig = null;
                }
            };
            addMouseListener(mouseAdapter);
            addMouseMotionListener(mouseAdapter);
        }

        private void evaluateExpression(Watch watch) {
            String expression = watch.getExpression();
            DebuggerEngine engine = DebuggerManager.getDebuggerManager().getCurrentEngine();
            if(engine != null) {
                final JPDADebugger d = engine.lookupFirst(null, JPDADebugger.class);
                if (d != null) {
                    JPDAThread t = d.getCurrentThread();
                    String toolTipText;
                        try {
                            Variable v = null;
//                            List<Operation> operations = t.getLastOperations();
//                            if (operations != null) {
//                                for (Operation operation: operations) {
//                                    if (!expression.endsWith(operation.getMethodName())) {
//                                        continue;
//                                    }
//                                    if (operation.getMethodStartPosition().getOffset() <= offset &&
//                                        offset <= operation.getMethodEndPosition().getOffset()) {
//                                        v = operation.getReturnValue();
//                                    }
//                                }
//                            }
                            if (v == null) {
//                                if (isMethodPtr[0]) {
//                                    return ; // We do not evaluate methods
//                                }
//                                String fieldClass = fieldOfPtr[0];
//                                if (fieldClass != null) {
//                                    CallStackFrame currentCallStackFrame = d.getCurrentCallStackFrame();
//                                    if (currentCallStackFrame != null) {
//                                        v = findField(currentCallStackFrame, fieldClass, expression);
//                                    }
//                                }
//                                if (v == null) {
                                    v = d.evaluate (expression);
//                                }
                            }
                            if (v == null) {
                                return ; // Something went wrong...
                            }
                            String type = v.getType ();
                            if (v instanceof ObjectVariable) {
                                ObjectVariable tooltipVariable = (ObjectVariable) v;
                                try {
                                    Object jdiValue = v.getClass().getMethod("getJDIValue").invoke(v);
                                    if (jdiValue == null) {
                                        tooltipVariable = null;
                                    }
                                } catch (Exception ex) {}
                                if (tooltipVariable != null) {
                                    try {
                                        v = (Variable) d.getClass().getMethod("getFormattedValue", ObjectVariable.class).invoke(d, v);
                                    } catch (Exception ex) {}
                                }
                            }
                            if (v instanceof ObjectVariable) {
                                try {
                                    String toString = ((ObjectVariable) v).getToStringValue();
                                    toolTipText = expression + " = " +
                                            (type.length () == 0 ?
                                            "" :
                                            "(" + type + ") ") +
                                            toString;
                                } catch (InvalidExpressionException ex) {
                                    toolTipText = expression + " = " +
                                        (type.length () == 0 ?
                                            "" :
                                            "(" + type + ") ") +
                                        v.getValue ();
                                }
                            } else {
                                toolTipText = expression + " = " +
                                    (type.length () == 0 ?
                                        "" :
                                        "(" + type + ") ") +
                                    v.getValue ();
                            }
                        } catch (InvalidExpressionException e) {
                            toolTipText = expression + " = >" + e.getMessage () + "<";
                        }
                        final String value = toolTipText;
                        javax.swing.SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                label.setText(value);
                                Dimension size = getPreferredSize();
                                Point loc = getLocation();
                                setBounds(loc.x, loc.y, size.width, size.height);
                            }
                        });
                }
            }
        }
        
        /**
         * Search for a field in a className class, that is accessible from 'this'.
         * 
         * @param csf the current stack frame
         * @param fieldClass the class that declares the field
         * @param fieldName the name of the field
         * @return the field, or <code>null</code> when not found.
         */
        private Variable findField(CallStackFrame csf, String fieldClass, String fieldName) {
           This thisVariable = csf.getThisVariable();
           if (thisVariable == null) {
               return null;
           }

           // Search for the field in parent classes/interfaces first:
           Field field = thisVariable.getField(fieldName);
           if (field != null && field.getDeclaringClass().getName().equals(fieldClass)) {
               return field;
           }

           // Test outer classes then:
           ObjectVariable outer = null;
           int i;
           for (i = 0; i < 10; i++) {
               outer = (ObjectVariable) thisVariable.getField("this$"+i);          // NOI18N
               if (outer != null) {
                   break;
               }
           }
           while (outer != null) {
               field = outer.getField(fieldName);
               if (field != null && field.getDeclaringClass().getName().equals(fieldClass)) {
                   return field;
               }
               if (i == 0) {
                   break;
               }
               // Go to the next outer class:
               i--;
               outer = (ObjectVariable) outer.getField("this$"+i);                 // NOI18N
           }
           return null;
        }
        
        private static JTextArea createMultiLineToolTip(String toolTipText, boolean wrapLines) {
            JTextArea ta = new TextToolTip(wrapLines);
            ta.setText(toolTipText);
            return ta;
        }
        
        private static class TextToolTip extends JTextArea {
            
            private static final String ELIPSIS = "..."; //NOI18N
            
            private final boolean wrapLines;
            
            public TextToolTip(boolean wrapLines) {
                this.wrapLines = wrapLines;
                setLineWrap(false); // It's necessary to have a big width of preferred size first.
            }
            
            public @Override void setSize(int width, int height) {
                Dimension prefSize = getPreferredSize();
                if (width >= prefSize.width) {
                    width = prefSize.width;
                } else { // smaller available width
                    // Set line wrapping and do super.setSize() to determine
                    // the real height (it will change due to line wrapping)
                    if (wrapLines) {
                        setLineWrap(true);
                        setWrapStyleWord(true);
                    }
                    
                    super.setSize(width, Integer.MAX_VALUE); // the height is unimportant
                    prefSize = getPreferredSize(); // re-read new pref width
                }
                if (height >= prefSize.height) { // enough height
                    height = prefSize.height;
                } else { // smaller available height
                    // Check how much can be displayed - cannot rely on line count
                    // because line wrapping may display single physical line
                    // into several visual lines
                    // Before using viewToModel() a setSize() must be called
                    // because otherwise the viewToModel() would return -1.
                    super.setSize(width, Integer.MAX_VALUE);
                    int offset = viewToModel(new Point(0, height));
                    Document doc = getDocument();
                    try {
                        if (offset > ELIPSIS.length()) {
                            offset -= ELIPSIS.length();
                            doc.remove(offset, doc.getLength() - offset);
                            doc.insertString(offset, ELIPSIS, null);
                        }
                    } catch (BadLocationException ble) {
                        // "..." will likely not be displayed but otherwise should be ok
                    }
                    // Recalculate the prefSize as it may be smaller
                    // than the present preferred height
                    height = Math.min(height, getPreferredSize().height);
                }
                super.setSize(width, height);
            }
            
            @Override
            public void setKeymap(Keymap map) {
                //#181722: keymaps are shared among components with the same UI
                //a default action will be set to the Keymap of this component below,
                //so it is necessary to use a Keymap that is not shared with other JTextAreas
                super.setKeymap(addKeymap(null, map));
            }
        }
    }
    
}
