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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.jabberstudio.jso.NSI;
import org.jabberstudio.jso.Stream;
import org.jabberstudio.jso.StreamException;
import org.jabberstudio.jso.StreamFeature;
import org.jabberstudio.jso.event.PacketEvent;
import org.jabberstudio.jso.event.StreamFeaturesEvent;
import org.jabberstudio.jso.event.StreamFeaturesListener;


/**
 *
 * @author Mridul Muralidharan
 */
public class FeatureIdentifier implements StreamFeaturesListener{
    
    private List featureList;
    private Stream stream;
    private boolean initialised = false;
    
    public FeatureIdentifier(Stream stream){
        this.featureList = new LinkedList();
        this.stream = stream;
    }

    public List getFeatureList(){
        return new LinkedList(featureList);
    }
    
    public void setFeatureList(List list){
        if (null != list){
            this.featureList = new LinkedList(list);
        }
        else{
            this.featureList = new LinkedList();
        }
    }
    
    public void resetFeatureList(){
        featureList.clear();
    }
    

    public synchronized void initialise() throws StreamException{
        if (null == stream){
            throw new IllegalStateException("Stream not specified");
        }
        if (!initialised){
            initialised = true;
            stream.addStreamFeaturesListener(PacketEvent.RECEIVED , this);
        }
    }
    
    public synchronized void release() throws StreamException{
        /*
        if (null == stream){
            throw new IllegalStateException("Stream not specified");
        }
         */
        if (initialised){
            initialised = false;
            stream.removeStreamFeaturesListener(PacketEvent.RECEIVED , this);
        }
    }
    
    public void process(long timeout) throws StreamException{
        if (null == stream){
            throw new IllegalStateException("Stream not specified");
        }

        synchronized(this){
            boolean done = false;
            long startTime = System.currentTimeMillis();
            while (!done){
                stream.process();

                if (!featureList.isEmpty()){
                    break;
                }

                int diff = (int)(System.currentTimeMillis() - startTime);

                // If timeout specified is <= 0 , then infinite loop until all the
                // specified features are found.
                if (stream.getCurrentStatus() == stream.CLOSED ||
                        (timeout > 0 && diff > timeout)){
                    XMPPSessionProvider.debug("feature wait timeout");
                    break;
                }
                try{
                    wait(10);
                }catch(InterruptedException ie) {}
            }
        }
    }
    
    public void featuresReported(StreamFeaturesEvent evt){
        
        synchronized(this){
            List features = evt.listFeatures();

            assert (null != features);
            //featureList.addAll(features);
            Iterator iter = features.iterator();

            XMPPSessionProvider.debug("featuresReported : " + evt);

            while (iter.hasNext()){
                StreamFeature sf = (StreamFeature)iter.next();
                NSI nsi = sf.getNSI();

                XMPPSessionProvider.debug("featuresReported(nsi) : " + nsi);

                featureList.add(sf);
            }
        }
    }
}
