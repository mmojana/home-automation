package ch.mojanam.mysensorsthing.mysensors.serialprotocol;

public class Event {

    private Integer nodeId;
    private Integer childSensorId;
    private Command command;
    private Boolean ack;
    private Type type;
    private String payload;

    public Event(String message) {
        String[] parts = message.split(";");
        nodeId = Integer.valueOf(parts[0]);
        childSensorId = Integer.valueOf(parts[1]);
        command = Command.fromCode(Integer.valueOf(parts[2]));
        if (parts[3].equals("1")) {
            ack = true;
        } else if (parts[3].equals("0")) {
            ack = false;
        } else {
            throw new IllegalArgumentException("ack must be either 0 or 1");
        }
        type = Type.fromValue(Integer.valueOf(parts[4]), command);
        payload = parts[5];
    }

    public Integer getNodeId() {
        return nodeId;
    }

    public Integer getChildSensorId() {
        return childSensorId;
    }

    public Command getCommand() {
        return command;
    }

    public Boolean getAck() {
        return ack;
    }

    public Type getType() {
        return type;
    }

    public String getPayload() {
        return payload;
    }

    @Override
    public String toString() {
        return "Event{" +
                "nodeId=" + nodeId +
                ", childSensorId=" + childSensorId +
                ", command=" + command +
                ", ack=" + ack +
                ", type=" + type +
                ", payload='" + payload + '\'' +
                '}';
    }
}
