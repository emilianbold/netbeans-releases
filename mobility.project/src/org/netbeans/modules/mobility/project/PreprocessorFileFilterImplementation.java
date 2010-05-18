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
package org.netbeans.modules.mobility.project;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.HashSet;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.mobility.antext.preprocessor.CommentingPreProcessor;
import org.netbeans.modules.java.preprocessorbridge.spi.JavaFileFilterImplementation;
import org.netbeans.spi.project.ProjectConfiguration;
import org.netbeans.spi.project.support.ant.AntProjectEvent;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.AntProjectListener;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Adam Sotona
 */
public class PreprocessorFileFilterImplementation implements JavaFileFilterImplementation, PropertyChangeListener, AntProjectListener {
    
    private HashMap<String,String> abilities, defAbilities;
    private ProjectConfigurationsHelper pch;
    private AntProjectHelper aph;
    private FileObject srcRoot;
    private HashSet<ChangeListener> changeListeners = new HashSet();
    
    /** Creates a new instance of PreprocessorFileFilterImplementation */
    PreprocessorFileFilterImplementation(ProjectConfigurationsHelper pch, AntProjectHelper aph) {
        this.pch = pch;
        this.aph = aph;
        pch.addPropertyChangeListener(this);
        aph.addAntProjectListener(this);
    }
    
    public Reader filterReader(final Reader r) {
        if (!pch.isPreprocessorOn()) return r;
        final StringWriter sw = new StringWriter();
        new CommentingPreProcessor(new CommentingPreProcessor.Source() {
            public Reader createReader() throws IOException {
                return r;
            }
        }, new CommentingPreProcessor.Destination() {
            public Writer createWriter(boolean validOutput) throws IOException {
                return sw;
            }
            public void doInsert(int line, String s) throws IOException {}
            public void doRemove(int line, int column, int length) throws IOException {}
        }, getAbilities()).run();
        return new StringReader(sw.toString());
    }
    
    public CharSequence filterCharSequence(final CharSequence charSequence) {
        if (!pch.isPreprocessorOn()) return charSequence;
        final StringWriter sw = new StringWriter();
        new CommentingPreProcessor(new CommentingPreProcessor.Source() {
            public Reader createReader() throws IOException {
                return new StringReader(charSequence.toString());
            }
        }, new CommentingPreProcessor.Destination() {
            public Writer createWriter(boolean validOutput) throws IOException {
                return sw;
            }
            public void doInsert(int line, String s) throws IOException {}
            public void doRemove(int line, int column, int length) throws IOException {}
        }, getAbilities()).run();
        return sw.toString();
//        return charSequence; //see issue #107490
    }
    
    public Writer filterWriter(final Writer w) {
        if (!pch.isPreprocessorOn()) return w;
        return new StringWriter() {
            public void close() throws IOException {
                new CommentingPreProcessor(new CommentingPreProcessor.Source() {
                    public Reader createReader() throws IOException {
                        return new StringReader(getBuffer().toString());
                    }
                }, new CommentingPreProcessor.Destination() {
                    public Writer createWriter(boolean validOutput) throws IOException {
                        return w;
                    }
                    public void doInsert(int line, String s) throws IOException {}
                    public void doRemove(int line, int column, int length) throws IOException {}
                }, getDefaultAbilities()).run();
            }
        };
    }
    
    public void propertyChange(PropertyChangeEvent ev) {
        fireChange();
    }
    
    public void propertiesChanged(AntProjectEvent ev) {
        if (AntProjectHelper.PROJECT_PROPERTIES_PATH.equals(ev.getPath())) fireChange();
    }
    
    private void fireChange() {
        abilities = null;
        defAbilities = null;
        ChangeEvent ev = new ChangeEvent(this);
        synchronized (changeListeners) {
            for (ChangeListener ch : changeListeners) ch.stateChanged(ev);
        }
    }
    
    private HashMap<String,String> getDefaultAbilities() {
        HashMap<String,String> identifiers = defAbilities;
        if (identifiers == null) {
            ProjectConfiguration cfg = pch.getDefaultConfiguration();
            identifiers = new HashMap();
            identifiers.putAll(pch.getAbilitiesFor(cfg));
            identifiers.put(cfg.getDisplayName(),null);
            defAbilities = identifiers;
        }
        return identifiers;
    }
    
    private HashMap<String,String> getAbilities() {
        HashMap<String,String> identifiers = abilities;
        if (identifiers == null) {
            ProjectConfiguration cfg = pch.getActiveConfiguration();
            identifiers = new HashMap();
            identifiers.putAll(pch.getAbilitiesFor(cfg));
            identifiers.put(cfg.getDisplayName(),null);
            abilities = identifiers;
        }
        return identifiers;
    }
    
    public void configurationXmlChanged(AntProjectEvent ev) {}
    
    public void addChangeListener(ChangeListener listener) {
        synchronized (changeListeners) {
            changeListeners.add(listener);
        }
    }
    
    public void removeChangeListener(ChangeListener listener) {
        synchronized (changeListeners) {
            changeListeners.remove(listener);
        }
    }
}
