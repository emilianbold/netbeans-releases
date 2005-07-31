/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.collab.channel.filesharing.filehandler;

import com.sun.collablet.CollabException;

import org.openide.*;
import org.openide.util.*;

import java.io.*;

import java.net.*;

import java.util.*;

import org.netbeans.modules.collab.channel.filesharing.FilesharingCollablet;
import org.netbeans.modules.collab.core.Debug;


/**
 * FileHandler Resolver
 *
 * @author Ayub Khan, ayub.khan@sun.com
 * @version 1.0
 */
public class CollabFileHandlerResolver extends Object {
    ////////////////////////////////////////////////////////////////////////////
    // Class variables
    ////////////////////////////////////////////////////////////////////////////
    private static CollabFileHandlerResolver instance;

    /* layerURL */
    private static final String layerURL = "nbresloc:/org/netbeans/modules/collab/channel/filesharing/resources/layer.xml";

    ////////////////////////////////////////////////////////////////////////////
    // instance variables
    ////////////////////////////////////////////////////////////////////////////
    private Lookup.Result mimeResolverResults;
    private CollabFileHandlerFactory[] filehanderFactories = null;

    /**
     *
     *
     */
    public CollabFileHandlerResolver() {
        super();

        // Lookup all the ChannelProviders in the system.  By default, these
        // should be registered in the /Services/Hidden/Collaboration/Channels
        // folder.  The results will be updated as they change.
        mimeResolverResults = Lookup.getDefault().lookup(new Lookup.Template(CollabFileHandlerFactory.class));
    }

    ////////////////////////////////////////////////////////////////////////////
    // methods
    ////////////////////////////////////////////////////////////////////////////

    /**
     *
     * @return
     */
    public synchronized static CollabFileHandlerResolver getDefault() {
        if (instance == null) {
            instance = new CollabFileHandlerResolver();
        }

        return instance;
    }

    /**
     * newInstance
     *
     * @return
     */
    public static CollabFileHandlerResolver newInstance() {
        return new CollabFileHandlerResolver();
    }

    /**
     *
     *
     */
    public synchronized CollabFileHandlerFactory[] getMimeResolvers()
    throws CollabException {
        //1st approach to read filehandler Factories
        if (filehanderFactories == null) { //lookup filehandler factories using IDE lookup from layer.xml
            Debug.log(
                this,
                "CollabFileHandlerResolver, Trying IDE lookup approach to instantiate " + //NoI18n
                "filehandler Factories"
            ); //NoI18n				

            Collection providers = mimeResolverResults.allInstances();

            if ((providers != null) && (providers.size() > 0)) {
                filehanderFactories = (CollabFileHandlerFactory[]) providers.toArray(new CollabFileHandlerFactory[0]);
            }
        }

        //2nd approach to read filehandler Factories if 1st fails
        if ((filehanderFactories == null) || (filehanderFactories.length == 0)) { //lookup filehandler factories using XML reader from filehandler.xml
            Debug.log(
                this,
                "CollabFileHandlerResolver, Trying collab approach to instantiate " + //NoI18n
                "filehandler Factories"
            ); //NoI18n				

            if (layerURL == null) {
                throw new IllegalArgumentException("config URL null: "); //NoI18n
            }

            try {
                URL url = new URL(layerURL);
                filehanderFactories = FileHandlerMapParser.getFileHandler(url);
            } catch (java.net.MalformedURLException mue) {
                throw new CollabException(mue);
            }
        }

        //Warn user if still cannot find filehanderFactories by both approaches
        if ((filehanderFactories == null) || (filehanderFactories.length == 0)) {
            String userHome = System.getProperties().getProperty("netbeans.user"); //No18n

            // Show the user a friendly error
            String message = NbBundle.getMessage(
                    FilesharingCollablet.class, "MSG_FilesharingCollablet_NoFileHandlers", // NOI18N
                    userHome + File.separator + "cache"
                ); // NOI18N
            NotifyDescriptor.Message descriptor = new NotifyDescriptor.Message(
                    message, NotifyDescriptor.WARNING_MESSAGE
                );
            DialogDisplayer.getDefault().notify(descriptor);

            return null;
        }

        Debug.log(
            this, "CollabFileHandlerResolver, total # of resolved filehandlers: " + //NoI18n
            filehanderFactories.length
        );

        return filehanderFactories;
    }

    /**
     *
     * @param mimeType
     * @param fileExt
     * @throws CollabException
     * @return
     */
    public CollabFileHandlerFactory resolve(String mimeType, String fileExt)
    throws CollabException {
        CollabFileHandlerFactory[] local = getMimeResolvers();
        CollabFileHandlerFactory savedDefaultFileHandlerFactory = null;

        try {
            for (int i = 0; i < local.length; i++) {
                if (local[i].getID().equals("defaultfilehandler")) // NOI18N
                 {
                    savedDefaultFileHandlerFactory = local[i];

                    continue;
                }

                boolean canHandle = local[i].canHandleMIMEType(mimeType, fileExt);

                if (canHandle) {
                    Debug.log(
                        this,
                        "CollabFileHandlerResolver, filehandler : " + //NoI18n
                        local[i].getID() + " canHandle mimetype: " + mimeType + " or file extension" + //NoI18n
                        fileExt
                    );

                    return local[i];
                } else {
                    //Do nothing
                }
            }
        } finally {
        }

        return savedDefaultFileHandlerFactory;
    }
}
