/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.ant.freeform.spi;

import java.util.List;
import java.util.Set;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

/**
 * Description of base freeform project extension. Instances should be 
 * registered into default lookup. Freeform project will always call all
 * registered implementations of this interface and it is up to the 
 * implementation to decide (based on the project's metadata) whether they
 * want or should enhance the project or not.
 *
 * @author David Konecny, Jesse Glick
 */
public interface ProjectNature {

    /**
     * Check project and provide additional lookup if it is project of your type.
     * @return lookup additional lookup to insert; cannot be null (use {@link Lookup#EMPTY} instead)
     */
    Lookup getLookup(Project project, AntProjectHelper projectHelper, PropertyEvaluator projectEvaluator, AuxiliaryConfiguration aux);
    
    /**
     * Check project and provide additional customizer panels if it is project of your type.
     * Order of panels is unimportant because 
     * {@link ProjectPropertiesPanel#getPreferredPosition} will be used for ordering.
     * @return a list of {@link ProjectPropertiesPanel}s (can be empty but not null)
     */
    Set/*<ProjectPropertiesPanel>*/ getCustomizerPanels(Project project, AntProjectHelper projectHelper, PropertyEvaluator projectEvaluator, AuxiliaryConfiguration aux);
    
    /**
     * Check project and provide additional build targets to be shown in 
     * target mapping customizer panel if it is project of your type. Order
     * of targets is important.
     * @return a list of {@link TargetDescriptor}s (can be empty but not null)
     */
    List/*<TargetDescriptor>*/ getExtraTargets(Project project, AntProjectHelper projectHelper, PropertyEvaluator projectEvaluator, AuxiliaryConfiguration aux);
    
    /**
     * Returns set of XML schemas describing syntax of <code>project.xml</code> defined by this project extension.
     * @return set of <code>String</code>s whose value is URL of XML schema file
     */
    Set/*<String>*/ getSchemas();
    
    /**
     * Get a set of view styles supported by the nature for displaying source folders in the logical view.
     * @return a set of <code>String</code> style names (may be empty but not null)
     */
    Set/*<String>*/ getSourceFolderViewStyles();
    
    /**
     * Produce a logical view of a source folder in a style supported by the nature.
     * @param project a project displaying the view
     * @param folder a file folder (typically part of the project but not necessarily) to produce a view of
     * @param style a view style; will be one of {@link #getSourceFolderViewStyles}
     * @param name a suggested code name for the new node
     * @param displayName a suggested display name for the new node (may be null, in which case provider is free to pick an appropriate display name)
     * @return a logical view of that folder
     * @throws IllegalArgumentException if the supplied style is not one of {@link #getSourceFolderViewStyles}
     */
    Node createSourceFolderView(Project project, FileObject folder, String style, String name, String displayName) throws IllegalArgumentException;
    
    /**
     * Try to find a node selection in a source folder logical view.
     * @param project a project displaying the view
     * @param root a source folder view node which may have been returned by {@link #createSourceFolderView} (or not)
     * @param target a lookup entry indicating the node to find (e.g. a {@link FileObject})
     * @return a subnode of the root node representing the target, or null if either the target could not be found, or the root node was not recognized
     * @see org.netbeans.spi.project.ui.LogicalViewProvider#findPath
     */
    Node findSourceFolderViewPath(Project project, Node root, Object target);
    
}
