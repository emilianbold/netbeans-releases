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


package org.netbeans.modules.uml.core.metamodel.structure;

import org.netbeans.modules.uml.core.support.IAssociatedProjectSourceRoots;
import java.io.File;

import org.dom4j.Document;
import org.dom4j.Node;

import org.netbeans.modules.uml.core.coreapplication.ICoreProduct;
import org.netbeans.modules.uml.core.eventframework.EventDispatchNameKeeper;
import org.netbeans.modules.uml.core.eventframework.EventDispatchRetriever;
import org.netbeans.modules.uml.core.metamodel.core.foundation.ElementCollector;
import org.netbeans.modules.uml.core.metamodel.core.foundation.ElementConnector;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IBackPointer;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.Classifier;
import org.netbeans.modules.uml.core.support.umlsupport.PathManip;
import org.netbeans.modules.uml.core.support.umlsupport.ProductRetriever;
import org.netbeans.modules.uml.core.support.umlsupport.StringUtilities;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.core.workspacemanagement.IWSProject;
import org.netbeans.modules.uml.core.workspacemanagement.IWorkspace;


public class Artifact extends Classifier implements IArtifact 
{

	/**
	 * 
	 */
	public Artifact() 
	{
		super();		
	}
	
	public IDeploymentSpecification getContent()
	{
		ElementCollector<IDeploymentSpecification> collector = 
											new ElementCollector<IDeploymentSpecification>();
		return collector.retrieveSingleElementWithAttrID(this,"content", IDeploymentSpecification.class);
	}

	public void setContent( IDeploymentSpecification pSpec )
	{
		final IDeploymentSpecification spec = pSpec;
		new ElementConnector<IArtifact>().setSingleElementAndConnect
						(
							this, spec, 
							"content",
							 new IBackPointer<IDeploymentSpecification>() 
							 {
								 public void execute(IDeploymentSpecification obj) 
								 {
                                     obj.addDeploymentDescriptor(Artifact.this);
								 }
							 },
							 new IBackPointer<IDeploymentSpecification>() 
							 {
								 public void execute(IDeploymentSpecification obj) 
								 {
                                     obj.removeDeploymentDescriptor(Artifact.this);
								 }
							 }										
						);
	}
	
	public ETList<IDeployment> getDeployments()
	{
		ElementCollector<IDeployment> collector = new ElementCollector<IDeployment>();
		return collector.retrieveElementCollectionWithAttrIDs(this,"deployment", IDeployment.class);
	}
	
	public void removeDeployment( IDeployment deployment )
	{
		final IDeployment dep = deployment;		
		new ElementConnector<IArtifact>().removeByID
							   (
								 this,dep,"deployment",
								 new IBackPointer<IArtifact>() 
								 {
									public void execute(IArtifact obj) 
									{
									   dep.removeDeployedArtifact(obj);
									}
								 }										
								);
	}
	
	public void addDeployment( IDeployment deployment )
	{
		final IDeployment dep = deployment;	
		new ElementConnector<IArtifact>().addChildAndConnect(
											this, true, "deployment", 
											"deployment", dep,
											 new IBackPointer<IArtifact>() 
											 {
												 public void execute(IArtifact obj) 
												 {
													dep.removeDeployedArtifact(obj);
												 }
											 }										
											);
	}
	
	public ETList<INamedElement> getImplementedElements()
	{
		ElementCollector<INamedElement> collector = new ElementCollector<INamedElement>();
		return collector.retrieveElementCollectionWithAttrIDs(this,"implementedElement", INamedElement.class);		
	}
	
	public void removeImplementedElement( INamedElement comp )
	{
		removeElementByID(comp,"implementedElement");
	}
	
	public void addImplementedElement( INamedElement comp )
	{
		addElementByID(comp,"implementedElement");
	}
	
	/**
	 * Establishes the appropriate XML elements for this UML type.
	 *
	 * [in] The document where this element will reside
	 * [in] The element's parent node.
	 */	
	public void establishNodePresence(Document doc, Node parent)
	{
		buildNodePresence("UML:Artifact",doc,parent);
	}
	
	/**
	 * The absolute path to the associated file.
	 */	 
	public String getFileName()
	{
		String absolutePath = null;
		String relPath = getAttributeValue("sourcefile");
		if (relPath != null && relPath.length() > 0)
		{
			String baseDir = getBaseDir();
			absolutePath = retrieveAbsolutePath(baseDir,relPath);
		}
		return absolutePath;
	}
	
	/**
	 * The absolute path to the associated file.
	 */
	public void setFileName( String newVal )
	{
		// this is different than in c++ because in c++ if we are setting the filename to an empty string
		// we get through this method but an uncaught exception happens in StructureEventDispatcherImpl::FireArtifactFileNamePreModified
		// which causes the process to stop, so the firePreFileNameChange returns false and it is done.
		// In jUML, this is not the case, so retrieveRelativePath returns "\." which is also wrong, but
		// to be consistent with c++, we will not allow the user to blank out the file name of an artifact. 
		if (newVal != null && newVal.length() > 0)
		{
			if (firePreFileNameChange(newVal))
			{
				String relPath = retrieveRelativePath(newVal);
				String oldFileName = getFileName();
				setAttributeValue("sourcefile",relPath);
				fireFileNameChange(oldFileName);
			}
		}			
	}
	
	/**
	 * The source file artifact's base directory.
	 */
	protected String getBaseDir()
	{
		String baseDir = null;
		IProject proj = getProject();		
		if (proj != null)
		{
         baseDir = proj.getBaseDirectory();
//			ICoreProduct product = ProductRetriever.retrieveProduct();			
//			if (product != null)
//			{
//				IWorkspace ws = product.getCurrentWorkspace();
//				if (ws != null)
//				{
//					String projName = proj.getName();					
//					IWSProject wsProj = ws.getWSProjectByName(projName);
//					if (wsProj != null)
//					{
//						baseDir = wsProj.getBaseDirectory();
//					}
//				}
//			}
		}
		return baseDir;
	}
	
	/**
	 *
	 * Retrieves the absolute path of relative.
	 *
	 * @param base[in] The location to root the path from
	 * @param relative[in] The relative path to root. If relative is not a relative path,
	 *                     its value will simply be returned
	 */
	public String retrieveAbsolutePath(String base, String relative)
	{
		String absolutePath = null;
      
//      // In NetBeans 4.1 we now associate a UML project to a NetBeans Project.
//      // Since a NetBeans project can have multiple source roots we first try
//      // convert the filename by using the soruce roots.  
//      IProject project = getProject();
//      if(project != null)
//      {
//         IAssociatedProjectSourceRoots roots = project.getAssociatedProjectSourceRoots();
//         if(roots != null)
//         {
//            absolutePath = roots.createAbsolutePath(relative);
//         }
//      }
      
      // If we failed to convert the filename by the source roots approach
      // try to use the old way. (Backward Compatible)
      if((absolutePath == null) || (absolutePath.length() == 0))
      {
         File file = new File(relative);
         if (file != null && !file.isAbsolute())
         {
            if (base != null && relative != null)
            {
               absolutePath = PathManip.retrieveSourceAbsolutePath(getProject(), relative, base);
            }
         }
         else if (relative != null)
         {
            absolutePath = relative;
         }
      }
		return absolutePath;		
	}
	
	/**
	 * 
	 * Retrieves the relative path between newFile and curFile.
	 *
	 * @param newFile[in] The new file we are trying to get a relative path to
	 *
	 * @return The relative path, else "" on error.
	 * 
	 */
	public String retrieveRelativePath(String newFileName)
	{
		String baseDir = getBaseDir();	
      
      String retVal = "";
      
//      // In NetBeans 4.1 we now associate a UML project to a NetBeans Project.
//      // Since a NetBeans project can have multiple source roots we first try
//      // convert the filename by using the soruce roots. 
//      IProject project = getProject();
//      if(project != null)
//      {
//         IAssociatedProjectSourceRoots roots = project.getAssociatedProjectSourceRoots();
//         if(roots != null)
//         {
//            retVal = roots.createRelativePath(newFileName);
//         }
//      }
      
      // If we still did not have a relative path then use the old mechanism.
      if(retVal.length() == 0)
      {
         retVal = PathManip.retrieveSourceRelativePath(getProject(), newFileName,baseDir);
      }
		return retVal;
	}
	
	/** 
	 * This operation calculates the artifact's base directory based on:
	 * - the owning classifier's fully qualified name
	 * - the path to the file
	 *
	 * For some examples:
	 *
	 * sourceFile                      qualifiedName                    baseDirectory
	 * ---------------------------------------------------------------------------------------
	 * C:\a\b\c\D.java                 D                                C:\a\b\c
	 * C:\a\b\c\D.java                 c.D                             C:\a\b
	 * C:\a\b\c\D.java                 b.c.D                          C:\a
	 * C:\a\b\c\D.java                 a.b.c.D                       C:\
	 * C:\b\c\D.java                   a.b.c.D                       ERROR - too few directories
	 * C:\a\b\c\D.java                 a.c.D                          ERROR - bad match (a vs. b)
	 * 
	 * @param sourceFile[in] the name of the Classifier's source file artifact
	 * @param qualifiedName[in] the qualified name of the Classifier
	 * @param baseDirectory[out] the base directory or empty string if an error occurred.
	 */
	public String getBaseDir(String sourceFile, String qualifiedName)
	{
        ETList<String> qualifiedNames = 
            StringUtilities.splitOnDelimiter(qualifiedName, "::");
        
        if (qualifiedNames.size() == 0)
            return sourceFile;
        
        File f = new File(sourceFile);
        for (int i = qualifiedNames.size() - 1; i >= 0 && f != null; --i)
        {
            String segment = qualifiedNames.get(i);
            String fs = f.getName();
            if (fs.indexOf(".") != -1) fs = StringUtilities.getFileName(fs);
            if (!segment.equals(fs))
                break;
            f = f.getParentFile();
        }
        
        return f != null? f.toString() : null;
        
//		String baseDirectory = null;
//		if ( (sourceFile != null && sourceFile.length() > 0) &&
//			 (qualifiedName != null && qualifiedName.length() > 0) )
//		{
//			String[] srcTokens = sourceFile.split("\\\\");
//			String[] qualifiedTokens = qualifiedName.split("\\.");
//			
//			//Sending only the directory
//			int srcTokensLengthMinus = srcTokens.length - qualifiedTokens.length;
//			String[] retDir = new String[srcTokensLengthMinus];			
//			for(int i=0;i<srcTokensLengthMinus;i++)
//			{
//				retDir[i] = srcTokens[i];
//			}
//			
//			boolean done = false;
//			int srcPtr = srcTokens.length - 1;
//			int quaPtr = qualifiedTokens.length - 1;
//		 
//			while (!done)
//			{	
//				if (srcPtr == 0)
//				{
//					// this is bad.  It means that our fully qualified name had more tokens 
//					// in it than tokens in the path to the source file.  
//					// We can't return a base directory.
//					done = true;
//				}
//				else if (quaPtr == 0)
//				{
//					// running out of qualified name tokens before running 
//					// out of source file tokens (i.e directories)
//					// is acceptable.					
//					baseDirectory = joinStrs(retDir,"\\");
//					done = true;
//				}
//				else
//				{
//					//save the last token from each of the token lists
//					String dirToken = srcTokens[srcPtr--];
//					String nameToken = qualifiedTokens[quaPtr--];
//					int k = dirToken.indexOf('.');
//					if ( k != -1)
//					{
//						dirToken = dirToken.substring(0,k);	
//					}								
//					if (dirToken.compareToIgnoreCase(nameToken) != 0)
//					{
//						// uh-oh, the directory token did not match the qualified name token. 
//						// This means that there is some disagreement between what the class' 
//						 //package structure looks like and what the artifact's
//						// path looks like.
//						done = true;
//					}
//				}
//			}						
//		}
//		return baseDirectory;
	}
			 	
	private String joinStrs(String[] tokens,String toJoin)
	{
		StringBuffer buf = new StringBuffer();
		for (int i=0;i<tokens.length;i++)
		{
			buf.append(tokens[i]);
			//don't add '\' at the end of the dir.
			if (i != tokens.length-1)
				buf.append(toJoin);
		}		
		return buf.toString();
	}
	
	public boolean firePreFileNameChange(String newFileName)
	{
		boolean proceed = false;
		IStructureEventDispatcher dispatcher = getStructureEventDispatcher();
		if (dispatcher != null)
		{
			proceed = dispatcher.fireArtifactFileNamePreModified
														(this,newFileName,null);																	
		}
		return proceed;
	}
	
	public void fireFileNameChange(String oldFileName)
	{		
		IStructureEventDispatcher dispatcher = getStructureEventDispatcher();
		if (dispatcher != null)
		{
			dispatcher.fireArtifactFileNameModified(this,oldFileName,null);																	
		}
	}
		
	private IStructureEventDispatcher getStructureEventDispatcher()
	{
		EventDispatchRetriever retriever = EventDispatchRetriever.instance();
		return (IStructureEventDispatcher)
				retriever.getDispatcher(EventDispatchNameKeeper.structure());
						    
	}
}


