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
import java.awt.event.*;
import java.io.File;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.swing.*;
import javax.swing.text.*;
import org.netbeans.editor.BaseKit;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.editor.hints.borrowed.EditorUtilities;
import org.netbeans.modules.editor.hints.borrowed.ListCompletionView;
import org.netbeans.modules.editor.hints.borrowed.ScrollCompletionPane;
import org.netbeans.modules.editor.hints.spi.ChangeInfo;
import org.netbeans.modules.editor.hints.spi.FeatureResolver;
import org.netbeans.modules.editor.hints.spi.Hint;
import org.openide.ErrorManager;
import org.openide.cookies.EditCookie;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;


/**
 * Responsible for painting the things the user sees that indicate available
 * hints.
 *
 * @author Tim Boudreau
 */
public class HintsUI implements MouseListener, KeyListener {
    private JTextComponent comp = null;
    private List hints = Collections.EMPTY_LIST;
    private static int WINDOW_GAP = 2;
    
    private Popup listPopup = null;
    private FadeComponent textHint = null;
    private JLabel hintIcon = null;
    private ScrollCompletionPane hintListComponent = null;
    
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
            showHints(popup);
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
        removeTextHint();
        removeIconHint();
        removePopup();
    }
    
    private void removeTextHint() {
        if (textHint != null && textHint.getParent() != null) {
            Container c = textHint.getParent();
            if (c != null) {
                Rectangle bds = textHint.getBounds();
                c.remove (textHint);
                c.repaint (bds.x, bds.y, bds.width, bds.height);
            }
        }
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
            hintListComponent = null;
        }
    }
    
    boolean isKnownComponent(Component c) {
        return c == comp || c == hintIcon || c == hintListComponent || c == textHint;
    }
    
    private void showHints(boolean suggest) {
        if (comp == null || !comp.isDisplayable() || !comp.isShowing()) {
            return;
        }
        if (suggest) {
            String hint = NbBundle.getMessage(HintsUI.class, 
                    "FMT_Hint", hints.get(0).toString());
            getTextHint().setText (hint);
            configureBounds (getTextHint(), true);
        }
        configureBounds (getHintIcon(), false);
    }
    
    private void configureBounds (JComponent jc, boolean trackCaret) {
        JRootPane pane = comp.getRootPane();
        JLayeredPane lp = pane.getLayeredPane();
        Rectangle r = null;
        try {
            int pos = comp.getCaret().getDot();
            if (!trackCaret) {
                pos = javax.swing.text.Utilities.getRowStart(comp, pos);
            }
            r = comp.modelToView (pos);
        } catch (BadLocationException e) {
            setHints (null, null, false);
            ErrorManager.getDefault().notify (e);
            return;
        }
        Point p = new Point(r.x, r.y );
         
        Dimension d = jc.getPreferredSize();
        
        if (trackCaret) {
            p.y += d.height;
        } else {
            p.x -= 17;
        }
        
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
    
    private FadeComponent getTextHint() {
        if (textHint == null) {
            textHint = new FadeComponent();
        }
        return textHint;
    }
    
    void showPopup() {
        if (comp == null || hints.isEmpty()) {
            return;
        }
        hintListComponent = 
                new ScrollCompletionPane(comp, hints, null, null);
        
        hintListComponent.getView().addMouseListener (this);
        
        try {
            int pos = javax.swing.text.Utilities.getRowStart(comp, comp.getCaret().getDot());
            Rectangle r = comp.modelToView (pos);

            Point p = new Point (r.x + 5, r.y + 20);
            SwingUtilities.convertPointToScreen(p, comp);
            
            listPopup = getPopupFactory().getPopup(
                    comp, hintListComponent, p.x, p.y);
            listPopup.show();
            removeTextHint();
        } catch (BadLocationException ble) {
            ErrorManager.getDefault().notify (ble);
            setHints (null, null, false);
            if (listPopup != null) {
                listPopup.hide();
                listPopup = null;
            }
        }
    }
    
    private PopupFactory pf = null;
    private PopupFactory getPopupFactory() {
        if (pf == null) {
            //Lightweight popup never repaints on mac os - no idea why
            //Seems to be solved by usage of the JLayeredPane
            if (false) {
                pf = new PopupFactory();
                try {
                    java.lang.reflect.Method m = PopupFactory.class.getDeclaredMethod("setPopupType", new Class[] {Integer.TYPE});
                    m.setAccessible(true);
                    java.lang.reflect.Field f = PopupFactory.class.getDeclaredField("HEAVY_WEIGHT_POPUP");
                    f.setAccessible(true);
                    Object arg = f.get(PopupFactory.class);
                    m.invoke (pf, new Object[] { arg });
                } catch (Exception e) {
                    pf = PopupFactory.getSharedInstance();
                    e.printStackTrace();
                    ErrorManager.getDefault().notify (ErrorManager.INFORMATIONAL,
                            e);
                }
            } else {
                pf = PopupFactory.getSharedInstance();
            }
        }
        return pf;
    }

    public void mouseClicked(java.awt.event.MouseEvent e) {
        if (e.getSource() == hintListComponent || e.getSource() instanceof ListCompletionView && e.getClickCount() >= 2) {
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
            removeTextHint();
            if (!isPopupActive()) {
                showPopup();
            }
        } 
    }

    public void mouseReleased(java.awt.event.MouseEvent e) {
    }
    
    public boolean isActive() {
        boolean bulbShowing = hintIcon != null && hintIcon.isShowing();
        boolean textShowing = textHint != null && textHint.isShowing();
        boolean popupShowing = hintListComponent != null && hintListComponent.isShowing();
        return bulbShowing || textShowing || popupShowing;
    }
    
    public boolean isPopupActive() {
        return hintListComponent != null && hintListComponent.isShowing();
    }
    
    public void keyPressed(KeyEvent e) {
        if (comp == null || hints.size() == 0) {
            return;
        }
        boolean bulbShowing = hintIcon != null && hintIcon.isShowing();
        boolean textShowing = textHint != null && textHint.isShowing();
        boolean popupShowing = hintListComponent != null && hintListComponent.isShowing();
        boolean multipleHints = hints.size() > 1;
        if ( e.getKeyCode() == KeyEvent.VK_ENTER ) {
            if ( e.getModifiersEx() == KeyEvent.ALT_DOWN_MASK ) {
                if (textShowing && !multipleHints) {
                    invokeHint ((Hint) hints.get(0));
                    e.consume();
                } else if ( bulbShowing && !popupShowing) {
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
            if ( textShowing ) {
                removeTextHint();
            }
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
                File file = fileForChange (change, c);
                if (file != null) {
                    try {
                        DataObject dob = 
                            DataObject.find (FileUtil.toFileObject(file));
                        
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
                
                int start = change.getStart();
                int end = change.getEnd();
                if (start != -1) {
                    c.setSelectionStart(start);
                }
                if (end != -1) {
                    c.setSelectionEnd(end);
                }
            }
        } finally {
            gp.setCursor (cur);
        }
    }
    
    private static File fileForChange(ChangeInfo.Change change, JTextComponent c) {
        Object feature = change.getFeature();
        if (feature instanceof File) {
            return (File) feature;
        }
        if (feature == null) {
            return null;
        }
        BaseKit kit = Utilities.getKit(c);
        if (kit == null) {
            return null;
        }
        Lookup lookup = EditorUtilities.getMimeLookup(kit.getContentType());
        Lookup.Result result = lookup.lookup(new Lookup.Template(FeatureResolver.class));
        for (Iterator i=result.allInstances().iterator(); i.hasNext();) {
            FeatureResolver resolver = (FeatureResolver) i.next();
            if (resolver.accept (feature)) {
                return resolver.toFile (feature);
            }
        }
        return null;
    }
}
