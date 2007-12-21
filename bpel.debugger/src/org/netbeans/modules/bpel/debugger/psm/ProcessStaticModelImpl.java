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

package org.netbeans.modules.bpel.debugger.psm;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import javax.xml.namespace.QName;
import org.netbeans.modules.bpel.debugger.api.psm.ProcessStaticModel;
import org.netbeans.modules.bpel.debugger.bdiclient.impl.BpelProcessImpl;
import org.netbeans.modules.bpel.debuggerbdi.rmi.api.XMLElementRef;

/**
 *
 * @author Alexander Zgursky
 * @author Kirill Sorokin
 */
public class ProcessStaticModelImpl implements ProcessStaticModel {
    
    private transient Map<String, AbstractBuilder> myBuilders;
    private final BpelProcessImpl myBpelProcess;
    private PsmEntityImpl myRoot;

    public static ProcessStaticModelImpl build(
            final BpelProcessImpl bpelProcess) {
        
        final ProcessStaticModelImpl psm = 
                new ProcessStaticModelImpl(bpelProcess);
        
        psm.build();
        return psm;
    }
    
    private ProcessStaticModelImpl(
            final BpelProcessImpl bpelProcess) {
        
        myBpelProcess = bpelProcess;
    }
    
    public QName getProcessQName() {
        return myBpelProcess.getQName();
    }
    
    public PsmEntityImpl getRoot() {
        return myRoot;
    }
    
    public PsmEntityImpl find(
            final String xpath) {
        
        if (myRoot == null) {
            return null;
        } else if (!xpath.startsWith("/")) {
            return null;
        }
        
        final StringTokenizer tokenizer = 
                new StringTokenizer(xpath, "/", false);
        
        PsmEntityImpl currentEntity = null;
        while (tokenizer.hasMoreTokens()) {
            final String token = tokenizer.nextToken();
            final String noPrefix = token.substring(token.indexOf(':') + 1);
            
            final int index;
            final String localPart;
            if (noPrefix.endsWith("]")) {
                final int i = noPrefix.indexOf('[');
                
                localPart = noPrefix.substring(0, i);
                index = Integer.parseInt(
                        noPrefix.substring(i + 1, noPrefix.length() - 1));
            } else {
                localPart = noPrefix;
                index = 1;
            }
            
            if ((currentEntity == null) && 
                    localPart.equals("process") && (index == 1)) {
                currentEntity = myRoot;
                continue;
            }
            
            if (currentEntity == null) {
                return null;
            }
            
            int foundIndex = 0;
            for (PsmEntityImpl child : currentEntity.getChildren()) {
                if (child.getQName().getLocalPart().equals(localPart)) {
                    foundIndex++;
                    if (foundIndex == index) {
                        currentEntity = child;
                        break;
                    }
                }
            }
            
            if (foundIndex != index) {
                return null;
            }
        }
        
        return currentEntity;
    }
    
    private void build() {
        final XMLElementRef rootRef = 
                myBpelProcess.getProcessRef().getXMLElement();
        
        assert "process".equals(rootRef.getLocalName());
        
        initBuilders();
        
        final AbstractBuilder processBuilder = new EntityBuilder();
        myRoot = processBuilder.build(null, rootRef, 1);
        myBuilders = null;
    }
    
    private void initBuilders() {
        final ActivityBuilder activityBuilder = 
                new ActivityBuilder();
        final LoopActivityBuilder loopActivityBuilder = 
                new LoopActivityBuilder();
        final EventHandlerBuilder eventHandlerBuilder = 
                new EventHandlerBuilder();
        final EventHandlersContainerBuilder eventHandlersContainerBuilder = 
                new EventHandlersContainerBuilder();
        final EntityBuilder entityBuilder = 
                new EntityBuilder();
        
        myBuilders = new HashMap<String, AbstractBuilder>();
        
        myBuilders.put("assign", activityBuilder);
        myBuilders.put("catch", entityBuilder);
        myBuilders.put("catchAll", entityBuilder);
        myBuilders.put("compensate", activityBuilder);
        myBuilders.put("compensateScope", activityBuilder);
        myBuilders.put("compensationHandler", entityBuilder);
        myBuilders.put("empty", activityBuilder);
        myBuilders.put("exit", activityBuilder);
        myBuilders.put("flow", activityBuilder);
        myBuilders.put("forEach", loopActivityBuilder);
        myBuilders.put("if", activityBuilder);
        myBuilders.put("invoke", activityBuilder);
        myBuilders.put("onMessage", entityBuilder);
        myBuilders.put("pick", activityBuilder);
        myBuilders.put("receive", activityBuilder);
        myBuilders.put("reply", activityBuilder);
        myBuilders.put("repeatUntil", loopActivityBuilder);
        myBuilders.put("rethrow", activityBuilder);
        myBuilders.put("scope", activityBuilder);
        myBuilders.put("sequence", activityBuilder);
        myBuilders.put("terminate", activityBuilder);
        myBuilders.put("terminationHandler", entityBuilder);
        myBuilders.put("throw", activityBuilder);
        myBuilders.put("validate", activityBuilder);
        myBuilders.put("wait", activityBuilder);
        myBuilders.put("while", loopActivityBuilder);
        
        myBuilders.put("condition", entityBuilder);
        myBuilders.put("copy", entityBuilder);
        myBuilders.put("else", entityBuilder);
        myBuilders.put("elseif", entityBuilder);
        
        myBuilders.put("faultHandlers", entityBuilder);
        
        myBuilders.put("eventHandlers", eventHandlersContainerBuilder);
        myBuilders.put("eventHandlers.onEvent", eventHandlerBuilder);
        myBuilders.put("eventHandlers.onAlarm", eventHandlerBuilder);
    }
    
    private abstract class AbstractBuilder {
        public PsmEntityImpl build(
                final PsmEntityImpl psmParent, 
                final XMLElementRef xmlElementRef, 
                final int tagIndex) {
            
            final String localName = xmlElementRef.getLocalName();
            final String name = xmlElementRef.getNameAttribute();
            final QName qName = new QName(BPEL_NAMESPACE_URI, localName);
            
            final String parentXpath = 
                    psmParent != null ? psmParent.getXpath() : "";
            final String xpath = parentXpath + "/" + 
                    BPEL_NAMESPACE_PREFIX + ":" + 
                    localName + "[" + tagIndex + "]";
            
            final PsmEntityImpl psmEntity = createPsmEntity(xpath, qName, name);
            if (psmParent != null) {
                psmParent.addChild(psmEntity);
            }
            
            buildChildren(psmEntity, xmlElementRef);
            
            return psmEntity;
        }
        
        protected void buildChildren(
                final PsmEntityImpl psmParent, 
                final XMLElementRef xmlElementRef) {
            
            final int childrenCount = xmlElementRef.getChildrenCount();
            
            XMLElementRef eventHandlers = null;
            XMLElementRef faultHandlers = null;
            
            Map<String, Integer> tagIndexes = new HashMap<String, Integer>();
            for (int i = 0; i < childrenCount; i++) {
                final XMLElementRef childRef = xmlElementRef.getChild(i);
                final String localName = childRef.getLocalName();
                
                if ("eventHandlers".equals(localName)) {
                    eventHandlers = childRef;
                    continue;
                }
                
                if ("faultHandlers".equals(localName)) {
                    faultHandlers = childRef;
                    continue;
                }
                
                final AbstractBuilder builder = myBuilders.get(localName);
                if (builder == null) {
                    continue;
                }
                
                final int tagIndex;
                if (tagIndexes.containsKey(localName)) {
                    tagIndex = tagIndexes.get(localName) + 1;
                } else {
                    tagIndex = 1;
                }
                tagIndexes.put(localName, tagIndex);
                
                builder.build(psmParent, childRef, tagIndex);
            }
            
            if (faultHandlers != null) {
                myBuilders.get("faultHandlers").build(
                        psmParent, faultHandlers, 1);
            }
            
            if (eventHandlers != null) {
                myBuilders.get("eventHandlers").build(
                        psmParent, eventHandlers, 1);
            }
        }
        
        protected abstract PsmEntityImpl createPsmEntity(
                final String xpath, 
                final QName qName, 
                final String name);
    }
    
    private class ActivityBuilder extends AbstractBuilder {
        protected PsmEntityImpl createPsmEntity(
                final String xpath, 
                final QName qName, 
                final String name) {
            
            return new PsmEntityImpl(xpath, qName, name, true, false, 
                    ProcessStaticModelImpl.this);
        }
    }
    
    private class LoopActivityBuilder extends AbstractBuilder {
        protected PsmEntityImpl createPsmEntity(
                final String xpath, 
                final QName qName, 
                final String name) {
            
            return new PsmEntityImpl(xpath, qName, name, true, true,
                    ProcessStaticModelImpl.this);
        }
    }
    
    private class EventHandlerBuilder extends AbstractBuilder {
        protected PsmEntityImpl createPsmEntity(
                final String xpath, 
                final QName qName, 
                final String name) {
            
            return new PsmEntityImpl(xpath, qName, name, false, true,
                    ProcessStaticModelImpl.this);
        }
    }
    
    private class EntityBuilder extends AbstractBuilder {
        protected PsmEntityImpl createPsmEntity(
                final String xpath, 
                final QName qName, 
                final String name) {
            
            return new PsmEntityImpl(xpath, qName, name, false, false,
                    ProcessStaticModelImpl.this);
        }
    }
    
    private class EventHandlersContainerBuilder extends EntityBuilder {
        @Override
        protected void buildChildren(
                final PsmEntityImpl psmParent, 
                final XMLElementRef xmlElementRef) {
            
            final int childrenCount = xmlElementRef.getChildrenCount();
            
            Map<String, Integer> tagIndexes = new HashMap<String, Integer>();
            for (int i = 0; i < childrenCount; i++) {
                final XMLElementRef childRef = xmlElementRef.getChild(i);
                final String localName = childRef.getLocalName();
                
                final AbstractBuilder builder = 
                        myBuilders.get("eventHandlers." + localName);
                if (builder == null) {
                    continue;
                }
                
                final int tagIndex;
                if (tagIndexes.containsKey(localName)) {
                    tagIndex = tagIndexes.get(localName) + 1;
                } else {
                    tagIndex = 1;
                }
                tagIndexes.put(localName, tagIndex);
                
                builder.build(psmParent, childRef, tagIndex);
            }
        }
    }
}
