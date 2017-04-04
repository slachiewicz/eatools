package no.eatools.diagramgen;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sparx.Attribute;
import org.sparx.Collection;
import org.sparx.Diagram;
import org.sparx.Element;
import org.sparx.ObjectType;
import org.sparx.Package;
import org.sparx.TaggedValue;

import static org.junit.Assert.*;

/**
 * @author AB22273
 * @since 23.okt.2008 10:55:06
 */
public class EaRepoTest extends AbstractEaTestCase {
    private static final transient Logger log = LoggerFactory.getLogger(EaRepoTest.class);

    @Test
    public void testOpenRepository() throws Exception {
        eaRepo.open();
        eaRepo.close();
    }

    @Test
    public void testGetRootPackage() throws Exception {
        final EaPackage rootPkg = eaRepo.getRootPackage();
        assertNotNull(rootPkg);
        assertEquals("Model", rootPkg.getName());
        log.info("Root Package Name: " + rootPkg.getName());
    }

    @Test
    public void testFindPackageByName() throws Exception {
        String packName = "Komponenter";
        final EaPackage rootPkg = eaRepo.getRootPackage();

        EaPackage thePkg = eaRepo.findPackageByName(packName, rootPkg, EaRepo.RECURSIVE);
        assertNotNull(thePkg);
        assertEquals(packName, thePkg.getName());

        thePkg = eaRepo.findPackageByName(packName, rootPkg, EaRepo.NON_RECURSIVE);
        assertNull(thePkg);

        packName = "Objekter";
        thePkg = eaRepo.findPackageByName(packName, rootPkg, EaRepo.RECURSIVE);
        assertNotNull(thePkg);
        assertEquals(packName, thePkg.getName());

        thePkg = eaRepo.findPackageByName(packName, rootPkg, EaRepo.NON_RECURSIVE);
        assertNull(thePkg);

        // The root package itself is not considered as a match
        thePkg = eaRepo.findPackageByName(rootPkg.getName(), rootPkg, EaRepo.NON_RECURSIVE);
        assertNull(thePkg);

        thePkg = eaRepo.findPackageByName(rootPkg.getName(), rootPkg, EaRepo.RECURSIVE);
        assertNull(thePkg);
    }

    @Test
    public void testFindKomponenterInPackage() throws Exception {
        final String packName = "Komponenter";
        final EaPackage rootPkg = eaRepo.getRootPackage();
        final EaPackage thePkg = eaRepo.findPackageByName(packName, rootPkg, EaRepo.RECURSIVE);
        final List<Element> components = eaRepo.findComponentsInPackage(thePkg.unwrap());
        for (final Element e : components) {
            assertEquals(EaMetaType.COMPONENT.toString(), e.GetMetaType());
            log.info("Component name: " + e.GetName());
            final Collection<TaggedValue> tvs = e.GetTaggedValues();
            for (final TaggedValue tv : tvs) {
                log.info(tv.GetName() + ":" + tv.GetValue());
            }
        }
    }

    @Test
    public void testSetTaggedValues() throws Exception {
        final String packName = "Komponenter";
        final EaPackage rootPkg = eaRepo.getRootPackage();
        final EaPackage thePkg = eaRepo.findPackageByName(packName, rootPkg, EaRepo.RECURSIVE);
        final List<Element> components = eaRepo.findComponentsInPackage(thePkg.unwrap());
        for (final Element e : components) {
            System.out.println("Element name: " + e.GetName());
            assertEquals(EaMetaType.COMPONENT.toString(), e.GetMetaType());
            e.SetAuthor("Ove");
            // Tag i denoted "Keyword" in the EA-model
            e.SetTag("aTag=smthg");
//            e.GetEmbeddedElements();
            final Collection<TaggedValue> tvs = e.GetTaggedValues();
            TaggedValue tv = tvs.GetByName("Ove");
            if (tv != null) {
                tv.SetValue("kul@" + new Date().toString());
            } else {
                tv = e.GetTaggedValues()
                      .AddNew("Ove", "kulere");
            }
            tv.Update();    // This is required.
            e.Update();
        }
    }

    @Test
    public void testFindClassesInPackage() throws Exception {
        final String packName = "System";
        final EaPackage rootPkg = eaRepo.getRootPackage();
        final EaPackage thePkg = eaRepo.findPackageByName(packName, rootPkg, EaRepo.RECURSIVE);
        final List<Element> classes = eaRepo.findClassesInPackage(thePkg.unwrap());
        for (final Element e : classes) {
            log.info("Class name: " + e.GetName());
            assertEquals(EaMetaType.CLASS.toString(), e.GetMetaType());
        }
    }

    @Test
    public void testAddElementToPackage() throws Exception {
        final String packName = "Klasser";
        final EaPackage rootPkg = eaRepo.getRootPackage();
        final EaPackage thePkg = eaRepo.findPackageByName(packName, rootPkg, EaRepo.RECURSIVE);
        final Collection<Element> theElements = thePkg.unwrap()
                                                      .GetElements();
        final Element newClass = theElements.AddNew("EnNyKlasse", EaMetaType.CLASS.toString());
        assertEquals(ObjectType.otElement, newClass.GetObjectType());
        assertEquals(newClass.GetClassfierID(), newClass.GetClassifierID()); // subtle...
        log.info(newClass.GetMetaType());

        final Attribute newAtt = newClass.GetAttributes()
                                         .AddNew("name", "string");
        assertEquals("name", newAtt.GetName());
        log.info(newAtt.GetObjectType()
                       .toString());
        newAtt.Update();
        newClass.Update();

        theElements.Refresh();
        thePkg.unwrap()
              .Update();


        final Element newObject = theElements.AddNew("EtObject", EaMetaType.OBJECT.toString());

        log.info(newObject.GetMetaType());
        newObject.GetAttributes()
                 .AddNew("name", "String");
        newObject.Update();
        newObject.SetRunState("name=petter");

        theElements.Refresh();
        thePkg.unwrap()
              .Update();
    }

    @Test
    public void testAddObjectOfClass() throws Exception {
        final String packName = "Klasser";
        final EaPackage thePkg = eaRepo.findPackageByName(packName, EaRepo.RECURSIVE);

        final Collection<Element> theElements = thePkg.unwrap()
                                                      .GetElements();
        assertNotNull(theElements);

        final String className = "MinKlasse";
        final Element myClass = eaRepo.findOrCreateClassInPackage(thePkg.unwrap(), className);

        assertNotNull(myClass);

        assertEquals(ObjectType.otElement, myClass.GetObjectType());
        log.info(myClass.GetClassifierID() + ":" + myClass.GetClassfierID());
        log.info(myClass.GetMetaType());

        log.info("ElementID {}", myClass.GetElementID());

        final Element newObject = theElements.AddNew("EtObject", EaMetaType.OBJECT.toString());

        log.info(newObject.GetMetaType());
        newObject.SetClassifierID(myClass.GetElementID());
        newObject.Update();

//        newObject.GetAttributes().AddNew("name", "String");
        newObject.SetRunState("navn=petter");
        newObject.Update();
        log.info(newObject.GetRunState());
        newObject.Update();

        theElements.Refresh();
        thePkg.unwrap()
              .Update();
    }

    @Test
    public void testGetObjectRunState() {
        final String packName = "Klasser";
        final EaPackage thePkg = eaRepo.findPackageByName(packName, EaRepo.RECURSIVE);

        final Collection<Element> theElements = thePkg.unwrap()
                                                      .GetElements();
        assertNotNull(theElements);
        Element myClass = null;
        for (final Element element : theElements) {
            if (element.GetName()
                       .equals("MyClass")) {
                myClass = element;
            }
        }
    }

    @Test
    public void testFindOrCreateObjectInPackage() throws Exception {
        final String packName = "Klasser";
        EaPackage pkg = eaRepo.findPackageByName(packName, EaRepo.RECURSIVE);
        Element myObject = eaRepo.findOrCreateObjectInPackage(pkg.unwrap(), "mittObjekt", null);
        assertNotNull(myObject);
        assertEquals("mittObjekt", myObject.GetName());
        assertEquals("", myObject.GetClassifierName());

        pkg = eaRepo.findPackageByName(packName, EaRepo.RECURSIVE);
        final Element mQueueClass = eaRepo.findOrCreateClassInPackage(pkg.unwrap(), "MQQueue");
        assertNotNull(mQueueClass);
        myObject = eaRepo.findOrCreateObjectInPackage(pkg.unwrap(), "enKo", mQueueClass);
        assertNotNull(myObject);
        assertEquals(mQueueClass.GetElementID(), myObject.GetClassifierID());

        // This fails, even though I set the ClassifierName explicitly when creating objects,
        // it is blank when retreieved later.
        // assertEquals(mQueueClass.getName(), myObject.GetClassifierName());

        // Do not create another but retrieve the object just created
        final Element sameObject = eaRepo.findOrCreateObjectInPackage(pkg.unwrap(), "enKo", mQueueClass);
        assertEquals(myObject.GetElementID(), sameObject.GetElementID());
        log.info(myObject.GetRunState());
    }


    @Test
    public void testSetAttributeValue() throws Exception {
        final String packName = "Klasser";
        final EaPackage pkg = eaRepo.findPackageByName(packName, EaRepo.RECURSIVE);
        final boolean wasDeleted = eaRepo.deleteObjectInPackage(pkg.unwrap(), "mittObjekt", null);
        final Element myObject = eaRepo.findOrCreateObjectInPackage(pkg.unwrap(), "mittObjekt", null);
        assertEquals("mittObjekt", myObject.GetName());

        final String runStateBefore = myObject.GetRunState();
        log.debug("RunState before: " + runStateBefore);
        eaRepo.setAttributeValue(myObject, "navn", "noe helt annet");

        final String runStateAfter = myObject.GetRunState();
        log.debug("RunStateAfter: " + runStateAfter);
        assertFalse(runStateBefore.equals(runStateAfter));
        log.info(runStateAfter);
    }

    @Test
    public void testFindElementOfType() throws Exception {
        final String packName = "Klasser";
        final EaPackage pkg = eaRepo.findPackageByName(packName, EaRepo.RECURSIVE);
        final String className = "MQQueue";
        eaRepo.findOrCreateClassInPackage(pkg.unwrap(), className);
        final Optional<Element> theClass = eaRepo.findElementOfType(pkg.unwrap(), EaMetaType.CLASS, className);
        assertTrue(theClass.isPresent());
        assertEquals(className, theClass.get().GetName());
    }

    /**
     * Assert that the index concept in EA is continuous and monotonous
     *
     * @throws Exception
     */
    @Test
    public void testIndexInCollection() throws Exception {
        final String packName = "Klasser";
        final EaPackage pkg = eaRepo.findPackageByName(packName, EaRepo.RECURSIVE);
        assertNotNull(pkg);
        short i = 0;
        final Package unwrappedPkg = pkg.unwrap();
        for (final Element element : unwrappedPkg
                .GetElements()) {
            assertEquals(element.GetName(), unwrappedPkg
                    .GetElements()
                    .GetAt(i++)
                    .GetName());
        }

        final String objectName = "uniqueObject";
        final Element anObject = eaRepo.findOrCreateObjectInPackage(unwrappedPkg, objectName, null);
        assertNotNull(anObject);
        short indexOfAnObject = -1;
        i = 0;
        for (final Element element : unwrappedPkg
                .GetElements()) {
            if (element.GetName()
                       .equals(objectName)) {
                indexOfAnObject = i;
            }
            assertEquals(element.GetName(), unwrappedPkg
                    .GetElements()
                    .GetAt(i++)
                    .GetName());
        }
        assertTrue(indexOfAnObject != -1);
        final Element elm = unwrappedPkg.GetElements()
                                        .GetAt(indexOfAnObject);
        log.info("About to delete [{}]", elm.GetName());
        assertTrue(eaRepo.deleteObjectInPackage(unwrappedPkg, elm.GetName(), null));

        i = 0;
        // pkg object is no longer valid after Update !!
        eaRepo.clearPackageCache();
        final EaPackage pkgAgain = eaRepo.findPackageByName(packName, EaRepo.RECURSIVE);
        final Package unwrappedPackage = pkgAgain.unwrap();
        for (final Element element : unwrappedPackage
                .GetElements()) {
            log.info("Processing [{}]", element.GetName());
            assertFalse("Expected " + element.GetName() + " to be deleted.", element.GetName()
                                                                                    .equals(objectName));
            assertEquals(element.GetName(), unwrappedPackage
                    .GetElements()
                    .GetAt(i++)
                    .GetName());
        }
    }

    @Test
    public void testDeleteObjectInPackage() throws Exception {
        final String packName = "Klasser";
        final EaPackage pkg = eaRepo.findPackageByName(packName, EaRepo.RECURSIVE);
        assertNotNull(pkg);
        final String objectName = "uniqueObject";
        final Package unwrapped = pkg.unwrap();
        Element anObject = eaRepo.findOrCreateObjectInPackage(unwrapped, objectName, null);
        assertNotNull(anObject);

        assertTrue(eaRepo.deleteObjectInPackage(unwrapped, objectName, null));

        final Element classifier = eaRepo.findOrCreateClassInPackage(pkg.unwrap(), "AClass");
        anObject = eaRepo.findOrCreateObjectInPackage(unwrapped, objectName, classifier);
        assertNotNull(anObject);
        assertFalse(eaRepo.deleteObjectInPackage(unwrapped, objectName, anObject));
        assertTrue(eaRepo.deleteObjectInPackage(unwrapped, objectName, classifier));

        eaRepo.clearPackageCache();
        final EaPackage rereadPkg = eaRepo.findPackageByName(packName, EaRepo.RECURSIVE);
        eaRepo.findObjectsInPackage(rereadPkg.unwrap())
              .forEach(e -> {
                  log.info("Found elment [{}]", e.GetName());
                  assertNotEquals("uniqueObject", e.GetName());
                  assertNotEquals("AClass", e.GetName());
              });
    }

    @Test
    public void testDeleteClassInPackage() throws Exception {
        final String packName = "Klasser";
        final EaPackage pkg = eaRepo.findPackageByName(packName, EaRepo.RECURSIVE);
        assertNotNull(pkg);
        final String className = "FoersteKlasse";
        final Package unwrapped = pkg.unwrap();
        final Element aClass = eaRepo.findOrCreateClassInPackage(unwrapped, className);
        assertNotNull(aClass);

        assertTrue(eaRepo.deleteObjectInPackage(unwrapped, className, null));

        eaRepo.clearPackageCache();final EaPackage rereadPkg = eaRepo.findPackageByName(packName, EaRepo.RECURSIVE);
        eaRepo.findClassesInPackage(rereadPkg.unwrap())
              .forEach(e -> {
                  log.info("Found class [{}]", e.GetName());
                  assertNotEquals(className, e.GetName());
              });
    }

    @Test
    public void testIsOfType() throws Exception {
        final String packName = "Klasser";
        final EaPackage pkg = eaRepo.findPackageByName(packName, EaRepo.RECURSIVE);

        Element classifier = eaRepo.findOrCreateClassInPackage(pkg.unwrap(), "AClass");
        assertNotNull(classifier);

        final String objectName = "uniqueObject1";
        Element anObject = eaRepo.findOrCreateObjectInPackage(pkg.unwrap(), objectName, null);
        assertNotNull(anObject);

        assertTrue(eaRepo.isOfType(anObject, null));
        assertFalse(eaRepo.isOfType(anObject, classifier));

        anObject = eaRepo.findOrCreateObjectInPackage(pkg.unwrap(), objectName, classifier);

        assertFalse(eaRepo.isOfType(anObject, null));
        assertTrue(eaRepo.isOfType(anObject, classifier));

        classifier = eaRepo.findOrCreateClassInPackage(pkg.unwrap(), "MQQueue");
        assertNotNull(classifier);

        assertFalse(eaRepo.isOfType(anObject, null));
        assertFalse(eaRepo.isOfType(anObject, classifier));

    }

    @Test
    public void testFindOrCreateComponentInPackage() throws Exception {
        final String packName = "Komponenter";
        final EaPackage pkg = eaRepo.findPackageByName(packName, EaRepo.RECURSIVE);
        assertNotNull(pkg);
        final String componentName = "nyKomponent";
        final Element newComponent = eaRepo.findOrCreateComponentInPackage(pkg.unwrap(), componentName);
        assertNotNull(newComponent);
        assertEquals(componentName, newComponent.GetName());
        final Element theComponent = eaRepo.findOrCreateComponentInPackage(pkg.unwrap(), componentName);
        assertEquals(newComponent.GetElementID(), theComponent.GetElementID());


        // clean up
        eaRepo.deleteComponent(pkg, componentName);
    }

    @Test
    public void testFindOrCreatePackage() throws Exception {
        final String packName = "Klasser";
        final String parentPackName = "Domain Model";
        final EaPackage parent = eaRepo.findPackageByName(parentPackName, EaRepo.RECURSIVE);
        assertNotNull(parent);

        final EaPackage pkg = eaRepo.findOrCreatePackage(parent.unwrap(), packName);
        assertNotNull(pkg);

        final String componentName = "Komponenter";
        final EaPackage newPkg = eaRepo.findOrCreatePackage(pkg.unwrap(), componentName);
        assertNotNull(newPkg);
        assertEquals(componentName, newPkg.getName());
        assertNotNull(newPkg.unwrap().GetElement());
        assertEquals(pkg.unwrap()
                        .GetPackageID(), newPkg.unwrap().GetParentID());

        // clean up
        eaRepo.deletePackage(newPkg, EaRepo.RECURSIVE);
    }

    @Test
    public void testGetEaDataTypes() throws Exception {
        log.info("EA DataTypes: " + eaRepo.getEaDataTypes());
    }

    @Test
    public void testFindComponentInstancesInPackage() throws Exception {
        final String packName = "System";
        final EaPackage rootPkg = eaRepo.getRootPackage();
        final EaPackage thePkg = eaRepo.findPackageByName(packName, rootPkg, EaRepo.RECURSIVE);
        final List<Element> componentInstances = eaRepo.findComponentInstancesInPackage(thePkg.unwrap());
        assertEquals(2, componentInstances.size());
        for (final Element e : componentInstances) {
            log.info("Component Instance name: " + e.GetName());
            assertEquals(EaMetaType.COMPONENT.toString(), e.GetMetaType());
            assertEquals(EaMetaType.COMPONENT.toString(), e.GetClassifierType());
        }
    }

    @Test
    public void testFindOrCreateComponentInstanceInPackage() throws Exception {
        final String packName = "MQ Managers";
        final EaPackage rootPkg = eaRepo.getRootPackage();
        final EaPackage thePkg = eaRepo.findPackageByName(packName, rootPkg, EaRepo.RECURSIVE);
        final Element classifier = eaRepo.findOrCreateComponentInPackage(thePkg.unwrap(), "KøManager");
        final String instanceName = "EnNyManager";
        final Element componentInstance = eaRepo.findOrCreateComponentInstanceInPackage(thePkg.unwrap(), instanceName, classifier);
        assertNotNull(componentInstance);
        assertEquals(EaMetaType.COMPONENT.toString(), componentInstance.GetMetaType());
        // todo this fails the first time, why?
//        assertEquals(EaMetaType.COMPONENT.toString(), componentInstance.GetClassifierType());
        assertEquals(instanceName, componentInstance.GetName());
        assertEquals(classifier.GetElementID(), componentInstance.GetClassifierID());

        // Clean up
        eaRepo.deleteObjectInPackage(thePkg.unwrap(), instanceName, classifier);
    }

    @Test
    public void testFindOrCreateDiagramInPackage() throws Exception {
        final String packName = "MQ Managers";
        final EaPackage rootPkg = eaRepo.getRootPackage();
        final EaPackage thePkg = eaRepo.findPackageByName(packName, rootPkg, /* doRecurse */ true);
        Diagram d = eaRepo.findOrCreateDiagramInPackage(thePkg.unwrap(), null, EaDiagramType.COMPOSITE_STRUCTURE);
        assertNotNull(d);
        assertEquals(d.GetName(), thePkg.getName());

        d = eaRepo.findOrCreateDiagramInPackage(thePkg.unwrap(), "XyZ Diagram", EaDiagramType.INTERACTION_OVERVIEW);
        assertNotNull(d);
        assertEquals(d.GetName(), "XyZ Diagram");
        assertEquals(d.GetType(), EaDiagramType.INTERACTION_OVERVIEW.toString());
    }

    @Test
    public void testFindAllMetaTypesInModel() throws Exception {
        log.debug(eaRepo.toString() + " has metatypes: " + eaRepo.findAllMetaTypesInModel());
    }

    @Test
    public void testCreateOrUpdateStandardDiagram() throws Exception {
        final String packName = "Komponenter";
        final EaPackage rootPkg = eaRepo.getRootPackage();
        final EaPackage thePkg = eaRepo.findPackageByName(packName, rootPkg, EaRepo.RECURSIVE);
        final List<Element> components = eaRepo.findComponentsInPackage(thePkg.unwrap());
        for (final Element e : components) {
            log.info("Component name: " + e.GetName());
            eaRepo.createOrUpdateStandardDiagram(new EaElement(e, eaRepo));
        }
    }
}
