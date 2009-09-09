/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.csl.editor.hyperlink;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.csl.api.DataLoadersBridge;
import org.netbeans.modules.csl.api.DeclarationFinder;
import org.netbeans.modules.csl.api.DeclarationFinder.AlternativeLocation;
import org.netbeans.modules.csl.api.DeclarationFinder.DeclarationLocation;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.api.CodeCompletionHandler;
import org.netbeans.modules.csl.api.ElementHandle;
import org.netbeans.modules.csl.core.GsfHtmlFormatter;
import org.netbeans.modules.csl.core.Language;
import org.netbeans.modules.csl.core.LanguageRegistry;
import org.netbeans.modules.csl.core.UiUtils;
import org.netbeans.modules.csl.editor.completion.GsfCompletionProvider;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.api.indexing.IndexingManager;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser;
import org.openide.awt.HtmlBrowser;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;


/**
 *
 */
public class GoToSupport {
    private static final Logger LOG = Logger.getLogger(GoToSupport.class.getName());
    
    /** Jump straight to declarations */
    static final boolean IM_FEELING_LUCKY = Boolean.getBoolean("gsf.im_feeling_lucky");
    
    private GoToSupport() {
    }
    
    public static String getGoToElementTooltip(final Document doc, final int offset) {
        return perform(doc, offset, true);
    }

    public static String performGoTo(final Document doc, final int offset) {
        return perform(doc, offset, false);
    }
    
    private static String perform(final Document doc, final int offset, final boolean tooltip) {
        if (IndexingManager.getDefault().isIndexing()) {
            if (!tooltip) {
                StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(GsfCompletionProvider.class, "scanning-in-progress")); //NOI18N
                Toolkit.getDefaultToolkit().beep();
            }
            return null;
        }
        
        if (tooltip && PopupUtil.isPopupShowing()) {
            return null;
        }

        final FileObject fo = getFileObject(doc);
        if (fo == null) {
            return null;
        }

        Source js = Source.create(fo);
        if (js == null) {
            return null;
        }
        final String[] result = new String[] { null };

        try {
            ParserManager.parse(Collections.singleton(js), new UserTask() {
                public void run(ResultIterator controller) throws Exception {

                    Parser.Result embeddedResult = controller.getParserResult(offset);
                    if (!(embeddedResult instanceof ParserResult)) {
                        return;
                    }

                    ParserResult info = (ParserResult) embeddedResult;
                    Language language = LanguageRegistry.getInstance().getLanguageByMimeType(info.getSnapshot().getMimeType());
                    if (language == null) {
                        return;
                    }

                    DeclarationFinder finder = language.getDeclarationFinder();
                    if (finder == null) {
                        return;
                    }

                    // Isn't this a waste of time? Unused
                    getIdentifierSpan(doc, offset);

                    DeclarationLocation location = finder.findDeclaration(info, offset);

                    if (tooltip) {
                        CodeCompletionHandler completer = language.getCompletionProvider();
                        if (location != DeclarationLocation.NONE && completer != null) {
                            ElementHandle element = location.getElement();
                            if (element != null) {
                                String documentation = completer.document(info, element);
                                if (documentation != null) {
                                    result[0] = "<html><body>" + documentation; // NOI18N
                                }
                            }
                        }

                    } else if (location != DeclarationLocation.NONE && location != null) {
                        URL url = location.getUrl();
                        String invalid = location.getInvalidMessage();
                        if (url != null) {
                            HtmlBrowser.URLDisplayer.getDefault().showURL(url);
                        } else if (invalid != null) {
                            // TODO - show in the editor as an error instead?
                            StatusDisplayer.getDefault().setStatusText(invalid);
                            Toolkit.getDefaultToolkit().beep();
                        } else {
                            if (!IM_FEELING_LUCKY && location.getAlternativeLocations().size() > 0 &&
                                    !PopupUtil.isPopupShowing()) {
                                // Many alternatives - pop up a dialog and make the user choose
                                if (chooseAlternatives(doc, offset, location.getAlternativeLocations())) {
                                    return;
                                }
                            }

                            FileObject f = location.getFileObject();
                            int offset = location.getOffset();

                            if (f != null && f.isValid()) {
                                UiUtils.open(f, offset);
                            }

//                            String desc = "Description not yet implemented";
//                            result[0] = "<html><body>" + desc;
                        }

                    } else {
                        Toolkit.getDefaultToolkit().beep();
                    }
                }
            });
        } catch (ParseException pe) {
            LOG.log(Level.WARNING, null, pe);
        }
        
        return result[0];
    }

    /** TODO - MOVE TO UTILITTY LIBRARY */
    private static JTextComponent findEditor(Document doc) {
        JTextComponent comp = EditorRegistry.lastFocusedComponent();
        if (comp.getDocument() == doc) {
            return comp;
        }
        List<? extends JTextComponent> componentList = EditorRegistry.componentList();
        for (JTextComponent component : componentList) {
            if (comp.getDocument() == doc) {
                return comp;
            }
        }
        
        return null;
    }
    
    private static boolean chooseAlternatives(Document doc, int offset, List<AlternativeLocation> alternatives) {
        Collections.sort(alternatives);
        
        // Prune results a bit
        int MAX_COUNT = 30; // Don't show more items than this
        String previous = "";
        GsfHtmlFormatter formatter = new GsfHtmlFormatter();
        int count = 0;
        List<AlternativeLocation> pruned = new ArrayList<AlternativeLocation>(alternatives.size());
        for (AlternativeLocation alt : alternatives) {
            String s = alt.getDisplayHtml(formatter);
            if (!s.equals(previous)) {
                pruned.add(alt);
                previous = s;
                count++;
                if (count == MAX_COUNT) {
                    break;
                }
            }
        }
        alternatives = pruned;
        if (alternatives.size() <= 1) {
            return false;
        }
        
        JTextComponent target = findEditor(doc);
        if (target != null) {
            try {
                Rectangle rectangle = target.modelToView(offset);
                Point point = new Point(rectangle.x, rectangle.y+rectangle.height);
                SwingUtilities.convertPointToScreen(point, target);

                String caption = NbBundle.getMessage(GoToSupport.class, "ChooseDecl");
                PopupUtil.showPopup(new DeclarationPopup(caption, alternatives), caption, point.x, point.y, true, 0);

                return true;
            } catch (BadLocationException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        
        return false;
    }
    
    private static FileObject getFileObject(Document doc) {
        return DataLoadersBridge.getDefault().getFileObject(doc);
    }

    public int[] getHyperlinkSpan(Document doc, int offset) {
        return getIdentifierSpan(doc, offset);
    }

    public static int[] getIdentifierSpan(Document doc, int offset) {
        FileObject fo = getFileObject(doc);

        if (fo == null) {
            //do nothing if FO is not attached to the document - the goto would not work anyway:
            return null;
        }

        List<Language> list = LanguageRegistry.getInstance().getEmbeddedLanguages((BaseDocument) doc,offset);
        Language language = null;
        for (Language l : list) {
            if (l.getDeclarationFinder() != null) {
                language = l;
                break;
            }
        }

        if (language == null) {
            return null;
        }

        DeclarationFinder finder = language.getDeclarationFinder();
        assert finder != null;

        OffsetRange range = finder.getReferenceSpan(doc, offset);
        if (range == null) {
            throw new NullPointerException(finder + " violates its contract; should not return null from getReferenceSpan."); //NOI18N
        } else if (range != OffsetRange.NONE) {
            return new int[] { range.getStart(), range.getEnd() };
        }
        
        return null;
    }
}
