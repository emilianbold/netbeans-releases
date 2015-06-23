<?php
/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */

?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" dir="ltr" lang="en-US">
    <head>
        <?php include_title(); ?>
        <?php include_http_metas() ?>
        <?php include_metas(); ?>

        <link rel="shortcut icon" href="<?php echo url_for('/favicon.ico'); ?>" type="image/x-icon" />
        <?php include_stylesheets(); ?>

        <script type="text/javascript">
            var BASE_URL = '<?php echo htmlspecialchars('http://' . $sf_request->getHost() . $sf_request->getRelativeUrlRoot(), ENT_QUOTES); ?>';
        </script>
        <?php include_javascripts(); ?>

    </head>
    <body>
        <div id="bodyimg">
            <div id="pagebox">
                <div id="top">
                    <h2><a href="<?php echo url_for('content/index'); ?>"
                           title="Go to Homepage">
                            <img src="<?php echo url_for('/images/netbeans-logo.gif'); ?>" height="93" alt="Logo - Rent A Flat" /></a>
                        <span>
                            Rent-a-Flat with NetBeans<br />
                            Symfony Framework Sample<br />
                            brought to you by<br />
                            <b>PHP NetBeans Dev Team!</b>
                        </span>
                    </h2>
                </div>

                <div id="menu">
                    <ul>
                        <li class="m1"><a href="<?php echo url_for('content/index'); ?>" title=""><strong><span class="ml"></span><span class="mr"></span>Homepage</strong></a></li>

                        <li class="modd"></li>
                        <li class="m2"><a href="<?php echo url_for('property/all'); ?>" title=""><strong><span class="ml"></span><span class="mr"></span>Offer</strong></a></li>

                        <li class="modd"></li>
                        <li class="m3"><a href="<?php echo url_for('property/add'); ?>" title=""><strong><span class="ml"></span><span class="mr"></span>Add new offer</strong></a></li>

                        <li class="modd"></li>
                        <li class="m4"><a href="<?php echo url_for('my-favorites/index'); ?>" title="">
                                <strong>
                                    <span class="ml"></span>
                                    <span class="mr"></span>My Favorites
                                </strong>
                            </a>
                        </li>
                        <li class="modd"></li>
                        <li class="m5"><a href="<?php echo url_for('contacts/index'); ?>" title=""><strong><span class="ml"></span><span class="mr"></span>Contacts</strong></a></li>
                    </ul>
                </div>
                <div class="cleaner"></div>

                <div id="conbox">
                    <div id="skip" class="none"><a name="skip"></a></div>
                    <div id="con">
                        <?php echo $sf_content; ?>
                        <div class="cleaner"></div>
                    </div>
                </div>
                <div class="cleaner"></div>

                <div id="footbox"><div id="foot">
                    <div class="fo f1">
                        <h2>Contacts</h2>
                        <p class="kon"> <strong>Branch Prague</strong><br />
                            V parku 2308/8<br />
                            140 00  Prague 4<br />
                            <br />
                        </p>
                        <p class="kon"> <strong>Headquarters</strong><br />
                            Oracle Pkwy<br />
                            Redwood City, CA 94065<br />
                            <br />
                        </p>
                        <div class="cleaner"></div>
                    </div>
                    <div class="fo f2">
                        <a href="http://netbeans.org/" target="_blank" title="Go to NetBeans homepage">
                            <img src="<?php echo url_for('/images/netbeans-logo-2.png'); ?>" width="100" alt="NetBeans" />
                        </a>
                    </div>
                    <div class="fo f3">
                        <h2>Copyright</h2>
                        <p>2015 &copy; Copyright<br />
                            Oracle corp.<br />
                            All rights reserved<br /><br />
                        </p>
                    </div>
                </div></div>
                <div class="cleaner"></div>

            </div>
        </div>

    </body>
</html>
