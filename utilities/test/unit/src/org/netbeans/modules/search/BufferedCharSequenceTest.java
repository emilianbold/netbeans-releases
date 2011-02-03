/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.search;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URI;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author vvg
 */
public class BufferedCharSequenceTest {

    private static final String UTF_8 = "UTF-8"; //NOI18N
    private static final String EUC_JP = "EUC_JP"; //NOI18N

    private static final String fc_10_bytes_content = "0123456789";
    private  File fc_10_bytes_file = getFile("10_bytes");
    private  FileInputStream fc_10_bytes_stream = getFileInputStream(fc_10_bytes_file);
    private  Charset cs_UTF_8 = Charset.forName(UTF_8);



    public BufferedCharSequenceTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
        fc_10_bytes_stream = getFileInputStream(fc_10_bytes_file);
        cs_UTF_8 = Charset.forName(UTF_8);
    }

    @After
    public void tearDown() {
        fc_10_bytes_stream = null;
        cs_UTF_8 = null;
    }

    /**
     * Test of close method, of class BufferedCharSequence.
     */
//    @Test
    public void testClose() throws Exception {
        System.out.println("close");
        BufferedCharSequence instance = null;
        BufferedCharSequence expResult = null;
        BufferedCharSequence result = instance.close();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of duplicate method, of class BufferedCharSequence.
     */
//    @Test
    public void testDuplicate() {
        System.out.println("duplicate");
        BufferedCharSequence instance = null;
        BufferedCharSequence expResult = null;
        BufferedCharSequence result = instance.duplicate();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of length method, of class BufferedCharSequence.
     */
    @Test
    public void testLength() {
        System.out.println("length");
        File file;        
        Charset cs;
        BufferedCharSequence instance;
        int result;
      
        file = getFile("10_bytes");                
        cs = Charset.forName(UTF_8);
        instance = new BufferedCharSequence(getFileInputStream(file), cs, file.length());
        instance.setMaxBufferSize(10);
        result = instance.length();
        assertEquals(10, result);

        file = getFile("0_bytes");        
        cs = Charset.forName(UTF_8);
        instance = new BufferedCharSequence(getFileInputStream(file), cs, file.length());
        instance.setMaxBufferSize(10);
        result = instance.length();
        assertEquals(0, result);

        file = getFile("1_byte");                
        cs = Charset.forName(UTF_8);
        instance = new BufferedCharSequence(getFileInputStream(file), cs, file.length());
        instance.setMaxBufferSize(10);
        result = instance.length();
        assertEquals(1, result);

    }

    /**
     * Test of charAt method, of class BufferedCharSequence.
     */
    @Test(expected=IndexOutOfBoundsException.class)
    public void testCharAt() {
        System.out.println("charAt");
        int index = 0;
        File file = getFile("0_bytes");
        Charset cs = Charset.forName(UTF_8);
        BufferedCharSequence instance =
                new BufferedCharSequence(getFileInputStream(file), cs, file.length());
        instance.charAt(index);
    }

    @Test
    public void testCharAt$1_byte() {
        System.out.println("testCharAt$1_byte");
        int index = 0;
        File file = getFile("1_byte");
        Charset cs = Charset.forName(UTF_8);
        try {
            BufferedCharSequence instance = 
                    new BufferedCharSequence(getFileInputStream(file), cs, file.length());
            char expResult = 'a';
            char result = instance.charAt(index);
            assertEquals(expResult, result);
        } catch (IndexOutOfBoundsException ioobe) {
            ioobe.printStackTrace();
            fail(ioobe.toString());
        } catch (BufferedCharSequence.SourceIOException bcse) {
            bcse.printStackTrace();
            fail(bcse.toString());
        }
    }

    @Test
    public void testCharAt$10_byte() {
        System.out.println("testCharAt$10_byte");
        File file = getFile("10_bytes");
        Charset cs = Charset.forName(UTF_8);

        BufferedCharSequence instance = 
                new BufferedCharSequence(getFileInputStream(file), cs, file.length());
        instance.setMaxBufferSize(10);
        char result;

        result = instance.charAt(0);
        assertEquals('0', result);

        result = instance.charAt(9);
        assertEquals('9', result);

        result = instance.charAt(5);
        assertEquals('5', result);

        result = instance.charAt(9);
        assertEquals('9', result);
   }

    @Test
    public void testCharAt_$10_byte$2() {
        System.out.println("testCharAt$10_byte$2");
        File file = getFile("10_bytes");
        Charset cs = Charset.forName(UTF_8);

        BufferedCharSequence instance = 
                new BufferedCharSequence(getFileInputStream(file), cs, file.length());
        instance.setMaxBufferSize(5);
        char result;

        result = instance.charAt(0);
        assertEquals('0', result);

        result = instance.charAt(9);
        assertEquals('9', result);

        result = instance.charAt(5);
        assertEquals('5', result);

        result = instance.charAt(9);
        assertEquals('9', result);
   }

    /**
     * Test of subSequence method, of class BufferedCharSequence.
     */
//    @Test
    public void testSubSequence() {
        System.out.println("subSequence");
        int start = 0;
        int end = 0;
        BufferedCharSequence instance = null;
        CharSequence expResult = null;
        CharSequence result = instance.subSequence(start, end);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of toString method, of class BufferedCharSequence.
     */
    @Test
    public void testToString() {
        System.out.println("toString");
        BufferedCharSequence instance = 
                new BufferedCharSequence(fc_10_bytes_stream, cs_UTF_8, fc_10_bytes_file.length());
        instance.setMaxBufferSize(5);
        String expResult = fc_10_bytes_content;
        String result = instance.toString();
        assertEquals(expResult, result);
    }

    /**
     * Test of position method, of class BufferedCharSequence.
     */
//    @Test
    public void testPosition() {
        System.out.println("position");
        BufferedCharSequence instance = null;
        int expResult = 0;
        int result = instance.position();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of nextChar method, of class BufferedCharSequence.
     */
//    @Test
    public void testNextChar() {
        System.out.println("nextChar");
        BufferedCharSequence instance = null;
        char expResult = ' ';
        char result = instance.nextChar();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of rewind method, of class BufferedCharSequence.
     */
//    @Test
    public void testRewind() {
        System.out.println("rewind");
        BufferedCharSequence instance = null;
        BufferedCharSequence expResult = null;
        BufferedCharSequence result = instance.rewind();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getLineText method, of class BufferedCharSequence.
     */
    @Test
    public void testNextLineText() {
        System.out.println("nextLineText");
        System.out.println("nextLineText@no line terminators in the file.");
        BufferedCharSequence bcs = 
                new BufferedCharSequence(fc_10_bytes_stream, cs_UTF_8, fc_10_bytes_file.length());
        assertEquals(0, bcs.position());
        String expResult = fc_10_bytes_content;
        String result = bcs.nextLineText();
        assertEquals(expResult, result);
        assertEquals(11, bcs.position());
    }

    /**
     * Returns {@code FileChennel} for the specified data file located in the
     * {@code org.netbeans.modules.search.data} package.
     * @param fileName - name of the data file.
     * @return {@code FileChennel} of the data file.
     */
    public FileChannel getDataFileChannel(String fileName) {
        File file = getFile(fileName);
        FileInputStream fis = getFileInputStream(file);
        return fis.getChannel();
    }


    public FileInputStream getFileInputStream(File f) {
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(f);
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        }
        assertNotNull(fis);
        return fis;
    }

    public File getFile(String fileName) {
        File dataSubPackage = getDataSubPackage();
        File file = new File (dataSubPackage, fileName);
        assertTrue (file.exists());
        return file;
    }

    public File getDataSubPackage() {
        URL url = BufferedCharSequenceTest.class.getResource("data");
        File dataSubPackage = new File(URI.create(url.toExternalForm()));
        assertTrue(dataSubPackage.isDirectory());
        return dataSubPackage;
    }

}
