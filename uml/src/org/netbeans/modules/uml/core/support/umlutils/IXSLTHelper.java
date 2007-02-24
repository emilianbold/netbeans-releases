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

package org.netbeans.modules.uml.core.support.umlutils;

import org.dom4j.Node;

public interface IXSLTHelper {
  public String getValueFromNode(Object pDisp, String sXpath);
  public String getValueFromNodeBasedOnPreference(Object pDisp, String sXpath, String sPref, String sPrefValue);
  public String getValueFromExpansionVariable(Object pDisp, String sVar);
  public String getPreferenceValue(String sPref);
  public String translateColons(String sValue);
  public String translateNewLines(String sValue);
  public String translateRelativePath(Object pDisp, String sValue);
  public String createWebReportHeading(Object pDisp, String sTitles, String sPaths);
  public String getFormatString(Object pDisp);
  public String getSourceCodeDirectory(Object pDisp);
  public String getProjectName(Object pDisp);
  public String calculateRelativePath(String sDir, String sFile);
  public String calculateWhereArtifactCopiedTo(String sFile, String sDir);
  public String getArtifactFileName(Object pDisp);
  public String getUnmarkedDocumentation(Object pDisp);
  public String getAllMarkedDocumentation(Object pDisp);
  public String getMarkedDocumentation(Object pDisp, String sMark);
}