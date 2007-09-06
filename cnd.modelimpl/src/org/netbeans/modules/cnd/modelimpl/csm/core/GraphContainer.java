/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 
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

package org.netbeans.modules.cnd.modelimpl.csm.core;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmInclude;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.model.CsmUID;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;
import org.netbeans.modules.cnd.modelimpl.repository.GraphContainerKey;
import org.netbeans.modules.cnd.modelimpl.uid.UIDCsmConverter;
import org.netbeans.modules.cnd.modelimpl.uid.UIDObjectFactory;
import org.netbeans.modules.cnd.repository.spi.Persistent;
import org.netbeans.modules.cnd.repository.support.SelfPersistent;

/**
 * Storage for include graph.
 * @author Alexander Simon
 */
public class GraphContainer extends ProjectComponent implements Persistent, SelfPersistent {
    
    /** Creates a new instance of GraphContainer */
    public GraphContainer(ProjectBase project) {
	super(new GraphContainerKey(project.getUniqueName()));
        if (TraceFlags.USE_REPOSITORY) {
            graph = new HashMap<CsmUID<CsmFile>, NodeLink>();
	    put();
        } else {
            graphOld = new HashMap<CsmFile,NodeLinkOld>();
        }
    }
    
    public GraphContainer (final DataInput input) throws IOException {
	super(input);
        assert input != null;
        graph = new HashMap<CsmUID<CsmFile>, NodeLink>();
        readUIDToNodeLinkMap(input, graph);
    }
    
    /**
     * save file graph.
     * called after (re)parse.
     */
    public void putFile(CsmFile master){
        if (TraceFlags.USE_REPOSITORY) {
            putFileNew(master);
        } else {
            putFileOld(master);
        }
    }
    
    private void putFileNew(CsmFile master) {
        CsmUID<CsmFile> key = UIDCsmConverter.fileToUID(master);
        if (key != null) {
            synchronized (graph){
                NodeLink node = graph.get(key);
                if (node != null){
                    Set<CsmUID<CsmFile>> outLink = node.getOutLinks();
                    for (CsmUID<CsmFile> out : outLink){
                        NodeLink pair = graph.get(out);
                        if (pair != null){
                            pair.removeInLink(key);
                        }
                    }
                    outLink.clear();
                } else {
                    node = new NodeLink();
                    graph.put(key,node);
                }
                for (CsmInclude include : master.getIncludes()){
                    CsmFile to = include.getIncludeFile();
                    if (to != null) {
                        CsmUID<CsmFile> out = UIDCsmConverter.fileToUID(to);
                        NodeLink pair = graph.get(out);
                        if (pair == null){
                            pair = new NodeLink();
                            graph.put(out,pair);
                        }
                        node.addOutLink(out);
                        pair.addInLink(key);
                    }
                }
            }
        }
	put();
    }
    
    private void putFileOld(CsmFile master){
        synchronized (graphOld){
            NodeLinkOld node = graphOld.get(master);
            if (node != null){
                Set<CsmFile> outLink = node.getOutLinks();
                for (CsmFile out : outLink){
                    NodeLinkOld pair = graphOld.get(out);
                    if (pair != null){
                        pair.removeInLink(master);
                    }
                }
                outLink.clear();
            } else {
                node = new NodeLinkOld();
                graphOld.put(master,node);
            }
            for (CsmInclude include : master.getIncludes()){
                CsmFile to = include.getIncludeFile();
                if (to != null) {
                    NodeLinkOld pair = graphOld.get(to);
                    if (pair == null){
                        pair = new NodeLinkOld();
                        graphOld.put(to,pair);
                    }
                    node.addOutLink(to);
                    pair.addInLink(master);
                }
            }
        }
    }
    
    /**
     * remove file graph.
     * called after remove, delelete.
     */
    public void removeFile(CsmFile master){
        if (TraceFlags.USE_REPOSITORY) {
            removeFileNew(master);
        } else {
            removeFileOld(master);
        }
    }
    
    private void removeFileNew(CsmFile master) {
        CsmUID<CsmFile> key = UIDCsmConverter.fileToUID(master);
        if (key != null) {
            synchronized (graph){
                NodeLink node = graph.get(key);
                if (node != null){
                    Set<CsmUID<CsmFile>> inLink = node.getInLinks();
                    for (CsmUID<CsmFile> in : inLink){
                        NodeLink pair = graph.get(in);
                        if (pair != null){
                            pair.removeOutLink(key);
                        }
                    }
                    inLink.clear();
                    Set<CsmUID<CsmFile>> outLink = node.getOutLinks();
                    for (CsmUID<CsmFile> out : outLink){
                        NodeLink pair = graph.get(out);
                        if (pair != null){
                            pair.removeInLink(key);
                        }
                    }
                    outLink.clear();
                    graph.remove(key);
                }
            }
        }
	put();
    }
    
    private void removeFileOld(CsmFile master){
        synchronized (graphOld){
            NodeLinkOld node = graphOld.get(master);
            if (node != null){
                Set<CsmFile> inLink = node.getInLinks();
                for (CsmFile in : inLink){
                    NodeLinkOld pair = graphOld.get(in);
                    if (pair != null){
                        pair.removeOutLink(master);
                    }
                }
                inLink.clear();
                Set<CsmFile> outLink = node.getOutLinks();
                for (CsmFile out : outLink){
                    NodeLinkOld pair = graphOld.get(out);
                    if (pair != null){
                        pair.removeInLink(master);
                    }
                }
                outLink.clear();
                graphOld.remove(master);
            }
        }
    }
    
    /**
     * gets all included files.
     */
    public Set<CsmFile> getIncludedFiles(CsmFile start){
        if (TraceFlags.USE_REPOSITORY) {
            Set<CsmUID<CsmFile>> res = new HashSet<CsmUID<CsmFile>>();
            CsmUID<CsmFile> keyFrom = UIDCsmConverter.fileToUID(start);
            if (keyFrom != null) {
                synchronized (graph){
                    getIncludedFiles(res, keyFrom);
                }
            }
            return convertToFiles(res);
        } else {
            Set<CsmFile> res = new HashSet<CsmFile>();
            synchronized (graphOld){
                getIncludedFiles(res, start);
            }
            return res;
        }
    }
    
    /**
     * gets all files that include this file.
     */
    public Set<CsmFile> getParentFiles(CsmFile start){
        if (TraceFlags.USE_REPOSITORY) {
            Set<CsmUID<CsmFile>> res = new HashSet<CsmUID<CsmFile>>();
            CsmUID<CsmFile> keyTo = UIDCsmConverter.fileToUID(start);
            if (keyTo != null) {
                synchronized (graph){
                    getParentFiles(res, keyTo);
                }
            }
            return convertToFiles(res);
        } else {
            Set<CsmFile> res = new HashSet<CsmFile>();
            synchronized (graph){
                getParentFiles(res, start);
            }
            return res;
        }
    }
    
    /**
     * gets all files that include this file.
     */
    public Set<CsmFile> getTopParentFiles(CsmFile start){
        if (TraceFlags.USE_REPOSITORY) {
            Set<CsmUID<CsmFile>> res = new HashSet<CsmUID<CsmFile>>();
            CsmUID<CsmFile> keyTo = UIDCsmConverter.fileToUID(start);
            if (keyTo != null) {
                synchronized (graph){
                    getParentFiles(res, keyTo);
                    if (res.size()==0) {
                        res.add(keyTo);
                    }
                    List<CsmUID<CsmFile>> list = new ArrayList<CsmUID<CsmFile>>(res);
                    res.clear();
                    for(CsmUID<CsmFile> uid : list){
                        NodeLink link = graph.get(uid);
                        if (link != null && link.getInLinks().size()==0){
                            res.add(uid);
                        }
                    }
                }
            }
            return convertToFiles(res);
        } else {
            Set<CsmFile> res = new HashSet<CsmFile>();
            synchronized (graphOld){
                getParentFiles(res, start);
                List<CsmFile> list = new ArrayList<CsmFile>(res);
                if (res.size()==0) {
                    res.add(start);
                }
                res.clear();
                for(CsmFile uid : list){
                    NodeLinkOld link = graphOld.get(uid);
                    if (link != null && link.getInLinks().size()==0){
                        res.add(uid);
                    }
                }
            }
            return res;
        }
    }
    
    /**
     * gets all files that include this file.
     */
    public Set<CsmFile> getCoherenceFiles(CsmFile start){
        CsmProject project = start.getProject();
        if (TraceFlags.USE_REPOSITORY) {
            Set<CsmUID<CsmFile>> res = new HashSet<CsmUID<CsmFile>>();
            CsmUID<CsmFile> keyTo = UIDCsmConverter.fileToUID(start);
            if (keyTo != null) {
                synchronized (graph){
                    getParentFiles(res, keyTo);
                    if (res.size()==0) {
                        res.add(keyTo);
                    }
                    for(CsmUID<CsmFile> uid : new ArrayList<CsmUID<CsmFile>>(res)){
                        getIncludedFiles(res, uid);
                    }
                }
            }
            return convertToFiles(res);
        } else {
            Set<CsmFile> res = new HashSet<CsmFile>();
            synchronized (graphOld){
                getParentFiles(res, start);
                if (res.size()==0) {
                    res.add(start);
                }
                for(CsmFile uid : new ArrayList<CsmFile>(res)){
                    getIncludedFiles(res, uid);
                }
            }
            return res;
        }
    }
    
    
    private Set<CsmFile> convertToFiles(Set<CsmUID<CsmFile>> res) {
        Set<CsmFile> res2= new HashSet<CsmFile>();
        for(CsmUID<CsmFile> uid : res) {
            CsmFile file = UIDCsmConverter.UIDtoFile(uid);
            if (file != null) {
                res2.add(file);
            }
        }
        return res2;
    }
    
    /*
     * method called in synchronized block
     */
    private void getIncludedFiles(Set<CsmUID<CsmFile>> res, CsmUID<CsmFile> keyFrom){
        NodeLink node = graph.get(keyFrom);
        if (node != null) {
            for(CsmUID<CsmFile> uid : node.getOutLinks()){
                if (!res.contains(uid)){
                    res.add(uid);
                    getIncludedFiles(res, uid);
                }
            }
        }
    }
    
    /*
     * method called in synchronized block
     */
    private void getIncludedFiles(Set<CsmFile> res, CsmFile keyFrom){
        NodeLinkOld node = graphOld.get(keyFrom);
        if (node != null) {
            for(CsmFile uid : node.getOutLinks()){
                if (!res.contains(uid)){
                    res.add(uid);
                    getIncludedFiles(res, uid);
                }
            }
        }
    }
    
    /*
     * method called in synchronized block
     */
    private void getParentFiles(Set<CsmUID<CsmFile>> res, CsmUID<CsmFile> keyTo){
        NodeLink node = graph.get(keyTo);
        if (node != null) {
            for(CsmUID<CsmFile> uid : node.getInLinks()){
                if (!res.contains(uid)){
                    res.add(uid);
                    getParentFiles(res, uid);
                }
            }
        }
    }
    
    /*
     * method called in synchronized block
     */
    private void getParentFiles(Set<CsmFile> res, CsmFile keyTo){
        NodeLinkOld node = graphOld.get(keyTo);
        if (node != null) {
            for(CsmFile uid : node.getInLinks()){
                if (!res.contains(uid)){
                    res.add(uid);
                    getParentFiles(res, uid);
                }
            }
        }
    }
    
    public void clear() {
        if (TraceFlags.USE_REPOSITORY) {
            synchronized (graph){
                graph.clear();
            }
	    put();
        } else {
            synchronized (graphOld){
                graphOld.clear();
            }
        }
    }

    public void write(DataOutput output) throws IOException {
	super.write(output);
	synchronized (graph){
	    writeUIDToNodeLinkMap(output, graph);
	}
    }
    
    private static void writeUIDToNodeLinkMap (
            final DataOutput output, final Map<CsmUID<CsmFile>,NodeLink> aMap) throws IOException {
        
        assert output != null;
        assert aMap != null;
        
        UIDObjectFactory uidFactory = UIDObjectFactory.getDefaultFactory();
        assert uidFactory != null;
        
        output.writeInt(aMap.size());
        
        final Set<Entry<CsmUID<CsmFile>,NodeLink>> entrySet = aMap.entrySet();
        final Iterator<Entry<CsmUID<CsmFile>,NodeLink>> setIterator = entrySet.iterator();
        
        while (setIterator.hasNext()) {
            final Entry<CsmUID<CsmFile>,NodeLink> anEntry = setIterator.next();
            assert anEntry != null;
            
            uidFactory.writeUID(anEntry.getKey(), output);
            anEntry.getValue().write(output);
        }
    }    
    
    private static void readUIDToNodeLinkMap (
            final DataInput input, Map<CsmUID<CsmFile>,NodeLink> aMap) throws IOException {
        
        assert input != null;
        assert aMap != null;
        UIDObjectFactory uidFactory = UIDObjectFactory.getDefaultFactory();
        assert uidFactory != null;
        
        aMap.clear();
        
        final int size = input.readInt();
        
        for (int i = 0; i < size; i++) {
            final CsmUID<CsmFile> uid = uidFactory.readUID(input);
            final NodeLink        link = new NodeLink(input);
            
            assert uid != null;
            assert link != null;
            
            aMap.put(uid, link);
        }
        
    }
    
    private Map<CsmUID<CsmFile>,NodeLink> graph;
    private Map<CsmFile,NodeLinkOld> graphOld;
    
    private static class NodeLink implements SelfPersistent, Persistent {
        Set<CsmUID<CsmFile>> in = new HashSet<CsmUID<CsmFile>>();
        Set<CsmUID<CsmFile>> out = new HashSet<CsmUID<CsmFile>>();
        private NodeLink(){
        }
        
        private NodeLink(final DataInput input) throws IOException {
            assert input != null;
            assert in != null;
            assert out != null;

            final UIDObjectFactory factory = UIDObjectFactory.getDefaultFactory();
            assert factory != null;
            
            factory.readUIDCollection(in, input);
            factory.readUIDCollection(out, input);
        }
        
        private void addInLink(CsmUID<CsmFile> inLink){
            in.add(inLink);
        }
        private void removeInLink(CsmUID<CsmFile> inLink){
            in.remove(inLink);
        }
        private Set<CsmUID<CsmFile>> getInLinks(){
            return in;
        }
        private void addOutLink(CsmUID<CsmFile> inLink){
            out.add(inLink);
        }
        private void removeOutLink(CsmUID<CsmFile> inLink){
            out.remove(inLink);
        }
        private Set<CsmUID<CsmFile>> getOutLinks(){
            return out;
        }

        public void write(final DataOutput output) throws IOException {
            assert output != null;
            assert in != null;
            assert out != null;
            
            final UIDObjectFactory factory = UIDObjectFactory.getDefaultFactory();
            assert factory != null;
            
            factory.writeUIDCollection(in, output, false);
            factory.writeUIDCollection(out, output, false);
        }
    }
    
    private static class NodeLinkOld{
        Set<CsmFile> in = new HashSet<CsmFile>();
        Set<CsmFile> out = new HashSet<CsmFile>();
        private NodeLinkOld(){
        }
        private void addInLink(CsmFile inLink){
            in.add(inLink);
        }
        private void removeInLink(CsmFile inLink){
            in.remove(inLink);
        }
        private Set<CsmFile> getInLinks(){
            return in;
        }
        private void addOutLink(CsmFile inLink){
            out.add(inLink);
        }
        private void removeOutLink(CsmFile inLink){
            out.remove(inLink);
        }
        private Set<CsmFile> getOutLinks(){
            return out;
        }
    }
}
