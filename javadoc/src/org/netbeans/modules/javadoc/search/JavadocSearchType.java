/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/*
 * JavadocSearchType.java
 *
 * Created on 19. únor 2001, 16:27
 */

package org.netbeans.modules.javadoc.search;

import java.util.*;
import java.io.File;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.ServiceType;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.ErrorManager;

import org.apache.regexp.RE;
import org.apache.regexp.REProgram;
import org.apache.regexp.RESyntaxException;
import org.apache.regexp.RECompiler;

/**
 *
 * @author  Petr Suchomel
 * @version 1.1
 */
public abstract class JavadocSearchType extends ServiceType {

    /** generated Serialized Version UID */
    static final long serialVersionUID =-7643543247564581246L;

    /** default returns null, must be overriden
     * @param fs File system where to find index files
     * @param rootOffset offset , position of index files in file system, normally null
     * @return File object containing index-files
    */
    public abstract FileObject getDocFileObject( FileSystem fs , String rootOffset );
    
    private RE[]  overviewLabelFilters;

    private synchronized void prepareOverviewFilter() {
        if (overviewLabelFilters != null)
            return;
        String filter = NbBundle.getMessage(getClass(), "FILTER_OverviewIndiceLabel");
        StringTokenizer tok = new StringTokenizer(filter, "\n");
        LinkedList ll = new LinkedList();
        for (int i = 0; tok.hasMoreTokens(); i++) {
            try {
                RECompiler rec = new RECompiler();
                String expr = tok.nextToken();
                REProgram rep = rec.compile(expr);
                RE re = new RE(rep, RE.MATCH_CASEINDEPENDENT);
                ll.add(re);
            } catch (RESyntaxException e) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            }
        }
        overviewLabelFilters = (RE[])ll.toArray(new RE[ll.size()]);
    }
    
    /**
     * This method is supposed to strip generic parts ("Overview (...)" or "... - Overview")
     * from the overview page's title. The default implementation does nothing,
     * returns the title unfiltered.
     *
     * @since
     */
    public String getOverviewTitleBase(String overviewTitle) {
        prepareOverviewFilter();
        RE match = null;
        String t = overviewTitle.trim();
        
        for (int i = 0; i < overviewLabelFilters.length; i++) {
            if (overviewLabelFilters[i].match(t)) {
                match = overviewLabelFilters[i];
                break;
            }
        }
        if (match == null)
            return overviewTitle;
        return match.getParen(1);
    }

    /** Returns Java doc search thread for doument
     * @param toFind String to find
     * @param fo File object containing index-files
     * @param diiConsumer consumer for parse events
     * @return IndexSearchThread
     * @see IndexSearchThread
     */    
    public abstract IndexSearchThread getSearchThread( String toFind, FileObject fo, IndexSearchThread.DocIndexItemConsumer diiConsumer );
}
