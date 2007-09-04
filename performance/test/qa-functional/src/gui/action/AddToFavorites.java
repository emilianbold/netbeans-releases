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

package gui.action;

import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.FavoritesOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.SourcePackagesNode;

import org.netbeans.jemmy.operators.ComponentOperator;

/**
 * Tests Add to Favorites.
 *
 * @author  mmirilovic@netbeans.org
 */
public class AddToFavorites extends org.netbeans.performance.test.utilities.PerformanceTestCase {

    protected static String ADD_TO_FAVORITES = Bundle.getStringTrimmed("org.openide.actions.Bundle","CTL_Tools") + "|" + Bundle.getStringTrimmed("org.netbeans.modules.favorites.Bundle","ACT_Add"); // Tools|Add to Favorites
    
    protected static String REMOVE_FROM_FAVORITES = Bundle.getStringTrimmed("org.netbeans.modules.favorites.Bundle","ACT_Remove"); // Remove from Favorites
    
    private String fileProject, filePackage, fileName;
    
    private Node addToFavoritesNode;
    
    private FavoritesOperator favoritesWindow;
    
    /**
     * Creates a new instance of AddToFavorites
     * @param testName the name of the test
     */
    public AddToFavorites(String testName) {
        super(testName);
        expectedTime = WINDOW_OPEN;
    }
    
    /**
     * Creates a new instance of AddToFavorites
     * @param testName the name of the test
     * @param performanceDataName measured values will be saved under this name
     */
    public AddToFavorites(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = WINDOW_OPEN;
    }
    
    public void testAddJavaFile(){
        fileProject = "PerformanceTestData";
        filePackage = "org.netbeans.test.performance";
        fileName = "Main20kB.java";
        doMeasurement();
    }
    
    public ComponentOperator open(){
        addToFavoritesNode.performMenuAction(ADD_TO_FAVORITES);
        favoritesWindow = new FavoritesOperator();
        return favoritesWindow;
    }

    public void close() {
	Node n=new Node(favoritesWindow.tree(), fileName);
	n.performPopupAction(REMOVE_FROM_FAVORITES);
        favoritesWindow.close();
    }
    
    public void prepare() {
        addToFavoritesNode = new Node(new SourcePackagesNode(fileProject), filePackage + '|' + fileName);
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(new AddToFavorites("testAddJavaFile"));
    }
    
}
