package com.the.good.club.dataU.sdk;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
@JsonIgnoreProperties(ignoreUnknown = true)
public class DataIdentificationGraph {
    @JsonProperty("didgraph")
    private List<DataIdentificationGraphNode> nodes;

    public List<DataIdentificationGraphNode> getNodes() {
        return nodes;
    }

    public void setNodes(List<DataIdentificationGraphNode> nodes) {
        this.nodes = nodes;
    }
}
