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

package org.netbeans.modules.uml.drawingarea.ui.addins.diagramcreator;

import java.io.File;
import java.io.IOException;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.uml.common.generics.ETPairT;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace;
import org.netbeans.modules.uml.core.metamodel.diagrams.IProxyDiagram;
import org.netbeans.modules.uml.core.support.umlsupport.FileExtensions;
import org.netbeans.modules.uml.core.support.umlsupport.FileSysManip;
import org.netbeans.modules.uml.core.support.umlsupport.IStrings;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.ui.support.archivesupport.IProductArchive;
import org.netbeans.modules.uml.ui.support.archivesupport.IProductArchiveDefinitions;
import org.netbeans.modules.uml.ui.support.archivesupport.IProductArchiveElement;
import org.netbeans.modules.uml.ui.support.diagramsupport.IProxyDiagramManager;
import org.netbeans.modules.uml.ui.support.diagramsupport.ProxyDiagramManager;
import org.openide.filesystems.FileUtil;

/**
 * @author sumitabhk
 *
 */
public class StubDiagramCreator implements IStubDiagramCreator
{
        private static final Logger logger = Logger.getLogger("org.netbeans.modules.uml.ui");

	/**
	 * 
	 */
	public StubDiagramCreator()
	{
		super();
	}

	/**
	 * Creates the specified diagram, and adds the input elements to the diagram when the diagram is next opened.
	 */
	public IProxyDiagram createDiagram(int diagramKind, INamespace pNamespace, String sDiagramName, ETList<IElement> pElements)
	{
		IProxyDiagram retObj = null;
		IProxyDiagramManager pDiaMgr = ProxyDiagramManager.instance();
		String filename = pDiaMgr.createDiagramFilename(pNamespace, sDiagramName);
		if (filename != null && filename.length() > 0)
		{
			// First create a stub tomsawyer file
			createStubETLDFile(filename);
			
			// Now use the product archive to create a stub etlp file.  When the diagram opens
			// it'll CDFS and initialize.
			if (pElements != null)
			{
				//remove duplicates
			}
			createStubETLPFile(diagramKind, pNamespace, sDiagramName, filename, pElements);
			retObj = pDiaMgr.getDiagram(filename);
		}
		return retObj;
	}

	/**
	 * Creates the specified diagram, and adds the input xmi.ids to the diagram when the diagram is next opened.
	 */
	public IProxyDiagram createDiagram(String sDiagramKind, INamespace pNamespace, String sDiagramName, String sProjectXMIID, IStrings pXMIIDsToCDFS, IStrings pXMIIDsForNavigationOnly)
	{
		IProxyDiagram retObj = null;
		IProxyDiagramManager pDiaMgr = ProxyDiagramManager.instance();
		
		// Get the filename we should use
		String filename = sDiagramName; //pDiaMgr.getValidDiagramName(sDiagramName);
		boolean isCorrect = pDiaMgr.isValidDiagram(filename);
		String fullFilename = pDiaMgr.createDiagramFilename(pNamespace, filename);
		if (fullFilename != null && fullFilename.length() > 0)
		{
			// First create a stub tomsawyer file
			createStubETLDFile(fullFilename);
			
			// Now use the product archive to create a stub etlp file.  When the diagram opens
			// it'll CDFS and initialize.
			IProductArchive cpArchive = createEmptyStubETLPFile(sDiagramKind, pNamespace, sDiagramName, fullFilename);
			
			// Insert the elements into the diagram stub
			if (cpArchive != null)
			{
				insert(cpArchive, sProjectXMIID, pXMIIDsToCDFS, false);
				insert(cpArchive, sProjectXMIID, pXMIIDsForNavigationOnly, true);
			}
			
			boolean success = cpArchive.save( null );
			
			retObj = pDiaMgr.getDiagram(fullFilename);
		}
		
		return retObj;
	}

	/**
	 * Creates the stub ETLD (TomSawyer) file
	 */
	public void createStubETLDFile(String sDiagramFullFilename)
	{
            try {
                String tempETLDFilename = FileSysManip.ensureExtension(sDiagramFullFilename, FileExtensions.DIAGRAM_LAYOUT_EXT);
                File file = new File(tempETLDFilename);
                FileUtil.createData(file);
                } catch (IOException ex) {
                    String mesg = ex.getMessage();
                    logger.log(Level.WARNING, mesg != null ? mesg : "", ex);
                }
	}

	/**
	 * Creates the stub ETLP (Presentation) file
	 */
	public void createStubETLPFile(int diagramKind, INamespace pNamespace, String sDiagramName, String sDiagramFullFilename, ETList<IElement> pElements)
	{
//		IDiagramTypesManager pMgr = DiagramTypesManager.instance();
//		String diaKind = pMgr.getDiagramTypeName(diagramKind);
//		IProductArchive pArchive = createEmptyStubETLPFile(diaKind, pNamespace, sDiagramName, sDiagramFullFilename);
//		if (pArchive != null)
//		{
//			// Keep two lists.  The first is the list of elements to upgrade, the second
//			// is the list of elements we'll put on the list with a special attribute saying "don't
//			// use this guy during CDFS".  The second list is used purely for presentation navigation
//			// (ie doubleclicking in the tree) and finding.
//			ETList<IElement> elemsToUpgrade = new ETArrayList<IElement>();
//			ETList<IElement> elemsForNavig = new ETArrayList<IElement>();
//			
//			if (pElements == null)
//			{
//				// Create a dummy list of 0 elements and put the namespace on the list.
//				elemsToUpgrade.add(pNamespace);
//				if (diagramKind == IDiagramKind.DK_SEQUENCE_DIAGRAM)
//				{
//					// We've got a sequence diagram that gets upgraded based on its interaction.  Get the lifelines
//					// of the interaction and add that to our list of elements to upgrade.  Mark them with a special
//					// attribute though that'll tell the CDFS mechanism to ignore them during the actual CDFS process
//					// though so that the CDFS will act on the interaction the way it exists when opened, not when
//					// upgraded.
//					if (pNamespace != null && pNamespace instanceof IInteraction)
//					{
//						IInteraction pInteraction = (IInteraction)pNamespace;
//						ETList<ILifeline> lifelines = pInteraction.getLifelines();
//						if (lifelines != null)
//						{
//							int count = lifelines.size();
//							for (int i=0; i<count; i++)
//							{
//								ILifeline pLifeline = lifelines.get(i);
//								elemsForNavig.add(pLifeline);
//							}
//						}
//					}
//				}
//			}
//			else
//			{
//				elemsToUpgrade.addAll(pElements);
//				//remove duplicates
//			}
//			
//			// Now create a table of all the elements
//			int count = elemsToUpgrade.size();
//			int navCount = elemsForNavig.size();
//			for (int i=0; i<count+navCount; i++)
//			{
//				IElement elem = null;
//				if (i<count)
//				{
//					elem = elemsToUpgrade.get(i);
//				}
//				else
//				{
//					elem = elemsForNavig.get(i-count);
//				}
//				
//				String xmiid = elem.getXMIID();
//				String topId = elem.getTopLevelId();
//				
//				insert(pArchive, topId, xmiid, (i >= count));
//			}
//			
//			pArchive.save( null );
//		}
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.addins.diagramcreator.IStubDiagramCreator#createEmptyStubETLPFile(java.lang.String, org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace, java.lang.String, java.lang.String)
	 */
	public IProductArchive createEmptyStubETLPFile(String sDiagramKind, INamespace pNamespace, String sDiagramName, String bsDiagramFullFilename)
	{
		IProductArchive retObj = null;
//		bsDiagramFullFilename = FileSysManip.ensureExtension(bsDiagramFullFilename, FileExtensions.DIAGRAM_PRESENTATION_EXT);
//		retObj = new ProductArchiveImpl();
//		if (pNamespace != null)
//		{
//                    retObj.save( bsDiagramFullFilename );
//         
//			String spaceXMIID = pNamespace.getXMIID();
//			String spaceTopId = pNamespace.getTopLevelId();
//			if (spaceXMIID.length() > 0 && spaceTopId.length() > 0 && sDiagramKind.length() > 0)
//			{
//				IProductArchiveElement cElem = retObj.createElement(IProductArchiveDefinitions.DIAGRAMINFO_STRING);
//				if (cElem != null)
//				{
//					// Tell the diagram it's a stub
//					cElem.addAttributeBool(IProductArchiveDefinitions.DIAGRAM_ISSTUB_STRING, true);
//					
//					// Add the name of the diagram
//					cElem.addAttributeString(IProductArchiveDefinitions.DIAGRAMNAME_STRING, sDiagramName);
//					
//					// Add the alias of the diagram
//					cElem.addAttributeString(IProductArchiveDefinitions.DIAGRAMALIAS_STRING, sDiagramName);
//					
//					// Add the documentation of the diagram
//					cElem.addAttributeString(IProductArchiveDefinitions.DIAGRAMNAME_DOCS, "");
//					
//					// Add the kind of the diagram
//					cElem.addAttributeString(IProductArchiveDefinitions.DRAWINGKIND2_STRING, sDiagramKind);
//					
//					// Add the namespace that owns this diagram
//					cElem.addAttributeString(IProductArchiveDefinitions.NAMESPACE_TOPLEVELID, spaceTopId);
//					cElem.addAttributeString(IProductArchiveDefinitions.NAMESPACE_MEID, spaceXMIID);
//					
//					// Add the xmiid of the diagram
//					cElem.addAttributeString(IProductArchiveDefinitions.DIAGRAM_XMIID, XMLManip.retrieveDCEID());
//				}
//			}
//		}
		return retObj;
	}

	public void insert(IProductArchive pArchive, String bsTopLevelXMIID, 
						IStrings pXMIIDs, boolean bIgnoreForCDFS)
	{
		if (bsTopLevelXMIID.length() > 0 && pXMIIDs != null)
		{
			int count = pXMIIDs.getCount();
			for (int i=0; i<count; i++)
			{
				String xmiid = pXMIIDs.item(i);
				insert(pArchive, bsTopLevelXMIID, xmiid, bIgnoreForCDFS);
			}
		}
	}

	public void insert(IProductArchive pArchive, String bsTopLevelXMIID, 
						String bsXMIID, boolean bIgnoreForCDFS)
	{
		if (bsXMIID.length() > 0 && bsTopLevelXMIID.length() > 0)
		{
			ETPairT<IProductArchiveElement, Integer> val = pArchive.insertIntoTable(IProductArchiveDefinitions.DIAGRAM_CDFS_STRING, bsXMIID);
			int nKey = (val.getParamTwo()).intValue();
			IProductArchiveElement elem = val.getParamOne();
			if (elem != null)
			{
				elem.addAttributeString(IProductArchiveDefinitions.TOPLEVELID_STRING, bsTopLevelXMIID);
				if (bIgnoreForCDFS)
				{
					elem.addAttributeBool(IProductArchiveDefinitions.DIAGRAM_IGNOREFORCDFS_STRING, true);
				}
			}
		}
	}

}



