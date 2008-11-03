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
package org.netbeans.modules.mobility.svgcore.palette;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.editor.indent.api.Reformat;
import org.netbeans.modules.mobility.svgcore.SVGDataObject;
import org.netbeans.modules.mobility.svgcore.composer.SceneManager;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.nodes.Node;
import org.openide.text.ActiveEditorDrop;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 *
 * @author Pavel Benes
 */
public final class SVGPaletteItemDataObject extends MultiDataObject {
    private final SVGPaletteItemData m_data;
    
    public SVGPaletteItemDataObject(FileObject pf, SVGPaletteItemDataLoader loader) throws DataObjectExistsException, IOException {
        super(pf, loader);
        //CookieSet cookies = getCookieSet();
        //cookies.add((Node.Cookie) DataEditorSupport.create(this, getPrimaryEntry(), cookies));
        m_data = SVGPaletteItemData.get(pf);
    }

    public  SVGPaletteItemData getData() {
        return m_data;
    }
    
    public File getReferencedFile() throws FileNotFoundException, IOException {
        return new File( m_data.getFilePath());
    }
    
    protected @Override Node createNodeDelegate() {
        return new SVGPaletteItemDataNode(this, getLookup());
    }
    
    @Override
    public Lookup getLookup() {
        InstanceContent ic = new InstanceContent();
        
        ic.add( new ActiveEditorDrop() {
            public boolean handleTransfer(JTextComponent target) {
                SVGDataObject dObj = SVGDataObject.getActiveDataObject(target);
                if ( dObj != null) {
                    try {
                        File file = getReferencedFile();
                        if ( file.exists() && file.isFile()) {
                            SceneManager.log(Level.INFO, "Dropping file " + file.getPath()); //NOI18N
                            insert( dObj.getModel().getSVGBody(file, null), target); 
                            return true;
                        } else {
                            SceneManager.log(Level.SEVERE, "Nothing to drop, file " + file + " not found"); //NOI18N
                        }
                    } catch (Exception ex) {
                        SceneManager.error("Error during image merge", ex); //NOI18N
                    } 
                } else {
                    SceneManager.log(Level.INFO, "SVGDataObject not found."); //NOI18N
                }
                return false;
            }
        });
        ic.add(this);
        return new AbstractLookup(ic);
        //return getCookieSet().getLookup();
    }       
    
    private static void insert(final String text, final JTextComponent target) {
        final Document doc = target.getDocument();
        
        if (doc instanceof BaseDocument) {
            final Reformat formatter = Reformat.get(doc);
            formatter.lock();
            try {
                final boolean [] ok = new boolean [] { false };
                ((BaseDocument) doc).runAtomic(new Runnable() {
                    public void run() {
                        try {
                            Caret caret = target.getCaret();
                            int p0 = Math.min(caret.getDot(), caret.getMark());
                            int p1 = Math.max(caret.getDot(), caret.getMark());
                            doc.remove(p0, p1 - p0);

                            int start = caret.getDot();
                            doc.insertString(start, text, null);

                            int end = start + text.length();
                            formatter.reformat(start, end);
                            ok[0] = true;
                        } catch (BadLocationException ble) {
                            // ignore
                        }
                    }
                });
                if (!ok[0]) {
                    ((BaseDocument) doc).atomicUndo();
                }
            } finally {
                formatter.unlock();
            }
        }
    }
}
