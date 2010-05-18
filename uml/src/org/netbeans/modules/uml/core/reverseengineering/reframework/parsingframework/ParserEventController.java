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
 * File       : ParseEventController.java
 * Created on : Oct 27, 2003
 * Author     : Aztec
 */
package org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework;

import java.util.Stack;

import org.netbeans.modules.uml.common.ETSystem;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

import antlr.CommonAST;
import antlr.CommonASTWithHiddenTokens;
import antlr.collections.AST;
import antlr.CommonASTWithLocationsAndHidden;

/**
 * @author Aztec
 */
public class ParserEventController implements IParserEventController
{
    private IStateListener   m_StateListener;
    private ITokenProcessor  m_TokenProcessor;
    private IStateFilter     m_StateFilter;
    private ITokenFilter     m_TokenFilter;
    private IErrorListener   m_ErrorListener;

    private String           m_Filename;
    private String           m_LanguageName;
    private CommentGather    m_CommentGather;

    //private Stack< String > stringStack;
    private Stack <StateInformation> mStateStack = new Stack<StateInformation>();
    private ETList <TokenInformation> mGuessingTokens = new ETArrayList <TokenInformation>();

    public String GUESSING_STATE = null;

    public ParserEventController()
    {
        GUESSING_STATE = "Guessing";
        m_CommentGather = null;
    }

    public ParserEventController(CommentGather pGather, String langName)
    {
        GUESSING_STATE = "Guessing";
        m_CommentGather = pGather;
        setLanguageName(langName);
    }

    public ParserEventController(String filename,
                          CommentGather pGather,
                          String langName)
    {
        GUESSING_STATE = "Guessing";
        m_CommentGather = pGather;
        setLanguageName(langName);
        setFilename(filename);
    }

    public ParserEventController(ParserEventController rhs,
                          CommentGather pGather,
                          String langName)
    {
        copy(rhs);
        GUESSING_STATE = "Guessing";
        m_CommentGather = pGather;
        setLanguageName(langName);
    }

    /**
     * Clear the internal information and prepare for the next parser instance.
     */
    public void clear()
    {
       mStateStack.clear();
       mGuessingTokens.clear();
       
       m_StateListener = null;
       m_TokenProcessor = null;
       m_StateFilter = null;
       m_TokenFilter = null;
       m_ErrorListener = null;
       
       m_Filename = "";
       m_CommentGather = null;
    }
    
    /**
     * Sends an error event to the registered error listener.
     *
     * @param msg [in]      The error message.
     * @param line [in]     The line number that contains the error.
     *                      If the error was not a parser error the line
     *                      number should be -1;
     * @param column [in]   The column number that contains the error.
     *                      If the error was not a parser error the line
     *                      number should be -1;
     * @param filename [in] The name of the file that was being parsed.
     */
    public void errorFound(String msg, int line, int column, String filename)
    {
        // I am not going to wrap the calls with _VH because what should I do if an error
        // occured.  If I failed to create or initialize the error there is nothing to do.
        if(m_ErrorListener != null)
        {
            IErrorEvent pError = new ErrorEvent();
           
            pError.setErrorMessage(msg);
            pError.setLineNumber(line);
            pError.setColumnNumber(column);
            pError.setFilename(filename);

            m_ErrorListener.onError(pError);
        }
    }

    /**
     * Get the the interface that will recieve the error information 
     * will parsing the file.
     * 
     * @param pVal [out] The error listener.
     */
    public IErrorListener getErrorListener()
    {
        return m_ErrorListener;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.IParseEventController#getFilename()
     */
    public String getFilename()
    {
        return m_Filename;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.IParseEventController#getLanguageName()
     */
    public String getLanguageName()
    {
        return m_LanguageName;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.IParseEventController#getStateFilter()
     */
    public IStateFilter getStateFilter()
    {
        return m_StateFilter;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.IParseEventController#getStateListener()
     */
    public IStateListener getStateListener()
    {
        return m_StateListener;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.IParseEventController#getTokenFilter()
     */
    public ITokenFilter getTokenFilter()
    {
        return m_TokenFilter;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.IParseEventController#getTokenProcessor()
     */
    public ITokenProcessor getTokenProcessor()
    {
        return m_TokenProcessor;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.IParseEventController#setErrorListener(org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.IErrorListener)
     */
    public void setErrorListener(IErrorListener errorListener)
    {
        m_ErrorListener = errorListener;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.IParseEventController#setFilename(java.lang.String)
     */
    public void setFilename(String filename)
    {
        m_Filename = filename;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.IParseEventController#setLanguageName(java.lang.String)
     */
    public void setLanguageName(String name)
    {
        m_LanguageName = name;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.IParseEventController#setStateFilter(org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.IStateFilter)
     */
    public void setStateFilter(IStateFilter stateFilter)
    {
        m_StateFilter = stateFilter;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.IParseEventController#setStateListener(org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.IStateListener)
     */
    public void setStateListener(IStateListener stateListener)
    {
        m_StateListener = stateListener;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.IParseEventController#setTokenFilter(org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ITokenFilter)
     */
    public void setTokenFilter(ITokenFilter tokenFilter)
    {
        m_TokenFilter = tokenFilter;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.IParseEventController#setTokenProcessor(org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ITokenProcessor)
     */
    public void setTokenProcessor(ITokenProcessor tokenProcessor)
    {
        m_TokenProcessor = tokenProcessor;
    }

    /**
     * Performs the processing of entering a new state in the parser.  If the 
     *  new state is <I>Guessing</I> then all discovered tokens will be 
     * queued until a concreate state is discovered.  
     * <p>
     * The specified token will be the first token to be added sent to the 
     * token processors before any other tokens.  Even if tokens where found
     * while in a guessing state the specified token will be sent first.
     */
    public void stateBegin(String stateName, AST tok, String type)
    {
        enteringState(stateName, false);
        tokenFound(tok, type);

        // Since we have started a new state.  I want to fire all the tokens
        // that have been discovered while in a guessing state.
        fireGuessingTokens();
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.IParseEventController#stateBegin(java.lang.String)
     */
    public void stateBegin(String stateName)
    {
        enteringState(stateName, true);
    }

    /**
     * Performs the processing of exiting an existing state in the parser.  
     * <p>
     * As new states are discovered the new state will be pushed onto a 
     * stack of states.  The top state will be the current state.  When 
     * the parser exits a state the top state will be removed from the
     * stack and the top state will again be the top state.
     * <I>Example:</I>
     * Begin Class State
     *    Begin Attribute State
     *       < Some Tokens Discovered >
     *    End Attribute State
     * End Class State
     * <p>
     * While discovering the class details the parser also entered and exited
     * the attribute discovery state.  While being in the attribute discovery 
     * state the parser was also in the class discovery state.
     *
     * @param stateName [in] The name the new state.
     */
    public void stateEnd()
    {
        if(mStateStack.size() > 0)
        {    
            StateInformation oldState = mStateStack.peek();
            mStateStack.pop();
                    
            String temp = oldState.stateName;

            if(!GUESSING_STATE.equals(temp))
            {
               fireEndState(oldState);
            }
            else
            {
                // If we found tokens will in the GUESSING_STATE state but never found
                // found a new state means that we had already found the correct state
                // for the tokens.
                fireGuessingTokens();
            }
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.IParseEventController#tokenFound(antlr.collections.AST, java.lang.String)
     */
    public void tokenFound(AST tok, String type)
    {
        if (mStateStack.size() > 0)
        {
            StateInformation curState = mStateStack.peek();
            
            // If the current state is blocked I do not want to do any thing 
            // with the token.  Basically the listener is not interested in the 
            // token so do not even queue up the token.
            if (!curState.blocked)
            {
                if (GUESSING_STATE.equals(curState.stateName))
                {
                    TokenInformation info = new TokenInformation();
                    info.type = type;
                    info.token = tok;
                    mGuessingTokens.add(info);
                }
                else
                {
                    // Go through with the send token logic.  Check if the 
                    // state is being monitored.  Then check if the token
                    // is being monitored.  If both test are passed then
                    // send the token to the listener.
                    fireToken(type, tok);
                }
            }
        }
    }

    public void copy(ParserEventController rhs)
    {
        if(this != rhs)
        {
            m_StateListener = rhs.m_StateListener;
            m_TokenProcessor = rhs.m_TokenProcessor;
            m_StateFilter   = rhs.m_StateFilter;
            m_TokenFilter   = rhs.m_TokenFilter;
            mStateStack     = rhs.mStateStack;
            mGuessingTokens = rhs.mGuessingTokens;
            m_ErrorListener = rhs.m_ErrorListener;
            m_Filename      = rhs.m_Filename;
            m_CommentGather = rhs.m_CommentGather;
            m_LanguageName  = rhs.m_LanguageName;
        }
    }

    private ITokenDescriptor addComment(AST pAST,ITokenDescriptor pDesc)
    {
        if(m_CommentGather != null)
            return m_CommentGather.gather(pAST, pDesc);
        return pDesc;
    }

    private ITokenDescriptor addFilename(ITokenDescriptor pDesc)
    {
        String filename = getFilename();
        if(filename != null)
        {
          pDesc.addProperty("Filename", filename);
        }
        return pDesc;
    }

    private void enteringState(String stateName, boolean flushGuessing)
    {
        StateInformation pInfo = new StateInformation();
        if(stateName != null)
        {
            pInfo.stateName = stateName;
            pInfo.blocked = false;

            // If current state is block then the new state is also blocked.
            if(mStateStack.size() > 0)
            {
                StateInformation curState = mStateStack.peek();
                pInfo.blocked = curState.blocked;
            }
            fireBeginState(pInfo, flushGuessing);

        }
        else if(pInfo != null)
        {
            pInfo.blocked = true;
        }

        if(pInfo != null)
        {
            mStateStack.push(pInfo);
        }
    }

    /**
     * Fires the begin state event to all registered listeners.  All token events that
     * was discovered while in a <I>Guessing</I> state will also be fired.  If the
     * state is being blocked by the state filter then the event will not be sent and
     * all the guessing tokens will be removed.
     *
     * @param stateName [in] The current state.
     * @param flushGuessing [in] True - sends guessing tokens to the processor, false does
     *                           not send guessing tokens to the processor
     */
    private void fireBeginState(StateInformation pInfo, boolean flushGuessing)
    {
        if(pInfo != null)
        {
            pInfo.blocked = isStateFiltered(pInfo.stateName);
            if(pInfo.blocked == false)
            {
                if(m_StateListener != null)
                {
                    IStatePayload pPayload = new StatePayload();
                    m_StateListener.onBeginState(pInfo.stateName, m_LanguageName, pPayload);
                }

                // Since we have started a new state.  I want to fire all the tokens
                // that have been discovered while in a guessing state.
                if(flushGuessing == true)
                {
                    fireGuessingTokens();
                }
            }
            else
            {
                // Since the state was filtered I want to just remove all the
                // guessing tokens.
                mGuessingTokens.clear();
            }
        }
    }

    /**
     * Fires a OnToken event to all registered listeners.
     *
     * @param pDescriptor [in] The discovered token.
     */
    private void fireToken(String type, AST pToken)
    {
        if((m_TokenProcessor != null) /*&& (m_TopLevel != null)*/)
       {
            if(isCurrentStateFiltered() == false)
            {
                if(isTokenFiltered(type) == false)
                {
                    ITokenDescriptor pDescriptor = new TokenDescriptor();
                    pDescriptor = initializeTokenDescriptor(type, pToken, pDescriptor);

                    m_TokenProcessor.processToken(pDescriptor, getLanguageName());
             }
          }
       }
    }

    /**
     * Test if the specified token is filtered by the token filter.
     *
     * @out type [in] The new token.
     * @return true if the specified token has been filtered, false otherwise.
     */
    private boolean isTokenFiltered(String type)
    {
        boolean retVal = false;

        if(m_TokenFilter != null)
        {
            StateInformation curState = null;
            if(mStateStack.size() > 0)
            {
                curState = mStateStack.peek();
                retVal = curState.blocked;
            }

            // If the current state is blocked do not even try to ask the token filter
            // because it is required to be filtered.  Matter a fact we should never
            // get to this routine if the state is blocked, however I have added this
            // check as as santity check.
            if(!retVal && curState != null)
            {
                retVal = !m_TokenFilter.isTokenValid(type, curState.stateName, m_LanguageName);
            }
        }
        return retVal;
    }

    /**
     * Test if the current state has been filtered.  Since the filter
     * structure is in a tree format if the states parent state is filtered the
     * sub state is also filtered.  Therefore, if the current state is filtered then
     * the specified state will also be filtered.
     *
     * @return true if the current state has been filtered, false otherwise.
     */
    private boolean isCurrentStateFiltered()
    {
        boolean retVal = false;

        if(mStateStack.size() > 0)
        {
            StateInformation curState = mStateStack.peek();
            retVal = curState.blocked;
        }
        return retVal;
    }

    /**
     * Test if the specified state is filtered by the state filter.  Since the filter
     * structure is in a tree format if the states parent state is filtered the
     * sub state is also filtered.  Therefore, if the current state is filtered then
     * the specified state will also be filtered.
     *
     * @out info [in/out] The new state.
     * @return true if the specified state has been filtered, false otherwise.
     */
    private boolean isStateFiltered(String stateName)
    {
        boolean retVal = false;

        if(mStateStack.size() > 0)
        {
            StateInformation curState = mStateStack.peek();
            retVal = curState.blocked;
        }

        // Now check if the filter wants to block this state.  The if condition
        // will only be entered if the parent state is not blocked.
        if(!retVal)
        {
            if(m_StateFilter != null)
            {
                retVal = !m_StateFilter.processState(stateName, m_LanguageName);
            }
        }

        return retVal;
    }
    /**
     * Creates a ne ITokenDescriptor object.  The ITokenDescritpor will be initialized with the
     * information from the Antlr AST.
     *
     * @param type [in] The token type.
     * @param pAST [in] The AST.
     * @param pDesc [out] The ITokenDescriptor that will contain the token information.
     */
    private ITokenDescriptor initializeTokenDescriptor(String  type, AST pAST, ITokenDescriptor pDesc)
    {
        if(pAST == null) return null;

        CommonASTWithLocationsAndHidden pToken = null;

        try
        {
            pToken = (CommonASTWithLocationsAndHidden)pAST;
        }
        catch (ClassCastException e) {
            ETSystem.out.println("Got " + pAST.getClass().getName() 
                    + " instead of CommonASTWithVisibleTokens");
        }
        if(pToken == null) return null;

        String text = pToken.getText();
        
        // pDesc = new TokenDescriptor();
        pDesc.setType(type);
        pDesc.setLine(pToken.getLineNumber());
        pDesc.setColumn(pToken.getColumn());
        pDesc.setPosition(pToken.getPosition());
        pDesc.setValue(text);
        pDesc.setLength(text.length());

        addComment(pToken, pDesc);
        return addFilename(pDesc);
    }
    
    /**
     * Fires a OnToken event for every state that was discovered while in a 
     * <I>Guessing</I> state.
     */
    private void fireGuessingTokens()
    {
        for(int i = 0; i < mGuessingTokens.size(); i++)
        {
            TokenInformation info = mGuessingTokens.get(i);
            fireToken(info.type, info.token);
        }
        mGuessingTokens.clear();
    }
    
    /**
     * Fires the end state event to all registered listeners.
     * 
     * @param stateName [in] The state that is being exited.
     */
    private void fireEndState(StateInformation info)
    {
        if(m_StateListener != null && info.blocked == false)
        {
            m_StateListener.onEndState(info.stateName);
        }
    }    

    class StateInformation
    {
        StateInformation()
        {
            stateName = "";
            blocked = false;
        }

        StateInformation(StateInformation rhs)
        {
            if(this != rhs)
            {
                if(rhs.stateName.length() > 0)
                {
                    stateName = rhs.stateName;
                }
                blocked   = rhs.blocked;
            }
        }

        public String stateName;
        public boolean blocked;
    }

    class TokenInformation
    {
        public String type;
        public AST token;
    }

}
