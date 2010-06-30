/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

package org.netbeans.modules.encoder.custom.aip;

import com.sun.encoder.custom.appinfo.DelimiterSet;
import com.sun.encoder.custom.appinfo.NodeProperties;
import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.Set;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.Lookup;
import org.openide.util.ImageUtilities;
import org.openide.util.WeakListeners;

/**
 * The node implementation for displaying encoding information.
 *
 * @author Jun Xu
 */
public class EncodingNode extends AbstractNode
        implements PropertyChangeListener {

    private static final ResourceBundle _bundle =
            ResourceBundle.getBundle("org/netbeans/modules/encoder/custom/aip/Bundle");
    private static final Set<String> mChangeSheetPropNames = new HashSet<String>();
    static {
        mChangeSheetPropNames.add("nodeType"); //NOI18N
        mChangeSheetPropNames.add("xmlType"); //NOI18N
        mChangeSheetPropNames.add("typeDef"); //NOI18N
        mChangeSheetPropNames.add("top");  //NOI18N
        mChangeSheetPropNames.add("fixedLengthType");  //NOI18N
    }

    private final EncodingOption mEncodingOption;

    /** Creates a new instance of EncodingInfoNode */
    public EncodingNode(EncodingOption encodingOption, Lookup lookup) {
        super(new Children.Array(), lookup);
        mEncodingOption = encodingOption;
        encodingOption.addPropertyChangeListener(
                WeakListeners.propertyChange(this, encodingOption));
    }

    @Override
    public String getDisplayName() {
        return _bundle.getString("encoding_node.lbl.encoding");
    }

    @Override
    public String getName() {
        return "encoding"; //NOI18N
    }

    @Override
    public boolean canRename() {
        return false;
    }

    @Override
    public boolean canCut() {
        return false;
    }

    @Override
    public boolean canCopy() {
        return false;
    }

    @Override
    public String getHtmlDisplayName() {
        if (mEncodingOption == null) {
            //Must be some kind of invalid XML causing this.
            //Display it using warning color
            return "<font color='!controlShadow'><i>" + getDisplayName()
                    + "</i></font>"; //NOI18N
        }
        return null;
    }

    @Override
    public Image getIcon(int i) {
        return ImageUtilities.loadImage(
                "org/netbeans/modules/encoder/custom/aip/icon.PNG");  //NOI18N
    }

    @Override
    public Image getOpenedIcon(int i) {
        return ImageUtilities.loadImage(
                "org/netbeans/modules/encoder/custom/aip/openIcon.PNG");  //NOI18N
    }

    @Override
    protected Sheet createSheet() {
        Sheet sheet = Sheet.createDefault();
        Sheet.Set propSet = Sheet.createPropertiesSet();
        try {
            //The read-only encoding style property
            propSet.put(new EncodingStyleProperty(
                    "encodingStyle", //NOI18N
                    String.class,
                    _bundle.getString("encoding_node.lbl.encoding_style"),
                    _bundle.getString("encoding_node.lbl.encoding_style_short")));

            //The Node Type Property
            {
                PropertySupport.Reflection<String> prop =
                        new PropertySupport.Reflection<String>(mEncodingOption,
                        String.class, "nodeType");  //NOI18N
                prop.setName("nodeType");  //NOI18N
                prop.setDisplayName(_bundle.getString("encoding_node.lbl.node_type"));
                prop.setPropertyEditorClass(NodeTypePropertyEditor.class);
                propSet.put(prop);
            }

            if (NodeProperties.NodeType.FIXED_LENGTH.equals(mEncodingOption.xgetNodeType())) {

                PropertySupport.Reflection<String> prop =
                        new PropertySupport.Reflection<String>(mEncodingOption,
                        String.class, "fixedLengthType");  //NOI18N
                prop.setName("fixedLengthType");  //NOI18N
                prop.setDisplayName(_bundle.getString("encoding_node.lbl.fixed_length_type"));
                prop.setPropertyEditorClass(FixedLengthTypePropertyEditor.class);
                propSet.put(prop);

                if (mEncodingOption.isUndeterminedFixedLengthType()) {
                    // do not show "length", "offset" or "position" fields
                } else if (mEncodingOption.isReversedFixedLengthType()) {
                    PropertySupport.Reflection<Integer> prop1 =
                            new PropertySupport.Reflection<Integer>(
                                mEncodingOption,
                                int.class,
                                "length");  //NOI18N
                    prop1.setName("lengthFromEnd");  //NOI18N
                    prop1.setDisplayName(_bundle.getString(
                            "encoding_node.lbl.length_from_end")); //NOI18N
                    propSet.put(prop1);
                } else if (mEncodingOption.isEncodedFixedLengthType()) {
                    PropertySupport.Reflection<Integer> prop1 =
                            new PropertySupport.Reflection<Integer>(
                                mEncodingOption,
                                int.class,
                                "length");  //NOI18N
                    prop1.setName("lengthEncoded");  //NOI18N
                    prop1.setDisplayName(_bundle.getString(
                            "encoding_node.lbl.length_encoded")); //NOI18N
                    propSet.put(prop1);

                    PropertySupport.Reflection<String> prop2 =
                            new PropertySupport.Reflection<String>(
                                mEncodingOption,
                                String.class,
                                "offset");  //NOI18N
                    prop2.setName("offsetEncoded");  //NOI18N
                    prop2.setDisplayName(_bundle.getString(
                            "encoding_node.lbl.offset_encoded")); //NOI18N
                    propSet.put(prop2);

                    PropertySupport.Reflection<String> prop3 =
                            new PropertySupport.Reflection<String>(
                                mEncodingOption,
                                String.class,
                                "position");  //NOI18N
                    prop3.setName("positionEncoded");  //NOI18N
                    prop3.setDisplayName(_bundle.getString(
                            "encoding_node.lbl.position_encoded")); //NOI18N
                    propSet.put(prop3);
                } else {
                    PropertySupport.Reflection<Integer> prop1 =
                            new PropertySupport.Reflection<Integer>(
                                mEncodingOption,
                                int.class,
                                "length");  //NOI18N
                    prop1.setName("length");  //NOI18N
                    prop1.setDisplayName(_bundle.getString(
                            "encoding_node.lbl.length")); //NOI18N
                    propSet.put(prop1);

                    PropertySupport.Reflection<String> prop2 =
                            new PropertySupport.Reflection<String>(
                                mEncodingOption,
                                String.class,
                                "offset");  //NOI18N
                    prop2.setName("offset");  //NOI18N
                    prop2.setDisplayName(_bundle.getString(
                            "encoding_node.lbl.offset")); //NOI18N
                    propSet.put(prop2);
                }
            }

            if (!NodeProperties.NodeType.TRANSIENT
                            .equals(mEncodingOption.xgetNodeType())) {
                PropertySupport.Reflection prop =
                        new DelimiterSetProperty(mEncodingOption,
                                DelimiterSet.class, "delimiterSet");  //NOI18N
                prop.setName("delimiterSet");  //NOI18N
                prop.setDisplayName(_bundle.getString(
                        "encoding_node.lbl.delim_list")); //NOI18N
                propSet.put(prop);
            }

            if (mEncodingOption.testIsGlobal()) {
                //The Top Property checkbox
                PropertySupport.Reflection<Boolean> prop =
                        new PropertySupport.Reflection<Boolean>(
                            mEncodingOption,
                            boolean.class,
                            "top");  //NOI18N
                prop.setName("top");  //NOI18N
                prop.setDisplayName(_bundle.getString("encoding_node.lbl.top")); //NOI18N
                propSet.put(prop);
            }

            if (mEncodingOption.testIsGlobal() && mEncodingOption.isTop()) {
                //The Input Character Set Property
                PropertySupport.Reflection<String> prop =
                        new PropertySupport.Reflection<String>(
                            mEncodingOption,
                            String.class,
                            "inputCharset");  //NOI18N
                prop.setName("inputCharset");  //NOI18N
                prop.setDisplayName(_bundle.getString(
                        "encoding_node.lbl.input_charset")); //NOI18N
                propSet.put(prop);

                //The Output Character Set Property
                PropertySupport.Reflection<String> prop2 =
                        new PropertySupport.Reflection<String>(
                            mEncodingOption,
                            String.class,
                            "outputCharset");  //NOI18N
                prop2.setName("outputCharset");  //NOI18N
                prop2.setDisplayName(_bundle.getString(
                        "encoding_node.lbl.output_charset")); //NOI18N
                propSet.put(prop2);
            }

            if (mEncodingOption.testIsGlobal() && mEncodingOption.isTop()
                    || NodeProperties.NodeType.FIXED_LENGTH.equals(
                            mEncodingOption.xgetNodeType())) {
                //The Parsing Character Set Property
                PropertySupport.Reflection<String> prop =
                        new PropertySupport.Reflection<String>(
                            mEncodingOption,
                            String.class,
                            "parsingCharset");  //NOI18N
                prop.setName("parsingCharset");  //NOI18N
                prop.setDisplayName(_bundle.getString(
                        "encoding_node.lbl.parsing_charset")); //NOI18N
                propSet.put(prop);

                //The Serializing Character Set Property
                PropertySupport.Reflection<String> prop2 =
                        new PropertySupport.Reflection<String>(
                            mEncodingOption,
                            String.class,
                            "serializingCharset");  //NOI18N
                prop2.setName("serializingCharset");  //NOI18N
                prop2.setDisplayName(_bundle.getString(
                        "encoding_node.lbl.serial_charset")); //NOI18N
                propSet.put(prop2);
            }

            if (!NodeProperties.NodeType.GROUP
                            .equals(mEncodingOption.xgetNodeType())
                    && mEncodingOption.testIsSimple()
                    && !NodeProperties.NodeType.TRANSIENT
                            .equals(mEncodingOption.xgetNodeType())) {
                PropertySupport.Reflection<String> prop =
                        new PropertySupport.Reflection<String>(
                            mEncodingOption,
                            String.class,
                            "alignment");  //NOI18N
                prop.setName("alignment");  //NOI18N
                prop.setDisplayName(_bundle.getString(
                        "encoding_node.lbl.alignment")); //NOI18N
                prop.setPropertyEditorClass(AlignmentPropertyEditor.class);
                propSet.put(prop);

                PropertySupport.Reflection<String> prop2 =
                        new PropertySupport.Reflection<String>(
                            mEncodingOption,
                            String.class,
                            "match");  //NOI18N
                prop2.setName("match");  //NOI18N
                prop2.setDisplayName(_bundle.getString(
                        "encoding_node.lbl.match")); //NOI18N
                propSet.put(prop2);

                //The NoMatch (boolean) Property
                PropertySupport.Reflection<Boolean> prop3 =
                        new PropertySupport.Reflection<Boolean>(
                            mEncodingOption,
                            boolean.class,
                            "noMatch");  //NOI18N
                prop3.setName("noMatch");  //NOI18N
                prop3.setDisplayName(_bundle.getString(
                        "encoding_node.lbl.no_match")); //NOI18N
                propSet.put(prop3);
            }

            if (!NodeProperties.NodeType.TRANSIENT
                            .equals(mEncodingOption.xgetNodeType())
                    && !mEncodingOption.testIsSimple()
                    && !mEncodingOption.testIsChoice()) {
                PropertySupport.Reflection<String> prop =
                        new PropertySupport.Reflection<String>(
                            mEncodingOption,
                            String.class,
                            "order");  //NOI18N
                prop.setName("order");  //NOI18N
                prop.setDisplayName(_bundle.getString(
                        "encoding_node.lbl.order")); //NOI18N
                prop.setPropertyEditorClass(OrderPropertyEditor.class);
                propSet.put(prop);
            }

            if (!NodeProperties.NodeType.TRANSIENT
                            .equals(mEncodingOption.xgetNodeType())
                    && !mEncodingOption.testIsSimple()) {
                PropertySupport.Reflection<String> prop1 =
                        new PropertySupport.Reflection<String>(
                            mEncodingOption,
                            String.class,
                            "nOfNminN");  //NOI18N
                prop1.setName("nOfNminN");  //NOI18N
                prop1.setDisplayName(_bundle.getString(
                        "encoding_node.lbl.n_of_n_min_n")); //NOI18N
                propSet.put(prop1);

                PropertySupport.Reflection<String> prop2 =
                        new PropertySupport.Reflection<String>(
                            mEncodingOption,
                            String.class,
                            "nOfNmaxN");  //NOI18N
                prop2.setName("nOfNmaxN");  //NOI18N
                prop2.setDisplayName(_bundle.getString(
                        "encoding_node.lbl.n_of_n_max_n")); //NOI18N
                propSet.put(prop2);
            }

            if (!NodeProperties.NodeType.TRANSIENT
                            .equals(mEncodingOption.xgetNodeType())
                    && !mEncodingOption.testIsGlobal()) {
                PropertySupport.Reflection<String> prop1 =
                        new PropertySupport.Reflection<String>(
                            mEncodingOption,
                            String.class,
                            "minOcc");  //NOI18N
                prop1.setName("minOcc");  //NOI18N
                prop1.setDisplayName(_bundle.getString(
                        "encoding_node.lbl.min_occ")); //NOI18N
                propSet.put(prop1);

                PropertySupport.Reflection<String> prop2 =
                        new PropertySupport.Reflection<String>(
                            mEncodingOption,
                            String.class,
                            "maxOcc");  //NOI18N
                prop2.setName("maxOcc");  //NOI18N
                prop2.setDisplayName(_bundle.getString(
                        "encoding_node.lbl.max_occ")); //NOI18N
                propSet.put(prop2);

                PropertySupport.Reflection<String> prop3 =
                        new PropertySupport.Reflection<String>(
                            mEncodingOption,
                            String.class,
                            "scvngrChars");  //NOI18N
                prop3.setName("scvngrChars");  //NOI18N
                prop3.setDisplayName(_bundle.getString(
                        "encoding_node.lbl.scvngr_chars")); //NOI18N
                propSet.put(prop3);

                PropertySupport.Reflection<Boolean> prop4 =
                        new PropertySupport.Reflection<Boolean>(
                            mEncodingOption,
                            boolean.class,
                            "scvngrEmit1st");  //NOI18N
                prop4.setName("scvngrEmit1st");  //NOI18N
                prop4.setDisplayName(_bundle.getString(
                        "encoding_node.lbl.scvngr_emit_1st")); //NOI18N
                propSet.put(prop4);
            }

            if (NodeProperties.NodeType.FIXED_LENGTH
                            .equals(mEncodingOption.xgetNodeType())) {
                PropertySupport.Reflection<String> prop =
                        new PropertySupport.Reflection<String>(
                            mEncodingOption,
                            String.class,
                            "beginDelimiter");  //NOI18N
                prop.setName("beginDelimiter");  //NOI18N
                prop.setDisplayName(_bundle.getString(
                        "encoding_node.lbl.begin_delimiter")); //NOI18N
                propSet.put(prop);
            } else if (NodeProperties.NodeType.DELIMITED
                            .equals(mEncodingOption.xgetNodeType())) {
                // reuse same real estate
                PropertySupport.Reflection<String> prop =
                        new ReadOnlyBeginDelimiterProperty(
                            mEncodingOption,
                            String.class,
                            "getBeginDelimitersAsString");  //NOI18N
                prop.setName("beginDelimiter");  //NOI18N
                prop.setDisplayName(_bundle.getString(
                        "encoding_node.lbl.begin_delimiter")); //NOI18N
                propSet.put(prop);
            }

            if (NodeProperties.NodeType.FIXED_LENGTH
                            .equals(mEncodingOption.xgetNodeType())) {
                PropertySupport.Reflection<Boolean> prop =
                        new PropertySupport.Reflection<Boolean>(
                            mEncodingOption,
                            boolean.class,
                            "beginDelimiterDetached");  //NOI18N
                prop.setName("beginDelimiterDetached");  //NOI18N
                prop.setDisplayName(_bundle.getString(
                        "encoding_node.lbl.begin_delimiter_detached")); //NOI18N
                propSet.put(prop);
            }

            if (NodeProperties.NodeType.DELIMITED
                            .equals(mEncodingOption.xgetNodeType())) {
                PropertySupport.Reflection<String> prop =
                        new ReadOnlyDelimiterProperty(
                            mEncodingOption,
                            String.class,
                            "getEndDelimitersAsString");  //NOI18N
                prop.setName("delimiter");  //NOI18N
                prop.setDisplayName(_bundle.getString(
                        "encoding_node.lbl.delimiter")); //NOI18N
                propSet.put(prop);
            }

            if (NodeProperties.NodeType.ARRAY
                            .equals(mEncodingOption.xgetNodeType())) {
                PropertySupport.Reflection<String> prop =
                        new ReadOnlyArrayDelimiterProperty(
                            mEncodingOption,
                            String.class,
                            "getArrayDelimitersAsString");  //NOI18N
                prop.setName("arrayDelimiter");  //NOI18N
                prop.setDisplayName(_bundle.getString(
                        "encoding_node.lbl.array_delimiter")); //NOI18N
                propSet.put(prop);
            }

            if (mEncodingOption.testIsGlobal() && mEncodingOption.isTop()) {
                //The Escape Sequence Property
                PropertySupport.Reflection<String> prop =
                        new PropertySupport.Reflection<String>(
                            mEncodingOption,
                            String.class,
                            "escapeSequence");  //NOI18N
                prop.setName("escapeSequence");  //NOI18N
                prop.setDisplayName(_bundle.getString(
                        "encoding_node.lbl.escape_sequence")); //NOI18N
                propSet.put(prop);
            }

            if (mEncodingOption.testIsGlobal() && mEncodingOption.isTop()) {
                //The Fine Inherit Property
                PropertySupport.Reflection<Boolean> prop =
                        new PropertySupport.Reflection<Boolean>(
                            mEncodingOption,
                            boolean.class,
                            "fineInherit");  //NOI18N
                prop.setName("fineInherit");  //NOI18N
                prop.setDisplayName(_bundle.getString(
                        "encoding_node.lbl.fine_inherit")); //NOI18N
                propSet.put(prop);

                PropertySupport.Reflection<String> prop2 =
                        new PropertySupport.Reflection<String>(
                            mEncodingOption,
                            String.class,
                            "undefDataPolicy");  //NOI18N
                prop2.setName("undefDataPolicy");  //NOI18N
                prop2.setDisplayName(_bundle.getString(
                        "encoding_node.lbl.undefined_data_policy")); //NOI18N
                prop2.setPropertyEditorClass(UndefDataPolicyPropertyEditor.class);
                propSet.put(prop2);
            }
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(_bundle.getString(
                    "encoding_node.exp.no_such_mthd"), e); //NOI18N
        }
        sheet.put(propSet);
        return sheet;
    }

    /**
     * This method gets called when a bound property is changed.
     * @param evt A PropertyChangeEvent object describing the event source
     *   	and the property that has changed.
     */
    public void propertyChange(PropertyChangeEvent evt) {
        if (mChangeSheetPropNames.contains(evt.getPropertyName())) {
            setSheet(createSheet());
        }
    }

    private static class EncodingStyleProperty
            extends PropertySupport.ReadOnly<String> {

        EncodingStyleProperty(String name, Class<String> clazz,
                String displayName, String desc) {
            super(name, clazz, displayName, desc);
        }

        public String getValue()
                throws IllegalAccessException, InvocationTargetException {
            return CustomEncodingConst.STYLE;
        }
    }
}
