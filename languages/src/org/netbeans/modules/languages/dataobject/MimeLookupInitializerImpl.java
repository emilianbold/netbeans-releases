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

package org.netbeans.modules.languages.dataobject;

import org.netbeans.modules.languages.LanguagesManager;
import org.netbeans.modules.languages.dataobject.LanguagesEditorKit;
import org.netbeans.modules.languages.features.CompletionProviderImpl;
import org.netbeans.modules.languages.features.GLFHighlightsLayerFactory;
import org.netbeans.spi.editor.mimelookup.MimeLookupInitializer;
import org.openide.util.Lookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.Lookups;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.modules.languages.features.BraceHighlighting;
import org.netbeans.modules.languages.features.IndentFactory;
import org.netbeans.spi.editor.bracesmatching.BracesMatcherFactory;


/**
 *
 *  @author Jan Jancura
 */
public class MimeLookupInitializerImpl implements MimeLookupInitializer {

    private String[]                    mimeTypes;
    private Map<String[],Lookup.Result> children = new HashMap<String[],Lookup.Result> ();
    
    public MimeLookupInitializerImpl () {
        this (new String [0]);
    }
    
    public MimeLookupInitializerImpl (String[] mimeTypes) {
        this.mimeTypes = mimeTypes;
//        if (mimeTypes.length == 0)
//            S ystem.out.println("new MimeLookupInitializerImpl []");
//        else 
//        if (mimeTypes.length == 1)
//            S ystem.out.println("new MimeLookupInitializerImpl " + mimeTypes [0]);
//        else 
//            S ystem.out.println("new MimeLookupInitializerImpl " + mimeTypes);
    }
    
    /**
     * Lookup providing mime-type sensitive or global-level data
     * depending on which level this initializer is defined.
     * 
     * @return Lookup or null, if there are no lookup-able objects for mime or global level.
     */
    public Lookup lookup () {
	return _lookup ();
    }
    
    /**
     * Retrieves a Lookup.Result of MimeLookupInitializers for the given sub-mimeType.
     *
     * @param mimeType mime-type string representation e.g. "text/x-java"
     * @return non-null lookup result of MimeLookupInitializer(s).
     *  <br/>
     *  Typically there should be just one child initializer although if there
     *  will be more than one all of them will be taken into consideration.
     *  <br/>
     *  If there will be no specific initializers for the particular mime-type
     *  then an empty result should be returned.
     */
    public Lookup.Result child (String mimeType) {
        synchronized (children){
            String[] newMimeType = new String [mimeTypes.length + 1];
            System.arraycopy (mimeTypes, 0, newMimeType, 0, mimeTypes.length);
            newMimeType [mimeTypes.length] = mimeType;
            Lookup.Result child = children.get (newMimeType);
            if (child == null){
                child = Lookups.fixed (
                    new Object[] {
                        new MimeLookupInitializerImpl (newMimeType)
                    }).<MimeLookupInitializerImpl>lookup (
                        new Lookup.Template<MimeLookupInitializerImpl> (MimeLookupInitializerImpl.class)
                    );
                children.put (newMimeType, child);
            }
            return child;
        }
    }
    
    private Lookup lookup;
    private Lookup _lookup () {
        if (lookup == null) {
            if (mimeTypes.length != 1){
                lookup  = Lookup.EMPTY;
                return lookup;
            }
            if (LanguagesManager.getDefault ().isSupported (mimeTypes [0])) {
                lookup = Lookups.fixed (
                    new Integer[] {
                        new Integer (1), 
                        new Integer (2), 
//                        new Integer (3), 
//                        new Integer (4),
                        new Integer (5),
                        new Integer (6),
                        new Integer (7)
                    },
                    new InstanceContent.Convertor<Integer,Object> () {
                        public Object convert (Integer i) {
                            switch (i.intValue ()) {
                                case 1:
                                    //S ystem.out.println("get LanguagesEditorKit for " + mimeTypes [0]);
//                                    MClassLoader mcl = new MClassLoader (getClass ().getClassLoader ());
//                                    try {
//                                        Class cl = mcl.loadClass (LanguagesEditorKit.class.getName () + "Virtual");
//                                        Constructor constructor = cl.getConstructor (new Class[] {String.class});
//                                        return constructor.newInstance (new Object[] {mimeTypes [0]});
//                                    } catch (Exception ex) {
//                                        ErrorManager.getDefault ().notify (ex);
//                                    }
                                    return new LanguagesEditorKit (mimeTypes [0]);
                                case 2:
                                    return new CompletionProviderImpl ();
//                                case 2:
//                                    S ystem.out.println("get LanguagesOptions for " + mimeTypes [0]);
//                                    return new LanguagesOptions ();
//                                case 3:
//                                    //S ystem.out.println("get LanguagesFoldManager.Factory for " + mimeTypes [0]);
//                                    //return new LanguagesFoldManager.Factory (mimeTypes [0]);
//                                    return null;
//                                case 4:
//                                    //S ystem.out.println("get CodeFoldingSideBarFactory for " + mimeTypes [0]);
//                                    return new CodeFoldingSideBarFactory ();
                                case 5:
                                    return new BraceHighlighting (mimeTypes[0]);
                                case 6:
                                    return new IndentFactory ();
                                case 7:
                                    return new GLFHighlightsLayerFactory ();
                            }
                            return null;
                        }
                        public Class<? extends Object> type (Integer i) {
                            switch (i.intValue ()) {
                                case 1:
                                    return LanguagesEditorKit.class;
                                case 2:
                                    return CompletionProviderImpl.class;
//                                case 2:
//                                    return LanguagesOptions.class;
//                                case 3:
//                                    return LanguagesFoldManager.Factory.class;
//                                case 4:
//                                    return CodeFoldingSideBarFactory.class;
                                case 5:
                                    return BracesMatcherFactory.class;
                                case 6:
                                    return IndentFactory.class;
                                case 7:
                                    return GLFHighlightsLayerFactory.class;
                            }
                            return null;
                        }
                        public String id (Integer i) {
                            return i.toString ();
                        }
                        public String displayName (Integer i) {
                            return i.toString ();
                        }
                    }
                );
            }
        }
        return lookup;
    }
    
//    static class MClassLoader extends ClassLoader {
//        MClassLoader (ClassLoader cl) {
//            super (cl);
//            ClassLoader cld = (ClassLoader) Lookup.getDefault ().lookup (ClassLoader.class);
//            S ystem.out.println("ClassLoader " + cld);
//        }
//        
//        public Class findClass (String name) throws ClassNotFoundException {
//            if (!name.endsWith ("Virtual")) return null;
//            String file = "/" + name.substring (0, name.length () - 7).replaceAll ("\\.", "/") + ".class";
//            InputStream is = getClass ().getResourceAsStream (file);
//            if (is == null) throw new ClassNotFoundException (name);
//            byte[] b = new byte [4000];
//            int off = 0;
//            try {
//                do {
//                    do {
//                        int l = is.read (b, off, b.length - off);
//                        if (l == -1) break;
//                        off += l;
//                    } while (off < b.length);
//                    if (off < b.length) {
//                        byte[] nb = new byte [off];
//                        System.arraycopy (b, 0, nb, 0, off);
//                        b = nb;
//                        break;
//                    }
//                    byte[] nb = new byte [b.length * 2];
//                    System.arraycopy (b, 0, nb, 0, off);
//                    b = nb;
//                } while (true);
//                return defineClass (name.substring (0, name.length () - 7), b, 0, b.length);
//            } catch (IOException ex) {
//                ex.printStackTrace ();
//                return null;
//            }
//        }
//    }
}
