package net.dragoncoding.groupbot.discord.command

import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationContext
import org.springframework.data.util.AnnotatedTypeScanner

class JDACommandManager(
    private val packageToScan: String,
    private val commandAnnotation: Class<out Annotation>
) {
    private val logger: Logger = LoggerFactory.getLogger(this::class.java)

    private var loggingEnabled: Boolean = false
    private val excludeAnnotations: HashSet<Class<out Annotation>> = HashSet()

    private var commands: HashSet<IDiscordSlashCommand> = HashSet()

    fun enableLogging() = apply { this.loggingEnabled = true }
    fun disableLogging() = apply { this.loggingEnabled = false }

    /**
     * Any annotation added to this list will exclude any command annotated with it
     */
    fun excludeAnnotations(vararg annotationClasses: Class<out Annotation>) =
        apply { this.excludeAnnotations.addAll(annotationClasses) }

    fun clearExcludeAnnotations() = apply { this.excludeAnnotations.clear() }
    fun getExcludeAnnotations() = setOf(*this.excludeAnnotations.toArray())

    fun getCommands() = setOf(*this.commands.toTypedArray())

    fun load(context: ApplicationContext): JDACommandManager {
        this.commands.clear()

        AnnotatedTypeScanner(
            false,
            commandAnnotation
        ).findTypes(packageToScan)
            .filter { command -> excludeAnnotations.none { command.isAnnotationPresent(it) } }
            .filter { IDiscordSlashCommand::class.java.isAssignableFrom(it) }
            .filterIsInstance<Class<IDiscordSlashCommand>>()
            .forEach {
                this.commands.add(context.getBean(it))
            }

        return this
    }

    fun buildJDACommands(): List<SlashCommandData> {
        val commands = HashMap<String, SlashCommandData>()

        for (commandToAdd in this.commands) {
            if (commandToAdd.isSimpleCommand() && !commands.contains(commandToAdd.name)) {
                commands[commandToAdd.name] = Commands.slash(commandToAdd.name, commandToAdd.description)
                    .addOptions(*commandToAdd.getCommandOptions().toTypedArray())
                continue
            }

            var commandRoot = commands[commandToAdd.parentCommandName]
            if (commandRoot == null) {
                commandRoot = Commands.slash(commandToAdd.parentCommandName!!, "Something")
                commands[commandToAdd.parentCommandName!!] = commandRoot
            }

            var subCommandGroup: SubcommandGroupData? = null
            if (commandToAdd.hasSubCommandGroup()) {
                subCommandGroup = commandRoot.subcommandGroups
                    .firstOrNull { it.name == commandToAdd.subCommandGroupName }

                if (subCommandGroup == null) {
                    subCommandGroup = SubcommandGroupData(commandToAdd.subCommandGroupName!!, "Something")
                    commandRoot.addSubcommandGroups(subCommandGroup)
                }
            }
            var subCommand: SubcommandData? =
                if (subCommandGroup != null) {
                    subCommandGroup.subcommands
                        .firstOrNull { it.name == commandToAdd.name }
                } else {
                    commandRoot.subcommands
                        .firstOrNull { it.name == commandToAdd.name }
                }

            if (subCommand == null) {
                subCommand = SubcommandData(commandToAdd.name, commandToAdd.description)
                    .addOptions(*commandToAdd.getCommandOptions().toTypedArray())
                if (subCommandGroup != null) {
                    subCommandGroup.addSubcommands(subCommand)
                } else {
                    commandRoot.addSubcommands(subCommand)
                }
            }
        }

        return commands.values.toList()
    }

    fun clear() {
        this.commands.clear()
    }

}