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
package org.netbeans.modules.debugger.ui.annotations;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.text.BadLocationException;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.LazyDebuggerManagerListener;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.debugger.Watch;
import org.netbeans.api.editor.document.LineDocumentUtils;
import org.netbeans.editor.EditorUI;
import org.netbeans.editor.Utilities;
import org.netbeans.spi.debugger.DebuggerServiceRegistration;
import org.netbeans.spi.debugger.ui.EditorContextDispatcher;
import org.netbeans.spi.debugger.ui.EditorPin;
import org.netbeans.spi.debugger.ui.PinWatchUISupport;
import org.netbeans.spi.debugger.ui.PinWatchUISupport.ValueProvider;
import org.netbeans.spi.debugger.ui.PinWatchUISupport.ValueProvider.ValueChangeListener;
import org.netbeans.api.editor.StickyWindowSupport;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.LineCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.Annotation;
import org.openide.text.AnnotationProvider;
import org.openide.text.CloneableEditorSupport;
import org.openide.text.Line;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;

/**
 *
 * @author Ralph Benjamin Ruijs
 */
@org.openide.util.lookup.ServiceProvider(service=org.openide.text.AnnotationProvider.class)
@DebuggerServiceRegistration(types={LazyDebuggerManagerListener.class})
public class WatchAnnotationProvider implements AnnotationProvider, LazyDebuggerManagerListener {
    
    @SuppressWarnings("PublicField")
    public static PinSupportedAccessor PIN_SUPPORT_ACCESS; // Set from PinWatchSupport.getDefault()
    private static WatchAnnotationProvider INSTANCE;
    
    private static final Map<Watch, Annotation> watchToAnnotation = new IdentityHashMap<>();
    private static final Map<Watch, JComponent> watchToWindow = new IdentityHashMap<>();
    private Set<PropertyChangeListener> dataObjectListeners;
    
    public WatchAnnotationProvider() {
        PinWatchUISupport.getDefault(); // To initialize PIN_SUPPORT_ACCESS
        INSTANCE = this;
    }

    @Override
    public void annotate(Line.Set lines, Lookup context) {
        DataObject dobj = context.lookup(DataObject.class);
        if(dobj == null) return;
        final CloneableEditorSupport ces = context.lookup(CloneableEditorSupport.class);
        if (ces == null) {
            return ;
        }
        FileObject file = context.lookup(FileObject.class);
        if (file == null) {
            file = dobj.getPrimaryFile();
        }
        List<Watch> pinnedWatches = null;
        Watch[] watches = DebuggerManager.getDebuggerManager().getWatches();
        for (Watch watch : watches) {
            Watch.Pin pin = watch.getPin();
            if (!(pin instanceof EditorPin)) {
                continue;
            }
            EditorPin epin = (EditorPin) pin;
            if (!file.equals(epin.getFile())) {
                continue;
            }
            if (pinnedWatches == null) {
                pinnedWatches = new LinkedList<>();
            }
            pinnedWatches.add(watch);
        }
        if (pinnedWatches != null) {
            final List<Watch> pws = pinnedWatches;
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    JEditorPane[] openedPanes = ces.getOpenedPanes();
                    for (JEditorPane pane : openedPanes) {
                        EditorUI eui = Utilities.getEditorUI(pane);
                        if (eui == null) {
                            continue;
                        }
                        synchronized (watchToAnnotation) {
                            for (Watch watch : pws) {
                                EditorPin epin = (EditorPin) watch.getPin();
                                Line line = lines.getOriginal(epin.getLine());
                                pin(watch, eui, line);
                            }
                        }
                    }
                }
            });
        }
    }

    @SuppressWarnings("AssignmentToMethodParameter")
    private void pin(Watch watch, EditorUI eui, Line line) throws IndexOutOfBoundsException {
        Annotation ann = watchToAnnotation.remove(watch); // just to be sure
        if(ann != null) ann.detach();
        JComponent frame = watchToWindow.remove(watch);
        StickyWindowSupport stickyWindowSupport = eui.getStickyWindowSupport();
        if(frame != null) {
            frame.setVisible(false);
            stickyWindowSupport.removeWindow(frame);
        }
        
        final EditorPin pin = (EditorPin) watch.getPin();
        if (pin == null) return;

        if(line == null) {
            line = getLine(pin.getFile(), pin.getLine());
        }
        
        final DebuggerAnnotation annotation = new DebuggerAnnotation(DebuggerAnnotation.WATCH_ANNOTATION_TYPE, line);
        annotation.setWatch(watch);
        watchToAnnotation.put(watch, annotation);
        annotation.attach(line);
        pin.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (EditorPin.PROP_LINE.equals(evt.getPropertyName())) {
                    annotation.detach();
                    Line line = getLine(pin.getFile(), pin.getLine());
                    annotation.attach(line);
                }
            }
        });
        
        JComponent window = new StickyPanel(watch, eui);
        stickyWindowSupport.addWindow(window);
        window.setLocation(pin.getLocation());
        watchToWindow.put(watch, window);
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                Dimension size = window.getPreferredSize();
                Point loc = window.getLocation();
                window.setBounds(loc.x, loc.y, size.width, size.height);
            }
        });
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
        /*
        Watch.Pin pin = watch.getPin();
        if (pin instanceof EditorPin) {
            // TODO:
            final JEditorPane ep = EditorContextDispatcher.getDefault().getMostRecentEditor();
            if(ep == null) return;
            EditorUI eui = Utilities.getEditorUI(ep);
            if(eui == null)  return;
            synchronized(watchToAnnotation) {
                pin(watch, eui, null);
            }
        }
        */
    }

    @Override
    public void watchRemoved(Watch watch) {
        synchronized(watchToAnnotation) {
            Annotation annotation = watchToAnnotation.remove(watch);
            if(annotation != null) {
                annotation.detach();
            }
            JComponent frame = watchToWindow.remove(watch);
            if(frame != null) {
                EditorUI eui = ((StickyPanel) frame).eui;
                eui.getStickyWindowSupport().removeWindow(frame);
            }
        }
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
        @StaticResource
        private static final String ICON_COMMENT = "org/netbeans/modules/debugger/resources/actions/Comment.png";   // NOI18N
        private static final String UI_PREFIX = "ToolTip"; // NOI18N
        private final Watch watch;
        private final EditorUI eui;
        private final JLabel label;
        private final JLabel valueLabel;
        private final JTextField valueField;
        private final JToolBar headActions;
        private final JToolBar tailActions;
        private JTextField commentField;
        private final ValueProvider valueProvider;
        private final String evaluatingValue;
        private String lastValue;

        @SuppressWarnings("OverridableMethodCallInConstructor")
        public StickyPanel(final Watch watch, final EditorUI eui) {
            this.watch = watch;
            this.eui = eui;
            EditorPin pin = (EditorPin) watch.getPin();
            Font font = UIManager.getFont(UI_PREFIX + ".font"); // NOI18N
            setOpaque(true);
            
            setBorder(BorderFactory.createLineBorder(getForeground()));
            
            setLayout(new GridBagLayout());
            GridBagConstraints gridConstraints = new GridBagConstraints();
            gridConstraints.gridx = 0;
            gridConstraints.gridy = 0;
            
            /*Icon expIcon = ImageUtilities.loadImageIcon(ICON_COMMENT, false);
            JButton expButton = new JButton(expIcon);
            expButton.setBorder(new javax.swing.border.EmptyBorder(0, 3, 0, 5));
            expButton.setBorderPainted(false);
            expButton.setContentAreaFilled(false);
            add(expButton, gridConstraints);
            expButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    addCommentListener();
                }
            });*/
            
//            label = createMultiLineToolTip(value, true);
            valueProvider = PIN_SUPPORT_ACCESS.getValueProvider(pin);
            headActions = createActionsToolbar();
            add(headActions, gridConstraints);
            Action[] actions = valueProvider.getHeadActions(watch);
            addActions(headActions, actions);
            evaluatingValue = valueProvider.getEvaluatingText();
            label = new JLabel(watch.getExpression() + " = ");
            valueLabel = new JLabel();
            valueField = new JTextField();
            valueField.setVisible(false);
            valueProvider.setChangeListener(watch, new ValueChangeListener() {
                @Override
                public void valueChanged(Watch w) {
                    final String text = getWatchValueText(watch, valueProvider);
                    javax.swing.SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            Action[] actions = valueProvider.getHeadActions(watch);
                            addActions(headActions, actions);
                            valueLabel.setText(text);
                            actions = valueProvider.getTailActions(watch);
                            addActions(tailActions, actions);
                            Dimension size = getPreferredSize();
                            Point loc = getLocation();
                            setBounds(loc.x, loc.y, size.width, size.height);
                        }
                    });
                }
            });
            valueLabel.setText(getWatchValueText(watch, valueProvider));
            if (font != null) {
                label.setFont(font);
                valueLabel.setFont(font);
            }
            label.setBorder(new javax.swing.border.EmptyBorder(0, 3, 0, 0));
            valueLabel.setBorder(new javax.swing.border.EmptyBorder(0, 0, 0, 3));
            gridConstraints.gridx++;
            add(label, gridConstraints);
            gridConstraints.gridx++;
            add(valueLabel, gridConstraints);
            add(valueField, gridConstraints);
            valueLabel.addMouseListener(new MouseListener() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    String editableValue = valueProvider.getEditableValue(watch);
                    if (editableValue != null) {
                        valueLabel.setVisible(false);
                        valueField.setVisible(true);
                        valueField.setPreferredSize(null);
                        valueField.setText(editableValue);
                        Dimension fieldSize = valueField.getPreferredSize();
                        int minWidth = 4*fieldSize.height;  // Have some reasonable minimum width
                        if (fieldSize.width < minWidth) {
                            fieldSize.width = minWidth;
                            valueField.setPreferredSize(fieldSize);
                        }
                        valueField.requestFocusInWindow();
                        Dimension size = getPreferredSize();
                        Point loc = getLocation();
                        setBounds(loc.x, loc.y, size.width, size.height);
                    }
                }
                @Override
                public void mousePressed(MouseEvent e) {}
                @Override
                public void mouseReleased(MouseEvent e) {}
                @Override
                public void mouseEntered(MouseEvent e) {}
                @Override
                public void mouseExited(MouseEvent e) {}
            });
            valueLabel.addMouseMotionListener(new MouseMotionListener() {
                @Override
                public void mouseDragged(MouseEvent e) {
                    valueLabel.getParent().dispatchEvent(e);
                }

                @Override
                public void mouseMoved(MouseEvent e) {}
            });
            valueField.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    String newValue = valueField.getText();
                    if (valueProvider.setValue(watch, newValue)) {
                        valueLabel.setText(getWatchValueText(watch, valueProvider));
                    }
                    valueLabel.setVisible(true);
                    valueField.setVisible(false);
                    Dimension size = getPreferredSize();
                    Point loc = getLocation();
                    setBounds(loc.x, loc.y, size.width, size.height);
                }
            });
            valueField.addFocusListener(new FocusListener() {
                @Override
                public void focusGained(FocusEvent e) {}

                @Override
                public void focusLost(FocusEvent e) {
                    valueLabel.setVisible(true);
                    valueField.setVisible(false);
                    Dimension size = getPreferredSize();
                    Point loc = getLocation();
                    setBounds(loc.x, loc.y, size.width, size.height);
                }
            });

            tailActions = createActionsToolbar();
            gridConstraints.gridx++;
            gridConstraints.weighty = 1;
            gridConstraints.fill = GridBagConstraints.VERTICAL;
            add(tailActions, gridConstraints);
            actions = valueProvider.getTailActions(watch);
            addActions(tailActions, actions);
            JSeparator iconsSeparator = new JSeparator(JSeparator.VERTICAL);
            gridConstraints.gridx++;
            gridConstraints.weighty = 1;
            gridConstraints.fill = GridBagConstraints.VERTICAL;
            add(iconsSeparator, gridConstraints);
            gridConstraints.weighty = 0;
            gridConstraints.fill = GridBagConstraints.NONE;

            Icon commentIcon = ImageUtilities.loadImageIcon(ICON_COMMENT, false);
            JButton commentButton = new JButton(commentIcon);
            commentButton.setBorder(new javax.swing.border.EmptyBorder(0, 3, 0, 3));
            commentButton.setBorderPainted(false);
            commentButton.setContentAreaFilled(false);
            gridConstraints.gridx++;
            add(commentButton, gridConstraints);

            JButton closeButton = org.openide.awt.CloseButtonFactory.createBigCloseButton();
            closeButton.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    eui.getStickyWindowSupport().removeWindow(StickyPanel.this);
                    watch.remove();
                }
            });
            gridConstraints.gridx++;
            add(closeButton, gridConstraints);

            final int gridwidth = gridConstraints.gridx + 1;
            if (pin.getComment() != null) {
                addCommentField(pin.getComment(), gridwidth);
            }
            commentButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    addCommentListener(gridwidth);
                }
            });

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
                    Point linePoint = new Point(p.x, p.y + label.getHeight()/2);
                    int pos = eui.getComponent().viewToModel(linePoint);
                    int line;
                    try {
                        line = LineDocumentUtils.getLineIndex(eui.getDocument(), pos);
                    } catch (BadLocationException ex) {
                        line = ((EditorPin) watch.getPin()).getLine();
                    }
                    ((EditorPin) watch.getPin()).move(line, p);
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    orig = null;
                }
            };
            addMouseListener(mouseAdapter);
            addMouseMotionListener(mouseAdapter);
        }

        private static JToolBar createActionsToolbar() {
            JToolBar jt = new JToolBar(JToolBar.HORIZONTAL);
            jt.setBorder(new EmptyBorder(0, 0, 0, 0));
            jt.setFloatable(false);
            jt.setRollover(false);
            return jt;
        }

        private void addActions(JToolBar tb, Action[] actions) {
            tb.removeAll();
            boolean visible = false;
            if (actions != null) {
                for (Action a : actions) {
                    if (a != null) {
                        JButton btn = tb.add(a);
                        btn.setBorder(new javax.swing.border.EmptyBorder(0, 2, 0, 2));
                        btn.setBorderPainted(false);
                        btn.setContentAreaFilled(false);
                        btn.setRolloverEnabled(false);
                        btn.setOpaque(false);
                        btn.setFocusable(false);
                        visible = true;
                    } else {
                        tb.add(new JSeparator(JSeparator.VERTICAL));
                    }
                }
            }
            tb.setVisible(visible);
        }

        private void addCommentField(String text, int gridwidth) {
            commentField = new JTextField(text);
            commentField.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    commentUpdated();
                }
            });
            GridBagConstraints gridConstraints = new GridBagConstraints();
            gridConstraints.gridy = 1;
            gridConstraints.gridwidth = gridwidth;
            gridConstraints.fill = GridBagConstraints.HORIZONTAL;
            add(commentField, gridConstraints);
        }

        private void addCommentListener(int gridwidth) {
            if (commentField == null) {
                addCommentField("", gridwidth);
                setSize(getPreferredSize());
                revalidate();
                repaint();
            } else {
                boolean visible = !commentField.isVisible();
                commentField.setVisible(visible);
                setSize(getPreferredSize());
                revalidate();
                repaint();
                if (visible) {
                    commentField.requestFocusInWindow();
                }
            }
        }

        @Override
        public void removeNotify() {
            valueProvider.unsetChangeListener(watch);
        }

        private void commentUpdated() {
            ((EditorPin) watch.getPin()).setComment(commentField.getText());
        }

        private String getWatchValueText(Watch watch, ValueProvider vp) {
            String value = vp.getValue(watch);
            //System.err.println("WatchAnnotationProvider.getWatchText("+watch.getExpression()+"): value = "+value+", lastValue = "+lastValue);
            if (value == evaluatingValue) {
                return "<html>" + "<font color=\"red\">" + value + "</font>" + "</html>";
            }
            boolean bold = false;
            boolean old = false;
            if (value != null) {
                bold = (lastValue != null && !lastValue.equals(value));
                lastValue = value;
            } else {
                old = true;
                value = lastValue;
            }
            String s1, s2;
            if (bold) {
                s1 = "<b>";
                s2 = "</b>";
            } else if (old) {
                s1 = "<font color=\"gray\">";
                s2 = "</font>";
            } else {
                s1 = s2 = "";
            }
            //System.err.println("  return: "+"<html>" + watch.getExpression() + " = " + s1 + value + s2 + "</html>");
            return "<html>" + s1 + value + s2 + "</html>";
        }
        
        /*
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
         */
    }

    public static abstract class PinSupportedAccessor {

        public abstract ValueProvider getValueProvider(EditorPin pin);
        
        public final void pin(Watch watch) throws DataObjectNotFoundException {
            EditorPin pin = (EditorPin) watch.getPin();
            DataObject dobj = DataObject.find(pin.getFile());
            EditorCookie ec = dobj.getLookup().lookup(EditorCookie.class);
            JEditorPane[] openedPanes = ec.getOpenedPanes();
            if (openedPanes == null) {
                throw new IllegalArgumentException("No editor panes opened for file "+pin.getFile());
            }
            LineCookie lineCookie = dobj.getLookup().lookup(LineCookie.class);
            if (lineCookie == null) {
                throw new IllegalArgumentException("No line cookie in "+pin.getFile());
            }
            Line.Set ls = lineCookie.getLineSet();
            Line line;
            try {
                line = ls.getCurrent(pin.getLine());
            } catch (IndexOutOfBoundsException e) {
                throw new IllegalArgumentException("Wrong line: "+pin.getLine(), e);
            }
            for (JEditorPane pane : openedPanes) {
                JEditorPane ep = EditorContextDispatcher.getDefault().getMostRecentEditor();
                EditorUI editorUI = Utilities.getEditorUI(pane);
                WatchAnnotationProvider.INSTANCE.pin(watch, editorUI, line);
            }
        }
    }
    
}
