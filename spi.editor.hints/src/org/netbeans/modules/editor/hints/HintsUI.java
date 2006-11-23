/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.editor.hints;

import java.awt.*;
import java.awt.event.AWTEventListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;
import org.netbeans.editor.AnnotationDesc;
import org.netbeans.editor.Annotations;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Registry;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.editor.hints.borrowed.ListCompletionView;
import org.netbeans.modules.editor.hints.borrowed.ScrollCompletionPane;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.editor.hints.LazyFixList;
import org.openide.ErrorManager;
import org.openide.cookies.EditCookie;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.Annotatable;
import org.openide.text.Annotation;
import org.openide.text.Line;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Task;
import org.openide.util.TaskListener;


/**
 * Responsible for painting the things the user sees that indicate available
 * hints.
 *
 * @author Tim Boudreau
 */
public class HintsUI implements MouseListener, KeyListener, ChangeListener, AWTEventListener  {
    
    private static HintsUI INSTANCE;
    private static final String POPUP_NAME = "hintsPopup"; // NOI18N
    
    public static synchronized HintsUI getDefault() {
        if (INSTANCE == null)
            INSTANCE = new HintsUI();
        
        return INSTANCE;
    }
    
    private JTextComponent comp;
    private LazyFixList hints = new StaticFixList();
    private Popup listPopup;
    private JLabel hintIcon;
    private ScrollCompletionPane hintListComponent;
    
    /** Creates a new instance of HintsUI */
    private HintsUI() {
        Registry.addChangeListener(this);
        stateChanged(null);
    }
    
    public JTextComponent getComponent() {
        return comp;
    }
    
    public void setHints (LazyFixList hints, JTextComponent comp, boolean showPopup) {
        if (this.hints.equals(hints) && this.comp == comp) {
            return;
        }
        if (comp != this.comp || !this.hints.equals(hints) && comp != null) {
            removePopups();
        }
        boolean show =  hints != null && comp != null/* && !hints.isEmpty()*/;
        if (!show && this.comp != null) {
            removePopups();
        }
        this.hints = hints == null ? new StaticFixList() : hints;
        setComponent (comp);
        if (show) {
            showHints();
            if (showPopup) {
                showPopup();
            }
        }
    }
    
    public void setComponent (JTextComponent comp) {
        boolean change = this.comp != comp;
        if (change) {
            unregister();
            this.comp = comp;
            register();
        }
    }
    
    private void register() {
        if (comp == null) {
            return;
        }
        comp.addKeyListener (this);
    }
    
    private void unregister() {
        if (comp == null) {
            return;
        }
        comp.removeKeyListener (this);
    }
    
    
    public void removePopups() {
        if (comp == null) {
            return;
        }
        removeIconHint();
        removePopup();
    }
    
    private void removeIconHint() {
        if (hintIcon != null) {
            Container c = hintIcon.getParent();
            if (c != null) {
                Rectangle bds = hintIcon.getBounds();
                c.remove (hintIcon);
                c.repaint (bds.x, bds.y, bds.width, bds.height);
            }
        }
    }
    
    private void removePopup() {
        Toolkit.getDefaultToolkit().removeAWTEventListener(this);
        if (listPopup != null) {
            listPopup.hide();
            hintListComponent.getView().removeMouseListener(this);
            hintListComponent = null;
            listPopup = null;
            if (hintIcon != null)
                hintIcon.setToolTipText(NbBundle.getMessage(HintsUI.class, "HINT_Bulb")); // NOI18N
        }
    }
    
    boolean isKnownComponent(Component c) {
        return c != null && 
               (c == comp 
                || c == hintIcon 
                || c == hintListComponent
                || (c instanceof Container && ((Container)c).isAncestorOf(hintListComponent))
                )
        ;
    }
    
    private void showHints() {
        if (comp == null || !comp.isDisplayable() || !comp.isShowing()) {
            return;
        }
        configureBounds (getHintIcon());
    }
    
    private void configureBounds (JComponent jc) {
        JRootPane pane = comp.getRootPane();
        JLayeredPane lp = pane.getLayeredPane();
        Rectangle r = null;
        try {
            int pos = javax.swing.text.Utilities.getRowStart(comp, comp.getCaret().getDot());
            r = comp.modelToView (pos);
        } catch (BadLocationException e) {
            setHints (null, null, false);
            ErrorManager.getDefault().notify (e);
            return;
        }
        Point p = new Point(r.x - comp.getX(), r.y );
         
        Dimension d = jc.getPreferredSize();
        
        SwingUtilities.convertPointToScreen(p, comp);
        SwingUtilities.convertPointFromScreen(p, lp);
        jc.setBounds (p.x, p.y, d.width, d.height);
        lp.add (jc, JLayeredPane.POPUP_LAYER);
        jc.setVisible(true);
        jc.repaint();
    }
    
    private JLabel getHintIcon() {
        if (hintIcon == null) {
            hintIcon = new JLabel();
            hintIcon.addMouseListener (this);
            hintIcon.setToolTipText(NbBundle.getMessage(HintsUI.class, "HINT_Bulb")); // NOI18N
        }
        String iconBase =
                "org/netbeans/modules/editor/hints/resources/error.png"; //NOI18N
        hintIcon.setIcon (new ImageIcon (org.openide.util.Utilities.loadImage(iconBase)));
        return hintIcon;
    }
    
    public void showPopup() {
        if (comp == null || (hints.isComputed() && hints.getFixes().isEmpty())) {
            return;
        }
        if (hintIcon != null)
            hintIcon.setToolTipText(null);
        // be sure that hint will hide when popup is showing
        ToolTipManager.sharedInstance().setEnabled(false);
        ToolTipManager.sharedInstance().setEnabled(true);
        assert hintListComponent == null;
        hintListComponent = 
                new ScrollCompletionPane(comp, hints, null, null);
        
        hintListComponent.getView().addMouseListener (this);
        hintListComponent.setName(POPUP_NAME);
        Toolkit.getDefaultToolkit().addAWTEventListener(this, AWTEvent.MOUSE_EVENT_MASK);
        
        try {
            int pos = javax.swing.text.Utilities.getRowStart(comp, comp.getCaret().getDot());
            Rectangle r = comp.modelToView (pos);

            Point p = new Point (r.x + 5, r.y + 20);
            SwingUtilities.convertPointToScreen(p, comp);
            
            assert listPopup == null;
            listPopup = getPopupFactory().getPopup(
                    comp, hintListComponent, p.x, p.y);
            listPopup.show();
        } catch (BadLocationException ble) {
            ErrorManager.getDefault().notify (ble);
            setHints (null, null, false);
        }
    }
    
    public void showPopup(LazyFixList fixes, String description, JTextComponent component, Point position) {
        setHints(null, null, false);
        setComponent(component);
        
        if (comp == null || fixes == null)
            return ;

        this.hints = fixes;
        
//        if (fixes.isEmpty()) {
//            //show tooltip:
//            EditorUI eui = Utilities.getEditorUI(component);
//
//            if (eui instanceof ExtEditorUI) {
//                ((ExtEditorUI) eui).getToolTipSupport().setToolTipText(description);
//            }
//
//            return ;
//        }

        if (hintIcon != null)
            hintIcon.setToolTipText(null);
        // be sure that hint will hide when popup is showing
        ToolTipManager.sharedInstance().setEnabled(false);
        ToolTipManager.sharedInstance().setEnabled(true);
        assert hintListComponent == null;
        hintListComponent = 
                new ScrollCompletionPane(comp, fixes, null, null);
        
        hintListComponent.getView().addMouseListener (this);
        hintListComponent.setName(POPUP_NAME);
        Toolkit.getDefaultToolkit().addAWTEventListener(this, AWTEvent.MOUSE_EVENT_MASK);
                
        Point p = new Point(position);
        SwingUtilities.convertPointToScreen(p, comp);
        
        assert listPopup == null;
        listPopup = getPopupFactory().getPopup(
                comp, hintListComponent, p.x, p.y);
        listPopup.show();
    }
    
    private PopupFactory pf = null;
    private PopupFactory getPopupFactory() {
        if (pf == null) {
            pf = PopupFactory.getSharedInstance();
        }
        return pf;
    }

    public void mouseClicked(java.awt.event.MouseEvent e) {
        if (e.getSource() == hintListComponent || e.getSource() instanceof ListCompletionView) {
            Fix f = null;
            Object selected = hintListComponent.getView().getSelectedValue();
            
            if (selected instanceof Fix) {
                f = (Fix) selected;
            }
            
            if (f != null) {
                JTextComponent c = this.comp;
                invokeHint (f);
                setHints (null, null, false);
                //the component was reset when setHints was called, set it back so further hints will work:
                setComponent(c);
            }
        }
    }

    public void mouseEntered(java.awt.event.MouseEvent e) {
    }

    public void mouseExited(java.awt.event.MouseEvent e) {
    }

    public void mousePressed(java.awt.event.MouseEvent e) {
        if (e.getSource() instanceof JLabel) {
            if (!isPopupActive()) {
                showPopup();
            }
        } 
    }

    public void mouseReleased(java.awt.event.MouseEvent e) {
    }
    
    public boolean isActive() {
        boolean bulbShowing = hintIcon != null && hintIcon.isShowing();
        boolean popupShowing = hintListComponent != null && hintListComponent.isShowing();
        return bulbShowing || popupShowing;
    }
    
    public boolean isPopupActive() {
        return hintListComponent != null && hintListComponent.isShowing();
    }
    
    private ParseErrorAnnotation findAnnotation(Document doc, AnnotationDesc desc, int lineNum) {
        AnnotationHolder annotations = HintsControllerImpl.getLayersForDocument(doc);
        
        for (Annotation a : annotations.getAnnotations()) {
            Annotatable at = a.getAttachedAnnotatable();
            
            if (   at instanceof Line
                && lineNum == ((Line) at).getLineNumber()
                && org.openide.util.Utilities.compareObjects(desc.getShortDescription(), a.getShortDescription())
                && a instanceof ParseErrorAnnotation) {
                return (ParseErrorAnnotation) a;
            }
        }
        
        return null;
    }
    
    boolean invokeDefaultAction() {
        if (comp == null) {
            Logger.getLogger(HintsUI.class.getName()).log(Level.WARNING, "HintsUI.invokeDefaultAction called, but comp == null");
            return false;
        }
        
        Document doc = comp.getDocument();
        
        if (doc instanceof BaseDocument) {
            Annotations annotations = ((BaseDocument) doc).getAnnotations();

            try {
                Rectangle carretRectangle = comp.modelToView(comp.getCaretPosition());            
                int line = Utilities.getLineOffset((BaseDocument) doc, comp.getCaretPosition());
                AnnotationDesc desc = annotations.getActiveAnnotation(line);
                Point p = comp.modelToView(Utilities.getRowStartFromLineOffset((BaseDocument) doc, line)).getLocation();
                p.y += carretRectangle.height;
                ParseErrorAnnotation annotation = findAnnotation(doc, desc, line);
                
                if (annotation == null)
                    return false;
                
                showPopup(annotation.getDescription().getFixes(), annotation.getDescription().getDescription(), comp, p);
                
                return true;
            } catch (BadLocationException ex) {
                ErrorManager.getDefault().notify(ex);
            }
        }
        
        return false;
    }
    
    public void keyPressed(KeyEvent e) {
        if (comp == null) {
            return;
        }
        boolean bulbShowing = hintIcon != null && hintIcon.isShowing();
        boolean popupShowing = hintListComponent != null && hintListComponent.isShowing();
        if ( e.getKeyCode() == KeyEvent.VK_ENTER ) {
            if (   e.getModifiersEx() == (KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK)
                || e.getModifiersEx() == KeyEvent.ALT_DOWN_MASK) {
                if ( !popupShowing) {
                    invokeDefaultAction();
                    e.consume();
                }
            } else if ( e.getModifiersEx() == 0 ) {
                if (popupShowing) {
                    Fix f = null;
                    Object selected = hintListComponent.getView().getSelectedValue();
                    
                    if (selected instanceof Fix) {
                        f = (Fix) selected;
                    }
                    
                    if (f != null) {
                        invokeHint(f);
                    }
                    
                    e.consume();
                }
            }
        } else if ( e.getKeyCode() == KeyEvent.VK_ESCAPE ) {
            if ( popupShowing ) {
                removePopup();
            }
        } else if ( popupShowing ) {
            InputMap input = hintListComponent.getInputMap();
            Object actionTag = input.get(KeyStroke.getKeyStrokeForEvent(e));
            if (actionTag != null) {
                Action a = hintListComponent.getActionMap().get(actionTag);
                a.actionPerformed(null);
                e.consume();
                return ;
            }
        }
    }    

    public void keyReleased(KeyEvent e) {
    }

    public void keyTyped(KeyEvent e) {
    }
    
    private ChangeInfo changes;
    
    private void invokeHint (final Fix f) {
        removePopups();
        final JTextComponent component = comp;
        final Cursor cur = component.getCursor();
        component.setCursor (Cursor.getPredefinedCursor (Cursor.WAIT_CURSOR));
        Task t = null;
        try {
            t = RequestProcessor.getDefault().post(new Runnable() {
                public void run() {
                    changes = f.implement();
                }
            });
        } finally {
            if (t != null) {
                t.addTaskListener(new TaskListener() {
                    public void taskFinished(Task task) {
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                open(changes, component);
                                component.setCursor (cur);
                            }
                        });
                    }
                });
            }
        }
    }
    
    private static void open(ChangeInfo changes, JTextComponent component) {
        JTextComponent c = component;
        if (changes != null && changes.size() > 0) {
            ChangeInfo.Change change = changes.get(0);
            FileObject file = change.getFileObject();
            if (file != null) {
                try {
                    DataObject dob = 
                        DataObject.find (file);

                    EditCookie ck = dob.getCookie(EditCookie.class);

                    if (ck != null) {
                        //Try EditCookie first so we don't open the form
                        //editor
                        ck.edit();
                    } else {
                        OpenCookie oc = dob.getCookie(OpenCookie.class);

                        oc.open();
                    }
                    EditorCookie edit = dob.getCookie(EditorCookie.class);

                    JEditorPane[] panes = edit.getOpenedPanes();
                    if (panes != null && panes.length > 0) {
                        c = panes[0];
                    } else {
                        return;
                    }

                } catch (DataObjectNotFoundException donfe) {
                    ErrorManager.getDefault().notify(donfe);
                    return;
                }
            }
            /////////////////////////////////
            Position start = change.getStart();
            Position end = change.getEnd();
            if (start != null) {
                c.setSelectionStart(start.getOffset());
            }
            if (end != null) {
                c.setSelectionEnd(end.getOffset());
            }
        }
    }

    public void stateChanged(ChangeEvent e) {
        JTextComponent active = Registry.getMostActiveComponent();
        
        if (getComponent() != active) {
            setHints(null, null, false);
            setComponent(active);
        }
    }
    
    public void eventDispatched(java.awt.AWTEvent aWTEvent) {
        if (aWTEvent instanceof MouseEvent) {
            MouseEvent mv = (MouseEvent)aWTEvent;
            if (mv.getID() == MouseEvent.MOUSE_CLICKED && mv.getClickCount() > 0) {
                Component comp = (Component)aWTEvent.getSource();
                Container par = SwingUtilities.getAncestorNamed(POPUP_NAME, comp); //NOI18N
                // Container barpar = SwingUtilities.getAncestorOfClass(PopupUtil.class, comp);
                // if (par == null && barpar == null) {
                if ( par == null ) {
                    removePopup();
                }
            }
        }
    }

}
