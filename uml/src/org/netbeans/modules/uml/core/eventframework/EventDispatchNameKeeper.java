/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */


package org.netbeans.modules.uml.core.eventframework;

/**
 * @author sumitabhk
 *
 */
public class EventDispatchNameKeeper {

	public static int EDT_WORKSPACE_KIND = 0;
	public static int EDT_DRAWINGAREA_KIND = 1;
	public static int EDT_PROJECTTREE_KIND = 2;
	public static int EDT_ELEMENTMODIFIED_KIND = 3;
	public static int EDT_EDITCTRL_KIND = 4;
	public static int EDT_PROJECTTREEDIALOGFILTER_KIND = 5;
	public static int EDT_ADDIN_KIND = 6;
	public static int EDT_MESSAGING_KIND = 7;
	public static int EDT_VBA_KIND = 8;
	public static int EDT_RELATION_KIND = 9; 
	public static int EDT_ELEMENT_LIFETIME_KIND = 10;
	public static int EDT_CLASSIFIER_KIND = 11;
	public static int EDT_COREPRODUCT_KIND = 12;
	public static int EDT_PREFERENCEMANAGER_KIND = 13;
	public static int EDT_SCM_KIND = 14;
	public static int EDT_DYNAMICS_KIND = 15;
	public static int EDT_STRUCTURE_KIND = 16;
	public static int EDT_ACTIVITIES_KIND = 17;
	public static int EDT_ROUNDTRIP_KIND = 18;
	public static int EDT_WORKSPACE_KIND_DP = 19;

	public static String dispatcherName( int type )
	{
		String typeName = "";

		if (type == EDT_WORKSPACE_KIND)
		   typeName = "WorkspaceDispatcher";

                else if (type == EDT_WORKSPACE_KIND_DP)
		   typeName = "WorkspaceDispatcherDP";

                else if (type == EDT_DRAWINGAREA_KIND)
		   typeName = "DrawingAreaDispatcher";

                else if (type == EDT_PROJECTTREE_KIND)
		   typeName = "ProjectTreeDispatcher" ;

		else if (type == EDT_ELEMENTMODIFIED_KIND)
		   typeName = "ElementChangeDispatcher" ;

		else if (type == EDT_EDITCTRL_KIND)
		   typeName = "EditCtrlDispatcher" ;

		else if (type == EDT_PROJECTTREEDIALOGFILTER_KIND)
		   typeName =  "ProjectTreeFilterDialogDispatcher" ;

		else if (type == EDT_ADDIN_KIND)
		   typeName =  "AddInDispatcher" ;

		else if (type == EDT_MESSAGING_KIND)
		   typeName =  "UMLMessagingDispatcher" ;

		else if (type == EDT_VBA_KIND)
		   typeName = "VBAIntegrationDispatcher";

		else if (type == EDT_RELATION_KIND)
		   typeName = "RelationValidatorEventDispatcher" ;

		else if (type == EDT_ELEMENT_LIFETIME_KIND)
		   typeName =  "LifeTimeDispatcher";

		else if (type == EDT_CLASSIFIER_KIND)
		   typeName =  "ClassifierDispatcher" ;

		else if (type == EDT_COREPRODUCT_KIND)
		   typeName =  "CoreProductDispatcher" ;

		else if (type == EDT_PREFERENCEMANAGER_KIND)
		   typeName =  "PreferenceManager" ;

		else if (type == EDT_SCM_KIND)
		   typeName =  "SCM" ;

		else if (type == EDT_DYNAMICS_KIND)
		   typeName =  "DynamicsDispatcher" ;

		else if (type == EDT_ACTIVITIES_KIND)
		   typeName =  "ActivitiesDispatcher" ;

		else if (type == EDT_STRUCTURE_KIND)
		   typeName =  "StructureDispatcher" ;

		else if (type == EDT_ROUNDTRIP_KIND)
		   typeName =  "RoundTripDispatcher" ;

		return typeName;
	}

	public static String workspaceName()
	{ 
		return dispatcherName( EventDispatchNameKeeper.EDT_WORKSPACE_KIND ); 
	}
	
	public static String drawingAreaName() 
	{ return dispatcherName( EventDispatchNameKeeper.EDT_DRAWINGAREA_KIND ); }
	
	public static String projectTreeName( )
	 { return dispatcherName( EventDispatchNameKeeper.EDT_PROJECTTREE_KIND ); }
	
	public static String modifiedName( )
	 { return dispatcherName( EventDispatchNameKeeper.EDT_ELEMENTMODIFIED_KIND ); }
	
	public static String editCtrlName() 
	{ return dispatcherName( EventDispatchNameKeeper.EDT_EDITCTRL_KIND ); }
	
	public static String projectTreeFilterDialogName( ) 
	{ return dispatcherName( EventDispatchNameKeeper.EDT_PROJECTTREEDIALOGFILTER_KIND ); }
	
	public static String addInName( ) 
	{ return dispatcherName( EventDispatchNameKeeper.EDT_ADDIN_KIND ); }
	
	public static String messagingName( ) 
	{ return dispatcherName( EventDispatchNameKeeper.EDT_MESSAGING_KIND ); }
	
	public static String vBA( ) 
	{ return dispatcherName( EventDispatchNameKeeper.EDT_VBA_KIND ); }
	
	public static String relation( ) 
	{ return dispatcherName( EventDispatchNameKeeper.EDT_RELATION_KIND ); }
	
	public static String lifeTime( ) 
	{ return dispatcherName( EventDispatchNameKeeper.EDT_ELEMENT_LIFETIME_KIND ); }
	
	public static String classifier( ) 
	{ return dispatcherName( EventDispatchNameKeeper.EDT_CLASSIFIER_KIND ); }
	
	public static String coreProduct( ) 
	{ return dispatcherName( EventDispatchNameKeeper.EDT_COREPRODUCT_KIND ); }
	
	public static String preferenceManager( ) 
	{ return dispatcherName( EventDispatchNameKeeper.EDT_PREFERENCEMANAGER_KIND ); }
	
	public static String SCM( ) 
	{ return dispatcherName( EventDispatchNameKeeper.EDT_SCM_KIND ); }
	
	public static String dynamics( ) 
	{ return dispatcherName( EventDispatchNameKeeper.EDT_DYNAMICS_KIND ); }
	
	public static String activities( ) 
	{ return dispatcherName( EventDispatchNameKeeper.EDT_ACTIVITIES_KIND ); }
	
	public static String structure( ) 
	{ return dispatcherName( EventDispatchNameKeeper.EDT_STRUCTURE_KIND ); }
	
	public static String roundTrip( ) 
	{ return dispatcherName( EventDispatchNameKeeper.EDT_ROUNDTRIP_KIND ); }

	public static String workspaceNameDP()
	{ 
		return dispatcherName( EventDispatchNameKeeper.EDT_WORKSPACE_KIND_DP ); 
	}

}


