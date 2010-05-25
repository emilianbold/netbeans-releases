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

import com.sun.encoder.custom.appinfo.CustomEncoding;
import com.sun.encoder.custom.appinfo.Delimiter;
import com.sun.encoder.custom.appinfo.DelimiterLevel;
import com.sun.encoder.custom.appinfo.DelimiterSet;
import com.sun.encoder.custom.appinfo.NodeProperties;
import com.sun.encoder.custom.appinfo.NodeProperties.DelimOfFixed;
import com.sun.encoder.custom.appinfo.NodeProperties.NOfN;
import com.sun.encoder.custom.appinfo.NodeProperties.Scvngr;
import com.sun.encoder.runtime.provider.Misc;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.netbeans.modules.encoder.ui.basic.EncodingConst;
import org.netbeans.modules.encoder.ui.basic.InvalidAppInfoException;
import org.netbeans.modules.encoder.ui.basic.SchemaUtility;
import org.netbeans.modules.encoder.ui.basic.ValidationException;
import org.netbeans.modules.xml.schema.model.Annotation;
import org.netbeans.modules.xml.schema.model.AppInfo;
import org.netbeans.modules.xml.schema.model.ComplexType;
import org.netbeans.modules.xml.schema.model.Element;
import org.netbeans.modules.xml.schema.model.ElementReference;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.LocalElement;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.schema.model.SimpleType;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * The model of the custom encoding node.
 *
 * @author Jun Xu
 */
public class EncodingOption {

    private static final ResourceBundle _bundle =
        ResourceBundle.getBundle(
        EncodingOption.class.getPackage().getName() + ".Bundle"); //NOI18N
    public static final String NODE_TYPE_PREFIX = "nodeType"; //NOI18N
    public static final String ALIGNMENT_PREFIX = "align"; //NOI18N
    public static final String ORDER_PREFIX = "order"; //NOI18N
    public static final String UNDEFINED_DATA_POLICY_PREFIX = "undefDataPolicy"; //NOI18N
    public static final String FIXED_LENGTH_TYPE_PREFIX = "fixedLengthType"; //NOI18N
    private static final String EMP = ""; //NOI18N
    private static final String UDS = "_"; //NOI18N
    private static final String MULTI_DELIM_SEPARATOR = ", "; //NOI18N

    private static final CustomEncoding mDefaultCustomEncoding =
            CustomEncoding.Factory.newInstance();

    private static final Map<String, String> mReverseTextMap =
            new HashMap<String, String>();
    private static final Map<String, String> mTextMap =
            new HashMap<String, String>();
    private static List<String> mNodeTypeTagList = new ArrayList<String>();
    private static List<String> mAlignmentTagList = new ArrayList<String>();
    private static List<String> mOrderTagList = new ArrayList<String>();
    private static List<String> mFixedLengthTypeTagList = new ArrayList<String>();
    private static List<String> mUndefDataPolicyTagList = new ArrayList<String>();

    static {
        //Populate the localized text map and the tag list for the node type property
        mReverseTextMap.put(NODE_TYPE_PREFIX + UDS
                + _bundle.getString("TAG_NodeType_group"), "group"); //NOI18N
        mReverseTextMap.put(NODE_TYPE_PREFIX + UDS
                + _bundle.getString("TAG_NodeType_array"), "array"); //NOI18N
        mReverseTextMap.put(NODE_TYPE_PREFIX + UDS
                + _bundle.getString("TAG_NodeType_delimited"), "delimited"); //NOI18N
        mReverseTextMap.put(NODE_TYPE_PREFIX + UDS
                + _bundle.getString("TAG_NodeType_fixedLength"), "fixedLength"); //NOI18N
        mReverseTextMap.put(NODE_TYPE_PREFIX + UDS
                + _bundle.getString("TAG_NodeType_transient"), "transient"); //NOI18N
        mTextMap.put(NODE_TYPE_PREFIX + UDS
                + "group", _bundle.getString("TAG_NodeType_group")); //NOI18N
        mTextMap.put(NODE_TYPE_PREFIX + UDS
                + "array", _bundle.getString("TAG_NodeType_array")); //NOI18N
        mTextMap.put(NODE_TYPE_PREFIX + UDS
                + "delimited", _bundle.getString("TAG_NodeType_delimited")); //NOI18N
        mTextMap.put(NODE_TYPE_PREFIX + UDS
                + "fixedLength", _bundle.getString("TAG_NodeType_fixedLength")); //NOI18N
        mTextMap.put(NODE_TYPE_PREFIX + UDS
                + "transient", _bundle.getString("TAG_NodeType_transient")); //NOI18N
        mNodeTypeTagList.add(_bundle.getString("TAG_NodeType_group"));
        mNodeTypeTagList.add(_bundle.getString("TAG_NodeType_array"));
        mNodeTypeTagList.add(_bundle.getString("TAG_NodeType_delimited"));
        mNodeTypeTagList.add(_bundle.getString("TAG_NodeType_fixedLength"));
        mNodeTypeTagList.add(_bundle.getString("TAG_NodeType_transient"));
        mNodeTypeTagList = Collections.unmodifiableList(mNodeTypeTagList);

        //Populate the localized text map and the tag list for the alignment property
        mReverseTextMap.put(ALIGNMENT_PREFIX + UDS
                + _bundle.getString("TAG_Alignment_blind"), "blind"); //NOI18N
        mReverseTextMap.put(ALIGNMENT_PREFIX + UDS
                + _bundle.getString("TAG_Alignment_exact"), "exact"); //NOI18N
        mReverseTextMap.put(ALIGNMENT_PREFIX + UDS
                + _bundle.getString("TAG_Alignment_begin"), "begin"); //NOI18N
        mReverseTextMap.put(ALIGNMENT_PREFIX + UDS
                + _bundle.getString("TAG_Alignment_final"), "final"); //NOI18N
        mReverseTextMap.put(ALIGNMENT_PREFIX + UDS
                + _bundle.getString("TAG_Alignment_inter"), "inter"); //NOI18N
        mReverseTextMap.put(ALIGNMENT_PREFIX + UDS
                + _bundle.getString("TAG_Alignment_super"), "super"); //NOI18N
        mReverseTextMap.put(ALIGNMENT_PREFIX + UDS
                + _bundle.getString("TAG_Alignment_oneof"), "oneof"); //NOI18N
        mReverseTextMap.put(ALIGNMENT_PREFIX + UDS
                + _bundle.getString("TAG_Alignment_regex"), "regex"); //NOI18N
        mTextMap.put(ALIGNMENT_PREFIX + UDS
                + "blind", _bundle.getString("TAG_Alignment_blind")); //NOI18N
        mTextMap.put(ALIGNMENT_PREFIX + UDS
                + "exact", _bundle.getString("TAG_Alignment_exact")); //NOI18N
        mTextMap.put(ALIGNMENT_PREFIX + UDS
                + "begin", _bundle.getString("TAG_Alignment_begin")); //NOI18N
        mTextMap.put(ALIGNMENT_PREFIX + UDS
                + "final", _bundle.getString("TAG_Alignment_final")); //NOI18N
        mTextMap.put(ALIGNMENT_PREFIX + UDS
                + "inter", _bundle.getString("TAG_Alignment_inter")); //NOI18N
        mTextMap.put(ALIGNMENT_PREFIX + UDS
                + "super", _bundle.getString("TAG_Alignment_super")); //NOI18N
        mTextMap.put(ALIGNMENT_PREFIX + UDS
                + "oneof", _bundle.getString("TAG_Alignment_oneof")); //NOI18N
        mTextMap.put(ALIGNMENT_PREFIX + UDS
                + "regex", _bundle.getString("TAG_Alignment_regex")); //NOI18N
        mAlignmentTagList.add(_bundle.getString("TAG_Alignment_blind"));
        mAlignmentTagList.add(_bundle.getString("TAG_Alignment_exact"));
        mAlignmentTagList.add(_bundle.getString("TAG_Alignment_begin"));
        mAlignmentTagList.add(_bundle.getString("TAG_Alignment_final"));
        mAlignmentTagList.add(_bundle.getString("TAG_Alignment_inter"));
        mAlignmentTagList.add(_bundle.getString("TAG_Alignment_super"));
        mAlignmentTagList.add(_bundle.getString("TAG_Alignment_oneof"));
        mAlignmentTagList.add(_bundle.getString("TAG_Alignment_regex"));
        mAlignmentTagList = Collections.unmodifiableList(mAlignmentTagList);

        //Populate the localized text map and the tag list for the order property
        mReverseTextMap.put(ORDER_PREFIX + UDS
                + _bundle.getString("TAG_Order_sequence"), "sequence"); //NOI18N
        mReverseTextMap.put(ORDER_PREFIX + UDS
                + _bundle.getString("TAG_Order_any"), "any"); //NOI18N
        mReverseTextMap.put(ORDER_PREFIX + UDS
                + _bundle.getString("TAG_Order_mixed"), "mixed"); //NOI18N
        mTextMap.put(ORDER_PREFIX + UDS
                + "sequence", _bundle.getString("TAG_Order_sequence")); //NOI18N
        mTextMap.put(ORDER_PREFIX + UDS
                + "any", _bundle.getString("TAG_Order_any")); //NOI18N
        mTextMap.put(ORDER_PREFIX + UDS
                + "mixed", _bundle.getString("TAG_Order_mixed")); //NOI18N
        mOrderTagList.add(_bundle.getString("TAG_Order_sequence"));
        mOrderTagList.add(_bundle.getString("TAG_Order_any"));
        mOrderTagList.add(_bundle.getString("TAG_Order_mixed"));
        mOrderTagList = Collections.unmodifiableList(mOrderTagList);

        //Populate the localized text map and the tag list for the fixedLengthType property
        mReverseTextMap.put(FIXED_LENGTH_TYPE_PREFIX + UDS
                + _bundle.getString("TAG_FixedLengthType_regular"), "regular"); //NOI18N
        mReverseTextMap.put(FIXED_LENGTH_TYPE_PREFIX + UDS
                + _bundle.getString("TAG_FixedLengthType_encoded"), "encoded"); //NOI18N
        mReverseTextMap.put(FIXED_LENGTH_TYPE_PREFIX + UDS
                + _bundle.getString("TAG_FixedLengthType_undetermined"), "undetermined"); //NOI18N
        mReverseTextMap.put(FIXED_LENGTH_TYPE_PREFIX + UDS
                + _bundle.getString("TAG_FixedLengthType_reversed"), "reversed"); //NOI18N
        mTextMap.put(FIXED_LENGTH_TYPE_PREFIX + UDS
                + "regular", _bundle.getString("TAG_FixedLengthType_regular")); //NOI18N
        mTextMap.put(FIXED_LENGTH_TYPE_PREFIX + UDS
                + "encoded", _bundle.getString("TAG_FixedLengthType_encoded")); //NOI18N
        mTextMap.put(FIXED_LENGTH_TYPE_PREFIX + UDS
                + "undetermined", _bundle.getString("TAG_FixedLengthType_undetermined")); //NOI18N
        mTextMap.put(FIXED_LENGTH_TYPE_PREFIX + UDS
                + "reversed", _bundle.getString("TAG_FixedLengthType_reversed")); //NOI18N
        mFixedLengthTypeTagList.add(_bundle.getString("TAG_FixedLengthType_regular"));
        mFixedLengthTypeTagList.add(_bundle.getString("TAG_FixedLengthType_encoded"));
        mFixedLengthTypeTagList.add(_bundle.getString("TAG_FixedLengthType_undetermined"));
        mFixedLengthTypeTagList.add(_bundle.getString("TAG_FixedLengthType_reversed"));
        mFixedLengthTypeTagList = Collections.unmodifiableList(mFixedLengthTypeTagList);

        //Populate the localized text map and the tag list for the undefDataPolicy property
        mReverseTextMap.put(UNDEFINED_DATA_POLICY_PREFIX + UDS
                + _bundle.getString("TAG_UndefDataPolicy_map"), "map"); //NOI18N
        mReverseTextMap.put(UNDEFINED_DATA_POLICY_PREFIX + UDS
                + _bundle.getString("TAG_UndefDataPolicy_skip"), "skip"); //NOI18N
        mReverseTextMap.put(UNDEFINED_DATA_POLICY_PREFIX + UDS
                + _bundle.getString("TAG_UndefDataPolicy_prohibit"), "prohibit"); //NOI18N
        mTextMap.put(UNDEFINED_DATA_POLICY_PREFIX + UDS
                + "map", _bundle.getString("TAG_UndefDataPolicy_map")); //NOI18N
        mTextMap.put(UNDEFINED_DATA_POLICY_PREFIX + UDS
                + "skip", _bundle.getString("TAG_UndefDataPolicy_skip")); //NOI18N
        mTextMap.put(UNDEFINED_DATA_POLICY_PREFIX + UDS
                + "prohibit", _bundle.getString("TAG_UndefDataPolicy_prohibit")); //NOI18N
        mUndefDataPolicyTagList.add(_bundle.getString("TAG_UndefDataPolicy_map"));
        mUndefDataPolicyTagList.add(_bundle.getString("TAG_UndefDataPolicy_skip"));
        mUndefDataPolicyTagList.add(_bundle.getString("TAG_UndefDataPolicy_prohibit"));

        //Populate the default NodeProperties
        mDefaultCustomEncoding.addNewNodeProperties();
        mDefaultCustomEncoding.getNodeProperties().setNodeType(
                NodeProperties.NodeType.DELIMITED);
    }

    /* Bean property change listeners */
    private final List<PropertyChangeListener> propChangeListeners =
        Collections.synchronizedList(new LinkedList<PropertyChangeListener>());

    /**
     * Component path from which the encoding options are read
     */
    private final SchemaComponent[] mComponentPath;

    /* Bean property variables */
    private String mNodeType = mTextMap.get(NODE_TYPE_PREFIX
        + UDS + NodeProperties.NodeType.DELIMITED);
    private boolean mTop = false;
    private String mInputCharset = EMP;
    private String mParsingCharset = EMP;
    private String mSerializingCharset = EMP;
    private String mOutputCharset = EMP;
    private DelimiterSet mDelimiterSet = null;
    private String mOrder = mTextMap.get(ORDER_PREFIX
        + UDS + NodeProperties.Order.SEQUENCE);
    private String mNOfNminN = EMP;
    private String mNOfNmaxN = EMP;
    private String mMatch = EMP;
    private boolean mNoMatch = false;
    private String mAlignment = mTextMap.get(ALIGNMENT_PREFIX
        + UDS + NodeProperties.Alignment.BLIND);
    private String mMinOcc = EMP;
    private String mMaxOcc = EMP;
    private String mScvngrChars = EMP;
    private boolean mScvngrEmit1st = false;

    // for a fixedLength field
    private String mFixedLengthType = mTextMap.get(FIXED_LENGTH_TYPE_PREFIX
        + UDS + "regular"); //NOI18N
    private int mLength = 0;
    private String mOffset = EMP;
    private String mPosition = EMP;
    private String mBeginDelimiter = EMP;
    private boolean mBeginDelimiterDetached = false;
    // global
    private String mEscapeSequence = EMP;
    private boolean mFineInherit = false;
    private String mUndefDataPolicy = mTextMap.get(UNDEFINED_DATA_POLICY_PREFIX
        + UDS + NodeProperties.UndefDataPolicy.PROHIBIT);

    private CustomEncoding mCustomEncoding = null;
    private AppInfo mAppInfo = null;
    private PropertyChangeListener mSchemaPropChangeListener;

    /**
     * Creates a new instance of EncodingOption
     * @param path - a list of SchemaComponent
     */
    private EncodingOption(List<SchemaComponent> path) {
        if (path == null) {
            throw new NullPointerException(
                _bundle.getString("encoding_opt.exp.no_component_path")); //NOI18N
        }
        if (path.size() < 1) {
            throw new IllegalArgumentException(
                _bundle.getString("encoding_opt.exp.illegal_comp_path")); //NOI18N
        }
        mComponentPath = path.toArray(new SchemaComponent[0]);
    }

    public static EncodingOption createFromAppInfo(List<SchemaComponent> path)
            throws InvalidAppInfoException {
        return createFromAppInfo(path, true);
    }

    /**
     * Create EncodingOption object from AppInfo.
     *
     * @param path list of SchemaComponent.
     * @param hookUpListener whether or not to hookup listener.
     * @return created EncodingOption object from AppInfo.
     * @throws org.netbeans.modules.encoder.ui.basic.InvalidAppInfoException
     */
    public static EncodingOption createFromAppInfo(List<SchemaComponent> path,
            boolean hookUpListener)
            throws InvalidAppInfoException {

        EncodingOption option = new EncodingOption(path);
        if (!option.init(hookUpListener)) {
            return null;
        }
        return option;
    }

    public static Map<String, String> textMap() {
        return mTextMap;
    }

    public static Map<String, String> reverseTextMap() {
        return mReverseTextMap;
    }

    public static List<String> nodeTypeTagList() {
        return mNodeTypeTagList;
    }

    public static List<String> alignmentTagList() {
        return mAlignmentTagList;
    }

    public static List<String> orderTagList() {
        return mOrderTagList;
    }

    public static List<String> fixedLengthTypeTagList() {
        return mFixedLengthTypeTagList;
    }

    public static List<String> undefDataPolicyTagList() {
        return mUndefDataPolicyTagList;
    }

    public String getAlignment() {
        return mAlignment;
    }

    public void setAlignment(String alignment) {
        String old = mAlignment;
        mAlignment = alignment;
        NodeProperties.Alignment.Enum enumAlignment =
            NodeProperties.Alignment.Enum.forString(
            mReverseTextMap.get(ALIGNMENT_PREFIX + UDS + mAlignment));
        firePropertyChange("alignment", old, mAlignment); //NOI18N
        mCustomEncoding.getNodeProperties().setAlignment(enumAlignment);
        commitToAppInfo();
    }

    public String getEndDelimitersAsString() {
        String delimString = null;
        try {
            delimString = computeEndDelimiters();
        } catch (InvalidAppInfoException ex) {
            return _bundle.getString("encoding_opt.lbl.error_retrieving_delim"); //NOI18N
        }
        // ensure that end delimiter(s) exist (not null).
        if (delimString == null) {
            delimString = _bundle.getString("encoding_opt.lbl.delim_not_set"); //NOI18N
        }
        return delimString;
    }

    public String getBeginDelimitersAsString() {
        String delimString = null;
        try {
            delimString = computeBeginDelimiters();
        } catch (InvalidAppInfoException ex) {
            return _bundle.getString("encoding_opt.lbl.error_retrieving_delim"); //NOI18N
        }
        if (delimString == null) {
            delimString = EMP;
        }
        return delimString;
    }

    public String getArrayDelimitersAsString() {
        String delimString = null;
        try {
            delimString = computeArrayDelimiters();
        } catch (InvalidAppInfoException ex) {
            return _bundle.getString("encoding_opt.lbl.error_retrieving_delim"); //NOI18N
        }
        if (delimString == null) {
            delimString = EMP;
        }
        return delimString;
    }

    public DelimiterSet getDelimiterSet() {
        return mDelimiterSet;
    }

    public void setDelimiterSet(DelimiterSet delimiterSet) {
        DelimiterSet old = mDelimiterSet;
        mDelimiterSet = delimiterSet;
        if (delimiterSet == null) {
            if (mCustomEncoding.getNodeProperties().isSetDelimiterSet()) {
                mCustomEncoding.getNodeProperties().unsetDelimiterSet();
            }
        } else {
            mCustomEncoding.getNodeProperties().setDelimiterSet(mDelimiterSet);
        }
        commitToAppInfo();
        firePropertyChange("delimiterSet", old, mDelimiterSet); //NOI18N
    }

    public String getInputCharset() {
        return mInputCharset;
    }

    public void setInputCharset(String inputCharset) {
        String old = mInputCharset;
        mInputCharset = inputCharset;
        if (mInputCharset == null || mInputCharset.length() == 0) {
            if (mCustomEncoding.getNodeProperties().isSetInputCharset()) {
                mCustomEncoding.getNodeProperties().unsetInputCharset();
            }
        } else {
            mCustomEncoding.getNodeProperties().setInputCharset(inputCharset);
        }
        commitToAppInfo();
        firePropertyChange("inputCharset", old, mInputCharset); //NOI18N
    }

    public int getLength() {
        return mLength;
    }

    public void setLength(int length) {
        if (length < 0) {
            length *= -1;
        }
        Integer old = Integer.valueOf(mLength);
        mLength = length;
        if (isReversedFixedLengthType()) {
            mCustomEncoding.getNodeProperties().setLength(mLength * (-1));
        } else {
            mCustomEncoding.getNodeProperties().setLength(mLength);
        }
        commitToAppInfo();
        firePropertyChange("length", old, Integer.valueOf(mLength)); //NOI18N
    }

    public String getOffset() {
        return mOffset;
    }

    public void setOffset(String offset) {
        if (offset == null || offset.length() == 0) {
            mCustomEncoding.getNodeProperties().unsetOffset();
        } else {
            long l = 0;
            try {
                l = Long.parseLong(offset);
            } catch (NumberFormatException e) {
                // do nothing
            }
            if (l < 0) {
                // change to positive number instead
                l *= -1;
            }
            offset = Long.toString(l);
            mCustomEncoding.getNodeProperties().setOffset(l);
        }
        String old = mOffset;
        mOffset = offset;
        commitToAppInfo();
        firePropertyChange("offset", old, mOffset); //NOI18N
    }

    public String getPosition() {
        return mPosition;
    }

    public void setPosition(String position) {
        if (position == null || position.length() == 0) {
            mCustomEncoding.getNodeProperties().unsetPosition();
        } else {
            long l = 0;
            try {
                l = Long.parseLong(position);
            } catch (NumberFormatException e) {
                // do nothing
            }
            if (l < 0) {
                // change to positive number instead
                l *= -1;
            }
            position = Long.toString(l);
            mCustomEncoding.getNodeProperties().setPosition(l);
        }
        String old = mPosition;
        mPosition = position;
        commitToAppInfo();
        firePropertyChange("position", old, mPosition); //NOI18N
    }

    public String getNOfNminN() {
        return mNOfNminN;
    }

    public void setNOfNminN(String nOfNminN) {
        if (nOfNminN == null || nOfNminN.length() == 0) {
            if (mCustomEncoding.getNodeProperties().isSetNOfN()) {
                mCustomEncoding.getNodeProperties().getNOfN().unsetMinN();
            }
        } else {
            int l = 0;
            try {
                l = Integer.parseInt(nOfNminN);
            } catch (NumberFormatException e) {
                // do nothing
            }
            if (l < 0) {
                // change to positive number instead
                l *= -1;
            }
            nOfNminN = Integer.toString(l);
            if (!mCustomEncoding.getNodeProperties().isSetNOfN()) {
                mCustomEncoding.getNodeProperties().addNewNOfN();
            }
            mCustomEncoding.getNodeProperties().getNOfN().setMinN(l);
        }
        String old = mNOfNminN;
        mNOfNminN = nOfNminN;
        commitToAppInfo();
        firePropertyChange("nOfNminN", old, mNOfNminN); //NOI18N
    }

    public String getNOfNmaxN() {
        return mNOfNmaxN;
    }

    public void setNOfNmaxN(String nOfNmaxN) {
        if (nOfNmaxN == null || nOfNmaxN.length() == 0) {
            if (mCustomEncoding.getNodeProperties().isSetNOfN()) {
                mCustomEncoding.getNodeProperties().getNOfN().unsetMaxN();
            }
        } else {
            int l = 1;
            try {
                l = Integer.parseInt(nOfNmaxN);
            } catch (NumberFormatException e) {
                // do nothing
            }
            if (l < 0) {
                // change to positive number instead
                l *= -1;
            }
            nOfNmaxN = Integer.toString(l);
            if (!mCustomEncoding.getNodeProperties().isSetNOfN()) {
                mCustomEncoding.getNodeProperties().addNewNOfN();
            }
            mCustomEncoding.getNodeProperties().getNOfN().setMaxN(l);
        }
        String old = mNOfNmaxN;
        mNOfNmaxN = nOfNmaxN;
        commitToAppInfo();
        firePropertyChange("nOfNmaxN", old, mNOfNmaxN); //NOI18N
    }

    public String getMinOcc() {
        return mMinOcc;
    }

    public void setMinOcc(String minOcc) {
        if (minOcc == null || minOcc.length() == 0) {
            mCustomEncoding.getNodeProperties().unsetMinOcc();
        } else {
            long l = 0;
            try {
                l = Long.parseLong(minOcc);
            } catch (NumberFormatException e) {
                // do nothing
            }
            if (l < 0) {
                // change to positive number instead
                l *= -1;
            }
            minOcc = Long.toString(l);
            mCustomEncoding.getNodeProperties().setMinOcc(l);
        }
        String old = mMinOcc;
        mMinOcc = minOcc;
        commitToAppInfo();
        firePropertyChange("minOcc", old, mMinOcc); //NOI18N
    }

    public String getMaxOcc() {
        return mMaxOcc;
    }

    public void setMaxOcc(String maxOcc) {
        if (maxOcc == null || maxOcc.length() == 0
                || _bundle.getString("encoding_node.value.unbounded").equalsIgnoreCase(maxOcc)) { //NOI18N
            mCustomEncoding.getNodeProperties().unsetMaxOcc();
        } else {
            long l = 0;
            try {
                l = Long.parseLong(maxOcc);
            } catch (NumberFormatException e) {
                // do nothing
            }
            if (l < 0) {
                // change to positive number instead
                l *= -1;
            }
            maxOcc = Long.toString(l);
            mCustomEncoding.getNodeProperties().setMaxOcc(l);
        }
        String old = mMaxOcc;
        mMaxOcc = maxOcc;
        commitToAppInfo();
        firePropertyChange("maxOcc", old, mMaxOcc); //NOI18N
    }

    public String getScvngrChars() {
        return mScvngrChars;
    }

    public void setScvngrChars(String scvngrChars) {
        String old = mScvngrChars;
        mScvngrChars = scvngrChars;
        if (mScvngrChars == null || mScvngrChars.length() == 0) {
            mCustomEncoding.getNodeProperties().unsetScvngr();
        } else {
            if (!mCustomEncoding.getNodeProperties().isSetScvngr()) {
                mCustomEncoding.getNodeProperties().addNewScvngr();
            }
            mCustomEncoding.getNodeProperties().getScvngr().setChars(scvngrChars);
        }
        commitToAppInfo();
        firePropertyChange("scvngrChars", old, mScvngrChars); //NOI18N
    }

    public boolean isScvngrEmit1st() {
        return mScvngrEmit1st;
    }

    public void setScvngrEmit1st(boolean scvngrEmit1st) {
        if (!mCustomEncoding.getNodeProperties().isSetScvngr()
                || mCustomEncoding.getNodeProperties().getScvngr().getChars() == null
                || mCustomEncoding.getNodeProperties().getScvngr().getChars().length() == 0) {
            // do nothing so that as if user can not
            // check the "Output First Scavenger Character" because the
            // "Scavenger Characters" field is not set to any value yet.
            mScvngrEmit1st = false;
            return;
        }
        boolean old = mScvngrEmit1st;
        mScvngrEmit1st = scvngrEmit1st;
        mCustomEncoding.getNodeProperties().getScvngr().setEmit1St(mScvngrEmit1st);
        commitToAppInfo();
        firePropertyChange("scvngrEmit1st", old, mScvngrEmit1st); //NOI18N
    }

    public String getBeginDelimiter() {
        return mBeginDelimiter;
    }

    public void setBeginDelimiter(String beginDelimiter) {
        String old = mBeginDelimiter;
        mBeginDelimiter = beginDelimiter;
        if (mBeginDelimiter == null || mBeginDelimiter.length() == 0) {
            // if empty delimiter is set, then remove the "delimOfFixed" element
            if (mCustomEncoding.getNodeProperties().isSetDelimOfFixed()) {
                mCustomEncoding.getNodeProperties().unsetDelimOfFixed();
            }
        } else {
            if (!mCustomEncoding.getNodeProperties().isSetDelimOfFixed()) {
                mCustomEncoding.getNodeProperties().addNewDelimOfFixed();
            }
            mCustomEncoding.getNodeProperties().getDelimOfFixed().setBeginBytes(mBeginDelimiter);
        }
        commitToAppInfo();
        firePropertyChange("beginDelimiter", old, mBeginDelimiter); //NOI18N
    }

    public boolean isBeginDelimiterDetached() {
        return mBeginDelimiterDetached;
    }

    public void setBeginDelimiterDetached(boolean beginDelimiterDetached) {
        if (!mCustomEncoding.getNodeProperties().isSetDelimOfFixed()) {
            // do nothing so that as if user can not
            // check the "Begin Delimiter Detached" because the
            // "Begin Delimiter" field is not set to any value yet.
            mBeginDelimiterDetached = false;
            return;
        }
        boolean old = mBeginDelimiterDetached;
        mBeginDelimiterDetached = beginDelimiterDetached;
        mCustomEncoding.getNodeProperties().getDelimOfFixed().setBeginAnch(!mBeginDelimiterDetached);
        commitToAppInfo();
        firePropertyChange("beginDelimiterDetached", old, mBeginDelimiterDetached); //NOI18N
    }

    public String getMatch() {
        return mMatch;
    }

    public void setMatch(String match) {
        String old = mMatch;
        mMatch = match;
        if (mMatch == null || mMatch.length() == 0) {
            if (mCustomEncoding.getNodeProperties().isSetMatch()) {
                mCustomEncoding.getNodeProperties().unsetMatch();
            }
        } else {
            mCustomEncoding.getNodeProperties().setMatch(match);
        }
        commitToAppInfo();
        firePropertyChange("match", old, mMatch); //NOI18N
    }

    public String getEscapeSequence() {
        return mEscapeSequence;
    }

    public void setEscapeSequence(String escapeSequence) {
        String old = mEscapeSequence;
        mEscapeSequence = escapeSequence;
        if (mEscapeSequence == null || mEscapeSequence.length() == 0) {
            if (mCustomEncoding.getNodeProperties().isSetEscapeSequence()) {
                // if no value, then unset the "escapeSequence" element
                mCustomEncoding.getNodeProperties().unsetEscapeSequence();
            }
        } else {
            mCustomEncoding.getNodeProperties().setEscapeSequence(mEscapeSequence);
        }
        commitToAppInfo();
        firePropertyChange("escapeSequence", old, mEscapeSequence); //NOI18N
    }

    public boolean isFineInherit() {
        return mFineInherit;
    }

    public void setFineInherit(boolean fineInherit) {
        Boolean old = Boolean.valueOf(mFineInherit);
        mFineInherit = fineInherit;
        if (!mFineInherit) {
            // if false, remove the "fineInherit" element.
            mCustomEncoding.getNodeProperties().unsetFineInherit();
        } else {
            mCustomEncoding.getNodeProperties().setFineInherit(mFineInherit);
        }
        commitToAppInfo();
        firePropertyChange("fineInherit", old, Boolean.valueOf(mFineInherit)); //NOI18N
    }

    public String getFixedLengthType() {
        return mFixedLengthType;
    }

    public void setFixedLengthType(String fixedLengthType) {
        String old = mFixedLengthType;
        mFixedLengthType = fixedLengthType;
        String type = mReverseTextMap.get(FIXED_LENGTH_TYPE_PREFIX + UDS + mFixedLengthType);
        String regexAlignment = mTextMap.get(ALIGNMENT_PREFIX + UDS + NodeProperties.Alignment.REGEX);
        String dummyMatch = _bundle.getString("encoding_node.value.default_match"); //NOI18N
        if ("regular".equals(type)) { //NOI18N
            // regular fixed length: only length and offset fields make sense
            if (mCustomEncoding.getNodeProperties().isSetPosition()) {
                mCustomEncoding.getNodeProperties().unsetPosition();
            }
            if (regexAlignment.equals(getAlignment()) && dummyMatch.equals(getMatch())) {
                setAlignment(mTextMap.get(ALIGNMENT_PREFIX + UDS + NodeProperties.Alignment.BLIND));
                setMatch(EMP);
            }
        } else if ("reversed".equals(type)) { //NOI18N
            // this means the length is deducted from the end of data
            // Only length field makes sense
            if (mCustomEncoding.getNodeProperties().isSetPosition()) {
                mCustomEncoding.getNodeProperties().unsetPosition();
            }
            if (mCustomEncoding.getNodeProperties().isSetOffset()) {
                mCustomEncoding.getNodeProperties().unsetOffset();
            }
            if (regexAlignment.equals(getAlignment()) && dummyMatch.equals(getMatch())) {
                setAlignment(mTextMap.get(ALIGNMENT_PREFIX + UDS + NodeProperties.Alignment.BLIND));
                setMatch(EMP);
            }
        } else if ("undetermined".equals(type)) { //NOI18N
            // this means this fixed length field is determined by a regular
            // expression match at runtime
            mLength = 0;
            mCustomEncoding.getNodeProperties().setLength(mLength);
            if (mCustomEncoding.getNodeProperties().isSetPosition()) {
                mCustomEncoding.getNodeProperties().unsetPosition();
            }
            if (mCustomEncoding.getNodeProperties().isSetOffset()) {
                mCustomEncoding.getNodeProperties().unsetOffset();
            }
            if (!regexAlignment.equals(getAlignment())) {
                setAlignment(regexAlignment);
                if (getMatch() == null || getMatch().length() == 0) {
                    setMatch(dummyMatch);
                }
            }
        } else {
            if (regexAlignment.equals(getAlignment()) && dummyMatch.equals(getMatch())) {
                setAlignment(mTextMap.get(ALIGNMENT_PREFIX + UDS + NodeProperties.Alignment.BLIND));
                setMatch(EMP);
            }
        }
        firePropertyChange("fixedLengthType", old, mFixedLengthType); //NOI18N
        commitToAppInfo();
    }

    public String getUndefDataPolicy() {
        return mUndefDataPolicy;
    }

    public void setUndefDataPolicy(String undefDataPolicy) {
        if (undefDataPolicy == null || undefDataPolicy.length() == 0) {
            // do nothing
            return;
        }
        String old = undefDataPolicy;
        mUndefDataPolicy = undefDataPolicy;
        String policy = mReverseTextMap.get(UNDEFINED_DATA_POLICY_PREFIX + UDS + mUndefDataPolicy);
        if ("prohibit".equals(policy)) {
            mCustomEncoding.getNodeProperties().unsetUndefDataPolicy();
        } else {
            NodeProperties.UndefDataPolicy.Enum enumUndefDataPolicy =
                    NodeProperties.UndefDataPolicy.Enum.forString(policy);
            mCustomEncoding.getNodeProperties().setUndefDataPolicy(enumUndefDataPolicy);
        }
        firePropertyChange("undefDataPolicy", old, mUndefDataPolicy); //NOI18N
        commitToAppInfo();
    }

    public String getNodeType() {
        return mNodeType;
    }

    public void setNodeType(String nodeType) {
        String old = mNodeType;
        mNodeType = nodeType;
        NodeProperties.NodeType.Enum enumNodeType =
                NodeProperties.NodeType.Enum.forString(
                    mReverseTextMap.get(NODE_TYPE_PREFIX + UDS + mNodeType));
        mCustomEncoding.getNodeProperties().setNodeType(enumNodeType);
        if (!NodeProperties.NodeType.FIXED_LENGTH.equals(enumNodeType)) {
            if (mCustomEncoding.getNodeProperties().isSetLength()) {
                mCustomEncoding.getNodeProperties().unsetLength();
                mLength = 0;
            }
            if (!mTop) {
                if (mCustomEncoding.getNodeProperties().isSetParsingCharset()) {
                    mParsingCharset = EMP;
                    mCustomEncoding.getNodeProperties().unsetParsingCharset();
                }
                if (mCustomEncoding.getNodeProperties().isSetSerializingCharset()) {
                    mSerializingCharset = EMP;
                    mCustomEncoding.getNodeProperties().unsetSerializingCharset();
                }
            }
        }
        if (NodeProperties.NodeType.GROUP.equals(enumNodeType)
                || NodeProperties.NodeType.TRANSIENT.equals(enumNodeType)
                || !testIsSimple()) {
            if (mCustomEncoding.getNodeProperties().isSetAlignment()) {
                mAlignment = mTextMap.get(ALIGNMENT_PREFIX + UDS + NodeProperties.Alignment.BLIND);
                mCustomEncoding.getNodeProperties().unsetAlignment();
            }
            if (mCustomEncoding.getNodeProperties().isSetMatch()) {
                mMatch = EMP;
                mCustomEncoding.getNodeProperties().unsetMatch();
            }
        }
        firePropertyChange("nodeType", old, mNodeType); //NOI18N
        commitToAppInfo();
    }

    public boolean isNoMatch() {
        return mNoMatch;
    }

    public void setNoMatch(boolean noMatch) {
        // if No Match is selected but there is no "Match" value, then skip
        // to make No Match not selected
        if (noMatch && (mMatch == null || mMatch.length() == 0)) {
            return;
        }
        Boolean old = Boolean.valueOf(mNoMatch);
        mNoMatch = noMatch;
        if (mNoMatch) {
            mCustomEncoding.getNodeProperties().setNoMatch(mNoMatch);
        } else {
            // remove the "noMatch" element
            mCustomEncoding.getNodeProperties().unsetNoMatch();
        }
        commitToAppInfo();
        firePropertyChange("noMatch", old, Boolean.valueOf(mNoMatch)); //NOI18N
    }

    public String getOrder() {
        return mOrder;
    }

    public void setOrder(String order) {
        String old = mOrder;
        mOrder = order;
        NodeProperties.Order.Enum enumOrder =
                NodeProperties.Order.Enum.forString(
                    mReverseTextMap.get(ORDER_PREFIX + UDS + mOrder));
        firePropertyChange("order", old, mOrder); //NOI18N
        mCustomEncoding.getNodeProperties().setOrder(enumOrder);
        commitToAppInfo();
    }

    public String getOutputCharset() {
        return mOutputCharset;
    }

    public void setOutputCharset(String outputCharset) {
        String old = mOutputCharset;
        mOutputCharset = outputCharset;
        if (mOutputCharset == null || mOutputCharset.length() == 0) {
            if (mCustomEncoding.getNodeProperties().isSetOutputCharset()) {
                mCustomEncoding.getNodeProperties().unsetOutputCharset();
            }
        } else {
            mCustomEncoding.getNodeProperties().setOutputCharset(outputCharset);
        }
        commitToAppInfo();
        firePropertyChange("outputCharset", old, mOutputCharset); //NOI18N
    }

    public String getParsingCharset() {
        return mParsingCharset;
    }

    public void setParsingCharset(String parsingCharset) {
        String old = mParsingCharset;
        mParsingCharset = parsingCharset;
        if (mParsingCharset == null || mParsingCharset.length() == 0) {
            if (mCustomEncoding.getNodeProperties().isSetParsingCharset()) {
                mCustomEncoding.getNodeProperties().unsetParsingCharset();
            }
        } else {
            mCustomEncoding.getNodeProperties().setParsingCharset(parsingCharset);
        }
        commitToAppInfo();
        firePropertyChange("parsingCharset", old, mParsingCharset); //NOI18N
    }

    public String getSerializingCharset() {
        return mSerializingCharset;
    }

    public void setSerializingCharset(String serializingCharset) {
        String old = mSerializingCharset;
        mSerializingCharset = serializingCharset;
        if (mSerializingCharset == null || mSerializingCharset.length() == 0) {
            if (mCustomEncoding.getNodeProperties().isSetSerializingCharset()) {
                mCustomEncoding.getNodeProperties().unsetSerializingCharset();
            }
        } else {
            mCustomEncoding.getNodeProperties().setSerializingCharset(serializingCharset);
        }
        commitToAppInfo();
        firePropertyChange("serializingCharset", old, mSerializingCharset); //NOI18N
    }

    public boolean isTop() {
        return mTop;
    }

    public void setTop(boolean top) {
        Boolean old = Boolean.valueOf(mTop);
        mTop = top;
        mCustomEncoding.setTop(mTop);
        if (!mTop) {
            if (mCustomEncoding.getNodeProperties().isSetInputCharset()) {
                mInputCharset = EMP;
                mCustomEncoding.getNodeProperties().unsetInputCharset();
            }
            if (mCustomEncoding.getNodeProperties().isSetOutputCharset()) {
                mOutputCharset = EMP;
                mCustomEncoding.getNodeProperties().unsetOutputCharset();
            }
            if (!NodeProperties.NodeType.FIXED_LENGTH.equals(xgetNodeType())) {
                if (mCustomEncoding.getNodeProperties().isSetParsingCharset()) {
                    mParsingCharset = EMP;
                    mCustomEncoding.getNodeProperties().unsetParsingCharset();
                }
                if (mCustomEncoding.getNodeProperties().isSetSerializingCharset()) {
                    mSerializingCharset = EMP;
                    mCustomEncoding.getNodeProperties().unsetSerializingCharset();
                }
            }
        }
        commitToAppInfo();
        firePropertyChange("top", old, Boolean.valueOf(mTop)); //NOI18N
    }

    /**
     * Test if current "encoding" node belongs to an XSD global element.
     * @return true if current "encoding" node belongs to an XSD global element.
     */
    public boolean testIsGlobal() {
        for (int i = 0; i < mComponentPath.length; i++) {
            if (mComponentPath[i] instanceof ElementReference) {
                return false;
            }
        }
        return annotation().getParent() instanceof GlobalElement;
    }

    /**
     * Test if current "encoding" node belongs to a leaf node.
     * @return true if current "encoding" node belongs to a leaf node.
     */
    public boolean testIsSimple() {
        if (!(annotation().getParent() instanceof Element)) {
            return false;
        }
        return SchemaUtility.isSimpleContent((Element) annotation().getParent());
    }

    /**
     * Test if current "encoding" node belongs to a choice node.
     * @return true if current "encoding" node belongs to a choice node.
     */
    public boolean testIsChoice() {
        if (!(annotation().getParent() instanceof Element)) {
            return false;
        }
        return SchemaUtility.isChoice((Element) annotation().getParent());
    }

    public NodeProperties.NodeType.Enum xgetNodeType() {
        return mCustomEncoding.getNodeProperties().getNodeType();
    }

    public boolean isReversedFixedLengthType() {
        if (mTextMap.get(FIXED_LENGTH_TYPE_PREFIX + UDS + "reversed") //NOI18N
                .equals(getFixedLengthType())) {
            return true;
        }
        return false;
    }

    public boolean isEncodedFixedLengthType() {
        if (mTextMap.get(FIXED_LENGTH_TYPE_PREFIX + UDS + "encoded") //NOI18N
                .equals(getFixedLengthType())) {
            return true;
        }
        return false;
    }

    public boolean isUndeterminedFixedLengthType() {
        if (mTextMap.get(FIXED_LENGTH_TYPE_PREFIX + UDS + "undetermined") //NOI18N
                .equals(getFixedLengthType())) {
            return true;
        }
        return false;
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propChangeListeners.add(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propChangeListeners.remove(listener);
    }

    public void validate(ErrorHandler handler)
            throws ValidationException, SAXException {
        if (NodeProperties.NodeType.DELIMITED.equals(xgetNodeType())) {
            //is delimited
            String delimAsString = null;
            try {
                delimAsString = computeEndDelimiters();
            } catch (InvalidAppInfoException ex) {
                delimAsString = _bundle.getString(
                        "encoding_opt.lbl.error_retrieving_delim"); //NOI18N
            }
            if (delimAsString == null) {
                handler.error(
                        new SAXParseException(
                            NbBundle.getMessage(
                                EncodingOption.class,
                                "encoding_opt.exp.delim_not_set", //NOI18N
                                SchemaUtility.getNCNamePath(mComponentPath)),
                            annotation().getModel().getSchema().getTargetNamespace(),
                            null /*ModelUtils.getFilePath(annotation().getModel())*/,
                            -1,
                            -1));
            }
        } else if (NodeProperties.NodeType.FIXED_LENGTH.equals(xgetNodeType())) {
            if (getLength() == 0) {
                handler.warning(
                        new SAXParseException(
                            NbBundle.getMessage(
                                EncodingOption.class,
                                "encoding_opt.exp.zero_fixed_length", //NOI18N
                                SchemaUtility.getNCNamePath(mComponentPath)),
                            annotation().getModel().getSchema().getTargetNamespace(),
                            null /*ModelUtils.getFilePath(annotation().getModel())*/,
                            -1,
                            -1));
            }
            if (mCustomEncoding.getNodeProperties().isSetAlignment()
                    && NodeProperties.Alignment.EXACT.equals(
                        mCustomEncoding.getNodeProperties().getAlignment())
                    && mMatch != null && mLength > 0
                    && Misc.str2bytes(Misc.nonPrintable(mMatch)).length > mLength) {
                handler.error(
                        new SAXParseException(
                            NbBundle.getMessage(
                                EncodingOption.class,
                                "encoding_opt.exp.match_len_gt_fld_len", //NOI18N
                                SchemaUtility.getNCNamePath(mComponentPath)),
                            annotation().getModel().getSchema().getTargetNamespace(),
                            null /*ModelUtils.getFilePath(annotation().getModel())*/,
                            -1,
                            -1));
            }
        }
    }

    /**
     * Check to see if global 'Fine Inherit" flag is true.
     * @return true if global 'Fine Inherit" flag is true.
     * @throws org.netbeans.modules.encoder.ui.basic.InvalidAppInfoException
     */
    private boolean testIsGlobalFineInherit() throws InvalidAppInfoException {
        SchemaComponent comp;
        Annotation anno;
        CustomEncoding customEncoding;
        // we need to find out if fine-grained inheritance global flag is set
        boolean globalFineInheritFlag = false;
        comp = mComponentPath[1];
        if (comp instanceof GlobalElement) {
            anno = ((Element) comp).getAnnotation();
            if (anno != null) {
                customEncoding = fetchCustomEncoding(anno, null);
                if (customEncoding != null && customEncoding.getTop()
                    && customEncoding.isSetNodeProperties()) {
                    NodeProperties nProp = customEncoding.getNodeProperties();
                    if (nProp.isSetFineInherit()) {
                        globalFineInheritFlag = nProp.getFineInherit();
                    }
                }
            }
        }
        return globalFineInheritFlag;
    }

    /**
     * Return all end delimiters at the given delimiter level
     * as a comma separate string, e.g. "], }". It returns null
     * if there is no delimiter defined.
     *
     * @param delimLevel a given delimiter level
     * @return a comma separate string, e.g. "], }"
     */
    private String getEndDelimitersAsString(DelimiterLevel delimLevel) {
        String delimAsString = EMP;
        // get all delimiters at this delimiter level
        Delimiter[] delimArr = delimLevel.getDelimiterArray();
        for (int i = 0; i < delimArr.length; i++) {
            if (Delimiter.Kind.REPEAT.equals(delimArr[i].getKind())) {
                // skip repeat/array delimiter(s)
                continue;
            }
            if (!delimArr[i].isSetBytes()) {
                continue;
            }
            if (delimAsString.length() > 0) {
                // add separator due to multiple delimiters.
                delimAsString += MULTI_DELIM_SEPARATOR;
            }
            if (delimArr[i].getBytes().isSetConstant()) {
                // i.e. regular constant delimiter
                delimAsString += delimArr[i].getBytes().getConstant();
            } else if (delimArr[i].getBytes().isSetEmbedded()) {
                // i.e. embedded delimiter
                // output as e.g. "...., {embedded:10,2}"
                delimAsString += (
                        "{" //NOI18N
                        + _bundle.getString("encoding_opt.lbl.embedded") //NOI18N
                        + delimArr[i].getBytes().getEmbedded().getOffset()
                        + "," //NOI18N
                        + delimArr[i].getBytes().getEmbedded().getLength()
                        + "}"); //NOI18N
            }
        }
        if (delimAsString.length() == 0) {
            delimAsString = null;
        }
        return delimAsString;
    }

    private String getBeginDelimitersAsString(DelimiterLevel delimLevel) {
        String delimAsString = EMP;
        // get all delimiters at this delimiter level
        Delimiter[] delimArr = delimLevel.getDelimiterArray();
        for (int i = 0; i < delimArr.length; i++) {
            if (Delimiter.Kind.REPEAT.equals(delimArr[i].getKind())) {
                // skip repeat/array delimiter(s)
                continue;
            }
            if (!delimArr[i].isSetBeginBytes()) {
                continue;
            }
            if (delimAsString.length() > 0) {
                // add separator due to multiple delimiters.
                delimAsString += MULTI_DELIM_SEPARATOR;
            }
            if (delimArr[i].getBeginBytes().isSetConstant()) {
                // i.e. regular constant delimiter
                delimAsString += delimArr[i].getBeginBytes().getConstant();
            } else if (delimArr[i].getBeginBytes().isSetEmbedded()) {
                // i.e. embedded delimiter
                // output as e.g. "...., {embedded:10,2}"
                delimAsString += (
                        "{" //NOI18N
                        + _bundle.getString("encoding_opt.lbl.embedded") //NOI18N
                        + delimArr[i].getBeginBytes().getEmbedded().getOffset()
                        + "," //NOI18N
                        + delimArr[i].getBeginBytes().getEmbedded().getLength()
                        + "}"); //NOI18N
            }
        }
        if (delimAsString.length() == 0) {
            delimAsString = null;
        }
        return delimAsString;
    }

    private String getArrayDelimitersAsString(DelimiterLevel delimLevel) {
        String delimAsString = EMP;
        // get all delimiters at this delimiter level
        Delimiter[] delimArr = delimLevel.getDelimiterArray();
        for (int i = 0; i < delimArr.length; i++) {
            if (!Delimiter.Kind.REPEAT.equals(delimArr[i].getKind())) {
                // skip non-repeat/array delimiter(s)
                continue;
            }
            if (!delimArr[i].isSetBytes()) {
                continue;
            }
            if (delimAsString.length() > 0) {
                // add separator due to multiple delimiters.
                delimAsString += MULTI_DELIM_SEPARATOR;
            }
            if (delimArr[i].getBytes().isSetConstant()) {
                // i.e. regular constant delimiter
                delimAsString += delimArr[i].getBytes().getConstant();
            } else if (delimArr[i].getBytes().isSetEmbedded()) {
                // i.e. embedded delimiter
                // output as e.g. "...., {embedded:10,2}"
                delimAsString += (
                        "{" //NOI18N
                        + _bundle.getString("encoding_opt.lbl.embedded") //NOI18N
                        + delimArr[i].getBytes().getEmbedded().getOffset()
                        + "," //NOI18N
                        + delimArr[i].getBytes().getEmbedded().getLength()
                        + "}"); //NOI18N
            }
        }
        if (delimAsString.length() == 0) {
            delimAsString = null;
        }
        return delimAsString;
    }

    /**
     * Compute the end delimiter value at current node.
     * @return the computed end delimiter value.
     */
    private String computeEndDelimiters()
        throws InvalidAppInfoException {
        // Only delimited or array node needs to compute delimiter
        if (!NodeProperties.NodeType.DELIMITED.equals(xgetNodeType())
                && !NodeProperties.NodeType.ARRAY.equals(xgetNodeType())) {
            return null;
        }
        String delimAsString = null;
        DelimiterLevel delimLevel;
        // Check to see if there are local delimiters defined. If so, they
        // takes precedence,
        if (mDelimiterSet != null) {
            // So we get first delimiter level out of it.
            int lvl = 0;
            delimLevel = mDelimiterSet.getLevelArray(lvl);
            // We get all end delimiters at the given delimiter level as
            // comma separated string
            delimAsString = getEndDelimitersAsString(delimLevel);
            if (delimAsString != null) {
                return delimAsString;
            }
        }
        SchemaComponent comp;
        Annotation anno;
        CustomEncoding customEncoding;
        boolean isFineInherit = testIsGlobalFineInherit();

        // Starting from (mComponentPath.length - 3) and doing bottom-up
        // traverse up the ladder, so the current element declaration can be
        // skipped (i.e. this will skip last 2 SchemaComponent objects in the
        // ladder array, namely, its parent "annotation" node and its
        // grandparent localElement node.) It traverses up the ladder, until it
        // finds an upper hierachy element or elementReference who has
        // annotation that has custom encoding that has node properties with
        // delimiters defined.
        int lvl = 0;
        int i = mComponentPath.length - 3;
        for (; i >= 0; i--) {
            comp = mComponentPath[i];
            if (!(comp instanceof Element)
                    || comp instanceof ElementReference) {
                // skip non-element or non-elementReference, and continue to
                // search up in the XSD structure ladder
                continue;
            }
            anno = ((Element) comp).getAnnotation();
            if (anno == null) {
                // continue to search up, and record delimiter level number
                lvl++;
                continue;
            }
            try {
                customEncoding = fetchCustomEncoding(anno, null);
            } catch (InvalidAppInfoException ex) {
                return _bundle.getString("encoding_opt.lbl.error_retrieving_delim"); //NOI18N
            }
            if (customEncoding == null || !customEncoding.isSetNodeProperties()) {
                // continue to search up, and record delimiter level number
                lvl++;
                continue;
            }
            NodeProperties nProp = customEncoding.getNodeProperties();
            if (NodeProperties.NodeType.DELIMITED.equals(nProp.getNodeType())
                || NodeProperties.NodeType.ARRAY.equals(nProp.getNodeType())) {
                // record delimiter level number
                lvl++;
            }
            if (!nProp.isSetDelimiterSet()) {
                // continue to search up
                continue;
            }
            if (nProp.getDelimiterSet().sizeOfLevelArray() <= lvl) {
                // no delim Level corresponding to lvl is defined
                if (isFineInherit) {
                    // continue to search up
                    continue;
                } else {
                    // no delimiters defined, break loop
                    break;
                }
            }
            delimLevel = nProp.getDelimiterSet().getLevelArray(lvl);
            delimAsString = getEndDelimitersAsString(delimLevel);
            if (delimAsString.length() == 0) {
                delimAsString = null;
            }
            if (delimAsString == null && isFineInherit) {
                // continue to search up
                continue;
            }
            break;
        } // end-- for (; i >= 0; i--)
        return delimAsString;
    }

    private String computeBeginDelimiters()
        throws InvalidAppInfoException {
        // Only delimited or array node needs to compute delimiter
        if (!NodeProperties.NodeType.DELIMITED.equals(xgetNodeType())
                && !NodeProperties.NodeType.ARRAY.equals(xgetNodeType())) {
            return null;
        }
        String delimAsString = null;
        DelimiterLevel delimLevel;
        // Check to see if there are local delimiters defined. If so, they
        // takes precedence,
        if (mDelimiterSet != null) {
            // So we get first delimiter level out of it.
            int lvl = 0;
            delimLevel = mDelimiterSet.getLevelArray(lvl);
            // We get all end delimiters at the given delimiter level as
            // comma separated string
            delimAsString = getBeginDelimitersAsString(delimLevel);
            if (delimAsString != null) {
                return delimAsString;
            }
        }
        SchemaComponent comp;
        Annotation anno;
        CustomEncoding customEncoding;
        boolean isFineInherit = testIsGlobalFineInherit();

        // Starting from (mComponentPath.length - 3) and doing bottom-up
        // traverse up the ladder, so the current element declaration can be
        // skipped (i.e. this will skip last 2 SchemaComponent objects in the
        // ladder array, namely, its parent "annotation" node and its
        // grandparent localElement node.) It traverses up the ladder, until it
        // finds an upper hierachy element or elementReference who has
        // annotation that has custom encoding that has node properties with
        // delimiters defined.
        int lvl = 0;
        int i = mComponentPath.length - 3;
        for (; i >= 0; i--) {
            comp = mComponentPath[i];
            if (!(comp instanceof Element)
                    || comp instanceof ElementReference) {
                // skip non-element or non-elementReference, and continue to
                // search up in the XSD structure ladder
                continue;
            }
            anno = ((Element) comp).getAnnotation();
            if (anno == null) {
                // continue to search up, and record delimiter level number
                lvl++;
                continue;
            }
            try {
                customEncoding = fetchCustomEncoding(anno, null);
            } catch (InvalidAppInfoException ex) {
                return _bundle.getString("encoding_opt.lbl.error_retrieving_delim"); //NOI18N
            }
            if (customEncoding == null || !customEncoding.isSetNodeProperties()) {
                // continue to search up, and record delimiter level number
                lvl++;
                continue;
            }
            NodeProperties nProp = customEncoding.getNodeProperties();
            if (NodeProperties.NodeType.DELIMITED.equals(nProp.getNodeType())
                || NodeProperties.NodeType.ARRAY.equals(nProp.getNodeType())) {
                // record delimiter level number
                lvl++;
            }
            if (!nProp.isSetDelimiterSet()) {
                // continue to search up
                continue;
            }
            if (nProp.getDelimiterSet().sizeOfLevelArray() <= lvl) {
                // no delim Level corresponding to lvl is defined
                if (isFineInherit) {
                    // continue to search up
                    continue;
                } else {
                    // no delimiters defined, break loop
                    break;
                }
            }
            delimLevel = nProp.getDelimiterSet().getLevelArray(lvl);
            delimAsString = getBeginDelimitersAsString(delimLevel);
            if (delimAsString.length() == 0) {
                delimAsString = null;
            }
            if (delimAsString == null && isFineInherit) {
                // continue to search up
                continue;
            }
            break;
        } // end-- for (; i >= 0; i--)
        return delimAsString;
    }

    private String computeArrayDelimiters()
        throws InvalidAppInfoException {
        // Only delimited or array node needs to compute delimiter
        if (!NodeProperties.NodeType.DELIMITED.equals(xgetNodeType())
                && !NodeProperties.NodeType.ARRAY.equals(xgetNodeType())) {
            return null;
        }
        String delimAsString = null;
        DelimiterLevel delimLevel;
        // Check to see if there are local delimiters defined. If so, they
        // takes precedence,
        if (mDelimiterSet != null) {
            // So we get first delimiter level out of it.
            int lvl = 0;
            delimLevel = mDelimiterSet.getLevelArray(lvl);
            // We get all end delimiters at the given delimiter level as
            // comma separated string
            delimAsString = getArrayDelimitersAsString(delimLevel);
            if (delimAsString != null) {
                return delimAsString;
            }
        }
        SchemaComponent comp;
        Annotation anno;
        CustomEncoding customEncoding;
        boolean isFineInherit = testIsGlobalFineInherit();

        // Starting from (mComponentPath.length - 3) and doing bottom-up
        // traverse up the ladder, so the current element declaration can be
        // skipped (i.e. this will skip last 2 SchemaComponent objects in the
        // ladder array, namely, its parent "annotation" node and its
        // grandparent localElement node.) It traverses up the ladder, until it
        // finds an upper hierachy element or elementReference who has
        // annotation that has custom encoding that has node properties with
        // delimiters defined.
        int lvl = 0;
        int i = mComponentPath.length - 3;
        for (; i >= 0; i--) {
            comp = mComponentPath[i];
            if (!(comp instanceof Element)
                    || comp instanceof ElementReference) {
                // skip non-element or non-elementReference, and continue to
                // search up in the XSD structure ladder
                continue;
            }
            anno = ((Element) comp).getAnnotation();
            if (anno == null) {
                // continue to search up, and record delimiter level number
                lvl++;
                continue;
            }
            try {
                customEncoding = fetchCustomEncoding(anno, null);
            } catch (InvalidAppInfoException ex) {
                return _bundle.getString("encoding_opt.lbl.error_retrieving_delim"); //NOI18N
            }
            if (customEncoding == null || !customEncoding.isSetNodeProperties()) {
                // continue to search up, and record delimiter level number
                lvl++;
                continue;
            }
            NodeProperties nProp = customEncoding.getNodeProperties();
            if (NodeProperties.NodeType.DELIMITED.equals(nProp.getNodeType())
                || NodeProperties.NodeType.ARRAY.equals(nProp.getNodeType())) {
                // record delimiter level number
                lvl++;
            }
            if (!nProp.isSetDelimiterSet()) {
                // continue to search up
                continue;
            }
            if (nProp.getDelimiterSet().sizeOfLevelArray() <= lvl) {
                // no delim Level corresponding to lvl is defined
                if (isFineInherit) {
                    // continue to search up
                    continue;
                } else {
                    // no delimiters defined, break loop
                    break;
                }
            }
            delimLevel = nProp.getDelimiterSet().getLevelArray(lvl);
            delimAsString = getArrayDelimitersAsString(delimLevel);
            if (delimAsString.length() == 0) {
                delimAsString = null;
            }
            if (delimAsString == null && isFineInherit) {
                // continue to search up
                continue;
            }
            break;
        } // end-- for (; i >= 0; i--)
        return delimAsString;
    }

    private Annotation annotation() {
        // annotation is stored as last element
        return (Annotation) mComponentPath[mComponentPath.length - 1];
    }

    private String elementName() {
        Element elem = (Element) annotation().getParent();
        if (elem instanceof GlobalElement) {
            return ((GlobalElement) elem).getName();
        } else if (elem instanceof LocalElement) {
            return ((LocalElement) elem).getName();
        } else if (elem instanceof ElementReference) {
            GlobalElement ref = ((ElementReference) elem).getRef().get();
            return ref.getName();
        }
        return null;
    }

    /**
     * Initialize this EncodingOption object.
     *
     * @param hookUpListener whether or not to hookup listener.
     * @return true if initialization was successful, false if no custom
     * encoding info was found.
     * @throws org.netbeans.modules.encoder.ui.basic.InvalidAppInfoException
     */
    private boolean init(boolean hookUpListener)
        throws InvalidAppInfoException {
        SchemaComponent comp = mComponentPath[mComponentPath.length - 1];
        if (!(comp instanceof Annotation)) {
            throw new IllegalArgumentException(
                _bundle.getString("encoding_opt.exp.must_be_annotation")); //NOI18N
        }
        CustomEncoding customEnc = null;
        AppInfo[] appinfoReturned = new AppInfo[1];
        customEnc = fetchCustomEncoding((Annotation) comp, appinfoReturned);
        if (customEnc == null || !customEnc.isSetNodeProperties()) {
            if (appinfoReturned[0] == null) {
                return false;
            }
            mAppInfo = appinfoReturned[0];
            boolean top = false;
            if (customEnc != null && customEnc.isSetTop()
                && customEnc.getTop()) {
                top = true;
            }
            customEnc = (CustomEncoding) mDefaultCustomEncoding.copy();
            if (top) {
                customEnc.setTop(true);
            }
        } else {
            mAppInfo = appinfoReturned[0];
        }
        NodeProperties nProp = customEnc.getNodeProperties();
        mNodeType = mTextMap.get(NODE_TYPE_PREFIX + UDS
                + nProp.getNodeType().toString());
        if (nProp.isSetAlignment()) {
            mAlignment = mTextMap.get(ALIGNMENT_PREFIX + UDS
                    + nProp.getAlignment().toString());
        }
        if (nProp.isSetOrder()) {
            mOrder = mTextMap.get(ORDER_PREFIX + UDS
                    + nProp.getOrder().toString());
        }
        if (nProp.isSetNOfN()) {
            NOfN nOfN = nProp.getNOfN();
            if (nOfN.isSetMinN()) {
                mNOfNminN = Integer.toString(nOfN.getMinN());
            }
            if (nOfN.isSetMaxN()) {
                mNOfNmaxN = Integer.toString(nOfN.getMaxN());
            }
        }
        if (nProp.isSetMinOcc()) {
            mMinOcc = Long.toString(nProp.getMinOcc());
        }
        if (nProp.isSetMaxOcc()) {
            mMaxOcc = Long.toString(nProp.getMaxOcc());
        }
        if (nProp.isSetScvngr()) {
            Scvngr scvngr = nProp.getScvngr();
            mScvngrChars = scvngr.getChars();
            if (scvngr.isSetEmit1St()) {
                mScvngrEmit1st = scvngr.getEmit1St();
            }
        }

        if (customEnc.isSetTop()) {
            mTop = customEnc.getTop();
        }
        if (mTop) {
            if (nProp.isSetInputCharset()) {
                mInputCharset = nProp.getInputCharset();
            }
            if (nProp.isSetOutputCharset()) {
                mOutputCharset = nProp.getOutputCharset();
            }
        }
        if (mTop || nProp.getNodeType().intValue()
                == NodeProperties.NodeType.INT_FIXED_LENGTH) {
            if (nProp.isSetParsingCharset()) {
                mParsingCharset = nProp.getParsingCharset();
            }
            if (nProp.isSetSerializingCharset()) {
                mSerializingCharset = nProp.getSerializingCharset();
            }
        }
        if (nProp.isSetMatch()) {
            mMatch = nProp.getMatch();
        }
        //Populates the NoMatch field
        if (nProp.isSetNoMatch()) {
            mNoMatch = nProp.getNoMatch();
        }
        if (nProp.isSetLength()) {
            mLength = nProp.getLength();
        }
        if (nProp.isSetOffset()) {
            mOffset = Long.toString(nProp.getOffset());
        }
        if (nProp.isSetPosition()) {
            mPosition = Long.toString(nProp.getPosition());
        }
        if (nProp.isSetLength()) {
            mLength = nProp.getLength();
        }
        if (nProp.isSetLength() && mLength == 0) {
            mFixedLengthType = mTextMap.get(FIXED_LENGTH_TYPE_PREFIX + UDS
                + "undetermined"); //NOI18N
        } else if (nProp.isSetPosition()) { // && mPosition >= 0
            mFixedLengthType = mTextMap.get(FIXED_LENGTH_TYPE_PREFIX + UDS
                + "encoded"); //NOI18N
        } else if (nProp.isSetLength() && mLength < 0) {
            mFixedLengthType = mTextMap.get(FIXED_LENGTH_TYPE_PREFIX + UDS
                + "reversed"); //NOI18N
            mLength = mLength * (-1);
        }

        if (nProp.isSetDelimOfFixed()) {
            DelimOfFixed delimOfFixed = nProp.getDelimOfFixed();
            mBeginDelimiter = delimOfFixed.getBeginBytes();
            if (delimOfFixed.isSetBeginAnch()) {
                mBeginDelimiterDetached = !delimOfFixed.getBeginAnch();
            }
        }
        if (nProp.isSetDelimiterSet()) {
            mDelimiterSet = nProp.getDelimiterSet();
        }
        mCustomEncoding = customEnc;
        //Populates the Escape Sequence field
        if (nProp.isSetEscapeSequence()) {
            mEscapeSequence = nProp.getEscapeSequence();
        }
        //Populates the FineInherit field
        if (nProp.isSetFineInherit()) {
            mFineInherit = nProp.getFineInherit();
        }
        //Populates the UndefDataPolicy field
        if (nProp.isSetUndefDataPolicy()) {
            mUndefDataPolicy = mTextMap.get(UNDEFINED_DATA_POLICY_PREFIX + UDS
                    + nProp.getUndefDataPolicy().toString());
        }

        // I guess that following lines will cause recursive loop when
        // the AppInfo is removed from the text editing pane.
        //if (mAppInfo == null) {
        //    commitToAppInfo();
        //}

        if (!(((Annotation) comp).getParent() instanceof Element)) {
            throw new IllegalArgumentException(
                    _bundle.getString("encoding_opt.exp.anno_must_under_elem")); //NOI18N
        }
        if (hookUpListener) {
            Element elem = (Element) ((Annotation) comp).getParent();
            Object xmlType = SchemaUtility.getXMLType(elem);
            SchemaModel refModel = null;
            if ((xmlType instanceof SimpleType)
                    || (xmlType instanceof ComplexType)) {
                refModel = ((SchemaComponent) xmlType).getModel();
            }
            mSchemaPropChangeListener =
                    new SchemaPropertyChangeListener(elem, xmlType);
            elem.getModel().addPropertyChangeListener(
                    WeakListeners.propertyChange(
                        mSchemaPropChangeListener, elem.getModel()));
            if (refModel != null && elem.getModel() != refModel) {
                refModel.addPropertyChangeListener(
                    WeakListeners.propertyChange(
                        mSchemaPropChangeListener, refModel));
            }
        }
        return true;
    }

    /**
     * Gets the Custom Encoding information from the given schema Annotation.
     * If the appinfoReturned is not null, then it will be populated with
     * schema AppInfo objects.
     *
     * @param anno the schema Annotation object.
     * @param appinfosReturned if not null, will be populated with schema
     * AppInfo objects.
     * @return CustomEncoding info.
     * @throws org.netbeans.modules.encoder.ui.basic.InvalidAppInfoException
     */
    private CustomEncoding fetchCustomEncoding(Annotation anno,
            AppInfo[] appinfosReturned)
            throws InvalidAppInfoException {
        CustomEncoding customEncoding = null;
        Collection<AppInfo> appinfos = anno.getAppInfos();
        if (appinfos != null) {
            for (AppInfo appinfo : appinfos) {
                // ensure the appinfo's uri is expected as "urn:com.sun:encoder"
                if (!EncodingConst.URI.equals(appinfo.getURI())) {
                    continue;
                }
                if (appinfosReturned != null) {
                    appinfosReturned[0] = appinfo;
                }
                try {
                    XmlOptions xmlOptions = new XmlOptions();
                    // set this option so that the document element is replaced
                    // with the given QName (null) when parsing.
                    xmlOptions.setLoadReplaceDocumentElement(null);
                    customEncoding = CustomEncoding.Factory.parse(
                            new StringReader(xmlFragFromAppInfo(appinfo)),
                            xmlOptions);
                    xmlOptions = new XmlOptions();
                    List errorList = new ArrayList();
                    // errorList will contain all the errors after the validate
                    // operation takes place.
                    xmlOptions.setErrorListener(errorList);
                    if (!customEncoding.validate(xmlOptions)) {
                        throw new XmlException(errorList.toString());
                    }
                } catch (XmlException ex) {
                    throw new InvalidAppInfoException(
                        NbBundle.getMessage(
                        EncodingOption.class,
                        "encoding_opt.exp.invalid_appinfo", //NOI18N
                        SchemaUtility.getNCNamePath(mComponentPath),
                        ex.getMessage()),
                        ex);
                } catch (IOException ex) {
                    throw new InvalidAppInfoException(
                        NbBundle.getMessage(
                        EncodingOption.class,
                        "encoding_opt.exp.io_exception", //NOI18N
                        SchemaUtility.getNCNamePath(mComponentPath),
                        ex.getMessage()),
                        ex);
                }
                break;
            }
        }
        return customEncoding;
    }

    private synchronized void commitToAppInfo() {
        boolean startedTrans = false;
        SchemaModel model = null;
        if (mAppInfo == null) {
            Annotation anno = annotation();
            model = anno.getModel();
            if (!model.isIntransaction()) {
                if (!model.startTransaction()) {
                    // happens if failed to acquire transaction, for e.g.
                    // when model has transitioned into invalid state.
                    //TODO how to handle???
                    }
                startedTrans = true;
            }
            // create a new AppInfo object
            mAppInfo = anno.getModel().getFactory().createAppInfo();
            anno.addAppInfo(mAppInfo);
            mAppInfo.setURI(EncodingConst.URI);
        } else {
            model = mAppInfo.getModel();
            if (!model.isIntransaction()) {
                if (!model.startTransaction()) {
                    // happens if failed to acquire transaction, for e.g.
                    // when model has transitioned into invalid state.
                    //TODO how to handle???
                    }
                startedTrans = true;
            }
        }
        try {
            String contentFrag = contentFragFromXmlObject(mCustomEncoding);
            mAppInfo.setContentFragment(contentFrag);
        } catch (IOException ex) {
            //TODO how to handle???
            } finally {
            if (startedTrans) {
                model.endTransaction();
            }
        }
    }

    private void firePropertyChange(String name, Object oldObj, Object newObj) {
        PropertyChangeListener[] pcls = (PropertyChangeListener[])
                propChangeListeners.toArray(new PropertyChangeListener[0]);
        for (int i = 0; i < pcls.length; i++) {
            pcls[i].propertyChange(
                    new PropertyChangeEvent (this, name, oldObj, newObj));
        }
    }

    private String xmlFragFromAppInfo(AppInfo appInfo) {
        StringBuffer sb = new StringBuffer("<xml-fragment "); //NOI18N
        sb.append("source=\"").append(EncodingConst.URI).append("\""); //NOI18N
        if (appInfo.getPeer() != null) {
            String prefix = appInfo.getPeer().lookupPrefix(EncodingConst.URI);
            if (prefix != null) {
                sb.append(" xmlns"); //NOI18N
                if (prefix.length() > 0) {
                    sb.append(":").append(prefix); //NOI18N
                }
                sb.append("=\"").append(EncodingConst.URI).append("\""); //NOI18N
            }
            prefix = appInfo.getPeer().lookupPrefix(CustomEncodingConst.URI);
            if (prefix != null) {
                sb.append(" xmlns"); //NOI18N
                if (prefix.length() > 0) {
                    sb.append(":").append(prefix); //NOI18N
                }
                sb.append("=\"").append(CustomEncodingConst.URI).append("\""); //NOI18N
            }
        }
        sb.append(">"); //NOI18N
        sb.append(appInfo.getContentFragment());
        sb.append("</xml-fragment>"); //NOI18N
        return sb.toString();
    }

    private String contentFragFromXmlObject(XmlObject xmlObject) {
        XmlCursor cursor = null;
        try {
            cursor = xmlObject.newCursor();
            StringBuffer buff = new StringBuffer();
            if (!cursor.toFirstChild()) {
                return EMP;
            }
            buff.append(cursor.xmlText());
            while (cursor.toNextSibling()) {
                buff.append(cursor.xmlText());
            }
            return buff.toString();
        } finally {
            if (cursor != null) {
                cursor.dispose();
            }
        }
    }

    private class SchemaPropertyChangeListener
        implements PropertyChangeListener {

        private final Element mElem;
        private final Set<SchemaModel> mModelSet = new HashSet<SchemaModel>();
        private Object mXMLType;

        SchemaPropertyChangeListener(Element elem, Object xmlType) {
            mElem = elem;
            mXMLType = xmlType;
            if (xmlType instanceof SchemaComponent) {
                SchemaModel refModel = ((SchemaComponent) xmlType).getModel();
                if (elem.getModel() != refModel) {
                    mModelSet.add(refModel);
                }
            }
        }

        public void propertyChange(PropertyChangeEvent evt) {
            if (mElem == evt.getSource()
                && "type".equals(evt.getPropertyName())) {   //NOI18N
                mXMLType = evt.getNewValue();
                if (mXMLType instanceof SchemaComponent) {
                    SchemaModel refModel = ((SchemaComponent) mXMLType).getModel();
                    if (mElem.getModel() != refModel
                            && !mModelSet.contains(refModel)) {
                        refModel.addPropertyChangeListener(
                            WeakListeners.propertyChange(
                                mSchemaPropChangeListener, refModel));
                        mModelSet.add(refModel);
                    }
                }
                firePropertyChange("xmlType", evt.getOldValue(), evt.getNewValue());   //NOI18N
                return;
            }
            if (mXMLType != null && mXMLType == evt.getSource()
                    && "definition".equals(evt.getPropertyName())) {   //NOI18N
                firePropertyChange("typeDef", evt.getOldValue(), evt.getNewValue());   //NOI18N
                return;
            }
        }
    }
}
