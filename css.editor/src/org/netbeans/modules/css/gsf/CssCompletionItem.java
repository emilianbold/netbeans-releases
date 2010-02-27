/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.css.gsf;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.Set;
import javax.swing.ImageIcon;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.modules.csl.api.CompletionProposal;
import org.netbeans.modules.csl.api.ElementHandle;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.csl.api.HtmlFormatter;
import org.netbeans.modules.csl.api.Modifier;
import org.netbeans.modules.csl.spi.DefaultCompletionProposal;
import org.netbeans.modules.css.editor.Property;
import org.netbeans.modules.css.editor.PropertyModel.Element;
import org.netbeans.modules.web.common.api.WebUtils;
import org.openide.util.NbBundle;

/**
 * @todo support for more completion type providers - like colors => subclass this class, remove the kind field, it's just temp. hack
 *
 */
public class CssCompletionItem implements CompletionProposal {

    public static enum Kind {
        PROPERTY, VALUE;
    }

    private int anchorOffset;
    private String value;
    private Kind kind;
    private CssElement element;
    protected boolean addSemicolon;

    public static  CssCompletionItem createValueCompletionItem(CssValueElement element,
            Element value,
            CssCompletionItem.Kind kind, 
            int anchorOffset,
            boolean addSemicolon,
            boolean addSpaceBeforeItem) {

        return new ValueCompletionItem(element, value.toString(), value.getResolvedOrigin(), kind, anchorOffset, addSemicolon, addSpaceBeforeItem);
    }

    public static CssCompletionItem createColorValueCompletionItem(CssValueElement element,
            Element value,
            CssCompletionItem.Kind kind,
            int anchorOffset,
            boolean addSemicolon,
            boolean addSpaceBeforeItem) {

        return new ColorCompletionItem(element, value.toString(), value.getResolvedOrigin(), kind, anchorOffset, addSemicolon, addSpaceBeforeItem);

    }

    public static CssCompletionItem createPropertyNameCompletionItem(CssElement element,
            String value,
            CssCompletionItem.Kind kind,
            int anchorOffset,
            boolean addSemicolon) {

        return new PropertyCompletionItem(element, value, kind, anchorOffset, addSemicolon);
    }

    public static CssCompletionItem createCompletionItem(CssElement element,
            String value,
            CssCompletionItem.Kind kind,
            int anchorOffset,
            boolean addSemicolon) {

        return new CssCompletionItem(element, value, kind, anchorOffset, addSemicolon);
    }

    public static CssCompletionItem createHashColorCompletionItem(CssElement element,
                String value,
                String origin,
                Kind kind,
                int anchorOffset,
                boolean addSemicolon,
                boolean addSpaceBeforeItem,
                boolean usedInCurrentFile) {
        
        return new HashColorCompletionItem(element, value, origin, kind, anchorOffset, addSemicolon, addSpaceBeforeItem, usedInCurrentFile);
    }

    public static CompletionProposal createColorChooserCompletionItem(int anchor, String origin, boolean addSemicolon) {
        return new ColorChooserItem(anchor, origin, addSemicolon);
    }

    public static CssCompletionItem createSelectorCompletionItem(CssElement element,
                String value,
                Kind kind,
                int anchorOffset,
                boolean related) {
        return new SelectorCompletionItem(element, value, kind, anchorOffset, related);

    }


    private CssCompletionItem() {
    }

    private CssCompletionItem(CssElement element, String value, Kind kind, int anchorOffset, boolean addSemicolon) {
        this.anchorOffset = anchorOffset;
        this.value = value;
        this.kind = kind;
        this.element = element;
        this.addSemicolon = addSemicolon;
    }

    @Override
    public int getAnchorOffset() {
        return anchorOffset;
    }

    @Override
    public String getName() {
        return value;
    }

    @Override
    public String getInsertPrefix() {
        return getName();
    }

    @Override
    public String getSortText() {
        return getName();
    }

    @Override
    public ImageIcon getIcon() {
        return null;
    }

    @Override
    public ElementKind getKind() {
        switch (kind) {
            case PROPERTY:
                return ElementKind.METHOD;
            case VALUE:
                return ElementKind.FIELD;
            default:
                return ElementKind.OTHER;
        }
    }

    @Override
    public String getLhsHtml(HtmlFormatter formatter) {
        formatter.appendText(getName());
        return formatter.getText();
    }

    @Override
    public String getRhsHtml(HtmlFormatter formatter) {
        return null;
    }

    @Override
    public Set<Modifier> getModifiers() {
        return Collections.emptySet();
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public boolean isSmart() {
        return false;
    }

    @Override
    public String getCustomInsertTemplate() {
        return null;
    }

    @Override
    public ElementHandle getElement() {
        return element;
    }

    @Override
    public int getSortPrioOverride() {
        return 0;
    }

    private static ImageIcon createIcon(String colorCode) {
        BufferedImage i = new BufferedImage(COLOR_ICON_SIZE, COLOR_ICON_SIZE, BufferedImage.TYPE_4BYTE_ABGR);
        Graphics g = i.createGraphics();

        boolean defaultIcon = colorCode == null;
        if (defaultIcon) {
            //unknown color code, we still want a generic icon
            colorCode = "ffffff"; //NOI18N
        }

        Color transparent = new Color(0x00ffffff, true);
        g.setColor(transparent);
        g.fillRect(0, 0, COLOR_ICON_SIZE, COLOR_ICON_SIZE);

        g.setColor(Color.decode("0x" + colorCode)); //NOI18N
        g.fillRect(COLOR_ICON_SIZE - COLOR_RECT_SIZE,
                COLOR_ICON_SIZE - COLOR_RECT_SIZE - 1,
                COLOR_RECT_SIZE - 1,
                COLOR_RECT_SIZE - 1);

        g.setColor(Color.DARK_GRAY);
        g.drawRect(COLOR_ICON_SIZE - COLOR_RECT_SIZE - 1,
                COLOR_ICON_SIZE - COLOR_RECT_SIZE - 2,
                COLOR_RECT_SIZE,
                COLOR_RECT_SIZE);

        if (defaultIcon) {
            //draw the X inside the icon
            g.drawLine(COLOR_ICON_SIZE - COLOR_RECT_SIZE - 1,
                    COLOR_ICON_SIZE - 2,
                    COLOR_ICON_SIZE - 1,
                    COLOR_ICON_SIZE - COLOR_RECT_SIZE - 2);
        }

        return new ImageIcon(i);
    }

    static class ValueCompletionItem extends CssCompletionItem {

        private String origin; //property name to which this value belongs
        private boolean addSpaceBeforeItem;

        private ValueCompletionItem(CssElement element,
                String value,
                String origin,
                Kind kind,
                int anchorOffset,
                boolean addSemicolon,
                boolean addSpaceBeforeItem) {

            super(element, value, kind, anchorOffset, addSemicolon);
            this.origin = origin;
            this.addSpaceBeforeItem = addSpaceBeforeItem;
        }

        @Override
        public String getInsertPrefix() {
            return (addSpaceBeforeItem && textsStartsWith(getName()) ? " " : "") + getName() + (addSemicolon ? ";" : ""); //NOI18N
        }

        private boolean textsStartsWith(String text) {
            char ch = text.charAt(0);
            return Character.isLetterOrDigit(ch);
        }

        @Override
        public String getRhsHtml(HtmlFormatter formatter) {
            return "<font color=999999>" + origin + "</font>"; //NOI18N
        }

        @Override
        public String getLhsHtml(HtmlFormatter formatter) {
            Property owningProperty = ((CssValueElement) getElement()).property();
            String initialValue = owningProperty.initialValue();
            if (initialValue != null && initialValue.equals(getName())) {
                //initial value
                return "<i>" + super.getLhsHtml(formatter) + "</i>"; //NOI18N
            }

            return super.getLhsHtml(formatter);
        }
    }
    private static final byte COLOR_ICON_SIZE = 16; //px
    private static final byte COLOR_RECT_SIZE = 10; //px

    //XXX fix the CssCompletionItem class so the Value and Property normally subclass it!!!!!!!!!
    static class ColorCompletionItem extends ValueCompletionItem {

        private ColorCompletionItem(CssElement element,
                String value,
                String origin,
                Kind kind,
                int anchorOffset,
                boolean addSemicolon,
                boolean addSpaceBeforeItem) {

            super(element, value, origin, kind, anchorOffset, addSemicolon, addSpaceBeforeItem);
        }

        @Override
        public ImageIcon getIcon() {
            CssColor color = CssColor.getColor(getName());
            return createIcon(color == null ? null : color.colorCode());
        }
    }

    static class HashColorCompletionItem extends ColorCompletionItem {

        private boolean usedInCurrentFile;

        private HashColorCompletionItem(CssElement element,
                String value,
                String origin,
                Kind kind,
                int anchorOffset,
                boolean addSemicolon,
                boolean addSpaceBeforeItem,
                boolean usedInCurrentFile) {

            super(element, value, origin, kind, anchorOffset, addSemicolon, addSpaceBeforeItem);
            this.usedInCurrentFile = usedInCurrentFile;
        }

        @Override
        public ImageIcon getIcon() {
            return createIcon(getName().substring(1)); //strip off the hash
        }

        @Override
        public String getLhsHtml(HtmlFormatter formatter) {
            return new StringBuilder().append(usedInCurrentFile ? "" : "<font color=999999>").
                    append(getName()).append(usedInCurrentFile ? "" : "</font>").toString();
        }

        @Override
        public int getSortPrioOverride() {
            return super.getSortPrioOverride() + (usedInCurrentFile ? 1 : 0);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final HashColorCompletionItem other = (HashColorCompletionItem) obj;

            if ((this.getName() == null) ? (other.getName() != null) : !this.getName().equals(other.getName())) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 37 * hash + (this.getName() != null ? this.getName().hashCode() : 0);
            return hash;
        }
    }

    static class ColorChooserItem extends DefaultCompletionProposal {

        private static final JColorChooser COLOR_CHOOSER = new JColorChooser();
        private Color color;
        private boolean addSemicolon;
        private String origin;

        private ColorChooserItem(int anchor, String origin, boolean addSemicolon) {
            this.anchorOffset = anchor;
            this.addSemicolon = addSemicolon;
            this.origin = origin;
        }

        @Override
        public boolean beforeDefaultAction() {
            JDialog dialog = JColorChooser.createDialog(EditorRegistry.lastFocusedComponent(),
                    NbBundle.getMessage(CssCompletion.class, "MSG_Choose_Color"), //NOI18N
                    true, COLOR_CHOOSER, new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    color = COLOR_CHOOSER.getColor();
                }
            }, new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    color = null;
                }
            });
            dialog.setVisible(true);
            dialog.dispose();

            return color == null;
        }

        @Override
        public int getAnchorOffset() {
            return anchorOffset;
        }

        @Override
        public ElementHandle getElement() {
            return new CssElement(null);
        }

        @Override
        public ElementKind getKind() {
            return getElement().getKind();
        }

        @Override
        public ImageIcon getIcon() {
            Color c = COLOR_CHOOSER.getColor();
            String colorCode = c == null ? "ffffff" : WebUtils.toHexCode(c).substring(1); //strip off the hash
            return createIcon(colorCode);
        }

        @Override
        public String getName() {
            return color == null ? "" : (WebUtils.toHexCode(color) + (addSemicolon ? ";" : "")); //NOI18N
        }

        @Override
        public String getLhsHtml(HtmlFormatter formatter) {
            return "<b>" + NbBundle.getMessage(CssCompletion.class, "MSG_OpenColorChooser") + "</b>"; //NOI18N
        }

        @Override
        public String getRhsHtml(HtmlFormatter formatter) {
            return "<font color=999999>" + origin + "</font>"; //NOI18N
        }

        @Override
        public boolean isSmart() {
            return true;
        }
    }

    static class PropertyCompletionItem extends CssCompletionItem {

        private PropertyCompletionItem(CssElement element,
                String value,
                Kind kind,
                int anchorOffset,
                boolean addSemicolon) {

            super(element, value, kind, anchorOffset, addSemicolon);
        }

        @Override
        public String getInsertPrefix() {
            return super.getInsertPrefix() + ": "; //NOI18N
        }
    }

    static class SelectorCompletionItem extends CssCompletionItem {

        private static final String RELATED_SELECTOR_COLOR = "007c00"; //NOI18N
        private static String GRAY_COLOR_CODE = Integer.toHexString(Color.GRAY.getRGB()).substring(2);
        private boolean related;

       private SelectorCompletionItem(CssElement element,
                String value,
                Kind kind,
                int anchorOffset,
                boolean related) {
            super(element, value, kind, anchorOffset, false);
            this.related = related;
        }

        @Override
        public String getLhsHtml(HtmlFormatter formatter) {
            StringBuilder buf = new StringBuilder();
            if (related) {
                buf.append("<b><font color=#");
                buf.append(RELATED_SELECTOR_COLOR);
            } else {
                buf.append("<font color=#");
                buf.append(GRAY_COLOR_CODE);
            }
            buf.append(">");
            buf.append(getName());
            buf.append("</font>");
            if (related) {
                buf.append("</b>");
            }

            formatter.appendHtml(buf.toString());
            return formatter.getText();
        }

        @Override
        public ElementKind getKind() {
            return ElementKind.RULE;
        }

        @Override
        public int getSortPrioOverride() {
            return super.getSortPrioOverride() + (related ? 1 : 0);
        }
    }
}
