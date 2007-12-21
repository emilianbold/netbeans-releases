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
import java.util.List;

/**
 * An abstract Node corresponding to each XML node when
 * a BPEL file is parsed. It includes the line number and XPath
 * information as well as the references to parent node and children node
 *
 * @author Sun Microsystems
 *
 */
public class BPELNode {

    /** "receive" element tag */
    public static final String RECEIVE = "receive";

    /** "reply" element tag */
    public static final String REPLY = "reply";
    
    /** "invoke" element tag */
    public static final String INVOKE = "invoke";
    
    /** "assign" element tag */
    public static final String ASSIGN = "assign";
    
    /** "throw" element tag */
    public static final String THROW = "throw";
    
    /** "exit" element tag */
    public static final String TERMINATE = "exit";
    
    /** "wait" element tag */
    public static final String WAIT = "wait";
    
    /** "empty" element tag */
    public static final String EMPTY = "empty";
    
    /** "sequence" element tag */
    public static final String SEQUENCE = "sequence";
    
    /** "switch" element tag */
    public static final String SWITCH = "switch";
    
    /** "if" element tag */
    public static final String IF = "if";
    
    /** "while" element tag */
    public static final String WHILE = "while";
    
    /** "repeatUntil element tag */
    public static final String REPEAT_UNTIL = "repeatUntil";
    
    /** "repeatUntil element tag */
    public static final String FOREACH = "forEach";
    
    /** "pick" element tag */
    public static final String PICK = "pick";
    
    /** "flow" element tag */
    public static final String FLOW = "flow";
    
    /** "scope" element tag */
    public static final String SCOPE = "scope";
    
    /** "compensate" element tag */
    public static final String COMPENSATE = "compensate";
    
    /** "compensateScope" element tag */
    public static final String COMPENSATE_SCOPE = "compensateScope";
    
    /** "rethrow" element tag */
    public static final String RETHROW = "rethrow";

    /** "validate" element tag */
    public static final String VALIDATE = "validate";

    
    
    private static HashMap ALL_ACTIVITIES_MAP;

    
    
    /**
     * NodeType class to ensure type safe comparison,
     * so that no equalsTo is required to indentify
     * what type the BPELNode is.
     *
     */
   static public final class BPELNodeType {
       
       private String mName;
       //Not an activity type
       private static final String NONE_ACTIVITY = "NoneActivity";
       /**
        * Private constructor to avoid creating an instance.
        * All instances are precreated.
        * 
        * @param nodeType The nodeType
        * 
        */
       private BPELNodeType (String nodeType) {
           mName = nodeType;
       }
       /**
        * The 'receive' activity
        */
       public static final BPELNodeType RECEIVE_TYPE = new BPELNodeType(RECEIVE);
       
       /**
        * The 'reply' activity
        */
       public static final BPELNodeType REPLY_TYPE = new BPELNodeType(REPLY);
       
       /**
        * The 'invoke' activity
        */
       public static final BPELNodeType INVOKE_TYPE = new BPELNodeType(INVOKE);       
       
       /**
        * The 'assign' activity
        */
       public static final BPELNodeType ASSIGN_TYPE = new BPELNodeType(ASSIGN);
       
       /**
        * The 'throw' activity
        */
       public static final BPELNodeType THROW_TYPE = new BPELNodeType(THROW);
       
       /**
        * The 'terminate' activity
        */       
       public static final BPELNodeType TERMINATE_TYPE = new BPELNodeType(TERMINATE); 
       
       /**
        * The 'wait' activity
        */
       public static final BPELNodeType WAIT_TYPE = new BPELNodeType(WAIT);
       
       /**
        * The 'empty' activity
        */
       public static final BPELNodeType EMPTY_TYPE = new BPELNodeType(EMPTY);
       
       /**
        * The 'sequence' activity
        */
       public static final BPELNodeType SEQUENCE_TYPE = new BPELNodeType(SEQUENCE);
       
       /**
        * The 'if' activity
        */
       public static final BPELNodeType IF_TYPE = new BPELNodeType(IF);
       
      
       /**
        * The 'switch' activity
        */
       public static final BPELNodeType SWITCH_TYPE = new BPELNodeType(SWITCH);
       
       /**
        * The 'repeatUntil' activity
        */
       public static final BPELNodeType REPEAT_UNTIL_TYPE = new BPELNodeType(REPEAT_UNTIL);       
       
       /**
        * The 'while' activity
        */
       public static final BPELNodeType WHILE_TYPE = new BPELNodeType(WHILE);
       
       /**
        * The 'forEach' activity
        */
       public static final BPELNodeType FOREACH_TYPE = new BPELNodeType(FOREACH);
       
       
       /**
        * The 'pick' activity
        */
       public static final BPELNodeType PICK_TYPE = new BPELNodeType(PICK);
       
       /**
        * The 'flow' activity
        */
       public static final BPELNodeType FLOW_TYPE = new BPELNodeType(FLOW);
       
       /**
        * The 'scope' activity
        */
       public static final BPELNodeType SCOPE_TYPE = new BPELNodeType(SCOPE);
       
       /**
        * The 'compensateScope' activity
        */
       public static final BPELNodeType COMPENSATE_SCOPE_TYPE = new BPELNodeType(COMPENSATE_SCOPE);
       
       /**
        * The 'rethrow' activity
        */
       public static final BPELNodeType RETHROW_TYPE = new BPELNodeType(RETHROW);
       /**
        * The 'validate' activity
        */
       public static final BPELNodeType VALIDATE_TYPE = new BPELNodeType(VALIDATE);
       /**
        * The 'compensate' activity
        */
       public static final BPELNodeType COMPENSATE_TYPE = new BPELNodeType(COMPENSATE);
       
       /**
        * Any node that is not the above mentioned types
        *
        */       
       public static final BPELNodeType NONE_ACTIVITY_TYPE = new BPELNodeType(NONE_ACTIVITY);

   
    public boolean equals(Object obj) {
        // TODO Auto-generated method stub
       if (obj instanceof BPELNodeType)
           return false;
       return ((BPELNodeType)obj).mName.equals(mName);
    }
    
    
    public int hashCode() {
        // TODO Auto-generated method stub
        return mName.hashCode();
    }
    
    public String toString() {
        // TODO Auto-generated method stub
        return "Type:" + mName;
    }
       
       
        
    }    
    
    static {
        ALL_ACTIVITIES_MAP = new HashMap();
        ALL_ACTIVITIES_MAP.put(RECEIVE, BPELNodeType.RECEIVE_TYPE);
        ALL_ACTIVITIES_MAP.put(REPLY, BPELNodeType.REPLY_TYPE);
        ALL_ACTIVITIES_MAP.put(INVOKE, BPELNodeType.INVOKE_TYPE);
        ALL_ACTIVITIES_MAP.put(ASSIGN, BPELNodeType.ASSIGN_TYPE);
        ALL_ACTIVITIES_MAP.put(THROW, BPELNodeType.THROW_TYPE);        
        ALL_ACTIVITIES_MAP.put(TERMINATE, BPELNodeType.TERMINATE_TYPE);
        ALL_ACTIVITIES_MAP.put(WAIT, BPELNodeType.WAIT_TYPE);
        ALL_ACTIVITIES_MAP.put(EMPTY, BPELNodeType.EMPTY_TYPE);
        ALL_ACTIVITIES_MAP.put(SEQUENCE, BPELNodeType.SEQUENCE_TYPE);
        ALL_ACTIVITIES_MAP.put(IF, BPELNodeType.IF_TYPE);
        ALL_ACTIVITIES_MAP.put(REPEAT_UNTIL, BPELNodeType.REPEAT_UNTIL_TYPE);
        ALL_ACTIVITIES_MAP.put(FOREACH, BPELNodeType.FOREACH_TYPE);
        ALL_ACTIVITIES_MAP.put(SWITCH, BPELNodeType.SWITCH_TYPE);        
        ALL_ACTIVITIES_MAP.put(WHILE, BPELNodeType.WHILE_TYPE);
        ALL_ACTIVITIES_MAP.put(PICK, BPELNodeType.PICK_TYPE);
        ALL_ACTIVITIES_MAP.put(FLOW, BPELNodeType.FLOW_TYPE);
        ALL_ACTIVITIES_MAP.put(SCOPE, BPELNodeType.SCOPE_TYPE);
        ALL_ACTIVITIES_MAP.put(COMPENSATE, BPELNodeType.COMPENSATE_TYPE);  
        ALL_ACTIVITIES_MAP.put(COMPENSATE_SCOPE, BPELNodeType.COMPENSATE_SCOPE_TYPE);
        ALL_ACTIVITIES_MAP.put(RETHROW, BPELNodeType.RETHROW_TYPE);
        ALL_ACTIVITIES_MAP.put(VALIDATE, BPELNodeType.VALIDATE_TYPE);
    }
    
    public static BPELNodeType getNodeType (String nodeName) {
        return ALL_ACTIVITIES_MAP.get(nodeName) != null ? 
            ((BPELNodeType) ALL_ACTIVITIES_MAP.get(nodeName)) : 
            BPELNodeType.NONE_ACTIVITY_TYPE;
    }
    
    private final int mLineNo;
    private final String mName;
    private final String mXpath;
    private final BPELNodeType mType;
    private final boolean mActivityFlag;
    private final String mTargetNameSpace;
    private int mClosingNo;
    
    private List mChildren = new ArrayList();
    private BPELNode mParent;
    
    public BPELNode(
            final String name, 
            final int lineNumber, 
            final String targetNamespace, 
            final BPELNodeType type, 
            final BPELNode parent) {
        mName = name;
        mLineNo = lineNumber;
        mTargetNameSpace = targetNamespace;
        mType = type;
        mActivityFlag = (getNodeType(name) != BPELNodeType.NONE_ACTIVITY_TYPE);
        mParent = parent;
        mXpath = constructXpath();
    }
    
    /**
     * Returns whether he BPEL Node is an Activity type
     * of node
     * @return true if it is 
     */
    public boolean isActivity() {
        return mActivityFlag;
    }    

    private String constructXpath() {
        // TODO Auto-generated method stub
       String result = "/";
       if (mParent == null) {
           return result + mName;
       }
       String xpath = mParent.getXpath();
       List sibings = mParent.getChildren();
       
       int index = 1;
       for (int i = 0; i < sibings.size(); i++) {
           BPELNode sibling = (BPELNode) sibings.get(i);
           if (sibling.getType() == mType) {
               index++;
           }           
       }
      return xpath + "/" + mName + "[" + index + "]";
    }
    
    public String getName() {
        return mName;
    }
    
    /**
     * Returns the type of this node
     * @return
     */
    public BPELNodeType getType() {
        // TODO Auto-generated method stub
        return mType;
    }

    /**
     * Returns the children of this node
     * @return
     */
    public List getChildren() {
        // TODO Auto-generated method stub
        return mChildren;
    }
    
    /**
     * Add a child
     * @param child
     */
    public void addChild (BPELNode child) {
        mChildren.add(child);
    }

    /**
     * Returns the xpath of this node
     * @return
     */
    public String getXpath() {
        // TODO Auto-generated method stub
        return mXpath;
    }
    
    /**
     * Returns the lineNo of this node
     * @return
     */
    public int getLineNumber() {
        return mLineNo;
    }
    
    public BPELNode getParent() {
        return mParent;
    }
    
    /**
     * Returns the target name space 
     */
    public String getTargetNameSpace() {
        return mTargetNameSpace;
    }
    /**
     * Sets the closing number
     * @param lineNo the line no. for the closing tag line
     */

    public void setClosingNumber (int lineNumber) {
        mClosingNo = lineNumber;
    }
    
    /**
     * Gets the closing number
     * @return the line no. for the closing tag line
     */
    public int getClosingNumber() {
        return mClosingNo;
    }
    
    @Override
    public boolean equals(Object obj) {
        // TODO Auto-generated method stub
        if (!(obj instanceof BPELNode)) {
            return false;
        }
        return ((BPELNode) obj).mTargetNameSpace.equals(mTargetNameSpace) && ((BPELNode) obj).mXpath.equals(mXpath);
    }
    
    @Override
    public int hashCode() {
        // TODO Auto-generated method stub
        return (mXpath.hashCode() + mTargetNameSpace.hashCode()) * 37;
    }
    
    @Override
    public String toString() {
        // TODO Auto-generated method stub
        return "targetNameSpace:" + mTargetNameSpace + "   name:" + mName + "   lineNo:" + mLineNo + "   xPath:" + mXpath; 
    }
}
