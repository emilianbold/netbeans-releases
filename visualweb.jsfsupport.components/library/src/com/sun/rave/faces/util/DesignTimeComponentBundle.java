/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

/*
 * Created on Oct 19, 2004
 *
 */
package com.sun.rave.faces.util;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * @author Matt
 */
public class DesignTimeComponentBundle extends ComponentBundle {
	
    private static final boolean isDebugOn = Boolean.getBoolean ("org.openide.util.NbBundle.DEBUG"); // NOI18N;
	
	public void init(String baseName, ClassLoader classLoader) {
        super.init(baseName, isDebugOn ? DebugLoader.get(classLoader) : classLoader);
	}
	
    /**
     * This code CLONED from org.openide.util.NbBundle in order to not have to have
     * a direct reference to it.
     * Search for // RAVE in this inner class for differences from cloned code.
     * 
     * Classloader whose special trick is inserting debug information
     * into any *.properties files it loads.
     */
    private static final class DebugLoader extends ClassLoader {
    
        /** global bundle index, each loaded bundle gets its own */
        private static int count = 0;
    
        /** indices of known bundles; needed since DebugLoader's can be collected
         * when softly reachable, but this should be transparent to the user
         */
        private static final Map knownIDs = new HashMap (); // Map<String,int>
    
        /** cache of existing debug loaders for regular loaders */
        private static final Map existing = new WeakHashMap (); // Map<ClassLoader,Reference<DebugLoader>>
    
        private static int getID (String name) {
            synchronized (knownIDs) {
                Integer i = (Integer) knownIDs.get (name);
                if (i == null) {
                    i = new Integer (++count);
                    knownIDs.put (name, i);
                    // RAVE changed NbBundle to ComponentBundle
                    System.err.println ("ComponentBundle trace: #" + i + " = " + name); // NOI18N
                }
                return i.intValue ();
            }
        }
    
        public static ClassLoader get (ClassLoader normal) {
            //System.err.println("Lookup: normal=" + normal);
            synchronized (existing) {
                Reference r = (Reference) existing.get (normal);
                if (r != null) {
                    ClassLoader dl = (ClassLoader) r.get ();
                    if (dl != null) {
                        //System.err.println("\tcache hit");
                        return dl;
                    } else {
                        //System.err.println("\tcollected ref");
                    }
                } else {
                    //System.err.println("\tnot in cache");
                }
                ClassLoader dl = new DebugLoader (normal);
                existing.put (normal, new WeakReference(dl));
                return dl;
            }
        }
    
        private DebugLoader (ClassLoader cl) {
            super (cl);
            //System.err.println ("new DebugLoader: cl=" + cl);
        }
    
        public InputStream getResourceAsStream (String name) {
            InputStream base = super.getResourceAsStream (name);
            if (base == null) return null;
            if (name.endsWith (".properties")) { // NOI18N
                int id = getID (name);
                //System.err.println ("\tthis=" + this + " parent=" + getParent ());
                boolean loc = name.indexOf ("/Bundle.") != -1 || name.indexOf ("/Bundle_") != -1; // NOI18N
                // RAVE - added to support Bundle-JSF.properties files :)
                loc |= name.matches(".*/Bundle.*\\.properties");
                return new DebugInputStream (base, id, loc);
            } else {
                return base;
            }
        }
    
        // [PENDING] getResource not overridden; but ResourceBundle uses getResourceAsStream anyhow
    
        /** Wrapper input stream which parses the text as it goes and adds annotations.
         * Resource-bundle values are annotated with their current line number and also
         * the supplied it, so e.g. if in the original input stream on line 50 we have:
         *   somekey=somevalue
         * so in the wrapper stream (id 123) this line will read:
         *   somekey=somevalue (123:50)
         * Since you see on stderr what #123 is, you can then pinpoint where any bundle key
         * originally came from, assuming NbBundle loaded it from a *.properties file.
         * @see {@link Properties#load} for details on the syntax of *.properties files.
         */
        private static final class DebugInputStream extends InputStream {
            protected static final HashSet debugIgnoreKeySet = new HashSet();
    
            static {
                debugIgnoreKeySet.add("currentVersion"); // NOI18N
                debugIgnoreKeySet.add("SplashRunningTextBounds"); // NOI18N
                debugIgnoreKeySet.add("SplashProgressBarBounds"); // NOI18N
                debugIgnoreKeySet.add("SplashRunningTextColor"); // NOI18N
                debugIgnoreKeySet.add("SplashProgressBarColor"); // NOI18N
                debugIgnoreKeySet.add("SplashProgressBarColor"); // NOI18N
                debugIgnoreKeySet.add("SplashProgressBarEdgeColor"); // NOI18N
                debugIgnoreKeySet.add("SplashProgressBarCornerColor"); // NOI18N
                debugIgnoreKeySet.add("SplashRunningTextFontSize"); // NOI18N
                debugIgnoreKeySet.add("SPLASH_WIDTH"); // NOI18N
                debugIgnoreKeySet.add("SPLASH_HEIGHT"); // NOI18N
                debugIgnoreKeySet.add("SplashShowProgressBar"); // NOI18N
                debugIgnoreKeySet.add("WelcomeLabelFontSize"); // NOI18N
                debugIgnoreKeySet.add("WelcomeLabelLine2FontSize"); // NOI18N
                debugIgnoreKeySet.add("OpenIDE-Module-Display-Category"); // NOI18N
                debugIgnoreKeySet.add("LBL_WebAppAppNameStub"); // NOI18N
                debugIgnoreKeySet.add("FOLDER_RaveProjects"); // NOI18N
            }
    
            private final InputStream base;
            private final int id;
            private final boolean localizable;
            /** current line number */
            private int line = 0;
            /** state transition diagram constants */
            private static final int
                WAITING_FOR_KEY = 0,
                IN_COMMENT = 1,
                IN_KEY = 2,
                IN_KEY_BACKSLASH = 3,
                AFTER_KEY = 4,
                WAITING_FOR_VALUE = 5,
                IN_VALUE = 6,
                IN_VALUE_BACKSLASH = 7;
            /** current state in state machine */
            private int state = WAITING_FOR_KEY;
            /** if true, the last char was a CR, waiting to see if we get a NL too */
            private boolean twixtCrAndNl = false;
            /** if non-null, a string to serve up before continuing (length must be > 0) */
            private String toInsert = null;
            /** if true, the next value encountered should be localizable if normally it would not be, or vice-versa */
            private boolean reverseLocalizable = false;
            /** text of currently read comment, including leading comment character */
            private StringBuffer lastComment = null;
            /** text of currently read key */
            private StringBuffer lastKey = null;
    
            /** Create a new InputStream which will annotate resource bundles.
             * Bundles named Bundle*.properties will be treated as localizable by default,
             * and so annotated; other bundles will be treated as nonlocalizable and not annotated.
             * Messages can be individually marked as localizable or not to override this default,
             * in accordance with some I18N conventions for NetBeans.
             * @param base the unannotated stream
             * @param id an identifying number to use in annotations
             * @param localizable if true, this bundle is expected to be localizable
             * @see http://www.netbeans.org/i18n/
             */
            public DebugInputStream (InputStream base, int id, boolean localizable) {
                this.base = base;
                this.id = id;
                this.localizable = localizable;
            }
    
            public int read () throws IOException {
                //try{
                if (toInsert != null) {
                    char result = toInsert.charAt (0);
                    if (toInsert.length () > 1) {
                        toInsert = toInsert.substring (1);
                    } else {
                        toInsert = null;
                    }
                    return result;
                }
                int next = base.read ();
                if (next == '\n') {
                    twixtCrAndNl = false;
                    line++;
                } else if (next == '\r') {
                    if (twixtCrAndNl) {
                        line++;
                    } else {
                        twixtCrAndNl = true;
                    }
                } else {
                    twixtCrAndNl = false;
                }
                switch (state) {
                case WAITING_FOR_KEY:
                    switch (next) {
                    case '#':
                    case '!':
                        state = IN_COMMENT;
                        lastComment = new StringBuffer ();
                        lastComment.append ((char) next);
                        return next;
                    case ' ':
                    case '\t':
                    case '\n':
                    case '\r':
                    case -1:
                        return next;
                    case '\\':
                        state = IN_KEY_BACKSLASH;
                        return next;
                    default:
                        state = IN_KEY;
                        lastKey = new StringBuffer ();
                        lastKey.append ((char) next);
                        return next;
                    }
                case IN_COMMENT:
                    switch (next) {
                    case '\n':
                    case '\r':
                        String comment = lastComment.toString ();
                        lastComment = null;
                        if (localizable && comment.equals ("#NOI18N")) { // NOI18N
                            reverseLocalizable = true;
                        } else if (localizable && comment.equals ("#PARTNOI18N")) { // NOI18N
                            System.err.println ("ComponentBundle WARNING (" + id + ":" + line + "): #PARTNOI18N encountered, will not annotate I18N parts"); // NOI18N
                            reverseLocalizable = true;
                        } else if (! localizable && comment.equals ("#I18N")) { // NOI18N
                            reverseLocalizable = true;
                        } else if (! localizable && comment.equals ("#PARTI18N")) { // NOI18N
                            System.err.println ("ComponentBundle WARNING (" + id + ":" + line + "): #PARTI18N encountered, will not annotate I18N parts"); // NOI18N
                            reverseLocalizable = false;
                        } else if ((localizable && (comment.equals ("#I18N") || comment.equals ("#PARTI18N"))) || // NOI18N
                                   (! localizable && (comment.equals ("#NOI18N") || comment.equals ("#PARTNOI18N")))) { // NOI18N
                            System.err.println ("ComponentBundle WARNING (" + id + ":" + line + "): incongruous comment " + comment + " found for bundle"); // NOI18N
                            reverseLocalizable = false;
                        }
                        state = WAITING_FOR_KEY;
                        return next;
                    default:
                        lastComment.append ((char) next);
                        return next;
                    }
                case IN_KEY:
                    switch (next) {
                    case '\\':
                        state = IN_KEY_BACKSLASH;
                        return next;
                    case ' ':
                    case '\t':
                        state = AFTER_KEY;
                        return next;
                    case '=':
                    case ':':
                        state = WAITING_FOR_VALUE;
                        return next;
                    case '\r':
                    case '\n':
                        state = WAITING_FOR_KEY;
                        return next;
                    default:
                        lastKey.append((char) next);
                        return next;
                    }
                case IN_KEY_BACKSLASH:
                    lastKey.append((char) next);
                    state = IN_KEY;
                    return next;
                case AFTER_KEY:
                    switch (next) {
                    case '=':
                    case ':':
                        state = WAITING_FOR_VALUE;
                        return next;
                    case '\r':
                    case '\n':
                        state = WAITING_FOR_KEY;
                        return next;
                    default:
                        return next;
                    }
                case WAITING_FOR_VALUE:
                    switch (next) {
                    case '\r':
                    case '\n':
                        state = WAITING_FOR_KEY;
                        return next;
                    case ' ':
                    case '\t':
                        return next;
                    case '\\':
                        state = IN_VALUE_BACKSLASH;
                        return next;
                    default:
                        state = IN_VALUE;
                        return next;
                    }
                case IN_VALUE:
                    switch (next) {
                    case '\\':
                        // Gloss over distinction between simple escapes and \u1234, which is not important for us.
                        // Also no need to deal specially with continuation lines; for us, there is an escaped
                        // newline, after which will be more value, and that is all that is important.
                        state = IN_VALUE_BACKSLASH;
                        return next;
                    case '\n':
                    case '\r':
                        // End of value. This is the tricky part.
                        if (!reverseLocalizable) {
                            String key = lastKey.toString();
                            reverseLocalizable = debugIgnoreKeySet.contains(key);
                        }
                        boolean revLoc = reverseLocalizable;
                        reverseLocalizable = false;
                        state = WAITING_FOR_KEY;
                        // XXX don't annotate keys ending in _Mnemonic
                        if (localizable ^ revLoc) {
                            // This value is intended to be localizable. Annotate it.
                            // RAVE - changed "(" to "[" and ")" to "]"
                            toInsert = "[" + id + ":" + line + "]" + new Character ((char) next); // NOI18N
                            // Now return the space before the rest of the string explicitly.
                            return ' ';
                        } else {
                            // This is not supposed to be a localizable value, leave it alone.
                            return next;
                        }
                    default:
                        return next;
                    }
                case IN_VALUE_BACKSLASH:
                    state = IN_VALUE;
                    return next;
                default:
                    throw new IOException ("should never happen"); // NOI18N
                }
            }
            //catch(IOException ioe) {ioe.printStackTrace(); throw ioe;}
            //catch(RuntimeException re) {re.printStackTrace(); throw re;}
            //}
    
            /** For testing correctness of the transformation. Run:
             * java org.openide.util.NbBundle$DebugLoader$DebugInputStream true < test.properties
             * (The argument says whether to treat the input as localizable by default.)
             */
            public static void main (String[] args) throws Exception {
                if (args.length != 1) throw new Exception ();
                boolean loc = Boolean.valueOf (args[0]).booleanValue ();
                DebugInputStream dis = new DebugInputStream (System.in, 123, loc);
                int c;
                while ((c = dis.read ()) != -1) {
                    System.out.write (c);
                }
            }
    
        }
    
    }	

}
