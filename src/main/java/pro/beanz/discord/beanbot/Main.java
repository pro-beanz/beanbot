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
import pro.beanz.discord.beanbot.commands.lib.Command;
import pro.beanz.discord.beanbot.reactionroles.ReactionRoleListener;

import javax.security.auth.login.LoginException;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

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
        InputStream inputStream = Main.class.getClassLoader().getResourceAsStream(logConfig);
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
            dataDir.mkdir();
            new File("./data/logs").mkdir();

            FileWriter writer = new FileWriter(tokenFile);
            writer.write(token);
            writer.close();
        }

        try {
            // set up listeners
            commandListener = new CommandListener();
            ListenerAdapter[] listeners = {
                    commandListener,
                    new ReactionRoleListener()
            };

            // set up bot
            JDABuilder builder = JDABuilder.createLight(token)
                    .setStatus(OnlineStatus.DO_NOT_DISTURB)
                    .setActivity(Activity.listening("void noises"))
                    .setEnabledIntents(getRequiredIntents());

            // add listeners
            for (ListenerAdapter listener : listeners) {
                builder.addEventListeners(listener);
            }

            // build bot
            JDA jda = builder.build().awaitReady();

            // generate invite link with required permissions
            log.info(jda.getInviteUrl(getRequiredPermissions()));
        } catch (LoginException e) {
            log.error("Login failure");
            e.printStackTrace();

            System.out.print("Remove token file [Y/n]? ");
            Scanner input = new Scanner(System.in);
            if (input.next().toLowerCase().charAt(0) == 'y') {
                tokenFile.delete();
            }
            input.close();
        }
    }

    private static List<Permission> getRequiredPermissions() {
        List<Permission> list = new ArrayList<>();
        for (Command command : commandListener.getCommands()) {
            for (Permission permission : command.getBotPermissions()) {
                if (!list.contains(permission)) list.add(permission);
            }
        }

        // log required permissions
        log.debug(list.toString());

        return list;
    }

    private static List<GatewayIntent> getRequiredIntents() {
        List<GatewayIntent> list = new ArrayList<>();
        for (Command command : commandListener.getCommands()) {
            for (GatewayIntent gatewayIntent : command.getGatewayIntents()) {
                if (!list.contains(gatewayIntent)) list.add(gatewayIntent);
            }
        }

        // log required gateway intents
        log.debug(list.toString());

        return list;
    }
}
