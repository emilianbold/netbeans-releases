/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.ant.freeform;

import java.io.File;
import java.io.IOException;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.swing.event.ChangeListener;
import org.apache.tools.ant.module.api.AntProjectCookie;
import org.apache.tools.ant.module.api.support.TargetLister;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Miscellaneous utilities.
 * @author Jesse Glick
 */
public class Util {
    
    private Util() {}
    
    public static final ErrorManager err = ErrorManager.getDefault().getInstance("org.netbeans.modules.ant.freeform"); // NOI18N
    
    /**
     * Returns name of the Ant script represented by the given file object.
     * @param fo Ant script which name should be returned
     * @return name of the Ant script as specified in name attribute of
     *    project element or null if fo does not represent valid Ant script
     *    or the script is anonymous
     */
    public static String getAntScriptName(FileObject fo) {
        AntProjectCookie apc = getAntProjectCookie(fo);
        if (apc == null) {
            return null;
        }
        Element projEl = apc.getProjectElement();
        if (projEl == null) {
            return null;
        }
        String name = projEl.getAttribute("name"); // NOI18N
        // returns "" if no such attribute
        return name.length() > 0 ? name : null;
    }
    
    private static AntProjectCookie getAntProjectCookie(FileObject fo) {
        DataObject dob;
        try {
            dob = DataObject.find(fo);
        } catch (DataObjectNotFoundException ex) {
            Util.err.notify(ErrorManager.INFORMATIONAL, ex);
            return null;
        }
        assert dob != null;
        AntProjectCookie apc = (AntProjectCookie) dob.getCookie(AntProjectCookie.class);
        if (apc == null && fo.getMIMEType().equals("text/xml")) { // NOI18N
            // Some file that *could* be an Ant script and just wasn't recognized
            // as such? Cf. also TargetLister.getAntProjectCookie, which has the
            // advantage of being inside the Ant module and therefore able to
            // directly instantiate AntProjectSupport.
            try {
                apc = forceParse(fo);
            } catch (IOException e) {
                err.notify(ErrorManager.INFORMATIONAL, e);
            } catch (SAXException e) {
                err.log("Parse error in " + fo + ": " + e);
            }
        }
        return apc;
    }

    /**
     * Returns sorted list of targets name of the Ant script represented by the
     * given file object.
     * @param fo Ant script which target names should be returned
     * @return sorted list of target names or null if fo does not represent 
     * valid Ant script
     */
    public static List/*<String>*/ getAntScriptTargetNames(FileObject fo) {
        if (fo == null) {
            throw new IllegalArgumentException("Cannot call Util.getAntScriptTargetNames with null"); // NOI18N
        }
        AntProjectCookie apc = getAntProjectCookie(fo);
        if (apc == null) {
            return null;
        }
        Set/*TargetLister.Target*/ allTargets;
        try {
            allTargets = TargetLister.getTargets(apc);
        } catch (IOException e) {
            err.notify(ErrorManager.INFORMATIONAL, e);
            return null;
        }
        SortedSet targetNames = new TreeSet(Collator.getInstance());
        Iterator it = allTargets.iterator();
        while (it.hasNext()) {
            TargetLister.Target target = (TargetLister.Target) it.next();
            if (target.isOverridden()) {
                // Cannot call it directly.
                continue;
            }
            if (target.isInternal()) {
                // Should not be called from outside.
                continue;
            }
            targetNames.add(target.getName());
        }
        return new ArrayList(targetNames);
    }
    
    /**
     * Try to parse a (presumably XML) file even though it is not known to be an Ant script.
     */
    private static AntProjectCookie forceParse(FileObject fo) throws IOException, SAXException {
        Document doc = XMLUtil.parse(new InputSource(fo.getURL().toExternalForm()), false, true, new ErrH(), null);
        return new TrivialAntProjectCookie(fo, doc);
    }
    
    private static final class ErrH implements ErrorHandler {
        public ErrH() {}
        public void fatalError(SAXParseException exception) throws SAXException {
            throw exception;
        }
        public void error(SAXParseException exception) throws SAXException {
            throw exception;
        }
        public void warning(SAXParseException exception) throws SAXException {
            // ignore that
        }
    }
    
    private static final class TrivialAntProjectCookie implements AntProjectCookie.ParseStatus {
        
        private final FileObject fo;
        private final Document doc;
        
        public TrivialAntProjectCookie(FileObject fo, Document doc) {
            this.fo = fo;
            this.doc = doc;
        }

        public FileObject getFileObject() {
            return fo;
        }

        public File getFile() {
            return FileUtil.toFile(fo);
        }

        public Document getDocument() {
            return doc;
        }

        public Element getProjectElement() {
            return doc.getDocumentElement();
        }
        
        public boolean isParsed() {
            return true;
        }

        public Throwable getParseException() {
            return null;
        }

        public void addChangeListener(ChangeListener l) {}

        public void removeChangeListener(ChangeListener l) {}

    }
    
}
