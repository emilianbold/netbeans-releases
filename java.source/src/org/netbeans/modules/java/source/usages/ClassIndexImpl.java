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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.java.source.usages;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.ClassIndex;
import org.netbeans.api.java.source.ElementHandle;
import org.openide.filesystems.FileObject;
import org.openide.util.Utilities;

/** Should probably final class with private constructor.
 *
 * @author Petr Hrebejk, Tomas Zezula
 */
public abstract class ClassIndexImpl {
    
    public final List<WeakReference<ClassIndexImplListener>> listeners = Collections.synchronizedList(new ArrayList<WeakReference<ClassIndexImplListener>> ());

    private State state = State.NEW;


    public static enum UsageType {

        SUPER_CLASS( 0 ),
        SUPER_INTERFACE( 1 ),
        FIELD_REFERENCE( 2 ), 
        METHOD_REFERENCE( 3 ),
        TYPE_REFERENCE (4);

        private int offset;

        UsageType( final int offset) {
            this.offset = offset;
        }

        int getOffset () {
            return this.offset;
        }
    }

    public static enum State {
        NEW,
        INITIALIZED,
    }
    
    
    public static ClassIndexFactory FACTORY;    
    
    public abstract <T> void search (final String binaryName, final Set<UsageType> usageType, final ResultConvertor<T> convertor, final Set<? super T> result) throws IOException, InterruptedException;
    
    public abstract <T> void getDeclaredTypes (String name, ClassIndex.NameKind kind, final ResultConvertor<T> convertor, final Set<? super T> result) throws IOException, InterruptedException;
    
    public abstract <T> void getDeclaredElements (String ident, ClassIndex.NameKind kind, ResultConvertor<T> convertor, Map<T,Set<String>> result) throws IOException, InterruptedException;
    
    public abstract void getPackageNames (String prefix, boolean directOnly, Set<String> result) throws IOException, InterruptedException;
    
    public abstract FileObject[] getSourceRoots ();
   
    public abstract BinaryAnalyser getBinaryAnalyser ();
    
    public abstract SourceAnalyser getSourceAnalyser ();
    
    public abstract String getSourceName (String binaryName) throws IOException;
    
    public abstract void setDirty (URL url);

    public abstract boolean isSource ();

    public abstract boolean isEmpty ();
    
    protected abstract void close () throws IOException;
    
    public void addClassIndexImplListener (final ClassIndexImplListener listener) {
        assert listener != null;        
        this.listeners.add (new Ref (listener));
    }
    
    public void removeClassIndexImplListener (final ClassIndexImplListener listener) {
        assert listener != null;
        synchronized (this.listeners) {
            for (Iterator<WeakReference<ClassIndexImplListener>> it = this.listeners.iterator(); it.hasNext();) {
                WeakReference<ClassIndexImplListener> lr = it.next();
                ClassIndexImplListener l = lr.get();
                if (listener == l) {
                    it.remove();
                }
            }
        }
    }
    
    public void typesEvent (final Collection<? extends ElementHandle<TypeElement>> added, final Collection<? extends ElementHandle<TypeElement>> removed, final Collection<? extends ElementHandle<TypeElement>> changed) {
        final ClassIndexImplEvent a = added == null || added.isEmpty() ? null : new ClassIndexImplEvent(this, added);
        final ClassIndexImplEvent r = removed == null || removed.isEmpty() ? null : new ClassIndexImplEvent(this, removed);
        final ClassIndexImplEvent ch = changed == null || changed.isEmpty() ? null : new ClassIndexImplEvent(this, changed);
        typesEvent(a, r, ch);
    }

    public void typesEvent (final ClassIndexImplEvent added, final ClassIndexImplEvent removed, final ClassIndexImplEvent changed) {
        WeakReference<ClassIndexImplListener>[] _listeners;
        synchronized (this.listeners) {
            _listeners = this.listeners.toArray(new WeakReference[this.listeners.size()]);
        }
        for (WeakReference<ClassIndexImplListener> lr : _listeners) {
            ClassIndexImplListener l = lr.get();
            if (l != null) {
                if (added != null) {
                    l.typesAdded(added);
                }
                if (removed != null) {
                    l.typesRemoved(removed);
                }
                if (changed != null) {
                    l.typesChanged(changed);
                }
            }
        }
    }

    public State getState() {
        return this.state;
    }

    public void setState(final State state) {
        assert state != null;
        assert this.state != null;
        if (state.ordinal() < this.state.ordinal()) {
            throw new IllegalArgumentException();
        }
        this.state=state;
    }
    
    public static final class IndexAlreadyClosedException extends IOException {        
    }
    
    private class Ref extends WeakReference<ClassIndexImplListener> implements Runnable {
        public Ref (ClassIndexImplListener listener) {
            super (listener, Utilities.activeReferenceQueue());
        }

        public void run() {
            listeners.remove(this);
        }
    }
}
