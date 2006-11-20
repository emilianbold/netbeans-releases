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

package org.netbeans.modules.websvc.core.testutils;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.LocalFileSystem;
import org.openide.filesystems.MultiFileSystem;
import org.openide.filesystems.Repository;
import org.openide.filesystems.XMLFileSystem;
import org.xml.sax.SAXException;

/**
 *
 * @author Lukas Jungmann
 */
public class RepositoryImpl extends Repository {
    
    /** Creates a new instance of RepositotyImpl */
    public RepositoryImpl() throws Exception {
        super(mksystem());
    }
    
    private static FileSystem mksystem() throws Exception {
        LocalFileSystem lfs = new LocalFileSystem();
        File systemDir = new File(System.getProperty("websvc.core.test.repo.root"));
        systemDir.mkdirs();
        lfs.setRootDirectory(systemDir);
        lfs.setReadOnly(false);
        List<FileSystem> layers = new ArrayList<FileSystem>();
        layers.add(lfs);
        // get layer for the TestServer
        //addLayer(layers, "org/netbeans/modules/j2ee/test/testserver/resources/layer.xml");
        // get layer for project types
//        addLayer(layers, "org/netbeans/modules/web/project/ui/resources/layer.xml");
//        addLayer(layers, "org/netbeans/modules/j2ee/ejbjarproject/ui/resources/layer.xml");
        addLayer(layers, "org/netbeans/modules/java/j2seproject/ui/resources/layer.xml");
//        addLayer(layers, "org/netbeans/modules/j2ee/clientproject/ui/resources/layer.xml");
        // get layer for the websvc/core
        addLayer(layers, "org/netbeans/modules/websvc/core/resources/mf-layer.xml");
        // get layer for the java support (for Main class template)
//        addLayer(layers, "org/netbeans/modules/java/resources/mf-layer.xml");
        MultiFileSystem mfs = new MultiFileSystem((FileSystem[]) layers.toArray(new FileSystem[layers.size()]));
        return mfs;
    }
    
    private static void addLayer(List<FileSystem> layers, String layerRes) throws SAXException {
        URL layerFile = RepositoryImpl.class.getClassLoader().getResource(layerRes);
        assert layerFile != null;
        layers.add(new XMLFileSystem(layerFile));
    }
    
}
