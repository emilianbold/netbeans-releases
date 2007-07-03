/*
 * RubyTestBase.java
 *
 * Created on March 16, 2007, 1:23 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.ruby;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import javax.swing.text.Document;
import org.jruby.ast.Node;
import org.netbeans.api.gsf.ParseListener;
import org.netbeans.api.gsf.ParserFile;
import org.netbeans.api.gsf.ParserResult;
import org.netbeans.editor.BaseDocument;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.gsf.GsfDataLoader;
import org.netbeans.modules.ruby.lexer.RubyTokenId;
import org.netbeans.spi.gsf.DefaultParseListener;
import org.netbeans.spi.gsf.DefaultParserFile;
import org.netbeans.spi.gsf.DefaultParserFile;
import org.openide.ErrorManager;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;

/**
 *
 * @author Tor Norbye
 */
public abstract class RubyTestBase extends NbTestCase {
    
    public RubyTestBase(String testName) {
        super(testName);
    }
    
    protected ParserResult parse(FileObject fileObject) {
        RubyParser parser = new RubyParser();
        int caretOffset = -1;
        
        ParserFile file = new DefaultParserFile(fileObject, null, false);
        String sequence = "";
        ParseListener listener = new DefaultParseListener();
        try {
            DataObject dobj = DataObject.find(fileObject);
            EditorCookie cookie = dobj.getCookie(EditorCookie.class);
            Document doc = cookie.openDocument();
            sequence = doc.getText(0, doc.getLength());
        } catch (Exception ex) {
            fail(ex.toString());
        }
        ParserResult result = parser.parseBuffer(file, caretOffset, -1, sequence, listener, RubyParser.Sanitize.NEVER);
        return result;
    }
    
    protected FileObject getTestFile(String relFilePath) {
        File wholeInputFile = new File(getDataDir(), relFilePath);
        if (!wholeInputFile.exists()) {
            NbTestCase.fail("File " + wholeInputFile + " not found.");
        }
        FileObject fo = FileUtil.toFileObject(wholeInputFile);
        assertNotNull(fo);
        
        return fo;
    }
    
    protected Node getRootNode(String relFilePath) {
        FileObject fileObject = getTestFile(relFilePath);
        ParserResult result = parse(fileObject);
        assertNotNull(result);
        RubyParseResult rpr = (RubyParseResult)result;
        Node root = rpr.getRootNode();
        
        return root;
    }

    protected FileObject findJRuby() {
        File data = getDataDir();
        File nbtree = data.getParentFile().getParentFile().getParentFile().getParentFile().getParentFile().getParentFile().getParentFile();
        assertNotNull(nbtree);
        assertTrue(nbtree.exists());
        File jruby = new File(nbtree, "nbbuild" + File.separator + "netbeans" + File.separator + "ruby1" + File.separator + "jruby-1.0");
        assertTrue(jruby.exists());
        try {
            jruby = jruby.getCanonicalFile();
        } catch (Exception ex) {
            fail(ex.toString());
        }
        assertTrue(jruby.exists());
        FileObject fo = FileUtil.toFileObject(jruby);
        assertNotNull(fo);
        
        return fo;
    }    
    
    // Locate as many Ruby files from the JRuby distribution as possible: libs, gems, etc.
     protected List<FileObject> findJRubyRubyFiles() {
         List<FileObject> l = new ArrayList<FileObject>();
         addRubyFiles(l, findJRuby());
         
         return l;
     }
     
     private void addRubyFiles(List<FileObject> list, FileObject parent) {
        for (FileObject child : parent.getChildren()) {
            if (child.isFolder()) {
                addRubyFiles(list, child);
            } else if (child.getMIMEType().equals(RubyMimeResolver.RUBY_MIME_TYPE)) {
                list.add(child);
            }
        }
     }

     private String readFile(final FileObject rakeTargetFile) {
        try {
            final StringBuilder sb = new StringBuilder(5000);
            rakeTargetFile.getFileSystem().runAtomicAction(new FileSystem.AtomicAction() {
                    public void run() throws IOException {

                        if (rakeTargetFile == null) {
                            return;
                        }

                        InputStream is = rakeTargetFile.getInputStream();
                        BufferedReader reader = new BufferedReader(new InputStreamReader(is));

                        while (true) {
                            String line = reader.readLine();

                            if (line == null) {
                                break;
                            }

                            sb.append(line);
                            sb.append('\n');
                        }
                    }
                });

            if (sb.length() > 0) {
                return sb.toString();
            } else {
                return null;
            }
        } catch (IOException ioe) {
            ErrorManager.getDefault().notify(ioe);

            return null;
        }
     }
     
     protected BaseDocument getDocument(String s) {
         try {
             BaseDocument doc = new BaseDocument(null, false);
             doc.putProperty(org.netbeans.api.lexer.Language.class,  RubyTokenId.language());

             doc.insertString(0, s, null);

             return doc;
         } catch (Exception ex) {
             fail(ex.toString());
             return null;
         }
     }

     protected BaseDocument getDocument(FileObject fo) {
         try {
//             DataObject dobj = DataObject.find(fo);
//             assertNotNull(dobj);
//
//             EditorCookie ec = (EditorCookie)dobj.getCookie(EditorCookie.class);
//             assertNotNull(ec);
//
//             return (BaseDocument)ec.openDocument();
             
             return getDocument(readFile(fo));
         } catch (Exception ex) {
             fail(ex.toString());
             return null;
         }
     }
}
