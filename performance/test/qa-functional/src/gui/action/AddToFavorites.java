/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package gui.action;

import org.netbeans.jellytools.FavoritesOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.nodes.Node;

import org.netbeans.jemmy.operators.ComponentOperator;

/**
 * Tests Add to Favorites.
 *
 * @author  mmirilovic@netbeans.org
 */
public class AddToFavorites extends org.netbeans.performance.test.utilities.PerformanceTestCase {
    
    protected static String ADD_TO_FAVORITES = "Tools|Add to Favorites"; //NOI18N
    
    protected static String REMOVE_FROM_FAVORITES = "Remove from Favorites";  //NOI18N
    
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
        new Node(favoritesWindow.tree(), fileName).performPopupAction(REMOVE_FROM_FAVORITES);
        favoritesWindow.close();
    }
    
    public void prepare() {
        addToFavoritesNode = new Node(new ProjectsTabOperator().getProjectRootNode(fileProject), gui.Utilities.SOURCE_PACKAGES + '|' +  filePackage + '|' + fileName);
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(new AddToFavorites("testAddJavaFile"));
    }
    
}
