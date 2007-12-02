/*
 * JingleListener.java
 *
 * Created on January 5, 2006, 2:38 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.lib.collab;

/**
 *
 * @author jerry
 */
public interface MediaListener {
    /**
     * Callback for when a Media initiate request is received
     * @param id the packet id
     * @param initiator the user id of the person requesting the call
     * @param target the user id to which the request is directed
     * @param sessionid the Media session ID
     **/
    public void onInitiate(String id, String initiator, String target, String sessionid);
    /**
     * Callback for when a Media redirect request is recieved
     * @param id the packet id
     * @param redirLocation the location to which the call has been redirected
     */
    public void onRedirect(String id, String redirlocation);
    
    /**
     * Callback for when a Media Session has been terminated by a peer
     * @param id the packet id
     */
    
    public void onTerminate(String id);
}
