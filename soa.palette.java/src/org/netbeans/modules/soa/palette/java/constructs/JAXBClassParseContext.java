/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.soa.palette.java.constructs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author gpatil
 */
public class JAXBClassParseContext {
    private Map<Class, XSD_DOMNode> classParsed = 
            new HashMap<Class, XSD_DOMNode>();
    private List<String> clsNames = new ArrayList<String>();
    private int nodesCreated = 0;
    
    public synchronized void setParsedJAXB_XSD_DOM(Class jaxbClass, XSD_DOMNode node){
        //Do not add if one exists already
        if (this.classParsed.get(jaxbClass) == null){
            classParsed.put(jaxbClass, node);
            clsNames.add(jaxbClass.getName());
        }
    }
    
    public void setPrimitiveClassUsed(Class cls){
        if (!clsNames.contains(cls.getName())){
            clsNames.add(cls.getName());
        }
    }
    
    public synchronized XSD_DOMNode getParsedJAXB_XSD_DOM(Class jaxbClass){
        return classParsed.get(jaxbClass);
    }    
    
    public boolean isDatatypeFactoryUsed(){
        boolean ret = false;
        if (!ret){
            ret = this.clsNames.contains("javax.xml.datatype.Duration"); //NOI18N
        }
        
        if (!ret){
            ret = this.clsNames.contains("javax.xml.datatype.XMLGregorianCalendar");//NOI18N
        }
        
        return ret;
    }

    public void addNodeCreated(){
        this.nodesCreated++ ;
    }
    
    public int getNodesCreated(){
        return this.nodesCreated;
    }
}
