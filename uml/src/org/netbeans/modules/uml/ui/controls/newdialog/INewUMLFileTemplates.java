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

package org.netbeans.modules.uml.ui.controls.newdialog;

/**
 *
 * @author Thuy
 */

public interface INewUMLFileTemplates
{   
   public static final String PROP_TEMPLATE_TYPE = "templateType"; //NOI18N
   public static final int NEW_DIAGRAM = 0;
   public static final int NEW_PACKAGE = 1;
   public static final int NEW_ELEMENT = 2;
   
   public static final String PROP_PROJECT_NAME = "name"; //NOI18N
   public static final String PROP_PROJECT = "project"; //NOI18N
   public static final String PROP_WIZARD_ERROR_MESSAGE = "WizardPanel_errorMessage"; //NOI18N
   
   public static final String DIAGRAM_DETAILS = "DIAGRAM_DETAILS";    //NOI18N
   public static final String PROP_DIAG_KIND = "DIAGRAM_KIND"; //NOI18N
   public static final String PROP_DIAG_NAME = "DIAGRAM_NAME"; //NOI18N
   public static final String PROP_NAMESPACE = "NAMESPACE"; //NOI18N
   
   public static final String PACKAGE_DETAILS = "packageDetails"; //NOI18N
   public static final String ELEMENT_DETAILS = "elementDetails";      //NOI18N
}
