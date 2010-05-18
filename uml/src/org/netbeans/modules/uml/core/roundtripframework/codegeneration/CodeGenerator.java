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

package org.netbeans.modules.uml.core.roundtripframework.codegeneration;

import java.util.Collection;
import java.util.Hashtable;
import java.util.List;

import org.dom4j.Node;

import org.netbeans.modules.uml.common.generics.ETPairT;
import org.netbeans.modules.uml.core.coreapplication.ICoreProduct;
import org.netbeans.modules.uml.core.coreapplication.IPreferenceManager2;
import org.netbeans.modules.uml.core.eventframework.EventBlocker;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.UMLXMLManip;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.INavigableEnd;
import org.netbeans.modules.uml.core.metamodel.structure.IProject;
import org.netbeans.modules.uml.core.preferenceframework.IPreferenceAccessor;
import org.netbeans.modules.uml.core.preferenceframework.PreferenceAccessor;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ILanguage;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ILanguageManager;
import org.netbeans.modules.uml.core.roundtripframework.IRoundTripController;
import org.netbeans.modules.uml.core.roundtripframework.RTMode;
import org.netbeans.modules.uml.core.support.umlmessagingcore.UMLMessagingHelper;
import org.netbeans.modules.uml.core.support.umlsupport.StringUtilities;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.core.support.umlutils.NameManager;
import org.netbeans.modules.uml.ui.support.ProductHelper;
import org.netbeans.modules.uml.ui.support.ProgressDialogMessageKind;
import org.netbeans.modules.uml.ui.support.messaging.IModalModeKind;
import org.netbeans.modules.uml.ui.support.messaging.IProgressController;
import org.netbeans.modules.uml.ui.support.messaging.IProgressDialog;
import org.openide.util.NbPreferences;

public class CodeGenerator implements ICodeGenerator, IProgressController
{
	private String m_LanguageName = "";
	private IProgressDialog	m_ProgressDialog = null;
	private boolean	m_Cancelled = false;
	private boolean	m_Done = false;

	/**
	 *
	 * Generates code for the given language for all the elements passed in.
	 *
	 * @param languageName[in] Name of the language to generate in
	 * @param elements[in]     The elements to generate for
	 *
	 * @return HRESULT
	 *
	 */
	public void generateCode( String languageName, ETList<IElement> pElements )
	{
		if (pElements != null)
		{
	         ICoreProduct pProd = ProductHelper.getCoreProduct();
	         if( pProd != null)
	         {
	           	ILanguageManager pMan = pProd.getLanguageManager();
	            if( pMan != null )
	            {
	               ILanguage pLang = pMan.getLanguage(languageName);
	               if( pLang != null )
	               {
	                  m_LanguageName = languageName;
	                  generateCode( pLang, pElements );
	               }
	               else
	               {
	                  if( languageName != null && languageName.length() > 0)
	                  {
	                  	//UMLMessagingHelper help = new UMLMessagingHelper();
	                  	/*
	                     UMLMessagingHelper __help( AfxGetResourceHandle(), IDS_MESSAGINGFACILITY );
	                     CComBSTR message;
	                     if( message.LoadString( AfxGetResourceHandle(),  IDS_LANGUAGE_NOT_FOUND ))
	                     { 
	                        USES_CONVERSION; 
	                        xstring mess( W2T( message ));
	
	                        _VH( __help.SendInfoMessage( mess.replace( mess.find( _T( "%1" )), 2, W2T( languageName ))));
	                     }
	                     */
	                  }
	               }
	            }
	         }
	      }
	   }
	
	/**
	 *
	 * Loops through all the passed in elements, generating code for any
	 * element that does not have an artifact that is already associated with
	 * the passed in language
	 *
	 * @param pLang[in]     The language that code is being generated in
	 * @param elements[in]  The collection of elements to generate for
	 *
	 * @return HRESULT
	 *
	 */
        private void generateCode( ILanguage pLang, ETList<IElement> pElements )
        {
            if (pLang != null && pElements != null)
            {
                int cnt = pElements.size();
                if (cnt > 0)
                {
                    establishProgress(cnt);
                    String tempMessage = translateString("IDS_GENERATING");
                    Integer i = new Integer(cnt);
                    String numFileBuffer = i.toString();
                    String message = tempMessage;
                    
                    String langName = pLang.getName();
                    ProjectKeeper keep = new ProjectKeeper();
                    RoundTripState state = new RoundTripState();
                    String unnamedValue = getUnnamedValue();
                    
                    boolean rtLive = state.m_Controller.getMode() == RTMode.RTM_LIVE;
                    
                    
                    for (int x = 0; x < cnt && !m_Cancelled; x++)
                    {
                        String groupMessage = message;
                        Integer i2 = new Integer(x + 1);
                        String buffer = i2.toString();
                        groupMessage = StringUtilities.replaceSubString( groupMessage, "%1", buffer );
                        groupMessage = StringUtilities.replaceSubString( groupMessage, "%2", numFileBuffer );
                        if( m_ProgressDialog != null )
                        {
                            m_ProgressDialog.setGroupingTitle(groupMessage);
                        }
                        IElement pElement = pElements.get(x);
                        if (pElement instanceof INamedElement)
                        {
                            INamedElement namedElement = (INamedElement)pElement;
                            String elementName = namedElement.getName();
                            if( m_ProgressDialog != null )
                            {
                                m_ProgressDialog.setFieldOne( elementName );
                            }
                            // Make sure the element doesn't already have an
                            // artifact associated with the passed in language
                            if( oKToCodeGen( langName, namedElement ))
                            {
                                if (!rtLive)
                                    state.m_Controller.setMode(RTMode.RTM_LIVE);
                                
                                generateCodeForElement(
                                    unnamedValue, namedElement);
                                
                                if (!rtLive)
                                    state.m_Controller.setMode(RTMode.RTM_OFF);
                            }
                        }
                        else
                        {
                            if( m_ProgressDialog != null )
                            {
                                String codeExists = translateString("IDS_CODE_EXISTS");
                                m_ProgressDialog.setFieldTwo( codeExists, ProgressDialogMessageKind.PDMK_ERROR );
                            }
                        }
                    }
                    
                    if( m_ProgressDialog != null )
                    {
                        m_ProgressDialog.increment();
                        m_ProgressDialog.clearFields();
                    }
                    keep.dispose();
                    state.dispose();
                }
                endProgress(false);
            }
        }
	
	private class ProjectKeeper
	{
		private Hashtable<String, ProjectState> m_Projects = new Hashtable<String, ProjectState>();
		public ProjectKeeper()
		{
		}
		public void dispose()
		{
			Collection coll = m_Projects.values();
			Object[] objs = coll.toArray();
			int cnt = objs.length;
			for (int x = 0; x < cnt; x++)
			{
				ProjectState state = (ProjectState)objs[x];
				if (state != null){
					state.cleanUp();
				}
			}
		}
		/**
		 *
		 * We need to capture the state of the Project before we begin code gen, so that
		 * we can set it back after.
		 *
		 * @param languageName[in] The language name
		 * @param element[in]      The element to code gen
		 *
		 * @return HRESULT
		 *
		 */
	
	
		boolean prepareProject( String languageName, IElement pElement )
		{
		   boolean prepared = false;
		   if( pElement != null )
		   {
			  IProject pProj = pElement.getProject();
			  if( pProj != null )
			  {
				 String xmiID = pProj.getXMIID();
				 ProjectState state = m_Projects.get(xmiID);
				 if( state != null )
				 {
					prepared = true;
				 }
				 else
				 {
					EventBlocker blocker;
					ProjectState newstate = new ProjectState( pProj );
					pProj.setDefaultLanguage( languageName );
					pProj.setMode("PSK_IMPLEMENTATION");
					m_Projects.put(xmiID, newstate);
					prepared = true;
				 }
			  }
		   }
		   return prepared;
		}
		
	}
	/**
	 *
	 * This is a bit goofy. The way we're doing codegen right now is to make sure the
	 * Project that the element is in is in a code genable state, meaning that RoundTrip
	 * must be able to run. So the mode of the Project will be set to Implementation, and the
	 * default language will be set to the language specified in the initial call
	 * to GenerateCode. 
	 *
	 * We're taking advantage of the fact that RoundTrip does 100% code gen on elements when
	 * transitioning from the Unnamed state, to a named state. So we block events, get
	 * the original name of the element, set it to the unnamed value, un plug events, then set
	 * the name back to the original name. This will kick roundtrip in, language specific
	 * request processors fire, and so on.
	 *
	 * @param unnamedValue[in] The value of the unnamed classifier preference
	 * @param element[in]      The element to code gen
	 *
	 * @return HRESULT
	 *
	 */
	private void generateCodeForElement( String unnamedValue, INamedElement element )
	{
		if (element != null)
		{
	      String origName = element.getName();
	      {
	         // Plug events so that nothing triggers for the setting back to the unnamedValue
	         boolean rtState = EventBlocker.startBlocking();
             try {
	         ensureUniqueRoleNames( element );
	         element.setName( unnamedValue );
	      }
             finally {
                EventBlocker.stopBlocking(rtState);
             }
	      }
	
	      // Now we need to pull all the SourceFileArtifacts off the element, just temporarily,
	      // so that roundtrip see this element as brand new.
	      
	      ArtifactState state = new ArtifactState( element );
	      // This will cause the code generation to happen
	      element.setName( origName );
	      state.dispose();
	   }
	}
	
	/**
	 *
	 * Get the unnamed value for new elements
	 *
	 * @return The value
	 *
	 */
	private String getUnnamedValue()
	{
		String unnamed = "";
		IPreferenceAccessor pPref = PreferenceAccessor.instance();
		if (pPref != null)
		{
			unnamed = pPref.getDefaultElementName();
		}
	   return unnamed;
	}
	
	private class RoundTripState
	{
		private IRoundTripController m_Controller = null;
		private int m_OrigMode = -1;
		public RoundTripState()
		{
		   ICoreProduct pProd =  ProductHelper.getCoreProduct();
		   if( pProd != null )
		   {
		      m_Controller = pProd.getRoundTripController();
		      if( m_Controller != null )
		      {
		         m_OrigMode = m_Controller.getMode();
		         m_Controller.setMode( RTMode.RTM_LIVE );
		      }
		   }
		}
		public void dispose()
		{
		   if( m_Controller != null )
		   {
		       m_Controller.setMode( m_OrigMode );
		   }
		}
	}
	
	private class ProjectState
	{
		private IProject m_Project = null;
		private String m_OrigLang = "";
		private String m_OrigMode = "";
		public ProjectState(IProject pProject)
		{
			m_Project = pProject;
		   if( m_Project != null )
		   {
		      m_OrigLang = m_Project.getDefaultLanguage();
			  m_OrigMode = m_Project.getMode();
		   }
		}
		/*
		CCodeGenerator::ProjectState::ProjectState( const ProjectState& copy )
		{
		   Copy( copy );
		}
		*/
		public void cleanUp()
		{
		   if( m_Project != null )
		   {
		      EventBlocker blocker;
		      m_Project.setDefaultLanguage( m_OrigLang );
		      m_Project.setMode( m_OrigMode );
		   }
		}
		public void dispose()
		{
		   // Can't put this code in the destructor, as the act of putting
		   // a ProjectState into a map will cause the destruction
		   if( m_Project != null )
		   {
		      EventBlocker blocker;
		      m_Project.setDefaultLanguage( m_OrigLang );
		      m_Project.setMode( m_OrigMode );
		   }
		}
		/*	
		void CCodeGenerator::ProjectState::Copy( const ProjectState& copy )
		{
		   m_Project   = copy.m_Project;
		   m_OrigMode  = copy.m_OrigMode;
		   m_OrigLang  = copy.m_OrigLang;
		}
		*/
		/*
		CCodeGenerator::ProjectState& CCodeGenerator::ProjectState::operator=( const ProjectState& rh )
		{
		   if( this != &rh )
		   {
		      Copy( rh );
		   }
		
		   return *this;
		}
		*/
	}
	
	/**
	 *
	 * Determines whether or not the passed in element has any SourceFileArtifacts
	 * that are associated with the language whose name is passed in.
	 *
	 * @param name[in]      Name of the language
	 * @param element[in]   The element to check
	 *
	 * @return true if it is alright to codegen for the element, else false
	 *
	 */
	private boolean oKToCodeGen( String name, IElement pElement )
	{
	   boolean ok = false;
	
	   if( pElement != null)
	   {
	      ETList < IElement > arts = pElement.getSourceFiles2(name);
	      if( arts != null )
	      {
	         int num = arts.size();
	         if( num == 0 )
	         {
	            ok = true;
	         }
	      }
	      else
	      {
	         ok = true;
	      }
	   }
	   return ok;
	}
	
	/**
	 *
	 * Called if the user hits the Cancel button on the ProgressDialog
	 *
	 *
	 * @return HRESULT
	 *
	 */
	public void onCancelled() {
		m_Cancelled = true;
		m_ProgressDialog = null;
	}
	
	/**
	 *
	 * Prepares the Progress Dialog
	 *
	 * @return HRESULT
	 */
	public void establishProgress(int numFiles) 
	{
			
		if (m_ProgressDialog == null) {
			m_ProgressDialog = ProductHelper.getProgressDialog();
		}
	
		if (m_ProgressDialog != null) {
			m_ProgressDialog.setCollapse(true);
			// Make the limits twice that of the number of files
			// to take into account the integration phase
			m_ProgressDialog.setLimits(new ETPairT < Integer, Integer > (new Integer(0), new Integer((int)numFiles)));
			String title = translateString("IDS_CODE_GEN_TITLE");
			m_ProgressDialog.setTitle(title);
			m_ProgressDialog.setProgressController((IProgressController) this);
			boolean bStatus = m_ProgressDialog.display(IModalModeKind.MMK_MODELESS);
		}
	}
		
	/**
	 * Prepare the progress dialog for closure
	 *
	 * @param err[in]
	 *
	 * @return HRESULT
	 *
	 */	
	public void endProgress(boolean err) {
			
		boolean status = false;
			
		if (m_ProgressDialog != null) {
				
			String doneStr = translateString("IDS_DONE");
			String errStr = translateString("IDS_ERROR");
			String buttonText = doneStr;
			m_Done = true;
				
			if (err) {
				buttonText = errStr;
				m_Done = false;
			}
				
			m_ProgressDialog.clearFields();
			m_ProgressDialog.setPosition(0);
			m_ProgressDialog.setCollapse(false);
				
			m_ProgressDialog.promptForClosure(buttonText, true);
		}
	}
	public void onProgressEnd() {
	   this.onCancelled();
	}
	
	/**
	 *
	 * ArtifactState removes any existing SourceFileArtifact elements from the passed in
	 * element temporarily ( it puts them back in the destructor ). This is done to prevent 
	 * roundtrip for ignoring the default language set on the project, which code gen has
	 * just put for the specific language being code gend. For example, if a Java source file
	 * artifact is already assocated with the element, and the user wanted to gen into a VB file,
	 * RT would ignore the fact that CodeGen has temporarily put the the DefalualtLanguage
	 * property to VB, and would use the JavaRequestProcessor instead.
	 *
	 * @param element[in] The element about to be gend
	 *
	 */
	private class ArtifactState
	{
		private INamedElement m_Element = null;
		private List m_RemovedArtifacts = null;
		public ArtifactState(INamedElement pElement)
		{
			m_Element = pElement;
		   if( m_Element != null )
		   {
		      Node node = m_Element.getNode();
		      if( node != null )
		      {
		         m_RemovedArtifacts = node.selectNodes("./UML:Element.ownedElement/UML:SourceFileArtifact");
		         if( m_RemovedArtifacts != null )
		         {
		            int num = m_RemovedArtifacts.size();
		            for( int x = 0; x < num; x++ )
		            {
		               Node artNode = (Node)m_RemovedArtifacts.get(x);
		               if( artNode != null )
		               {
		                  Node parent = artNode.getParent();
		                  if( parent != null )
		                  {
		                  	artNode.detach();
		                  }
		               }
		            }
		         }
		      }
		   }
		}
		/**
		 * See the constructor comment for details
		 */
		public void dispose()
		{
		   if( m_Element != null && m_RemovedArtifacts != null)
		   {
		      Node node = m_Element.getNode();
		      if( node != null )
		      {
		         Node parent = node.selectSingleNode("./UML:Element.ownedElement");
		         if( parent != null )
		         {
		            int num = m_RemovedArtifacts.size();
		            for( int x = 0; x < num; x++ )
		            {
		               Node artNode = (Node)m_RemovedArtifacts.get(x);
		               if( artNode != null )
		               {
						((org.dom4j.Element)parent).add(artNode);
		               }
		            }
		         }
		      }
		   }
		}
	}
	
	/**
	 *
	 * Makes sure that all navigable ends are properly named
	 *
	 * @param element[in]   The element to query for navigable ends for
	 *
	 * @return HRESULT
	 *
	 */
	private void ensureUniqueRoleNames(IElement element)
	{
		if (element != null)
		{
			if (element instanceof IClassifier)
			{
				IClassifier classifier = (IClassifier)element;
	         	ETList <INavigableEnd> ends = classifier.getOutboundNavigableEnds();
	
	         	if( ends != null )
	         	{
	            	int num = ends.size();
		            if( num > 0)
		            {
		               NameManager manager = new NameManager();
		               for( int x = 0; x < num; x++ )
		               {
		                  INavigableEnd end = ends.get(x);
		                  if( end != null )
		                  {
		                     String curName = end.getName();
		                     if( curName == null || curName.length() == 0 )
		                     {
		                        String attrPrefix = getAttrPrefix( end );
		                        manager.ensureUniqueRoleName( end, attrPrefix, attrPrefix, 0 );
		                     }
		                  }
		               }
	            	}
	         	}
	      	}
	   	}
	}
	
	/**
	 *
	 * Retrieves the name to use for a attribute or navigable end that includes
	 * the prefix set in the preferences and the name of the participant on the end.
	 *
	 * @param end[in] The end to generate a name for
	 *
	 * @return HRESULT
	 *
	 */
	private String getAttrPrefix(INavigableEnd end)
	{
	   String prefix = "";
	   if( end != null )
	   {
	      IClassifier pClass = end.getParticipant();
	      if ( pClass != null )
	      {
	         // Use this class as the type, which will be part of the default name.
	         prefix = getAttrPrefixFromPreferences();
	         String typeName = pClass.getName();
	         String attrName = prefix;
	         attrName += typeName;
	         prefix = attrName;
	      }
	   }
	   return prefix;
	}
	
	/**
	 *
	 * Retrieves the language specific prefix to be used for attribute names
	 *
	 * @return The prefix, if any
	 *
	 */
	private String getAttrPrefixFromPreferences()
	{
	   return NbPreferences.forModule (CodeGenerator.class).get ("UML_ATTRIBUTE_PREFIX", "m"); // NOI18N
	}
	
	private String getLanguageName()
	{
	   return m_LanguageName;
	}
	private String translateString(String inStr)
	{
		return DefaultCodeGenerationResource.getString(inStr);
	}
	
}
