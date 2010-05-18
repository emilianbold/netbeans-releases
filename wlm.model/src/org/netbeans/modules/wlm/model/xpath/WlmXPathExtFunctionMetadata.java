/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 1997-2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.wlm.model.xpath;

import java.util.ArrayList;
import java.util.List;
import javax.swing.Icon;
import javax.xml.namespace.QName;
import org.netbeans.modules.xml.xpath.ext.XPathOperationOrFuntion;
import org.netbeans.modules.xml.xpath.ext.metadata.AbstractArgument;
import org.netbeans.modules.xml.xpath.ext.metadata.ArgumentDescriptor;
import org.netbeans.modules.xml.xpath.ext.metadata.ExtFunctionMetadata;
import org.netbeans.modules.xml.xpath.ext.metadata.ResultTypeCalculator;
import org.netbeans.modules.xml.xpath.ext.metadata.XPathType;
import org.netbeans.modules.xml.xpath.ext.metadata.impl.images.IconLoader;
import org.openide.util.NbBundle;

/**
 * Contains Metadata constants for Extended XPath functions for BPEL
 * 
 * @author nk160297
 */
public interface WlmXPathExtFunctionMetadata {

    String IMAGE_FOLDER_NAME = "org/netbeans/modules/wlm/model/xpath/"; // NOI18N

    // WLM Functions

    String WLM_EXT_FUNC_NS = "http://jbi.com.sun/wfse/xpath-functions"; // NOI18N
    String BPEL_EXT_FUNC_NS = "http://www.sun.com/wsbpel/2.0/process/executable/SUNExtension/XPathFunctions"; // NOI18N

    ExtFunctionMetadata GET_TASK_ID_METADATA = new ExtFunctionMetadata() {
        private List<AbstractArgument> mArguments;

        public boolean isContextItemRequired(XPathOperationOrFuntion func) {
            return false;
        }

        public QName getName() {
            return new QName(WLM_EXT_FUNC_NS, "get-task-id"); // NOI18N
        }

        public Icon getIcon() {
            return IconLoader.getIcon(getName().getLocalPart(), IMAGE_FOLDER_NAME);
        }

        public String getDisplayName() {
            return NbBundle.getMessage(WlmXPathExtFunctionMetadata.class,
                            "DN_GetTaskID"); // NOI18N
        }

        public String getShortDescription() {
            return "";
        }

        public String getLongDescription() {
            return "";
        }

        public FunctionType getFunctionType() {
            return FunctionType.EXT_FUNCTION;
        }

        public synchronized List<AbstractArgument> getArguments() {
            if (mArguments == null) {
                mArguments = new ArrayList<AbstractArgument>();
            }
            return mArguments;
        }

        public XPathType getResultType() {
            return XPathType.STRING_TYPE;
        }

        public ResultTypeCalculator getResultTypeCalculator() {
            return null;
        }

        public String toStrign() {
            return getName().toString();
        }
    };


    ExtFunctionMetadata GET_TASK_OWNER_METADATA = new ExtFunctionMetadata() {
        private List<AbstractArgument> mArguments;

        public boolean isContextItemRequired(XPathOperationOrFuntion func) {
            return false;
        }

        public QName getName() {
            return new QName(WLM_EXT_FUNC_NS, "get-task-owner"); // NOI18N
        }

        public Icon getIcon() {
            return IconLoader.getIcon(getName().getLocalPart(), IMAGE_FOLDER_NAME);
        }

        public String getDisplayName() {
            return NbBundle.getMessage(WlmXPathExtFunctionMetadata.class,
                            "DN_GetTaskOwner"); // NOI18N
        }

        public String getShortDescription() {
            return "";
        }

        public String getLongDescription() {
            return "";
        }

        public FunctionType getFunctionType() {
            return FunctionType.EXT_FUNCTION;
        }

        public synchronized List<AbstractArgument> getArguments() {
            if (mArguments == null) {
                mArguments = new ArrayList<AbstractArgument>();
            }
            return mArguments;
        }

        public XPathType getResultType() {
            return XPathType.STRING_TYPE;
        }

        public ResultTypeCalculator getResultTypeCalculator() {
            return null;
        }

        public String toStrign() {
            return getName().toString();
        }
    };


    ExtFunctionMetadata GET_EMAIL_METADATA = new ExtFunctionMetadata() {

        private List<AbstractArgument> mArguments;

        public boolean isContextItemRequired(XPathOperationOrFuntion func) {
            return false;
        }

        public QName getName() {
            return new QName(WLM_EXT_FUNC_NS, "get-email"); // NOI18N
        }

        public Icon getIcon() {
            return IconLoader.getIcon(getName().getLocalPart(), IMAGE_FOLDER_NAME);
        }

        public String getDisplayName() {
            return NbBundle.getMessage(WlmXPathExtFunctionMetadata.class,
                            "DN_GetEmail"); // NOI18N
        }

        public String getShortDescription() {
            return "";
        }

        public String getLongDescription() {
            return "";
        }

        public FunctionType getFunctionType() {
            return FunctionType.EXT_FUNCTION;
        }

        public synchronized List<AbstractArgument> getArguments() {
            if (mArguments == null) {
                mArguments = new ArrayList<AbstractArgument>();
                initArguments();
            }
            return mArguments;
        }

        public XPathType getResultType() {
            return XPathType.STRING_TYPE;
        }

        public ResultTypeCalculator getResultTypeCalculator() {
            return null;
        }

        public String toStrign() {
            return getName().toString();
        }

        private void initArguments() {
            mArguments.add(ArgumentDescriptor.Predefined.OPTIONAL_STRING);
        }

    };

    ExtFunctionMetadata GET_MANAGER_EMAIL_METADATA = new ExtFunctionMetadata() {

        private List<AbstractArgument> mArguments;

        public boolean isContextItemRequired(XPathOperationOrFuntion func) {
            return false;
        }

        public QName getName() {
            return new QName(WLM_EXT_FUNC_NS, "get-manager-email"); // NOI18N
        }

        public Icon getIcon() {
            return IconLoader.getIcon(getName().getLocalPart(), IMAGE_FOLDER_NAME);
        }

        public String getDisplayName() {
            return NbBundle.getMessage(WlmXPathExtFunctionMetadata.class,
                            "DN_GetManagerEmail"); // NOI18N
        }

        public String getShortDescription() {
            return "";
        }

        public String getLongDescription() {
            return "";
        }

        public FunctionType getFunctionType() {
            return FunctionType.EXT_FUNCTION;
        }

        public synchronized List<AbstractArgument> getArguments() {
            if (mArguments == null) {
                mArguments = new ArrayList<AbstractArgument>();
                initArguments();
            }
            return mArguments;
        }

        public XPathType getResultType() {
            return XPathType.STRING_TYPE;
        }

        public ResultTypeCalculator getResultTypeCalculator() {
            return null;
        }

        public String toStrign() {
            return getName().toString();
        }

        private void initArguments() {
            mArguments.add(ArgumentDescriptor.Predefined.OPTIONAL_STRING);
        }

    };

    ExtFunctionMetadata GET_MANAGER_UID_METADATA = new ExtFunctionMetadata() {

        private List<AbstractArgument> mArguments;

        public boolean isContextItemRequired(XPathOperationOrFuntion func) {
            return false;
        }

        public QName getName() {
            return new QName(WLM_EXT_FUNC_NS, "get-manager-uid"); // NOI18N
        }

        public Icon getIcon() {
            return IconLoader.getIcon(getName().getLocalPart(), IMAGE_FOLDER_NAME);
        }

        public String getDisplayName() {
            return NbBundle.getMessage(WlmXPathExtFunctionMetadata.class,
                            "DN_GetManagerUID"); // NOI18N
        }

        public String getShortDescription() {
            return "";
        }

        public String getLongDescription() {
            return "";
        }

        public FunctionType getFunctionType() {
            return FunctionType.EXT_FUNCTION;
        }

        public synchronized List<AbstractArgument> getArguments() {
            if (mArguments == null) {
                mArguments = new ArrayList<AbstractArgument>();
                initArguments();
            }
            return mArguments;
        }

        public XPathType getResultType() {
            return XPathType.STRING_TYPE;
        }

        public ResultTypeCalculator getResultTypeCalculator() {
            return null;
        }

        public String toStrign() {
            return getName().toString();
        }

        private void initArguments() {
            mArguments.add(ArgumentDescriptor.Predefined.OPTIONAL_STRING);
        }

    };


    // Functions taken from BPEL module
    // TODO: Remove unnecessary functions

    // String SUN_EXT_FUNC_NS = "http://www.sun.com/wsbpel/2.0/process/executable/SUNExtension/XPathFunctions"; // NOI18N
    
    ExtFunctionMetadata CURRENT_TIME_METADATA = new ExtFunctionMetadata() {

        public boolean isContextItemRequired(XPathOperationOrFuntion func) {
            return false;
        }

        public QName getName() {
            return new QName(BPEL_EXT_FUNC_NS, "current-time"); // NOI18N
        }

        public Icon getIcon() {
            return IconLoader.getIcon(getName().getLocalPart(), IMAGE_FOLDER_NAME);
        }
        
        public String getDisplayName() {
            return NbBundle.getMessage(WlmXPathExtFunctionMetadata.class, 
                            "DN_CurrentTime"); // NOI18N
        }

        public String getShortDescription() {
            return "";
        }

        public String getLongDescription() {
            return "";
        }

        public FunctionType getFunctionType() {
            return FunctionType.EXT_FUNCTION;
        }

        public synchronized List<AbstractArgument> getArguments() {
            return null;
        }

        public XPathType getResultType() {
            return XPathType.STRING_TYPE;
        }

        public ResultTypeCalculator getResultTypeCalculator() {
            return null;
        }
        
        public String toStrign() {
            return getName().toString();
        }
        
    };
    
    ExtFunctionMetadata CURRENT_DATE_METADATA = new ExtFunctionMetadata() {

        public boolean isContextItemRequired(XPathOperationOrFuntion func) {
            return false;
        }

        public QName getName() {
            return new QName(BPEL_EXT_FUNC_NS, "current-date"); // NOI18N
        }

        public Icon getIcon() {
            return IconLoader.getIcon(getName().getLocalPart(), IMAGE_FOLDER_NAME);
        }
        
        public String getDisplayName() {
            return NbBundle.getMessage(WlmXPathExtFunctionMetadata.class, 
                            "DN_CurrentDate"); // NOI18N
        }

        public String getShortDescription() {
            return "";
        }

        public String getLongDescription() {
            return "";
        }

        public FunctionType getFunctionType() {
            return FunctionType.EXT_FUNCTION;
        }

        public synchronized List<AbstractArgument> getArguments() {
            return null;
        }

        public XPathType getResultType() {
            return XPathType.STRING_TYPE;
        }

        public ResultTypeCalculator getResultTypeCalculator() {
            return null;
        }
        
        public String toStrign() {
            return getName().toString();
        }
        
    };
    
    ExtFunctionMetadata CURRENT_DATE_TIME_METADATA = new ExtFunctionMetadata() {

        public boolean isContextItemRequired(XPathOperationOrFuntion func) {
            return false;
        }

        public QName getName() {
            return new QName(BPEL_EXT_FUNC_NS, "current-dateTime"); // NOI18N
        }

        public Icon getIcon() {
            return IconLoader.getIcon(getName().getLocalPart(), IMAGE_FOLDER_NAME);
        }
        
        public String getDisplayName() {
            return NbBundle.getMessage(WlmXPathExtFunctionMetadata.class, 
                            "DN_CurrentDateAndTime"); // NOI18N
        }

        public String getShortDescription() {
            return "";
        }

        public String getLongDescription() {
            return "";
        }

        public FunctionType getFunctionType() {
            return FunctionType.EXT_FUNCTION;
        }

        public synchronized List<AbstractArgument> getArguments() {
            return null;
        }

        public XPathType getResultType() {
            return XPathType.STRING_TYPE;
        }

        public ResultTypeCalculator getResultTypeCalculator() {
            return null;
        }
        
        public String toStrign() {
            return getName().toString();
        }
        
    };
    
//    ExtFunctionMetadata DO_MARSHAL_METADATA = new ExtFunctionMetadata() {
//
//        private List<AbstractArgument> mArguments;
//
//        public boolean isContextItemRequired(XPathOperationOrFuntion func) {
//            return false;
//        }
//
//        public QName getName() {
//            return new QName(SUN_EXT_FUNC_NS, "doMarshal"); // NOI18N
//        }
//
//        public Icon getIcon() {
//            return IconLoader.getIcon("marshal", IMAGE_FOLDER_NAME); // NOI18N
//        }
//
//        public String getDisplayName() {
//            return NbBundle.getMessage(WlmXPathExtFunctionMetadata.class,
//                            "DN_DoMarshal"); // NOI18N
//        }
//
//        public String getShortDescription() {
//            return "";
//        }
//
//        public String getLongDescription() {
//            return "";
//        }
//
//        public FunctionType getFunctionType() {
//            return FunctionType.EXT_FUNCTION;
//        }
//
//        public synchronized List<AbstractArgument> getArguments() {
//            if (mArguments == null) {
//                mArguments = new ArrayList<AbstractArgument>();
//                initArguments();
//            }
//            return mArguments;
//        }
//
//        public XPathType getResultType() {
//            return XPathType.STRING_TYPE;
//        }
//
//        public ResultTypeCalculator getResultTypeCalculator() {
//            return null;
//        }
//
//        private void initArguments() {
//            mArguments.add(ArgumentDescriptor.Predefined.SIMPLE_NODE);
//        }
//
//        public String toStrign() {
//            return getName().toString();
//        }
//
//    };
//
//    ExtFunctionMetadata DO_UNMARSHAL_METADATA = new ExtFunctionMetadata() {
//
//        private List<AbstractArgument> mArguments;
//
//        public boolean isContextItemRequired(XPathOperationOrFuntion func) {
//            return false;
//        }
//
//        public QName getName() {
//            return new QName(SUN_EXT_FUNC_NS, "doUnMarshal"); // NOI18N
//        }
//
//        public Icon getIcon() {
//            return IconLoader.getIcon("unmarshal", IMAGE_FOLDER_NAME); // NOI18N
//        }
//
//        public String getDisplayName() {
//            return NbBundle.getMessage(WlmXPathExtFunctionMetadata.class,
//                            "DN_DoUnmarshal"); // NOI18N
//        }
//
//        public String getShortDescription() {
//            return "";
//        }
//
//        public String getLongDescription() {
//            return "";
//        }
//
//        public FunctionType getFunctionType() {
//            return FunctionType.EXT_FUNCTION;
//        }
//
//        public synchronized List<AbstractArgument> getArguments() {
//            if (mArguments == null) {
//                mArguments = new ArrayList<AbstractArgument>();
//                initArguments();
//            }
//            return mArguments;
//        }
//
//        public XPathType getResultType() {
//            return XPathType.NODE_TYPE;
//        }
//
//        public ResultTypeCalculator getResultTypeCalculator() {
//            return null;
//        }
//
//        private void initArguments() {
//            mArguments.add(ArgumentDescriptor.Predefined.SIMPLE_STRING);
//        }
//
//        public String toStrign() {
//            return getName().toString();
//        }
//
//    };
//
//    ExtFunctionMetadata GET_GUID_METADATA = new ExtFunctionMetadata() {
//
//        public boolean isContextItemRequired(XPathOperationOrFuntion func) {
//            return false;
//        }
//
//        public QName getName() {
//            return new QName(SUN_EXT_FUNC_NS, "getGUID"); // NOI18N
//        }
//
//        public Icon getIcon() {
//            return IconLoader.getIcon(getName().getLocalPart(), IMAGE_FOLDER_NAME);
//        }
//
//        public String getDisplayName() {
//            return NbBundle.getMessage(WlmXPathExtFunctionMetadata.class,
//                            "DN_GetGUID"); // NOI18N
//        }
//
//        public String getShortDescription() {
//            return "";
//        }
//
//        public String getLongDescription() {
//            return "";
//        }
//
//        public FunctionType getFunctionType() {
//            return FunctionType.EXT_FUNCTION;
//        }
//
//        public synchronized List<AbstractArgument> getArguments() {
//            return null;
//        }
//
//        public XPathType getResultType() {
//            return XPathType.STRING_TYPE;
//        }
//
//        public ResultTypeCalculator getResultTypeCalculator() {
//            return null;
//        }
//
//        public String toStrign() {
//            return getName().toString();
//        }
//
//    };
//
//    ExtFunctionMetadata GET_BPID_METADATA = new ExtFunctionMetadata() {
//
//        public boolean isContextItemRequired(XPathOperationOrFuntion func) {
//            return false;
//        }
//
//        public QName getName() {
//            return new QName(SUN_EXT_FUNC_NS, "getBPId"); // NOI18N
//        }
//
//        public Icon getIcon() {
//            return IconLoader.getIcon(getName().getLocalPart(), IMAGE_FOLDER_NAME);
//        }
//
//        public String getDisplayName() {
//            return NbBundle.getMessage(WlmXPathExtFunctionMetadata.class,
//                            "DN_GetBPId"); // NOI18N
//        }
//
//        public String getShortDescription() {
//            return "";
//        }
//
//        public String getLongDescription() {
//            return "";
//        }
//
//        public FunctionType getFunctionType() {
//            return FunctionType.EXT_FUNCTION;
//        }
//
//        public synchronized List<AbstractArgument> getArguments() {
//            return null;
//        }
//
//        public XPathType getResultType() {
//            return XPathType.STRING_TYPE;
//        }
//
//        public ResultTypeCalculator getResultTypeCalculator() {
//            return null;
//        }
//
//        public String toStrign() {
//            return getName().toString();
//        }
//
//    };
//
//    ExtFunctionMetadata EXIST_METADATA = new ExtFunctionMetadata() {
//
//        private List<AbstractArgument> mArguments;
//
//        public boolean isContextItemRequired(XPathOperationOrFuntion func) {
//            return false;
//        }
//
//        public QName getName() {
//            return new QName(SUN_EXT_FUNC_NS, "exist"); // NOI18N
//        }
//
//        public Icon getIcon() {
//            return IconLoader.getIcon(getName().getLocalPart(), IMAGE_FOLDER_NAME);
//        }
//
//        public String getDisplayName() {
//            return NbBundle.getMessage(WlmXPathExtFunctionMetadata.class,
//                            "DN_Exist"); // NOI18N
//        }
//
//        public String getShortDescription() {
//            return "";
//        }
//
//        public String getLongDescription() {
//            return "";
//        }
//
//        public FunctionType getFunctionType() {
//            return FunctionType.EXT_FUNCTION;
//        }
//
//        public synchronized List<AbstractArgument> getArguments() {
//            if (mArguments == null) {
//                mArguments = new ArrayList<AbstractArgument>();
//                mArguments.add(ArgumentDescriptor.Predefined.SIMPLE_NODE_SET);
//            }
//            return mArguments;
//        }
//
//        public XPathType getResultType() {
//            return XPathType.BOOLEAN_TYPE;
//        }
//
//        public ResultTypeCalculator getResultTypeCalculator() {
//            return null;
//        }
//
//        public String toStrign() {
//            return getName().toString();
//        }
//
//    };
//
//    ExtFunctionMetadata DATE_TIME_LT_METADATA = new ExtFunctionMetadata() {
//
//        private List<AbstractArgument> mArguments;
//
//        public boolean isContextItemRequired(XPathOperationOrFuntion func) {
//            return false;
//        }
//
//        public QName getName() {
//            return new QName(SUN_EXT_FUNC_NS, "dateTime-less-than"); // NOI18N
//        }
//
//        public Icon getIcon() {
//            return IconLoader.getIcon(getName().getLocalPart(), IMAGE_FOLDER_NAME);
//        }
//
//        public String getDisplayName() {
//            return NbBundle.getMessage(WlmXPathExtFunctionMetadata.class,
//                            "DN_DateTimeLessThan"); // NOI18N
//        }
//
//        public String getShortDescription() {
//            return "";
//        }
//
//        public String getLongDescription() {
//            return "";
//        }
//
//        public FunctionType getFunctionType() {
//            return FunctionType.EXT_FUNCTION;
//        }
//
//        public synchronized List<AbstractArgument> getArguments() {
//            if (mArguments == null) {
//                mArguments = new ArrayList<AbstractArgument>();
//                mArguments.add(ArgumentDescriptor.Predefined.SIMPLE_DATE_TIME_STRING);
//                mArguments.add(ArgumentDescriptor.Predefined.SIMPLE_DATE_TIME_STRING);
//            }
//            return mArguments;
//        }
//
//        public XPathType getResultType() {
//            return XPathType.BOOLEAN_TYPE;
//        }
//
//        public ResultTypeCalculator getResultTypeCalculator() {
//            return null;
//        }
//
//        public String toStrign() {
//            return getName().toString();
//        }
//
//    };
//
//    ExtFunctionMetadata DATE_LT_METADATA = new ExtFunctionMetadata() {
//
//        private List<AbstractArgument> mArguments;
//
//        public boolean isContextItemRequired(XPathOperationOrFuntion func) {
//            return false;
//        }
//
//        public QName getName() {
//            return new QName(SUN_EXT_FUNC_NS, "date-less-than"); // NOI18N
//        }
//
//        public Icon getIcon() {
//            return IconLoader.getIcon(getName().getLocalPart(), IMAGE_FOLDER_NAME);
//        }
//
//        public String getDisplayName() {
//            return NbBundle.getMessage(WlmXPathExtFunctionMetadata.class,
//                            "DN_DateLessThan"); // NOI18N
//        }
//
//        public String getShortDescription() {
//            return "";
//        }
//
//        public String getLongDescription() {
//            return "";
//        }
//
//        public FunctionType getFunctionType() {
//            return FunctionType.EXT_FUNCTION;
//        }
//
//        public synchronized List<AbstractArgument> getArguments() {
//            if (mArguments == null) {
//                mArguments = new ArrayList<AbstractArgument>();
//                mArguments.add(ArgumentDescriptor.Predefined.SIMPLE_DATE_TIME_STRING);
//                mArguments.add(ArgumentDescriptor.Predefined.SIMPLE_DATE_TIME_STRING);
//            }
//            return mArguments;
//        }
//
//        public XPathType getResultType() {
//            return XPathType.BOOLEAN_TYPE;
//        }
//
//        public ResultTypeCalculator getResultTypeCalculator() {
//            return null;
//        }
//
//        public String toStrign() {
//            return getName().toString();
//        }
//
//    };
//
//    ExtFunctionMetadata TIME_LT_METADATA = new ExtFunctionMetadata() {
//
//        private List<AbstractArgument> mArguments;
//
//        public boolean isContextItemRequired(XPathOperationOrFuntion func) {
//            return false;
//        }
//
//        public QName getName() {
//            return new QName(SUN_EXT_FUNC_NS, "time-less-than"); // NOI18N
//        }
//
//        public Icon getIcon() {
//            return IconLoader.getIcon(getName().getLocalPart(), IMAGE_FOLDER_NAME);
//        }
//
//        public String getDisplayName() {
//            return NbBundle.getMessage(WlmXPathExtFunctionMetadata.class,
//                            "DN_TimeLessThan"); // NOI18N
//        }
//
//        public String getShortDescription() {
//            return "";
//        }
//
//        public String getLongDescription() {
//            return "";
//        }
//
//        public FunctionType getFunctionType() {
//            return FunctionType.EXT_FUNCTION;
//        }
//
//        public synchronized List<AbstractArgument> getArguments() {
//            if (mArguments == null) {
//                mArguments = new ArrayList<AbstractArgument>();
//                mArguments.add(ArgumentDescriptor.Predefined.SIMPLE_DATE_TIME_STRING);
//                mArguments.add(ArgumentDescriptor.Predefined.SIMPLE_DATE_TIME_STRING);
//            }
//            return mArguments;
//        }
//
//        public XPathType getResultType() {
//            return XPathType.BOOLEAN_TYPE;
//        }
//
//        public ResultTypeCalculator getResultTypeCalculator() {
//            return null;
//        }
//
//        public String toStrign() {
//            return getName().toString();
//        }
//
//    };
    
    
}
