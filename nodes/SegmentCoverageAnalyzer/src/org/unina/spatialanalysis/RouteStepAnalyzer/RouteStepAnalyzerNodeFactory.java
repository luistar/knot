package org.unina.spatialanalysis.RouteStepAnalyzer;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * This is an example implementation of the node factory of the
 * "RouteStepAnalyzer" node.
 *
 * @author Sinogrante Principe
 */
public class RouteStepAnalyzerNodeFactory 
        extends NodeFactory<RouteStepAnalyzerNodeModel> {

    /**
     * {@inheritDoc}
     */
    @Override
    public RouteStepAnalyzerNodeModel createNodeModel() {
		// Create and return a new node model.
        return new RouteStepAnalyzerNodeModel();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNrNodeViews() {
		// The number of views the node should have, in this cases there is none.
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NodeView<RouteStepAnalyzerNodeModel> createNodeView(final int viewIndex,
            final RouteStepAnalyzerNodeModel nodeModel) {
		// We return null as this example node does not provide a view. Also see "getNrNodeViews()".
		return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasDialog() {
		// Indication whether the node has a dialog or not.
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NodeDialogPane createNodeDialogPane() {
		// This example node has a dialog, hence we create and return it here. Also see "hasDialog()".
        return new RouteStepAnalyzerNodeDialog();
    }

}

