package link.alpinia.SlashComLib;

import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

public class ContextCommandInfo extends CommandInfo {
    private Command.Type type;
    private String name;

    /**
     * Context Commands that can be applied to messages or users, helpful for moderation and other things.
     * @param name Name of the command. (No restrictions on content, other than basic ASCII less than 32 char).
     * @param type Type of context it will be available.
     */
    public ContextCommandInfo(String name, Command.Type type) {
        this.type = type;
        this.name = name;
    }

    public CommandData build() {
        if(type.equals(Command.Type.SLASH)) {
            System.out.println("[SlashComLib] INVALID TYPE 'SLASH' ON CONTEXT COMMAND " + name + ". This command will crash JDA!");
        }
        return Commands.context(type, name);
    }
}
