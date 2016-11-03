package no.eatools.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.regex.Pattern;

import no.bouvet.ohs.dotuml.Dotter;
import no.bouvet.ohs.jops.Resource;
import no.bouvet.ohs.jops.ResourceUtils;
import no.eatools.diagramgen.EaPackage;
import no.eatools.diagramgen.EaRepo;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.internal.stubbing.answers.DoesNothing;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sparx.Collection;
import org.sparx.Package;

import static com.google.common.collect.Lists.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

//import static org.easymock.EasyMock.anyLong;
//import static org.easymock.EasyMock.expect;
//import static org.junit.Assert.*;
//import static org.powermock.api.easymock.PowerMock.*;


/**
 * @author ohs
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({PackageCache.class, Package.class, PackageCacheTest.class, Collection.class})
public class PackageCacheTest {
    private static final transient Logger LOG = LoggerFactory.getLogger(PackageCacheTest.class);
    PackageCache subject;
    Map<Integer, Package> pkgMap;

    static {
        //Disable the call to System.loadLibrary inside the EA-classes static inits
        PowerMockito.mockStatic(System.class, new DoesNothing());
        PowerMockito.doNothing()
                    .when(System.class);

    }

    private EaPackage localRoot;

    @Before
    public void setUp() throws Exception {
        subject = new PackageCache();
        pkgMap = new HashMap<>();

        EaPackage rootPackage = null;

        final EaRepo repos = mock(EaRepo.class);

//        EaPackage{name='Solution Building Block Metamodel', id=1457, parentId=1456}
        for (final String packLine : FileUtils.readLines(ResourceUtils.getResourceAsFile(this, new Resource("packages.txt")))) {
            if(! packLine.startsWith("#")) {
                final String[] parts = packLine.split("=");
                final String name = parts[1].replaceAll("'", "")
                                            .replaceAll(",.*", "");

                final Integer id = NumberUtils.createInteger(parts[2].replaceAll(",.*", "")
                                                                     .trim());
                final Integer parentId = NumberUtils.createInteger(parts[3].replaceAll("}.*", "")
                                                                           .trim());
                LOG.info("name [{}] id [{}] parentid [{}]", name, id, parentId);

                final Package pkg = mock(Package.class);
                when(pkg.GetPackageID()).thenReturn(id);
                when(pkg.GetParentID()).thenReturn(parentId);
                when(pkg.GetName()).thenReturn(name);
                when(pkg.toString()).thenReturn(id.toString() + ":" + StringUtils.left(name.replaceAll("\\W", ""), 20));
//            when(pkg.toString()).thenReturn(id.toString());
                pkgMap.put(id, pkg);
                if (parentId == 0) {
                    rootPackage = new EaPackage(pkg, repos, null);
                }
                when(repos.findPackageByIdNoCache(id)).thenReturn(pkgMap.get(id));
            }
        }
        for (final Package pkg : pkgMap.values()) {
            final Collection<Package> children = mock(Collection.class);

            // The mocked iterators get "used up" each time the mocked method is called
            when(children.iterator()).thenReturn(createIterator(pkg), createIterator(pkg), createIterator(pkg));
            when(pkg.GetPackages()).thenReturn(children);
        }

        new Dotter<Package, Integer>("packagesN.dot")
                .dotItWithParents(pkgMap.values(), p -> pkgMap.get(p.GetParentID()));
        final Package root = pkgMap.get(1334);

        new Dotter<Package, Integer>("packagesC.dot")
                .dotItWithChildrenWithIterator(newArrayList(root), p -> p.GetPackages()
                                                                         .iterator());

//        new Dotter<Package, Integer>("packagesC.dot")
//                .dotItWithChildrenWithIterator(pkgMap.values(), p -> p.GetPackages()
//                                                                      .iterator());

//        printChildrenOfAll();

        subject.populate(repos, rootPackage, rootPackage);
//        List<EaPackage> packages = new ArrayList<>();
//        for (Map.Entry<Integer, Package> pkgEntry : pkgMap.entrySet()) {
//            packages.add(new EaPackage())
//            subject.put(new EaPackage(pkgEntry.getValue(), null, ));
//        }
        localRoot = rootPackage; // Does not work for root: subject.findPackageByHierarchicalName(rootPackage, "Elhub Architecture", null);
        System.out.println(localRoot);

    }

    private void printChildrenOfAll() {
        for (final Package aPackage : pkgMap.values()) {
            final Collection<Package> children = aPackage.GetPackages();
            final StringJoiner sb = new StringJoiner(",");
            for (final Package child : children) {
                final int i = child.GetPackageID();
                sb.add(Integer.toString(i));
            }
            LOG.debug("I am [{}] My children [{}]", aPackage, sb.toString());
        }
    }

    private Iterator<Package> createIterator(final Package pkg) {
        return pkgMap.values()
                     .stream()
                     .filter(e -> pkg.GetPackageID() == e.GetParentID())
                     .iterator();
    }

    @Test
    public void testFind() throws Exception {
        final EaPackage interfaces = subject.findPackageByName(subject.findById(1334), "Interfaces", true);
        logAncestorTree(interfaces);
        assertEquals("Interfaces", interfaces.getName());
        assertEquals(1907, interfaces.getParentId());
    }

    @Test
    public void testFindDescendantsOf() throws Exception {
        assertTrue(subject.contains(localRoot));
        List<EaPackage> descendants = subject.findDescendantsOf(localRoot, false);
        assertFalse(descendants.isEmpty());
    }

    @Test
    public void testFindAndPrintHiearchicalName() throws Exception {
        System.out.println(localRoot);
        subject.findDescendantsOf(localRoot, true)
               .stream()
               .forEach(p-> System.out.println(p.toHierarchicalString()));
    }

    @Test
    public void testFindPackageByNameWithFilter() throws Exception {
        final EaPackage interfaces = subject.findPackageByName(subject.findById(1334), "Interfaces", Pattern.compile("Solution Building Blocks"),
                                                               true);
        logAncestorTree(interfaces);
        assertEquals("Interfaces", interfaces.getName());
        assertEquals(1659, interfaces.getParentId());
    }

    private void logAncestorTree(final EaPackage pkg) {
        if (pkg == null) {
            LOG.info("Tree is empty");
            return;
        }
        final StringJoiner sj = new StringJoiner("->\n").add(pkg.toString());
        int parentId = pkg.getParentId();
        while (parentId > 0) {
            final EaPackage parentPkg = subject.findById(parentId);
            sj.add(parentPkg.toString());
            parentId = parentPkg.getParentId();
        }
        LOG.info(sj.toString());
    }

    @Test
    public void testFindPackageByHierarcicalName() throws Exception {
        EaPackage mimLogical = subject.findPackageByHierarchicalName(subject.findById(1334), "MIM->Logical", Pattern.compile("Elhub.*"));
        logAncestorTree(mimLogical);
        assertEquals("Logical", mimLogical.getName());
        assertEquals("MIM", mimLogical.getParent()
                                      .getName());

        EaPackage eimLogical = subject.findPackageByHierarchicalName(subject.findById(1334), "EIM->Logical", Pattern.compile("Elhub.*"));
        logAncestorTree(eimLogical);
        assertEquals("Logical", eimLogical.getName());
        assertEquals("EIM", eimLogical.getParent()
                                      .getName());

        eimLogical = subject.findPackageByHierarchicalName(subject.findById(1334), "Information Architecture->EIM->Logical", Pattern.compile("Elhub.*"));
        logAncestorTree(eimLogical);
        assertEquals("Logical", eimLogical.getName());
        assertEquals("EIM", eimLogical.getParent()
                                      .getName());
        assertEquals("Information Architecture", eimLogical.getParent()
                                                           .getParent()
                                                           .getName());

        assertNotEquals(mimLogical.getId(), eimLogical.getId());

        EaPackage rubbish = subject.findPackageByHierarchicalName(subject.findById(1334), "MIM->Logical->rubbish", Pattern.compile("Elhub.*"));
        assertNull(rubbish);

        rubbish = subject.findPackageByHierarchicalName(subject.findById(1334), "rubbish->MIM->Logical", Pattern.compile("Elhub.*"));
        assertNull(rubbish);

        rubbish = subject.findPackageByHierarchicalName(subject.findById(1334), "", Pattern.compile("Elhub.*"));
        assertNull(rubbish);

        rubbish = subject.findPackageByHierarchicalName(subject.findById(1334), "  ", Pattern.compile("Elhub.*"));
        assertNull(rubbish);

        rubbish = subject.findPackageByHierarchicalName(subject.findById(1334), "->", Pattern.compile("Elhub.*"));
        assertEquals(1334, rubbish.getId()); // returns root

        rubbish = subject.findPackageByHierarchicalName(subject.findById(1334), "EIM->", Pattern.compile("Elhub.*"));
        assertEquals("EIM", rubbish.getName());

        rubbish = subject.findPackageByHierarchicalName(subject.findById(1334), "EIM", Pattern.compile("Elhub.*"));
        assertEquals("EIM", rubbish.getName());

        rubbish = subject.findPackageByHierarchicalName(subject.findById(1334), "->EIM", Pattern.compile("Elhub.*"));
        assertNull(rubbish);

        rubbish = subject.findPackageByHierarchicalName(subject.findById(1334), " ->EIM ", Pattern.compile("Elhub.*"));
        assertNull(rubbish);

        rubbish = subject.findPackageByHierarchicalName(subject.findById(1334), "MIM->Logical", Pattern.compile("Elhux.*"));
        assertNull(rubbish);



        eimLogical = subject.findPackageByHierarchicalName(subject.findById(1334), "EIM->Logical", Pattern.compile("Logical"));
        logAncestorTree(eimLogical);
        assertNull(eimLogical);

        eimLogical = subject.findPackageByHierarchicalName(subject.findById(1334), "EIM->Logical", Pattern.compile("EIM"));
        logAncestorTree(eimLogical);
        assertEquals("Logical", eimLogical.getName());
        assertEquals("EIM", eimLogical.getParent()
                                      .getName());

        EaPackage elhubArch = subject.findPackageByHierarchicalName(subject.findById(1334), "Elhub Architecture", null);
        logAncestorTree(mimLogical);
        assertEquals("Elhub Architecture", elhubArch.getName());
        assertEquals(1334, elhubArch.getId());

        elhubArch = subject.findPackageByHierarchicalName(subject.findById(1334), "Dashboard->Elhub Architecture", null);
        logAncestorTree(mimLogical);
        assertEquals("Elhub Architecture", elhubArch.getName());
        assertEquals(1700, elhubArch.getId());

        elhubArch = subject.findPackageByHierarchicalName(subject.findById(1700), "Elhub Architecture", null);
        logAncestorTree(mimLogical);
        assertEquals("Elhub Architecture", elhubArch.getName());
        assertEquals(1700, elhubArch.getId());

    }
}
