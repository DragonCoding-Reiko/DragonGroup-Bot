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
    private var logAddedCommands: Boolean = false
    private var logExcludeWarnings: Boolean = false

    private val excludeAnnotations: HashSet<Class<out Annotation>> = HashSet()

    private var commands: HashSet<IDiscordSlashCommand> = HashSet()

    fun enableLogging() = apply { this.loggingEnabled = true }
    fun disableLogging() = apply { this.loggingEnabled = false }

    fun showCommandLogging() = apply { this.logAddedCommands = true }
    fun disableCommandLogging() = apply { this.logAddedCommands = false }

    fun showExcludeWarning() = apply { this.logExcludeWarnings = true }
    fun suppressExclusionWarning() = apply { this.logExcludeWarnings = false }

    /**
     * Any annotation added to this list will exclude any command annotated with it
     */
    fun excludeAnnotations(vararg annotationClasses: Class<out Annotation>) =
        apply { this.excludeAnnotations.addAll(annotationClasses) }

    fun clearExcludeAnnotations() = apply { this.excludeAnnotations.clear() }
    fun getExcludeAnnotations() = setOf(*this.excludeAnnotations.toArray())

    fun getCommands() = setOf(*this.commands.toTypedArray())

    fun load(context: ApplicationContext): JDACommandManager {
        debug(
            "Loading discord commands in package '{}' with annotation '{}' and none of {}",
            packageToScan, commandAnnotation.name, excludeAnnotations.map { it.name }.toSet()
        )
        this.commands.clear()

        val scanner = AnnotatedTypeScanner(
            false,
            commandAnnotation
        )

        var foundClasses = scanner.findTypes(packageToScan)
        val totalSize = foundClasses.size
        debug("Found {} class[es] with command annotation in the selected package", totalSize)

        foundClasses =
            foundClasses.filter { command -> excludeAnnotations.none { command.isAnnotationPresent(it) } }.toSet()
        val afterExclusionSize = foundClasses.size
        if (totalSize - afterExclusionSize > 0 && logExcludeWarnings)
            warn("Excluded {} class[es] because of exclude annotations", totalSize - afterExclusionSize)

        foundClasses = foundClasses.filter { IDiscordSlashCommand::class.java.isAssignableFrom(it) }.toSet()
        if (afterExclusionSize - foundClasses.size > 0 && logExcludeWarnings)
            warn(
                "Excluded {} class[es] because they do not implement '{}'",
                afterExclusionSize - foundClasses.size, IDiscordSlashCommand::class.java.name
            )

        foundClasses.filterIsInstance<Class<IDiscordSlashCommand>>()
            .forEach {
                val command = context.getBean(it)
                if (logAddedCommands)
                    info("Adding Command '{}' (JDA-Name: {})", it.name, command.fullName)
                this.commands.add(command)
            }

        return this
    }

    fun buildJDACommands(): List<SlashCommandData> {
        val commands = HashMap<String, SlashCommandData>()

        for (commandToAdd in this.commands) {
            if (commandToAdd.isSimpleCommand() && !commands.contains(commandToAdd.name)) {
                val command = Commands.slash(commandToAdd.name, commandToAdd.description)
                    .addOptions(*commandToAdd.getCommandOptions().toTypedArray())

                if (commandToAdd.getCommandPermission() != null)
                    command.setDefaultPermissions(commandToAdd.getCommandPermission()!!)

                commands[commandToAdd.name] = command
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

    private fun debug(message: String, vararg args: Any) {
        if (loggingEnabled)
            logger.debug(message, *args)
    }

    private fun info(message: String, vararg args: Any) {
        if (loggingEnabled)
            logger.info(message, *args)
    }

    private fun warn(message: String, vararg args: Any) {
        if (loggingEnabled)
            logger.warn(message, *args)
    }
}