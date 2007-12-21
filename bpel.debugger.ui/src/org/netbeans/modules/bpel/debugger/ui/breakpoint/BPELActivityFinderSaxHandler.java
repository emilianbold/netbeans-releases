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


package org.netbeans.modules.bpel.debugger.ui.breakpoint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;


/**
 * A Handler that construct BPEL node tree and finds all the Activities
 * @author Sun Microsystems
 *
 */
public class BPELActivityFinderSaxHandler extends DefaultHandler {
    
    private List mAllNodes = new ArrayList();
    private BPELNode mCurrentNode;
    private Locator mLocator;
    private String mTargetNS;
    private BPELNode mFirstAct;
    private BPELNode mLastAct;
    private int mBreakpointLine;
    private BPELNode mFindingNode;
    private boolean found;
    
    
    public BPELActivityFinderSaxHandler(int lineNumber) {
        mBreakpointLine = lineNumber;
    }

    public List getAllNodes() {
        return mAllNodes;
    }

    @Override
    public void endElement(
            final String uri, 
            final String localName, 
            final String qName) throws SAXException {
        
        mCurrentNode.setClosingNumber(mLocator.getLineNumber());
        if (!found && 
                (mFindingNode == mCurrentNode) && 
                (mCurrentNode.getClosingNumber() >= mBreakpointLine))  {
            found = true;
        }
        
        mCurrentNode = mCurrentNode.getParent();
        if (!found) {
            mFindingNode = mCurrentNode;
        }
    }

    @Override
    public void setDocumentLocator(
            final Locator locator) {
        mLocator = locator;
    }

    @Override
    public void startElement(
            final String uri, 
            final String localName, 
            final String qName, 
            final Attributes attributes) throws SAXException {
            
        if (mCurrentNode == null) {
            for (int i = 0; i < attributes.getLength(); i++) {
                if (attributes.getLocalName(i).equals ("targetNamespace")) {
                    mTargetNS = attributes.getValue(i);
                }
            }            
        }
        
        final BPELNode.BPELNodeType nodeType = BPELNode.getNodeType(localName);
        final BPELNode bpelNode = new BPELNode (
                localName, 
                mLocator.getLineNumber(), 
                mTargetNS, 
                nodeType, 
                mCurrentNode);
        
        if (mCurrentNode != null) {
            mCurrentNode.addChild(bpelNode);
        }
        
        mAllNodes.add(bpelNode);
        
        if (bpelNode.isActivity() && (mFirstAct == null)) {
            mFirstAct = bpelNode;
        } else if (bpelNode.isActivity()) {
            mLastAct = bpelNode;
        }
        
        mCurrentNode = bpelNode;
        
        if(!found && (mCurrentNode.getLineNumber() < mBreakpointLine)) {
            mFindingNode = mCurrentNode;
        } else if (!found && (mCurrentNode.getLineNumber() >= mBreakpointLine)) {
            if (mCurrentNode.getParent() == mFindingNode) {
                mFindingNode = mCurrentNode;
            }
            
            found = true;
        }
    }
    
    public BPELNode getFirstActivity() {
        return mFirstAct;
    }
    
    public BPELNode getLastActivity() {
        return mLastAct;
    }
    
    public HashMap getAllActMap() {
        final LinkedHashMap activityLineXpathMap = new LinkedHashMap();
        
        for (Iterator it = mAllNodes.iterator(); it.hasNext();) {
            final BPELNode bpelNode = (BPELNode) it.next();
            activityLineXpathMap.put(
                    new Integer(bpelNode.getLineNumber()), bpelNode);
        }
        
        return activityLineXpathMap;
    }
    
    public BPELNode getFoundNode() {
        return mFindingNode;
    }
}
