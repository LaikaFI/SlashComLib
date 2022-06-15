package link.alpinia.SlashComLib;

import com.google.common.reflect.ClassPath;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.requests.restaction.CommandCreateAction;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * CommandRegistrar Class
 * Used for easy command package loading, we use this to avoid having 20 lines of list.add() methods.
 * @implNote - Just create a new instance of this in your main JDA class, and use it for loading commands as needed.
 * @author Laika
 */
public class CommandRegistrar {

    /**
     * Locates all classes that contain the package name provided. Use CAREFULLY.
     * @param packageName - the name of the package to look for
     * @return - A set of classes that contain that package name.
     */
    public Set<Class> findAllClassesContaining(String packageName) {
        try {
            return ClassPath.from(ClassLoader.getSystemClassLoader())
                    .getAllClasses()
                    .stream()
                    .filter(clazz -> clazz.getPackageName()
                            .contains(packageName))
                    .map(clazz -> clazz.load())
                    .collect(Collectors.toSet());
        } catch (Exception ex) {
            System.out.println("[SlashComLib] Failed to load classes containing " + packageName + ", check stack.");
            ex.printStackTrace();
            return null;
        }
    }

    /**
     * Utilizes findAllClassesContaining() to find all command classes and return them in a simple manner.
     * @implNote - Place global commands under another classpath that isn't searched by this, and register them individually with the registerGlobalCommands function.
     * @param classPath - The classpath location where all of your commands are stored, this makes loading them much quicker (i.e. com.example.bot.commands)
     * @return - The CommandClass's located.
     */
    public List<CommandClass> getCommandClasses(String classPath) {
        try {
            Set<Class> classes = findAllClassesContaining(classPath);
            List<CommandClass> commands = new ArrayList<>();
            System.out.println("[CommandRegistrar]Found " + classes.size() + " classes containing " + classPath +   " in package class.");
            for (Class clazz : classes) {
                for (Constructor cnstr : clazz.getConstructors()) {
                    try {
                        var obj = cnstr.newInstance(); //making an attempt.
                        if (obj instanceof CommandClass) {
                            System.out.println("[CommandRegistrar]Instance found (" + cnstr.getName() + ")! Registering.");
                            commands.add((CommandClass) obj);
                        }
                    } catch (InstantiationException ex) {
                        //Ignore, this is just us trying to load the CommandClass abstract class or a non-conforming class. We ignore it.
                    }
                }
            }
            System.out.println("[CommandRegistrar]CommandClasses loaded [" + commands.size() + "]");
            return commands;
        } catch (IllegalAccessException | InvocationTargetException exception) {
            //Now we don't ignore, this is a core issue.
            exception.printStackTrace();
            System.out.println("[CommandRegistrar] Failed to load classes under " + classPath);
            return null;
        }
    }

    /**
     * Registers all bot commands in all Discords.
     * @param jda - your JDA instance. (needed to load guilds and such).
     * @param activeCommands - the commands to register.
     */
    public void registerCommands(JDA jda, List<CommandClass> activeCommands) {
        //Registers our slash commands
        for(Guild guild : jda.getGuilds()) {
            registerForGuild(guild, activeCommands);
        }

        //Registers the event listeners for those commands.
        for(CommandClass cmd : activeCommands) {
            jda.addEventListener(cmd);
        }
    }

    /**
     * Globally registers your command
     * @implNote Please note that new global commands may take a few hours to show up!
     * @param jda - your JDA instance, used for actually registering globally
     * @param commandClass - the command to be registered.
     */
    public void registerGlobalCommands(JDA jda, CommandClass commandClass) {
        if(commandClass.getCommandInfo().size() < 1 || commandClass.getCommandInfo() == null) { return; }
        for(var ci : commandClass.getCommandInfo()) {
            if(ci instanceof SlashCommandInfo) {
                var sci = (SlashCommandInfo) ci;
                CommandCreateAction cca = jda.upsertCommand(sci.getName(), sci.getDescription());
                if (sci.hasSubCommands()) {
                    for (String name : sci.getSubCommands().keySet()) {
                        SlashCommandInfo si = sci.getSubCommands().get(name);
                        SubcommandData sd = new SubcommandData(si.getName(), si.getDescription());
                        for (String option : si.getOptions().keySet()) {
                            sd.addOptions(si.getOptions().get(option));
                        }
                        cca.addSubcommands(sd);
                    }
                }
                if (sci.hasOptions()) {
                    for (String name : sci.getOptions().keySet()) {
                        //Any intelligent IDE will rage about the option not being used, it's added to the action then executed later, DO not edit this.
                        cca.addOptions(sci.getOptions().get(name));
                    }
                }
                System.out.println("[SlashComLib] Finished preparing GlobalCommand " + cca.getName());
                try {
                    cca.queue();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    System.out.println("[SlashComLib] Failed to register global command.");
                }
            }
            if(ci instanceof ContextCommandInfo) {
                jda.upsertCommand(((ContextCommandInfo) ci).build()).queue();
            }
        }
    }

    /**
     * Registers all bot commands with the guild provided
     * @param guild - guild to have commands provided to
     * @param activeCommands - The commands to load for said guild.
     */
    public void registerForGuild(Guild guild, List<CommandClass> activeCommands) {
        System.out.println("[SlashComLib] Registering commands for Guild[" + guild.getId() + "]");
        int i = 0; //Slash Cmds
        int c = 0; //Context Cmds
        for (CommandClass cmd : activeCommands) {
            if(cmd.getCommandInfo().size() < 1 || cmd.getCommandInfo() == null) { continue; }
            for (CommandInfo ci : cmd.getCommandInfo()) {
                if (ci instanceof SlashCommandInfo) {
                    var sci = (SlashCommandInfo) ci;
                    CommandCreateAction cca = guild.upsertCommand(sci.getName(), sci.getDescription());
                    i++;
                    if (sci.hasSubCommands()) {
                        for (String name : sci.getSubCommands().keySet()) {
                            SlashCommandInfo si = sci.getSubCommands().get(name);
                            SubcommandData sd = new SubcommandData(si.getName(), si.getDescription());
                            for (String option : si.getOptions().keySet()) {
                                sd.addOptions(si.getOptions().get(option));
                            }
                            cca.addSubcommands(sd);
                        }
                    }
                    if (sci.hasOptions()) {
                        for (String name : sci.getOptions().keySet()) {
                            //Any intelligent IDE will rage about the option not being used, it's added to the action then executed later, DO not edit this.
                            cca.addOptions(sci.getOptions().get(name));
                        }
                    }
                    //Push w/ modifications.
                    //commented for spam sake info("Command: " + ci.getName() + " registration on " + guild.getId() + " completed.");
                    try {
                        cca.queue();
                    } catch (Exception ex) {
                        //Only time this *should* occur is in the event of a server not having the proper scope.
                        System.out.println("[SlashComLib] Failed to queue command for RestAction, not printing stack to avoid console spam. (Improper Scope?)");
                    }
                }
                if(ci instanceof ContextCommandInfo) {
                    guild.upsertCommand(((ContextCommandInfo) ci).build()).queue();
                    c++;
                }
            }
            System.out.println("[SlashComLib] Registered " + i + " slash commands. On Guild[" + guild.getId() + "]");
            System.out.println("[SlashComLib] Registered " + c + " context commands. On Guild[" + guild.getId() + "]");
        }
    }
 }

