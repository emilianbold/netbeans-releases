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

import com.sun.encoder.custom.appinfo.Delimiter;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * The model of the delimiter node.
 *
 * @author Jun Xu
 */
public final class DelimiterOption {

    public static final String KIND_PREFIX = "kind"; //NOI18N
    public static final String OPTION_MODE_PREFIX = "optionmode"; //NOI18N
    public static final String TERM_MODE_PREFIX = "termmode"; //NOI18N

    private static final Map<String, String> mReverseTextMap =
            new HashMap<String, String>();
    private static final Map<String, String> mTextMap =
            new HashMap<String, String>();
    private static List<String> mKindTagList = new ArrayList<String>();
    private static List<String> mOptionModeTagList = new ArrayList<String>();
    private static List<String> mTermModeTagList = new ArrayList<String>();

    static {
        ResourceBundle bundle =
                ResourceBundle.getBundle(
                    DelimiterOption.class.getPackage().getName() + ".Bundle"); //NOI18N

        //Populate the localized text map and the tag list for the delimiter kind property
        mReverseTextMap.put(KIND_PREFIX + "_" + bundle.getString("TAG_DelimKind_normal"), "normal"); //NOI18N
        mReverseTextMap.put(KIND_PREFIX + "_" + bundle.getString("TAG_DelimKind_repeat"), "repeat"); //NOI18N
        mReverseTextMap.put(KIND_PREFIX + "_" + bundle.getString("TAG_DelimKind_escape"), "escape"); //NOI18N
        mReverseTextMap.put(KIND_PREFIX + "_" + bundle.getString("TAG_DelimKind_quot_escape"), "quot-escape"); //NOI18N
        mTextMap.put(KIND_PREFIX + "_" + "normal", bundle.getString("TAG_DelimKind_normal")); //NOI18N
        mTextMap.put(KIND_PREFIX + "_" + "repeat", bundle.getString("TAG_DelimKind_repeat")); //NOI18N
        mTextMap.put(KIND_PREFIX + "_" + "escape", bundle.getString("TAG_DelimKind_escape")); //NOI18N
        mTextMap.put(KIND_PREFIX + "_" + "quot-escape", bundle.getString("TAG_DelimKind_quot_escape")); //NOI18N
        mKindTagList.add(bundle.getString("TAG_DelimKind_normal")); //NOI18N
        mKindTagList.add(bundle.getString("TAG_DelimKind_repeat")); //NOI18N
        mKindTagList.add(bundle.getString("TAG_DelimKind_escape")); //NOI18N
        mKindTagList.add(bundle.getString("TAG_DelimKind_quot_escape")); //NOI18N
        mKindTagList = Collections.unmodifiableList(mKindTagList);

        //Populate the localized text map and the tag list for the optionality mode property
        mReverseTextMap.put(OPTION_MODE_PREFIX + "_" + bundle.getString("TAG_OptionMode_never"), "never"); //NOI18N
        mReverseTextMap.put(OPTION_MODE_PREFIX + "_" + bundle.getString("TAG_OptionMode_allow"), "allow"); //NOI18N
        mReverseTextMap.put(OPTION_MODE_PREFIX + "_" + bundle.getString("TAG_OptionMode_favor"), "favor"); //NOI18N
        mReverseTextMap.put(OPTION_MODE_PREFIX + "_" + bundle.getString("TAG_OptionMode_force"), "force"); //NOI18N
        mTextMap.put(OPTION_MODE_PREFIX + "_" + "never", bundle.getString("TAG_OptionMode_never")); //NOI18N
        mTextMap.put(OPTION_MODE_PREFIX + "_" + "allow", bundle.getString("TAG_OptionMode_allow")); //NOI18N
        mTextMap.put(OPTION_MODE_PREFIX + "_" + "favor", bundle.getString("TAG_OptionMode_favor")); //NOI18N
        mTextMap.put(OPTION_MODE_PREFIX + "_" + "force", bundle.getString("TAG_OptionMode_force")); //NOI18N
        mOptionModeTagList.add(bundle.getString("TAG_OptionMode_never"));
        mOptionModeTagList.add(bundle.getString("TAG_OptionMode_allow"));
        mOptionModeTagList.add(bundle.getString("TAG_OptionMode_favor"));
        mOptionModeTagList.add(bundle.getString("TAG_OptionMode_force"));
        mOptionModeTagList = Collections.unmodifiableList(mOptionModeTagList);

        //Populate the localized text map and the tag list for the optionality mode property
        mReverseTextMap.put(TERM_MODE_PREFIX + "_" + bundle.getString("TAG_TermMode_never"), "never"); //NOI18N
        mReverseTextMap.put(TERM_MODE_PREFIX + "_" + bundle.getString("TAG_TermMode_allow"), "allow"); //NOI18N
        mReverseTextMap.put(TERM_MODE_PREFIX + "_" + bundle.getString("TAG_TermMode_favor"), "favor"); //NOI18N
        mReverseTextMap.put(TERM_MODE_PREFIX + "_" + bundle.getString("TAG_TermMode_force"), "force"); //NOI18N
        mTextMap.put(TERM_MODE_PREFIX + "_" + "never", bundle.getString("TAG_TermMode_never")); //NOI18N
        mTextMap.put(TERM_MODE_PREFIX + "_" + "allow", bundle.getString("TAG_TermMode_allow")); //NOI18N
        mTextMap.put(TERM_MODE_PREFIX + "_" + "favor", bundle.getString("TAG_TermMode_favor")); //NOI18N
        mTextMap.put(TERM_MODE_PREFIX + "_" + "force", bundle.getString("TAG_TermMode_force")); //NOI18N
        mTermModeTagList.add(bundle.getString("TAG_TermMode_never"));
        mTermModeTagList.add(bundle.getString("TAG_TermMode_allow"));
        mTermModeTagList.add(bundle.getString("TAG_TermMode_favor"));
        mTermModeTagList.add(bundle.getString("TAG_TermMode_force"));
        mTermModeTagList = Collections.unmodifiableList(mTermModeTagList);
    }

    /* Bean property change listeners */
    private final List<PropertyChangeListener> propChangeListeners =
            Collections.synchronizedList(new LinkedList<PropertyChangeListener>());

    private final Delimiter mDelimiter;

    /***************************\
     * Bean property variables *
    \***************************/

    private String mKind = mTextMap.get(KIND_PREFIX + "_" + "normal"); //NOI18N
    private short mPrecedence = 10;
    private String mOptionMode = mTextMap.get(OPTION_MODE_PREFIX + "_" + "never"); //NOI18N
    private String mTermMode = mTextMap.get(TERM_MODE_PREFIX + "_" + "never"); //NOI18N
    private String mBytes = ""; //NOI18N
    private int mOffset = 0;
    private short mLength = 0;
    private boolean mDetached = false;
    private String mBeginBytes = ""; //NOI18N
    private int mBeginOffset = 0;
    private short mBeginLength = 0;
    private boolean mBeginDetached = false;
    private boolean mSkipLeading = false;
    private boolean mCollapse = false;

    /** Creates a new instance of DelimiterOption. */
    private DelimiterOption(Delimiter delim) {
        mDelimiter = delim;
    }

    public static DelimiterOption create(Delimiter delim) {
        DelimiterOption option = new DelimiterOption(delim);
        option.init();
        return option;
    }

    public static Map<String, String> textMap() {
        return mTextMap;
    }

    public static Map<String, String> reverseTextMap() {
        return mReverseTextMap;
    }

    public static List<String> kindTagList() {
        return mKindTagList;
    }

    public static List<String> optionModeTagList() {
        return mOptionModeTagList;
    }

    public static List<String> termModeTagList() {
        return mTermModeTagList;
    }

    /*****************************\
     * bean getters and setters  *
    \*****************************/

    public String getKind() {
        return mKind;
    }

    public void setKind(String kind) {
        String old = mKind;
        mKind = kind;
        mDelimiter.setKind(
                Delimiter.Kind.Enum.forString(
                    mReverseTextMap.get(KIND_PREFIX + "_" + kind))); //NOI18N
        firePropertyChange("kind", old, mKind); //NOI18N
    }

    public String getBytes() {
        return mBytes;
    }

    public void setBytes(String bytes) {
        String old = mBytes;
        mBytes = bytes;
        if (mDelimiter.getBytes() == null) {
            // no "bytes" element in XSD, so
            // we append a new empty "bytes" element
            mDelimiter.addNewBytes();
        }
        if (mDelimiter.getBytes().isSetEmbedded()) {
            // if "bytes" contains "embedded" choice element,
            // then unset it.
            mDelimiter.getBytes().unsetEmbedded();
        }
        // set the "constant" choice element under "bytes"
        mDelimiter.getBytes().setConstant(mBytes);
        int oldOffset = mOffset;
        mOffset = 0;
        short oldLength = mLength;
        mLength = 0;
        firePropertyChange("offset", oldOffset, mOffset); //NOI18N
        firePropertyChange("length", oldLength, mLength); //NOI18N
        firePropertyChange("bytes", old, mBytes); //NOI18N
    }

    public String getBeginBytes() {
        return mBeginBytes;
    }

    public void setBeginBytes(String beginBytes) {
        String oldBeginBytes = mBeginBytes;
        mBeginBytes = beginBytes;
        if (mDelimiter.getBeginBytes() == null) {
            // no "beginBytes" element in XSD, so
            // we append a new empty "beginBytes" element
            mDelimiter.addNewBeginBytes();
        }
        if (mDelimiter.getBeginBytes().isSetEmbedded()) {
            // if "beginBytes" contains "embedded" choice element,
            // then unset it.
            mDelimiter.getBeginBytes().unsetEmbedded();
        }
        // set the "constant" choice element under "beginBytes"
        mDelimiter.getBeginBytes().setConstant(mBeginBytes);
        // reset both "offset" and "length" to 0
        int oldBeginOffset = mBeginOffset;
        mBeginOffset = 0;
        short oldBeginLength = mBeginLength;
        mBeginLength = 0;
        firePropertyChange("beginOffset", oldBeginOffset, mBeginOffset); //NOI18N
        firePropertyChange("beginLength", oldBeginLength, mBeginLength); //NOI18N
        firePropertyChange("beginBytes", oldBeginBytes, mBeginBytes); //NOI18N
    }

    public short getPrecedence() {
        return mPrecedence;
    }

    public void setPrecedence(short precedence) {
        int old = mPrecedence;
        mPrecedence = precedence;
        mDelimiter.setPrecedence(mPrecedence);
        firePropertyChange("precedence", old, mPrecedence); //NOI18N
    }

    public boolean getSkipLeading() {
        return mSkipLeading;
    }

    public void setSkipLeading(boolean skipLeading) {
        boolean old = mSkipLeading;
        mSkipLeading = skipLeading;
        mDelimiter.setSkipLeading(mSkipLeading);
        firePropertyChange("skipLeading", old, mSkipLeading); //NOI18N
    }

    public boolean getCollapse() {
        return mCollapse;
    }

    public void setCollapse(boolean collpase) {
        boolean old = mCollapse;
        mCollapse = collpase;
        mDelimiter.setCollapse(mCollapse);
        firePropertyChange("collapse", old, mCollapse); //NOI18N
    }

    public boolean getDetached() {
        return mDetached;
    }

    public void setDetached(boolean detached) {
        boolean old = mDetached;
        mDetached = detached;
        if (mDetached) {
            mDelimiter.setEndAnch(false);
        } else {
            mDelimiter.unsetEndAnch();
        }
        firePropertyChange("detached", old, mDetached); //NOI18N
    }

    public String getOptionMode() {
        return mOptionMode;
    }

    public void setOptionMode(String optionMode) {
        String old = mOptionMode;
        mOptionMode = optionMode;
        mDelimiter.setOptionalMode(
                Delimiter.OptionalMode.Enum.forString(
                    mReverseTextMap.get(OPTION_MODE_PREFIX + "_" + optionMode))); //NOI18N
        firePropertyChange("optionMode", old, mOptionMode); //NOI18N
    }

    public String getTermMode() {
        return mTermMode;
    }

    public void setTermMode(String termMode) {
        String old = mTermMode;
        mTermMode = termMode;
        mDelimiter.setTerminatorMode(
                Delimiter.TerminatorMode.Enum.forString(
                    mReverseTextMap.get(TERM_MODE_PREFIX + "_" + termMode))); //NOI18N
        firePropertyChange("termMode", old, mTermMode); //NOI18N
    }

    public int getOffset() {
        return mOffset;
    }

    public void setOffset(int offset) {
        int old = mOffset;
        mOffset = offset;
        if (mDelimiter.getBytes() == null) {
            mDelimiter.addNewBytes();
        }
        if (mDelimiter.getBytes().isSetConstant()) {
            mDelimiter.getBytes().unsetConstant();
        }
        if (!mDelimiter.getBytes().isSetEmbedded()) {
            mDelimiter.getBytes().addNewEmbedded();
        }
        mDelimiter.getBytes().getEmbedded().setOffset(mOffset);
        String oldBytes = mBytes;
        mBytes = ""; //NOI18N
        firePropertyChange("bytes", oldBytes, mBytes); //NOI18N
        firePropertyChange("offset", old, mOffset); //NOI18N
    }

    public short getLength() {
        return mLength;
    }

    public void setLength(short length) {
        int old = mLength;
        mLength = length;
        if (mDelimiter.getBytes() == null) {
            mDelimiter.addNewBytes();
        }
        if (mDelimiter.getBytes().isSetConstant()) {
            mDelimiter.getBytes().unsetConstant();
        }
        if (!mDelimiter.getBytes().isSetEmbedded()) {
            mDelimiter.getBytes().addNewEmbedded();
        }
        mDelimiter.getBytes().getEmbedded().setLength(length);
        String oldBytes = mBytes;
        mBytes = ""; //NOI18N
        firePropertyChange("bytes", oldBytes, mBytes); //NOI18N
        firePropertyChange("length", old, mLength); //NOI18N
    }

    public int getBeginOffset() {
        return mBeginOffset;
    }

    public void setBeginOffset(int beginOffset) {
        int oldBeginOffset = mBeginOffset;
        mBeginOffset = beginOffset;
        if (mDelimiter.getBeginBytes() == null) {
            mDelimiter.addNewBeginBytes();
        }
        if (mDelimiter.getBeginBytes().isSetConstant()) {
            mDelimiter.getBeginBytes().unsetConstant();
        }
        if (!mDelimiter.getBeginBytes().isSetEmbedded()) {
            mDelimiter.getBeginBytes().addNewEmbedded();
        }
        mDelimiter.getBeginBytes().getEmbedded().setOffset(mBeginOffset);
        String oldBeginBytes = mBeginBytes;
        mBeginBytes = ""; //NOI18N
        firePropertyChange("beginBytes", oldBeginBytes, mBeginBytes); //NOI18N
        firePropertyChange("beginOffset", oldBeginOffset, mBeginOffset); //NOI18N
    }

    public short getBeginLength() {
        return mBeginLength;
    }

    public void setBeginLength(short beginLength) {
        int oldBeginLength = mBeginLength;
        mBeginLength = beginLength;
        if (mDelimiter.getBeginBytes() == null) {
            mDelimiter.addNewBeginBytes();
        }
        if (mDelimiter.getBeginBytes().isSetConstant()) {
            mDelimiter.getBeginBytes().unsetConstant();
        }
        if (!mDelimiter.getBeginBytes().isSetEmbedded()) {
            mDelimiter.getBeginBytes().addNewEmbedded();
        }
        mDelimiter.getBeginBytes().getEmbedded().setLength(beginLength);
        String oldBeginBytes = mBeginBytes;
        mBeginBytes = ""; //NOI18N
        firePropertyChange("beginBytes", oldBeginBytes, mBeginBytes); //NOI18N
        firePropertyChange("beginLength", oldBeginLength, mBeginLength); //NOI18N
    }

    public boolean getBeginDetached() {
        return mBeginDetached;
    }

    public void setBeginDetached(boolean beginDetached) {
        boolean old = mBeginDetached;
        mBeginDetached = beginDetached;
        if (mBeginDetached) {
            mDelimiter.setBeginAnch(false);
        } else {
            mDelimiter.unsetBeginAnch();
        }
        firePropertyChange("beginDetached", old, mBeginDetached); //NOI18N
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propChangeListeners.add(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propChangeListeners.remove(listener);
    }

    /** initalizes the options. */
    private void init() {
        if (mDelimiter.isSetKind()) {
            mKind = mTextMap.get(KIND_PREFIX + "_" //NOI18N
                    + mDelimiter.getKind().toString());
        }
        if (mDelimiter.isSetPrecedence()) {
            mPrecedence = mDelimiter.getPrecedence();
        }
        if (mDelimiter.isSetOptionalMode()) {
            mOptionMode = mTextMap.get(OPTION_MODE_PREFIX + "_" //NOI18N
                    + mDelimiter.getOptionalMode().toString());
        }
        if (mDelimiter.isSetTerminatorMode()) {
            mTermMode = mTextMap.get(TERM_MODE_PREFIX + "_" //NOI18N
                    + mDelimiter.getTerminatorMode().toString());
        }
        if (mDelimiter.getBytes() != null) {
            if (mDelimiter.getBytes().isSetConstant()) {
                mBytes = mDelimiter.getBytes().getConstant();
                mOffset = 0;
                mLength = 0;
            } else if (mDelimiter.getBytes().isSetEmbedded()) {
                mOffset = mDelimiter.getBytes().getEmbedded().getOffset();
                mLength = mDelimiter.getBytes().getEmbedded().getLength();
                mBytes = ""; //NOI18N
            }
        }
        if (mDelimiter.isSetEndAnch() && !mDelimiter.getEndAnch()) {
            mDetached = true;
        }
        if (mDelimiter.isSetSkipLeading() && mDelimiter.getSkipLeading()) {
            mSkipLeading = true;
        }
        if (mDelimiter.getBeginBytes() != null) {
            if (mDelimiter.getBeginBytes().isSetConstant()) {
                mBeginBytes = mDelimiter.getBeginBytes().getConstant();
                mBeginOffset = 0;
                mBeginLength = 0;
            } else if (mDelimiter.getBeginBytes().isSetEmbedded()) {
                mBeginOffset = mDelimiter.getBeginBytes().getEmbedded().getOffset();
                mBeginLength = mDelimiter.getBeginBytes().getEmbedded().getLength();
                mBeginBytes = ""; //NOI18N
            }
        }
        if (mDelimiter.isSetBeginAnch() && !mDelimiter.getBeginAnch()) {
            mBeginDetached = true;
        }
        if (mDelimiter.isSetCollapse() && mDelimiter.getCollapse()) {
            mCollapse = true;
        }
    }

    private void firePropertyChange(String name, Object oldObj, Object newObj) {
        PropertyChangeListener[] pcls = (PropertyChangeListener[])
                propChangeListeners.toArray(new PropertyChangeListener[0]);
        for (int i = 0; i < pcls.length; i++) {
            pcls[i].propertyChange(
                    new PropertyChangeEvent(this, name, oldObj, newObj));
        }
    }

}
