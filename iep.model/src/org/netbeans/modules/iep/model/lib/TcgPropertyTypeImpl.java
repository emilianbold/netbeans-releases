/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */


package org.netbeans.modules.iep.model.lib;

/**
 * Concrete class implementing TcgPropertyType intrface. It serves as meta data
 * for TcgProperty
 *
 * @author Bing Lu
 *
 * @see TcgPropertyType
 * @see TcgProperty
 * @since April 30, 2002
 */
class TcgPropertyTypeImpl
    implements TcgPropertyType {

    private String mName = null;
    private String mTitle = null;
    private String mDescription = null;
    private Object mDefaultValue = null;
    private String mDefaultValueAsString = null;
    
    private TcgType mType = null;
    private String mEditor = null;
    private String mRenderer = null;
    private String mAccess = null;
    private boolean mReadable = true;
    private boolean mWritable = false;
    private boolean mMappableL = false;
    private boolean mMappableR = false;
    private boolean mExecutable = false;
    private boolean mMultiple = false;
    private boolean mRequired = true;
    private String mScript = null;
    private String mCategory = null;
    private boolean mIsTransient = false;


    /**
     * Constructor for the TcgPropertyTypeImpl object
     *
     * @param name Description of the Parameter
     * @param title Description of the Parameter
     * @param type Description of the Parameter
     * @param description Description of the Parameter
     * @param editor Description of the Parameter
     * @param renderer Description of the Parameter
     * @param defaultValue Description of the Parameter
     * @param access Description of the Parameter
     * @param multiple Description of the Parameter
     * @param required
     */
    TcgPropertyTypeImpl(String name, String title, String type,
                        String description, String editor, String renderer,
                        String defaultValue, String access, boolean multiple,
                        boolean required, String script, String category, boolean isTransient) {

        mName = name;
        mTitle = title;
        mType = multiple
                ? TcgType.getType(type + "List")
                : TcgType.getType(type);
        mDescription = description;
        mEditor = editor;
        mRenderer = renderer;
        mReadable = access.indexOf("read") >= 0;
        mWritable = access.indexOf("write") >= 0;
        mMappableL = access.indexOf("mapL") >= 0;
        mMappableR = access.indexOf("mapR") >= 0;
        mExecutable = access.indexOf("execute") >= 0;
        mAccess = access;
        mMultiple = multiple;
        mRequired = required;
        mDefaultValueAsString = defaultValue;
        mDefaultValue = mType.parse(defaultValue);
        mScript = script;
        mCategory = category;
        mIsTransient = isTransient;
    }
    
    /**
     * Constructor for the TcgPropertyTypeImpl object
     *
     * @param ppt Description of the Parameter
     * @param name Description of the Parameter
     * @param title Description of the Parameter
     * @param description Description of the Parameter
     * @param editor Description of the Parameter
     * @param renderer Description of the Parameter
     * @param defaultValue Description of the Parameter
     * @param access Description of the Parameter
     * @param required
     */
    TcgPropertyTypeImpl(TcgPropertyType ppt, String name, String title, 
                        String description, String editor, String renderer,
                        Object defaultValue, String access,
                        boolean required, String script, String category, boolean isTransient) {
        mName = name;
        mTitle = title;
        mType = ppt.getType();
        mDescription = description;
        mEditor = editor;
        mRenderer = renderer;
        mReadable = access.indexOf("read") >= 0;
        mWritable = access.indexOf("write") >= 0;
        mMappableL = access.indexOf("mapL") >= 0;
        mMappableR = access.indexOf("mapR") >= 0;
        mExecutable = access.indexOf("execute") >= 0;
        mAccess = access;
        mMultiple = ppt.isMultiple();
        mRequired = required;
        mDefaultValueAsString = defaultValue != null ? defaultValue.toString() : null;
        mDefaultValue = defaultValue;
        mScript = script;
        mCategory = category;
        mIsTransient = isTransient;
    }                            
    
    /**
     * Gets the defaultValue attribute of the TcgPropertyType object
     *
     * @return The defaultValue value
     */
    public Object getDefaultValue() {
        return mDefaultValue;
    }

    public String getDefaultValueAsString() {
        return mDefaultValueAsString;
    }
    
    /**
     * Gets the description attribute of the TcgPropertyType object
     *
     * @return The description value
     */
    public String getDescription() {
        return mDescription;
    }

    /**
     * Gets the editorName attribute of the TcgPropertyType object
     *
     * @return The editorName value
     */
    public String getEditorName() {
        return mEditor;
    }

    /**
     * Gets the multiple attribute of the TcgPropertyType object
     *
     * @return The multiple value
     */
    public boolean isMultiple() {
        return mMultiple;
    }

    /**
     * Gets the name attribute of the TcgPropertyType object
     *
     * @return The name value
     */
    public String getName() {
        return mName;
    }

    /**
     * Gets the readable attribute of the TcgPropertyType object
     *
     * @return The readable value
     */
    public boolean isReadable() {
        return mReadable;
    }

    /**
     * Gets the writable attribute of the TcgPropertyType object
     *
     * @return The writable value
     */
    public boolean isWritable() {
        return mWritable;
    }

    /**
     * Gets the mappableL attribute of the TcgPropertyType object
     *
     * @return The mappable value
     */
    public boolean isMappableL() {
        return mMappableL;
    }
    
    /**
     * Gets the mappableR attribute of the TcgPropertyType object
     *
     * @return The mappable value
     */
    public boolean isMappableR() {
        return mMappableR;
    }    

    /**
     * Gets the executable attribute of the TcgPropertyType object
     *
     * @return The executable value
     */
    public boolean isExecutable() {
        return mExecutable;
    }

    /**
     * Gets the access attribute of the TcgPropertyType object
     *
     * @return The access value
     */
    public String getAccess() {
        return mAccess;
    }

    /**
     * Gets the access attribute of the TcgPropertyType object
     *
     * @return The access value
     */
    public boolean hasAccess(String access) {
        return getAccess().indexOf(access) >= 0;
    }      

    /**
     * Gets the rendererName attribute of the TcgPropertyType object
     *
     * @return The rendererName value
     */
    public String getRendererName() {
        return mRenderer;
    }

    /**
     * Gets the required attribute of the TcgPropertyType object
     *
     * @return The required value
     */
    public boolean isRequired() {
        return mRequired;
    }

    /**
     * Gets the title attribute of the TcgPropertyType object
     *
     * @return The title value
     */
    public String getTitle() {
        return mTitle;
    }

    /**
     * Gets the type attribute of the TcgPropertyType object
     *
     * @return The type value
     */
    public TcgType getType() {
        return mType;
    }

    /**
     * Creates an instance of TcgProperty using this TcgPropertyType
     *
     * @param parentComponent the containing TcgComponent of the new property
     *
     * @return an instance of TcgProperty with this TcgPropertyType as its
     *         attribute
     */
    public TcgProperty newTcgProperty(TcgComponent parentComponent) {
        return new TcgPropertyImpl(this, parentComponent);
    }
    
    public String getScript() {
        return mScript;
    }
    
    public String getCategory() {
        return mCategory;
    }
    
    public boolean isTransient() {
        return mIsTransient;
    }
}


/*--- Formatted in SeeBeyond Java Convention Style on Thu, Dec 5, '02 ---*/


/*------ Formatted by Jindent 3.24 Gold 1.02 --- http://www.jindent.de ------*/
