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

//  import org.netbeans.tax.grammar.Grammar; // will be ...

/**
 *
 * @author  Libor Kramolis
 * @version 0.1
 */
abstract class AbstractTreeDocument extends TreeParentNode {

    /** -- can be null. */
    //      private Grammar softGrammar; // will be ...


    //
    // init
    //

    /**
     * Creates new AbstractTreeDocument.
     */
    protected AbstractTreeDocument () {
        super ();
    }

    /** Creates new AbstractTreeDocument -- copy constructor. */
    protected AbstractTreeDocument (AbstractTreeDocument abstractDocument, boolean deep) {
        super (abstractDocument, deep);
        
        //  	this.softGrammar = abstractDocument.softGrammar; // will be ...
    }
    
    
    //
    // grammar // will be ...
    //
    
    //      /** Set soft grammar. Soft grammar is not directly used in document but is used for validation only.
    //       *  There are some document formats which are opened but has basic structure, e.g. ant, xsl.
    //       * @param grammar soft grammar
    //       */
    //      public void setSoftGrammar (Grammar grammar) {
    //  	softGrammar = grammar;
    //      }
    
    //      /** */
    //      public Grammar getSoftGrammar () {
    //  	return softGrammar;
    //      }
    
    
    //
    // TreeObjectList.ContentManager
    //
    
    /**
     *
     */
    protected abstract class ChildListContentManager extends TreeParentNode.ChildListContentManager {
        
    } // end: class ChildListContentManager
    
}
