/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.bugtracking.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.SwingUtilities;
import javax.swing.text.StyledDocument;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.modules.bugtracking.BugtrackingManager;
import org.netbeans.modules.bugtracking.spi.VCSSupport;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.LineCookie;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.text.Line;
import org.openide.util.Lookup;

/**
 * Finds stacktraces in texts.
 *
 *  XXX Does not handle poorly formated stacktraces e.g.
 *  http://www.netbeans.org/issues/show_bug.cgi?id=100005&x=17&y=10
 *
 *  XXX: Needs to filter out indentical stacktrace hashes
 *
* @author Petr Hrebejk, Jan Stola, Tomas Stupka
 */
public class StackTraceSupport {

    private static final Pattern ST_PATTERN =
           Pattern.compile("([\\p{Alnum}\\.\\$_<>]*?)\\((?:Native Method|Unknown Source|Compiled Code|([\\p{Alnum}\\.\\$_]*?):(\\p{Digit}+?))\\)", Pattern.DOTALL);

    private StackTraceSupport() { }

    @SuppressWarnings("empty-statement")
    public static void findAndOpen(String text) {
        List<StackTracePosition> st = StackTraceSupport.find(text);
        for (StackTracePosition stp : st) {
            StackTraceElement ste = stp.getStackTraceElements()[0];
            String path = getPath(ste);
            open(path, ste.getLineNumber() - 1); // XXX -1 ???
            break;
        }
    }

    public static void findAndShowHistory(String text) {
        List<StackTracePosition> st = StackTraceSupport.find(text);
        for (StackTracePosition stp : st) {
            StackTraceElement ste = stp.getStackTraceElements()[0];
            String path = getPath(ste);
            openSearchHistory(path, ste.getLineNumber() - 1); // XXX -1 ???
            break;
        }
    }

    private static String getPath(StackTraceElement ste ) {
        String path = ste.getClassName();
        int index = path.indexOf('$');
        if (index != -1) {
            path = path.substring(0, index);
        }
        path = path.replace(".", "/") + ".java"; // XXX .java ???
        return path;
    }

    public static List<StackTracePosition> find(String text) {

       LinkedList<StackTracePosition> result = new LinkedList<StackTracePosition>();
       if ( text == null) {
           return result;
       }

       List<Integer> lineBreaks = new ArrayList<Integer>();
       int pos = -1;
       while( (pos = text.indexOf("\n", pos + 1)) > -1) {
           lineBreaks.add(pos);
       }

       String nt = removeAll( text, '\n');
       //String nt = text.replace('\n', ' ');

       Matcher m  = ST_PATTERN.matcher(nt);

       List<StackTraceElement> st = new ArrayList<StackTraceElement>();
       subs = new ArrayList<String>();
       int last = -1;       
       int start = -1;
       while( m.find() ) {
           if(start == -1) start = m.start();
           if ( !isStacktraceContinuation( nt, last, m.start() ) ) {
               StackTraceElement[] stArray = st.toArray(new StackTraceElement[0]);
               // Ignore zero line and one line stacktraces
               if ( stArray.length > 1 ) {
                   start = adjustFirstLinePosition(text, start);
                   result.add( new StackTracePosition(stArray, start, last) );
//                   if (result.size() > 50) {
//                       result.removeFirst(); // XXX WTF
//                   }
               }
               st = new ArrayList<StackTraceElement>();
               start = m.start();
               subs = new ArrayList<String>();
           }
           StackTraceElement ste = createStackTraceElement(m.group(1), m.group(2), m.group(3));
           if ( ste != null ) {
               st.add(ste);
           }

           last = m.end();
       }
       if ( !st.isEmpty() ) {
           start = adjustFirstLinePosition(text, start);
           result.add( new StackTracePosition(st.toArray(new StackTraceElement[st.size()]), start, last) );
       }

//       int i = 0;
//       for (StackTracePosition stp : result) {
//           for (; i < lineBreaks.size(); i++) {
//               int lb = lineBreaks.get(i);
//               if(lb > stp.end) break;
//           }
//           stp.start += i;
//           stp.end += i;
//       }

       return result;
   }

   private static List<String> subs;

   // XXX Pretty ugly heuristics
   private static boolean isStacktraceContinuation(String text, int last, int start) {
       if ( last == -1 ) {
           return true;
       }

       else {
           String sub = text.substring(last,start);
           subs.add(sub);
           //System.out.println("  SUB: " + sub );
//            if ( !sub.contains("at")) {
//                return false;
//            }
           for( int i = 0; i < sub.length(); i++) {
               char ch = sub.charAt(i);
               switch( ch ) {
                   case ' ':
                   case 'a':
                   case '\t':
                   case 't':
                   case '\n':
                   case '\r':
                   case 'c':
                   case 'h':
                   case '[':
                   case ']':
                       continue;
                   default:
                     //  System.out.println("  ???? " + new Integer(ch));
                       return false;
               }
           }
           return true;
       }
   }

   private static int adjustFirstLinePosition(String text, int start) {
       // Adjust the start index so the first line of the stacktrace also
       // includes 'at' or '[catch]'.
       if (start > 0) {
           int startOfLine = start - 1;
           while (startOfLine > 0) {
               if (text.charAt(startOfLine) == '\n') {
                   startOfLine++;
                   break;
               } else {
                   startOfLine--;
               }
           }
           if (isStacktraceContinuation(text, startOfLine, start)) {
               return startOfLine;
           }
       }
       return start;
   }

   private static StackTraceElement createStackTraceElement(String method, String file, String line) {
       int lastDot = method.lastIndexOf('.');
       if ( lastDot == -1 ) {
           return null;
       }
       return new StackTraceElement( method.substring(0, lastDot),
                                     method.substring(lastDot + 1),
                                     file,
                                     line == null ? -1 : Integer.parseInt(line) );

   }

   private static String removeAll( String source, char toRemove) {

       StringBuilder sb = new StringBuilder();

       for (int i = 0; i < source.length(); i++) {
           char c = source.charAt(i);
           if ( c == '\n' ) {
               if ( i > 1 && source.charAt( i - 2) == 'a' && source.charAt( i - 2) == 't' ) { // XXX WTF
                   sb.append("");
               }
               // Skip the new line
               sb.append(" ");
           }
           else {
               sb.append(c);
           }
       }

       return sb.toString();
   }

    public static class StackTracePosition {
        private final StackTraceElement[] stackTraceElements;
        private int start;
        private int end;
        public StackTracePosition(StackTraceElement[] stackTraceElements, int start, int end) {
            this.stackTraceElements = stackTraceElements;
            this.start = start;
            this.end = end;
        }
        public int getStartOffset() {
            return start;
        }
        public int getEndOffset() {
            return end;
        }
        public StackTraceElement[] getStackTraceElements() {
            return stackTraceElements;
        }
    }

    public static void open(String path, final int line) {
        final FileObject fo = search(path);
        if ( fo != null ) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    doOpen(fo, line);
                }
            });
        }
    }

    public static void openSearchHistory(String path, final int line) {
        final FileObject fo = search(path);
        if ( fo != null ) {
            final File file = FileUtil.toFile(fo);
            if(file == null) {
                // XXX any chance to disable the action if it's not a real io.File - e.g. a jdk class?
                return;
            }
            Collection<? extends VCSSupport> supports = Lookup.getDefault().lookupAll(VCSSupport.class);
            if(supports == null) {
                return;
            }
            for (final VCSSupport s : supports) {
                // XXX this is messy - we implicitly expect that unrelevant VCS modules
                // will skip the action
                BugtrackingManager.getInstance().getRequestProcessor().post(new Runnable() {
                    public void run() {
                        s.searchHistory(file, line);
                    }
                });
            }
        }
    }

//   public static boolean doOpen(FileObject fo, int offset) {
//       try {
//           DataObject od = DataObject.find(fo);
//           EditorCookie ec = od.getCookie(org.openide.cookies.EditorCookie.class);
//           LineCookie lc = od.getCookie(org.openide.cookies.LineCookie.class);
//
//           if (ec != null && lc != null && offset != -1) {
//               StyledDocument doc = ec.openDocument();
//               if (doc != null) {
//                   int line = NbDocument.findLineNumber(doc, offset);
//                   int lineOffset = NbDocument.findLineOffset(doc, line);
//                   int column = offset - lineOffset;
//
//                   if (line != -1) {
//                       Line l = lc.getLineSet().getCurrent(line);
//
//                       if (l != null) {
//                           l.show(Line.SHOW_GOTO, column);
//                           return true;
//                       }
//                   }
//               }
//           }
//
//           OpenCookie oc = od.getCookie(org.openide.cookies.OpenCookie.class);
//           if (oc != null) {
//               oc.open();
//               return true;
//           }
//       } catch (IOException e) {
//           Exceptions.printStackTrace(e);
//       }
//
//       return false;
//   }
    public static boolean doOpen(FileObject fo, int line) {
        try {
            DataObject od = DataObject.find(fo);
            EditorCookie ec = od.getCookie(org.openide.cookies.EditorCookie.class);
            LineCookie lc = od.getCookie(org.openide.cookies.LineCookie.class);

            if (ec != null && lc != null && line != -1) {
                StyledDocument doc = ec.openDocument();
                if (doc != null) {
                    if (line != -1) {
                        Line l = null;
                        try {
                            l = lc.getLineSet().getCurrent(line);
                        } catch (IndexOutOfBoundsException e) {
                            BugtrackingManager.LOG.log(Level.FINE, null, e);
                            ec.open();
                            return false;
                        }
                        if (l != null) {
                            l.show(Line.ShowOpenType.OPEN, Line.ShowVisibilityType.FOCUS);
                            return true;
                        }
                    }
                 }
            }

            OpenCookie oc = od.getCookie(org.openide.cookies.OpenCookie.class);
            if (oc != null) {
                oc.open();
                return true;
            }
        } catch (IOException e) {
            BugtrackingManager.LOG.log(Level.SEVERE, null, e);
        }

        return false;
   }

   static private FileObject search(String path) {
       return GlobalPathRegistry.getDefault().findResource(path);
    }

}
