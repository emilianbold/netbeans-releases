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

package org.netbeans.modules.web.jsps.parserapi;

//import java.io.IOException;
//import java.util.*;
import org.netbeans.modules.web.jspparser.JspParserImpl;
//import org.openide.ErrorManager;
//import org.openide.cookies.InstanceCookie;
//import org.openide.loaders.*;
//import org.openide.loaders.FolderInstance;
//import org.openide.filesystems.*;
//import org.netbeans.api.registry.Context;
//import org.openide.util.NbBundle;

public class JspParserFactory /*extends FolderInstance*/ {
    
    private static JspParserAPI parser;

    //private static JspParserFactory parserFactory;
    
    /*public JspParserFactory(DataFolder folder) {
        super(folder);
    }*/

    public static synchronized JspParserAPI getJspParser() {
        if (parser == null) {
             parser = new JspParserImpl();
        }
        return parser;
    }
    
/*    protected Object createInstance(InstanceCookie[] cookies) {
        Collection l = new ArrayList(cookies.length);
        for (int i = 0; i < cookies.length; i++) {
            try {
                Object o = cookies[i].instanceCreate();
                if (o instanceof JspParserAPI) {
                    l.add(o);
                }
            } 
            catch (IOException ex) {
                ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, ex);
            }
            catch (ClassNotFoundException ex) {
                ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, ex);
            }
        }
        return l.toArray(new JspParserAPI[l.size()]);
    }
*/
     /*  if (parserFactory == null) {
            FileObject f = Repository.getDefault().findResource("/J2EE/JSPParser"); // NOI18N
            if (f != null) {
                try {
                    DataFolder folder = (DataFolder)DataObject.find(f).getCookie(DataFolder.class);
                    parserFactory = new JspParserFactory(folder);
                } 
                catch (DataObjectNotFoundException ex) {
                    ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, ex);
                }
            } else {
                ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, 
                    new Exception(NbBundle.getBundle(JspCompileUtil.class).getString("EXC_JspParserNotInstalled")));
            }
        }
        return (parserFactory == null) ? null : parserFactory.getJspParser0();
    }
    
    JspParserAPI getJspParser0() {
        if (parser == null) {
            try {
                JspParserAPI[] parsers = (JspParserAPI[])instanceCreate();
                if (parsers.length > 0) {
                    parser = parsers[0];
                }
            } 
            catch (IOException ex) {
                ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, ex);
            }
            catch (ClassNotFoundException ex) {
                ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, ex);
            }
        }
        return parser;
    }

    protected Object createInstance(InstanceCookie[] cookies) {
        Collection l = new ArrayList(cookies.length);
        for (int i = 0; i < cookies.length; i++) {
            try {
                Object o = cookies[i].instanceCreate();
                if (o instanceof JspParserAPI) {
                    l.add(o);
                }
            } 
            catch (IOException ex) {
                ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, ex);
            }
            catch (ClassNotFoundException ex) {
                ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, ex);
            }
        }
        return l.toArray(new JspParserAPI[l.size()]);
    }*/
    
}
