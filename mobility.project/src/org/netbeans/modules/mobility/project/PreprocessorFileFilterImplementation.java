/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
    }
    
    public Writer filterWriter(final Writer w) {
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
