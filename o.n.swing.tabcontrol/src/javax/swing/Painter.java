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

package javax.swing;

import java.awt.Graphics2D;

/**
 * <p>A painting delegate. The Painter interface defines exactly one method,
 * <code>paint</code>. It is used in situations where the developer can change
 * the painting routine of a component without having to resort to subclassing
 * the component. It is also generically useful when doing any form of painting
 * delegation.</p>
 *
 * <p><code>Painter</code>s are simply encapsulations of Java2D code and make
 * it fairly trivial to reuse existing <code>Painter</code>s or to combine
 * them together. Implementations of this interface are also trivial to write,
 * such that if you can't find a <code>Painter</code> that does what you need,
 * you can write one with minimal effort. Writing a <code>Painter</code> requires
 * knowledge of Java2D.</p>
 *
 * <p>A <code>Painter</code> may be created with a type parameter. This type will be
 * expected in the <code>paint</code> method. For example, you may wish to write a
 * <code>Painter</code> that only works with subclasses of {@link java.awt.Component}.
 * In that case, when the <code>Painter</code> is declared, you may declare that
 * it requires a <code>Component</code>, allowing the paint method to be type safe. Ex:
 * <pre><code>
 *     Painter<Component> p = new Painter<Component>() {
 *         public void paint(Graphics2D g, Component c, int width, int height) {
 *             g.setColor(c.getBackground());
 *             //and so forth
 *         }
 *     }
 * </code></pre></p>
 *
 * <p>This interface makes no guarantees of threadsafety.</p>
 *
 * @author rbair
 */
public interface Painter<T> {
    /**
     * <p>Renders to the given {@link java.awt.Graphics2D} object. Implementations
     * of this method <em>may</em> modify state on the <code>Graphics2D</code>, and are not
     * required to restore that state upon completion. In most cases, it is recommended
     * that the caller pass in a scratch graphics object. The <code>Graphics2D</code>
     * must never be null.</p>
     *
     * <p>State on the graphics object may be honored by the <code>paint</code> method,
     * but may not be. For instance, setting the antialiasing rendering hint on the
     * graphics may or may not be respected by the <code>Painter</code> implementation.</p>
     *
     * <p>The supplied object parameter acts as an optional configuration argument.
     * For example, it could be of type <code>Component</code>. A <code>Painter</code>
     * that expected it could then read state from that <code>Component</code> and
     * use the state for painting. For example, an implementation may read the
     * backgroundColor and use that.</p>
     *
     * <p>Generally, to enhance reusability, most standard <code>Painter</code>s ignore
     * this parameter. They can thus be reused in any context. The <code>object</code>
     * may be null. Implementations must not throw a NullPointerException if the object
     * parameter is null.</p>
     *
     * <p>Finally, the <code>width</code> and <code>height</code> arguments specify the
     * width and height that the <code>Painter</code> should paint into. More
     * specifically, the specified width and height instruct the painter that it should
     * paint fully within this width and height. Any specified clip on the
     * <code>g</code> param will further constrain the region.</p>
     *
     * <p>For example, suppose I have a <code>Painter</code> implementation that draws
     * a gradient. The gradient goes from white to black. It "stretches" to fill the
     * painted region. Thus, if I use this <code>Painter</code> to paint a 500 x 500
     * region, the far left would be black, the far right would be white, and a smooth
     * gradient would be painted between. I could then, without modification, reuse the
     * <code>Painter</code> to paint a region that is 20x20 in size. This region would
     * also be black on the left, white on the right, and a smooth gradient painted
     * between.</p>
     *
     * @param g The Graphics2D to render to. This must not be null.
     * @param object an optional configuration parameter. This may be null.
     * @param width width of the area to paint.
     * @param height height of the area to paint.
     */
    public void paint(Graphics2D g, T object, int width, int height);
}
