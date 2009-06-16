/*
 * test /mojarra_ext/mojarra_ext.taglib.xml
 */
package org.netbeans.modules.web.frameworks.facelets.parser;

import com.sun.facelets.tag.TagLibrary;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import org.netbeans.junit.NbTestCase;
import org.openide.util.Exceptions;

/**
 *
 * @author fye
 */
public class TagLibraryConfigTest extends NbTestCase {

    public TagLibraryConfigTest(String testName) {
        super(testName);
    }

    @Override
    public void setUp() {
    }

    @Override
    public void tearDown() {
    }

    public void testMojara() {
        try {
            File mojarraTagLib = new File(getDataDir(), "/mojarra_ext/mojarra_ext.taglib.xml");
            TagLibraryConfig tagLibraryConfig = new TagLibraryConfig();
            TagLibrary mojarraTagLibrary = tagLibraryConfig.create(mojarraTagLib.toURL());
            boolean hasNameSpace = mojarraTagLibrary.containsNamespace("http://mojarra.dev.java.net/mojarra_ext");
            assertEquals("fail to parse" + mojarraTagLib.getName(), true, hasNameSpace);
        } catch (MalformedURLException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }

    }
}
