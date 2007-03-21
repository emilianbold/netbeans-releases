/*
 * ServerVersionProvider.java
 *
 * Created on March 21, 2007, 2:40 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.websvc.api.jaxws.project;

/**
 *
 * @author rico
 *  
 */
public interface JAXWSVersionProvider {
    
    public static final String JAXWS20 = "jaxws20";
    public static final String JAXWS21 = "jaxws21";
    
    public String getJAXWSVersion();
    
}
