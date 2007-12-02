/*
 * Copyright 2006 Sun Microsystems, Inc.  All rights reserved.
 */

package org.netbeans.lib.collab.xmpp.sasl;

import java.io.ByteArrayOutputStream;
import org.netbeans.lib.collab.SASLClientProvider;
import org.netbeans.lib.collab.SASLData;
import org.netbeans.lib.collab.SASLProviderException;

/**
 *
 * @author mridul
 */
public class IdentitySSOSASLClientProvider implements SASLClientProvider{
    
    private String loginName;
    private String token;
    private String server;
    
    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }

    public void setPassword(String password) {
        this.token = password;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public void init() throws SASLProviderException {
        // do nothing.
    }

    public void process(SASLData data) throws SASLProviderException {
        int status = data.getRequestStatus();
        if (status == SASLData.START){
            // send uid/passwd
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try{
                baos.write(loginName.getBytes("UTF-8"));
                baos.write(0);
                baos.write(token.getBytes("UTF-8"));
                baos.write(0);
                data.setResponseData(baos.toByteArray());
                data.setResponseStatus(SASLData.START);
            } catch(Exception ex){
                // Unexpected ...
                data.setResponseStatus(SASLData.ABORT);
            }
            return ;
        }
        if (status == SASLData.SUCCESS){
            // Cool !
            data.setResponseData(null);
            data.setResponseStatus(SASLData.SUCCESS);
            return ;
        }
        
        // Everything else is unexpected ...
        data.setResponseData(null);
        data.setResponseStatus(SASLData.FAILURE);
        return ;
    }

    public void close() {
        // do nothing.
    }
}
