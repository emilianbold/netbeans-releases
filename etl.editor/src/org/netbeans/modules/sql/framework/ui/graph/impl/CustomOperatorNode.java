package org.netbeans.modules.sql.framework.ui.graph.impl;

import java.awt.datatransfer.Transferable;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.Icon;

import org.netbeans.modules.sql.framework.model.SQLOperatorArg;
import org.netbeans.modules.sql.framework.ui.graph.IOperatorField;
import org.netbeans.modules.sql.framework.ui.graph.IOperatorXmlInfo;

import com.sun.etl.jdbc.SQLUtils;

/**
 * Implements IOperatorXmlInfo, but will be used as model for UserFunctions w/o persisting
 * each of user's customization in Layer file.
 * 
 * @author Girish Patil
 * @version $Revision$
 */
public class CustomOperatorNode implements IOperatorXmlInfo {
    private IOperatorXmlInfo delegate = null;
    private int inputCount = 0;
    private List inputList = new ArrayList();
    private List outputList = new ArrayList();
    private Map inputFieldMap = new HashMap();

    private CustomOperatorNode(IOperatorXmlInfo delegate){
    	this.delegate = delegate;
    }
/**
    public CustomOperatorNode(IOperatorXmlInfo delegate, Map inputMap) {
    	this(delegate);

    	if ((inputMap != null) && (inputMap.size() > 0)) {
			this.inputCount = inputMap.size();
			Iterator itr = inputMap.values().iterator();
			IOperatorField templateField = delegate.getInputField("arg1");
			IOperatorField newField = null;

			int cnt = 1;
			String argName = null;
			String argDisplayName = null;
			String argToolTip = null;

			if (templateField != null) {
				argDisplayName = templateField.getDisplayName();
				argToolTip = templateField.getToolTip();
			}

			while (itr.hasNext()) {
				itr.next();
				argName = "arg" + cnt;

				if (templateField != null) {
					newField = new OperatorFieldNode(argName, argDisplayName);
					newField.setToolTip(argToolTip);
				} else {
					newField = new OperatorFieldNode(argName, argName);
				}

				newField.setEditable(false);
				this.inputList.add(newField);
				this.inputFieldMap.put(argName, newField);
				cnt++;
			}
		}
    }*/

    public CustomOperatorNode(IOperatorXmlInfo delegate, List inputArgs,
                              SQLOperatorArg retType) {
        this(delegate);

        if ((inputArgs != null) && (inputArgs.size() > 0)) {
            this.inputCount = inputArgs.size();
            Iterator itr = inputArgs.iterator();
            IOperatorField templateField = delegate.getInputField("arg1");
            IOperatorField newField = null;

            int cnt = 1;
            String argName = null;
            String argDisplayName = null;
            String argToolTip = null;

            if (templateField != null) {
                //argDisplayName = templateField.getDisplayName();
                argToolTip = templateField.getToolTip();
            }

            while (itr.hasNext()) {
                SQLOperatorArg inputArg = (SQLOperatorArg) itr.next();
                argName = inputArg.getArgName(); // "arg" + cnt;
                argDisplayName = argName + " (" + SQLUtils.getStdSqlType(inputArg.getJdbcType()) + ')';
                if (templateField != null) {
                    newField = new OperatorFieldNode(argName, argDisplayName);
                    newField.setToolTip(argToolTip);
                } else {
                    newField = new OperatorFieldNode(argName, argName);
                }

                newField.setEditable(false);
                this.inputList.add(newField);
                this.inputFieldMap.put(argName, newField);
                cnt++;
            }
            String retTypeStr = SQLUtils.getStdSqlType(retType.getJdbcType());
            IOperatorField outputField = new OperatorFieldNode(retType.getArgName(), retType.getArgName() + " (" + retTypeStr + ')');
            outputField.setAttributeValue("retTypeStr",retTypeStr);
            this.outputList.add(outputField);
        }
    }

    public void addPropertyChangeListener(PropertyChangeListener l) {
        delegate.addPropertyChangeListener(l);
    }

    public Object getAttributeValue(String name) {
        return delegate.getAttributeValue(name);
    }

    public String getDisplayName() {
        return delegate.getDisplayName();
    }

    public Icon getIcon() {
        return delegate.getIcon();
    }

    public int getInputCount() {
        return this.inputCount;
    }

    public IOperatorField getInputField(String name) {
        return (IOperatorField) this.inputFieldMap.get(name);
    }

    public List getInputFields() {
        return this.inputList;
    }

    public String getName() {
        return delegate.getName();
    }

    public String getObjectClassName() {
        return delegate.getObjectClassName();
    }

    public int getOutputCount() {
        return delegate.getOutputCount();
    }

    public List getOutputFields() {
        return this.outputList; // delegate.getOutputFields();
    }

    public int getToolbarType() {
        return delegate.getToolbarType();
    }

    public String getToolTip() {
        return delegate.getToolTip();
    }

    public Transferable getTransferable() {
        return delegate.getTransferable();
    }

    public boolean isChecked() {
        return delegate.isChecked();
    }

    public boolean isJavaOperator() {
        return delegate.isJavaOperator();
    }

    public boolean isShowParenthesis() {
        return delegate.isShowParenthesis();
    }

    public void removePropertyChangeListener(PropertyChangeListener l) {
        delegate.removePropertyChangeListener(l);
    }

    public void setAttributeValue(String attrName, Object val) {
        delegate.setAttributeValue(attrName, val);
    }

    public void setChecked(boolean checked) {
        delegate.setChecked(checked);
    }

    public void setDropInstance(boolean dropped) {
        delegate.setDropInstance(dropped);
    }
}
