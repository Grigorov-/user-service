package com.the.good.club.dataU.sdk;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.protobuf.ByteString;
import org.springframework.core.io.ResourceLoader;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static com.the.good.club.dataU.sdk.ClientUtils.UUIDStringToByteString;
import static com.the.good.club.dataU.sdk.ClientUtils.byteStringToUUID;

public class DataIdentificationGraphHelper {
    private static final Logger logger = Logger.getLogger(DataIdentificationGraphHelper.class.getName());
    public static Map<String, DataIdentificationGraphNode> dataIdentificationGraph = new HashMap<>();
    static Map<String, ByteString> localizedDataIdentificationGraph = new HashMap<>();
    public static final String BULK_PROCESS = "BULK";
    public static final String USER_ID = "user_id";
    public static final String DATE_TIME = "date_time";
    public static final String INDIVIDUAL_PROCESS = "INDIVIDUAL";
    public static final String UNKNOWN_FIELD = "UNKNOWN_FIELD";

    public static void loadDataIdentificationGraph(String dataIdentificationGraphFileName) {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        ClassLoader classLoader = ResourceLoader.class.getClassLoader();

        try (InputStream inputStream = classLoader.getResourceAsStream(dataIdentificationGraphFileName)) {
            List<DataIdentificationGraphNode> nodes = mapper.readValue(inputStream, DataIdentificationGraph.class).getNodes();

            nodes.forEach(node -> dataIdentificationGraph.put(node.getKey(), node));
        } catch (IOException e) {
            logger.info("Could not load " + dataIdentificationGraphFileName);
            e.printStackTrace();
        }
    }

    static void loadLocalizedDataIdentificationGraph(String localizedDataIdentificationGraphFileName) {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        ClassLoader classLoader = ResourceLoader.class.getClassLoader();

        try (InputStream inputStream = classLoader.getResourceAsStream(localizedDataIdentificationGraphFileName)) {
            Map<String, String> localizedStringsByUUIDs = mapper.readValue(inputStream, Map.class);

            for (Map.Entry<String, String> entry : localizedStringsByUUIDs.entrySet()) {
                localizedDataIdentificationGraph.put(entry.getValue(), UUIDStringToByteString(entry.getKey()));
            }

        } catch (IOException e) {
            logger.info("Could not load localized graph!");
            e.printStackTrace();
        }
    }


    /**
     * Retrieves list of children UUIDs for a node
     * @param nodeKey uuid of a node as ByteString
     * @return list of children UUIDs as ByteString
     *
     */
    public static List<ByteString> getChildrenUUIDs(ByteString nodeKey) {
        String nodeUUID = byteStringToUUID(nodeKey).toString();

        return dataIdentificationGraph.get(nodeUUID)
                .getChildren()
                .stream()
                .map(ClientUtils::UUIDStringToByteString)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves list of children names for a node
     * @param nodeKey uuid of a node as ByteString
     * @return list of children names
     *
     */
    public static List<String> getChildrenNames(ByteString nodeKey) {
        String nodeUUID = byteStringToUUID(nodeKey).toString();

        return dataIdentificationGraph.get(nodeUUID)
                .getChildren()
                .stream()
                .map(childUUID -> dataIdentificationGraph.get(childUUID).getDescription())
                .collect(Collectors.toList());
    }

    /**
     * Retrieves parent uuid for the provided children list
     * @param childrenNames list of children names e.g. Arrays.asList("first name", "last name", "birth names")
     * @return parent UUID as ByteString
     */
    public static ByteString getParent(List<String> childrenNames) {
        List<String> copyOfChildrenNames = new ArrayList<>(childrenNames);

        // ignore metadata fields
        copyOfChildrenNames.remove(USER_ID);
        copyOfChildrenNames.remove(DATE_TIME);

        Collections.sort(copyOfChildrenNames);

        Optional<DataIdentificationGraphNode> parentOptional = dataIdentificationGraph
                .values()
                .stream()
                .filter(node -> copyOfChildrenNames.equals(getNodesNames(node.getChildren())))
                .findAny();

        return parentOptional
                .map(dataIdentificationGraphNode -> UUIDStringToByteString(dataIdentificationGraphNode.getKey()))
                .orElse(null);
    }

    /**
     * Retrieves uuid for the provided node name
     * @param nodeName description of the field represented by the node
     * @return UUID as ByteString or null if a node with the provided name does not exist
     */
    public static ByteString getNodeUUID(String nodeName) {
        Optional<DataIdentificationGraphNode> nodeOptional = dataIdentificationGraph
                .values()
                .stream()
                .filter(node -> nodeName.equals(node.getDescription()))
                .findFirst();

        return nodeOptional
                .map(dataIdentificationGraphNode -> UUIDStringToByteString(dataIdentificationGraphNode.getKey()))
                .orElse(null);
    }

    /**
     * Retrieves UUIDs for all nodes with provided node name
     * @param nodeName description of the field represented by the node
     * @return a list with UUIDs or empty list if no nodes with the provided name exist
     */
    public static List<ByteString> getNodesUUIDs(String nodeName) {
        return dataIdentificationGraph
                .values()
                .stream()
                .filter(node -> nodeName.equals(node.getDescription()))
                .map(dataIdentificationGraphNode -> UUIDStringToByteString(dataIdentificationGraphNode.getKey()))
                .collect(Collectors.toList());
    }

    static List<String> getNodesNames(List<String> nodesUUIDs) {
        return nodesUUIDs
                .stream()
                .map(nodeUUID -> dataIdentificationGraph.get(nodeUUID).getDescription())
                .sorted()
                .collect(Collectors.toList());
    }

    /**
     * Retrieves node name for the provided node UUID
     * @param nodeUUID as ByteString
     * @return node name as String
     */
    public static String getNodeName(ByteString nodeUUID) {
        DataIdentificationGraphNode dataIdentificationGraphNode =
                dataIdentificationGraph.get(byteStringToUUID(nodeUUID).toString());
        if (dataIdentificationGraphNode != null) {
            return dataIdentificationGraphNode.getDescription();
        } else {
            return UNKNOWN_FIELD;
        }
    }

    /**
     * Retrieves process uuid for the provided process name
     * @param processName one of String 'BULK' | 'INDIVIDUAL'
     * @return process UUID as ByteString
     */
    public static ByteString getProcessUUID(String processName) {
        if (BULK_PROCESS.equals(processName)) {
            return UUIDStringToByteString("1234f4fd-8ff3-46ab-a950-8f136addba5c");
        } else if (INDIVIDUAL_PROCESS.equals(processName)){
            return UUIDStringToByteString("b4f3e53e-7af4-46b0-8bab-5256ff420080");
        } else {
            return null;
        }
    }

    /**
     * Retrieves process name for the provided process UUID
     * @param processUUID as ByteString
     * @return process name as String
     */
    public static String getProcessName(ByteString processUUID) {
        if (UUIDStringToByteString("1234f4fd-8ff3-46ab-a950-8f136addba5c").equals(processUUID)) {
            return BULK_PROCESS;
        } else if (UUIDStringToByteString("b4f3e53e-7af4-46b0-8bab-5256ff420080").equals(processUUID)){
            return INDIVIDUAL_PROCESS;
        } else {
            return null;
        }
    }
}
