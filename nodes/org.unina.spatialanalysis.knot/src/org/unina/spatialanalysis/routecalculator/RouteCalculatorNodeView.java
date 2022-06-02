package org.unina.spatialanalysis.routecalculator;

import java.io.IOException;

import org.knime.core.node.NodeView;



/**
 * This is an example implementation of the node view of the
 * "RouteCalculator" node.
 * 
 * As this example node does not have a view, this is just an empty stub of the 
 * NodeView class which not providing a real view pane.
 *
 * @author Sinogrante
 */
public class RouteCalculatorNodeView extends NodeView<RouteCalculatorNodeModel> {
    /**
     * Creates a new view.
     * 
     * @param nodeModel The model (class: {@link RouteCalculatorNodeModel})
     * @throws IOException 
     */
    protected RouteCalculatorNodeView(final RouteCalculatorNodeModel nodeModel) throws IOException {
        super(nodeModel);        
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void modelChanged() {

        // TODO retrieve the new model from your nodemodel and 
        // update the view.
        RouteCalculatorNodeModel nodeModel = 
            (RouteCalculatorNodeModel) getNodeModel();
        assert nodeModel != null;
        
        // be aware of a possibly not executed nodeModel! The data you retrieve
        // from your nodemodel could be null, emtpy, or invalid in any kind.
        
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onClose() {
    	
        // TODO things to do when closing the view
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onOpen() {

       
    }

}

