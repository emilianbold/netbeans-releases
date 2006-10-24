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

package org.netbeans.modules.java.source.usages;
import javax.management.NotCompliantMBeanException;
import javax.management.StandardMBean;
import org.apache.lucene.index.IndexWriter;
import org.openide.ErrorManager;

/**
 *
 * @author Tomas Zezula
 */
public class LuceneIndexMBeanImpl extends StandardMBean implements LuceneIndexMBean {

    private static LuceneIndexMBeanImpl instance;

    private int mergeFactor = IndexWriter.DEFAULT_MERGE_FACTOR;
    private int maxMergeDocs = IndexWriter.DEFAULT_MAX_MERGE_DOCS;
    private int maxBufferedDocs = IndexWriter.DEFAULT_MAX_BUFFERED_DOCS;

    private LuceneIndexMBeanImpl () throws NotCompliantMBeanException {
        super (LuceneIndexMBean.class);
    }
    
    public int getMergeFactor () {
        return this.mergeFactor;
    }
    
    public void setMergeFactor (int mf) {
        this.mergeFactor = mf;
    }
    
    public int getMaxMergeDocs () {
        return this.maxMergeDocs;
    }
    
    public void setMaxMergeDocs (int nd) {
        this.maxMergeDocs = nd;
    }
    
    public int getMaxBufferedDocs () {
        return this.maxBufferedDocs;
    }
    
    public void setMaxBufferedDocs (int nd) {
        this.maxBufferedDocs = nd;
    }
    
    public static synchronized LuceneIndexMBeanImpl getDefault () {
        if (instance == null) {
            try {
                instance = new LuceneIndexMBeanImpl ();            
            } catch (NotCompliantMBeanException e) {
                ErrorManager.getDefault().notify(e);                
            }
        }
        return instance;
    }
    
}
