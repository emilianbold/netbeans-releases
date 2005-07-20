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

package org.netbeans.modules.editor.hints;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import java.util.Collections;
import java.util.List;
import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;
import org.netbeans.modules.editor.hints.borrowed.ListCompletionView;
import org.netbeans.modules.editor.hints.borrowed.ScrollCompletionPane;
import org.netbeans.modules.editor.hints.spi.ChangeInfo;
import org.netbeans.modules.editor.hints.spi.Hint;
import org.openide.ErrorManager;
import org.openide.cookies.EditCookie;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.NbBundle;


/**
 * Responsible for painting the things the user sees that indicate available
 * hints.
 *
 * @author Tim Boudreau
 */
public class HintsUI implements MouseListener, KeyListener {
    private JTextComponent comp;
    private List hints = Collections.EMPTY_LIST;   
    private Popup listPopup;
    private JLabel hintIcon;
    private ScrollCompletionPane hintListComponent;
    
    /** Creates a new instance of HintsUI */
    HintsUI() {
    }
    
    public JTextComponent getComponent() {
        return comp;
    }
    
    public void setHints (List hints, JTextComponent comp, boolean showPopup) {
        if (this.hints.equals(hints) && this.comp == comp) {
            return;
        }
        if (comp != this.comp || !this.hints.equals(hints) && comp != null) {
            removePopups();
        }
        boolean show =  hints != null && comp != null && !hints.isEmpty();
        if (!show && this.comp != null) {
            removePopups();
        }
        this.hints = hints == null ? Collections.EMPTY_LIST : hints;
        setComponent (comp);
        if (show) {
            boolean popup = ((Hint) hints.get(0)).getType() == Hint.ERROR;
            showHints();
            if (!popup && showPopup) {
                showPopup();
            }
        }
    }
    
    private void setComponent (JTextComponent comp) {
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
    
    
    private void removePopups() {
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
        if (listPopup != null) {
            listPopup.hide();
            hintListComponent.getView().removeMouseListener(this);
            hintListComponent = null;
            listPopup = null;
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
        Point p = new Point(r.x, r.y );
         
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
        String iconBase;
        if (hints.size() > 1) {
            iconBase =
                "org/netbeans/modules/editor/hints/resources/multisuggestion.gif"; //NOI18N
        } else {
            int type = Hint.SUGGESTION;
            if (hints.size() > 0) { //should be
                Hint hint = (Hint) hints.get(0);
                type = hint.getType();
            }
            iconBase = type == Hint.SUGGESTION ?
                "org/netbeans/modules/editor/hints/resources/suggestion.gif" : //NOI18N
                "org/netbeans/modules/editor/hints/resources/error-sg.gif"; //NOI18N
        }
        hintIcon.setIcon (new ImageIcon (org.openide.util.Utilities.loadImage(iconBase)));
        return hintIcon;
    }
    
    void showPopup() {
        if (comp == null || hints.isEmpty()) {
            return;
        }
        hintIcon.setToolTipText(null);
        // be sure that hint will hide when popup is showing
        ToolTipManager.sharedInstance().setEnabled(false);
        ToolTipManager.sharedInstance().setEnabled(true);
        assert hintListComponent == null;
        hintListComponent = 
                new ScrollCompletionPane(comp, hints, null, null);
        
        hintListComponent.getView().addMouseListener (this);
        
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
    
    private PopupFactory pf = null;
    private PopupFactory getPopupFactory() {
        if (pf == null) {
            pf = PopupFactory.getSharedInstance();
        }
        return pf;
    }

    public void mouseClicked(java.awt.event.MouseEvent e) {
        if (e.getSource() == hintListComponent || e.getSource() instanceof ListCompletionView) {
            Hint h = (Hint) hintListComponent.getView().getSelectedValue();
            if (h != null) {
                invokeHint (h);
                setHints (null, null, false);
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
    
    public void keyPressed(KeyEvent e) {
        if (comp == null || hints.size() == 0) {
            return;
        }
        boolean bulbShowing = hintIcon != null && hintIcon.isShowing();
        boolean popupShowing = hintListComponent != null && hintListComponent.isShowing();
        if ( e.getKeyCode() == KeyEvent.VK_ENTER ) {
            if ( e.getModifiersEx() == KeyEvent.ALT_DOWN_MASK ) {
                if ( bulbShowing && !popupShowing) {
                    showPopup();
                    e.consume();
                }
            } else if ( e.getModifiersEx() == 0 ) {
                if (popupShowing) {
                    Hint hint = (Hint) hintListComponent.getView().getSelectedValue();
                    invokeHint (hint);
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
    
    private void invokeHint (Hint h) {
        removePopups();
        Component gp = this.comp.getRootPane().getGlassPane();
        Cursor cur = comp.getCursor();
        gp.setCursor (Cursor.getPredefinedCursor (Cursor.WAIT_CURSOR));
        try {
            //Store component in a local variable - once we move the dot,
            //the comp ivar will become null
            JTextComponent c = this.comp;
            
            ChangeInfo changes = h.implement();
            if (changes != null && changes.size() > 0) {
                ChangeInfo.Change change = changes.get(0);
                FileObject file = change.getFileObject();
                if (file != null) {
                    try {
                        DataObject dob = 
                            DataObject.find (file);
                        
                        EditCookie ck = 
                            (EditCookie) dob.getCookie(EditCookie.class);
                        
                        if (ck != null) {
                            //Try EditCookie first so we don't open the form
                            //editor
                            ck.edit();
                        } else {
                            OpenCookie oc = (OpenCookie) 
                                dob.getCookie(OpenCookie.class);
                            
                            oc.open();
                        }
                        EditorCookie edit = (EditorCookie) 
                            dob.getCookie (EditorCookie.class);
                        
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
                
                Position start = change.getStart();
                Position end = change.getEnd();
                if (start != null) {
                    c.setSelectionStart(start.getOffset());
                }
                if (end != null) {
                    c.setSelectionEnd(end.getOffset());
                }
            }
        } finally {
            gp.setCursor (cur);
        }
    }
}
