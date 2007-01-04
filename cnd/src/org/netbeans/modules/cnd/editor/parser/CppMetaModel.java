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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.editor.parser;

import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.text.Document;

import org.openide.ErrorManager;
import org.openide.loaders.DataObject;
import org.openide.util.RequestProcessor;
import org.openide.util.Task;

public class CppMetaModel {

    // TODO: need to get reparse time from settings
    private int reparseDelay = 1000;
    
    /** map of all files we're interested in */
    private HashMap map = new HashMap();

    private ArrayList listeners = new ArrayList();

    private static CppMetaModel instance;

    private static RequestProcessor cppParserRP;

    private static final ErrorManager log = ErrorManager.getDefault().getInstance(
		"CppFoldTracer"); // NOI18N

    private CppMetaModel() {
	//log.log("CppMetaModel: Constructor");
    }

    public static CppMetaModel getDefault() {
	if (instance == null) {
	    instance = new CppMetaModel();
	}
	return instance;
    }

    // Helper methods for awhile...
    private static synchronized RequestProcessor getCppParserRP() {
	if (cppParserRP == null) {
	    cppParserRP = new RequestProcessor("CPP Parser", 1); // NOI18N
	}
	return cppParserRP;
    }

    // we need to provide mechanism for handling only most recent changes and 
    // reject the unnecessary ones, so cancel previous one and create new task 
    // using delay
    private RequestProcessor.Task task = null;
    public void scheduleParsing(final Document doc) {

	final String title = (String) doc.getProperty(Document.TitleProperty);
	log.log("CppMetaModel.scheduleParsing: Checking " + getShortName(doc) +
		" [" + Thread.currentThread().getName() + "]"); // NOI18N
	final CppFile file = (CppFile) map.get(title);
        // try to cancel task
        if (task != null) {
            task.cancel();
        }
	if (file == null) {
	    log.log("CppMetaModel.scheduleParsing: Starting initial parse for " +
			getShortName(doc));
	    task = getCppParserRP().post(new Runnable() {
		public void run() {
		    CppFile file = new CppFile(title);
		    map.put(title, file);
		    file.startParsing(doc);
                    fireObjectParsed(doc);
		}
	    }, reparseDelay);
	} else if (file.needsUpdate()) {
	    log.log("CppMetaModel.scheduleParsing: Starting update parse for " +
			getShortName(doc));
	    task = getCppParserRP().post(new Runnable() {
		public void run() {
		    file.startParsing(doc);
                    fireObjectParsed(doc);
		}
	    }, reparseDelay);
	} /*else {
	    DataObject dobj;
	    Object o = doc.getProperty(Document.StreamDescriptionProperty);
	    if (o instanceof DataObject) {
		dobj = (DataObject) o;
		log.log("CppMetaModel.scheduleParsing: Existing record for " + getShortName(doc));
	    }
	}*/
    }
    
    private void fireObjectParsed(Document doc)
    {
        synchronized (listeners)
        {
	    Object o = doc.getProperty(Document.StreamDescriptionProperty);
            DataObject dobj = (o instanceof DataObject) ? (DataObject) o : null;
//            for(Iterator it = listeners.iterator(); it.hasNext();)
//            {
//                ((ParsingListener)it.next()).objectParsed(new ParsingEvent(dobj));
//            }
            // vk++ had to change the code above to avoid concurrent modification exception
            ParsingListener[] alist = new ParsingListener[listeners.size()];
            listeners.toArray(alist);
            for (int i = 0; i < alist.length; i++) {
                alist[i].objectParsed(new ParsingEvent(dobj));
            }
            // vk--
            
        }        
    }

    private String getShortName(Document doc) {
	String longname = (String) doc.getProperty(Document.TitleProperty);
	int slash = longname.lastIndexOf(java.io.File.separatorChar);

	if (slash != -1) {
	    return longname.substring(slash + 1);
	} else {
	    return longname;
	}
    }

    public CppFile get(String key) {
	return (CppFile) map.get(key);
    }

    public void addParsingListener(ParsingListener listener) {
	//log.log("CppMetaModel: addParsingListener");
	synchronized (listeners) {
	    listeners.add(listener);
	}
    }

    public void removeParsingListener(ParsingListener listener) {
	//log.log("CppMetaModel: removeParsingListener");
	synchronized (listeners) {
	    listeners.remove(listener);
	}
    }

    private synchronized void fireParsingEvent(ParsingEvent evt) {
	for (int i = 0; i < listeners.size(); i++) {
	    ParsingListener listener = (ParsingListener) listeners.get(i);
	    listener.objectParsed(evt);
	}
    }
}
