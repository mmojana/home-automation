package ch.mojanam.mysensorsthing.mysensors.serialprotocol;

import java.util.stream.Stream;

public enum Command {
    PRESENTATION(0),
    SET(1),
    REQ(2),
    INTERNAL(3),
    STREAM(4);

    private Integer code;

    Command(Integer code) {
        this.code = code;
    }

    static Command fromCode(Integer code) {
        return Stream.of(Command.values())
                .filter(c -> code.equals(c.code))
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
    }

}
