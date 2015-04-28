/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */
package org.netbeans.modules.jumpto.common;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import javax.swing.ButtonModel;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JViewport;
import javax.swing.event.ChangeEvent;
import javax.swing.text.AttributeSet;
import javax.swing.text.StyleConstants;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.settings.FontColorSettings;
import org.netbeans.modules.jumpto.EntitiesListCellRenderer;
import org.openide.awt.HtmlRenderer;
import org.openide.util.ImageUtilities;
import org.openide.util.Parameters;

/**
 *
 * @author Tomas Zezula
 */
public final class ItemRenderer<T> extends EntitiesListCellRenderer implements ActionListener {

    public static interface ItemConvertor<T> {
        String getName(T item);
        String getHighlightText(T item);
        String getOwnerName(T item);
        String getProjectName(T item);
        String getFilePath(T item);
        Icon getItemIcon(T item);
        Icon getProjectIcon(T item);
    }

    @StaticResource
    private static final String SAMPLE_ITEM_ICON = "org/netbeans/modules/jumpto/type/sample.png";
    private static final int DARKER_COLOR_COMPONENT = 15;
    private static final int LIGHTER_COLOR_COMPONENT = 80;
    private static final String PROP_HIGHLIGHT="jumpto.highlight";  //NOI18N
    private static final String STRATEGY_BOLD="bold";       //NOI18N
    private static final String STRATEGY_COLOR="color";     //NOI18N
    private static final HighlightStrategy HIGHLIGHT_STRATEGY;
    static {
        final String strategyName = System.getProperty(PROP_HIGHLIGHT);
        if (STRATEGY_COLOR.equals(strategyName)) {
            HIGHLIGHT_STRATEGY = new Background();
        } else {
            HIGHLIGHT_STRATEGY = new Bold();
        }
    }

    private final ItemConvertor<T> convertor;
    private final MyPanel<T> rendererComponent;
    private final JLabel jlName = HIGHLIGHT_STRATEGY.createNameLabel();
    private final JLabel jlOwner = new JLabel();
    private final JLabel jlPrj = new JLabel();
    private final Color fgColor;
    private final Color fgColorLighter;
    private final Color bgColor;
    private final Color bgColorDarker;
    private final Color bgSelectionColor;
    private final Color fgSelectionColor;
    private final JList jList;

    private boolean caseSensitive;
    private Class<T> clzCache;

    public ItemRenderer(
            @NonNull final JList<T> list,
            @NonNull final ButtonModel caseSensitive,
            @NonNull final ItemConvertor<T> convertor) {
        Parameters.notNull("list", list);   //NOI18N
        Parameters.notNull("caseSensitive", caseSensitive); //NOI18N
        Parameters.notNull("convertor", convertor); //NOI18N
        jList = list;
        this.caseSensitive = caseSensitive.isSelected();
        this.convertor = convertor;

        HIGHLIGHT_STRATEGY.resetNameLabel(jlName, jList.getFont());
        Container container = list.getParent();
        if ( container instanceof JViewport ) {
            ((JViewport)container).addChangeListener(this);
            stateChanged(new ChangeEvent(container));
        }
        rendererComponent = new MyPanel<T>(convertor);
        rendererComponent.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.fill = GridBagConstraints.NONE;
        c.weightx = 0;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets (0,0,0,7);
        rendererComponent.add( jlName, c);
        jlOwner.setOpaque(false);
        jlOwner.setFont(list.getFont());
        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 0;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0.1;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets (0,0,0,7);
        rendererComponent.add( jlOwner, c);

        c = new GridBagConstraints();
        c.gridx = 2;
        c.gridy = 0;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.fill = GridBagConstraints.NONE;
        c.weightx = 0;
        c.anchor = GridBagConstraints.EAST;
        rendererComponent.add( jlPrj, c);


        jlPrj.setOpaque(false);
        jlPrj.setFont(list.getFont());


        jlPrj.setHorizontalAlignment(RIGHT);
        jlPrj.setHorizontalTextPosition(LEFT);

        // setFont( list.getFont() );
        fgColor = list.getForeground();
        fgColorLighter = new Color(
                               Math.min( 255, fgColor.getRed() + LIGHTER_COLOR_COMPONENT),
                               Math.min( 255, fgColor.getGreen() + LIGHTER_COLOR_COMPONENT),
                               Math.min( 255, fgColor.getBlue() + LIGHTER_COLOR_COMPONENT)
                              );

        bgColor = new Color( list.getBackground().getRGB() );
        bgColorDarker = new Color(
                                Math.abs(bgColor.getRed() - DARKER_COLOR_COMPONENT),
                                Math.abs(bgColor.getGreen() - DARKER_COLOR_COMPONENT),
                                Math.abs(bgColor.getBlue() - DARKER_COLOR_COMPONENT)
                        );
        bgSelectionColor = list.getSelectionBackground();
        fgSelectionColor = list.getSelectionForeground();
        caseSensitive.addActionListener(this);
    }

    @NonNull
    public Component getListCellRendererComponent(
            @NonNull final JList list,
            @NullAllowed final Object value,
            final int index,
            final boolean isSelected,
            final boolean hasFocus) {

            int height = list.getFixedCellHeight();
            int width = list.getFixedCellWidth() - 1;
            width = width < 200 ? 200 : width;

            Dimension size = new Dimension( width, height );
            rendererComponent.setMaximumSize(size);
            rendererComponent.setPreferredSize(size);
            HIGHLIGHT_STRATEGY.resetNameLabel(jlName, jList.getFont());
            if ( isSelected ) {
                jlName.setForeground(fgSelectionColor);
                jlOwner.setForeground(fgSelectionColor);
                jlPrj.setForeground(fgSelectionColor);
                rendererComponent.setBackground(bgSelectionColor);
            } else {
                jlName.setForeground(fgColor);
                jlOwner.setForeground(fgColorLighter);
                jlPrj.setForeground(fgColor);
                rendererComponent.setBackground( index % 2 == 0 ? bgColor : bgColorDarker );
            }

            final T item = dynamic_cast(value);
            if (item != null) {
                jlName.setIcon(convertor.getItemIcon(item));
                final String formatedName;
                if (isSelected || HIGHLIGHT_STRATEGY.isHighlightAll()) {
                    formatedName = HIGHLIGHT_STRATEGY.highlight(
                            convertor.getName(item),
                            convertor.getHighlightText(item),
                            caseSensitive,
                            isSelected? fgSelectionColor : fgColor);
                } else {
                    formatedName = convertor.getName(item);
                }
                jlName.setText(formatedName);
                jlOwner.setText(convertor.getOwnerName(item));
                setProjectName(jlPrj, convertor.getProjectName(item));
                jlPrj.setIcon(convertor.getProjectIcon(item));
		rendererComponent.setItem(item);
            } else {
                jlName.setText(String.valueOf(value));
            }
            return rendererComponent;
        }

    @Override
    public void stateChanged(@NonNull final ChangeEvent event) {
        final JViewport jv = (JViewport)event.getSource();
        jlName.setText( "Sample" ); // NOI18N
        jlName.setIcon(ImageUtilities.loadImageIcon(SAMPLE_ITEM_ICON, false));
        jList.setFixedCellHeight(jlName.getPreferredSize().height);
        jList.setFixedCellWidth(jv.getExtentSize().width);
    }

    @Override
    public void actionPerformed(@NonNull final ActionEvent e) {
        caseSensitive = ((ButtonModel)e.getSource()).isSelected();
    }

    @CheckForNull
    @SuppressWarnings("unchecked")
    private T dynamic_cast (
            @NullAllowed final Object obj) {
        if (clzCache == null) {
            for (Type type : convertor.getClass().getGenericInterfaces()) {
                if (type instanceof ParameterizedType && ItemConvertor.class == ((ParameterizedType)type).getRawType()) {
                    clzCache = (Class<T>) ((ParameterizedType)type).getActualTypeArguments()[0];
                    break;
                }
            }
        }
        return clzCache != null && clzCache.isInstance(obj) ?
            clzCache.cast(obj) :
            null;
    }

    private static class MyPanel<T> extends JPanel {

        private final ItemConvertor<T> convertor;
	private T item;

        MyPanel(@NonNull final ItemConvertor<T> convertor) {
            this.convertor = convertor;
        }

	void setItem(final  T item) {
	    this.item = item;
	    // since the same component is reused for dirrerent list itens,
	    // null the tool tip
	    putClientProperty(TOOL_TIP_TEXT_KEY, null);
	}

	@Override
	public String getToolTipText() {
	    // the tool tip is gotten from the descriptor
	    // and cached in the standard TOOL_TIP_TEXT_KEY property
	    String text = (String) getClientProperty(TOOL_TIP_TEXT_KEY);
	    if( text == null ) {
                if(this.item != null) {
                    text = convertor.getFilePath(item);
                }
                putClientProperty(TOOL_TIP_TEXT_KEY, text);
	    }
	    return text;
	}
    }

    private static interface HighlightStrategy {
        boolean isHighlightAll();
        @NonNull
        JLabel createNameLabel();
        @NonNull
        void resetNameLabel(@NonNull JLabel nameLabel, @NonNull Font font);
        @NonNull
        String highlight(@NonNull String name, @NonNull String highlightText, boolean caseSensitive, Color color);
    }

    private static class Bold implements HighlightStrategy {

        private final HighlightingNameFormatter nameFormater;

        Bold() {
            nameFormater = HighlightingNameFormatter.Builder.create().buildBoldFormatter();
        }

        @Override
        public boolean isHighlightAll() {
            return true;
        }

        @NonNull
        @Override
        public JLabel createNameLabel() {
            return HtmlRenderer.createLabel();
        }

        @Override
        public void resetNameLabel(
                @NonNull final JLabel nameLabel,
                @NonNull final Font font) {
            ((HtmlRenderer.Renderer)nameLabel).reset();
            nameLabel.setFont(font);
            nameLabel.setOpaque(false);
            ((HtmlRenderer.Renderer)nameLabel).setHtml(true);
            ((HtmlRenderer.Renderer)nameLabel).setRenderStyle(HtmlRenderer.STYLE_TRUNCATE);
        }

        @Override
        public String highlight(
                @NonNull final String name,
                @NonNull final String highlightText,
                final boolean caseSensitive,
                @NonNull final Color color) {
            return nameFormater.formatName(
                        name,
                        highlightText,
                        caseSensitive,
                        color);
        }

    }

    private static class Background implements HighlightStrategy {

        private final HighlightingNameFormatter nameFormater;

        Background() {
            Color back = new Color(255,180,66);
            Color front = Color.BLACK;
            final FontColorSettings colors = MimeLookup.getLookup(MimePath.EMPTY).lookup(FontColorSettings.class);
            if (colors != null) {
                final AttributeSet attrs = colors.getFontColors("inc-search");  //NOI18N
                if (attrs != null) {
                    Object o = attrs.getAttribute(StyleConstants.Background);
                    if (o instanceof Color) {
                        back = (Color) o;
                    }
                    o = attrs.getAttribute(StyleConstants.Foreground);
                    if (o instanceof Color) {
                        front = (Color) o;
                    }
                }
            }
            nameFormater = HighlightingNameFormatter.Builder.create().buildColorFormatter(back, front);
        }

        @Override
        public boolean isHighlightAll() {
            return false;
        }

        @NonNull
        @Override
        public JLabel createNameLabel() {
            return new JLabel();
        }

        @Override
        public void resetNameLabel(
                @NonNull final JLabel nameLabel,
                @NonNull final Font font) {
            nameLabel.setFont(font);
            nameLabel.setOpaque(false);
        }

        @Override
        public String highlight(String name, String highlightText, boolean caseSensitive, Color color) {
            return new StringBuilder("<html><nobr>").   //NOI18N
                    append(nameFormater.formatName(
                            name,
                            highlightText,
                            caseSensitive,
                            color)).
                    append("</nobr>").  //NOI18N
                    toString();
        }
    }
}