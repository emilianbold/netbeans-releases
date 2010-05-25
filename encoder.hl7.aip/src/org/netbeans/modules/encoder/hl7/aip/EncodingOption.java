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

package org.netbeans.modules.encoder.hl7.aip;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.CharConversionException;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import javax.xml.namespace.QName;
import org.netbeans.modules.encoder.ui.basic.EncodingConst;
import org.netbeans.modules.encoder.ui.basic.InvalidAppInfoException;
import org.netbeans.modules.encoder.ui.basic.SchemaUtility;
import org.netbeans.modules.xml.schema.model.Annotation;
import org.netbeans.modules.xml.schema.model.AppInfo;
import org.netbeans.modules.xml.schema.model.Element;
import org.netbeans.modules.xml.schema.model.ElementReference;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.openide.util.NbBundle;
import org.openide.xml.XMLUtil;
import org.w3c.dom.NodeList;

/**
 * The model of the HL7 encoding node.
 *
 * @author Jun Xu
 */
public class EncodingOption {
    
    private static final ResourceBundle _bundle =
            ResourceBundle.getBundle("org/netbeans/modules/encoder/hl7/aip/Bundle");
    private static final String[] mPropKeys = new String[] {"top", "pre", "post", "itm", "typ", "tbl", "lnm"}; //NOI18N
    private static final QName[] mPropNames = new QName[] {
        new QName(EncodingConst.URI, EncodingConst.TOP_FLAG, "enc"), //NOI18N
        new QName(EncodingConst.URI, EncodingConst.PRE_DECODE_CHAR_CODING, "enc"), //NOI18N
        new QName(EncodingConst.URI, EncodingConst.POST_ENCODE_CHAR_CODING, "enc"), //NOI18N
        new QName(HL7EncodingConst.URI, "Item", "hl7"), //NOI18N
        new QName(HL7EncodingConst.URI, "Type", "hl7"), //NOI18N
        new QName(HL7EncodingConst.URI, "Table", "hl7"), //NOI18N
        new QName(HL7EncodingConst.URI, "LongName", "hl7") //NOI18N
    };
    private static final String KEY_TOP = mPropKeys[0];
    private static final String KEY_PRE_DECODE_CHAR_CODING = mPropKeys[1];
    private static final String KEY_POST_ENCODE_CHAR_CODING = mPropKeys[2];
    private static final String KEY_ITEM = mPropKeys[3];
    private static final String KEY_TYPE = mPropKeys[4];
    private static final String KEY_TABLE = mPropKeys[5];
    private static final String KEY_LONG_NAME = mPropKeys[6];
    
    private static final Map<QName, String> mName2Key = new HashMap<QName, String>();
    static {
        for (int i = 0; i < mPropNames.length; i++) {
            mName2Key.put(mPropNames[i], mPropKeys[i]);
        }
    }
    
    private final Map<String, String> mPropMap = new HashMap<String, String>();
    
    /* Bean property change listeners */
    private final List<PropertyChangeListener> propChangeListeners =
            Collections.synchronizedList(new LinkedList<PropertyChangeListener>());

    /* Component path from which the encoding options are read */
    private final SchemaComponent[] mComponentPath;
    
    private AppInfo mAppInfo;
    
    private boolean mSetHL7Props;
    
    private static final String EMP = ""; //NOI18N
    private String mPreDecodeCharCoding = EMP;
    private String mPostEncodeCharCoding = EMP;

    /** Creates a new instance of EncodingOption */
    private EncodingOption(List<SchemaComponent> path) {
        if (path == null) {
            throw new NullPointerException(
                    _bundle.getString("encoding_opt.exp.no_component_path"));
        }
        if (path.size() < 1) {
            throw new IllegalArgumentException(
                    _bundle.getString("encoding_opt.exp.illegal_component_path"));
        }
        mComponentPath = path.toArray(new SchemaComponent[0]);
    }

    public static EncodingOption createFromAppInfo(List<SchemaComponent> path)
            throws InvalidAppInfoException {
        
        EncodingOption option = new EncodingOption(path);
        if (!option.init()) {
            return null;
        }
        return option;
    }
    
    public boolean isSetHL7Props() {
        return mSetHL7Props;
    }
    
    public void setSetHL7Props(boolean value) throws IOException {
        boolean old = mSetHL7Props;
        if (!value) {
            for (int i = 0; i < mPropNames.length; i++) {
                if ("hl7".equals(mPropNames[i].getPrefix())) { //NOI18N
                    mPropMap.remove(mName2Key.get(mPropNames[i]));
                }
            }
        }
        mSetHL7Props = value;
        commitToAppInfo();
        firePropertyChange("setHL7Props", old, mSetHL7Props); //NOI18N
    }
    
    public String getItem() {
        return mPropMap.get(KEY_ITEM) == null ? "" : mPropMap.get(KEY_ITEM); //NOI18N
    }

    public void setItem(String item) throws IOException {
        String old = mPropMap.get(KEY_ITEM);
        mPropMap.put(KEY_ITEM, item);
        commitToAppInfo();
        firePropertyChange("item", old, item); //NOI18N
    }

    public String getType() {
        return mPropMap.get(KEY_TYPE) == null ? "" : mPropMap.get(KEY_TYPE); //NOI18N
    }

    public void setType(String type) throws IOException {
        String old = mPropMap.get(KEY_TYPE);
        mPropMap.put(KEY_TYPE, type);
        commitToAppInfo();
        firePropertyChange("type", old, mPropMap.get(KEY_TYPE)); //NOI18N
    }

    public String getTable() {
        return mPropMap.get(KEY_TABLE) == null ? "" : mPropMap.get(KEY_TABLE); //NOI18N
    }

    public void setTable(String table) throws IOException {
        String old = mPropMap.get(KEY_TABLE);
        mPropMap.put(KEY_TABLE, table);
        commitToAppInfo();
        firePropertyChange("table", old, mPropMap.get(KEY_TABLE)); //NOI18N
    }

    public String getLongName() {
        return mPropMap.get(KEY_LONG_NAME) == null ? "" : mPropMap.get(KEY_LONG_NAME); //NOI18N
    }

    public void setLongName(String longName) throws IOException {
        String old = mPropMap.get(KEY_LONG_NAME);
        mPropMap.put(KEY_LONG_NAME, longName);
        commitToAppInfo();
        firePropertyChange("longName", old, mPropMap.get(KEY_LONG_NAME)); //NOI18N
    }

    public boolean isTop() {
        return Boolean.valueOf(mPropMap.get(KEY_TOP)).booleanValue();
    }

    public void setTop(boolean top) throws IOException {
        Boolean old = Boolean.valueOf(mPropMap.get(KEY_TOP));
        mPropMap.put(KEY_TOP, Boolean.valueOf(top).toString());
        commitToAppInfo();
        firePropertyChange("top", old, Boolean.valueOf(mPropMap.get(KEY_TOP))); //NOI18N
    }
    
    public String getPreDecodeCharCoding() {
        return mPropMap.get(KEY_PRE_DECODE_CHAR_CODING) == null ? "" : mPropMap.get(KEY_PRE_DECODE_CHAR_CODING); //NOI18N
    }

    public void setPreDecodeCharCoding(String preDecodeCharCoding)
            throws IOException {
        String old = mPropMap.get(KEY_PRE_DECODE_CHAR_CODING);
        mPropMap.put(KEY_PRE_DECODE_CHAR_CODING, preDecodeCharCoding);
        commitToAppInfo();
        firePropertyChange("preDecodeCharCoding", old, mPropMap.get(KEY_PRE_DECODE_CHAR_CODING)); //NOI18N
    }

    public String getPostEncodeCharCoding() {
        return mPropMap.get(KEY_POST_ENCODE_CHAR_CODING) == null ? "" : mPropMap.get(KEY_POST_ENCODE_CHAR_CODING); //NOI18N
    }

    public void setPostEncodeCharCoding(String postEncodeCharCoding)
            throws IOException {
        String old = mPropMap.get(KEY_POST_ENCODE_CHAR_CODING);
        mPropMap.put(KEY_POST_ENCODE_CHAR_CODING, postEncodeCharCoding);
        commitToAppInfo();
        firePropertyChange("postEncodeCharCoding", old, mPropMap.get(KEY_POST_ENCODE_CHAR_CODING)); //NOI18N
    }

    public boolean testIsGlobal() {
        int count = 0;
        for (int i = 0; i < mComponentPath.length; i++) {
            if (mComponentPath[i] instanceof ElementReference) {
                return false;
            }
        }
        return annotation().getParent() instanceof GlobalElement;
    }
    
    public boolean testIsUnderElement() {
        return annotation().getParent() instanceof Element;
    }
    
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propChangeListeners.add(listener);
    }
    
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propChangeListeners.remove(listener);
    }

    private Annotation annotation() {
        return (Annotation) mComponentPath[mComponentPath.length - 1];
    }
    
    private boolean init() throws InvalidAppInfoException {
        mAppInfo = null;
        mPropMap.clear();
        Collection<AppInfo> appinfos = annotation().getAppInfos();
        if (appinfos != null) {
            for (AppInfo appinfo : appinfos) {
                if (!EncodingConst.URI.equals(appinfo.getURI())) {
                    continue;
                }
                mAppInfo = appinfo;
                break;
            }
        }
        if (mAppInfo == null) {
            return false;
        }
        NodeList propNodes = mAppInfo.getPeer().getChildNodes();
        if (propNodes.getLength() == 0) {
            return true;
        }
        int i = 0;
        int j = 0;
        org.w3c.dom.Element elem;
        while (i < mPropNames.length) {
            innerLoop: while (j < propNodes.getLength()) {
                if (propNodes.item(j).getNodeType() != org.w3c.dom.Node.ELEMENT_NODE) {
                    j++;
                    continue;
                }
                elem = (org.w3c.dom.Element) propNodes.item(j);
                if (!mPropNames[i].getNamespaceURI().equals(elem.getNamespaceURI())
                        || !mPropNames[i].getLocalPart().equals(elem.getLocalName())) {
                    i++;
                    break innerLoop;
                }
                
                mPropMap.put(mName2Key.get(mPropNames[i]), getTextValue(elem));
                if ("hl7".equals(mPropNames[i].getPrefix())) { //NOI18N
                    mSetHL7Props = true;
                }
                j++;
                i++;
            }
            if (j >= propNodes.getLength()) {
                break;
            }
        }
        if (j < propNodes.getLength()) {
            //There must be something not matched
            String msg =
                    NbBundle.getMessage(
                            EncodingOption.class,
                            "encoding_opt.exp.invalid_appinfo", //NOI18N
                            SchemaUtility.getNCNamePath(mComponentPath),
                            propNodes.item(j).getNamespaceURI(),
                            propNodes.item(j).getLocalName());
            throw new InvalidAppInfoException(msg);
        }
        return true;
    }

    private String getTextValue(org.w3c.dom.Element elem) {
        NodeList childList = elem.getChildNodes();
        if (childList == null || childList.getLength() == 0) {
            return ""; //NOI18N
        }
        for (int i = 0; i < childList.getLength(); i++) {
            if (childList.item(i).getNodeType() == org.w3c.dom.Node.TEXT_NODE) {
                if (childList.item(i).getNodeValue() == null) {
                    return ""; //NOI18N
                }
                return childList.item(i).getNodeValue();
            }
        }
        return ""; //NOI18N
    }
    
    private synchronized void commitToAppInfo() throws IOException {
        if (mAppInfo == null) {
            return;
        }
        boolean startedTrans = false;
        try {
            if (!mAppInfo.getModel().isIntransaction()) {
                if (!mAppInfo.getModel().startTransaction()) {
                    //TODO how to handle???
                }
                startedTrans = true;
            }
            mAppInfo.setContentFragment(marshalContentFragment());
        } finally {
            if (startedTrans) {
                mAppInfo.getModel().endTransaction();
            }
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
    
    private String marshalContentFragment() throws CharConversionException {
        StringBuffer sb = new StringBuffer();
        boolean isTop = false;
        for (int i = 0; i < mPropKeys.length; i++) {
            if (mPropMap.containsKey(mPropKeys[i])) {
                String prefix = mPropNames[i].getPrefix();
                String localPart = mPropNames[i].getLocalPart();
                String nsuri = mPropNames[i].getNamespaceURI();
                String val = mPropMap.get(mPropKeys[i]);
                if (EncodingConst.TOP_FLAG.equals(localPart)
                        && "true".equals(val)) {
                    isTop = true;
                }
                if (!isTop || val == null || val.length() == 0) {
                    // skip preDecodeCharCoding and postEncodeCharCoding elements
                    if (EncodingConst.PRE_DECODE_CHAR_CODING.equals(localPart)
                            || EncodingConst.POST_ENCODE_CHAR_CODING.equals(localPart)) {
                        continue;
                    }
                }
                // now marshal it
                sb.append("\n").append("<").append(prefix); //NOI18N
                sb.append(':').append(localPart); //NOI18N
                sb.append(" xmlns:").append(prefix); //NOI18N
                sb.append("=\"").append(nsuri).append("\">"); //NOI18N
                sb.append(XMLUtil.toAttributeValue(val));
                sb.append("</").append(prefix).append(':'); //NOI18N
                sb.append(localPart).append(">"); //NOI18N
            }
        }
        return sb.toString();
    }
}
