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

package org.netbeans.modules.javadoc.search;
import java.beans.PropertyChangeEvent;

import java.beans.PropertyChangeListener;


import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import java.io.Reader;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import javax.swing.event.ChangeEvent;

import javax.swing.event.ChangeListener;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.HTML;
import javax.swing.text.html.parser.ParserDelegator;
import javax.swing.text.MutableAttributeSet;

import org.netbeans.api.java.classpath.ClassPath;

import org.openide.filesystems.FileObject;

import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.api.java.classpath.GlobalPathRegistryEvent;

import org.netbeans.api.java.classpath.GlobalPathRegistryListener;

import org.netbeans.api.java.queries.JavadocForBinaryQuery;
import org.openide.ErrorManager;
import org.openide.filesystems.URLMapper;
import org.openide.util.Lookup;

/**
 * Class which is able to serve index files of Javadoc for all
 * currently used Javadoc documentation sets.
 * @author Petr Hrebejk
 */
public class JavadocRegistry implements GlobalPathRegistryListener, ChangeListener, PropertyChangeListener  {
        
    private static JavadocRegistry INSTANCE;

    
    private GlobalPathRegistry regs;    
    private ArrayList listeners;
    private Set/*<JavadocForBinaryQuery.Result>*/ results;
    private Set/*ClassPath*/ classpaths;
    private FileObject[] roots;
    
    /** Creates a new instance of JavadocRegistry */
    private JavadocRegistry() {
        this.regs = GlobalPathRegistry.getDefault ();        
        this.regs.addGlobalPathRegistryListener(this);
    }
    
    public static synchronized JavadocRegistry getDefault() {
        if ( INSTANCE == null ) {
            INSTANCE = new JavadocRegistry();
        }
        return INSTANCE;
    }

    /** Returns Array of the Javadoc Index roots
     */
    public FileObject[] getDocRoots() {
        synchronized (this) {
            if (this.roots != null) {
                return this.roots;
            }
        }        
        //XXX must be called out of synchronized block to prevent
        // deadlock. throwCache is called under the ProjectManager.mutex
        // write lock and Project's SFBQI requires the ProjectManager.mutex readLock
        Set/*<ClassPath>*/ _classpaths = new HashSet/*<ClassPath>*/();
        Set/*<JavadocForBinaryQuery.Result>*/  _results = new HashSet/*<JavadocForBinaryQuery.Result>*/();
        Set/*<FileObject>*/ s = readRoots(this, _classpaths, _results);
        synchronized (this) {
            if (this.roots == null) {
                this.roots = new FileObject[ s.size() ];
                s.toArray( this.roots );
                this.classpaths = _classpaths;
                this.results = _results;
                registerListeners(this, _classpaths, _results);
            }
            return this.roots;
        }
    }
    
    
    public JavadocSearchType findSearchType( FileObject apidocRoot ) {
        String encoding = getDocEncoding (apidocRoot);
        Lookup.Result result = Lookup.getDefault().lookup(new Lookup.Template(JavadocSearchType.class));
        for (Iterator it = result.allInstances().iterator(); it.hasNext();) {
            JavadocSearchType jdst = (JavadocSearchType) it.next ();
            if (jdst.accepts(apidocRoot, encoding)) {
                return jdst;
            }
        }        
        return null;
    }    
        
    // Private methods ---------------------------------------------------------
    
    private static Set/*<FileObject>*/ readRoots(
            JavadocRegistry jdr,
            Set/*<ClassPath>*/ classpaths,
            Set/*<JavadocForBinaryQuery.Result>*/ results) {
        
        Set roots = new HashSet ();
        List paths = new LinkedList();
        paths.addAll( jdr.regs.getPaths( ClassPath.COMPILE ) );        
        paths.addAll( jdr.regs.getPaths( ClassPath.BOOT ) );
        for( Iterator it = paths.iterator(); it.hasNext(); ) {
            ClassPath ccp = (ClassPath)it.next();
            classpaths.add (ccp);
            //System.out.println("CCP " + ccp );
            FileObject ccpRoots[] = ccp.getRoots();
            
            for( int i = 0; i < ccpRoots.length; i++ ) {
                //System.out.println(" CCPR " + ccpRoots[i]);
                JavadocForBinaryQuery.Result result = JavadocForBinaryQuery.findJavadoc( URLMapper.findURL(ccpRoots[i], URLMapper.EXTERNAL ) );
                results.add (result);
                URL[] jdRoots = result.getRoots();                    
                for ( int j = 0; j < jdRoots.length; j++ ) {
                    //System.out.println( "  JDR " + jdRoots[j] );
                    //System.out.println("Looking for root of: "+jdRoots[j]);
                    FileObject fo = URLMapper.findFileObject(jdRoots[j]);
                    //System.out.println("Found: "+fo);
                    if (fo != null) {                        
                        roots.add(fo);
                    }
                }                                    
            }
        }
        //System.out.println("roots=" + roots);
        return roots;
    }
    
    private static void registerListeners(
            JavadocRegistry jdr,
            Set/*<ClassPath>*/ classpaths,
            Set/*<JavadocForBinaryQuery.Result>*/ results) {
        
        for (Iterator it = classpaths.iterator(); it.hasNext();) {
            ClassPath cpath = (ClassPath) it.next();
            cpath.addPropertyChangeListener(jdr);
        }
        for (Iterator it = results.iterator(); it.hasNext();) {
            JavadocForBinaryQuery.Result result = (JavadocForBinaryQuery.Result) it.next();
            result.addChangeListener(jdr);
        }
    }

    public void pathsAdded(GlobalPathRegistryEvent event) {
        this.throwCache ();
        this.fireChange ();
    }

    public void pathsRemoved(GlobalPathRegistryEvent event) {
        this.throwCache ();
        this.fireChange ();
    }
    
    public void propertyChange (PropertyChangeEvent event) {        
        if (ClassPath.PROP_ENTRIES.equals (event.getPropertyName())) {
            this.throwCache ();
            this.fireChange ();
        }
    }
    
    
    public void stateChanged(javax.swing.event.ChangeEvent e) {
        this.throwCache ();
        this.fireChange ();
    }

    
    
    public synchronized void addChangeListener (ChangeListener l) {
        assert l != null : "Listener can not be null.";     //NOI18N
        if (this.listeners == null) {
            this.listeners = new ArrayList ();
        }
        this.listeners.add (l);
    }
    
    public synchronized void removeChangeListener (ChangeListener l) {
        assert l != null : "Listener can not be null.";     //NOI18N
        if (this.listeners == null) {
            return;
        }
        this.listeners.remove (l);
    }
    
    private void fireChange () {
        Iterator it = null;
        synchronized (this) {
            if (this.listeners == null) {
                return;
            }
            it = ((ArrayList)this.listeners.clone()).iterator();
        }
        ChangeEvent event = new ChangeEvent (this);
        while (it.hasNext()) {
            ((ChangeListener)it.next()).stateChanged(event);
        }
    }    
    
    private synchronized void throwCache () {
        this.roots = null;
        //Unregister itself from classpaths, not interested in events
        for (Iterator it = this.classpaths.iterator(); it.hasNext();) {
            ClassPath cp = (ClassPath) it.next ();
            cp.removePropertyChangeListener(this);
            it.remove ();
        }
        //Unregister itself from results, not interested in events
        for (Iterator it = this.results.iterator(); it.hasNext();) {
            JavadocForBinaryQuery.Result result = (JavadocForBinaryQuery.Result) it.next ();
            result.removeChangeListener (this);
            it.remove ();
        }
    }


    private String getDocEncoding (FileObject root) {
         assert root != null && root.isFolder();
        FileObject fo = root.getFileObject("index-all.html");   //NOI18N
        if (fo == null) {
            fo = root.getFileObject("index-files"); //NOI18N
            if (fo == null) {
                return null;
            }
            fo = fo.getFileObject("index-1.html");  //NOI18N
            if (fo == null) {
                return null;
            }
        }
        ParserDelegator pd = new ParserDelegator();
        try {
            BufferedReader in = new BufferedReader( new InputStreamReader( fo.getInputStream () ));
            EncodingCallback ecb = new EncodingCallback (in);
            try {                
                pd.parse( in, ecb, true );                
            } catch (IOException ioe) {                
                //Do nothing
            } finally {
               in.close ();              
            }
            return ecb.getEncoding ();
        } catch (IOException ioe) {
            ErrorManager.getDefault().notify (ioe);
        }
        return null;
    }



    private static class EncodingCallback extends HTMLEditorKit.ParserCallback {


        private Reader in;
        private String encoding;

        public EncodingCallback (Reader in) {
            this.in = in;
        }


        public String getEncoding () {
            return this.encoding;
        }


        public void handleSimpleTag(HTML.Tag t, MutableAttributeSet a, int pos) {
            if (t == HTML.Tag.META) {
                String value = (String) a.getAttribute(HTML.Attribute.CONTENT);
                if (value != null) {
                    StringTokenizer tk = new StringTokenizer(value,";");
                    while (tk.hasMoreTokens()) {
                        String str = tk.nextToken().trim();
                        if (str.startsWith("charset")) {        //NOI18N
                            str = str.substring(7).trim();
                            if (str.charAt(0)=='=') {
                                this.encoding = str.substring(1).trim();
                                try {
                                    this.in.close();
                                } catch (IOException ioe) {/*Ignore it*/}
                                return;                                
                            }
                        }
                    }
                }
            }
        }

        public void handleStartTag(javax.swing.text.html.HTML.Tag t, javax.swing.text.MutableAttributeSet a, int pos) {
            if (t == HTML.Tag.BODY) {
                try {
                    this.in.close ();
                } catch (IOException ioe) {/*Ignore it*/}
            }
        }
    }

}
