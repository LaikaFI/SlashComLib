package link.alpinia.SlashComLib;

import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.GenericContextInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.List;

/**
 * CommandClass Abstract Class
 * Use this for any commands you make.
 */
public abstract class CommandClass extends ListenerAdapter {

    public static final String USER = "USER_INTERACTION";
    public static final String MESSAGE = "MESSAGE_INTERACTION";

    //Whats the name of the Command Package?
    public abstract String getName();

    /**
     * Actual command handling method.
     * @param name - name of the command executed
     * @param e - SlashCommandInteractionEvent, used for all references.
     */
    public abstract void slashCommand(String name, SlashCommandInteractionEvent e);

    public abstract void modalResponse(String name, ModalInteractionEvent e);

    public abstract void contextResponse(String name, GenericContextInteractionEvent event, String type);

    /**
     * Overriding our SlashCommandInteractionEvent that way it can execute a cmd.
     * @param event - The event to handle
     */
    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        slashCommand(event.getName(), event);
    }

    @Override
    public void onModalInteraction(ModalInteractionEvent event) { modalResponse(event.getModalId(), event); }

    @Override
    public void onMessageContextInteraction(MessageContextInteractionEvent event) {
        contextResponse(event.getName(), event, MESSAGE);
    }

    @Override
    public void onUserContextInteraction(UserContextInteractionEvent event) {
        contextResponse(event.getName(), event, USER);
    }

    /**
     * Also for the sake of organization. To upsert slash commands.
     * Follow as name, description. (for upsertCommand(name, description);
     * @return - The name and description for the commands contained within the class.
     */
    public abstract List<CommandInfo> getCommandInfo();
}
