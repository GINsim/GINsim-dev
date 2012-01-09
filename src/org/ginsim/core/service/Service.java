package org.ginsim.core.service;

import org.ginsim.core.graph.regulatorygraph.RegulatoryGraph;
import org.ginsim.service.tool.reg2dyn.SimulationParameters;

/**
 * This interface is the central interface for GINsim services.
 * GINsim services are separated in four kind of services, all of them represented by distinct abstract classes
 * implementing this interface:
 *  - Import : those services correspond to data import from file of various format. They are represented by the
 *  		   GsImportService class
 *  - Export : those services correspond to data export to file of various format. They are represented by the
 *  		   GsExportService class
 *  - Layout : those services correspond to graph layout. They are represented by the
 *  		   LayoutService class
 *  - Action : those services correspond to various data management algorithm. They are represented by the
 *  		   GsActionService class
 *  
 *  Each new Service must extend one of these four abstract class and declare the annotation "@ProviderFor(Service.class)"
 * 
 * @author Lionel Spinelli
 *
 */

public interface Service {

	
}
