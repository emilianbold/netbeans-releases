/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package threaddemo.views.looktree;

import java.util.*;
import org.netbeans.spi.looks.Look;
import org.netbeans.spi.looks.LookSelector;
import org.netbeans.modules.looks.LookListener;
import org.netbeans.modules.looks.LookEvent;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.enum.AlterEnumeration;
import org.openide.util.enum.EmptyEnumeration;

/**
 * One node in a tree of looks.
 * @author Jesse Glick
 */
abstract class LookTreeNode implements LookListener {
    
    public static LookTreeNode createRoot(Object o, LookSelector s, LookTreeModel m) {
        return new RootLookTreeNode(findLook(o, s), o, s, m);
    }
    
    private final Object representedObject;
    private final Look look;
    // private Map children = null; // Map<Object,LookTreeNode>
    private LookTreeNode[] children;
    private List childrenList;
    protected int index = -1;
    
    private static final class RootLookTreeNode extends LookTreeNode {
        private final LookSelector s;
        private final LookTreeModel m;
        public RootLookTreeNode(Look l, Object o, LookSelector s, LookTreeModel m) {
            super(l, o);
            this.s = s;
            this.m = m;
        }
        protected LookSelector getSelector() {
            return s;
        }
        protected void fireDisplayChange(LookTreeNode source) {
            m.fireDisplayChange(source);
        }
        protected void fireChildrenChange(LookTreeNode source) {
            m.fireChildrenChange(source);
        }
        
        public void lookupItemsChanged(LookEvent evt) {
        }
        
    }
    
    private static final class ChildLookTreeNode extends LookTreeNode {
        private final LookTreeNode p;
        public ChildLookTreeNode(Look l, Object o, LookTreeNode p, int index) {
            super(l, o);
            this.p = p;
            this.index = index;
        }
        protected LookSelector getSelector() {
            return p.getSelector();
        }
        protected void fireDisplayChange(LookTreeNode source) {
            p.fireDisplayChange(source);
        }
        protected void fireChildrenChange(LookTreeNode source) {
            p.fireChildrenChange(source);
        }
        
        public void lookupItemsChanged(LookEvent evt) {
        }
        
    }
    
    private LookTreeNode(Look l, Object o) {
        this.representedObject = o;
        this.look = l;
        org.netbeans.modules.looks.Accessor.DEFAULT.addLookListener( l, representedObject, this );
    }
    
    private static Look findLook(Object o, LookSelector s) {
        Enumeration e = s.getLooks(o);
        while (e.hasMoreElements()) {
            Object x = e.nextElement();
            if (x instanceof Look) {
                return (Look)x;
            }
        }
        throw new IllegalArgumentException("No look found for " + o + " with selector " + s);
    }
    
    protected abstract LookSelector getSelector();
    
    protected abstract void fireDisplayChange(LookTreeNode source);
    
    protected abstract void fireChildrenChange(LookTreeNode source);
    
    public Look getLook() {
        return look;
    }
    
    
    
    void forgetChildren() {
        /*
        if (children != null) {
            Iterator it = children.values().iterator();
            while (it.hasNext()) {
                ((LookTreeNode)it.next()).forgetEverything();
            }
            children = null;
        }
         */
        childrenList = null;
        children = null;
    }
    
    void forgetEverything() {
        forgetChildren();
        //FirerSupport.DEFAULT.unregisterSubstitute(n);
    }
    
    private List getChildrenList() {
        if ( childrenList == null ) {
            childrenList = getLook().getChildObjects( representedObject, getLookup() );
            assert childrenList != null : "null kids from " + getLook() + " on " + representedObject;
            children = new LookTreeNode[childrenList.size()];
        }
        return childrenList;
    }
    
    public LookTreeNode getParent() {
        if (this instanceof ChildLookTreeNode) {
            return ((ChildLookTreeNode)this).p;
        } else {
            return null;
        }
    }
    
    public Object getData() {
        return representedObject;
    }

    public Lookup getLookup() {
        return Lookup.EMPTY; // PENDING
    }

    public String toString() {
        return "LookTreeNode<" + representedObject + ">";
    }
    
    // Methods for TreeModel ---------------------------------------------------
    
    public LookTreeNode getChild( int index ) {
        
        if (children == null || children[index] == null) {
            Object o = getChildrenList().get(index);
            LookTreeNode ltn = new ChildLookTreeNode(findLook(o, getSelector()), o, this, index);
            children[index] = ltn; 
        }
        
        return children[index];
    }
    
    public int getChildCount() {                
        return getChildrenList().size();
    }
    
    public int getIndexOfChild(LookTreeNode child) {
        // XXX this is not very nice for performance
        
        
        if ( child.index == -1 ) {
            System.out.println("Uggly: find " + child + " in " + this );
            for( int i = 0; i < children.length; i++ ) {
                if ( children[i] == child ) 
                    return i;
            }
            throw new IllegalStateException( "Can't find LookTreeNode " + child + " in " + this );
        }
        else {
            return child.index;
        }
    }
    
    public boolean isLeaf() {
        return getLook().isLeaf(representedObject, getLookup());
    }
    
    // Implementation of LookListener ------------------------------------------
    
    public void change( LookEvent evt ) {
        long mask = evt.getMask();
        
        // XXX Look.GET_PROPERTY_SETS not impl.
        
        if ( ( mask & 
             ( Look.GET_NAME | Look.GET_DISPLAY_NAME | Look.GET_ICON | 
               Look.GET_OPENED_ICON | Look.GET_SHORT_DESCRIPTION ) ) > 0 ) {
         
            fireDisplayChange( this );       
        }
        
        if ( ( mask & Look.GET_CHILD_OBJECTS ) > 0 ) {
            forgetChildren();
            fireChildrenChange(this);
        }
    }
    
    
    public void propertyChange(LookEvent evt) {
        // XXX prop sets not impl
    }
            
}
