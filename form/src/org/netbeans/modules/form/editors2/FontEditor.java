/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.form.editors2;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ResourceBundle;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.form.FormModel;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import org.jdesktop.layout.GroupLayout;
import org.jdesktop.layout.LayoutStyle;

import org.openide.awt.Mnemonics;
import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.explorer.propertysheet.editors.XMLPropertyEditor;
import org.openide.util.NbBundle;

import org.netbeans.modules.form.FormAwareEditor;
import org.netbeans.modules.form.FormDesignValue;
import org.netbeans.modules.form.FormProperty;
import org.netbeans.modules.form.RADComponent;
import org.netbeans.modules.form.RADProperty;
import org.netbeans.modules.form.codestructure.CodeVariable;

/**
 * Font property editor that supports relative changes of the font.
 *
 * @author Jan Stola
 */
public class FontEditor implements ExPropertyEditor, XMLPropertyEditor,
    FormAwareEditor, PropertyChangeListener {
    /**
     * Property editor delegate used for static/absolute values.
     */
    private PropertyEditor delegate;
    /**
     * The current value of the property maintained by this property editor.
     */
    private NbFont propertyValue;
    /**
     * Edited property.
     */
    private RADProperty property;
    /**
     * Property change support.
     */
    private PropertyChangeSupport propChangeSupport;
    /**
     * Determines whether (internal) updates should be ignored.
     */
    private boolean ignoreUpdates = true;
    
    public FontEditor() {
        propertyValue = new NbFont();
        propChangeSupport = new PropertyChangeSupport(this);
        delegate = PropertyEditorManager.findEditor(Font.class);
        if (delegate == null) {
            throw new IllegalStateException("FontEditor delegate not found."); // NOI18N
        }
        if (!(delegate instanceof XMLPropertyEditor)) {
            throw new IllegalStateException("FontEditor delegate doesn't implement XMLPropertyEditor."); // NOI18N
        }
        delegate.addPropertyChangeListener(this);
    }

    public void setValue(Object value) {
        if ((value instanceof Font) || (value == null)) {
            propertyValue = new NbFont();
            propertyValue.absolute = true;
            propertyValue.font = (Font)value;
        } else if (value instanceof NbFont) {
            propertyValue = ((NbFont)value).copy();
        } else {
            throw new IllegalArgumentException();
        }
        if (property != null) {
            propertyValue.property = property;
        }
        delegate.setValue(propertyValue.getDesignValue());
    }

    public Object getValue() {
        return propertyValue;
    }

    public boolean isPaintable() {
        return delegate.isPaintable();
    }

    public void paintValue(Graphics gfx, Rectangle box) {
        delegate.paintValue(gfx, box);
    }

    public String getJavaInitializationString() {
        String exp;
        if (propertyValue.absolute) {
            exp = delegate.getJavaInitializationString();
        } else {
            RADComponent comp = property.getRADComponent();
            CodeVariable var = comp.getCodeExpression().getVariable();
            String varName = (var == null) ? null : var.getName();
            String readMethod = property.getPropertyDescriptor().getReadMethod().getName();
            String getter = readMethod + "()"; // NOI18N
            if (varName != null) {
                getter = varName + '.' + getter;
            }
            exp = getter + ".deriveFont("; // NOI18N
            boolean styleChanged = (propertyValue.italic != null) || (propertyValue.bold != null);
            if (styleChanged) {
                String styleExp = null;
                if (propertyValue.italic != null)  {
                    styleExp = getter + ".getStyle()"; // NOI18N
                    if (Boolean.TRUE.equals(propertyValue.italic)) {
                        styleExp += " | "; // NOI18N
                    } else{
                        styleExp += " & ~"; // NOI18N
                    }
                    styleExp += "java.awt.Font.ITALIC"; // NOI18N
                }
                if (styleExp == null) {
                    styleExp = getter + ".getStyle()"; // NOI18N
                } else {
                    styleExp = "(" + styleExp + ")"; // NOI18N
                }
                if (propertyValue.bold != null)  {
                    if (Boolean.TRUE.equals(propertyValue.bold)) {
                        styleExp += " | "; // NOI18N
                    } else{
                        styleExp += " & ~"; // NOI18N
                    }
                    styleExp += "java.awt.Font.BOLD"; // NOI18N
                }
                exp += styleExp;
            }
            if (propertyValue.absoluteSize) {
                exp += styleChanged ? ", " : "(float)"; // NOI18N
                exp += propertyValue.size + ")"; // NOI18N
            } else {
                if (propertyValue.size == 0) {
                    if (styleChanged) {
                        exp += ')';
                    } else {
                        exp = getter;
                    }
                } else {
                    if (styleChanged) {
                        exp += ", "; // NOI18N
                    }
                    exp += getter + ".getSize()"; // NOI18N
                    if (propertyValue.size > 0) {
                        exp += '+';
                    }
                    exp += propertyValue.size + ")"; // NOI18N
                }
            }
        }
        return exp;
    }

    public String getAsText() {
        return propertyValue.absolute ? delegate.getAsText() : propertyValue.getDescription();
    }

    public void setAsText(String text) {
        return;
    }

    public String[] getTags() {
        return null;
    }

    // Hack for ugly behaviour of property sheet - it sometimes calls
    // getCustomEditor() before attachEnv()
    private WeakReference lastSwitchBox;
    public Component getCustomEditor() {
        // The custom editor changes iternals of NbFont
        // We must edit another instance because user can cancel the changes
        // and they would remain in the original NbFont
        propertyValue = ((NbFont)propertyValue).copy();
        final Component absolute = propertyValue.absolute ? delegate.getCustomEditor() : null;
        String switchBoxText = NbBundle.getMessage(FontEditor.class, "CTL_DeriveFont"); // NOI18N
        final JCheckBox switchBox = new JCheckBox();
        lastSwitchBox = new WeakReference(switchBox);
        if (property == null) {
            switchBox.setVisible(false);
        } else {
            Mnemonics.setLocalizedText(switchBox, switchBoxText);
            switchBox.setSelected(!propertyValue.absolute);
        }
        final RelativeFontPanel relative = new RelativeFontPanel();
        Component pane = propertyValue.absolute ? absolute : relative;
        if (!propertyValue.absolute) relative.updateFromPropertyValue();

        final JPanel editor = new JPanel();
        final GroupLayout layout = new GroupLayout(editor);
        editor.setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup()
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(switchBox))
            .add(pane));
        layout.setVerticalGroup(layout.createSequentialGroup()
            .addContainerGap()
            .add(switchBox)
            .addPreferredGap(LayoutStyle.RELATED)
            .add(pane)
            .addContainerGap());

        switchBox.addItemListener(new ItemListener() {
            private Component absoluteInLayout = absolute;
            public void itemStateChanged(ItemEvent e) {
                if (switchBox.isSelected()) {
                    layout.replace(absoluteInLayout, relative);
                    propertyValue.absolute = false;
                    convertToRelative();
                    relative.updateFromPropertyValue();
                } else {
                    absoluteInLayout = delegate.getCustomEditor();
                    layout.replace(relative, absoluteInLayout);
                    propertyValue.absolute = true;
                }
                firePropertyChange();
                editor.revalidate();
                editor.repaint();
            }
        });

        return editor;
    }

    private void convertToRelative() {
        if (propertyValue.font == null) return;
        Font defaultFont = (Font)property.getDefaultValue();
        if (propertyValue.absoluteSize) {
            propertyValue.size = propertyValue.font.getSize();
        } else {
            if (defaultFont == null) return;
            propertyValue.size = propertyValue.font.getSize() - defaultFont.getSize();
        }
        if (defaultFont == null) return;
        int absoluteStyle = propertyValue.font.getStyle();
        int defaultStyle = defaultFont.getStyle();
        boolean aItalic = ((absoluteStyle & Font.ITALIC) != 0);
        boolean dItalic = ((defaultStyle & Font.ITALIC) != 0);
        if (aItalic && !dItalic) {
            propertyValue.italic = Boolean.TRUE;
        }
        if (!aItalic && dItalic) {
            propertyValue.italic = Boolean.FALSE;
        }
        if ((propertyValue.italic != null) && (aItalic == dItalic)
            && (aItalic != propertyValue.italic.booleanValue())) {
            propertyValue.italic = null;
        }
        boolean aBold = ((absoluteStyle & Font.BOLD) != 0);
        boolean dBold = ((defaultStyle & Font.BOLD) != 0);
        if (aBold && !dBold) {
            propertyValue.bold = Boolean.TRUE;
        }
        if (!aBold && dBold) {
            propertyValue.bold = Boolean.FALSE;
        }
        if ((propertyValue.bold != null) && (aBold == dBold)
            && (aBold != propertyValue.bold.booleanValue())) {
            propertyValue.bold = null;
        }
    }

    public boolean supportsCustomEditor() {
        return true;
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propChangeSupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propChangeSupport.removePropertyChangeListener(listener);
    }

    // PropertyChangeListener implementation
    public void propertyChange(PropertyChangeEvent evt) {
        Font font = (Font)delegate.getValue();
        propertyValue.font = font;
        if (!ignoreUpdates) {
            firePropertyChange();
        }
    }

    private void updateDelegate() {
        ignoreUpdates = true;
        delegate.setValue(propertyValue.getDesignValue());
        ignoreUpdates = false;
        firePropertyChange();
    }

    private void firePropertyChange() {
        propChangeSupport.firePropertyChange("", null, null); // NOI18N
    }

    // ExPropertyEditor implementation
    public void attachEnv(PropertyEnv env) {
        FeatureDescriptor prop = env.getFeatureDescriptor();
        // Don't support relative changes for nested properties
        // (for example Font property of TitledBorder)
        if ((prop instanceof RADProperty) && (env.getBeans().length == 1)) {
            property = (RADProperty)prop;
            if (propertyValue != null) {
                propertyValue.property = property;
            }
        } else {
            if (lastSwitchBox != null) { // Hack - see comment for lastSwitchBox field
                Object switchBox = lastSwitchBox.get();
                if (switchBox != null) {
                    AbstractButton button = ((AbstractButton)switchBox);
                    button.setVisible(false);
                    if (button.isSelected()) button.setSelected(false);
                }
            }
            property = null;
        }
    }

    // XMLPropertyEditor implementation
    /** Root of the XML representation of the font. */
    public static final String XML_FONT_ROOT = "FontInfo"; // NOI18N
    /** Element with information about the font. */
    public static final String XML_FONT = "Font"; // NOI18N
    /** Determines whether the font is relative. */
    public static final String ATTR_RELATIVE = "relative"; // NOI18N
    /** Determines whether the font size is relative to the default value. */
    public static final String ATTR_RELATIVE_SIZE = "relativeSize"; // NOI18N
    /** Attribute for the font size. */
    public static final String ATTR_SIZE = "size"; // NOI18N
    /** Attribute for the change of italic. */
    public static final String ATTR_ITALIC_CHANGE = "italic"; // NOI18N
    /** Attribute for the change of thickness. */
    public static final String ATTR_BOLD_CHANGE = "bold"; // NOI18N
    /** Name of the component this value belongs to. */
    public static final String ATTR_COMP_NAME = "component"; // NOI18N
    /** Name of the property this value belongs to. */
    public static final String ATTR_PROP_NAME = "property"; // NOI18N

    public void readFromXML(Node element) throws IOException {
        if (!XML_FONT_ROOT.equals(element.getNodeName())) {
            // Backward compatibility with openide's FontEditor
            ((XMLPropertyEditor)delegate).readFromXML(element);
            setValue(delegate.getValue());
            return;
        }
        org.w3c.dom.NamedNodeMap attributes = element.getAttributes();
        boolean relative = Boolean.valueOf(attributes.getNamedItem(ATTR_RELATIVE).getNodeValue()).booleanValue();
        propertyValue = new NbFont();
        propertyValue.absolute = !relative;
        org.w3c.dom.NodeList subnodes = element.getChildNodes();
        for (int i=0; i<subnodes.getLength(); i++){
            org.w3c.dom.Node subnode = subnodes.item(i);
            if (subnode.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                if (relative) {
                    if (!XML_FONT.equals(subnode.getNodeName())) {
                        throw new java.io.IOException();
                    }
                    attributes = subnode.getAttributes();
                    propertyValue.absoluteSize = !Boolean.valueOf(attributes.getNamedItem(ATTR_RELATIVE_SIZE).getNodeValue()).booleanValue();
                    propertyValue.size = Integer.parseInt(attributes.getNamedItem(ATTR_SIZE).getNodeValue());
                    org.w3c.dom.Node italicChange = attributes.getNamedItem(ATTR_ITALIC_CHANGE);
                    if (italicChange != null) {
                        propertyValue.italic = Boolean.valueOf(italicChange.getNodeValue());
                    }
                    org.w3c.dom.Node boldChange = attributes.getNamedItem(ATTR_BOLD_CHANGE);
                    if (boldChange != null) {
                        propertyValue.bold = Boolean.valueOf(boldChange.getNodeValue());
                    }
                    String compName = attributes.getNamedItem(ATTR_COMP_NAME).getNodeValue();
                    RADComponent component = formModel.findRADComponent(compName);
                    String propName = attributes.getNamedItem(ATTR_PROP_NAME).getNodeValue();
                    property = (RADProperty)component.getPropertyByName(propName);
                    propertyValue.property = property;
                    delegate.setValue(propertyValue.getDesignValue());
                } else {
                    ((XMLPropertyEditor)delegate).readFromXML(subnode);
                    propertyValue.font = (Font)delegate.getValue();
                }
                break;
            }
        }
    }

    public Node storeToXML(Document doc) {
        org.w3c.dom.Element el = doc.createElement(XML_FONT_ROOT);
        el.setAttribute(ATTR_RELATIVE, Boolean.toString(!propertyValue.absolute));
        if (propertyValue.absolute) {
            org.w3c.dom.Node absNode = ((XMLPropertyEditor)delegate).storeToXML(doc);
            el.appendChild(absNode);
        } else {
            org.w3c.dom.Element subel = doc.createElement(XML_FONT);
            el.appendChild(subel);
            subel.setAttribute(ATTR_RELATIVE_SIZE, Boolean.toString(!propertyValue.absoluteSize));
            subel.setAttribute(ATTR_SIZE, Integer.toString(propertyValue.size));
            if (propertyValue.italic != null) {
                subel.setAttribute(ATTR_ITALIC_CHANGE, propertyValue.italic.toString());
            }
            if (propertyValue.bold != null) {
                subel.setAttribute(ATTR_BOLD_CHANGE, propertyValue.bold.toString());
            }
            subel.setAttribute(ATTR_COMP_NAME, property.getRADComponent().getName());
            subel.setAttribute(ATTR_PROP_NAME, property.getName());
        }
        return el;
    }

    // FormAwarePropertyEditor implementation
    private FormModel formModel;
    public void setFormModel(FormModel model) {
        this.formModel = model;
    }

    static class NbFont implements FormDesignValue {
        /**
         * Determines whether this is an absolute/static value
         * or relative/dynamic value.
         */
        boolean absolute = false;
        /**
         * Absolute/static value of the font.
         */
        Font font;
        /**
         * Describes the relative change of italic.
         * <code>true = add italic, false = remove italic, null = leave it as it is</code>
         */
        Boolean italic;
        /**
         * Describes the relative change of bold.
         * <code>true = add bold, false = remove false, null = leave it as it is</code>
         */
        Boolean bold;
        /**
         * Determines whether the change of the font size is relative or absolute.
         */
        boolean absoluteSize;
        /**
         * Describes the change of the font size.
         * <code>size = (absoluteSize) ? absolute size : relative change</code>
         */
        int size;
        /**
         * Property that contains this value.
         */
        FormProperty property;

        public Object getDesignValue() {
            Font value;
            if (absolute) {
                value = font;
            } else {
                value = (Font)property.getDefaultValue();
                if (value != null) {
                    int origStyle = value.getStyle();
                    int style = origStyle;
                    if (italic != null) {
                        if (italic.booleanValue()) {
                            style |= Font.ITALIC;
                        } else {
                            style &= ~Font.ITALIC;
                        }
                    }
                    if (bold != null) {
                        if (bold.booleanValue()) {
                            style |= Font.BOLD;
                        } else {
                            style &= ~Font.BOLD;
                        }
                    }
                    int origSize = value.getSize();
                    int newSize = (absoluteSize) ? size : (size + origSize);
                    if ((style != origStyle) || (origSize != newSize)) {
                        value = value.deriveFont(style, newSize);
                    }
                    // Hack - propagation of the new font (created as a result
                    // of LaF switch) back to the delegate
                    PropertyEditor editor = property.getCurrentEditor();
                    if (editor instanceof FontEditor) {
                        FontEditor fontEditor = (FontEditor)editor;
                        if (!value.equals(fontEditor.delegate.getValue())) {
                            fontEditor.delegate.setValue(value);
                        }
                    }
                }
            }
            return value;
        }

        public String getDescription() {
            ResourceBundle bundle = NbBundle.getBundle(FontEditor.class);
            String description;
            if (absolute) {
                String style = null;
                switch (font.getStyle()) {
                    case Font.PLAIN: style = bundle.getString("CTL_FontStylePlain"); break; // NOI18N
                    case Font.BOLD: style = bundle.getString("CTL_FontStyleBold"); break; // NOI18N
                    case Font.ITALIC: style = bundle.getString("CTL_FontStyleItalic"); break; // NOI18N
                    case Font.BOLD|Font.ITALIC: style = bundle.getString("CTL_FontStyleBoldItalic"); break; // NOI18N
                    default: style = Integer.toString(font.getStyle()); break;
                }
                description = font.getName() + ' ' + font.getSize() + ' ' + style;
            } else {
                description = Integer.toString(size);
                if (!absoluteSize && (size > 0)) {
                    description = '+' + description;
                }
                if (italic != null) {
                    description += " " + (italic.booleanValue() ? '+' : '-') + bundle.getString("CTL_FontStyleItalic"); // NOI18N
                }
                if (bold != null) {
                    description += " " + (bold.booleanValue() ? '+' : '-') + bundle.getString("CTL_FontStyleBold"); // NOI18N
                }
                if (description.charAt(0) == '0') description = description.substring(Math.min(2, description.length()));
            }
            return description;
        }

        public FormDesignValue copy(FormProperty targetFormProperty) {
            NbFont copy = copy();
            copy.property = targetFormProperty;
            return copy;
        }

        NbFont copy() {
            NbFont newValue = new NbFont();
            newValue.absolute = absolute;
            newValue.font = font;
            newValue.italic = italic;
            newValue.bold = bold;
            newValue.absoluteSize = absoluteSize;
            newValue.size = size;
            newValue.property = property;
            return newValue;
        }

    }

    /**
     * Panel used to configurate the relative change.
     */
    private class RelativeFontPanel extends JPanel {
        private JRadioButton absoluteChoice;
        private JSpinner absoluteSize;
        private JRadioButton addBoldChoice;
        private JRadioButton addItalicChoice;
        private JCheckBox italicCheckBox;
        private JRadioButton relativeChoice;
        private JSpinner relativeSize;
        private JRadioButton removeBoldChoice;
        private JRadioButton removeItalicChoice;
        private JCheckBox thicknessCheckBox;

        RelativeFontPanel() {
            initComponents();
        }

        void updateFromPropertyValue() {
            ignoreUpdates = true;
            boolean changeItalic = (propertyValue.italic != null);
            italicCheckBox.setSelected(changeItalic);
            addItalicChoice.setEnabled(changeItalic);
            removeItalicChoice.setEnabled(changeItalic);
            if (!changeItalic) {
                addItalicChoice.setSelected(true);
            } else if (Boolean.TRUE.equals(propertyValue.italic)) {
                addItalicChoice.setSelected(true);
            } else if (Boolean.FALSE.equals(propertyValue.italic)) {
                removeItalicChoice.setSelected(true);
            }
            boolean changeBold = (propertyValue.bold != null);
            thicknessCheckBox.setSelected(changeBold);
            addBoldChoice.setEnabled(changeBold);
            removeBoldChoice.setEnabled(changeBold);
            if (!changeBold) {
                addBoldChoice.setSelected(true);
            } else if (Boolean.TRUE.equals(propertyValue.bold)) {
                addBoldChoice.setSelected(true);
            } else if (Boolean.FALSE.equals(propertyValue.bold)) {
                removeBoldChoice.setSelected(true);
            }
            absoluteSize.setEnabled(propertyValue.absoluteSize);
            relativeSize.setEnabled(!propertyValue.absoluteSize);
            if (propertyValue.absoluteSize) {
                absoluteSize.setValue(new Integer(propertyValue.size));
                absoluteChoice.setSelected(true);
                synchronizeSizeControls();
            } else {
                relativeSize.setValue(new Integer(propertyValue.size));
                relativeChoice.setSelected(true);
                synchronizeSizeControls();
            }
            ignoreUpdates = false;
        }

        private void synchronizeSizeControls() {
            if (propertyValue.absoluteSize) {
                Font defaultFont = (Font)property.getDefaultValue();
                if (defaultFont != null) {
                    relativeSize.setValue(new Integer(propertyValue.size - defaultFont.getSize()));
                }
            } else {
                Font font = (Font)propertyValue.getDesignValue();
                absoluteSize.setValue(new Integer(font == null ? 12 : font.getSize()));   
            }
        }

        private void initComponents() {
            relativeSize = new JSpinner(new SpinnerNumberModel(0, Short.MIN_VALUE, Short.MAX_VALUE, 1));
            relativeSize.setEditor(new JSpinner.NumberEditor(relativeSize, "+#;-#")); // NOI18N
            absoluteSize = new JSpinner(new SpinnerNumberModel(12, 1, Short.MAX_VALUE, 1));

            ResourceBundle bundle = NbBundle.getBundle(FontEditor.class);
            JLabel fontSizeLabel = new JLabel(bundle.getString("CTL_FontSize")); // NOI18N
            JLabel fontStyleLabel = new JLabel(bundle.getString("CTL_FontStyle")); // NOI18N

            absoluteChoice = new JRadioButton();
            Mnemonics.setLocalizedText(absoluteChoice, bundle.getString("CTL_AbsoluteFontSize")); // NOI18N

            relativeChoice = new JRadioButton();
            Mnemonics.setLocalizedText(relativeChoice, bundle.getString("CTL_RelativeFontSize")); // NOI18N

            italicCheckBox = new JCheckBox();
            Mnemonics.setLocalizedText(italicCheckBox, bundle.getString("CTL_ChangeItalic")); // NOI18N

            addItalicChoice = new JRadioButton();
            Mnemonics.setLocalizedText(addItalicChoice, bundle.getString("CTL_AddItalic")); // NOI18N

            removeItalicChoice = new JRadioButton();
            Mnemonics.setLocalizedText(removeItalicChoice, bundle.getString("CTL_RemoveItalic")); // NOI18N

            thicknessCheckBox = new JCheckBox();
            Mnemonics.setLocalizedText(thicknessCheckBox, bundle.getString("CTL_ChangeBold")); // NOI18N

            addBoldChoice = new JRadioButton();
            Mnemonics.setLocalizedText(addBoldChoice, bundle.getString("CTL_AddBold")); // NOI18N

            removeBoldChoice = new JRadioButton();
            Mnemonics.setLocalizedText(removeBoldChoice, bundle.getString("CTL_RemoveBold")); // NOI18N

            // Listener
            Listener listener = new Listener();
            relativeChoice.addItemListener(listener);
            thicknessCheckBox.addItemListener(listener);
            italicCheckBox.addItemListener(listener);
            relativeSize.addChangeListener(listener);
            absoluteSize.addChangeListener(listener);
            addItalicChoice.addItemListener(listener);
            addBoldChoice.addItemListener(listener);

            // Radio button groups
            ButtonGroup italicGroup = new ButtonGroup();
            italicGroup.add(addItalicChoice);
            italicGroup.add(removeItalicChoice);

            ButtonGroup thicknessGroup = new ButtonGroup();
            thicknessGroup.add(addBoldChoice);
            thicknessGroup.add(removeBoldChoice);

            ButtonGroup fontSizeGroup = new ButtonGroup();
            fontSizeGroup.add(absoluteChoice);
            fontSizeGroup.add(relativeChoice);

            // Eliminate redundant borders
            Border emptyBorder = BorderFactory.createEmptyBorder(0, 0, 0, 0);
            Insets emptyInsets = new Insets(0, 0, 0, 0);

            absoluteChoice.setBorder(emptyBorder);
            absoluteChoice.setMargin(emptyInsets);
            relativeChoice.setBorder(emptyBorder);
            relativeChoice.setMargin(emptyInsets);
            italicCheckBox.setBorder(emptyBorder);
            italicCheckBox.setMargin(emptyInsets);
            addItalicChoice.setBorder(emptyBorder);
            addItalicChoice.setMargin(emptyInsets);
            removeItalicChoice.setBorder(emptyBorder);
            removeItalicChoice.setMargin(emptyInsets);
            thicknessCheckBox.setBorder(emptyBorder);
            thicknessCheckBox.setMargin(emptyInsets);
            addBoldChoice.setBorder(emptyBorder);
            addBoldChoice.setMargin(emptyInsets);
            removeBoldChoice.setBorder(emptyBorder);
            removeBoldChoice.setMargin(emptyInsets);

            GroupLayout layout = new GroupLayout(this);
            setLayout(layout);
            layout.setHorizontalGroup(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup()
                    .add(fontSizeLabel)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup()
                            .add(relativeChoice)
                            .add(absoluteChoice))
                        .addPreferredGap(LayoutStyle.RELATED)
                        .add(layout.createParallelGroup()
                            .add(relativeSize, GroupLayout.PREFERRED_SIZE, 50, GroupLayout.PREFERRED_SIZE)
                            .add(absoluteSize, GroupLayout.PREFERRED_SIZE, 50, GroupLayout.PREFERRED_SIZE))))
                .addPreferredGap(LayoutStyle.UNRELATED)
                .add(layout.createParallelGroup()
                    .add(fontStyleLabel)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup()
                            .add(italicCheckBox)
                            .add(layout.createSequentialGroup()
                                .add(17)
                                .add(layout.createParallelGroup()
                                    .add(addItalicChoice)
                                    .add(removeItalicChoice))))
                        .addPreferredGap(LayoutStyle.RELATED)
                        .add(layout.createParallelGroup()
                            .add(thicknessCheckBox)
                            .add(layout.createSequentialGroup()
                                .add(17)
                                .add(layout.createParallelGroup()
                                    .add(removeBoldChoice)
                                    .add(addBoldChoice))))))
                    .addContainerGap());
            layout.setVerticalGroup(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(GroupLayout.BASELINE)
                    .add(fontSizeLabel)
                    .add(fontStyleLabel))
                .addPreferredGap(LayoutStyle.RELATED)
                .add(layout.createParallelGroup(GroupLayout.BASELINE)
                    .add(relativeChoice)
                    .add(relativeSize, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .add(italicCheckBox)
                    .add(thicknessCheckBox))
                .addPreferredGap(LayoutStyle.RELATED)
                .add(layout.createParallelGroup(GroupLayout.BASELINE)
                    .add(absoluteChoice)
                    .add(absoluteSize, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .add(addItalicChoice)
                    .add(addBoldChoice))
                .addPreferredGap(LayoutStyle.RELATED)
                .add(layout.createParallelGroup(GroupLayout.BASELINE)
                    .add(removeItalicChoice)
                    .add(removeBoldChoice))
                .addContainerGap());
        }

        private class Listener implements ItemListener, ChangeListener {
            public void itemStateChanged(ItemEvent e) {
                if (ignoreUpdates) return;
                ignoreUpdates = true;
                Object source = e.getSource();
                if (source == relativeChoice) {
                    boolean relative = relativeChoice.isSelected();
                    relativeSize.setEnabled(relative);
                    absoluteSize.setEnabled(!relative);
                    propertyValue.absoluteSize = !relative;
                    propertyValue.size = ((Number)((relative ? relativeSize : absoluteSize).getValue())).intValue();
                } else if (source == italicCheckBox) {
                    boolean changeItalic = italicCheckBox.isSelected();
                    addItalicChoice.setEnabled(changeItalic);
                    removeItalicChoice.setEnabled(changeItalic);
                    propertyValue.italic = changeItalic ? Boolean.valueOf(addItalicChoice.isSelected()) : null;
                } else if (source == thicknessCheckBox) {
                    boolean changeBold = thicknessCheckBox.isSelected();
                    addBoldChoice.setEnabled(changeBold);
                    removeBoldChoice.setEnabled(changeBold);
                    propertyValue.bold = changeBold ? Boolean.valueOf(addBoldChoice.isSelected()) : null;
                } else if (source == addBoldChoice) {
                    propertyValue.bold = Boolean.valueOf(addBoldChoice.isSelected());
                } else if (source == addItalicChoice) {
                    propertyValue.italic = Boolean.valueOf(addItalicChoice.isSelected());
                }
                ignoreUpdates = false;
                updateDelegate();
            }

            public void stateChanged(ChangeEvent e) {
                if (ignoreUpdates) return;
                ignoreUpdates = true;
                Object source = e.getSource();
                if (source == relativeSize) {
                    propertyValue.size = ((Number)relativeSize.getValue()).intValue();
                    synchronizeSizeControls();
                } else if(source == absoluteSize) {
                    propertyValue.size = ((Number)absoluteSize.getValue()).intValue();
                    synchronizeSizeControls();
                }
                ignoreUpdates = false;
                updateDelegate();
            }
        }
    }
    
}