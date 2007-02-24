package org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser;

import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.JavaUMLParserProcessor;
import java.util.ArrayList;

import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.statehandlers.PackageStateHandler;

public class UMLParserTestListener
        extends
        org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.JavaUMLParserProcessor {
    /**Stores XMIData */
    private ArrayList<String> xmiData = new ArrayList<String>();
    
    public void clearList() {
        xmiData.clear();
    }
    
    @Override
            protected void removeStateHandler(String stateName) {
        if (m_StateHandlers.size() > 0) {
            HandlerData oldData = m_StateHandlers.pop();
            
            // Remove the hander from the stack and notify the handler that
            // the state has ended.
            if (oldData.handler != null) {
                oldData.handler.stateComplete(stateName);
            /*
             * JavaUMLParserProcessor.removeStateHandler method is copied
             * and placed here. Only the Following line is added
             * additionally. If any modification made in
             * JavaUMLParserProcessor.removeStateHandler then immediately
             * that should copied to this place.
             */
                addXMIData(stateName, oldData);
                if ("Package".equals(oldData.stateName)) {
                    PackageStateHandler pd = (PackageStateHandler) oldData.handler;
                    if (pd != null)
                        m_CurrentPackage = pd.getFullPackageName();
                }
            }
        }
    }
    
    /**
     *Add Valid XMI Data to the List.
     * This will be joined together to form the retrieved result
     */
    private void addXMIData(String stateName, HandlerData oldData) {
        String possibleRootDeclarations = "|Class Declaration|Enumeration Declaration|"
                + "Interface Declaration|Dependency|Package|";
        // Some state may contains part of these state name. To find out exact
        // Phrase Pipe(|) symbol used.
        if (possibleRootDeclarations.indexOf("|" + stateName + "|") > -1) {
            String strXmiData = oldData.handler.getDOMNode().asXML();
            if (strXmiData.indexOf("language=\"Java\"") > 0)
                xmiData.add(strXmiData);
        }
        
    }
    
    public ArrayList<String> getXMIData() {
        return xmiData;
    }
}
