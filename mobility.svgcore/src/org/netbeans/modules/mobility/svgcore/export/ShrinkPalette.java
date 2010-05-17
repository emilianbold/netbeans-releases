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
package org.netbeans.modules.mobility.svgcore.export;

public final class ShrinkPalette {
    private static final int     MAX_RGB        = 255;
    private static final int     MAX_NODES      = 266817;
    private static final int     MAX_TREE_DEPTH = 8;
    private static final int[]   SQUARES;
    private static final int[]   SHIFT;
    
    static {
        SQUARES = new int[MAX_RGB + MAX_RGB + 1];
        for (int i = -MAX_RGB; i <= MAX_RGB; i++) {
            SQUARES[i + MAX_RGB] = i * i;
        }

        SHIFT = new int[MAX_TREE_DEPTH + 1];
        for (int i = 0; i < MAX_TREE_DEPTH + 1; ++i) {
            SHIFT[i] = 1 << (15 - i);
        }
    }

    public static int[] quantizeImage(int[][] pixels, int max_colors) {
        ColorCube cube = new ColorCube(pixels, max_colors);
        cube.clasify();
        cube.reduce();
        cube.assign();
        return cube.getColorMap();
    }
    
    private static class Search {
        private int distance;
        private int color_number;
    }

    private static class Node {
        private ColorCube m_cube;
        private Node      m_parent;
        private Node[]    m_child;
        private int       m_nchild;
        private int       m_id;
        private int       m_level;
        private int       m_mid_red;
        private int       m_mid_green;
        private int       m_mid_blue;
        private int       m_number_pixels;
        private int       m_unique;
        private int       m_total_alpha;
        private int       m_total_red;
        private int       m_total_green;
        private int       m_total_blue;
        private int       m_color_number;

        Node(ColorCube cube) {
            m_cube          = cube;
            m_parent        = this;
            m_child         = new Node[8];
            m_id            = 0;
            m_level         = 0;
            m_number_pixels = Integer.MAX_VALUE;
            m_mid_red       = (MAX_RGB + 1) >> 1;
            m_mid_green     = (MAX_RGB + 1) >> 1;
            m_mid_blue      = (MAX_RGB + 1) >> 1;
        }

        Node(Node parent, int id, int level) {
            m_cube   = parent.m_cube;
            m_parent = parent;
            m_child  = new Node[8];
            m_id     = id;
            m_level  = level;

            ++m_cube.m_nodes;
            if (level == m_cube.m_depth) {
                ++m_cube.m_colors;
            }

            ++parent.m_nchild;
            parent.m_child[id] = this;

            int bi      = (1 << (MAX_TREE_DEPTH - level)) >> 1;
            m_mid_red   = parent.m_mid_red + ((id & 1) > 0 ? bi : -bi);
            m_mid_green = parent.m_mid_green + ((id & 2) > 0 ? bi : -bi);
            m_mid_blue  = parent.m_mid_blue + ((id & 4) > 0 ? bi : -bi);
        }

        void removeChild() {
            --m_parent.m_nchild;
            m_parent.m_unique += m_unique;
            m_parent.m_total_alpha += m_total_alpha;
            m_parent.m_total_red += m_total_red;
            m_parent.m_total_green += m_total_green;
            m_parent.m_total_blue += m_total_blue;
            m_parent.m_child[m_id] = null;
            --m_cube.m_nodes;
            m_cube = null;
            m_parent = null;
        }

        void removeLevel() {
            if (m_nchild != 0) {
                for (int id = 0; id < 8; id++) {
                    if (m_child[id] != null) {
                        m_child[id].removeLevel();
                    }
                }
            }
            if (m_level == m_cube.m_depth) {
                removeChild();
            }
        }

        int reduce(int threshold, int next_threshold) {
            if (m_nchild != 0) {
                for (int id = 0; id < 8; id++) {
                    if (m_child[id] != null) {
                        next_threshold = m_child[id].reduce(threshold, next_threshold);
                    }
                }
            }
            if (m_number_pixels <= threshold) {
                removeChild();
            } else {
                if (m_unique != 0) {
                    m_cube.m_colors++;
                }
                if (m_number_pixels < next_threshold) {
                    next_threshold = m_number_pixels;
                }
            }
            return next_threshold;
        }

        void fillColorMaps() {
            if (m_nchild != 0) {
                for (int id = 0; id < 8; id++) {
                    if (m_child[id] != null) {
                        m_child[id].fillColorMaps();
                    }
                }
            }
            if (m_unique != 0) {
                int a = (m_total_alpha + (m_unique >> 1)) / m_unique;
                int r = (m_total_red + (m_unique >> 1)) / m_unique;
                int g = (m_total_green + (m_unique >> 1)) / m_unique;
                int b = (m_total_blue + (m_unique >> 1)) / m_unique;
                m_cube.m_colormap[m_cube.m_colors] = (((a & 0xFF) << 24) | ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | ((b & 0xFF) << 0));
                m_color_number = m_cube.m_colors++;
            }
        }

        void findNearestColor(int red, int green, int blue, Search search) {
            if (m_nchild != 0) {
                for (int id = 0; id < 8; id++) {
                    if (m_child[id] != null) {
                        m_child[id].findNearestColor(red, green, blue, search);
                    }
                }
            }

            if (m_unique != 0) {
                int color = m_cube.m_colormap[m_color_number];
                int distance = getDistance(color, red, green, blue);
                if (distance < search.distance) {
                    search.distance = distance;
                    search.color_number = m_color_number;
                }
            }
        }

        static final int getDistance(int color, int r, int g, int b) {
            return SQUARES[((color >> 16) & 0xFF) - r + MAX_RGB] + SQUARES[((color >> 8) & 0xFF) - g + MAX_RGB] + SQUARES[((color >> 0) & 0xFF) - b + MAX_RGB];
        }
    }
    
    private static class ColorCube {
        private final Node    m_root;
        private final int[][] m_pixels;
        private final int     m_max_colors;
        private int[]         m_colormap;
        private int           m_depth;
        private int           m_colors;
        private int           m_nodes;

        ColorCube(int[][] pixels, int max_colors) {
            m_pixels     = pixels;
            m_max_colors = max_colors;
            m_colors     = 1;

            int i = max_colors;
            for (m_depth = 1; i != 0; m_depth++) {
                i /= 4;
            }
            if (m_depth > 1) {
                --m_depth;
            }
            if (m_depth > MAX_TREE_DEPTH) {
                m_depth = MAX_TREE_DEPTH;
            } else if (m_depth < 2) {
                m_depth = 2;
            }
            m_root = new Node(this);
        }

        void clasify() {
            int[][] pixels = m_pixels;

            int width  = pixels.length;
            int height = pixels[0].length;

            for (int x = width; x-- > 0;) {
                for (int y = height; y-- > 0;) {
                    int pixel = pixels[x][y];
                    int alpha = (pixel >> 24) & 0xFF;
                    int red = (pixel >> 16) & 0xFF;
                    int green = (pixel >> 8) & 0xFF;
                    int blue = (pixel >> 0) & 0xFF;

                    if (alpha > 0) {
                        if (m_nodes > MAX_NODES) {
                            m_root.removeLevel();
                            --m_depth;
                        }

                        Node node = m_root;
                        for (int level = 1; level <= m_depth; ++level) {
                            int id = ((red > node.m_mid_red ? 1 : 0) << 0) | 
                                     ((green > node.m_mid_green ? 1 : 0) << 1) |
                                     ((blue > node.m_mid_blue ? 1 : 0) << 2);
                            if (node.m_child[id] == null) {
                                new Node(node, id, level);
                            }
                            node = node.m_child[id];
                            node.m_number_pixels += SHIFT[level];
                        }

                        ++node.m_unique;
                        node.m_total_alpha += alpha;
                        node.m_total_red += red;
                        node.m_total_green += green;
                        node.m_total_blue += blue;
                    }
                }
            }
        }

        void reduce() {
            int threshold = 1;
            while (m_colors > m_max_colors) {
                m_colors = 1;
                threshold = m_root.reduce(threshold, Integer.MAX_VALUE);
            }
        }

        void assign() {
            m_colormap    = new int[m_colors];
            m_colormap[0] = 0x00800000;
            m_colors      = 1;
            m_root.fillColorMaps();

            int[][] pixels = m_pixels;
            int width = pixels.length;
            int height = pixels[0].length;

            Search search = new Search();

            for (int x = width; x-- > 0;) {
                for (int y = height; y-- > 0;) {
                    int pixel = pixels[x][y];
                    int alpha = (pixel >> 24) & 0xFF;
                    int red = (pixel >> 16) & 0xFF;
                    int green = (pixel >> 8) & 0xFF;
                    int blue = (pixel >> 0) & 0xFF;

                    if (alpha > 0) {
                        Node node = m_root;
                        for (;;) {
                            int id = ((red > node.m_mid_red ? 1 : 0) << 0) |
                                    ((green > node.m_mid_green ? 1 : 0) << 1) |
                                    ((blue > node.m_mid_blue ? 1 : 0) << 2);
                            if (node.m_child[id] == null) {
                                break;
                            }
                            node = node.m_child[id];
                        }

                        search.distance = Integer.MAX_VALUE;
                        node.m_parent.findNearestColor(red, green, blue, search);
                        pixels[x][y] = search.color_number;
                    } else {
                        pixels[x][y] = 0;
                    }
                }
            }
        }
        
        public int []  getColorMap() {
            return m_colormap;
        }
    }
}
