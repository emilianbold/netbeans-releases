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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
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
package org.netbeans.modules.javacard.project.deps;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.xml.parsers.ParserConfigurationException;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Models the dependencies data from a project.xml.
 *
 * To actually modify and save dependencies, use ResolvedDependencies,
 * which allows setting of paths and storing the necessary data to both
 * project.properties and project.xml.  This class simply represents the
 * data in project.xml only.
 *
 * @author Tim Boudreau
 */
public final class Dependencies {
    private final List <Dependency> dependencies = Collections.synchronizedList(new ArrayList<Dependency>());

    public Dependencies() {}

    public static Dependencies parse(InputSource in, PropertyEvaluator eval) throws SAXException, IOException {
        try {
            return DependenciesParser.parse(in);
        } catch (ParserConfigurationException ex) {
            IOException ioe = new IOException("Could not create XML parser");
            ioe.initCause(ex);
            throw ioe;
        }
    }

    public static Dependencies parse(Element configRoot) throws IOException {
        return DependenciesParser.parse(configRoot);
    }

    public Dependencies copy() {
        Dependencies result = new Dependencies();
        for (Dependency dep : dependencies) {
            result.add (dep.copy());
        }
        return result;
    }

    public void add (Dependency dep) {
        dependencies.add (dep);
    }

    public boolean moveUp (Dependency d) {
        int ix = dependencies.indexOf(d);
        assert ix >= 0;
        boolean result = ix > 0;
        if (result) {
            dependencies.remove (d);
            dependencies.add (ix -1, d);
        }
        return result;
    }

   public boolean moveDown (Dependency d) {
        int ix = dependencies.indexOf(d);
        assert ix >= 0;
        boolean result = ix < dependencies.size() - 1;
        if (result) {
            dependencies.remove (d);
            dependencies.add (ix + 1, d);
        }
        return result;
    }
   
    Dependency getByID(String id) {
        for (Dependency d : dependencies) {
            if (id.equals(d.getID())) {
                return d;
            }
        }
        return null;
    }

    public void removeAll (Dependencies other) {
        for (Dependency d : other.all()) {
            String id = d.getID();
            Dependency ours = getByID(id);
            if (ours != null) {
                dependencies.remove(ours);
            }
        }
    }

    public boolean canMoveUp (Dependency d) {
        return !dependencies.isEmpty() && !d.equals(dependencies.get(0));
    }

    public boolean canMoveDown(Dependency d) {
        return !dependencies.isEmpty() && !d.equals(dependencies.get(dependencies.size() - 1));
    }

    public List<Dependency> all() {
        return Collections.unmodifiableList(dependencies);
    }

   public void remove (Dependency d) {
       for (Iterator<Dependency> it = dependencies.iterator(); it.hasNext();) {
           if (d.getID().equals(it.next().getID())) {
               it.remove();
           }
       }
   }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Dependencies other = (Dependencies) obj;
        if (this.dependencies != other.dependencies && (this.dependencies == null || !this.dependencies.equals(other.dependencies))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 79 * hash + (this.dependencies != null ? this.dependencies.hashCode() : 0);
        return hash;
    }
}
