import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ArgsParserTests {

    @BeforeEach
    void setUp() {
    }

    @Test
    void pareArgs() {
    }

    @Test
    void isPassed() {
    }

    @Test
    void isShortPassed() {
    }

    @Test
    void isLongPassed() {
    }

    @Test
    void getArgument() {
    }

    @Test
    void getShortArgument() {
    }

    @Test
    void getLongPassed() {
    }

/* Target help output.
===================================================================================================
======================================= Set Console Colours =======================================
===================================================================================================
This program set the colours used by this console.
By Max Whitehouse, version 1.0.0.


  -b, --Set-Background                 This command sets the background colour of the console.
									   Usage: Key-value pair.
									   Example: ColColorize ... -b (0,0,0) ...
									   Example: ColColorize ... -Set-Background=(0,0,0) ...

  -t, --Set-Text                       This command sets the text colour if the console.
                                       Usage: Key-value pair.
									   Example: ColColorize ... -t {value} ...
									   Example: ColColorize ... -Set-Text={value} ...

  --Use-Defaults                       This command tells the console revert to its default colour
									   scheme. This should be used on its own.
									   Usage: Key.
									   Example: ColColorize --Use-Defaults

  [SPACE DELIMITED LIST]               This will take the path to json files and read a "Set
								       Console Colours" configuration file.
									   Usage: List, a space delimited list of values at the end of
									   the command.
									   Example: ColColorize ... {value} {value} {value}

*/
    @Test
    void getHelpText() {
    }
}