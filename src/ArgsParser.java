// By Max Whitehouse.


import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import static java.lang.System.exit;

@SuppressWarnings("unused")
public class ArgsParser {

    // Parser things.
    private final ProgrammeDetails programmeDetails;

    private final ArrayList<ArgOption> argOptions;

    private final HashMap<String, ArgOption> keyMap = new HashMap<>();

    private ArgOption optionListArg = null;

    private final String help;

    private static final String[] HELP_FLAGS = new String[] {"-h", "--help", "--Help"};

    // Input things.
    private String[] rawInputs;

    private ArgReceived listArg = null;

    private final HashMap<ArgOption, ArgReceived> optionResultMap = new HashMap<>();

    private final HashMap<Character, ArgReceived> shortMap = new HashMap<>();

    private final HashMap<String, ArgReceived> longMap = new HashMap<>();

    private ArgReceived currentKeyPair;

    private ArgReceived lastKeyPair;

    private boolean expectingKey = false;



    /**
     * Make an enum that implements EnumOptions and has a field that contains an ArgOption. Then call {@code
     * ArgParser argParser = ArgParser(programmeDetails, EnumArgOptions.class);} <br><br>
     *
     * Why? Because then you can index into the results of the parse with the enum, for example {@code
     * argParser.isPassed(EnumArgOptions.OPTION1);} <br><br>
     *
     * Enum:
     * <pre> {@code
     * enum EnumArgOptions implements EnumOptions {
     *     OPTION1 (new ArgOption(...)),
     *     ... ;
     *
     *     private ArgOption option;
     *
     *     EnumArgOptions(ArgOption option) {
     *         this.option = option;
     *     }
     *
     *     public ArgOption get() {
     *         return option;
     *     }
     * } } </pre>
     */
    public <E extends Enum<E> & EnumOptions> ArgsParser(ProgrammeDetails programmeDetails, Class<E> enumArgOptions) {
        this(programmeDetails, convertEnumToOptionsList(enumArgOptions));
    }

    private static <E extends Enum<E> & EnumOptions> ArrayList<ArgOption> convertEnumToOptionsList(
            Class<E> enumArgOptions) {
        ArrayList<ArgOption> argOptions = new ArrayList<>();
        for (EnumOptions enumOption : enumArgOptions.getEnumConstants()) {
            argOptions.add(enumOption.get());
        }
        return argOptions;
    }

    public ArgsParser(ProgrammeDetails programmeDetails, ArgOption[] argOptions) {
        this(programmeDetails, new ArrayList<>(Arrays.asList(argOptions)));
    }

    public ArgsParser(ProgrammeDetails programmeDetails, ArrayList<ArgOption> argOptions) {
        if (programmeDetails == null) {
            throw new NullPointerException("programmeDetails cannot be null.");
        }

        if (argOptions == null) {
            throw new NullPointerException("argOptions cannot be null.");
        }

        if (programmeDetails.getCommandName() == null || programmeDetails.getCommandName().isEmpty()) {
            throw new ArgumentOptionException("The command mnemonic/name must be set.");
        }

        this.programmeDetails = programmeDetails;
        this.argOptions = argOptions;
        for (ArgOption argOption : argOptions) {
            validateOptions(argOption);
            buildKeyMap(argOption);
        }

        help = new HelpBuilder().buildHelp();
    }

    private void validateOptions(ArgOption argOption) {
        if (argOption.shortKey == '\0' && argOption.longKey.isEmpty() && argOption.usage != E_Usage.LIST) {
            String message = "Nether a short or long key have been provided for the following Argument. \n"
                    + "Argument option: " + argOption;
            throw new ArgumentOptionException(message);
        }

        if (argOption.longKey.length() == 1) {
            throw new ArgumentOptionException("Long keys should be at least 2 charters long. \n"
                    + "Argument option: " + argOption);
        }

        if (argOption.usage == null) {
            throw new ArgumentOptionException("The usage of an argument option must be set. \n"
                    + "Argument option: " + argOption);
        }

        checkForRepeatKeys(argOption);

        identifyUsageList(argOption);
    }

    private void checkForRepeatKeys(ArgOption argOption) {
        boolean dupeShortKey = keyMap.containsKey(String.valueOf(argOption.shortKey));
        boolean dupeLongKey = keyMap.containsKey(argOption.longKey);

        if (dupeShortKey || dupeLongKey) {
            ArgOption argShort = keyMap.get(String.valueOf(argOption.shortKey));
            ArgOption argLong = keyMap.get(argOption.longKey);

            String message;
            if (dupeShortKey && dupeLongKey) {
                message = "Arguments share a short and long key. \n" + "Option in conflict: " + argOption
                        + "\nShort key: " + argLong + "\nLong key: " + argShort;
            } else if (dupeShortKey) {
                message = "Arguments share a short key. \n" + "Option in conflict: " + argOption
                        + "\nShort key: " + argShort;
            } else {
                message = "Arguments share a long key. \n" + "Option in conflict: " + argOption
                        + "\nLong key: " + argLong;
            }
            throw new ArgumentOptionException(message);
        }
    }

    private void identifyUsageList(ArgOption argOption) {
        if (argOption.usage == E_Usage.LIST) {
            if (optionListArg != null) {
                throw new ArgumentOptionException("More than one list argument has been set. \n"
                        + "Options 1: " + optionListArg + "\n" + "Options 2: " + argOption);
            }
            optionListArg = argOption;
        }
    }

    private void buildKeyMap(ArgOption argOption) {
        if (argOption.shortKey != '\0') {
            keyMap.put(String.valueOf(argOption.shortKey), argOption);
        }

        if (!argOption.longKey.isEmpty()) {
            keyMap.put(argOption.longKey, argOption);
        }
    }



    public void pareArgs(String[] commandLineArgs) {
        rawInputs = commandLineArgs;
        checkForHelpRequest();
        initParseState();
        parseInputs();
    }

    private void checkForHelpRequest() {
        // Note(Max): I still prefer this over streams.
        for (String input : rawInputs) {
            for (String helpFlag : HELP_FLAGS) {
                if (input.equals(helpFlag)) {
                    System.out.println(help);
                    exit(0);
                }
            }
        }
    }

    private void initParseState() {
        if (optionListArg != null) {
            listArg = new ArgReceived(optionListArg);
        }

        // This can be true as pos args are treated as keys.
        expectingKey = true;
    }



    private void parseInputs() {
        for (String rawInput : rawInputs) {
            if (expectingKey) {
                getKey(rawInput);
                continue;
            }

            // Expecting a value.
            checkMalformedValue(rawInput);
            assignValueAndCycleSearch(rawInput);
        }

        // There is only one case where we should exit the loop while not expecting to receive a Key next. That is when
        // we are receiving values for the listArg.
        if (!expectingKey && currentKeyPair != listArg) {
            throw new ParseArgumentException("All input was parsed and a value was still expected."
                    + ((currentKeyPair == null) ? "" : "\nExpected Value Owner: " + currentKeyPair + "."));
        }
    }

    private void getKey(String rawInput) {
        if (rawInput.startsWith("--")) {
            String trimmedInput = rawInput.substring("--".length());
            parseKey(trimmedInput, true);
            return;
        }

        if (rawInput.startsWith("-")) {
            String trimmedInput = rawInput.substring("-".length());
            parseKey(trimmedInput, false);
            return;
        }

        if (listArg == null) {
            throw new ParseArgumentException("A key was expected. Check for spaces. \nReceived: " + rawInput + "\n");
        }
        // This must be the first positional arg.
        listArg.addValue(rawInput);
        currentKeyPair = listArg;
        expectingKey = false;
    }

    private void parseKey(String input, boolean isLongKey) {
        // Get the key and a value. Value maybe empty. This protects the short key from malformed name value pairs.
        String[] splitInput = input.split("=", 2);

        String key = splitInput[0];

        ArgOption argOption = keyMap.get(key);
        ArgReceived argReceived = optionResultMap.computeIfAbsent(argOption, ArgReceived::new);


        if (argOption == null) {
            throw new ParseArgumentException("No key match.\n"
                    + "Passed Key: " + key + ".\n"
                    + "Input: " + input + ".");
        }

        if (!isLongKey && key.equals(argOption.longKey)) {
            throw new ParseArgumentException("A long key has been passed with only one dash this effects argument formatting.\n"
                    + "Please add a dash.\n"
                    + "Key: " + key + ".\n"
                    + "Input: " + Arrays.toString(rawInputs));
        } else if (isLongKey && key.charAt(0) == argOption.shortKey) {
            throw new ParseArgumentException("A short key has been passed with two dash this effects argument formatting.\n"
                    + "Please remove the extra dash.\n"
                    + "Key: " + key + ".\n"
                    + "Input: " + Arrays.toString(rawInputs));
        }

        if (!expectingKey) {
            throw new ParseArgumentException("New key received while not expecting a new key.\n"
                    + "This is likely due to a missing key-value missing a value.\n"
                    + "New Key: " + key + ".\n"
                    + "Input: " + input + ".");
        }

        if (!argReceived.getValues().isEmpty() && !argOption.isRepeatable()) {
            String errorStart = "An argument has been used multiply times that should only be used once.\n";
            String errorMid = "";
            String errorEnd = "Short Key: '" + argOption.getShortKey() + "', Long Key: \"" + argOption.getLongKey() + "\".\n"
                    + "Input: " + Arrays.toString(rawInputs);

            if (argOption.getUsage() == E_Usage.KEY_VALUE) {
                String shortError = "First Usage Value: " + argReceived.getValue() + ".\n";
                String longError = "Second Usage: " + input + "First Usage Value: " + argReceived.getValue() + ".\n";

                errorMid = (isLongKey) ? longError : shortError;
            }

            throw new ParseArgumentException(errorStart + errorMid + errorEnd);
        }

        expectingKey = false;

        switch (argOption.getUsage()) {
            case KEY:
                if (isLongKey) {
                    checkLongKeyKeyErrors(splitInput, argOption);
                } else {
                    checkShortKeyKeyErrors(splitInput, argOption);
                }
                updateResultMaps(argReceived);
                argReceived.addValue("");
                expectingKey = true;
                lastKeyPair = argReceived;
                currentKeyPair = null;
                break;

            case KEY_VALUE:
                if (isLongKey) {
                    checkLongKeyKeyValueErrors(splitInput, argOption);
                    updateResultMaps(argReceived);
                    argReceived.addValue(splitInput[1]);
                    expectingKey = true;
                    lastKeyPair = argReceived;
                    currentKeyPair = null;
                } else {
                    checkShortKeyKeyValueErrors(splitInput, argOption);
                    expectingKey = false;
                    lastKeyPair = currentKeyPair;
                    currentKeyPair = argReceived;
                }
                break;

            case LIST:
                throw new ArgsParser.ParseArgumentException("During command line key parsing, a key of type list was "
                        + "found.");
        }
    }

    private void checkLongKeyKeyErrors(String[] splitInput, ArgOption argOption) {
        if (splitInput.length >= 2) {
            String message = "A long key was used as a pair with a value instead of a lone key. Good format: ... --" + argOption.longKey + "... \n"
                    + "Current parse: " + Arrays.toString(splitInput) + "\n"
                    + "Key options: " + argOption;
            throw new ParseArgumentException(message);
        }
    }

    private void checkLongKeyKeyValueErrors(String[] splitInput, ArgOption argOption) {
        if (splitInput.length < 2) {
            String message = "A long key for a key-value pair was used without the value. Good format: ... --" + argOption.longKey + "=value ... \n"
                    + "Current parse: " + Arrays.toString(splitInput) + "\n"
                    + "Key-value pair options: " + argOption;
            throw new ParseArgumentException(message);
        }
    }

    private void checkShortKeyKeyErrors(String[] splitInput, ArgOption argOption) {
        if (splitInput.length >= 2) {
            String message = "A short key was used with an equals sign."
                    + "Current parse: " + Arrays.toString(splitInput) + "\n"
                    + "Key options: " + argOption;
            throw new ParseArgumentException(message);
        }
    }

    private void checkShortKeyKeyValueErrors(String[] splitInput, ArgOption argOption) {
        if (splitInput.length >= 2) {
            String message = "A short key was used as a pair with a value join via an equals. Good format: ... -" + argOption.shortKey + " value ..."
                    + "Current parse: " + Arrays.toString(splitInput) + "\n"
                    + "Key-value pair options: " + argOption;
            throw new ParseArgumentException(message);
        }
    }

    private void updateResultMaps(ArgReceived argReceived) {
        ArgOption option = argReceived.option;
        optionResultMap.put(option, argReceived);

        if (option.shortKey != '\0') {
            shortMap.put(option.shortKey, argReceived);
        }

        if (!option.longKey.isEmpty()) {
            longMap.put(option.longKey, argReceived);
        }
    }

    private void checkMalformedValue(String rawInput) {
        if (rawInput.startsWith("-")) {
            String errorMessage =
                    "Expected a value, got a key. Look for spaces and check if a key can except a value.\n"
                            + "Last group: " + lastKeyPair + "\n"
                            + "Malformed value: " + rawInput;
            throw new ParseArgumentException(errorMessage);
        }
    }

    private void assignValueAndCycleSearch(String value) {
        if (currentKeyPair == null) {
            throw new ParseArgumentException("A value was passed while no key has been parsed to attach the value to.\n"
                    + "Passed Value: " + value + "."
                    + ((lastKeyPair == null) ? "" : "\nLast Argument Parsed: " + lastKeyPair + "."));
        }

        // Note(Max): Once the first argument assigned to `listArg` is passed the all subsequent values should be a part
        // of the listArg value.
        if (listArg != null && listArg == currentKeyPair) {
            listArg.addValue(value);
            expectingKey = false;
            return;
        }

        updateResultMaps(currentKeyPair);
        currentKeyPair.addValue(value);
        lastKeyPair = currentKeyPair;
        currentKeyPair = null;
        expectingKey = true;
    }



    public <T extends ArgsParser.EnumOptions> boolean isPassed(T option) {
        return isPassed(option.get());
    }

    public boolean isPassed(ArgOption option) {
        if (option == null) {
            throw new NullPointerException("The option received to look for a result was null.");
        }

        return optionResultMap.containsKey(option) && optionResultMap.get(option) != null;
    }

    public boolean isPassed(String key) {
        boolean isShortKey = key.length() == 1;
        boolean hasShortKey = isShortKey && isShortPassed(key.charAt(0));
        return longMap.containsKey(key) || hasShortKey;
    }

    public boolean isShortPassed(char key) {
        return shortMap.containsKey(key);
    }

    public boolean isLongPassed(String longKey) {
        return longMap.containsKey(longKey);
    }

    public <T extends ArgsParser.EnumOptions> ArgReceived getArgument(T option) {
        return optionResultMap.get(option.get());
    }

    public ArgReceived getArgument(String key) {
        if (longMap.containsKey(key)) {
            return longMap.get(key);
        }

        boolean isShortKey = key.length() == 1;
        if (isShortKey && shortMap.containsKey(key.charAt(0))) {
            return shortMap.get(key.charAt(0));
        }

        return null;
    }

    public ArgReceived getShortArgument(char key) {
        return shortMap.get(key);
    }

    public ArgReceived getLongPassed(String longKey) {
        return longMap.get(longKey);
    }

    public String getHelpText() {
        return help;
    }



    /**
     * The format, set by usage, for args are: <br>
     * &emsp;     1) -k <br>
     * &emsp;    2) --Key-Word <br>
     * &emsp;     3) -k arg <br>
     * &emsp;     4) --Key-Word=arg <br>
     * &emsp;    5) ... argList     &emsp;// There can only be one of these list args, and they are always at the end of the command. <br><br>
     *
     * The short key is `k`, the long key is `Key-Word`. <br><br>
     *
     * The position field describes how to combine the received string. If the key is to be parsed on its own assign it
     * `KEY` 1) and 2), if the argument should be a key-value pair assign `KEY_VALUE` 3) and 4) or if it should be a
     * list arg assign `LIST` 5). <br><br>
     *
     * The description, alongside the short value example, long value example and list example will be used in the help.
     * This is dependent on whether they are set, if they match the usage and if the associated key is set. <br><br>
     *
     * There are 3 reserved keys they are -h, --help and --Help. They print the help and stop excursion.
     */
    public static class ArgOption {

        /**
         * The short key used for an argument. Should be a single char.
         * <br>
         * Example: 'w' -> '-w'
         */
        private char shortKey = '\0';

        /**
         * The long key used for an argument. Should be more than one char long.
         * <br>
         * {@code Example: 'Window-Name' -> '--Window-Name'}
         */
        private String longKey = "";

        /**
         * The way to use the argument. See {@link E_Usage} for a description of the options.
         */
        private E_Usage usage = null;

        /**
         * An example usage of the short key that will be printed for the user if they use one of the help flags.
         * <br>
         * {@code Example: (1, 2, 3) -> ColColorize ... -t (1,2,3) ...}
         */
        private String shortValueExample = "{value}";

        /**
         * An example usage of the long key that will be printed for the user if they use one of the help flags.
         * <br>
         * {@code Example: Bright Blue -> ColColorize ... -Text-Colour="Bright Blue" ...}
         */
        private String longKeyValueExample = "{value}";

        /**
         * An example usage of the space delimited list positional argument that will be printed for the user if they
         * use one of the help flags.
         * <br>
         * {@code Example: Max, Maximus and Maximilian -> BuildProfiles ... Max Maximus Maximilian}
         */
        private String listExample = "{value} {value} {value}";

        /**
         * A description of what the argument is used for.
         */
        private String description = "";

        /**
         * Set this to true if the option is supposed to be used without any other arguments. I.E. '--help'.
         */
        private boolean useOnItsOwn = false;

        private boolean repeatable = false;

        @SuppressWarnings({"unused", "FieldMayBeFinal"})
        private E_Types typeSig = null;



        /**
         * Return the long key if the argument has one set if not the short key is returned.
         */
        public String getName() {
            return (longKey.isEmpty()) ? String.valueOf(shortKey) : longKey;
        }

        /**
         * Return the short key, if not set will be {@code '\0'}. See {@link ArgsParser.ArgOption#shortKey}.
         */
        public char getShortKey() {
            return shortKey;
        }

        /**
         * Sets the short key. See {@link ArgsParser.ArgOption#shortKey}.
         */
        public ArgOption setShortKey(char shortKey) {
            this.shortKey = shortKey;
            return this;
        }

        /**
         * Return the long key, if not set will be empty. See {@link ArgsParser.ArgOption#longKey}.
         */
        public String getLongKey() {
            return longKey;
        }

        /**
         * Sets the long key. See {@link ArgsParser.ArgOption#longKey}.
         *
         * @throws ArgumentOptionException if the long key is not at least 2 chars long.
         */
        public ArgOption setLongKey(String longKey) {
            if (longKey.length() < 2) {
                throw new ArgumentOptionException("Long keys should be at least 2 charters long. \n");
            }
            this.longKey = longKey;
            return this;
        }


        public E_Usage getUsage() {
            return usage;
        }

        public ArgOption setUsage(E_Usage usage) {
            this.usage = usage;
            return this;
        }

        public String getShortValueExample() {
            return shortValueExample;
        }

        public ArgOption setShortValueExample(String shortValueExample) {
            this.shortValueExample = shortValueExample;
            return this;
        }

        public String getLongKeyValueExample() {
            return longKeyValueExample;
        }

        public ArgOption setLongKeyValueExample(String longKeyValueExample) {
            this.longKeyValueExample = longKeyValueExample;
            return this;
        }

        public String getListExample() {
            return listExample;
        }

        public ArgOption setListExample(String listExample) {
            this.listExample = listExample;
            return this;
        }

        public String getDescription() {
            return description;
        }

        public ArgOption setDescription(String description) {
            this.description = description;
            return this;
        }

        public boolean isUseOnItsOwn() {
            return useOnItsOwn;
        }

        /**
         * Set this as true and if the key is passed it should be the only key. Like '--help'.
         * <br>
         * Default: false.
         */
        public ArgOption setUseOnItsOwn(boolean useOnItsOwn) {
            this.useOnItsOwn = useOnItsOwn;
            return this;
        }

        public boolean isRepeatable() {
            return repeatable;
        }

        public ArgOption setRepeatable(boolean repeatable) {
            this.repeatable = repeatable;
            return this;
        }



        @Override
        public String toString() {
            return "ArgOption{" +
                    "shortKey='" + shortKey + "', " +
                    "longKey=\"" + longKey + "\", " +
                    "usage=" + usage + ", " +
                    "shortValueExample=\"" + shortValueExample + "\", " +
                    "longKeyValueExample=\"" + longKeyValueExample + "\", " +
                    "listExample=\"" + listExample + "\", " +
                    "description=\"" + description + "\"" +
                    "}";
        }
    }

    public static class ArgReceived {

        private final ArgOption option;

        private final ArrayList<String> values = new ArrayList<>();



        public ArgReceived(ArgOption option) {
            this.option = option;
        }



        private void addValue(String value) {
            values.add(value);
        }



        public ArgOption getOption() {
            return option;
        }

        /**
         * @return A list of length n, where n is the number of times the argument has been used. If the argument is a
         * key type then the string will be empty.
         */
        public ArrayList<String> getValues() {
            return values;
        }

        /**
         * @return The string passed to the first use of this argument if it is a key-value pair, an empty string if it
         * was a key that was used or null if the argument has not been used.
         */
        public String getValue() {
            return (values.isEmpty()) ? null : values.get(0);
        }

        @Override
        public String toString() {
            return "ArgReceived{Values=\"" + values + "\", Option=" + option + "}";
        }

    }

    public static class ProgrammeDetails {

        /**
         * This is the mnemonic used to call the programme. This field must be set by the user.
         */
        private String commandName = "";

        /**
         * This field is used in the help text. If this field is not set by the user commandName will be used instead.
         */
        private String programmeName = "";

        private String programmeDescription = "";

        private String author = "";

        private String version = "";



        public String getCommandName() {
            return commandName;
        }

        public ProgrammeDetails setCommandName(String commandName) {
            this.commandName = commandName;
            return this;
        }

        public String getProgrammeName() {
            return programmeName;
        }

        public ProgrammeDetails setProgrammeName(String programmeName) {
            this.programmeName = programmeName;
            return this;
        }

        public String getProgrammeDescription() {
            return programmeDescription;
        }

        public ProgrammeDetails setProgrammeDescription(String programmeDescription) {
            this.programmeDescription = programmeDescription;
            return this;
        }

        public String getAuthor() {
            return author;
        }

        public ProgrammeDetails setAuthor(String author) {
            this.author = author;
            return this;
        }

        public String getVersion() {
            return version;
        }

        public ProgrammeDetails setVersion(String version) {
            this.version = version;
            return this;
        }

    }

    private class HelpBuilder {

        public StringBuilder stringBuilder = new StringBuilder();

        // All measurements are mono-font cells.
        private int infoWidth;

        private static final int LINE_WIDTH = 100;

        private static final int NAME_MARGIN = 5;

        private static final int BASE_NAME_COL_WIDTH = 40;

        private static final int EXTRA_NAME_SPACE = 10;

        private static final int LEFT_MARGIN_WIDTH = 2;

        private static final int KEY_DESCRIPTION_GAP = 3;

        private static final String HELP_FLAG_DESCRIPTION = "Use to print this help.";

        private static final String LIST_USAGE_KEY = "[SPACE DELIMITED LIST]";

        private static final String LIST_USAGE = "List, a space delimited list of values at the end of the command.";

        private static final String EXAMPLE_PREFIX = "Example: ";



        public String buildHelp() {
            buildNameBox();
            buildProgrammeDescription();
            buildProgrammeDetail();
            buildOptionHelpBlocks();
            stringBuilder.append("\n");
            return stringBuilder.toString();
        }

        private void buildNameBox() {
            int nameSpace = LINE_WIDTH - (NAME_MARGIN * 2);

            String programmeName = (programmeDetails.programmeName.isEmpty())
                    ? programmeDetails.commandName : programmeDetails.programmeName;

            ArrayList<String> nameLines = lineWrapString(programmeName, nameSpace);

            boxProgrammeNameLines(nameLines);

            addLinesToHelpText(nameLines);
        }

        private void boxProgrammeNameLines(ArrayList<String> nameLines) {
            for (int i = 0; i < nameLines.size(); i++) {
                String line = nameLines.get(i);

                int leftSpace = (LINE_WIDTH - line.length()) / 2;
                int rightSpace = LINE_WIDTH - (leftSpace + line.length());

                String newLine = dupeString("=", leftSpace - 1) + " " + line
                        + " " + dupeString("=", rightSpace - 1);

                nameLines.set(i, newLine);
            }

            nameLines.add(0, dupeString("=", LINE_WIDTH));
            nameLines.add(dupeString("=", LINE_WIDTH));
        }

        private void buildProgrammeDescription() {
            String description = programmeDetails.programmeDescription;

            if (description.isEmpty()) {
                return;
            }

            addLinesToHelpText(lineWrapString(description, LINE_WIDTH));
        }

        private void buildProgrammeDetail() {
            String author = programmeDetails.author;
            String version = programmeDetails.version;

            if (!author.isEmpty()) {
                author = "By " + author;
                author += (version.isEmpty()) ? "." : ", ";
            }

            if (!version.isEmpty()) {
                String v = (author.isEmpty()) ? "Version " : "version ";
                version = v + version + ".";
            }

            String details = author + version;

            if (details.isEmpty()) {
                return;
            }

            addLinesToHelpText(lineWrapString(details, LINE_WIDTH));
        }

        private void buildOptionHelpBlocks() {
            // The fist option to be printed is help.

            handBuildHelpOptionBlock();

            // Note(Max): Have a temp variable so access to the parents classes private members isn't needed.
            // We pull listOption out so that we can make sure it is the last option printed in the help and has one
            // more new line.
            ArgOption listOption = null;

            for (ArgOption option : argOptions) {
                if (option.usage == E_Usage.LIST) {
                    listOption = option;
                    continue;
                }

                buildOptionBlock(option);
            }

            if (listOption != null) {
                stringBuilder.append("\n");
                buildOptionBlock(listOption);
            }
        }

        private void handBuildHelpOptionBlock() {
            stringBuilder.append("\n");

            StringBuilder keyList = new StringBuilder();

            for (String helpFlag : HELP_FLAGS) {
                keyList.append(helpFlag).append(", ");
            }

            keyList.delete(keyList.length() - 2, keyList.length() - 1);
            String keyLine = dupeString(" ", LEFT_MARGIN_WIDTH) + keyList;


            infoWidth = calcInfoWidth(keyLine);
            ArrayList<String> description = lineWrapString(HELP_FLAG_DESCRIPTION, infoWidth);

            ArrayList<String> example = lineWrapString(
                    EXAMPLE_PREFIX + programmeDetails.commandName + " " + HELP_FLAGS[0],
                    infoWidth
            );

            ArrayList<String> infoLines = new ArrayList<>();
            infoLines.addAll(description);
            infoLines.addAll(example);


            mergeAndIndent(keyLine, infoLines);

            addLinesToHelpText(infoLines);
        }

        private void buildOptionBlock(ArgOption option) {
            stringBuilder.append("\n");

            String keyLine = buildKeyLine(option);

            ArrayList<String> infoLines = buildInfo(option, keyLine);

            mergeAndIndent(keyLine, infoLines);

            addLinesToHelpText(infoLines);
        }

        private String buildKeyLine(ArgOption option) {
            if (option.usage == E_Usage.LIST) {
                return dupeString(" ", LEFT_MARGIN_WIDTH) + LIST_USAGE_KEY;
            }

            String keyLine = buildShortKeyString(option.shortKey);
            keyLine = buildLongKeyString(option.longKey, keyLine);
            return keyLine;
        }

        private String buildShortKeyString(char shortKey) {
            if (shortKey == '\0') {
                int keyWidth = "-x, ".length();
                return dupeString(" ", LEFT_MARGIN_WIDTH + keyWidth);
            }

            return dupeString(" ", LEFT_MARGIN_WIDTH) + "-" + shortKey + ", ";
        }

        private String buildLongKeyString(String longKey, String keyLine) {
            // Note(Max): Reassigning to keyLine looks like it might be wrong.
            int spaceForLongKey = (BASE_NAME_COL_WIDTH + EXTRA_NAME_SPACE) - (keyLine.length());

            if ("--".length() + longKey.length() + KEY_DESCRIPTION_GAP > spaceForLongKey) {
                int maxLongKeyLength = spaceForLongKey - (2 + KEY_DESCRIPTION_GAP);
                throw new ArgumentOptionException("Long key is too long for the help block.\n" +
                        "With the current setting the charter limit for a long key is " + maxLongKeyLength + ".\n" +
                        "Long key: " + longKey);
            }

            keyLine += "--" + longKey + dupeString(" ", KEY_DESCRIPTION_GAP);

            // This is where you would change the layout so that you only let the extra space be used a chunk at a time
            // rather than a space at a time I.E. use 5 spaces rather than 2.
            int extraSpace = keyLine.length() - BASE_NAME_COL_WIDTH;
            if (extraSpace > 0) {
                keyLine += dupeString(" ", extraSpace);
            }

            return keyLine;
        }

        private ArrayList<String> buildInfo(ArgOption option, String keyLine) {
            infoWidth = calcInfoWidth(keyLine);

            ArrayList<String> description = buildDescription(option);

            ArrayList<String> usageOption = buildUsage(option);

            ArrayList<String> examples = buildExamples(option);

            ArrayList<String> infoLines = new ArrayList<>();
            infoLines.addAll(description);
            infoLines.addAll(usageOption);
            infoLines.addAll(examples);
            return infoLines;
        }

        private int calcInfoWidth(String keyLine) {
            // Note(Max): I would be easy to argue for this to set the field and not return a value, but I like how it
            // looks when it returns a value, so I am going to leave it like this for the moment.
            int indentWidth = Math.max(keyLine.length(), BASE_NAME_COL_WIDTH);

            return LINE_WIDTH - indentWidth;
        }

        private ArrayList<String> buildDescription(ArgOption option) {
            if (option.description.isEmpty()) {
                return new ArrayList<>(0);
            }

            return lineWrapString(option.description, infoWidth);
        }

        private ArrayList<String> buildUsage(ArgOption option) {
            String usage = "";
            switch (option.usage) {
                case KEY: usage = "Key"; break;
                case KEY_VALUE: usage = "Key-value pair"; break;
                case LIST: usage = LIST_USAGE.replaceAll(".$", ""); break;
            }

            return lineWrapString("Usage: " + usage + ".", infoWidth);
        }

        private ArrayList<String> buildExamples(ArgOption option) {
            ArrayList<String> out = null;

            switch (option.usage) {
                case KEY:
                case KEY_VALUE:
                    out = buildValueExamples(option); break;
                case LIST: out = buildListExample(option); break;
            }

            return out;
        }

        private ArrayList<String> buildListExample(ArgOption option) {
            String ellipses = (option.useOnItsOwn) ? " " : " ... ";
            String exampleBuilder = EXAMPLE_PREFIX + programmeDetails.commandName + ellipses + option.getListExample();
            return lineWrapString(exampleBuilder, infoWidth);
        }

        private ArrayList<String> buildValueExamples(ArgOption option) {
            String commandName = programmeDetails.commandName;
            ArrayList<String> exampleLines = new ArrayList<>();

            String ellipses = (option.useOnItsOwn) ? " " : " ... ";

            if (option.shortKey != '\0') {
                String valueExample = (option.usage == E_Usage.KEY) ? "" : " " + option.getShortValueExample();

                String shortExample = EXAMPLE_PREFIX + commandName + ellipses + "-" + option.getShortKey()
                        + valueExample + ellipses;

                exampleLines.addAll(lineWrapString(shortExample, infoWidth));
            }

            if (!option.longKey.isEmpty()) {
                String valueExample = (option.usage == E_Usage.KEY) ? "" : "=" + option.getLongKeyValueExample();

                String longExample = EXAMPLE_PREFIX + commandName + ellipses + "--" + option.getLongKey()
                        + valueExample + ellipses;

                exampleLines.addAll(lineWrapString(longExample, infoWidth));
            }

            return exampleLines;
        }

        private void mergeAndIndent(String keyLine, ArrayList<String> infoLines) {
            if (infoLines.size() >= 1) {
                String padding = dupeString(" ", LINE_WIDTH - (infoWidth + keyLine.length()));
                infoLines.set(0, keyLine + padding + infoLines.get(0));
            }

            for (int i = 1; i < infoLines.size(); i++) {
                String newLine = dupeString(" ", LINE_WIDTH - infoWidth) + infoLines.get(i);
                infoLines.set(i, newLine);
            }
        }



        private ArrayList<String> lineWrapString(String input, int lineWidth) {
            ArrayList<String> wrappedLines = new ArrayList<>();

            String[] inputWords = input.split(" ");
            StringBuilder lineBuilder = new StringBuilder(lineWidth);
            int lineLength = 0;

            for (String word : inputWords) {
                if (lineLength + word.length() <= lineWidth) {
                    lineBuilder.append(word).append(" ");
                    lineLength += 1 + word.length();
                    continue;
                }

                wrappedLines.add(lineBuilder.toString().trim());
                lineLength = word.length();
                lineBuilder = new StringBuilder(lineWidth);
                lineBuilder.append(word).append(" ");
            }

            wrappedLines.add(lineBuilder.toString().trim());
            return wrappedLines;
        }

        private String dupeString(String string, int count) {
            StringBuilder stringBuilder = new StringBuilder(string.length() * count);
            for (int i = 0; i < count; i++) {
                stringBuilder.append(string);
            }
            return stringBuilder.toString();
        }

        private void addLinesToHelpText(ArrayList<String> lines) {
            if (lines.isEmpty()) {
                return;
            }

            for (String line : lines) {
                stringBuilder.append(line).append(System.lineSeparator());
            }
        }

    }

    public static class ArgumentOptionException extends RuntimeException {

        public ArgumentOptionException() {
            super();
        }

        public ArgumentOptionException(String message) {
            super(message);
        }

    }

    public static class ParseArgumentException extends RuntimeException {

        public ParseArgumentException() {
            super();
        }

        public ParseArgumentException(String message) {
            super(message + "\n" + "Use -h, --help or --Help for help.");
        }

    }



    public enum E_Types {
        FILE,
        STRING,
        INT,
        FLOAT
    }

    public enum E_Usage {
        KEY,
        KEY_VALUE,
        LIST
    }

    public interface EnumOptions {
        ArgOption get();
    }

}
