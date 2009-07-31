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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.editor.hints;

import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.editor.AnnotationDesc;
import org.netbeans.editor.Annotations;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.GuardedException;
import org.netbeans.editor.JumpList;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.editor.hints.borrowed.ListCompletionView;
import org.netbeans.modules.editor.hints.borrowed.ScrollCompletionPane;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.Context;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.editor.hints.PositionRefresher;
import org.openide.ErrorManager;
import org.openide.cookies.EditCookie;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.Annotation;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
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
public class HintsUI implements MouseListener, MouseMotionListener, KeyListener, PropertyChangeListener, AWTEventListener  {
    
    private static HintsUI INSTANCE;
    private static final String POPUP_NAME = "hintsPopup"; // NOI18N
    private static final int POPUP_VERTICAL_OFFSET = 5;
    
    public static synchronized HintsUI getDefault() {
        if (INSTANCE == null)
            INSTANCE = new HintsUI();
        
        return INSTANCE;
    }
    
    static Logger UI_GESTURES_LOGGER = Logger.getLogger("org.netbeans.ui.editor.hints");
    
    private Reference<JTextComponent> compRef;
    private Popup listPopup;
    private Popup tooltipPopup;
    private JLabel hintIcon;
    private ScrollCompletionPane hintListComponent;
    private JLabel errorTooltip;
    private AtomicBoolean cancel;
    
    /** Creates a new instance of HintsUI */
    private HintsUI() {
        EditorRegistry.addPropertyChangeListener(this);
        propertyChange(null);
        cancel = new AtomicBoolean(false);
    }
    
    public JTextComponent getComponent() {
        return compRef == null ? null : compRef.get();
    }
    
    public void removeHints() {
        removePopups();
        setComponent(null);
    }
    
    public void setComponent (JTextComponent comp) {
        JTextComponent thisComp = getComponent();
        boolean change = thisComp != comp;
        if (change) {
            unregister();
            this.compRef = new WeakReference<JTextComponent>(comp);
            register();
        }
    }

    private AnnotationHolder getAnnotationHolder(Document doc) {
        DataObject od = (DataObject) doc.getProperty(Document.StreamDescriptionProperty);
        if (od == null) {
            return null;
        }
        return AnnotationHolder.getInstance(od.getPrimaryFile());
    }

    private void register() {
        JTextComponent comp = getComponent(); 
        if (comp == null) {
            return;
        }
        comp.addKeyListener (this);
    }
    
    private void unregister() {
        JTextComponent comp = getComponent(); 
        if (comp == null) {
            return;
        }
        comp.removeKeyListener (this);
    }
    
    
    public void removePopups() {
        JTextComponent comp = getComponent(); 
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
            if( tooltipPopup != null )
                tooltipPopup.hide();
            tooltipPopup = null;
            listPopup.hide();
            if (hintListComponent != null) {
                hintListComponent.getView().removeMouseListener(this);
                hintListComponent.getView().removeMouseMotionListener(this);
            }
            if (errorTooltip != null) {
                errorTooltip.removeMouseListener(this);
            }
            hintListComponent = null;
            errorTooltip = null;
            listPopup = null;
            if (hintIcon != null)
                hintIcon.setToolTipText(NbBundle.getMessage(HintsUI.class, "HINT_Bulb")); // NOI18N
        }
    }
    
    boolean isKnownComponent(Component c) {
        JTextComponent comp = getComponent(); 
        return c != null && 
               (c == comp 
                || c == hintIcon 
                || c == hintListComponent
                || (c instanceof Container && ((Container)c).isAncestorOf(hintListComponent))
                )
        ;
    }
    
    public void showPopup(FixData hints) {
        JTextComponent comp = getComponent(); 
        if (comp == null || (hints.isComputed() && hints.getFixes().isEmpty())) {
            return;
        }
        if (hintIcon != null)
            hintIcon.setToolTipText(null);
        // be sure that hint will hide when popup is showing
        ToolTipManager.sharedInstance().setEnabled(false);
        ToolTipManager.sharedInstance().setEnabled(true);
        assert hintListComponent == null;
        Toolkit.getDefaultToolkit().addAWTEventListener(this, AWTEvent.MOUSE_EVENT_MASK);
        
        try {
            int pos = javax.swing.text.Utilities.getRowStart(comp, comp.getCaret().getDot());
            Rectangle r = comp.modelToView (pos);

            Point p = new Point (r.x + 5, r.y + 20);
            SwingUtilities.convertPointToScreen(p, comp);
            
            Dimension maxSize = Toolkit.getDefaultToolkit().getScreenSize();
            maxSize.width -= p.x;
            maxSize.height -= p.y;
            hintListComponent = 
                    new ScrollCompletionPane(comp, hints, null, null, maxSize);

            hintListComponent.getView().addMouseListener (this);
            hintListComponent.getView().addMouseMotionListener(this);
            hintListComponent.setName(POPUP_NAME);
            
            assert listPopup == null;
            listPopup = getPopupFactory().getPopup(
                    comp, hintListComponent, p.x, p.y);
            listPopup.show();
        } catch (BadLocationException ble) {
            ErrorManager.getDefault().notify (ble);
            removeHints();
        }
    }
    
    public void showPopup(FixData fixes, String description, JTextComponent component, Point position) {
        removeHints();
        setComponent(component);
        JTextComponent comp = getComponent(); 
        
        if (comp == null || fixes == null)
            return ;

        Point p = new Point(position);
        SwingUtilities.convertPointToScreen(p, comp);
        
        if (hintIcon != null)
            hintIcon.setToolTipText(null);
        // be sure that hint will hide when popup is showing
        ToolTipManager.sharedInstance().setEnabled(false);
        ToolTipManager.sharedInstance().setEnabled(true);
        Toolkit.getDefaultToolkit().addAWTEventListener(this, AWTEvent.MOUSE_EVENT_MASK);
        
        errorTooltip = new JLabel("<html>" + translate(description)); // NOI18N
        errorTooltip.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.BLACK),
            BorderFactory.createEmptyBorder(0, 3, 0, 3)
        ));
        errorTooltip.addMouseListener(this);
        
        if (!fixes.isComputed() || fixes.getFixes().isEmpty()) {
            //show tooltip:
            assert listPopup == null;
            
            listPopup = getPopupFactory().getPopup(
                    comp, errorTooltip, p.x, p.y);
        } else {
            assert hintListComponent == null;

            int rowHeight = 14; //default
            
            hintListComponent =
                    new ScrollCompletionPane(comp, fixes, null, null, comp.getPreferredSize());

            final Dimension hintPopup = hintListComponent.getPreferredSize();
            Rectangle screen = getScreenBounds();
            boolean exceedsHeight = p.y + hintPopup.height > screen.height;

            try {
                int pos = javax.swing.text.Utilities.getRowStart(comp, comp.getCaret().getDot());
                Rectangle r = comp.modelToView (pos);
                rowHeight = r.height;

                int y;
                final Dimension errorPopup = errorTooltip.getPreferredSize();
                if (exceedsHeight)
                    y = p.y + POPUP_VERTICAL_OFFSET;
                else
                    y = p.y-rowHeight-errorPopup.height-POPUP_VERTICAL_OFFSET;

                //shift error popup left if necessary
                int xPos = p.x;
                if (p.x - screen.x + errorPopup.width > screen.width) {
                    xPos -= p.x - screen.x + errorPopup.width - screen.width;
                }

                tooltipPopup = getPopupFactory().getPopup(
                        comp, errorTooltip, xPos, y);
            } catch( BadLocationException blE ) {
                ErrorManager.getDefault().notify (blE);
                errorTooltip = null;
            }

            if(exceedsHeight) {
                p.y -= hintPopup.height + rowHeight + POPUP_VERTICAL_OFFSET;
            }

            //shift hint popup left if necessary
            if(p.x - screen.x + hintPopup.width > screen.width) {
                p.x -= p.x - screen.x + hintPopup.width - screen.width;
            }

            hintListComponent.getView().addMouseListener (this);
            hintListComponent.getView().addMouseMotionListener(this);
            hintListComponent.setName(POPUP_NAME);
            assert listPopup == null;
            listPopup = getPopupFactory().getPopup(
                    comp, hintListComponent, p.x, p.y);
        }
        
        if( tooltipPopup != null )
            tooltipPopup.show();
        listPopup.show();
    }
    
    private PopupFactory pf = null;
    private PopupFactory getPopupFactory() {
        if (pf == null) {
            pf = PopupFactory.getSharedInstance();
        }
        return pf;
    }

    private Rectangle getScreenBounds() throws HeadlessException {
        JTextComponent comp = getComponent();
        Rectangle screenBounds = null;
        if (null != comp && null != comp.getGraphicsConfiguration()) {
            screenBounds = comp.getGraphicsConfiguration().getBounds();
        } else {
            screenBounds = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
        }
        return screenBounds;
    }

//    private Dimension getMaxSizeAt( Point p ) {
//        Rectangle screenBounds = getScreenBounds();
//        Dimension maxSize = screenBounds.getSize();
//        maxSize.width -= p.x - screenBounds.x;
//        maxSize.height -= p.y - screenBounds.y;
//        return maxSize;
//    }

    public void mouseClicked(java.awt.event.MouseEvent e) {
        if (e.getSource() == hintListComponent || e.getSource() instanceof ListCompletionView) {
            Fix f = null;
            Object selected = hintListComponent.getView().getSelectedValue();
            
            if (selected instanceof Fix) {
                f = (Fix) selected;
            }
            
            if (f != null) {
                e.consume();
                JTextComponent c = getComponent(); 
                invokeHint (f);
                if (c != null && org.openide.util.Utilities.isMac()) {
                    // see issue #65326
                    c.requestFocus();
                }
                removeHints();
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
    }

    public void mouseReleased(java.awt.event.MouseEvent e) {
    }

    public void mouseDragged(MouseEvent e) {
    }

    public void mouseMoved(MouseEvent e) {
        ListCompletionView view = hintListComponent.getView();
        view.setSelectedIndex(view.locationToIndex(e.getPoint()));
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
        AnnotationHolder annotations = getAnnotationHolder(doc);

        if (annotations != null) {
            for (Annotation a : annotations.getAnnotations()) {
                if (a instanceof ParseErrorAnnotation) {
                    ParseErrorAnnotation pa = (ParseErrorAnnotation) a;

                    if (lineNum == pa.getLineNumber() && desc != null
                            && org.openide.util.Utilities.compareObjects(desc.getShortDescription(), a.getShortDescription())) {
                        return pa;
                    }
                }
            }
        }
        
        return null;
    }

    boolean invokeDefaultAction(boolean onlyActive) {
        JTextComponent comp = getComponent(); 
        if (comp == null) {
            Logger.getLogger(HintsUI.class.getName()).log(Level.WARNING, "HintsUI.invokeDefaultAction called, but comp == null");
            return false;
        }

        Document doc = comp.getDocument();

        cancel.set(false);
        refresh(doc, comp.getCaretPosition());
        
        if (doc instanceof BaseDocument) {
            Annotations annotations = ((BaseDocument) doc).getAnnotations();

            try {
                Rectangle carretRectangle = comp.modelToView(comp.getCaretPosition());            
                int line = Utilities.getLineOffset((BaseDocument) doc, comp.getCaretPosition());
                AnnotationDesc desc = annotations.getActiveAnnotation(line);
                ParseErrorAnnotation annotation = findAnnotation(doc, desc, line);
                
                if (annotation == null) {
                    if (onlyActive) {
                        return false;
                    }
                    
                    AnnotationDesc[] pas = annotations.getPasiveAnnotations(line);
                    
                    if (pas == null) {
                        return false;
                    }
                    
                    for (AnnotationDesc ad : pas) {
                        if ((annotation = findAnnotation(doc, ad, line)) != null) {
                            break;
                        }
                    }

                    if (annotation == null) {
                        return false;
                    }
                }
                
                Point p = comp.modelToView(Utilities.getRowStartFromLineOffset((BaseDocument) doc, line)).getLocation();
                p.y += carretRectangle.height;
                if( comp.getParent() instanceof JViewport ) {
                    p.x += ((JViewport)comp.getParent()).getViewPosition().x;
                }
                
                showPopup(annotation.getFixes(), annotation.getDescription(), comp, p);
                
                return true;
            } catch (BadLocationException ex) {
                ErrorManager.getDefault().notify(ex);
            }
        }
        
        return false;
    }
    
    public void keyPressed(KeyEvent e) {
        JTextComponent comp = getComponent(); 
        if (comp == null) {
            return;
        }
//        boolean bulbShowing = hintIcon != null && hintIcon.isShowing();
        boolean errorTooltipShowing = errorTooltip != null && errorTooltip.isShowing();
        boolean popupShowing = hintListComponent != null && hintListComponent.isShowing();
        
        if (errorTooltipShowing && !popupShowing) {
            //any key should disable the errorTooltip:
            removePopup();
            return ;
        }
        if ( e.getKeyCode() == KeyEvent.VK_ENTER ) {
            if (e.getModifiersEx() == KeyEvent.ALT_DOWN_MASK) {
                if ( !popupShowing) {
                    invokeDefaultAction(false);
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
            } else {
                //user is tired of waiting for refresh before popup is shown
                cancel.set(true);
            }
        } else if ( popupShowing ) {
            InputMap input = hintListComponent.getInputMap();
            Object actionTag = input.get(KeyStroke.getKeyStrokeForEvent(e));
            if (actionTag != null) {
                Action a = hintListComponent.getActionMap().get(actionTag);
                a.actionPerformed(null);
                e.consume();
                return ;
            } else {
                removePopup();
            }
        } 
    }    

    public void keyReleased(KeyEvent e) {
    }

    public void keyTyped(KeyEvent e) {
    }
    
    private ChangeInfo changes;
    
    private void invokeHint (final Fix f) {
        if (UI_GESTURES_LOGGER.isLoggable(Level.FINE)) {
            LogRecord rec = new LogRecord(Level.FINE, "GEST_HINT_INVOKED");
            
            rec.setResourceBundle(NbBundle.getBundle(HintsUI.class));
            rec.setParameters(new Object[] {f.getText()});
            UI_GESTURES_LOGGER.log(rec);
        }
        
        removePopups();
        final JTextComponent component = getComponent(); 
        JumpList.checkAddEntry(component);
        final Cursor cur = component.getCursor();
        component.setCursor (Cursor.getPredefinedCursor (Cursor.WAIT_CURSOR));
        Task t = null;
        try {
            t = RequestProcessor.getDefault().post(new Runnable() {
                public void run() {
                    try {
                        changes = f.implement();
                    } catch (GuardedException ge) {
                            reportGuardedException(component, ge);
                    } catch (IOException e) {
                        if (e.getCause() instanceof GuardedException) {
                            reportGuardedException(component, e);
                        } else {
                            Exceptions.printStackTrace(e);
                        }
                    } catch (Exception e) {
                        Exceptions.printStackTrace(e);
                    }
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
    
    private static void reportGuardedException(final JTextComponent component, final Exception e) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                String message = NbBundle.getMessage(HintsUI.class, "ERR_CannotApplyGuarded");

                Utilities.setStatusBoldText(component, message);
                Logger.getLogger(HintsUI.class.getName()).log(Level.FINE, null, e);
            }
        });
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
                    Logger.getLogger(HintsUI.class.getName()).log(Level.FINE, null, donfe);
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

    public void propertyChange(PropertyChangeEvent e) {
        JTextComponent active = EditorRegistry.lastFocusedComponent();
        
        if (getComponent() != active) {
            removeHints();
            setComponent(active);
        }
    }
    
    public void eventDispatched(java.awt.AWTEvent aWTEvent) {
        if (aWTEvent instanceof MouseEvent) {
            MouseEvent mv = (MouseEvent)aWTEvent;
            if (mv.getID() == MouseEvent.MOUSE_CLICKED && mv.getClickCount() > 0) {
                //#118828
                if (! (aWTEvent.getSource() instanceof Component)) {
                    removePopup();
                    return;
                }
                
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

    private static String[] c = new String[] {"&", "<", ">", "\n", "\""}; // NOI18N
    private static String[] tags = new String[] {"&amp;", "&lt;", "&gt;", "<br>", "&quot;"}; // NOI18N
    
    private String translate(String input) {
        for (int cntr = 0; cntr < c.length; cntr++) {
            input = input.replaceAll(c[cntr], tags[cntr]);
        }
        
        return input;
    }

    private void refresh(Document doc, int pos) {
        Context context = ContextAccessor.getDefault().newContext(pos, cancel);
        String mimeType = org.netbeans.lib.editor.util.swing.DocumentUtilities.getMimeType(doc);
        Lookup lookup = MimeLookup.getLookup(mimeType);
        Collection<? extends PositionRefresher> refreshers = lookup.lookupAll(PositionRefresher.class);
        //set errors from all available refreshers
        for (PositionRefresher ref : refreshers) {
            Map<String, List<ErrorDescription>> layer2Errs = ref.getErrorDescriptionsAt(context, doc);
            getAnnotationHolder(doc).setErrorsForLine(pos, layer2Errs);
        }
    }

}
