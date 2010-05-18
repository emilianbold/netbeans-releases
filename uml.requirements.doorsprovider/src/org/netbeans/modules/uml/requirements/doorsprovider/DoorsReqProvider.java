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
 * DoorsReqProvider.java
 *
 * Created on July 2, 2004, 7:04 AM
 */

package org.netbeans.modules.uml.requirements.doorsprovider;

//import org.netbeans.modules.uml.core.addinframework.IAddInDescriptor;
import java.awt.Dialog;
import org.netbeans.modules.uml.core.metamodel.structure.IRequirementArtifact;
import org.netbeans.modules.uml.core.requirementsframework.IRequirement;
import org.netbeans.modules.uml.core.requirementsframework.IRequirementSource;
import org.netbeans.modules.uml.core.requirementsframework.RequirementSource;
import org.netbeans.modules.uml.core.requirementsframework.IRequirementsProvider;
import org.netbeans.modules.uml.core.requirementsframework.RequirementsException;
import org.netbeans.modules.uml.core.requirementsframework.RequirementUtility;
import org.netbeans.modules.uml.core.support.umlsupport.XMLManip;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.ui.support.ProductHelper;
import org.netbeans.modules.uml.ui.support.UIFactory;
import org.netbeans.modules.uml.ui.support.applicationmanager.IProxyUserInterface;
import org.netbeans.modules.uml.ui.support.commondialogs.IQuestionDialog;
import org.netbeans.modules.uml.ui.support.commondialogs.MessageDialogKindEnum;
import org.netbeans.modules.uml.ui.support.commondialogs.MessageIconKindEnum;
import org.netbeans.modules.uml.ui.support.commondialogs.MessageResultKindEnum;
import org.netbeans.modules.uml.ui.support.commondialogs.IErrorDialog;
import java.awt.Frame;
import org.dom4j.Document;
import org.dom4j.Node;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

/**
 *
 * @author  Trey Spiva
 */
public class DoorsReqProvider implements IDoorsReqProvider, IRequirementsProvider
{
   private String m_ProgID = "org.netbeans.modules.uml.ui.products.ad.requirementsprovider.doorprovider";
   private static boolean m_bProviderAvailable = true;
   //private BundleSupport m_Bundle = new BundleSupport("org/netbeans/modules/uml/requirements/doorsprovider/Bundle");
   
   /** Creates a new instance of DoorsReqProvider */
   public DoorsReqProvider()
   {
   }
   
   public String getLocation()
   {
      return "";
   }
   
   public String getID()
   {
      return "";
   }
   
   public String getName()
   {
      return "DoorsReqProvider";
   }
   public String getProgID( )
   {
      return m_ProgID;
   }
   
   public void setProgID( String sProgID )
   {
      m_ProgID = sProgID;
   }
   
   public String getDisplayName()
   {
      return "DOORS Requirements Provider";
   }
   
   public String getDescription()
   {
       return NbBundle.getMessage(DoorsReqProvider.class, "" +
               "IDS_DOORS_Requirement_Provider_Desc");
   }
   
   /**
    * Called when the addin is initialized.
    */
   public long initialize(  Object  context )
   {
      return 0;
   }
  
   
   /**
    * Called when the addin is deinitialized.
    */
   public long deInitialize(  Object  context )
   {
      return 0;
   }
   
   /**
    *
    * Called when the addin is unloaded.
    *
    * @param context[in]
    *
    * @return void
    *
    */
   public long unLoad(  Object  context)
   {
      return 0;
   }
   
   /**
    * Get the version of the addin.
    */
   public String getVersion()
   {
      return "1.0";
   }
   
   
   /**
    * Build and return an IRequirementSource.
    *
    * @param pAddInDescriptor Descriptor, from Registry, contains ProgID and Friendly
    *                         Name to be put into IRequirementSource and .etd file.
    * @return Addr of IRequirementSource ptr.
    */   
   public IRequirementSource displaySources( /*IAddInDescriptor  pAddInDescriptor*/ )
      throws RequirementsException
   
   {
      IRequirementSource retVal = null;
      
      // We support doors only on windows. Display a popup and return.
      if (!java.io.File.separator.equals("\\"))
      {
          IErrorDialog errDialog = UIFactory.createErrorDialog();          
          errDialog.display(NbBundle.getMessage(DoorsReqProvider.class,"IDS_OSNOTSUPPORTED"),"");
          throw new RequirementsException(NbBundle.getMessage(DoorsReqProvider.class,"IDS_OSNOTSUPPORTED"));
      }
      
//      if( null == pAddInDescriptor) 
//      {
//         throw new IllegalArgumentException();
//      }       
      
      retVal = new RequirementSource();
      
      IProxyUserInterface ui = ProductHelper.getProxyUserInterface();
      Frame parent = null;
      if(ui != null)
      {
         parent = ui.getWindowHandle();
      }
      try
      {
          String dlgTitle = NbBundle.getMessage(DoorsReqProvider.class, "IDS_DOORS_DLG_TITLE");
          DoorsProjectDialog doorsProjPanel = new DoorsProjectDialog();
          if (!doorsProjPanel.hasError()) {
              DialogDescriptor dialogDescriptor = new DialogDescriptor(doorsProjPanel, dlgTitle);
              Dialog dialog = DialogDisplayer.getDefault().createDialog(dialogDescriptor);  // OK/CANCEL dialog
              dialog.getAccessibleContext().setAccessibleDescription(dlgTitle);
			  try {
                  dialog.setVisible(true);
                  Object bttnPressed = dialogDescriptor.getValue();
                  if (bttnPressed == DialogDescriptor.OK_OPTION) 
                  {
                      String doorsLocation = doorsProjPanel.performAction(bttnPressed);
                      retVal.setLocation(doorsLocation);

                      // Currently handling whether the Requirement source requires a login or not.
                      retVal.setRequiresLogin( false );
                  
                      // Set a unique ID for the requirement source. For Doors, project name is unique.
                      // Use the 'location' (name) field set in the dialog.
                      retVal.setID( retVal.getLocation() );

                      // pAddInDescriptor.getFriendlyName( &cbsFriendlyName )
                      String strFriendlyName = "DOORS - " + retVal.getLocation();

                      // Set the name displayed in the design center tree for the requirements source.
                      retVal.setDisplayName( strFriendlyName );

                      // Set the prog Id for the requirement source provider.
                      //retVal.setProvider( pAddInDescriptor.getProgID());
                      retVal.setProvider( getProgID());
                  }
                  else 
                  {
                      throw new RequirementsException(RequirementsException.RP_E_NO_SELECTION,
                                             NbBundle.getMessage(DoorsReqProvider.class,"IDS_NOSELECTION"));
                  }
              }
              finally
              {
                  dialog.dispose();
              }
          }
//          DoorsProjectDialog dpDialog = new DoorsProjectDialog(parent, true);
//          dpDialog.setVisible(true);
//
//          if( dpDialog.wasAccepted() == true )
//          {
//             retVal.setLocation( dpDialog.getDoorsLocation( ) );
//
//             // Currently handling whether the Requirement source requires a login or not.
//             retVal.setRequiresLogin( false );
//
//             // Set a unique ID for the requirement source. For Doors, project name is unique.
//             // Use the 'location' (name) field set in the dialog.
//             retVal.setID( retVal.getLocation() );
//
//             // pAddInDescriptor.getFriendlyName( &cbsFriendlyName )
//             String strFriendlyName = "DOORS - " + retVal.getLocation();
//
//             // Set the name displayed in the design center tree for the requirements source.
//             retVal.setDisplayName( strFriendlyName );
//
//             // Set the prog Id for the requirement source provider.
//             //retVal.setProvider( pAddInDescriptor.getProgID());
//             retVal.setProvider( getProgID());
//          }
//          else
//          {
//             throw new RequirementsException(RequirementsException.RP_E_NO_SELECTION,
//                                             NbBundle.getMessage(DoorsReqProvider.class,"IDS_NOSELECTION"));
//          }
      }
      catch(UnsatisfiedLinkError ee)
      {   //ee.printStackTrace();
          String msg = NbBundle.getMessage(DoorsReqProvider.class,"IDS_REQUIREMENTLIBRARYNOTFOUND");
          NotifyDescriptor d = new NotifyDescriptor.Message(msg, NotifyDescriptor.ERROR_MESSAGE);
          DialogDisplayer.getDefault().notify(d);
          
          throw new RequirementsException(RequirementsException.RP_E_REQUIREMENTLIBRARYNOTFOUND,
                                          msg);          
      }
      catch(NoClassDefFoundError e)
      {
          String msg = NbBundle.getMessage(DoorsReqProvider.class,"IDS_REQUIREMENTLIBRARYNOTFOUND");
          NotifyDescriptor d = new NotifyDescriptor.Message(msg, NotifyDescriptor.ERROR_MESSAGE);
          DialogDisplayer.getDefault().notify(d);
          
          throw new RequirementsException(RequirementsException.RP_E_REQUIREMENTLIBRARYNOTFOUND,
                                          msg);        
      }
      
      return retVal;
   }
   
//   String getConfigScriptLocation() {
//       Properties prop = System.getProperties();
//       String value = prop.getProperty("netbeans.dirs");
//       String [] clusterDirs = value.split(";");
//       String dir = "";
//       for (int i = 0; i < clusterDirs.length; i++) {
//           dir = clusterDirs[i];
//           if (dir != null && dir.indexOf("uml") != -1) {
//               break;
//           }
//       }
//       return dir;
//   }
   /**
    * Given a RequirementSource, populate an IRequirements collection.
    *
    * @param IRequirementSource A RequirementSource that each derived provider should know
    *                           how to handle.
    * @retturn A collection of IRequirements.
    */
   public ETList < IRequirement > loadRequirements( IRequirementSource  pRequirementSource)
      throws RequirementsException
   {
      ETList < IRequirement > retVal = new ETArrayList < IRequirement >();
      
      if( null == pRequirementSource) 
      {
         throw new IllegalArgumentException();
      }
      
      try
      {
         if( m_bProviderAvailable == true)
         {
            // Ask the RequirementsSource for its project.            
            String strReqFile = pRequirementSource.getLocation();
            
            // Call Doors object to execute DXL GetProjectXML2 function.            
            //DIDoorsDXLPtr pDoors( CLSID_DoorsDXL );
            
            String strRequest = "#include \"GetProjectInfo.dxl\";";
            strRequest += "GetProjectXML2(\"";
            strRequest +=  strReqFile;
            strRequest += "\")";
            
            //pDoors.runStr( strRequest )
            //String strResult = pDoors.getresult();
            String strResult = DoorUtility.sendRequestToDoors(strRequest);
            
            if( strResult.equals("ProjectNotFound") == true  )
            {
               throw new RequirementsException(RequirementsException.RP_E_REQUIREMENTSOURCENOTFOUND,
                                               NbBundle.getMessage(DoorsReqProvider.class,"IDS_REQUIREMENTSOURCENOTFOUND"));
            }
            else
            {
               // Load the returned XML into a doc and send off to the ProcessChild to
               // build a nested IRequirements structure.
               
               Document pDoc = XMLManip.loadXML(strResult);
               
               if( pDoc != null )
               {
                  String strPattern = "/RequirementsProject";
                  Node node = XMLManip.selectSingleNode(pDoc, strPattern);
                  
                  if( node != null )
                  {
                     retVal = RequirementUtility.processChildElements( node, DoorsRequirement.class, null);
                  }
               }
            }
         }
         else
         {
            throw new RequirementsException(RequirementsException.RP_E_REQUIREMENTSOURCENOTFOUND,
                                            NbBundle.getMessage(DoorsReqProvider.class,"IDS_REQUIREMENTSOURCENOTFOUND"));
         }
      }
      catch( RequirementsException e )
      {
         Frame hwnd = null;
         IProxyUserInterface cpProxyUserInterface = ProductHelper.getProxyUserInterface();
         
         if( cpProxyUserInterface != null)
         {
            hwnd = cpProxyUserInterface.getWindowHandle();
         }
         
         String msgText = NbBundle.getMessage(DoorsReqProvider.class,"IDS_DOORSNOTAVAILABLEMESSAGE");
         String msgTitle= NbBundle.getMessage(DoorsReqProvider.class,"IDS_DOORSNOTAVAILABLETITLE" );
         
         IQuestionDialog cpQuestionDialog = UIFactory.createQuestionDialog();
         
         cpQuestionDialog.displaySimpleQuestionDialog( MessageDialogKindEnum.SQDK_OK,
                                                       MessageIconKindEnum.EDIK_ICONWARNING,
                                                       msgText ,
                                                       MessageResultKindEnum.SQDRK_RESULT_YES,
                                                       hwnd,
                                                       msgTitle  );
         m_bProviderAvailable = false;
      }
      catch(Exception e)
      {
         e.printStackTrace();
      }
      
      return retVal;
   }
   
   /**
    *
    * Given a RequirementID, find the Requirement
    *
    * @param IRequirementArtifact Requirement Artifact
    * @param IRequirementSource Requirement Source - not needed by this Provider derived type
    * @return The requirement
    */
   public IRequirement getRequirement( IRequirementArtifact  pRequirementArtifact,
                                        IRequirementSource  pRequirementSource)
   {
      IRequirement retVal = null;
      if( (pRequirementArtifact == null) || 
          (pRequirementSource == null)) 
      {
         throw new IllegalArgumentException();
      }
      
      try
      {
         if( m_bProviderAvailable == true)
         {
            //Ask the RequirementArtifact for its project and module.
            
            String strProjectName = pRequirementArtifact.getRequirementProjectName();
            String strModName = pRequirementArtifact.getRequirementModName();
            String strID = pRequirementArtifact.getRequirementID();
            
//            String strResult;
//            
//            DIDoorsDXLPtr pDoors( CLSID_DoorsDXL );
            
            // Call Doors object to execute DXL GetObjectByIDAsXML function.
            
            String strRequest = "#include \"GetProjectInfo.dxl\";";
            strRequest += "getObjectByIDAsXML2(\"";
            strRequest +=  strProjectName;
            strRequest += "\", \"";
            strRequest +=  strModName;
            strRequest += "\", ";
            strRequest +=  strID;
            strRequest += ")";
            
//            pDoors.runStr( strRequest )
//            strResult = pDoors.getresult();
            String strResult = DoorUtility.sendRequestToDoors(strRequest);
            
            // Load the returned XML into a doc and send off to the ProcessChild to
            // build a nested IRequirements structure.            
            Document pDoc = XMLManip.loadXML(strResult);
            
            if( pDoc != null )
            {
               String strPattern = "/RequirementsProject";
               Node node = XMLManip.selectSingleNode(pDoc, strPattern);
               
               if( node != null )
               {
                  // There should a <RequirementsProject> container node and one <Requirement> child node.
                  ETList < IRequirement > cpRequirements = RequirementUtility.processChildElements( node, DoorsRequirement.class, null);
                  
                  if( cpRequirements != null)
                  {
                     // One and Only.
                     if( cpRequirements.size() > 0 )
                     {
                        retVal = cpRequirements.get(0);
                     }                     
                  }
               }
            }
         }
      }
      catch( Exception e )
      {
         Frame hwnd = null;
         IProxyUserInterface cpProxyUserInterface = ProductHelper.getProxyUserInterface();
         
         if( cpProxyUserInterface != null)
         {
            hwnd = cpProxyUserInterface.getWindowHandle();
         }
         
         String msgText = NbBundle.getMessage(DoorsReqProvider.class,"IDS_DOORSNOTAVAILABLEMESSAGE");
         String msgTitle= NbBundle.getMessage(DoorsReqProvider.class,"IDS_DOORSNOTAVAILABLETITLE" );
         
         IQuestionDialog cpQuestionDialog = UIFactory.createQuestionDialog();         
         cpQuestionDialog.displaySimpleQuestionDialog( MessageDialogKindEnum.SQDK_OK,
                                                       MessageIconKindEnum.EDIK_ICONWARNING,
                                                       msgText ,
                                                       MessageResultKindEnum.SQDRK_RESULT_YES,
                                                       hwnd,
                                                       msgTitle  );
         m_bProviderAvailable = false;
      }
      return retVal;
   }
   
   /** save the design center addin */
    public void save()
    {
        // There is nothing to save.
    }
}
