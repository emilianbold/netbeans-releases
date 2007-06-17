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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.refactoring;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.spi.SimpleRefactoringElementImplementation;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.taglib.TLDDataObject;
import org.netbeans.modules.web.taglib.TLDLoader;
import org.netbeans.modules.web.taglib.model.Taglib;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.PositionBounds;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

/**
 * A base class for TLD refactorings.
 *
 * @author Erno Mononen
 */
public abstract class TldRefactoring implements WebRefactoring{
    
    public Problem preCheck() {
        return null;
    }
    
    protected List<TaglibHandle> getTaglibs(FileObject source){
        WebModule wm = WebModule.getWebModule(source);
        if (wm == null){
            return Collections.<TaglibHandle>emptyList();
        }
        FileObject webInf = wm.getWebInf();
        if (webInf == null){
            return Collections.<TaglibHandle>emptyList();
        }
        
        List<TaglibHandle> result = new ArrayList<TaglibHandle>();
        Enumeration<? extends FileObject> children = webInf.getChildren(true);
        while(children.hasMoreElements()){
            FileObject child = children.nextElement();
            Taglib taglib = getTaglib(child);
            if (taglib != null){
                result.add(new TaglibHandle(taglib, child));
            }
        }
        return result;
    }
    
    private boolean isTld(FileObject fo){
        return TLDLoader.tldExt.equalsIgnoreCase(fo.getExt());
    }
    
    private Taglib getTaglib(FileObject tld) {
        if (!isTld(tld)){
            return null;
        }
        DataObject tldData = null;
        try {
            tldData = DataObject.find(tld);
        } catch (DataObjectNotFoundException dne) {
            Exceptions.printStackTrace(dne);
        }
        Taglib result = null;
        if (tldData instanceof TLDDataObject) {
            try {
                result = ((TLDDataObject)tldData).getTaglib();
            } catch (IOException ioe) {
                Exceptions.printStackTrace(ioe);
            }
        }
        return result;
    }
    
    protected static class TaglibHandle {
        private final Taglib taglib;
        private final FileObject tldFile;
        
        public TaglibHandle(Taglib taglib, FileObject tldFile) {
            this.taglib = taglib;
            this.tldFile = tldFile;
        }
        
        public Taglib getTaglib() {
            return taglib;
        }
        
        public FileObject getTldFile() {
            return tldFile;
        }
        
    }
    
    
    protected abstract static class TldRefactoringElement extends SimpleRefactoringElementImplementation{
        
        protected final Taglib taglib;
        protected final FileObject tldFile;
        protected final String clazz;
        
        public TldRefactoringElement(String clazz, Taglib taglib, FileObject tldFile) {
            this.clazz = clazz;
            this.taglib = taglib;
            this.tldFile = tldFile;
        }
        
        public String getText() {
            return getDisplayText();
        }
        
        public Lookup getLookup() {
            return Lookup.EMPTY;
        }
        
        public FileObject getParentFile() {
            return tldFile;
        }
        
        public PositionBounds getPosition() {
            try {
                //XXX: does not work correctly when a class is specified more than once in one tld file
                return new PositionBoundsResolver(DataObject.find(tldFile), clazz).getPositionBounds();
            } catch (DataObjectNotFoundException ex) {
                Exceptions.printStackTrace(ex);
            }
            return null;
        }
        
        protected void write() {
            try {
                TLDDataObject tdo = (TLDDataObject) DataObject.find(tldFile);
                if (tdo != null) {
                    tdo.write(taglib);
                }
            } catch (IOException ioe) {
                Exceptions.printStackTrace(ioe);
            }
        }
        
        
    }
}
