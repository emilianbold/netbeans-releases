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
<!-- apps/frontend/templates/layout.php -->
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
    "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
    <head>
        <title>
            <?php if (!include_slot('title')): ?>
            Jobeet - Your best job board
            <?php endif; ?>
        </title>
        <link rel="shortcut icon" href="/favicon.ico" />
        <link rel="alternate" type="application/atom+xml" title="Latest Jobs" href="<?php echo url_for('@job?sf_format=atom', true) ?>" />

        <?php include_stylesheets() ?>

        <?php use_javascript('jquery-1.3.min.js') ?>
        <?php use_javascript('search.js') ?>

        <?php include_javascripts() ?>
    </head>
    <body>
        <div id="container">
            <div id="header">
                <div class="content">
                    <h1>
                        <a href="<?php echo url_for('@homepage') ?>">
                            <img src="/images/jobeet.gif" alt="Jobeet Job Board" />
                        </a>
                    </h1>

                    <div id="sub_header">
                        <div class="post">
                            <h2>Ask for people</h2>
                            <div>
                                <a href="<?php echo url_for('@job_new') ?>">Post a Job</a>
                            </div>
                        </div>

                        <div class="search">
                            <h2>Ask for a job</h2>
                            <form action="<?php echo url_for('@job_search') ?>" method="get">
                                <input type="text" name="query" value="<?php echo $sf_request->getParameter('query') ?>" id="search_keywords" />
                                <input type="submit" value="search" />
                                <img id="loader" src="/images/loader.gif" style="vertical-align: middle; display: none" />
                                <div class="help">
                                    Enter some keywords (city, country, position, ...)
                                </div>
                            </form>
                        </div>
                    </div>
                </div>
            </div>

            <div id="content">
                <?php if ($sf_user->hasFlash('notice')): ?>
                <div class="flash_notice">
                        <?php echo $sf_user->getFlash('notice') ?>
                </div>
                <?php endif; ?>

                <?php if ($sf_user->hasFlash('error')): ?>
                <div class="flash_error">
                        <?php echo $sf_user->getFlash('error') ?>
                </div>
                <?php endif; ?>

                <div id="job_history">
                    Recent viewed jobs:
                    <ul>
                        <?php foreach ($sf_user->getJobHistory() as $job): ?>
                        <li>
                                <?php echo link_to($job->getPosition() . ' - ' . $job->getCompany(), 'job_show_user', $job) ?>
                        </li>
                        <?php endforeach; ?>
                    </ul>
                </div>

                <div class="content">
                    <?php echo $sf_content ?>
                </div>
            </div>

            <div id="footer">
                <div class="content">
                    <span class="symfony">
                        <img src="/images/jobeet-mini.png" />
                        powered by <a href="http://www.symfony-project.org/">
                            <img src="/images/symfony.gif" alt="symfony framework" /></a>
                    </span>
                    <ul>
                        <!--
                      <li>
                        <a href=""><?php echo __('About Jobeet') ?></a>
                      </li>
                        -->
                        <li class="feed">
                            <?php echo link_to(__('Full feed'), '@job?sf_format=atom') ?>
                        </li>
                        <!--
                      <li>
                        <a href=""><?php echo __('Jobeet API') ?></a>
                      </li>
                        -->
                        <li class="last">
                            <?php echo link_to(__('Become an affiliate'), '@affiliate_new') ?>
                        </li>
                    </ul>
                    <?php include_component('sfJobeetLanguage', 'language') ?>
                </div>
            </div>
        </div>
    </body>
</html>