package pro.beanz.commands.lib;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.requests.GatewayIntent;

@Retention(RetentionPolicy.RUNTIME)
public @interface CommandData {
    String name() default "";

    String description() default "";

    String usage() default "";

    String[] triggers() default {};

    Permission[] callerPermissions() default {};

    Permission[] botPermissions() default {};

    GatewayIntent[] gatewayIntents() default {};

    int minimumArgs() default 0;

    boolean serverOnly() default false;
}
