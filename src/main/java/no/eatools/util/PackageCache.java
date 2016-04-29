package no.eatools.util;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import no.eatools.diagramgen.EaPackage;
import no.eatools.diagramgen.EaRepo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sparx.Package;

import static org.apache.commons.lang3.StringUtils.*;

/**
 * @author ohs
 */
public class PackageCache {
    private static final transient Logger LOG = LoggerFactory.getLogger(PackageCache.class);
    // Package ID -> package
    private final Map<Integer, EaPackage> theCache = new HashMap<>();

    public EaPackage findPackageByName(final EaPackage rootPackage, final String theName, final boolean recursive) {
        final Optional<Map.Entry<Integer, EaPackage>> entry = theCache.entrySet()
                                                                      .stream()
                                                                      .filter(e -> e.getValue()
                                                                                    .getName()
                                                                                    .equals(trimToEmpty(theName)))
                                                                      .findFirst();
        if (!entry.isPresent()) {
            LOG.info("Package [{}] not in cache", theName);
            return null;
        }
        final EaPackage result = entry.get()
                                      .getValue();
        if (isDescendantOf(rootPackage, result, recursive)) {
            return result;
        }
        return null;
    }

    public boolean isDescendantOf(final EaPackage parent, final EaPackage descendant, final boolean recursive) {
        if (parent == null || descendant == null) {
            return false;
        }
        final EaPackage directParent = descendant.getParent();
        if (directParent == null) {
            return false;
        }
        if (parent.getId() == directParent.getId()) {
            return true;
        }
        if (recursive) {
            return isDescendantOf(parent, directParent, true);
        }
        return false;
    }

    public EaPackage findById(final int id) {
        return theCache.get(id);
    }

    /**
     * @param repo
     * @param localRootPackage generate complete tree from the point
     * @param repositoryRoot   The global root, must traverse all the way up inorder to generate correct paths
     */
    public void populate(final EaRepo repo, final EaPackage localRootPackage, final EaPackage repositoryRoot) {
        System.out.println("Start populating package cache " + new Date());
        put(localRootPackage);
        if (localRootPackage != repositoryRoot) {
            put(repositoryRoot);
            populateAncestorTail(repo, localRootPackage.unwrap(), repositoryRoot);
        }
        populateCache(repo, localRootPackage.unwrap(), null);
        System.out.println("Finished populating package cache " + new Date() + " # of Packages " + theCache.size());
    }

    void populateAncestorTail(final EaRepo repo, final Package pack, final EaPackage globalRoot) {
        if (pack == null || globalRoot == null || globalRoot.getName()
                                                            .equals(trimToEmpty(pack.GetName()))) {
            return;
        }

        final Package parent = repo.findPackageByIdNoCache(pack.GetParentID());
        populateAncestorTail(repo, parent, globalRoot);

        final EaPackage parentPkg = new EaPackage(pack, repo, parent != null ? theCache.get(parent.GetParentID()) : null);
        put(parentPkg);
    }

    private void populateCache(final EaRepo repos, final Package pkg, final EaPackage parent) {
        final EaPackage parentPkg = new EaPackage(pkg, repos, parent);
        put(parentPkg);
        for (final Package aPackage : pkg.GetPackages()) {
            populateCache(repos, aPackage, parentPkg);
        }
    }

    public boolean isEmpty() {
        return theCache.isEmpty();
    }

    /**
     * Recursive
     *
     * @param ancestor
     * @return
     */
    public Collection<EaPackage> findFamilyOf(final EaPackage ancestor) {
        return findDescendantsOf(ancestor, true);
    }

    /**
     * Non-recursive
     *
     * @param pkg
     * @return
     */
    public List<EaPackage> findChildrenOf(final EaPackage pkg) {
        return findDescendantsOf(pkg, false);
    }

    /**
     * Non-recursive
     *
     * @param pkg
     * @return
     */
    public List<EaPackage> findDescendantsOf(final EaPackage pkg, final boolean recursive) {
        return theCache.values()
                       .stream()
                       .filter(e -> isDescendantOf(pkg, e, recursive))
                       .collect(Collectors.toList());
    }

    public EaPackage getById(final int packageID) {
        return theCache.get(packageID);
    }

    private void put(final EaPackage eaPackage) {
        if (eaPackage == null) {
            LOG.error("Trying to put null");
            return;
        }
        final EaPackage previous = theCache.get(eaPackage.getId());
        if (previous != null) {
            LOG.warn("Trying to double put. Has {} new {}. Not replacing", previous, eaPackage);
            return;
        }
        LOG.debug("Putting {}", eaPackage);
        theCache.put(eaPackage.getId(), eaPackage);
    }
}
