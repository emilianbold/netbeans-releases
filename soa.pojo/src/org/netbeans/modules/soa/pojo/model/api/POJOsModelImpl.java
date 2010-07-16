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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.soa.pojo.model.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.api.project.Project;
import org.netbeans.modules.soa.pojo.api.model.POJOsModel;
import org.netbeans.modules.soa.pojo.api.model.event.POJOsEvent;
import org.netbeans.modules.soa.pojo.api.model.event.POJOsEventListener;
import org.netbeans.modules.soa.pojo.schema.POJOConsumer;
import org.netbeans.modules.soa.pojo.schema.POJOConsumers;
import org.netbeans.modules.soa.pojo.schema.POJOProvider;
import org.netbeans.modules.soa.pojo.schema.POJOProviders;
import org.netbeans.modules.soa.pojo.schema.POJOs;
import org.netbeans.modules.soa.pojo.util.Util;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 *
 * @author gpatil
 */

//@ProjectServiceProvider(service=POJOsModel.class, projectType={
//    "org-netbeans-modules-java-j2seproject"
//})
public class POJOsModelImpl implements POJOsModel {
    private List<POJOsEventListener> listeners = null;
    private Project project;
    private boolean swallowCfgFileEditEvent = false;

    public POJOsModelImpl(Project prj){
        this.project = prj;
    }

    public synchronized void addPOJOsEventListener(POJOsEventListener listener) {
        if (this.listeners == null){
            this.listeners = new ArrayList<POJOsEventListener>();
        }
        this.listeners.add(listener);
    }

    public synchronized void removePOJOsEventListener(POJOsEventListener listener) {
        if (this.listeners != null){
            this.listeners.remove(listener);
        }
    }

    public void firePojoAddedEvent(POJOs ss, POJOProvider Pojo){
        POJOsEvent event = new POJOsEventImpl(ss, null, Pojo,
                POJOsEvent.POJOsEventType.EVENT_POJO_ADDED);
        dispatchEvent(event);
    }

    public void firePojoAddedEvent(POJOs ss, POJOConsumer Pojo){
        POJOsEvent event = new POJOsEventImpl(ss, null, Pojo,
                POJOsEvent.POJOsEventType.EVENT_POJO_ADDED);
        dispatchEvent(event);
    }
    public void firePojoChangedEvent(POJOs ss, POJOProvider oS, POJOProvider nS){
        POJOsEvent event = new POJOsEventImpl(ss, oS, nS,
                POJOsEvent.POJOsEventType.EVENT_POJO_CHANGED);
        dispatchEvent(event);
    }
    public void firePojoChangedEvent(POJOs ss, POJOConsumer oS, POJOConsumer nS){
        POJOsEvent event = new POJOsEventImpl(ss, oS, nS,
                POJOsEvent.POJOsEventType.EVENT_POJO_CHANGED);
        dispatchEvent(event);
    }

    public void firePojoDeletedEvent(POJOs ss, POJOProvider oS){
        POJOsEvent event = new POJOsEventImpl(ss, oS, null,
                POJOsEvent.POJOsEventType.EVENT_POJO_DELETED);
        dispatchEvent(event);
    }

    public void fireCfgFileEditedEvent(POJOs ss){
        POJOsEvent event = new POJOsEventImpl(ss, null, null,
                POJOsEvent.POJOsEventType.EVENT_CFG_FILE_EDITED);
        dispatchEvent(event);
    }

    private void dispatchEvent(POJOsEvent event){
        List<POJOsEventListener> lss = new ArrayList<POJOsEventListener>();
        synchronized (this){
            if (this.listeners != null){
                lss.addAll(this.listeners);
            }
        }
        POJOsEvent.POJOsEventType  eventType = event.getEventType();

        for (POJOsEventListener ls: lss){
            try {
                switch (eventType){
                    case EVENT_POJO_ADDED:
                        ls.pojoAdded(event);
                        break;
                    case EVENT_POJO_CHANGED:
                        ls.pojoChanged(event);
                        break;
                    case EVENT_POJO_DELETED:
                        ls.pojoDeleted(event);
                        break;
                    case EVENT_CFG_FILE_EDITED:
                        ls.configFileEdited(event);
                        break;
                }
            } catch (Exception ex){
                Exceptions.printStackTrace(ex);
            }
        }
    }

    public void addPojo(POJOProvider pojo){
        POJOs pjs = Util.getPOJOs(project);
        try {
            synchronized(this){
                this.swallowCfgFileEditEvent = true;
            }
            POJOProviders ps = pjs.getPOJOProviders();
            if (ps == null){
                ps = new POJOProviders();
                pjs.setPOJOProviders(ps);
            }
            ps.addPOJOProvider(pojo);
            Util.savePOJOs(project, pjs);
            this.firePojoAddedEvent(pjs, pojo);
        } finally {
            synchronized(this){
                this.swallowCfgFileEditEvent = false;
            }
        }
    }

    public void addConsumer(POJOConsumer pojo){
        POJOs scs = Util.getPOJOs(project);
        try {
            synchronized(this){
                this.swallowCfgFileEditEvent = true;
            }

            POJOConsumers pjcs = scs.getPOJOConsumers();
            if ( pjcs == null) {
                pjcs = new POJOConsumers();
                scs.setPOJOConsumers(pjcs);
            }
            POJOConsumer[] pjcsArry = pjcs.getPOJOConsumer();
            boolean bMatchFound = false;
            for (POJOConsumer pjcons: pjcsArry) {
                if ( pjcons.equals(pojo)){
                    bMatchFound = true;
                    break;
                }
            }
            if ( !bMatchFound) {
                pjcs.addPOJOConsumer(pojo);
                Util.savePOJOs(project, scs);
                this.firePojoAddedEvent(scs, pojo);

            }
        } finally {
            synchronized(this){
                this.swallowCfgFileEditEvent = false;
            }
        }
    }
    public void changePOJOConsumer(POJOConsumer os, POJOConsumer ns){
        POJOs scs = Util.getPOJOs(project);
        try {
            synchronized(this){
                this.swallowCfgFileEditEvent = true;
            }
            POJOConsumers pjcs = scs.getPOJOConsumers();
            if ( pjcs != null) {
                pjcs.removePOJOConsumer(os);
                pjcs.addPOJOConsumer(ns);
                Util.savePOJOs(project, scs);
                this.firePojoChangedEvent(scs, os, ns);

            }
        } finally {
            synchronized(this){
                this.swallowCfgFileEditEvent = false;
            }
        }

    }

    private Map<String, POJOProvider> getPOJOProviderMap(POJOProviders ps){
        Map<String, POJOProvider> ret = new HashMap<String, POJOProvider>();
        if (ps != null){
            POJOProvider[] prs = ps.getPOJOProvider();
            for (POJOProvider p : prs){
                ret.put(p.getPackage() + "." + p.getClassName(), p);
            }
        }
        return ret;
    }

    public void changePojo(POJOProvider os, POJOProvider ns){
        POJOs pjs = Util.getPOJOs(project);
        try {
            synchronized(this){
                this.swallowCfgFileEditEvent = true;
            }

            POJOProviders ps = pjs.getPOJOProviders();

            if (ps != null){
                Map<String, POJOProvider> map = getPOJOProviderMap(ps);
                ps.removePOJOProvider(map.get(os.getPackage() + "." + os.getClassName()));
                ps.addPOJOProvider(ns);
            }
            Util.savePOJOs(project, pjs);
            this.firePojoChangedEvent(pjs, os, ns);
        } finally {
            synchronized(this){
                this.swallowCfgFileEditEvent = false;
            }
        }
    }

    public void deletePojo(POJOProvider Pojo){
        POJOs pjs = Util.getPOJOs(project);
        try {
            synchronized(this){
                this.swallowCfgFileEditEvent = true;
            }

            POJOProviders ps = pjs.getPOJOProviders();
            if (ps != null){
                ps.removePOJOProvider(Pojo);
            }

            Util.savePOJOs(project, pjs);
            this.firePojoDeletedEvent(pjs, Pojo);
        } finally {
            synchronized(this){
                this.swallowCfgFileEditEvent = false;
            }
        }
    }

    private final class JaxbCfgChangeListener extends FileChangeAdapter {
        private void refreshNodes() {
            boolean skipEvent = false;
            synchronized (POJOsModelImpl.this){
                skipEvent = POJOsModelImpl.this.swallowCfgFileEditEvent;
            }

            if (!skipEvent){
                POJOs scs = Util.getPOJOs(project);
                POJOsModelImpl.this.fireCfgFileEditedEvent(scs);
            }

//            SwingUtilities.invokeLater(new Runnable() {
//
//                public void run() {
//                    try {
//                        JAXBRootNodeList.this.rootKeys.clear();
//                        fireChange();
//                    } catch (Exception ex) {
//                        logger.log(Level.WARNING, "refreshing root nodes.", ex);
//                    }
//
//                    try {
//                        updateKeys();
//                    } catch (Exception ex) {
//                        logger.log(Level.WARNING, "refreshing root nodes.", ex);
//                    }
//                    fireChange();
//                }
//            });
        }

        public void fileChanged(FileEvent fe) {
            refreshNodes();
        }

        public void fileRenamed(FileEvent fe) {
            refreshNodes();
        }

        public void fileDataCreated(FileEvent fe) {
            // New file is created, check if config file is created.
            FileObject fo = Util.getFOForPOJOsCfgFile(project);
            if ((fo != null) && (fo.isValid())) {
                // Remove listening on folder, add for the file

                // Do not need to listen to Dir see #110406
                //ProjectHelper.removeModelListner(project, jaxbListener);
                //ProjectHelper.addCfgFileChangeListener(project, jaxbListener);
                refreshNodes();
            }
        }

        public void fileDeleted(FileEvent fe) {
            refreshNodes();
        }
    }
}
