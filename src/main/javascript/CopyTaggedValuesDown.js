!INC Local Scripts.EAConstants-JScript

/*
 * Script Name: Copy all tagged values from classes in package to all attributes of these classes.
 * Author: ohs
 * Purpose: To add tagged values to selected package
 * Date: 11. jan 2016
 */

function SetTaggedValue(tagName, value, attribute as EA.ATTRIBUTE_NODE) {
    var tag as EA.TaggedValue;

    if (value != null) {
        tag = attribute.TaggedValues.GetByName(tagName);
        if (tag == null) {
            tag = attribute.TaggedValues.AddNew(tagName, "");
            tag.Update();
        }
        Session.Output("Tag  Before was: " + " name:" + tag.Name + " value:" + tag.Value);
        tag.Value = value;
        Session.Output("Tag  After was: " + " name:" + tag.Name + " value:" + tag.Value);
        tag.Update();
    }
    return tag;
}

function main()
{
    var pack as EA.Package

    pack = GetTreeSelectedPackage();

    for ( i = 0; i < pack.Elements.Count; i ++ )
    {
        var element as EA.Element;
        var confValue;
        var integrityValue;
        var availValue;
        var tagValue as EA.TaggedValue;
        element = pack.Elements.GetAt(i);
        Session.Output( "Processing element " + element.Name );

        tagValue = element.TaggedValues.GetByName("Confidentiality");
        if(tagValue != null) {
            confValue = tagValue.Value;
        }
        tagValue = element.TaggedValues.GetByName("Integrity");
        if(tagValue != null) {
            integrityValue = tagValue.Value;
        }
        tagValue = element.TaggedValues.GetByName("Availability");
        if(tagValue != null) {
            availValue = tagValue.Value;
        }

        Session.Output( "Class " + element.Name  + " C :" + confValue + " I:" + integrityValue + " A:" +  availValue);

        for ( j = 0; j < element.Attributes.Count; j ++ )
        {
            var attribute as EA.Attribute;

            attribute = element.Attributes.GetAt(j);
            Session.Output( "Processing attribute " + attribute.Name );

            tag = SetTaggedValue("Confidentiality", confValue, attribute);
            tag = SetTaggedValue("Integrity", integrityValue, tag, attribute);
            tag = SetTaggedValue("Availability", availValue, tag, attribute);
            attribute.Update();
        }
    }
}

main();
