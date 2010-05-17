/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
