/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.csl.api;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;
import javax.swing.text.Document;
import javax.swing.text.StyledDocument;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.csl.api.DeclarationFinder.DeclarationLocation;
import org.netbeans.modules.csl.core.Language;
import org.netbeans.modules.csl.core.LanguageRegistry;
import org.netbeans.modules.csl.navigation.Icons;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.parsing.api.Embedding;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.LineCookie;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileObject;
import org.openide.text.Line;
import org.openide.text.NbDocument;


/** 
 * This file is originally from Retouche, the Java Support 
 * infrastructure in NetBeans. I have modified the file as little
 * as possible to make merging Retouche fixes back as simple as
 * possible. 
 *
 * This class contains various methods bound to visualization of Java model
 * elements. It was formerly included under SourceUtils
 *
 * XXX - needs cleanup
 *
 * @author Jan Lahoda
 * @author Tor Norbye
 */
public final class UiUtils {

    public static boolean open(Source source, ElementHandle handle) {
        assert source != null;
        assert handle != null; // Only one should be set

        DeclarationLocation location = getElementLocation(source, handle);

        if (location != DeclarationLocation.NONE) {
            return doOpen(location.getFileObject(), location.getOffset());
        }

        return false;
    }

    public static boolean open(final FileObject fo, final int offset) {
        assert fo != null;
        
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(new Runnable() {
                public @Override void run() {
                    doOpen(fo, offset);
                }
            });
            return true; // not exactly accurate, but....
        }
        
        return doOpen(fo, offset);
    }

    public static ImageIcon getElementIcon( ElementKind elementKind, Collection<Modifier> modifiers ) {
        return Icons.getElementIcon(elementKind, modifiers);
    }

    public static KeystrokeHandler getBracketCompletion(Document doc, int offset) {
        BaseDocument baseDoc = (BaseDocument)doc;
        List<Language> list = LanguageRegistry.getInstance().getEmbeddedLanguages(baseDoc, offset);
        for (Language l : list) {
            if (l.getBracketCompletion() != null) {
                return l.getBracketCompletion();
            }
        }

        return null;
    }

    // Private methods ---------------------------------------------------------

    private static final Logger LOG = Logger.getLogger(UiUtils.class.getName());

    private UiUtils() {
    }

    private static boolean doOpen(FileObject fo, int offset) {
        try {
            EditorCookie ec = DataLoadersBridge.getDefault().getCookie(fo, EditorCookie.class);
            LineCookie lc = DataLoadersBridge.getDefault().getCookie(fo, LineCookie.class);

            if ((ec != null) && (lc != null) && (offset != -1)) {
                StyledDocument doc = ec.openDocument();

                if (doc != null) {
                    int line = NbDocument.findLineNumber(doc, offset);
                    int lineOffset = NbDocument.findLineOffset(doc, line);
                    int column = offset - lineOffset;

                    if (line != -1) {
                        Line l = lc.getLineSet().getCurrent(line);

                        if (l != null) {
                            l.show(Line.ShowOpenType.OPEN, Line.ShowVisibilityType.FOCUS, column);
                            return true;
                        }
                    }
                }
            }

            OpenCookie oc = DataLoadersBridge.getDefault().getCookie(fo, OpenCookie.class);

            if (oc != null) {
                oc.open();
                return true;
            }
        } catch (IOException ioe) {
            LOG.log(Level.WARNING, null, ioe);
        }

        return false;
    }

    private static DeclarationLocation getElementLocation(Source source, final ElementHandle handle) {
        if (source.getFileObject() == null) {
            return DeclarationLocation.NONE;
        }

        FileObject fileObject = handle.getFileObject();
        if (fileObject != null && fileObject != source.getFileObject()) {
            // The element is not in the parse tree for this parse job; it is
            // probably something like an indexed element
            return new DeclarationLocation(fileObject, -1);
        }

        final DeclarationLocation[] result = new DeclarationLocation[] { null };
        try {
            Future<Void> f = ParserManager.parseWhenScanFinished(Collections.singleton(source), new UserTask() {
                public @Override void run(ResultIterator resultIterator) throws ParseException {
                    if (resultIterator.getSnapshot().getMimeType().equals(handle.getMimeType())) {
                        Parser.Result r = resultIterator.getParserResult();
                        if (r instanceof ParserResult) {
                            ParserResult info = (ParserResult) r;
                            OffsetRange range = handle.getOffsetRange(info);
                            if (range != OffsetRange.NONE && range != null) {
                                result[0] = new DeclarationLocation(info.getSnapshot().getSource().getFileObject(), range.getStart());
                                return;
                            }
                        }
                    }

                    for(Embedding e : resultIterator.getEmbeddings()) {
                        run(resultIterator.getResultIterator(e));
                        if (result[0] != null) {
                            break;
                        }
                    }
                }
            });
            //#169806: Do not block when parsing is in progress
            if (!f.isDone()) {
                f.cancel(true);
                return new DeclarationLocation(source.getFileObject(), -1);
            }
        } catch (ParseException e) {
            LOG.log(Level.WARNING, null, e);
        }

        return result[0] == null ? DeclarationLocation.NONE : result[0];
    }
}
