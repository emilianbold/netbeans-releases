/*
 * ParserManagerListener.java
 *
 * Created on January 4, 2007, 11:57 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.api.languages;

import java.util.EventListener;

/**
 *
 * @author Jan Jancura
 */
public interface ParserManagerListener extends EventListener {
    
    public void parsed (int state, ASTNode ast);
}
