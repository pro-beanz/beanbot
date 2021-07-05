package pro.beanz.discord.beanbot.commands.lib;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.requests.GatewayIntent;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface CommandData {
    String name() default "";

    String description() default "";

    String usage() default "";

    String[] subcommands() default {};

    String[] triggers() default {};

    Permission[] callerPermissions() default {};

    Permission[] botPermissions() default {};

    GatewayIntent[] gatewayIntents() default {};

    int minimumArgs() default 0;

    boolean serverOnly() default false;

    String[] prerequisites() default {};
}
