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
package org.netbeans.modules.bpel.properties.props.editors;

import java.awt.Component;
import java.beans.PropertyEditorSupport;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.netbeans.modules.bpel.model.api.BaseScope;
import org.netbeans.modules.bpel.model.api.BpelContainer;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.CompensationHandlerHolder;
import org.netbeans.modules.bpel.model.api.references.BpelReference;
import org.netbeans.modules.bpel.model.api.references.ReferenceCollection;
import org.netbeans.modules.bpel.model.spi.FindHelper;
import org.netbeans.modules.bpel.nodes.BpelNode;
import org.netbeans.modules.bpel.properties.Constants;
import org.netbeans.modules.xml.xam.Named;
import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.util.Lookup;

/**
 *
 * @author nk160297
 */
public class CompensationTargetEditor extends PropertyEditorSupport
        implements ExPropertyEditor {
    
    private PropertyEnv myPropertyEnv = null;
    private List<CompensationHandlerHolder> chHoldersList;
    
    /** Creates a new instance of ModelReferenceEditor */
    public CompensationTargetEditor() {
    }
    
    public String getAsText() {
        BpelReference<CompensationHandlerHolder> valRef =
                (BpelReference<CompensationHandlerHolder>)getValue();
        if (valRef != null) {
            CompensationHandlerHolder chHolder = valRef.get();
            if (chHolder != null && chHolder instanceof Named) {
                String result = ((Named)chHolder).getName();
                return result;
            }
        }
        return Constants.NOT_ASSIGNED;
    }
    
    public Component getCustomEditor() {
        return null;
    }
    
    public void setAsText(String text) throws IllegalArgumentException {
        CompensationHandlerHolder chHolder = findChHolderByName(text);
        BpelReference<CompensationHandlerHolder> chHolderRef = null;
        if (myPropertyEnv != null) {
            Object[] beans = myPropertyEnv.getBeans();
            BpelNode node = (BpelNode)beans[0];
            Object obj = node.getReference();
            if (obj instanceof ReferenceCollection) {
                chHolderRef = ((ReferenceCollection)obj).
                        createReference(chHolder, CompensationHandlerHolder.class);
            }
        }
        setValue(chHolderRef);
    }
    
    public String[] getTags() {
        List<String> list = getChHolderNamesList();
        return list.toArray(new String[list.size()]);
    }
    
    public Object getValue() {
        Object obj = super.getValue();
        if (obj != null) {
            assert obj instanceof BpelReference;
        }
        return obj;
    }
    
    public void setValue(Object newValue) {
        if (newValue != null) {
            assert newValue instanceof BpelReference;
        }
        super.setValue(newValue);
    }
    
    public void attachEnv(PropertyEnv propertyEnv) {
        boolean envChanged = false;
        //
        // Check if the environment has changed
        if (propertyEnv.equals(myPropertyEnv)) {
            Object[] beans = propertyEnv.getBeans();
            BpelNode newNode = (BpelNode)beans[0];
            //
            beans = myPropertyEnv.getBeans();
            BpelNode oldNode = (BpelNode)beans[0];
            //
            if (!newNode.equals(oldNode)) {
                envChanged = true;
            }
        } else {
            envChanged = true;
        }
        //
        myPropertyEnv = propertyEnv;
        if (envChanged) {
            chHoldersList = null;
        }
    }
    
    private CompensationHandlerHolder findChHolderByName(String name) {
        List<CompensationHandlerHolder> chhList = getChHoldersList();
        for (CompensationHandlerHolder chHolder : chhList) {
            if (chHolder instanceof Named) {
                String chHolderName = ((Named)chHolder).getName();
                if (name.equals(chHolderName)) {
                    return chHolder;
                }
            } else {
                assert false : "The CompensationHandlerHolder should be Named!";
            }
        }
        return null;
    }
    
    private List<CompensationHandlerHolder> getChHoldersList() {
        if (chHoldersList == null ) {
            chHoldersList = new ArrayList<CompensationHandlerHolder>();
            //
            if (myPropertyEnv != null) {
                Object[] beans = myPropertyEnv.getBeans();
                BpelNode node = (BpelNode)beans[0];
                Lookup lookup = node.getLookup();
                //
                Object obj = node.getReference();
                if (obj instanceof BpelEntity) {
                    BpelEntity entity = (BpelEntity)obj;
                    FindHelper helper =
                            (FindHelper)Lookup.getDefault().lookup(FindHelper.class);
                    Iterator<BaseScope> itr = helper.scopeIterator(entity);
                    //
                    // Get upper nearest scope
                    BaseScope baseScope = itr.next();
                    //
                    populateChHoldersList(baseScope, chHoldersList);
                }
            }
        }
        //
        return chHoldersList;
    }
    
    private List<String> getChHolderNamesList() {
        ArrayList<String> resultList = new ArrayList<String>();
        //
        List<CompensationHandlerHolder> chhList = getChHoldersList();
        for (CompensationHandlerHolder chHolder : chhList) {
            if (chHolder instanceof Named) {
                String name = ((Named)chHolder).getName();
                resultList.add(name);
            }
        }
        //
        return resultList;
    }
    
    // Recursive method for looking for the set of nearest nested
    // CompensationHandlerHolder entities.
    private void populateChHoldersList(BpelContainer container,
            List<CompensationHandlerHolder> chhList) {
        for (BpelEntity child : container.getChildren()) {
            if (child instanceof CompensationHandlerHolder) {
                chhList.add((CompensationHandlerHolder)child);
            } else if (child instanceof BpelContainer) {
                populateChHoldersList((BpelContainer)child, chhList);
            }
        }
    }
}
