/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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
