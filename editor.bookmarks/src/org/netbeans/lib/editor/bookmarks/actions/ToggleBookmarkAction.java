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

package org.netbeans.lib.editor.bookmarks.actions;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.ButtonModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JToggleButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.editor.BaseDocument;
import org.netbeans.lib.editor.bookmarks.BookmarksApiPackageAccessor;
import org.netbeans.lib.editor.bookmarks.api.Bookmark;
import org.netbeans.lib.editor.bookmarks.api.BookmarkList;
import org.openide.cookies.EditorCookie;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;
import org.openide.util.actions.Presenter;


/**
 * Toggles a bookmark in a line in an opened document.
 *
 * @author Vita Stejskal
 */
public final class ToggleBookmarkAction extends AbstractAction implements ContextAwareAction, Presenter.Toolbar {

    private static final String ACTION_NAME = "bookmark-toggle"; // NOI18N
    private static final String ACTION_ICON = "org/netbeans/modules/editor/bookmarks/resources/toggle_bookmark.png"; // NOI18N
        
    private final JTextComponent component;
    
    public ToggleBookmarkAction() {
        this(null);
    }
    
    public ToggleBookmarkAction(JTextComponent component) {
        super(
            NbBundle.getMessage(ToggleBookmarkAction.class, ACTION_NAME), 
            new ImageIcon(Utilities.loadImage(ACTION_ICON))
        );
        putValue(SHORT_DESCRIPTION, getValue(NAME));
        putValue("noIconInMenu", Boolean.TRUE); // NOI18N
        
        this.component = component;
    }

    public Action createContextAwareInstance(Lookup actionContext) {
        JTextComponent jtc = findComponent(actionContext);
        return new ToggleBookmarkAction(jtc);
    }

    public void actionPerformed(ActionEvent arg0) {
        if (component != null) {
            // cloned action with context
            actionPerformed(component);
        } else {
            // global action, will have to find the current component
            JTextComponent jtc = findComponent(Utilities.actionsGlobalContext());
            if (jtc != null) {
                actionPerformed(jtc);
            }
        }
    }

    public Component getToolbarPresenter() {
        AbstractButton b;
        
        if (component != null) {
            b = new MyGaGaButton();
            b.setModel(new BookmarkButtonModel(component));
        } else {
            b = new JButton();
        }
        
        b.putClientProperty("hideActionText", Boolean.TRUE); //NOI18N
        b.setAction(this);
        
        return b;
    }

    private static JTextComponent findComponent(Lookup lookup) {
        EditorCookie ec = (EditorCookie) lookup.lookup(EditorCookie.class);
        if (ec != null) {
            JEditorPane panes[] = ec.getOpenedPanes();
            if (panes != null && panes.length > 0) {
                return panes[0];
            }
        }
        return null;
    }
    
    private static void actionPerformed(JTextComponent target) {
        if (target != null) {
            if (org.netbeans.editor.Utilities.getEditorUI(target).isGlyphGutterVisible()) {
                Caret caret = target.getCaret();
                BookmarkList bookmarkList = BookmarkList.get(target.getDocument());
                bookmarkList.toggleLineBookmark(caret.getDot());

            } else { // Glyph gutter not visible -> just beep
                target.getToolkit().beep();
            }
        }
    }
    
    private static final class BookmarkButtonModel extends JToggleButton.ToggleButtonModel implements PropertyChangeListener, ChangeListener {
        
        private final JTextComponent component;
        private Caret caret;
        private BookmarkList bookmarks;
        private int lastCurrentLineStartOffset = -1;
        
        private PropertyChangeListener bookmarksListener = null;
        private ChangeListener caretListener = null;
        
        public BookmarkButtonModel(JTextComponent component) {
            this.component = component;
            this.component.addPropertyChangeListener(WeakListeners.propertyChange(this, this.component));
            rebuild();
        }

        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getPropertyName() == null || 
                "document".equals(evt.getPropertyName()) || //NOI18N
                "caret".equals(evt.getPropertyName()) //NOI18N
            ) {
                rebuild();
            } else if ("bookmarks".equals(evt.getPropertyName())) { //NOI18N
                lastCurrentLineStartOffset = -1;
                updateState();
            }
        }

        public void stateChanged(ChangeEvent evt) {
            updateState();
        }

        private static boolean isBookmarkOnTheLine(BookmarkList bookmarks, int lineStartOffset) {
            Bookmark bm = bookmarks.getNextBookmark(lineStartOffset - 1, false);
//            System.out.println("offset: " + lineStartOffset + " -> " + bm + (bm == null ? "" : "; bm.getOffset() = " + bm.getOffset()));
            return bm == null ? false : lineStartOffset == bm.getOffset();
        }
        
        private void rebuild() {
            // Hookup the bookmark list
            BookmarkList newBookmarks = BookmarkList.get(component.getDocument());
            if (newBookmarks != bookmarks) {
                if (bookmarksListener != null) {
                    BookmarksApiPackageAccessor.get().removeBookmarkListPcl(bookmarks, bookmarksListener);
                    bookmarksListener = null;
                }

                bookmarks = newBookmarks;

                if (bookmarks != null) {
                    bookmarksListener = WeakListeners.propertyChange(this, bookmarks);
                    BookmarksApiPackageAccessor.get().addBookmarkListPcl(bookmarks, bookmarksListener);
                }
            }
            
            // Hookup the caret
            Caret newCaret = component.getCaret();
            if (newCaret != caret) {
                if (caretListener != null) {
                    caret.removeChangeListener(caretListener);
                    caretListener = null;
                }

                caret = newCaret;

                if (caret != null) {
                    caretListener = WeakListeners.change(this, caret);
                    caret.addChangeListener(caretListener);
                }
            }
            
            lastCurrentLineStartOffset = -1;
            updateState();
        }
        
        private void updateState() {
            Document doc = component.getDocument();
            if (caret != null && bookmarks != null && doc instanceof BaseDocument) {
                try {
                    int currentLineStartOffset = org.netbeans.editor.Utilities.getRowStart((BaseDocument) doc, caret.getDot());
                    if (currentLineStartOffset != lastCurrentLineStartOffset) {
                        lastCurrentLineStartOffset = currentLineStartOffset;
                        boolean selected = isBookmarkOnTheLine(bookmarks, currentLineStartOffset);
                        
//                        System.out.println("updateState: offset=" + currentLineStartOffset + ", hasBookmark=" + selected);
                        
                        setSelected(selected);
                    }
                } catch (BadLocationException e) {
                    // ignore
                    lastCurrentLineStartOffset = -1;
                }
            }
        }
    } // End of BookmarkButtonModel class
    
    private static final class MyGaGaButton extends JToggleButton implements ChangeListener {

        public MyGaGaButton() {

        }

        public void setModel(ButtonModel model) {
            ButtonModel oldModel = getModel();
            if (oldModel != null) {
                oldModel.removeChangeListener(this);
            }

            super.setModel(model);

            ButtonModel newModel = getModel();
            if (newModel != null) {
                newModel.addChangeListener(this);
            }

            stateChanged(null);
        }

        public void stateChanged(ChangeEvent evt) {
            boolean selected = isSelected();
            super.setContentAreaFilled(selected);
            super.setBorderPainted(selected);
        }

        public void setBorderPainted(boolean arg0) {
            if (!isSelected()) {
                super.setBorderPainted(arg0);
            }
        }

        public void setContentAreaFilled(boolean arg0) {
            if (!isSelected()) {
                super.setContentAreaFilled(arg0);
            }
        }
    } // End of MyGaGaButton class

}

