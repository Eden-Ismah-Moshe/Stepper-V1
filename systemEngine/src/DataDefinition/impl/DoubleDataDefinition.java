package DataDefinition.impl;

import DataDefinition.api.AbstractDataDefinition;
public class DoubleDataDefinition extends AbstractDataDefinition {

    public DoubleDataDefinition() {
        super("Double", true, Double.class);
    }
}