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

package org.netbeans.modules.javadoc.search;

import java.util.*;
import java.util.regex.*;

import org.openide.filesystems.FileObject;
import org.openide.ServiceType;
import org.openide.util.NbBundle;
import org.openide.ErrorManager;

/**
 * Service that knows how to parse doclet output and can
 * search in it.
 *
 * @author  Petr Suchomel
 * @version 1.1
 */
public abstract class JavadocSearchType extends ServiceType {

    /** generated Serialized Version UID */
    static final long serialVersionUID =-7643543247564581246L;

    /** Tries to find javadoc index files in given directory
     * @param apidocRoot Folder where to look for index files where to find index files
     * @return File object containing index-files e.g index-files directory
     *         or index-all.html.
     */
    public abstract FileObject getDocFileObject( FileObject apidocRoot );
    
    private Pattern[]  overviewLabelFilters;

    private synchronized void prepareOverviewFilter() {
        if (overviewLabelFilters != null)
            return;
        String filter = NbBundle.getMessage(JavadocSearchType.class, "FILTER_OverviewIndiceLabel"); // NOI18N
        StringTokenizer tok = new StringTokenizer(filter, "\n"); // NOI18N
        LinkedList ll = new LinkedList();
        while (tok.hasMoreTokens()) {
            try {
                String expr = tok.nextToken();
                Pattern re = Pattern.compile(expr);
                ll.add(re);
            } catch (PatternSyntaxException e) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            }
        }
        overviewLabelFilters = (Pattern[])ll.toArray(new Pattern[ll.size()]);
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
        Matcher match;
        String t = overviewTitle.trim();
        
        for (int i = 0; i < overviewLabelFilters.length; i++) {
            match = overviewLabelFilters[i].matcher(t);
            if  (match.matches()) {
                return match.group(1);
            }
        }
        return overviewTitle;
    }

    /** Returns Java doc search thread for doument
     * @param toFind String to find
     * @param fo File object containing index-files
     * @param diiConsumer consumer for parse events
     * @return IndexSearchThread
     */    
    public abstract IndexSearchThread getSearchThread( String toFind, FileObject fo, IndexSearchThread.DocIndexItemConsumer diiConsumer );
    

    /**
     * Returns true if the JavadocSearchType accepts the given apidocRoot.
     * @param apidocRoot root of the javadoc
     * @param encoding of the javadoc, may be null if the javadoc has no encoding
     */
    public abstract boolean accepts (FileObject apidocRoot, String encoding);

}
