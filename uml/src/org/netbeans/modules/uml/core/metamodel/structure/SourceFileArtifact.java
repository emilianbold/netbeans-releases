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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Node;

import org.netbeans.modules.uml.core.coreapplication.ICoreProduct;
import org.netbeans.modules.uml.core.eventframework.EventDispatchNameKeeper;
import org.netbeans.modules.uml.core.eventframework.EventDispatchRetriever;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ILanguage;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ILanguageManager;
import org.netbeans.modules.uml.core.roundtripframework.codegeneration.IFileSystemManipulation;
import org.netbeans.modules.uml.core.roundtripframework.codegeneration.ISourceCodeManipulation;
import org.netbeans.modules.uml.core.support.umlsupport.ProductRetriever;
import org.netbeans.modules.uml.core.support.umlsupport.UMLUtilities;
import org.netbeans.modules.uml.core.support.umlutils.UMLUtilitiesResource;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;



public class SourceFileArtifact extends Artifact implements ISourceFileArtifact
{
	int nextCookie = 1;
	private List<ISourceFileArtifactEventsSink>  m_eventSinks = 
        new ArrayList<ISourceFileArtifactEventsSink>();

	private ISourceCodeHolder m_pSourceCodeHolder = null;
	private ISourceCodeManipulation m_pSourceManipulation = null;
	
	/**
	 * The source file language.
	 */
	public ILanguage getLanguage()
	{
		String sourceFileName = getSourceFile();
		ILanguage retLang = null;
		if (sourceFileName != null)
		{
			ICoreProduct product = ProductRetriever.retrieveProduct();
			if (product != null)
			{
				ILanguageManager langMan = product.getLanguageManager();
				if (langMan != null)
				{
					retLang = langMan.getLanguageForFile(sourceFileName);
				}
			}
		}
		return retLang;
	}
	
	/**
	 * Establishes the appropriate XML elements for this UML type.
	 *
	 * [in] The document where this element will reside
	 * [in] The element's parent node.
	 */
	public void establishNodePresence(Document doc, Node parent)
	{
		buildNodePresence("UML:SourceFileArtifact",doc,parent);
	}	

	/** 
	 * Return the absolute path to the source file.
	 * 
	 * @return pVal[out] the absolute path to the source file.
	 */
	public String getName()
	{
		return getSourceFile();
	}
	
	/** 
	 * Returns the short name of the source file artifact which is just the file name
	 * of the artifact minus any directory information.
	 * 
	 * @return pVal[out] The source file artifact's short name
	 */
	public String getShortName()
	{
		String sourceFile = getSourceFile();
		// find the file portion of the path
		File file = new File(sourceFile);
		String fileName = null;
		if (file != null)
		{
			fileName = file.getName();
		}		
		return fileName;
	}
	
	/** 
	 * Returns the drive (in X: format) of the volume that the source file
	 * artifact resides on.
	 * 
	 * @return pVal[out] The drive 
	 */
	public String getDrive()
	{
		String sourceFile = getSourceFile();
		// find the file portion of the path
		File file = new File(sourceFile);
		String fileName = null;
		String drive = null;
		//strip off everything but the drive letter and the colon
		if (file != null)
		{
			fileName = file.toString();
            int sl = fileName.indexOf(File.separatorChar);
            if (sl == 2)
            {
                drive = fileName.substring(0, sl);
                if (!Character.isLetter(drive.charAt(0)) ||
                        drive.charAt(1) != ':')
                    drive = null;
            }
		}		
		return drive;
	}
	
	/** 
	 * Returns the source file artifact's base directory with no trailing directory delimiter.
	 * 
	 * @return pVal[out] The base directory for the source file artifact
	 */ 
	public String getBaseDirectory()
	{
        IClassifier owner = UMLUtilities.getOwningClassifier(this);
        if (owner != null)
        {
            IClassifier outermost = UMLUtilities.getOutermostNestingClass(owner);
            if (outermost != null)
                return getBaseDir(getSourceFile(), outermost.getQualifiedName());
        }
		return null;
	}
    
    /** 
	 * Returns the directory that the source file artifact resides in (with no
	 * trailing directory delimiter (e.g. backslash, yen symbol)).
	 * 
	 * @return pVal[out] The directory that the source file resides in
	 */
	public String getDirectory()
	{
		// get the full source file path
		String sourceFile = getSourceFile();
		//strip off the file name
		File file = new File(sourceFile);
		return file.getParent();		
	}
	
	/** 
	 * registers an ISourceFileArtifactEventsSink to receive source code
	 * modification events for this Source File Artifact.
	 * 
	 * @param pEventsSink[in] the event sink to register
	 * @return cookie[out] the cookie that should be used to unregister the event sink.
	 */
	public void registerForSourceFileArtifactEvents(ISourceFileArtifactEventsSink pEventsSink)
	{
		/**
		 *  See if the sink is already registered.
		 */		
		if (!m_eventSinks.contains(pEventsSink))
		{
			m_eventSinks.add(pEventsSink);
		}
	}
    
	public void revokeSourceFileArtifactSink(ISourceFileArtifactEventsSink pEventsSink)
	{
        m_eventSinks.remove(pEventsSink);
	}
	/** 
	 * Notifies all registered event sinks that some text was inserted into the artifact's source code.
	 * 
	 * @param fileOffset[in] the location at which the text was inserted
	 * @param insertedText[in] the text that was inserted
	 */
	public void fireTextInserted( int fileOffset, String insertedText )
	{
		if (m_eventSinks != null && m_eventSinks.size() > 0)
		{
			Iterator iter = m_eventSinks.iterator();
			while (iter.hasNext())
			{
				ISourceFileArtifactEventsSink eSink = 
						(ISourceFileArtifactEventsSink)iter.next();
				eSink.onTextInserted(fileOffset,insertedText);		
			}
		}						
	}
	

	/** 
	 * Notifies all registered event sinks that a range of source code was deleted
	 * 
	 * @param rangeStart[in] start of the deleted range
	 * @param rangeEnd[in] end of the deleted range
	 * @param deletedText[in] the text that was deleted
	 */
	public void fireRangeDeleted( int rangeStart, int rangeEnd, String deletedText )
	{
		if (m_eventSinks != null && m_eventSinks.size() > 0)
		{
			Iterator iter = m_eventSinks.iterator();
			while (iter.hasNext())
			{
				ISourceFileArtifactEventsSink eSink = 
						(ISourceFileArtifactEventsSink)iter.next();
				eSink.onRangeDeleted(rangeStart,rangeEnd,deletedText);		
			}
		}		
	}

	/** 
	 * Notifies all registered event sinks that a range of source code has been modified.
	 * 
	 * @param rangeStart[in] beginning of the range
	 * @param rangeEnd[in] end of the range
	 * @param originalText[in] what text used to be in that range
	 * @param newText[in] the text that replaced the original text
	 */
	public void fireRangeModified( int rangeStart, int rangeEnd, String originalText, String newText )
	{
		if (m_eventSinks != null && m_eventSinks.size() > 0)
		{
			Iterator iter = m_eventSinks.iterator();
			while (iter.hasNext())
			{
				ISourceFileArtifactEventsSink eSink = 
						(ISourceFileArtifactEventsSink)iter.next();
				eSink.onRangeModified(rangeStart,rangeEnd,originalText,newText);
			}
		}		
	}
	
	/** 
	 * Calculates the CRC of the artifact's source code
	 * 
	 * @return calculatedCRC[out] the calculated CRC of the current source code
	 */
	public int calculateCRC()
	{
		String sourceCode = getSourceCode();
		int crc = 0;
        if (sourceCode != null)
        {
    		for (int i=0;i<sourceCode.length();i++)
    		{
    			crc += sourceCode.charAt(i);
    		}
        }
		return crc;
	}
	
	/** 
	 * Returns an ISourceCodeManipulation object that allows read and
	 * write access to the ISourceFileArtifact's source code.
	 *
	 * @param pFileSystemManipulation[in] the IFileSystemManipulation object that will manage
	 *        the source file artifact's file.
	 *
	 * @param pSourceFileManipulation[out] an ISourceCodeManipulation object that can
	 *                                     be used to modify the ISourceFileArtifact's
	 *                                     source code.
	 */
	public ISourceCodeManipulation modify(IFileSystemManipulation pFileSystemManipulation)
	{
        if (m_pSourceManipulation == null)
        {
            m_pSourceManipulation = pFileSystemManipulation.modifyFile(getSourceFile());
            if (m_pSourceManipulation != null)
            {
                if (firePreDirty())
                {
                    m_pSourceManipulation.setSourceCode(getSourceCode());
                    m_pSourceManipulation.setSourceFileArtifact(this);
                    
                    fireDirty();
                }
            }
        }
		return m_pSourceManipulation;
	}
	
	/** 
	 * Returns this object's Source Code
	 * 
	 * @return pVal[out] the source code.
	 *
	 * @see Modify if you want to actually modify the source code.  There is no put_Source code method (yet).
	 */
	public String getSourceCode()
	{
		String srcCode = null;
		if( m_pSourceCodeHolder != null)
		{
			srcCode = m_pSourceCodeHolder.getSourceCode();
		}
		else if (m_pSourceManipulation != null)
		{
			srcCode = m_pSourceManipulation.getSourceCode();
		}
		else
		{
			srcCode = readSourceCodeFromDisk();
		}
		return srcCode;
	}
	
	/** 
	 * Reads the artifact's source code directly from disk and returns it via @a sourceCode
	 * 
	 * @return sourceCode[out] the source code that was read from disk
	 */
	protected String readSourceCodeFromDisk()
	{
		byte[] srcBytes = null;
		String sourceFileName = getSourceFile();
		if (sourceFileName != null)
		{
			// can only read from a file that exists.
			File file = new File(sourceFileName);
                        FileObject fo=FileUtil.toFileObject(file);
			if (fo!=null && !fo.isFolder())
			{
				int length = (int)fo.getSize();
				srcBytes = new byte[length];
				InputStream fis = null;
				try
				{
					fis = fo.getInputStream();
					fis.read(srcBytes);
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}	
                                finally
                                {
                                    try {
                                        if (fis != null) {
                                            fis.close();
                                        }
                                    } catch (IOException ex) {
                                        Exceptions.printStackTrace(ex);
                                    }
                                }
			}
		}
		return srcBytes != null? new String(srcBytes) : null;
	}
	
	/** 
	 * returns the current source code holder object (if there is one)
	 * 
	 * @return pVal[out] the current ISourceCodeHolder object (if there is one)
	 */
	public ISourceCodeHolder getSourceCodeHolder()
	{
		return m_pSourceCodeHolder;
	}
	
	/** 
	 * Sets the current source code holder.  If one already exists, it is overwritten.
	 * 
	 * @param newVal[in] the new source code holder (or NULL to remove the current source code holder).
	 */
	public void setSourceCodeHolder(ISourceCodeHolder newVal)
	{
		m_pSourceCodeHolder = newVal;
	}
	

	/** 
	 * Notify all ISourceFileArtifactEventsSink objects that the
	 * Artifact's changes are being committed.
	 */
	public void fireCommit()
	{
		//need to be coded.
	}
	
	/** 
	 * commits all changes made to this ISourceFileArtifact's source code.
	 */
	public void commitChanges()
	{
		fireCommit();
		if (m_pSourceCodeHolder != null)
		{
			m_pSourceCodeHolder.onCommit();
		}
		String sourceCode = getSourceCode();
		String sourceFileName = getSourceFile();
		// We can throw away any modifications that were made.
		m_pSourceManipulation = null;
		if (sourceFileName != null && sourceCode != null)
		{
			try
			{
				File file = new File(sourceFileName);
                                FileObject fo=FileUtil.toFileObject(file);
				if (fo != null)
				{
					OutputStream fos = fo.getOutputStream();
					fos.write(sourceCode.getBytes());
                                        fos.close();
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
	}
	
	public void setSourceCode(String newVal)
	{
		if (m_pSourceManipulation != null)
		{
			m_pSourceManipulation.setSourceCode(newVal);
		}
	}
	
	/**
	 * The absolute path to the source file.
	 * @return String
	 */
	public String getSourceFile()
	{
		return getFileName();		
	}
	
	/**
	 * The absolute path to the source file.
	 *
	 * @param newVal[in]
	 */
	public void setSourceFile(String newVal)
	{
		String oldFileName = getFileName();
		if (newVal != null && (oldFileName == null ||
                !(new File(newVal).toString()).equals((new File(oldFileName)).toString())))
		{
			setFileName(newVal);
			fireSourceFileNameChanged(oldFileName,newVal);
		}
	}
	
	/** 
	 * Notifies all registered event sinks that the artifact's source file name was changed.
	 * 
	 * @param oldFileName[in] old file name
	 * @param newFileName[in] new file name
	 */
	public void fireSourceFileNameChanged(String oldName, String newName)
	{
		if (m_eventSinks != null && m_eventSinks.size() > 0)
		{
			Iterator<ISourceFileArtifactEventsSink> iter = m_eventSinks.iterator();
			while (iter.hasNext())
			{
				ISourceFileArtifactEventsSink eSink = iter.next();
				eSink.onSourceFileNameChanged(oldName,newName);		
			}
		}
	}
	
	public boolean firePreDirty()
	{
		boolean retVal = false;
		EventDispatchRetriever ret = EventDispatchRetriever.instance();
		IStructureEventDispatcher disp = (IStructureEventDispatcher) 
						  ret.getDispatcher(EventDispatchNameKeeper.structure());
		if (disp != null)
		{			
			retVal = disp.fireArtifactPreDirty(this,null);									  
		}
		return retVal;
	}
	
	public void fireDirty()
	{		
		EventDispatchRetriever ret = EventDispatchRetriever.instance();
		IStructureEventDispatcher disp = (IStructureEventDispatcher) 
						  ret.getDispatcher(EventDispatchNameKeeper.structure());
		if (disp != null)
		{			
			disp.fireArtifactDirty(this,null);									  
		}		
	}	
	
	public boolean ensureWriteAccess()
	{
		boolean canWrite = false;
		if( firePreDirty() )
		{
		   canWrite = true;
		   fireDirty();
		}
		return canWrite;
	}
}


