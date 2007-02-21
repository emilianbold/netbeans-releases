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
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.StringTokenizer;
import javax.xml.namespace.QName;
import org.netbeans.modules.bpel.debugger.api.BpelProcess;
import org.netbeans.modules.bpel.debugger.api.EditorContextBridge;
import org.netbeans.modules.bpel.debugger.api.psm.ProcessStaticModel;
import org.netbeans.modules.bpel.debugger.bdiclient.impl.BpelProcessImpl;
import org.netbeans.modules.bpel.debuggerbdi.rmi.api.BPELProcessRef;
import org.netbeans.modules.bpel.debuggerbdi.rmi.api.XMLElementRef;

/**
 *
 * @author Alexander Zgursky
 */
public class ProcessStaticModelImpl implements ProcessStaticModel {
    
    private transient Map<String, AbstractBuilder> myBuilders;
    private final BpelProcessImpl myBpelProcess;
    private PsmEntityImpl myRoot;

    public static ProcessStaticModelImpl build(BpelProcessImpl bpelProcess) {
        ProcessStaticModelImpl psm = new ProcessStaticModelImpl(bpelProcess);
        psm.build();
        return psm;
    }
    
    /** Creates a new instance of ProcessStaticModelImpl */
    private ProcessStaticModelImpl(BpelProcessImpl bpelProcess) {
        myBpelProcess = bpelProcess;
    }
    
    public QName getProcessQName() {
        return myBpelProcess.getQName();
    }
    
    public PsmEntityImpl getRoot() {
        return myRoot;
    }
    
    public PsmEntityImpl find(String xpath) {
        if (myRoot == null) {
            return null;
        } else if (!xpath.startsWith("/")) {
            return null;
        }
        
        StringTokenizer tokenizer = new StringTokenizer(xpath, "/", false);
        PsmEntityImpl currentEntity = null;
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            String noPrefix = token.substring(token.indexOf(':') + 1);
            int index;
            String localPart;
            if (noPrefix.endsWith("]")) {
                int ind = noPrefix.indexOf('[');
                localPart = noPrefix.substring(0, ind);
                index = Integer.parseInt(noPrefix.substring(ind + 1, noPrefix.length() - 1));
            } else {
                localPart = noPrefix;
                index = 1;
            }
            
            if (currentEntity == null && localPart.equals("process") && index == 1) {
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
        XMLElementRef rootRef = myBpelProcess.getProcessRef().getXMLElement();
        assert "process".equals(rootRef.getLocalName());
        initBuilders();
        AbstractBuilder processBuilder = new EntityBuilder();
        myRoot = processBuilder.build(null, rootRef, 1);
        myBuilders = null;
    }
    
    private void initBuilders() {
        ActivityBuilder activityBuilder = new ActivityBuilder();
        LoopActivityBuilder loopActivityBuilder = new LoopActivityBuilder();
        EventHandlerBuilder eventHandlerBuilder = new EventHandlerBuilder();
        EventHandlersContainerBuilder eventHandlersContainerBuilder = new EventHandlersContainerBuilder();
        EntityBuilder entityBuilder = new EntityBuilder();
        
        myBuilders = new HashMap<String, AbstractBuilder>();
        
        myBuilders.put("empty", activityBuilder);
        myBuilders.put("invoke", activityBuilder);
        myBuilders.put("receive", activityBuilder);
        myBuilders.put("reply", activityBuilder);
        myBuilders.put("assign", activityBuilder);
        myBuilders.put("wait", activityBuilder);
        myBuilders.put("throw", activityBuilder);
        myBuilders.put("terminate", activityBuilder);
        myBuilders.put("flow", activityBuilder);
        myBuilders.put("while", loopActivityBuilder);
        myBuilders.put("sequence", activityBuilder);
        myBuilders.put("pick", activityBuilder);
        myBuilders.put("scope", activityBuilder);
        myBuilders.put("faultHandlers", entityBuilder);
        myBuilders.put("catch", entityBuilder);
        myBuilders.put("catchAll", entityBuilder);
        myBuilders.put("onMessage", entityBuilder);
        myBuilders.put("compensationHandler", entityBuilder);
        myBuilders.put("compensate", activityBuilder);
        myBuilders.put("else", entityBuilder);
        myBuilders.put("elseIf", entityBuilder);
        myBuilders.put("validate", activityBuilder);
        myBuilders.put("terminationHandler", entityBuilder);
        myBuilders.put("rethrow", activityBuilder);
        myBuilders.put("repeatUntil", loopActivityBuilder);
        myBuilders.put("onAlarm", entityBuilder); //pick
        myBuilders.put("forEach", loopActivityBuilder);
        myBuilders.put("if", activityBuilder);
        myBuilders.put("compensateScope", activityBuilder);
        
        myBuilders.put("eventHandlers", eventHandlersContainerBuilder);
        myBuilders.put("eventHandlers.onEvent", eventHandlerBuilder);
        myBuilders.put("eventHandlers.onAlarm", eventHandlerBuilder);
    }
    
    private abstract class AbstractBuilder {
        public PsmEntityImpl build(PsmEntityImpl psmParent, XMLElementRef xmlElementRef, int tagIndex) {
            String localName = xmlElementRef.getLocalName();
            String name = xmlElementRef.getNameAttribute();
            QName qName = new QName(BPEL_NAMESPACE_URI, localName);
            String parentXpath = psmParent != null ? psmParent.getXpath() : "";
            String xpath = parentXpath + "/" + BPEL_NAMESPACE_PREFIX + ":" + localName + "[" + tagIndex + "]";
            PsmEntityImpl psmEntity = createPsmEntity(xpath, qName, name);
            if (psmParent != null) {
                psmParent.addChild(psmEntity);
            }
            buildChildren(psmEntity, xmlElementRef);
            return psmEntity;
        }
        
        protected void buildChildren(PsmEntityImpl psmParent, XMLElementRef xmlElementRef) {
            int childrenCount = xmlElementRef.getChildrenCount();
            XMLElementRef eventHandlers = null;
            XMLElementRef faultHandlers = null;
            Map<String, Integer> tagIndexes = new HashMap<String, Integer>();
            for (int i = 0; i < childrenCount; i++) {
                XMLElementRef childRef = xmlElementRef.getChild(i);
                String localName = childRef.getLocalName();
                
                if ("eventHandlers".equals(localName)) {
                    eventHandlers = childRef;
                    continue;
                } else if ("faultHandlers".equals(localName)) {
                    faultHandlers = childRef;
                    continue;
                }
                
                AbstractBuilder builder = myBuilders.get(localName);
                if (builder == null) {
                    continue;
                }
                
                int tagIndex;
                if (tagIndexes.containsKey(localName)) {
                    tagIndex = tagIndexes.get(localName) + 1;
                } else {
                    tagIndex = 1;
                }
                tagIndexes.put(localName, tagIndex);
                
                builder.build(psmParent, childRef, tagIndex);
            }
            if (faultHandlers != null) {
                myBuilders.get("faultHandlers").build(psmParent, faultHandlers, 1);
            }
            if (eventHandlers != null) {
                myBuilders.get("eventHandlers").build(psmParent, eventHandlers, 1);
            }
        }
        
        protected abstract PsmEntityImpl createPsmEntity(String xpath, QName qName, String name);
    }
    
    private class ActivityBuilder extends AbstractBuilder {
        protected PsmEntityImpl createPsmEntity(String xpath, QName qName, String name) {
            return new PsmEntityImpl(xpath, qName, name, true, false);
        }
    }
    
    private class LoopActivityBuilder extends AbstractBuilder {
        protected PsmEntityImpl createPsmEntity(String xpath, QName qName, String name) {
            return new PsmEntityImpl(xpath, qName, name, true, true);
        }
    }
    
    private class EventHandlerBuilder extends AbstractBuilder {
        protected PsmEntityImpl createPsmEntity(String xpath, QName qName, String name) {
            return new PsmEntityImpl(xpath, qName, name, false, true);
        }
    }
    
    private class EntityBuilder extends AbstractBuilder {
        protected PsmEntityImpl createPsmEntity(String xpath, QName qName, String name) {
            return new PsmEntityImpl(xpath, qName, name, false, false);
        }
    }
    
    private class EventHandlersContainerBuilder extends EntityBuilder {
        protected void buildChildren(PsmEntityImpl psmParent, XMLElementRef xmlElementRef) {
            int childrenCount = xmlElementRef.getChildrenCount();
            Map<String, Integer> tagIndexes = new HashMap<String, Integer>();
            for (int i = 0; i < childrenCount; i++) {
                XMLElementRef childRef = xmlElementRef.getChild(i);
                String localName = childRef.getLocalName();
                AbstractBuilder builder = myBuilders.get("eventHandlers." + localName);
                if (builder == null) {
                    continue;
                }
                
                int tagIndex;
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
