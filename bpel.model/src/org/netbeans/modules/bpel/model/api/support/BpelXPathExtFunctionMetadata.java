/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.bpel.model.api.support;

import java.util.ArrayList;
import java.util.List;
import java.lang.reflect.Method;
import javax.swing.Icon;
import javax.xml.namespace.QName;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.ext.Extensions;
import org.netbeans.modules.xml.xpath.ext.XPathOperationOrFuntion;
import org.netbeans.modules.xml.xpath.ext.metadata.AbstractArgument;
import org.netbeans.modules.xml.xpath.ext.metadata.ArgumentDescriptor;
import org.netbeans.modules.xml.xpath.ext.metadata.ArgumentGroup;
import org.netbeans.modules.xml.xpath.ext.metadata.ExtFunctionMetadata;
import org.netbeans.modules.xml.xpath.ext.metadata.ResultTypeCalculator;
import org.netbeans.modules.xml.xpath.ext.metadata.XPathType;
import org.netbeans.modules.xml.xpath.ext.metadata.impl.images.IconLoader;
import org.netbeans.modules.xml.xpath.ext.spi.ExtensionFunctionResolver;
import org.openide.util.NbBundle;

/**
 * Contains Metadata constants for Extended XPath functions for BPEL
 * 
 * @author nk160297
 */
public interface BpelXPathExtFunctionMetadata {

    String IMAGE_FOLDER_NAME = "org/netbeans/modules/bpel/model/api/support/"; // NOI18N
    String SUN_EXT_FUNC_NS = "http://www.sun.com/wsbpel/2.0/process/executable/SUNExtension/XPathFunctions"; // NOI18N
    
    ExtFunctionMetadata DO_XSL_TRANSFORM_METADATA = new ExtFunctionMetadata() {

        private List<AbstractArgument> mArguments;
        
        public boolean isContextItemRequired(XPathOperationOrFuntion func) {
            return false;
        }

        public QName getName() {
            return new QName(BpelEntity.BUSINESS_PROCESS_NS_URI, "doXslTransform"); // NOI18N
        }

        public Icon getIcon() {
            return IconLoader.getIcon("doXslTransform", IMAGE_FOLDER_NAME); // NOI18N
        }
        
        public String getDisplayName() {
            return NbBundle.getMessage(BpelXPathExtFunctionMetadata.class, 
                            "DN_DoXSLTransform"); // NOI18N
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
            return XPathType.NODE_TYPE;
        }

        public ResultTypeCalculator getResultTypeCalculator() {
            return null;
        }
        
        private void initArguments() {
            mArguments.add(ArgumentDescriptor.Predefined.SIMPLE_STRING);
            mArguments.add(ArgumentDescriptor.Predefined.SIMPLE_NODE_SET);
            //
            String nameValuePairsGroupName = "Pairs Name&Value"; // TODO: take from resources
            mArguments.add(new ArgumentGroup(0, Integer.MAX_VALUE,
                    nameValuePairsGroupName,
                    ArgumentDescriptor.Predefined.SIMPLE_STRING,
                    ArgumentDescriptor.Predefined.ANY_TYPE));
        }
        
        public String toStrign() {
            return getName().toString();
        }
        
    };

    ExtFunctionMetadata GET_VARIABLE_PROPERTY_METADATA = 
            new ExtFunctionMetadata() {

        private List<AbstractArgument> mArguments;
        
        public boolean isContextItemRequired(XPathOperationOrFuntion func) {
            return false;
        }

        public QName getName() {
            return new QName(BpelEntity.BUSINESS_PROCESS_NS_URI, 
                    "getVariableProperty"); // NOI18N
        }

        public Icon getIcon() {
            return IconLoader.getIcon("getVariableProperty", IMAGE_FOLDER_NAME); // NOI18N
        }
        
        public String getDisplayName() {
            return NbBundle.getMessage(BpelXPathExtFunctionMetadata.class, 
                            "DN_GetVariableProperty"); // NOI18N
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
            return XPathType.ANY_TYPE;
        }

        public ResultTypeCalculator getResultTypeCalculator() {
            return null;
        }
        
        private void initArguments() {
            mArguments.add(ArgumentDescriptor.Predefined.SIMPLE_STRING);
            mArguments.add(ArgumentDescriptor.Predefined.SIMPLE_STRING);
        }
        
        public String toStrign() {
            return getName().toString();
        }
    };

    ExtFunctionMetadata GET_VARIABLE_NM_PROPERTY_METADATA = 
            new ExtFunctionMetadata() 
    {
        private List<AbstractArgument> mArguments;
        
        public boolean isContextItemRequired(XPathOperationOrFuntion func) {
            return false;
        }

        public QName getName() {
            return new QName(Extensions.NM_PROPERTY_EXT_URI, 
                    "getVariableNMProperty"); // NOI18N
        }

        public Icon getIcon() {
            return IconLoader.getIcon("getVariableProperty", IMAGE_FOLDER_NAME); // NOI18N
        }
        
        public String getDisplayName() {
            return NbBundle.getMessage(BpelXPathExtFunctionMetadata.class, 
                            "DN_GetVariableNMProperty"); // NOI18N
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
            return XPathType.ANY_TYPE;
        }

        public ResultTypeCalculator getResultTypeCalculator() {
            return null;
        }
        
        private void initArguments() {
            mArguments.add(ArgumentDescriptor.Predefined.SIMPLE_STRING);
            mArguments.add(ArgumentDescriptor.Predefined.SIMPLE_STRING);
        }
        
        public String toStrign() {
            return getName().toString();
        }
    };
    
    ExtFunctionMetadata CURRENT_TIME_METADATA = new ExtFunctionMetadata() {

        public boolean isContextItemRequired(XPathOperationOrFuntion func) {
            return false;
        }

        public QName getName() {
            return new QName(SUN_EXT_FUNC_NS, "current-time"); // NOI18N
        }

        public Icon getIcon() {
            return IconLoader.getIcon(getName().getLocalPart(), IMAGE_FOLDER_NAME);
        }
        
        public String getDisplayName() {
            return NbBundle.getMessage(BpelXPathExtFunctionMetadata.class, 
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
            return new QName(SUN_EXT_FUNC_NS, "current-date"); // NOI18N
        }

        public Icon getIcon() {
            return IconLoader.getIcon(getName().getLocalPart(), IMAGE_FOLDER_NAME);
        }
        
        public String getDisplayName() {
            return NbBundle.getMessage(BpelXPathExtFunctionMetadata.class, 
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
            return new QName(SUN_EXT_FUNC_NS, "current-dateTime"); // NOI18N
        }

        public Icon getIcon() {
            return IconLoader.getIcon(getName().getLocalPart(), IMAGE_FOLDER_NAME);
        }
        
        public String getDisplayName() {
            return NbBundle.getMessage(BpelXPathExtFunctionMetadata.class, 
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
    
    ExtFunctionMetadata DO_MARSHAL_METADATA = new ExtFunctionMetadata() {

        private List<AbstractArgument> mArguments;
        
        public boolean isContextItemRequired(XPathOperationOrFuntion func) {
            return false;
        }

        public QName getName() {
            return new QName(SUN_EXT_FUNC_NS, "doMarshal"); // NOI18N
        }

        public Icon getIcon() {
            return IconLoader.getIcon("marshal", IMAGE_FOLDER_NAME); // NOI18N
        }
        
        public String getDisplayName() {
            return NbBundle.getMessage(BpelXPathExtFunctionMetadata.class, 
                            "DN_DoMarshal"); // NOI18N
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
        
        private void initArguments() {
            mArguments.add(ArgumentDescriptor.Predefined.SIMPLE_NODE);
        }
        
        public String toStrign() {
            return getName().toString();
        }
        
    };
    
    ExtFunctionMetadata DO_UNMARSHAL_METADATA = new ExtFunctionMetadata() {

        private List<AbstractArgument> mArguments;
        
        public boolean isContextItemRequired(XPathOperationOrFuntion func) {
            return false;
        }

        public QName getName() {
            return new QName(SUN_EXT_FUNC_NS, "doUnMarshal"); // NOI18N
        }

        public Icon getIcon() {
            return IconLoader.getIcon("unmarshal", IMAGE_FOLDER_NAME); // NOI18N
        }
        
        public String getDisplayName() {
            return NbBundle.getMessage(BpelXPathExtFunctionMetadata.class, 
                            "DN_DoUnmarshal"); // NOI18N
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
            return XPathType.NODE_TYPE;
        }

        public ResultTypeCalculator getResultTypeCalculator() {
            return null;
        }
        
        private void initArguments() {
            mArguments.add(ArgumentDescriptor.Predefined.SIMPLE_STRING);
        }
        
        public String toStrign() {
            return getName().toString();
        }
        
    };
    
    ExtFunctionMetadata GET_GUID_METADATA = new ExtFunctionMetadata() {

        public boolean isContextItemRequired(XPathOperationOrFuntion func) {
            return false;
        }

        public QName getName() {
            return new QName(SUN_EXT_FUNC_NS, "getGUID"); // NOI18N
        }

        public Icon getIcon() {
            return IconLoader.getIcon(getName().getLocalPart(), IMAGE_FOLDER_NAME);
        }
        
        public String getDisplayName() {
            return NbBundle.getMessage(BpelXPathExtFunctionMetadata.class, 
                            "DN_GetGUID"); // NOI18N
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
    
    ExtFunctionMetadata GET_BPID_METADATA = new ExtFunctionMetadata() {

        public boolean isContextItemRequired(XPathOperationOrFuntion func) {
            return false;
        }

        public QName getName() {
            return new QName(SUN_EXT_FUNC_NS, "getBPId"); // NOI18N
        }

        public Icon getIcon() {
            return IconLoader.getIcon(getName().getLocalPart(), IMAGE_FOLDER_NAME);
        }
        
        public String getDisplayName() {
            return NbBundle.getMessage(BpelXPathExtFunctionMetadata.class, 
                            "DN_GetBPId"); // NOI18N
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
    
    ExtFunctionMetadata EXIST_METADATA = new ExtFunctionMetadata() {

        private List<AbstractArgument> mArguments;
        
        public boolean isContextItemRequired(XPathOperationOrFuntion func) {
            return false;
        }

        public QName getName() {
            return new QName(SUN_EXT_FUNC_NS, "exist"); // NOI18N
        }

        public Icon getIcon() {
            return IconLoader.getIcon(getName().getLocalPart(), IMAGE_FOLDER_NAME);
        }
        
        public String getDisplayName() {
            return NbBundle.getMessage(BpelXPathExtFunctionMetadata.class, 
                            "DN_Exist"); // NOI18N
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
                mArguments.add(ArgumentDescriptor.Predefined.SIMPLE_NODE_SET);
            }
            return mArguments;
        }

        public XPathType getResultType() {
            return XPathType.BOOLEAN_TYPE;
        }

        public ResultTypeCalculator getResultTypeCalculator() {
            return null;
        }
        
        public String toStrign() {
            return getName().toString();
        }
        
    };
    
    ExtFunctionMetadata DATE_TIME_LT_METADATA = new ExtFunctionMetadata() {

        private List<AbstractArgument> mArguments;
        
        public boolean isContextItemRequired(XPathOperationOrFuntion func) {
            return false;
        }

        public QName getName() {
            return new QName(SUN_EXT_FUNC_NS, "dateTime-less-than"); // NOI18N
        }

        public Icon getIcon() {
            return IconLoader.getIcon(getName().getLocalPart(), IMAGE_FOLDER_NAME);
        }
        
        public String getDisplayName() {
            return NbBundle.getMessage(BpelXPathExtFunctionMetadata.class, 
                            "DN_DateTimeLessThan"); // NOI18N
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
                mArguments.add(ArgumentDescriptor.Predefined.SIMPLE_DATE_TIME_STRING);
                mArguments.add(ArgumentDescriptor.Predefined.SIMPLE_DATE_TIME_STRING);
            }
            return mArguments;
        }

        public XPathType getResultType() {
            return XPathType.BOOLEAN_TYPE;
        }

        public ResultTypeCalculator getResultTypeCalculator() {
            return null;
        }
        
        public String toStrign() {
            return getName().toString();
        }
        
    };
    
    ExtFunctionMetadata DATE_LT_METADATA = new ExtFunctionMetadata() {

        private List<AbstractArgument> mArguments;
        
        public boolean isContextItemRequired(XPathOperationOrFuntion func) {
            return false;
        }

        public QName getName() {
            return new QName(SUN_EXT_FUNC_NS, "date-less-than"); // NOI18N
        }

        public Icon getIcon() {
            return IconLoader.getIcon(getName().getLocalPart(), IMAGE_FOLDER_NAME);
        }
        
        public String getDisplayName() {
            return NbBundle.getMessage(BpelXPathExtFunctionMetadata.class, 
                            "DN_DateLessThan"); // NOI18N
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
                mArguments.add(ArgumentDescriptor.Predefined.SIMPLE_DATE_TIME_STRING);
                mArguments.add(ArgumentDescriptor.Predefined.SIMPLE_DATE_TIME_STRING);
            }
            return mArguments;
        }

        public XPathType getResultType() {
            return XPathType.BOOLEAN_TYPE;
        }

        public ResultTypeCalculator getResultTypeCalculator() {
            return null;
        }
        
        public String toStrign() {
            return getName().toString();
        }
        
    };
    
    ExtFunctionMetadata TIME_LT_METADATA = new ExtFunctionMetadata() {

        private List<AbstractArgument> mArguments;
        
        public boolean isContextItemRequired(XPathOperationOrFuntion func) {
            return false;
        }

        public QName getName() {
            return new QName(SUN_EXT_FUNC_NS, "time-less-than"); // NOI18N
        }

        public Icon getIcon() {
            return IconLoader.getIcon(getName().getLocalPart(), IMAGE_FOLDER_NAME);
        }
        
        public String getDisplayName() {
            return NbBundle.getMessage(BpelXPathExtFunctionMetadata.class, 
                            "DN_TimeLessThan"); // NOI18N
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
                mArguments.add(ArgumentDescriptor.Predefined.SIMPLE_DATE_TIME_STRING);
                mArguments.add(ArgumentDescriptor.Predefined.SIMPLE_DATE_TIME_STRING);
            }
            return mArguments;
        }

        public XPathType getResultType() {
            return XPathType.BOOLEAN_TYPE;
        }

        public ResultTypeCalculator getResultTypeCalculator() {
            return null;
        }
        
        public String toStrign() {
            return getName().toString();
        }
        
    };
}
