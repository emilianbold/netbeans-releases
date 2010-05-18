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
package org.netbeans.lib.collab.xmpp.httpbind;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * I dont know if there is already something like this in the io api.
 * Essentially , this acts as a input and output stream on the same
 * data. Something like a queue for streams.
 * Piped(In/Out)putStream would provide similar functionality , but they have a
 * size restriction which makes them unsuitable for some needs.
 * Also, this is non-blocking.
 *
 * @author Mridul Muralidharan
 */
public class QueuedStream {
    
    //protected List<byte[]> dataList = new LinkedList<byte[]>();
    protected List dataList = new LinkedList();
    protected int availableBytes = 0;
    protected Object lockObj = new Object();
    private boolean initialised = true;
    private boolean closed = false;
    
    public int available() throws IOException{
        if (!isInitialised()){
            throw new IOException ("Not initialised"); // NOI18N
        }
        return availableBytes;
    }
    
    public int read(byte data[] , int offset , int len) 
        throws IllegalArgumentException , IOException{
        
        // Only for debugging.
        int startOffset = offset;
        
        /*
        if (HTTPSessionController.isDebugOn()){
            HTTPSessionController.debug("QueuedStream.read()"); // NOI18N
        }
         */        
        
        if (!isInitialised()){
            throw new IOException ("Not initialised"); // NOI18N
        }

        if (isClosed() && 0 == available()){
            //throw new IOException ("Closed"); // NOI18N
            // EOF
            return -1;
        }
        
        validateBounds(data , offset , len);
        
        if (0 == len || 0 == available()){
            return 0;
        }
        if (HTTPSessionController.isDebugOn()){
            HTTPSessionController.debug("QueuedStream.read : " + available() + " , " + len); // NOI18N
        }
        
        synchronized(lockObj){
            int avail = available();
            Iterator iter = dataList.iterator();
            int retval;
            
            if (avail < len){
                len = avail;
            }
            
            retval = len;
            
            while (iter.hasNext() && len > 0){
                byte[] srcData = (byte[])iter.next();
                
                if (srcData.length > len){
                    // Split this block into two , and then
                    // reinsert at this location.
                    copyBytes(data , offset , srcData , 0 , len);
                    byte[] narr = new byte[srcData.length - len];
                    System.arraycopy(srcData , len , narr, 0 , srcData.length - len);
                    iter.remove();
                    dataList.add(0 , narr);
                    break;
                }
                else {
                    copyBytes(data , offset , srcData , 0 , srcData.length);
                    offset += srcData.length;
                    len -= srcData.length;
                    iter.remove();
                }
            }

            availableBytes -= retval;
            if (HTTPSessionController.isDebugOn()){
                HTTPSessionController.debug("QueuedStream.read() :\n" +  // NOI18N
                    new String(data , startOffset, retval));
            }
            return retval;
        }
    }
    
    public int write(byte data[]) throws IllegalArgumentException , IOException{
        if (HTTPSessionController.isDebugOn()){
            HTTPSessionController.debug("QueuedStream.write : " + data.length); // NOI18N
        }
        if (!isInitialised()){
            throw new IOException ("Not initialised"); // NOI18N
        }
        
        if (isClosed()){
            throw new IOException ("Closed"); // NOI18N
        }
        
        if (null == data){
            throw new IllegalArgumentException("data == null"); // NOI18N
        }
        
        if (0 == data.length){
            return 0;
        }
        
        synchronized(lockObj){
            dataList.add(data);
            availableBytes += data.length;
            return data.length;
        }
    }

    // No input validation - use with care !
    protected void copyBytes(byte[] sink , int sinkOffset, byte[] source, int  sourceOffset, 
            int length){
        // while (length -- > 0) sink[sinkOffset ++] = source[sourceOffset ++];
        System.arraycopy(source, sourceOffset , sink, sinkOffset, length);
    }
    
    
    protected void validateBounds(byte[] buffer , int offset , int length)
        throws IllegalArgumentException{

        if (null == buffer || offset < 0 || length < 0 || 
                offset >= buffer.length || 
                offset + length > buffer.length){
            throw new IllegalArgumentException("Invalid inputs : " + buffer +  // NOI18N
                    " , " + offset + " , " + length); // NOI18N
        }
    }
    
    public void close() throws IOException{
        setClosed(false);
    }
    
    protected void setInitialised(boolean initialised){
        this.initialised = initialised;
    }
    
    public boolean isInitialised(){
        return initialised;
    }

    protected void setClosed(boolean closed){
        this.closed = closed;
    }
    
    public boolean isClosed(){
        return closed;
    }
}
