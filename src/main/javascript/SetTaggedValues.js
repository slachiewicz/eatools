!INC Local Scripts.EAConstants-JScript

/*
 * Script Name: Add tagged values to select diagram items
 * Author: TomO
 * Purpose: To add tagged values to selected diagram items
 * Date: 31st August 2012
 */

function main()
{
    var diagram as EA.Diagram
    var pack as EA.Package

    diagram = GetCurrentDiagram();
    pack = GetTreeSelectedPackage();

    for ( i = 0; i < pack.Elements.Count; i ++ )
    {
        var element as EA.Element;
        var tag as EA.TaggedValue;

        element = pack.Elements.GetAt(i);
        tag = element.TaggedValues.AddNew("Confidentiality", "");
        tag.Update();
        tag = element.TaggedValues.AddNew("Integrity", "");
        tag.Update();
        tag = element.TaggedValues.AddNew("Availability", "");
        tag.Update();
        for ( j = 0; j < element.Attributes.Count; j ++ )
        {
            var attribute as EA.Attribute;
            var tag as EA.TaggedValue;

            attribute = element.Attributes.GetAt(j);
            tag = attribute.TaggedValues.AddNew("Confidentiality", "");
            tag.Update();
            tag = attribute.TaggedValues.AddNew("Integrity", "");
            tag.Update();
            tag = attribute.TaggedValues.AddNew("Availability", "");
            tag.Update();
        }
    }
}


main();
