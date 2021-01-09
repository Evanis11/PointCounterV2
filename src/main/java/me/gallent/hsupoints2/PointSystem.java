package me.gallent.hsupoints2;

import net.minecraft.server.v1_16_R3.NBTTagCompound;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.craftbukkit.v1_16_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class PointSystem {
    public String activeChar = null;
    public PointCounter2 plugin;
    public Player player;

    public ItemStack mPoint = new ItemStack(Material.DIRT);
    ;
    public ItemStack sPoint = new ItemStack(Material.DIRT);
    ;
    public ItemStack gPoint = new ItemStack(Material.DIRT);
    ;
    public ItemMeta mPMeta = mPoint.getItemMeta();
    public ItemMeta sPMeta = sPoint.getItemMeta();
    public ItemMeta gPMeta = gPoint.getItemMeta();
    private Double oldVal;

    public NamespacedKey pointCheck = null;

    public NBTTagCompound mpCompound = null;
    public net.minecraft.server.v1_16_R3.ItemStack nmsMPoint = null;
    public NBTTagCompound spCompound = null;
    public net.minecraft.server.v1_16_R3.ItemStack nmsSPoint = null;
    public NBTTagCompound gpCompound = null;
    public net.minecraft.server.v1_16_R3.ItemStack nmsGPoint = null;

    public Byte nbtTest = (byte) 0;
    private Double spendAmt;
    private String mPPath;
    private String gPPath;
    private String sPPath;
    private Integer gPointsUnspent;
    private Integer sPointsUnspent;
    private Integer mPointsUnspent;
    private String pointPath;
    private Double oldPoints;
    private Double oldSpendAmt;
    private String parentPath;
    private Double parentPoints;
    private String unlockPath;
    private Player target;

    public PointSystem(PointCounter2 plugin) {
        this.plugin = plugin;
    }

    public void createPoints() {
        ConfigurationSection section1 = plugin.getConfig().getConfigurationSection("systemv2");
        section1.getKeys(true).forEach(key -> {
            if (PCFunctions.isInt(section1.getString(key))) {
                int intValue = section1.getInt(key);
                plugin.getPData().set("characters." + plugin.charName + "." + key, intValue);
                return;
            }
            if (section1.isBoolean(key)) {
                plugin.getPData().set("characters." + plugin.charName + "." + key, section1.getBoolean(key));
                return;
            }

            String stringValue = section1.getString(key);

            plugin.getPData().set("characters." + plugin.charName + "." + key, stringValue);

        });
        plugin.getPData().set("choosing", true);
        plugin.createAversions();
        plugin.player.openInventory(plugin.aversions);
    }


    public void bank() {
        this.player = plugin.player;
        if (!(PCFunctions.isInt(plugin.varA))) {
            player.sendMessage(ChatColor.RED + plugin.varA + " is not a number.");
            return;
        }
        Integer bankAmt = Integer.valueOf(plugin.varA);
        this.activeChar = plugin.getPData().getString("ActiveCharacter");

        pointItems();
        mPoint.setAmount(bankAmt);
        sPoint.setAmount(bankAmt);
        gPoint.setAmount(bankAmt);


        //bank magic
        if (player.getInventory().getItemInMainHand().isSimilar(mPoint) && player.getInventory().getItemInMainHand().getAmount() >= bankAmt) {
            if (!checkM()) {
                player.sendMessage("Check failed. Not a point.");
                return;
            }

            player.getInventory().removeItem(mPoint);
            this.oldVal = plugin.getPData().getDouble("characters." + activeChar + ".points.unspent.magic");
            Double newVal = oldVal + bankAmt;
            plugin.getPData().set("characters." + activeChar + ".points.unspent.magic", newVal);
            player.sendMessage((bankAmt == 1) ? ChatColor.GOLD + "You have banked one magic point." : ChatColor.GOLD + "You have banked " + bankAmt + " magic points.");
            plugin.savePData();
            return;
        }

        if (player.getInventory().getItemInMainHand().isSimilar(sPoint) && player.getInventory().getItemInMainHand().getAmount() >= bankAmt) {
            player.getInventory().removeItem(sPoint);
            this.oldVal = plugin.getPData().getDouble("characters." + activeChar + ".points.unspent.physical");
            Double newVal = oldVal + bankAmt;
            plugin.getPData().set("characters." + activeChar + ".points.unspent.physical", newVal);
            player.sendMessage((bankAmt == 1) ? ChatColor.GOLD + "You have banked one stat point." : ChatColor.GOLD + "You have banked " + bankAmt + " stat points.");
            plugin.savePData();
            return;
        }

        if (player.getInventory().getItemInMainHand().isSimilar(gPoint)) {
            if (player.getInventory().getItemInMainHand().getAmount() < bankAmt) {
                player.sendMessage(ChatColor.RED + "Not enough points.");
                return;
            }
            Integer handitem = player.getInventory().getItemInMainHand().getAmount();
            player.getInventory().getItemInMainHand().setAmount(handitem - bankAmt);
            this.oldVal = plugin.getPData().getDouble("characters." + activeChar + ".points.unspent.general");
            Double newVal = oldVal + bankAmt;
            plugin.getPData().set("characters." + activeChar + ".points.unspent.general", newVal);
            player.sendMessage((bankAmt == 1) ? ChatColor.GOLD + "You have banked one general point." : ChatColor.GOLD + "You have banked " + bankAmt + " general points.");
            plugin.savePData();
            return;
        }

        player.sendMessage(ChatColor.RED + "You are not holding enough valid points in your hand.");

        return;
    }

    //point items
    public void pointItems() {
        player = plugin.player;
        PointCounter2.instance.reloadConfig();
        if (!(PointCounter2.instance.getConfig().contains("pointItem.magic"))) {
            player.sendMessage(ChatColor.RED + "No magic point found in Config");
            return;
        }
        String mPItem = PointCounter2.instance.getConfig().getString("pointItem.magic.type");


        try {
            this.mPoint.setType(Material.matchMaterial(mPItem, false));
        } catch (Exception e) {
            this.mPoint.setType(Material.QUARTZ);
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + mPItem + " not found.");
        }
        this.mPMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', PointCounter2.instance.getConfig().getString("pointItem.magic.name")));
        List<String> mPLore = new ArrayList<String>();
        for (String msg : PointCounter2.instance.getConfig().getStringList("pointItem.magic.lore")) {
            mPLore.add(ChatColor.translateAlternateColorCodes('&', msg));
        }

        this.mPMeta.setLore(mPLore);


        //physical
        if (!(PointCounter2.instance.getConfig().contains("pointItem.physical"))) {
            player.sendMessage(ChatColor.RED + "No physical point found in Config");
            return;
        }

        String sPItem = PointCounter2.instance.getConfig().getString("pointItem.physical.type");

        try {
            this.sPoint.setType(Material.matchMaterial(sPItem, false));
        } catch (Exception e) {
            this.sPoint.setType(Material.QUARTZ);
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + sPItem + " not found.");
        }
        this.sPMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', PointCounter2.instance.getConfig().getString("pointItem.physical.name")));
        List<String> sPLore = new ArrayList<String>();
        for (String msg : PointCounter2.instance.getConfig().getStringList("pointItem.physical.lore")) {
            sPLore.add(ChatColor.translateAlternateColorCodes('&', msg));
        }
        this.sPMeta.setLore(sPLore);


        //general

        if (!(PointCounter2.instance.getConfig().contains("pointItem.general"))) {
            player.sendMessage(ChatColor.RED + "No general point found in Config");
            return;
        }
        String gPItem = PointCounter2.instance.getConfig().getString("pointItem.general.type");

        try {
            this.gPoint.setType(Material.matchMaterial(gPItem, false));
        } catch (Exception e) {
            this.gPoint.setType(Material.ENDER_PEARL);
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + gPItem + " not found.");
        }
        this.gPMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', PointCounter2.instance.getConfig().getString("pointItem.general.name")));
        List<String> gPLore = new ArrayList<String>();
        for (String msg : PointCounter2.instance.getConfig().getStringList("pointItem.general.lore")) {
            gPLore.add(ChatColor.translateAlternateColorCodes('&', msg));
        }

        this.gPMeta.setLore(gPLore);


        this.mPoint.setItemMeta(this.mPMeta);
        this.sPoint.setItemMeta(this.sPMeta);
        this.gPoint.setItemMeta(this.gPMeta);

        this.nmsMPoint = CraftItemStack.asNMSCopy(mPoint);
        this.nmsSPoint = CraftItemStack.asNMSCopy(sPoint);
        this.nmsGPoint = CraftItemStack.asNMSCopy(gPoint);

        this.mpCompound = (nmsMPoint.hasTag()) ? nmsMPoint.getTag() : new NBTTagCompound();
        this.spCompound = (nmsSPoint.hasTag()) ? nmsSPoint.getTag() : new NBTTagCompound();
        this.gpCompound = (nmsGPoint.hasTag()) ? nmsGPoint.getTag() : new NBTTagCompound();
        this.mpCompound.setByte("isPoint", (byte) 1);
        this.spCompound.setByte("isPoint", (byte) 1);
        this.gpCompound.setByte("isPoint", (byte) 1);
        nmsMPoint.setTag(mpCompound);
        nmsSPoint.setTag(spCompound);
        nmsGPoint.setTag(gpCompound);
        mPoint = CraftItemStack.asBukkitCopy(nmsMPoint);
        sPoint = CraftItemStack.asBukkitCopy(nmsSPoint);
        gPoint = CraftItemStack.asBukkitCopy(nmsGPoint);


        return;
    }


    public boolean checkM() {
        this.nbtTest = mpCompound.getByte("isPoint");
        this.nmsMPoint = CraftItemStack.asNMSCopy(mPoint);
        if (this.nmsMPoint.hasTag()) {
            Bukkit.getConsoleSender().sendMessage("Tag Detected");
        }
        if (this.nbtTest == (byte) 1) {
            return true;
        }
        return false;
    }

    public boolean checkS() {
        this.nbtTest = spCompound.getByte("isPoint");
        this.nmsSPoint = CraftItemStack.asNMSCopy(sPoint);
        if (this.nmsSPoint.hasTag()) {
            Bukkit.getConsoleSender().sendMessage("Tag Detected");
        }
        if (this.nbtTest == (byte) 1) {
            return true;
        }
        return false;
    }

    public boolean checkG() {
        this.nbtTest = gpCompound.getByte("isPoint");
        this.nmsGPoint = CraftItemStack.asNMSCopy(mPoint);
        if (this.nmsGPoint.hasTag()) {
            Bukkit.getConsoleSender().sendMessage("Tag Detected");
        }
        if (this.nbtTest == (byte) 1) {
            return true;
        }
        return false;
    }

    //spending
    public void spend() {
        this.player = plugin.player;
        plugin.reloadPData();
        this.activeChar = plugin.getPData().getString("ActiveCharacter");
        if (!(PCFunctions.isInt(plugin.varA))) {
            player.sendMessage(ChatColor.RED + plugin.varA + " is not a number.");
            return;
        }
        this.spendAmt = Double.valueOf(plugin.varA);
        this.mPPath = "characters." + activeChar + ".points.unspent.magic";
        this.gPPath = "characters." + activeChar + ".points.unspent.general";
        this.sPPath = "characters." + activeChar + ".points.unspent.physical";
        this.gPointsUnspent = plugin.getPData().getInt(gPPath);
        this.mPointsUnspent = plugin.getPData().getInt(mPPath);
        this.sPointsUnspent = plugin.getPData().getInt(sPPath);
        switch (plugin.varB.toLowerCase()) {
            //MAGIC
            //Tier 1
            //elemental

            case "elemental":
                this.pointPath = "characters." + activeChar + ".points.magic.elemental.base";
                this.oldPoints = plugin.getPData().getDouble(pointPath);
                if (oldPoints >= 50.0) {
                    player.sendMessage(ChatColor.RED + "You cannot spend in this magic: You have reached tier 2");
                    return;
                }
                if (Double.sum(spendAmt, oldPoints) > 50.0) {
                    this.oldSpendAmt = Double.sum(spendAmt, oldPoints) - 50.0;
                    this.spendAmt = 50.0;
                    player.sendMessage("Could not spend " + oldSpendAmt + " points: cannot have more than 50 in a tier 1 magic.");
                }
                magicSpend(1);
                break;

            //cosmic/cosmos
            case "cosmic":
            case "cosmos":
                this.pointPath = "characters." + activeChar + ".points.magic.cosmic.base";
                this.oldPoints = plugin.getPData().getDouble(pointPath);

                Bukkit.getConsoleSender().sendMessage(pointPath);
                if (oldPoints >= 50.0) {
                    player.sendMessage(ChatColor.RED + "You cannot spend in this magic: You have reached tier 2");
                    return;
                }
                if (Double.sum(spendAmt, oldPoints) > 50.0) {
                    this.oldSpendAmt = Double.sum(spendAmt, oldPoints) - 50.0;
                    this.spendAmt = 50.0;
                    player.sendMessage("Could not spend " + oldSpendAmt + " points: cannot have more than 50 in a tier 1 magic.");
                }
                magicSpend(1);
                break;

            //nature
            case "nature":
                this.pointPath = "characters." + activeChar + ".points.magic.nature.base";
                this.oldPoints = plugin.getPData().getDouble(pointPath);

                Bukkit.getConsoleSender().sendMessage(pointPath);
                if (oldPoints >= 50.0) {
                    player.sendMessage(ChatColor.RED + "You cannot spend in this magic: You have reached tier 2");
                    return;
                }
                if (Double.sum(spendAmt, oldPoints) > 50.0) {
                    this.oldSpendAmt = Double.sum(spendAmt, oldPoints) - 50.0;
                    this.spendAmt = 50.0;
                    player.sendMessage("Could not spend " + oldSpendAmt + " points: cannot have more than 50 in a tier 1 magic.");
                }
                magicSpend(1);
                break;

            //tier 2
            //Elemental
            //water
            case "water":
            case "hydromancy":

                this.parentPath = "characters." + activeChar + ".points.magic.elemental.base";

                this.pointPath = "characters." + activeChar + ".points.magic.elemental.water";
                this.oldPoints = plugin.getPData().getDouble(pointPath + ".points");
                this.parentPoints = plugin.getPData().getDouble(parentPath);
                if (parentPoints < 50) {
                    player.sendMessage(ChatColor.RED + "You cannot spend in this magic: You have not reached tier 2");
                    return;
                }
                if (!plugin.getPData().getBoolean(pointPath + ".unlocked")) {
                    player.sendMessage(ChatColor.RED + "You cannot spend in this magic: You have not unlocked this magic.");
                    return;
                }
                magicSpend(2);
                break;
            //earth
            case "earth":
            case "geomancy":
                this.parentPath = "characters." + activeChar + ".points.magic.elemental.base";

                this.pointPath = "characters." + activeChar + ".points.magic.elemental.earth";
                this.oldPoints = plugin.getPData().getDouble(pointPath + ".points");
                this.parentPoints = plugin.getPData().getDouble(parentPath);
                if (parentPoints < 50) {
                    player.sendMessage(ChatColor.RED + "You cannot spend in this magic: You have not reached tier 2");
                    return;
                }
                if (!plugin.getPData().getBoolean(pointPath + ".unlocked")) {
                    player.sendMessage(ChatColor.RED + "You cannot spend in this magic: You have not unlocked this magic.");
                    return;
                }

                magicSpend(2);
                break;
            //fire
            case "fire":
            case "pyromancy":
                this.parentPath = "characters." + activeChar + ".points.magic.elemental.base";

                this.pointPath = "characters." + activeChar + ".points.magic.elemental.fire";
                this.oldPoints = plugin.getPData().getDouble(pointPath + ".points");
                this.parentPoints = plugin.getPData().getDouble(parentPath);
                if (parentPoints < 50) {
                    player.sendMessage(ChatColor.RED + "You cannot spend in this magic: You have not reached tier 2");
                    return;
                }
                if (!plugin.getPData().getBoolean(pointPath + ".unlocked")) {
                    player.sendMessage(ChatColor.RED + "You cannot spend in this magic: You have not unlocked this magic.");
                    return;
                }
                magicSpend(2);
                break;
            //air
            case "air":
            case "aeromancy":
            case "wind":
                this.parentPath = "characters." + activeChar + ".points.magic.elemental.base";

                this.pointPath = "characters." + activeChar + ".points.magic.elemental.wind";
                this.oldPoints = plugin.getPData().getDouble(pointPath + ".points");
                this.parentPoints = plugin.getPData().getDouble(parentPath);
                if (parentPoints < 50) {
                    player.sendMessage(ChatColor.RED + "You cannot spend in this magic: You have not reached tier 2");
                    return;
                }
                if (!plugin.getPData().getBoolean(pointPath + ".unlocked")) {
                    player.sendMessage(ChatColor.RED + "You cannot spend in this magic: You have not unlocked this magic.");
                    return;
                }
                magicSpend(2);
                break;


            //cosmic
            case "astral":
            case "lunimancy":
            case "celestial":

                this.pointPath = "characters." + activeChar + ".points.magic.cosmic.astral";
                this.oldPoints = plugin.getPData().getDouble(pointPath + ".points");
                this.parentPoints = plugin.getPData().getDouble(parentPath);
                if (parentPoints < 50) {
                    player.sendMessage(ChatColor.RED + "You cannot spend in this magic: You have not reached tier 2");
                    return;
                }
                if (!plugin.getPData().getBoolean(pointPath + ".unlocked")) {
                    player.sendMessage(ChatColor.RED + "You cannot spend in this magic: You have not unlocked this magic.");
                    return;
                }
                magicSpend(2);

                break;
            case "shadow":
            case "umbramancy":
            case "void":

                this.pointPath = "characters." + activeChar + ".points.magic.cosmic.shadow";
                this.oldPoints = plugin.getPData().getDouble(pointPath + ".points");
                this.parentPoints = plugin.getPData().getDouble(parentPath);
                if (parentPoints < 50) {
                    player.sendMessage(ChatColor.RED + "You cannot spend in this magic: You have not reached tier 2");
                    return;
                }
                if (!plugin.getPData().getBoolean(pointPath + ".unlocked")) {
                    player.sendMessage(ChatColor.RED + "You cannot spend in this magic: You have not unlocked this magic.");
                    return;
                }
                magicSpend(2);
                break;

            //nature
            case "life":
            case "vivimancy":
            case "healing":
                this.parentPath = "characters." + activeChar + ".points.magic.nature.base";

                this.pointPath = "characters." + activeChar + ".points.magic.nature.life";
                this.oldPoints = plugin.getPData().getDouble(pointPath + ".points");
                this.parentPoints = plugin.getPData().getDouble(parentPath);
                if (parentPoints < 50) {
                    player.sendMessage(ChatColor.RED + "You cannot spend in this magic: You have not reached tier 2");
                    return;
                }
                if (!plugin.getPData().getBoolean(pointPath + ".unlocked")) {
                    player.sendMessage(ChatColor.RED + "You cannot spend in this magic: You have not unlocked this magic.");
                    return;
                }
                magicSpend(2);
                break;
            case "ecomancy":
            case "plants":
            case "animals":
                this.parentPath = "characters." + activeChar + ".points.magic.nature.base";

                this.pointPath = "characters." + activeChar + ".points.magic.nature.ecomancy";
                this.oldPoints = plugin.getPData().getDouble(pointPath + ".points");
                this.parentPoints = plugin.getPData().getDouble(parentPath);
                if (parentPoints < 50) {
                    player.sendMessage(ChatColor.RED + "You cannot spend in this magic: You have not reached tier 2");
                    return;
                }
                if (!plugin.getPData().getBoolean(pointPath + ".unlocked")) {
                    player.sendMessage(ChatColor.RED + "You cannot spend in this magic: You have not unlocked this magic.");
                    return;
                }
                magicSpend(2);
                break;

            case "death":
            case "necromancy":
                this.parentPath = "characters." + activeChar + ".points.magic.nature.base";

                this.pointPath = "characters." + activeChar + ".points.magic.nature.death";
                this.oldPoints = plugin.getPData().getDouble(pointPath + ".points");
                this.parentPoints = plugin.getPData().getDouble(parentPath);
                if (parentPoints < 50) {
                    player.sendMessage(ChatColor.RED + "You cannot spend in this magic: You have not reached tier 2");
                    return;
                }
                if (!plugin.getPData().getBoolean(pointPath + ".unlocked")) {
                    player.sendMessage(ChatColor.RED + "You cannot spend in this magic: You have not unlocked this magic.");
                    return;
                }
                magicSpend(2);

                break;

            //PHYSICAL
            case "health":
                this.pointPath = "characters." + activeChar + ".points.physical.health";
                this.oldPoints = plugin.getPData().getDouble(pointPath);
                statSpend();
                break;
            case "strength":
                this.pointPath = "characters." + activeChar + ".points.physical.strength";
                this.oldPoints = plugin.getPData().getDouble(pointPath);
                statSpend();
                break;
            case "agility":
                this.pointPath = "characters." + activeChar + ".points.physical.agility";
                this.oldPoints = plugin.getPData().getDouble(pointPath);
                statSpend();
                break;
            case "craftsmanship":
            case "craft":
                this.pointPath = "characters." + activeChar + ".points.physical.craftsmanship";
                this.oldPoints = plugin.getPData().getDouble(pointPath);
                statSpend();
                break;


        }

    }

    public void magicSpend(int tier) {

        if (spendAmt <= Integer.sum(mPointsUnspent, gPointsUnspent)) {
            double spendTrue = spendAmt;
            if (plugin.getPData().getBoolean(pointPath + ".aversion")) {
                spendTrue = (Double) spendAmt / 2;
                player.sendMessage(ChatColor.RED + "WARNING: You have an aversion to this magic. You will only recieve " + spendTrue + " points in the magic.");
            }
            if (tier == 2) {
                if (spendAmt > mPointsUnspent) {

                    plugin.getPData().set(pointPath + ".points", oldPoints + spendTrue);
                    spendAmt = spendAmt - mPointsUnspent;
                    gPointsUnspent = (int) (gPointsUnspent - spendAmt);
                    plugin.player.sendMessage(ChatColor.GOLD + "You spend " + mPointsUnspent.toString() + " magic points and " + spendAmt + " general points.");
                    plugin.getPData().set(mPPath, 0);
                    plugin.getPData().set(gPPath, gPointsUnspent);
                    plugin.savePData();
                    return;
                }

                plugin.getPData().set(pointPath + ".points", oldPoints + spendTrue);
                mPointsUnspent = (int) (mPointsUnspent - spendAmt);
                plugin.player.sendMessage(ChatColor.GOLD + "You spend " + spendAmt + " magic points.");
                plugin.getPData().set(mPPath, mPointsUnspent);
                plugin.savePData();
                return;
            }
            if (spendAmt > mPointsUnspent) {

                plugin.getPData().set(pointPath, oldPoints + spendTrue);
                spendAmt = spendAmt - mPointsUnspent;
                gPointsUnspent = (int) (gPointsUnspent - spendAmt);
                plugin.player.sendMessage(ChatColor.GOLD + "You spend " + mPointsUnspent.toString() + " magic points and " + spendAmt + " general points.");
                plugin.getPData().set(mPPath, 0);
                plugin.getPData().set(gPPath, gPointsUnspent);
                plugin.savePData();
                return;
            }

            plugin.getPData().set(pointPath, oldPoints + spendTrue);
            mPointsUnspent = (int) (mPointsUnspent - spendAmt);
            plugin.player.sendMessage(ChatColor.GOLD + "You spend " + spendAmt + " magic points.");
            plugin.getPData().set(mPPath, mPointsUnspent);
            plugin.savePData();
            return;
        }
        plugin.player.sendMessage(ChatColor.RED + "You do not have enough points.");
        return;
    }

    public void statSpend() {
        if (spendAmt <= Integer.sum(sPointsUnspent, gPointsUnspent)) {
            if (spendAmt > sPointsUnspent) {
                plugin.getPData().set(pointPath, oldPoints + spendAmt);
                spendAmt = spendAmt - sPointsUnspent;
                gPointsUnspent = (int) (gPointsUnspent - spendAmt);
                plugin.player.sendMessage(ChatColor.GOLD + "You spend " + sPointsUnspent.toString() + " physical stat points and " + spendAmt + " general points.");
                plugin.getPData().set(sPPath, 0);
                plugin.getPData().set(gPPath, gPointsUnspent);
                plugin.savePData();
                return;
            }

            plugin.getPData().set(pointPath, oldPoints + spendAmt);
            sPointsUnspent = (int) (sPointsUnspent - spendAmt);
            plugin.player.sendMessage(ChatColor.GOLD + "You spend " + spendAmt + " physical stat points.");
            plugin.getPData().set(sPPath, sPointsUnspent);
            plugin.savePData();
            return;
        }
        plugin.player.sendMessage(ChatColor.RED + "You do not have enough points.");
        return;
    }


    public void view() {
        this.player = plugin.player;
        plugin.reloadPData();
        this.activeChar = plugin.getPData().getString("ActiveCharacter");
        this.mPPath = "characters." + activeChar + ".points.unspent.magic";
        this.gPPath = "characters." + activeChar + ".points.unspent.general";
        this.sPPath = "characters." + activeChar + ".points.unspent.physical";
        this.gPointsUnspent = plugin.getPData().getInt(gPPath);
        this.mPointsUnspent = plugin.getPData().getInt(mPPath);
        this.sPointsUnspent = plugin.getPData().getInt(sPPath);
        int pointAmt = 0;
        switch (plugin.varB.toLowerCase()) {
            //MAGIC
            //Tier 1
            //elemental
            case "elemental":
                pointAmt = plugin.getPData().getInt("characters." + activeChar + ".points.magic.elemental.base");
                player.sendMessage(ChatColor.GOLD + "You have " + pointAmt + " points in Tier 1 elemental magic.");
                return;

            //cosmic
            case "cosmic":
                pointAmt = plugin.getPData().getInt("characters." + activeChar + ".points.magic.cosmic.base");
                player.sendMessage(ChatColor.BLUE + "You have " + pointAmt + " points in Tier 1 Cosmos magic.");
                return;

            //nature
            case "nature":
                pointAmt = plugin.getPData().getInt("characters." + activeChar + ".points.magic.nature.base");
                player.sendMessage(ChatColor.GREEN + "You have " + pointAmt + " points in Tier 1 nature magic.");
                return;

            //tier 2
            //Elemental
            //water
            case "water":
            case "hydromancy":
                this.parentPath = "characters." + activeChar + ".points.magic.elemental.base";
                this.pointPath = "characters." + activeChar + ".points.magic.elemental.water";
                this.unlockPath = pointPath + ".unlocked";
                viewG("Elemental", "Hydromancy");
                return;
            //earth
            case "earth":
            case "geomancy":
                this.parentPath = "characters." + activeChar + ".points.magic.elemental.base";
                this.pointPath = "characters." + activeChar + ".points.magic.elemental.earth";
                this.unlockPath = pointPath + ".unlocked";
                viewG("Elemental", "Geomancy");
                return;
            //fire
            case "fire":
            case "pyromancy":
                this.parentPath = "characters." + activeChar + ".points.magic.elemental.base";
                this.pointPath = "characters." + activeChar + ".points.magic.elemental.fire";
                this.unlockPath = pointPath + ".unlocked";
                viewG("Elemental", "Pyromancy");
                return;
            //air
            case "air":
            case "aeromancy":
            case "wind":
                this.parentPath = "characters." + activeChar + ".points.magic.elemental.base";
                this.pointPath = "characters." + activeChar + ".points.magic.elemental.wind";
                this.unlockPath = pointPath + ".unlocked";
                viewG("Elemental", "Aeromancy");
                return;

            //cosmic
            case "astral":
            case "lunimancy":
            case "celestial":
                this.parentPath = "characters." + activeChar + ".points.magic.cosmic.base";
                this.pointPath = "characters." + activeChar + ".points.magic.cosmic.astral";
                this.unlockPath = pointPath + ".unlocked";
                viewG("Cosmos", "Lunimancy");
                return;
            case "shadow":
            case "umbramancy":
            case "void":
                this.parentPath = "characters." + activeChar + ".points.magic.cosmic.base";
                this.pointPath = "characters." + activeChar + ".points.magic.cosmic.shadow";
                this.unlockPath = pointPath + ".unlocked";
                viewG("Cosmos", "Umbramancy");
                return;

            //nature
            case "life":
            case "vivimancy":
            case "healing":
                this.parentPath = "characters." + activeChar + ".points.magic.nature.base";
                this.pointPath = "characters." + activeChar + ".points.magic.nature.life";
                this.unlockPath = pointPath + ".unlocked";
                viewG("Nature", "Vivimancy");
                return;

            case "ecomancy":
            case "plants":
            case "animals":
                this.parentPath = "characters." + activeChar + ".points.magic.nature.base";
                this.pointPath = "characters." + activeChar + ".points.magic.nature.life";
                this.unlockPath = pointPath + ".unlocked";
                viewG("Nature", "Ecomancy");
                return;

            case "death":
            case "necromancy":
                this.parentPath = "characters." + activeChar + ".points.magic.nature.base";
                this.pointPath = "characters." + activeChar + ".points.magic.nature.life";
                this.unlockPath = pointPath + ".unlocked";
                viewG("Nature", "Necromancy");
                return;

            //PHYSICAL
            case "health":
                this.pointPath = "characters." + activeChar + ".points.physical.health";
                pointAmt = plugin.getPData().getInt(pointPath);
                player.sendMessage(ChatColor.GOLD + "You have " + pointAmt + " health points");
                return;

            case "strength":
                this.pointPath = "characters." + activeChar + ".points.physical.strength";
                pointAmt = plugin.getPData().getInt(pointPath);
                player.sendMessage(ChatColor.GOLD + "You have " + pointAmt + " strength points");
                return;

            case "agility":
                this.pointPath = "characters." + activeChar + ".points.physical.agility";
                pointAmt = plugin.getPData().getInt(pointPath);
                player.sendMessage(ChatColor.GOLD + "You have " + pointAmt + " agility points");
                return;

            case "craftsmanship":
            case "craft":
                this.pointPath = "characters." + activeChar + ".points.physical.craftsmanship";
                pointAmt = plugin.getPData().getInt(pointPath);
                player.sendMessage(ChatColor.GOLD + "You have " + pointAmt + " craftsmanship points");
                return;

            case "unspent":
                player.sendMessage(ChatColor.GOLD + "You have " + mPointsUnspent + " unspent magic points");
                player.sendMessage(ChatColor.GOLD + "You have " + sPointsUnspent + " unspent stat points");
                player.sendMessage(ChatColor.GOLD + "You have " + gPointsUnspent + " unspent general points");
                return;

            case "magic":
                player.sendMessage(ChatColor.GOLD + "You have " + mPointsUnspent + " unspent magic points");
                return;

            case "stat":
            case "physical":
                player.sendMessage(ChatColor.GOLD + "You have " + sPointsUnspent + " unspent stat points");
                return;

            case "general":
                player.sendMessage(ChatColor.GOLD + "You have " + gPointsUnspent + " unspent general points");
                return;
        }

    }

    public void viewG(String parentSchool, String magicName) {
        if (plugin.getPData().getInt(parentPath) < 50) {
            player.sendMessage(ChatColor.RED + "You have not unlocked this magic. Must have 50 points in " + parentSchool + " magic.");
            return;
        }
        if (!plugin.getPData().getBoolean(unlockPath)) {
            player.sendMessage(ChatColor.RED + "You have not unlocked this magic.");
            return;
        }

        Double schoolPoints = plugin.getPData().getDouble(pointPath + ".points");
        Double parentPoints = plugin.getPData().getDouble(parentPath);
        Double pointAmt = Double.sum(parentPoints, schoolPoints);
        player.sendMessage(ChatColor.GOLD + "You have " + pointAmt.intValue() + " (" + pointAmt + ") points in " + magicName + ".");
        if (plugin.getPData().getBoolean(pointPath + ".aversion")) {
            player.sendMessage(ChatColor.RED + "You have an aversion in this magic.");
        }
        return;
    }


    public void view2() {
        this.target = plugin.target;
        plugin.reloadPData();
        plugin.reloadTarData();
        this.activeChar = plugin.getTarData().getString("ActiveCharacter");
        this.mPPath = "characters." + activeChar + ".points.unspent.magic";
        this.gPPath = "characters." + activeChar + ".points.unspent.general";
        this.sPPath = "characters." + activeChar + ".points.unspent.physical";
        this.gPointsUnspent = plugin.getTarData().getInt(gPPath);
        this.mPointsUnspent = plugin.getTarData().getInt(mPPath);
        this.sPointsUnspent = plugin.getTarData().getInt(sPPath);
        int pointAmt = 0;

        switch (plugin.varB.toLowerCase()) {
            case "elemental":
                pointAmt = plugin.getTarData().getInt("characters." + activeChar + ".points.magic.elemental.base");
                plugin.sendB.sendMessage(ChatColor.GOLD + "" + target.getName() + " has " + pointAmt + " points in Tier 1 Elemental magic.");
                return;

            //cosmic
            case "cosmic":
            case "cosmos":
                pointAmt = plugin.getTarData().getInt("characters." + activeChar + ".points.magic.cosmic.base");
                plugin.sendB.sendMessage(ChatColor.BLUE + "" + target.getName() + " has " + pointAmt + " points in Tier 1 Cosmos magic.");
                return;

            //nature
            case "nature":
                pointAmt = plugin.getTarData().getInt("characters." + activeChar + ".points.magic.nature.base");
                plugin.sendB.sendMessage(ChatColor.GREEN + "" + target.getName() + " has " + pointAmt + " points in Tier 1 Nature magic.");
                return;

            //tier 2
            //Elemental
            //water
            case "water":
            case "hydromancy":
                this.parentPath = "characters." + activeChar + ".points.magic.elemental.base";
                this.pointPath = "characters." + activeChar + ".points.magic.elemental.water";
                this.unlockPath = pointPath + ".unlocked";
                viewG2("Hydromancy");
                return;

            //earth
            case "earth":
            case "geomancy":
                this.parentPath = "characters." + activeChar + ".points.magic.elemental.base";
                this.pointPath = "characters." + activeChar + ".points.magic.elemental.earth";
                this.unlockPath = pointPath + ".unlocked";
                viewG2("Geomancy");
                return;

            //fire
            case "fire":
            case "pyromancy":
                this.parentPath = "characters." + activeChar + ".points.magic.elemental.base";
                this.pointPath = "characters." + activeChar + ".points.magic.elemental.fire";
                this.unlockPath = pointPath + ".unlocked";
                viewG2("Pyromancy");
                return;

            //air
            case "air":
            case "aeromancy":
            case "wind":
                this.parentPath = "characters." + activeChar + ".points.magic.elemental.base";
                this.pointPath = "characters." + activeChar + ".points.magic.elemental.wind";
                this.unlockPath = pointPath + ".unlocked";
                viewG2("Geomancy");
                return;


            //cosmic
            case "astral":
            case "lunimancy":
            case "celestial":
                this.parentPath = "characters." + activeChar + ".points.magic.cosmic.base";
                this.pointPath = "characters." + activeChar + ".points.magic.cosmic.astral";
                this.unlockPath = pointPath + ".unlocked";
                viewG2("Lunimancy");
                return;

            case "shadow":
            case "umbramancy":
            case "void":
                this.parentPath = "characters." + activeChar + ".points.magic.cosmic.base";
                this.pointPath = "characters." + activeChar + ".points.magic.cosmic.shadow";
                this.unlockPath = pointPath + ".unlocked";
                viewG2("Umbramancy");
                return;

            //nature
            case "life":
            case "vivimancy":
            case "healing":
                this.parentPath = "characters." + activeChar + ".points.magic.nature.base";
                this.pointPath = "characters." + activeChar + ".points.magic.nature.life";
                this.unlockPath = pointPath + ".unlocked";
                viewG2("Vivimancy");
                return;

            case "ecomancy":
            case "plants":
            case "animals":
                this.parentPath = "characters." + activeChar + ".points.magic.nature.base";
                this.pointPath = "characters." + activeChar + ".points.magic.nature.ecomancy";
                this.unlockPath = pointPath + ".unlocked";
                viewG2("Ecomancy");
                return;

            case "death":
            case "necromancy":
                this.parentPath = "characters." + activeChar + ".points.magic.nature.base";
                this.pointPath = "characters." + activeChar + ".points.magic.nature.death";
                this.unlockPath = pointPath + ".unlocked";
                viewG2("Necromancy");
                return;

            //PHYSICAL
            case "health":
                this.pointPath = "characters." + activeChar + ".points.physical.health";
                pointAmt = plugin.getTarData().getInt(pointPath);
                plugin.sendB.sendMessage(ChatColor.GOLD + "" + target.getName() + " has " + pointAmt + " health points");
                return;

            case "strength":
                this.pointPath = "characters." + activeChar + ".points.physical.strength";
                pointAmt = plugin.getTarData().getInt(pointPath);
                plugin.sendB.sendMessage(ChatColor.GOLD + "" + target.getName() + " has " + pointAmt + " strength points");
                return;

            case "agility":
                this.pointPath = "characters." + activeChar + ".points.physical.agility";
                pointAmt = plugin.getTarData().getInt(pointPath);
                plugin.sendB.sendMessage(ChatColor.GOLD + "" + target.getName() + " has " + pointAmt + " agility points");
                return;

            case "craftsmanship":
            case "craft":
                this.pointPath = "characters." + activeChar + ".points.physical.craftsmanship";
                pointAmt = plugin.getTarData().getInt(pointPath);
                plugin.sendB.sendMessage(ChatColor.GOLD + "" + target.getName() + " has " + pointAmt + " craftsmanship points");
                return;

            case "unspent":
                plugin.sendB.sendMessage(ChatColor.GOLD + "" + target.getName() + " has " + mPointsUnspent + " unspent magic points");
                plugin.sendB.sendMessage(ChatColor.GOLD + "" + target.getName() + " has " + sPointsUnspent + " unspent stat points");
                plugin.sendB.sendMessage(ChatColor.GOLD + "" + target.getName() + " has " + gPointsUnspent + " unspent general points");
                return;

            case "magic":
                plugin.sendB.sendMessage(ChatColor.GOLD + "" + target.getName() + " has " + mPointsUnspent + " unspent magic points");
                return;

            case "stat":
            case "physical":
                plugin.sendB.sendMessage(ChatColor.GOLD + "" + target.getName() + " has " + sPointsUnspent + " unspent stat points");
                return;

            case "general":
                plugin.sendB.sendMessage(ChatColor.GOLD + "" + target.getName() + " has " + gPointsUnspent + " unspent general points");
                return;
            default:
                plugin.sendB.sendMessage(ChatColor.RED + "Unknown Point Type");
        }

    }

    public void viewG2(String magicName) {
        if (!plugin.getTarData().getBoolean(unlockPath)) {
            plugin.sendB.sendMessage(ChatColor.RED + "" + target.getName() + " has not unlocked this magic.");
            return;
        }

        int pointAmt = plugin.getTarData().getInt(pointPath + ".points");
        plugin.sendB.sendMessage(ChatColor.GOLD + "" + target.getName() + " has " + pointAmt + " points in " + magicName + ".");
        return;
    }


    public void unlock() {
        this.player = plugin.player;
        plugin.reloadPData();
        this.activeChar = plugin.getPData().getString("ActiveCharacter");
        Integer unlockedMagic = plugin.getPData().getInt("characters." + activeChar + ".unlockedMagic");
        Integer maxUnlock = plugin.getPData().getInt("characters." + activeChar + ".maxUnlock");
        if (unlockedMagic >= maxUnlock) {
            Bukkit.getConsoleSender().sendMessage(unlockedMagic.toString());
            Bukkit.getConsoleSender().sendMessage(maxUnlock.toString());
            player.sendMessage(ChatColor.RED + "You cannot unlock any more magic.");
            return;
        }
        switch (plugin.varB.toLowerCase()) {
            //water
            case "water":
            case "hydromancy":
                this.unlockPath = "characters." + activeChar + ".points.magic.elemental.water.unlocked";
                if (plugin.getPData().getDouble("characters." + activeChar + ".points.magic.elemental.base") < 50.0) {
                    player.sendMessage(ChatColor.RED + "You have not reached tier II Elemental magic yet.");
                    return;
                }
                if (plugin.getPData().getBoolean(unlockPath)) {
                    player.sendMessage(ChatColor.RED + "You have already unlocked this magic.");
                    return;
                }
                plugin.getPData().set(unlockPath, true);
                plugin.getPData().set("characters." + activeChar + ".unlockedMagic", unlockedMagic + 1);
                player.sendMessage(ChatColor.GREEN + "Magic unlocked.");
                plugin.savePData();
                return;

            //earth
            case "earth":
            case "geomancy":
                this.unlockPath = "characters." + activeChar + ".points.magic.elemental.earth.unlocked";
                if (plugin.getPData().getDouble("characters." + activeChar + ".points.magic.elemental.base") < 50.0) {
                    player.sendMessage(ChatColor.RED + "You have not reached tier II Elemental magic yet.");
                    return;
                }
                if (plugin.getPData().getBoolean(unlockPath)) {
                    player.sendMessage(ChatColor.RED + "You have already unlocked this magic.");
                    return;
                }
                plugin.getPData().set(unlockPath, true);
                plugin.getPData().set("characters." + activeChar + ".unlockedMagic", unlockedMagic + 1);
                player.sendMessage(ChatColor.GREEN + "Magic unlocked.");
                plugin.savePData();
                return;

            //fire
            case "fire":
            case "pyromancy":
                this.unlockPath = "characters." + activeChar + ".points.magic.elemental.fire.unlocked";
                if (plugin.getPData().getDouble("characters." + activeChar + ".points.magic.elemental.base") < 50.0) {
                    player.sendMessage(ChatColor.RED + "You have not reached tier II Elemental magic yet.");
                    return;
                }
                if (plugin.getPData().getBoolean(unlockPath)) {
                    player.sendMessage(ChatColor.RED + "You have already unlocked this magic.");
                    return;
                }
                plugin.getPData().set(unlockPath, true);
                plugin.getPData().set("characters." + activeChar + ".unlockedMagic", unlockedMagic + 1);
                player.sendMessage(ChatColor.GREEN + "Magic unlocked.");
                plugin.savePData();
                return;

            //air
            case "air":
            case "aeromancy":
            case "wind":
                this.unlockPath = "characters." + activeChar + ".points.magic.elemental.wind.unlocked";
                if (plugin.getPData().getDouble("characters." + activeChar + ".points.magic.elemental.base") < 50.0) {
                    player.sendMessage(ChatColor.RED + "You have not reached tier II Elemental magic yet.");
                    return;
                }
                if (plugin.getPData().getBoolean(unlockPath)) {
                    player.sendMessage(ChatColor.RED + "You have already unlocked this magic.");
                    return;
                }
                plugin.getPData().set(unlockPath, true);
                plugin.getPData().set("characters." + activeChar + ".unlockedMagic", unlockedMagic + 1);
                player.sendMessage(ChatColor.GREEN + "Magic unlocked.");
                plugin.savePData();
                return;

            //cosmic
            case "astral":
            case "lunimancy":
            case "celestial":
                this.unlockPath = "characters." + activeChar + ".points.magic.cosmic.astral.unlocked";
                if (plugin.getPData().getDouble("characters." + activeChar + ".points.magic.cosmic.base") < 50.0) {
                    player.sendMessage(ChatColor.RED + "You have not reached tier II Cosmos magic yet.");
                    return;
                }
                if (plugin.getPData().getBoolean(unlockPath)) {
                    player.sendMessage(ChatColor.RED + "You have already unlocked this magic.");
                    return;
                }
                plugin.getPData().set(unlockPath, true);
                plugin.getPData().set("characters." + activeChar + ".unlockedMagic", unlockedMagic + 1);
                player.sendMessage(ChatColor.GREEN + "Magic unlocked.");
                plugin.savePData();
                return;

            case "shadow":
            case "umbramancy":
            case "void":
                this.unlockPath = "characters." + activeChar + ".points.magic.cosmic.shadow.unlocked";
                if (plugin.getPData().getDouble("characters." + activeChar + ".points.magic.cosmic.base") < 50.0) {
                    player.sendMessage(ChatColor.RED + "You have not reached tier II Cosmos magic yet.");
                    return;
                }
                if (plugin.getPData().getBoolean(unlockPath)) {
                    player.sendMessage(ChatColor.RED + "You have already unlocked this magic.");
                    return;
                }
                plugin.getPData().set(unlockPath, true);
                plugin.getPData().set("characters." + activeChar + ".unlockedMagic", unlockedMagic + 1);
                player.sendMessage(ChatColor.GREEN + "Magic unlocked.");
                plugin.savePData();
                return;

            //nature
            case "life":
            case "vivimancy":
            case "healing":
                this.unlockPath = "characters." + activeChar + ".points.magic.nature.life.unlocked";
                if (plugin.getPData().getDouble("characters." + activeChar + ".points.magic.nature.base") < 50.0) {
                    player.sendMessage(ChatColor.RED + "You have not reached tier II Nature magic yet.");
                    return;
                }
                if (plugin.getPData().getBoolean(unlockPath)) {
                    player.sendMessage(ChatColor.RED + "You have already unlocked this magic.");
                    return;
                }
                plugin.getPData().set(unlockPath, true);
                plugin.getPData().set("characters." + activeChar + ".unlockedMagic", unlockedMagic + 1);
                player.sendMessage(ChatColor.GREEN + "Magic unlocked.");
                plugin.savePData();
                return;

            case "ecomancy":
            case "plants":
            case "animals":
                this.unlockPath = "characters." + activeChar + ".points.magic.nature.ecomancy.unlocked";
                if (plugin.getPData().getDouble("characters." + activeChar + ".points.magic.nature.base") < 50.0) {
                    player.sendMessage(ChatColor.RED + "You have not reached tier II Nature magic yet.");
                    return;
                }
                if (plugin.getPData().getBoolean(unlockPath)) {
                    player.sendMessage(ChatColor.RED + "You have already unlocked this magic.");
                    return;
                }
                plugin.getPData().set(unlockPath, true);
                plugin.getPData().set("characters." + activeChar + ".unlockedMagic", unlockedMagic + 1);
                player.sendMessage(ChatColor.GREEN + "Magic unlocked.");
                plugin.savePData();
                return;

            case "death":
            case "necromancy":
                this.unlockPath = "characters." + activeChar + ".points.magic.nature.death.unlocked";
                if (plugin.getPData().getDouble("characters." + activeChar + ".points.magic.nature.base") < 50.0) {
                    player.sendMessage(ChatColor.RED + "You have not reached tier II Nature magic yet.");
                    return;
                }
                if (plugin.getPData().getBoolean(unlockPath)) {
                    player.sendMessage(ChatColor.RED + "You have already unlocked this magic.");
                    return;
                }
                plugin.getPData().set(unlockPath, true);
                plugin.getPData().set("characters." + activeChar + ".unlockedMagic", unlockedMagic + 1);
                player.sendMessage(ChatColor.GREEN + "Magic unlocked.");
                plugin.savePData();
                return;
        }
    }
}
