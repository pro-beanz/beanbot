package pro.beanz.discord.beanbot.commands.lib;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.requests.GatewayIntent;

public abstract class Command {
    protected final String PERMISSION_ERROR = "TARGET has insufficient permissions.";
    protected final String PRIVATE_MESSAGE_ERROR = "You may only run this command in a server.";

    private CommandData getCommandData() {
        return getClass().getAnnotation(CommandData.class);
    }

    public String getName() {
        return getCommandData().name();
    }

    public String getDescription() {
        return getCommandData().description();
    }

    public String getUsage() {
        return getCommandData().usage();
    }

    public String[] getTriggers() {
        return getCommandData().triggers();
    }

    public Permission[] getCallerPermissions() {
        return getCommandData().callerPermissions();
    }

    public Permission[] getBotPermissions() {
        return getCommandData().botPermissions();
    }

    public GatewayIntent[] getGatewayIntents() {
        return getCommandData().gatewayIntents();
    }

    public int getMinArgs() {
        return getCommandData().minimumArgs();
    }

    public boolean isServerOnly() {
        return getCommandData().serverOnly();
    }

    public String[] getPrerequisites() { return getCommandData().prerequisites(); }

    public boolean userPermissionCheck(MessageReceivedEvent event) throws NullPointerException {
        Member user = event.getMember();
        assert user != null;
        return user.hasPermission(getCallerPermissions()) || user.hasPermission(Permission.ADMINISTRATOR);
    }

    public boolean botPermissionCheck(MessageReceivedEvent event) {
        Member bot = event.getGuild().getSelfMember();
        return bot.hasPermission(getBotPermissions()) || bot.hasPermission(Permission.ADMINISTRATOR);
    }

    public abstract void execute(MessageReceivedEvent event, String[] args);
}