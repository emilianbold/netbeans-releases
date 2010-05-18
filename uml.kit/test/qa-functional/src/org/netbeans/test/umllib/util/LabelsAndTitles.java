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


/*
 * LabelsAndTitles.java
 *
 * Created on March 29, 2005, 12:58 PM
 */

package org.netbeans.test.umllib.util;

/**
 *
 * @author Alexei Mokeev
 */
public interface LabelsAndTitles {
    
    public static final String JAVA_UML_PROJECT_LABEL = "Java-Platform Model";
    public static final String ANALYSIS_UML_PROJECT_LABEL = "Platform-Independent Model";
    public static final String RE_UML_PROJECT_LABEL = "Reverse Engineered Java-Platform Model";
    public static final String RM_UML_PROJECT_LABEL = "Imported Rose Model";
    public static final String UML_PROJECTS_CATEGORY = "UML";
    public static final String RE_UML_PROJECT_WIZARD_TITLE = "New Reverse Engineer a Java Project";
    public static final String RM_UML_PROJECT_WIZARD_TITLE = "New Imported Rose Model";
    public static final String EMPTY_UML_PROJECT_WIZARD_TITLE = "";
    //public static final String UML_WIZARD_ADD_JAVA_PROJECT_CAPTION = "Choose Java Project...";
    public static final String ADD_JAVA_PROJECT_DIALOG_TITLE = "Choose Java project";
    public static final String RE_PROGRESS_DIALOG_TITLE = "Reverse Engineering" ;
    public static final String RM_PROGRESS_DIALOG_TITLE = "Rose Model Import" ;
    public static final String RE_RESULTS_SAVE_DIALOG_TITLE = "Save" ;

    public static final String NBMODULE_PROJECTS_CATEGORY = "NetBeans Plug-in Modules";
    public static final String NBMODULE_PROJECT_LABEL = "Module Project";
    public static final String NBMODULE_SUITE_PROJECT_LABEL = "Module Suite Project";
    public static final String NBMODULE_LIBRARY_WRAPPER_PROJECT_LABEL = "Library Wrapper Module Project";

    
    public static final String DESIGN_CENTER_TITLE="UML Design Center";

    public static final String SAVE_DIAGRAM_CHANGES = "Save Diagram" ;
    
    public static final String FIND_IN_MODEL_DIALOG_TITLE = "Find" ;
    public static final String REPLACE_IN_MODEL_DIALOG_TITLE = "Replace" ;
    public static final String ASSOCIATE_DIALOG_TITLE = "Associate" ;

    public static final String MOVING_ELEMENTS_DIALOG_TITLE = "Moving Elements into Different Project" ;
    
    // class diagram palette group labels
    public static final String CLD_PALETTE_GROUP_BASIC = "Basic" ;
    public static final String CLD_PALETTE_GROUP_ROBUSTNESS = "Robustness" ;
    public static final String CLD_PALETTE_GROUP_DEPENDENCIES = "Dependencies" ;
    public static final String CLD_PALETTE_GROUP_TEMPLATES = "Templates" ;
    public static final String CLD_PALETTE_GROUP_ASSOCIATION = "Association" ;
    public static final String CLD_PALETTE_GROUP_DESIGNPATTERN = "Design Pattern" ;
    public static final String CLD_PALETTE_GROUP_COMMENTS = "Comments" ;
    //edit control context for classes
    public static final String POPUP_ADD_ATTRIBUTE="Create Attribute";
    public static final String POPUP_ADD_OPERATION="Create Operation";
    public static final String POPUP_DELETE_ATTRIBUTE="Delete Attribute";
    public static final String POPUP_DELETE_OPERATION="Delete Operation";
    //options
    public static final String OPTIONS_CHANGE_VIEW_BUTTON_LABEL="Advanced Options";

    // dilaogs 
    public static final String DIALOG_TITLE_SAVE = "Save";
    public static final String DIALOG_TITLE_DELETE = "Delete";
    public static final String DIALOG_TITLE_EXIT_IDE = "Exit IDE";

    public static final String DIALOG_TITLE_NEW_MODULE_PROJECT = "New Module Project";
    public static final String DIALOG_TITLE_NEW_MODULE_SUITE_PROJECT = "New Module Suite Project";
    
    
    public static final String DIALOG_BUTTON_YES = "Yes";
    public static final String DIALOG_BUTTON_OK = "OK";
    public static final String DIALOG_BUTTON_DONE = "Done";
    public static final String DIALOG_BUTTON_CANCEL = "Cancel";
    public static final String DIALOG_BUTTON_SAVE_ALL = "Save All";
    public static final String DIALOG_BUTTON_EXIT_IDE = "Exit IDE";

    
    public static final String PROJECT_CATEGORY_GENERAL= "Java";
    public static final String PROJECT_CATEGORY_UML = UML_PROJECTS_CATEGORY;

    public static final String PROJECT_JAVA_APPLICATION = "Java Application";
    public static final String PROJECT_UML_PLATFORM_INDEPENDENT_MODEL = ANALYSIS_UML_PROJECT_LABEL;
    public static final String PROJECT_UML_JAVA_PLATFORM_MODEL = JAVA_UML_PROJECT_LABEL;
    public static final String PROJECT_UML_JAVA_REVERSE_ENGINEERING = RE_UML_PROJECT_LABEL;
    public static final String PROJECT_UML_IMPORTED_ROSE_MODEL = RM_UML_PROJECT_LABEL;
    
    
}
