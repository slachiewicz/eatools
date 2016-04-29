package no.eatools.documenter;

import no.eatools.diagramgen.EaRepo;
import no.eatools.util.ObjectGraph;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sparx.Element;

/**
 * Represents an XSDType element as found in an EA Repos.
 *
 * @author ohs
 */
public class EaXsdType {
    private static final transient Logger LOG = LoggerFactory.getLogger(EaXsdType.class);

    private final EaRepo eaRepo;

    public EaXsdType(final EaRepo eaRepo) {
        this.eaRepo = eaRepo;
    }

    public void setNewDocumentation(final String s) {
        final Element xsdType = eaRepo.findXsdType(eaRepo.getRootPackage().unwrap(), "ComplexType1");

        final ObjectGraph objectGraph = new ObjectGraph();
        LOG.debug(objectGraph.createDotGraph(xsdType));

        LOG.debug("Old doc " + xsdType.GetNotes());
        LOG.debug("New doc " + s);
        xsdType.SetNotes(s);
        xsdType.Update();
    }
}
