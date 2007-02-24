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

		if (type == EDT_WORKSPACE_KIND_DP)
		   typeName = "WorkspaceDispatcherDP";

		if (type == EDT_DRAWINGAREA_KIND)
		   typeName = "DrawingAreaDispatcher";

		if (type == EDT_PROJECTTREE_KIND)
		   typeName = "ProjectTreeDispatcher" ;

		if (type == EDT_ELEMENTMODIFIED_KIND)
		   typeName = "ElementChangeDispatcher" ;

		if (type == EDT_EDITCTRL_KIND)
		   typeName = "EditCtrlDispatcher" ;

		if (type == EDT_PROJECTTREEDIALOGFILTER_KIND)
		   typeName =  "ProjectTreeFilterDialogDispatcher" ;

		if (type == EDT_ADDIN_KIND)
		   typeName =  "AddInDispatcher" ;

		if (type == EDT_MESSAGING_KIND)
		   typeName =  "UMLMessagingDispatcher" ;

		if (type == EDT_VBA_KIND)
		   typeName = "VBAIntegrationDispatcher";

		if (type == EDT_RELATION_KIND)
		   typeName = "RelationValidatorEventDispatcher" ;

		if (type == EDT_ELEMENT_LIFETIME_KIND)
		   typeName =  "LifeTimeDispatcher";

		if (type == EDT_CLASSIFIER_KIND)
		   typeName =  "ClassifierDispatcher" ;

		if (type == EDT_COREPRODUCT_KIND)
		   typeName =  "CoreProductDispatcher" ;

		if (type == EDT_PREFERENCEMANAGER_KIND)
		   typeName =  "PreferenceManager" ;

		if (type == EDT_SCM_KIND)
		   typeName =  "SCM" ;

		if (type == EDT_DYNAMICS_KIND)
		   typeName =  "DynamicsDispatcher" ;

		if (type == EDT_ACTIVITIES_KIND)
		   typeName =  "ActivitiesDispatcher" ;

		if (type == EDT_STRUCTURE_KIND)
		   typeName =  "StructureDispatcher" ;

		if (type == EDT_ROUNDTRIP_KIND)
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


