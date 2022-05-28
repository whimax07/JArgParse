import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ErrorTests {

    ArgsParser.ProgrammeDetails makeProgrammeDetails() {
        return new ArgsParser.ProgrammeDetails().setCommandName("ColColorize");
    }



    @Test
    void suppressingStackTraceDoesntBreakEverything() {
        ArgsParser argsParser = new ArgsParser(makeProgrammeDetails(), EnumArgOptions.class);
        argsParser.setParseErrorsDisplayStackTrace(false);

        String[] input = new String[] {"--Set-Text"};
        ArgsParser.ParseArgumentException parseArgumentException =
                assertThrows(ArgsParser.ParseArgumentException.class, () -> argsParser.pareArgs(input));

        parseArgumentException.printStackTrace();
    }

    @Test
    void notSuppressingStackTraceDoesntBreakEverything() {
        ArgsParser argsParser = new ArgsParser(makeProgrammeDetails(), EnumArgOptions.class);
        argsParser.setParseErrorsDisplayStackTrace(true);

        String[] input = new String[] {"--Set-Text"};
        ArgsParser.ParseArgumentException parseArgumentException =
                assertThrows(ArgsParser.ParseArgumentException.class, () -> argsParser.pareArgs(input));

        parseArgumentException.printStackTrace();
    }

}
