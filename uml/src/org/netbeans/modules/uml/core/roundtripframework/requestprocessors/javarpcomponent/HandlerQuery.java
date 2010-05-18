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
 * File       : HandlerQuery.java
 * Created on : Oct 29, 2003
 * Author     : Aztec
 */
package org.netbeans.modules.uml.core.roundtripframework.requestprocessors.javarpcomponent;

import java.text.MessageFormat;

import org.netbeans.modules.uml.ui.support.QuestionResponse;
import org.netbeans.modules.uml.ui.support.SimpleQuestionDialogKind;
import org.netbeans.modules.uml.ui.support.SimpleQuestionDialogResultKind;
import org.netbeans.modules.uml.ui.support.commondialogs.IQuestionDialog;
import org.netbeans.modules.uml.ui.swing.commondialogs.SwingQuestionDialogImpl;

/**
 * @author Aztec
 */
public class HandlerQuery implements IHandlerQuery
{

    String m_Key;
    String m_Title;
    String m_Text;

    boolean m_Silent;
    boolean m_QueryOnce;
    boolean m_SingleQueried;
    boolean m_QueryAnswer;
    boolean m_DefaultAnswer;
    boolean m_Persist;
    int m_IconType;
    
	public HandlerQuery( String queryKey, int textID, int titleID, 
									boolean defaultAnswer, int iconType, boolean silent,
									boolean persist)
	{
		m_Key = queryKey;
		
		//the next two assignments need the corresponding string for the textID and titleID
		//Uncomment once the values are ready 
//		m_Title = titleID;
//		m_Text = textID;

		m_QueryOnce = true;
		m_Persist = persist;		

		setSilent ( silent );
		setDefaultAnswer ( defaultAnswer );
		setSingleQueried ( false );
		setIconType ( iconType );
		setSingleQueryAnswer ( defaultAnswer );								
	}
    
	public HandlerQuery( String queryKey, String text, String title, 
									boolean defaultAnswer, int iconType, boolean silent,
									boolean persist)
	{
		m_Key = queryKey;		
		m_Title = title;
		m_Text = text;
		m_QueryOnce = true;
		m_Persist = persist;		

		setSilent ( silent );
		setDefaultAnswer ( defaultAnswer );
		setSingleQueried ( false );
		setIconType ( iconType );
		setSingleQueryAnswer ( defaultAnswer );								
	}
	
	public HandlerQuery(HandlerQuery copy)
	{
	   m_Key   = copy.m_Key;
	   m_Title = copy.m_Title;
	   m_Text  = copy.m_Text;

	   m_Silent        = copy.m_Silent;
	   m_QueryOnce     = copy.m_QueryOnce;
	   m_SingleQueried = copy.m_SingleQueried;
	   m_QueryAnswer   = copy.m_QueryAnswer;
	   m_DefaultAnswer = copy.m_DefaultAnswer;
	   m_IconType      = copy.m_IconType;
	   m_Persist       = copy.m_Persist;
	}
    
	public HandlerQuery(String queryKey, HandlerQuery copy)
	{
	   m_Key   = queryKey;
	   m_Title = copy.m_Title;
	   m_Text  = copy.m_Text;

	   m_Silent        = copy.m_Silent;
	   m_QueryOnce     = copy.m_QueryOnce;
	   m_SingleQueried = copy.m_SingleQueried;
	   m_QueryAnswer   = copy.m_QueryAnswer;
	   m_DefaultAnswer = copy.m_DefaultAnswer;
	   m_IconType      = copy.m_IconType;
	   m_Persist       = copy.m_Persist;
	}

	/**
	 *
	 * A query can be displayed with up to 4 arguments that are substituted for
	 * the substrings "%1", "%2", "%3", "%4" in the text
	 *
	 * @param parent[in]
	 * @param arg1[in]
	 * @param arg2[in]
	 * @param arg3[in]
	 * @param arg4[in]
	 */
    public boolean doQuery(String arg1, String arg2, String arg3, String arg4)
    {
		String finalText = getParameterizedText (arg1, arg2, arg3, arg4);
		return displayQuery (finalText, m_Title);
    }

	/**
	 *
	 * A query can be displayed with up to 4 arguments that are substituted for
	 * the substrings "%1", "%2", "%3", "%4" in the text
	 *
	 * @param parent[in]
	 * @param arg1[in]
	 * @param arg2[in]
	 * @param arg3[in]
	 */
    public boolean doQuery(String arg1, String arg2, String arg3)
    {
		String finalText = getParameterizedText (arg1, arg2, arg3);
		return displayQuery (finalText, m_Title);
    }

	/**
	 *
	 * A query can be displayed with up to 4 arguments that are substituted for
	 * the substrings "%1", "%2", "%3", "%4" in the text
	 *
	 * @param parent[in]
	 * @param arg1[in]
	 * @param arg2[in]
	 */
    public boolean doQuery(String arg1, String arg2)
    {
		String finalText = getParameterizedText (arg1, arg2);
		return displayQuery (finalText, m_Title);
    }

	/**
	 *
	 * A query can be displayed with up to 4 arguments that are substituted for
	 * the substrings "%1", "%2", "%3", "%4" in the text
	 *
	 * @param arg1[in]
	 */
    public boolean doQuery(String arg1)
    {
		String finalText = getParameterizedText (arg1);
		return displayQuery (finalText, m_Title);
    }

	/**
	 *
	 * A query can be displayed with up to 4 arguments that are substituted for
	 * the substrings "%1", "%2", "%3", "%4" in the text
	 *
	 * @param parent[in]
	 */
    public boolean doQuery()
    {
		return displayQuery(m_Text, m_Title);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.requestprocessors.javarpccomponent.IHandlerQuery#getKey()
     */		
    public String getKey()
    {
		return m_Key;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.requestprocessors.javarpccomponent.IHandlerQuery#getSilent()
     */
    public boolean getSilent()
    {
		return m_Silent;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.requestprocessors.javarpccomponent.IHandlerQuery#persist()
     */
    public boolean getPersist()
    {
		return m_Persist;
    }

	/**
	 *
	 * Reset allows us to reset an existing query and "reuse" it
	 * without having to reconstruct it.
	 */
    public void reset()
    {
		setSingleQueried ( false );
		setSingleQueryAnswer ( getDefaultAnswer() );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.requestprocessors.javarpccomponent.IHandlerQuery#setSilent(boolean)
     */
    public void setSilent(boolean silent)
    {
		m_Silent = silent;
    }
    
    /**
     *
     * @param arg1[in]
     * @param arg2[in]
     * @param arg3[in]
     * @param arg4[in]
     *
     * @return 
     *
     */

    protected String getParameterizedText (String arg1,
                                        String arg2,
                                        String arg3, 
                                        String arg4 )
    {
        String finalText = m_Text;

        if (finalText.indexOf("%1") != -1)
        {
        finalText = finalText.replaceAll("%1", arg1);
        finalText = finalText.replaceAll("%2", arg2);
        finalText = finalText.replaceAll("%3", arg3);
        finalText = finalText.replaceAll("%4", arg4);
        }
        else
        {
            finalText = MessageFormat.format(finalText, 
                                new Object[] { arg1, arg2, arg3, arg4 });
        }
        return finalText;
    }
    
    /**
     *
     * @param arg1[in]
     * @param arg2[in]
     * @param arg3[in]
     *
     * @return 
     *
     */

    protected String getParameterizedText (String arg1,
                                        String arg2,
                                        String arg3)
    {
        String finalText = m_Text;

        if (finalText.indexOf("%1") != -1)
        {
        finalText = finalText.replaceAll("%1", arg1);
        finalText = finalText.replaceAll("%2", arg2);
        finalText = finalText.replaceAll("%3", arg3);
        }
        else
        {
            finalText = MessageFormat.format(finalText, 
                                new Object[] { arg1, arg2, arg3 });
        }
        return finalText;
    }
    
    /**
     * 
     * @param arg1[in]
     * @param arg2[in]
     *
     * @return 
     *
     */

    protected String getParameterizedText (String arg1,
                                        String arg2)
    {
        String finalText = m_Text;

        if (finalText.indexOf("%1") != -1)
        {
        finalText = finalText.replaceAll("%1", arg1);
        finalText = finalText.replaceAll("%2", arg2);
        }
        else
        {
            finalText = MessageFormat.format(finalText, 
                                new Object[] { arg1, arg2 });
        }
        return finalText;
    }
    
    /**
     *
     * @param arg1[in]
     *
     * @return 
     *
     */

    protected String getParameterizedText (String arg1)
    {
        String finalText = m_Text;

        if (finalText.indexOf("%1") != -1)
        finalText = finalText.replaceAll("%1", arg1);
        else
            finalText = MessageFormat.format(finalText, 
                                new Object[] { arg1 });
        return finalText;
    }
    
    protected boolean displayQuery(String text, String title)
    {
        boolean doQuery = true;
        // This routine knows how to interpret the silent and query once flags

        if (getSilent())
        {
            setSingleQueryAnswer(getDefaultAnswer());
            doQuery = false;
        }
        else
        {
            // Ok, we are allowed to query only once. Have we queried once?
            if (getSingleQueried())
            {
                doQuery = false;
            }
        }

        if (doQuery)
        {
            setAnswer(displayDialog(text, title));
        }

        return getSingleQueryAnswer();
    }
    
    public void setSingleQueryAnswer(boolean queryAns)
    {
        m_QueryAnswer = queryAns;
    }
    
    public boolean getSingleQueryAnswer()
    {
        return m_QueryAnswer;
    }
    
    public void setDefaultAnswer(boolean ans)
    {
        m_DefaultAnswer = ans;
    }
    
    public boolean getDefaultAnswer()
    {
        return m_DefaultAnswer;
    } 
    
    public void setSingleQueried(boolean singleQueried)
    {
        m_SingleQueried = singleQueried;
    }
    
    public boolean getSingleQueried()
    {
        return m_SingleQueried;
    }
    
    public void setIconType(int iconType)
    {
        m_IconType = iconType;
    }
    
    public int getIconType()
    {
        return m_IconType;
    }     
    
    public int displayDialog(String text, String title)
    {
    	// Aztec: TODO: Dialogs! Re-visit when implemented.
        int dlgAnswer = SimpleQuestionDialogResultKind.SQDRK_RESULT_YES;

		// Aztec: TODO: Verify if this is the correct dialog to use.
		IQuestionDialog dlg = new SwingQuestionDialogImpl();        
        
        int dType = SimpleQuestionDialogKind.SQDK_YESNO;
        int defaultDlgAnswer = SimpleQuestionDialogResultKind.SQDRK_RESULT_NO;
        
        if(getDefaultAnswer())
        {
            defaultDlgAnswer = SimpleQuestionDialogResultKind.SQDRK_RESULT_YES;
        }
		QuestionResponse dlgReply = null;		
		dlgReply = dlg.displaySimpleQuestionDialogWithCheckbox( dType, 
                                           getIconType(), 
                                           text, 
                                           null,
										   title,
                                           defaultDlgAnswer,                                           
                                           false);
 
 		if (dlgReply != null)
			dlgAnswer = dlgReply.getResult();
			 
        return dlgAnswer;
    }
    
    protected boolean setAnswer(int ans)
    {
        setSingleQueried(true);
        if (ans == SimpleQuestionDialogResultKind.SQDRK_RESULT_YES )
        {
            setSingleQueryAnswer (true);
        }
        else 
        {
            setSingleQueryAnswer (false);
        }
        return getSingleQueryAnswer();
    }
    
    protected String getText()
    {
		return m_Text;
    }
    
	protected String getTitle()
	{
		return m_Title;
	}
	
	
}
