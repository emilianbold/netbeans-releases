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

package org.netbeans.modules.refactoring.spi.ui;

import org.openide.util.Lookup;
/**
 * Create your own provider of this class and register it in META-INF services, if you want to
 * create your own implementations of refactorin actions.
 * For instance Java module wants to have refactoring rename action for java files.
 * So Java Refactoring module must implement 2 methods. 
 *
 * <pre>
 * public boolean canRename(Lookup lookup) {
 *   Node[] nodes = lookup.lookupAll(Node.class);
 *   if (..one node selected and the node belongs to java...)
 *      return true;
 *   else 
 *      return false;
 * }
 *
 * public Runnable renameImpl(Lookup selectedNodes) {
 *   Node[] nodes = lookup.lookupAll(Node.class);
 *   final FileObject fo = getFileFromNode(nodes[0]);
 *   return new Runnable() {
 *     public void run() {
 *       UI.openRefactoringUI(new RenameRefactoringUI(fo);
 *     }
 *   }    
 * }
 * </pre>     
 *
 * @author Jan Becicka
 */
public abstract class ActionsImplementationProvider {
    
    /**
     * @return true if provider can handle rename
     */
    public boolean canRename(Lookup node) {
        return false;
    }

    /**
     * @return implementation of Rename Action
     */
    public Runnable renameImpl(Lookup selectedNodes) {
        return null;
    }

    /**
     * @return true if provider can handle find usages
     */
    public boolean canFindUsages(Lookup lookup) {
        return false;
    }

    /**
     * @return implementation of Find Usages Action
     */
    public Runnable findUsagesImpl(Lookup lookup) {
        return null;
    }

    /**
     * @return true if provider can handle delete
     */
    public boolean canDelete(Lookup lookup) {
        return false;
    }
    
    /**
     * @return implementation of Delete Action
     */
    public Runnable deleteImpl(Lookup lookup) {
        return null;
    }

    /**
     * @return true if provider can handle move
     */
    public boolean canMove(Lookup lookup) {
        return false;
    }

    /**
     * @return implementation of Move Action
     */
    public Runnable moveImpl(Lookup lookup) {
        return null;
    }

    /**
     * @return true if provider can handle copy
     */
    public boolean canCopy(Lookup lookup) {
        return false;
    }

    /**
     * @return implementation of Copy Action
     */
    public Runnable copyImpl(Lookup lookup) {
        return null;
    }
}
