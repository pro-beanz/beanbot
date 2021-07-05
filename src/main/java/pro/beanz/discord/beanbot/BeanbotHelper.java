package pro.beanz.discord.beanbot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BeanbotHelper {
    private static final Logger log = LoggerFactory.getLogger(BeanbotHelper.class);

    public static void insufficientFilePrivileges() {
        log.error("Insufficient file privileges.");
        System.exit(1);
    }
}
