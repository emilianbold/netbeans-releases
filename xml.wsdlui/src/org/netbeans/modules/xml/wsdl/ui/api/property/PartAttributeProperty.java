/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.xml.wsdl.ui.api.property;

import java.beans.PropertyEditor;
import java.util.ArrayList;
import java.util.Collection;

import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.xam.ui.XAMUtils;
import org.openide.nodes.PropertySupport;

/**
 *
 * @author radval
 *
 */
public class PartAttributeProperty extends PropertySupport.Reflection {
	
    private MessageProvider messageProv;

    private WSDLModel model;
    
    private boolean isMultiSelect = true;
    
    private ExtensibilityElementPropertyAdapter adapter;
	public PartAttributeProperty(MessageProvider messageProv, WSDLModel model, ExtensibilityElementPropertyAdapter instance, Class valueType, String getter, String setter, boolean isMultiSelect) throws NoSuchMethodException {
		super(instance, valueType, getter, setter);
        this.messageProv = messageProv; 
        this.model = model;
        this.isMultiSelect = isMultiSelect;
        adapter = instance;
	}
	
	@Override
    public PropertyEditor getPropertyEditor() {
		String[] parts = getAllMessageParts();
        if (isMultiSelect) {
            return new PartsSelectorPropertyEditor(parts, adapter.getValue());
        }
        
        return new ComboBoxPropertyEditor(parts);
	}
	
	private String[] getAllMessageParts() {
    	ArrayList<String> messageList = new ArrayList<String>();
        Message message = PropertyUtil.getMessage(messageProv, model);
    	if (message != null) {
    	    if (!isMultiSelect && adapter.isOptional()) {
    	        messageList.add(adapter.getMessageForUnSet());
    	    }
    	    //first get all messages in current wsdl document
    	    messageList.addAll(getAllMessageParts(message));
    	}
    	
    	return messageList.toArray(new String[messageList.size()]);
    }
	
	
	
	private ArrayList<String> getAllMessageParts(Message msg) {
        ArrayList<String> allParts = new ArrayList<String>();
        
    	if (msg == null) return allParts;
        
    	Collection<Part> parts = msg.getParts();
    	if (parts != null) {
    	    for (Part part : parts) {
    	        allParts.add(part.getName());
    	    }
    	}
    	
    	return allParts;
    }
    
    @Override
    public boolean canWrite() {
        return XAMUtils.isWritable(model);
    }
	
}