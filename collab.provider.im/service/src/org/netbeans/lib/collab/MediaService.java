/*
 * JingleService.java
 *
 * Created on January 5, 2006, 2:37 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.lib.collab;

import org.netbeans.lib.collab.xmpp.jingle.*;
import org.netbeans.lib.collab.MediaListener;
import org.jabberstudio.jso.Packet;


/**
 *
 * @author jerry
 */
public interface MediaService {
    
        /**
         * add a listener for Voip requests
         */
        public void addListener(MediaListener listener);   
        /**
         * remove an active listener
         */
        public void removeListener(MediaListener listener);
        
        /**
         * Send a call initiate request
         * @param target the user id of the person to call
         */
        public void initiate(String target);
        /**
         * Send a redirect to a caller.
         * @param id The session id
         * @param caller User id of the caller who initiated the call
         * @param location where the call should be redirected
         **/
	public void redirect(String id, String caller, String redirLocation);
	/**
	 This method should be called from the client only
	 It tries to find if there are any server-side components
	 that can service voip requests
	 */
	public String findMediaGateway();
        
        /**
         * Send a terminate request to the peer
         * @param id The session ID
         * @param target the userid to send the request
         */
        public void terminate(String id, String target);
        
        /**
         * Find media addresses of a user
         * Used to locate the addresses at which a user can be reached
         * This could be, for eg, a telephone number or a SIP url
         * @param userid the User whose addresses are to be found
         */
        
        public String getAddress(String userid);
    
}
