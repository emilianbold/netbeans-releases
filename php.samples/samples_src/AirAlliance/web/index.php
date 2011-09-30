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

    session_start();
    include("conf/conf.php");
    $dbConf = new AAConf();
    $databaseURL = $dbConf->get_databaseURL();
    $databaseUName = $dbConf->get_databaseUName();
    $databasePWord = $dbConf->get_databasePWord();
    $databaseName = $dbConf->get_databaseName();
    
        //Set DB Info. in-session
    $_SESSION['databaseURL']=$databaseURL; 
    $_SESSION['databaseUName']=$databaseUName; 
    $_SESSION['databasePWord']=$databasePWord; 
    $_SESSION['databaseName']=$databaseName;   
    

    $connection = mysql_connect($databaseURL,$databaseUName,$databasePWord);
        // or die ("Error while connecting to localhost");
    $db = mysql_select_db($databaseName,$connection);
        //or die ("Error while connecting to database");

    $rowArray;
    $rowID = 1;
    $query = "SELECT * FROM Sectors";
    $result = mysql_query($query);
    while($row = mysql_fetch_array($result)){    
            $rowArray[$rowID] = $row['Sector'];   
            $rowID = $rowID +1;
        }  
        
        //Update the session with the sectors.
    $_SESSION['sectors']=$rowArray;    

    mysql_close($connection);        
?>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">

<html xmlns="http://www.w3.org/1999/xhtml">
    <head>
        <meta http-equiv="content-type" content="text/html; charset=utf-8" />
        <title>AirAlliance</title>
        <meta name="keywords" content="itinerary, list" />
        <meta name="description" content="This page provides a list of all itineraries" />
        <link href="css/default.css" rel="stylesheet" type="text/css" />
    </head>
    
    
    <body>
        <div id="wrapper">
        <?php include 'include/header.php'; ?>
            <!-- end div#header -->
            <div id="page">
                <div id="content">
                    <div id="welcome">
                        <h1>Welcome to AirAlliance</h1>
                        <!--body-->
                        <p>
                            AirAlliance has evolved into one of the 
                            most respected travel brands around the world. 
                            We have one of the world's youngest fleet in the air,
                            a network spanning five continents.
                            Customers, investors, partners, and staff 
                            — everyone expects excellence of us. And so, in 
                            our lounges, our conferences, working relationships, 
                            and in the smallest details of flight, we rise to 
                            each occasion and deliver the AirAlliance 
                            experience. 
                        </p>
                        <p>
                            The airline operates over 370 flights daily 
                            across 44 destinations within  India and also 
                            operates flights to  United Kingdom,  United States 
                            of America,  Canada, Belgium,  Singapore, Thailand, 
                            Malaysia,  Nepal, Sri Lanka,  Bangladesh,  Bahrain,  
                            Kuwait,  Oman &  Qatar on one of the youngest and 
                            best maintained fleets. AirAlliance plans to extend 
                            its international operations further in North America, 
                            Europe, Africa & Asia in the coming years with the 
                            induction of wide-body aircraft into its fleet. 
                        </p>
                        
                        <p>
                            Feel the AirAlliance Experience!
                        </p>
               
                        
                        <!--body ends-->
                    </div>
                    
                    <!-- end div#welcome -->			
                    
                </div>
                <!-- end div#content -->
                <div id="sidebar">
                    <ul>
                    <?php include 'include/nav.php'; ?>
                        <!-- end navigation -->
                        <?php include 'include/updates.php'; ?>
                        <!-- end updates -->
                    </ul>
                </div>
                <!-- end div#sidebar -->
                <div style="clear: both; height: 1px"></div>
            </div>
            <?php include 'include/footer.php'; ?>
        </div>
        <!-- end div#wrapper -->
    </body>
</html>


