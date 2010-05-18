/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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

package org.netbeans.modules.uml.diagrams.nodes.sqd;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collection;
import org.netbeans.api.visual.anchor.Anchor;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.uml.drawingarea.view.UMLEdgeWidget;
import org.netbeans.modules.uml.drawingarea.widgets.MessagePin;


/**
 *
 * @author sp153251
 */
public class MessagePinWidget extends Widget implements Comparable<MessagePinWidget>,MessagePin{

    private static long id_c;
    
 
    private ArrayList<UMLEdgeWidget> connections;
    
    private PINKIND kind;
    
    private long id;
    
    public MessagePinWidget(Scene scene,PINKIND kind) {
        super(scene);
        this.kind=kind;
        setPreferredBounds(new Rectangle(0,0,0,0));
        //
        id=id_c;
        id_c++;
    }

    public PINKIND getKind()
    {
        return kind;
    }

    public int getNumbetOfConnections() {
        Collection<Dependency> dependencies=getDependencies();
        int counter=0;
        for(Dependency d:dependencies)
        {
            if(d instanceof Anchor)
            {
                Anchor a=(Anchor) d;
                if(a.getEntries().get(0).getAttachedConnectionWidget()!=null)counter++;
            }
        }
        return counter;
    }


    public UMLEdgeWidget getConnection(int index) {
        if(index<0)throw new IndexOutOfBoundsException("can't find connection with "+index+" psition");
        Collection<Dependency> dependencies=getDependencies();
        int counter=0;
        for(Dependency d:dependencies)
        {
            if(d instanceof Anchor)
            {
                Anchor a=(Anchor) d;
                if(a.getEntries().get(0).getAttachedConnectionWidget()!=null)
                {
                    counter++;
                    if(counter>index)return (UMLEdgeWidget) a.getEntries().get(0).getAttachedConnectionWidget();
                }
            }
        }
        throw new IndexOutOfBoundsException("can't find connection with "+index+" psition");
    }

    /**
     * return margin(i.e. space on execution specification before corresponding pin)
     * @param kind
     * @return
     */
    public int getMarginBefore()
    {
        int ret=10;
        switch(kind)
        {
            case ASYNCHRONOUS_CALL_IN:
            case SYNCHRONOUS_CALL_IN:
            case CREATE_CALL_IN:
                ret=0;
                break;
        }
        return ret;
    }

    /**
     * return margin(i.e. space on execution specification after corresponding pin)
     * @param kind
     * @return
     */
    public int getMarginAfter()
    {
        int ret=10;
        switch(kind)
        {
            case ASYNCHRONOUS_CALL_IN:
                ret=30;
                break;
            case CREATE_CALL_IN:
            case SYNCHRONOUS_RETURN_OUT:
                ret=0;
                break;
        }
        return ret;
    }
    
    public int compareTo(MessagePinWidget o) {
        if(this==o)return 0;
        else
        {
            Point loc1=getPreferredLocation();
            Point loc2=o.getPreferredLocation();
            if(loc1==null)loc1=getLocation();
            if(loc2==null)loc2=o.getLocation();
            int y1=getParentWidget().convertLocalToScene(loc1).y;
            int y2=o.getParentWidget().convertLocalToScene(loc2).y;
            int res=y2-y1;
            if(res==0)
            {
                if(id!=o.id)res=(id - o.id)>0 ? 1 : -1;
            }
            return res;
        }
    }

    @Override
    public String toString() {
        return getKind().name()+": "+super.toString();
    }
    
}
