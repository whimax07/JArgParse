import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class EnumArgumentTest {

    ArgsParser.ProgrammeDetails makeProgrammeDetails() {
        return new ArgsParser.ProgrammeDetails().setCommandName("ColColorize");
    }



    @Test
    public void canAutoDetectAnnotatedClass() {
        assertDoesNotThrow(() -> ArgsParser.autoDetectArgumentEnum(makeProgrammeDetails()));
    }

}
