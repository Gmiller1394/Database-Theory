let $d := doc("c:/classes/6010/medline13n0717.xml")
for $x in $d/MedlineCitationSet/MedlineCitation/MeshHeadingList
return concat("&#x0A;", string-join($x/MeshHeading/DescriptorName/text(), ';'))
