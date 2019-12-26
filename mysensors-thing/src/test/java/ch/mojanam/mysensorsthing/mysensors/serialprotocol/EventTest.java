package ch.mojanam.mysensorsthing.mysensors.serialprotocol;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class EventTest {

    @Test
    public void testPresentationMessage() {
        Event event = new Event("12;6;0;0;3;My Light");
        Assert.assertEquals(12, event.getNodeId().intValue());
        Assert.assertEquals(6, event.getChildSensorId().intValue());
        Assert.assertEquals(Command.PRESENTATION, event.getCommand());
        Assert.assertEquals(Type.S_BINARY, event.getType());
        Assert.assertEquals("My Light", event.getPayload());
    }

    @Test
    public void testSetMessage() {
        Event event = new Event("12;6;1;0;0;36.5");
        Assert.assertEquals(12, event.getNodeId().intValue());
        Assert.assertEquals(6, event.getChildSensorId().intValue());
        Assert.assertEquals(Command.SET, event.getCommand());
        Assert.assertEquals(Type.V_TEMP, event.getType());
        Assert.assertEquals("36.5", event.getPayload());
    }

    @Test
    public void testReqMessage() {
        Event event = new Event("13;7;1;0;2;1");
        Assert.assertEquals(13, event.getNodeId().intValue());
        Assert.assertEquals(7, event.getChildSensorId().intValue());
        Assert.assertEquals(Command.SET, event.getCommand());
        Assert.assertEquals(Type.V_STATUS, event.getType());
        Assert.assertEquals("1", event.getPayload());
    }

}
