
/*
 * File       : ParserTestListener.java
 * Created on : Feb 3, 2004
 * Author     : Aztec
 */
package org.netbeans.modules.uml.core.reverseengineering;

import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.IStateListener;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.IStatePayload;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ITokenDescriptor;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ITokenProcessor;

/**
 * @author Aztec
 */
public class ParserTestListener implements IStateListener, ITokenProcessor
{

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.IStateListener#onBeginState(java.lang.String, java.lang.String, org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.IStatePayload)
     */
    public void onBeginState(
        String stateName,
        String language,
        IStatePayload Payload)
    {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.IStateListener#onEndState(java.lang.String)
     */
    public void onEndState(String stateName)
    {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ITokenProcessor#processToken(org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ITokenDescriptor, java.lang.String)
     */
    public void processToken(ITokenDescriptor pToken, String language)
    {
        // TODO Auto-generated method stub

    }

}
