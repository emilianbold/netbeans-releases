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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.web.jsf.navigation.graph.layout;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.netbeans.modules.web.jsf.navigation.Page;
import org.netbeans.modules.web.jsf.navigation.Pin;
import org.netbeans.modules.web.jsf.navigation.graph.PageFlowScene;
import org.netbeans.modules.web.jsf.navigation.graph.PageFlowSceneElement;
import org.netbeans.modules.web.jsf.navigation.graph.layout.SceneElementComparator.SceneElement;

/**
 *
 * @author joelle
 */
public class SceneElementComparator implements Comparator<SceneElement> {

    PageFlowScene scene;

    public SceneElementComparator(PageFlowScene scene) {
        this.scene = scene;
    }

    @SuppressWarnings(value = "unchecked")
    public int compare(SceneElement s1, SceneElement s2) {

        Point p1 = s1.p;
        Point p2 = s2.p;
        PageFlowSceneElement e1 = s1.element;
        PageFlowSceneElement e2 = s2.element;

        if (p1 == null || p2 == null) {
            throw new NullPointerException("Can not compare null value");
        }

        if (p1.x == p2.x) {
            if (p1.y == p2.y) {
                return scene.getIdentityCode(s1.element).compareTo(scene.getIdentityCode(s2.element));
            }
            return p1.y - p2.y;
        }
        return p1.x - p2.x;
    }


    public static class SceneElement {

        public PageFlowSceneElement element;
        public Point p;

        public SceneElement(PageFlowSceneElement element, Point p) {
            this.p = p;
            this.element = element;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                throw new NullPointerException("Can not compare null object");
            }
            if (!(obj instanceof SceneElement)) {
                return false;
            }
            SceneElement e = (SceneElement) obj;
            if (!(element.equals(e.element) && p.equals(e.p))) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            return element.toString() + ":" + p.toString();
        }
    }


    public static PageFlowSceneElement getNextSelectableElement(PageFlowScene scene, boolean reverse, boolean nodesSelectable, boolean edgesSelectable, boolean pinsSelectable) {
        List<Object> selectedObjs = new ArrayList<Object>(scene.getSelectedObjects());
        List objs = new ArrayList();
        List<SceneElement> sortedElements = new ArrayList<SceneElement>();

        if (nodesSelectable) {
            objs.addAll(scene.getNodes());
        }
        if (edgesSelectable) {
            objs.addAll(scene.getEdges());
        }
        if (pinsSelectable) {
            objs.addAll(scene.getPins());
            /* Remove all the page pins */
            for (Page page : scene.getNodes()) {
                Pin pin = scene.getDefaultPin(page);
                sortedElements.remove(pin);
                objs.remove(pin);
            }
        }

        for (Object obj : objs) {
            Point p = scene.findWidget(obj).getLocation();
            SceneElement se = new SceneElement((PageFlowSceneElement) obj, p);
            sortedElements.add(se);
        }
        SceneElement mySelectedSceneElement = null;
        for (Object selObj : selectedObjs) {
            mySelectedSceneElement = new SceneElement((PageFlowSceneElement) selObj, scene.findWidget(selObj).getLocation());
            if (sortedElements.contains(mySelectedSceneElement)) {
                break;
            }
        }

        PageFlowSceneElement nextElement = null;
        if (sortedElements.size() > 0) {
            Collections.sort(sortedElements, new SceneElementComparator(scene));
            if (reverse) {
                Collections.reverse(sortedElements);
            }
            if (mySelectedSceneElement != null) {
                if (sortedElements.contains(mySelectedSceneElement)) {
                    int index = sortedElements.indexOf(mySelectedSceneElement);
                    /*System.out.println("Index: " + index + " Selected Element: " + mySelectedSceneElement.element);*/
                    if (sortedElements.size() > index + 1) {
                        nextElement = sortedElements.get(index + 1).element;
                    } else {
                        return null; // Let me know if you are on the last one so I can send focus to the scene.
                    }
                }
            }
            if (nextElement == null) {
                nextElement = sortedElements.get(0).element;
            }
        }
        return nextElement;
    }
}
