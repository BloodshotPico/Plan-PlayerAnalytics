/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test.java.main.java.com.djrapitops.plan.database;

import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.data.KillData;
import main.java.com.djrapitops.plan.data.SessionData;
import main.java.com.djrapitops.plan.data.TPS;
import main.java.com.djrapitops.plan.data.UserData;
import main.java.com.djrapitops.plan.data.cache.DBCallableProcessor;
import main.java.com.djrapitops.plan.data.time.GMTimes;
import main.java.com.djrapitops.plan.data.time.WorldTimes;
import main.java.com.djrapitops.plan.database.Database;
import main.java.com.djrapitops.plan.database.databases.MySQLDB;
import main.java.com.djrapitops.plan.database.databases.SQLiteDB;
import main.java.com.djrapitops.plan.database.tables.TPSTable;
import main.java.com.djrapitops.plan.utilities.ManageUtils;
import main.java.com.djrapitops.plan.utilities.MiscUtils;
import main.java.com.djrapitops.plan.utilities.analysis.MathUtils;
import main.java.com.djrapitops.plan.utilities.file.FileUtil;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import test.java.utils.MockUtils;
import test.java.utils.TestInit;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.SQLException;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Rsl1122
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(JavaPlugin.class)
public class DatabaseTest {

    private Plan plan;
    private Database db;
    private Database backup;
    private int rows;

    /**
     *
     */
    public DatabaseTest() {
    }

    /**
     * @throws IOException
     * @throws Exception
     */
    @Before
    public void setUp() throws Exception {
        TestInit t = TestInit.init();
        plan = t.getPlanMock();
        db = new SQLiteDB(plan, "debug" + MiscUtils.getTime()) {
            @Override
            public void startConnectionPingTask() {

            }

            @Override
            public void convertBukkitDataToDB() {

            }
        };
        File f = new File(plan.getDataFolder(), "Errors.txt");
        rows = FileUtil.lines(f).size();
    }

    /**
     * @throws IOException
     * @throws SQLException
     */
    @After
    public void tearDown() throws IOException, SQLException {
        db.close();
        if (backup != null) {
            backup.close();
        }
        File f = new File(plan.getDataFolder(), "Errors.txt");

        List<String> lines = FileUtil.lines(f);
        int rowsAgain = lines.size();
        if (rowsAgain > 0) {
            for (String line : lines) {
                System.out.println(line);
            }
        }
        assertTrue("Errors were caught.", rows == rowsAgain);
    }

    /**
     *
     */
    @Test
    public void testInit() {
        assertTrue("Database failed to init.", db.init());
    }

    /**
     *
     */
    @Test
    public void testSqLiteGetConfigName() {
        assertEquals("sqlite", db.getConfigName());
    }

    /**
     *
     */
    @Test
    public void testSqLiteGetgName() {
        assertEquals("SQLite", db.getName());
    }

    /**
     *
     */
    @Test
    public void testMysqlGetConfigName() {
        assertEquals("mysql", new MySQLDB(plan) {
            @Override
            public void startConnectionPingTask() {

            }

            @Override
            public void convertBukkitDataToDB() {

            }
        }.getConfigName());
    }

    /**
     *
     */
    @Test
    public void testMysqlGetName() {
        assertEquals("MySQL", new MySQLDB(plan) {
            @Override
            public void startConnectionPingTask() {

            }

            @Override
            public void convertBukkitDataToDB() {

            }
        }.getName());
    }

    /**
     * @throws SQLException
     */
    @Test
    public void testRemoveAll() throws SQLException {
        db.init();
        UserData data = MockUtils.mockUser();
        db.saveUserData(data);
        HashMap<String, Integer> c = new HashMap<>();
        c.put("/plan", 1);
        c.put("/tp", 4);
        c.put("/pla", 7);
        c.put("/help", 21);
        c.put("/roiergbnougbierubieugbeigubeigubgierbgeugeg", 3);
        db.saveCommandUse(c);
        assertTrue(db.removeAllData());
        assertTrue("Contains the user", db.getUserDataForUUIDS(Arrays.asList(MockUtils.getPlayerUUID(), MockUtils.getPlayer2UUID())).isEmpty());
        assertTrue("Contains commandUse", db.getCommandUse().isEmpty());
    }

    /**
     * @throws SQLException
     */
    @Test
    public void testSaveCommandUse() throws SQLException {
        db.init();
        HashMap<String, Integer> c = new HashMap<>();
        c.put("/plan", 1);
        c.put("/tp", 4);
        c.put("/pla", 7);
        c.put("/help", 21);
        c.put("/roiergbnougbierubieugbeigubeigubgierbgeugeg", 3);
        db.saveCommandUse(c);
        c.remove("/roiergbnougbierubieugbeigubeigubgierbgeugeg");
        Map<String, Integer> commandUse = db.getCommandUse();
        assertEquals(c, commandUse);
        c.put("/test", 3);
        c.put("/tp", 6);
        c.put("/pla", 4);
        db.saveCommandUse(c);
        c.put("/pla", 7);
        commandUse = db.getCommandUse();
        assertEquals(c, commandUse);
    }

    /**
     * @throws SQLException
     */
    @Test
    public void testSaveUserData() throws SQLException {
        db.init();
        UserData data = MockUtils.mockUser();
        GMTimes gmTimes = data.getGmTimes();
        gmTimes.setAllGMTimes(5L, 10L, 15L, 20L);
        gmTimes.setState("SURVIVAL");
        gmTimes.setLastStateChange(10L);
        WorldTimes worldTimes = data.getWorldTimes();
        worldTimes.setTime("World", 20L);
        worldTimes.setState("World");
        worldTimes.setLastStateChange(10L);
        db.saveUserData(data);
        data.addNickname("TestUpdateForSave");
        db.saveUserData(data);
        DBCallableProcessor process = d -> {
            System.out.println("\nOriginal: " + data);
            System.out.println("Database: " + d);
            assertTrue("Not Equals", data.equals(d));
        };
        db.giveUserDataToProcessors(data.getUuid(), process);
    }

    /**
     * @throws SQLException
     */
    @Test
    public void testNicknameInjection() throws SQLException {
        db.init();
        UserData data = MockUtils.mockUser();
        UserData data2 = MockUtils.mockUser2();
        db.saveUserData(data2);
        data.addNickname("s); DROP TABLE plan_users;--");
        db.saveUserData(data);
        assertTrue("Removed Users table.", db.getUsersTable().getUserId(data2.getUuid().toString()) != -1);
    }

    /**
     * @throws SQLException
     * @throws java.net.UnknownHostException
     */
    @Test
    public void testSaveMultipleUserData() throws SQLException, UnknownHostException {
        db.init();
        UserData data = MockUtils.mockUser();
        data.getGmTimes().setAllGMTimes(5L, 10L, 15L, 20L);
        data.getWorldTimes().setTime("World", 20L);
        data.addIpAddress(InetAddress.getByName("185.64.113.61"));
        data.addSession(new SessionData(1286349L, 2342978L));
        data.addNickname("TestNick");
        data.addPlayerKill(new KillData(MockUtils.getPlayer2UUID(), 2, "DiamondSword", 75843759L));
        System.out.println(data.toString());
        db.saveUserData(data);
        data.getPlayerKills().clear();
        System.out.println(data.toString());
        data.addNickname("TestUpdateForSave");
        UserData data2 = MockUtils.mockUser2();
        data2.getGmTimes().setAllGMTimes(5L, 10L, 15L, 20L);
        data2.addNickname("Alright");
        data.addNickname("TestNick2");
        data2.addIpAddress(InetAddress.getByName("185.64.113.60"));
        data2.addSession(new SessionData(2348743L, 4839673L));
        data2.addPlayerKill(new KillData(MockUtils.getPlayerUUID(), 1, "DiamondSword", 753759L));
        List<UserData> list = new ArrayList<>();
        list.add(data);
        list.add(data2);
        db.saveMultipleUserData(list);
        data.addPlayerKill(new KillData(MockUtils.getPlayer2UUID(), 2, "DiamondSword", 75843759L));
        List<UserData> userDataForUUIDS = db.getUserDataForUUIDS(MockUtils.getUUIDs());

        System.out.println("\nData1:" + data.toString());
        System.out.println("Data2:" + data2.toString() + "\n");
        for (UserData uData : userDataForUUIDS) {
            System.out.println("uData:" + uData.toString());
            assertTrue("Not Equals", (data.equals(uData) || data2.equals(uData)));
        }
    }

    /**
     * @throws SQLException
     */
    @Test
    public void testRemove() throws SQLException {
        db.init();
        UserData data = MockUtils.mockUser();
        db.saveUserData(data);
        assertTrue(db.removeAccount(data.getUuid().toString()));
        assertTrue("Contains the user", !db.wasSeenBefore(data.getUuid()));
    }

    /**
     * @throws SQLException
     */
    @Test
    @Ignore("Backup has to be rewritten")
    public void testBackup() throws SQLException {
        db.init();
        UserData data = MockUtils.mockUser();
        UserData data2 = MockUtils.mockUser2();
        List<UserData> list = new ArrayList<>();
        list.add(data);
        list.add(data2);
        db.saveMultipleUserData(list);
        backup = new SQLiteDB(plan, "debug-backup") {
            @Override
            public void startConnectionPingTask() {

            }

            @Override
            public void convertBukkitDataToDB() {

            }
        };
        backup.init();
        ManageUtils.clearAndCopy(backup, db);
        Set<UUID> savedUUIDs = backup.getSavedUUIDs();
        assertTrue("Didn't contain 1", savedUUIDs.contains(data.getUuid()));
        assertTrue("Didn't contain 2", savedUUIDs.contains(data2.getUuid()));
    }

    /**
     * @throws SQLException
     */
    // Big test because
    @Test
    @Ignore("Backup has to be rewritten")
    public void testRestore() throws SQLException {
        db.init();
        UserData data = MockUtils.mockUser();
        UserData data2 = MockUtils.mockUser2();
        List<UserData> list = new ArrayList<>();
        list.add(data);
        list.add(data2);
        db.saveMultipleUserData(list);
        HashMap<String, Integer> c = new HashMap<>();
        c.put("/plan", 1);
        c.put("/tp", 4);
        c.put("/pla", 7);
        c.put("/help", 21);
        db.saveCommandUse(c);
        backup = new SQLiteDB(plan, "debug-backup") {
            @Override
            public void startConnectionPingTask() {

            }

            @Override
            public void convertBukkitDataToDB() {

            }
        };
        backup.init();
        ManageUtils.clearAndCopy(backup, db);
        ManageUtils.clearAndCopy(db, backup);
        Set<UUID> savedUUIDs = db.getSavedUUIDs();
        assertTrue("Didn't contain 1", savedUUIDs.contains(data.getUuid()));
        assertTrue("Didn't contain 2", savedUUIDs.contains(data2.getUuid()));
        Map<String, Integer> commandUse = db.getCommandUse();
        assertTrue("Doesn't contain /plan", commandUse.containsKey("/plan"));
        assertTrue("Doesn't contain /tp", commandUse.containsKey("/tp"));
        assertTrue("Doesn't contain /pla", commandUse.containsKey("/pla"));
        assertTrue("Doesn't contain /help", commandUse.containsKey("/help"));
    }

    @Test
    public void testTPSSaving() throws SQLException {
        db.init();
        TPSTable tpsTable = db.getTpsTable();
        List<TPS> expected = new ArrayList<>();
        Random r = new Random();

        OperatingSystemMXBean operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean();
        int availableProcessors = ManagementFactory.getOperatingSystemMXBean().getAvailableProcessors();
        final double averageCPUUsage = MathUtils.round(operatingSystemMXBean.getSystemLoadAverage() / availableProcessors * 100.0);

        final long usedMemory = 51231251254L;
        final int entityCount = 6123;
        final int chunksLoaded = 2134;

        expected.add(new TPS(r.nextLong(), r.nextDouble(), r.nextInt(100000000), averageCPUUsage, usedMemory, entityCount, chunksLoaded));
        expected.add(new TPS(r.nextLong(), r.nextDouble(), r.nextInt(100000000), averageCPUUsage, usedMemory, entityCount, chunksLoaded));
        expected.add(new TPS(r.nextLong(), r.nextDouble(), r.nextInt(100000000), averageCPUUsage, usedMemory, entityCount, chunksLoaded));
        expected.add(new TPS(r.nextLong(), r.nextDouble(), r.nextInt(100000000), averageCPUUsage, usedMemory, entityCount, chunksLoaded));

        tpsTable.saveTPSData(expected);
        assertEquals(expected, tpsTable.getTPSData());
    }

    @Test
    @Ignore("Changed clean limit.")
    public void testTPSClean() throws SQLException {
        db.init();
        TPSTable tpsTable = db.getTpsTable();
        List<TPS> expected = new ArrayList<>();
        Random r = new Random();
        long now = System.currentTimeMillis();

        OperatingSystemMXBean operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean();
        int availableProcessors = ManagementFactory.getOperatingSystemMXBean().getAvailableProcessors();
        final double averageCPUUsage = MathUtils.round(operatingSystemMXBean.getSystemLoadAverage() / availableProcessors * 100.0);

        final long usedMemory = 51231251254L;
        final int entityCount = 6123;
        final int chunksLoaded = 2134;

        expected.add(new TPS(now, r.nextDouble(), r.nextInt(100000000), averageCPUUsage, usedMemory, entityCount, chunksLoaded));
        expected.add(new TPS(now - 1000L, r.nextDouble(), r.nextInt(100000000), averageCPUUsage, usedMemory, entityCount, chunksLoaded));
        expected.add(new TPS(now - 3000L, r.nextDouble(), r.nextInt(100000000), averageCPUUsage, usedMemory, entityCount, chunksLoaded));
        expected.add(new TPS(now - (690000L * 1000L), r.nextDouble(), r.nextInt(100000000), averageCPUUsage, usedMemory, entityCount, chunksLoaded));
        TPS tooOldTPS = new TPS(now - (691400L * 1000L), r.nextDouble(), r.nextInt(100000000), averageCPUUsage, usedMemory, entityCount, chunksLoaded);

        expected.add(tooOldTPS);
        tpsTable.saveTPSData(expected);
        tpsTable.clean();
        expected.remove(tooOldTPS);
        assertEquals(expected, tpsTable.getTPSData());
    }
}