<?xml version="1.0" encoding="windows-1250"?>
<xsl:stylesheet version="1.0"
		xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:output method="html" encoding="UTF-8"/>

<xsl:template match="/">
  <html>
    <head>
      <title>Performance results</title>
    </head>
    <body>
      <xsl:apply-templates/>
    </body>
  </html>
</xsl:template>

<xsl:template match="TestRun">
  <h2>TestRun</h2>

  <table border="1"><thead><td>Name</td><td>Run Order</td><td>Average</td></thead>
    <tbody>
      <xsl:for-each select="TestBag/UnitTestSuite/Data/PerformanceData">
	<xsl:sort select="concat(@name,@runOrder)"/>
        <xsl:variable name="name_order" select="concat(@name,@runOrder)"/>
	<xsl:if test="not(preceding-sibling::PerformanceData[concat(@name,@runOrder)=$name_order])">
	  <tr> 
            <td><xsl:value-of select="@name"/></td> 
            <td><xsl:value-of select="@runOrder"/></td>
            <td><xsl:value-of select="sum(//UnitTestSuite/Data/PerformanceData[concat(@name,@runOrder)=$name_order]/@value) /
                                      count(//UnitTestSuite/Data/PerformanceData[concat(@name,@runOrder)=$name_order])"/></td>
          </tr>

<!--
	  <xsl:for-each select="//UnitTestSuite/Data/PerformanceData[concat(@name,@runOrder)=$name_order]">
	    <!--<xsl:sort select="text"/>-->
            <p>
	      <xsl:value-of select="@value"/>
	    </p>
	  </xsl:for-each>
-->
	</xsl:if>
      </xsl:for-each>
    </tbody>
  </table>

  <p>Details:</p>
  <xsl:apply-templates/>
  <hr/>
</xsl:template>

<xsl:template match="TestBag">
  <h2>TestBag <xsl:value-of select="@name"/></h2>
  <p><xsl:apply-templates/></p>
</xsl:template>

<xsl:template match="UnitTestSuite">
  <table border="1"><thead><td>Name</td><td>Value</td><td>Comment</td></thead>
    <tbody>
      <xsl:apply-templates/>
    </tbody>
  </table>
</xsl:template>

<xsl:template match="Data">
  <xsl:apply-templates/>
</xsl:template>

<xsl:template match="PerformanceData">
  <tr>
    <td><xsl:value-of select="@name"/></td>
    <td><xsl:value-of select="@value"/></td>
  <xsl:if test="@value > @threshold">
    <td>Failed - out of limits</td>
  </xsl:if>
  </tr>
  <xsl:apply-templates/>
</xsl:template>

</xsl:stylesheet>

