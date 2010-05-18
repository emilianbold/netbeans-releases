/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.

Oracle and Java are registered trademarks of Oracle and/or its affiliates.
Other names may be trademarks of their respective owners.
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 2007 Sun
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

package org.netbeans.lib.collab.xmpp.jso.impl.x.jingle;

import org.jabberstudio.jso.StreamElement;

/**
 *
 * @author jerry
 */
public class JingleCandidate {

    /**
     * Creates a new instance of JingleCandidate
     */
    private int _port;
    private String _ip;
    private int _generation;
    private String _name;
    public JingleCandidate(String name, String ip, int port, int generation) {
        _port = port;
        _ip = ip;
        _name = name;
        _generation = generation;
    }

    public JingleCandidate(StreamElement elem){
       if(elem.getLocalName().equals("candidate")){
            _name = elem.getAttributeValue("name");
            _ip = elem.getAttributeValue("ip");
            _port = Integer.parseInt(elem.getAttributeValue("port"));
            _generation = Integer.parseInt(elem.getAttributeValue("generation"));
        }
        else
            throw new IllegalArgumentException("Not a valid <candidate> type: " + elem.toString());
    }
    
    public int getPort(){
        return _port;
    }
    public void setPort(int i){
        _port = i;
    }
    public int getGeneration(){
        return _generation;
        
    }
    public void setGeneration(int i){
        _generation = i;
    }
    
    public String getName(){
        return _name;
    }
    public void setName(String i){
        _name = i;
    }
    
    public String getIP(){
        return _ip;
    }
    public void setIP(String i){
        _ip = i;
    }
   
    public String toString(){
        return "JingleCandidate[name = " + _name + ", ip = " + _ip + ", port = " + _port + ", generation = " + _generation + "]";
    }
    
}
