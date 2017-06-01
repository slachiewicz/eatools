package no.eatools.util;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
    private static final Logger LOG = LoggerFactory.getLogger(PackageCache.class);
    // Package ID -> package
    private final Map<Integer, EaPackage> theCache = new HashMap<>();

    public EaPackage findPackageByName(final EaPackage rootPackage, final String theName, final boolean recursive) {
        final List<Map.Entry<Integer, EaPackage>> entries = theCache.entrySet()
                                                                    .stream()
                                                                    .filter(e -> e.getValue()
                                                                                  .getName()
                                                                                  .equals(trimToEmpty(theName)))
                                                                    .collect(Collectors.toList());
        for (final Map.Entry<Integer, EaPackage> entry : entries) {
            final EaPackage result = entry.getValue();
            if (isDescendantOf(rootPackage, result, recursive)) {
                return result;
            }
        }
        LOG.info("Package [{}] with ancestor [{}] not in cache", theName, rootPackage.getName());
        return null;
    }

    public List<EaPackage> findPackagesByName(final EaPackage rootPackage, final String theName, final boolean recursive) {
        final List<EaPackage> entries = theCache.values()
                                                .stream()
                                                .filter(e -> e.getName()
                                                        .equals(trimToEmpty(theName)))
                                                .filter(e -> isDescendantOf(rootPackage, e, recursive))
                                                .collect(Collectors.toList());
        if(entries.isEmpty()) {
            LOG.info("Package [{}] with ancestor [{}] not in cache", theName, rootPackage.getName());
        }
        return entries;
    }

    private boolean isDescendantOf(final EaPackage parent, final EaPackage descendant, final boolean recursive) {
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

    public boolean packageMatch(final Package p, final Pattern packagePattern) {
        if (p == null) {
            return false;
        }

        if (packagePattern == null) {
            return true;
        }
        final Matcher matcher = packagePattern.matcher(p.GetName());
        if (matcher.matches()) {
            LOG.debug("Package match : {}", p.GetName());
            return true;
        }
        LOG.debug("Looking for parent match for {} ", p.GetName());
        return packageMatch(findParentPackage(p), packagePattern);
    }

    private Package findParentPackage(final Package pack) {
        if (pack == null || pack.GetParentID() == 0) {
            return null;
        }
        final int key = pack.GetParentID();
        return findById(key).unwrap();
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
    public List<EaPackage> findFamilyOf(final EaPackage ancestor) {
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

    void put(final EaPackage eaPackage) {
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

    /**
     * Find a package with given name and where parent matches the given package pattern.
     *
     * @param rootPkg
     * @param theName
     * @param packagePattern
     * @param recursive
     * @return
     */
    public EaPackage findPackageByName(final EaPackage rootPkg, final String theName, final Pattern packagePattern, final boolean recursive) {
        final List<EaPackage> candidates = findPackagesByName(rootPkg, theName, recursive);

        for (final EaPackage candidate : candidates) {
            if (packageMatch(candidate.unwrap(), packagePattern)) {
                return candidate;
            }
        }
        return null;
    }

    /**
     * Find a package with given hierarchical name and where parent matches the given package pattern.
     *
     * @param rootPkg
     * @param nameHierarchy hierarchical name, e.g. "A->subPack->child"
     * @param packagePattern matching pattern agains any parent package.
     * @return
     */
    public EaPackage findPackageByHierarchicalName(final EaPackage rootPkg, final String nameHierarchy, final Pattern packagePattern) {
        // root is special
        if(rootPkg.getName().equals(nameHierarchy)) {
            return rootPkg;
        }

        final LinkedList<String> hier = EaPackage.hirearchyToList(nameHierarchy);

        return findPackage(hier, rootPkg, packagePattern);
    }

    private EaPackage findPackage(final LinkedList<String> hier, final EaPackage rootPkg, final Pattern packagePattern) {
        if (hier.isEmpty()) {
            return rootPkg;
        }
        final String child = hier.pollFirst();
        final EaPackage pack = findPackageByName(rootPkg, child, packagePattern, true);
        if(pack != null) {
            return findPackage(hier, pack, packagePattern);
        }
        return null;
    }

    public boolean contains(EaPackage localRoot) {
        return theCache.containsValue(localRoot);
    }

    public void clear() {
        theCache.clear();
    }
}
