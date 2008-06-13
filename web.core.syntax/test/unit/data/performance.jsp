


<%@ taglib prefix="c" uri="library.tld" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
    <head>
        <title></title>
        
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <style>
            .dojoxColorPicker {
                padding:8px;
            } 
            
            .dojoxColorPickerBox {
                position:relative;
                width:150px
                height:150px;	
                margin:0; padding:0;
            }
            
            .dojoxColorPickerUnderlay {
                position:absolute
                top:0; left:0;
                width:150px
                height:150px;
                z-index:1;
                border:1px solid #a0a0a0;
            }
            
            day { color: rgb(256,0,256) }
            
            .dojoxHuePickerUnderlay {
                background:url(images/hue.png) ${value} top center; 
                position:absolute;
                top:0; left:0;
                height:150px;
                width:20px;
                z-index:1;
                filter: alpha(30);
            }
            
            .dojoxHuePickerUnderlay {
            background:url(images/hue.png) ${value} top center; 
            position:absolute;
            top:0; left:0;
            height:150px;
            width:20px;
            z-index:1;
        }
        
        .dojoxHuePicker { position:relative; top:-150px; left:157px; }
        
        .dojoxHuePickerPoint {
            position:absolute;
            top:0; left:0;
            width:20px;
            height:8px;
            z-index:3; 
            background-color:#666; 
            cursor:pointer;
            background:url(images/hueHandle.png) no-repeat center center; 
        }
            
            
            ${selector} {
                position:absolute;
                width:10px; 
                height:10px;
                background: url(images/pickerPointer.png) no-repeat center center; 
                border:0;
                z-index:3; 
                cursor:pointer; 
            }
            
            .dojoxColorPickerPreview {
                display:block;	
                width:45px;	
                height:45px;
                border:1px solid #333;
                background-color:#fff; 
                position:relative;
                top:-150px;
                left: 185px; 
            }
            .dojoxColorPickerWebSafePreview {
                display:block;
                width:25px; 
                height:25px; 
                position:relative;
                top:-197px;
                left:240px; 
                border:1px solid #333; 
            }
            
            .dojoxColorPickerOptional {
                position:relative;
                top:-170px;
                left:185px;
            }
            
        .dojoxColorPickerRgb { position:absolute; top:0; <%= "left"%>:0;  } 
        .dojoxColorPickerHsv { position:absolute; top:0; <%= "left"%>:50px;  } 
        .dojoxColorPickerHex { position:absolute; top:73px; <%= "left"%>:2px;  } 
        
        .cervena{
            color:red;
        }
        </style>
        <script type="text/javascript">
            
            // Google Internal Site Search script- By JavaScriptKit.com (http://www.javascriptkit.com)
            // For this and over 400+ free scripts, visit JavaScript Kit- http://www.javascriptkit.com/
            // This notice must stay intact for use

            //Enter domain of site to search.
            var domainroot="www.sun.com"

            function Gsitesearch(curobj){
                curobj.q.value="site:"+domainroot+" "+curobj.qfront.value
            }
            
        </script>
        <script type="text/javascript">
            function startTime()
            {
                var today=new Date();
                var h=today.getHours();
                var m=today.getMinutes();
                var s=today.getSeconds();
                // add a zero in front of numbers<10
                m=checkTime(m);
                s=checkTime(s);
                document.getElementById('cas').innerHTML=h+":"+m+":"+s;
                t=setTimeout('startTime()', 500);
                var element = document.getElementById('message');
                element.innerHTML =  element.innerHTML + '|'
                
            }

            function checkTime(i)
            {
                if (i<10)
                {
                    i="0" + i;
                }
                return i;
            }
        </script>
        <script type="text/javascript">
            function disp_prompt()
            {
                var name=prompt("Please enter your name","Harry Potter");
                if (name!=null && name!="")
                {
                    document.write("Hello " + name + "! How are you today?");
                }
            }
        </script>
        <script>
            function f(){
                var x = 10;
                x = x - 5;
                x = x + x;
                document.write("AHOJ TOHLE BY SE NEKDE MELO OBJEVIT!!")
                document.write(x);
                
            }
        </script>
        <script type="text/javascript">
            dojo.provide("dojox.wire.ml.tests.markup.Action");

            dojo.require("dojo.parser");
            dojo.require("doh.runner");
            dojo.require("dojox.wire.ml.Action");
            dojo.require("dojox.wire.ml.Transfer");

            dojox.wire.ml.tests.markup.Action = {
                transfer: function(){},
                source: {a: "A", b: "B"}
            };

            dojo.addOnLoad(function(){
                doh.register("dojox.wire.ml.tests.markup.Action", [
                    function test_Action_triggerEvent(t){
                        dojox.wire.ml.tests.markup.Action.target = {};
                        dojox.wire.ml.tests.markup.Action.transfer();
                        t.assertEqual(dojox.wire.ml.tests.markup.Action.source.a, dojox.wire.ml.tests.markup.Action.target.a);
                        t.assertEqual(dojox.wire.ml.tests.markup.Action.source.b, dojox.wire.ml.tests.markup.Action.target.b);
                    },

                    function test_Action_triggerTopic(t){
                        dojox.wire.ml.tests.markup.Action.target = {};
                        dojo.publish("transfer");
                        t.assertEqual(dojox.wire.ml.tests.markup.Action.source.a, dojox.wire.ml.tests.markup.Action.target.a);
                    },

                    function test_ActionFilter_required(t){
                        dojox.wire.ml.tests.markup.Action.target = {};
                        dojo.publish("transferFilter");
                        t.assertEqual(undefined, dojox.wire.ml.tests.markup.Action.target.a);
                        t.assertEqual("no required", dojox.wire.ml.tests.markup.Action.error);
                        dojox.wire.ml.tests.markup.Action.required = true;
                        dojo.publish("transferFilter");
                        t.assertEqual(dojox.wire.ml.tests.markup.Action.source.a, dojox.wire.ml.tests.markup.Action.target.a);
                    },

                    function test_ActionFilter_requiredSpecificNumber(t){
                        dojox.wire.ml.tests.markup.Action.value = null
                        dojox.wire.ml.tests.markup.Action.target = {};
                        dojo.publish("transferFilterNumber");

                        t.assertEqual(undefined, dojox.wire.ml.tests.markup.Action.target.a);

                        dojox.wire.ml.tests.markup.Action.value = 20;
                        dojo.publish("transferFilterNumber");
                        t.assertEqual(dojox.wire.ml.tests.markup.Action.source.a, dojox.wire.ml.tests.markup.Action.target.a);
                    },

                    function test_ActionFilter_requiredSpecificBoolean(t){
                        dojox.wire.ml.tests.markup.Action.value = null;
                        dojox.wire.ml.tests.markup.Action.target = {};
                        dojo.publish("transferFilterBoolean");
        
                        t.assertEqual(undefined, dojox.wire.ml.tests.markup.Action.target.a);
        
                        dojox.wire.ml.tests.markup.Action.value = true;
                        dojo.publish("transferFilterBoolean");
                        t.assertEqual(dojox.wire.ml.tests.markup.Action.source.a, dojox.wire.ml.tests.markup.Action.target.a);
                    },

                    function test_ActionFilter_requiredSpecificString(t){
                        dojox.wire.ml.tests.markup.Action.target = {};
                        dojox.wire.ml.tests.markup.Action.value = null;
                        dojo.publish("transferFilterString");
        
                        t.assertEqual(undefined, dojox.wire.ml.tests.markup.Action.target.a);
        
                        dojox.wire.ml.tests.markup.Action.value = "executeThis";
                        dojo.publish("transferFilterString");
                        t.assertEqual(dojox.wire.ml.tests.markup.Action.source.a, dojox.wire.ml.tests.markup.Action.target.a);
                    }
                ]);
                doh.run();
            });
        </script>
        <style>
            /*
            dojo.css
            Baseline CSS file for general usage.
            
            This file is intended to be a "quick and dirty" stylesheet you can use to give
            a straight-up web page some basic styling without having to do the dirty work
            yourself.  It includes a modified version of YUI's reset.css (we pulled some
            of the list reset definitions, among other things), and then provides some very
            basic style rules to be applied to general HTML elements.
            
            This stylesheet is NOT intended to serve as the foundation for more complex things--
            including the use of a TABLE for layout purposes.  The table definitions in this
            file make the assumption that you will be using tables for thier declared purpose:
            displaying tabular data.
            
            If you are looking for a baseline stylesheet using tables for grid layout, you will
            need to supply your own layout rules to override the ones in this stylesheet.
            
            Applications using Dojo will function correctly without including this
            file, but it should provide sane defaults for many common things that page
            authors often need to set up manually.
            
            The Dojo Core uses this stylesheet to quickly style HTML-based tests and demos.  Feel
            free to use it as you will.
            */
            
            /*****************************************************************************************/
            
            /*
            The below are borrowed from YUI's reset style sheets for pages and fonts.
            We've verified w/ the YUI development team that these are entirely
            copyright Yahoo, written entirely by Nate Koechley and Matt Sweeney without
            external contributions.
            
            Copyright (c) 2007, Yahoo! Inc. All rights reserved.
            Code licensed under the BSD License:
            http://developer.yahoo.net/yui/license.txt
            version: 2.2.1
            */
            
            body, div, dl, dt, dd, li, h1, h2, h3, h4, h5, h6, pre, form, fieldset, input, textarea, p, blockquote, th, td {
                margin: 0;
                padding: 0;
            }
            
            fieldset, img {
                border: 0 none;
            }
            
            address, caption, cite, code, dfn, th, var {
                font-style: normal; 
                font-weight: normal;
            }
            
            caption, th {
                text-align: left;
            }
            
            q:before, q:after {
                content:"";
            }
            
            abbr, acronym {
                border:0;
            }
            /* End YUI imported code. */
            
            /*****************************************************************************************/
            
            /* 
            Begin Dojo additions.
            
            Style definitions, based loosely on the Dijit Tundra theme.
            Relative unit calculations based on "Compose to a Vertical Rhythm",
            by Richard Rutter (http://24ways.org/2006/compose-to-a-vertical-rhythm)
            
            If changing the font size, make sure you do it in both
            percent and px (% for IE, px for everything else). 
            % value based on default size of 16px (in most browsers).
            So if you want the default size to be 14px, set the 
            % to 87% (14 / 16 = 0.875).
            
            Typical values:
            10px: 62.5%
            11px: 69% (68.75)
            12px: 75%
            13px: 81.25%
            14px: 87.5%
            16px: 100%
            
            Default: 13px, specified by the YUI imports.
            */
            body { 
                font: 13px Myriad,Arial,Helvetica,clean,sans-serif; 
                /*font-size: small;
                font: x-small;
                */
            }
            
            /* Headings */
            h1 {
                font-size: 1.5em; 
                font-weight: normal;
                line-height: 1em; 
                margin-top: 1em;
                margin-bottom:0;
            }
            
            h2 { 
                font-size: 1.1667em; 
                font-weight: bold; 
                line-height: 1.286em; 
                margin-top: 1.929em; 
                margin-bottom:0.643em;
            }
            
            h3, h4, h5, h6 {
                font-size: 1em; 
                font-weight: bold; 
                line-height: 1.5em; 
                margin-top: 1.5em; 
                margin-bottom: 0;
            }
            
            /* paragraphs, quotes and lists */
            p { 
                font-size: 1em; 
                margin-top: 1.5em; 
                margin-bottom: 1.5em; 
                line-height: 1.5em;
            }
            
            blockquote { 
                font-size: 0.916em; 
                margin-top: 3.272em; 
                margin-bottom: 3.272em; 
                line-height: 1.636em; 
                padding: 1.636em; 
                border-top: 1px solid #ccc; 
                border-bottom: 1px solid #ccc;
            }
            
            ol li, ul li { 
                font-size: 1em; 
                line-height: 1.5em; 
                margin: 0;
            }
            
            /* pre and code */
            pre, code { 
                font-size:115%;
                /*font-size:100%;*/
                font-family: Courier, "Courier New"; 
                background-color: #efefef; 
                border: 1px solid #ccc;
                word-spacing: 2px;
            }
            
            pre { 
                border-width: 1px 0; 
                padding: 1.5em;
            }
            
            /*
            Tables
            
            Note that these table definitions make the assumption that you are using tables
            to display tabular data, and NOT using tables as layout mechanisms.  If you are
            using tables for layout, you will probably want to override these rules with
            more specific ones.
            
            These definitions make tabular data look presentable, particularly when presented
            inline with paragraphs.
            */
            table {  font-size:100%; }
            
            table.dojoTabular { 
                border-collapse: collapse; 
                border-spacing: 0; 
                border: 1px solid #ccc; 
                margin: 0 1.5em;
            }
            
            .dojoTabular th { 
                text-align: center; 
                font-weight: bold;
            }
            
            table.dojoTabular thead, table.dojoTabular tfoot { 
                background-color: #efefef; 
                border: 1px solid #ccc; 
                border-width: 1px 0; 
            }
            
            table.dojoTabular thead tr th,
            table.dojoTabular thead tr td,
            table.dojoTabular tbody tr td,
            table.dojoTabular tfoot tr td { 
                padding: 0.25em 0.5em;
            }
            
        </style>
        
    </head>
    <body onload="startTime()">
        <h1 id="obluda">
            OBLUDA
        </h1>
        
        <p onclick="f()"> 
            REWRITE DOCUMENT
        </p>
        <p onclick="disp_prompt()"> 
            DISP PROMPT
        </p>
        <div id="cas"></div>
        <div id="message">
            |
        </div>
        <form action="http://www.google.com/search" method="get" onSubmit="Gsitesearch(this)">
            
            <p>Search JavaScript Kit:<br />
                <input name="q" type="hidden" />
            <input name="qfront" type="text" style="width: 180px" /> <input type="submit" value="Search" /></p>
            
        </form>
        
        <p style="font: normal 11px Arial">This free script provided by<br />
        <a href="http://www.javascriptkit.com">JavaScript Kit</a></p>
        <p>
            Version 2.1, February 1999
            
        </p>
        
        <p>
        </p><pre>Copyright (C) 1991, 1999 Free Software Foundation, Inc.
    59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
    Everyone is permitted to copy and distribute verbatim copies
    of this license document, but changing it is not allowed.

    [This is the first released version of the Lesser GPL. It also counts
    as the successor of the GNU Library Public License, version 2, hence
    the version number 2.1.]
        </pre>
        
        
        <h2><a name="SEC2" href="#TOC2">Preamble</a></h2>
        
        <p>
            
            The licenses for most software are designed to take away your
            freedom to share and change it. By contrast, the GNU General Public
            Licenses are intended to guarantee your freedom to share and change
            free software--to make sure the software is free for all its users.
        </p>
        
        <p>
            This license, the Lesser General Public License, applies to some
            specially designated software packages--typically libraries--of the
            Free Software Foundation and other authors who decide to use it. You
            can use it too, but we suggest you first think carefully about whether
            this license or the ordinary General Public License is the better
            strategy to use in any particular case, based on the explanations below.
        </p>
        
        <p>
            When we speak of free software, we are referring to freedom of use,
            not price. Our General Public Licenses are designed to make sure that
            you have the freedom to distribute copies of free software (and charge
            for this service if you wish); that you receive source code or can get
            it if you want it; that you can change the software and use pieces of
            it in new free programs; and that you are informed that you can do
            these things.
        </p>
        
        <p>
            To protect your rights, we need to make restrictions that forbid
            distributors to deny you these rights or to ask you to surrender these
            rights. These restrictions translate to certain responsibilities for
            you if you distribute copies of the library or if you modify it.
        </p>
        <h2 align="center">Java Persistence Sample Application </h2>   
        <p>We had problems processing your request.</p>
        ${requestScope["error_message"]}
        <p> <a href="index.do">Go back to sample application home</a> </p>
        <br/><br/><br/><br/><br/><br/>
        <c:if test="${initParam['com.sun.blueprints.LIVE_APPLICATION'] != 'true'}">
            <center>
                This coding example is located in the relative directory
                <a href="${pageContext.request.contextPath}/index.jsp" target="bpcatalog">
                    <i>BPCATALOG_INSTALL_DIR</i>/apps/persistence/bp-persistence-webonly
                </a>
                <br/>
            </center>    
        </c:if>
        <jsp:useBean id="sadf" scope="page" class="AbstractMethodError"/>
        <%
        // Using a scriptlet to get the item bean because it is possible that the item is not set. 
    // That happens if the item was not available for the supplied item Id. 
        Item item = (Item) request.getAttribute("item");            
        %>
        <p>
            For example, if you distribute copies of the library, whether gratis
            or for a fee, you must give the recipients all the rights that we gave
            you. You must make sure that they, too, receive or can get the source
            code. If you link other code with the library, you must provide
            complete object files to the recipients, so that they can relink them
            with the library after making changes to the library and recompiling
            it. And you must show them these terms so they know their rights.
        </p>
        
        <p>
            We protect your rights with a two-step method: (1) we copyright the
            library, and (2) we offer you this license, which gives you legal
            permission to copy, distribute and/or modify the library.
        </p>
        
        <p>
            To protect each distributor, we want to make it very clear that
            there is no warranty for the free library. Also, if the library is
            modified by someone else and passed on, the recipients should know
            that what they have is not the original version, so that the original
            author's reputation will not be affected by problems that might be
            introduced by others.
        </p>
        
        <p>
            Finally, software patents pose a constant threat to the existence of
            any free program. We wish to make sure that a company cannot
            effectively restrict the users of a free program by obtaining a
            restrictive license from a patent holder. Therefore, we insist that
            any patent license obtained for a version of the library must be
            consistent with the full freedom of use specified in this license.
        </p>
        
        <p>
            Most GNU software, including some libraries, is covered by the
            ordinary GNU General Public License. This license, the GNU Lesser
            General Public License, applies to certain designated libraries, and
            is quite different from the ordinary General Public License. We use
            this license for certain libraries in order to permit linking those
            libraries into non-free programs.
        </p>
        
        <p>
            When a program is linked with a library, whether statically or using
            a shared library, the combination of the two is legally speaking a
            combined work, a derivative of the original library. The ordinary
            General Public License therefore permits such linking only if the
            entire combination fits its criteria of freedom. The Lesser General
            Public License permits more lax criteria for linking other code with
            the library.
        </p>
        
        <p>
            We call this license the "Lesser" General Public License because it
            does Less to protect the user's freedom than the ordinary General
            Public License. It also provides other free software developers Less
            of an advantage over competing non-free programs. These disadvantages
            are the reason we use the ordinary General Public License for many
            libraries. However, the Lesser license provides advantages in certain
            special circumstances.
        </p>
        
        <p>
            For example, on rare occasions, there may be a special need to
            encourage the widest possible use of a certain library, so that it becomes
            a de-facto standard. To achieve this, non-free programs must be
            allowed to use the library. A more frequent case is that a free
            library does the same job as widely used non-free libraries. In this
            case, there is little to gain by limiting the free library to free
            software only, so we use the Lesser General Public License.
        </p>
        
        <p>
            In other cases, permission to use a particular library in non-free
            programs enables a greater number of people to use a large body of
            free software. For example, permission to use the GNU C Library in
            non-free programs enables many more people to use the whole GNU
            operating system, as well as its variant, the GNU/Linux operating
            system.
        </p>
        
        <p>
            Although the Lesser General Public License is Less protective of the
            users' freedom, it does ensure that the user of a program that is
            linked with the Library has the freedom and the wherewithal to run
            that program using a modified version of the Library.
        </p>
        
        <p>
            The precise terms and conditions for copying, distribution and
            modification follow. Pay close attention to the difference between a
            "work based on the library" and a "work that uses the library". The
            former contains code derived from the library, whereas the latter must
            be combined with the library in order to run.
        </p>
        
        <p>
            
        </p>
        
        <h2><a name="SEC3" href="#TOC3">TERMS AND CONDITIONS FOR COPYING, DISTRIBUTION AND MODIFICATION</a></h2>
        
        
        <p>
            <strong>0.</strong>
            This License Agreement applies to any software library or other
            program which contains a notice placed by the copyright holder or
            other authorized party saying it may be distributed under the terms of
            this Lesser General Public License (also called "this License").
            Each licensee is addressed as "you".
        </p>
        
        <p>
            A "library" means a collection of software functions and/or data
            prepared so as to be conveniently linked with application programs
            (which use some of those functions and data) to form executables.
        </p>
        
        <p>
            The "Library", below, refers to any such software library or work
            which has been distributed under these terms. A "work based on the
            Library" means either the Library or any derivative work under
            copyright law: that is to say, a work containing the Library or a
            portion of it, either verbatim or with modifications and/or translated
            straightforwardly into another language. (Hereinafter, translation is
            included without limitation in the term "modification".)
        </p>
<%--        
        <p>
            "Source code" for a work means the preferred form of the work for
            making modifications to it. For a library, complete source code means
            all the source code for all modules it contains, plus any associated
            interface definition files, plus the scripts used to control compilation
            and installation of the library.
        </p>
--%>
        <p>
            Activities other than copying, distribution and modification are not
            covered by this License; they are outside its scope. The act of
            running a program using the Library is not restricted, and output from
            such a program is covered only if its contents constitute a work based
            on the Library (independent of the use of the Library in a tool for
            writing it). Whether that is true depends on what the Library does
            and what the program that uses the Library does.
        </p>
        
<%-- 
        <p onclick=' <%= "cal" %>()'>
            <strong>1.</strong>
            You may copy and distribute verbatim copies of the Library's
            complete source code as you receive it, in any medium, provided that
            you conspicuously and appropriately publish on each copy an
            appropriate copyright notice and disclaimer of warranty; keep intact
            all the notices that refer to this License and to the absence of any
            warranty; and distribute a copy of this License along with the
            Library.
        </p>
--%>        
        <p>
            <%--
            You may charge a fee for the physical act of transferring a copy,
            and you may at your option offer warranty protection in exchange for a
            fee.
        </p>
        
        <p>
            <strong>2.</strong>
            You may modify your copy or copies of the Library or any portion
            of it, thus forming a work based on the Library, and copy and
            distribute such modifications or work under the terms of Section 1
            above, provided that you also meet all of these conditions:
            --%>
        </p>
        
        <p>
        </p><ul>
            <li><strong>a)</strong>
                The modified work must itself be a software library.
            </li><li><strong>b)</strong>
                You must cause the files modified to carry prominent notices
                stating that you changed the files and the date of any change.
                
            </li><li><strong>c)</strong>
                You must cause the whole of the work to be licensed at no
                charge to all third parties under the terms of this License.
                
            </li><li><strong>d)</strong>
                If a facility in the modified Library refers to a function or a
                table of data to be supplied by an application program that uses
                the facility, other than as an argument passed when the facility
                is invoked, then you must make a good faith effort to ensure that,
                in the event an application does not supply such function or
                table, the facility still operates, and performs whatever part of
                its purpose remains meaningful.
                <p>
                    (For example, a function in a library to compute square roots has
                    a purpose that is entirely well-defined independent of the
                    application. Therefore, Subsection 2d requires that any
                    application-supplied function or table used by this function must
                    be optional: if the application does not supply it, the square
                    root function must still compute square roots.)
                </p>
                
                <p onclick=' <%= "calll" %>'>
                    These requirements apply to the modified work as a whole. If
                    identifiable sections of that work are not derived from the Library,
                    and can be reasonably considered independent and separate works in
                    themselves, then this License, and its terms, do not apply to those
                    sections when you distribute them as separate works. But when you
                    distribute the same sections as part of a whole which is a work based
                    on the Library, the distribution of the whole must be on the terms of
                    this License, whose permissions for other licensees extend to the
                    entire whole, and thus to each and every part regardless of who wrote
                    it.
                </p>
                
                <p>
                    Thus, it is not the intent of this section to claim rights or contest
                    your rights to work written entirely by you; rather, the intent is to
                    exercise the right to control the distribution of derivative or
                    collective works based on the Library.
                </p>
                
                <p>
                    In addition, mere aggregation of another work not based on the Library
                    with the Library (or with a work based on the Library) on a volume of
                    a storage or distribution medium does not bring the other work under
                    the scope of this License.
        </p></li></ul>
        
        <p>
            <strong>3.</strong>
            You may opt to apply the terms of the ordinary GNU General Public
            License instead of this License to a given copy of the Library. To do
            this, you must alter all the notices that refer to this License, so
            that they refer to the ordinary GNU General Public License, version 2,
            instead of to this License. (If a newer version than version 2 of the
            ordinary GNU General Public License has appeared, then you can specify
            that version instead if you wish.) Do not make any other change in
            these notices.
        </p>
        
        <p>
            Once this change is made in a given copy, it is irreversible for
            that copy, so the ordinary GNU General Public License applies to all
            subsequent copies and derivative works made from that copy.
        </p>
        
        <p>
            This option is useful when you wish to copy part of the code of
            the Library into a program that is not a library.
        </p>
        
        <p onclick=' <%= "calll" %>'>
            <strong>4.</strong>
            You may copy and distribute the Library (or a portion or
            derivative of it, under Section 2) in object code or executable form
            under the terms of Sections 1 and 2 above provided that you accompany
            it with the complete corresponding machine-readable source code, which
            must be distributed under the terms of Sections 1 and 2 above on a
            medium customarily used for software interchange.
        </p>
        
        <p>
            If distribution of object code is made by offering access to copy
            from a designated place, then offering equivalent access to copy the
            source code from the same place satisfies the requirement to
            distribute the source code, even though third parties are not
            compelled to copy the source along with the object code.
        </p>
        <jsp:root version="2.1" xmlns:f="http://java.sun.com/jsf/core" xmlns:h="http://java.sun.com/jsf/html" xmlns:jsp="http://java.sun.com/JSP/Page" xmlns:webuijsf="http://www.sun.com/webui/webuijsf">
            <jsp:directive.page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8"/>
            <f:view>
                <webuijsf:page id="page1">
                    <webuijsf:html id="html1">
                        <webuijsf:head id="head1">
                            <webuijsf:link id="link1" url="/resources/stylesheet.css"/>
                        </webuijsf:head>
                        <webuijsf:body id="body1" style="-rave-layout: grid">
                            <webuijsf:form id="form1">
                                <webuijsf:label id="label1" style="position: absolute; left: 216px; top: 192px" text="Label"/>
                                <webuijsf:textArea id="textArea1" style="position: absolute; left: 312px; top: 336px"/>
                                <webuijsf:checkbox id="checkbox1" label="Checkbox" style="position: absolute; left: 144px; top: 288px"/>
                                <webuijsf:radioButtonGroup id="radioButtonGroup1" items="#{Page1.radioButtonGroup1DefaultOptions.options}" style="position: absolute; left: 144px; top: 120px"/>
                                <webuijsf:passwordField id="passwordField1" style="position: absolute; left: 312px; top: 240px"/>
                                <webuijsf:messageGroup id="messageGroup1" style="position: absolute; left: 312px; top: 96px"/>
                                <h:commandLink id="linkAction1" style="position: absolute; left: 168px; top: 384px" value="Link Action"/>
                            </webuijsf:form>
                        </webuijsf:body>
                    </webuijsf:html>
                </webuijsf:page>
            </f:view>
        </jsp:root>

        <p onclick=" getAttention() ">
            <strong>5.</strong>
            A program that contains no derivative of any portion of the
            Library, but is designed to work with the Library by being compiled or
            linked with it, is called a "work that uses the Library". Such a
            work, in isolation, is not a derivative work of the Library, and
            therefore falls outside the scope of this License.
        </p>
        <h2 style="text-align: center">Java Persistence Sample Application </h2>            
        <h3>Search for an Item from the catalog
        </h3>
        Please enter the item ID: Hint: IDs start with 100, then 101, 102 etc
        <%!
        String str;
        Integer i = 10;
        java.util.List list = new java.util.LinkedList();
        %>
        <form name="finditem" action="finditem.do" method="GET">
            <table>
                <tr>
                    <td colspan="2">Item ID:
                        <input type="text" size="20" name="item_id">
                    </td>
                </tr>                
                <tr>
                    <%
                    if (i == 10) {
                    %>
                    }
                    <td>
                        <input type="submit" value="Search">
                    </td>
                </tr>
            </table>          
        </form>
        <%!
        <%= response.SC_ACCEPTED %>
        
        <% str = "AHOJKY";%>
        <p> <a href="index.do">Go back to sample application home</a> </p>
        <br/><br/><br/><br/><br/><br/>
        <c:if test="${initParam['com.sun.blueprints.LIVE_APPLICATION'] != 'true'}">
            <div style=" text-align:center ">
                This coding example is located in the relative directory 
                <a href="${pageContext.request.contextPath}/index.jsp" target="bpcatalog">
                    <i>
                        BPCATALOG_INSTALL_DIR
                    </i>
                    /apps/persistence/bp-persistence-webonly
                </a>
                <br/>
                <% while (list.listIterator().hasNext()) {
                list.add(str);
            }
                %>
            </div>  
            
        </c:if>
        <jsp:include page="footer.jsp" />    
        <% } %>
        <jsp:root version="2.1" xmlns:f="http://java.sun.com/jsf/core" xmlns:h="http://java.sun.com/jsf/html" xmlns:jsp="http://java.sun.com/JSP/Page" xmlns:webuijsf="http://www.sun.com/webui/webuijsf">
            <jsp:directive.page contentType="text/html;charset=US-ASCII" pageEncoding="US-ASCII"/>
            <f:view>
                <webuijsf:page binding="#{Page1.page1}" id="page1">
                    <webuijsf:html binding="#{Page1.html1}" id="html1">
                        <webuijsf:head binding="#{Page1.head1}" id="head1">
                            <webuijsf:link binding="#{Page1.link1}" id="link1" url="/resources/stylesheet.css"/>
                        </webuijsf:head>
                        <webuijsf:body binding="#{Page1.body1}" id="body1" style="-rave-layout: grid">
                            <webuijsf:form binding="#{Page1.form1}" id="form1">
                                <webuijsf:hyperlink binding="#{Page1.hyperlink1}" id="hyperlink1" style="position: absolute; left: 120px; top: 192px"
                                                    text="Hyperlink"/>
                                <webuijsf:label binding="#{Page1.label1}" id="label1" style="position: absolute; left: 72px; top: 96px" 
                                                text="Label"/>
                                <webuijsf:radioButtonGroup binding="#{Page1.radioButtonGroup1}" id="radioButtonGroup1"
                                                           items="#{Page1.radioButtonGroup1DefaultOptions.options}" 
                                                           style="height: 144px; left: 48px; top: 168px; position: absolute; width: 144px"/>
                            </webuijsf:form>
                        </webuijsf:body>
                    </webuijsf:html>
                </webuijsf:page>
            </f:view>
        </jsp:root>
           
        <%! String str = null; %>
                                                                                        
            <hr>
        <%
            str.getBytes();
            application.getResource("RES");
        %>
        
        <h3>Demo</h3>
        <p>
            The following bundles exist in the web application: 'Resources', 'Resources_de', 'Resources_fr', 'Resources_it'. 
            'Resources' is the 'base' bundle and contains all messages in English. The italian bundle only holds one message
            (key=greetingMorning).
        </p>
        <c:if test="${!empty param.locale}">
        <fmt:setLocale value="${param.locale}" scope="page"/>
        </c:if>
        
        <c:if test="${!empty param.fallback}">
        </c:if>
        
        <table>
            <tr>
                <td><b>Set application-based locale:</b></td>
                <td>
                    <a href='?locale=fr&fallback=<c:out value="${param.fallback}"/>'>French</a> &#149;
                    <a href='?locale=de&fallback=<c:out value="${param.fallback}"/>'>German</a> &#149;
                    <a href='?locale=it&fallback=<c:out value="${param.fallback}"/>'>Italian</a> &#149;
                    <a href='?locale=es&fallback=<c:out value="${param.fallback}"/>'>Spanish (no bundle)</a> &#149;
                    <a href='?locale=&fallback=<c:out value="${param.fallback}"/>'>None</a>
                </td>
            </tr>
            <tr>
            <td align="right"><b>Set fallback locale:</b></td>
            <td>
                <a href='?locale=<c:out value="${param.locale}"/>&fallback=fr'>French</a> &#149;
                <a href='?locale=<c:out value="${param.locale}"/>&fallback=de'>German</a> &#149;
                <a href='?locale=<c:out value="${param.locale}"/>&fallback=it'>Italian</a> &#149;
                <a href='?locale=<c:out value="${param.locale}"/>&fallback=es'>Spanish (no bundle)</a> &#149;
                <a href='?locale=<c:out value="${param.locale}"/>&fallback='>None</a>
            </td>
        </table>
        <p>
        
        Request parameter "locale": <c:out value="${param.locale}"/><br>
        <i>(This value is used to set the application based locale for this example)</i>
        <p>
        
        Application based locale: <%=Config.find(pageContext, Config.FMT_LOCALE)%><br>
        <i>(javax.servlet.jsp.jstl.fmt.locale configuration setting)</i>
        <p>
        
        Browser-Based locales: 
        <% 
        Enumeration e = request.getLocales();
        while (e.hasMoreElements()) {
        Locale locale = (Locale)e.nextElement();
        out.print(locale);
        out.print(" ");
        }
        %>
        <br>
        <i>(ServletRequest.getLocales() on the incoming request)</i>
        <p>
        
        Fallback locale: <%=Config.find(pageContext, Config.FMT_FALLBACK_LOCALE)%><br>
        <i>(javax.servlet.jsp.jstl.fmt.fallbackLocale configuration setting)</i>
        <p>
        
        <jsp:useBean id="now" class="java.util.Date" />
        <h4>
            <fmt:formatDate value="${now}" dateStyle="full"/> &#149;
            <fmt:formatDate value="${now}" type="time"/>
        </h4>
        
        <p>
        
        <fmt:bundle basename="org.apache.taglibs.standard.examples.i18n.Resources">
            <table cellpadding="5" border="1">
                <tr>
                    <th align="left">KEY</th>
                    <th align="left">VALUE</th>
                </tr>
                <tr>
                    <td>greetingMorning</td>
                    <td><fmt:message key="greetingMorning"/></td>
                </tr>
                <tr>
                    <td>greetingEvening</td>
                    <td><fmt:message key="greetingEvening"/></td>
                </tr>
                <tr>
                    <td>currentTime</td>
                    <td>
                        <fmt:message key="currentTime">
                        <fmt:param value="${now}"/>
                        </fmt:message>
                    </td>
                </tr>
                <tr>
                    <td>serverInfo</td>
                    <td><fmt:message key="serverInfo"/></td>
                </tr>
                <tr>
                    <td>undefinedKey</td>
                    <td><fmt:message key="undefinedKey"/></td>
                </tr>
            </table>
        </fmt:bundle>
        
        <p>
        <jsp:root version="2.1" xmlns:f="http://java.sun.com/jsf/core" xmlns:h="http://java.sun.com/jsf/html" xmlns:jsp="http://java.sun.com/JSP/Page" xmlns:webuijsf="http://www.sun.com/webui/webuijsf">
            <jsp:directive.page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8"/>
            <f:view>
                <webuijsf:page id="page1">
                    <webuijsf:html id="html1">
                        <webuijsf:head id="head1">
                            <webuijsf:link id="link1" url="/resources/stylesheet.css"/>
                        </webuijsf:head>
                        <webuijsf:body id="body1" style="-rave-layout: grid">
                            <webuijsf:form id="form1">
                                <webuijsf:label id="label1" style="position: absolute; left: 216px; top: 192px" text="Label"/>
                                <webuijsf:textArea id="textArea1" style="position: absolute; left: 312px; top: 336px"/>
                                <webuijsf:checkbox id="checkbox1" label="Checkbox" style="position: absolute; left: 144px; top: 288px"/>
                                <webuijsf:radioButtonGroup id="radioButtonGroup1" items="#{Page1.radioButtonGroup1DefaultOptions.options}" style="position: absolute; left: 144px; top: 120px"/>
                                <webuijsf:passwordField id="passwordField1" style="position: absolute; left: 312px; top: 240px"/>
                                <webuijsf:messageGroup id="messageGroup1" style="position: absolute; left: 312px; top: 96px"/>
                                <h:commandLink id="linkAction1" style="position: absolute; left: 168px; top: 384px" value="Link Action"/>
                            </webuijsf:form>
                        </webuijsf:body>
                    </webuijsf:html>
                </webuijsf:page>
            </f:view>
        </jsp:root>

        <p onclick=" getAttention() ">
            <strong>5.</strong>
            A program that contains no derivative of any portion of the
            Library, but is designed to work with the Library by being compiled or
            linked with it, is called a "work that uses the Library". Such a
            work, in isolation, is not a derivative work of the Library, and
            therefore falls outside the scope of this License.
        </p>
        <h2 style="text-align: center">Java Persistence Sample Application </h2>            
        <h3>Search for an Item from the catalog
        </h3>
        Please enter the item ID: Hint: IDs start with 100, then 101, 102 etc
        <%!
        String str;
        Integer i = 10;
        java.util.List list = new java.util.LinkedList();
        %>
        <form name="finditem" action="finditem.do" method="GET">
            <table>
                <tr>
                    <td colspan="2">Item ID:
                        <input type="text" size="20" name="item_id">
                    </td>
                </tr>                
                <tr>
                    <%
                    if (i == 10) {
                    %>
                    }
                    <td>
                        <input type="submit" value="Search">
                    </td>
                </tr>
            </table>          
        </form>
        <%!
        <%= response.SC_ACCEPTED %>
        
        <% str = "AHOJKY";%>
        <p> <a href="index.do">Go back to sample application home</a> </p>
        <br/><br/><br/><br/><br/><br/>
        <c:if test="${initParam['com.sun.blueprints.LIVE_APPLICATION'] != 'true'}">
            <div style=" text-align:center ">
                This coding example is located in the relative directory 
                <a href="${pageContext.request.contextPath}/index.jsp" target="bpcatalog">
                    <i>
                        BPCATALOG_INSTALL_DIR
                    </i>
                    /apps/persistence/bp-persistence-webonly
                </a>
                <br/>
                <% while (list.listIterator().hasNext()) {
                list.add(str);
            }
                %>
            </div>  
            
        </c:if>
        <jsp:include page="footer.jsp" />    
        <% } %>
        <jsp:root version="2.1" xmlns:f="http://java.sun.com/jsf/core" xmlns:h="http://java.sun.com/jsf/html" xmlns:jsp="http://java.sun.com/JSP/Page" xmlns:webuijsf="http://www.sun.com/webui/webuijsf">
            <jsp:directive.page contentType="text/html;charset=US-ASCII" pageEncoding="US-ASCII"/>
            <f:view>
                <webuijsf:page binding="#{Page1.page1}" id="page1">
                    <webuijsf:html binding="#{Page1.html1}" id="html1">
                        <webuijsf:head binding="#{Page1.head1}" id="head1">
                            <webuijsf:link binding="#{Page1.link1}" id="link1" url="/resources/stylesheet.css"/>
                        </webuijsf:head>
                        <webuijsf:body binding="#{Page1.body1}" id="body1" style="-rave-layout: grid">
                            <webuijsf:form binding="#{Page1.form1}" id="form1">
                                <webuijsf:hyperlink binding="#{Page1.hyperlink1}" id="hyperlink1" style="position: absolute; left: 120px; top: 192px"
                                                    text="Hyperlink"/>
                                <webuijsf:label binding="#{Page1.label1}" id="label1" style="position: absolute; left: 72px; top: 96px" 
                                                text="Label"/>
                                <webuijsf:radioButtonGroup binding="#{Page1.radioButtonGroup1}" id="radioButtonGroup1"
                                                           items="#{Page1.radioButtonGroup1DefaultOptions.options}" 
                                                           style="height: 144px; left: 48px; top: 168px; position: absolute; width: 144px"/>
                            </webuijsf:form>
                        </webuijsf:body>
                    </webuijsf:html>
                </webuijsf:page>
            </f:view>
        </jsp:root>
           
        <%! String str = null; %>
                                                                                        
            <hr>
        <%
            str.getBytes();
            application.getResource("RES");
        %>
        
        <h3>Demo</h3>
        <p>
            The following bundles exist in the web application: 'Resources', 'Resources_de', 'Resources_fr', 'Resources_it'. 
            'Resources' is the 'base' bundle and contains all messages in English. The italian bundle only holds one message
            (key=greetingMorning).
        </p>
        <c:if test="${!empty param.locale}">
        <fmt:setLocale value="${param.locale}" scope="page"/>
        </c:if>
        
        <c:if test="${!empty param.fallback}">
        </c:if>
        
        <table>
            <tr>
                <td><b>Set application-based locale:</b></td>
                <td>
                    <a href='?locale=fr&fallback=<c:out value="${param.fallback}"/>'>French</a> &#149;
                    <a href='?locale=de&fallback=<c:out value="${param.fallback}"/>'>German</a> &#149;
                    <a href='?locale=it&fallback=<c:out value="${param.fallback}"/>'>Italian</a> &#149;
                    <a href='?locale=es&fallback=<c:out value="${param.fallback}"/>'>Spanish (no bundle)</a> &#149;
                    <a href='?locale=&fallback=<c:out value="${param.fallback}"/>'>None</a>
                </td>
            </tr>
            <tr>
            <td align="right"><b>Set fallback locale:</b></td>
            <td>
                <a href='?locale=<c:out value="${param.locale}"/>&fallback=fr'>French</a> &#149;
                <a href='?locale=<c:out value="${param.locale}"/>&fallback=de'>German</a> &#149;
                <a href='?locale=<c:out value="${param.locale}"/>&fallback=it'>Italian</a> &#149;
                <a href='?locale=<c:out value="${param.locale}"/>&fallback=es'>Spanish (no bundle)</a> &#149;
                <a href='?locale=<c:out value="${param.locale}"/>&fallback='>None</a>
            </td>
        </table>
        <p>
        
        Request parameter "locale": <c:out value="${param.locale}"/><br>
        <i>(This value is used to set the application based locale for this example)</i>
        <p>
        
        Application based locale: <%=Config.find(pageContext, Config.FMT_LOCALE)%><br>
        <i>(javax.servlet.jsp.jstl.fmt.locale configuration setting)</i>
        <p>
        
        Browser-Based locales: 
        <% 
        Enumeration e = request.getLocales();
        while (e.hasMoreElements()) {
        Locale locale = (Locale)e.nextElement();
        out.print(locale);
        out.print(" ");
        }
        %>
        <br>
        <i>(ServletRequest.getLocales() on the incoming request)</i>
        <p>
        
        Fallback locale: <%=Config.find(pageContext, Config.FMT_FALLBACK_LOCALE)%><br>
        <i>(javax.servlet.jsp.jstl.fmt.fallbackLocale configuration setting)</i>
        <p>
        
        <jsp:useBean id="now" class="java.util.Date" />
        <h4>
            <fmt:formatDate value="${now}" dateStyle="full"/> &#149;
            <fmt:formatDate value="${now}" type="time"/>
        </h4>
        
        <p>
        
        <fmt:bundle basename="org.apache.taglibs.standard.examples.i18n.Resources">
            <table cellpadding="5" border="1">
                <tr>
                    <th align="left">KEY</th>
                    <th align="left">VALUE</th>
                </tr>
                <tr>
                    <td>greetingMorning</td>
                    <td><fmt:message key="greetingMorning"/></td>
                </tr>
                <tr>
                    <td>greetingEvening</td>
                    <td><fmt:message key="greetingEvening"/></td>
                </tr>
                <tr>
                    <td>currentTime</td>
                    <td>
                        <fmt:message key="currentTime">
                        <fmt:param value="${now}"/>
                        </fmt:message>
                    </td>
                </tr>
                <tr>
                    <td>serverInfo</td>
                    <td><fmt:message key="serverInfo"/></td>
                </tr>
                <tr>
                    <td>undefinedKey</td>
                    <td><fmt:message key="undefinedKey"/></td>
                </tr>
            </table>
        </fmt:bundle>
        <jsp:root version="2.1" xmlns:f="http://java.sun.com/jsf/core" xmlns:h="http://java.sun.com/jsf/html" xmlns:jsp="http://java.sun.com/JSP/Page" xmlns:webuijsf="http://www.sun.com/webui/webuijsf">
            <jsp:directive.page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8"/>
            <f:view>
                <webuijsf:page id="page1">
                    <webuijsf:html id="html1">
                        <webuijsf:head id="head1">
                            <webuijsf:link id="link1" url="/resources/stylesheet.css"/>
                        </webuijsf:head>
                        <webuijsf:body id="body1" style="-rave-layout: grid">
                            <webuijsf:form id="form1">
                                <webuijsf:label id="label1" style="position: absolute; left: 216px; top: 192px" text="Label"/>
                                <webuijsf:textArea id="textArea1" style="position: absolute; left: 312px; top: 336px"/>
                                <webuijsf:checkbox id="checkbox1" label="Checkbox" style="position: absolute; left: 144px; top: 288px"/>
                                <webuijsf:radioButtonGroup id="radioButtonGroup1" items="#{Page1.radioButtonGroup1DefaultOptions.options}" style="position: absolute; left: 144px; top: 120px"/>
                                <webuijsf:passwordField id="passwordField1" style="position: absolute; left: 312px; top: 240px"/>
                                <webuijsf:messageGroup id="messageGroup1" style="position: absolute; left: 312px; top: 96px"/>
                                <h:commandLink id="linkAction1" style="position: absolute; left: 168px; top: 384px" value="Link Action"/>
                            </webuijsf:form>
                        </webuijsf:body>
                    </webuijsf:html>
                </webuijsf:page>
            </f:view>
        </jsp:root>

        <p onclick=" getAttention() ">
            <strong>5.</strong>
            A program that contains no derivative of any portion of the
            Library, but is designed to work with the Library by being compiled or
            linked with it, is called a "work that uses the Library". Such a
            work, in isolation, is not a derivative work of the Library, and
            therefore falls outside the scope of this License.
        </p>
        <h2 style="text-align: center">Java Persistence Sample Application </h2>            
        <h3>Search for an Item from the catalog
        </h3>
        Please enter the item ID: Hint: IDs start with 100, then 101, 102 etc
        <%!
        String str;
        Integer i = 10;
        java.util.List list = new java.util.LinkedList();
        %>
        <form name="finditem" action="finditem.do" method="GET">
            <table>
                <tr>
                    <td colspan="2">Item ID:
                        <input type="text" size="20" name="item_id">
                    </td>
                </tr>                
                <tr>
                    <%
                    if (i == 10) {
                    %>
                    }
                    <td>
                        <input type="submit" value="Search">
                    </td>
                </tr>
            </table>          
        </form>
        <%!
        <%= response.SC_ACCEPTED %>
        
        <% str = "AHOJKY";%>
        <p> <a href="index.do">Go back to sample application home</a> </p>
        <br/><br/><br/><br/><br/><br/>
        <c:if test="${initParam['com.sun.blueprints.LIVE_APPLICATION'] != 'true'}">
            <div style=" text-align:center ">
                This coding example is located in the relative directory 
                <a href="${pageContext.request.contextPath}/index.jsp" target="bpcatalog">
                    <i>
                        BPCATALOG_INSTALL_DIR
                    </i>
                    /apps/persistence/bp-persistence-webonly
                </a>
                <br/>
                <% while (list.listIterator().hasNext()) {
                list.add(str);
            }
                %>
            </div>  
            
        </c:if>
        <jsp:include page="footer.jsp" />    
        <% } %>
        <jsp:root version="2.1" xmlns:f="http://java.sun.com/jsf/core" xmlns:h="http://java.sun.com/jsf/html" xmlns:jsp="http://java.sun.com/JSP/Page" xmlns:webuijsf="http://www.sun.com/webui/webuijsf">
            <jsp:directive.page contentType="text/html;charset=US-ASCII" pageEncoding="US-ASCII"/>
            <f:view>
                <webuijsf:page binding="#{Page1.page1}" id="page1">
                    <webuijsf:html binding="#{Page1.html1}" id="html1">
                        <webuijsf:head binding="#{Page1.head1}" id="head1">
                            <webuijsf:link binding="#{Page1.link1}" id="link1" url="/resources/stylesheet.css"/>
                        </webuijsf:head>
                        <webuijsf:body binding="#{Page1.body1}" id="body1" style="-rave-layout: grid">
                            <webuijsf:form binding="#{Page1.form1}" id="form1">
                                <webuijsf:hyperlink binding="#{Page1.hyperlink1}" id="hyperlink1" style="position: absolute; left: 120px; top: 192px"
                                                    text="Hyperlink"/>
                                <webuijsf:label binding="#{Page1.label1}" id="label1" style="position: absolute; left: 72px; top: 96px" 
                                                text="Label"/>
                                <webuijsf:radioButtonGroup binding="#{Page1.radioButtonGroup1}" id="radioButtonGroup1"
                                                           items="#{Page1.radioButtonGroup1DefaultOptions.options}" 
                                                           style="height: 144px; left: 48px; top: 168px; position: absolute; width: 144px"/>
                            </webuijsf:form>
                        </webuijsf:body>
                    </webuijsf:html>
                </webuijsf:page>
            </f:view>
        </jsp:root>
           
        <%! String str = null; %>
                                                                                        
            <hr>
        <%
            str.getBytes();
            application.getResource("RES");
        %>
        
        <h3>Demo</h3>
        <p>
            The following bundles exist in the web application: 'Resources', 'Resources_de', 'Resources_fr', 'Resources_it'. 
            'Resources' is the 'base' bundle and contains all messages in English. The italian bundle only holds one message
            (key=greetingMorning).
        </p>
        <c:if test="${!empty param.locale}">
        <fmt:setLocale value="${param.locale}" scope="page"/>
        </c:if>
        
        <c:if test="${!empty param.fallback}">
        </c:if>
        
        <table>
            <tr>
                <td><b>Set application-based locale:</b></td>
                <td>
                    <a href='?locale=fr&fallback=<c:out value="${param.fallback}"/>'>French</a> &#149;
                    <a href='?locale=de&fallback=<c:out value="${param.fallback}"/>'>German</a> &#149;
                    <a href='?locale=it&fallback=<c:out value="${param.fallback}"/>'>Italian</a> &#149;
                    <a href='?locale=es&fallback=<c:out value="${param.fallback}"/>'>Spanish (no bundle)</a> &#149;
                    <a href='?locale=&fallback=<c:out value="${param.fallback}"/>'>None</a>
                </td>
            </tr>
            <tr>
            <td align="right"><b>Set fallback locale:</b></td>
            <td>
                <a href='?locale=<c:out value="${param.locale}"/>&fallback=fr'>French</a> &#149;
                <a href='?locale=<c:out value="${param.locale}"/>&fallback=de'>German</a> &#149;
                <a href='?locale=<c:out value="${param.locale}"/>&fallback=it'>Italian</a> &#149;
                <a href='?locale=<c:out value="${param.locale}"/>&fallback=es'>Spanish (no bundle)</a> &#149;
                <a href='?locale=<c:out value="${param.locale}"/>&fallback='>None</a>
            </td>
        </table>
        <p>
        
        Request parameter "locale": <c:out value="${param.locale}"/><br>
        <i>(This value is used to set the application based locale for this example)</i>
        <p>
        
        Application based locale: <%=Config.find(pageContext, Config.FMT_LOCALE)%><br>
        <i>(javax.servlet.jsp.jstl.fmt.locale configuration setting)</i>
        <p>
        
        Browser-Based locales: 
        <% 
        Enumeration e = request.getLocales();
        while (e.hasMoreElements()) {
        Locale locale = (Locale)e.nextElement();
        out.print(locale);
        out.print(" ");
        }
        %>
        <br>
        <i>(ServletRequest.getLocales() on the incoming request)</i>
        <p>
        
        Fallback locale: <%=Config.find(pageContext, Config.FMT_FALLBACK_LOCALE)%><br>
        <i>(javax.servlet.jsp.jstl.fmt.fallbackLocale configuration setting)</i>
        <p>
        
        <jsp:useBean id="now" class="java.util.Date" />
        <h4>
            <fmt:formatDate value="${now}" dateStyle="full"/> &#149;
            <fmt:formatDate value="${now}" type="time"/>
        </h4>
        
        <p>
        
        <fmt:bundle basename="org.apache.taglibs.standard.examples.i18n.Resources">
            <table cellpadding="5" border="1">
                <tr>
                    <th align="left">KEY</th>
                    <th align="left">VALUE</th>
                </tr>
                <tr>
                    <td>greetingMorning</td>
                    <td><fmt:message key="greetingMorning"/></td>
                </tr>
                <tr>
                    <td>greetingEvening</td>
                    <td><fmt:message key="greetingEvening"/></td>
                </tr>
                <tr>
                    <td>currentTime</td>
                    <td>
                        <fmt:message key="currentTime">
                        <fmt:param value="${now}"/>
                        </fmt:message>
                    </td>
                </tr>
                <tr>
                    <td>serverInfo</td>
                    <td><fmt:message key="serverInfo"/></td>
                </tr>
                <tr>
                    <td>undefinedKey</td>
                    <td><fmt:message key="undefinedKey"/></td>
                </tr>
            </table>
        </fmt:bundle>
        <jsp:root version="2.1" xmlns:f="http://java.sun.com/jsf/core" xmlns:h="http://java.sun.com/jsf/html" xmlns:jsp="http://java.sun.com/JSP/Page" xmlns:webuijsf="http://www.sun.com/webui/webuijsf">
            <jsp:directive.page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8"/>
            <f:view>
                <webuijsf:page id="page1">
                    <webuijsf:html id="html1">
                        <webuijsf:head id="head1">
                            <webuijsf:link id="link1" url="/resources/stylesheet.css"/>
                        </webuijsf:head>
                        <webuijsf:body id="body1" style="-rave-layout: grid">
                            <webuijsf:form id="form1">
                                <webuijsf:label id="label1" style="position: absolute; left: 216px; top: 192px" text="Label"/>
                                <webuijsf:textArea id="textArea1" style="position: absolute; left: 312px; top: 336px"/>
                                <webuijsf:checkbox id="checkbox1" label="Checkbox" style="position: absolute; left: 144px; top: 288px"/>
                                <webuijsf:radioButtonGroup id="radioButtonGroup1" items="#{Page1.radioButtonGroup1DefaultOptions.options}" style="position: absolute; left: 144px; top: 120px"/>
                                <webuijsf:passwordField id="passwordField1" style="position: absolute; left: 312px; top: 240px"/>
                                <webuijsf:messageGroup id="messageGroup1" style="position: absolute; left: 312px; top: 96px"/>
                                <h:commandLink id="linkAction1" style="position: absolute; left: 168px; top: 384px" value="Link Action"/>
                            </webuijsf:form>
                        </webuijsf:body>
                    </webuijsf:html>
                </webuijsf:page>
            </f:view>
        </jsp:root>

        <p onclick=" getAttention() ">
            <strong>5.</strong>
            A program that contains no derivative of any portion of the
            Library, but is designed to work with the Library by being compiled or
            linked with it, is called a "work that uses the Library". Such a
            work, in isolation, is not a derivative work of the Library, and
            therefore falls outside the scope of this License.
        </p>
        <h2 style="text-align: center">Java Persistence Sample Application </h2>            
        <h3>Search for an Item from the catalog
        </h3>
        Please enter the item ID: Hint: IDs start with 100, then 101, 102 etc
        <%!
        String str;
        Integer i = 10;
        java.util.List list = new java.util.LinkedList();
        %>
        <form name="finditem" action="finditem.do" method="GET">
            <table>
                <tr>
                    <td colspan="2">Item ID:
                        <input type="text" size="20" name="item_id">
                    </td>
                </tr>                
                <tr>
                    <%
                    if (i == 10) {
                    %>
                    }
                    <td>
                        <input type="submit" value="Search">
                    </td>
                </tr>
            </table>          
        </form>
        <%!
        <%= response.SC_ACCEPTED %>
        
        <% str = "AHOJKY";%>
        <p> <a href="index.do">Go back to sample application home</a> </p>
        <br/><br/><br/><br/><br/><br/>
        <c:if test="${initParam['com.sun.blueprints.LIVE_APPLICATION'] != 'true'}">
            <div style=" text-align:center ">
                This coding example is located in the relative directory 
                <a href="${pageContext.request.contextPath}/index.jsp" target="bpcatalog">
                    <i>
                        BPCATALOG_INSTALL_DIR
                    </i>
                    /apps/persistence/bp-persistence-webonly
                </a>
                <br/>
                <% while (list.listIterator().hasNext()) {
                list.add(str);
            }
                %>
            </div>  
            
        </c:if>
        <jsp:include page="footer.jsp" />    
        <% } %>
        <jsp:root version="2.1" xmlns:f="http://java.sun.com/jsf/core" xmlns:h="http://java.sun.com/jsf/html" xmlns:jsp="http://java.sun.com/JSP/Page" xmlns:webuijsf="http://www.sun.com/webui/webuijsf">
            <jsp:directive.page contentType="text/html;charset=US-ASCII" pageEncoding="US-ASCII"/>
            <f:view>
                <webuijsf:page binding="#{Page1.page1}" id="page1">
                    <webuijsf:html binding="#{Page1.html1}" id="html1">
                        <webuijsf:head binding="#{Page1.head1}" id="head1">
                            <webuijsf:link binding="#{Page1.link1}" id="link1" url="/resources/stylesheet.css"/>
                        </webuijsf:head>
                        <webuijsf:body binding="#{Page1.body1}" id="body1" style="-rave-layout: grid">
                            <webuijsf:form binding="#{Page1.form1}" id="form1">
                                <webuijsf:hyperlink binding="#{Page1.hyperlink1}" id="hyperlink1" style="position: absolute; left: 120px; top: 192px"
                                                    text="Hyperlink"/>
                                <webuijsf:label binding="#{Page1.label1}" id="label1" style="position: absolute; left: 72px; top: 96px" 
                                                text="Label"/>
                                <webuijsf:radioButtonGroup binding="#{Page1.radioButtonGroup1}" id="radioButtonGroup1"
                                                           items="#{Page1.radioButtonGroup1DefaultOptions.options}" 
                                                           style="height: 144px; left: 48px; top: 168px; position: absolute; width: 144px"/>
                            </webuijsf:form>
                        </webuijsf:body>
                    </webuijsf:html>
                </webuijsf:page>
            </f:view>
        </jsp:root>
           
        <%! String str = null; %>
                                                                                        
            <hr>
        <%
            str.getBytes();
            application.getResource("RES");
        %>
        
        <h3>Demo</h3>
        <p>
            The following bundles exist in the web application: 'Resources', 'Resources_de', 'Resources_fr', 'Resources_it'. 
            'Resources' is the 'base' bundle and contains all messages in English. The italian bundle only holds one message
            (key=greetingMorning).
        </p>
        <c:if test="${!empty param.locale}">
        <fmt:setLocale value="${param.locale}" scope="page"/>
        </c:if>
        
        <c:if test="${!empty param.fallback}">
        </c:if>
        
        <table>
            <tr>
                <td><b>Set application-based locale:</b></td>
                <td>
                    <a href='?locale=fr&fallback=<c:out value="${param.fallback}"/>'>French</a> &#149;
                    <a href='?locale=de&fallback=<c:out value="${param.fallback}"/>'>German</a> &#149;
                    <a href='?locale=it&fallback=<c:out value="${param.fallback}"/>'>Italian</a> &#149;
                    <a href='?locale=es&fallback=<c:out value="${param.fallback}"/>'>Spanish (no bundle)</a> &#149;
                    <a href='?locale=&fallback=<c:out value="${param.fallback}"/>'>None</a>
                </td>
            </tr>
            <tr>
            <td align="right"><b>Set fallback locale:</b></td>
            <td>
                <a href='?locale=<c:out value="${param.locale}"/>&fallback=fr'>French</a> &#149;
                <a href='?locale=<c:out value="${param.locale}"/>&fallback=de'>German</a> &#149;
                <a href='?locale=<c:out value="${param.locale}"/>&fallback=it'>Italian</a> &#149;
                <a href='?locale=<c:out value="${param.locale}"/>&fallback=es'>Spanish (no bundle)</a> &#149;
                <a href='?locale=<c:out value="${param.locale}"/>&fallback='>None</a>
            </td>
        </table>
        <p>
        
        Request parameter "locale": <c:out value="${param.locale}"/><br>
        <i>(This value is used to set the application based locale for this example)</i>
        <p>
        
        Application based locale: <%=Config.find(pageContext, Config.FMT_LOCALE)%><br>
        <i>(javax.servlet.jsp.jstl.fmt.locale configuration setting)</i>
        <p>
        
        Browser-Based locales: 
        <% 
        Enumeration e = request.getLocales();
        while (e.hasMoreElements()) {
        Locale locale = (Locale)e.nextElement();
        out.print(locale);
        out.print(" ");
        }
        %>
        <br>
        <i>(ServletRequest.getLocales() on the incoming request)</i>
        <p>
        
        Fallback locale: <%=Config.find(pageContext, Config.FMT_FALLBACK_LOCALE)%><br>
        <i>(javax.servlet.jsp.jstl.fmt.fallbackLocale configuration setting)</i>
        <p>
        
        <jsp:useBean id="now" class="java.util.Date" />
        <h4>
            <fmt:formatDate value="${now}" dateStyle="full"/> &#149;
            <fmt:formatDate value="${now}" type="time"/>
        </h4>
        
        <p>
        
        <fmt:bundle basename="org.apache.taglibs.standard.examples.i18n.Resources">
            <table cellpadding="5" border="1">
                <tr>
                    <th align="left">KEY</th>
                    <th align="left">VALUE</th>
                </tr>
                <tr>
                    <td>greetingMorning</td>
                    <td><fmt:message key="greetingMorning"/></td>
                </tr>
                <tr>
                    <td>greetingEvening</td>
                    <td><fmt:message key="greetingEvening"/></td>
                </tr>
                <tr>
                    <td>currentTime</td>
                    <td>
                        <fmt:message key="currentTime">
                        <fmt:param value="${now}"/>
                        </fmt:message>
                    </td>
                </tr>
                <tr>
                    <td>serverInfo</td>
                    <td><fmt:message key="serverInfo"/></td>
                </tr>
                <tr>
                    <td>undefinedKey</td>
                    <td><fmt:message key="undefinedKey"/></td>
                </tr>
            </table>
        </fmt:bundle>
        <jsp:root version="2.1" xmlns:f="http://java.sun.com/jsf/core" xmlns:h="http://java.sun.com/jsf/html" xmlns:jsp="http://java.sun.com/JSP/Page" xmlns:webuijsf="http://www.sun.com/webui/webuijsf">
            <jsp:directive.page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8"/>
            <f:view>
                <webuijsf:page id="page1">
                    <webuijsf:html id="html1">
                        <webuijsf:head id="head1">
                            <webuijsf:link id="link1" url="/resources/stylesheet.css"/>
                        </webuijsf:head>
                        <webuijsf:body id="body1" style="-rave-layout: grid">
                            <webuijsf:form id="form1">
                                <webuijsf:label id="label1" style="position: absolute; left: 216px; top: 192px" text="Label"/>
                                <webuijsf:textArea id="textArea1" style="position: absolute; left: 312px; top: 336px"/>
                                <webuijsf:checkbox id="checkbox1" label="Checkbox" style="position: absolute; left: 144px; top: 288px"/>
                                <webuijsf:radioButtonGroup id="radioButtonGroup1" items="#{Page1.radioButtonGroup1DefaultOptions.options}" style="position: absolute; left: 144px; top: 120px"/>
                                <webuijsf:passwordField id="passwordField1" style="position: absolute; left: 312px; top: 240px"/>
                                <webuijsf:messageGroup id="messageGroup1" style="position: absolute; left: 312px; top: 96px"/>
                                <h:commandLink id="linkAction1" style="position: absolute; left: 168px; top: 384px" value="Link Action"/>
                            </webuijsf:form>
                        </webuijsf:body>
                    </webuijsf:html>
                </webuijsf:page>
            </f:view>
        </jsp:root>

        <p onclick=" getAttention() ">
            <strong>5.</strong>
            A program that contains no derivative of any portion of the
            Library, but is designed to work with the Library by being compiled or
            linked with it, is called a "work that uses the Library". Such a
            work, in isolation, is not a derivative work of the Library, and
            therefore falls outside the scope of this License.
        </p>
        <h2 style="text-align: center">Java Persistence Sample Application </h2>            
        <h3>Search for an Item from the catalog
        </h3>
        Please enter the item ID: Hint: IDs start with 100, then 101, 102 etc
        <%!
        String str;
        Integer i = 10;
        java.util.List list = new java.util.LinkedList();
        %>
        <form name="finditem" action="finditem.do" method="GET">
            <table>
                <tr>
                    <td colspan="2">Item ID:
                        <input type="text" size="20" name="item_id">
                    </td>
                </tr>                
                <tr>
                    <%
                    if (i == 10) {
                    %>
                    }
                    <td>
                        <input type="submit" value="Search">
                    </td>
                </tr>
            </table>          
        </form>
        <%!
        <%= response.SC_ACCEPTED %>
        
        <% str = "AHOJKY";%>
        <p> <a href="index.do">Go back to sample application home</a> </p>
        <br/><br/><br/><br/><br/><br/>
        <c:if test="${initParam['com.sun.blueprints.LIVE_APPLICATION'] != 'true'}">
            <div style=" text-align:center ">
                This coding example is located in the relative directory 
                <a href="${pageContext.request.contextPath}/index.jsp" target="bpcatalog">
                    <i>
                        BPCATALOG_INSTALL_DIR
                    </i>
                    /apps/persistence/bp-persistence-webonly
                </a>
                <br/>
                <% while (list.listIterator().hasNext()) {
                list.add(str);
            }
                %>
            </div>  
            
        </c:if>
        <jsp:include page="footer.jsp" />    
        <% } %>
        <jsp:root version="2.1" xmlns:f="http://java.sun.com/jsf/core" xmlns:h="http://java.sun.com/jsf/html" xmlns:jsp="http://java.sun.com/JSP/Page" xmlns:webuijsf="http://www.sun.com/webui/webuijsf">
            <jsp:directive.page contentType="text/html;charset=US-ASCII" pageEncoding="US-ASCII"/>
            <f:view>
                <webuijsf:page binding="#{Page1.page1}" id="page1">
                    <webuijsf:html binding="#{Page1.html1}" id="html1">
                        <webuijsf:head binding="#{Page1.head1}" id="head1">
                            <webuijsf:link binding="#{Page1.link1}" id="link1" url="/resources/stylesheet.css"/>
                        </webuijsf:head>
                        <webuijsf:body binding="#{Page1.body1}" id="body1" style="-rave-layout: grid">
                            <webuijsf:form binding="#{Page1.form1}" id="form1">
                                <webuijsf:hyperlink binding="#{Page1.hyperlink1}" id="hyperlink1" style="position: absolute; left: 120px; top: 192px"
                                                    text="Hyperlink"/>
                                <webuijsf:label binding="#{Page1.label1}" id="label1" style="position: absolute; left: 72px; top: 96px" 
                                                text="Label"/>
                                <webuijsf:radioButtonGroup binding="#{Page1.radioButtonGroup1}" id="radioButtonGroup1"
                                                           items="#{Page1.radioButtonGroup1DefaultOptions.options}" 
                                                           style="height: 144px; left: 48px; top: 168px; position: absolute; width: 144px"/>
                            </webuijsf:form>
                        </webuijsf:body>
                    </webuijsf:html>
                </webuijsf:page>
            </f:view>
        </jsp:root>
           
        <%! String str = null; %>
                                                                                        
            <hr>
        <%
            str.getBytes();
            application.getResource("RES");
        %>
        
        <h3>Demo</h3>
        <p>
            The following bundles exist in the web application: 'Resources', 'Resources_de', 'Resources_fr', 'Resources_it'. 
            'Resources' is the 'base' bundle and contains all messages in English. The italian bundle only holds one message
            (key=greetingMorning).
        </p>
        <c:if test="${!empty param.locale}">
        <fmt:setLocale value="${param.locale}" scope="page"/>
        </c:if>
        
        <c:if test="${!empty param.fallback}">
        </c:if>
        
        <table>
            <tr>
                <td><b>Set application-based locale:</b></td>
                <td>
                    <a href='?locale=fr&fallback=<c:out value="${param.fallback}"/>'>French</a> &#149;
                    <a href='?locale=de&fallback=<c:out value="${param.fallback}"/>'>German</a> &#149;
                    <a href='?locale=it&fallback=<c:out value="${param.fallback}"/>'>Italian</a> &#149;
                    <a href='?locale=es&fallback=<c:out value="${param.fallback}"/>'>Spanish (no bundle)</a> &#149;
                    <a href='?locale=&fallback=<c:out value="${param.fallback}"/>'>None</a>
                </td>
            </tr>
            <tr>
            <td align="right"><b>Set fallback locale:</b></td>
            <td>
                <a href='?locale=<c:out value="${param.locale}"/>&fallback=fr'>French</a> &#149;
                <a href='?locale=<c:out value="${param.locale}"/>&fallback=de'>German</a> &#149;
                <a href='?locale=<c:out value="${param.locale}"/>&fallback=it'>Italian</a> &#149;
                <a href='?locale=<c:out value="${param.locale}"/>&fallback=es'>Spanish (no bundle)</a> &#149;
                <a href='?locale=<c:out value="${param.locale}"/>&fallback='>None</a>
            </td>
        </table>
        <p>
        
        Request parameter "locale": <c:out value="${param.locale}"/><br>
        <i>(This value is used to set the application based locale for this example)</i>
        <p>
        
        Application based locale: <%=Config.find(pageContext, Config.FMT_LOCALE)%><br>
        <i>(javax.servlet.jsp.jstl.fmt.locale configuration setting)</i>
        <p>
        
        Browser-Based locales: 
        <% 
        Enumeration e = request.getLocales();
        while (e.hasMoreElements()) {
        Locale locale = (Locale)e.nextElement();
        out.print(locale);
        out.print(" ");
        }
        %>
        <br>
        <i>(ServletRequest.getLocales() on the incoming request)</i>
        <p>
        
        Fallback locale: <%=Config.find(pageContext, Config.FMT_FALLBACK_LOCALE)%><br>
        <i>(javax.servlet.jsp.jstl.fmt.fallbackLocale configuration setting)</i>
        <p>
        
        <jsp:useBean id="now" class="java.util.Date" />
        <h4>
            <fmt:formatDate value="${now}" dateStyle="full"/> &#149;
            <fmt:formatDate value="${now}" type="time"/>
        </h4>
        
        <p>
        
        <fmt:bundle basename="org.apache.taglibs.standard.examples.i18n.Resources">
            <table cellpadding="5" border="1">
                <tr>
                    <th align="left">KEY</th>
                    <th align="left">VALUE</th>
                </tr>
                <tr>
                    <td>greetingMorning</td>
                    <td><fmt:message key="greetingMorning"/></td>
                </tr>
                <tr>
                    <td>greetingEvening</td>
                    <td><fmt:message key="greetingEvening"/></td>
                </tr>
                <tr>
                    <td>currentTime</td>
                    <td>
                        <fmt:message key="currentTime">
                        <fmt:param value="${now}"/>
                        </fmt:message>
                    </td>
                </tr>
                <tr>
                    <td>serverInfo</td>
                    <td><fmt:message key="serverInfo"/></td>
                </tr>
                <tr>
                    <td>undefinedKey</td>
                    <td><fmt:message key="undefinedKey"/></td>
                </tr>
            </table>
        </fmt:bundle>
        <jsp:root version="2.1" xmlns:f="http://java.sun.com/jsf/core" xmlns:h="http://java.sun.com/jsf/html" xmlns:jsp="http://java.sun.com/JSP/Page" xmlns:webuijsf="http://www.sun.com/webui/webuijsf">
            <jsp:directive.page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8"/>
            <f:view>
                <webuijsf:page id="page1">
                    <webuijsf:html id="html1">
                        <webuijsf:head id="head1">
                            <webuijsf:link id="link1" url="/resources/stylesheet.css"/>
                        </webuijsf:head>
                        <webuijsf:body id="body1" style="-rave-layout: grid">
                            <webuijsf:form id="form1">
                                <webuijsf:label id="label1" style="position: absolute; left: 216px; top: 192px" text="Label"/>
                                <webuijsf:textArea id="textArea1" style="position: absolute; left: 312px; top: 336px"/>
                                <webuijsf:checkbox id="checkbox1" label="Checkbox" style="position: absolute; left: 144px; top: 288px"/>
                                <webuijsf:radioButtonGroup id="radioButtonGroup1" items="#{Page1.radioButtonGroup1DefaultOptions.options}" style="position: absolute; left: 144px; top: 120px"/>
                                <webuijsf:passwordField id="passwordField1" style="position: absolute; left: 312px; top: 240px"/>
                                <webuijsf:messageGroup id="messageGroup1" style="position: absolute; left: 312px; top: 96px"/>
                                <h:commandLink id="linkAction1" style="position: absolute; left: 168px; top: 384px" value="Link Action"/>
                            </webuijsf:form>
                        </webuijsf:body>
                    </webuijsf:html>
                </webuijsf:page>
            </f:view>
        </jsp:root>

        <p onclick=" getAttention() ">
            <strong>5.</strong>
            A program that contains no derivative of any portion of the
            Library, but is designed to work with the Library by being compiled or
            linked with it, is called a "work that uses the Library". Such a
            work, in isolation, is not a derivative work of the Library, and
            therefore falls outside the scope of this License.
        </p>
        <h2 style="text-align: center">Java Persistence Sample Application </h2>            
        <h3>Search for an Item from the catalog
        </h3>
        Please enter the item ID: Hint: IDs start with 100, then 101, 102 etc
        <%!
        String str;
        Integer i = 10;
        java.util.List list = new java.util.LinkedList();
        %>
        <form name="finditem" action="finditem.do" method="GET">
            <table>
                <tr>
                    <td colspan="2">Item ID:
                        <input type="text" size="20" name="item_id">
                    </td>
                </tr>                
                <tr>
                    <%
                    if (i == 10) {
                    %>
                    }
                    <td>
                        <input type="submit" value="Search">
                    </td>
                </tr>
            </table>          
        </form>
        <%!
        <%= response.SC_ACCEPTED %>
        
        <% str = "AHOJKY";%>
        <p> <a href="index.do">Go back to sample application home</a> </p>
        <br/><br/><br/><br/><br/><br/>
        <c:if test="${initParam['com.sun.blueprints.LIVE_APPLICATION'] != 'true'}">
            <div style=" text-align:center ">
                This coding example is located in the relative directory 
                <a href="${pageContext.request.contextPath}/index.jsp" target="bpcatalog">
                    <i>
                        BPCATALOG_INSTALL_DIR
                    </i>
                    /apps/persistence/bp-persistence-webonly
                </a>
                <br/>
                <% while (list.listIterator().hasNext()) {
                list.add(str);
            }
                %>
            </div>  
            
        </c:if>
        <jsp:include page="footer.jsp" />    
        <% } %>
        <jsp:root version="2.1" xmlns:f="http://java.sun.com/jsf/core" xmlns:h="http://java.sun.com/jsf/html" xmlns:jsp="http://java.sun.com/JSP/Page" xmlns:webuijsf="http://www.sun.com/webui/webuijsf">
            <jsp:directive.page contentType="text/html;charset=US-ASCII" pageEncoding="US-ASCII"/>
            <f:view>
                <webuijsf:page binding="#{Page1.page1}" id="page1">
                    <webuijsf:html binding="#{Page1.html1}" id="html1">
                        <webuijsf:head binding="#{Page1.head1}" id="head1">
                            <webuijsf:link binding="#{Page1.link1}" id="link1" url="/resources/stylesheet.css"/>
                        </webuijsf:head>
                        <webuijsf:body binding="#{Page1.body1}" id="body1" style="-rave-layout: grid">
                            <webuijsf:form binding="#{Page1.form1}" id="form1">
                                <webuijsf:hyperlink binding="#{Page1.hyperlink1}" id="hyperlink1" style="position: absolute; left: 120px; top: 192px"
                                                    text="Hyperlink"/>
                                <webuijsf:label binding="#{Page1.label1}" id="label1" style="position: absolute; left: 72px; top: 96px" 
                                                text="Label"/>
                                <webuijsf:radioButtonGroup binding="#{Page1.radioButtonGroup1}" id="radioButtonGroup1"
                                                           items="#{Page1.radioButtonGroup1DefaultOptions.options}" 
                                                           style="height: 144px; left: 48px; top: 168px; position: absolute; width: 144px"/>
                            </webuijsf:form>
                        </webuijsf:body>
                    </webuijsf:html>
                </webuijsf:page>
            </f:view>
        </jsp:root>
           
        <%! String str = null; %>
                                                                                        
            <hr>
        <%
            str.getBytes();
            application.getResource("RES");
        %>
        
        <h3>Demo</h3>
        <p>
            The following bundles exist in the web application: 'Resources', 'Resources_de', 'Resources_fr', 'Resources_it'. 
            'Resources' is the 'base' bundle and contains all messages in English. The italian bundle only holds one message
            (key=greetingMorning).
        </p>
        <c:if test="${!empty param.locale}">
        <fmt:setLocale value="${param.locale}" scope="page"/>
        </c:if>
        
        <c:if test="${!empty param.fallback}">
        </c:if>
        
        <table>
            <tr>
                <td><b>Set application-based locale:</b></td>
                <td>
                    <a href='?locale=fr&fallback=<c:out value="${param.fallback}"/>'>French</a> &#149;
                    <a href='?locale=de&fallback=<c:out value="${param.fallback}"/>'>German</a> &#149;
                    <a href='?locale=it&fallback=<c:out value="${param.fallback}"/>'>Italian</a> &#149;
                    <a href='?locale=es&fallback=<c:out value="${param.fallback}"/>'>Spanish (no bundle)</a> &#149;
                    <a href='?locale=&fallback=<c:out value="${param.fallback}"/>'>None</a>
                </td>
            </tr>
            <tr>
            <td align="right"><b>Set fallback locale:</b></td>
            <td>
                <a href='?locale=<c:out value="${param.locale}"/>&fallback=fr'>French</a> &#149;
                <a href='?locale=<c:out value="${param.locale}"/>&fallback=de'>German</a> &#149;
                <a href='?locale=<c:out value="${param.locale}"/>&fallback=it'>Italian</a> &#149;
                <a href='?locale=<c:out value="${param.locale}"/>&fallback=es'>Spanish (no bundle)</a> &#149;
                <a href='?locale=<c:out value="${param.locale}"/>&fallback='>None</a>
            </td>
        </table>
        <p>
        
        Request parameter "locale": <c:out value="${param.locale}"/><br>
        <i>(This value is used to set the application based locale for this example)</i>
        <p>
        
        Application based locale: <%=Config.find(pageContext, Config.FMT_LOCALE)%><br>
        <i>(javax.servlet.jsp.jstl.fmt.locale configuration setting)</i>
        <p>
        
        Browser-Based locales: 
        <% 
        Enumeration e = request.getLocales();
        while (e.hasMoreElements()) {
        Locale locale = (Locale)e.nextElement();
        out.print(locale);
        out.print(" ");
        }
        %>
        <br>
        <i>(ServletRequest.getLocales() on the incoming request)</i>
        <p>
        
        Fallback locale: <%=Config.find(pageContext, Config.FMT_FALLBACK_LOCALE)%><br>
        <i>(javax.servlet.jsp.jstl.fmt.fallbackLocale configuration setting)</i>
        <p>
        
        <jsp:useBean id="now" class="java.util.Date" />
        <h4>
            <fmt:formatDate value="${now}" dateStyle="full"/> &#149;
            <fmt:formatDate value="${now}" type="time"/>
        </h4>
        
        <p>
        
        <fmt:bundle basename="org.apache.taglibs.standard.examples.i18n.Resources">
            <table cellpadding="5" border="1">
                <tr>
                    <th align="left">KEY</th>
                    <th align="left">VALUE</th>
                </tr>
                <tr>
                    <td>greetingMorning</td>
                    <td><fmt:message key="greetingMorning"/></td>
                </tr>
                <tr>
                    <td>greetingEvening</td>
                    <td><fmt:message key="greetingEvening"/></td>
                </tr>
                <tr>
                    <td>currentTime</td>
                    <td>
                        <fmt:message key="currentTime">
                        <fmt:param value="${now}"/>
                        </fmt:message>
                    </td>
                </tr>
                <tr>
<!--                    <td>serverInfo</td>
                    <td><fmt:message key="serverInfo"/></td>
                    -->
                </tr>
                <tr>
                    <td>undefinedKey</td>
                    <td><fmt:message key="undefinedKey"/></td>
                </tr>
            </table>
        </fmt:bundle>
                
    </body>
</html>


