/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.openide.explorer.propertysheet;

import org.openide.util.Utilities;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;


/**
 * A component which can display a description, a title and a button.
 *
 * @author  Tim Boudreau
 */
class DescriptionComponent extends JComponent implements ActionListener, MouseListener {
    private static int fontHeight = -1;
    private JTextArea jta;
    private JLabel lbl;
    private JButton btn;
    private JScrollPane jsc;

    /** Creates a new instance of SplitLowerComponent */
    public DescriptionComponent() {
        init();
    }

    private void init() {
        setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));

        jta = new JTextArea();
        jta.setLineWrap(true);
        jta.setWrapStyleWord(true);
        jta.setOpaque(false);
        jta.setBackground(getBackground());
        jta.setEditable(false);
        jta.setOpaque(false);

        //We use a JScrollPane to suppress the changes in layout that will be
        //caused by adding the raw JTextArea directly - JTextAreas can fire
        //preferred size changes from within their paint methods, leading to
        //cyclic revalidation problems
        jsc = new JScrollPane(jta);
        jsc.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        jsc.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        jsc.setBorder(BorderFactory.createEmptyBorder());
        jsc.setViewportBorder(jsc.getBorder());
        jsc.setOpaque(false);
        jsc.setBackground(getBackground());
        jsc.getViewport().setOpaque(false);

        Font f = UIManager.getFont("Tree.font"); //NOI18N

        if (f != null) {
            jta.setFont(f);
        }

        btn = new JButton();
        btn.addActionListener(this);

        Image help = Utilities.loadImage("org/openide/resources/propertysheet/propertySheetHelp.gif", true); //NOI18N

        ImageIcon helpIcon = new ImageIcon(help); //NOI18N
        btn.setIcon(helpIcon);
        btn.setPreferredSize(new Dimension(helpIcon.getIconWidth(), helpIcon.getIconHeight()));
        btn.setBorder(BorderFactory.createEmptyBorder());
        btn.setBorderPainted(false);
        btn.setFocusable(false);

        lbl = new JLabel("Label"); //NOI18N

        lbl.setFont(new Font(lbl.getFont().getName(), Font.BOLD, lbl.getFont().getSize()));

        add(jsc);
        add(lbl);
        add(btn);
        jta.addMouseListener(this);
        jsc.addMouseListener(this);
        lbl.addMouseListener(this);
        btn.addMouseListener(this);
        jsc.getViewport().addMouseListener(this);
    }

    public void doLayout() {
        Insets ins = getInsets();
        Dimension bttn = btn.getMinimumSize();
        Dimension lbll = lbl.getPreferredSize();

        int height = Math.max(bttn.height, lbll.height);
        int right = getWidth() - (ins.right + bttn.width);

        btn.setBounds(right, ins.top, bttn.width, height);

        lbl.setBounds(ins.left, ins.top, right, height);

        jsc.setBounds(ins.left, height, getWidth() - (ins.left + ins.right), getHeight() - height);
    }

    public void setDescription(String title, String txt) {
        if (title == null) {
            title = "";
        }

        if (txt == null) {
            txt = "";
        }

        lbl.setText(title);

        if (title.equals(txt)) {
            jta.setText("");
        } else {
            jta.setText(txt);
        }
    }

    public void setHelpEnabled(boolean val) {
        btn.setEnabled(val);
    }

    /**
     * Overridden to calculate a font height on the first paint
     */
    public void paint(Graphics g) {
        if (fontHeight == -1) {
            fontHeight = g.getFontMetrics(lbl.getFont()).getHeight();
        }

        super.paint(g);
    }

    /** Overridden to ensure the description area doesn't grow too big
     * with large amounts of text */
    public Dimension getPreferredSize() {
        Dimension d = new Dimension(super.getPreferredSize());

        if (fontHeight > 0) {
            Insets ins = getInsets();
            d.height = Math.max(50, Math.max(d.height, (4 * fontHeight) + ins.top + ins.bottom + 12));
        } else {
            d.height = Math.min(d.height, 64);
        }

        return d;
    }

    public Dimension getMinimumSize() {
        if (fontHeight < 0) {
            return super.getMinimumSize();
        }

        Dimension d = new Dimension(4 * fontHeight, 4 * fontHeight);

        return d;
    }

    public void actionPerformed(ActionEvent actionEvent) {
        PSheet sheet = (PSheet) SwingUtilities.getAncestorOfClass(PSheet.class, this);

        if (sheet != null) {
            sheet.helpRequested();
        }
    }

    private PSheet findSheet() {
        return (PSheet) SwingUtilities.getAncestorOfClass(PSheet.class, this);
    }

    public void mouseClicked(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    /**
     * Forward events that might invoke the popup menu
     */
    public void mousePressed(MouseEvent e) {
        PSheet sh = findSheet();

        if (sh != null) {
            sh.mousePressed(e);
        }
    }

    /**
     * Forward events that might invoke the popup menu
     */
    public void mouseReleased(MouseEvent e) {
        PSheet sh = findSheet();

        if (sh != null) {
            sh.mousePressed(e);
        }
    }
}
