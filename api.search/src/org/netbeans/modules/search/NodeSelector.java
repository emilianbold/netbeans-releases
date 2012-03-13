/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.search;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import javax.swing.tree.TreePath;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.util.logging.Level.FINER;
import static java.util.logging.Level.FINEST;

/**
 * Utility class for working with selection of nodes in search results.
 *
 * @author  Marian Petras
 */
final class NodeSelector {

    /** */
    private final TreePath[] nodes;
    private final Map<TreePathWrapper,Boolean> map;
    /** whether some detail node has been put to the {@link #map} */
    private boolean detailNodeAdded = false;

    private TreePath lsfn = null;   //last selected file node
    private TreePath ldn = null;    //last selected detail node not yet stored in the map
    private TreePath ldfn = null;   //parent of 'ldn'

    private final Logger log = Logger.getLogger(getClass().getName());

    /**
     * Selects nodes that are substantial for what will happen when Enter/Return
     * is pressed if the given nodes are selected.
     * <p>Substantial nodes are:</p>
     * <ul>
     *     <li>nodes representing files</li>
     *     <li>nodes representing details inside a file;
     *         if there are two or more detail nodes for the same file,
     *         these detail nodes are replaced with a single node representing
     *         the file</li>
     * </ul>
     * 
     * @param  selectedNodes  paths to all currently selected nodes
     * @param  canHaveDetails  whether detail nodes should be taken into account
     * @return  the selected nodes with non-essential nodes removed or replaced
     *          with substantial equivalents
     */
    static List<TreePath> selectMainNodes(final TreePath[] selectedNodes,
                                          final boolean canHaveDetails) {
        final Logger log = Logger.getLogger(NodeSelector.class.getName());
        if (log.isLoggable(FINER)) {
            log.finer("selectMainNodes(canHaveDetails = "               //NOI18N
                      + canHaveDetails + ')');
        }

        if (!canHaveDetails) {
            return simpleSelect(selectedNodes);
        } else {
            return new NodeSelector(selectedNodes).selectMainNodes();
        }
    }

    /**
     */
    private static List<TreePath> simpleSelect(TreePath[] selectedNodes) {
        final TreePath rootNode = findRootNode(selectedNodes[0]);
        int rootNodeIndex = -1;
        for (int i = 0; i < selectedNodes.length; i++) {
            if (selectedNodes[i] == rootNode) {
                rootNodeIndex = i;
                break;
            }
        }

        if (rootNodeIndex == -1) {
            return Arrays.asList(selectedNodes);
        } else {
            if (rootNodeIndex == 0) {
                return Arrays.asList(selectedNodes)
                             .subList(1, selectedNodes.length);
            } else if (rootNodeIndex == selectedNodes.length - 1) {
                return Arrays.asList(selectedNodes)
                             .subList(0, selectedNodes.length - 1);
            } else {
                List<TreePath> result
                        = new ArrayList<TreePath>(selectedNodes.length - 1);
                for (int i = 0; i < rootNodeIndex; i++) {
                    result.add(selectedNodes[i]);
                }
                for (int i = rootNodeIndex + 1; i < selectedNodes.length; i++) {
                    result.add(selectedNodes[i]);
                }
                assert result.size() == selectedNodes.length - 1;
                return result;
            }
        }
    }

    /**
     */
    private NodeSelector(TreePath[] nodes) {
        this.nodes = nodes;
        this.map = new LinkedHashMap<TreePathWrapper,Boolean>(
                                      (int) (nodes.length * 1.35 + .5),
                                      0.75f);
    }

    /**
     */
    private List<TreePath> selectMainNodes() {
        final boolean finest = log.isLoggable(FINEST);

        final TreePath rootNode = findRootNode(nodes[0]);

        if (finest) {
            log.finest("Selected nodes:");                              //NOI18N
            for (TreePath current : nodes) {
                if (current == rootNode) {
                    log.finest("* <root>");                             //NOI18N
                    continue;
                }
                TreePath parent = current.getParentPath();
                assert parent != null;
                if (parent == rootNode) {
                    log.finest("* " + getFileNodeName(current));        //NOI18N
                } else {
                    log.finest("* " + getDetailNodeName(current, -1));  //NOI18N
                }
            }
            log.finest("");                                             //NOI18N
        }
        for (TreePath current : nodes) {
            if (current == rootNode) {
                log.finest("*** <root> - filtered out");                //NOI18N
                continue;
            }

            assert (ldfn == null) == (ldn == null);
            assert (ldfn == null) || (ldfn == ldn.getParentPath());
            assert (current != ldfn) || (current != lsfn);

            TreePath parent = current.getParentPath();
            assert parent != null;
            if (finest) {
                log.finest("*** " + ((parent == rootNode)               //NOI18N
                                     ? getFileNodeName(current)
                                     : getDetailNodeName(current, -1)));
            }
            if (parent == rootNode) {
                /* it is a node representing a file */
                if (current == lsfn) {
                    /*
                     * when two or more detail nodes under this node had been
                     * selected, this node became selected, too
                     */
                    if (finest) {
                        log.finest(getFileNodeName(current)
                                   + " has been already selected");     //NOI18N
                    }
                    continue;
                }

                if ((ldfn != null) && (current != ldfn)) {
                    /* save information about the last selected detail node */
                    if (finest) {
                        log.finest("handlePendingDetailNode(...)");     //NOI18N
                    }
                    handlePendingDetailNode();
                }
                if (finest) {
                    log.finest("map.put("                               //NOI18N
                               + getFileNodeName(current)
                               + ", TRUE)");                            //NOI18N
                    log.finest("LSFN = " + getFileNodeName(current));   //NOI18N
                    log.finest("LDFN = null");                          //NOI18N
                    log.finest("LDN = null");                           //NOI18N
                }
                map.put(new TreePathWrapper(current), TRUE);
                lsfn = current;
                ldfn = null;
                ldn = null;
            } else if (parent == lsfn) {    //lsfn may be <null>
                /* its parent is the most recently selected file node */
                //do nothing
                if (finest) {
                    log.finest(getDetailNodeName(current)
                               + " - its parent is already selected");  //NOI18N
                }
            } else if (parent == ldfn) {    //ldfn may be <null>
                /* detail node added to the same parent as the last detail node */
                map.put(new TreePathWrapper(ldfn), TRUE);
                if (finest) {
                    log.finest(getDetailNodeName(current)
                               + " - at least second selected node"
                               + " under the same parent ("             //NOI18N
                               + getFileNodeName(parent)
                               + ") - will be added permanently");      //NOI18N
                    log.finest("map.put("                               //NOI18N
                               + getFileNodeName(ldfn)
                               + " /LDFN/, TRUE)");                     //NOI18N
                    log.finest("LSFN = "                                //NOI18N
                               + getFileNodeName(parent)
                               + " /parent/");                          //NOI18N
                    log.finest("LDFN = null");                          //NOI18N
                    log.finest("LDN = null");                           //NOI18N
                }
                lsfn = parent;
                ldfn = null;
                ldn = null;
            } else {
                /*
                 * detail node added to a node that is not selected,
                 * or under a node that is selected but is not the last
                 * selected file node
                 */
                if (finest) {
                    if (map.containsKey(new TreePathWrapper(parent))) {
                        log.finest(getDetailNodeName(current)
                                   + " - added under a file node ("     //NOI18N
                                   + getFileNodeName(parent)
                                   + ") that is selected but is not"    //NOI18N
                                   + " the most recently selected"      //NOI18N
                                   + " file node");                     //NOI18N
                    }
                }
                if (ldfn != null) {
                    /* save information about the last selected detail node */
                    if (finest) {
                        log.finest("handlePendingDetailNode(...)");     //NOI18N
                    }
                    handlePendingDetailNode();
                }
                if (parent == lsfn) {
                    ldfn = null;
                    ldn = null;
                } else {
                    ldfn = parent;
                    ldn = current;
                }
                if (finest) {
                    log.finest("LDFN = "                                //NOI18N
                               + getFileNodeName(parent)
                               + " /parent/");                          //NOI18N
                    log.finest("LDN = "                                 //NOI18N
                               + getDetailNodeName(current)
                               + " /current/");                         //NOI18N
                }
            }
        }
        /* Store information about the last pending detail node, if any: */
        if (ldfn != null) {
            if (finest) {
                log.finest("*** END");                                  //NOI18N
                log.finest("handlePendingDetailNode(...)");             //NOI18N
            }
            handlePendingDetailNode();
            ldfn = null;
            ldn = null;
        }

        /* Build the resulting list from information collected in the map: */
        log.finest("***");                                              //NOI18N
        log.finest("*** BUILDING RESULT LIST");                         //NOI18N
        List<TreePath> result = new ArrayList<TreePath>(map.size());
        if (!detailNodeAdded) {
            log.finest("No detail nodes - simple case");                //NOI18N
            for (Map.Entry<TreePathWrapper,Boolean> entry : map.entrySet()) {
                result.add(entry.getKey().treePath);
                if (finest) {
                    log.finest("+ "                                     //NOI18N
                               + getFileNodeName(entry.getKey().treePath));
                }
            }
        } else {
            for (Map.Entry<TreePathWrapper,Boolean> entry : map.entrySet()) {
                if (entry.getValue() == FALSE) {
                    /*
                     * file node that is not selected, having exactly one child node
                     * (detail node) selected
                     */
                    assert !entry.getKey().isDetail;
                    if (finest) {
                        log.finest("- "                                 //NOI18N
                                   + getFileNodeName(entry.getKey().treePath)
                                   + " (FALSE)");                       //NOI18N
                    }
                    continue;
                }

                TreePathWrapper wrapper = entry.getKey();
                if (!wrapper.isDetail) {
                    assert entry.getValue() == TRUE;
                    /* selected file node */
                    result.add(wrapper.treePath);
                    if (finest) {
                        log.finest("+ "                                 //NOI18N
                                   + getFileNodeName(wrapper.treePath));
                    }
                } else if (map.get(new TreePathWrapper(wrapper.treePath.getParentPath()))
                           != TRUE) {
                    /* selected detail node whose parent is not selected */
                    result.add(wrapper.treePath);
                    if (finest) {
                        log.finest("+ "                                 //NOI18N
                                   + getDetailNodeName(wrapper.treePath, -1));
                    }
                } else if (finest) {
                    log.finest("- "                                     //NOI18N
                               + getDetailNodeName(wrapper.treePath, -1)
                               + " - parent ("                          //NOI18N
                               + getFileNodeName(wrapper.treePath.getParentPath())
                               + ") selected");                         //NOI18N
                }
            }
        }
        map.clear();

        assert !result.isEmpty();
            // no essential node found
            //  - this should not happen because:
            //     - we know that there are at least two nodes selected
            //     - from each pair of selected nodes, at least one
            //       of them is essential
        return result;
    }

    /**
     * Saves information about a given detail node to the given map.
     * This method is only called from method {@link #selectMainNodes}.
     *
     * @param  map   map to store the information to
     * @param  ldn   detail node that should be added to the map
     * @param  ldfn  parent (file node) of the detail node
     * @param  log   logger of debugging messages
     * @return  {@code true} if the parent node ({@code ldfn}) became selected
     *          during the operation, {@code false} otherwise
     */
    private void handlePendingDetailNode() {
        assert ldfn != null;
        assert ldn != null;

        /* save information about the last selected detail node */
        final Boolean oldValue = map.put(new TreePathWrapper(ldfn), TRUE);
        if (oldValue == null) {
            /*
             * the detail node (ldn) is the only selected
             * detail node under its parent node so far
             * - we should mark the parent node as only
             * conditionally added
             */
            map.put(new TreePathWrapper(ldfn), FALSE);
            map.put(new TreePathWrapper(ldn), TRUE);
            detailNodeAdded = true;
            if (log.isLoggable(FINEST)) {
                log.finest("map.put("                                   //NOI18N
                           + getFileNodeName(ldfn)
                           + " /LDFN/, FALSE)");                        //NOI18N
                log.finest("map.put("                                   //NOI18N
                           + getDetailNodeName(ldn)
                           + " /LDN/, TRUE)");                          //NOI18N
            }
        } else if (oldValue == FALSE) {
            /*
             * The unhandled detail node (ldn) is the second
             * selected detail node under its parent (ldfn).
             * It means that parent should be added permanently,
             * which is exactly what was done by the above
             * assignment of value TRUE.
             */
            if (log.isLoggable(FINEST)) {
                log.finest(getFileNodeName(ldfn)
                           + " - membership status changed"             //NOI18N
                           + " (conditional -> permanent)");            //NOI18N
            }
            lsfn = ldfn;
            if (log.isLoggable(FINEST)) {
                log.finest("LSFN = "                                    //NOI18N
                           + getFileNodeName(ldfn)
                           + " /LDFN/");                                //NOI18N
            }
        } else {
            assert (oldValue == TRUE);
            /*
             * The detail node's parent (ldfn) had already been
             * added permanently so the above assigment of value
             * TRUE changes nothing, which is exactly what we need.
             */
        }
    }

    /**
     * Class wrapping {@code TreePath} objects for the purpose of overriding
     * methods {@code equals(...)} and {@code hashCode()}.
     */
    private static final class TreePathWrapper {
        private final TreePath treePath;
        private final Object lastPathComp;
        private final boolean isDetail;
        TreePathWrapper(TreePath node) {
            this.treePath = node;
            this.lastPathComp = node.getLastPathComponent();
            this.isDetail = (lastPathComp.getClass() != MatchingObject.class);
        }
        @Override
        public boolean equals(Object o) {
            if ((o == null) || (o.getClass() != TreePathWrapper.class)) {
                return false;
            }

            TreePathWrapper other = (TreePathWrapper) o;
            return (other.isDetail == this.isDetail)
                   && other.lastPathComp.equals(this.lastPathComp);
        }

        @Override
        public int hashCode() {
            int hash = lastPathComp.hashCode();
            if (isDetail) {
                hash += 5;
            }
            return hash;
        }
    }

    private static String getFileNodeName(TreePath fileNode) {
        return ((MatchingObject) fileNode.getLastPathComponent()).getName();
    }

    private static String getDetailNodeName(TreePath detailNode) {
        return getDetailNodeName(detailNode, 15);
    }

    private static String getDetailNodeName(TreePath detailNode, int length) {
        String name = ((TextDetail.DetailNode) detailNode.getLastPathComponent())
                      .getName().trim();
        if (length != -1) {
            return name.substring(0, length);
        }

        int bracketIndex = name.indexOf('[');
        if (bracketIndex != -1) {
            name = name.substring(0, bracketIndex);
        }
        return name.trim();
    }

    /**
     * Finds the root part of the given {@code TreePath}.
     * 
     * @param  node  tree path whose root path is to be found
     * @param  ascendant of the given tree path that does not have any parent
     */
    private static TreePath findRootNode(TreePath node) {
        TreePath parent;
        while ((parent = node.getParentPath()) != null) {
            node = parent;
        }
        return node;
    }
    

}
