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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
