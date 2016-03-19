#!/usr/bin/env bash


java -Xmx1024m -jar diagramgen.jar ea.application.properties -p ea.doc.root.dir=U:/tmp -p "ea.top.level.package=Information Architecture" -p ea.package.filter=Settlement -p ea.url.base=http://ea-images.elhub.org -p ea.diagram.name.level=2 -p ea.diagram.name.mode=GUID_AT_END
