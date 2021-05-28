package pro.beanz.beanbot;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

import javax.security.auth.login.LoginException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import pro.beanz.beanbot.commands.CommandListener;
import pro.beanz.beanbot.commands.lib.Command;
import pro.beanz.beanbot.reactionroles.ReactionRoleListener;

public class Main {
    private static Logger log;

    private static CommandListener commandListener;

    public static void main(String[] args) throws IOException, InterruptedException {
        // logger setup
        System.setProperty(org.slf4j.impl.SimpleLogger.SHOW_LOG_NAME_KEY, "false");
        System.setProperty(org.slf4j.impl.SimpleLogger.SHOW_THREAD_NAME_KEY, "false");
        log = LoggerFactory.getLogger(Main.class);

        // collect token info
        File tokenFile = new File("./token");
        String token;
        try (Scanner s = new Scanner(tokenFile)) {
            token = s.next();
        } catch (NoSuchElementException | FileNotFoundException e) {
            Scanner s = new Scanner(System.in);
            System.out.print("bot token: ");
            token = s.next();
            s.close();

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

            // add all listeners, then setup and build the bot
            JDABuilder builder = JDABuilder.createLight(token)
                    .setStatus(OnlineStatus.DO_NOT_DISTURB)
                    .setActivity(Activity.listening("void noises"))
                    .setEnabledIntents(getRequiredIntents());
            for (ListenerAdapter listener : listeners) {
                builder.addEventListeners(listener);
            }
            JDA jda = builder.build().awaitReady();

            // generate invite link with required permissions
            log.info(jda.getInviteUrl(getRequiredPermissions()));

            Scanner input = new Scanner(System.in);
            boolean running = true;
            while (running) {
                if (input.next().toLowerCase().equals("stop")) {
                    running = false;
                    log.info("Shutting down...");
                    jda.shutdownNow();
                }
            }
            input.close();
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
        List<Permission> list = new ArrayList<Permission>();
        for (Command command : commandListener.getCommands()) {
            for (Permission permission : command.getBotPermissions()) {
                if (!list.contains(permission)) list.add(permission);
            }
        }

        // log required permissions
        log.info(list.toString());

        return list;
    }

    private static List<GatewayIntent> getRequiredIntents() {
        List<GatewayIntent> list = new ArrayList<GatewayIntent>();
        for (Command command : commandListener.getCommands()) {
            for (GatewayIntent gatewayIntent : command.getGatewayIntents()) {
                if (!list.contains(gatewayIntent)) list.add(gatewayIntent);
            }
        }

        // log required gateway intents
        log.info(list.toString());

        return list;
    }
}
