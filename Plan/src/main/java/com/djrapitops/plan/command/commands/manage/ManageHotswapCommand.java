package main.java.com.djrapitops.plan.command.commands.manage;

import com.djrapitops.plugin.api.config.Config;
import com.djrapitops.plugin.api.utility.log.Log;
import com.djrapitops.plugin.command.CommandType;
import com.djrapitops.plugin.command.ISender;
import com.djrapitops.plugin.command.SubCommand;
import com.djrapitops.plugin.utilities.Verify;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.database.Database;
import main.java.com.djrapitops.plan.settings.Permissions;
import main.java.com.djrapitops.plan.settings.Settings;
import main.java.com.djrapitops.plan.settings.locale.Locale;
import main.java.com.djrapitops.plan.settings.locale.Msg;
import main.java.com.djrapitops.plan.utilities.Condition;
import main.java.com.djrapitops.plan.utilities.ManageUtils;

import java.io.IOException;

/**
 * This manage subcommand is used to swap to a different database and reload the
 * plugin if the connection to the new database can be established.
 *
 * @author Rsl1122
 * @since 2.3.0
 */
public class ManageHotswapCommand extends SubCommand {

    private final Plan plugin;

    /**
     * Class Constructor.
     *
     * @param plugin Current instance of Plan
     */
    public ManageHotswapCommand(Plan plugin) {
        super("hotswap",
                CommandType.PLAYER_OR_ARGS,
                Permissions.MANAGE.getPermission(),
                Locale.get(Msg.CMD_USG_MANAGE_HOTSWAP).toString(),
                "<DB>");

        this.plugin = plugin;

    }

    @Override
    public String[] addHelp() {
        return Locale.get(Msg.CMD_HELP_MANAGE_HOTSWAP).toArray();
    }

    @Override
    public boolean onCommand(ISender sender, String commandLabel, String[] args) {
        if (!Condition.isTrue(args.length >= 1, Locale.get(Msg.CMD_FAIL_REQ_ONE_ARG).toString(), sender)) {
            return true;
        }
        String dbName = args[0].toLowerCase();
        boolean isCorrectDB = "sqlite".equals(dbName) || "mysql".equals(dbName);

        if (!Condition.isTrue(isCorrectDB, Locale.get(Msg.MANAGE_FAIL_INCORRECT_DB) + dbName, sender)) {
            return true;
        }

        if (Condition.isTrue(dbName.equals(plugin.getDB().getConfigName()), Locale.get(Msg.MANAGE_FAIL_SAME_DB).toString(), sender)) {
            return true;
        }

        try {
            final Database database = ManageUtils.getDB(plugin, dbName);

            // If DB is null return
            if (!Condition.isTrue(Verify.notNull(database), Locale.get(Msg.MANAGE_FAIL_FAULTY_DB).toString(), sender)) {
                Log.error(dbName + " was null!");
                return true;
            }

            assert database != null;


            database.getVersion(); //Test db connection
        } catch (Exception e) {
            Log.toLog(this.getClass().getName(), e);
            sender.sendMessage(Locale.get(Msg.MANAGE_FAIL_FAULTY_DB).toString());
            return true;
        }

        Config config = plugin.getMainConfig();
        config.set(Settings.DB_TYPE.getPath(), dbName);
        try {
            config.save();
            plugin.reloadPlugin(true);
        } catch (IOException e) {
            Log.toLog(this.getClass().getName(), e);
        }
        return true;
    }
}
