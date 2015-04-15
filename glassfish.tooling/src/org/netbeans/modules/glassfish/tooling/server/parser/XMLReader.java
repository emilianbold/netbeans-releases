/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.glassfish.tooling.server.parser;

import java.util.List;

/**
 * Interface for various implementations that read data from domain config (domain.xml).
 * 
 * 
 * @author Peter Benedikovic, Tomas Kraus
 */
public interface XMLReader {
    
    /**
     * Every implementation needs to provide path objects.
     * Path represents the xpath on which the reader wants to be notified.
     * 
     * @return paths that the reader listens to
     */
    public List<TreeParser.Path> getPathsToListen();
    
}
