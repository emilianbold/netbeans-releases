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
 * 
 * $Id$
 */
package org.registry;
import java.awt.Component;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import junit.framework.TestCase;
import org.netbeans.installer.product.components.Product;
import org.netbeans.installer.utils.exceptions.UnresolvedDependencyException;
/*
 * Created on 29 ¿‚„ÛÒÚ 2006 „., 16:45
 */

/**
 *
 * @author Danila_Dugurov
 */

public class TestCircles extends TestCase {
    
    private List<Product> list;
    
    public void testThingInSelf() {
        try {
            list = new LinkedList<Product>();
            Product component = new Product();
            component.addRequirement(component);
            list.add(component);
            checkCircles();
            fail();
        } catch (UnresolvedDependencyException ex) {
        }
    }
    
    public void testMoreSofisticatedRequirements() {
        try {
            list = new LinkedList<Product>();
            Product component = new Product();
            Product depp = new Product();
            Product jony = new Product();
            list.add(component);
            list.add(depp);
            list.add(jony);
            component.addRequirement(depp);
            depp.addRequirement(jony);
            jony.addRequirement(component);
            checkCircles();
            fail();
        } catch (UnresolvedDependencyException ex) {
        }
    }
    
    public void testRequirementsAndConflicts() {
        try {
            list = new LinkedList<Product>();
            Product component = new Product();
            Product depp = new Product();
            Product jonny = new Product();
            list.add(component);
            list.add(depp);
            list.add(jonny);
            component.addRequirement(depp);
            depp.addRequirement(jonny);
            jonny.addConflict(component);
            checkCircles();
            fail();
        } catch (UnresolvedDependencyException ex) {
        }
    }
    
    public void testSofisticatedRequirementsAndConflicts() {
        try {
            list = new LinkedList<Product>();
            Product root = new Product();
            Product depp = new Product();
            Product jonny = new Product();
            Product independant = new Product();
            list.add(root);
            list.add(depp);
            list.add(jonny);
            list.add(independant);
            root.addRequirement(depp);
            root.addRequirement(jonny);
            jonny.addConflict(depp);
            jonny.addRequirement(independant);
            depp.addRequirement(independant);
            checkCircles();
            fail();
        } catch (UnresolvedDependencyException ex) {
        }
    }
    
    public void testOkConflicts() {
        try {
            list = new LinkedList<Product>();
            Product root = new Product();
            Product depp = new Product();
            Product jonny = new Product();          
            list.add(depp);
            list.add(jonny);
            list.add(root);
            root.addConflict(depp);
            root.addConflict(jonny);
            jonny.addRequirement(depp);
            checkCircles();
        } catch (UnresolvedDependencyException ex) {
            fail();
        }
    }
    
    private void checkCircles() throws UnresolvedDependencyException {
        for (Product component : list) {
            final Stack<Product> visited = new Stack<Product>();
            final Set<Product> conflictSet = new HashSet<Product>();
            final Set<Product> requirementSet = new HashSet<Product>();
            checkCircles(component, visited, conflictSet, requirementSet);
        }
    }
    
    private void checkCircles(Product component, Stack<Product> visited,
            Set<Product> conflictSet, Set<Product> requirementSet)
            throws UnresolvedDependencyException {
        if (visited.contains(component) || conflictSet.contains(component))
            throw new UnresolvedDependencyException("circles found");
        visited.push(component);
        requirementSet.add(component);
        if (!Collections.disjoint(requirementSet, component.getConflicts()))
            throw new UnresolvedDependencyException("circles found");
        conflictSet.addAll(component.getConflicts());
        for (Product comp : component.getRequirements())
            checkCircles(comp, visited, conflictSet, requirementSet);
        visited.pop();
    }
}
