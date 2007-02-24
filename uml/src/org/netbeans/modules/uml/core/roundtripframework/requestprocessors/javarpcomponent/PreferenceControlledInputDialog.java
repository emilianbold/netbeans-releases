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

/*
 * Created on Mar 5, 2004
 *
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.netbeans.modules.uml.core.roundtripframework.requestprocessors.javarpcomponent;

/**
 * @author avaneeshj
 *
 * Window - Preferences - Java - Code Generation - Code and Comments
 */

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.StringTokenizer;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.UIManager;

import org.netbeans.modules.uml.core.coreapplication.IPreferenceManager2;
import org.netbeans.modules.uml.core.support.umlsupport.Log;
import org.netbeans.modules.uml.ui.products.ad.applicationcore.IADProduct;
import org.netbeans.modules.uml.ui.support.ProductHelper;
import org.netbeans.modules.uml.ui.support.QuestionResponse;
import org.netbeans.modules.uml.ui.support.SimpleQuestionDialogKind;
import org.netbeans.modules.uml.ui.support.SimpleQuestionDialogResultKind;
import org.netbeans.modules.uml.ui.support.UserResultListener;
import org.netbeans.modules.uml.ui.support.applicationmanager.IProduct;
import org.netbeans.modules.uml.ui.support.commondialogs.MessageDialogKindEnum;
import org.netbeans.modules.uml.ui.support.commondialogs.MessageIconKindEnum;
import org.netbeans.modules.uml.ui.swing.commondialogs.JCenterDialog;


public class PreferenceControlledInputDialog extends JCenterDialog
{
	private 	JCheckBox m_Checkbox = null;
	private 	JTextField m_TextBox = null;
	private 	int m_DefaultButton = -1;
	private 	String textBoxText = null;
	JDialog 	retVal = null;
	protected 	IJavaChangeHandlerUtilities m_Utilities = null;
	 
	String 	m_PrefKey = null;
	String 	m_PrefName = null;
	String 	m_PrefPath = null;
	boolean 	m_AutoUpdatePreference = false;
	String m_sAffirmative = null;
	String m_sNegative = null;
	String m_sAsk = null;
	  
	public PreferenceControlledInputDialog()
	{
	}
	 
	private JDialog createDialog(String  		  message, 
		            			 String           title, 
								 Icon             icon, 
								 String           checkboxMsg,
								 int              dialogType,
								 QuestionResponse result)
	{
		IProduct  pProduct = ProductHelper.getProduct();
		IADProduct iADProduct = (IADProduct)pProduct;
		retVal = new JCenterDialog(iADProduct.getProxyUserInterface().getWindowHandle());
		
		retVal.setTitle(title);
		retVal.setModal(true);
		try
		{
			if(checkboxMsg != null && checkboxMsg.length() > 0)
			{
				JPanel messagePanel = new JPanel();
                messagePanel.setBorder(BorderFactory.createEmptyBorder(
                        0, //top
                        8, //left
                        0, //bottom
                        8) //right
                        );
				messagePanel.setLayout(new GridLayout(3,1));
                if (message.indexOf(")") > 0) 
			        	{	
                	String functionName = 
                        message.substring(0, message.indexOf(")")) + ")";
                	JLabel label = new JLabel(functionName);
			        			if(icon != null)
			        			label.setIcon(icon);
                    
                    label.setBorder(BorderFactory.createEmptyBorder(8, 8, 0, 8));
                    
			        			retVal.getContentPane().add(label,BorderLayout.NORTH);
                    messagePanel.add(new JLabel(message.substring(message.indexOf(")")+1,message.length())));
			    }

 			    if(textBoxText != null && textBoxText.length() > 0)
				{
					m_TextBox  = new JTextField(textBoxText);
					m_TextBox.setPreferredSize(new Dimension(10,1));
					messagePanel.add(m_TextBox);
				}
				m_Checkbox = new JCheckBox(checkboxMsg);
				messagePanel.add(m_Checkbox);
				retVal.getContentPane().add(messagePanel,BorderLayout.CENTER);
				retVal.setLocationRelativeTo(null);
				
			}
			else
			{
				retVal.getContentPane().add(new JLabel(message), BorderLayout.CENTER);
				if(icon != null)
				{
					retVal.getContentPane().add(new JLabel(icon), BorderLayout.WEST);
				}
			}
			addButtons(dialogType, result, retVal);
			
			m_TextBox.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(ActionEvent e) {
                  performFinalAction();
                }
              });
		}
		catch(Exception e)
		{
			Log.stackTrace(e);
		}
		return retVal;
	}
	 
	
	protected JButton createActionButton(String         displayName, 
	 									 String         command,
										 ActionListener listener)
	{
		JButton retVal = new JButton(RPMessages.determineText(displayName));
		RPMessages.setMnemonic(retVal, displayName);
	 	retVal.setActionCommand(command);

		WindowListener wndCloser = new WindowAdapter()
		{
			public void windowClosing(WindowEvent e)
			{
				return;
			}
		};
		this.retVal.addWindowListener(wndCloser);
	 	
	 	retVal.addActionListener(new ActionListener() 
	 	{
	 		public void actionPerformed(ActionEvent e) 
			{
	 			if(e.getActionCommand().equals("OK"))
	 			{	
	 				performFinalAction();
	 			}	
	 	    }
	 	}
	 	);
	 	return retVal;
	}
	 
	 public void displayFromStrings(String	sPreferenceKey,
									String	sPreferencePath,
									String	sPreferenceName,
                                    String	sAffirmative,
								    String	sNegative,
								    String	sAsk,
                                    String  sMessageString, 
						            int 	nDefaultResult,
                                    int 	nResult, 
                                    String  sTitle, 
                                    int		nDialogType, 
                                    int		nDialogIcon,
                                    int		parent )
	 {
	 	try
		{
	 		// Set the preference file information
	 		preferenceInformation(sPreferenceKey, sPreferencePath, 
	 				sPreferenceName, true);
	 		String bsPreferenceValue = getPreferenceValue();
	 		// Set the preference strings
	 		m_sAffirmative = sAffirmative;
	 		m_sNegative = sNegative;
	 		m_sAsk = sAsk;
//	 		if(dlg != null)
//	 		{
//	 			int userAnswer = 
//	 				DisplaySimpleDialog(sMessageString, nDefaultResult,
//                                        sTitle, nDialogType, 
//                                        nDialogIcon, bsPreferenceValue,
//                                        dlg);   

	 			int userAnswer = 
      			displaySimpleQuestionDialogWithCheckbox
							(nDialogType, nDialogIcon, sMessageString, 
							RPMessages.getString("IDS_NEVER_SHOW"), 
							sTitle, nDefaultResult, false, bsPreferenceValue);
      			
	 			String bsPrefValue = null;
	 			if((SimpleQuestionDialogKind.SQDK_YESNOALWAYS == nDialogType) || 
	 				(SimpleQuestionDialogKind.SQDK_YESNONEVER == nDialogType))
	 			{
	 				bsPrefValue = getAlwaysNeverResult(nDialogType, userAnswer, 
	 													nResult);
	 			}
	 			else
	 			{
	 				bsPrefValue = getDefaultResult(userAnswer, nResult, 
	 						m_Checkbox.isSelected());
	 			}
      
	 			// Because we do not want to screw up any bodies preference 
	 			// by putting a blank 
	 			// into the value, do not set if blank.
	 			if(( bsPrefValue != null) &&  bsPrefValue.length() > 0)
	 			{
	 				setPreferenceValue( bsPrefValue );
	 			}
//	 		}	
		}
	 	catch(Exception e)
		{
	 		// I just want to forward the error to the listener.
	 		Log.stackTrace(e);
		}
	}
	 
	public int displaySimpleQuestionDialogWithCheckbox(
										int 		nDialogType, 
										int 		dialogIcon, 
										String 		message, 
										String 		checkboxMsg, 
										String 		title, 
										int 		nDefaultResult, 
										boolean 	defaultIsChecked,
										String 		bsPreferenceValue)
	{
		QuestionResponse retVall = new QuestionResponse(defaultIsChecked,
							nDefaultResult);
		int retVal = nDefaultResult;

   		boolean bIsYesNo =((SimpleQuestionDialogKind.SQDK_YESNO == nDialogType) 
				||(SimpleQuestionDialogKind.SQDK_YESNOCANCEL == nDialogType));

    	if( bsPreferenceValue.equals(m_sAffirmative ))
   		{
      		if(nDialogType == SimpleQuestionDialogKind.SQDK_YESNOALWAYS)
      		{
         		retVal = SimpleQuestionDialogResultKind.SQDRK_RESULT_ALWAYS;
      		}
      		else
      		{
         		retVal = bIsYesNo ? SimpleQuestionDialogResultKind.SQDRK_RESULT_YES 
         				: SimpleQuestionDialogResultKind.SQDRK_RESULT_OK;
      		}
   		}
   		else if( bsPreferenceValue.equals(m_sNegative))
   		{
    		if(nDialogType == SimpleQuestionDialogKind.SQDK_YESNONEVER)
      		{
        		retVal = SimpleQuestionDialogResultKind.SQDRK_RESULT_NEVER;
      		}
      		else
      		{
        		retVal = bIsYesNo ? SimpleQuestionDialogResultKind.SQDRK_RESULT_NO 
        				: SimpleQuestionDialogResultKind.SQDRK_RESULT_CANCEL;
      		}
   		}
   		else
   		{
	 		if((message != null) && (message.length() > 0))
	 		{
	 			if(isRunSilent() == true)
	 			{
	 				retVal = nDefaultResult;
	 			}
		 		else
		 		{
		 			// With Swing if you use NULL as the parent it will use the active
		 			// window.
		 			Icon icon = getIconForType(dialogIcon);
		 			JDialog dialog = createDialog(message, "", icon, checkboxMsg, 
		 						nDialogType, retVall);
		 			if(dialog != null)
		 			{
		 				dialog.pack();
		 				Insets insets = dialog.getInsets();
		 				insets.top = 5;
		 				insets.left = 5;
		 				insets.bottom = 5;
		 				insets.right = 5;
		 				dialog.setTitle(title);
		 				dialog.setModal(true);
		 				dialog.setVisible(true);
		 				if(m_Checkbox != null)
		 				{
		 					retVall.setChecked(m_Checkbox.isSelected());
		 				}
		 			}
		 		}
		}
	}	
	 	return retVal;
	}
 /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.ISilentDialog#isRunSilent()
    */
   public boolean isRunSilent()
   {
	    return ProductHelper.getMessenger().getDisableMessaging();
   }
   
   //**************************************************
   // Helper Methods
   //**************************************************

   protected Icon getIconForType(int messageType) 
   {
      Icon retVal = null;
      
      if(messageType >= 0 || messageType <= 7)
      {
         switch(messageType) 
         {
            case MessageIconKindEnum.EDIK_ICONHAND:
            case MessageIconKindEnum.EDIK_ICONSTOP:
            case MessageIconKindEnum.EDIK_ICONERROR:
               retVal = UIManager.getIcon("OptionPane.errorIcon");
                break;
            case MessageIconKindEnum.EDIK_ICONINFORMATION:
               retVal = UIManager.getIcon("OptionPane.informationIcon");
               break;
            case MessageIconKindEnum.EDIK_ICONEXCLAMATION:
            case MessageIconKindEnum.EDIK_ICONWARNING:
            case MessageIconKindEnum.EDIK_ICONASTERISK:
                retVal = UIManager.getIcon("OptionPane.warningIcon");
                break;
            case MessageIconKindEnum.EDIK_ICONQUESTION:
                retVal = UIManager.getIcon("OptionPane.questionIcon");
                break;
         }
      }
      return retVal;
    }

	/**
	 * The default button (ie IDOK).  See the return values for AfxMessageBox.
	 */
	public void setDefaultButton(int nButton)
	{
		m_DefaultButton = nButton;
	}
	
	public void setEditText( String value )
	{
		textBoxText = value;
	}

	/**
	 * Sets / Gets the text in the edit control.
	*/
	public String getEditText()
	{
		return textBoxText;
	}
		
	private void performFinalAction()
	{
		setEditText(m_TextBox.getText().trim());
		this.retVal.dispose();
	}
		
	public void setPreferenceValue( String sVal )
	{
		try
		{
			boolean autoUpdate =  getAutoUpdatePreference ();
			if ( autoUpdate )
		    {
		    	// Get the preference manager
		        IProduct  pProduct = ProductHelper.getProduct();
				if(pProduct != null)
				{
		        	IPreferenceManager2 pManager = 
		        		pProduct.getPreferenceManager();
		        	
		            if(pManager != null)
		            {
		               	pManager.setPreferenceValue( m_PrefKey, m_PrefPath, 
		               									m_PrefName, sVal);
		            }
				}
			}
		}
		catch( Exception e )
		{
			Log.stackTrace(e);
		}
	}
	
	public String getPreferenceValue()
	{
		String prefValue = null;
		try
		{
	      // Get the preference manager
			IProduct  pProduct = ProductHelper.getProduct();
			if(pProduct != null)
			{
				IPreferenceManager2  pManager = pProduct.getPreferenceManager();
				if(pManager != null)
				{
					try
					{
						prefValue = pManager.getPreferenceValue( m_PrefKey, 
												m_PrefPath, m_PrefName);
						// We can now overwrite the value passed in, 
						// thereby overwriting a "default" value.
						// That is why we have a second try/catch here.
					}
					catch( Exception e)
					{
						Log.stackTrace(e);
					}
				}
			}
		}
		catch( Exception e )
		{
			Log.stackTrace(e);
		}
		return prefValue;
	}
		
	public void preferenceInformation ( String sKey, 
										String sPath,
										String sName,
										boolean bAutoUpdatePreference )
	{
		try
		{
			setPrefKey( sKey);
			setPrefPath( sPath);
			setPrefName( sName);
			setAutoUpdatePreference( bAutoUpdatePreference );
		}
		catch( Exception e)
		{
			Log.stackTrace(e);
		}
	}
	
	public void  setAutoUpdatePreference ( boolean bVal )
	{
	   m_AutoUpdatePreference = bVal;
	}

	/**
	 *
	 * Gets whether the preference file should be updated when the Preference Value
	 * is set.
	 *
	 * @param bVal[out]
	 *
	 * @return 
	 *
	 */

	public boolean getAutoUpdatePreference ()
	{
	   return m_AutoUpdatePreference;
	}

	/**
	 *
	 * Set the preference key.  If no key is specified, Default is assumed.
	 *
	 * @param sVal[in]
	 *
	 * @return 
	 *
	 */

	public void setPrefKey (String sVal)
	{
	   m_PrefKey = sVal;
	}

	/**
	 *
	 * Gets the preference key.  If no key is specified, Default is assumed.
	 *
	 * @param sVal[out]
	 *
	 * @return 
	 *
	 */

	public String getPrefKey( String sVal )
	{
	   return m_PrefName;
	}

	/**
	 *
	 * Set the preference path.  The path is the part between the key
	 * and the name.
	 *
	 * @param sVal[in]
	 *
	 * @return 
	 *
	 */

	public void setPrefPath( String sVal )
	{
	   m_PrefPath = sVal;
	}

	/**
	 *
	 * Get the preference path.  The path is the part between the key
	 * and the name.
	 *
	 * @param sVal[out]
	 *
	 * @return 
	 *
	 */

	public String getPrefPath ( String sVal )
	{
	   return m_PrefName;
	}
	/**
	 *
	 * Set the preference name.
	 *
	 * @param sVal[in]
	 *
	 * @return 
	 *
	 */

	public void setPrefName( String sVal )
	{
	   m_PrefName = sVal;
	}
	/**
	 *
	 * Get the preference name.
	 *
	 * @param sVal[out]
	 *
	 * @return 
	 *
	 */

	public String getPrefName()
	{
	   return m_PrefName;
	}
	
	/**
	 * Retrieves the questions answer and preference value for Always and Never dialogs.
	 * When the user selects <I>Always</I> nResult will be set to SQDRK_RESULT_YES and
	 * the preference will be m_sAffirmative.  When the user selects <I>Never</> nResult
	 * will be set to SQDRK_RESULT_NO and the preference will be m_sNegative.
	 *
	 * @param nDialogType [in] The type of dialog to display.
	 * @param userAnswer [in] The answer that the user choose.
	 * @param nResult [in] The final result.
	 * @param prefValue [out] The preference value that represent the users answer.
	 */
	public String getAlwaysNeverResult(int  nDialogType, int  userAnswer,
	                                  int nResult)
	{     
		String prefValue = null;
		nResult = userAnswer;
	   	if(SimpleQuestionDialogResultKind.SQDRK_RESULT_ALWAYS == userAnswer)
	   	{
	    	nResult  = SimpleQuestionDialogResultKind.SQDRK_RESULT_YES;
	      	prefValue = m_sAffirmative;
	   	}
	   	else if(SimpleQuestionDialogResultKind.SQDRK_RESULT_NEVER == userAnswer)
	   	{
	    	nResult  = SimpleQuestionDialogResultKind.SQDRK_RESULT_NO;
	      	prefValue = m_sNegative;
	   	}
	   	else
	   	{
	    	prefValue = m_sAsk;
	  	}
	   	return prefValue;
	}
	
	/**
	 * Retrieves the questions answer and preference value for Always and Never dialogs.
	 * When the user selects <I>Yes</I> and checks the checkbox nResult will be set to 
	 * SQDRK_RESULT_YES or SQDRK_RESULT_OK and the preference will be m_sAffirmative.  
	 * When the user selects <I>No</> of <I>Cancel</> nResult will be set to 
	 * SQDRK_RESULT_NO or SQDRK_RESULT_CANCEL and the preference will be m_sNegative.
	 *
	 * @param userAnswer [in] The answer that the user choose.
	 * @param nResult [in] The final result.
	 * @param isChecked [in] True if the user selected the check box, false otherwise.
	 * @param prefValue [out] The preference value that represent the users answer.
	 */
	public String getDefaultResult(int  userAnswer, int nResult, 
									boolean isChecked)
	{    
	   String prefValue = null;
	   nResult = userAnswer;
	   if( userAnswer != SimpleQuestionDialogResultKind.SQDRK_RESULT_CANCEL )
	   {
	      // Update the preference
	      if( isChecked == true )
	      {
	         prefValue = 
	         	((SimpleQuestionDialogResultKind.SQDRK_RESULT_YES == userAnswer) 
			 	|| (SimpleQuestionDialogResultKind.SQDRK_RESULT_OK == userAnswer))
	                      ? m_sAffirmative : m_sNegative;         
	      }
	      else
	      {
	         prefValue = m_sAsk;
	      }
	   }
	   return prefValue;
	}
	
	protected void addButtons(int dialogType,QuestionResponse result,
									JDialog retVal)
	{
		JPanel buttonPanel = new JPanel();
		retVal.getContentPane().add(buttonPanel, BorderLayout.SOUTH);
		
		ActionListener resultListener = new UserResultListener(result, retVal);
		switch(dialogType)
		{
			case MessageDialogKindEnum.SQDK_OK:
			{
				buttonPanel.add(createActionButton(RPMessages.getString("IDS_OK"), "OK", resultListener));
				break;
			}
			case MessageDialogKindEnum.SQDK_ABORTRETRYIGNORE:
			{
				buttonPanel.add(createActionButton(RPMessages.getString("IDS_ABORT"), "ABORT", resultListener));
				buttonPanel.add(createActionButton(RPMessages.getString("IDS_RETRY"), "RETRY", resultListener));
				buttonPanel.add(createActionButton(RPMessages.getString("IDS_IGNORE"), "IGNORE", resultListener));
				break;
			}
			case MessageDialogKindEnum.SQDK_OKCANCEL:
			{
				buttonPanel.add(createActionButton(RPMessages.getString("IDS_OK"), "OK", resultListener));
				buttonPanel.add(createActionButton(RPMessages.getString("IDS_CANCEL"), "CANCEL", resultListener));
				break;
			}
			case MessageDialogKindEnum.SQDK_RETRYCANCEL:
			{
				buttonPanel.add(createActionButton(RPMessages.getString("IDS_RETRY"), "RETRY", resultListener));
				buttonPanel.add(createActionButton(RPMessages.getString("IDS_CANCEL"), "CANCEL", resultListener));
				break;
			}
			case MessageDialogKindEnum.SQDK_YESNO:
			{
				buttonPanel.add(createActionButton(RPMessages.getString("IDS_YES"), "YES", resultListener));
				buttonPanel.add(createActionButton(RPMessages.getString("IDS_NO"), "NO", resultListener));
				break;
			}
			case MessageDialogKindEnum.SQDK_YESNOCANCEL:
			{
				buttonPanel.add(createActionButton(RPMessages.getString("IDS_YES"), "YES", resultListener));
				buttonPanel.add(createActionButton(RPMessages.getString("IDS_NO"), "NO", resultListener));
				buttonPanel.add(createActionButton(RPMessages.getString("IDS_CANCEL"), "CANCEL", resultListener));
				break;
			}
			case MessageDialogKindEnum.SQDK_YESNOALWAYS:
			{
				buttonPanel.add(createActionButton(RPMessages.getString("IDS_YES"), "YES", resultListener));
				buttonPanel.add(createActionButton(RPMessages.getString("IDS_NO"), "NO", resultListener));
				buttonPanel.add(createActionButton(RPMessages.getString("IDS_ALWAYS"), "ALWAYS", resultListener));
				break;
			}
			case MessageDialogKindEnum.SQDK_YESNONEVER:
			{
				buttonPanel.add(createActionButton(RPMessages.getString("IDS_YES"), "YES", resultListener));
				buttonPanel.add(createActionButton(RPMessages.getString("IDS_NO"), "NO", resultListener));
				buttonPanel.add(createActionButton(RPMessages.getString("IDS_NEVER"), "NEVER", resultListener));
				break;
			}
		}
	}  
}



