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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.tax;

import org.netbeans.tax.spec.DTD;
import org.netbeans.tax.spec.ParameterEntityReference;
import org.netbeans.tax.spec.ConditionalSection;

/**
 *
 * @author  Libor Kramolis
 * @version 0.1
 */
public class TreeConditionalSection extends AbstractTreeDTD implements DTD.Child, ParameterEntityReference.Child, ConditionalSection.Child {

    /** */
    public static final String PROP_INCLUDE         = "include"; // NOI18N
    /** */
    public static final String PROP_IGNORED_CONTENT = "ignoredContent"; // NOI18N

    /** */
    public static final boolean IGNORE  = false;
    
    /** */
    public static final boolean INCLUDE = true;
    
    
    /** */
    private boolean include;
    
    /** -- can be null. */
    private String ignoredContent;  //or null
    
    
    //
    // init
    //
    
    /** Creates new TreeConditionalSection. */
    public TreeConditionalSection (boolean include) {
        super ();
        
        this.include        = include;
        this.ignoredContent = new String ();
    }
    
    /** Creates new TreeConditionalSection -- copy constructor. */
    protected TreeConditionalSection (TreeConditionalSection conditionalSection, boolean deep) {
        super (conditionalSection, deep);
        
        this.include        = conditionalSection.include;
        this.ignoredContent = conditionalSection.ignoredContent;
    }
    
    
    //
    // from TreeObject
    //
    
    /**
     */
    public Object clone (boolean deep) {
        return new TreeConditionalSection (this, deep);
    }
    
    /**
     */
    public boolean equals (Object object, boolean deep) {
        if (!!! super.equals (object, deep))
            return false;
        
        TreeConditionalSection peer = (TreeConditionalSection) object;
        if (this.include != peer.include)
            return false;
        if (!!! Util.equals (this.getIgnoredContent (), peer.getIgnoredContent ()))
            return false;
        
        return true;
    }
    
    /*
     * Merges following properties: ignored, ignoredContent.
     */
    public void merge (TreeObject treeObject) throws CannotMergeException {
        super.merge (treeObject);
        
        TreeConditionalSection peer = (TreeConditionalSection) treeObject;
        
        setIncludeImpl (peer.isInclude ());
        setIgnoredContentImpl (peer.getIgnoredContent ());
    }
    
    
    //
    // itself
    //
    
    /**
     */
    public final boolean isInclude () {
        return include;
    }
    
    /**
     */
    private final void setIncludeImpl (boolean newInclude) {
        if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("\nTreeConditionalSection::setIncludeImpl: oldInclude = " + this.include); // NOI18N
        if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("                      ::setIncludeImpl: newInclude = " + newInclude); // NOI18N
        
        boolean oldInclude = this.include;
        
        this.include = newInclude;
        
        firePropertyChange (PROP_INCLUDE, oldInclude ? Boolean.TRUE : Boolean.FALSE, newInclude ? Boolean.TRUE : Boolean.FALSE);
    }
    
    /**
     * @throws ReadOnlyException
     * @throws InvalidArgumentException
     */
    public final void setInclude (boolean newInclude) throws ReadOnlyException, InvalidArgumentException {
        if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("\nTreeConditionalSection::setInclude: oldInclude = " + this.include); // NOI18N
        if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("                      ::setInclude: newInclude = " + newInclude); // NOI18N
        
        //
        // check new value
        //
        if ( this.include == newInclude )
            return;
        checkReadOnly ();
        //  	checkInclude (newInclude);
        
        //
        // set new value
        //
        setIncludeImpl (newInclude);
    }
    
    
    /**
     */
    public final String getIgnoredContent () {
        return ignoredContent;
    }
    
    /**
     */
    private void setIgnoredContentImpl (String newContent) {
        String oldContent = this.ignoredContent;
        
        this.ignoredContent = newContent;
        
        firePropertyChange (PROP_IGNORED_CONTENT, oldContent, newContent);
    }
    
    /**
     * @throws ReadOnlyException
     * @throws InvalidArgumentException
     */
    public final void setIgnoredContent (String newIgnoredContent) throws ReadOnlyException, InvalidArgumentException {
        //
        // check new value
        //
        if ( Util.equals (this.ignoredContent, newIgnoredContent) )
            return;
        checkReadOnly ();
        //  	checkIgnoredContent (newIgnoredContent);
        
        //
        // set new value
        //
        setIgnoredContentImpl (newIgnoredContent);
    }
    
    
    //
    // TreeObjectList.ContentManager
    //
    
    /**
     */
    protected TreeObjectList.ContentManager createChildListContentManager () {
        return new ChildListContentManager ();
    }
    
    
    /**
     *
     */
    protected class ChildListContentManager extends AbstractTreeDTD.ChildListContentManager {
        
        /**
         */
        public TreeNode getOwnerNode () {
            return TreeConditionalSection.this;
        }
        
        /**
         */
        public void checkAssignableObject (Object obj) {
            super.checkAssignableObject (obj);
            checkAssignableClass (ConditionalSection.Child.class, obj);
        }
        
    } // end: class ChildListContentManager
    
}
