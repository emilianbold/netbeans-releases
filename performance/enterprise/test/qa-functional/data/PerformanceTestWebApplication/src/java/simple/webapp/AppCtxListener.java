/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/*
 * AppCtxListener.java
 *
 * Created on January 12, 2005, 7:06 PM
 */

package simple.webapp;

import javax.servlet.ServletContextListener;
import javax.servlet.ServletContextEvent;

/**
 *
 * @author  radim
 * @version
 *
 * Web application lifecycle listener.
 */

public class AppCtxListener implements ServletContextListener {
    /**
     * ### Method from ServletContextListener ###
     * 
     * Called when a Web application is first ready to process requests
     * (i.e. on Web server startup and when a context is added or reloaded).
     * 
     * For example, here might be database connections established
     * and added to the servlet context attributes.
     */
    public void contextInitialized(ServletContextEvent evt) {
        // TODO add your code here e.g.:
        /*
            Connection con = // create connection
            evt.getServletContext().setAttribute("con", con);
        */
    }

    /**
     * ### Method from ServletContextListener ###
     * 
     * Called when a Web application is about to be shut down
     * (i.e. on Web server shutdown or when a context is removed or reloaded).
     * Request handling will be stopped before this method is called.
     * 
     * For example, the database connections can be closed here.
     */
    public void contextDestroyed(ServletContextEvent evt) {
        // TODO add your code here e.g.:
        /*
                Connection con = (Connection) e.getServletContext().getAttribute("con");
                try { con.close(); } catch (SQLException ignored) { } // close connection
        */
    }
}
