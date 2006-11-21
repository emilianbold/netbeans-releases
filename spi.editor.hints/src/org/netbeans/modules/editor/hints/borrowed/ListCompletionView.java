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

package org.netbeans.modules.editor.hints.borrowed;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import javax.swing.AbstractListModel;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.SwingUtilities;
import org.netbeans.editor.LocaleSupport;
import org.netbeans.modules.editor.hints.HintsUI;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.editor.hints.LazyFixList;
import org.openide.awt.HtmlRenderer;

/**
* @author Miloslav Metelka, Dusan Balek
* @version 1.00
*/

public class ListCompletionView extends JList implements ListCellRenderer {

    private final HtmlRenderer.Renderer defaultRenderer = HtmlRenderer.createRenderer();
    private Font font;
    private Icon icon = new ImageIcon (org.openide.util.Utilities.loadImage("org/netbeans/modules/editor/hints/resources/suggestion.gif")); // NOI18N
                
    public ListCompletionView() {
        setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        font = getFont();
        if (font.getSize() < 15 ) {
            font = font.deriveFont(font.getSize2D() + 1);
        }
        
        setFont( font );
        setCellRenderer( this );
        setBorder( BorderFactory.createEmptyBorder() );
        getAccessibleContext().setAccessibleName(LocaleSupport.getString("ACSN_CompletionView"));
        getAccessibleContext().setAccessibleDescription(LocaleSupport.getString("ACSD_CompletionView"));
    }

    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) { 
        Component c  = defaultRenderer.getListCellRendererComponent( list, value instanceof Fix ? ((Fix) value).getText() : value.toString(), index, isSelected, cellHasFocus );
        defaultRenderer.setIcon( icon );
        defaultRenderer.setHtml(true);
        defaultRenderer.setParentFocused(true);
        defaultRenderer.setRenderStyle(HtmlRenderer.STYLE_CLIP);
        c.setBackground(list.getBackground());
        return c;
    }
    
    public void setResult(LazyFixList data) {
        if (data != null) {
            Model model = new Model(data);
            
            setModel(model);
            if (model.fixes != null && !model.fixes.isEmpty()) {
                setSelectedIndex(0);
            }
        }
    }

    /** Force the list to ignore the visible-row-count property */
    public Dimension getPreferredScrollableViewportSize() {
        return getPreferredSize();
    }

    public void up() {
        if (getModel().getSize() > 0) {
            setSelectedIndex(Math.max(getSelectedIndex() - 1, 0));
            ensureIndexIsVisible(getSelectedIndex());
            repaint();
        }
    }

    public void down() {
        int lastInd = getModel().getSize() - 1;
        if (lastInd >= 0) {
            setSelectedIndex(Math.min(getSelectedIndex() + 1, lastInd));
            ensureIndexIsVisible(getSelectedIndex());
            validate();
        }
    }

    public void pageUp() {
        if (getModel().getSize() > 0) {
            int pageSize = Math.max(getLastVisibleIndex() - getFirstVisibleIndex(), 0);
            int ind = Math.max(getSelectedIndex() - pageSize, 0);

            setSelectedIndex(ind);
            ensureIndexIsVisible(ind);
        }
    }

    public void pageDown() {
        int lastInd = getModel().getSize() - 1;
        if (lastInd >= 0) {
            int pageSize = Math.max(getLastVisibleIndex() - getFirstVisibleIndex(), 0);
            int ind = Math.min(getSelectedIndex() + pageSize, lastInd);

            setSelectedIndex(ind);
            ensureIndexIsVisible(ind);
        }
    }

    public void begin() {
        if (getModel().getSize() > 0) {
            setSelectedIndex(0);
            ensureIndexIsVisible(0);
        }
    }

    public void end() {
        int lastInd = getModel().getSize() - 1;
        if (lastInd >= 0) {
            setSelectedIndex(lastInd);
            ensureIndexIsVisible(lastInd);
        }
    }

    static class Model extends AbstractListModel implements PropertyChangeListener {

        private LazyFixList data;
        private List<Fix> fixes;
        private boolean computed;
        

        static final long serialVersionUID = 3292276783870598274L;

        public Model(LazyFixList data) {
            this.data = data;
            data.addPropertyChangeListener(this);
            update();
        }

        private synchronized void update() {
            computed = data.isComputed();
            if (computed)
                fixes = new ArrayList<Fix>(new LinkedHashSet<Fix>(data.getFixes()));
            else
                data.getFixes();
        }
        
        public synchronized int getSize() {
            return computed ? fixes.size() : 1;
        }

        public synchronized Object getElementAt(int index) {
            if (!computed) {
                return "computing...";
            } else {
                return fixes.get(index);
            }
        }

        public void propertyChange(PropertyChangeEvent evt) {
//            update();
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    HintsUI.getDefault().removePopups();
                    HintsUI.getDefault().showPopup();
                }
            });
        }

    }

}
