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

package org.netbeans.modules.uml.core.support.umlutils;

import com.sun.org.apache.xml.internal.dtm.ref.DTMNodeProxy;
import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.List;
import java.util.Stack;
import java.util.StringTokenizer;

//import org.apache.xml.dtm.ref.DTMNodeIterator;
import com.sun.org.apache.xml.internal.dtm.ref.DTMNodeIterator;
import org.dom4j.Branch;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.dom.DOMNodeHelper;
import org.dom4j.Document;
import org.w3c.dom.NodeList;

import org.netbeans.modules.uml.core.coreapplication.ICoreProduct;
import org.netbeans.modules.uml.core.coreapplication.IPreferenceManager2;
import org.netbeans.modules.uml.core.generativeframework.ITemplateManager;
import org.netbeans.modules.uml.core.metamodel.core.foundation.FactoryRetriever;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPackage;
import org.netbeans.modules.uml.core.metamodel.core.foundation.UMLXMLManip;
import org.netbeans.modules.uml.core.support.umlsupport.FileSysManip;
import org.netbeans.modules.uml.core.support.umlsupport.ProductRetriever;
import org.netbeans.modules.uml.core.support.umlsupport.StringUtilities;
import org.netbeans.modules.uml.core.support.umlsupport.URILocator;
import org.netbeans.modules.uml.core.support.umlsupport.XMLManip;
import org.netbeans.modules.uml.core.support.Debug;

public class XSLTHelper //implements IXSLTHelper
{
   private static Stack < Document > mActiveDocuments = new Stack <Document>();
   
   public XSLTHelper()
   {
   }
   
   public final static void pushActiveDocument(Document doc)
   {
      //mActiveDocument = doc;
       mActiveDocuments.push(doc);
   }
   
   public final static void popActiveDocument()
   {
       mActiveDocuments.pop();
   }
   
   public final static Document getDocument()
   {
       Document retVal = null;
       try
       {
           retVal = mActiveDocuments.peek();
       }
       catch(EmptyStackException e)
       {
           retVal = null;
       }
      return retVal;
   }
   
   /**
    * Retrieves the value found at the xpath in the passed in node/nodes.  If it is
    * a list, then the values are appended in a "|" delimited string.
    *
    * WARNING:  Using this xpath lookup with xml attributes that contain ids
    * may not work when the project has been divided, version controlled due to the
    * fact that the id lookup may be in another file and the xsl function id(xxx)
    * assumes that everything is loaded in the dom document (which when divided or version
    * controlled it may not be loaded).
    *
    *
    * @param pDisp[in]			The xml dom node or node list
    * @param sXpath[in]			The xpath query to execute on the dom node or nodes
    * @param sOutValue[out]	The string of the result of the query (if one node) or
    *									a "|" delimited string if a node list
    *
    * @return HRESULT
    *
    */
   public static String getValueFromNode(Object pDisp, String sXpath)
   {
      String str = "";
    
      try
      {
          if (pDisp != null)
          {
             if (pDisp instanceof DTMNodeIterator)
             {
                DTMNodeIterator pList = (DTMNodeIterator)pDisp;
                if (pList != null)
                {
                   // if what is passed in is an xml node list
                   // we are going to loop through each one
                   // executing the passed in query on each one
                   // and concatenate all the values into a "|" delimited string
                   org.w3c.dom.Node w3cNode = pList.nextNode();
                   while (w3cNode != null)
                   {
                      Node n = (Node)w3cNode;
                      Node pResultNode = n.selectSingleNode(sXpath);
                      if (pResultNode != null)
                      {
                         String tempStr = pResultNode.getText();
                         if (tempStr != null && tempStr.length() > 0)
                         {
                            if (str.length() > 0)
                            {
                               str += "|";
                            }
                            str += tempStr;
                         }
                      }
                      w3cNode = pList.nextNode();;
                   }
                }
             }
          }
      }
      catch(Throwable t)
      {
          t.printStackTrace();
      }
      return str;
   }
   /**
    * Retrieves the value found at the xpath in the passed in node/nodes.  If it is
    * a list, then the values are appended in a "|" delimited string.
    *
    * A preference will be checked for the sPrefValue and this information will only be
    * returned if the preference criteria is met.
    *
    * WARNING:  Using this xpath lookup with xml attributes that contain ids
    * may not work when the project has been divided, version controlled due to the
    * fact that the id lookup may be in another file and the xsl function id(xxx)
    * assumes that everything is loaded in the dom document (which when divided or version
    * controlled it may not be loaded).
    *
    * @param pDisp[in]			The xml dom node or node list
    * @param sXpath[in]			The xpath query to execute on the dom node or nodes
    * @param sPref[in]			The full path to the preference in the preference file
    * @param sPrefValue[in]	The preference value to match in order to perform the query
    * @param sOutValue[out]	The string of the result of the query (if one node) or
    *									a "|" delimited string if a node list
    *
    * @return HRESULT
    *
    */
   public static String getValueFromNodeBasedOnPreference(Object pDisp, String sXpath, String sPref, String sPrefValue)
   {
      String str = "";
      try
      {
      ICoreProduct prod = ProductRetriever.retrieveProduct();
      if (prod != null)
      {
         IPreferenceManager2 prefMgr = prod.getPreferenceManager();
         if (prefMgr != null)
         {
            String result = prefMgr.getPreferenceValueWithFullPath( sPref );
            if (result != null)
            {
               if (result.equals(sPrefValue))
               {
                  if (pDisp != null)
                  {
                     if (pDisp instanceof DTMNodeIterator)
                     {
                        DTMNodeIterator pList = (DTMNodeIterator)pDisp;
                        if (pList != null)
                        {
                           // if what is passed in is an xml node list
                           // we are going to loop through each one
                           // executing the passed in query on each one
                           // and concatenate all the values into a "|" delimited string
                           org.w3c.dom.Node w3cNode = pList.nextNode();
                           while (w3cNode != null)
                           {
                              Node n = (Node)w3cNode;
                              Node pResultNode = n.selectSingleNode(sXpath);
                              if (pResultNode != null)
                              {
                                 String resultStr = pResultNode.getText();
                                 if (resultStr != null && resultStr.length() > 0)
                                 {
                                    if (str.length() > 0)
                                    {
                                       str += "|";
                                    }
                                    str += resultStr;
                                 }
                              }
                              w3cNode = pList.nextNode();;
                           }
                        }
                     }
                  }
               }
               else
               {
                  // blank out the return result because there isn't an error
                  // we are just not supposed to return anything because the preference
                  // didn't match
               }
            }
         }
      }
      }
      catch(Throwable t)
      {
          t.printStackTrace();
      }
      return str;
   }
   /**
    * Executes the passed in expansion variable on the node/nodes and returns the result
    * of the expansion variable.
    *
    *
    * @param pDisp[in]			The xml dom node or node list
    * @param sVar[in]			The expansion variable name to execute
    * @param sOutValue[out]	The string of the result of the query (if one node) or
    *									a "|" delimited string if a node list
    *
    * @return HRESULT
    */
   public static String getValueFromExpansionVariable(Object pDisp, String sVar)
   {
      String str = "";
      try
      {
      ICoreProduct prod = ProductRetriever.retrieveProduct();
      if (prod != null)
      {
         ITemplateManager pTemplateMgr = prod.getTemplateManager();
         if (pTemplateMgr != null)
         {
            if (pDisp != null)
            {
               if (pDisp instanceof DTMNodeIterator)
               {
                  DTMNodeIterator pList = (DTMNodeIterator)pDisp;
                  if (pList != null)
                  {
                     org.w3c.dom.Node w3cNode = pList.nextNode();
                     while (w3cNode != null)
                     {
                        Node n = (Node)w3cNode;
                        String resultStr  = pTemplateMgr.expandVariableWithNode(sVar, n);
                        if (resultStr != null && resultStr.length() > 0)
                        {
                           if (str.length() > 0)
                           {
                              str += "|";
                           }
                           str += resultStr;
                        }
                        w3cNode = pList.nextNode();;
                     }
                  }
               }
               else if(pDisp instanceof DTMNodeProxy)
               {
                  // This will cause a exception.  I want it to be thrown so we
                  // can handle the formating via the next formatting attempt.
                  str  = pTemplateMgr.expandVariableWithNode(sVar, new Dom4JNodeProxy((DTMNodeProxy)pDisp));
               }
            }
         }
      }
      }
      catch(Throwable t)
      {
          t.printStackTrace();
      }
      return str;
   }
   
   /**
    * Retrieves the value for the passed in preference
    *
    * @param sPref[in]			The full path to the preference in the preference file
    * @param sOutValue[out]	The preference value
    *
    * @return HRESULT
    *
    */
   public static String getPreferenceValue(String sPref)
   {
      String str = "";
      try
      {
      ICoreProduct prod = ProductRetriever.retrieveProduct();
      if (prod != null)
      {
         IPreferenceManager2 prefMgr = prod.getPreferenceManager();
         if (prefMgr != null)
         {
            str = prefMgr.getPreferenceValueWithFullPath( sPref );
         }
      }
      }
      catch(Throwable t)
      {
          t.printStackTrace();
      }
      return str;
   }
   
   public static String translateColons(String sValue)
   {
      String str = "";
      try
      {
      if (sValue != null && sValue.length() > 0)
      {
         // currently we are only having to check strings that have &#58; in them
         // which represents the ":"
         // we couldn't do this in the xslt because the translate function was messing
         // up when hitting the 5 or the 8
         if (sValue.indexOf("&#58;") > -1)
         {
            str = StringUtilities.replaceAllSubstrings(sValue, "&#58;", ":");
         }
         else
         {
            str = sValue;
         }
      }
      }
      catch(Throwable t)
      {
          t.printStackTrace();
      }
      return str;
   }
   public static String translateNewLines(String sValue)
   {
      String str = "";
      try
      {
      if (sValue != null && sValue.length() > 0)
      {
         if (sValue.indexOf("\n") > -1)
         {
            str = StringUtilities.replaceAllSubstrings(sValue, "\n", "<br/>");
         }
         else
         {
            str = sValue;
         }
      }
      }
      catch(Throwable t)
      {
          t.printStackTrace();
      }
      return str;
   }
   public static String translateRelativePath(Object pDisp, String sValue)
   {
      String str = "";
      try
      {
      if (pDisp != null)
      {
         if (pDisp instanceof DTMNodeIterator)
         {
            DTMNodeIterator pList = (DTMNodeIterator)pDisp;
            if (pList != null)
            {
               if (sValue != null && sValue.length() > 0)
               {
                  org.w3c.dom.Node w3cNode = pList.nextNode();
                  while (w3cNode != null)
                  {
                     Node n = (Node)w3cNode;
                     String sXpath = "ancestor::UML:Project/@fileName";
                     Node pResultNode = n.selectSingleNode(sXpath);
                     if (pResultNode != null)
                     {
                        String resultStr = pResultNode.getText();
                        String outStr = translateColons(resultStr);
                        String justDirectory = StringUtilities.getPath(outStr);
                        str = FileSysManip.retrieveAbsolutePath(sValue, justDirectory);
                     }
                     // should only be one node in the stack
                     w3cNode = null;
                  }
               }
            }
         }
      }
      }
      catch(Throwable t)
      {
          t.printStackTrace();
      }
      return str;
   }
   public static String createWebReportHeading(Object pDisp, String sTitles, String sPaths)
   {
      String message=org.openide.util.NbBundle.getMessage(XSLTHelper.class,"Details"); //Jyothi: Fix for Bug#6324300
      String str = "<a href=\"#Details\">" + message + "</a>";

      try
      {
      //	this will need to be localized once the web report is going thru here to localize
      if (pDisp != null)
      {
         if (pDisp instanceof DTMNodeIterator)
         {
            DTMNodeIterator pList = (DTMNodeIterator)pDisp;
            if (pList != null)
            {
               org.w3c.dom.Node w3cNode = pList.nextNode();
               while (w3cNode != null)
               {
                  Node n = (Node)w3cNode;
                  StringTokenizer titleToken = new StringTokenizer(sTitles, "|");
                  StringTokenizer pathToken = new StringTokenizer(sPaths, "|");
                  String titleStr = titleToken.nextToken();
                  String pathStr = pathToken.nextToken();
                  while (titleStr != null && titleStr.length() > 0)
                  {
                     if (pathStr != null && pathStr.length() > 0)
                     {
                        String path = StringUtilities.replaceAllSubstrings(pathStr, "%%", "\'");
                        List pResult = n.selectNodes(path);
                        if (pResult != null)
                        {
                           int resCount = pResult.size();
                           if (resCount > 0)
                           {
                              str += "|";
                              str += "<a href=\"#";
                              str += titleStr;
                              str += "\">";
                              str += titleStr;
                              str += "</a>";
                           }
                        }
                     }
                     if (titleToken.hasMoreTokens())
                     {
                        titleStr = titleToken.nextToken();
                        pathStr = pathToken.nextToken();
                     }
                     else
                     {
                        titleStr = null;
                     }
                  }
                  // should only be one node in the stack
                  w3cNode = null;
               }
            }
         }
      }
      }
      catch(Throwable t)
      {
          t.printStackTrace();
      }      
      return str;
   }
   public static String getFormatString(Object pDisp)
   {
      String str = "";      
      try
      {
          if (pDisp != null)
          {
             if (pDisp instanceof DTMNodeIterator)
             {
                DTMNodeIterator pList = (DTMNodeIterator)pDisp;
                if (pList != null)
                {
                   // if what is passed in is an xml node list
                   // we are going to loop through each one
                   // executing the passed in query on each one
                   // and concatenate all the values into a "|" delimited string
                   org.w3c.dom.Node w3cNode = pList.nextNode();
                   while (w3cNode != null)
                   {
                      IDataFormatter pFormatter = ProductRetriever.retrieveProduct().getDataFormatter();
                      if (pFormatter != null)
                      {
                         String xslFile = pFormatter.getFormatStringFile(w3cNode);
                         if (xslFile.length() > 0)
                         {
                            String nodeName = w3cNode.getNodeName();
                            String key = nodeName;
                            if (key.indexOf("UML:") > -1)
                            {
                               key = nodeName.substring(4);
                            }
                            pFormatter.addScript(key, xslFile);
                            str = pFormatter.formatNode(w3cNode, key);
                         /* 6.2 release
                         ILanguage pLang = pFormatter.getActiveLanguage(w3cNode);
                         if (pLang != null)
                         {
                            String langName = pLang.getName();
                            key += langName;
                            pFormatter.addScript(key, xslFile);
                            str = pFormatter.formatNodeW3C(w3cNode, key);
                         }
                          */
                         }
                      }
                      // going to break after the first one (should only be one)
                      w3cNode = null;
                   }
                }
             }
             else if(pDisp instanceof DTMNodeProxy)
             {
                 IDataFormatter pFormatter = ProductRetriever.retrieveProduct().getDataFormatter();
                 if (pFormatter != null)
                 {
                     Node node = new W3CNodeProxy((DTMNodeProxy)pDisp);
                     String xslFile = pFormatter.getFormatStringFile(node);
                     if (xslFile.length() > 0)
                     {
                         String nodeName = node.getName();
                         String key = nodeName;
                         if (key.indexOf("UML:") > -1)
                         {
                             key = nodeName.substring(4);
                         }
                         pFormatter.addScript(key, xslFile);
                         str = pFormatter.formatNode(node, key);
                         /* 6.2 release
                         ILanguage pLang = pFormatter.getActiveLanguage(w3cNode);
                         if (pLang != null)
                         {
                            String langName = pLang.getName();
                            key += langName;
                            pFormatter.addScript(key, xslFile);
                            str = pFormatter.formatNodeW3C(w3cNode, key);
                         }
                          */
                     }
                 }
                // This will cause a exception.  I want it to be thrown so we
                // can handle the formating via the next formatting attempt.
    //            str  = pTemplateMgr.expandVariableWithNode(sVar, );
             }
          }
      }
      catch(Throwable t)
      {
          t.printStackTrace();
      }
      return str;
   }
   public static String getSourceCodeDirectory(Object pDisp)
   {
      String str = "";
      try
      {
      if (pDisp != null)
      {
         if (pDisp instanceof DTMNodeIterator)
         {
            DTMNodeIterator pList = (DTMNodeIterator)pDisp;
            if (pList != null)
            {
               org.w3c.dom.Node w3cNode = pList.nextNode();
               while (w3cNode != null)
               {
                  Node n = (Node)w3cNode;
                  FactoryRetriever retriever = FactoryRetriever.instance();
                  if( retriever != null)
                  {
                     String nodeName = n.getName();
                     if( nodeName != null && nodeName.length() > 0 )
                     {
                        int pos = nodeName.indexOf(":");
                        if ( pos > -1)
                        {
                           nodeName = nodeName.substring( pos + 1 );
                        }
                     }
                     Object obj = retriever.createTypeAndFill(nodeName, n);
                     if( obj != null )
                     {
                        if (obj instanceof IPackage)
                        {
                           IPackage pPack = (IPackage)obj;
                           str = pPack.getSourceDir();
                        }
                     }
                  }
                  // going to break after the first one (should only be one)
                  w3cNode = null;
               }
            }
         }
      }
      }
      catch(Throwable t)
      {
          t.printStackTrace();
      }
      return str;
   }
   public static String getProjectName(Object pDisp)
   {
      String str = "";
      try
      {
      if (pDisp != null)
      {
         if (pDisp instanceof DTMNodeIterator)
         {
            DTMNodeIterator pList = (DTMNodeIterator)pDisp;
            if (pList != null)
            {
               org.w3c.dom.Node w3cNode = pList.nextNode();
               while (w3cNode != null)
               {
                  Node n = (Node)w3cNode;
                  String sXpath = "@href";
                  Node pResultNode = n.selectSingleNode(sXpath);
                  if (pResultNode != null)
                  {
                     String text = pResultNode.getText();
                     // we should have something like __uri_..\proj2\proj2.etd#id('DCE...
                     // so, split on .etd
                     StringTokenizer token = new StringTokenizer(text, ".etd");
                     // now we should have __uri_..\proj2\proj2
                     String tokenStr = token.nextToken();
                     // now it should be broken up by the "\", so we want the last one
                     StringTokenizer token2 = new StringTokenizer(tokenStr, "\\");
                     String tokenStr2 = token2.nextToken();
                     while (token2.hasMoreTokens())
                     {
                        tokenStr2 = token2.nextToken();
                     }
                     str = tokenStr2;
                  }
                  // going to break after the first one (should only be one)
                  w3cNode = null;
               }
            }
         }
      }
      }
      catch(Throwable t)
      {
          t.printStackTrace();
      }
      return str;
   }
   public static String calculateRelativePath(String sDir, String sFile)
   {
      String str = "";
      try
      {
      String transStr = translateColons(sFile);
      if (transStr != null && transStr.length() > 0)
      {
         str = FileSysManip.retrieveRelativePath(sFile, sDir);
      }
      }
      catch(Throwable t)
      {
          t.printStackTrace();
      }
      return str;
   }
   public static String calculateWhereArtifactCopiedTo(String sFile, String sDir)
   {
      String str = "";
      try
      {
      String fileName = translateColons(sFile);
      if (fileName != null && fileName.length() > 0)
      {
         str = sDir;
         str += StringUtilities.getPath(fileName);
         str += StringUtilities.getFileName(fileName);
         str += StringUtilities.getExtension(fileName);
      }
      }
      catch(Throwable t)
      {
          t.printStackTrace();
      }
      return str;
   }
   public static String getArtifactFileName(Object pDisp)
   {
      String str = "";
      try
      {
      if (pDisp != null)
      {
         if (pDisp instanceof DTMNodeIterator)
         {
            DTMNodeIterator pList = (DTMNodeIterator)pDisp;
            if (pList != null)
            {
               org.w3c.dom.Node w3cNode = pList.nextNode();
               while (w3cNode != null)
               {
                  Node n = (Node)w3cNode;
                  String sXpath = "@sourcefile";
                  Node pResultNode = n.selectSingleNode(sXpath);
                  if (pResultNode != null)
                  {
                     String text = pResultNode.getText();
                     // we should have something like temp\classA.java
                     // so, split on last "\"
                     if (text != null && text.length() > 0)
                     {
                        int pos = text.lastIndexOf('\\');
                        if( pos > -1 )
                        {
                           str = text.substring( pos + 1 );
                        }
                     }
                  }
                  // going to break after the first one (should only be one)
                  w3cNode = null;
               }
            }
         }
      }
      }
      catch(Throwable t)
      {
          t.printStackTrace();
      }
      return str;
   }
   public static String getUnmarkedDocumentation(Object pDisp)
   {
      String str = "";
      return str;
   }
   public static String getAllMarkedDocumentation(Object pDisp)
   {
      String str = "";
      return str;
   }
   public static String getMarkedDocumentation(Object pDisp, String sMark)
   {
      String str = "";
      return str;
   }
   public static String calculateHREF(Object pDisp, String findID, String bFlatDir)
   {
      String str = "";
      try
      {
      if (pDisp != null)
      {
         if (pDisp instanceof DTMNodeIterator)
         {
            DTMNodeIterator pList = (DTMNodeIterator)pDisp;
            if (pList != null)
            {
               org.w3c.dom.Node w3cNode = pList.nextNode();
               while (w3cNode != null)
               {
                  Node curNode = (Node)w3cNode;
                  str = XSLTHelper.calculateHREF(curNode, findID, bFlatDir);
                  // going to break after the first one (should only be one)
                  w3cNode = null;
               }
            }
         }
         else if(pDisp instanceof DTMNodeProxy)
         {
            Node node = new W3CNodeProxy((DTMNodeProxy)pDisp);
            if(node != null)
            {
               str = XSLTHelper.calculateHREF(node, findID, bFlatDir);
            }
         }
      }
      }
      catch(Throwable t)
      {
          t.printStackTrace();
      }
      return str;
   }
   private static String determinePath(Node toFindNode)
   {
      String str = "";
      try
      {
      if (toFindNode != null)
      {
         String query = "ancestor::UML:Package";
         List list = toFindNode.selectNodes(query);
         if (list != null)
         {
            int cnt = list.size();
            for (int x = 0; x < cnt; x++)
            {
               Object obj = list.get(x);
               Node n = (Node)obj;
               if (n != null)
               {
                  String name = XMLManip.getAttributeValue(n, "name");
                  if (str.length() > 0)
                  {
                     str = FileSysManip.addBackslash(str);
                  }
                  str += name;
               }
            }
         }
      }
      }
      catch(Throwable t)
      {
          t.printStackTrace(Debug.out);
      }
      return str;
   }
   private static String calculateRelPath(String p1)
   {
      String str = "";
      try
      {
      if (p1 != null && p1.length() > 0)
      {
         // break up the string
         ETList<String> strs = StringUtilities.splitOnDelimiter(p1, java.io.File.separator);
         int count = strs.size();
         for (int x = 0; x < count; x++)
         {
            str += FileSysManip.addBackslash("..");
         }
      }
      }
      catch(Throwable t)
      {
          t.printStackTrace();
      }
      return str;
   }
   public static NodeList convertIDListToNodeList(Object pDisp, String idList)
   {
      NodeList nodeList = null;
      try
      {
      if (pDisp != null)
      {
         if (pDisp instanceof DTMNodeIterator)
         {
            DTMNodeIterator pList = (DTMNodeIterator)pDisp;
            if (pList != null)
            {
               ArrayList l = new ArrayList();
               // if what is passed in is an xml node list
               // we are going to loop through each one
               // executing the passed in query on each one
               // and concatenate all the values into a "|" delimited string
               org.w3c.dom.Node w3cNode = pList.nextNode();
               while (w3cNode != null)
               {
                  Node n = (Node)w3cNode;
                  ETList<String> strs = StringUtilities.splitOnDelimiter(idList, " ");
                  int count = strs.size();
                  for (int x = 0; x < count; x++)
                  {
                     String str = strs.get(x);
                     String decoded = URILocator.decodeURI(str);
                     Node foundNode = UMLXMLManip.findElementByID(n, decoded);
                     if (foundNode != null)
                     {
                        l.add(foundNode);
                     }
                  }
                  w3cNode = null;
               }
               nodeList = DOMNodeHelper.createNodeList(l);
            }
         }
      }
      }
      catch(Throwable t)
      {
          t.printStackTrace();
      }
      return nodeList;
   }
   public static boolean getProjectAliased(Object pDisp)
   {
      boolean bFlag = false;
      try
      {
      if (pDisp != null)
      {
         if (pDisp instanceof DTMNodeIterator)
         {
            DTMNodeIterator pList = (DTMNodeIterator)pDisp;
            if (pList != null)
            {
               org.w3c.dom.Node w3cNode = pList.nextNode();
               while (w3cNode != null)
               {
                  Node n = (Node)w3cNode;
                  String sXpath = "ancestor::UML:Project/@embt__Aliased";
                  Node pResultNode = n.selectSingleNode(sXpath);
                  if (pResultNode != null)
                  {
                     String text = pResultNode.getText();
                     if (text != null && text.equals("on"))
                     {
                        bFlag = true;
                     }
                  }
                  // going to break after the first one (should only be one)
                  w3cNode = null;
               }
            }
         }
      }
      }
      catch(Throwable t)
      {
          t.printStackTrace();
      }
      return bFlag;
   }
  /*
   * Special processing to take information from the xsl scripts and return it in a string format so that
   * the xsl script can process it.  We were using convertIDListToNodeList in the scripts but refer to bug
   * 2211 or 2507 to see why we switched to using this.  It has to do with the xml parser that we are using
   * and whether or not the doctype dtd is commented out in our etd file.  If it is commented out, then
   * the convertIDListToNodeList throws an exception and the script is not processed.  This routine is
   * the workaround.
   *
   * pDisp  - the current xml node in the xsl processor
   * idList - the list of xmi ids to convert to nodes and return their "names" and href links in string format
   * bFlatDir - a flag set by the web report whether or not the user is generating the report by package structure or not
   */
   public static String convertIDListToStringListName(Object pDisp, String idList, String bFlatDir)
   {
      String str = null;
      try
      {
      if (pDisp != null)
      {
         if (pDisp instanceof DTMNodeIterator)
         {
            DTMNodeIterator pList = (DTMNodeIterator)pDisp;
            if (pList != null)
            {
               // if what is passed in is an xml node list
               // we are going to loop through each one
               // executing the passed in query on each one
               // and concatenate all the values into a "|" delimited string
               org.w3c.dom.Node w3cNode = pList.nextNode();
               while (w3cNode != null)
               {
                  Node n = (Node)w3cNode;
                  ETList<String> strs = StringUtilities.splitOnDelimiter(idList, " ");
                  int count = strs.size();
                  for (int x = 0; x < count; x++)
                  {
                     // find the element with this id
                     String item = strs.get(x);
                     String decoded = URILocator.decodeURI(item);
                     Node foundNode = UMLXMLManip.findElementByID(n, decoded);
                     if (foundNode != null)
                     {
                        // now get its name attribute, use its name if there is one, otherwise use its node type
                        String type = foundNode.getName();
                        Node nameNode = foundNode.selectSingleNode("@name");
                        String name = "";
                        if (nameNode != null)
                        {
                           name = nameNode.getText();
                        }
                        else
                        {
                           name = type;
                        }
                        // determines what the href to this node will be
                        String href = XSLTHelper.calculateHREF(n, decoded, bFlatDir);
                        // format the results so the xsl script can interpret them
                        if (str == null)
                        {
                           str = name + "?" + href;
                        }
                        else
                        {
                           str += "^" + name + "?" + href;
                        }
                     }
                  }
                  w3cNode = null;
               }
            }
         }
      }
      }
      catch(Throwable t)
      {
          t.printStackTrace();
      }
      return str;
   }
  /*
   * Special processing to take information from the xsl scripts and return it in a string format so that
   * the xsl script can process it.  We were using convertIDListToNodeList in the scripts but refer to bug
   * 2211 or 2507 to see why we switched to using this.  It has to do with the xml parser that we are using
   * and whether or not the doctype dtd is commented out in our etd file.  If it is commented out, then
   * the convertIDListToNodeList throws an exception and the script is not processed.  This routine is
   * the workaround.
   *
   * pDisp  - the current xml node in the xsl processor
   * idList - the list of xmi ids to convert to nodes and return their "names" and href links in string format
   * bFlatDir - a flag set by the web report whether or not the user is generating the report by package structure or not
   * attrName - the name of the attribute to use its value when doing the string formatting for the xsl
   */
   public static String convertIDListToStringListAttribute(Object pDisp, String idList, String bFlatDir, String attrName)
   {
      String str = null;
      try
      {
      if (pDisp != null)
      {
         if (pDisp instanceof DTMNodeIterator)
         {
            DTMNodeIterator pList = (DTMNodeIterator)pDisp;
            if (pList != null)
            {
               // if what is passed in is an xml node list
               // we are going to loop through each one
               // executing the passed in query on each one
               // and concatenate all the values into a "|" delimited string
               org.w3c.dom.Node w3cNode = pList.nextNode();
               while (w3cNode != null)
               {
                  Node n = (Node)w3cNode;
                  ETList<String> strs = StringUtilities.splitOnDelimiter(idList, " ");
                  int count = strs.size();
                  for (int x = 0; x < count; x++)
                  {
                     String item = strs.get(x);
                     String decoded = URILocator.decodeURI(item);
                     Node foundNode = UMLXMLManip.findElementByID(n, decoded);
                     if (foundNode != null)
                     {
                        String attr = "@" + attrName;
                        Node attrNode = foundNode.selectSingleNode(attr);
                        if (attrNode != null)
                        {
                           String value = attrNode.getText();
                           String decoded2 = URILocator.decodeURI(value);
                           Node foundNode2 = UMLXMLManip.findElementByID(foundNode, decoded2);
                           if (foundNode2 != null)
                           {
                              String type = foundNode2.getName();
                              Node nameNode = foundNode2.selectSingleNode("@name");
                              String name = "";
                              if (nameNode != null)
                              {
                                 name = nameNode.getText();
                              }
                              else
                              {
                                 name = type;
                              }
                              String href = XSLTHelper.calculateHREF(n, decoded2, bFlatDir);
                              if (str == null)
                              {
                                 str = name + "?" + href;
                              }
                              else
                              {
                                 str += "^" + name + "?" + href;
                              }
                           }
                        }
                     }
                  }
                  w3cNode = null;
               }
            }
         }
      }
      }
      catch(Throwable t)
      {
          t.printStackTrace();
      }
      return str;
   }
   
   private static String calculateHREF(Node pNode, String findID, String bFlatDir)
   {
      String str = "";
      try
      {
      if (pNode != null)
      {
         String id1 = StringUtilities.replaceAllSubstrings(findID, "-", "");
         String id2 = StringUtilities.replaceAllSubstrings(id1, ".", "");
         if (id2 != null && id2.length() > 0)
         {
            String id3 = id2 + ".html";
            String idtrans1 = StringUtilities.replaceAllSubstrings(id3, "/", "_");
            String idtrans2 = StringUtilities.replaceAllSubstrings(idtrans1, "&quot;", "_");
            String idtrans3 = StringUtilities.replaceAllSubstrings(idtrans2, ":", "_");
            String idtrans4 = StringUtilities.replaceAllSubstrings(idtrans3, "*", "_");
            String idtrans5 = StringUtilities.replaceAllSubstrings(idtrans4, "?", "_");
            String idtrans6 = StringUtilities.replaceAllSubstrings(idtrans5, "&lt;", "_");
            String idtrans7 = StringUtilities.replaceAllSubstrings(idtrans6, "&gt;", "_");
            String idtrans8 = StringUtilities.replaceAllSubstrings(idtrans7, "|", "_");
            id3 = idtrans8;
            // Don't have to do any calculations if the preference is set to generate the files in a flat structure.  All of the files are in the same
            // directory so there doesn't need to be any path calculations
            if (bFlatDir.equals("false"))
            {
               // This builds a string representing the path of the curNode (will be based on project\package structure)
               String x = determinePath(pNode);
               // This builds a string representing the path of the findNode (will be based on project\package structure)
               Node findNode = UMLXMLManip.findElementByID(pNode, findID);
               String x2 = determinePath(findNode);
               // Given the path of the current node, this determines how many ..\ need to be placed to get back to the root
               // because we are then going to append the path to the root
               String relPath = calculateRelPath(x);
               String id4 = "";
               if (relPath != null && relPath.length() > 0)
               {
                  id4 += relPath;
               }
               if (x2 != null && x2.length() > 0)
               {
                  id4 += FileSysManip.addBackslash(x2);
               }
               id4 += id3;
               str = id4;
            }
            else
            {
               str = id3;
            }
         }
      }
      }
      catch(Throwable t)
      {
          t.printStackTrace();
      }
      return str;
   }
   
}
