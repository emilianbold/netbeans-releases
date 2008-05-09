importPackage(Packages.java.lang);

doc = <doc>
<node>
<node.1> hello </node.1>
<node.1> world </node.1>
<node.1> welcome to </node.1>
<node.1> E4X </node.1>
<node.2> NOT SEEN </node.2>
</node>
</doc>;

qn=QName("node.1");

for each ( text in doc..*["node.1"] )
{
 System.out.println(text);
}


