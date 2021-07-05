package pro.beanz.discord.beanbot;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.beanz.discord.beanbot.commands.CommandListener;
import pro.beanz.discord.beanbot.commands.Help;
import pro.beanz.discord.beanbot.commands.Ping;
import pro.beanz.discord.beanbot.commands.ReactionRoleSetup;
import pro.beanz.discord.beanbot.commands.lib.Command;
import pro.beanz.discord.beanbot.reactionroles.ReactionRoleListener;

import javax.security.auth.login.LoginException;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

import static java.io.File.pathSeparator;
import static java.lang.System.getenv;
import static java.nio.file.Files.isExecutable;
import static java.util.regex.Pattern.quote;

public class Main {
    private static Logger log;

    private static CommandListener commandListener;

    public static void main(String[] args) throws IOException, InterruptedException, JoranException {
        // logger configuration
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        loggerContext.reset();
        JoranConfigurator configurator = new JoranConfigurator();
        String logConfig = "logback.xml";
        if (args.length > 0) {
            if (args[0].equals("debug")) {
                logConfig = "logback-debug.xml";
            } else if (args[0].equals("trace")) {
                logConfig = "logback-trace.xml";
            }
        }
        InputStream inputStream = Main.class.getClassLoader().getResourceAsStream("logback/" + logConfig);
        configurator.setContext(loggerContext);
        configurator.doConfigure(inputStream);
        assert inputStream != null;
        inputStream.close();
        log = loggerContext.getLogger(Main.class);

        // collect token info
        File tokenFile = new File("./data/token");
        String token;
        try (Scanner s = new Scanner(tokenFile)) {
            token = s.next();
        } catch (NoSuchElementException | FileNotFoundException e) {
            Scanner s = new Scanner(System.in);
            System.out.print("bot token: ");
            token = s.next();
            s.close();

            File dataDir = new File("./data");
            if (dataDir.exists()) {
                if (!dataDir.isDirectory()) {
                    Files.move(dataDir.toPath(), Paths.get(String.format(
                            "%s_%s", dataDir.getName(), new Date().toString().replaceAll(" ", "-"))));
                }
            }
            if (!dataDir.mkdir() || new File("./data/logs").mkdir()) {
                BeanbotHelper.insufficientFilePrivileges();
            }

            FileWriter writer = new FileWriter(tokenFile);
            writer.write(token);
            writer.close();
        }

        try {
            // set up listeners
            Command[] commands = {
                    new Help(),
                    new Ping(),
                    new ReactionRoleSetup()
            };

            commandListener = new CommandListener('/', commands);
            ListenerAdapter[] listeners = {
                    commandListener,
                    new ReactionRoleListener()
            };

            // if-statement for pre-requisites like yt-dl
            if (satisfiesPrerequisites()) {
                // set up bot
                JDABuilder builder = JDABuilder.createLight(token)
                        .setStatus(OnlineStatus.DO_NOT_DISTURB)
                        .setActivity(Activity.listening("void noises"))
                        .setEnabledIntents(getRequiredIntents());

                // add main listeners
                for (ListenerAdapter listener : listeners) {
                    builder.addEventListeners(listener);
                }

                // add subcommand listeners
                for (Command command : commands) {
                    CommandListener listener = command.getCommandListener();
                    if (listener != null) {
                        builder.addEventListeners(listener);
                    }
                }

                // build bot
                JDA jda = builder.build().awaitReady();

                // generate invite link with required permissions
                log.info(jda.getInviteUrl(getRequiredPermissions()));
            } else {
                log.error("Prerequisites are not met. Refer to https://github.com/pro-beanz/beanbot for information.");
            }
        } catch (LoginException e) {
            log.error("Login failure");
            e.printStackTrace();

            System.out.print("Remove token file [Y/n]? ");
            Scanner input = new Scanner(System.in);
            if (input.next().toLowerCase().charAt(0) == 'y') {
                if (!tokenFile.delete()) {
                    BeanbotHelper.insufficientFilePrivileges();
                }
            }
            input.close();
        }
    }

    private static HashSet<Permission> getRequiredPermissions() {
        HashSet<Permission> set = new HashSet<>();
        getRequiredPermissionsHelper(set, commandListener);

        // log required permissions
        log.debug(set.toString());

        return set;
    }

    private static void getRequiredPermissionsHelper(HashSet<Permission> set, CommandListener target) {
        for (Command command : target.getCommands()) {
            set.addAll(Arrays.asList(command.getBotPermissions()));
            if (command.getCommandListener() != null) {
                getRequiredPermissionsHelper(set, command.getCommandListener());
            }
        }
    }

    private static HashSet<GatewayIntent> getRequiredIntents() {
        HashSet<GatewayIntent> set = new HashSet<>();
        getRequiredIntentsHelper(set, commandListener);

        // log required gateway intents
        log.debug(set.toString());

        return set;
    }

    private static void getRequiredIntentsHelper(HashSet<GatewayIntent> set, CommandListener target) {
        for (Command command : target.getCommands()) {
            set.addAll(Arrays.asList(command.getGatewayIntents()));
            if (command.getCommandListener() != null) {
                getRequiredIntentsHelper(set, command.getCommandListener());
            }
        }
    }

    private static boolean satisfiesPrerequisites() {
        HashSet<String> set = new HashSet<>();
        satisfiesPrerequisitesHelper(set, commandListener);

        // log prerequisites
        log.debug(set.toString());

        boolean satisfied = true;
        final String[] paths = getenv("PATH").split(quote(pathSeparator));
        for (String prerequisite : set) {
            satisfied = satisfied && Stream.of(paths).map(Paths::get).anyMatch(path ->
                    isExecutable(Path.of(path.resolve(prerequisite).toString()))
            );

            if (!satisfied) {
                log.error(String.format("Missing prerequisite: %s", prerequisite));
            }
        }

        return satisfied;
    }

    private static void satisfiesPrerequisitesHelper(HashSet<String> set, CommandListener target) {
        for (Command command : target.getCommands()) {
            set.addAll(Arrays.asList(command.getPrerequisites()));
            if (command.getCommandListener() != null) {
                satisfiesPrerequisitesHelper(set, command.getCommandListener());
            }
        }
    }
}
