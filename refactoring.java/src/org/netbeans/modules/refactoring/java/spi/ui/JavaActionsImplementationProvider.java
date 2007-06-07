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

package org.netbeans.modules.refactoring.java.spi.ui;

import org.openide.util.Lookup;

/**
 * Create your own provider of this class and register it in META-INF services, if you want to
 * create your own implementations of refactorin actions.
 * For instance Java module wants to have refactoring rename action for java files.
 * So Java Refactoring module must implement 2 methods. 
 *
 * <pre>
 * public boolean canChangeParameters(Lookup lookup) {
 *   Node[] nodes = lookup.lookupAll(Node.class);
 *   if (..one node selected and the node belongs to java...)
 *      return true;
 *   else 
 *      return false;
 * }
 *
 * public void doChangeParameters(Lookup lookup) {
 *   Node[] nodes = lookup.lookupAll(Node.class);
 *   final FileObject fo = getFileFromNode(nodes[0]);
 *   UI.openRefactoringUI(new ChangeParametersUI(fo);
 * }
 * </pre>     
 *
 * For help on creating and registering actions
 * See <a href=http://wiki.netbeans.org/wiki/view/RefactoringFAQ>Refactoring FAQ</a>
 * 
 * @author Jan Becicka
 */
public class JavaActionsImplementationProvider {

    /**
     * @param lookup 
     * @return true if provider can handle rename
     */
    public boolean canEncapsulateFields(Lookup lookup) {
        return false;
    }

    /**
     * @param lookup 
     */
    public void doEncapsulateFields(Lookup lookup) {
        new UnsupportedOperationException("Not implemented");
    }

    /**
     * @param lookup 
     * @return true if provider can handle find usages
     */
    public boolean canChangeParameters(Lookup lookup) {
        return false;
    }

    /**
     * implementation of "invoke Change Parameters"
     * @param lookup 
     */
    public void doChangeParameters(Lookup lookup) {
        new UnsupportedOperationException("Not implemented");
    }

    /**
     * @param lookup 
     * @return true if provider can handle Pull Up
     */
    public boolean canPullUp(Lookup lookup) {
        return false;
    }
    
    /**
     * implementation of "invoke Pull Up"
     * @param lookup 
     */
    public void doPullUp(Lookup lookup) {
        new UnsupportedOperationException("Not implemented");
    }

    /**
     * @param lookup 
     * @return true if provider can handle push down
     */
    public boolean canPushDown(Lookup lookup) {
        return false;
    }

    /**
     * implementation of "invoke Push Down"
     * @param lookup 
     */
    public void doPushDown(Lookup lookup) {
        new UnsupportedOperationException("Not implemented");
    }
    
    /**
     * @param lookup 
     * @return true if provider can handle Inner to Outer
     */
    public boolean canInnerToOuter(Lookup lookup) {
        return false;
    }

    /**
     * implementation of "invoke Inner To Outer"
     * @param lookup 
     */
    public void doInnerToOuter(Lookup lookup) {
        new UnsupportedOperationException("Not implemented");
    }    
    
    /**
     * @param lookup 
     * @return true if provider can handle Use Super Type
     */
    public boolean canUseSuperType(Lookup lookup) {
        return false;
    }

    /**
     * implementation of "invoke Use Super Type"
     * @param lookup 
     */
    public void doUseSuperType(Lookup lookup) {
        new UnsupportedOperationException("Not implemented");
    }    
    
    /**
     * @param lookup 
     * @return true if provider can handle extract superclass
     */
    public boolean canExtractSuperclass(Lookup lookup) {
        return false;
    }

    /**
     * implementation of "invoke Extract Superclass"
     * @param lookup 
     */
    public void doExtractSuperclass(Lookup lookup) {
        new UnsupportedOperationException("Not implemented");
    }    
    
    /**
     * @param lookup 
     * @return true if provider can handle extract Interface
     */
    public boolean canExtractInterface(Lookup lookup) {
        return false;
    }

    /**
     * implementation of "invoke Extract Interface"
     * @param lookup 
     */
    public void doExtractInterface(Lookup lookup) {
        new UnsupportedOperationException("Not implemented");
    }    
    
}
