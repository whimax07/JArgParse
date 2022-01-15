// By Max Whitehouse.


import java.util.*;

import static java.lang.System.exit;

public class ArgsParser {

    // Parser things.
    private final ProgrammeDetails programmeDetails;

    private final ArrayList<ArgOption> argOptions;

    private final HashMap<String, ArgOption> keyMap = new HashMap<>();

    private ArgOption optionListArg = null;

    private final String help;

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
     * ArgParser(programmeDetails, EnumArgOptions.class);} <br><br>
     *
     * Why? Because then you can index into the results of the parse with the enum. <br>
     *
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

    private static <E extends Enum<E> & EnumOptions> ArrayList<ArgOption>
    convertEnumToOptionsList(Class<E> enumArgOptions) {
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

        if (programmeDetails.commandName.isEmpty()) {
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
        for (String input : rawInputs) {
            if (input.equals("-h") || input.equals("--help") || input.equals("--Help")) {
                System.out.println(help);
                exit(0);
            }
        }
    }

    private void initParseState() {
        listArg = new ArgReceived(optionListArg);

        // This can be true as pos args are treated as keys.
        expectingKey = true;
    }

    private void parseInputs() {
        //noinspection ForLoopReplaceableByForEach
        for (int i = 0; i < rawInputs.length; i++) {
            parseInput(rawInputs[i]);
        }
    }

    private void parseInput(String rawInput) {
        if (expectingKey) {
            getKey(rawInput);
            return;
        }

        // Expecting a value.
        checkMalformedValue(rawInput);

        assignValue(rawInput);
    }

    private void getKey(String rawInput) {
        if (rawInput.startsWith("--")) {
            String input = rawInput.substring("--".length());
            parseKey(input, true);
            return;
        }

        if (rawInput.startsWith("-")) {
            String input = rawInput.substring("-".length());
            parseKey(input, false);
            return;
        }

        if (listArg == null) {
            throw new ParseArgumentException("A key was expected. Check for spaces. \nReceived: " + rawInput + "\n");
        }
        // This must be the first positional arg.
        listArg.value += " " + rawInput;
        currentKeyPair = listArg;
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

    private void parseKey(String input, boolean isLongKey) {
        // Get the key and a value. (value maybe empty.)
        String[] splitInput = input.split("=", 2);

        String key = splitInput[0];
        ArgOption argOption = keyMap.get(key);

        if (argOption == null) {
            throw new ParseArgumentException("Key could not be found. \nBad key: " + input);
        }

        boolean isKeyValuePair = argOption.usage == E_Usage.KEY_VALUE;

        // Do some error checking for miss formed or miss parsed args.
        if (isLongKey) {
            longKeyErrors(splitInput, argOption, isKeyValuePair);
        } else {
            shortKeyErrors(splitInput, argOption, isKeyValuePair);
        }

        ArgReceived argReceived = new ArgReceived(argOption);
        longMap.put(argOption.longKey, argReceived);
        shortMap.put(argOption.shortKey, argReceived);


        if (isLongKey && isKeyValuePair) {
            argReceived.value = splitInput[1];
            // We receive both the key and the value at the same time. This is a trick to put it in the lastKeyPair
            // field by the end of the function.
            currentKeyPair = argReceived;
        }

        lastKeyPair = currentKeyPair;
        expectingKey = isKeyValuePair;
    }

    private void longKeyErrors(String[] splitInput, ArgOption argOption, boolean isKeyValuePair) {
        if (isKeyValuePair && splitInput.length < 2) {
            String message = "A key for a key-value pair was used without the value. Format: ... --key=value ... \n"
                    + "Current parse: " + Arrays.toString(splitInput) + "\n"
                    + "Key-value pair options: " + argOption;
            throw new ParseArgumentException(message);
        } else if (!isKeyValuePair && splitInput.length >= 2 && !splitInput[1].isEmpty()) {
            String message = "A key was used as a pair with a value. Format: ... --key ... \n"
                    + "Current parse: " + Arrays.toString(splitInput) + "\n"
                    + "Key options: " + argOption;
            throw new ParseArgumentException(message);
        }
    }

    private void shortKeyErrors(String[] splitInput, ArgOption argOption, boolean isKeyValuePair) {
        if (splitInput.length >= 2) {
            String message;
            if (isKeyValuePair) {
                message = "A key was used as a pair with a value. Format: ... -key value ..."
                        + "Current parse: " + Arrays.toString(splitInput) + "\n"
                        + "Key-value pair options: " + argOption;
            } else {
                message = "A key was used as a pair with a value. Format: ... -key ..."
                        + "Current parse: " + Arrays.toString(splitInput) + "\n"
                        + "Key options: " + argOption;
            }
            throw new ParseArgumentException(message);
        }
    }

    private void assignValue(String value) {
        // Note(Max): Once the first argument assigned to `listArg` is passed the all subsequent values should be a part
        // of the listArg value.
        if (listArg != null && listArg == currentKeyPair) {
            listArg.value += " " + value;
            return;
        }

        currentKeyPair.value = value;
        lastKeyPair = currentKeyPair;
        currentKeyPair = null;
        expectingKey = true;
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
     * &emsp     1) -k <br>
     * &emsp     2) --Key-Word <br>
     * &emsp     3) -k arg <br>
     * &emsp     4) --Key-Word=arg <br>
     * &emsp     5) ... argList     &emsp// There can only be one of these list args, and they are always at the end of the command. <br><br>
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

        private char shortKey = '\0';

        private String longKey = "";

        private E_Usage usage = null;

        private String shortValueExample = "{value}";

        private String longKeyValueExample = "{value}";

        private String listExample = "{value} {value} {value}";

        private String description = "";

        private boolean useOnItsOwn = false;

        @SuppressWarnings({"unused", "FieldMayBeFinal"})
        private E_Types typeSig = null;

        @SuppressWarnings({"unused", "FieldMayBeFinal"})
        private boolean repeated = false;




        public char getShortKey() {
            return shortKey;
        }

        public ArgOption setShortKey(char shortKey) {
            this.shortKey = shortKey;
            return this;
        }

        public String getLongKey() {
            return longKey;
        }

        public ArgOption setLongKey(String longKey) {
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

        public ArgOption setUseOnItsOwn(boolean useOnItsOwn) {
            this.useOnItsOwn = useOnItsOwn;
            return this;
        }

        @Override
        public String toString() {
            return "ArgOption{" +
                    "shortKey=" + shortKey +
                    ", longKey='" + longKey + '\'' +
                    ", usage=" + usage +
                    ", shortValueExample='" + shortValueExample + '\'' +
                    ", longKeyValueExample='" + longKeyValueExample + '\'' +
                    ", listExample='" + listExample + '\'' +
                    ", description='" + description + '\'' +
                    '}';
        }
    }

    public static class ArgReceived {

        private final ArgOption option;

        private String value = "";



        public ArgReceived(ArgOption option) {
            this.option = option;
        }



        public ArgOption getOption() {
            return option;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return "ArgReceived{" +
                    "Option=" + option +
                    ", Value='" + value + '\'' +
                    '}';
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

                int rightSpace = (LINE_WIDTH - line.length()) / 2;
                int leftSpace = LINE_WIDTH - rightSpace;

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
            for (ArgOption option : argOptions) {
                buildOptionBlock(option);
            }
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
                return dupeString(" ", LEFT_MARGIN_WIDTH + "-x, ".length());
            }

            return dupeString(" ", LEFT_MARGIN_WIDTH) + "-" + shortKey + ", ";
        }

        private String buildLongKeyString(String longKey, String keyLine) {
            int spaceForLongKey = (BASE_NAME_COL_WIDTH + EXTRA_NAME_SPACE) - (keyLine.length());

            if ("--".length() + longKey.length() + KEY_DESCRIPTION_GAP > spaceForLongKey) {
                throw new ArgumentOptionException("Long key to long. Long key: " + longKey);
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
            int indentWidth = keyLine.length();

            if (indentWidth < BASE_NAME_COL_WIDTH) {
                indentWidth = BASE_NAME_COL_WIDTH;
            }

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
                case LIST: usage = LIST_USAGE; break;
            }

            return lineWrapString("Usage: " + usage, infoWidth);
        }

        private ArrayList<String> buildExamples(ArgOption option) {
            if (option.usage == E_Usage.LIST) {
                return buildListExample(option);
            }

            return buildValueExamples(option);
        }

        private ArrayList<String> buildListExample(ArgOption option) {
            String ellipses = (option.useOnItsOwn) ? " " : " ... ";
            String exampleBuilder = EXAMPLE_PREFIX + programmeDetails.commandName + ellipses + option.listExample;
            return lineWrapString(exampleBuilder, infoWidth);
        }

        private ArrayList<String> buildValueExamples(ArgOption option) {
            String commandName = programmeDetails.commandName;
            ArrayList<String> exampleLines = new ArrayList<>();

            String ellipses = (option.useOnItsOwn) ? " " : " ... ";

            if (option.shortKey != '\0') {
                String shortExample = EXAMPLE_PREFIX + commandName + ellipses + "-" + option.shortKey + " "
                        + option.shortKey + ellipses;

                exampleLines.addAll(lineWrapString(shortExample, infoWidth));
            }

            if (!option.longKey.isEmpty()) {
                String longExample = EXAMPLE_PREFIX + commandName + ellipses + "--" + option.longKey + "="
                        + option.longKey + ellipses;

                exampleLines.addAll(lineWrapString(longExample, infoWidth));
            }

            return exampleLines;
        }

        private void mergeAndIndent(String keyLine, ArrayList<String> infoLines) {
            if (infoLines.size() >= 1) {
                // The key text is already packed.
                infoLines.set(0, keyLine + infoLines.get(0));
            }

            for (int i = 1; i < infoLines.size(); i++) {
                String newLine = dupeString(" ", keyLine.length()) + infoLines.get(i);
                infoLines.set(0, newLine);
            }
        }



        private ArrayList<String> lineWrapString(String input, int lineWidth) {
            ArrayList<String> wrappedLines = new ArrayList<>();

            String[] inputWords = input.split(" ");
            StringBuilder lineBuilder = new StringBuilder(lineWidth);
            int lineLength = 0;

            for (String word : inputWords) {
                if (lineLength + word.length() < lineWidth) {
                    lineBuilder.append(" ").append(word);
                    lineLength += 1 + word.length();
                    continue;
                }

                wrappedLines.add(lineBuilder.toString());
                lineLength = word.length();
                lineBuilder = new StringBuilder(lineWidth);
            }

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
