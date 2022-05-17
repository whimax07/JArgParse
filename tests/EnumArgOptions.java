enum EnumArgOptions implements ArgsParser.EnumOptions {

    BACKGROUND(new ArgsParser.ArgOption()
            .setShortKey('b')
            .setLongKey("Set-Background")
            .setUsage(ArgsParser.E_Usage.KEY_VALUE)
            .setDescription("This command sets the background colour of the console using an RGB 0-255 triplet.")
            .setShortValueExample("(0,0,0)")
            .setLongValueExample("(0,0,0)")),

    TEXT(new ArgsParser.ArgOption()
            .setShortKey('t')
            .setLongKey("Set-Text")
            .setUsage(ArgsParser.E_Usage.KEY_VALUE)
            .setDescription("This command sets the text colour if the console using an RGB 0-255 triplet.")),

    RESET(new ArgsParser.ArgOption()
            .setShortKey('r')
            .setLongKey("Use-Defaults")
            .setUsage(ArgsParser.E_Usage.KEY)
            .setDescription("This command tells the console revert to its default colour scheme. This should be used on its own.")),

    CONFIGS(new ArgsParser.ArgOption()
            .setUsage(ArgsParser.E_Usage.LIST)
            .setDescription("This will take the path to json files and read a \"Set Console Colours\" configuration file."));

    private final ArgsParser.ArgOption option;

    EnumArgOptions(ArgsParser.ArgOption option) {
        this.option = option;
    }

    public ArgsParser.ArgOption get() {
        return option;
    }

}
