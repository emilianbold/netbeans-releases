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

package org.netbeans.modules.uml.ui.addins.diagramcreator;

import java.io.File;
import java.io.IOException;

import org.netbeans.modules.uml.common.generics.ETPairT;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagramKind;
import org.netbeans.modules.uml.core.metamodel.diagrams.IProxyDiagram;
import org.netbeans.modules.uml.core.metamodel.dynamics.IInteraction;
import org.netbeans.modules.uml.core.metamodel.dynamics.ILifeline;
import org.netbeans.modules.uml.core.support.umlsupport.FileExtensions;
import org.netbeans.modules.uml.core.support.umlsupport.FileSysManip;
import org.netbeans.modules.uml.core.support.umlsupport.IStrings;
import org.netbeans.modules.uml.core.support.umlsupport.XMLManip;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.ui.support.archivesupport.IProductArchive;
import org.netbeans.modules.uml.ui.support.archivesupport.IProductArchiveDefinitions;
import org.netbeans.modules.uml.ui.support.archivesupport.IProductArchiveElement;
import org.netbeans.modules.uml.ui.support.archivesupport.ProductArchiveImpl;
import org.netbeans.modules.uml.ui.support.diagramsupport.DiagramTypesManager;
import org.netbeans.modules.uml.ui.support.diagramsupport.IDiagramTypesManager;
import org.netbeans.modules.uml.ui.support.diagramsupport.IProxyDiagramManager;
import org.netbeans.modules.uml.ui.support.diagramsupport.ProxyDiagramManager;

/**
 * @author sumitabhk
 *
 */
public class StubDiagramCreator implements IStubDiagramCreator
{

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
		String filename = pDiaMgr.getValidDiagramName(sDiagramName);
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
		String tempETLDFilename = FileSysManip.ensureExtension(sDiagramFullFilename, FileExtensions.DIAGRAM_LAYOUT_EXT);
		File file = new File(tempETLDFilename);
		if (!file.exists())
		{
			try
			{
				file.createNewFile();
			}
			catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * Creates the stub ETLP (Presentation) file
	 */
	public void createStubETLPFile(int diagramKind, INamespace pNamespace, String sDiagramName, String sDiagramFullFilename, ETList<IElement> pElements)
	{
		IDiagramTypesManager pMgr = DiagramTypesManager.instance();
		String diaKind = pMgr.getDiagramTypeName(diagramKind);
		IProductArchive pArchive = createEmptyStubETLPFile(diaKind, pNamespace, sDiagramName, sDiagramFullFilename);
		if (pArchive != null)
		{
			// Keep two lists.  The first is the list of elements to upgrade, the second
			// is the list of elements we'll put on the list with a special attribute saying "don't
			// use this guy during CDFS".  The second list is used purely for presentation navigation
			// (ie doubleclicking in the tree) and finding.
			ETList<IElement> elemsToUpgrade = new ETArrayList<IElement>();
			ETList<IElement> elemsForNavig = new ETArrayList<IElement>();
			
			if (pElements == null)
			{
				// Create a dummy list of 0 elements and put the namespace on the list.
				elemsToUpgrade.add(pNamespace);
				if (diagramKind == IDiagramKind.DK_SEQUENCE_DIAGRAM)
				{
					// We've got a sequence diagram that gets upgraded based on its interaction.  Get the lifelines
					// of the interaction and add that to our list of elements to upgrade.  Mark them with a special
					// attribute though that'll tell the CDFS mechanism to ignore them during the actual CDFS process
					// though so that the CDFS will act on the interaction the way it exists when opened, not when
					// upgraded.
					if (pNamespace != null && pNamespace instanceof IInteraction)
					{
						IInteraction pInteraction = (IInteraction)pNamespace;
						ETList<ILifeline> lifelines = pInteraction.getLifelines();
						if (lifelines != null)
						{
							int count = lifelines.size();
							for (int i=0; i<count; i++)
							{
								ILifeline pLifeline = lifelines.get(i);
								elemsForNavig.add(pLifeline);
							}
						}
					}
				}
			}
			else
			{
				elemsToUpgrade.addAll(pElements);
				//remove duplicates
			}
			
			// Now create a table of all the elements
			int count = elemsToUpgrade.size();
			int navCount = elemsForNavig.size();
			for (int i=0; i<count+navCount; i++)
			{
				IElement elem = null;
				if (i<count)
				{
					elem = elemsToUpgrade.get(i);
				}
				else
				{
					elem = elemsForNavig.get(i-count);
				}
				
				String xmiid = elem.getXMIID();
				String topId = elem.getTopLevelId();
				
				insert(pArchive, topId, xmiid, (i >= count));
			}
			
			pArchive.save( null );
		}
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.addins.diagramcreator.IStubDiagramCreator#createEmptyStubETLPFile(java.lang.String, org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace, java.lang.String, java.lang.String)
	 */
	public IProductArchive createEmptyStubETLPFile(String sDiagramKind, INamespace pNamespace, String sDiagramName, String bsDiagramFullFilename)
	{
		IProductArchive retObj = null;
		bsDiagramFullFilename = FileSysManip.ensureExtension(bsDiagramFullFilename, FileExtensions.DIAGRAM_PRESENTATION_EXT);
		retObj = new ProductArchiveImpl();
		if (pNamespace != null)
		{
         retObj.save( bsDiagramFullFilename );
         
			String spaceXMIID = pNamespace.getXMIID();
			String spaceTopId = pNamespace.getTopLevelId();
			if (spaceXMIID.length() > 0 && spaceTopId.length() > 0 && sDiagramKind.length() > 0)
			{
				IProductArchiveElement cElem = retObj.createElement(IProductArchiveDefinitions.DIAGRAMINFO_STRING);
				if (cElem != null)
				{
					// Tell the diagram it's a stub
					cElem.addAttributeBool(IProductArchiveDefinitions.DIAGRAM_ISSTUB_STRING, true);
					
					// Add the name of the diagram
					cElem.addAttributeString(IProductArchiveDefinitions.DIAGRAMNAME_STRING, sDiagramName);
					
					// Add the alias of the diagram
					cElem.addAttributeString(IProductArchiveDefinitions.DIAGRAMALIAS_STRING, sDiagramName);
					
					// Add the documentation of the diagram
					cElem.addAttributeString(IProductArchiveDefinitions.DIAGRAMNAME_DOCS, "");
					
					// Add the kind of the diagram
					cElem.addAttributeString(IProductArchiveDefinitions.DRAWINGKIND2_STRING, sDiagramKind);
					
					// Add the namespace that owns this diagram
					cElem.addAttributeString(IProductArchiveDefinitions.NAMESPACE_TOPLEVELID, spaceTopId);
					cElem.addAttributeString(IProductArchiveDefinitions.NAMESPACE_MEID, spaceXMIID);
					
					// Add the xmiid of the diagram
					cElem.addAttributeString(IProductArchiveDefinitions.DIAGRAM_XMIID, XMLManip.retrieveDCEID());
				}
			}
		}
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
			int nKey = ((Integer)val.getParamTwo()).intValue();
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



