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


/*
 *
 * Created on Jun 11, 2003
 * @author Trey Spiva
 */
package org.netbeans.modules.uml.ui.support.diagramsupport;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;

import java.util.logging.Level;
import org.netbeans.modules.uml.common.generics.ETPairT;
import org.netbeans.modules.uml.core.IApplication;
import org.netbeans.modules.uml.core.coreapplication.IDiagramCleanupManager;
import org.netbeans.modules.uml.core.eventframework.IEventPayload;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IVersionableElement;
import org.netbeans.modules.uml.core.metamodel.diagrams.DiagramDetails;
import org.netbeans.modules.uml.core.metamodel.diagrams.IBroadcastAction;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram;
import org.netbeans.modules.uml.core.metamodel.diagrams.IProxyDiagram;
import org.netbeans.modules.uml.core.metamodel.structure.IProject;
import org.netbeans.modules.uml.core.support.UMLLogger;
import org.netbeans.modules.uml.core.support.umlsupport.ExtensionFileFilter;
import org.netbeans.modules.uml.core.support.umlsupport.FileExtensions;
import org.netbeans.modules.uml.core.support.umlsupport.FileSysManip;
import org.netbeans.modules.uml.core.support.umlsupport.ICustomValidator;
import org.netbeans.modules.uml.core.support.umlsupport.StringUtilities;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.core.support.umlutils.ElementLocator;
import org.netbeans.modules.uml.core.support.umlutils.IElementLocator;
import org.netbeans.modules.uml.ui.controls.drawingarea.IDrawingAreaEventDispatcher;
import org.netbeans.modules.uml.ui.controls.drawingarea.ProxyDiagramImpl;
import org.netbeans.modules.uml.ui.support.DispatchHelper;
import org.netbeans.modules.uml.ui.support.ProductHelper;
import org.netbeans.modules.uml.ui.support.applicationmanager.IProductDiagramManager;
import org.netbeans.modules.uml.ui.support.archivesupport.IProductArchive;
import org.netbeans.modules.uml.ui.support.archivesupport.IProductArchiveAttribute;
import org.netbeans.modules.uml.ui.support.archivesupport.IProductArchiveDefinitions;
import org.netbeans.modules.uml.ui.support.archivesupport.IProductArchiveElement;
import org.netbeans.modules.uml.ui.support.archivesupport.ProductArchiveImpl;
import org.netbeans.modules.uml.ui.support.commondialogs.IErrorDialog;
import org.netbeans.modules.uml.ui.swing.commondialogs.SwingErrorDialog;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Trey Spiva
 */
public class ProxyDiagramManager implements IProxyDiagramManager,
        IDiagramCleanupManager,
        ICustomValidator
{
    private static ProxyDiagramManager m_Instance = null;
    
    public ProxyDiagramManager()
    {
    }
    
    public static ProxyDiagramManager instance()
    {
        if (m_Instance == null)
        {
            m_Instance = new ProxyDiagramManager();
        }
        return m_Instance;
    }
    
   /*
    * returns valid diagram name
    */
//    public String getValidDiagramName(String sSuggestedDiagramName)
//    {
//        String newName = sSuggestedDiagramName;
//        boolean isCorrect = false;
//        if (newName != null && newName.length() > 0)
//        {
//            char[] newArr = newName.toCharArray();
//            int count = newName.length();
//            for (int i=0; i<count; i++)
//            {
//                char c = newName.charAt(i);
//                if (!Character.isLetterOrDigit(c))
//                {
//                    if (c == '_' || c=='(' || c==')' || c == '{' || c == '}' || c == '[' || c == ']' || c == ' ')
//                    {
//                        //these are ok
//                    }
//                    else
//                    {
//                        newArr[i] = '_';
//                        isCorrect = false;
//                    }
//                }
//            }
//            newName = String.valueOf(newArr);
//        }
//        return newName;
//    }
    
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.diagramsupport.IProxyDiagramManager#verifyUniqueDiagramName(java.lang.String, java.lang.String)
    */
    public String verifyUniqueDiagramName(String sProjectBaseDirectory, String sProposedDiagramName)
    {
        // TODO Auto-generated method stub
        return null;
    }
    
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.diagramsupport.IProxyDiagramManager#verifyUniqueDiagramName(com.embarcadero.describe.foundation.IElement, java.lang.String)
    */
    public String verifyUniqueDiagramName(IElement pElementInProject, String sProposedDiagramName)
    {
        // TODO Auto-generated method stub
        return null;
    }
    
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.diagramsupport.IProxyDiagramManager#removeDiagram(java.lang.String)
    */
    public void removeDiagram(String sDiagramFullFilename)
    {
        
        IDrawingAreaEventDispatcher dispatcher = getDispatcher();
        
        boolean proceed = true;
        if(dispatcher != null)
        {
            IEventPayload payload = dispatcher.createPayload("DrawingAreaPreFileRemoved");
            proceed = dispatcher.fireDrawingAreaPreFileRemoved(sDiagramFullFilename, payload);
        }
        
        if(proceed == true)
        {
            // The jbuilder gui is threaded so we mark the diagram
            // as not dirty so it doesn't accidentally get re-saved if
            // the close doesn't happen right away.
            IProxyDiagram proxyDiagram = getDiagram(sDiagramFullFilename);
            if(proxyDiagram != null)
            {
                IDiagram diagram = proxyDiagram.getDiagram();
                if(diagram != null)
                {
                    diagram.setIsDirty(false);
                    diagram.setReadOnly(true);
                }
            }
            
            IProductDiagramManager manager = ProductHelper.getProductDiagramManager();
            if(manager != null)
            {
                manager.closeDiagram3(proxyDiagram);
                closedDiagram(proxyDiagram);
            }
        }
    }
    
//    protected void closedDiagram(IProxyDiagram proxyDiagram)
//    {
//        if(proxyDiagram != null)
//        {
//            String diagramFileName =proxyDiagram.getFilename();
//            if((diagramFileName != null) && (diagramFileName.length() > 0))
//            {
//                String etlFilename = FileSysManip.ensureExtension(diagramFileName, FileExtensions.DIAGRAM_LAYOUT_EXT ); //C:\...\UMLProject10\Class Diagram 1_1202347482156.etld"
//                String etlpFilename = FileSysManip.ensureExtension(diagramFileName, FileExtensions.DIAGRAM_PRESENTATION_EXT );
//                
//                String path = StringUtilities.getPath(diagramFileName);
//                String fileName = StringUtilities.getFileName(diagramFileName);
//                path = FileSysManip.addBackslash(path);
//                path += "DiagramBackup";     // "C:\Users\thuy\Documents\NetBeansProjects\UMLProject10\DiagramBackup"
//                String newETLFilename = FileSysManip.createFullPath(path, fileName, FileExtensions.DIAGRAM_LAYOUT_EXT); //C:\...UMLProject10\DiagramBackup\Class Diagram 1_1202347482156.etld
//                String newETLPFilename = FileSysManip.createFullPath(path, fileName, FileExtensions.DIAGRAM_PRESENTATION_EXT);
//                
//                File backupPath = new File(path);
//                boolean backupExist = true;
//                if(backupPath.exists() == false)
//                {
//                    backupExist = backupPath.mkdirs();
//                }
//                
//                if((backupExist == true) &&
//                        (etlFilename.length() > 0) &&//
//                        (newETLFilename.length() > 0) &&
//                        (etlpFilename.length() > 0) &&
//                        (newETLPFilename.length() > 0))
//                {
//                    File etlFile = new File(etlFilename);
//                    etlFile.renameTo(new File(newETLFilename));
//                    
//                    File etlpFile = new File(etlpFilename);
//                    etlpFile.renameTo(new File(newETLPFilename));
//                    
//                    // Make sure that the original file is removed.
//                    File oldETLFile = new File(etlFilename);
//                    if(oldETLFile.exists() == true)
//                    {
//                        oldETLFile.delete();
//                        oldETLFile = null;
//                    }
//                    
//                    File oldETLPFile = new File(etlpFilename);
//                    if(oldETLPFile.exists() == true)
//                    {
//                        oldETLPFile.delete();
//                        oldETLPFile = null;
//                    }
//                }
//                
//                IDrawingAreaEventDispatcher dispatcher = getDispatcher();
//                if(dispatcher != null)
//                {
//                    IEventPayload payload = dispatcher.createPayload("DrawingAreaFileRemoved");
//                    dispatcher.fireDrawingAreaFileRemoved(diagramFileName, payload);
//                }
//                
//            }
//        }
//    }
    
    protected void closedDiagram(IProxyDiagram proxyDiagram)
    {
        if(proxyDiagram != null)
        {
            String diagramFileName = proxyDiagram.getFilename();
            if((diagramFileName != null) && (diagramFileName.length() > 0))
            {
                String etlFilename = FileSysManip.ensureExtension(diagramFileName, FileExtensions.DIAGRAM_LAYOUT_EXT ); //C:\...\UMLProject10\Class Diagram 1_1202347482156.etld"
                String etlpFilename = FileSysManip.ensureExtension(diagramFileName, FileExtensions.DIAGRAM_PRESENTATION_EXT );
                
                //FileObject diagFO = FileUtil.toFileObject(new File(diagramFileName));
                FileObject etldFO = FileUtil.toFileObject(new File(etlFilename));
                FileObject etlpFO = FileUtil.toFileObject(new File(etlpFilename));
                
                if (etldFO != null && etlpFO != null)
                {
                    try {
                        FileObject parentFolder = etldFO.getParent();
                        String fileNameWihoutExt = etldFO.getName();
                        FileObject destFolderFO = parentFolder.getFileObject("DiagramBackup");
                        if (destFolderFO == null) 
                        {
                            destFolderFO = parentFolder.createFolder("DiagramBackup");
                        }

                        // move *.etld and *.etlp to the DiagramBackup folder
                        FileUtil.moveFile(etldFO, destFolderFO, fileNameWihoutExt);
                        FileUtil.moveFile(etlpFO, destFolderFO, fileNameWihoutExt);

                        IDrawingAreaEventDispatcher dispatcher = getDispatcher();
                        if (dispatcher != null) {
                            IEventPayload payload = dispatcher.createPayload("DrawingAreaFileRemoved");
                            dispatcher.fireDrawingAreaFileRemoved(diagramFileName, payload);
                        }
                    } catch (IOException ex) {
                        UMLLogger.logException(ex, Level.WARNING);
                    }
                }
            }
        }
    }
    
    /**
     * @return
     */
    private IDrawingAreaEventDispatcher getDispatcher()
    {
        IDrawingAreaEventDispatcher retVal = null;
        
        DispatchHelper helper = new DispatchHelper();
        retVal = helper.getDrawingAreaDispatcher();
        
        return retVal;
    }
    
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.diagramsupport.IProxyDiagramManager#getDiagramsInDirectory(java.lang.String)
    */
    public ETList<IProxyDiagram> getDiagramsInDirectory(String sProjectBaseDirectory)
    {
        ETList<IProxyDiagram> retVal = new ETArrayList<IProxyDiagram>();
        if (retVal != null)
        {
            if (sProjectBaseDirectory != null && sProjectBaseDirectory.length() > 0)
            {
                ArrayList diagrams = new ArrayList();
                getDiagramsInDirectory(sProjectBaseDirectory, diagrams);
                retVal = vectorToList(diagrams);
            }
        }
        return retVal;
    }
    
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.diagramsupport.IProxyDiagramManager#getDiagramsInDirectory(com.embarcadero.describe.foundation.IElement)
    */
    public ETList<IProxyDiagram> getDiagramsInDirectory(IElement pElementInProject)
    {
        ETList<IProxyDiagram> retVal = new ETArrayList<IProxyDiagram>();
        if (retVal != null)
        {
            if (pElementInProject != null)
            {
                IProject pProject = pElementInProject.getProject();
                if (pProject != null)
                {
                    String baseDirectory = pProject.getBaseDirectory();
                    if (baseDirectory != null && baseDirectory.length() > 0)
                    {
                        retVal = getDiagramsInDirectory(baseDirectory);
                    }
                }
            }
        }
        return retVal;
    }
    
    /**
     * Returns a diagram proxy for this tom filename.  The diagram proxy may represent a closed diagram.
     *
     * @param setlFilename [in] The full filename for the etld file.
     * @param pProxyDiagram [out,retval] The resulting IProxyDiagram used to control this diagram file (and the etlp)
     */
    
    public static java.util.HashMap<String, IProxyDiagram> fileList = new java.util.HashMap<String, IProxyDiagram>();
    public IProxyDiagram getDiagram(String sTOMFilename)
    {
        IProxyDiagram retVal = null;
        if (!fileList.containsKey(sTOMFilename) ) 
        {
            retVal = new ProxyDiagramImpl();
            retVal.setFilename(sTOMFilename);
            fileList.put(sTOMFilename, retVal);
        } 
        else 
        {
            retVal = fileList.get(sTOMFilename);
        }
        return retVal;
    }
    /**
     * Returns a diagram proxy for a diagram with this xmiid.  The diagram proxy may represent a closed diagram.
     *
     * @param sXMIID [in] The XMIID of the diagram
     * @param pProxyDiagram [out,retval] The resulting IProxyDiagram used to control this diagram file (and the etlp).
     * NULL if not found.
     */
    public IProxyDiagram getDiagram2(String sXMIID)
    {
        IProxyDiagram pProxyDiagram = null;
        if (sXMIID != null && sXMIID.length() > 0)
        {
            int count = 0;
            ETList<IProxyDiagram> pAllDiagrams = getDiagramsInWorkspace();
            if (pAllDiagrams != null)
            {
                count = pAllDiagrams.getCount();
            }
            for (int i = 0 ; i < count ; i++)
            {
                IProxyDiagram pThisProxyDiagram = pAllDiagrams.get(i);
                if (pThisProxyDiagram != null)
                {
                    String xThisXMIID = pThisProxyDiagram.getXMIID();
                    if (xThisXMIID != null && xThisXMIID.equals(sXMIID))
                    {
                        pProxyDiagram = pThisProxyDiagram;
                        break;
                    }
                }
            }
        }
        return pProxyDiagram;
    }
    
    /**
     * Returns a diagram proxy for a diagram with this xmiid.  The diagram proxy may represent a closed diagram.
     *
     * @param sXMIID [in] The XMIID of the diagram
     * @param pProxyDiagram [out,retval] The resulting IProxyDiagram used to control this diagram file (and the etlp).
     * NULL if not found.
     */
    public IProxyDiagram getDiagramForXMIID(String sXMIID)
    {
        IProxyDiagram retVal = null;
        ETList<IProxyDiagram> pAllDias = getDiagramsInWorkspace();
        if (pAllDias != null)
        {
            int count = pAllDias.size();
            for (int i=0; i<count; i++)
            {
                IProxyDiagram dia = pAllDias.get(i);
                String xmiid = dia.getXMIID();
                if (xmiid != null && xmiid.equals(sXMIID))
                {
                    retVal = dia;
                    break;
                }
            }
        }
        return retVal;
    }
    
    /**
     * Returns a diagram proxy for this IDiagram.
     *
     * @param pDiagram [in] The IDiagram we need a proxy for
     * @param pProxyDiagram [out,retval] The resulting IProxyDiagram used to control this diagram file (and the etlp).
     * NULL if not found.
     */
    public IProxyDiagram getDiagram(IDiagram pDiagram)
    {
        IProxyDiagram retVal = null;
        if (pDiagram != null)
        {
            String sFilename = pDiagram.getFilename();
            retVal = getDiagram(sFilename);
        }
        return retVal;
    }
    
    /**
     * Returns a diagram proxy for this name.  The diagram proxy may represent a closed diagram.
     *
     * @param sName [in] The diagram name we're looking for
     * @param pFoundDiagrams [out,retval] A list of all the diagrams with this name in the open workspace
     */
    public ETList<IProxyDiagram> getDiagramsByName(String sName)
    {
        ETList<IProxyDiagram> retVal = null;
        ETList<IProxyDiagram> proxies = getDiagramsInWorkspace();
        if (proxies != null)
        {
            retVal = new ETArrayList<IProxyDiagram>();
            int count = proxies.size();
            for (int i=0; i<count; i++)
            {
                IProxyDiagram dia = proxies.get(i);
                String name = dia.getName();
                if (name != null && name.equals(sName))
                {
                    retVal.add(dia);
                }
            }
        }
        return retVal;
    }
    
    /**
     * Returns a diagram proxy for this name and namespace.  The diagram proxy may represent a closed diagram.
     *
     * @param sName [in] The diagram name we're looking for
     * @param pNamespace [in] The diagram namespace we're looking for
     * @param pFoundDiagrams [out,retval] A list of all the diagrams with this name and namespace in the open workspace.
     */
    public ETList<IProxyDiagram> getDiagrams(String sName, INamespace pNamespace)
    {
        ETList<IProxyDiagram> retVal = null;
        if ((sName == null || sName.length() == 0) && pNamespace == null)
        {
            retVal = getDiagramsInWorkspace();
        }
        else if (sName != null && sName.length() > 0 && pNamespace == null)
        {
            retVal = getDiagramsByName(sName);
        }
        else
        {
            ETList<IProxyDiagram> proxyDias = getDiagramsInNamespace(pNamespace);
            if (proxyDias != null)
            {
                retVal = new ETArrayList<IProxyDiagram>();
                int count = proxyDias.size();
                for (int i=0; i<count; i++)
                {
                    IProxyDiagram dia = proxyDias.get(i);
                    String name = dia.getName();
                    if (name != null && name.equals(sName))
                    {
                        retVal.add(dia);
                    }
                }
            }
        }
        return retVal;
    }
    
    /**
     * Returns all the diagram proxies.  If namespace null then all the diagrams in the
     * workspace are returned.  The diagram proxy may represent a closed diagram.
     *
     * @return A list of all the diagrams in the workspace
     * @see org.netbeans.modules.uml.ui.support.diagramsupport.IProxyDiagramManager#getDiagramsInWorkspace()
     */
    public ETList<IProxyDiagram> getDiagramsInWorkspace()
    {
        ETList<IProxyDiagram> retVal = null;
        
        ArrayList < String > locations = getDiagramLocationsInProject(null);
        retVal = vectorToList(locations);
        
        return retVal;
    }
    
    /**
     * Returns the diagrams in the namespace or under the namespace
     *
     * @param sToplevelXMIID The toplevel xmiid of the namespace element
     * @param sXMIID The xmiid of the namespace element
     * @return A list of all the diagrams in the sXMIID namespace
     * @see org.netbeans.modules.uml.ui.support.diagramsupport.IProxyDiagramManager#getDiagramsInNamespace(java.lang.String, java.lang.String)
     */
    public ETList<IProxyDiagram> getDiagramsInNamespace(String sToplevelXMIID, String sXMIID)
    {
        return getDiagramsInNamespace(sToplevelXMIID, sXMIID, false);
    }
    
    /**
     * Gets the IProject for pElementInProject, then the base directory and
     * calls VerifyUniqueDiagramName
     *
     * @param pElementInProject An element in the project where the diagram is to live
     * @return The returned list of proxy diagrams
     *
     * @see org.netbeans.modules.uml.ui.support.diagramsupport.IProxyDiagramManager#getDiagramsInNamespace(com.embarcadero.describe.foundation.INamespace)
     */
    public ETList<IProxyDiagram> getDiagramsInNamespace(INamespace pNamespace)
    {
        ETList<IProxyDiagram> retVal = null;
        
        if(pNamespace != null)
        {
            IElement element = (IElement)pNamespace;
            String xmiID = element.getXMIID();
            String topXMIID = element.getTopLevelId();

            if (xmiID != null && (xmiID.length() > 0) &&
                    topXMIID != null && (topXMIID.length() > 0))
            {
                retVal = getDiagramsInNamespace(topXMIID, xmiID);
            }
            else
            {
                // No namespace has been provided so just return all the diagrams
                retVal = getDiagramsInWorkspace();
            }
        }
        
        return retVal;
    }
    
    /**
     * Returns all the diagram proxies under the namespace.  The diagram proxy may represent a closed diagram.
     *
     * @param sXMIID [in] The xmiid of the namespace element
     * @param pProxyDiagrams [out,retval] A list of all the diagrams in the sXMIID namespace
     */
    private ETList<IProxyDiagram> getDiagramsUnderNamespace(String sToplevelXMIID,
            String sXMIID)
    {
        return getDiagramsInNamespace(sToplevelXMIID, sXMIID, true);
    }
    
    /**
     * Returns all the diagram proxies under the target namespace.  The diagram proxy may represent a closed diagram.
     * @param sToplevelXMIID the xmiid of the top level element 
     * @param targetElem [in] the target namespace element
     * @return A list of all the diagrams under the sXMIID namespace
     */
    private ETList<IProxyDiagram> getDiagramsUnderNamespace(String sToplevelXMIID,
            INamespace targetElem)
    {
        ETList<IProxyDiagram> retDiagrams =  new ETArrayList <IProxyDiagram>();
        ETList<IProxyDiagram> diagramsToSearch = null;
        
        if(sToplevelXMIID.length() > 0)
        {
            diagramsToSearch = getDiagramsInProject(sToplevelXMIID);
        }
        else
        {
            diagramsToSearch = getDiagramsInWorkspace();
        }
        getOwnedDiagrams(targetElem, diagramsToSearch, retDiagrams);
        return retDiagrams;
    }
    
    /** 
     * Recursively finds all the diagrams proxies under the target namespace
     * @param targetElem  The target namespace
     * @param diagramsToSearch  A list of all diagram proxies under the project namespace
     * @param retDiagrams  A list of all the diagram proxies under the target namespace. This list is a subset of diagramstoSearch list
     */
    private void getOwnedDiagrams(INamespace targetElem, ETList<IProxyDiagram> diagramsToSearch, ETList<IProxyDiagram> retDiagrams)
    {
        if (targetElem == null)
        {
            return;
        }
        ETList<IProxyDiagram> foundDiagrams = getDiagramsInNamespace(diagramsToSearch, targetElem.getXMIID());
        if (foundDiagrams != null && foundDiagrams.size() > 0)
        {
            retDiagrams.addAll(foundDiagrams);
        }
        ETList<INamedElement> ownedElems = targetElem.getOwnedElements();
        if (ownedElems.size() > 0)
        {
            for (INamedElement elem : ownedElems)
            {
                if (elem instanceof INamespace)
                {
                    getOwnedDiagrams(((INamespace)elem), diagramsToSearch, retDiagrams);
                }
            }
        }
        
    }
    
    /**
     * Returns the diagrams in the namespace
     *
     * @param diagramsToSearch a list of open in diagrams in project
     * @param sXMIID The xmiid of the namespace element
     *
     * @return A list of all the (immediate) diagrams in the sXMIID namespace
     */
    public ETList<IProxyDiagram> getDiagramsInNamespace(ETList<IProxyDiagram> diagramsToSearch,
            String targetXMIID)
    {
        ETList < IProxyDiagram > diagramsInNamespace = new ETArrayList < IProxyDiagram >();
        
        // Search the diagrams to find the ones that have the specified XMIID
        for (int index = 0; index < diagramsToSearch.size(); index++)
        {
            try
            {
                IProxyDiagram thisDiagram = diagramsToSearch.get(index);
                String thisXMIID = thisDiagram.getNamespaceXMIID();
                if(thisXMIID.equals(targetXMIID) == true)
                {
                    diagramsInNamespace.add(thisDiagram);
                }
            }
            catch(NullPointerException e)
            {
                // Do nothing.  Continue onto the next item.
            }
        }
        return diagramsInNamespace;
    }
    
    /**
     * Returns the diagrams in the namespace or under the namespace
     *
     * @param sToplevelXMIID The toplevel xmiid of the namespace element
     * @param sXMIID The xmiid of the namespace element
     * @param bUnderNamespace true to look for all diagrams under the namespace
     *                        this will include all diagram under child namespaces.
     * @return A list of all the diagrams in the sXMIID namespace
     * @see org.netbeans.modules.uml.ui.support.diagramsupport.IProxyDiagramManager#getDiagramsInNamespace(java.lang.String, java.lang.String)
     */
    public ETList<IProxyDiagram> getDiagramsInNamespace(String sToplevelXMIID,
            String sXMIID,
            boolean bUnderNamespace)
    {
        ETList<IProxyDiagram> retVal = null;
        
        ETList < IProxyDiagram > diagramsInNamespace = new ETArrayList < IProxyDiagram >();
        //ArrayList diagramsInNamespace = new ArrayList ();
        
        // Get the diagrams in the workspace then check the xmiid and see if it
        // matches the one passed in
        ETList<IProxyDiagram> diagramsToSearch = null;
        if(sToplevelXMIID.length() > 0)
        {
            diagramsToSearch = getDiagramsInProject(sToplevelXMIID);
        }
        else
        {
            diagramsToSearch = getDiagramsInWorkspace();
        }
        
        if(bUnderNamespace == true)
        {
            // HAVE TODO: reload element.
        }
        
        // Search the diagrams to find the ones that have the specified XMIID
        ArrayList < String > foundChildren = new ArrayList < String >();
        //ArrayList foundChildren = new ArrayList();
        for (int index = 0; index < diagramsToSearch.size(); index++)
        {
            try
            {
                IProxyDiagram thisDiagram = diagramsToSearch.get(index);
                String thisXMIID = thisDiagram.getNamespaceXMIID();
                
                if(bUnderNamespace == true)
                {
                    if(thisXMIID.equals(sXMIID) == true)
                    {
                        diagramsInNamespace.add(thisDiagram);
                    }
                    
                }
                else
                {
                    if(foundChildren.contains(thisXMIID) == true)
                    {
                        // The current diagram is a child of one of the child namespaces.
                        diagramsInNamespace.add(thisDiagram);
                    }
                    else if(thisXMIID.equals(sXMIID) == true)
                    {
                        // The diagram belongs inside of this namespace.
                        diagramsInNamespace.add(thisDiagram);
                        foundChildren.add(thisXMIID);
                    }
                    else
                    {
                        // If bUnderNamespace is set to true then pSearchedNamespaceElement
                        // should not be null.  Therefore, we can search child namespaces
                        // to see if the diagram is in a child namespace of the namespace
                        // specified by sXMIID parameter.
                        
                        // TODO: Check Child Namespaces for Diagram.
                    }
                }
            }
            catch(NullPointerException e)
            {
                // Do nothing.  I just want to continue onto the next item.
            }
            
        }
        
        retVal = diagramsInNamespace;
        
        return retVal;
    }
    
    /**
     * Returns all the diagram proxies for diagrams in this project.  The diagram proxy may represent a closed diagram.
     *
     * @param sProjectName [in] The name of the project to retrieve the diagrams for
     * @param pProxyDiagrams [out,retval] A list of all the diagrams in the project
     */
    public ETList<IProxyDiagram> getDiagramsInProject(IProject pProject)
    {
        ETList<IProxyDiagram> retVal = null;
        
        if(pProject != null)
        {
            retVal = getDiagramsInProject(pProject.getXMIID());
        }
        else
        {
            retVal = getDiagramsInWorkspace();
        }
        
        return retVal;
    }
    
    /**
     * Returns a list of all the diagrams in the project.
     *
     * @param sProjectXMIID The xmiid of the project
     * @return An array of all the diagram locations (.etld files)
     * @see org.netbeans.modules.uml.ui.support.diagramsupport.IProxyDiagramManager#getDiagramsInProject(java.lang.String)
     */
    public ETList<IProxyDiagram> getDiagramsInProject(String sProjectXMIID)
    {
        ETList<IProxyDiagram> retVal = null;
        
        if(sProjectXMIID != null )
        {
            ArrayList < String > locations = getDiagramLocationsInProject(sProjectXMIID);
            retVal = vectorToList(locations);
        }
        else
        {
            retVal = getDiagramsInWorkspace();
        }
        
        return retVal;
    }
    
    /**
     * Converts a vector of diagrams locations into a IProxyDiagram Array
     *
     * @param rDiagrams [in] A vector of all the diagram locations (.etld files)
    @* return The IProxyDiagrams list
     */
    protected ETList<IProxyDiagram> vectorToList(ArrayList < String > locations)
    {
        ETList<IProxyDiagram> retVal = null;
        
        //ArrayList < IProxyDiagram > list = new ArrayList < IProxyDiagram >();
        retVal = new ETArrayList<IProxyDiagram>();
        for (int index = 0; index < locations.size(); index++)
        {
            String location = (String)locations.get(index);
            IProxyDiagram proxy = new ProxyDiagramImpl();
            proxy.setFilename(location);
            retVal.add(proxy);
        }
        
        return retVal;
    }
    
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.diagramsupport.IProxyDiagramManager#getDiagramDetails(java.lang.String)
    */
    public DiagramDetails getDiagramDetails(String bDiagramFilename)
    {
        IProxyDiagram dia = new ProxyDiagramImpl();
        dia.setFilename(bDiagramFilename);
        return dia.getDiagramDetails();
    }
    
    public INamespace getDiagramNamespace(String bDiagramFilename)
    {
        INamespace retVal = null;
        
        try
        {
            DiagramDetails details = getDiagramDetails(bDiagramFilename);
            retVal = details.getNamespace();
            
            if(retVal == null)
            {
                IApplication app = ProductHelper.getApplication();
                IElementLocator locator = new ElementLocator();
                
                IProject foundProject = app.getProjectByID(details.getToplevelXMIID());
                IElement modelElement = locator.findElementByID(foundProject, details.getNamespaceXMIID());
                
                if(modelElement instanceof INamespace)
                {
                    retVal = (INamespace) modelElement;
                }
            }
        }
        catch(NullPointerException e)
        {
            // Ignore I just want to bail.
            retVal = null;
        }
        
        return retVal;
    }
    
    /**
     * Verifies we have a valid diagram by making sure a .etld and .etlp file exist.
     *
     * @param path The path to the filename.
     * @param filename The etlp or etl file to be validated
     * @return <b>true</b> if both the etl and etlp file exists.  The files are not opened.
     * @see org.netbeans.modules.uml.ui.support.diagramsupport.IProxyDiagramManager#isValidDiagram(java.lang.String)
     */
    public boolean isValidDiagram(String fullname)
    {
        String path = StringUtilities.getPath(fullname);
        String filename = StringUtilities.getFileName(fullname);
        return isValidDiagram(path, filename);
    }
    
    /**
     * Verifies we have a valid diagram by making sure a .etld and .etlp file exist.
     *
     * @param path The path to the filename.
     * @param filename The etlp or etl file to be validated
     * @return <b>true</b> if both the etl and etlp file exists.  The files are not opened.
     * @see org.netbeans.modules.uml.ui.support.diagramsupport.IProxyDiagramManager#isValidDiagram(java.lang.String)
     */
    public boolean isValidDiagram(String path, String filename)
    {
        boolean retVal = false;
        
        if((filename != null) && (filename.length() > 0))
        {
            String fileWOExtension = StringUtilities.getFileName(filename);
            
            String layPath = StringUtilities.createFullPath(path,
                    fileWOExtension,
                    FileExtensions.DIAGRAM_LAYOUT_EXT);
            File etlFile = new File(layPath);
            
            String presPath = StringUtilities.createFullPath(path,
                    fileWOExtension,
                    FileExtensions.DIAGRAM_PRESENTATION_EXT);
            File etlpFile = new File(presPath);
            
            if((etlFile.exists() == true) &&
                    (etlpFile.exists() == true))
            {
                retVal = true;
            }
        }
        
        return retVal;
    }
    
    /**
     * Looks into the .etlp file for presentation elements that represent the queried model element.
     *
     * @param pModelElement [in] The model element to look for in closed diagrams as a presentation element
     * @param sDiagramFilename [in] The etlp file where the model elements may have presentation elements
     * @param pVal [out,retval] A list of the found presentation elmeents in the closed diagram
     */
    public ETList<IPresentationTarget> getPresentationTargetsFromClosedDiagram(IElement pModelElement, String sDiagramFilename)
    {
        ETList<IPresentationTarget> retObj = new ETArrayList<IPresentationTarget>();
        if (pModelElement != null)
        {
            String xmiid = pModelElement.getXMIID();
            
            // Now go through the tom file to see if the model element id is in there.
            String etlFilename = sDiagramFilename;
            String etlpFilename = FileSysManip.ensureExtension(etlFilename, FileExtensions.DIAGRAM_PRESENTATION_EXT);
            File file = new File(etlpFilename);
            if (file.exists())
            {
                IProductArchive pProdArch = new ProductArchiveImpl();
                boolean loaded = true;
                // Note that this loaded archive could either be a stub or a full up diagram.
                // so after we're done looking for normal presentation elements see if there's
                // a cdfs table indicating this diagram is a stub.
                loaded = pProdArch.load(etlpFilename);
                if (loaded)
                {
                    ETList<IProductArchiveElement> pElems = pProdArch.getElements();
                    if (pElems != null)
                    {
                        int count = pElems.size();
                        for (int i=0; i<count; i++)
                        {
                            IProductArchiveElement ele = pElems.get(i);
                            String sMEID = ele.getAttributeString(IProductArchiveDefinitions.MEID_STRING);
                            String sPEID = ele.getAttributeString(IProductArchiveDefinitions.PRESENTATIONELEMENTID_STRING);
                            
                            // Make sure we don't have a label
                            IProductArchiveAttribute pLabelAttr = ele.getAttribute(IProductArchiveDefinitions.LABELVIEW_TSLABELKIND);
                            IProductArchiveAttribute meidAttr = ele.getAttribute(IProductArchiveDefinitions.MEID_STRING);
                            IProductArchiveAttribute peidAttr = ele.getAttribute(IProductArchiveDefinitions.PRESENTATIONELEMENTID_STRING);
                            if (meidAttr != null && peidAttr != null && pLabelAttr == null)
                            {
                                if (sMEID != null && sMEID.equals(xmiid))
                                {
                                    // We found one instance
                                    IPresentationTarget presTarget = new PresentationTarget();
                                    presTarget.setPresentationID(sPEID);
                                    presTarget.setDiagramFilename(etlFilename);
                                    
                                    retObj.add(presTarget);
                                }
                            }
                        }
                        
                        // Now see if this diagram has a cdfs table meaning it's a stub
                        ETList<IProductArchiveElement> pAllElems = pProdArch.getAllTableEntries(IProductArchiveDefinitions.DIAGRAM_CDFS_STRING);
                        if (pAllElems != null)
                        {
                            int num = pAllElems.size();
                            for (int j=0; j<num; j++)
                            {
                                IProductArchiveElement pFoundEle = pAllElems.get(j);
                                String sMEID = pFoundEle.getID();
                                String sTopLevelXMIID = pFoundEle.getAttributeString(IProductArchiveDefinitions.TOPLEVELID_STRING);
                                if (sMEID != null && sTopLevelXMIID != null && sMEID.equals(sTopLevelXMIID))
                                {
                                    IPresentationTarget presTar = new PresentationTarget();
                                    presTar.setModelElementID(sMEID);
                                    presTar.setTopLevelID(sTopLevelXMIID);
                                    presTar.setDiagramFilename(etlFilename);
                                    retObj.add(presTar);
                                }
                            }
                        }
                    }
                }
                else
                {
                    //show error for IDS_COULDNOTLOAD etlpFilename
                }
            }
        }
        return retObj;
    }
    
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.diagramsupport.IProxyDiagramManager#markPresentationTargetsAsDeleted(com.embarcadero.describe.foundation.IVersionableElement[])
    */
    public void markPresentationTargetsAsDeleted(ETList<IVersionableElement> pElements)
    {
        // TODO Auto-generated method stub
        
    }
    
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.diagramsupport.IProxyDiagramManager#broadcastToAllOpenDiagrams(com.embarcadero.describe.diagrams.IBroadcastAction)
    */
    public void broadcastToAllOpenDiagrams(IBroadcastAction pAction)
    {
        if (pAction != null)
        {
            IProductDiagramManager pProductDiagramManager = ProductHelper.getProductDiagramManager();
            ETList<IProxyDiagram> pProxyDiagrams = null;
            if (pProductDiagramManager != null)
            {
                // Find all the open diagrams.
                pProxyDiagrams = pProductDiagramManager.getOpenDiagrams();
            }
            else
            {
                // Do it the hard way without the product implementing this interface
                pProxyDiagrams = getDiagramsInWorkspace();
            }
            
            if (pProxyDiagrams != null)
            {
                int numDiagrams = pProxyDiagrams.size();
                for (int i = 0 ; i < numDiagrams ; i++)
                {
                    IProxyDiagram pProxyDiagram = pProxyDiagrams.get(i);
                    if (pProxyDiagram != null)
                    {
                        IDiagram pDiagram = pProxyDiagram.getDiagram();
                        if (pDiagram != null)
                        {
                            pDiagram.receiveBroadcast(pAction);
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Returns VARIANT_TRUE if any of the open diagrams for this project are dirty.  If sProjectName is
     * empty then all diagrams are looked at.
     *
     * @param pProject [in] The project name to see if we have dirty diagrams for.  If NULL then
     * we return the dirty flag for all projects in the workspace
     * @param bDirtyOnesExist [out,retval] VARIANT_TRUE if dirty diagrams exist
     */
    public boolean areAnyOpenDiagramsDirty(IProject pProject)
    {
        boolean anyDirty = false;
        String topLevelId = "";
        if (pProject != null)
        {
            topLevelId = pProject.getXMIID();
        }
        
        IProductDiagramManager diaMgr = ProductHelper.getProductDiagramManager();
        if (diaMgr != null)
        {
            // Get the open diagrams that have not yet been saved
            ETList<IProxyDiagram> pProxyDias = diaMgr.getOpenDiagrams();
            int count = 0;
            if (pProxyDias != null)
            {
                count = pProxyDias.size();
            }
            
            for (int i=0; i<count; i++)
            {
                IProxyDiagram pDia = pProxyDias.get(i);
                boolean bContinue = true;
                if (topLevelId != null && topLevelId.length() > 0)
                {
                    String diaTopLevelId = "";
                    DiagramDetails details = pDia.getDiagramDetails();
                    if (details != null)
                    {
                        diaTopLevelId = details.getDiagramXMIID();
                    }
                    if (diaTopLevelId != null && !diaTopLevelId.equals(topLevelId))
                    {
                        bContinue = false;
                    }
                }
                
                if (bContinue)
                {
                    IDiagram dia = pDia.getDiagram();
                    if (dia != null)
                    {
                        anyDirty = dia.getIsDirty();
                        if (anyDirty)
                        {
                            break;
                        }
                    }
                }
            }
        }
        return anyDirty;
    }
    
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.diagramsupport.IProxyDiagramManager#cleanWorkspaceOfDeadDiagrams(boolean)
    */
    public int cleanWorkspaceOfDeadDiagrams(boolean bRemoveDeadOnes)
    {
        // TODO Auto-generated method stub
        return 0;
    }
    
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.diagramsupport.IProxyDiagramManager#cleanDiagramBackupFolder(java.lang.String)
    */
    public void cleanDiagramBackupFolder(String sProjectName)
    {
        // TODO Auto-generated method stub
        
    }
    
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.diagramsupport.IProxyDiagramManager#getAssociatedDiagramsForElement(java.lang.String)
    */
    public ETList<IProxyDiagram> getAssociatedDiagramsForElement(String sElementXMIID)
    {
        ETList<IProxyDiagram> foundProxyDiagrams = new ETArrayList<IProxyDiagram>();
        
        if ( (sElementXMIID != null) &&
                (sElementXMIID.length() > 0) )
        {
            ETList< IProxyDiagram > proxyDiagrams = getDiagramsInWorkspace();
            
            for (Iterator iter = proxyDiagrams.iterator(); iter.hasNext();)
            {
                IProxyDiagram proxyDiagram = (IProxyDiagram)iter.next();
                
                if( proxyDiagram.isAssociatedElement( sElementXMIID ))
                {
                    foundProxyDiagrams.add( proxyDiagram );
                }
            }
        }
        
        return foundProxyDiagrams;
    }
    
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.diagramsupport.IProxyDiagramManager#getAssociatedDiagramsForElement(com.embarcadero.describe.foundation.IElement)
    */
    public ETList<IProxyDiagram> getAssociatedDiagramsForElement(IElement element)
    {
        ETList<IProxyDiagram> retVal = null;
        if (element != null)
        {
            retVal = getAssociatedDiagramsForElement( element.getXMIID() );
        }
        return retVal;
    }
    
    /**
     * Returns a list of diagrams this diagram is associated with.
     *
     * @param sDiagramXMIID [in] The diagram xmiid to find associated diagrams for
     * @param pDiagrams [out,retval] The diagrams associated with the argument element
     */
    public ETList<IProxyDiagram> getAssociatedDiagramsForDiagram(String sDiagramXMIID)
    {
        ETList<IProxyDiagram> retVal = new ETArrayList<IProxyDiagram>();
        if (sDiagramXMIID != null && sDiagramXMIID.length() > 0)
        {
            ETList<IProxyDiagram> pProxyDiagrams = getDiagramsInWorkspace();
            if (pProxyDiagrams != null)
            {
                int count = pProxyDiagrams.size();
                for (int i=0; i<count; i++)
                {
                    IProxyDiagram pDia = pProxyDiagrams.get(i);
                    if (pDia.isAssociatedDiagram(sDiagramXMIID))
                    {
                        retVal.add(pDia);
                    }
                }
            }
        }
        return retVal;
    }
    
    /**
     * Returns a list of diagrams this diagram is associated with.
     *
     * @param pProxyDiagram [in] The diagram to find associated diagrams for
     * @param pDiagrams [out,retval] The diagrams associated with the argument element
     */
    public ETList<IProxyDiagram> getAssociatedDiagramsForDiagram(IProxyDiagram pProxyDiagram)
    {
        ETList<IProxyDiagram> retVal = null;
        if (pProxyDiagram != null)
        {
            String xmiid = pProxyDiagram.getXMIID();
            if (xmiid != null && xmiid.length() > 0)
            {
                retVal = getAssociatedDiagramsForDiagram(xmiid);
            }
        }
        return retVal;
    }
    
    //**************************************************
    // Helper Methods
    //**************************************************
    
    /**
     * Returns a list of all the diagrams in the project.
     *
     * @param sProjectXMIID The xmiid of the project
     * @return An array of all the diagram locations (.etld files)
     */
    protected ArrayList < String > getDiagramLocationsInProject(String sProjectXMIID)
    {
        ArrayList < String > retVal = new ArrayList < String >();
        
        IApplication app = ProductHelper.getApplication();
        
        // HAVE TODO: Should use the Java version
        IProductDiagramManager diagramManager = ProductHelper.getProductDiagramManager();
        
        if((sProjectXMIID != null) && (sProjectXMIID.length() > 0))
        {
            try
            {
                IProject project = app.getProjectByID(sProjectXMIID);
                String baseDirectory = project.getBaseDirectory();
                getDiagramsInDirectory(baseDirectory, retVal);
                
                ETList<IProxyDiagram> openDiagrams = null;
                
                // Since IProductDiagramManager is configurable (Dependends on the enviroment
                // in which we are running inside) we must always check that we actually
                // have a diagram manager.
                if(diagramManager != null)
                {
                    openDiagrams = diagramManager.getOpenDiagrams();
                }
                
                if(openDiagrams != null)
                {
                    for (int index = 0; index < openDiagrams.size(); index++)
                    {
                        IProxyDiagram curDiagram = openDiagrams.get(index);
                        
                        // Get the filename and make sure this diagram is in the
                        // correct project
                        if(project.isSame(curDiagram.getProject()) == true)
                        {
                            String diagramFilename = curDiagram.getFilename();
                            if((diagramFilename != null) &&
                                    (retVal.contains(diagramFilename) == false))
                            {
                                retVal.add(diagramFilename);
                            }
                        }
                    }
                }
            }
            catch(NullPointerException e)
            {
                // Just bail.
            }
        }
        else
        {
            // No id was added so include all open projects
            Vector<IProject> projects = ProductHelper.getOpenProjects(true);
            for(int index = 0; index < projects.size(); index++)
            {
                IProject project = projects.get(index);
                if(project != null)
                {
                    String xmiid = project.getXMIID();
                    if(xmiid.length() > 0)
                    {
                        // Get all diagrams for this project and append to our list
                        ArrayList < String > curDiagrams = getDiagramLocationsInProject(xmiid);
                        retVal.addAll(curDiagrams);
                    }
                }
            }
        }
        
        return retVal;
    }
    
    /**
     * Returns a list of all the diagrams in the directory
     *
     * @param path The base directory for the project.  This is where diagrams live.
     * @param diagrams The collection of diagrams in the specified directory.
     */
    protected void getDiagramsInDirectory(String path, ArrayList diagrams)
    {
        diagrams.clear();
        
        if((path != null) && (path.length() > 0))
        {
//         StringBuffer searchPath = new StringBuffer(path);
//         searchPath.append(File.separator);
//         searchPath.append(FileExtensions.DIAGRAM_SEARCH_STRING);
            File directoryFile = new File(path);
            String[] diagramFiles = directoryFile.list(new ExtensionFileFilter(FileExtensions.DIAGRAM_LAYOUT_EXT_NODOT));
            
            String independPath = directoryFile.getPath();
            
            StringBuffer filenameBuffer = new StringBuffer();
            for (int index = 0; index < diagramFiles.length; index++)
            {
                filenameBuffer.append(independPath);
                
                if(independPath.charAt(independPath.length() - 1) != File.separatorChar)
                {
                    filenameBuffer.append(File.separatorChar);
                }
                
                filenameBuffer.append(diagramFiles[index]);
                String fullPath = filenameBuffer.toString();
                
                if(isValidDiagram(independPath, diagramFiles[index]) == true)
                {
                    diagrams.add(fullPath);
                }
                
                filenameBuffer.delete(0, filenameBuffer.length());
            }
            
        }
    }
    
    /**
     * Used to move diagrams on this namespace when it gets moved from one project to another
     *
     * @param pVerFromProject [in] The project we're pulling pVerElementBeingMoved out of
     * @param pVerToProject [in] The project pVerElementBeingMoved is being placed into
     * @param pVerElementBeingMoved [in] The element being moved.
     */
    public void moveOwnedAndNestedDiagrams( IVersionableElement pFromProject,
            IVersionableElement pToProject,
            IVersionableElement pElementBeingMoved )
    {
        if (pElementBeingMoved instanceof IElement)
        {
            IElement elem = (IElement)pElementBeingMoved;
            String xmiid = elem.getXMIID();
            String sTopLevelXMIID = elem.getTopLevelId();
            if (xmiid != null && xmiid.length() > 0 &&
                    sTopLevelXMIID != null && sTopLevelXMIID.length() > 0)
            {
                ETList<IProxyDiagram> proxyDias = getDiagramsUnderNamespace(sTopLevelXMIID, xmiid);
                if (proxyDias != null)
                {
                    int count = proxyDias.size();
                    if (count > 0)
                    {
                        String fromProjDir = "";
                        String toProjDir = "";
                        if (pFromProject != null && pFromProject instanceof IProject)
                        {
                            fromProjDir = ((IProject)pFromProject).getBaseDirectory();
                        }
                        if (pToProject != null && pToProject instanceof IProject)
                        {
                            toProjDir = ((IProject)pToProject).getBaseDirectory();
                        }
                        
                        if (fromProjDir != null && fromProjDir.length() > 0 &&
                                toProjDir != null && toProjDir.length() > 0)
                        {
                            for (int i=0; i<count; i++)
                            {
                                IProxyDiagram dia = proxyDias.get(i);
                                String filename = dia.getFilename();
                                if (filename != null && filename.length() > 0)
                                {
                                    String ext = FileSysManip.getExtension(filename);
                                    
                                    // Now save and close the diagram if it's open
                                    IDiagram pDia = dia.getDiagram();
                                    if (pDia != null)
                                    {
                                        pDia.save();
                                        pDia.setIsDirty(false);
                                        pDia.setReadOnly(true);
                                        
                                        // Now close the diagram if its open
                                        IProductDiagramManager diaMan = ProductHelper.getProductDiagramManager();
                                        if (diaMan != null)
                                        {
                                            diaMan.closeDiagram3(dia);
                                        }
                                    }
                                    
                                    // Create the new and old filenames
                                    String fromETLPFilename = FileSysManip.createFullPath(fromProjDir, ext, FileExtensions.DIAGRAM_PRESENTATION_EXT);
                                    String fromETLDFilename = FileSysManip.createFullPath(fromProjDir, ext, FileExtensions.DIAGRAM_LAYOUT_EXT);
                                    String toETLPFilename = FileSysManip.createFullPath(toProjDir, ext, FileExtensions.DIAGRAM_PRESENTATION_EXT);
                                    String toETLDFilename = FileSysManip.createFullPath(toProjDir, ext, FileExtensions.DIAGRAM_LAYOUT_EXT);
                                    
                                    String fromProjBackupDir = fromProjDir + "DiagramBackup" + File.pathSeparator;
                                    String fromBackupETLPFilename = FileSysManip.createFullPath(fromProjBackupDir, ext, FileExtensions.DIAGRAM_PRESENTATION_EXT);
                                    String fromBackupETLDFilename = FileSysManip.createFullPath(fromProjBackupDir, ext, FileExtensions.DIAGRAM_LAYOUT_EXT);
                                    
                                    // Make sure the backup directory exists
                                    File backupDir = new File(fromProjBackupDir);
                                    if (backupDir.exists())
                                    {
                                        // Move old files to the backup directory
                                        boolean success = false;
                                        success = FileSysManip.copyFile(fromETLPFilename, fromBackupETLPFilename);
                                        if (success)
                                        {
                                            success = FileSysManip.copyFile(fromETLDFilename, fromBackupETLDFilename);
                                        }
                                        
                                        // Move this file to the new project directory
                                        if (success)
                                        {
                                            success = FileSysManip.copyFile(fromETLPFilename, toETLPFilename);
                                        }
                                        if (success)
                                        {
                                            success = FileSysManip.copyFile(fromETLDFilename, toETLDFilename);
                                        }
                                        
                                        if (!success)
                                        {
                                            // Get rid of the files we may have copied over because
                                            // one of the copies failed.
//											DeleteFile(xsFromBackupETLPFilename,sErrorMsg);
//											DeleteFile(xsFromBackupETLDFilename,sErrorMsg);
//											DeleteFile(xsToETLPFilename,sErrorMsg);
//											DeleteFile(xsToETLDFilename,sErrorMsg);
//
//											// Cancel the event
//											hr = EFR_S_EVENT_CANCELLED;
//
//											// Tell the user
//											xstring message;
//
//											message = StringUtilities::Format(_Module.GetResourceInstance(),
//																			  IDS_DIAGRAM_MOVE_FAILED,
//																			  xsJustFile.c_str(),
//																			  sErrorMsg.c_str());
//											if (message.size())
//											{
//											   INFO_MESSAGE(IDS_MESSAGINGFACILITY, message.c_str() );
//											}
                                            
                                        }
                                        else
                                        {
                                            // Make sure the old ones are gone
//											bSuccess = DeleteFile(xsFromETLPFilename,sErrorMsg);
//											bSuccess = DeleteFile(xsFromETLDFilename,sErrorMsg);
//
//											xstring message;
//
//											message = StringUtilities::Format(_Module.GetResourceInstance(),
//																			  IDS_DIAGRAM_MOVED,
//																			  xsJustFile.c_str(),
//																			  xsToProjectDirectory.c_str());
//											if (message.size())
//											{
//											   INFO_MESSAGE(IDS_MESSAGINGFACILITY, message.c_str() );
//											}
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Used to cleanup diagrams on this namespace.
     *
     * @param pVerElementBeingDeleted [in] The element being deleted
     */
    public void cleanupOwnedDiagrams(IVersionableElement pVerElementBeingDeleted)
    {
        cleanupDiagrams(pVerElementBeingDeleted, false);
    }
    
    /**
     *
     * Deletes all diagrams immediately owned by pElementBeingDeleted as
     * well as diagrams owned by any nested elements as well.
     *
     * @param *pElementBeingDeleted[in] The element being deleted
     *
     * @return HRESULT
     *
     */
    public void cleanupOwnedAndNestedDiagrams( IVersionableElement pElementBeingDeleted )
    {
        cleanupDiagrams(pElementBeingDeleted, true);
    }
    
    /**
     * Called by the cleanup manager to clean diagrams in and under a namespace
     *
     * @param pVerElementBeingDeleted [in] The element being deleted
     * @param bCleanChildren [in] TRUE to delete all the child diagrams as well.
     */
    private void cleanupDiagrams(IVersionableElement delEle,
            boolean bCleanChildren)
    {
        if (delEle != null && delEle instanceof IElement)
        {
            IElement elemDeleted = (IElement)delEle;
            
            String xmiid = elemDeleted.getXMIID();
            String sTopLevelId = elemDeleted.getTopLevelId();
            if (xmiid != null && xmiid.length() > 0 &&
                    sTopLevelId != null && sTopLevelId.length() > 0)
            {
                // First delete any diagrams that are owned by this element or under the elements namespace.
                ETList<IProxyDiagram> proxyDias = null;
                Vector diagramsToDeassociate = new Vector();
                if (bCleanChildren)
                {
                    if (elemDeleted instanceof INamespace)
                    {
                        proxyDias = getDiagramsUnderNamespace(sTopLevelId, ((INamespace)elemDeleted) );
                    }
                    else 
                    {
                        proxyDias = getDiagramsUnderNamespace(sTopLevelId, xmiid );
                    }
                }
                else
                {
                    proxyDias = getDiagramsInNamespace(sTopLevelId, xmiid, false);
                }
                
                if (proxyDias != null)
                {
                    int count = proxyDias.size();
                    for (int i=0; i<count; i++)
                    {
                        IProxyDiagram pDia = proxyDias.get(i);
                        String filename = pDia.getFilename();
                        
                        // Now remove the diagram
                        if (filename != null && filename.length() > 0)
                        {
                            String diaXMIID = pDia.getXMIID();
                            if (diaXMIID != null && diaXMIID.length() > 0)
                            {
                                diagramsToDeassociate.add(diaXMIID);
                            }
                            removeDiagram(filename);
                        }
                    }
                    
                    // Now remove this element if it's referenced by any diagrams.
                    ETList<IProxyDiagram> allDias = getDiagramsInWorkspace();
                    if (allDias != null)
                    {
                        int num = allDias.size();
                        for(int j=0; j<num; j++)
                        {
                            IProxyDiagram pDia = allDias.get(j);
                            boolean isAssociated = pDia.isAssociatedElement(xmiid);
                            if (isAssociated)
                            {
                                pDia.removeAssociatedElement(sTopLevelId, xmiid);
                            }
                            
                            // Also remove any diagrams that just got whacked.
                            if (diagramsToDeassociate.size() > 0)
                            {
                                for (int k=0; k<diagramsToDeassociate.size(); k++)
                                {
                                    String str = (String)diagramsToDeassociate.get(k);
                                    isAssociated = pDia.isAssociatedDiagram(str);
                                    if (isAssociated)
                                    {
                                        pDia.removeAssociatedDiagram(str);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Validate the passed in values according to the Describe business rules.
     * See method for the rules.
     *
     * @param pDisp[in]			The dispatch that needs validating
     * @param fieldName[in]		The name of the field to validate
     * @param fieldValue[in]	The string to validate
     * @param outStr[out]		The string changed to be valid (if necessary)
     * @param bValid[out]		Whether the string is valid as passed in
     *
     * @return HRESULT
     *
     */
    public boolean validate( Object pDisp, String fieldName, String fieldValue)
    {
        boolean isValid = false;
        if (fieldName != null)
        {
            if (fieldName.equals("Name") || fieldName.equals("DefaultProjectName")
            || fieldName.equals("DefaultElementName") || fieldName.equals("DefaultDiagramName"))
            {
                ETPairT<Boolean, String> retVal = isValidDiagramName(fieldValue);
                isValid = ((Boolean)retVal.getParamOne()).booleanValue();
                fieldValue = retVal.getParamTwo();
            }
        }
        return isValid;
    }
    
    /**
     * This verifies that the diagram name is correct and returns the corrected one if not.
     *
     * @param sSuggestedDiagramName [in] The proposed,new diagram name
     * @param sCorrectedDiagramName [out] If the proposed diagram name has some invalid characters,
     * then here's the corrected version
     * @param bIsCorrect [out] true if the suggested diagram name has all valid characters
     */
    public ETPairT<Boolean, String> isValidDiagramName(String diaName)
    {
        boolean retVal = true;
        String finalStr = "";
        if (diaName != null)
        {
            int length = diaName.length();
            if (length == 0)
            {
                retVal = false;
            }
            for (int i=0; i<length; i++)
            {
                char c = diaName.charAt(i);
                if (Character.isLetterOrDigit(c) || (c == '_') || (c == '{') || (c == '}')
                || (c == '[') || (c == ']') || (c == ' ') )
                {
                    finalStr += Character.toString(c);
                }
                else
                {
                    retVal = false;
                    finalStr += "_";
                }
            }
        }
        return new ETPairT<Boolean, String>( Boolean.valueOf(retVal), finalStr);
    }
    
    public void whenValid(Object pDisp)
    {
        //nothing to do
    }
    
    public void whenInvalid(Object pDisp)
    {
        IErrorDialog pErrorDialog = new SwingErrorDialog();
        if (pErrorDialog != null)
        {
            String title = DiagramSupportResources.getString("IDS_TITLE1");
            String msg = DiagramSupportResources.getString("IDS_INVALIDNAME");
            pErrorDialog.display(msg, title);
        }
    }
    
    /**
     * Creates a diagram filename for use when creating a new diagram (stub or not).
     */
    public String createDiagramFilename( INamespace diagramNamespace, String sDiagramName )
    {
        String sDiagramFilename = "";
        
        String proposedFilename;
        
        // Hash the filename and append the timestamp to provide a unique filename
        String formatString;
        
        // To avoid conflicts between filenames, esp for large groups that are
        // using an SCC to manage their model we append a timestamp to the
        // file name.
        final long timeNow = System.currentTimeMillis();
        
        // formatString should now be something like mydiagram_3452619430 or c:\myws\mydiagram_3452619430
        formatString = sDiagramName + "_" + String.valueOf(timeNow);
        
        // Here we fix the problem of possible duplicate file name by checking the previous
        // time stamp put on the file.  If it is the same we add an extra index.
        // This happens a lot when importing rose models and we're quick enough that it
        // doesn't take a full second ('cause we're using time).  We could use ftime, but then
        // the filenames would be really long.  So instead we just spin here.
        {
            if( timeNow == m_timePrev )
            {
                final String strFileName = formatString;
                formatString = strFileName + "_" + m_lIndex++;
            }
            else
            {
                m_timePrev = timeNow;
                m_lIndex = 0;
            }
        }
        
        // proposedFilename should now be something like mydiagram_3452619430 or c:\myws\mydiagram_3452619430
        proposedFilename = formatString;
        
        // Make sure we have a legal file.  If it is just a name then add the
        // .etld extension and put it in the same spot as the workspace.
        
//      TCHAR buffer[ MAX_PATH ];
//      TCHAR drive[_MAX_DRIVE];
//      TCHAR dir[ MAX_PATH ];
//      TCHAR fname[ MAX_PATH ];
//      TCHAR path_buffer[ MAX_PATH 2 ];
        
//      _tcscpy( buffer, w2T(proposedFilename) );
//      _tsplitpath( buffer, drive, dir, fname, 0 );
        
        
        // Split the path so we can determine if we've got a full path
        String strPath = StringUtilities.getPath( proposedFilename );
        // fname should now be mydiagram_3452619430
        if (strPath.length() <= 0)
        {
            // Assume we don't have a path and create one from the project directory
            
            String sFilename;
            
            // Use the workspace path
            IProject project = diagramNamespace.getProject();
            
            assert (project != null);
            if (project != null)
            {
                sFilename = project.getFileName();
                if ( sFilename.length() > 0 )
                {
                    strPath = StringUtilities.getPath( sFilename );
                }
            }
        }
        
        return StringUtilities.createFullPath( strPath, proposedFilename , FileExtensions.DIAGRAM_LAYOUT_EXT );
/* TODO
      if ( _tcslen(path_buffer) > MAX_PATH)
      {
         // We're too long of a filename
         formatString = StringUtilities.format("%ld_%ld", (long)timet, (long)lIndex);
         _tmakepath(path_buffer, drive, dir, formatString, DIAGRAM_LAYOUT_EXT);
 
         if ( _tcslen(path_buffer) > MAX_PATH)
         {
            assert (0 &"Diagram filename still too long");
         }
      }
      if ( _tcslen(path_buffer) <= MAX_PATH)
      {
         // Lets make sure (at least in debug) that we're not about to overwrite an existing diagram.
         assert ( (_taccess( path_buffer, 0 )) != 0 );
 
         CCompath_buffer.copyTo(sDiagramFilename);
      }
 */
    }
    
    /**
     * Returns all the diagram proxies in the namespace.  If bDeepSearch is true, all diagrams found through all the namespace's children will also be found.
     */
    public ETList<IProxyDiagram> getDiagramsInNamespace( INamespace pSpace, boolean bDeepSearch )
    {
        return null;
    }
    
    private static long m_timePrev = 0;
    private static long m_lIndex = 1;
}
