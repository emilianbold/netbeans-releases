package org.netbeans.modules.uml.parser.java;

import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.IStateListener;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.IStatePayload;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ITokenDescriptor;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ITokenProcessor;

/**
 * 
 */
public class ParserTestListener implements IStateListener, ITokenProcessor
{
    
    
    AbstractParserTestCase pt;
    public ParserTestListener(AbstractParserTestCase pt)
    {
       this.pt=pt; 
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.IStateListener#onBeginState(java.lang.String, java.lang.String, org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.IStatePayload)
     */
    public void onBeginState(String stateName, String language, IStatePayload payload)
    {       
        pt.addState(stateName);        
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.IStateListener#onEndState(java.lang.String)
     */
    public void onEndState(String stateName)
    {
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ITokenProcessor#processToken(org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ITokenDescriptor, java.lang.String)
     */
    public void processToken(ITokenDescriptor pToken, String language)
    {
    	    pt.addToken(pToken.getValue(),pToken.getType());
//    	  System.out.println("{\""+pToken.getValue()+"\",  \""+pToken.getType()+"\"}");
    }
}