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

package org.netbeans.modules.cnd.editor.cplusplus;

import javax.swing.text.BadLocationException;
import org.netbeans.modules.cnd.editor.api.CodeStyle;
import org.netbeans.modules.cnd.editor.options.EditorOptions;
import org.netbeans.modules.cnd.editor.reformat.Reformatter;

/**
 *
 * @author as204739
 */
public class CCNewFormatterSingleTestCase extends CCFormatterBaseUnitTestCase {

    public CCNewFormatterSingleTestCase(String testMethodName) {
        super(testMethodName);
    }

    /**
     * Perform reformatting of the whole document's text.
     */
    @Override
    protected void reformat() {
        Reformatter f = new Reformatter(getDocument(), CodeStyle.getDefault(getDocument()));
        try {
            f.reformat();
        } catch (BadLocationException e) {
            e.printStackTrace(getLog());
            fail(e.getMessage());
	}
    }

    private void setDefaultsOptions(){
        EditorOptions.resetToDefault(CodeStyle.getDefault(CodeStyle.Language.CPP));
    }

    public void testIdentMultyConstructor3() {
        setDefaultsOptions();
        setLoadDocumentText(
            "class IndexReader : LUCENE_BASE\n" +
            "{\n" +
            "public:\n" +
            "class IndexReaderCommitLockWith : \n" +
            "public CL_NS(store)::LuceneLockWith\n" +
            "{\n" +
            "private:\n" +
            "IndexReader* reader;\n" +
            "};\n" +
            "};\n"
            );
        reformat();
        assertDocumentText("Incorrect identing multyline constructor",
            "class IndexReader : LUCENE_BASE\n" +
            "{\n" +
            "public:\n" +
            "    class IndexReaderCommitLockWith :\n" +
            "    public CL_NS(store)::LuceneLockWith\n" +
            "    {\n" +
            "    private:\n" +
            "        IndexReader* reader;\n" +
            "    };\n" +
            "};\n"
        );
    }
    
//        if (!line) // End of file
//            {
//            status.exit_status = 0;
//            break;
//        }
//----------------------
//     switch (optid) {
//#ifdef __NETWARE__
//        case OPT_AUTO_CLOSE:
//        setscreenmode(SCR_AUTOCLOSE_ON_EXIT);
//        break;
//#endif
//        case OPT_CHARSETS_DIR:
//        strmov(mysql_charsets_dir, argument);
//        charsets_dir = mysql_charsets_dir;
//        break;
//    case OPT_DEFAULT_CHARSET:
//        default_charset_used = 1;
//        break;
// -----------------------
// C_MODE_START
//#    include <decimal.h>
//        C_MODE_END
//
//#    define DECIMAL_LONGLONG_DIGITS 22
//#    define DECIMAL_LONG_DIGITS 10
//#    define DECIMAL_LONG3_DIGITS 8
//
//        /* maximum length of buffer in our big digits (uint32) */
//#    define DECIMAL_BUFF_LENGTH 9
//        /*
//        maximum guaranteed precision of number in decimal digits (number of our
//        digits * number of decimal digits in one our big digit - number of decimal
//        digits in one our big digit decreased on 1 (because we always put decimal
//        point on the border of our big digits))
//*/
//#    define DECIMAL_MAX_PRECISION ((DECIMAL_BUFF_LENGTH * 9) - 8*2)
//#    define DECIMAL_MAX_SCALE 30
//#    define DECIMAL_NOT_SPECIFIED 31
//---------------------------    
//typedef struct st_line_buffer
//{
//    File file;
//    char *buffer;
//    /* The buffer itself, grown as needed. */
//    char *end;
//    /* Pointer at buffer end */
//    char *start_of_line, *end_of_line;
//    uint bufread;
//    /* Number of bytes to get with each read(). */
//    uint eof;
//    ulong max_size;
//    ulong read_length;
//    /* Length of last read string */
//}
//LINE_BUFFER;
//-----------------------    
//    end :
//            if (fd >= 0)
//        my_close(fd, MYF(MY_WME));
//    end_io_cache(file);
//    delete description_event;
//    return error;
//}

    
}
