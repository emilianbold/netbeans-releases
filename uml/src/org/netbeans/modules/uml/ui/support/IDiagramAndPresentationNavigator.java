/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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



package org.netbeans.modules.uml.ui.support;

import java.awt.Frame;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.structure.IProject;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.ui.support.diagramsupport.IPresentationTarget;

/**
 *
 * @author Trey Spiva
 */
public interface IDiagramAndPresentationNavigator
{
   /**
    * Navigate to a specific presentation target.  If more then one then a dialog is displayed.
    */
   public boolean navigateToPresentationTarget( int pParent, IElement pParentModelElement, ETList<IPresentationTarget> pPossibleTargets );

   /**
    * Navigate to a specific presentation target.  If more then one then a dialog is displayed.
    *
    * @param doShow if there is a preference governing whether to show the dialog by default or not to, 
    *               and the preference is set to not to, then 'true' value will still show the dialog.   
    *               For example, in current version of UI user has the option to press Shift  
    *               to indicate explicitly that s/he does want the dialog 
    */
   public boolean navigateToPresentationTarget( IElement pParentModelElement, ETList<IPresentationTarget> pPossibleTargets, boolean doShow);

   /**
    * Bring up the navigation dialog which allows user to go to either diagrams or presentation elements
    */
   public boolean handleNavigation( int pParent, IElement pElement, boolean isShift );

   /**
    * Bring up the project diagram dialog which shows all the closed diagrams in the project.
    */
   public void showScopedDiagrams( Frame pParent, IProject pCurrentProject );

   /**
    * This actually does the navigation.
    */
   public void doNavigate( IPresentationTarget pTarget );
}
