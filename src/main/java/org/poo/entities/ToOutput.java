package org.poo.entities;

import com.fasterxml.jackson.databind.node.ObjectNode;

public interface ToOutput {
    /**
     * This method is used to convert objects to JSON objects.
     * @return ObjectNode
     */
    ObjectNode toJson();
}
