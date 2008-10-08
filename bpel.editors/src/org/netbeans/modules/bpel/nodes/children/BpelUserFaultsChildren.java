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

package org.netbeans.modules.bpel.nodes.children;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import org.netbeans.modules.bpel.editors.api.nodes.NodeType;

import org.netbeans.modules.bpel.model.api.BpelContainer;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.support.BpelModelVisitorAdaptor;
import org.netbeans.modules.bpel.model.api.BpelContainer;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.Catch;
import org.netbeans.modules.bpel.model.api.CompensationHandler;
import org.netbeans.modules.bpel.model.api.Else;
import org.netbeans.modules.bpel.model.api.ElseIf;
import org.netbeans.modules.bpel.model.api.EventHandlers;
import org.netbeans.modules.bpel.model.api.FaultHandlers;
import org.netbeans.modules.bpel.model.api.Flow;
import org.netbeans.modules.bpel.model.api.ForEach;
import org.netbeans.modules.bpel.model.api.If;
import org.netbeans.modules.bpel.model.api.Invoke;
import org.netbeans.modules.bpel.model.api.OnAlarmEvent;
import org.netbeans.modules.bpel.model.api.OnAlarmPick;
import org.netbeans.modules.bpel.model.api.OnEvent;
import org.netbeans.modules.bpel.model.api.OnMessage;
import org.netbeans.modules.bpel.model.api.Pick;
import org.netbeans.modules.bpel.model.api.Process;
import org.netbeans.modules.bpel.model.api.RepeatUntil;
import org.netbeans.modules.bpel.model.api.Reply;
import org.netbeans.modules.bpel.model.api.Scope;
import org.netbeans.modules.bpel.model.api.Sequence;
import org.netbeans.modules.bpel.model.api.TerminationHandler;
import org.netbeans.modules.bpel.model.api.Throw;
import org.netbeans.modules.bpel.model.api.While;
import org.netbeans.modules.bpel.model.ext.Extensions;
import org.netbeans.modules.bpel.nodes.BpelNode;
import org.netbeans.modules.soa.ui.nodes.NodeFactory;


import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

/**
 * Supports the sorted by name list of Fault Nodes which correspods
 * to faults declared at the specified BPEL file
 *
 * @author nk160297
 */
public class BpelUserFaultsChildren extends Children.Keys {
    
    private Lookup myLookup;
    private FaultNameStandardChildren mStandardFaultsChildren;
    private WsdlImportsChildren mWsdlImportFaultsChildren;
    
    @SuppressWarnings("unchecked")
    public BpelUserFaultsChildren(Process process, Lookup lookup, 
            FaultNameStandardChildren standardFaultsChildren, 
            WsdlImportsChildren wsdlImportFaultsChildren) {
        myLookup = lookup;
        mStandardFaultsChildren = standardFaultsChildren;
        mWsdlImportFaultsChildren = wsdlImportFaultsChildren;
        //
        if (process != null) {
            setKeys(new Object[] {process});
        }
    }
    
    protected Node[] createNodes(Object key) {
        if (!(key instanceof Process)) {
            return null;
        }
        Process process = (Process)key;
        //
        SearchFaultVisitor sfVisitor = new SearchFaultVisitor();
        process.accept(sfVisitor);
        HashSet<QName> allUsedFaultNames = sfVisitor.getAllUsedFaultNames();
        //
        Set<QName> standardFaultNames = 
                mStandardFaultsChildren.getStandardFaultNames();
        allUsedFaultNames.removeAll(standardFaultNames);
        //
         Set<QName> systemFault = new HashSet<QName>();
        systemFault.add( new QName(Extensions.ERROR_EXT_URI, 
                Extensions.SYSTEM_FAULT_NAME)); 
        allUsedFaultNames.removeAll(systemFault);
        //
        Set<QName> wsdlFaultNames = 
                mWsdlImportFaultsChildren.getImportedWsdlFaultNames();
        allUsedFaultNames.removeAll(wsdlFaultNames);
        //
        ArrayList<Node> nodesList = new ArrayList<Node>();
        //
        NodeFactory nodeFactory = myLookup.lookup(NodeFactory.class);
        for (QName faultQName : allUsedFaultNames) {
            @SuppressWarnings("unchecked")
            Node newNode = nodeFactory.createNode(
                    NodeType.FAULT, faultQName, myLookup);
            nodesList.add(newNode);
        }
        //
        BpelNode.DisplayNameComparator comparator =
                new BpelNode.DisplayNameComparator();
        Collections.sort(nodesList, comparator);
        Node[] resultNodes = nodesList.toArray(new Node[nodesList.size()]);
        return resultNodes;
    }
 
    /**
     * This visitor traverses over the bpel model and collects values of 
     * all attributes "faultName". It looks them in Catch, Throw and Reply 
     * entities. 
     */ 
    private class SearchFaultVisitor extends BpelModelVisitorAdaptor {

        private HashSet<QName> mFaultNameSet = new HashSet<QName>();

        public HashSet<QName> getAllUsedFaultNames() {
            return mFaultNameSet;
        }
        
        protected void visitChildren(BpelContainer parent) {
            List<BpelEntity> children = parent.getChildren();
            for (BpelEntity entity : children) {
                entity.accept(this);
            }
        }

        protected void addFaultName(BpelEntity owner, QName newFaultName) {
            if (newFaultName == null) {
                return;
            }
            //
            // The namespace URI has to be specified because it is used 
            // while putting a new element to the HashSet.
            String nsUri = newFaultName.getNamespaceURI();
            if (nsUri == null || nsUri.length() == 0) {
                // try resolve nsUri by the prefix
                String prefix = newFaultName.getPrefix();
                NamespaceContext nsContext = owner.getNamespaceContext();
                assert nsContext != null;
                //
                String newNsUri = nsContext.getNamespaceURI(prefix);
                boolean isTheSameNsUri = false;
                if (newNsUri == null) {
                    isTheSameNsUri = (nsUri == null);
                } else {
                    isTheSameNsUri = newNsUri.equals(nsUri);
                }
                //    
                if (!isTheSameNsUri) {
                    newFaultName = new QName(newNsUri, 
                            newFaultName.getLocalPart(), prefix);
                }
            }
            //
            mFaultNameSet.add(newFaultName);
        }
        
        //======================================================================
        
        @Override
        public void visit( Catch aCatch ) {
            QName faultName = aCatch.getFaultName();
            addFaultName(aCatch, faultName);
            //
            visitChildren(aCatch);
        }

        @Override
        public void visit( Throw aThrow ) {
            QName faultName = aThrow.getFaultName();
            addFaultName(aThrow, faultName);
        }

        @Override
        public void visit( Reply reply ) {
            QName faultName = reply.getFaultName();
            addFaultName(reply, faultName);
        }
        
        //======================================================================
        // visit bpel containers
        //======================================================================

        @Override
        public void visit( Process process ) {
            visitChildren(process);
        }
        
        @Override
        public void visit( Scope scope ) {
            visitChildren(scope);
        }
        
        @Override
        public void visit( Flow flow ) {
            visitChildren(flow);
        }
        
        @Override
        public void visit( Sequence seq ) {
            visitChildren(seq);
        }
        
        @Override
        public void visit( If iff ) {
            visitChildren(iff);
        }
        
        @Override
        public void visit( Else els ) {
            visitChildren(els);
        }

        @Override
        public void visit( ElseIf elseIf ) {
            visitChildren(elseIf);
        }

        @Override
        public void visit( While whil ) {
            visitChildren(whil);
        }
        
        @Override
        public void visit( RepeatUntil repeatUntil ) {
            visitChildren(repeatUntil);
        }
        
        @Override
        public void visit( ForEach forEach ) {
            visitChildren(forEach);
        }
        
        @Override
        public void visit( Pick pick ) {
            visitChildren(pick);
        }
        
        @Override
        public void visit( OnMessage message ) {
            visitChildren(message);
        }

        @Override
        public void visit( OnAlarmPick alarmPick ) {
            visitChildren(alarmPick);
        }

        @Override
        public void visit( EventHandlers handlers ) {
            visitChildren(handlers);
        }

        @Override
        public void visit( OnEvent event ) {
            visitChildren(event);
        }

        @Override
        public void visit( OnAlarmEvent alarmEvent ) {
            visitChildren(alarmEvent);
        }

        @Override
        public void visit( FaultHandlers handlers ) {
            visitChildren(handlers);
        }

        @Override
        public void visit( Invoke invoke ) {
            visitChildren(invoke);
        }
        
        @Override
        public void visit( CompensationHandler handler ) {
            visitChildren(handler);
        }

        @Override
        public void visit( TerminationHandler handler ) {
            visitChildren(handler);
        }

    }
    
}
