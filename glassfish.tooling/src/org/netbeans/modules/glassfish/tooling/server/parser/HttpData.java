/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.glassfish.tooling.server.parser;

/**
 * Data holder for port informations from domain.xml.
 * <p/>
 * @author Peter Benedikovic, Tomas Kraus
 */
public class HttpData {

	private final String id;
    private final int port;
    private final boolean secure;
    
    public HttpData(String id, int port, boolean secure) {
        this.id = id;
        this.port = port;
        this.secure = secure;
    }
    
    public String getId() {
        return id;
    }

    public int getPort() {
        return port;
    }

    public boolean isSecure() {
        return secure;
    }
    
    @Override
    public String toString() {
        return "{ " + id + ", " + port + ", " + secure + " }";	//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
    }
}
