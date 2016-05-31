package no.eatools.util;

import no.bouvet.ohs.jops.Resource;
import no.bouvet.ohs.jops.ResourceUtils;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.internal.stubbing.answers.DoesNothing;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.sparx.Package;

import static org.mockito.Mockito.*;

//import static org.easymock.EasyMock.anyLong;
//import static org.easymock.EasyMock.expect;
//import static org.junit.Assert.*;
//import static org.powermock.api.easymock.PowerMock.*;



/**
 * @author ohs
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({PackageCache.class, Package.class, PackageCacheTest.class})
public class PackageCacheTest {
    PackageCache subject = new PackageCache();

    @Before
    public void setUp() throws Exception {

        //Disable the call to System.loadLibrary inside the EA-classes static inits
        PowerMockito.mockStatic(System.class, new DoesNothing());
        PowerMockito.doNothing().when(System.class);
//        System sysMoc = mock(System.class);

//        PowerMockito.doNothing()
//                    .when(System.class, System.class.getMethod("loadLibrary", new Class[]{String.class}));

//        expect(URLEncoder.encode("string", "enc")).andReturn("something");
//        replayAll();
//
//        assertEquals("something", new SystemClassUser().performEncode());
//
//        verifyAll();


//        when(System.loadLibrary("")).thenReturn((Void)null);
//
//        PowerMockito.doNothing()
//                .when(System.class);


//        System.loadLibrary("SSJavaCOM");
//        EaPackage{name='Solution Building Block Metamodel', id=1457, parentId=1456}

        for (String packLine : FileUtils.readLines(ResourceUtils.getResourceAsFile(this, new Resource("packages.txt")))) {
            String[] parts = packLine.split("=");
            String name = parts[1].replaceAll("'", "")
                                  .replaceAll(",.*", "");
            System.out.println(name);

            Package pkg = mock(Package.class);
            when(pkg.GetPackageID()).thenReturn(1);

//            subject.put(new EaPackage());
        }

    }

    @Test
    public void testFind() throws Exception {


    }
}
