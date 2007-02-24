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



package org.netbeans.modules.uml.ui.support.viewfactorysupport;

/**
 *
 * @author Trey Spiva
 */
public interface ModelElementChangedKind
{
	public final static int MECK_UNKNOWN	= -1;
	public final static int MECK_NAMEMODIFIED	= MECK_UNKNOWN + 1;
	public final static int MECK_ELEMENTMODIFIED	= MECK_NAMEMODIFIED + 1;
	public final static int MECK_FEATUREADDED	= MECK_ELEMENTMODIFIED + 1;
	public final static int MECK_FEATUREMOVED	= MECK_FEATUREADDED + 1;
	public final static int MECK_FEATUREDUPLICATEDTOCLASSIFIER	= MECK_FEATUREMOVED + 1;
	public final static int MECK_MULTIPLICITYMODIFIED	= MECK_FEATUREDUPLICATEDTOCLASSIFIER + 1;
	public final static int MECK_TYPEMODIFIED	= MECK_MULTIPLICITYMODIFIED + 1;
	public final static int MECK_LOWERMODIFIED	= MECK_TYPEMODIFIED + 1;
	public final static int MECK_UPPERMODIFIED	= MECK_LOWERMODIFIED + 1;
	public final static int MECK_RANGEADDED	= MECK_UPPERMODIFIED + 1;
	public final static int MECK_RANGEREMOVED	= MECK_RANGEADDED + 1;
	public final static int MECK_ORDERMODIFIED	= MECK_RANGEREMOVED + 1;
	public final static int MECK_RELATIONENDMODIFIED	= MECK_ORDERMODIFIED + 1;
	public final static int MECK_RELATIONENDADDED	= MECK_RELATIONENDMODIFIED + 1;
	public final static int MECK_RELATIONENDREMOVED	= MECK_RELATIONENDADDED + 1;
	public final static int MECK_RELATIONCREATED	= MECK_RELATIONENDREMOVED + 1;
	public final static int MECK_RELATIONDELETED	= MECK_RELATIONCREATED + 1;
	public final static int MECK_ALIASNAMEMODIFIED	= MECK_RELATIONDELETED + 1;
	public final static int MECK_ABSTRACTMODIFIED	= MECK_ALIASNAMEMODIFIED + 1;
	public final static int MECK_IMPACTED	= MECK_ABSTRACTMODIFIED + 1;
	public final static int MECK_STEREOTYPEAPPLIED	= MECK_IMPACTED + 1;
	public final static int MECK_STEREOTYPEDELETED	= MECK_STEREOTYPEAPPLIED + 1;
	public final static int MECK_ELEMENTADDEDTONAMESPACE	= MECK_STEREOTYPEDELETED + 1;
	public final static int MECK_VISIBILITYMODIFIED	= MECK_ELEMENTADDEDTONAMESPACE + 1;
	public final static int MECK_DERIVEDMODIFIED	= MECK_VISIBILITYMODIFIED + 1;
	public final static int MECK_PRIMARYKEYMODIFIED	= MECK_DERIVEDMODIFIED + 1;
	public final static int MECK_DEFAULTMODIFIED	= MECK_PRIMARYKEYMODIFIED + 1;
	public final static int MECK_DEFAULTEXPMODIFIED	= MECK_DEFAULTMODIFIED + 1;
	public final static int MECK_DEFAULTEXPBODYMODIFIED	= MECK_DEFAULTEXPMODIFIED + 1;
	public final static int MECK_PARAMETERADDED	= MECK_DEFAULTEXPBODYMODIFIED + 1;
	public final static int MECK_PARAMETERREMOVED	= MECK_PARAMETERADDED + 1;
	public final static int MECK_REDEFININGELEMENTREMOVED	= MECK_PARAMETERREMOVED + 1;
	public final static int MECK_REPRESENTINGCLASSIFIERCHANGED	= MECK_REDEFININGELEMENTREMOVED + 1;
	public final static int MECK_STATICMODIFIED	= MECK_REPRESENTINGCLASSIFIERCHANGED + 1;
	public final static int MECK_ELEMENTDELETED	= MECK_STATICMODIFIED + 1;
	public final static int MECK_ELEMENTTRANSFORMED	= MECK_ELEMENTDELETED + 1;
	public final static int MECK_ACTIVITYEDGE_WEIGHTMODIFIED	= MECK_ELEMENTTRANSFORMED + 1;
	public final static int MECK_ACTIVITYEDGE_GUARDMODIFIED	= MECK_ACTIVITYEDGE_WEIGHTMODIFIED + 1;
	public final static int MECK_PROJECT_MODEMODIFIED	= MECK_ACTIVITYEDGE_GUARDMODIFIED + 1;
	public final static int MECK_PROJECT_LANGUAGEMODIFIED	= MECK_PROJECT_MODEMODIFIED + 1;
	public final static int MECK_QUALIFIER_ADDED	= MECK_PROJECT_LANGUAGEMODIFIED + 1;
	public final static int MECK_QUALIFIER_REMOVED	= MECK_QUALIFIER_ADDED + 1;
	public final static int MECK_OPERATION_PROPERTY_CHANGE	= MECK_QUALIFIER_REMOVED + 1;

}
