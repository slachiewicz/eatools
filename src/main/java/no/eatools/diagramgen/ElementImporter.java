package no.eatools.diagramgen;

import java.time.ZonedDateTime;
import java.util.Date;

import no.bouvet.ohs.ea.dd.DDEntry;
import no.bouvet.ohs.ea.dd.DDEntryList;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sparx.Element;

/**
 * @author ohs
 */
public class ElementImporter {
    private static final transient Logger LOG = LoggerFactory.getLogger(ElementImporter.class);

    private final EaRepo eaRepo;
    private final String fileToImport;

    public ElementImporter(final EaRepo eaRepo, final String fileToImport) {
        this.eaRepo = eaRepo;
        this.fileToImport = fileToImport;
    }


    public void importComponents() {
        final DDEntryList ddEntryList = DDEntryList.parseFromFile(fileToImport);

        int numberUpdated = 0;
        for (final DDEntry ddEntry : ddEntryList) {
            if (createOrUpdate(ddEntry)) {
                ++numberUpdated;
            }
        }
        LOG.info("Updated [{}] elements", numberUpdated);
    }

    private boolean createOrUpdate(final DDEntry entry) {

        final EaPackage pack = eaRepo.findInPackageCache(entry.getPackagePath());
        if (pack == null) {
            LOG.warn("Package [{}] does not exist, skipping element [{}]", entry.getPackagePath(), entry.getTitle());
            return false;
        }

        final Element element = eaRepo.findOrCreateComponentInPackage(pack.me, entry.getTitle()
                                                                                    .trim());
        final EaElement eaElement = new EaElement(element, eaRepo);

        final String oldStereotype = element.GetStereotype();
        final String entryStereotypes = entry.getStereotypes();
        if (StringUtils.isNotBlank(oldStereotype) && oldStereotype.equalsIgnoreCase(entryStereotypes)) {
            LOG.warn("[{}]: Not touching stereotype old: [{}] new : [{}]", entry.getTitle(), oldStereotype, entryStereotypes);
            LOG.info("StereotypeEx : [{}]", element.GetStereotypeEx());
            LOG.info("StereotypeList : [{}]", element.GetStereotypeList());
            LOG.info("FQStereotype : [{}]", element.GetFQStereotype());
        } else {
            element.SetStereotype(entryStereotypes);
        }
        element.SetVersion(entry.getVersion());
        element.SetNotes(entry.getDescription());
        element.SetAuthor(entry.getAuthor());
        final ZonedDateTime created = entry.getCreated();
        if (created != null) {
            element.SetCreated(new Date(created.toEpochSecond()));
        }
        entry.getTaggedValues()
             .forEach(eaElement::updateTaggedValue);

        LOG.debug("TaggedValues: [{}]", entry.getTaggedValues());

        element.Update();
        pack.me.Update();

        entry.getAssociations()
             .forEach(eaElement::updateAssociation);
        return true;
    }
}
