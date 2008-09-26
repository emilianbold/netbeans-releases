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

package org.netbeans.modules.editor.hints.borrowed;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import javax.swing.AbstractListModel;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.SwingUtilities;
import org.netbeans.editor.LocaleSupport;
import org.netbeans.modules.editor.hints.FixData;
import org.netbeans.modules.editor.hints.HintsUI;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.awt.HtmlRenderer;
import org.openide.util.ImageUtilities;

/**
* @author Miloslav Metelka, Dusan Balek
* @version 1.00
*/

public class ListCompletionView extends JList implements ListCellRenderer {

    private final HtmlRenderer.Renderer defaultRenderer = HtmlRenderer.createRenderer();
    private Font font;
    private Icon icon = new ImageIcon (ImageUtilities.loadImage("org/netbeans/modules/editor/hints/resources/suggestion.gif")); // NOI18N
                
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
        defaultRenderer.setParentFocused(true);
        defaultRenderer.setRenderStyle(HtmlRenderer.STYLE_CLIP);
        c.setBackground(list.getBackground());
        return c;
    }
    
    public void setResult(FixData data) {
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

        private FixData data;
        private List<Fix> fixes;
        private boolean computed;
        

        static final long serialVersionUID = 3292276783870598274L;

        public Model(FixData data) {
            this.data = data;
            data.addPropertyChangeListener(this);
            update();
        }

        private synchronized void update() {
            computed = data.isComputed();
            if (computed)
                fixes = data.getSortedFixes();
            else
                data.getSortedFixes();
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
                    HintsUI.getDefault().showPopup(data);
                }
            });
        }
        
    }

}
