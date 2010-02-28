package org.netbeans.lib.terminalemulator.support;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.Vector;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * A clone of o.n.core/src/org/netbeans/beaninfo/editors/FontEditor.java
 */
class FontPanel extends JPanel {

    private final TermOptionsPanel fontPanel;

    private JCheckBox showFixedCheckBox;
    private JTextField tfFont;
    private JTextField tfStyle;
    private JTextField tfSize;
    private JList lFont;
    private JList lStyle;
    private JList lSize;
    private boolean dontSetValue = false;

    private boolean showFixed = true;

    private String fontFamily;
    private int size = 12;
    private int style;
    private Font font = null;

    private String errorMsg = null;

    // List of fonts the user can choose from.
    private Vector<FontDescr> fonts;

    private static class FontDescr {
        private String name;
        private boolean isFixed;

        public FontDescr(String name, boolean isFixed) {
            this.name = name;
            this.isFixed = isFixed;
        }

        public String name() {
            return name;
        }

        public boolean isFixed() {
            return isFixed;
        }
    }

    /**
     * Return true if this font is fixed width.
     * Only the first 256 characters are considered.
     * @param font
     * @return true if this font is fixed width.
     */
    private static boolean isFixedWidth(Component context, Font font) {
        FontMetrics metrics = context.getFontMetrics(font);
        int[] widths = metrics.getWidths();
        int Swidth = widths[0];
        for (int cx = 1; cx < widths.length; cx++) {
            int width = widths[cx];
            if (width == 0) {
                continue;
            } else if (Swidth != width) {
                return false;
            }
        }
        return true;
    }

    private FontDescr descrByName(String fontName) {
        for (FontDescr fontDescr : fonts) {
            if (fontDescr.name().equals(fontName))
                return fontDescr;
        }
        return null;
    }

    private Vector<FontDescr> getFonts(int size, int style) {
        if (fonts == null) {
            String[] fontNames;
            try {
                fontNames = GraphicsEnvironment.getLocalGraphicsEnvironment ().getAvailableFontFamilyNames();
            } catch (RuntimeException e) {
                /* OLD
                if (org.openide.util.Utilities.isMac()) {
                    String msg = "MSG_AppleBug"; //NOI18N
                    DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(msg));
                } else {
                    throw e;
                }
                 */
                throw e;
            }

            // It turns out that "Monospaced" is actually not fixed width
            // in bold style. So if we honor style then under certain
            // circumstances Monospaced will become non-fixed.
            // But we depend on it as a surefire fallback/
            // All I can think of to deal with this is to consider fixedness
            // only under PLAIN.
            // Perhaps SHOULD special-case "Monospaced"?
            // TMP style = Font.PLAIN;

            fonts = new Vector<FontDescr>();
            for (int fx = 0; fx < fontNames.length; fx++) {
                Font f = new Font (fontNames[fx], style, size);
                boolean isFixedWidth = isFixedWidth(this, f);
                if (showFixed) {
                    if (isFixedWidth)
                        fonts.add(new FontDescr(fontNames[fx], isFixedWidth));
                } else {
                    fonts.add(new FontDescr(fontNames[fx], isFixedWidth));
                }
            }
        }
        return fonts;
    }


    static final Integer[] sizes = new Integer [] {
                                       Integer.valueOf (3),
                                       Integer.valueOf (5),
                                       Integer.valueOf (8),
                                       Integer.valueOf (10),
                                       Integer.valueOf (12),
                                       Integer.valueOf (14),
                                       Integer.valueOf (18),
                                       Integer.valueOf (24),
                                       Integer.valueOf (36),
                                       Integer.valueOf (48)
                                   };

    static final String[] styles = new String [] {
                                       Catalog.get("CTL_Plain"),	// NOI18N
                                       Catalog.get("CTL_Bold"),		// NOI18N
                                       Catalog.get("CTL_Italic"),	// NOI18N
                                       Catalog.get("CTL_BoldItalic")	// NOI18N
                                   };

    /**
     * Help render FontDescr's.
     * This renderer does two things:
     * 1) The lFont JList is a list of FontDescrs's and JLIst doesn't know how
     *    to render them by default, so this renderer bridges them.
     * 2) When we have fonts that are not fixed width we'd like to render
     *    them in grey.
     */
    static class MyListCellRenderer implements ListCellRenderer {
        private final ListCellRenderer delegate;

        MyListCellRenderer(ListCellRenderer delegate) {
            this.delegate = delegate;
        }

	@Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            FontDescr fd = (FontDescr) value;
            Component c = delegate.getListCellRendererComponent(list, fd.name(), index, isSelected, cellHasFocus);
            if (fd.isFixed()) {
                c.setForeground(Color.BLACK);
            } else {
                c.setForeground(Color.GRAY);
            }
            return c;
        }

    }

    /*
     * Handle font family selection changes (lFont).
     */
    private final ListSelectionListener lFontListener = new ListSelectionListener() {
	@Override
        public void valueChanged(ListSelectionEvent e) {
            if (!lFont.isSelectionEmpty()) {
                if (getFonts(size, style).size() > 0) {
                    //Mac bug workaround
                    int i = lFont.getSelectedIndex();
                    String newFontName = getFonts(size, style).get(i).name();
                    if (! newFontName.equals(fontFamily)) {
                        tfFont.setText(newFontName);
                        setValue();
                    }
                }
            }
        }
    };

    private void lFontListen(boolean listen) {
        if (listen)
            lFont.addListSelectionListener(lFontListener);
        else
            lFont.removeListSelectionListener(lFontListener);
    }

    FontPanel(Font font, TermOptionsPanel parent) {
        super();
        this.fontPanel = parent;
        this.font = font;
        dontSetValue = false;
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(12, 12, 0, 11));
        if (font == null) {
            if (getFonts(size, style).size() > 0) {
                font = new Font(fonts.get(0).name(), Font.PLAIN, 10);
            } else {
                font = UIManager.getFont("Label.font");		// NOI18N
            }
        }


        lFont = new JList(getFonts(size, style));
        lFont.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        lFont.getAccessibleContext().setAccessibleDescription("ACSD_CTL_Font");	// NOI18N
        lFont.setCellRenderer(new MyListCellRenderer(lFont.getCellRenderer()));

        lStyle = new JList(styles);
        lStyle.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        lStyle.getAccessibleContext().setAccessibleDescription("ACSD_CTL_FontStyle");	// NOI18N
        lSize = new JList(sizes);
        lSize.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        lSize.getAccessibleContext().setAccessibleDescription("ACSD_CTL_Size");	// NOI18N
        tfSize = new JTextField(String.valueOf(font.getSize()));
        tfSize.getAccessibleContext().setAccessibleDescription(lSize.getAccessibleContext().getAccessibleDescription());
        getAccessibleContext().setAccessibleDescription("ACSD_FontCustomEditor");	// NOI18N


        GridBagLayout la = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        setLayout(la);

        c.gridwidth = 1;
        c.weightx = 1.0;
        c.anchor = GridBagConstraints.WEST;

        c.insets = new Insets(0, 0, 10, 0);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridwidth = GridBagConstraints.REMAINDER;
        showFixedCheckBox = new JCheckBox();
        showFixedCheckBox.setSelected(showFixed);
        showFixedCheckBox.setText(Catalog.get("LBL_FixedOnly"));	// NOI18N
        showFixedCheckBox.setMnemonic(Catalog.get("MNM_FixedOnly").charAt(0));	// NOI18N
        showFixedCheckBox.addActionListener(new java.awt.event.ActionListener() {
	    @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                // showFixedActionPerformed(evt);
                showFixed = showFixedCheckBox.isSelected();
                setFontList();
            }
        });
        add(showFixedCheckBox, c);

        c.insets = new Insets(0, 0, 0, 0);
        c.fill = GridBagConstraints.NONE;
        c.gridwidth = 1;
        JLabel l = new JLabel();
        l.setText(Catalog.get("LBL_Font"));	// NOI18N
        l.setDisplayedMnemonic(Catalog.mnemonic("MNM_Font"));	// NOI18N
        l.setLabelFor(lFont);
        la.setConstraints(l, c);
        add(l);


        c.insets = new Insets(0, 5, 0, 0);
        l = new JLabel();
        l.setText(Catalog.get("LBL_FontStyle"));	// NOI18N
        l.setDisplayedMnemonic(Catalog.mnemonic("MNM_FontStyle"));	// NOI18N
        l.setLabelFor(lStyle);
        la.setConstraints(l, c);
        add(l);


        c.insets = new Insets(0, 5, 0, 0);
        c.gridwidth = GridBagConstraints.REMAINDER;
        l = new JLabel();
        l.setText(Catalog.get("LBL_Size"));	// NOI18N
        l.setDisplayedMnemonic(Catalog.mnemonic("MNM_Size"));	// NOI18N
        l.setLabelFor(tfSize);
        la.setConstraints(l, c);
        add(l);


        c.insets = new Insets(5, 0, 0, 0);
        c.gridwidth = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        tfFont = new JTextField(font.getFamily());
        tfFont.setEnabled(false);
        la.setConstraints(tfFont, c);
        add(tfFont);
        c.insets = new Insets(5, 5, 0, 0);
        tfStyle = new JTextField(Catalog.get(parent.getStyleName(font.getStyle())));
        tfStyle.setEnabled(false);
        la.setConstraints(tfStyle, c);
        add(tfStyle);
        c.insets = new Insets(5, 5, 0, 0);
        c.gridwidth = GridBagConstraints.REMAINDER;

        tfSize.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    setValue();
                }
            }
        });

        tfSize.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent evt) {
                if (dontSetValue) {
                    return;
                } else {
                    dontSetValue = true;
                }
                Component c = evt.getOppositeComponent();
                if (c != null) {
                    if (c instanceof JButton) {
                        if (((JButton) c).getText().equals("CTL_OK")) {	// NOI18N
                            setValue();
                        }
                    } else {
                        setValue();
                    }
                }
            }

            @Override
            public void focusGained(FocusEvent evt) {
                dontSetValue = false;
            }
        });

        la.setConstraints(tfSize, c);
        add(tfSize);
        c.gridwidth = 1;
        c.insets = new Insets(5, 0, 0, 0);
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1.0;
        c.weighty = 1.0;
        lFont.setVisibleRowCount(5);
        fontFamily = font.getFamily();
        lFont.setSelectedValue(descrByName(fontFamily), true);
        int is = lFont.getSelectedIndex();
        if (is ==-1) {
            errorMsg = Catalog.format("FMT_FontUnavailable", fontFamily);	// NOI18N
            fontFamily = fonts.get(0).name();
            lFont.setSelectedValue(fonts.get(0), true);
        } else {
            errorMsg = null;
        }

        lFontListen(true);
        JScrollPane sp = new JScrollPane(lFont);
        sp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        la.setConstraints(sp, c);
        add(sp);
        style = font.getStyle();
        lStyle.setVisibleRowCount(5);
        lStyle.setSelectedValue(parent.getStyleName(style), true);

        lStyle.addListSelectionListener(new ListSelectionListener() {
	    @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!lStyle.isSelectionEmpty()) {
                    int i = lStyle.getSelectedIndex();
                    String newStyleName = styles[i];
                    if (! newStyleName.equals(tfStyle.getText())) {
                        tfStyle.setText(styles[i]);
                        setValue();
                    }
                }
            }
        });

        sp = new JScrollPane(lStyle);
        sp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        c.insets = new Insets(5, 5, 0, 0);
        la.setConstraints(sp, c);
        add(sp);
        c.gridwidth = GridBagConstraints.REMAINDER;
        lSize.getAccessibleContext().setAccessibleName(tfSize.getAccessibleContext().getAccessibleName());
        lSize.setVisibleRowCount(5);
        updateSizeList(font.getSize());

        lSize.addListSelectionListener(new ListSelectionListener() {
	    @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!lSize.isSelectionEmpty()) {
                    int i = lSize.getSelectedIndex();
                    tfSize.setText(String.valueOf(sizes[i]));
                    setValue();
                }
            }
        });

        sp = new JScrollPane(lSize);
        sp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        c.insets = new Insets(5, 5, 0, 0);
        la.setConstraints(sp, c);
        add(sp);
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.weighty = 2.0;
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(new TitledBorder(" " + Catalog.get("CTL_Preview") + " "));	// NOI18N
        JPanel pp = new JPanel() {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(150, 60);
            }

            @Override
            public void paint(Graphics g) {
                //          super.paint (g);
                paintValue(g, new Rectangle(0, 0, getSize().width - 1, getSize().height - 1),
                           font(),
			   Catalog.get("MSG_Sample"),	// NOI18N
                           errorMsg);
            }
        };

        p.add("Center", pp);                    // NOI18N
        c.insets = new Insets(12, 0, 0, 0);
        la.setConstraints(p, c);
        add(p);
    }

    private boolean settingList;

    private void setFontList() {
        if (settingList)
            return;
        settingList = true;
        lFontListen(false);
        try {
            fonts = null;
            lFont.setListData(getFonts(size, style));

            // As a result of this the current selected fontName might vanish from
            // the list. In this case we will fall back on the first item in
            // the list.
            lFont.setSelectedValue(descrByName(fontFamily), true);
            int i = lFont.getSelectedIndex();
            if (i ==-1) {
		errorMsg = Catalog.format("FMT_FontUnavailable", fontFamily);	// NOI18N
                fontFamily = fonts.get(0).name();
                lFont.setSelectedValue(fonts.get(0), true);
                tfFont.setText(fontFamily);
            }
        } finally {
            lFontListen(true);
            settingList = false;
        }
    }

    public Font font() {
        return font;
    }

    public void paintValue(Graphics g, Rectangle rectangle, Font font, String sample, String errorMsg) {
        Font originalFont = g.getFont();
        // Fix of 21713, set default value
        // LATER if ( font == null ) setValue( null );
        Font paintFont = font == null ? originalFont : font;
        // NOI18N
        assert paintFont != null : "paintFont must exist.";
        FontMetrics fm = g.getFontMetrics(paintFont);
        if (fm.getHeight() > rectangle.height) {
            /* LATER
            if (Utilities.isMac()) {
            // don't use deriveFont() - see #49973 for details
            paintFont = new Font(paintFont.getName(), paintFont.getStyle(), 12);
            } else
             */
            {
                paintFont = paintFont.deriveFont(12.0F);
            }
            fm = g.getFontMetrics(paintFont);
        }
        g.setFont(paintFont);
        int height = (rectangle.height - fm.getHeight()) / 2 + fm.getAscent();
        g.drawString(sample == null ? "null" : sample,	// NOI18N
                     rectangle.x, rectangle.y + height);
        if (errorMsg != null) {
            Color originalColor = g.getColor();
            g.setColor(Color.RED);
            g.drawString(errorMsg,
                         rectangle.x, rectangle.y + height + fm.getAscent());
            g.setColor(originalColor);

        }
        g.setFont(originalFont);
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(400, 350);
    }

    private void updateSizeList(int size) {
        if (Arrays.asList(sizes).contains(Integer.valueOf(size))) {
            lSize.setSelectedValue(Integer.valueOf(size), true);
        } else {
            lSize.clearSelection();
        }
    }

    /**
     * Called whenever any of the controls on the panel gets an event
     */
    private void setValue() {

        boolean sizeChanged = false;
        boolean styleChanged = false;

        int oldSize = size;
        size = 12;
        try {
            size = Integer.parseInt(tfSize.getText());
            if (size <= 0) {
                IllegalArgumentException iae = new IllegalArgumentException();
                /* LATER
                UIExceptions.annotateUser (iae, null,
                size == 0 ? "CTL_InvalidValueWithParam", tfSize.getText () : // NOI18N
                "CTL_NegativeSize", // NOI18N
                null, null);
                tfSize.setText (String.valueOf (font.getSize ()));
                 */
                throw iae;
            }
            updateSizeList(size);
        } catch (NumberFormatException e) {
            /* LATER
            UIExceptions.annotateUser (e, null,
            "CTL_InvalidValueWithExc", // NOI18N
            null, null);
            tfSize.setText (String.valueOf (font.getSize ()));
             */
            throw e;
        }
        sizeChanged = size != oldSize;

        int oldStyle = style;
        int i = lStyle.getSelectedIndex();
        style = Font.PLAIN;
        switch (i) {
            case 0:
                style = Font.PLAIN;
                break;
            case 1:
                style = Font.BOLD;
                break;
            case 2:
                style = Font.ITALIC;
                break;
            case 3:
                style = Font.BOLD | Font.ITALIC;
                break;
        }
        styleChanged = style != oldStyle;


        fontFamily = tfFont.getText();
        if (sizeChanged || styleChanged) {
            errorMsg = null;
            setFontList();
        } else {
            errorMsg = null;
        }
        // TMP FontEditor.this.setValue (new Font (tfFont.getText (), ii, size));
        font = new Font(fontFamily, style, size);
        invalidate();
        Component p = getParent();
        if (p != null) {
            p.validate();
        }
        repaint();
    }
}
