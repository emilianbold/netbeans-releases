/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
 
#include "vehicle_list.h"


List* List::prev(Vehicle *veh) {
    List *i = this->find(veh);

    if (i->hasValue()) {
        return i->p;
    } else {
        return NULL;    // couldn't find vehicle
    }
}


List* List::next(Vehicle *veh) {
    List *i = this->find(veh);

    if (i->hasValue()) {
        return i->n;
    } else {
        return NULL;    // couldn't find vehicle
    }
}


List* List::find(Vehicle *veh) {
    for (List *i = this->first(); i->hasValue(); i = i->n) {
        if (i->v->name() == veh->name()) {
            return i;
        }
    }
    return NULL; // couldn't find vehicle
}

void List::remove(Vehicle *veh) {
    List *i = this->find(veh);

    if (i->hasValue()) {
        i->p->n = i->n;
        i->n->p = i->p;
    }
}

void List::append(Vehicle *veh) {
    List *i = new List();

    i->v = veh;
    i->n = this;
    i->p = p;
    p->n = i;
    p    = i;
}

void List::prepend(Vehicle *veh) {
    List *i = new List();

    i->v = veh;
    i->p = this;
    i->n = n;
    n->p = i;
    n    = i;
}

void List::insert(Vehicle *veh) {
    // Scan over list looking for element which is smaller than v and then insert v after it
    for (List *i = this->last(); i->hasValue(); i = i->p)  {
        if (i->v->pos() < veh->pos()) {
            i->insertAfter(veh);
            return;
        }
    }

    // If there is no element smaller than v, insert it at the beginning
    this->insertAfter(veh);
}

ostream& operator<<(ostream & o, List & l)
{ 
    o << "{ ";

    for (List *i = l.first(); i->hasValue(); i = i->next()) {
        o << i->value();
        if (i != l.last()){
            o << " , ";
        }
    }

    o << " }" << endl;

    return o;
}

