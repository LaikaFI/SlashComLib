package link.alpinia.SlashComLib;

import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.HashMap;

/**
 * Helpful SlashCommandInfo class to easily make slash commands.
 * @author Laika
 */
public class SlashCommandInfo extends CommandInfo {
    private String name;
    private String description;
    private SlashCommandType type;
    private HashMap<String, OptionData> options;
    private HashMap<String, SlashCommandInfo> subCommands;

    /**
     * Constructor to build CommandInfo with.
     * @param name - Name of the slash command (MUST BE ALL LOWERCASE)
     * @param description - Description of the slash command
     * @param type - The type of command, either COMMAND or SUBCOMMAND.
     */
    public SlashCommandInfo(String name, String description, SlashCommandType type) {
        this.name = name;
        this.description = description;
        this.type = type;
        this.options = new HashMap<>();
        this.subCommands = new HashMap<>();
    }

    /**
     * Returns whether the command has options/input.
     * @return - boolean
     */
    public boolean hasOptions() {
        return options.size() != 0;
    }

    public boolean hasSubCommands() { return subCommands.size() != 0; }

    public String getDescription() {
        return description;
    }

    public String getName() {
        return name;
    }

    public HashMap<String, OptionData> getOptions() {
        return options;
    }

    public HashMap<String, SlashCommandInfo> getSubCommands() { return subCommands; }

    /**
     * The way you add options to a command. Use this function for EACH argument.
     * @param name - name of the field
     * @param description - description for the field
     * @param type - the OptionType of the field (e.x. OptionType.STRING, OptionType.CHANNEL, etc.)
     * @param required - whether the command can be run without the field or not.
     */
    public void addOption(String name, String description, OptionType type, boolean required) {
        if(subCommands.size() > 0) {
            System.out.println("[SlashComLib] Malformed Command! Contains subcommands when trying to implement options. " +
                    "Discord will reject this command if upserted!");
        }
        OptionData od = new OptionData(type, name, description);
        od.setRequired(required);
        options.put(name, od);
    }

    public void addOption(OptionData data) {
        options.put(data.getName(), data);
    }

    /**
     * The way you add subcommands to a command. NOTE you cannot have options for the command if you use this.
     * @param cmdInfo - The CommandInfo for the subcommand (including options).
     */
    public void addSubcommand(SlashCommandInfo cmdInfo) {
        if(options.size() > 0) {
            System.out.println("[SlashComLib] Malformed Command! Contains options when trying to implement a subcommand." +
                    " Discord will reject this command if upserted!");
        }
        subCommands.put(cmdInfo.name, cmdInfo);
    }
}

