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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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


package org.netbeans.lib.collab.xmpp;

import java.util.Collections;
import org.jabberstudio.jso.NSI;
import org.jabberstudio.jso.Stream;
import org.jabberstudio.jso.event.PacketEvent;
import org.jabberstudio.jso.util.XPathListener;
import org.jabberstudio.jso.xpath.XPathSupport;
import org.saxpath.SAXPathException;

/**
 *
 * @author Mridul Muralidharan
 */
public class PacketWatcher extends XPathListener {
    private Throwable th;
    private boolean completed = false;
    private boolean started = false;
    private long lastActivity;
    private boolean inprogress = false;
    
    private int status;
    private Stream stream;
    private PacketHandler handler;
    
    //Constructors
    public PacketWatcher(XPathSupport support , String expr , 
            String type , String namespace) throws SAXPathException {
        super(support, expr,
                Collections.singletonMap(type, namespace));
        lastActivity = System.currentTimeMillis();
    }
    
    public void setStream(Stream stream){
        this.stream = stream;
    }

    public void setHandler(PacketHandler handler){
        this.handler = handler;
    }
    
    public void init(){
        if (null == stream || null == handler){
            throw new IllegalStateException("Not initialised");
        }
        stream.addPacketListener(PacketEvent.RECEIVED, this);
        lastActivity = System.currentTimeMillis();
        setStatus(PacketHandler.IN_PROGRESS);
    }

    public void release(){
        if (null == stream || null == handler){
            throw new IllegalStateException("Not initialised");
        }
        stream.removePacketListener(PacketEvent.RECEIVED, this);
        completed = true;
    }
    
    // Not MT-safe
    public void packetMatched(PacketEvent evt) {

        try{
            lastActivity = System.currentTimeMillis();
            inprogress = true;
            if (!started){
                synchronized(this){
                    started = true;
                    handler.preProcess();
                }
            }

            try {
                setStatus(handler.process(evt.getData()));
            } catch (Throwable th) {
                this.th = th;
                setStatus(PacketHandler.FAILURE);
            }

            if (0 == (getStatus() & PacketHandler.IN_PROGRESS)){
                synchronized(this){
                    completed = true;
                    handler.postProcess();
                }
            }
        }finally{
            lastActivity = System.currentTimeMillis();
            inprogress = false;
        }
    }
    
    protected void setStatus(int status){
        this.status = status;
    }
    
    public int getStatus(){
        return status;
    }

    public Throwable getThrowable(){
        return th;
    }
    
    public boolean isCompleted(){
        return completed;
    }
    
    public boolean isStarted(){
        return started;
    }
    
    public boolean exceedsTimeout(int timeout){
        if (inprogress){
            return false;
        }
        return System.currentTimeMillis() - lastActivity > timeout;
    }
}
