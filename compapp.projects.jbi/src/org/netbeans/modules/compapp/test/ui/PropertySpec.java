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


package org.netbeans.modules.compapp.test.ui;

import java.beans.PropertyEditor;
import org.openide.util.NbBundle;

/**
 * PropertySpec.java
 *
 * Created on February 13, 2006, 5:19 PM
 *
 * @author Bing Lu
 */
public class PropertySpec {
    public static PropertySpec DESCRIPTION = new PropertySpec("description",  // NOI18N
                                      String.class,
                                      null,
                                      NbBundle.getMessage(PropertySpec.class, "description.displayName"), // NOI18N
                                      NbBundle.getMessage(PropertySpec.class, "description.description"), // NOI18N
                                      "", // NOI18N
                                      true,
                                      true,
                                      "Properties", // NOI18N
                                      NbBundle.getMessage(PropertySpec.class, "category.properties.displayName")); // NOI18N
    public static PropertySpec DESTINATION = new PropertySpec("destination",  // NOI18N
                                      String.class, 
                                      null,
                                      NbBundle.getMessage(PropertySpec.class, "destination.displayName"), // NOI18N
                                      NbBundle.getMessage(PropertySpec.class, "destination.description"), // NOI18N
                                      "", // NOI18N
                                      true,
                                      true,
                                      "Properties", // NOI18N
                                      NbBundle.getMessage(PropertySpec.class, "category.properties.displayName")); // NOI18N
    public static PropertySpec SOAP_ACTION = new PropertySpec("soapaction",  // NOI18N
                                      String.class, 
                                      null,
                                      NbBundle.getMessage(PropertySpec.class, "soapaction.displayName"), // NOI18N
                                      NbBundle.getMessage(PropertySpec.class, "soapaction.description"), // NOI18N
                                      "", // NOI18N
                                      true,
                                      true,
                                      "Properties", // NOI18N
                                      NbBundle.getMessage(PropertySpec.class, "category.properties.displayName")); // NOI18N
    public static PropertySpec INPUT_FILE = new PropertySpec("inputfile",  // NOI18N
                                      String.class, 
                                      null,
                                      NbBundle.getMessage(PropertySpec.class, "inputfile.displayName"), // NOI18N
                                      NbBundle.getMessage(PropertySpec.class, "inputfile.description"), // NOI18N
                                      "Input.xml", // NOI18N
                                      false,
                                      true,
                                      "Properties", // NOI18N
                                      NbBundle.getMessage(PropertySpec.class, "category.properties.displayName")); // NOI18N
    public static PropertySpec OUTPUT_FILE = new PropertySpec("outputfile",  // NOI18N
                                      String.class, 
                                      null,
                                      NbBundle.getMessage(PropertySpec.class, "outputfile.displayName"), // NOI18N
                                      NbBundle.getMessage(PropertySpec.class, "outputfile.description"), // NOI18N
                                      "Output.xml", // NOI18N
                                      false,
                                      true,
                                      "Properties", // NOI18N
                                      NbBundle.getMessage(PropertySpec.class, "category.properties.displayName")); // NOI18N
    public static PropertySpec CONCURRENT_THREADS = new PropertySpec("concurrentthreads",  // NOI18N
                                      Integer.class, 
                                      null,
                                      NbBundle.getMessage(PropertySpec.class, "concurrentthreads.displayName"), // NOI18N
                                      NbBundle.getMessage(PropertySpec.class, "concurrentthreads.description"), // NOI18N
                                      new Integer(1),
                                      true,
                                      true,
                                      "Properties", // NOI18N
                                      NbBundle.getMessage(PropertySpec.class, "category.properties.displayName")); // NOI18N
    public static PropertySpec INVOKES_PER_THREAD = new PropertySpec("invokesperthread",  // NOI18N
                                      Integer.class, 
                                      null,
                                      NbBundle.getMessage(PropertySpec.class, "invokesperthread.displayName"), // NOI18N
                                      NbBundle.getMessage(PropertySpec.class, "invokesperthread.description"), // NOI18N
                                      new Integer(1),
                                      true,
                                      true,
                                      "Properties", // NOI18N
                                      NbBundle.getMessage(PropertySpec.class, "category.properties.displayName")); // NOI18N
    public static PropertySpec TEST_TIMEOUT = new PropertySpec("testtimeout",  // NOI18N
                                      Integer.class, 
                                      null,
                                      NbBundle.getMessage(PropertySpec.class, "testtimeout.displayName"), // NOI18N
                                      NbBundle.getMessage(PropertySpec.class, "testtimeout.description"), // NOI18N
                                      new Integer(30),
                                      true,
                                      true,
                                      "Properties", // NOI18N
                                      NbBundle.getMessage(PropertySpec.class, "category.properties.displayName")); // NOI18N
    public static PropertySpec CALCULATE_THROUGHPUT = new PropertySpec("calculatethroughput",  // NOI18N
                                      Boolean.class, 
                                      null,
                                      NbBundle.getMessage(PropertySpec.class, "calculatethroughput.displayName"), // NOI18N
                                      NbBundle.getMessage(PropertySpec.class, "calculatethroughput.description"), // NOI18N
                                      Boolean.FALSE,
                                      true,
                                      true,
                                      "Properties", // NOI18N
                                      NbBundle.getMessage(PropertySpec.class, "category.properties.displayName")); // NOI18N
    public static PropertySpec COMPARISON_TYPE = new PropertySpec("comparisontype",  // NOI18N
                                      String.class, 
                                      ComparisonTypePropertyEditor.class,
                                      NbBundle.getMessage(PropertySpec.class, "comparisontype.displayName"), // NOI18N
                                      NbBundle.getMessage(PropertySpec.class, "comparisontype.description"), // NOI18N
                                      "identical", // NOI18N
                                      true,
                                      true,
                                      "Properties", // NOI18N
                                      NbBundle.getMessage(PropertySpec.class, "category.properties.displayName")); // NOI18N
    public static PropertySpec FEATURE_STATUS = new PropertySpec("featurestatus",  // NOI18N
                                      String.class, 
                                      FeatureStatusPropertyEditor.class,
                                      NbBundle.getMessage(PropertySpec.class, "featurestatus.displayName"), // NOI18N
                                      NbBundle.getMessage(PropertySpec.class, "featurestatus.description"), // NOI18N
                                      "done", // NOI18N
                                      true,
                                      true,
                                      "Properties", // NOI18N
                                      NbBundle.getMessage(PropertySpec.class, "category.properties.displayName")); // NOI18N
    
    private String mName;
    private Class mType;
    private Class mEditorType;
    private String mDisplayName;
    private String mShortDescription;
    private Object mDefaultValue;
    private boolean mCanWrite;
    private boolean mCanRead;
    private String mCategoryName;
    private String mCategoryDisplayName;
    
        
    /** Creates a new instance of PropertySpec */
    public PropertySpec(String name, 
                        Class type, 
                        Class editorType,
                        String displayName, 
                        String shortDescription,
                        Object defaultValue,
                        boolean canWrite, 
                        boolean canRead, 
                        String categoryName, 
                        String categoryDisplayName) 
    {
        mName = name;
        mType = type;
        mEditorType = editorType;
        mDisplayName = displayName;
        mShortDescription = shortDescription;
        mDefaultValue = defaultValue;
        mCanWrite = canWrite;
        mCanRead = canRead;
        mCategoryName = categoryName;
        mCategoryDisplayName = categoryDisplayName;
    }

    public String getName() {
        return mName;
    }

    public Class getType() {
        return mType;
    }

    public boolean canWrite() {
        return mCanWrite;
    }

    public boolean canRead() {
        return mCanRead;
    }

    public String getCategoryName() {
        return mCategoryName;
    }

    public String getCategoryDisplayName() {
        return mCategoryDisplayName;
    }

    public String getDisplayName() {
        return mDisplayName;
    }

    public String getShortDescription() {
        return mShortDescription;
    }
    
    public Object getDefaultValue() {
        return mDefaultValue;
    }

    public boolean hasEditorType() {
        return mEditorType != null;
    }
    
    public PropertyEditor getPropertyEditor() {
        try {
            return (PropertyEditor)mEditorType.newInstance();
        } catch (Exception e) {
            return null;
        }
    }
    
}
