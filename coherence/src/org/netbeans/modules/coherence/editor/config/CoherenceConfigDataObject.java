/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.coherence.editor.config;

import org.netbeans.modules.coherence.xml.coherence.Coherence;
import org.netbeans.modules.coherence.util.xml.XMLObjectFactory;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import javax.xml.transform.Source;
import org.netbeans.spi.xml.cookies.CheckXMLSupport;
import org.netbeans.spi.xml.cookies.DataObjectAdapters;
import org.netbeans.spi.xml.cookies.TransformableSupport;
import org.netbeans.spi.xml.cookies.ValidateXMLSupport;
import org.openide.awt.StatusDisplayer;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataNode;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiFileLoader;
import org.openide.loaders.XMLDataObject;
import org.openide.nodes.Node;
import org.openide.nodes.Children;
import org.openide.nodes.Node.Cookie;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.xml.sax.InputSource;

public class CoherenceConfigDataObject extends XMLDataObject implements Lookup.Provider {

    private static final Logger logger = Logger.getLogger(CoherenceConfigDataObject.class.getCanonicalName());
    final InstanceContent ic;
    private AbstractLookup lookup = null;
    private Coherence coherence = null;
    private XMLObjectFactory xmlObjectFactory = XMLObjectFactory.getInstance();
    private Cookie saveCookie = new MySaveCookie();

    public CoherenceConfigDataObject(FileObject pf, MultiFileLoader loader) throws DataObjectExistsException, IOException {
        super(pf, loader);

        ic = new InstanceContent();
        lookup = new AbstractLookup(ic);
        InputSource is = DataObjectAdapters.inputSource(this);
        Source source = DataObjectAdapters.source(this);
        ic.add(new CheckXMLSupport(is));
        ic.add(new ValidateXMLSupport(is));
        ic.add(new TransformableSupport(source));
        ic.add(CoherenceConfigEditorSupport.create(this));
    }
    /*
     * Inner Classes
     */

    public class CoherenceConfigDataNode extends DataNode implements Node.Cookie {

        public CoherenceConfigDataNode(CoherenceConfigDataObject obj) {
            super(obj, Children.LEAF, lookup);
            ic.add(this);
            setIconBaseWithExtension(org.openide.util.NbBundle.getMessage(CoherenceConfigDataObject.class, "CoherenceConfig.file.icon"));
        }
    }

    public class MySaveCookie implements SaveCookie {

        @Override
        public void save() throws IOException {
            storeData(coherence);
        }
    };

    /*
     * Overrides
     */
    @Override
    protected Node createNodeDelegate() {
        return new CoherenceConfigDataNode(this);
    }

    @Override
    public Lookup getLookup() {
        return lookup;
    }

    @Override
    public Node.Cookie getCookie(Class type) {
        Object o = lookup.lookup(type);
        return o instanceof Node.Cookie ? (Node.Cookie) o : null;
    }

    @Override
    public void setModified(final boolean isModified) {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    setModified(isModified);
                }
            });
        } else {
            // I tied the SaveCookie implementation into this such that
            // the Save action is enabled whenever the object is modified.
            if (isModified) {
                Object o = null;
                if ((o = getCookie(SaveCookie.class)) == null) {
                    ic.add(saveCookie);
                } else {
                    logger.log(Level.FINE, "*** APH-I2 : Save Cookie already exists " + o.getClass().getCanonicalName());
                }
            } else {
                SaveCookie cookie = (SaveCookie) getCookie(SaveCookie.class);
                if (cookie != null) {
                    getCookieSet().remove(cookie);
                }
            }
            super.setModified(isModified);
        }
    }

    /*
     * Load & Save Methods
     */
    public Coherence getCoherence() {
        if (coherence == null) {
            loadData();
        }
        return coherence;
    }

    public Coherence loadData() {
        CoherenceConfigEditorSupport support = (CoherenceConfigEditorSupport) lookup.lookup(CoherenceConfigEditorSupport.class);
        InputStream is = null;
        try {
            is = support.getInputStream();
            coherence = xmlObjectFactory.unmarshalCoherenceConfigFile(is);
        } catch (Exception ex) {
            logger.log(Level.WARNING, "*** APH-I3 : Failed to unmarshal ", ex);
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (Exception e) {
                logger.log(Level.WARNING, "*** APH-I3 : Failed to close fos ", e);
            }
        }
        return coherence;
    }

    public void storeData(Coherence coherence) {
        this.coherence = coherence;
        if (coherence != null) {
            if (getPrimaryFile().canWrite()) {
                OutputStream fos = null;
                try {
                    File file = FileUtil.toFile(getPrimaryFile());
                    fos = new FileOutputStream(file);
                    xmlObjectFactory.marshalXMLToStream(coherence, fos, "\n<!DOCTYPE pof-config SYSTEM \"coherence.dtd\">\n");
                } catch (Exception e) {
                    logger.log(Level.WARNING, "*** APH-I3 : Failed to save data", e);
                    StatusDisplayer.getDefault().setStatusText("Failed to save data");
                } finally {
                    try {
                        if (fos != null) {
                            fos.close();
                        }
                    } catch (Exception e) {
                        logger.log(Level.WARNING, "*** APH-I3 : Failed to close fos", e);
                    }
                }
            } else {
                StatusDisplayer.getDefault().setStatusText("Can't Write");
            }
        }
        setModified(false);
//        printData(pofConfig);
    }

    public void printData(Coherence coherence) {
        if (coherence != null) {
            logger.log(Level.INFO, xmlObjectFactory.marshalXMLToString(coherence));
        }
    }
}
