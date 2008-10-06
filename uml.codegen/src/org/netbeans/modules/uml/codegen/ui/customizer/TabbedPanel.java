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

package org.netbeans.modules.uml.codegen.ui.customizer;


import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import javax.swing.AbstractButton;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;


/**
 * @author Jan Jancura
 */
public class TabbedPanel extends JPanel
{
    public static int EXPAND_ONE = 1;
    public static int EXPAND_SOME = 2;
    public static int EXPAND_ALL = 3;
    
    public ImageIcon expanded;
    public ImageIcon collapsed;
    
    private static Color bColor = new Color(
        Math.max((int)(SystemColor.control.getRed()   * 0.9), 0),
        Math.max((int)(SystemColor.control.getGreen() * 0.9), 0),
        Math.max((int)(SystemColor.control.getBlue()  * 0.9), 0));
    
    private TabbedPanelModel model;
    private int selectedIndex = -1;
    private Set<Integer> selectedIndexes = new HashSet<Integer>();
    private int expansionPolicy;
    private boolean fill;
    private JComponent selectedComponent;
    private JComponent[] titles;
    private Vector<ActionListener> listeners = new Vector<ActionListener> ();
    
    
    public TabbedPanel(TabbedPanelModel model, int expansionPolicy, boolean fill)
    {
        this.model = model;
        this.expansionPolicy = expansionPolicy;
        this.fill = fill;
        addKeyListener(listener);
        refreshPanels();
        setBorder(new LineBorder(new Color(127, 157, 185)));
        
        ClassLoader loader = getClass().getClassLoader();
        expanded = new ImageIcon(loader.getResource("org/netbeans/modules/uml/codegen/resources/expanded.gif")); // NOI18N
        collapsed = new ImageIcon(loader.getResource("org/netbeans/modules/uml/codegen/resources/collapsed.gif"));  // NOI18N
    }
    
    public int getExpansionPolicy()
    {
        return expansionPolicy;
    }
    
    public int getSelectedIndex()
    {
        return selectedIndex;
    }
    
    public boolean isExpanded(int index)
    {
        if (index == selectedIndex) 
            return true;
        
        return selectedIndexes.contains(new Integer(index));
    }
    
    public void setSelectedIndex(final int index)
    {
        final int ii = index >= 0 ? index : selectedIndex;
        
        if (expansionPolicy == EXPAND_ALL)
        {
            Integer i = new Integer(index);
            if (!selectedIndexes.remove(i))
                selectedIndexes.add(i);
        }
        
        else
            selectedIndex = index;
        
        refreshPanels();
        
        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                if (ii >= 0)
                    titles [ii].requestFocus();
            }
        });
    }
    
    public JComponent getSelectedComponent()
    {
        return selectedComponent;
    }
    
    public String getSelectedCategory()
    {
        int i = getSelectedIndex();
        
        if (i < 0)
            return null;
        
        return 
            (String)model.getCategories().get(i);
    }
    
    public void addActionListener(ActionListener listener)
    {
        listeners.add(listener);
    }
    
    public void removeActionListener(ActionListener listener)
    {
        listeners.remove(listener);
    }
    
    protected void fireActionPerformed(ActionEvent event)
    {
        Vector vector = (Vector) listeners.clone();
        Iterator it = vector.iterator();
        
        while (it.hasNext())
            ((ActionListener) it.next()).actionPerformed(event);
    }
    
    public void refreshPanels()
    {
        removeAll();
        selectedComponent = null;
        
        if (expansionPolicy == EXPAND_ONE && selectedIndex < 0)
            selectedIndex = 0;
        
        setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.weightx = 1.0;
        constraints.fill = GridBagConstraints.BOTH;
        List<String> categories = model.getCategories();
        int i, k = categories.size(), j = 1;
        titles = new JComponent [k];
        
        for (i = 0; i < k; i++)
        {
            String category = categories.get(i);
            
            JComponent comp = createTitleComponent(
                category, model.getToolTip(category), i);
            
            add(comp, constraints);
            titles [i] = comp;
            
            if ((expansionPolicy == EXPAND_ALL &&
                selectedIndexes.contains(new Integer(i))) ||
                i == selectedIndex)
            {
                selectedComponent = model.getPanel(categories.get(i));
                add(selectedComponent, constraints);
                
                if (comp instanceof JLabel)
                {
                    JLabel lable = (JLabel) comp;
                    lable.setLabelFor(selectedComponent);
                }
            }
        }

        constraints.weighty = 1.0;
        JPanel fooPanel = new JPanel();
        fooPanel.setOpaque(false);
        add(fooPanel, constraints);
        revalidate();
        repaint();
        fireActionPerformed(new ActionEvent(this, 0, "selectedIndex")); // NOI18N
    }
    
    protected JComponent createTitleComponent(
        String name, String toolTip, int index)
    {
        JLabel label = new JLabel(
            name,
            index == selectedIndex ? expanded : collapsed,
            JButton.LEFT);
        
        label.setFont(label.getFont().deriveFont(Font.BOLD));
        label.setBackground(bColor);
        label.setOpaque(true);
        label.putClientProperty("index", new Integer(index)); // NOI18N
        label.addMouseListener(listener);
        label.setBorder(new EmptyBorder(1, 1, 1, 1));
        label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return label;
    }
    
    
    private Listener listener = new Listener();
    
    private class Listener implements ActionListener, KeyListener,
        MouseListener
    {
        public void actionPerformed(ActionEvent e)
        {
            AbstractButton b = (AbstractButton) e.getSource();
            int i = ((Integer) b.getClientProperty("index")).intValue(); // NOI18N
            
            if (i == selectedIndex)
                setSelectedIndex(-1);
            
            else
                setSelectedIndex(i);
        }
        
        public void keyTyped(KeyEvent e)
        {}
        
        public void keyPressed(KeyEvent e)
        {}
        public void keyReleased(KeyEvent e)
        {}
        
        public void mouseClicked(MouseEvent e)
        {
            if (!(e.getSource() instanceof JLabel))
                return;
            
            JLabel l = (JLabel) e.getSource();
            int i = ((Integer) l.getClientProperty("index")).intValue(); // NOI18N
            
            if (i == selectedIndex)
            {
                if (expansionPolicy != EXPAND_ONE)
                    setSelectedIndex(-1);
            }
            else
                setSelectedIndex(i);
        }
        
        public void mousePressed(MouseEvent e)
        {}
        public void mouseReleased(MouseEvent e)
        {}
        
        public void mouseEntered(MouseEvent e)
        {
            if (!(e.getSource() instanceof JLabel)) return;
            JLabel l = (JLabel) e.getSource();
            l.setBackground(SystemColor.control);
            revalidate();
            repaint();
        }
        
        public void mouseExited(MouseEvent e)
        {
            if (!(e.getSource() instanceof JLabel)) return;
            JLabel l = (JLabel) e.getSource();
            l.setBackground(bColor);
            revalidate();
            repaint();
        }
    };
}
