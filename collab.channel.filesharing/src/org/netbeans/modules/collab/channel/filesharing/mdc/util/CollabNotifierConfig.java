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
package org.netbeans.modules.collab.channel.filesharing.mdc.util;

import com.sun.collablet.CollabException;

import org.openide.execution.*;

import java.io.*;

import java.lang.reflect.Method;

import java.net.*;

import java.util.*;

import org.netbeans.modules.collab.channel.filesharing.mdc.configbean.*;
import org.netbeans.modules.collab.core.Debug;


/**
 * Default EventProcessor
 *
 * @author  Ayub Khan, ayub.khan@sun.com
 * @version 1.0
 */
public class CollabNotifierConfig extends CollabConfigVerifier {
    ////////////////////////////////////////////////////////////////////////////
    // Instance variables
    ////////////////////////////////////////////////////////////////////////////

    /* normalizedEventID Map */
    protected HashMap normalizedEventIDMap = new HashMap();

    /**
     * constructor
     *
     */
    public CollabNotifierConfig() {
        super();
    }

    /**
     * constructor
     *
     */
    public CollabNotifierConfig(String currentVersion) {
        super(currentVersion);
    }

    ////////////////////////////////////////////////////////////////////////////
    // methods
    ////////////////////////////////////////////////////////////////////////////

    /**
     * init
     *
     */
    protected void init(String configURL) throws CollabException {
        loadConfig(configURL);
    }

    /**
     * getNormalizedEventID
     *
     * @return        normalized event ID Map
     */
    public HashMap getNormalizedEventID() {
        return this.normalizedEventIDMap;
    }

    /**
     * getNormalizedEventID
     *
     * @return        normalized event ID for given event
     */
    public String getNormalizedEventID(String eventBeanID) {
        return (String) this.normalizedEventIDMap.get(eventBeanID);
    }

    /**
     * load config for notifer
     *
     * @param configURL
     */
    protected void loadConfig(String configURL) throws CollabException {
        if (configURL == null) {
            throw new IllegalArgumentException("config URL null: ");
        }

        CCollab collab = null;

        try {
            URL url = new URL(configURL);
            InputStream in = url.openStream();

            //create DOM
            collab = CCollab.read(in);
        } catch (java.net.MalformedURLException murlex) {
            throw new CollabException(murlex);
        } catch (javax.xml.parsers.ParserConfigurationException parsex) {
            throw new CollabException(parsex);
        } catch (org.xml.sax.SAXException saxx) {
            throw new CollabException(saxx);
        } catch (java.io.IOException iox) {
            throw new CollabException(iox);
        }

        if (collab == null) {
            throw new IllegalArgumentException("config load failed for: " + configURL);
        }

        //get config
        Config[] config = collab.getMdcConfig();

        for (int i = 0; i < config.length; i++) {
            //getconfigVersion
            String configVersion = config[i].getVersion();

            //get NotifierConfig
            EventNotifierConfig notifierConfig = config[i].getMdcEventNotifierConfig();

            //get all registered events
            RegisterEvent[] registerEvents = notifierConfig.getRegisterEvent();

            for (int j = 0; j < registerEvents.length; j++) {
                RegisterEvent registerEvent = registerEvents[j];
                String eventName = registerEvent.getEventName();
                String normalizedEventID = createUniqueNormalizedEventID(configVersion, eventName);
                String eventClassName = registerEvent.getEventClass();
                Debug.log(this, "eventClassName: " + eventClassName); //NoI18n

                try {
                    ClassLoader cl = new NbClassLoader();
                    Class myClass = Class.forName(eventClassName, true, cl);
                    Method getEventIDMethod = findGetEventID(myClass);
                    String eventBeanID = (String) getEventIDMethod.invoke(null, new Object[] {  });
                    Debug.log(this, "Strong type eventID: " + eventBeanID); //NoI18n
                    Debug.log(this, "normalized eventID: " + normalizedEventID); //NoI18n
                    normalizedEventIDMap.put(eventBeanID, normalizedEventID);
                } catch (ClassNotFoundException classNotFound) {
                    throw new CollabException(classNotFound);
                } catch (java.lang.InstantiationException iex) {
                    throw new CollabException(iex);
                } catch (java.lang.IllegalAccessException iax) {
                    throw new CollabException(iax);
                } catch (ExceptionInInitializerError eiie) {
                    Throwable t = eiie.getException();

                    if (t instanceof IllegalStateException) {
                        throw new CollabException(t.getMessage());
                    } else if (t instanceof Exception) {
                        throw (CollabException) t;
                    } else {
                        throw new CollabException(t.toString());
                    }
                } catch (Exception ex) {
                    throw new CollabException(ex);
                }
            }
        }
    }

    private static Method findGetEventID(Class clazz) throws Exception {
        Method[] methods = clazz.getMethods();

        for (int i = 0; i < methods.length; i++) {
            if (methods[i].getName().equals("getEventID")) //NoI18n
             {
                return methods[i];
            }
        }

        Debug.log("CollabNotifierConfig", clazz.getName() + "::getEventID() method not found"); //NoI18n		

        return null;
    }
}
