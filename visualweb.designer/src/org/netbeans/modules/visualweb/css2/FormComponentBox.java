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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.visualweb.css2;

import org.netbeans.modules.visualweb.api.designer.cssengine.CssProvider;
import org.netbeans.modules.visualweb.api.designer.markup.MarkupService;
import org.netbeans.modules.visualweb.designer.CssUtilities;
import org.netbeans.modules.visualweb.designer.ImageCache;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import javax.swing.AbstractButton;

import javax.swing.CellRendererPane;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JViewport;
import javax.swing.ListModel;
import javax.swing.LookAndFeel;
import javax.swing.SwingConstants;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.plaf.ButtonUI;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.ScrollPaneUI;
import javax.swing.plaf.TextUI;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.PlainDocument;

import org.openide.ErrorManager;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.netbeans.modules.visualweb.designer.DesignerUtils;
import org.netbeans.modules.visualweb.designer.WebForm;
import org.netbeans.modules.visualweb.api.designer.cssengine.XhtmlCss;
import org.netbeans.modules.visualweb.designer.html.HtmlAttribute;
import org.netbeans.modules.visualweb.designer.html.HtmlTag;
import org.w3c.dom.Text;


/**
 * Box that visually represents a form component - such as a button,
 * dropdown, checkbox, text area, or listbox.
 *
 * <p>
 * Portions of this code taken from javax.swing.text.html.FormView
 * <p>
 * @todo "checked" is not supported: 'the value "radio" or "checkbox",
 * this boolean attribute specifies that the button is on.'
 * @todo the "size" attribute should be supported: it sets the pixel
 * width - unless it's a password field and text field in which case
 * it refers to text field.
 * @todo Support the "button" element; it's similar to input type="submit",
 * except the button label comes from the text node children of the
 * button instead of from an attribute.
 * @todo Fonts
 * @todo Background painting for for example radios/checkboxes:
 *
 * @author Timothy Prinzing
 * @author Sunita Mani
 * @author Tor Norbye
 */
public class FormComponentBox extends ContainerBox {
    /** Show Mac-style buttons (on the mac) instead of firefox/cross platform style buttons? */
    private static final boolean MAC_STYLE_BUTTONS = System.getProperty("rave.macButtons") != null; // NOI18N
    private static boolean sCreateChildren; // static used before object create

    /** UIDefaults to use when creating native components */
    private static UIDefaults uidef = null;
    private JComponent createdC;
    private boolean nonrectangular;
    private boolean createChildren;

    //private static int id = 0;
    private Image image = null;
    private AffineTransform transform = new AffineTransform();
    private boolean attempted = false;
    private int imageWidth = -1;
    private int imageHeight = -1;
//    private boolean focus = false;
    private int baseline;

    private FormComponentBox(WebForm webform, Element element, JComponent createdC,
        BoxType boxType, boolean inline, boolean replaced, boolean createChildren) {
        super(webform, element, boxType, inline, replaced);
        this.createdC = createdC;
        this.nonrectangular = !createdC.isOpaque();
        this.createChildren = createChildren;
//        focus = DesignerActions.isFocus(getDesignBean());
    }

    /**
     *  Create a FormComponentBox representing the given element's
     *  component. May return null if it's an invisible component
     *  (e.g. input hidden).
     *
     * @param element The element this inline box is associated with
     * @param x The x position where the inline box should be rendered
     *        relative to the containing block.
     * @param y The y position where the inline box should be rendered
     *        relative to the containing block.
     * @param width The width of this inline box.
     * @param height The height of this inline box.
     */
    public static CssBox getBox(WebForm webform, Element element, HtmlTag tag, BoxType boxType,
        boolean inline, boolean replaced) {
        // <input type="image"/> special case: don't use a FormComponentBox,
        // use an image box instead
        if ((tag == HtmlTag.INPUT) || (tag == HtmlTag.BUTTON)) {
            String type = element.getAttribute(HtmlAttribute.TYPE);

            if ("image".equals(type)) { // NOI18N

                Container container = webform.getPane();

                // This works because the image button is also supposed to
                // use the "src" and "alt" attributes to locate image and
                // alternate text for the button
                return ImageBox.getImageBox(webform, element, container, boxType, inline);
            } else if (("submit".equals(type) || "button".equals(type) || "reset".equals(type)) &&
                    // No gradient buttons on the mac - Safari doesn't do it, probably
                // intentionally
                (!MAC_STYLE_BUTTONS || (Utilities.getOperatingSystem() != Utilities.OS_MAC))) {
                // See if we need to create a custom-painted button. This is
                // the case if the user specifies a background-image (normal
                // background-color we can handle in the normal button code.)
                // TODO - decide if we need to use custom button painting code
                // for unusual borders too.
//                ImageIcon bgImage = BackgroundImagePainter.getBackgroundImage(webform, element);
//                URL imageUrl = CssBoxUtilities.getBackgroundImageUrl(element, webform.getMarkup().getBase());
//                URL imageUrl = CssProvider.getEngineService().getBackgroundImageUrlForElement(element, webform.getMarkup().getBase());
                URL imageUrl = CssProvider.getEngineService().getBackgroundImageUrlForElement(element, webform.getBaseUrl());
                ImageIcon bgImage;
                if (imageUrl != null) {
                    // XXX Revise this caching impl.
//                    ImageCache imageCache = webform.getDocument().getImageCache();
                    ImageCache imageCache = webform.getImageCache();
                    bgImage = imageCache.get(imageUrl);
                    if (bgImage == null) {
                        bgImage = new ImageIcon(imageUrl);
                        imageCache.put(imageUrl, bgImage);
                    }
                } else {
                    bgImage = null;
                }

                if (bgImage != null) {
                    // XXX TODO - provide a facility to pass in the background image?
                    CssBorder border =
                        CssBorder.getBorder(element, 2, CssBorder.STYLE_OUTSET,
                            CssBorder.FRAME_UNSET);
                    String label = getButtonLabel(element, type);

                    return new CustomButtonBox(webform, element, boxType, label, border, inline);
                }
            }
        }

        // The following method will also as a side effect set the
        // sCreateChildren flag which we'll use to seed the
        // "createChildren" flag for the box we create
        JComponent proxyComp = createComponent(tag, webform, element);

        if (proxyComp == null) { // invisible component
            return null;
        }

        return new FormComponentBox(webform, element, proxyComp, boxType, inline, replaced,
            sCreateChildren);
    }

    protected void initialize() {
        super.initialize(); // hmmm this gets done repeatedly... during layout (see LineBoxGroup's relayout super call)

        if (createdC != null) { // might be null for inputHidden for example

            JComponent c = createdC;

            // Make background color visible. 
            // This may not have the desired effect on for example textfields. Check!
            //if (bg != null) {
            //    c.setOpaque(false);
            //}
            // Set dimensions and colors according to CSS
            // XXX This doesn't -quite- work; somehow the component may end
            // up a bit smaller than I ask it to be - I think the borders
            // are getting subtracted or something like that.
            Element fel = getElement();

//            int width = CssLookup.getLength(fel, XhtmlCss.WIDTH_INDEX);
            int width = CssUtilities.getCssLength(fel, XhtmlCss.WIDTH_INDEX);

            if ((width != AUTO) && (c instanceof JTextField)) {
                // If a width is defined for the text field,
                // wed need to reset the columns property since
                // JTextField.getPreferredSize overrides the
                // preferred setting if columns != 0
                ((JTextField)c).setColumns(0);
            }

//            int height = CssLookup.getLength(fel, XhtmlCss.HEIGHT_INDEX);
            int height = CssUtilities.getCssLength(fel, XhtmlCss.HEIGHT_INDEX);

            if ((width != AUTO) || (height != AUTO)) {
                if (width == AUTO) {
                    width = (int)c.getPreferredSize().getWidth();
                } else if (height == AUTO) {
                    height = (int)c.getPreferredSize().getHeight();
                }

                c.setPreferredSize(new Dimension(width, height));
            }

            // For some components we wrap the real component inside a JScrollPane;
            // JTextArea is one example. However, fonts and colors should be set on 
            // the JTextArea itself, not the JScrollPane
            if (c instanceof JScrollPane) {
                c = (JComponent)((JScrollPane)c).getViewport().getView();
            }

            // Colors and fonts should only be set if they have been set
            // in CSS -on- this element, NOT inherited (even though color
            // is an inherited property).  This appears to be against
            // what the CSS spec says, but browsers do quite a few weird
            // things with the form input components
            Element element = getElement();
//            XhtmlCssEngine engine = CssLookup.getCssEngine(element);

//            if (!engine.isInheritedValue((RaveElement)element, XhtmlCss.COLOR_INDEX)) {
            if (!CssProvider.getEngineService().isInheritedStyleValueForElement(element, XhtmlCss.COLOR_INDEX)) {
//                Color color = CssLookup.getColor(element, XhtmlCss.COLOR_INDEX);
                Color color = CssProvider.getValueService().getColorForElement(element, XhtmlCss.COLOR_INDEX);

                if (color != null) {
                    c.setForeground(color);
                }
            }

            //bg = Css.getColor(element, XhtmlCssEngine.BACKGROUND_COLOR_INDEX); 
            // We've already looked up the background in the box constructor
            if (bg != null) {
                c.setBackground(bg);
            }

            // XXX should I check to see if a particular font is defined first?
            // Or I guess one always would be through style cascade/inheritance?
//            if (!engine.isInheritedValue((RaveElement)element, XhtmlCss.FONT_SIZE_INDEX)) {
            if(!CssProvider.getEngineService().isInheritedStyleValueForElement(element, XhtmlCss.FONT_SIZE_INDEX)) {
                // check font-family, font-variant etc. too
//                Font font = CssLookup.getFont(fel, DesignerSettings.getInstance().getDefaultFontSize());
//                Font font = CssProvider.getValueService().getFontForElement(fel, DesignerSettings.getInstance().getDefaultFontSize(), Font.PLAIN);
                Font font = CssUtilities.getDesignerFontForElement(fel, getTextFromComponent(c), webform.getDefaultFontSize());
                setFontForComponent(c, font);
            }
        }

        if (createdC != null) {
            getImage(); // initialize sizes
            contentWidth = width = getWidth();
            contentHeight = height = getHeight();
            baseline = height;

            Font font = createdC.getFont();

            // XXX #109310 The baseline is at the bottom.
//            // Set the baseline alignment for all form components except Text Areas
//            // (these are not baseline aligned)
//            if ((tag != HtmlTag.TEXTAREA) && (font != null)) {
//                // This returns 0 for some reason:
//                //baseline = font.getBaselineFor('j');
////                FontMetrics metrics = Toolkit.getDefaultToolkit().getFontMetrics(font);
//                FontMetrics metrics = DesignerUtils.getFontMetrics(font);
//                baseline = metrics.getHeight() - metrics.getDescent();
//
//                // Here I wanted to look up the component's insets, and border insets
//                // and add in the top offsets - but that doesn't work right. Various
//                // look and feels are returning strange values here, so components end
//                // up getting aligned in a weird way. Therefore, instead I just use 
//                // half leading:
//                baseline += ((height - baseline) / 2);
//            }
        }
    }
    
    private static void setFontForComponent(JComponent component, Font font) {
        if (component instanceof JScrollPane) {
            JScrollPane scrollPane = (JScrollPane)component;
            JViewport viewport = scrollPane.getViewport();
            if (viewport != null) {
                Component comp = viewport.getView();
                if (comp != null) {
                    comp.setFont(font);
                }
            }
        } else {
            component.setFont(font);
        }
    }
    
    /** Tries to retrieve the text to be shown from the component.
     * It checks AbstractButton, JTextComponent, JList and content of JScrollPane. */
    private static String getTextFromComponent(Component component) {
        if (component instanceof AbstractButton) {
            return ((AbstractButton)component).getText();
        } else if (component instanceof JTextComponent) {
            return ((JTextComponent)component).getText();
        } else if (component instanceof JComboBox) {
            JComboBox comboBox = (JComboBox)component;
            ComboBoxModel comboBoxModel = comboBox.getModel();
            List<String> strings = new ArrayList<String>();
            for (int i = 0; i < comboBoxModel.getSize(); i++) {
                Object item = comboBoxModel.getElementAt(i);
                if (item != null) {
                    // XXX Relying upon the items are strings.
                    strings.add(item.toString());
                }
            }
            StringBuilder sb = new StringBuilder();
            for (String string : strings) {
                sb.append(string);
                sb.append('\n'); // NOI18N
            }
            return sb.toString();
        } else if (component instanceof JList) {
            JList list = (JList)component;
            ListModel listModel = list.getModel();
            List<String> strings = new ArrayList<String>();
            for (int i = 0; i < listModel.getSize(); i++) {
                Object item = listModel.getElementAt(i);
                if (item != null) {
                    // XXX Relying upon the items are strings.
                    strings.add(item.toString());
                }
            }
            StringBuilder sb = new StringBuilder();
            for (String string : strings) {
                sb.append(string);
                sb.append('\n'); // NOI18N
            }
            return sb.toString();
        } else if (component instanceof JScrollPane) {
            JScrollPane scrollPane = (JScrollPane)component;
            JViewport viewport = scrollPane.getViewport();
            if (viewport == null) {
                return null;
            }
            return getTextFromComponent(viewport.getView());
        }
        return null;
    }

    public boolean isBorderSizeIncluded() {
        return true;
    }

    public Insets getCssSizeInsets() {
        return new Insets(0, 0, 0, 0);
    }

    /**
     * Used to indicate if the maximum span should be the same as the
     * preferred span. This is used so that the Component's size doesn't
     * change if there is extra room on a line. The first bit is used for
     * the X direction, and the second for the y direction.
     */

    //private short maxIsPreferred;

    /**
     * COPIED FROM javax.swing.text.html.FormView: then modified for
     * Rave purposes.... (for example, no live models)
     * <p>
     * Create the component.  This is basically a
     * big switch statement based upon the tag type
     * and html attributes of the associated element.
     */
    private static JComponent createComponent(HtmlTag tag, WebForm webform, Element element) {
        /*
        type = text|password|checkbox|radio|submit|reset|file|hidden|image|button [CI]
        type = text|password|checkbox|radio|submit|reset|file|hidden|image|button [CI]
        This attribute specifies the type of control to create. The default value for this attribute is "text".
        name = cdata [CI]
        This attribute assigns the control name.
        value = cdata [CA]
        This attribute specifies the initial value of the control. It is optional except when the type attribute has the value "radio" or "checkbox".
        size = cdata [CN]
        This attribute tells the user agent the initial width of the control. The width is given in pixels except when type attribute has the value "text" or "password". In that case, its value refers to the (integer) number of characters.
        maxlength = number [CN]
        When the type attribute has the value "text" or "password", this attribute specifies the maximum number of characters the user may enter. This number may exceed the specified size, in which case the user agent should offer a scrolling mechanism. The default value for this attribute is an unlimited number.
        checked [CI]
        When the type attribute has the value "radio" or "checkbox", this boolean attribute specifies that the button is on. User agents must ignore this attribute for other control types.
        src = uri [CT]
        When the type attribute has the value "image", this attribute specifies the location of the image to be used to decorate the graphical submit button.

        text
        Creates a single-line text input control.
        password
        Like "text", but the input text is rendered in such a way as to hide the characters (e.g., a series of asterisks). This control type is often used for sensitive input such as passwords. Note that the current value is the text entered by the user, not the text rendered by the user agent.

        Note. Application designers should note that this mechanism affords only light security protection. Although the password is masked by user agents from casual observers, it is transmitted to the server in clear text, and may be read by anyone with low-level access to the network.
        checkbox
        Creates a checkbox.
        radio
        Creates a radio button.
        submit
        Creates a submit button.
        image
        Creates a graphical submit button. The value of the src attribute specifies the URI of the image that will decorate the button. For accessibility reasons, authors should provide alternate text for the image via the alt attribute.

        When a pointing device is used to click on the image, the form is submitted and the click coordinates passed to the server. The x value is measured in pixels from the left of the image, and the y value in pixels from the top of the image. The submitted data includes name.x=x-value and name.y=y-value where "name" is the value of the name attribute, and x-value and y-value are the x and y coordinate values, respectively.

        If the server takes different actions depending on the location clicked, users of non-graphical browsers will be disadvantaged. For this reason, authors should consider alternate approaches:

        * Use multiple submit buttons (each with its own image) in place of a single graphical submit button. Authors may use style sheets to control the positioning of these buttons.
        * Use a client-side image map together with scripting.

        reset
        Creates a reset button.
        button
        Creates a push button. User agents should use the value of the value attribute as the button's label.
        hidden
        Creates a hidden control.
        file
        Creates a file select control. User agents may use the value of the value attribute as the initial file name.



        */
        Element el = element;
        JComponent c = null;

        if ((tag == HtmlTag.INPUT) || (tag == HtmlTag.BUTTON)) {
            c = createInputComponent(tag, el);
        } else if (tag == HtmlTag.SELECT) {
            // <select> cannot have children. It has <option>
            // children that should go into the list, but we process
            // those separately.
            sCreateChildren = false;

            int size = HtmlAttribute.getIntegerAttributeValue(el, HtmlAttribute.SIZE, 1);
            boolean multiple = el.hasAttribute(HtmlAttribute.MULTIPLE);

            if ((size > 1) || multiple) {
                Vector<String> v = new Vector<String>();
                int[] selected = populateOptions(element, v);

                JList list;
                if (v.size() == 0) {
                    // XXX #6195204 Faking default component.
                    if (size == 1) {
                        // XXX No scrollbar will be shown, so make the comp wider.
                        v.addElement("    " + "    "); // NOI18N // XXX Fake width.
                    } else {
                        v.addElement("    "); // NOI18N // XXX Fake width.
                    }
                    // XXX Why DefaultComboBoxModel?
                    list = new JList(new DefaultComboBoxModel(v));
                    list.setVisibleRowCount(size > 0 ? size : 2);
                } else {
                    // XXX Why DefaultComboBoxModel?
                    list = new JList(new DefaultComboBoxModel(v));
                    list.setVisibleRowCount(size);
                }

                if ((selected != null) && (selected.length > 0)) {
                    list.setSelectedIndices(selected);
                }

                // List should not respond to selection!
                //list.setSelectionModel((ListSelectionModel)model);
                JScrollPane jp = new JScrollPane(list);
                if (list.getVisibleRowCount() > 1) {
                    // XXX Show scrollbars when more than one row available.
                    jp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
                }
                
                c = jp;

                ScrollPaneUI ui = (ScrollPaneUI)createNativeUI(c);

                if (ui != null) {
                    jp.setUI(ui);
                }
            } else {
                Vector<String> v = new Vector<String>();
                int[] selected = populateOptions(element, v);

                JComboBox jc;
                if (v.size() == 0) {
                    // XXX #6195204 Faking default component.
                    v.addElement("    "); // NOI18N // XXX Fake width.
                    // XXX Why DefaultComboBoxModel?
                    jc = new JComboBox(new DefaultComboBoxModel(v));
                    jc.setEditable(false);
                } else {
                    // XXX Why DefaultComboBoxModel?
                    jc = new JComboBox(new DefaultComboBoxModel(v));
                    jc.setEditable(false);
                }
                
                c = jc;

                if ((selected != null) && (selected.length > 0)) {
                    jc.setSelectedIndex(selected[0]);
                }

                //maxIsPreferred = 3;

                /*
                // J1 HACK: Make sure the combo box is roughly the same
                // size as in the browser. Screenshots from 1280x1024 and
                // 1400x1050 resolutions showed the width of the combo
                // to be roughly 140-145 pixels.
                Dimension pfz = c.getPreferredSize();
                if (c.getWidth() < 142) {
                    pfz.setSize(142, pfz.getHeight());
                    c.setPreferredSize(pfz);
                }
                */
            }
        } else if (tag == HtmlTag.TEXTAREA) {
            // <textarea> cannot have children. It has content
            // text that should go into the text area, but we that
            // separately.
            sCreateChildren = false;

            PlainDocument sdoc = new PlainDocument();
            StringBuffer sb = new StringBuffer();
            populateText(element, sb);

            if (sb.length() == 0) {
                sb.append("          "); // ensure some width
            }

            try {
                sdoc.insertString(0, sb.toString(), null);
            } catch (javax.swing.text.BadLocationException e) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            }

            JTextArea area = new JTextAreaColumnWidth(sdoc);

            int rows = HtmlAttribute.getIntegerAttributeValue(el, HtmlAttribute.ROWS, 3);
            area.setRows(rows);

            int cols = HtmlAttribute.getIntegerAttributeValue(el, HtmlAttribute.COLS, 22);

            //maxIsPreferred = 3;
            // or check CSS attribute first!
            area.setColumns(cols);

            TextUI tui = (TextUI)createNativeUI(area);

            if (tui != null) {
                area.setUI(tui);
            }

            // On Windows, show a vertical scrollbar by default, since
            // that's what IE does. Mozilla does not so on other platforms
            // we'll leave out the vertical scrollbar.
            int vertical;

            if (org.openide.util.Utilities.isWindows()) {
                vertical = JScrollPane.VERTICAL_SCROLLBAR_ALWAYS;
            } else {
                vertical = JScrollPane.VERTICAL_SCROLLBAR_NEVER;
            }

            JScrollPane jc =
                new JScrollPane(area, 
                // Even if vertical == VERTICAL_SCROLLBAR_NEVER,
                // we use a scrollpane because we want the 
                // inset border it provides
                vertical, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            c = jc;

            ScrollPaneUI ui = (ScrollPaneUI)createNativeUI(c);

            if (ui != null) {
                jc.setUI(ui);
            }
        }

        if (c != null) {
            c.setAlignmentY(1.0f);
        }

        // c might be null for a <input type="hidden"/> for example...
        return c;
    }

    /** Find all the <option> children of this tag, and add the
     * text content below each <option> as a separate item
     * of the given Vector. (After whitespace is stripped, of course.)
     * Return the set of selected indices.
     */
    public static int[] populateOptions(Element element, Vector<String> v) {
        // <markup_separation>
//        MarkupService markupService = MarkupServiceProvider.getDefault();
        // </markup_separation>
        List<Integer> selected = new ArrayList<Integer>();
        NodeList list = element.getChildNodes();
        int len = list.getLength();

        for (int i = 0; i < len; i++) {
            Node child = list.item(i);

            if (child.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }

            Element option = (Element)child;

            if (!option.getTagName().equals(HtmlTag.OPTION.getTagName())) {
                continue;
            }

            // Found an option
            NodeList list2 = option.getChildNodes();
            int len2 = list2.getLength();
            StringBuffer sb = new StringBuffer();

            for (int j = 0; j < len2; j++) {
                Node child2 = list2.item(j);

                if ((child2.getNodeType() != Node.TEXT_NODE) &&
                        (child2.getNodeType() != Node.CDATA_SECTION_NODE)) {
                    continue;
                }

                String nodeVal = child2.getNodeValue();

                if (nodeVal != null) {
                    nodeVal = nodeVal.trim();

//                    RaveText textNode = (child2 instanceof RaveText) ? (RaveText)child2 : null;
//                    if ((textNode != null) && textNode.isJspx()) {
                    Text textNode = (child2 instanceof Text) ? (Text)child2 : null;
                    if (textNode != null && MarkupService.isJspxNode(textNode)) {
                        // <markup_separation>
//                        nodeVal = markupService.expandHtmlEntities(nodeVal, true, element);
                        // ====
//                        nodeVal = InSyncService.getProvider().expandHtmlEntities(nodeVal, true, element);
                        nodeVal = WebForm.getDomProviderService().expandHtmlEntities(nodeVal, true, element);
                        // </markup_separation>
                    } // ELSE: regular entity fixing?

                    sb.append(nodeVal);

                    // XXX I should be able to bail here - for combo
                    // boxes I only show the first item! (There's no
                    // way for the user to open the menu). However,
                    // for things like a multi select, you need to
                    // show possibly multiple choices, so perhaps pass
                    // in a max count?
                }
            }

            if (sb.length() > 0) {
                // Is this item selected too?
                Attr attr = option.getAttributeNode(HtmlAttribute.SELECTED);

                if (attr != null) {
                    selected.add(new Integer(v.size()));
                }

                v.addElement(sb.toString());
            }
        }

        if (selected != null) {
            int[] result = new int[selected.size()];

            for (int i = 0, n = selected.size(); i < n; i++) {
                result[i] = (selected.get(i)).intValue();
            }

            return result;
        }

        return null;
    }

    /** Find all the text children of this tag, and add the
     * text into the string buffer; whitespace is trimmed.
     */
    private static void populateText(Element element, StringBuffer sb) {
        NodeList list = element.getChildNodes();
        int len = list.getLength();

        for (int i = 0; i < len; i++) {
            Node child = list.item(i);

            if ((child.getNodeType() != Node.TEXT_NODE) &&
                    (child.getNodeType() != Node.CDATA_SECTION_NODE)) {
                continue;
            }

            String nodeVal = child.getNodeValue();

            if (nodeVal != null) {
                nodeVal = nodeVal.trim();
                sb.append(nodeVal);
            }
        }
    }

    protected void createChildren(CreateContext context) {
        if (createChildren) {
            super.createChildren(context);
        }
    }

    protected boolean isOpaqueBox() {
        return true;
    }

    // XXX #6473220 The default size of the input components seems to be 20.
    private static final int DEFAULT_INPUT_COMPONENT_SIZE = 20;
    
    /**
     * COPIED FROM javax.swing.text.html.FormView:
     * <p>
     * Creates a component for an &lt;INPUT&gt; element based on the
     * value of the "type" attribute.
     *
     * @param set of attributes associated with the &lt;INPUT&gt; element.
     * @param model the value of the StyleConstants.ModelAttribute
     * @return the component.
     */
    private static JComponent createInputComponent(HtmlTag tag, Element el) {
        JComponent c = null;
        String type = el.getAttribute(HtmlAttribute.TYPE);

        // <input> content should be added in
        sCreateChildren = true;

        // Default is text field
        if ((tag == HtmlTag.INPUT) &&
                ((type == null) || (type.length() == 0) || type.equals("text"))) { // NOI18N

            JTextField field = new JTextFieldColumnWidth();
            c = field;

            int size = HtmlAttribute.getIntegerAttributeValue(el, HtmlAttribute.SIZE, -1);
            field.setColumns((size > 0) ? size : DEFAULT_INPUT_COMPONENT_SIZE);

            // Don't use IDE look and feel; use the platform's native l&f
            TextUI ui = (TextUI)createNativeUI(c);

            if (ui != null) {
                field.setUI(ui);
            }

            String value = el.getAttribute(HtmlAttribute.VALUE);

            if ((value != null) && (value.length() > 0)) {
                field.setText(value);
            }

            if (CssProvider.getValueService().isColorTransparentForElement(el, XhtmlCss.BACKGROUND_COLOR_INDEX)) {
                field.setOpaque(false);
            }
            //field.addActionListener(this);
            //maxIsPreferred = 3;
        } else if ((tag == HtmlTag.BUTTON) || type.equals("submit") // NOI18N
                 ||type.equals("reset") // NOI18N
                 ||type.equals("button")) { // NOI18N

            String value = getButtonLabel(el, type);

            JButton button = new JButton(value);

            // XXX change insets here??? Try to make buttons narrower
            // the below is not enough
            java.awt.Insets insets = button.getMargin();

            if (insets != null) {
                button.setMargin(new java.awt.Insets(insets.top / 2, insets.left / 2,
                        insets.bottom / 2, insets.right / 2));
            }

            // Grrrr.. unlike JComboBox, setUI is not public in JButton
            button.setUI((ButtonUI)createNativeUI(button));

            if (org.openide.util.Utilities.getOperatingSystem() == org.openide.util.Utilities.OS_MAC) {
                // Buttons are rounded to treat as nonrectangular
                // even though the aqua buttons will paint                    
                // a rectangular area - but it respects the background
                // color in this area (it does NOT paint the background
                // on the button itself, which is a bit weird...)
                // See Apple Technical Q&A QA1272
                // http://developer.apple.com/qa/qa2001/qa1272.html
                button.setOpaque(false); // will indicate nonrectangular
            }

            c = button;

            //maxIsPreferred = 3;
            //} else if (type.equals("image")) { // NOI18N
            // handled in factory instead - delegates to ImageBox rather
            // than using a JButton component for this as before
        } else if (type.equals("checkbox")) {
            JCheckBox jc = new JCheckBox();
            c = jc;

            jc.setVerticalAlignment(SwingConstants.BOTTOM);

            java.awt.Insets insets = jc.getMargin();

            if (insets != null) {
                jc.setMargin(new java.awt.Insets(0, 0, 0, 0));
            }

            String checked = el.getAttribute(HtmlAttribute.CHECKED);

            if ((checked != null) && (checked.length() > 0)) {
                jc.setSelected(true);
            }

            // Don't use IDE look and feel; use the platform's native l&f
            ButtonUI ui = (ButtonUI)createNativeUI(c);

            if (ui != null) {
                jc.setUI(ui);
            }

            //maxIsPreferred = 3;
            jc.setOpaque(false); // will indicate nonrectangular
        } else if (type.equals("radio")) {
            JRadioButton jc = new JRadioButton();
            c = jc;

            // Try to isolate only the radiobutton graphic itself
            jc.setVerticalAlignment(SwingConstants.BOTTOM);

            java.awt.Insets insets = jc.getMargin();

            if (insets != null) {
                jc.setMargin(new java.awt.Insets(0, 0, 0, 0));
            }

            String checked = el.getAttribute(HtmlAttribute.CHECKED);

            if ((checked != null) && (checked.length() > 0)) {
                jc.setSelected(true);
            }

            // Don't use IDE look and feel; use the platform's native l&f
            ButtonUI ui = (ButtonUI)createNativeUI(c);

            if (ui != null) {
                jc.setUI(ui);
            }

            //maxIsPreferred = 3;
            jc.setOpaque(false); // will indicate nonrectangular
        } else if (type.equals("password")) {
            JPasswordField field = new JPasswordField();
            c = field;

            TextUI ui = (TextUI)createNativeUI(c);

            if (ui != null) {
                field.setUI(ui);
            }

            int size = HtmlAttribute.getIntegerAttributeValue(el, HtmlAttribute.SIZE, -1);
            field.setColumns((size > 0) ? size : DEFAULT_INPUT_COMPONENT_SIZE);

            String value = el.getAttribute(HtmlAttribute.VALUE);

            if ((value != null) && (value.length() > 0)) {
                field.setText(value);
            } else {
                // No point looking up bean name - will become "*****"s anyway
                field.setText("abcdefghijk");
            }

            //field.addActionListener(this);
            //maxIsPreferred = 3;
        } else if (type.equals("file")) {
            JTextField field = new JTextFieldColumnWidth();
            int size = HtmlAttribute.getIntegerAttributeValue(el, HtmlAttribute.SIZE, -1);
            field.setColumns((size > 0) ? size : DEFAULT_INPUT_COMPONENT_SIZE);

            TextUI ui = (TextUI)createNativeUI(field);

            if (ui != null) {
                field.setUI(ui);
            }

            // XXX Why am I using the Swing label here?
            JButton browseButton =
                new JButton(UIManager.getString("FormView.browseFileButtonText"));
            browseButton.setUI((ButtonUI)createNativeUI(browseButton));

            javax.swing.Box box = javax.swing.Box.createHorizontalBox();
            box.add(field);
            box.add(javax.swing.Box.createHorizontalStrut(5));
            box.add(browseButton);
            c = box;

            //maxIsPreferred = 3;
            c.setOpaque(false); // will indicate nonrectangular
        }

        return c;
    }

    /** Return the label to be shown for a given button element */
    private static String getButtonLabel(Element element, String type) {
        if (element.getTagName().equals(HtmlTag.BUTTON.name)) {
            StringBuffer sb = new StringBuffer();
//            DesignerUtils.addNodeText(sb, element, true);
            addNodeText(sb, element, true);

            return sb.toString().trim();
        }

        // Can't do this:
        //String value = el.getAttribute(HtmlAttribute.VALUE);
        // because I need to distinguish between value not set
        // (in which case I show "Submit" instead) or value set to ""
        // in which case I should leave the button blank
        Attr attr = element.getAttributeNode(HtmlAttribute.VALUE);
        String value = null;

        if (attr != null) {
            value = attr.getValue();

            if (value.length() == 0) {
                // "" isn't good enough because we want the
                // button to be as tall as text
                value = " "; // NOI18N
            }
        } else {
            if (type.equals("reset")) {
                value = NbBundle.getMessage(FormComponentBox.class, "Reset"); // NOI18N
            } else {
                // submit or button
                value = NbBundle.getMessage(FormComponentBox.class, "Submit"); // NOI18N
            }
        }

        return value;
    }
    
    // XXX Moved from DesignerUtils.
    /**
     * Add all the text content you can find under the given node into
     * the given StringBuffer.
     */
    private static void addNodeText(StringBuffer sb, Node n, boolean skipSpace) {
        int type = n.getNodeType();
        
        if (type == Node.TEXT_NODE) {
            if (skipSpace && DesignerUtils.onlyWhitespace(n.getNodeValue())) {
                return;
            }
            
            sb.append(n.getNodeValue());
        } else if (type == Node.COMMENT_NODE) {
            String comment = n.getNodeValue();
            int newline = comment.indexOf('\n');
            
            if (newline != -1) {
                sb.append(comment.substring(newline + 1));
            }
        } else if (type == Node.CDATA_SECTION_NODE) {
            if (skipSpace && DesignerUtils.onlyWhitespace(n.getNodeValue())) {
                return;
            }
            
            sb.append(n.getNodeValue());
        } else {
            NodeList children = n.getChildNodes();
            
            for (int i = 0; i < children.getLength(); i++) {
                addNodeText(sb, children.item(i), skipSpace);
            }
        }
    }


    /**
     * Look up the native look and feel to use on this platform.
     * This will return the current look and feel (if it calls itself
     * native), otherwise it will pick an arbitrary native (& supported)
     * look and feel. If none can be found, it just returns the current
     * look and feel.
     */
    private static LookAndFeel findNativeLnF() {
        // Check the current look and feel first, and give that one
        // preference if it fits the bill
        LookAndFeel feel = UIManager.getLookAndFeel();

        if (feel.isNativeLookAndFeel()) {
            return feel;
        }

        UIManager.LookAndFeelInfo[] info = UIManager.getInstalledLookAndFeels();

        if (info == null) {
            return feel;
        }

        for (int i = 0; i < info.length; i++) {
            String clsname = info[i].getClassName();
            Class clz;

            try {
                clz = Class.forName(clsname);
            } catch (ClassNotFoundException clf) {
                clf.printStackTrace();

                return feel;
            }

            if (clz != null) {
                try {
                    LookAndFeel f = (LookAndFeel)clz.newInstance();

                    if (f.isSupportedLookAndFeel() && f.isNativeLookAndFeel()) {
                        // Found one...
                        return f;
                    }
                } catch (InstantiationException ie) {
                    ie.printStackTrace();

                    // continue
                } catch (IllegalAccessException iae) {
                    iae.printStackTrace();

                    // continue
                }
            }
        }

        // Give up - no native look and feels found.
        // Just use the current look and feel.
        return feel;
    }

    /** Create a ComponentUI for the given JComponent. It will attempt
     * to find a native look and feel for the component.
     */
    private static ComponentUI createNativeUI(JComponent c) {
        if (uidef == null) {
            LookAndFeel feel = findNativeLnF();
            uidef = feel.getDefaults();

            if (uidef == null) {
                return null;
            }
        }

        javax.swing.plaf.ComponentUI cu = uidef.getUI(c);

        return cu;
    }

    protected void paintBackground(Graphics g, int x, int y) {
        // We don't need any background painted - fully covered
        // by component bitmap, unless we have children
        if (createChildren && (getBoxCount() > 0)) {
            super.paintBackground(g, x, y);
        }
    }

    public void paint(Graphics g, int px, int py) {
        super.paint(g, px, py);

        if (hidden) {
            return;
        }

        px += leftMargin;
        py += effectiveTopMargin;

        // XXX Don't I need to add in margins?
        if (getComponent() != null) { // not the case for e.g. input hidden

            Image image = getImage();

            if (image != null) {
                transform.setToTranslation((float)(px + getX()), (float)(py + getY()));
                ((Graphics2D)g).drawImage(image, transform, null);
            }
        }
    }

    private Image getImage() {
        if ((image != null) || attempted) { // only try once

            return image;
        }

        if (getComponent() == null) {
            return null;
        }

        attempted = true;

        JComponent comp = getComponent();
        Dimension size = comp.getPreferredSize();
        imageWidth = size.width;
        imageHeight = size.height;

        //System.out.println("size=" + size);
        if ((imageWidth == 0) || (imageHeight == 0)) {
            // XXX #6504407 If not relevant case (component should have actual 0 size) -> bad architecture.
            ErrorManager.getDefault().log("Component has 0 size - " + this + " w=" + imageWidth +
                " h=" + imageHeight);
            
            // XXX #6504407 Don't count this attempt, 
            // there might be still uninitialized lengths (bad architecture).
            attempted = false;

            return null;
        }

        comp.setSize(size);

        image = comp.createImage(imageWidth, imageHeight);

        if (image == null) {
            image = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_RGB);

            if (image == null) {
                return null;
            }
        }

        Graphics og = image.getGraphics(); // offscreen buffer

        // XXX This 'nonrectangular' (should be transparent) stuff is very suspicious,
        // why to repaint the parent's color again? Is it because of the grid dots only?
        if (nonrectangular) {
            //og.setColor(bg);
            // Hmmm, what about borders? Just call super-duper instead?
            //og.fillRect(0, 0, imageWidth, imageHeight);
            //super.paintBackground(og, 0, 0, imageWidth, imageHeight);
            Color oldBg = bg;
            CssBox p = this;

            while ((p != null) && (p.bg == null)) {
                p = p.getParent();
            }

            if (p != null) {
                bg = p.bg;
            } else {
                // XXX Ugh! How did this happen?
                if (DEBUGFORMAT) {
                    ErrorManager.getDefault().log("Surprising - no parent with bg! this=" + this +
                        ", element=" + getElement() + ", parent=" + getParent() + ", p=" + p);
                }

                bg = Color.white;
            }

            super.paintBackground(og, 0, 0);
            bg = oldBg;
        }

        CellRendererPane rendererPane = webform.getRenderPane();

        if (rendererPane == null) { // testsuite
            og.drawString("Error", 0, imageHeight / 2);

            return image;
        }

        rendererPane.paintComponent(og, comp, null, 0, 0, imageWidth, imageHeight, true);

        // Empty out the renderer pane, allowing renderers to be gc'ed.
        // (The paintComponent call above implicitly adds the component
        // to its child list and does not remove it!)
        rendererPane.removeAll();

        // XXX #6277297. This is needed, otherwise it gets overlapped by the image.
        // FIXME Improve the painting machinery.
        if (hasInitialFocus()) {
            paintFocusWaterMark(og, 0, 0);
        }

        //paintFacesWatermark(og, 0, 0);
        //        String name ="foo";
        //        if (bean != null) {
        //            name = bean.getInstanceName();
        //        }
        //        String filename = "/tmp/comp" + name + ".png";
        //        try {
        //            if (image instanceof BufferedImage) {
        //                javax.imageio.ImageIO.write((BufferedImage)image, "png", new java.io.File(filename));
        //            } else {
        //                System.out.println("Image was not a BufferedImage");
        //            }
        //        } catch (java.io.IOException e) {
        //            e.printStackTrace();
        //        }
        return image;
    }

    // XXX #112462 Width of text fields (and other textuals) is computed
    // differently in browser and differently in Swing, trying to adjust.
    private static Dimension computeSizeForTextComponent(JTextComponent textComponent) {
        if (textComponent instanceof JTextField) {
            JTextField textField = (JTextField)textComponent;
            int columns = textField.getColumns();
            if (columns > 0) {
                return computeSizeForTextComponentColumns(textComponent, columns);
            }
        } else if (textComponent instanceof JTextArea) {
            JTextArea textArea = (JTextArea)textComponent;
            int columns = textArea.getColumns();
            if (columns > 0) {
                return computeSizeForTextComponentColumns(textComponent, columns);
            }
        }
        return textComponent.getPreferredSize();
    }
    
    // XXX #112462 Width of text fields (and other textuals) is computed
    // differently in browser and differently in Swing, trying to adjust.
    private static Dimension computeSizeForTextComponentColumns(JTextComponent textComponent, int columns) {
        Dimension size = textComponent.getPreferredSize();
        Font font = textComponent.getFont();
        if (font == null) {
            return size;
        }
        FontMetrics fontMetrics = textComponent.getFontMetrics(font);
        if (fontMetrics == null) {
            return size;
        }
//        Insets insets = textComponent.getInsets();
//        int insetsWidth = insets == null ? 0 : insets.left + insets.right;
        size.width = columns * (fontMetrics.charWidth('a'))/* + insetsWidth*/; // NOI18N
        return size;
    }
    
//    /**
//     * Paints a watermark on the component, indicating that it has initial focus.
//     * @param g The graphics context
//     */
//    private static void paintFocusWatermark(Graphics g) {
//        Image watermark =
//            org.openide.util.Utilities.loadImage("org/netbeans/modules/visualweb/css2/focus-watermark.gif"); // NOI18N
//
//        if ((watermark != null) && (g instanceof Graphics2D)) {
//            Graphics2D g2d = (Graphics2D)g;
//            AffineTransform t = new AffineTransform(); // XXX keep transform object around?
//            t.translate(0.0D, 0.0D);
//
//            Composite oldAlpha = g2d.getComposite();
//            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
//            g2d.drawImage(watermark, t, null);
//            g2d.setComposite(oldAlpha);
//        }
//    }

    

    /**
     * Determines the preferred span for this view along an
     * axis.  This is implemented to return the value
     * returned by Component.getPreferredSize along the
     * axis of interest.
     *
     * @param axis may be either CssBox.X_AXIS or CssBox.Y_AXIS
     * @return   the span the view would like to be rendered into >= 0.
     *           Typically the view is told to render into the span
     *           that is returned, although there is no guarantee.
     *           The parent may choose to resize or break the view.
     * @exception IllegalArgumentException for an invalid axis
     */
    public float getPreferredSpan(int axis) {
        if ((axis != X_AXIS) && (axis != Y_AXIS)) {
            throw new IllegalArgumentException("Invalid axis: " + axis);
        }

        if (imageWidth != -1) {
            return (axis == CssBox.X_AXIS) ? imageWidth : imageHeight;
        }

        if (createdC != null) {
            Dimension size = createdC.getPreferredSize();

            if (axis == CssBox.X_AXIS) {
                return size.width;
            } else {
                return size.height;
            }
        }

        return 0;
    }

    /**
     * Determines the minimum span for this view along an
     * axis.  This is implemented to return the value
     * returned by Component.getMinimumSize along the
     * axis of interest.
     *
     * @param axis may be either CssBox.X_AXIS or CssBox.Y_AXIS
     * @return   the span the view would like to be rendered into >= 0.
     *           Typically the view is told to render into the span
     *           that is returned, although there is no guarantee.
     *           The parent may choose to resize or break the view.
     * @exception IllegalArgumentException for an invalid axis
     */
    public float getMinimumSpan(int axis) {
        if ((axis != X_AXIS) && (axis != Y_AXIS)) {
            throw new IllegalArgumentException("Invalid axis: " + axis);
        }

        if (imageWidth != -1) {
            // XXX TODO: return createdC.getMinimumSize() ? Must cache!
            return (axis == CssBox.X_AXIS) ? imageWidth : imageHeight;
        }

        if (createdC != null) {
            Dimension size = createdC.getMinimumSize();

            if (axis == CssBox.X_AXIS) {
                return size.width;
            } else {
                return size.height;
            }
        }

        return 0;
    }

    /**
     * Determines the maximum span for this view along an
     * axis.  This is implemented to return the value
     * returned by Component.getMaximumSize along the
     * axis of interest.
     *
     * @param axis may be either CssBox.X_AXIS or CssBox.Y_AXIS
     * @return   the span the view would like to be rendered into >= 0.
     *           Typically the view is told to render into the span
     *           that is returned, although there is no guarantee.
     *           The parent may choose to resize or break the view.
     * @exception IllegalArgumentException for an invalid axis
     */
    public float getMaximumSpan(int axis) {
        // XXX TODO Roll in the FormView subclass of this
        if ((axis != X_AXIS) && (axis != Y_AXIS)) {
            throw new IllegalArgumentException("Invalid axis: " + axis);
        }

        if (imageWidth != -1) {
            // XXX TODO: return createdC.getMaximumSize() ? Must cache!
            return (axis == CssBox.X_AXIS) ? imageWidth : imageHeight;
        }

        if (createdC != null) {
            Dimension size = createdC.getMaximumSize();

            if (axis == CssBox.X_AXIS) {
                return size.width;
            } else {
                return size.height;
            }
        }

        return 0;
    }

    /**
     * FOR TESTSUITE ONLY!
     * Fetch the component associated with the view.
     */
    public final JComponent getComponent() {
        return createdC;
    }

    public int getWidth() {
        // XXX This doesn't account for children - how do we do that?
        return (int)getPreferredSpan(X_AXIS);
    }

    public int getHeight() {
        // XXX This doesn't account for children - how do we do that?
        return (int)getPreferredSpan(Y_AXIS);
    }

//    public String toString() {
//        Element element = getElement();
//        return "FormComponentBox[value=" + (element == null ? null : element.getAttribute("value")) + ", " +
//        //"component=" + createdC + ", " +
//        paramString() + "]";
//    }
    
    protected String paramString() {
        Element element = getElement();
        return "value=" + (element == null ? null : element.getAttribute("value")) + ", " + super.paramString()
        + ", component=" + createdC; // NOI18N
    }

    /*
    protected String paramString() {
        return super.paramString() + ", " +
            "component=" + createdC;
    }
    */
    public int getIntrinsicWidth() {
        return (int)getPreferredSpan(X_AXIS);
    }

    public int getIntrinsicHeight() {
        return (int)getPreferredSpan(Y_AXIS);
    }

    public int getBaseline() {
        return baseline;
    }
    
    
    /** XXX #112462 Text field with adjusted (more 'browser-like') column width. */
    private static class JTextFieldColumnWidth extends JTextField {
        private int columnWidth;
        
        public JTextFieldColumnWidth() {
            super();
        }
        
        public JTextFieldColumnWidth(String text, int columns) {
            super(text, columns);
        }
        
        @Override
        public void setFont(Font font) {
            super.setFont(font);
            columnWidth = 0;
        }
        
        @Override
        protected int getColumnWidth() {
            if (columnWidth == 0) {
                FontMetrics metrics = getFontMetrics(getFont());
//                columnWidth = metrics.charWidth('m');
                columnWidth = metrics.charWidth('a'); // NOI18N
            }
            return columnWidth;
        }
    } // End of JTextFieldColumnWidth.
    
    
    /** XXX #112462 Text area with adjusted (more 'browser-like') column width. */
    private static class JTextAreaColumnWidth extends JTextArea {
        private int columnWidth;
        
        public JTextAreaColumnWidth(Document doc) {
            super(doc);
        }
        
        public JTextAreaColumnWidth(String text, int rows, int columns) {
            super(text, rows, columns);
        }
        
        @Override
        public void setFont(Font font) {
            super.setFont(font);
            columnWidth = 0;
        }
        
        @Override
        protected int getColumnWidth() {
            if (columnWidth == 0) {
                FontMetrics metrics = getFontMetrics(getFont());
//                columnWidth = metrics.charWidth('m');
                columnWidth = metrics.charWidth('a'); // NOI18N
            }
            return columnWidth;
        }
    } // End of JTextAreaColumnWidth.

    
    public static JTextField createTextField() {
        return new JTextFieldColumnWidth();
    }
    
    public static JTextField createTextField(String text, int columns) {
        return new JTextFieldColumnWidth(text, columns);
    }

    public static JTextArea createTextArea(String text, int rows, int columns) {
        return new JTextAreaColumnWidth(text, rows, columns);
    }
    
}

