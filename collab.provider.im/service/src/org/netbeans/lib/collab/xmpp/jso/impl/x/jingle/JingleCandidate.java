/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2007, 2016 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 */

package org.netbeans.lib.collab.xmpp.jso.impl.x.jingle;

import org.jabberstudio.jso.StreamElement;

/**
 *
 * 
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
