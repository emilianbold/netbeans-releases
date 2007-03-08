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

package org.netbeans.modules.uml.project.ui;

import java.awt.Component;
import java.awt.Dialog;
import java.text.MessageFormat;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import java.awt.Frame;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.Window;
import java.net.URL;
import javax.swing.SwingUtilities;
import org.openide.util.Utilities;
import org.netbeans.modules.uml.ui.controls.newdialog.AddElementWizardIterator;
import org.netbeans.modules.uml.ui.controls.newdialog.AddPackageWizardIterator;
import org.openide.DialogDisplayer;
import org.openide.WizardDescriptor;

import org.openide.awt.HtmlBrowser;

import org.openide.windows.WindowManager;

import org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram;
import org.netbeans.modules.uml.core.metamodel.structure.IProject;
import org.netbeans.modules.uml.ui.controls.newdialog.INewDialogElementDetails;
import org.netbeans.modules.uml.ui.controls.newdialog.INewDialogPackageDetails;
import org.netbeans.modules.uml.ui.controls.newdialog.INewDialogResultProcessor;
import org.netbeans.modules.uml.ui.controls.newdialog.NewDialogElementDetails;
import org.netbeans.modules.uml.ui.controls.newdialog.NewDialogPackageDetails;
import org.netbeans.modules.uml.ui.controls.newdialog.NewDialogResultProcessor;
import org.netbeans.modules.uml.ui.support.applicationmanager.IProxyUserInterface;

/**
 *
 * @author Trey Spiva
 * @author Craig Conover, craig.conover@sun.com
 */
public class UMLUserInterface implements IProxyUserInterface
{
	
	/**
	 *
	 */
	public UMLUserInterface()
	{
		super();
	}
	
   /* (non-Javadoc)
	* @see org.netbeans.modules.uml.ui.support.applicationmanager.IProxyUserInterface#getWindowHandle()
	*/
	public Frame getWindowHandle()
	{
            // 86994, in sdi mode, use the current activated TC instead of IDE 
            // main window as the parent for the dialog
            Component comp = WindowManager.getDefault().getRegistry().getActivated();
            Window window = SwingUtilities.getWindowAncestor(comp);
            if (window instanceof Frame)
                return (Frame)window;
            else
            {
                // Since the WindowsManager.getMainWindow() can only be called from
                // AWT event dispatch thread use the MainWindowRetriever class
                // can be used to display the group.  However, the only time we need to
                // execute the invokeAndWait is when we are currently not in the
                // AWT event dispatch thread.
                //
                
                MainWindowRetriever retriever = new MainWindowRetriever();
                if(SwingUtilities.isEventDispatchThread() == true)
                {
                    retriever.retrieveWindow();
                }
                else
                {
			try
			{
				SwingUtilities.invokeAndWait(retriever);
			}
			catch(InterruptedException e)
			{
			}
			catch(java.lang.reflect.InvocationTargetException ie)
			{
			}
		}
		
		return retriever.getMainWindow();
            }
	}
	
	public class MainWindowRetriever implements Runnable
	{
		private Frame mMainWindow = null;
		
		public void run()
		{
			retrieveWindow();
		}
		
		public void retrieveWindow()
		{
			mMainWindow = WindowManager.getDefault().getMainWindow();
		}
		
		public Frame getMainWindow()
		{
			return mMainWindow;
		}
	}
	
	
   /* (non-Javadoc)
	* @see org.netbeans.modules.uml.ui.support.applicationmanager.IProxyUserInterface#dirtyStateChanged(org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram, boolean)
	*/
	public long dirtyStateChanged(IDiagram pDiagram, boolean bNewDirtyState)
	{
		// TODO Auto-generated method stub
		return 0;
	}
	
   /* (non-Javadoc)
	* @see org.netbeans.modules.uml.ui.support.applicationmanager.IProxyUserInterface#quit()
	*/
	public void quit()
	{
		// TODO Auto-generated method stub
		
	}
	
   /* (non-Javadoc)
	* @see org.netbeans.modules.uml.ui.support.applicationmanager.IProxyUserInterface#setVisible(boolean)
	*/
	public void setVisible(boolean value)
	{
		
	}
	
   /* (non-Javadoc)
	* @see org.netbeans.modules.uml.ui.support.applicationmanager.IProxyUserInterface#getVisible()
	*/
	public boolean getVisible()
	{
		return true;
	}
	
   /* (non-Javadoc)
	* @see org.netbeans.modules.uml.ui.support.applicationmanager.IProxyUserInterface#setPropertyEditorVisible(boolean)
	*/
	public void setPropertyEditorVisible(boolean value)
	{
		// TODO Auto-generated method stub
		
	}
	
   /* (non-Javadoc)
	* @see org.netbeans.modules.uml.ui.support.applicationmanager.IProxyUserInterface#getPropertyEditorVisible()
	*/
	public boolean getPropertyEditorVisible()
	{
		// TODO Auto-generated method stub
		return false;
	}
	
   /* (non-Javadoc)
	* @see org.netbeans.modules.uml.ui.support.applicationmanager.IProxyUserInterface#openWorkspaceDialog()
	*/
	public void openWorkspaceDialog()
	{
		// TODO Auto-generated method stub
		
	}
	
   /* (non-Javadoc)
	* @see org.netbeans.modules.uml.ui.support.applicationmanager.IProxyUserInterface#newWorkspaceDialog()
	*/
	public void newWorkspaceDialog()
	{
		// TODO Auto-generated method stub
		
	}
	
   /* (non-Javadoc)
	* @see org.netbeans.modules.uml.ui.support.applicationmanager.IProxyUserInterface#newPackageDialog(org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace)
	*/
	public IElement newPackageDialog(INamespace pDefaultNamespace)
	{
//		INewDialog diag = new NewDialog();
//		
//		diag.addTab(NewDialogTabKind.NWIK_NEW_PACKAGE);
		
		INewDialogPackageDetails details = new NewDialogPackageDetails();
		
		details.setAllowFromRESelection( false );
		details.setNamespace( pDefaultNamespace );
//		diag.specifyDefaults( details );
		
//		diag.display( null );
                //Jyothi:
                WizardDescriptor.Iterator iterator = new AddPackageWizardIterator();
                ((AddPackageWizardIterator)iterator).setDetails(details); //this is a hack to pass the details object
                WizardDescriptor wizardDescriptor = new WizardDescriptor(iterator);
                // {0} will be replaced by WizardDescriptor.Panel.getComponent().getName()
                // {1} will be replaced by WizardDescriptor.Iterator.name()
                wizardDescriptor.setTitleFormat(new MessageFormat("{0} ({1})"));
                wizardDescriptor.setTitle(org.openide.util.NbBundle.getMessage(UMLUserInterface.class, "IDS_NEW_PACKAGE_WIZARD_TITLE"));                
                Dialog dialog = DialogDisplayer.getDefault().createDialog(wizardDescriptor);
                dialog.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(UMLUserInterface.class, "IDS_NEW_PACKAGE_WIZARD_TITLE_DESCRIPTION"));
                dialog.setVisible(true);
                dialog.toFront();
                boolean cancelled = wizardDescriptor.getValue() != WizardDescriptor.FINISH_OPTION;
                if (!cancelled) {
                    // do something
                    Object results = wizardDescriptor.getProperty(AddPackageWizardIterator.PACKAGE_DETAILS);
                                        
//		INewDialogTabDetails results = diag.getResult();
                    
                    if( results != null && results instanceof INewDialogPackageDetails) {
                        INewDialogPackageDetails packResults = (INewDialogPackageDetails)results;
                        INewDialogResultProcessor processor = new NewDialogResultProcessor();
                        return processor.handleResult( packResults );
                    }
                }
                return null;
	}
	
   /* (non-Javadoc)
	* @see org.netbeans.modules.uml.ui.support.applicationmanager.IProxyUserInterface#newElementDialog(org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace)
	*/
	public IElement newElementDialog(INamespace pDefaultNamespace)
	{
//		INewDialog diag = new NewDialog();
//		diag.addTab(NewDialogTabKind.NWIK_NEW_ELEMENT);
		
		INewDialogElementDetails details = new NewDialogElementDetails();
		details.setNamespace(pDefaultNamespace);
		
//		diag.specifyDefaults(details); 
                
                //jyothi:                
                WizardDescriptor.Iterator iterator = new AddElementWizardIterator();
                ((AddElementWizardIterator)iterator).setDetails(details); //this is a hack to pass the details object
                WizardDescriptor wizardDescriptor = new WizardDescriptor(iterator);
                // {0} will be replaced by WizardDescriptor.Panel.getComponent().getName()
                // {1} will be replaced by WizardDescriptor.Iterator.name()
                wizardDescriptor.setTitleFormat(new MessageFormat("{0} ({1})"));                
                wizardDescriptor.setTitle(org.openide.util.NbBundle.getMessage(UMLUserInterface.class, "IDS_NEW_ELEMENT_WIZARD_TITLE"));
                Dialog dialog = DialogDisplayer.getDefault().createDialog(wizardDescriptor);
                dialog.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(UMLUserInterface.class, "IDS_NEW_ELEMENT_WIZARD_TITLE_DESCRIPTION"));
                dialog.setVisible(true);
                dialog.toFront();
                boolean cancelled = wizardDescriptor.getValue() != WizardDescriptor.FINISH_OPTION;
                if (!cancelled) {
                    // do something
                    Object obj = wizardDescriptor.getProperty(AddElementWizardIterator.ELEMENT_DETAILS);
                    if (obj instanceof INewDialogElementDetails ) {
                        INewDialogElementDetails elementResults = (INewDialogElementDetails)obj;
                        if (elementResults != null) {
                            INewDialogResultProcessor processor = new NewDialogResultProcessor();
                            return processor.handleResult(elementResults);
                        }
                    }                
                }
                return null;                
        }
	
   /* (non-Javadoc)
	* @see org.netbeans.modules.uml.ui.support.applicationmanager.IProxyUserInterface#closeWorkspace()
	*/
	public void closeWorkspace()
	{
		// TODO Auto-generated method stub
		
	}
	
   /* (non-Javadoc)
	* @see org.netbeans.modules.uml.ui.support.applicationmanager.IProxyUserInterface#closeProject(org.netbeans.modules.uml.core.metamodel.structure.IProject)
	*/
	public void closeProject(IProject pProject)
	{
		// TODO Auto-generated method stub
		
	}
	
   /* (non-Javadoc)
	* @see org.netbeans.modules.uml.ui.support.applicationmanager.IProxyUserInterface#setDisableContextMenu(boolean)
	*/
	public void setDisableContextMenu(boolean value)
	{
		// TODO Auto-generated method stub
		
	}
	
   /* (non-Javadoc)
	* @see org.netbeans.modules.uml.ui.support.applicationmanager.IProxyUserInterface#getDisableContextMenu()
	*/
	public boolean getDisableContextMenu()
	{
		// TODO Auto-generated method stub
		return false;
	}
	
	/**
	 * Uses NetBeans to display the URL.
	 */
	public void displayInBrowser(URL url)
	{
		HtmlBrowser.URLDisplayer displayer = HtmlBrowser.URLDisplayer.getDefault();
		displayer.showURL(url);
	}
	
	public Image getResource(String iconLocation)
	{
		Image retVal = null;
		
		try
		{
			retVal = Utilities.loadImage(iconLocation);
		}
		catch(Exception e)
		{
			URL url = this.getClass().getClassLoader().getResource(iconLocation);
			if(url != null)
			{
				retVal = Toolkit.getDefaultToolkit().createImage(url);
			}
		}
		
		return retVal;
	}
	
}
